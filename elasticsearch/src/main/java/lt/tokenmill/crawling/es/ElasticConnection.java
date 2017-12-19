package lt.tokenmill.crawling.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElasticConnection {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticConnection.class);

    private static final int DEFAULT_TRANSPORT_PORT = 9300;
    private static final int DEFAULT_REST_PORT = 9200;
    private static final String DEFAULT_REST_SCHEME = "http";
    private static final int DEFAULT_BULK_ACTIONS = 10;
    private static final String DEFAULT_FLUSH_INTERVAL_STRING = "5s";

    private Client client;
    private RestHighLevelClient restHighLevelClient;
    private RestClientBuilder restClientBuilder;
    private BulkProcessor processor;

    private ElasticConnection(Client client, BulkProcessor processor, RestHighLevelClient restHighLevelClient, RestClientBuilder restClient) {
        this.processor = processor;
        this.client = client;
        this.restHighLevelClient = restHighLevelClient;
        this.restClientBuilder = restClient;
    }

    @Deprecated
    public Client getClient() {
        return client;
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public BulkProcessor getProcessor() {
        return processor;
    }

    @Deprecated
    private static Client getClient(String hostname, int transportPort) {
        Settings settings = Settings.builder()
                .put("client.transport.ignore_cluster_name", true)
                .build();
        try {
            return new PreBuiltTransportClient(settings).addTransportAddress(
                    new TransportAddress(InetAddress.getByName(hostname), transportPort));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static ElasticConnection getConnection(String hostname) {
        return getConnection(hostname, DEFAULT_TRANSPORT_PORT, DEFAULT_REST_PORT, DEFAULT_REST_SCHEME, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_ACTIONS, DEFAULT_BULK_LISTENER);
    }

    public static ElasticConnection getConnection(String hostname, int transportPort) {
        return getConnection(hostname, transportPort, DEFAULT_REST_PORT, DEFAULT_REST_SCHEME, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_ACTIONS, DEFAULT_BULK_LISTENER);
    }

    private static ElasticConnection getConnection(String hostname, int transportPort, int restPort, String restScheme, String flushIntervalString, int bulkActions, BulkProcessor.Listener listener) {
        LOG.info("Creating ElasticConnection with params {} {} {} {}", hostname, transportPort, restPort, restScheme);
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        TimeValue flushInterval = TimeValue.parseTimeValue(flushIntervalString, TimeValue.timeValueSeconds(5), "flush");

        Client client = getClient(hostname, transportPort);

        RestClientBuilder restClient = RestClient.builder(new HttpHost(hostname, restPort, restScheme));
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClient);

        BulkProcessor bulkProcessor = BulkProcessor.builder(restHighLevelClient::bulkAsync, listener)
                .setFlushInterval(flushInterval)
                .setBulkActions(bulkActions)
                .setConcurrentRequests(10)
                .build();

        return new ElasticConnection(client, bulkProcessor, restHighLevelClient, restClient);
    }

    public void close() {
        if (client != null) {
            client.close();
        }
        if (processor != null) {
            processor.close();
        }
    }

    private static final BulkProcessor.Listener DEFAULT_BULK_LISTENER = new BulkProcessor.Listener() {

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable response) {
        }

        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
        }
    };

}
