package lt.tokenmill.crawling.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ElasticConnection {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticConnection.class);

    private static final String DEFAULT_HOSTNAME = "0.0.0.0";
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String hostname = DEFAULT_HOSTNAME;
        private int restPort = DEFAULT_REST_PORT;
        private String restScheme = DEFAULT_REST_SCHEME;
        private String flushIntervalString = ElasticConnection.DEFAULT_FLUSH_INTERVAL_STRING;
        private int bulkActions = DEFAULT_BULK_ACTIONS;
        private BulkProcessor.Listener listener = DEFAULT_BULK_LISTENER;

        public Builder() {}

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder restPort(int restPort) {
            this.restPort = restPort;
            return this;
        }

        public Builder restScheme(String restScheme) {
            this.restScheme = restScheme;
            return this;
        }

        public Builder flushIntervalString(String flushIntervalString) {
            this.flushIntervalString = flushIntervalString;
            return this;
        }

        public Builder bulkActions(int bulkActions) {
            this.bulkActions = bulkActions;
            return this;
        }

        public Builder listener(BulkProcessor.Listener listener) {
            this.listener = listener;
            return this;
        }

        public ElasticConnection build() {
            return getConnection(this.hostname, this.restPort, this.restScheme, this.flushIntervalString,
                    this.bulkActions, this.listener);
        }
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return restHighLevelClient;
    }

    public BulkProcessor getProcessor() {
        return processor;
    }

    public static ElasticConnection getConnection(String hostname) {
        return getConnection(hostname, DEFAULT_REST_PORT, DEFAULT_REST_SCHEME, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_ACTIONS, DEFAULT_BULK_LISTENER);
    }

    public static ElasticConnection getConnection(String hostname, int restPort, String restScheme) {
        return getConnection(hostname, restPort, restScheme, DEFAULT_FLUSH_INTERVAL_STRING, DEFAULT_BULK_ACTIONS, DEFAULT_BULK_LISTENER);
    }

    private static ElasticConnection getConnection(String hostname, int restPort, String restScheme, String flushIntervalString, int bulkActions, BulkProcessor.Listener listener) {
        LOG.info("Creating ElasticConnection with params {} {} {}", hostname, restPort, restScheme);
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
            for (BulkItemResponse item : response.getItems()) {
                if (item.isFailed()) {
                    LOG.error("Bulk item failure: '{}' for request '{}'",
                            item.getFailure(), request.requests().get(item.getItemId()));
                }
            }
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable response) {
            LOG.error("Bulk failed:" + response);
        }

        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
            for (DocWriteRequest r :request.requests()) {
                try {
                    if (r instanceof IndexRequest) {
                        IndexRequest indexRequest = (IndexRequest) r;
                        indexRequest.id(URLDecoder.decode(indexRequest.id(), "utf-8"));

                    } else if (r instanceof UpdateRequest) {
                        UpdateRequest updateRequest = (UpdateRequest) r;
                        updateRequest.id(URLDecoder.decode(updateRequest.id(), "utf-8"));
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
