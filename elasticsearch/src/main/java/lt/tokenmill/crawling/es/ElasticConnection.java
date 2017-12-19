package lt.tokenmill.crawling.es;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ElasticConnection {

    private static final int DEFAULT_TRANSPORT_PORT = 9300;
    private static final String DEFAULT_FLUSH_INTERVAL_STRING = "5s";

    private Client client;

    private BulkProcessor processor;

    ElasticConnection(Client client, BulkProcessor processor) {
        this.processor = processor;
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public BulkProcessor getProcessor() {
        return processor;
    }

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
        return getConnection(hostname, DEFAULT_TRANSPORT_PORT, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_LISTENER);
    }

    public static ElasticConnection getConnection(String hostname, int transportPort) {
        return getConnection(hostname, transportPort, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_LISTENER);
    }

    private static ElasticConnection getConnection(String hostname, int transportPort, String flushIntervalString, BulkProcessor.Listener listener) {
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        TimeValue flushInterval = TimeValue.parseTimeValue(flushIntervalString, TimeValue.timeValueSeconds(5), "flush");

        Client client = getClient(hostname, transportPort);

        BulkProcessor bulkProcessor = BulkProcessor.builder(client, listener)
                .setFlushInterval(flushInterval)
                .setBulkActions(10)
                .setConcurrentRequests(10)
                .build();

        return new ElasticConnection(client, bulkProcessor);
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
