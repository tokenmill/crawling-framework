package lt.tokenmill.crawling.adminui.view.sourcetest;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
import lt.tokenmill.crawling.adminui.utils.GridUtils.UrlToLinkConverter;
import lt.tokenmill.crawling.adminui.view.BaseView;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.HttpSourceTest;
import lt.tokenmill.crawling.data.PageableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

public class HttpSourceTestsView extends BaseView {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSourceTestsView.class);

    private Grid itemsGrid = new Grid(new GeneratedPropertyContainer(new BeanItemContainer<>(HttpSourceTest.class)));
    private Label totalCountLabel = new Label();
    private TextField filterField = new TextField();

    public HttpSourceTestsView() {
        super("HTTP Source Tests");
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidth(100, PERCENTAGE);
        mainLayout.setHeight(100, PERCENTAGE);
        mainLayout.setSpacing(true);

        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSpacing(true);
        gridLayout.setWidth(100, PERCENTAGE);


        // Search field and create new button
        filterField.setInputPrompt("Enter URL...");
        filterField.addTextChangeListener(event -> refreshGrid(event.getText()));

        Button testAllButton = new Button("Run All Tests");
        testAllButton.addClickListener(event -> testAll());


        Button addNewButton = new Button("Add New Test");
        addNewButton.addClickListener(event -> showHttpSourceTestForm(new HttpSourceTest()));

        HorizontalLayout actionHeader = new HorizontalLayout(filterField, testAllButton, addNewButton);
        actionHeader.setSpacing(true);
        actionHeader.setWidth(100, PERCENTAGE);
        filterField.setWidth(100, PERCENTAGE);
        actionHeader.setExpandRatio(filterField, 1.0f);
        gridLayout.addComponent(actionHeader);

        // Grid
        itemsGrid.setWidth(100, PERCENTAGE);
        itemsGrid.setHeight(700, PIXELS);
        itemsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        itemsGrid.addSelectionListener(
                e -> {
                    HttpSourceTest hst = (HttpSourceTest) itemsGrid.getSelectedRow();
                    hst = ElasticSearch.getHttpSourceTestOperations().get(hst.getUrl());
                    showHttpSourceTestForm(hst);
                });
        itemsGrid.getColumn("source").setRenderer(new HtmlRenderer(), new UrlToLinkConverter());
        itemsGrid.getColumn("url").setRenderer(new HtmlRenderer(), new UrlToLinkConverter());
        itemsGrid.setColumns("source", "url");
        gridLayout.addComponent(itemsGrid);
        gridLayout.addComponent(totalCountLabel);
        refreshGrid(filterField.getValue());
        mainLayout.addComponent(gridLayout);
        mainLayout.setExpandRatio(gridLayout, 1f);
        addComponent(mainLayout);
    }

    private void refreshGrid(String text) {
        PageableList<HttpSourceTest> data = ElasticSearch.getHttpSourceTestOperations().filter(text);
        itemsGrid.getContainerDataSource().removeAllItems();
        for (HttpSourceTest hst : data.getItems()) {
            itemsGrid.getContainerDataSource().addItem(hst);
        }
        totalCountLabel.setValue(String.format("Total count: %d", data.getTotalCount()));
        LOG.info("Refreshed grid using filter '{}'. Total items: {}", text, data.getTotalCount());
    }

    private void showHttpSourceTestForm(HttpSourceTest hst) {
        HttpSourceTestFormWindow formWindow = new HttpSourceTestFormWindow(hst);
        formWindow.addAfterUpdateListener(() -> refreshGrid(filterField.getValue()));
        UI.getCurrent().addWindow(formWindow);
    }

    private void testAll() {
        HttpSourceAllTestsWindow window = new HttpSourceAllTestsWindow();
        UI.getCurrent().addWindow(window);
    }
}
