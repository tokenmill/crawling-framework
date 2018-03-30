package lt.tokenmill.crawling.adminui.view;

import com.google.common.base.Joiner;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.PageableList;
import lt.tokenmill.crawling.es.model.DateHistogramValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

public class HttpSourcesView extends BaseView {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSourcesView.class);

    private Grid itemsGrid = new Grid(new GeneratedPropertyContainer(new BeanItemContainer<>(HttpSource.class)));
    private Label totalCountLabel = new Label();
    private TextField filterField = new TextField();
    private HorizontalLayout pagingRow = new HorizontalLayout();
    ;
    private long totalCount = 0;
    private int currentPage = 1;

    public HttpSourcesView() {
        super("HTTP Sources");
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidth(100, PERCENTAGE);
        mainLayout.setHeight(100, PERCENTAGE);
        mainLayout.setSpacing(true);

        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSpacing(true);
        gridLayout.setWidth(100, PERCENTAGE);

        HttpSourceForm form = new HttpSourceForm() {
            @Override
            public void afterUpdate() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                refreshGrid(filterField.getValue());
            }
        };
        form.setWidth(100, PERCENTAGE);

        // Search field and create new button
        filterField.setInputPrompt("Enter search text...");
        filterField.addTextChangeListener(event -> {
            this.currentPage = 1;
            refreshGrid(event.getText());
        });
        Button addNewButton = new Button("Add New (Alt + N)");
        addNewButton.addClickListener(event -> form.edit(new HttpSource()));
        addShortcutListener(new Button.ClickShortcut(addNewButton, ShortcutAction.KeyCode.N, ShortcutAction.ModifierKey.ALT));

        HorizontalLayout actionHeader = new HorizontalLayout(filterField, addNewButton);
        actionHeader.setSpacing(true);
        actionHeader.setWidth(100, PERCENTAGE);
        filterField.setWidth(100, PERCENTAGE);
        actionHeader.setExpandRatio(filterField, 1.0f);
        gridLayout.addComponent(actionHeader);

        // Grid
        itemsGrid.setWidth(100, PERCENTAGE);
        itemsGrid.setHeight(750, PIXELS);
        itemsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        itemsGrid.addSelectionListener(
                e -> form.edit((HttpSource) itemsGrid.getSelectedRow()));
        itemsGrid.getColumn("name").setConverter(new StringTrimmer(50));
        itemsGrid.getColumn("url").setRenderer(new HtmlRenderer(), new UrlToLinkConverter());
        itemsGrid.getColumn("enabled");


        ((GeneratedPropertyContainer) itemsGrid.getContainerDataSource())
                .addGeneratedProperty("test", new ButtonPropertyGenerator("Test"));
        ((GeneratedPropertyContainer) itemsGrid.getContainerDataSource())
                .addGeneratedProperty("statistics", new StatsButtonPropertyGenerator("Stats"));
        itemsGrid.getColumn("test").setRenderer(new ButtonRenderer(e -> {
            HttpSource hs = (HttpSource) e.getItemId();
            hs = ElasticSearch.getHttpSourceOperations().get(hs.getUrl());
            UI.getCurrent().addWindow(new HttpSourceTestWindow(hs));

        }));
        itemsGrid.getColumn("statistics").setRenderer(new ButtonRenderer(e -> {
            HttpSource hs = (HttpSource) e.getItemId();
            UI.getCurrent().addWindow(new HttpSourceStatsWindow(hs.getUrl()));
        }));
        itemsGrid.setColumns("name", "url", "enabled", "test", "statistics");
        gridLayout.addComponent(itemsGrid);
        gridLayout.addComponent(totalCountLabel);
        refreshGrid(filterField.getValue());
        mainLayout.addComponent(gridLayout);
        mainLayout.setExpandRatio(gridLayout, 0.55f);

        mainLayout.addComponent(form);
        mainLayout.setExpandRatio(form, 0.45f);
        addComponent(mainLayout);
        gridLayout.addComponent(pagingRow);
    }

    private void refreshPagingRow() {
        pagingRow.removeAllComponents();
        pagingRow.addComponent(new Label("Pages: "));
        long amountOfPages = this.totalCount / 100;
        amountOfPages = amountOfPages + (this.totalCount % 100 != 0 ? 1 : 0);
        for (int i = 1; i <= amountOfPages; i++) {
            String buttonLabel = String.valueOf(i);
            Button button = new Button();
            if (i == currentPage) {
                buttonLabel = ">>" + buttonLabel + "<<";
            }
            button.setCaption(buttonLabel);
            button.setIconAlternateText(String.valueOf(i));
            button.addClickListener(clickEvent -> {
                this.currentPage = (Integer.parseInt(clickEvent.getButton().getIconAlternateText()));
                refreshGrid(filterField.getValue());
            });
            pagingRow.addComponent(button);
        }
    }

    private int getOffset() {
        return (this.currentPage - 1) * 100;
    }

    private void refreshGrid(String text) {
        LOG.info("Refreshing grid using filter '{}'", text);
        PageableList<HttpSource> data = ElasticSearch.getHttpSourceOperations().filter(text, getOffset());
        itemsGrid.getContainerDataSource().removeAllItems();
        for (HttpSource source : data.getItems()) {
            itemsGrid.getContainerDataSource().addItem(source);
        }
        this.totalCount = data.getTotalCount();
        totalCountLabel.setValue(String.format("Total count: %d", data.getTotalCount()));
        refreshPagingRow();
    }

    private static class StringListConverter implements Converter<String, List> {
        @Override
        public List convertToModel(String s, Class<? extends List> aClass, Locale locale) throws ConversionException {
            return new ArrayList();
        }

        @Override
        public String convertToPresentation(List list, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return Joiner.on(", ").join(list);
        }

        @Override
        public Class<List> getModelType() {
            return List.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }

    private static class StringTrimmer implements Converter<String, String> {

        private int maxLength;

        public StringTrimmer(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public String convertToModel(String string, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return string;
        }

        @Override
        public String convertToPresentation(String string, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return string != null && string.length() > maxLength ? string.substring(0, maxLength) + "..." : string;
        }

        @Override
        public Class<String> getModelType() {
            return String.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }

    private static class UrlToLinkConverter implements Converter<String, String> {

        @Override
        public String convertToModel(String string, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return string;
        }

        @Override
        public String convertToPresentation(String string, Class<? extends String> aClass, Locale locale) throws ConversionException {
            return String.format("<a href=\"%s\" target=\"_blank\">%s</a>", string, string);
        }

        @Override
        public Class<String> getModelType() {
            return String.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }

    private static class StatsButtonPropertyGenerator extends PropertyValueGenerator<String> {


        private String name;

        public StatsButtonPropertyGenerator(String name) {
            this.name = name;
        }

        private Long lastHistogramValue(List<DateHistogramValue> coll) {
            return coll.size() == 0 ? 0 : coll.get(coll.size() - 1).getValue();
        }

        @Override
        public String getValue(Item item, Object itemId, Object propertyId) {
            HttpSource hs = (HttpSource) itemId;
            List<DateHistogramValue> urls = ElasticSearch.getUrlOperations().calculateStats(hs.getUrl());
            List<DateHistogramValue> documents = ElasticSearch.getDocumentOperations().calculateStats(hs.getUrl());

            return String.format("%s (%4d/%4d)", name, lastHistogramValue(urls), lastHistogramValue(documents));
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }

    private static class ButtonPropertyGenerator extends PropertyValueGenerator<String> {


        private String name;

        public ButtonPropertyGenerator(String name) {
            this.name = name;
        }

        @Override
        public String getValue(Item item, Object itemId, Object propertyId) {
            return name;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }
}
