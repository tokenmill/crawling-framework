package lt.tokenmill.crawling.crawler;

import com.digitalpebble.stormcrawler.bolt.FeedParserBolt;
import com.digitalpebble.stormcrawler.bolt.SiteMapParserBolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CrawlerConstants {
    private static final Logger LOG = LoggerFactory.getLogger(CrawlerConstants.class);

    private static long getReloadDelayInSeconds() {
        long reloadDelay = 300;
        String envVar = System.getenv("DEFAULT_SOURCE_RELOAD_DELAY");
        if (envVar != null) {
            try {
                reloadDelay = Long.parseLong(envVar);
            } catch (NumberFormatException e) {
                LOG.warn("Environment variable 'DEFAULT_SOURCE_RELOAD_DELAY' is not a number '{}'", envVar);
            }
        }
        return  reloadDelay;
    }

    public static final long MIN_FETCH_DELAY = TimeUnit.MINUTES.toMillis(1);
    public static final long DEFAULT_URL_FETCH_DELAY = TimeUnit.MINUTES.toMillis(10);
    public static final long DEFAULT_FEED_FETCH_DELAY = TimeUnit.MINUTES.toMillis(10);
    public static final long DEFAULT_SITEMAP_FETCH_DELAY = TimeUnit.MINUTES.toMillis(30);
    public static final long DEFAULT_SOURCE_RELOAD_DELAY = TimeUnit.SECONDS.toMillis(getReloadDelayInSeconds());

    public static final String META_IS_SITEMAP = SiteMapParserBolt.isSitemapKey;
    public static final String META_IS_FEED = FeedParserBolt.isFeedKey;
    public static final String META_IS_SEED = "isSeed";
    public static final String META_SOURCE = "source";
    public static final String META_PUBLISHED = "published";
    public static final String META_DISCOVERED = "discovered";
    public static final String META_FEED_PUBLISHED = "feed.publishedDate";

    public static final String URL_FILTERS_FILE = "urlfilters.config.file";

    public static final String PARTIAL_ANALYSIS_STATUS = "PARTIAL_ANALYSIS";
}
