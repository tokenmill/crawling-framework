package lt.tokenmill.crawling.es;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.HttpSourceTest;
import lt.tokenmill.crawling.data.PageableList;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
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
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        if (!Strings.isNullOrEmpty(prefix)) {
            filter.must(QueryBuilders
                    .prefixQuery("url", prefix.trim()));
        }

        SearchResponse response = getConnection().getClient().prepareSearch(getIndex())
                .setTypes(getType())
                .setPostFilter(filter)
                .setSize(100)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        List<HttpSourceTest> items = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .filter(Objects::nonNull)
                .map(this::mapToHttpSourceTest)
                .collect(Collectors.toList());
        return PageableList.create(items, response.getHits().getTotalHits());
    }


    public HttpSourceTest get(String url) {
        GetResponse response = getConnection().getClient().prepareGet(getIndex(), getType(), url)
                .setFetchSource(true)
                .get();
        if (response.isExists()) {
            return mapToHttpSourceTest(response.getSource());
        }
        return null;
    }

    public List<HttpSourceTest> all() {
        BoolQueryBuilder filter = QueryBuilders.boolQuery()
                .must(QueryBuilders.existsQuery("source_url"));


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

        List<HttpSourceTest> result = Lists.newArrayList();
        do {
            result.addAll(Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSourceAsMap)
                    .filter(Objects::nonNull)
                    .map(this::mapToHttpSourceTest)
                    .collect(Collectors.toList()));
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(keepAlive)
                    .execute()
                    .actionGet();
        } while (response.getHits().getHits().length != 0);
        return result;
    }

    public void save(HttpSourceTest hst) {
        try {
            String url = hst.getUrl().toLowerCase();
            getConnection().getClient().prepareIndex(getIndex(), getType(), url)
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("url", url)
                            .field("source_url", hst.getSource().trim())
                            .field("html", hst.getHtml() != null ? hst.getHtml().trim() : null)
                            .field("url_accepted", hst.getUrlAccepted() != null ? hst.getUrlAccepted() : false)
                            .field("title", hst.getTitle() != null ? hst.getTitle().trim() : null)
                            .field("text", hst.getText() != null ? hst.getText().trim() : null)
                            .field("date", hst.getDate() != null ? hst.getDate().trim() : null)
                            .field("updated", new Date())
                            .endObject())
                    .get();
        } catch (IOException e) {
            LOG.error("Failed to save HTTP source test with url '{}'", hst.getUrl());
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
                    .map(SearchHit::getSourceAsMap)
                    .filter(Objects::nonNull)
                    .map(this::mapToHttpSourceTest)
                    .map(HttpSourceTest::getUrl)
                    .forEach(this::delete);
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(keepAlive)
                    .execute()
                    .actionGet();
        } while (response.getHits().getHits().length != 0);
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