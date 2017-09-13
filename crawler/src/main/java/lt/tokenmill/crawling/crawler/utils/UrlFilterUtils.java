package lt.tokenmill.crawling.crawler.utils;

import com.digitalpebble.stormcrawler.Metadata;
import com.digitalpebble.stormcrawler.filtering.URLFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class UrlFilterUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UrlFilterUtils.class);

    public static URLFilters load(Map conf, String filtersConfigFile) {
        if (filtersConfigFile != null) {
            try {
                URLFilters loaded = new URLFilters(conf, filtersConfigFile);
                LOG.info("Loaded URLFilters from '{}'", filtersConfigFile);
                return loaded;
            } catch (IOException e) {
                LOG.error("Exception caught while loading the URLFilters");
                throw new RuntimeException("Exception caught while loading the URLFilters", e);
            }
        } else {
            return URLFilters.emptyURLFilters;
        }
    }

    public static String firstMatch(URL sourceUrl, Metadata metadata, String targetUrl, URLFilters...filters) {
        for (URLFilters filter : filters) {
            String filtered = filter.filter(sourceUrl, metadata, targetUrl);
            if (filtered != null) {
                return filtered;
            }
        }
        return null;
    }

}
