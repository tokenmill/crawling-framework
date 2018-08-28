package lt.tokenmill.crawling.data;

import java.io.Serializable;
import java.util.List;

public class HighlightedSearchResult implements Serializable {

    private HttpArticle article;

    private List<String> highlights;

    public HighlightedSearchResult(HttpArticle article, List<String> highlights) {
        this.article = article;
        this.highlights = highlights;
    }

    public HttpArticle getArticle() {
        return article;
    }

    public void setArticle(HttpArticle article) {
        this.article = article;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }
}
