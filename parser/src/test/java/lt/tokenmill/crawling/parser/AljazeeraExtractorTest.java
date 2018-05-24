package lt.tokenmill.crawling.parser;

import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.HttpSource;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

public class AljazeeraExtractorTest extends BaseArticleExtractorTest {

    @Test
    public void testFortune2() throws Exception {
        String html = loadArticle("aljazeera1");
        String url = "https://www.aljazeera.com/news/2018/05/2000-jewish-settlers-storm-al-aqsa-setting-record-180513161200107.html";
        HttpArticle article = ArticleExtractor.extractArticle(html, url, getSourceConf(), null);
        assertEquals("2018-05-13T00:00:00.000Z", article.getPublished().toInstant().toString());
    }

    private HttpSource getSourceConf() {
        HttpSource source = new HttpSource();
        source.setDateSelectors(Arrays.asList(".article-duration"));
        return source;
    }

}
