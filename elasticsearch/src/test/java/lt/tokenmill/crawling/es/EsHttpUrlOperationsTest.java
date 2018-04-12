package lt.tokenmill.crawling.es;

import lt.tokenmill.crawling.data.HttpUrl;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class EsHttpUrlOperationsTest {

    private static final Logger LOG = LoggerFactory.getLogger(EsHttpUrlOperationsTest.class);

    private static final String ES_TEST_HOST = "127.0.0.1";
    private static final int ES_HTTP_TEST_PORT = 9200;
    private static final String ES_REST_TEST_SCHEME = "http";
    private static final String INDEX_ALIAS = "demo-urls";
    private static final String DOC_TYPE = "url";


    @Test
    @Ignore
    public void testEsHttpSourceOperations000() throws IOException, InterruptedException {
        ElasticConnection connection = ElasticConnection.getConnection(ES_TEST_HOST, ES_HTTP_TEST_PORT, ES_REST_TEST_SCHEME);
        EsHttpUrlOperations esHttpUrlOperations = EsHttpUrlOperations.getInstance(connection, INDEX_ALIAS, DOC_TYPE);

        String url = "http://www.bbc.com/news/science-environment-43727547";
        String source = "www.bbc.com";
        esHttpUrlOperations.upsertUrlStatus(url, null, source, true, "a");
        Thread.sleep(6000);
        esHttpUrlOperations.upsertUrlStatus(url, null, source, false, "b");
        Thread.sleep(6000);
        List<HttpUrl> urls = esHttpUrlOperations.findUrlsByStatusAndSource("b", source, 10);
        assertTrue(urls.size() > 0);
    }
}
