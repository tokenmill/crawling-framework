package lt.tokenmill.crawling.adminui.view.sourcetest;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.HttpSourceTest;
import lt.tokenmill.crawling.parser.utils.HttpSourceTester;

import java.util.Map;

public class TestResultsPanel extends Panel {

    private Map<String, HttpSourceTester.Difference> difference;

    public TestResultsPanel(HttpSource source, HttpSourceTest test) {
        this.difference = HttpSourceTester.test(source, test);
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        if (this.difference.isEmpty()) {
            Label resultLabel = new Label(String.format("'%s' Test Passed", test.getUrl()));
            resultLabel.addStyleName(ValoTheme.LABEL_SUCCESS);
            resultLabel.setSizeFull();
            layout.addComponent(resultLabel);
        } else {
            Label resultLabel = new Label(String.format("'%s' Test Failed", test.getUrl()));
            resultLabel.addStyleName(ValoTheme.LABEL_FAILURE);
            resultLabel.setSizeFull();
            layout.addComponent(resultLabel);
        }

        for (Map.Entry<String, HttpSourceTester.Difference> diff : difference.entrySet()) {
            HorizontalLayout fieldLayout = new HorizontalLayout();
            fieldLayout.setSizeFull();

            Label resultLabel = new Label(diff.getKey());
            resultLabel.addStyleName(ValoTheme.LABEL_LARGE);
            fieldLayout.addComponent(resultLabel);
            fieldLayout.setComponentAlignment(resultLabel, Alignment.MIDDLE_CENTER);
            fieldLayout.setExpandRatio(resultLabel, 0.15f);

            FormLayout valuesLayout = new FormLayout();
            valuesLayout.setWidth(100, Unit.PERCENTAGE);
            valuesLayout.setSizeFull();

            TextArea expected = new TextArea("Expected");
            expected.setSizeFull();
            expected.setRows(2);
            expected.setValue(diff.getValue().getExpected());
            expected.setReadOnly(true);

            TextArea actual = new TextArea("Actual");
            actual.setSizeFull();
            actual.setRows(2);
            actual.setValue(diff.getValue().getActual());
            actual.setReadOnly(true);

            valuesLayout.addComponents(expected, actual);

            fieldLayout.addComponent(valuesLayout);
            fieldLayout.setExpandRatio(valuesLayout, 0.85f);

            layout.addComponent(fieldLayout);
        }

        setContent(layout);
    }

    public boolean passed() {
        return difference != null && difference.isEmpty();
    }
}
