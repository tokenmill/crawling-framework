package lt.tokenmill.crawling.es;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lt.tokenmill.crawling.data.HttpSource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EsHttpSourcesCache {


    private static LoadingCache<String, HttpSource> INSTANCE;

    private static synchronized LoadingCache<String, HttpSource> getInstance(
            final EsHttpSourceOperations operations) {
        if (INSTANCE == null) {
            INSTANCE = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, HttpSource>() {
                        public HttpSource load(String url) {
                            return operations.get(url);
                        }
                    });
        }
        return INSTANCE;
    }

    public static HttpSource get(EsHttpSourceOperations operations, String source) {
        try {
            return getInstance(operations).get(source);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static void invalidate() {
        if (INSTANCE != null) {
            INSTANCE.invalidateAll();
        }
    }

}
