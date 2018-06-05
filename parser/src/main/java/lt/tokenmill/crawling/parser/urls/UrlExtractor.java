package lt.tokenmill.crawling.parser.urls;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UrlExtractor {

    private static boolean isAbsolute(String url) {
        try {
            URI uri = new URI(url);
            return uri.isAbsolute();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Set<String> extract(Document document) {
        Set<String> canonicalUrls = new HashSet<>();
        if (document == null) {
            return canonicalUrls;
        }

        Elements elements = document.select("meta[property=og:url]");
        elements.forEach(element -> {
            String attr = element.attr("content");
            if (attr != null) {
                canonicalUrls.add(attr);
            }
        });

        elements = document.select("link[rel=canonical]");
        elements.forEach(element -> {
            String attr = element.attr("href");
            if (attr != null) {
                canonicalUrls.add(attr);
            }
        });

        return canonicalUrls.stream()
                .filter(UrlExtractor::isAbsolute)
                .collect(Collectors.toSet());
    }

    public static String extract(String url, Document document) {
        Set<String> canonicalUrls = extract(document);
        if (canonicalUrls == null) {
            return url;
        } else {
            return canonicalUrls.stream().findFirst().orElse(url);
        }
    }
}
