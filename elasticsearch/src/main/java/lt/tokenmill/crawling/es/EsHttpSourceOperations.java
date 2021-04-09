package lt.tokenmill.crawling.es;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.PageableList;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static lt.tokenmill.crawling.es.EsDataParser.falseOrBoolean;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


public class EsHttpSourceOperations extends BaseElasticOps {

    private static final Logger LOG = LoggerFactory.getLogger(EsHttpSourceOperations.class);

    protected EsHttpSourceOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES HTTP Sources Operations for index='{}', type='{}'", index, type);
    }

    public static EsHttpSourceOperations getInstance(ElasticConnection connection, String index, String type) {
        return new EsHttpSourceOperations(connection, index, type);
    }

    public List<HttpSource> findEnabledSources() {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("enabled", true));

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .query(filter)
                    .sort("updated", SortOrder.ASC)
                    .size(10000)
                    .fetchSource(true)
                    .explain(false);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .searchType(SearchType.DEFAULT)
                    .source(searchSourceBuilder);
            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest, getRequestOptions());

            SearchHits hits = response.getHits();
            return Arrays.stream(hits.getHits())
                    .map(SearchHit::getSourceAsMap)
                    .map(this::mapToHttpSource)
                    .collect(Collectors.toList());
        } catch (ElasticsearchStatusException e) {
            LOG.error("Failed while searching for enabled sources", e);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Failed while searching for enabled sources", e);
        }
        return Collections.emptyList();
    }

    public HttpSource get(String url) {
        try {
            GetRequest getRequest = new GetRequest(getIndex(), formatId(url))
                    .fetchSourceContext(new FetchSourceContext(true));
            GetResponse response = getConnection().getRestHighLevelClient().get(getRequest, getRequestOptions());
            if (response.isExists()) {
                return mapToHttpSource(response.getSource());
            }
            else {
                LOG.error("No response for HTTP Source url: {}", url);
            }
        } catch (ElasticsearchStatusException e) {
            LOG.error("Failed to fetch HTTP Source for {}", url, e);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Failed to fetch HTTP Source for {}", url, e);
        }
        return null;
    }

    public PageableList<HttpSource> filter(String text) {
        return filter(text, 0);
    }

    public PageableList<HttpSource> filter(String text, int offset) {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery();
            if (!Strings.isNullOrEmpty(text)) {
                filter.should(QueryBuilders.termQuery("url", text));
                filter.should(QueryBuilders.termQuery("name", text));
                filter.should(QueryBuilders
                        .queryStringQuery(QueryParser.escape(text.trim()))
                        .field("search_field")
                        .defaultOperator(Operator.AND));
                filter.should(QueryBuilders
                        .prefixQuery("search_field", QueryParser.escape(text.trim())));
                filter.should(QueryBuilders
                        .prefixQuery("search_field", "www." + QueryParser.escape(text.trim())));
            }

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .query(filter)
                    .size(100)
                    .from(offset)
                    .fetchSource(true)
                    .explain(false);

            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .searchType(SearchType.DEFAULT)
                    .source(searchSourceBuilder);

            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest, getRequestOptions());

            List<HttpSource> items = Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSourceAsMap)
                    .filter(Objects::nonNull)
                    .map(this::mapToHttpSource)
                    .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                    .collect(Collectors.toList());
            return PageableList.create(items, response.getHits().getTotalHits().value);
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageableList<>();
    }

    public List<HttpSource> all() {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery()
                    .must(QueryBuilders.existsQuery("url"))
                    .must(QueryBuilders.existsQuery("name"));

            TimeValue keepAlive = TimeValue.timeValueMinutes(10);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .explain(false)
                    .fetchSource(true)
                    .size(100)
                    .query(filter);

            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .scroll(keepAlive)
                    .source(searchSourceBuilder);

            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest, getRequestOptions());
            List<HttpSource> result = Lists.newArrayList();
            do {
                result.addAll(Arrays.stream(response.getHits().getHits())
                        .map(SearchHit::getSourceAsMap)
                        .filter(Objects::nonNull)
                        .map(this::mapToHttpSource)
                        .collect(Collectors.toList()));

                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(response.getScrollId())
                        .scroll(keepAlive);
                response = getConnection().getRestHighLevelClient().searchScroll(searchScrollRequest, getRequestOptions());
            } while (response.getHits().getHits().length != 0);
            return result;
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void save(HttpSource source) {
        try {
            String url = source.getUrl().toLowerCase();
            XContentBuilder xContentBuilder = jsonBuilder()
                    .startObject()
                    .field("url", url)
                    .field("name", source.getName().trim())
                    .field("language", source.getLanguage() != null ? source.getLanguage().trim() : null)
                    .field("timezone", source.getTimezone() != null ? source.getTimezone().trim() : null)
                    .field("enabled", source.isEnabled())
                    .field("discovery_enabled", source.isDiscoveryEnabled())
                    .field("url_crawl_delay_secs", source.getUrlRecrawlDelayInSecs())
                    .field("feed_crawl_delay_secs", source.getFeedRecrawlDelayInSecs())
                    .field("sitemap_crawl_delay_secs", source.getSitemapRecrawlDelayInSecs())
                    .field("urls", Utils.listToText(source.getUrls()))
                    .field("feeds", Utils.listToText(source.getFeeds()))
                    .field("sitemaps", Utils.listToText(source.getSitemaps()))
                    .field("categories", Utils.listToText(source.getCategories()))
                    .field("app_ids", Utils.listToText(source.getAppIds()))
                    .field("url_filters", Utils.listToText(source.getUrlFilters()))
                    .field("url_normalizers", Utils.listToText(source.getUrlNormalizers()))
                    .field("title_selectors", Utils.listToText(source.getTitleSelectors()))
                    .field("text_selectors", Utils.listToText(source.getTextSelectors()))
                    .field("text_normalizers", Utils.listToText(source.getTextNormalizers()))
                    .field("date_selectors", Utils.listToText(source.getDateSelectors()))
                    .field("date_regexps", Utils.listToText(source.getDateRegexps()))
                    .field("date_formats", Utils.listToText(source.getDateFormats()))
                    .field("updated", new Date())
                    .endObject();
            IndexRequest indexRequest = new IndexRequest(getIndex(), formatId(url))
                    .source(xContentBuilder)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            getConnection().getRestHighLevelClient().index(indexRequest, getRequestOptions());
        } catch (IOException e) {
            LOG.error("Failed to save HTTP source with url '{}'", source.getUrl());
        }
    }


    public void delete(String url) {
        if (url != null) {
            try {
                DeleteRequest deleteRequest = new DeleteRequest(getIndex(), formatId(url))
                        .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                getConnection().getRestHighLevelClient().delete(deleteRequest, getRequestOptions());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteAll() {
        try {
            TimeValue keepAlive = TimeValue.timeValueMinutes(10);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .query(QueryBuilders.matchAllQuery())
                    .size(100)
                    .fetchSource(true)
                    .explain(false);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .scroll(keepAlive)
                    .source(searchSourceBuilder);
            SearchResponse response = getConnection().getRestHighLevelClient()
                    .search(searchRequest, getRequestOptions());
            do {
                Arrays.stream(response.getHits().getHits())
                        .map(SearchHit::getSourceAsMap)
                        .filter(Objects::nonNull)
                        .map(this::mapToHttpSource)
                        .map(HttpSource::getUrl)
                        .forEach(this::delete);
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(response.getScrollId())
                        .scroll(keepAlive);
                response = getConnection().getRestHighLevelClient().searchScroll(searchScrollRequest, getRequestOptions());
            } while (response.getHits().getHits().length != 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpSource mapToHttpSource(Map<String, Object> source) {
        HttpSource hs = new HttpSource();
        hs.setUrl(Objects.toString(source.get("url"), null));
        hs.setName(Objects.toString(source.get("name"), null));
        hs.setLanguage(Objects.toString(source.get("language"), null));
        hs.setTimezone(Objects.toString(source.get("timezone"), null));
        hs.setUrls(DataUtils.parseStringList(source.get("urls")));
        hs.setUrlRecrawlDelayInSecs(DataUtils.tryParseInteger(source.get("url_crawl_delay_secs")));
        hs.setFeeds(DataUtils.parseStringList(source.get("feeds")));
        hs.setFeedRecrawlDelayInSecs(DataUtils.tryParseInteger(source.get("feed_crawl_delay_secs")));
        hs.setSitemaps(DataUtils.parseStringList(source.get("sitemaps")));
        hs.setSitemapRecrawlDelayInSecs(DataUtils.tryParseInteger(source.get("sitemap_crawl_delay_secs")));
        hs.setEnabled(falseOrBoolean(source.get("enabled")));
        hs.setDiscoveryEnabled(falseOrBoolean(source.get("discovery_enabled")));
        hs.setUrlFilters(DataUtils.parseStringList(source.get("url_filters")));
        hs.setUrlNormalizers(DataUtils.parseStringList(source.get("url_normalizers")));
        hs.setCategories(DataUtils.parseStringList(source.get("categories")));
        hs.setAppIds(DataUtils.parseStringList(source.get("app_ids")));
        hs.setTitleSelectors(DataUtils.parseStringList(source.get("title_selectors")));
        hs.setTextSelectors(DataUtils.parseStringList(source.get("text_selectors")));
        hs.setTextNormalizers(DataUtils.parseStringList(source.get("text_normalizers")));
        hs.setDateSelectors(DataUtils.parseStringList(source.get("date_selectors")));
        hs.setDateRegexps(DataUtils.parseStringList(source.get("date_regexps")));
        hs.setDateFormats(DataUtils.parseStringList(source.get("date_formats")));
        return hs;
    }

}