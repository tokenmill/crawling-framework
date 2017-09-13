package lt.tokenmill.crawling.crawler.spout;

import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.filtering.URLFilters;
import com.digitalpebble.stormcrawler.util.ConfUtils;
import lt.tokenmill.crawling.crawler.CrawlerConstants;
import lt.tokenmill.crawling.crawler.utils.UrlFilterUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UrlFilterUtilsTest {

    private static final Logger LOG = LoggerFactory.getLogger(UrlFilterUtilsTest.class);
    private final String testSourceUrl = "http://www.tokenmill.lt/";

    @Test
    public void testUrlFilters() {
        Map conf = new HashMap();
        conf.put(CrawlerConstants.URL_FILTERS_FILE, "urlfilters.json");
        String filtersConfigFile = ConfUtils.getString(conf, CrawlerConstants.URL_FILTERS_FILE);
        URLFilters filters = UrlFilterUtils.load(conf, filtersConfigFile);
        URL sourceUrl;
        try {
            sourceUrl = new URL(testSourceUrl);
        } catch (MalformedURLException e) {
            // we would have known by now as previous components check whether the URL is valid
            LOG.error("MalformedURLException on {}", testSourceUrl);
            return;
        }
        // test good URL
        assertNotNull(null, UrlFilterUtils.firstMatch(sourceUrl, new Metadata(), testSourceUrl, filters));
        // test on bad URL
        assertNull(null, UrlFilterUtils.firstMatch(sourceUrl, new Metadata(), testSourceUrl.concat("song.mp3"), filters));
    }
}
