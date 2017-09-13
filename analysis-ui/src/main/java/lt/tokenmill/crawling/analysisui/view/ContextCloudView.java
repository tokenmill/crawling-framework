package lt.tokenmill.crawling.analysisui.view;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.analysisui.search.ResultPanel;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HighlightedSearchResult;
import lt.tokenmill.crawling.data.NamedQuery;
import lt.tokenmill.crawling.data.PageableList;
import lt.tokenmill.crawling.parser.utils.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

public class ContextCloudView extends BaseView {

    private static final Logger LOG = LoggerFactory.getLogger(ContextCloudView.class);

    private TextField filterField = new TextField();
    private Label queryDescriptionLabel = new Label();
    private VerticalLayout resultLayout = new VerticalLayout();

    public ContextCloudView() {
        super("Context Cloud");
        Button searchButton = new Button("Search");
        searchButton.addClickListener(event -> search());

        VerticalLayout searchLayout = new VerticalLayout();
        searchLayout.setSpacing(true);
        searchLayout.setWidth(50, PERCENTAGE);

        HorizontalLayout actionHeader = new HorizontalLayout(filterField, searchButton);
        actionHeader.setSpacing(true);
        actionHeader.setWidth(100, PERCENTAGE);
        actionHeader.setExpandRatio(filterField, 1.0f);
        filterField.setWidth(100, PERCENTAGE);

        searchLayout.addComponent(actionHeader);
        searchLayout.addComponent(queryDescriptionLabel);

        addComponent(searchLayout);
        setComponentAlignment(searchLayout, Alignment.TOP_CENTER);

        resultLayout.setWidth(80, PERCENTAGE);
        resultLayout.setSpacing(true);

        addComponent(resultLayout);
        setComponentAlignment(resultLayout, Alignment.TOP_CENTER);

    }

    private void search() {
        resultLayout.removeAllComponents();
        List<String> query = QueryParser.parseQuery(filterField.getValue());
        LOG.info("Parsed '{}' from query '{}'", query, filterField.getValue());
        List<NamedQuery> includedNamed = Lists.newArrayList();
        List<NamedQuery> excludedNamed = Lists.newArrayList();
        StringBuilder additionalQuery = new StringBuilder();
        for (String q : query) {
            boolean excluded = q.startsWith("-");
            String name = q.replaceAll("^[+-]+", "");
            NamedQuery namedQuery = ElasticSearch.getNamedQueryOperations().get(name);
            if (namedQuery != null && excluded) {
                excludedNamed.add(namedQuery);
                LOG.info("Named query '{}' is negative", namedQuery.getName());
            } else if (namedQuery != null) {
                includedNamed.add(namedQuery);
                LOG.info("Named query '{}' is positive", namedQuery.getName());
            } else {
                additionalQuery.append(" ").append(q);
            }
        }
        LOG.info("Additional query: '{}'", additionalQuery.toString().trim());
        PageableList<HighlightedSearchResult> result = ElasticSearch.getDocumentOperations().query(includedNamed, excludedNamed, additionalQuery.toString().trim());
        List<NamedQuery> namedQueries = Lists.newArrayList(includedNamed);
        namedQueries.addAll(excludedNamed);

        queryDescriptionLabel.setValue(String.format("Named Queries: %s, Additional Query: '%s'",
                namedQueries.stream().map(NamedQuery::getName).collect(Collectors.joining("', '", "'", "'")),
                additionalQuery.toString().trim()));

        Map<String, Integer> tokenCounts = Maps.newHashMap();

        for (HighlightedSearchResult r : result.getItems()) {
            List<String> tokens = r.getHighlights().stream()
                    .map(s -> s.replaceAll("<em>.+</em>", " "))
                    .map(s -> s.split("[\\s\\.\\?,\\!’”“\"]"))
                    .flatMap(Stream::of)
                    .map(String::toLowerCase)
                    .filter(s -> s.matches("^[a-z]{2,}$"))
                    .collect(Collectors.toList());
            tokens.forEach(t -> {
                Integer count = tokenCounts.getOrDefault(t, 0);
                tokenCounts.put(t, count + 1);
            });
        }
        List<String> tokens = tokenCounts.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .limit(200)
                .collect(Collectors.toList());
        resultLayout.removeAllComponents();
        Label tokensLabel = new Label(Joiner.on(", ").join(tokens));
        tokensLabel.addStyleName(ValoTheme.LABEL_LARGE);
        resultLayout.addComponent(tokensLabel);
    }
}
