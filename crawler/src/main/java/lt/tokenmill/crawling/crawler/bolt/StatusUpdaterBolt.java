package lt.tokenmill.crawling.crawler.bolt;

import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.persistence.AbstractStatusUpdaterBolt;
import com.digitalpebble.stormcrawler.persistence.Status;
import com.digitalpebble.stormcrawler.util.ConfUtils;
import lt.tokenmill.crawling.crawler.CrawlerConstants;
import lt.tokenmill.crawling.crawler.ServiceProvider;
import lt.tokenmill.crawling.crawler.utils.UrlFiltersCache;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.es.*;
import lt.tokenmill.crawling.parser.urls.UrlFilters;
import org.apache.storm.metric.api.MultiCountMetric;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class StatusUpdaterBolt extends AbstractStatusUpdaterBolt {

    private static final Logger LOG = LoggerFactory.getLogger(StatusUpdaterBolt.class);

    private MultiCountMetric eventCounter;

    private EsHttpUrlOperations esUrlsOperations;
    private EsHttpSourceOperations esHttpSourcesOperations;
    private ServiceProvider serviceProvider;

    @Override
    public void store(String url, Status status, Metadata metadata, Date nextFetch) throws Exception {
        try {
            String source = metadata.getFirstValue(CrawlerConstants.META_SOURCE);
            Boolean isSeed = Boolean.parseBoolean(metadata.getFirstValue(CrawlerConstants.META_IS_SEED));
            HttpSource httpSource = EsHttpSourcesCache.get(esHttpSourcesOperations, source);
            UrlFilters filters = UrlFiltersCache.get(httpSource);

            String filtered = filters.filter(url);
            if (isSeed || (filtered == null && status.equals(Status.DISCOVERED))) {
                LOG.debug("Url '{}' is seed or rejected by filters", url);
                return;
            }

            String id = (filtered == null) ? url : filtered;

            LOG.debug("Setting '{}' status to '{}'", id, status);


            boolean create = status.equals(Status.DISCOVERED);
            String published = metadata.getFirstValue(CrawlerConstants.META_PUBLISHED);
            if (published == null) {
                published = metadata.getFirstValue(CrawlerConstants.META_FEED_PUBLISHED);
            }
            esUrlsOperations.upsertUrlStatus(id, published, source, create, status);

            if (status == Status.DISCOVERED) {
                eventCounter.scope("urls_discovered").incr();
            }
        } catch (Exception e) {
            LOG.error("Failed to set status for url '{}'", url, e);
        }
    }


    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector outputCollector) {
        super.prepare(conf, context, outputCollector);
        this.eventCounter = context.registerMetric(this.getClass().getSimpleName(), new MultiCountMetric(), 10);
        this.serviceProvider = new ServiceProvider();
        this.esUrlsOperations = this.serviceProvider.createEsHttpUrlOperations(conf);
        this.esHttpSourcesOperations = this.serviceProvider.createEsHttpSourceOperations(conf);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}