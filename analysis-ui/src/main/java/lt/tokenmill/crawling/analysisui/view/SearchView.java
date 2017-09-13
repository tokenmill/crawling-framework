package lt.tokenmill.crawling.analysisui.view;

import com.google.common.collect.Lists;
import com.vaadin.ui.*;
import lt.tokenmill.crawling.analysisui.search.ResultPanel;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HighlightedSearchResult;
import lt.tokenmill.crawling.data.NamedQuery;
import lt.tokenmill.crawling.data.PageableList;
import lt.tokenmill.crawling.parser.utils.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

public class SearchView extends BaseView {

    private static final Logger LOG = LoggerFactory.getLogger(SearchView.class);

    private TextField filterField = new TextField();
    private Label queryDescriptionLabel = new Label();
    private VerticalLayout resultLayout = new VerticalLayout();

    public SearchView() {
        super("Search");
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

        for (HighlightedSearchResult r : result.getItems()) {
            resultLayout.addComponent(new ResultPanel(r));
        }


    }
}
