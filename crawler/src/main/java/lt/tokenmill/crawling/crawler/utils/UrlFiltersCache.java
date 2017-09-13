package lt.tokenmill.crawling.crawler.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.parser.urls.UrlFilters;

import java.util.concurrent.TimeUnit;

public class UrlFiltersCache {

    private static final Cache<String, UrlFilters> CACHE;

    static {
        CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    public static UrlFilters get(HttpSource source) {
        UrlFilters filters = CACHE.getIfPresent(source.getUrl());
        if (filters == null) {
            filters = UrlFilters.create(source.getUrlNormalizers(), source.getUrlFilters());
            CACHE.put(source.getUrl(), filters);
        }
        return filters;
    }

    public static void invalidate() {
        CACHE.invalidateAll();
    }
}