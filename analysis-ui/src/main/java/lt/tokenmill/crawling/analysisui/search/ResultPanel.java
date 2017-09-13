package lt.tokenmill.crawling.analysisui.search;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HighlightedSearchResult;
import lt.tokenmill.crawling.data.HttpArticle;

import java.util.stream.Collectors;

public class ResultPanel extends Panel {

    private static final String RESULTS_TEMPLATE = "<b>%s</b>&nbsp;<a href=\"%s\" target=\"_blank\"><strong>%s</strong></a>&nbsp;<b>•</b>&nbsp;%s<br/>%s";

    public ResultPanel(HighlightedSearchResult searchResult) {
        HttpArticle article  = searchResult.getArticle();
        String highlights = searchResult.getHighlights().stream().collect(Collectors.joining("<br/>...<br/>"));
        String text = String.format(RESULTS_TEMPLATE,
                DataUtils.formatInUTC(article.getPublished()).replace("T", " "),
                article.getUrl(), article.getTitle(), article.getSource(), highlights);
        Label content = new Label(text);
        content.setContentMode(ContentMode.HTML);
        VerticalLayout component = new VerticalLayout(content);
        component.setMargin(true);
        setContent(component);
    }


}
