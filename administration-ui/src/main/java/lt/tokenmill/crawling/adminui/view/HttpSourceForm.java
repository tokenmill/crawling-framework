package lt.tokenmill.crawling.adminui.view;

import com.google.common.base.Strings;
import com.vaadin.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.es.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static lt.tokenmill.crawling.es.Utils.linesToList;

public class HttpSourceForm extends FormLayout {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSourceForm.class);

    private Button saveButton = new Button("Save (Alt + S)", (event) -> this.save());
    private Button cancelButton = new Button("Cancel (Alt + C)", (event) -> this.cancel());
    private Button deleteButton = new Button("Delete", (event) -> this.delete());
    private Button testButton = new Button("Test", (event) -> this.test());



    private TextField urlField = new TextField("URL");
    private TextField textField = new TextField("Name");
    private CheckBox enabledField = new CheckBox("Enabled");
    private CheckBox discoveryEnabledField = new CheckBox("Discovery Enabled");
    private TextField languageField = new TextField("Default Language");
    private TextField timezoneField = new TextField("Default Timezone");
    private TextField urlRecrawlDelayField = new TextField("URL Recrawl Delay (seconds)");
    private TextField sitemapRecrawlDelayField = new TextField("Sitemap Recrawl Delay (seconds)");
    private TextField feedRecrawlDelayField = new TextField("Feed Recrawl Delay (seconds)");
    private TextArea urlsField = new TextArea("URLs");
    private TextArea sitemapsField = new TextArea("Sitemaps");
    private TextArea feedsField = new TextArea("Feeds");
    private TextArea categoriesField = new TextArea("Categories");
    private TextArea appIdsField = new TextArea("Application IDs");
    private TextArea urlFiltersField = new TextArea("URL Filters");
    private TextArea urlNormalizersField = new TextArea("URL Normalizers");
    private TextArea titleSelectorsField = new TextArea("Title Selectors");
    private TextArea textSelectorsField = new TextArea("Text Selectors");
    private TextArea textNormalizerField = new TextArea("Text Normalizers");
    private TextArea dateSelectorsField = new TextArea("Date Selectors");
    private TextArea dateRegexpsField = new TextArea("Date Regexps");
    private TextArea dateFormatsField = new TextArea("Date Formats");

    public HttpSourceForm() {
        addShortcutListener(new Button.ClickShortcut(saveButton,
                ShortcutAction.KeyCode.S, ShortcutAction.ModifierKey.ALT));
        addShortcutListener(new Button.ClickShortcut(cancelButton,
                ShortcutAction.KeyCode.C, ShortcutAction.ModifierKey.ALT));
        setMargin(true);

        saveButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        deleteButton.setStyleName(ValoTheme.BUTTON_DANGER);
        HorizontalLayout actions = new HorizontalLayout(saveButton, testButton, cancelButton, deleteButton);
        actions.setSpacing(true);

        urlField.setSizeFull();
        urlField.setRequired(true);
        urlField.setRequiredError("URL is Required");

        textField.setSizeFull();
        textField.setRequired(true);
        textField.setRequiredError("Name is Required");

        enabledField.setSizeFull();

        discoveryEnabledField.setSizeFull();

        languageField.setSizeFull();
        languageField.setMaxLength(2);

        timezoneField.setSizeFull();

        urlRecrawlDelayField.setSizeFull();
        urlRecrawlDelayField.setConverter(Integer.class);
        urlRecrawlDelayField.setNullRepresentation("");
        sitemapRecrawlDelayField.setSizeFull();
        sitemapRecrawlDelayField.setConverter(Integer.class);
        sitemapRecrawlDelayField.setNullRepresentation("");
        feedRecrawlDelayField.setSizeFull();
        feedRecrawlDelayField.setConverter(Integer.class);
        feedRecrawlDelayField.setNullRepresentation("");

        urlsField.setSizeFull();
        feedsField.setSizeFull();
        sitemapsField.setSizeFull();
        categoriesField.setSizeFull();
        appIdsField.setSizeFull();

        urlFiltersField.setSizeFull();
        urlNormalizersField.setSizeFull();
        titleSelectorsField.setSizeFull();
        textSelectorsField.setSizeFull();
        textNormalizerField.setSizeFull();
        dateSelectorsField.setSizeFull();
        dateFormatsField.setSizeFull();
        dateRegexpsField.setSizeFull();


        addComponents(urlField, textField,
                enabledField, discoveryEnabledField,
                languageField, timezoneField,
                urlRecrawlDelayField, sitemapRecrawlDelayField, feedRecrawlDelayField,
                urlsField, feedsField, sitemapsField,
                categoriesField, appIdsField,
                urlFiltersField, urlNormalizersField,
                titleSelectorsField,
                textSelectorsField, textNormalizerField,
                dateSelectorsField, dateRegexpsField, dateFormatsField);

        addComponents(actions);
        setVisible(false);
    }

    private void save() {
        try {
            urlField.validate();
            textField.validate();

            HttpSource data = mapFieldsToSource();

            ElasticSearch.getHttpSourceOperations().save(data);
            setVisible(false);
            Notification.show(String.format("Saved '%s'", urlField.getValue()), Notification.Type.TRAY_NOTIFICATION);
            reset();
            afterUpdate();
        } catch (Validator.InvalidValueException e) {
        } catch (Exception e) {
            LOG.error("Error while saving data", e);
        }
    }


    private void cancel() {
        setVisible(false);
        reset();
        afterUpdate();
    }

    private void delete() {
        LOG.info("Deleting data for url '{}'", urlField.getValue());
        ElasticSearch.getHttpSourceOperations().delete(urlField.getValue());
        setVisible(false);
        Notification.show(String.format("Deleted '%s'", urlField.getValue()), Notification.Type.TRAY_NOTIFICATION);
        reset();
        afterUpdate();
    }

    private void test() {
        HttpSource source = mapFieldsToSource();
        UI.getCurrent().addWindow(new HttpSourceTestWindow(source));
    }

    void edit(HttpSource data) {
        if (data != null) {
            textField.setValue(Strings.nullToEmpty(data.getName()));
            urlField.setValue(Strings.nullToEmpty(data.getUrl()));
            enabledField.setValue(data.isEnabled());
            discoveryEnabledField.setValue(data.isDiscoveryEnabled());
            languageField.setValue(Strings.nullToEmpty(data.getLanguage()));
            timezoneField.setValue(Strings.nullToEmpty(data.getTimezone()));
            urlRecrawlDelayField.setConvertedValue(data.getUrlRecrawlDelayInSecs());
            sitemapRecrawlDelayField.setConvertedValue(data.getSitemapRecrawlDelayInSecs());
            feedRecrawlDelayField.setConvertedValue(data.getFeedRecrawlDelayInSecs());
            urlsField.setValue(Utils.listToText(data.getUrls()));
            feedsField.setValue(Utils.listToText(data.getFeeds()));
            sitemapsField.setValue(Utils.listToText(data.getSitemaps()));
            categoriesField.setValue(Utils.listToText(data.getCategories()));
            appIdsField.setValue(Utils.listToText(data.getAppIds()));
            appIdsField.setInputPrompt("One appId per line.");
            urlFiltersField.setValue(Utils.listToText(data.getUrlFilters()));
            urlFiltersField.setInputPrompt("e.g. +^http://www.tokenmill.lt/.*\ne.g. -.*apache.*");
            urlNormalizersField.setValue(Utils.listToText(data.getUrlNormalizers()));
            urlNormalizersField.setInputPrompt("e.g. a-->>b");
            titleSelectorsField.setValue(Utils.listToText(data.getTitleSelectors()));
            textSelectorsField.setValue(Utils.listToText(data.getTextSelectors()));
            textNormalizerField.setValue(Utils.listToText(data.getTextNormalizers()));
            textNormalizerField.setInputPrompt("e.g. a-->>b");
            dateSelectorsField.setValue(Utils.listToText(data.getDateSelectors()));
            dateFormatsField.setValue(Utils.listToText(data.getDateFormats()));
            dateFormatsField.setInputPrompt("'Posted' MMMM dd, yyyy");
            dateRegexpsField.setValue(Utils.listToText(data.getDateRegexps()));
            dateRegexpsField.setInputPrompt("before date (.*) after");
            if (Strings.emptyToNull(data.getUrl()) == null) {
                urlField.focus();
            } else {
                textField.focus();
            }
        }
        LOG.info("Showing form for item '{}'", data != null ? data.getUrl() : null);
        setVisible(data != null);
    }

    public void setShowDeleteButton(boolean showDeleteButton) {
        this.deleteButton.setVisible(showDeleteButton);
    }

    public void reset() {
        textField.setValue("");
        urlField.setValue("");
        enabledField.setValue(false);
        discoveryEnabledField.setValue(false);
        languageField.setValue("");
        timezoneField.setValue("");
        urlRecrawlDelayField.setValue("");
        feedRecrawlDelayField.setValue("");
        sitemapRecrawlDelayField.setValue("");
        urlsField.setValue("");
        feedsField.setValue("");
        sitemapsField.setValue("");
        categoriesField.setValue("");
        appIdsField.setValue("");
        appIdsField.setInputPrompt("One appId per line.");
        urlFiltersField.setValue("");
        urlFiltersField.setInputPrompt("e.g. +^http://www.tokenmill.lt/.*\ne.g. -.*apache.*");
        urlNormalizersField.setValue("");
        urlNormalizersField.setInputPrompt("e.g. a-->>b");
        titleSelectorsField.setValue("");
        textSelectorsField.setValue("");
        textNormalizerField.setValue("");
        textNormalizerField.setInputPrompt("e.g. a-->>b");
        dateSelectorsField.setValue("");
        dateFormatsField.setValue("");
        dateFormatsField.setInputPrompt("'Posted' MMMM dd, yyyy");
        dateRegexpsField.setValue("");
        dateRegexpsField.setInputPrompt("before date (.*) after");
    }

    public void afterUpdate() {

    }

    private HttpSource mapFieldsToSource() {
        HttpSource data = new HttpSource();
        data.setName(textField.getValue());
        data.setUrl(urlField.getValue());
        data.setEnabled(enabledField.getValue());
        data.setDiscoveryEnabled(discoveryEnabledField.getValue());
        data.setLanguage(languageField.getValue());
        data.setTimezone(timezoneField.getValue());
        data.setUrlRecrawlDelayInSecs((Integer) urlRecrawlDelayField.getConvertedValue());
        data.setFeedRecrawlDelayInSecs((Integer) feedRecrawlDelayField.getConvertedValue());
        data.setSitemapRecrawlDelayInSecs((Integer) sitemapRecrawlDelayField.getConvertedValue());
        data.setUrls(linesToList(urlsField.getValue()));
        data.setFeeds(linesToList(feedsField.getValue()));
        data.setSitemaps(linesToList(sitemapsField.getValue()));
        data.setCategories(linesToList(categoriesField.getValue()));
        data.setAppIds(linesToList(appIdsField.getValue()));
        data.setUrlFilters(linesToList(urlFiltersField.getValue()));
        data.setUrlNormalizers(linesToList(urlNormalizersField.getValue()));
        data.setTitleSelectors(linesToList(titleSelectorsField.getValue()));
        data.setTextSelectors(linesToList(textSelectorsField.getValue()));
        data.setTextNormalizers(linesToList(textNormalizerField.getValue()));
        data.setDateSelectors(linesToList(dateSelectorsField.getValue()));
        data.setDateRegexps(linesToList(dateRegexpsField.getValue()));
        data.setDateFormats(linesToList(dateFormatsField.getValue()));
        return data;
    }
}