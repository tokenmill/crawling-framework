package lt.tokenmill.crawling.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticConnection {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticConnection.class);

    private static final int DEFAULT_TRANSPORT_PORT = 9300;
    private static final int DEFAULT_REST_PORT = 9200;
    private static final String DEFAULT_REST_SCHEME = "http";
    private static final int DEFAULT_BULK_ACTIONS = 10;
    private static final String DEFAULT_FLUSH_INTERVAL_STRING = "5s";

    private RestHighLevelClient restHighLevelClient;
    private RestClientBuilder restClientBuilder;
    private BulkProcessor processor;

    private ElasticConnection(BulkProcessor processor, RestHighLevelClient restHighLevelClient, RestClientBuilder restClient) {
        this.processor = processor;
        this.restHighLevelClient = restHighLevelClient;
        this.restClientBuilder = restClient;
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public BulkProcessor getProcessor() {
        return processor;
    }

    public static ElasticConnection getConnection(String hostname) {
        return getConnection(hostname, DEFAULT_TRANSPORT_PORT, DEFAULT_REST_PORT, DEFAULT_REST_SCHEME, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_ACTIONS, DEFAULT_BULK_LISTENER);
    }

    public static ElasticConnection getConnection(String hostname, int transportPort) {
        return getConnection(hostname, transportPort, DEFAULT_REST_PORT, DEFAULT_REST_SCHEME, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_ACTIONS, DEFAULT_BULK_LISTENER);
    }

    public static ElasticConnection getConnection(String hostname, int transportPort, int restPort, String restScheme) {
        return getConnection(hostname, transportPort, restPort, restScheme, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_ACTIONS, DEFAULT_BULK_LISTENER);
    }

    private static ElasticConnection getConnection(String hostname, int transportPort, int restPort, String restScheme, String flushIntervalString, int bulkActions, BulkProcessor.Listener listener) {
        LOG.info("Creating ElasticConnection with params {} {} {} {}", hostname, transportPort, restPort, restScheme);
        System.setProperty("es.set.netty.runtime.available.processors", "false");

        TimeValue flushInterval = TimeValue.parseTimeValue(flushIntervalString, TimeValue.timeValueSeconds(5), "flush");

        RestClientBuilder restClient = RestClient.builder(new HttpHost(hostname, restPort, restScheme));
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClient);

        BulkProcessor bulkProcessor = BulkProcessor.builder(restHighLevelClient::bulkAsync, listener)
                .setFlushInterval(flushInterval)
                .setBulkActions(bulkActions)
                .setConcurrentRequests(10)
                .build();

        return new ElasticConnection(bulkProcessor, restHighLevelClient, restClient);
    }

    public void close() {
        if (restHighLevelClient != null) {
            try {
                restHighLevelClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
