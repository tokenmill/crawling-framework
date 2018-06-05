package lt.tokenmill.crawling.parser.urls;

import lt.tokenmill.crawling.parser.BaseArticleExtractorTest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UrlExtractorTest extends BaseArticleExtractorTest {

    @Test
    public void testExtraction00() throws Exception {
        String html = loadArticle("aljazeera1");
        String url = "https://www.aljazeera.com/news/2018/05/2000-jewish-settlers-storm-al-aqsa-setting-record-180513161200107.html";
        Document document = Jsoup.parse(html);
        assertEquals(url, UrlExtractor.extract(url, document));
        assertEquals("https://www.aljazeera.com/news/2018/05/2000-jewish-settlers-storm-al-aqsa-setting-record-180513161200107.html", UrlExtractor.extract("", document));
    }

    @Test
    public void testExtraction01() throws Exception {
        String html = loadArticle("kedainietis");
        String url = "url";
        Document document = Jsoup.parse(html);
        assertEquals(url, UrlExtractor.extract(url, document));
    }
}
