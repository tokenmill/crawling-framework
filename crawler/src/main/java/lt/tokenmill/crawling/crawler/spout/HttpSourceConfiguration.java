package lt.tokenmill.crawling.crawler.spout;

import com.google.common.collect.Iterables;
import lt.tokenmill.crawling.crawler.CrawlerConstants;
import lt.tokenmill.crawling.crawler.utils.PrioritizedSource;
import lt.tokenmill.crawling.crawler.utils.UrlFiltersCache;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.es.EsHttpSourcesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;

public class HttpSourceConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(HttpSourceConfiguration.class);

    private final List<HttpSource> sources;
    private final List<String> sourceUrls;
    private final Iterator<String> sourceCycle;
    private final PriorityQueue<PrioritizedSource> prioritizedSources;

    private static long lastReloadMillis = 0;

    private HttpSourceConfiguration(List<HttpSource> sources) {
        this.sources = sources;
        this.sourceUrls = sources.stream()
                .map(HttpSource::getUrl)
                .collect(Collectors.toList());
        LOG.info("Loaded {} active HTTP sources", this.sourceUrls.size());
        this.sourceCycle = Iterables.cycle(this.sourceUrls).iterator();
        this.prioritizedSources =
                new PriorityQueue<>(new PrioritizedSource.PrioritizedUrlComparator());
        sources.forEach(s -> {
            s.getUrls().forEach(u -> prioritizedSources.offer(PrioritizedSource.createUrl(u, s)));
            s.getFeeds().forEach(u -> prioritizedSources.offer(PrioritizedSource.createFeed(u, s)));
            s.getSitemaps().forEach(u -> prioritizedSources.offer(PrioritizedSource.createSitemap(u, s)));
        });
    }

    public PrioritizedSource prioritized() {
        PrioritizedSource prioritized = prioritizedSources.peek();
        if (prioritized != null &&
                (prioritized.getNextFetchTime() <= currentTimeMillis())) {
            prioritized = prioritizedSources.poll();
            prioritized.recalculateNextFetchTime();
            prioritizedSources.offer(prioritized);
            return prioritized;
        }
        return null;
    }

    public int maxTries() {
        return Math.min(10, sourceUrls.size());
    }

    public boolean hasNextActive() {
        return sourceCycle.hasNext();
    }


    public String nextActive() {
        return sourceCycle.next();
    }

    public static HttpSourceConfiguration reload(HttpSourceConfiguration current, List<HttpSource> sources) {
        HttpSourceConfiguration configuration;
        if (current != null && current.sources.equals(sources)) {
            LOG.info("HTTP source configuration didn't change. Using current version");
            configuration = current;
        } else {
            configuration = new HttpSourceConfiguration(sources);
            EsHttpSourcesCache.invalidate();
            UrlFiltersCache.invalidate();
        }
        lastReloadMillis = currentTimeMillis();
        return configuration;
    }

    public static boolean needsReload() {
        LOG.info("Checking reloading timeout. Remaining milliseconds: {}",
                lastReloadMillis + CrawlerConstants.DEFAULT_SOURCE_RELOAD_DELAY - currentTimeMillis());
        return lastReloadMillis + CrawlerConstants.DEFAULT_SOURCE_RELOAD_DELAY < currentTimeMillis();
    }

}
