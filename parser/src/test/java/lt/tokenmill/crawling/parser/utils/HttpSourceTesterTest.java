package lt.tokenmill.crawling.parser.utils;

import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.data.HttpSourceTest;
import lt.tokenmill.crawling.parser.ArticleExtractor;
import lt.tokenmill.crawling.parser.BaseArticleExtractorTest;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HttpSourceTesterTest extends BaseArticleExtractorTest {

    @Test
    public void exactMatch() throws Exception {
        String html = loadArticle("reuters3");
        String url = "http://www.reuters.com/article/us-tesla-product-idUSKCN10Y1R2";

        HttpSource source = new HttpSource();
        source.setUrlFilters(Lists.newArrayList("+https?://www.reuters.com/.+$"));

        HttpArticle article = ArticleExtractor.extractArticle(html, url, source, null);

        HttpSourceTest sourceTest = new HttpSourceTest();
        sourceTest.setHtml(html);
        sourceTest.setUrl(url);
        sourceTest.setUrlAccepted(true);
        sourceTest.setTitle(article.getTitle());
        sourceTest.setDate(DataUtils.formatInUTC(article.getPublished()));
        sourceTest.setText(article.getText());

        Map<String, HttpSourceTester.Difference> differences = HttpSourceTester.test(source, sourceTest);
        assertEquals(0, differences.size());
    }

    @Test
    public void allDifferent() throws Exception {
        String html = loadArticle("reuters3");
        String url = "http://www.reuters.com/article/us-tesla-product-idUSKCN10Y1R2";

        HttpSource source = new HttpSource();
        source.setUrlFilters(Lists.newArrayList("+https?://www.reuters.com/.+$"));

        HttpArticle article = ArticleExtractor.extractArticle(html, url, source, null);

        HttpSourceTest sourceTest = new HttpSourceTest();
        sourceTest.setHtml(html);
        sourceTest.setUrl(url);
        sourceTest.setUrlAccepted(false);
        sourceTest.setTitle("Title");
        sourceTest.setDate("Published");
        sourceTest.setText("Text");

        Map<String, HttpSourceTester.Difference> differences = HttpSourceTester.test(source, sourceTest);
        assertEquals(4, differences.size());
        assertEquals("false", differences.get(HttpSourceTester.URL_ACCEPTED).getExpected());
        assertEquals("true", differences.get(HttpSourceTester.URL_ACCEPTED).getActual());
        assertEquals("Title", differences.get(HttpSourceTester.TITLE).getExpected());
        assertEquals(article.getTitle(), differences.get(HttpSourceTester.TITLE).getActual());
        assertEquals("Published", differences.get(HttpSourceTester.DATE).getExpected());
        assertEquals(DataUtils.formatInUTC(article.getPublished()), differences.get(HttpSourceTester.DATE).getActual());
        assertEquals("Text", differences.get(HttpSourceTester.TEXT).getExpected());
        assertEquals(article.getText(), differences.get(HttpSourceTester.TEXT).getActual());
    }
}
