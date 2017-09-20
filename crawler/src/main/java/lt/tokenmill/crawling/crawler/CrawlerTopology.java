package lt.tokenmill.crawling.crawler;

import com.digitalpebble.stormcrawler.ConfigurableTopology;
import com.digitalpebble.stormcrawler.Constants;
import com.digitalpebble.stormcrawler.bolt.FeedParserBolt;
import com.digitalpebble.stormcrawler.bolt.FetcherBolt;
import com.digitalpebble.stormcrawler.bolt.SiteMapParserBolt;
import com.digitalpebble.stormcrawler.bolt.URLPartitionerBolt;
import lt.tokenmill.crawling.crawler.bolt.ArticleIndexerBolt;
import lt.tokenmill.crawling.crawler.bolt.LinkExtractorBolt;
import lt.tokenmill.crawling.crawler.bolt.StatusUpdaterBolt;
import lt.tokenmill.crawling.crawler.spout.UrlGeneratorSpout;
import org.apache.storm.Config;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class CrawlerTopology extends ConfigurableTopology {

    private final ServiceProvider serviceProvider;

    public static void main(String[] args) throws Exception {
        ConfigurableTopology.start(new CrawlerTopology(), args);
    }

    public CrawlerTopology() {
        this(new DefaultServiceProvider());
    }

    public CrawlerTopology(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected int run(String[] strings) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("generator", createUrlGeneratorSpout(serviceProvider));

        builder.setBolt("partitioner", new URLPartitionerBolt())
                .shuffleGrouping("generator");

        builder.setBolt("fetch", new FetcherBolt())
                .fieldsGrouping("partitioner", new Fields("key"));

        builder.setBolt("sitemap", new SiteMapParserBolt())
                .localOrShuffleGrouping("fetch");

        builder.setBolt("feed", new FeedParserBolt())
                .localOrShuffleGrouping("sitemap");

        builder.setBolt("links", createLinkExtractor(serviceProvider))
                .localOrShuffleGrouping("feed");

        builder.setBolt("index", createArticleIndexer(serviceProvider))
                .localOrShuffleGrouping("fetch");

        builder.setBolt("status", createStatusUpdater(serviceProvider))
                .localOrShuffleGrouping("fetch", Constants.StatusStreamName)
                .localOrShuffleGrouping("sitemap", Constants.StatusStreamName)
                .localOrShuffleGrouping("index", Constants.StatusStreamName)
                .localOrShuffleGrouping("links", Constants.StatusStreamName);

        String topologyName = (String) conf.getOrDefault(Config.TOPOLOGY_NAME, "crawler");
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        return submit(topologyName, conf, builder);
    }

    protected IRichSpout createUrlGeneratorSpout(ServiceProvider serviceProvider) {
        return new UrlGeneratorSpout(serviceProvider);
    }

    protected IRichBolt createLinkExtractor(ServiceProvider serviceProvider) {
        return new LinkExtractorBolt(serviceProvider);
    }

    protected IRichBolt createArticleIndexer(ServiceProvider serviceProvider) {
        return new ArticleIndexerBolt(serviceProvider);
    }

    protected IRichBolt createStatusUpdater(ServiceProvider serviceProvider) {
        return new StatusUpdaterBolt(serviceProvider);
    }

}
