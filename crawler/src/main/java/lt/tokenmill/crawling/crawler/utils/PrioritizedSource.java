package lt.tokenmill.crawling.crawler.utils;


import lt.tokenmill.crawling.data.HttpSource;

import java.io.Serializable;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static lt.tokenmill.crawling.crawler.CrawlerConstants.*;

public class PrioritizedSource implements Serializable {



    private final String url;

    private final HttpSource source;

    private Long delay = MIN_FETCH_DELAY;

    private boolean sitemap = false;
    private boolean feed = false;

    private Long nextFetchTime = System.currentTimeMillis();

    private PrioritizedSource(String url, HttpSource source) {
        this.url = url;
        this.source = source;
    }

    private void setDelay(Long delay) {
        this.delay = Math.max(delay, MIN_FETCH_DELAY);
    }

    private void setSitemap(boolean sitemap) {
        this.sitemap = sitemap;
    }

    private void setFeed(boolean feed) {
        this.feed = feed;
    }

    public void recalculateNextFetchTime() {
        nextFetchTime = System.currentTimeMillis() + delay;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSitemap() {
        return sitemap;
    }

    public boolean isFeed() {
        return feed;
    }

    public HttpSource getSource() {
        return source;
    }

    public long getNextFetchTime() {
        return nextFetchTime;
    }

    public static class PrioritizedUrlComparator implements Comparator<PrioritizedSource>, Serializable {

        @Override
        public int compare(PrioritizedSource u1, PrioritizedSource u2) {
            return u1.nextFetchTime.compareTo(u2.nextFetchTime);
        }
    }

    public static PrioritizedSource createUrl(String url, HttpSource source) {
        PrioritizedSource result = new PrioritizedSource(url, source);
        long delay = source.getUrlRecrawlDelayInSecs() != null ?
                TimeUnit.SECONDS.toMillis(source.getUrlRecrawlDelayInSecs()) : DEFAULT_URL_FETCH_DELAY;
        result.setDelay(delay);
        return result;
    }

    public static PrioritizedSource createFeed(String url, HttpSource source) {
        PrioritizedSource result = new PrioritizedSource(url, source);
        long delay = source.getFeedRecrawlDelayInSecs() != null ?
                TimeUnit.SECONDS.toMillis(source.getFeedRecrawlDelayInSecs()) : DEFAULT_FEED_FETCH_DELAY;
        result.setDelay(delay);
        result.setFeed(true);
        return result;
    }

    public static PrioritizedSource createSitemap(String url, HttpSource source) {
        PrioritizedSource result = new PrioritizedSource(url, source);
        long delay = source.getSitemapRecrawlDelayInSecs() != null ?
                TimeUnit.SECONDS.toMillis(source.getSitemapRecrawlDelayInSecs()) : DEFAULT_SITEMAP_FETCH_DELAY;
        result.setDelay(delay);
        result.setSitemap(true);
        return result;
    }
}