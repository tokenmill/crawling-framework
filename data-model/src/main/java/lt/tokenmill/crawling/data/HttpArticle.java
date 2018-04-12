package lt.tokenmill.crawling.data;


import org.joda.time.DateTime;

import java.util.List;

public class HttpArticle {

    private String source;

    private String language;

    private String url;

    private String title;

    private String text;

    private List<String> appIds;

    private DateTime published;

    private DateTime discovered;

    private List<String> categories;

    public String getSource() {
        return source;
    }

    public String getUrl() {
        return url;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public DateTime getPublished() {
        return published;
    }

    public void setPublished(DateTime published) {
        this.published = published;
    }

    public DateTime getDiscovered() {
        return discovered;
    }

    public void setDiscovered(DateTime discovered) {
        this.discovered = discovered;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getAppIds() {
        return appIds;
    }

    public void setAppIds(List<String> appIds) {
        this.appIds = appIds;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "HttpArticle{" +
                "source='" + source + '\'' +
                ", language='" + language + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", appIds=" + appIds +
                ", published=" + published +
                ", discovered=" + discovered +
                ", categories=" + categories +
                '}';
    }
}