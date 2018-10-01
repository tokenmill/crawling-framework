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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.junit.Assert.assertNotNull;

public class ElasticConnectionTest {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticConnectionTest.class);
    @Test
    public void testConnectionBuilder() {
        ElasticConnection connection = ElasticConnection.builder().build();
        assertNotNull(connection.getRestHighLevelClient());
    }

    @Test
    public void testBuilder() {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {

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
        ElasticConnection connection = ElasticConnection.builder()
                .hostname("0.0.0.0")
                .restPort(443)
                .restScheme("https")
                .bulkActions(1)
                .flushIntervalString("1s")
                .listener(listener)
                .build();
        assertNotNull(connection);
    }
}
