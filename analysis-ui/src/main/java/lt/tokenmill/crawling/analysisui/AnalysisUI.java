package lt.tokenmill.crawling.analysisui;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import lt.tokenmill.crawling.analysisui.view.SearchView;

import javax.servlet.annotation.WebServlet;

@Theme("analysistheme")
public class AnalysisUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setContent(new SearchView());
    }

    @WebServlet(urlPatterns = "/*", name = "AnalysisUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = AnalysisUI.class, productionMode = false)
    public static class AnalysisUIServlet extends VaadinServlet {
    }
}
