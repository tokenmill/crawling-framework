package lt.tokenmill.crawling.adminui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import lt.tokenmill.crawling.adminui.view.HttpSourcesView;

import javax.servlet.annotation.WebServlet;

@Theme("crawleradmintheme")
public class CrawlerAdminUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setContent(new HttpSourcesView());
    }

    @WebServlet(urlPatterns = "/*", name = "CrawlerAdminUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = CrawlerAdminUI.class, productionMode = false)
    public static class CrawlerAdminUIServlet extends VaadinServlet {
    }
}
