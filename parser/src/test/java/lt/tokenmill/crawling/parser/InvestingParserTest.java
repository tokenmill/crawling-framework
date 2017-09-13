package lt.tokenmill.crawling.parser;

import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.HttpArticleParseResult;
import lt.tokenmill.crawling.data.HttpSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvestingParserTest  extends BaseArticleExtractorTest {


    @Test
    public void testInvesting1() throws Exception {
        String html = loadArticle("investing1");
        String url = "https://www.investing.com/analysis/opening-bell:-brexit,-davos-meetings-are-today%E2%80%99s-big-drivers-200172664";
        HttpArticleParseResult result = ArticleExtractor.extractArticleWithDetails(html, url, investingSource(), null);
        HttpArticle article = result.getArticle();
        assertEquals("Opening Bell: USD Drops, Pound Pops, Yen Soars", article.getTitle());
        assertTrue(article.getText().startsWith("by Eli Wright\nAs markets in the US return from the long holiday weekend"));
        assertTrue(article.getText().endsWith("ab Corporation (NYSE:SCHW) expects EPS of $0.36."));
        DateTime actualPublished = article.getPublished();
        DateTime expectedPublished = new DateTime(2017, 1, 17, 11, 8, 00, 00, DateTimeZone.UTC);
        assertTrue(actualPublished.toDate().equals(expectedPublished.toDate()));
    }

    private HttpSource investingSource() {
        HttpSource source = new HttpSource();
        source.setTextSelectors(Lists.newArrayList("#contentSection p, #contentSection li"));
        source.setDateSelectors(Lists.newArrayList(".contentSectionDetails span"));
        source.setDateRegexps(Lists.newArrayList(".*\\((.+)\\).*"));
        return source;
    }

}
