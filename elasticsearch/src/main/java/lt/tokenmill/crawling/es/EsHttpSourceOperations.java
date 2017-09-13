package lt.tokenmill.crawling.es;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.PageableList;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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

    private EsHttpSourceOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES HTTP Sources Operations {}/{}", index, type);
    }

    public static EsHttpSourceOperations getInstance(ElasticConnection connection, String index, String type) {
        return new EsHttpSourceOperations(connection, index, type);
    }

    public List<HttpSource> findEnabledSources() {
        BoolQueryBuilder filter = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("enabled", true));

        SearchResponse response = getConnection().getClient()
                .prepareSearch(getIndex())
                .setTypes(getType())
                .setSearchType(SearchType.DEFAULT)
                .setPostFilter(filter)
                .addSort("updated", SortOrder.ASC)
                .setSize(10000)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        SearchHits hits = response.getHits();
        return Arrays.stream(hits.getHits())
                .map(SearchHit::sourceAsMap)
                .map(this::mapToHttpSource)
                .collect(Collectors.toList());

    }

    public HttpSource get(String url) {
        GetResponse response = getConnection().getClient().prepareGet(getIndex(), getType(), url)
                .setFetchSource(true)
                .get();
        if (response.isExists()) {
            return mapToHttpSource(response.getSource());
        }
        return null;
    }

    public PageableList<HttpSource> filter(String text) {
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        if (!Strings.isNullOrEmpty(text)) {
            filter.must(QueryBuilders
                    .queryStringQuery(QueryParser.escape(text.trim()))
                    .field("search_field")
                    .defaultOperator(QueryStringQueryBuilder.DEFAULT_OPERATOR.AND));
        }

        SearchResponse response = getConnection().getClient().prepareSearch(getIndex())
                .setTypes(getType())
                .setPostFilter(filter)
                .setSize(100)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        List<HttpSource> items = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSource)
                .filter(Objects::nonNull)
                .map(this::mapToHttpSource)
                .sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
                .collect(Collectors.toList());
        return PageableList.create(items, response.getHits().getTotalHits());
    }

    public List<HttpSource> all() {
        BoolQueryBuilder filter = QueryBuilders.boolQuery()
                .must(QueryBuilders.existsQuery("url"))
                .must(QueryBuilders.existsQuery("name"));


        Client client = getConnection().getClient();
        TimeValue keepAlive = TimeValue.timeValueMinutes(10);
        SearchResponse response = client.prepareSearch(getIndex())
                .setTypes(getType())
                .setPostFilter(filter)
                .setSize(100)
                .setScroll(keepAlive)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        List<HttpSource> result = Lists.newArrayList();
        do {
            result.addAll(Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSource)
                    .filter(Objects::nonNull)
                    .map(this::mapToHttpSource)
                    .collect(Collectors.toList()));
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(keepAlive)
                    .execute()
                    .actionGet();
        } while (response.getHits().getHits().length != 0);
        return result;
    }

    public void save(HttpSource source) {
        try {
            String url = source.getUrl().toLowerCase();
            getConnection().getClient().prepareIndex(getIndex(), getType(), url)
                    .setSource(jsonBuilder()
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
                            .endObject())
                    .get();
        } catch (IOException e) {
            LOG.error("Failed to save HTTP source with url '{}'", source.getUrl());
        }
    }


    public void delete(String url) {
        if (url != null) {
            getConnection().getClient().prepareDelete(getIndex(), getType(), url.toLowerCase()).get();
        }
    }

    public void deleteAll() {
        Client client = getConnection().getClient();

        TimeValue keepAlive = TimeValue.timeValueMinutes(10);
        SearchResponse response = client.prepareSearch(getIndex())
                .setTypes(getType())
                .setPostFilter(QueryBuilders.matchAllQuery())
                .setSize(100)
                .setScroll(keepAlive)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();
        do {
           Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSource)
                    .filter(Objects::nonNull)
                    .map(this::mapToHttpSource)
                    .map(HttpSource::getUrl)
                    .forEach(this::delete);
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(keepAlive)
                    .execute()
                    .actionGet();
        } while (response.getHits().getHits().length != 0);
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