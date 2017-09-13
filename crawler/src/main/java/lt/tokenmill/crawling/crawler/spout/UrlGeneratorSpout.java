package lt.tokenmill.crawling.crawler.spout;

import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.persistence.Status;
import com.digitalpebble.stormcrawler.util.ConfUtils;
import lt.tokenmill.crawling.crawler.CrawlerConstants;
import lt.tokenmill.crawling.crawler.ServiceProvider;
import lt.tokenmill.crawling.crawler.utils.PrioritizedSource;
import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.HttpUrl;
import lt.tokenmill.crawling.es.*;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UrlGeneratorSpout extends BaseRichSpout {

    private static final Logger LOG = LoggerFactory.getLogger(UrlGeneratorSpout.class);
    private static final Fields FIELDS = new Fields("url", "metadata");
    private ServiceProvider serviceProvider;

    private PriorityQueue<PrioritizedSource> prioritizedSources =
            new PriorityQueue<>(new PrioritizedSource.PrioritizedUrlComparator());

    private EsHttpUrlOperations esUrlOperations;
    private EsHttpSourceOperations esSourceOperations;

    private Set<Object> processing = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private boolean active;
    private SpoutOutputCollector collector;
    private HttpSourceConfiguration configuration;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(FIELDS);
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.serviceProvider = new ServiceProvider();
        this.esUrlOperations = this.serviceProvider.createEsHttpUrlOperations(conf);
        this.esSourceOperations = this.serviceProvider.createEsHttpSourceOperations(conf);
        this.collector = collector;
        this.configuration = getConfiguration();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void activate() {
        super.activate();
        active = true;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        active = false;
    }

    @Override
    public void nextTuple() {
        if (!active) {
            //If Spout is disabled do nothing
            return;
        }
        HttpSourceConfiguration configuration = getConfiguration();
        PrioritizedSource prioritized = configuration.prioritized();
        if (prioritized != null && emitUrl(prioritized)) {
            return;
        }

        int tries = configuration.maxTries();
        while (configuration.hasNextActive() && tries > 0) {
            String source = configuration.nextActive();
            LOG.info("Searching crawlable urls for source '{}'", source);
            List<HttpUrl> urls = esUrlOperations.findUrlsByStatusAndSource(Status.DISCOVERED, source, 5);
            boolean emitted = false;
            for (HttpUrl u : urls) {
                if (emitUrl(u)) {
                    emitted = true;
                }
            }
            if (emitted) {
                return;
            } else {
                tries--;
            }
        }
    }

    private boolean emitUrl(HttpUrl urlInfo) {
        String url = urlInfo.getUrl();
        if (!processing.contains(url)) {
            Metadata meta = new Metadata();
            meta.addValue(CrawlerConstants.META_SOURCE, urlInfo.getSource());
            meta.addValue(CrawlerConstants.META_PUBLISHED, urlInfo.getPublished());
            meta.addValue(CrawlerConstants.META_DISCOVERED, DataUtils.formatInUTC(urlInfo.getDiscovered()));
            collector.emit(new Values(url, meta), url);
            processing.add(url);
            LOG.info("Emitted url {} with meta {}", url, meta);
            return true;
        }
        return false;
    }

    private boolean emitUrl(PrioritizedSource source) {
        if (!processing.contains(source.getUrl())) {
            Metadata meta = new Metadata();
            meta.addValue(CrawlerConstants.META_SOURCE, source.getSource().getUrl());
            meta.addValue(CrawlerConstants.META_IS_SEED, "true");
            if (source.isFeed()) {
                meta.addValue(CrawlerConstants.META_IS_FEED, "true");
            }
            if (source.isSitemap()) {
                meta.addValue(CrawlerConstants.META_IS_SITEMAP, "true");
            }
            collector.emit(new Values(source.getUrl(), meta), source.getUrl());
            processing.add(source.getUrl());
            LOG.info("Emitted prioritized seed {} with meta {}", source.getUrl(), meta);
            return true;
        }
        return false;
    }

    private HttpSourceConfiguration getConfiguration() {
        if (configuration == null || HttpSourceConfiguration.needsReload()) {
            LOG.info("Loading HTTP sources");
            List<HttpSource> sources = esSourceOperations.findEnabledSources();
            configuration = HttpSourceConfiguration.reload(configuration, sources);
            return configuration;
        }
        return configuration;
    }

    @Override
    public void ack(Object url) {
        super.ack(url);
        processing.remove(url);
    }

    @Override
    public void fail(Object url) {
        super.fail(url);
        processing.remove(url);
    }
}