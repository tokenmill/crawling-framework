package lt.tokenmill.crawling.parser;

import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.HttpArticleParseResult;
import lt.tokenmill.crawling.data.HttpSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BloombergExtractorTest extends BaseArticleExtractorTest {


    @Test
    public void testBloomberg1() throws Exception {
        String html = loadArticle("bloomberg1");
        String url = "http://www.bloomberg.com/news/articles/2016-09-08/japan-index-futures-signal-bounce-as-ecb-outlook-weighs-on-bonds";
        HttpArticleParseResult parseResult = ArticleExtractor.extractArticleWithDetails(html, url, bloombergSource(), null);
        HttpArticle article = parseResult.getArticle();
        assertEquals("Stocks Sink With Bonds, Dollar Rallies as Complacency Broken", article.getTitle());
        assertTrue(article.getText().contains("erted declines of this size in stocks and bonds are rare though not "));
        assertTrue(article.getText().startsWith("Tranquility that has enveloped global"));
        assertEquals(parseResult.getPublishedMatches().get(0), "META:parsely-pub-date");
        DateTime actualPublished = article.getPublished();
        DateTime expectedPublished = new DateTime(2016, 9, 8, 23, 14, 29, 36, DateTimeZone.UTC);
        assertTrue(actualPublished.toDate().equals(expectedPublished.toDate()));
    }

    private HttpSource bloombergSource() {
        HttpSource source = new HttpSource();
        return source;
    }

}
