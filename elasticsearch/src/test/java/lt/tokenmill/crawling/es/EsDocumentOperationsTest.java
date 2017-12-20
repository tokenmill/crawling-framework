package lt.tokenmill.crawling.es;

import lt.tokenmill.crawling.data.HttpArticle;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

public class EsDocumentOperationsTest {

    @Test
    @Ignore
    public void test() throws InterruptedException {
        ElasticConnection connection = ElasticConnection.getConnection("localhost", 9200, "http");
        EsDocumentOperations esDocumentOperations = EsDocumentOperations.getInstance(connection, "demo-docs", "doc");
        HttpArticle article = new HttpArticle();
        article.setUrl("url1");
        article.setTitle("title");
        article.setText("text");
        article.setPublished(DateTime.now());
        esDocumentOperations.store(article);
        Thread.sleep(6000);
        System.out.println(esDocumentOperations.get(article.getUrl()).getText());
    }
}
