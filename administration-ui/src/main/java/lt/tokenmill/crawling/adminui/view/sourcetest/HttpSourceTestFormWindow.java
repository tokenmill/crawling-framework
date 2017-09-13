package lt.tokenmill.crawling.adminui.view.sourcetest;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.HttpSourceTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HttpSourceTestFormWindow extends Window {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSourceTestFormWindow.class);

    private List<Runnable> afterUpdateListeners = Lists.newArrayList();

    private Button saveButton = new Button("Save", (event) -> this.save());
    private Button cancelButton = new Button("Cancel", (event) -> this.close());
    private Button deleteButton = new Button("Delete", (event) -> this.delete());
    private Button testButton = new Button("Test", (event) -> this.test());

    private Panel resultsPanel = new Panel();

    private TextField sourceField = new TextField("Source");
    private TextField urlField = new TextField("URL");
    private TextArea htmlField = new TextArea("HTML");
    private CheckBox urlAcceptedField = new CheckBox("URL Accepted");
    private TextArea titleField = new TextArea("Title");
    private TextArea textField = new TextArea("Text");
    private TextField dateField = new TextField("Date");

    public HttpSourceTestFormWindow(HttpSourceTest hst) {
        setCaption("HTTP Source Test");
        setModal(true);
        center();
        setWidth(80, Unit.PERCENTAGE);
        setHeight(80, Unit.PERCENTAGE);

        FormLayout mainLayout = new FormLayout();

        mainLayout.setMargin(true);

        saveButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        deleteButton.setStyleName(ValoTheme.BUTTON_DANGER);
        deleteButton.setVisible(!Strings.isNullOrEmpty(hst.getUrl()));

        HorizontalLayout actions = new HorizontalLayout(saveButton, testButton, cancelButton, deleteButton);
        actions.setSpacing(true);

        sourceField.setSizeFull();
        sourceField.setRequired(true);
        sourceField.setRequiredError("Source is Required");
        sourceField.setValue(Strings.nullToEmpty(hst.getSource()));

        urlField.setSizeFull();
        urlField.setRequired(true);
        urlField.setRequiredError("URL is Required");
        urlField.setValue(Strings.nullToEmpty(hst.getUrl()));

        htmlField.setSizeFull();
        htmlField.setRows(12);
        htmlField.setValue(Strings.nullToEmpty(hst.getHtml()));

        urlAcceptedField.setSizeFull();
        urlAcceptedField.setValue(hst.getUrlAccepted() != null ? hst.getUrlAccepted() : false);

        titleField.setSizeFull();
        titleField.setRows(2);
        titleField.setValue(Strings.nullToEmpty(hst.getTitle()));

        textField.setSizeFull();
        textField.setRows(5);
        textField.setValue(Strings.nullToEmpty(hst.getText()));

        dateField.setSizeFull();
        dateField.setValue(Strings.nullToEmpty(hst.getDate()));

        mainLayout.addComponents(sourceField, urlField, htmlField,
                urlAcceptedField, textField, titleField, dateField);

        mainLayout.addComponents(actions);

        resultsPanel.setVisible(false);
        mainLayout.addComponent(resultsPanel);

        setContent(mainLayout);
    }

    private void save() {
        try {
            urlField.validate();
            sourceField.validate();

            HttpSourceTest data = mapFieldsToSourceTest();

            ElasticSearch.getHttpSourceTestOperations().save(data);
            setVisible(false);
            Notification.show(String.format("Saved '%s'", urlField.getValue()), Notification.Type.TRAY_NOTIFICATION);
            afterUpdate();
            close();
        } catch (Validator.InvalidValueException e) {
        } catch (Exception e) {
            LOG.error("Error while saving data", e);
        }
    }

    private void delete() {
        LOG.info("Deleting HTTP Source test data for url '{}'", urlField.getValue());
        ElasticSearch.getHttpSourceTestOperations().delete(urlField.getValue());
        setVisible(false);
        Notification.show(String.format("Deleted '%s'", urlField.getValue()), Notification.Type.TRAY_NOTIFICATION);
        afterUpdate();
        close();
    }

    private void test() {
        HttpSourceTest test = new HttpSourceTest();
        test.setHtml(htmlField.getValue());
        test.setUrl(urlField.getValue());
        test.setUrlAccepted(urlAcceptedField.getValue());
        test.setTitle(titleField.getValue());
        test.setText(textField.getValue());
        test.setDate(dateField.getValue());
        HttpSource source = ElasticSearch.getHttpSourceOperations().get(sourceField.getValue());
        if (source == null) {
            resultsPanel.setVisible(false);
            Notification.show(String.format("No source configuration found for '%s'", sourceField.getValue()), Notification.Type.ERROR_MESSAGE);
        } else {
            resultsPanel.setContent(new TestResultsPanel(source, test));
            resultsPanel.setVisible(true);
        }
    }

    public void addAfterUpdateListener(Runnable listener) {
        afterUpdateListeners.add(listener);
    }

    private void afterUpdate() {
        afterUpdateListeners.forEach(Runnable::run);
    }

    private HttpSourceTest mapFieldsToSourceTest() {
        HttpSourceTest data = new HttpSourceTest();
        data.setSource(sourceField.getValue());
        data.setUrl(urlField.getValue());
        data.setHtml(htmlField.getValue());
        data.setUrlAccepted(urlAcceptedField.getValue());
        data.setTitle(titleField.getValue());
        data.setText(textField.getValue());
        data.setDate(dateField.getValue());
        return data;
    }
}