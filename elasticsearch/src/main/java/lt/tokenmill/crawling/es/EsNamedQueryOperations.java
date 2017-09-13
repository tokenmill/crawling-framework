package lt.tokenmill.crawling.es;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.NamedQuery;
import lt.tokenmill.crawling.data.PageableList;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
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

    private EsNamedQueryOperations(ElasticConnection connection, String index, String type) {
        super(connection, index, type);
        LOG.info("Created ES HTTP Source Tests Operations {}/{}", index, type);
    }

    public static EsNamedQueryOperations getInstance(ElasticConnection connection, String index, String type) {
        return new EsNamedQueryOperations(connection, index, type);
    }

    public PageableList<NamedQuery> filter(String prefix) {
        BoolQueryBuilder filter = QueryBuilders.boolQuery();
        if (!Strings.isNullOrEmpty(prefix)) {
            filter.must(QueryBuilders
                    .prefixQuery("name", prefix.trim()));
        }

        SearchResponse response = getConnection().getClient().prepareSearch(getIndex())
                .setTypes(getType())
                .setPostFilter(filter)
                .setSize(100)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        List<NamedQuery> items = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSource)
                .filter(Objects::nonNull)
                .map(this::mapToNamedQuery)
                .collect(Collectors.toList());
        return PageableList.create(items, response.getHits().getTotalHits());
    }

    public List<String> suggest(String prefix) {
        CompletionSuggestionBuilder suggestionBuilder = new CompletionSuggestionBuilder("name_suggest")
                .prefix(prefix);

        SearchResponse response = getConnection().getClient().prepareSearch(getIndex())
                .setTypes(getType())
                .suggest(new SuggestBuilder().addSuggestion("suggestion", suggestionBuilder))
                .setSize(100)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        return response.getSuggest().filter(CompletionSuggestion.class).stream()
                .flatMap(s -> s.getOptions().stream())
                .sorted(Comparator.comparingDouble(Suggest.Suggestion.Entry.Option::getScore))
                .map(Suggest.Suggestion.Entry.Option::getText)
                .map(Text::toString)
                .collect(Collectors.toList());
    }


    public NamedQuery get(String name) {
        GetResponse response = getConnection().getClient()
                .prepareGet(getIndex(), getType(), name.toLowerCase())
                .setFetchSource(true)
                .get();
        if (response.isExists()) {
            return mapToNamedQuery(response.getSource());
        }
        return null;
    }

    public List<NamedQuery> all() {
        Client client = getConnection().getClient();
        TimeValue keepAlive = TimeValue.timeValueMinutes(10);
        SearchResponse response = client.prepareSearch(getIndex())
                .setTypes(getType())
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(100)
                .setScroll(keepAlive)
                .setFetchSource(true)
                .setExplain(false)
                .execute()
                .actionGet();

        List<NamedQuery> result = Lists.newArrayList();
        do {
            result.addAll(Arrays.stream(response.getHits().getHits())
                    .map(SearchHit::getSource)
                    .filter(Objects::nonNull)
                    .map(this::mapToNamedQuery)
                    .collect(Collectors.toList()));
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(keepAlive)
                    .execute()
                    .actionGet();
        } while (response.getHits().getHits().length != 0);
        return result;
    }

    public void save(NamedQuery nq) {
        try {
            getConnection().getClient().prepareIndex(getIndex(), getType(), nq.getName().toLowerCase())
                    .setSource(jsonBuilder()
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
                            .endObject())
                    .get();
        } catch (IOException e) {
            LOG.error("Failed to save HTTP source test with url '{}'", nq.getName());
        }
    }


    public void delete(NamedQuery nq) {
        if (nq != null && nq.getName() != null) {
            getConnection().getClient().prepareDelete(getIndex(), getType(), nq.getName()).get();
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
                    .map(this::mapToNamedQuery)
                    .forEach(this::delete);
            response = client.prepareSearchScroll(response.getScrollId())
                    .setScroll(keepAlive)
                    .execute()
                    .actionGet();
        } while (response.getHits().getHits().length != 0);
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