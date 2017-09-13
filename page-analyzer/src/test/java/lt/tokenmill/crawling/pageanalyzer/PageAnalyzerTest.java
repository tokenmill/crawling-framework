package lt.tokenmill.crawling.pageanalyzer;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import lt.tokenmill.crawling.data.HtmlAnalysisResult;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PageAnalyzerTest {

    @Test
    public void headersAndStatus() {
        Map<String, List<String>> headers = Maps.newHashMap();
        headers.put("Etag", Lists.newArrayList("c1dc8d7be85325149", "ed5fc4d62b84752"));
        headers.put("Date", Lists.newArrayList("Wed, 11 Jan 2017 13:00:18 GMT"));
        HashMap<String, String> config = Maps.newHashMap();
        HtmlAnalysisResult result = PageAnalyzer.analyze(config, "http://example.org", "<html></html>", 200, headers);

        assertEquals(new Integer(200), result.getHttpStatus());
        assertEquals(2, result.getHeaders().size());
        assertEquals("c1dc8d7be85325149\ned5fc4d62b84752", result.getHeaders().get("Etag"));
        assertEquals("Wed, 11 Jan 2017 13:00:18 GMT", result.getHeaders().get("Date"));
    }


    @Test
    public void htmlParsing() {
        HashMap<String, String> config = Maps.newHashMap();
        HtmlAnalysisResult result = PageAnalyzer.analyze(config, "https://bloomberg.com/", loadHtml("bloomberg.com"), 200, Maps.newHashMap());
        assertEquals("Bloomberg.com", result.getTitle());
        assertEquals(33, result.getMetaValues().size());
        assertTrue(result.getMetaValues().contains("<meta property=\"og:url\" content=\"https://www.bloomberg.com/\" data-ephemeral=\"true\">"));
        assertEquals(361, result.getLinks().size());
        assertTrue(result.getLinks().contains("https://www.bloomberg.com/news/articles/2017-01-10/netanyahu-s-grip-on-power-under-threat-as-gift-scandal-escalates"));
    }

    @Test
    @Ignore
    public void fetchAndParse() {
        HashMap<String, String> config = Maps.newHashMap();
        config.put(PageAnalyzer.CONFIG_ANALYZE_ROBOTS_TXT, "true");
        HtmlAnalysisResult result = PageAnalyzer.analyze(config, "http://www.tokenmill.lt/");
        assertEquals("TokenMill - Natural Language Processing", result.getTitle());
        assertEquals(10, result.getMetaValues().size());
        assertEquals(42, result.getLinks().size());
        assertTrue(result.getLinks().contains("http://www.tokenmill.lt/#case-monitoring"));
        assertTrue(result.getRobotsAllowedAll());
        assertFalse(result.getRobotsAllowedNone());
        assertTrue(result.getRobotsAllowedHome());
        assertEquals(Lists.newArrayList(), result.getRobotsSitemaps());
        assertEquals(Long.MIN_VALUE, (long) result.getRobotsCrawlDelay());

    }

    @Test
    @Ignore
    public void fetchAndParseRobotsTxt() {
        HashMap<String, String> config = Maps.newHashMap();
        config.put(PageAnalyzer.CONFIG_ANALYZE_ROBOTS_TXT, "true");
        HtmlAnalysisResult result = PageAnalyzer.analyze(config, "https://www.google.com");
        assertFalse(result.getRobotsAllowedAll());
        assertFalse(result.getRobotsAllowedNone());
        assertTrue(result.getRobotsAllowedHome());
        assertTrue(result.getRobotsSitemaps().contains("http://www.gstatic.com/culturalinstitute/sitemaps/www_google_com_culturalinstitute/sitemap-index.xml"));
        assertEquals(Long.MIN_VALUE, (long) result.getRobotsCrawlDelay());

    }

    private static String loadHtml(String name) {
        try {
            URL htmlResource = Resources.getResource(name + ".html");
            return Resources.toString(htmlResource, Charsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
