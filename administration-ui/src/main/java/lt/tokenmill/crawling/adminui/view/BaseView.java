package lt.tokenmill.crawling.adminui.view;


import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import lt.tokenmill.crawling.adminui.view.namedquery.NamedQueriesView;
import lt.tokenmill.crawling.adminui.view.pageanalysis.PageAnalysisView;
import lt.tokenmill.crawling.adminui.view.sourcetest.HttpSourceTestsView;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

public class BaseView extends VerticalLayout {

    public BaseView(String title) {
        UI.getCurrent().getPage().setTitle(String.format("Crawler Admin | %s", title));
        setWidth(100, PERCENTAGE);
        setSpacing(true);
        setMargin(true);

        HorizontalLayout actionBarLayout = new HorizontalLayout();
        actionBarLayout.setWidth(100, PERCENTAGE);

        MenuBar menu = new MenuBar();

        MenuBar.MenuItem dataItem = menu.addItem("Configuration", null);
        dataItem.addItem("HTTP Sources", (item) -> UI.getCurrent().setContent(new HttpSourcesView()));
        dataItem.addItem("HTTP Source Tests", (item) -> UI.getCurrent().setContent(new HttpSourceTestsView()));
        dataItem.addItem("Named Queries", (item) -> UI.getCurrent().setContent(new NamedQueriesView()));
        dataItem.addItem("Import / Export", (item) -> UI.getCurrent().setContent(new ImportExportView()));

        menu.addItem("Page Analysis", (item) -> UI.getCurrent().setContent(new PageAnalysisView()));

        actionBarLayout.addComponent(menu);

        addComponent(actionBarLayout);
    }

}