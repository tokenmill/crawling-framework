package lt.tokenmill.crawling.adminui.utils;

import com.google.common.base.Strings;
import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.es.Utils;

import java.util.Map;
import java.util.Objects;

public class HttpSourceCSVUtils {

    public static final String[] CSV_COLUMNS = new String[]{
            "url", "name", "language", "timezone", "enabled",
            "discovery_enabled", "url_crawl_delay_secs", "feed_crawl_delay_secs",
            "sitemap_crawl_delay_secs", "urls", "feeds", "sitemaps",
            "categories", "app_ids",
            "url_filters", "url_normalizers", "title_selectors",
            "text_selectors", "text_normalizers",
            "date_selectors", "date_regexps", "date_formats"};

    public static String[] mapHttpSourceToCsvRow(HttpSource ld) {
        return new String[]{
                ld.getUrl(), ld.getName(), ld.getLanguage(), ld.getTimezone(),
                String.valueOf(ld.isEnabled()), String.valueOf(ld.isDiscoveryEnabled()),
                Objects.toString(ld.getUrlRecrawlDelayInSecs(), ""),
                Objects.toString(ld.getFeedRecrawlDelayInSecs(), ""),
                Objects.toString(ld.getSitemapRecrawlDelayInSecs(), ""),
                Utils.listToText(ld.getUrls()), Utils.listToText(ld.getFeeds()), Utils.listToText(ld.getSitemaps()),
                Utils.listToText(ld.getCategories()), Utils.listToText(ld.getAppIds()),
                Utils.listToText(ld.getUrlFilters()), Utils.listToText(ld.getUrlNormalizers()),
                Utils.listToText(ld.getTitleSelectors()),
                Utils.listToText(ld.getTextSelectors()), Utils.listToText(ld.getTextNormalizers()),
                Utils.listToText(ld.getDateSelectors()), Utils.listToText(ld.getDateRegexps()),
                Utils.listToText(ld.getDateFormats())
        };
    }

    public static HttpSource mapCsvRowToHttpSource(String[] row, Map<String, Integer> columnIndexes) {
        HttpSource hs = new HttpSource();
        hs.setUrl(Strings.emptyToNull(row[columnIndexes.get("url")]));
        hs.setName(Strings.emptyToNull(row[columnIndexes.get("name")]));
        hs.setLanguage(Strings.emptyToNull(row[columnIndexes.get("language")]));
        hs.setTimezone(Strings.emptyToNull(row[columnIndexes.get("timezone")]));
        hs.setEnabled(Boolean.parseBoolean(row[columnIndexes.get("enabled")]));
        hs.setDiscoveryEnabled(Boolean.parseBoolean(row[columnIndexes.get("discovery_enabled")]));
        hs.setUrlRecrawlDelayInSecs(DataUtils.tryParseInteger(row[columnIndexes.get("url_crawl_delay_secs")]));
        hs.setFeedRecrawlDelayInSecs(DataUtils.tryParseInteger(row[columnIndexes.get("feed_crawl_delay_secs")]));
        hs.setSitemapRecrawlDelayInSecs(DataUtils.tryParseInteger(row[columnIndexes.get("sitemap_crawl_delay_secs")]));
        hs.setUrls(DataUtils.parseStringList(row[columnIndexes.get("urls")]));
        hs.setFeeds(DataUtils.parseStringList(row[columnIndexes.get("feeds")]));
        hs.setSitemaps(DataUtils.parseStringList(row[columnIndexes.get("sitemaps")]));
        hs.setCategories(DataUtils.parseStringList(row[columnIndexes.get("categories")]));
        hs.setAppIds(DataUtils.parseStringList(row[columnIndexes.get("app_ids")]));
        hs.setUrlFilters(DataUtils.parseStringList(row[columnIndexes.get("url_filters")]));
        hs.setUrlNormalizers(DataUtils.parseStringList(row[columnIndexes.get("url_normalizers")]));
        hs.setTitleSelectors(DataUtils.parseStringList(row[columnIndexes.get("title_selectors")]));
        hs.setTextSelectors(DataUtils.parseStringList(row[columnIndexes.get("text_selectors")]));
        hs.setTextNormalizers(DataUtils.parseStringList(row[columnIndexes.get("text_normalizers")]));
        hs.setDateSelectors(DataUtils.parseStringList(row[columnIndexes.get("date_selectors")]));
        hs.setDateRegexps(DataUtils.parseStringList(row[columnIndexes.get("date_regexps")]));
        hs.setDateFormats(DataUtils.parseStringList(row[columnIndexes.get("date_formats")]));
        return hs;
    }
}
