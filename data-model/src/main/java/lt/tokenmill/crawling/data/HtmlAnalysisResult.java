package lt.tokenmill.crawling.data;

import java.util.List;
import java.util.Map;

public class HtmlAnalysisResult {

    private String url;
    private String title;
    private Integer httpStatus;
    private List<String> links;
    private Map<String, String> headers;
    private List<String> metaValues;

    private String robotsTxt;
    private Boolean robotsAllowedAll;
    private Boolean robotsAllowedNone;
    private Boolean robotsAllowedHome;
    private List<String> robotsSitemaps;
    private Long robotsCrawlDelay;

    public String getUrl() {
        return url;
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

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public List<String> getMetaValues() {
        return metaValues;
    }

    public void setMetaValues(List<String> metaValues) {
        this.metaValues = metaValues;
    }

    public String getRobotsTxt() {
        return robotsTxt;
    }

    public void setRobotsTxt(String robotsTxt) {
        this.robotsTxt = robotsTxt;
    }

    public Boolean getRobotsAllowedAll() {
        return robotsAllowedAll;
    }

    public void setRobotsAllowedAll(Boolean robotsAllowedAll) {
        this.robotsAllowedAll = robotsAllowedAll;
    }

    public Boolean getRobotsAllowedNone() {
        return robotsAllowedNone;
    }

    public void setRobotsAllowedNone(Boolean robotsAllowedNone) {
        this.robotsAllowedNone = robotsAllowedNone;
    }

    public Boolean getRobotsAllowedHome() {
        return robotsAllowedHome;
    }

    public void setRobotsAllowedHome(Boolean robotsAllowedHome) {
        this.robotsAllowedHome = robotsAllowedHome;
    }

    public List<String> getRobotsSitemaps() {
        return robotsSitemaps;
    }

    public void setRobotsSitemaps(List<String> robotsSitemaps) {
        this.robotsSitemaps = robotsSitemaps;
    }

    public Long getRobotsCrawlDelay() {
        return robotsCrawlDelay;
    }

    public void setRobotsCrawlDelay(Long robotsCrawlDelay) {
        this.robotsCrawlDelay = robotsCrawlDelay;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
