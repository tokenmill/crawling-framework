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
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

public class CrawlerTopology extends ConfigurableTopology {

    public static void main(String[] args) throws Exception {
        ConfigurableTopology.start(new CrawlerTopology(), args);
    }

    @Override
    protected int run(String[] strings) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("generator", new UrlGeneratorSpout());

        builder.setBolt("partitioner", new URLPartitionerBolt())
                .shuffleGrouping("generator");

        builder.setBolt("fetch", new FetcherBolt())
                .fieldsGrouping("partitioner", new Fields("key"));

        builder.setBolt("sitemap", new SiteMapParserBolt())
                .localOrShuffleGrouping("fetch");

        builder.setBolt("feed", new FeedParserBolt())
                .localOrShuffleGrouping("sitemap");

        builder.setBolt("links", new LinkExtractorBolt())
                .localOrShuffleGrouping("feed");

        builder.setBolt("index", new ArticleIndexerBolt())
                .localOrShuffleGrouping("fetch");

        builder.setBolt("status", new StatusUpdaterBolt())
                .localOrShuffleGrouping("fetch", Constants.StatusStreamName)
                .localOrShuffleGrouping("sitemap", Constants.StatusStreamName)
                .localOrShuffleGrouping("index", Constants.StatusStreamName)
                .localOrShuffleGrouping("links", Constants.StatusStreamName);

        String topologyName = (String) conf.getOrDefault(Config.TOPOLOGY_NAME, "crawler");
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        return submit(topologyName, conf, builder);
    }
}
