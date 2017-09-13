package lt.tokenmill.crawling.data;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public class HttpSource {

    /**
     * Name of the source. Eg. Bloomberg
     */
    private String name;

    /**
     * Index url of the source. Eg. http://www.bloomberg.com
     */
    private String url;

    /**
     * Default timezone of the source. Used to shift detected timestamp if it has no timezone info.
     */
    private String timezone;

    /**
     * Is this source enabled. Crawler should start/stop crawling on this flag change.
     */
    private boolean enabled;

    /**
     * Should crawler search for new urls in articles. Otherwise new urls are discovered only from seed pages.
     */
    private boolean discoveryEnabled;

    /**
     * Default language of the source.
     */
    private String language;

    /**
     * How often seed urls should be re-fetched to discover new urls.
     */
    private Integer urlRecrawlDelayInSecs;

    /**
     * How often seed sitemaps should be re-fetched to discover new urls.
     */
    private Integer sitemapRecrawlDelayInSecs;

    /**
     * How often seed feeds should be re-fetched to discover new urls.
     */
    private Integer feedRecrawlDelayInSecs;

    /**
     * List of seed urls to HTML pages.
     */
    private List<String> urls;

    /**
     * List of seed urls to Sitemap XMLs.
     */
    private List<String> sitemaps;

    /**
     * List of seed urls to RSS/Atom feeds.
     */
    private List<String> feeds;

    /**
     * List of categories this source belongs to. Will be copied to extracted documents.
     */
    private List<String> categories;

    /**
     * List of application IDs this source belongs to. Will be copied to extracted documents.
     */
    private List<String> appIds;

    /**
     * List of Regexp filters to select relevant urls.
     */
    private List<String> urlFilters;

    /**
     * List of Regexp filters to normalize urls.
     */
    private List<String> urlNormalizers;

    /**
     * Title selectors which have higher priority than default ones.
     */
    private List<String> titleSelectors;

    /**
     * Date selectors which have higher priority than default ones.
     */
    private List<String> dateSelectors;

    /**
     * Date regexps to extract dates. regexps have higher priority than selectors.
     */
    private List<String> dateRegexps;

    /**
     * Text selectors which have higher priority than default ones.
     */
    private List<String> textSelectors;

    /**
     * List of Regexp filters to normalize test.
     */
    private List<String> textNormalizers;

    /**
     * Date formats to use when parsing dates. They have higher priority than default ones.
     */
    private List<String> dateFormats;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public List<String> getUrls() {
        return urls != null ? urls : emptyList();
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getSitemaps() {
        return sitemaps != null ? sitemaps : emptyList();
    }

    public void setSitemaps(List<String> sitemaps) {
        this.sitemaps = sitemaps;
    }

    public List<String> getFeeds() {
        return feeds != null ? feeds : emptyList();
    }

    public void setFeeds(List<String> feeds) {
        this.feeds = feeds;
    }

    public List<String> getCategories() {
        return categories != null ? categories : emptyList();
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getAppIds() {
        return appIds != null ? appIds : emptyList();
    }

    public void setAppIds(List<String> appIds) {
        this.appIds = appIds;
    }

    public List<String> getUrlFilters() {
        return urlFilters != null ? urlFilters : emptyList();
    }

    public void setUrlFilters(List<String> urlFilters) {
        this.urlFilters = urlFilters;
    }

    public List<String> getUrlNormalizers() {
        return urlNormalizers != null ? urlNormalizers : emptyList();
    }

    public void setUrlNormalizers(List<String> urlNormalizers) {
        this.urlNormalizers = urlNormalizers;
    }

    public List<String> getTitleSelectors() {
        return titleSelectors != null ? titleSelectors : emptyList();
    }

    public void setTitleSelectors(List<String> titleSelectors) {
        this.titleSelectors = titleSelectors;
    }

    public List<String> getDateSelectors() {
        return dateSelectors != null ? dateSelectors : emptyList();
    }

    public void setDateSelectors(List<String> dateSelectors) {
        this.dateSelectors = dateSelectors;
    }

    public List<String> getTextSelectors() {
        return textSelectors != null ? textSelectors : emptyList();
    }

    public void setTextSelectors(List<String> textSelectors) {
        this.textSelectors = textSelectors;
    }

    public List<String> getTextNormalizers() {
        return textNormalizers != null ? textNormalizers : emptyList();
    }

    public void setTextNormalizers(List<String> textNormalizers) {
        this.textNormalizers = textNormalizers;
    }

    public List<String> getDateFormats() {
        return dateFormats != null ? dateFormats : emptyList();
    }

    public void setDateFormats(List<String> dateFormats) {
        this.dateFormats = dateFormats;
    }

    public Integer getUrlRecrawlDelayInSecs() {
        return urlRecrawlDelayInSecs;
    }

    public void setUrlRecrawlDelayInSecs(Integer urlRecrawlDelayInSecs) {
        this.urlRecrawlDelayInSecs = urlRecrawlDelayInSecs;
    }

    public Integer getSitemapRecrawlDelayInSecs() {
        return sitemapRecrawlDelayInSecs;
    }

    public void setSitemapRecrawlDelayInSecs(Integer sitemapRecrawlDelayInSecs) {
        this.sitemapRecrawlDelayInSecs = sitemapRecrawlDelayInSecs;
    }

    public Integer getFeedRecrawlDelayInSecs() {
        return feedRecrawlDelayInSecs;
    }

    public void setFeedRecrawlDelayInSecs(Integer feedRecrawlDelayInSecs) {
        this.feedRecrawlDelayInSecs = feedRecrawlDelayInSecs;
    }

    public List<String> getDateRegexps() {
        return dateRegexps != null ? dateRegexps : emptyList();
    }

    public void setDateRegexps(List<String> dateRegexps) {
        this.dateRegexps = dateRegexps;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isDiscoveryEnabled() {
        return discoveryEnabled;
    }

    public void setDiscoveryEnabled(boolean discoveryEnabled) {
        this.discoveryEnabled = discoveryEnabled;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }


    @Override
    public String toString() {
        return "HttpSource{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", timezone='" + timezone + '\'' +
                ", enabled=" + enabled +
                ", discoveryEnabled=" + discoveryEnabled +
                ", language='" + language + '\'' +
                ", urlRecrawlDelayInSecs=" + urlRecrawlDelayInSecs +
                ", sitemapRecrawlDelayInSecs=" + sitemapRecrawlDelayInSecs +
                ", feedRecrawlDelayInSecs=" + feedRecrawlDelayInSecs +
                ", urls=" + urls +
                ", sitemaps=" + sitemaps +
                ", feeds=" + feeds +
                ", categories=" + categories +
                ", appIds=" + appIds +
                ", urlFilters=" + urlFilters +
                ", urlNormalizers=" + urlNormalizers +
                ", titleSelectors=" + titleSelectors +
                ", dateSelectors=" + dateSelectors +
                ", dateRegexps=" + dateRegexps +
                ", textSelectors=" + textSelectors +
                ", textNormalizers=" + textNormalizers +
                ", dateFormats=" + dateFormats +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpSource that = (HttpSource) o;
        return enabled == that.enabled &&
                discoveryEnabled == that.discoveryEnabled &&
                Objects.equals(name, that.name) &&
                Objects.equals(url, that.url) &&
                Objects.equals(timezone, that.timezone) &&
                Objects.equals(language, that.language) &&
                Objects.equals(urlRecrawlDelayInSecs, that.urlRecrawlDelayInSecs) &&
                Objects.equals(sitemapRecrawlDelayInSecs, that.sitemapRecrawlDelayInSecs) &&
                Objects.equals(feedRecrawlDelayInSecs, that.feedRecrawlDelayInSecs) &&
                Objects.equals(urls, that.urls) &&
                Objects.equals(sitemaps, that.sitemaps) &&
                Objects.equals(feeds, that.feeds) &&
                Objects.equals(appIds, that.appIds) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(urlFilters, that.urlFilters) &&
                Objects.equals(urlNormalizers, that.urlNormalizers) &&
                Objects.equals(titleSelectors, that.titleSelectors) &&
                Objects.equals(dateSelectors, that.dateSelectors) &&
                Objects.equals(dateRegexps, that.dateRegexps) &&
                Objects.equals(textSelectors, that.textSelectors) &&
                Objects.equals(textNormalizers, that.textNormalizers) &&
                Objects.equals(dateFormats, that.dateFormats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, timezone, enabled, discoveryEnabled, language, urlRecrawlDelayInSecs, sitemapRecrawlDelayInSecs, feedRecrawlDelayInSecs, urls, sitemaps, feeds, categories, appIds, urlFilters, urlNormalizers, titleSelectors, dateSelectors, dateRegexps, textSelectors, textNormalizers, dateFormats);
    }
}