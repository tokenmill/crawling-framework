package lt.tokenmill.crawling.adminui.utils;

import lt.tokenmill.crawling.data.HttpSource;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static lt.tokenmill.crawling.adminui.utils.HttpSourceCSVUtils.CSV_COLUMNS;
import static org.junit.Assert.assertEquals;

public class HttpSourcesCSVUtilsTest {

    @Test
    public void testHttpSourcesToCsvAndBack() {
        HttpSource source = new HttpSource();
        source.setUrl("url");
        source.setName("name");
        source.setLanguage("language");
        source.setTimezone("timezone");
        source.setEnabled(true);
        source.setDiscoveryEnabled(true);
        source.setUrlRecrawlDelayInSecs(1);
        source.setFeedRecrawlDelayInSecs(1);
        source.setSitemapRecrawlDelayInSecs(1);
        source.setUrls(Arrays.asList("url1", "url2"));
        source.setFeeds(Arrays.asList("feed1", "feed2"));
        source.setSitemaps(Arrays.asList("sitemap1", "sitemap2"));
        source.setCategories(Arrays.asList("cat1", "cat2"));
        source.setAppIds(Arrays.asList("app1", "app2"));
        source.setUrlFilters(Arrays.asList("f1", "f2"));
        source.setUrlNormalizers(Arrays.asList("n1", "n2"));
        source.setTitleSelectors(Arrays.asList("ts1", "ts2"));
        source.setTextSelectors(Arrays.asList("ts1", "ts2"));
        source.setTextNormalizers(Arrays.asList("tn1", "tn2"));
        source.setDateSelectors(Arrays.asList("ds1", "ds2"));
        source.setDateRegexps(Arrays.asList("dr1", "dr2"));
        source.setDateFormats(Arrays.asList("df1", "df2"));

        String[] row = HttpSourceCSVUtils.mapHttpSourceToCsvRow(source);
        String[] headerLine = CSV_COLUMNS;
        Map<String, Integer> columnIndexes = CSVUtils.resolveColumnIndexes(headerLine, CSV_COLUMNS);
        HttpSource fromRow = HttpSourceCSVUtils.mapCsvRowToHttpSource(row, columnIndexes);
        assertEquals(source, fromRow);
    }
}
