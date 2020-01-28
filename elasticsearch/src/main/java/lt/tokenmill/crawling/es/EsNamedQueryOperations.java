package lt.tokenmill.crawling.es;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.NamedQuery;
import lt.tokenmill.crawling.data.PageableList;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


public class EsNamedQueryOperations extends BaseElasticOps {

    private static final Logger LOG = LoggerFactory.getLogger(EsNamedQueryOperations.class);

    protected EsNamedQueryOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES HTTP Source Tests Operations {}/{}", index, type);
    }

    public static EsNamedQueryOperations getInstance(ElasticConnection connection, String index, String type) {
        return new EsNamedQueryOperations(connection, index, type);
    }

    public PageableList<NamedQuery> filter(String prefix) {
        try {
            BoolQueryBuilder filter = QueryBuilders.boolQuery();
            if (!Strings.isNullOrEmpty(prefix)) {
                filter.must(QueryBuilders
                        .prefixQuery("name", prefix.trim()));
            }
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .fetchSource(true)
                    .explain(false)
                    .size(100)
                    .query(filter);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .source(searchSourceBuilder);

            SearchResponse response = getConnection().getRestHighLevelClient()
                    .search(searchRequest, getRequestOptions());

            List<NamedQuery> items = Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSourceAsMap)
                    .filter(Objects::nonNull)
                    .map(this::mapToNamedQuery)
                    .collect(Collectors.toList());
            return PageableList.create(items, response.getHits().getTotalHits().value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PageableList<>();
    }

    public List<String> suggest(String prefix) {
        try {
            CompletionSuggestionBuilder suggestionBuilder = new CompletionSuggestionBuilder("name_suggest")
                    .prefix(prefix);
            SearchSourceBuilder sourceRequestBuilder = new SearchSourceBuilder()
                    .size(100)
                    .explain(false)
                    .fetchSource(true)
                    .suggest(new SuggestBuilder().addSuggestion("suggestion", suggestionBuilder));
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .source(sourceRequestBuilder);
            SearchResponse response = getConnection().getRestHighLevelClient()
                    .search(searchRequest, getRequestOptions());
            return response.getSuggest().filter(CompletionSuggestion.class).stream()
                    .flatMap(s -> s.getOptions().stream())
                    .sorted(Comparator.comparingDouble(Suggest.Suggestion.Entry.Option::getScore))
                    .map(Suggest.Suggestion.Entry.Option::getText)
                    .map(Text::toString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }


    public NamedQuery get(String name) {
        try {
            GetResponse response = getConnection().getRestHighLevelClient()
                    .get(new GetRequest(getIndex(), getType(), formatId(name))
                            .fetchSourceContext(new FetchSourceContext(true)), getRequestOptions());
            if (response.isExists()) {
                return mapToNamedQuery(response.getSource());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<NamedQuery> all() {
        try {
            TimeValue keepAlive = TimeValue.timeValueMinutes(10);
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .types(getType())
                    .scroll(keepAlive)
                    .source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));
            SearchResponse response = getConnection().getRestHighLevelClient()
                    .search(searchRequest, getRequestOptions());
            List<NamedQuery> result = Lists.newArrayList();
            do {
                result.addAll(Arrays.stream(response.getHits().getHits())
                        .map(SearchHit::getSourceAsMap)
                        .filter(Objects::nonNull)
                        .map(this::mapToNamedQuery)
                        .collect(Collectors.toList()));
                response = getConnection().getRestHighLevelClient()
                        .searchScroll(new SearchScrollRequest(response.getScrollId())
                                .scroll(keepAlive), getRequestOptions());
            } while (response.getHits().getHits().length != 0);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  Collections.emptyList();
    }

    public void save(NamedQuery nq) {
        try {
            XContentBuilder contentBuilder = jsonBuilder()
                    .startObject()
                    .field("name", nq.getName())
                    .startObject("name_suggest")
                    .field("input", Lists.newArrayList(nq.getName()))
                    .endObject()
                    .field("stemmed_case_sensitive", nq.getStemmedCaseSensitive())
                    .field("stemmed_case_insensitive", nq.getStemmedCaseInSensitive())
                    .field("not_stemmed_case_sensitive", nq.getNotStemmedCaseSensitive())
                    .field("not_stemmed_case_insensitive", nq.getNotStemmedCaseInSensitive())
                    .field("advanced", nq.getAdvanced())
                    .field("updated", new Date())
                    .endObject();
            IndexRequest indexRequest = new IndexRequest(getIndex(), getType(), formatId(nq.getName()))
                    .source(contentBuilder)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            getConnection().getRestHighLevelClient().index(indexRequest, getRequestOptions());
        } catch (IOException e) {
            LOG.error("Failed to save HTTP source test with url '{}'", nq.getName());
        }
    }


    public void delete(NamedQuery nq) {
        if (nq != null && nq.getName() != null) {
            try {
                DeleteRequest deleteRequest = new DeleteRequest(getIndex(), getType(), formatId(nq.getName()))
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
            SearchRequest searchRequest = new SearchRequest(getIndex())
                    .scroll(keepAlive)
                    .types(getType())
                    .source(new SearchSourceBuilder()
                            .size(100)
                            .explain(false)
                            .fetchSource(true)
                            .query(QueryBuilders.matchAllQuery()));

            SearchResponse response = getConnection().getRestHighLevelClient().search(searchRequest, getRequestOptions());
            do {
                Arrays.stream(response.getHits().getHits())
                        .map(SearchHit::getSourceAsMap)
                        .filter(Objects::nonNull)
                        .map(this::mapToNamedQuery)
                        .forEach(this::delete);
                response = getConnection().getRestHighLevelClient()
                        .searchScroll(new SearchScrollRequest(response.getScrollId()).scroll(keepAlive), getRequestOptions());
            } while (response.getHits().getHits().length != 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private NamedQuery mapToNamedQuery(Map<String, Object> source) {
        NamedQuery nq = new NamedQuery();
        nq.setName(Objects.toString(source.get("name"), null));
        nq.setStemmedCaseSensitive(Objects.toString(source.get("stemmed_case_sensitive"), null));
        nq.setStemmedCaseInSensitive(Objects.toString(source.get("stemmed_case_insensitive"), null));
        nq.setNotStemmedCaseSensitive(Objects.toString(source.get("not_stemmed_case_sensitive"), null));
        nq.setNotStemmedCaseInSensitive(Objects.toString(source.get("not_stemmed_case_insensitive"), null));
        nq.setAdvanced(Objects.toString(source.get("advanced"), null));
        return nq;
    }

}