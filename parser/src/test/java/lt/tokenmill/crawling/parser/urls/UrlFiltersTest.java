package lt.tokenmill.crawling.parser.urls;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class UrlFiltersTest {

    @Test
    public void testURLNormalizer000() {
        UrlFilters urlFilters = UrlFilters.create(Arrays.asList("a-->>b"), Arrays.asList());
        assertEquals("bbbb", urlFilters.filterWithDetails("aaaa").getNormalized());
        assertEquals("bbbb", urlFilters.filterWithDetails("abba").getNormalized());

        urlFilters = UrlFilters.create(Arrays.asList("#.*-->>"), Arrays.asList());
        String url = "http://www.tokenmill.lt/#case-understand";
        assertEquals("http://www.tokenmill.lt/", urlFilters.filterWithDetails(url).getNormalized());
    }

    @Test
    public void testURLFilters000() {
        String url = "http://www.tokenmill.lt/#case-understand";
        UrlFilters urlFilters = UrlFilters.create(Arrays.asList("#.*-->>"), Arrays.asList("+^http://www.tokenmill.lt/.*", "-.*apache.*"));
        UrlFilters.FilteringResult filteringResult = urlFilters.filterWithDetails(url);
        assertEquals(true, filteringResult.getAccepted());
        assertEquals("+^http://www.tokenmill.lt/.*", filteringResult.getFilter());
        assertEquals(1, filteringResult.getNormalizers().size());
        assertEquals("http://www.tokenmill.lt/", filteringResult.getNormalized());

        assertEquals("http://www.tokenmill.lt/", urlFilters.filter(url));
        assertEquals(null, urlFilters.filter("http://nutch.apache.org/"));

        filteringResult = urlFilters.filterWithDetails("http://nutch.apache.org/");
        assertEquals(false, filteringResult.getAccepted());
        assertEquals("-.*apache.*", filteringResult.getFilter());
    }
}
