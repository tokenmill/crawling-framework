package lt.tokenmill.crawling.crawler.bolt;


import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.persistence.Status;
import com.digitalpebble.stormcrawler.protocol.HttpHeaders;
import lt.tokenmill.crawling.crawler.CrawlerConstants;
import lt.tokenmill.crawling.crawler.ServiceProvider;
import lt.tokenmill.crawling.crawler.utils.UrlFiltersCache;
import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.es.EsDocumentOperations;
import lt.tokenmill.crawling.es.EsHttpSourceOperations;
import lt.tokenmill.crawling.es.EsHttpSourcesCache;
import lt.tokenmill.crawling.parser.ArticleExtractor;
import lt.tokenmill.crawling.parser.urls.UrlFilters;
import org.apache.storm.metric.api.MultiCountMetric;
import org.apache.storm.shade.com.google.common.base.Strings;
import org.apache.storm.shade.com.google.common.collect.Maps;
import org.apache.storm.shade.org.apache.http.entity.ContentType;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;


public class ArticleIndexerBolt extends BaseRichBolt {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected ServiceProvider serviceProvider;
    protected OutputCollector collector;
    protected MultiCountMetric eventCounter;
    protected EsHttpSourceOperations esHttpSourceOperations;
    protected EsDocumentOperations esDocumentOperations;

    public ArticleIndexerBolt(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void execute(Tuple tuple) {
        String url = tuple.getStringByField("url");
        Metadata metadata = (Metadata) tuple.getValueByField("metadata");
        String source = metadata.getFirstValue(CrawlerConstants.META_SOURCE);
        Boolean isSeed = Boolean.parseBoolean(metadata.getFirstValue(CrawlerConstants.META_IS_SEED));
        HttpSource httpSource = EsHttpSourcesCache.get(esHttpSourceOperations, source);
        UrlFilters urlFilters = UrlFiltersCache.get(httpSource);
        String filtered = urlFilters.filter(url);

        if (filtered == null) {
            LOG.info("Skipping analysis of '{}' because it is rejected by filters", url);
            eventCounter.scope("analysis_skipped").incr();
            collector.emit(tuple, new Values(url, metadata));
            collector.ack(tuple);
            return;
        } else if (isSeed) {
            eventCounter.scope("analysis_skipped_seed").incr();
            collector.emit(tuple, new Values(url, metadata));
            collector.ack(tuple);
            return;
        }

        byte[] content = tuple.getBinaryByField("content");

        String charset = null;
        String mimeType = null;
        try {
            String contentTypeValue = metadata.getFirstValue(HttpHeaders.CONTENT_TYPE);
            if (contentTypeValue != null) {
                ContentType contentType = ContentType.parse(contentTypeValue);
                mimeType = contentType.getMimeType();
                charset = contentType.getCharset().displayName();
            }
        } catch (Exception e) {
            LOG.warn("Failed to get charset and mime type for '{}'", url);
        }

        if (charset == null) {
            charset = "UTF-8";
        }

        if (content == null || content.length == 0) {
            LOG.warn("Skipping url '{}' because it has no content", url);
            eventCounter.scope("analysis_empty").incr();
            collector.emit(StatusStreamName, tuple, new Values(url, metadata, Status.ERROR));
        } else if (mimeType == null || !mimeType.toLowerCase().contains("html")) {
            LOG.warn("Ignoring url '{}' because mime-type is '{}'", url, mimeType);
            eventCounter.scope("analysis_wrong_type").incr();
            collector.emit(StatusStreamName, tuple, new Values(url, metadata, Status.ERROR));
        } else {
            try {
                LOG.info("Analyzing url '{}'", url);
                String html = new String(content, charset);
                HttpArticle article = analyze(url, filtered, httpSource, html, metadata);
                if (Strings.isNullOrEmpty(article.getTitle()) || Strings.isNullOrEmpty(article.getText()) || article.getPublished() == null) {
                    LOG.warn("Url '{}' analysis returned incomplete data", url);
                    eventCounter.scope("analysis_incomplete").incr();
                    collector.emit(StatusStreamName, tuple, new Values(url, metadata, Status.ERROR));
                } else {
                    Map<String, Object> fields = extractFields(url, filtered, httpSource, html, metadata);
                    storeDocument(article, fields);
                    LOG.info("Stored article '{}'", url);
                    eventCounter.scope("analysis_success").incr();
                    collector.emit(StatusStreamName, tuple, new Values(url, metadata, Status.FETCHED));
                }
                collector.emit(tuple, new Values(url, metadata));
            } catch (Exception e) {
                LOG.error("Failed to analyze '{}'", url, e);
                eventCounter.scope("analysis_error").incr();
                collector.emit(StatusStreamName, tuple, new Values(url, metadata, Status.ERROR));
            }
        }
        collector.ack(tuple);
    }

    protected Map<String, Object> extractFields(String url, String filtered, HttpSource httpSource, String html, Metadata metadata) {
        return Maps.newHashMap();
    }

    protected void storeDocument(HttpArticle article, Map<String, Object> fields) throws Exception {
        this.esDocumentOperations.store(article, fields);
    }

    private HttpArticle analyze(String url, String filtered, HttpSource httpSource, String html, Metadata metadata) throws Exception {
        String publishedHint = metadata.getFirstValue(CrawlerConstants.META_PUBLISHED);
        if (publishedHint == null) {
            publishedHint = metadata.getFirstValue(CrawlerConstants.META_FEED_PUBLISHED);
        }
        HttpArticle article = ArticleExtractor.extractArticle(html, filtered, httpSource, publishedHint);
        String discovered = metadata.getFirstValue(CrawlerConstants.META_DISCOVERED);
        article.setDiscovered(DataUtils.parseFromUTC(discovered));
        return article;
    }


    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.esHttpSourceOperations = this.serviceProvider.createEsHttpSourceOperations(conf);
        this.esDocumentOperations = this.serviceProvider.creatEsDocumentOperations(conf);
        this.collector = collector;
        this.eventCounter = context.registerMetric(this.getClass().getSimpleName(), new MultiCountMetric(), 10);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url", "metadata"));
        declarer.declareStream(StatusStreamName, new Fields("url", "metadata", "status"));
    }
}