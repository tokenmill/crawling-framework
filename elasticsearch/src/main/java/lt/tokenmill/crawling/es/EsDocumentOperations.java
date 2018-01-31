package lt.tokenmill.crawling.es;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lt.tokenmill.crawling.data.*;
import lt.tokenmill.crawling.es.model.DateHistogramValue;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class EsDocumentOperations extends BaseElasticOps {

    private static final Logger LOG = LoggerFactory.getLogger(EsDocumentOperations.class);

    public static final String URL_FIELD = "url";
    public static final String SOURCE_FIELD = "source";
    public static final String CREATED_FIELD = "created";
    public static final String UPDATED_FIELD = "updated";
    public static final String PUBLISHED_FIELD = "published";
    public static final String DISCOVERED_FIELD = "discovered";
    public static final String TITLE_FIELD = "title";
    public static final String TEXT_FIELD = "text";
    public static final String STATUS_FIELD = "status";
    public static final String APP_IDS_FIELD = "app_ids";
    public static final String CATEGORIES_FIELD = "categories";

    private static final Set<String> DEFAULT_FIELDS = Sets.newHashSet(
            URL_FIELD, SOURCE_FIELD, CREATED_FIELD, UPDATED_FIELD,
            PUBLISHED_FIELD, DISCOVERED_FIELD, TITLE_FIELD, TEXT_FIELD,
            STATUS_FIELD, APP_IDS_FIELD, CATEGORIES_FIELD);

    protected EsDocumentOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES Documents Operations {}/{}", index, type);
    }

    public static EsDocumentOperations getInstance(ElasticConnection connection, String indexName, String type) {
        return new EsDocumentOperations(connection, indexName, type);
    }

    public PageableList<HttpArticle> query(NamedQuery... queries) {
        try {
            BoolQueryBuilder query = QueryBuilders.boolQuery();
            for (NamedQuery nq : queries) {
                addQuery(query, true, nq.getNotStemmedCaseSensitive(), "nostem_cs");
                addQuery(query, true, nq.getNotStemmedCaseInSensitive(), "nostem_ci");
                addQuery(query, true, nq.getStemmedCaseSensitive(), "stem_cs");
                addQuery(query, true, nq.getStemmedCaseInSensitive(), "stem_ci");
                if (!Strings.isNullOrEmpty(nq.getAdvanced())) {
                    query.must(QueryBuilders.queryStringQuery(nq.getAdvanced())
                            .defaultOperator(Operator.AND));
                }
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .explain(false)
                    .fetchSource(true)
                    .sort(PUBLISHED_FIELD, SortOrder.DESC)
                    .size(100).query(query);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .source(searchSourceBuilder);
            SearchResponse response = getConnection().getRestHighLevelClient()
                    .search(searchRequest);

            List<HttpArticle> items = Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSourceAsMap)
                    .filter(Objects::nonNull)
                    .map(this::mapToHttpArticle)
                    .collect(Collectors.toList());
            return PageableList.create(items, response.getHits().getTotalHits());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PageableList<>();
    }

    public PageableList<HighlightedSearchResult> query(List<NamedQuery> included, List<NamedQuery> excluded, String additional) {
        try {
            BoolQueryBuilder query = QueryBuilders.boolQuery();
            for (NamedQuery nq : included) {
                addQuery(query, true, nq.getNotStemmedCaseSensitive(), "nostem_cs");
                addQuery(query, true, nq.getNotStemmedCaseInSensitive(), "nostem_ci");
                addQuery(query, true, nq.getStemmedCaseSensitive(), "stem_cs");
                addQuery(query, true, nq.getStemmedCaseInSensitive(), "stem_ci");
                if (!Strings.isNullOrEmpty(nq.getAdvanced())) {
                    query.must(QueryBuilders.queryStringQuery(nq.getAdvanced())
                            .defaultOperator(Operator.AND));
                }
            }
            for (NamedQuery nq : excluded) {
                addQuery(query, false, nq.getNotStemmedCaseSensitive(), "nostem_cs");
                addQuery(query, false, nq.getNotStemmedCaseInSensitive(), "nostem_ci");
                addQuery(query, false, nq.getStemmedCaseSensitive(), "stem_cs");
                addQuery(query, false, nq.getStemmedCaseInSensitive(), "stem_ci");
                if (!Strings.isNullOrEmpty(nq.getAdvanced())) {
                    query.mustNot(QueryBuilders.queryStringQuery(nq.getAdvanced())
                            .defaultOperator(Operator.AND));
                }
            }
            if (!Strings.isNullOrEmpty(additional)) {
                query.must(QueryBuilders.queryStringQuery(QueryParser.escape(additional))
                        .field("title.nostem_ci")
                        .field("text.nostem_ci")
                        .defaultOperator(Operator.AND));
            }
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .query(query)
                    .size(100)
                    .fetchSource(true)
                    .explain(false)
                    .highlighter(new HighlightBuilder()
                            .field("text.nostem_cs")
                            .field("text.nostem_ci")
                            .field("text.stem_cs")
                            .field("text.stem_ci"))
                    .sort(PUBLISHED_FIELD, SortOrder.DESC);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .source(sourceBuilder);
            SearchResponse response = getConnection().getRestHighLevelClient()
                    .search(searchRequest);

            List<HighlightedSearchResult> items = Arrays.stream(response.getHits().getHits())
                    .filter(sh -> sh.getSourceAsMap() != null)
                    .map(this::mapToHighlightedResult)
                    .collect(Collectors.toList());
            return PageableList.create(items, response.getHits().getTotalHits());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PageableList<>();
    }

    private void addQuery(BoolQueryBuilder boolQuery, boolean positive, String query, String modifier) {
        if (Strings.isNullOrEmpty(query)) {
            return;
        }
        QueryStringQueryBuilder builder = QueryBuilders.queryStringQuery(query)
                .field("title." + modifier)
                .field("text." + modifier)
                .defaultOperator(Operator.AND);
        if (positive) {
            boolQuery.must(builder);
        } else {
            boolQuery.mustNot(builder);
        }
    }

    public void store(HttpArticle article) {
        store(article, Collections.emptyMap());
    }

    public void store(HttpArticle article, Map<String, Object> fields) {
        try {
            XContentBuilder jsonBuilder = jsonBuilder();
            jsonBuilder.startObject();
            applyFields(jsonBuilder, article, fields);
            jsonBuilder.endObject();
            IndexRequest indexRequest = new IndexRequest(getIndex(), getType(), formatId(article.getUrl()))
                    .source(jsonBuilder);
            getConnection().getProcessor().add(indexRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpArticle get(String url) {
        try {
            GetRequest getRequest = new GetRequest(getIndex(), getType(), formatId(url))
                    .fetchSourceContext(new FetchSourceContext(true));
            GetResponse response = getConnection().getRestHighLevelClient().get(getRequest);
            if (response.isExists()) {
                return mapToHttpArticle(response.getSource());
            }
        } catch (ElasticsearchStatusException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DateHistogramValue> calculateStats(String sourceUrl) {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery()
                    .must(QueryBuilders.rangeQuery("created").gte("now-1M"))
                    .must(QueryBuilders.termQuery("source", sourceUrl));

            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .searchType(SearchType.DEFAULT)
                    .source(new SearchSourceBuilder()
                            .query(filter)
                            .aggregation(AggregationBuilders
                                    .dateHistogram("urls_over_time")
                                    .field("created")
                                    .format("yyyy-MM-dd")
                                    .dateHistogramInterval(DateHistogramInterval.DAY))
                            .explain(false)
                            .fetchSource(true)
                            .size(0));
            SearchResponse response = getConnection().getRestHighLevelClient()
                    .search(searchRequest);
            ParsedDateHistogram hits = response.getAggregations().get("urls_over_time");
            return hits.getBuckets().stream()
                    .map(b -> new DateHistogramValue(b.getKeyAsString(), b.getDocCount()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void applyField(XContentBuilder builder, String fieldName,
                            Map<String, Object> fieldValues, Object defaultValue) throws IOException {
        Object fieldValue = fieldValues.getOrDefault(fieldName, defaultValue);
        if (fieldValue != null) {
            builder.field(fieldName, Utils.formatFieldValue(fieldValue));
        }
    }

    private void applyFields(XContentBuilder builder, HttpArticle article, Map<String, Object> fields) throws IOException {
        applyField(builder, URL_FIELD, fields, article.getUrl());
        applyField(builder, SOURCE_FIELD, fields, article.getSource());
        applyField(builder, CREATED_FIELD, fields, new Date());
        applyField(builder, UPDATED_FIELD, fields, new Date());
        applyField(builder, PUBLISHED_FIELD, fields, article.getPublished());
        applyField(builder, DISCOVERED_FIELD, fields, article.getDiscovered());
        applyField(builder, TITLE_FIELD, fields, article.getTitle());
        applyField(builder, TEXT_FIELD, fields, article.getText());
        applyField(builder, STATUS_FIELD, fields, "NEW");
        applyField(builder, APP_IDS_FIELD, fields, article.getAppIds());
        applyField(builder, CATEGORIES_FIELD, fields, article.getCategories());
        for (Map.Entry<String, Object> fieldValue : fields.entrySet()) {
            if (!DEFAULT_FIELDS.contains(fieldValue.getKey())) {
                applyField(builder, fieldValue.getKey(), fields, null);
            }
        }
    }

    private HighlightedSearchResult mapToHighlightedResult(SearchHit hit) {
        HttpArticle article = mapToHttpArticle(hit.getSourceAsMap());
        List<String> highlights = Lists.newArrayList();
        for (Map.Entry<String, HighlightField> fields : hit.getHighlightFields().entrySet()) {
            for (Text text : fields.getValue().getFragments()) {
                highlights.add(text.toString());
            }
        }
        return new HighlightedSearchResult(article, highlights);
    }


    protected HttpArticle mapToHttpArticle(Map<String, Object> source) {
        HttpArticle ha = new HttpArticle();
        ha.setUrl(Objects.toString(source.get("url"), null));
        ha.setSource(Objects.toString(source.get("source"), null));
        ha.setTitle(Objects.toString(source.get("title"), null));
        ha.setText(Objects.toString(source.get("text"), null));
        ha.setPublished(EsDataParser.nullOrDate(source.get("published")));
        ha.setDiscovered(EsDataParser.nullOrDate(source.get("discovered")));
        ha.setAppIds(DataUtils.parseStringList(source.get("app_ids")));
        ha.setCategories(DataUtils.parseStringList(source.get("categories")));
        return ha;
    }

}