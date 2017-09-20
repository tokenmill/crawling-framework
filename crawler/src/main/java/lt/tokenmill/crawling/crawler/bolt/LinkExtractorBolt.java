package lt.tokenmill.crawling.crawler.bolt;

import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.persistence.Status;
import com.digitalpebble.stormcrawler.protocol.HttpHeaders;
import com.digitalpebble.stormcrawler.util.ConfUtils;
import com.digitalpebble.stormcrawler.util.MetadataTransfer;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import lt.tokenmill.crawling.crawler.CrawlerConstants;
import lt.tokenmill.crawling.crawler.DefaultServiceProvider;
import lt.tokenmill.crawling.crawler.ServiceProvider;
import lt.tokenmill.crawling.crawler.utils.UrlFilterUtils;
import lt.tokenmill.crawling.crawler.utils.UrlFiltersCache;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.es.EsHttpSourceOperations;
import lt.tokenmill.crawling.es.EsHttpSourcesCache;
import lt.tokenmill.crawling.parser.urls.UrlFilters;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.storm.metric.api.MultiCountMetric;
import org.apache.storm.shade.com.google.common.collect.Lists;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.digitalpebble.stormcrawler.Constants.StatusStreamName;


public class LinkExtractorBolt extends BaseRichBolt {

    private static final Logger LOG = LoggerFactory.getLogger(LinkExtractorBolt.class);

    private ServiceProvider serviceProvider;
    private OutputCollector collector;

    private MultiCountMetric eventCounter;

    private MetadataTransfer metadataTransfer;
    private EsHttpSourceOperations esHttpSourcesOperations;

    private com.digitalpebble.stormcrawler.filtering.URLFilters defaultUrlFilters;

    public LinkExtractorBolt(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.eventCounter = context.registerMetric(this.getClass()
                .getSimpleName(), new MultiCountMetric(), 10);
        this.metadataTransfer = MetadataTransfer.getInstance(conf);
        this.esHttpSourcesOperations = this.serviceProvider.createEsHttpSourceOperations(conf);
        String filtersConfigFile = ConfUtils.getString(conf, CrawlerConstants.URL_FILTERS_FILE);
        this.defaultUrlFilters = UrlFilterUtils.load(conf, filtersConfigFile);
    }

    @Override
    public void execute(Tuple tuple) {
        byte[] content = tuple.getBinaryByField("content");
        String url = tuple.getStringByField("url");
        Metadata metadata = (Metadata) tuple.getValueByField("metadata");
        String source = metadata.getFirstValue(CrawlerConstants.META_SOURCE);

        long start = System.currentTimeMillis();
        List<String> links;
        HttpSource httpSource;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
            httpSource = EsHttpSourcesCache.get(esHttpSourcesOperations, source);
            String charset = getContentCharset(content, metadata);
            Document jsoupDoc = Jsoup.parse(bais, charset, url);
            Elements linkElements = jsoupDoc.select("a[href]");
            links = Lists.newArrayListWithExpectedSize(linkElements.size());
            for (Element link : linkElements) {
                String outlinkUrl = link.attr("abs:href");
                if (StringUtils.isNotBlank(outlinkUrl)) {
                    links.add(outlinkUrl);
                }
            }
        } catch (Throwable e) {
            String errorMessage = "Exception while parsing outlinks from " + url + ": " + e;
            LOG.error(errorMessage);
            metadata.setValue(Constants.STATUS_ERROR_SOURCE, "outlink parsing");
            metadata.setValue(Constants.STATUS_ERROR_MESSAGE, errorMessage);
            collector.emit(StatusStreamName, tuple, new Values(url, metadata, Status.ERROR));
            collector.ack(tuple);
            eventCounter.scope("error_content_parsing_" + e.getClass().getSimpleName()).incrBy(1);
            eventCounter.scope("parse_exception").incrBy(1);
            return;
        }

        long duration = System.currentTimeMillis() - start;

        LOG.info("Parsed {} outlinks from {} in {} msec", links.size(), url, duration);

        emitOutlinks(tuple, url, metadata, links,  httpSource);

        collector.ack(tuple);
        eventCounter.scope("tuple_success").incr();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(StatusStreamName, new Fields("url", "metadata",
                "status"));
    }

    private String getContentCharset(byte[] content, Metadata metadata) {
        String charset = null;

        // check if the server specified a charset
        String specifiedContentType = metadata
                .getFirstValue(HttpHeaders.CONTENT_TYPE);
        try {
            if (specifiedContentType != null) {
                ContentType parsedContentType = ContentType
                        .parse(specifiedContentType);
                charset = parsedContentType.getCharset().name();
            }
        } catch (Exception e) {
            charset = null;
        }

        // filter HTML tags
        CharsetDetector detector = new CharsetDetector();
        detector.enableInputFilter(true);
        // give it a hint
        detector.setDeclaredEncoding(charset);
        detector.setText(content);
        try {
            CharsetMatch charsetMatch = detector.detect();
            if (charsetMatch != null) {
                charset = charsetMatch.getName();
            }
        } catch (Exception e) {
            // ignore and leave the charset as-is
        }
        return charset;
    }

    private void emitOutlinks(Tuple tuple, String url, Metadata metadata, List<String> outlinks, HttpSource httpSource) {
        Boolean isSeed = Boolean.parseBoolean(metadata.getFirstValue(CrawlerConstants.META_IS_SEED));
        if (!isSeed && !httpSource.isDiscoveryEnabled()) {
            //Urls can be discovered only from seeds
            return;
        }
        URL sourceUrl;
        try {
            sourceUrl = new URL(url);
        } catch (MalformedURLException e) {
            // we would have known by now as previous components check whether the URL is valid
            LOG.error("MalformedURLException on {}", url);
            eventCounter.scope("error_invalid_source_url").incrBy(1);
            return;
        }
        UrlFilters filters = UrlFiltersCache.get(httpSource);
        for (String link : outlinks) {
            String defaultFiltered = UrlFilterUtils.firstMatch(sourceUrl, metadata, link, defaultUrlFilters);
            String filtered = filters.filter(link);
            if (defaultFiltered == null || filtered == null) {
                eventCounter.scope("outlink_filtered").incr();
                continue;
            }
            Metadata linkMetadata = metadataTransfer.getMetaForOutlink(filtered, url, metadata);
            collector.emit(StatusStreamName, tuple, new Values(filtered, linkMetadata, Status.DISCOVERED));

            eventCounter.scope("outlink_kept").incr();
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

}