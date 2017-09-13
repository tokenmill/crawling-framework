package lt.tokenmill.crawling.adminui.view.pageanalysis;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.adminui.CrawlerAdminUI;
import lt.tokenmill.crawling.adminui.view.BaseView;
import lt.tokenmill.crawling.data.HtmlAnalysisResult;
import lt.tokenmill.crawling.pageanalyzer.PageAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

public class PageAnalysisView extends BaseView {

    private static final Logger LOG = LoggerFactory.getLogger(PageAnalysisView.class);

    private TextField urlField = new TextField();
    private VerticalLayout resultLayout = new VerticalLayout();

    public PageAnalysisView() {
        super("Page Analysis");
        Button searchButton = new Button("Analyze");
        searchButton.addClickListener(event -> analyze());

        VerticalLayout searchLayout = new VerticalLayout();
        searchLayout.setSpacing(true);
        searchLayout.setWidth(50, PERCENTAGE);

        HorizontalLayout actionHeader = new HorizontalLayout(urlField, searchButton);
        actionHeader.setSpacing(true);
        actionHeader.setWidth(100, PERCENTAGE);
        actionHeader.setExpandRatio(urlField, 1.0f);
        urlField.setWidth(100, PERCENTAGE);

        searchLayout.addComponent(actionHeader);

        addComponent(searchLayout);
        setComponentAlignment(searchLayout, Alignment.TOP_CENTER);

        resultLayout.setWidth(80, PERCENTAGE);
        resultLayout.setSpacing(true);

        addComponent(resultLayout);
        setComponentAlignment(resultLayout, Alignment.TOP_CENTER);

    }

    private void analyze() {
        resultLayout.removeAllComponents();
        String url = urlField.getValue();
        LOG.info("Analyzing '{}' ", urlField.getValue());
        HashMap<String, String> config = Maps.newHashMap();
        config.put(PageAnalyzer.CONFIG_ANALYZE_ROBOTS_TXT, "true");
        try {
            HtmlAnalysisResult result = PageAnalyzer.analyze(config, url);
            FormLayout layout = new FormLayout();
            TextField httpStatusField = new TextField("HTTP Status");
            httpStatusField.setSizeFull();
            httpStatusField.setValue(String.valueOf(result.getHttpStatus()));
            httpStatusField.setReadOnly(true);

            TextField titleField = new TextField("Title");
            titleField.setSizeFull();
            titleField.setValue(result.getTitle());
            titleField.setReadOnly(true);

            TextArea headersField = new TextArea("Response Headers");
            headersField.setSizeFull();
            headersField.setValue(result.getHeaders().entrySet().stream()
                    .map(e -> String.format("%s: %s", e.getKey(), e.getValue()))
                    .collect(Collectors.joining("\n")));
            headersField.setRows(Math.min(5, Math.max(2, result.getHeaders().size())));
            headersField.setReadOnly(true);

            TextArea linksField = new TextArea("Links");
            linksField.setSizeFull();
            linksField.setValue(result.getLinks().stream()
                .sorted()
                .distinct()
                .collect(Collectors.joining("\n")));
            linksField.setRows(Math.min(100, Math.max(2, result.getHeaders().size())));
            linksField.setReadOnly(true);

            TextArea metaField = new TextArea("Meta");
            metaField.setSizeFull();
            metaField.setValue(Joiner.on("\n").join(result.getMetaValues()));
            metaField.setRows(Math.min(10, Math.max(2, result.getHeaders().size())));
            metaField.setReadOnly(true);

            CheckBox crawlingForbiddenField = new CheckBox("Crawling Forbidden");
            crawlingForbiddenField.setSizeFull();
            crawlingForbiddenField.setValue(result.getRobotsAllowedNone());
            crawlingForbiddenField.setReadOnly(true);


            TextArea robotsTxtField = new TextArea("robots.txt");
            robotsTxtField.setSizeFull();
            robotsTxtField.setValue(Strings.nullToEmpty(result.getRobotsTxt()));
            robotsTxtField.setRows(10);
            robotsTxtField.setReadOnly(true);


            layout.addComponents(httpStatusField, headersField, titleField, linksField, metaField,
                    crawlingForbiddenField, robotsTxtField);
            resultLayout.addComponent(layout);
        } catch (Exception e) {
            Label errorLabel = new Label(e.getMessage());
            errorLabel.setSizeFull();
            errorLabel.addStyleName(ValoTheme.LABEL_FAILURE);
            resultLayout.addComponent(errorLabel);
        }
    }
}