package lt.tokenmill.crawling.es;

import lt.tokenmill.crawling.data.HttpUrl;
import lt.tokenmill.crawling.es.model.DateHistogramValue;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EsHttpUrlOperations extends BaseElasticOps{

    private static final Logger LOG = LoggerFactory.getLogger(EsHttpUrlOperations.class);

    protected EsHttpUrlOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES URLs Operations {}/{}", index, type);
    }

    public static EsHttpUrlOperations getInstance(ElasticConnection connection, String index, String type) {
        return new EsHttpUrlOperations(connection, index, type);
    }

    public void upsertUrlStatus(String url, String published, String source, boolean create, Enum status) throws IOException {
        Date now = new Date();
        IndexRequestBuilder insert = getConnection().getClient()
                .prepareIndex(getIndex(), getType(), url)
                .setSource(jsonBuilder()
                        .startObject()
                        .field("url", url)
                        .field("source", source)
                        .field("created", now)
                        .field("updated", now)
                        .field("published", published)
                        .field("status", String.valueOf(status))
                        .endObject())
                .setCreate(create);

        UpdateRequestBuilder update = getConnection().getClient()
                .prepareUpdate(getIndex(), getType(), url)
                .setDoc(jsonBuilder()
                        .startObject()
                        .field("updated", now)
                        .field("published", published)
                        .field("status", String.valueOf(status))
                        .endObject())
                .setUpsert(insert.request());

        getConnection().getProcessor().add(create ? insert.request() : update.request());
    }

    public List<HttpUrl> findUrlsByStatusAndSource(Enum status, String source, int count) {
        BoolQueryBuilder filter = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("status", String.valueOf(status)))
                .must(QueryBuilders.termQuery("source", source));

        SearchResponse response = getConnection().getClient()
                .prepareSearch(getIndex())
                .setTypes(getType())
                .setSearchType(SearchType.DEFAULT)
                .setPostFilter(filter)
                .addSort("created", SortOrder.DESC)
                .setSize(count)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        SearchHits hits = response.getHits();
        return Arrays.stream(hits.getHits())
                .map(SearchHit::getSourceAsMap)
                .map(s -> {
                    HttpUrl httpUrl = new HttpUrl();
                    httpUrl.setUrl(Objects.toString(s.get("url"), null));
                    httpUrl.setPublished(Objects.toString(s.get("published"), null));
                    httpUrl.setDiscovered(EsDataParser.nullOrDate(s.get("created")));
                    httpUrl.setSource(source);
                    return httpUrl;
                })
                .collect(Collectors.toList());
    }

    public List<DateHistogramValue> calculateStats(String sourceUrl) {
        BoolQueryBuilder filter = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("created").gte("now-1M"))
                .must(QueryBuilders.termQuery("source", sourceUrl));

        SearchResponse response = getConnection().getClient()
                .prepareSearch(getIndex())
                .setTypes(getType())
                .setSearchType(SearchType.DEFAULT)
                .setQuery(filter)
                .addAggregation(AggregationBuilders
                        .dateHistogram("urls_over_time")
                        .field("created")
                        .format("yyyy-MM-dd")
                        .dateHistogramInterval(DateHistogramInterval.DAY))
                .setSize(0)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        InternalDateHistogram hits = response.getAggregations().get("urls_over_time");
        return hits.getBuckets().stream()
                .map(b -> new DateHistogramValue(b.getKeyAsString(), b.getDocCount()))
                .collect(Collectors.toList());
    }


}
