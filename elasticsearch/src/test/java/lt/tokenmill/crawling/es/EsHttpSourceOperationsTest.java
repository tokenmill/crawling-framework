package lt.tokenmill.crawling.es;

import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.PageableList;
import org.junit.Ignore;
import org.junit.Test;

public class EsHttpSourceOperationsTest {

    @Test
    @Ignore
    public void test() {
        ElasticConnection connection = ElasticConnection.getConnection("localhost", 9200, "http");
        EsHttpSourceOperations esHttpSourceOperations = new EsHttpSourceOperations(connection, "demo-http_sources", "http_source");
        PageableList<HttpSource> data = esHttpSourceOperations.filter(null);
        for (HttpSource source : data.getItems()) {
            System.out.println(">>" + source);
        }
    }
}
