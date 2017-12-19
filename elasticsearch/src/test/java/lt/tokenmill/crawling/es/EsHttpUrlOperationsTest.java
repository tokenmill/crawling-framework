package lt.tokenmill.crawling.es;

import lt.tokenmill.crawling.data.HttpSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EsHttpUrlOperationsTest {

    private static final Logger LOG = LoggerFactory.getLogger(EsHttpUrlOperationsTest.class);

    private static final String ES_TEST_HOST = "127.0.0.1";
    private static final int ES_HTTP_TEST_PORT = 9205;
    private static final int ES_TRANSPORT_TEST_PORT = 9305;
    private static final String ES_DATA_DIRECTORY = "target/elasticsearch-data";
    private static final boolean ES_CLEAN_DATA_DIR = true;
    private static final String INDEX_ALIAS = "urls";
    private static final String DOC_TYPE = "url";
    private static final String INDEX_CONF_RESOURCE_FILE = "indices/url.json";

    private ElasticsearchTestServer elasticsearchTestServer;

    @Before // setup()
    public void before() throws Exception {
        LOG.info("Setting ES server up!");
        this.elasticsearchTestServer = ElasticsearchTestServer.builder()
                .httpPort(ES_HTTP_TEST_PORT)
                .transportPort(ES_TRANSPORT_TEST_PORT)
                .dataDirectory(ES_DATA_DIRECTORY)
                .cleanDataDir(ES_CLEAN_DATA_DIR)
                .build();
        this.elasticsearchTestServer.start();

        String indexConf = TestUtils.readResourceAsString(INDEX_CONF_RESOURCE_FILE);
        new IndexManager(ES_TEST_HOST, ES_HTTP_TEST_PORT).prepare(INDEX_ALIAS, indexConf, true);
    }

    @After
    public void after() throws Exception {
        LOG.info("Tearing ES server down.");
        this.elasticsearchTestServer.stop();
    }

    @Test
    @Ignore
    public void testEsHttpSourceOperations000() throws IOException {
        ElasticConnection connection = ElasticConnection.getConnection(ES_TEST_HOST, ES_TRANSPORT_TEST_PORT);

        EsHttpSourceOperations esHttpSourceOperations = EsHttpSourceOperations.getInstance(connection, INDEX_ALIAS, DOC_TYPE);

        assertEquals(0, esHttpSourceOperations.all().size());
        HttpSource source = new HttpSource();

//        esHttpSourceOperations.save(source);
//        assertEquals(1, esHttpSourceOperations.all().size());
    }
}
