package lt.tokenmill.crawling.cache;

import lt.tokenmill.crawling.cache.providers.CacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lt.tokenmill.crawling.cache.utils.FutureUtils.tryGet;
import static lt.tokenmill.crawling.cache.utils.FutureUtils.waitFor;
import static lt.tokenmill.crawling.cache.utils.HashUtils.hashKey;

public class UrlProcessingCache {
    private static final Logger LOG = LoggerFactory.getLogger(UrlProcessingCache.class);
    private final CacheProvider provider;

    public UrlProcessingCache(CacheProvider provider){
        this.provider = provider;
    }

    public static String parseDomain(String url){
        try {
            URL u = new URL(url);
            return u.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String globalNamespace(String domain){
        return String.format("global:%s", hashKey(domain));
    }

    public void addUrl(String url){
        String domain = parseDomain(url);
        waitFor(provider.addKey(globalNamespace(domain), url));
    }

    public Set<String> filterUrls(String domain, Collection<String> urls) {
        return filterUrls(domain, urls.stream());
    }

    public Set<String> filterUrls(String domain, Stream<String> urls) {
        Set<String> keys = tryGet(provider.keysInNamespace(globalNamespace(domain)), () -> Collections.emptySet());
        return urls.filter(k -> !keys.contains(hashKey(k))).collect(Collectors.toSet());
    }
}
