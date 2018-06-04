package lt.tokenmill.crawling.es;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.HttpSourceTest;
import lt.tokenmill.crawling.data.PageableList;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


public class EsHttpSourceTestOperations extends BaseElasticOps {

    private static final Logger LOG = LoggerFactory.getLogger(EsHttpSourceTestOperations.class);

    private EsHttpSourceTestOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES HTTP Source Tests Operations {}/{}", index, type);
    }

    public static EsHttpSourceTestOperations getInstance(ElasticConnection connection, String index, String type) {
        return new EsHttpSourceTestOperations(connection, index, type);
    }

    public PageableList<HttpSourceTest> filter(String prefix) {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery();
            if (!Strings.isNullOrEmpty(prefix)) {
                filter.must(QueryBuilders
                        .prefixQuery("url", prefix.trim()));
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .size(100)
                    .fetchSource(true)
                    .explain(false)
                    .query(filter);

            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .source(searchSourceBuilder);
            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest);
            List<HttpSourceTest> items = Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSourceAsMap)
                    .filter(Objects::nonNull)
                    .map(this::mapToHttpSourceTest)
                    .collect(Collectors.toList());
            return PageableList.create(items, response.getHits().getTotalHits());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageableList<>();
    }


    public HttpSourceTest get(String url) {
        try {
            GetRequest getRequest = new GetRequest(getIndex(), getType(), formatId(url))
                    .fetchSourceContext(new FetchSourceContext(true));
            GetResponse response = getConnection().getRestHighLevelClient().get(getRequest);
            if (response.isExists()) {
                return mapToHttpSourceTest(response.getSource());
            }
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<HttpSourceTest> all() {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery()
                    .must(QueryBuilders.existsQuery("source_url"));
            TimeValue keepAlive = TimeValue.timeValueMinutes(10);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .fetchSource(true)
                    .explain(false)
                    .query(filter)
                    .size(100);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .source(searchSourceBuilder)
                    .scroll(keepAlive);

            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest);


            List<HttpSourceTest> result = Lists.newArrayList();
            do {
                result.addAll(Arrays.stream(response.getHits().getHits())
                        .map(SearchHit::getSourceAsMap)
                        .filter(Objects::nonNull)
                        .map(this::mapToHttpSourceTest)
                        .collect(Collectors.toList()));
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(response.getScrollId())
                        .scroll(keepAlive);
                response = getConnection().getRestHighLevelClient().searchScroll(searchScrollRequest);
            } while (response.getHits().getHits().length != 0);
            return result;
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void save(HttpSourceTest hst) {
        try {
            String url = hst.getUrl();
            XContentBuilder contentBuilder = jsonBuilder()
                    .startObject()
                    .field("url", url)
                    .field("source_url", hst.getSource().trim())
                    .field("html", hst.getHtml() != null ? hst.getHtml().trim() : null)
                    .field("url_accepted", hst.getUrlAccepted() != null ? hst.getUrlAccepted() : false)
                    .field("title", hst.getTitle() != null ? hst.getTitle().trim() : null)
                    .field("text", hst.getText() != null ? hst.getText().trim() : null)
                    .field("date", hst.getDate() != null ? hst.getDate().trim() : null)
                    .field("updated", new Date())
                    .endObject();
            IndexRequest indexRequest = new IndexRequest(getIndex(), getType(), formatId(hst.getUrl()))
                    .source(contentBuilder)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            getConnection().getRestHighLevelClient().index(indexRequest);
        } catch (IOException e) {
            LOG.error("Failed to save HTTP source test with url '{}'", hst.getUrl());
        }
    }


    public void delete(String url) {
        if (url != null) {
            try {
                DeleteRequest deleteRequest = new DeleteRequest(getIndex(), getType(), formatId(url))
                        .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                DeleteResponse delete = getConnection().getRestHighLevelClient().delete(deleteRequest);
                LOG.debug("Delete HttpSourceTest url {} with response status {}", url, delete.status());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteAll() {
        try {
            TimeValue keepAlive = TimeValue.timeValueMinutes(10);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .scroll(keepAlive)
                    .source(new SearchSourceBuilder()
                            .size(100)
                            .fetchSource(true)
                            .explain(false)
                            .query(QueryBuilders.matchAllQuery()));
            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest);
            do {
                Arrays.stream(response.getHits().getHits())
                        .map(SearchHit::getSourceAsMap)
                        .filter(Objects::nonNull)
                        .map(this::mapToHttpSourceTest)
                        .map(HttpSourceTest::getUrl)
                        .forEach(this::delete);
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(response.getScrollId())
                        .scroll(keepAlive);
                response = getConnection().getRestHighLevelClient().searchScroll(searchScrollRequest);
            } while (response.getHits().getHits().length != 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HttpSourceTest mapToHttpSourceTest(Map<String, Object> source) {
        HttpSourceTest hst = new HttpSourceTest();
        hst.setUrl(Objects.toString(source.get("url"), null));
        hst.setSource(Objects.toString(source.get("source_url"), null));
        hst.setHtml(Objects.toString(source.get("html"), null));
        hst.setUrlAccepted(EsDataParser.falseOrBoolean(source.get("url_accepted")));
        hst.setTitle(Objects.toString(source.get("title"), null));
        hst.setText(Objects.toString(source.get("text"), null));
        hst.setDate(Objects.toString(source.get("date"), null));
        return hst;
    }

}