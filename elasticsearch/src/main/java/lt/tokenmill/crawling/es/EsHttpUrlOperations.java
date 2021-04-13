package lt.tokenmill.crawling.es;

import lt.tokenmill.crawling.data.HttpUrl;
import lt.tokenmill.crawling.es.model.DateHistogramValue;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EsHttpUrlOperations extends BaseElasticOps{

    private static final Logger LOG = LoggerFactory.getLogger(EsHttpUrlOperations.class);

    protected EsHttpUrlOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES URLs Operations for index='{}', type='{}'", index, type);
    }

    public static EsHttpUrlOperations getInstance(ElasticConnection connection, String index, String type) {
        return new EsHttpUrlOperations(connection, index, type);
    }

    public void upsertUrlStatus(String url, String published, String source, boolean create, Enum status) throws IOException {
        upsertUrlStatus(url, published, source, create, String.valueOf(status));
    }

    public void upsertUrlStatus(String url, String published, String source, boolean create, String status) throws IOException {
        try {
            Date now = new Date();
            String id = formatId(url);
            XContentBuilder insert = jsonBuilder()
                    .startObject()
                    .field("url", url)
                    .field("source", source)
                    .field("created", now)
                    .field("updated", now)
                    .field("published", published)
                    .field("status", status)
                    .endObject();
            IndexRequest indexRequest = new IndexRequest(getIndex())
                    .id(id)
                    .source(insert)
                    .create(create);

            if (create) {
                getConnection().getProcessor().add(indexRequest);
            } else {
                XContentBuilder update = jsonBuilder()
                        .startObject()
                        .field("updated", now)
                        .field("published", published)
                        .field("status", status)
                        .endObject();
                UpdateRequest upsert = new UpdateRequest(getIndex(), id)
                        .doc(update)
                        .upsert(indexRequest);
                getConnection().getProcessor().add(upsert);
            }
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<HttpUrl> findUrlsByStatusAndSource(Enum status, String source, int count) {
        return findUrlsByStatusAndSource(String.valueOf(status), source, count);
    }

    public List<HttpUrl> findUrlsByStatusAndSource(String status, String source, int count) {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("status", status))
                    .must(QueryBuilders.termQuery("source", source));

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .fetchSource(true)
                    .size(count)
                    .query(filter)
                    .sort("created", SortOrder.DESC);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .searchType(SearchType.DEFAULT)
                    .source(searchSourceBuilder);

            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest, getRequestOptions());

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
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public List<DateHistogramValue> calculateStats(String sourceUrl) {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("created").gte("now-1M"))
                    .must(QueryBuilders.termQuery("source", sourceUrl));
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .size(0)
                    .fetchSource(true)
                    .explain(false)
                    .query(filter)
                    .aggregation(AggregationBuilders
                            .dateHistogram("urls_over_time")
                            .field("created")
                            .format("yyyy-MM-dd")
                            .dateHistogramInterval(DateHistogramInterval.DAY));
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .searchType(SearchType.DEFAULT)
                    .source(searchSourceBuilder);
            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest, getRequestOptions());
            ParsedDateHistogram hits = response.getAggregations().get("urls_over_time");
            return hits.getBuckets().stream()
                    .map(b -> new DateHistogramValue(b.getKeyAsString(), b.getDocCount()))
                    .collect(Collectors.toList());
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

}
