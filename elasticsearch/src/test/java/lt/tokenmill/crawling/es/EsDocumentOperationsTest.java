package lt.tokenmill.crawling.es;

import lt.tokenmill.crawling.data.HttpArticle;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EsDocumentOperationsTest {

    @Test
    @Ignore
    public void test() throws InterruptedException {
        ElasticConnection connection = ElasticConnection.getConnection("localhost", 9200, "http");
        EsDocumentOperations esDocumentOperations = EsDocumentOperations.getInstance(connection, "demo-docs", "doc");
        HttpArticle article = new HttpArticle();
        article.setUrl("http://www.bbc.com/news/science-environment-43727547");
        article.setTitle("title");
        article.setText("text");
        article.setPublished(DateTime.now());

        esDocumentOperations.store(article);

        Thread.sleep(6000);

        HttpArticle httpArticle = esDocumentOperations.get(article.getUrl());
        assertEquals(article.getUrl(), httpArticle.getUrl());
        assertEquals(article.getText(), httpArticle.getText());
    }
}
