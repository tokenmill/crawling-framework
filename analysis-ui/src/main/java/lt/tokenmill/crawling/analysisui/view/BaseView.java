package lt.tokenmill.crawling.analysisui.view;


import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

public class BaseView extends VerticalLayout {

    public BaseView(String title) {
        UI.getCurrent().getPage().setTitle(String.format("Analysis | %s", title));
        setWidth(100, PERCENTAGE);
        setSpacing(true);
        setMargin(true);

        HorizontalLayout actionBarLayout = new HorizontalLayout();
        actionBarLayout.setWidth(100, PERCENTAGE);

        MenuBar menu = new MenuBar();

        menu.addItem("Search", (item) -> UI.getCurrent().setContent(new SearchView()));
        menu.addItem("Context Cloud", (item) -> UI.getCurrent().setContent(new ContextCloudView()));

        actionBarLayout.addComponent(menu);

        addComponent(actionBarLayout);
    }

}