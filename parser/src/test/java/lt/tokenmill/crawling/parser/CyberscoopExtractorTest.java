package lt.tokenmill.crawling.parser;

import lt.tokenmill.crawling.data.HttpArticleParseResult;
import lt.tokenmill.crawling.data.HttpSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class CyberscoopExtractorTest extends BaseArticleExtractorTest {

    private static final String TITLE_SELECTOR = "h1.article__title";

    private HttpSource cyberscoopSourceWithoutTitleSelector() {
        HttpSource source = new HttpSource();
        return source;
    }

    private HttpSource cyberscoopSourceWithTitleSelector() {
        HttpSource source = new HttpSource();
        source.setTitleSelectors(Arrays.asList(TITLE_SELECTOR));
        return source;
    }

    @Test
    public void testTitleExtraction000() throws Exception {
        String url = "https://www.cyberscoop.com/u-s-oil-gas-companies-still-trying-catch-cybersecurity-experts-say/";
        String html = loadArticle("cyberscoop1");
        Document document = Jsoup.parse(html, url);
        HttpArticleParseResult article = ArticleExtractor.extractArticleWithDetails(html, url, cyberscoopSourceWithoutTitleSelector(), null);
        assertEquals(1, article.getTitleMatches().size());
        assertEquals("META:og:title", article.getTitleMatches().get(0));
    }

    @Test
    public void testTitleExtraction001() throws Exception {
        String url = "https://www.cyberscoop.com/u-s-oil-gas-companies-still-trying-catch-cybersecurity-experts-say/";
        String html = loadArticle("cyberscoop1");
        Document document = Jsoup.parse(html, url);
        HttpArticleParseResult article = ArticleExtractor.extractArticleWithDetails(html, url, cyberscoopSourceWithTitleSelector(), null);
        assertEquals(1, article.getTitleMatches().size());
        assertEquals(TITLE_SELECTOR, article.getTitleMatches().get(0));
    }

}
