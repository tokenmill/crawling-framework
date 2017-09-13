package lt.tokenmill.crawling.adminui.view.namedquery;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.ui.*;
import lt.tokenmill.crawling.adminui.view.BaseView;
import lt.tokenmill.crawling.commonui.ElasticSearch;
import lt.tokenmill.crawling.data.NamedQuery;
import lt.tokenmill.crawling.data.PageableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

public class NamedQueriesView extends BaseView {

    private static final Logger LOG = LoggerFactory.getLogger(NamedQueriesView.class);

    private Grid itemsGrid = new Grid(new GeneratedPropertyContainer(new BeanItemContainer<>(NamedQuery.class)));
    private Label totalCountLabel = new Label();
    private TextField filterField = new TextField();

    public NamedQueriesView() {
        super("Named Queries");
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidth(100, PERCENTAGE);
        mainLayout.setHeight(100, PERCENTAGE);
        mainLayout.setSpacing(true);

        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSpacing(true);
        gridLayout.setWidth(100, PERCENTAGE);


        // Search field and create new button
        filterField.setInputPrompt("Enter Name...");
        filterField.addTextChangeListener(event -> refreshGrid(event.getText()));

        Button addNewButton = new Button("Add New Query");
        addNewButton.addClickListener(event -> showNamedQueryForm(new NamedQuery()));

        HorizontalLayout actionHeader = new HorizontalLayout(filterField, addNewButton);
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
                    NamedQuery nq = (NamedQuery) itemsGrid.getSelectedRow();
                    if (nq != null) {
                        nq = ElasticSearch.getNamedQueryOperations().get(nq.getName());
                        showNamedQueryForm(nq);
                    }
                });
        itemsGrid.setColumns("name");
        gridLayout.addComponent(itemsGrid);
        gridLayout.addComponent(totalCountLabel);
        refreshGrid(filterField.getValue());
        mainLayout.addComponent(gridLayout);
        mainLayout.setExpandRatio(gridLayout, 1f);
        addComponent(mainLayout);
    }

    private void refreshGrid(String text) {
        PageableList<NamedQuery> data = ElasticSearch.getNamedQueryOperations().filter(text);
        itemsGrid.getContainerDataSource().removeAllItems();
        for (NamedQuery nq : data.getItems()) {
            itemsGrid.getContainerDataSource().addItem(nq);
        }
        totalCountLabel.setValue(String.format("Total count: %d", data.getTotalCount()));
        LOG.info("Refreshed grid using filter '{}'. Total items: {}", text, data.getTotalCount());
    }

    private void showNamedQueryForm(NamedQuery nq) {
        NamedQueryFormWindow formWindow = new NamedQueryFormWindow(nq);
        formWindow.addAfterUpdateListener(() -> refreshGrid(filterField.getValue()));
        UI.getCurrent().addWindow(formWindow);
    }
}
