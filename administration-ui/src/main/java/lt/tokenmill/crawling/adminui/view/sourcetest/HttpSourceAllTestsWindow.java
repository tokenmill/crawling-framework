package lt.tokenmill.crawling.adminui.view.sourcetest;

import com.google.common.collect.Lists;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.HttpSourceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HttpSourceAllTestsWindow extends Window {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSourceAllTestsWindow.class);

    private List<Runnable> afterUpdateListeners = Lists.newArrayList();

    private Button cancelButton = new Button("Close", (event) -> this.close());

    public HttpSourceAllTestsWindow() {
        setCaption("All Tests");
        setModal(true);
        center();
        setWidth(80, Unit.PERCENTAGE);
        setHeight(80, Unit.PERCENTAGE);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);

        List<HttpSourceTest> tests = ElasticSearch.getHttpSourceTestOperations().all();
        for (HttpSourceTest test : tests) {
            HttpSource source = ElasticSearch.getHttpSourceOperations().get(test.getSource());
            if (source == null) {
                Label noSourceLabel = new Label(String.format("Source configuration '%s' not found", test.getSource()));
                noSourceLabel.addStyleName(ValoTheme.LABEL_FAILURE);
                noSourceLabel.setSizeFull();
                mainLayout.addComponent(noSourceLabel);
            } else {
                mainLayout.addComponent(new TestResultsPanel(source, test));
            }
        }

        HorizontalLayout actions = new HorizontalLayout(cancelButton);
        actions.setSpacing(true);

        setContent(mainLayout);
    }
}