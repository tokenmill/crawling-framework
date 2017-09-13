package lt.tokenmill.crawling.data;

import org.joda.time.DateTime;

public class HttpUrl {

    /**
     * Source url.
     */
    private String source;

    private String url;

    /**
     * Publish data when it is known before parsing. Eg. from RSS feed.
     */
    private String published;

    /**
     * When this url was dicovered.
     */
    private DateTime discovered;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public DateTime getDiscovered() {
        return discovered;
    }

    public void setDiscovered(DateTime discovered) {
        this.discovered = discovered;
    }
}