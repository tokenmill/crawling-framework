package lt.tokenmill.crawling.es;

import com.google.common.collect.ImmutableMap;
import lt.tokenmill.crawling.data.HttpArticle;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

        esDocumentOperations.update(article, ImmutableMap.of("TESTKEY", "TESTVAL"));
        Thread.sleep(6000);
        Map<String, Object> articleMap = esDocumentOperations.getAsMap(article.getUrl());
        assertEquals(article.getText(), articleMap.get("text"));
        assertEquals("TESTVAL", articleMap.get("TESTKEY"));
    }

    @Test
    @Ignore
    public void testDuplicateFinder() throws InterruptedException {
        ElasticConnection connection = ElasticConnection.getConnection("localhost", 9200, "http");
        EsDocumentOperations esDocumentOperations = EsDocumentOperations.getInstance(connection, "cf-docs", "doc");
        HttpArticle article = new HttpArticle();
        article.setUrl("url1");
        article.setSource("source");
        article.setTitle("title");
        article.setText("text");
        article.setTextSignature("text_signature");
        article.setPublished(DateTime.now());
        esDocumentOperations.store(article);
        Thread.sleep(6000);
        HttpArticle duplicate = esDocumentOperations.findDuplicate(article);
        assertNull(duplicate);
        article.setUrl("url2");
        esDocumentOperations.store(article);
        Thread.sleep(6000);
        assertEquals("url1", esDocumentOperations.getAsMap("url2").get("duplicate_of"));
    }
}
