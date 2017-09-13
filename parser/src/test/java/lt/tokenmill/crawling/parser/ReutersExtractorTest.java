package lt.tokenmill.crawling.parser;

import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.HttpArticleParseResult;
import lt.tokenmill.crawling.data.HttpSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReutersExtractorTest extends BaseArticleExtractorTest {


    @Test
    public void testReuters1() throws Exception {
        String html = loadArticle("reuters1");
        String url = "http://www.reuters.com/finance/stocks/TEX/key-developments/article/3414284";
        HttpArticle article = ArticleExtractor.extractArticle(html, url, reutersSource(), null);
        assertEquals("Marcato reports 5.1 pct stake in Terex, to urge spinoff & restructuring- CNBC, citing source", article.getTitle());
        assertTrue(article.getText().contains("Marcato reports 5.1 pct stake in Terex, to urge spinoff & restructuring; Marcato supports Terex CEO - CNBC, citing source"));
        DateTime actualPublished = article.getPublished();
        DateTime expectedPublished = new DateTime(2016, 7, 28, 15, 35, DateTimeZone.UTC);
        assertTrue(actualPublished.toDate().equals(expectedPublished.toDate()));
    }

    @Test
    public void testReuters2() throws Exception {
        String html = loadArticle("reuters2");
        String url = "http://www.reuters.com/article/idUSFWN1B40B5";
        HttpArticleParseResult parseResult = ArticleExtractor.extractArticleWithDetails(html, url, reutersSource(), null);
        HttpArticle article = parseResult.getArticle();
        assertEquals("BRIEF-Canadian Solar unit Recurrent Energy reached commercial operation of 100 MWac/134 MWp", article.getTitle());
        assertTrue(article.getText().contains("Unit Recurrent Energy has reached commercial operation of 100 MWac/134 MWp Mustang solar power project"));
        assertEquals("LD+JSON", parseResult.getPublishedMatches().get(0));
        DateTime expectedPublished = new DateTime(2016, 8, 23, 12, 24, 3, DateTimeZone.UTC);
        DateTime actualPublished = article.getPublished();
        assertTrue(actualPublished.toDate().equals(expectedPublished.toDate()));
    }

    @Test
    public void testReuters3() throws Exception {
        String html = loadArticle("reuters3");
        String url = "http://www.reuters.com/article/us-tesla-product-idUSKCN10Y1R2";
        HttpArticle article = ArticleExtractor.extractArticle(html, url, reutersSource(), null);
        assertEquals("Tesla touts speed and driving range with new upgraded battery", article.getTitle());
        assertTrue(article.getText().contains(" models. But Musk said those were both millio"));
        DateTime expectedPublished = new DateTime(2016, 8, 23, 22, 41, 57, DateTimeZone.UTC);
        DateTime actualPublished = article.getPublished();
        assertTrue(actualPublished.toDate().equals(expectedPublished.toDate()));
    }

    @Test
    public void testReutersBlog1() throws Exception {
        String html = loadArticle("reuters-blogs1");
        String url = "http://blogs.reuters.com/breakingviews/2016/08/22/pfizer-bets-14-bln-it-knows-better-than-market/";
        HttpArticle article = ArticleExtractor.extractArticle(html, url, reutersBlogsSource(), null);
        assertEquals("Pfizer bets $14 bln it knows better than market", article.getTitle());
        assertTrue(article.getText().contains("r may believe in a far more lucrative outcom"));
        DateTime actualPublished = article.getPublished();
        assertNull(actualPublished);
    }


    private HttpSource reutersSource() {
        HttpSource source = new HttpSource();
        source.setTitleSelectors(Lists.newArrayList("h1"));
        source.setDateSelectors(Lists.newArrayList("#sigDevArticleText .timestamp"));
        source.setTextSelectors(Lists.newArrayList("#article-text p"));
        return source;
    }

    private HttpSource reutersBlogsSource() {
        HttpSource source = new HttpSource();
        source.setTextSelectors(Lists.newArrayList("#postcontent p"));
        return source;
    }
}
