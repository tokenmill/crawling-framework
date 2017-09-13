package lt.tokenmill.crawling.adminui;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class HttpSourceTestsCache {

    private static final Cache<String, HttpSourceTest> CACHE = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.DAYS)
            .build();

    public static HttpSourceTest get(String sourceUrl) {
        HttpSourceTest test = CACHE.getIfPresent(sourceUrl.toLowerCase());
        return test != null ? test : new HttpSourceTest("", "");
    }

    public static void put(String sourceUrl, String url, String html) {
        CACHE.put(sourceUrl.toLowerCase(),
                new HttpSourceTest(Strings.nullToEmpty(url), Strings.nullToEmpty(html)));
    }

    public static class HttpSourceTest {

        private String url;
        private String html;

        public HttpSourceTest(String url, String html) {
            this.url = url;
            this.html = html;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }
    }
}
