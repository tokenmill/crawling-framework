package lt.tokenmill.crawling.adminui.view.namedquery;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.NamedQuery;
import lt.tokenmill.crawling.data.PageableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NamedQueryFormWindow extends Window {

    private static final Logger LOG = LoggerFactory.getLogger(NamedQueryFormWindow.class);

    private List<Runnable> afterUpdateListeners = Lists.newArrayList();

    private Button saveButton = new Button("Save", (event) -> this.save());
    private Button cancelButton = new Button("Cancel", (event) -> this.close());
    private Button deleteButton = new Button("Delete", (event) -> this.delete());
    private Button testButton = new Button("Test", (event) -> this.test());

    private Panel resultsPanel = new Panel();

    private TextField nameField = new TextField("Name");
    private TextArea stemmedCSField = new TextArea("Case Sensitive Query on Stemmed Text");
    private TextArea stemmedCIField = new TextArea("Case Insensitive Query on Stemmed Text");
    private TextArea notStemmedCSField = new TextArea("Case Sensitive Query on Non-Stemmed Text");
    private TextArea notStemmedCIField = new TextArea("Case Insensitive Query on Non-Stemmed Text");
    private TextArea advancedField = new TextArea("Advanced Query");

    public NamedQueryFormWindow(NamedQuery nq) {
        setCaption("Named Query");
        setModal(true);
        center();
        setWidth(80, Unit.PERCENTAGE);
        setHeight(80, Unit.PERCENTAGE);

        VerticalLayout mainLayout = new VerticalLayout();

        FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(true);
        formLayout.setMargin(true);

        saveButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        deleteButton.setStyleName(ValoTheme.BUTTON_DANGER);
        deleteButton.setVisible(!Strings.isNullOrEmpty(nq.getName()));

        HorizontalLayout actions = new HorizontalLayout(saveButton, testButton, cancelButton, deleteButton);
        actions.setSpacing(true);

        nameField.setSizeFull();
        nameField.setRequired(true);
        nameField.setRequiredError("Name is Required");
        nameField.setValue(Strings.nullToEmpty(nq.getName()));

        stemmedCSField.setSizeFull();
        stemmedCSField.setRows(2);
        stemmedCSField.setValue(Strings.nullToEmpty(nq.getStemmedCaseSensitive()));

        stemmedCIField.setSizeFull();
        stemmedCIField.setRows(2);
        stemmedCIField.setValue(Strings.nullToEmpty(nq.getStemmedCaseInSensitive()));

        notStemmedCSField.setSizeFull();
        notStemmedCSField.setRows(2);
        notStemmedCSField.setValue(Strings.nullToEmpty(nq.getNotStemmedCaseSensitive()));

        notStemmedCIField.setSizeFull();
        notStemmedCIField.setRows(2);
        notStemmedCIField.setValue(Strings.nullToEmpty(nq.getNotStemmedCaseInSensitive()));

        advancedField.setSizeFull();
        advancedField.setRows(2);
        advancedField.setValue(Strings.nullToEmpty(nq.getAdvanced()));

        formLayout.addComponents(nameField, stemmedCSField, stemmedCIField,
                notStemmedCSField, notStemmedCIField, advancedField);

        formLayout.addComponents(actions);

        formLayout.addComponent(resultsPanel);

        mainLayout.addComponents(formLayout, resultsPanel);

        setContent(mainLayout);
    }

    private void save() {
        try {
            nameField.validate();

            NamedQuery data = mapFieldsToNamedQuery();

            ElasticSearch.getNamedQueryOperations().save(data);
            Notification.show(String.format("Saved '%s'", nameField.getValue()), Notification.Type.TRAY_NOTIFICATION);
            afterUpdate();
            close();
        } catch (Validator.InvalidValueException e) {
        } catch (Exception e) {
            LOG.error("Error while saving data", e);
        }
    }

    private void delete() {
        LOG.info("Deleting Named Query data with name '{}'", nameField.getValue());
        NamedQuery nq = ElasticSearch.getNamedQueryOperations().get(nameField.getValue());
        ElasticSearch.getNamedQueryOperations().delete(nq);
        Notification.show(String.format("Deleted '%s'", nameField.getValue()), Notification.Type.TRAY_NOTIFICATION);
        afterUpdate();
        close();
    }

    private void test() {
        NamedQuery nq = mapFieldsToNamedQuery();
        PageableList<HttpArticle> documents = ElasticSearch.getDocumentOperations().query(nq);
        LOG.info("{} documents found when testing named query '{}'", documents.getTotalCount(), nq.getName());
        resultsPanel.setContent(new NamedQueryResultsPanel(documents));
        resultsPanel.setVisible(true);
    }

    public void addAfterUpdateListener(Runnable listener) {
        afterUpdateListeners.add(listener);
    }

    private void afterUpdate() {
        afterUpdateListeners.forEach(Runnable::run);
    }

    private NamedQuery mapFieldsToNamedQuery() {
        NamedQuery data = new NamedQuery();
        data.setName(nameField.getValue());
        data.setStemmedCaseSensitive(stemmedCSField.getValue());
        data.setStemmedCaseInSensitive(stemmedCIField.getValue());
        data.setNotStemmedCaseSensitive(notStemmedCSField.getValue());
        data.setNotStemmedCaseInSensitive(notStemmedCIField.getValue());
        data.setAdvanced(advancedField.getValue());
        return data;
    }
}