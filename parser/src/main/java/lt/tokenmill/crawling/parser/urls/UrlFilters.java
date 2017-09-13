package lt.tokenmill.crawling.parser.urls;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UrlFilters {

    private static final Logger LOG = LoggerFactory.getLogger(UrlFilters.class);

    private List<URLNormalizer> normalizers;
    private List<UrlFilter> filters;

    private UrlFilters() {}

    public static UrlFilters create(List<String> normalizers, List<String> filters) {
        UrlFilters result = new UrlFilters();
        result.normalizers = normalizers.stream()
                .map(URLNormalizer::create)
                .collect(Collectors.toList());
        result.filters = filters.stream()
                .map(UrlFilter::create)
                .collect(Collectors.toList());
        return result;
    }

    public String filter(String url) {
        FilteringResult result = filterWithDetails(url);
        return result.getAccepted() ? result.getNormalized() : null;
    }

    public FilteringResult filterWithDetails(String url) {
        FilteringResult result = new FilteringResult(url);
        for (URLNormalizer un : normalizers) {
            String normalized = un.normalize(url);
            if (!normalized.equals(url)) {
                result.addNormalizer(un.getSource());
                url = normalized;
            }
        }
        result.setNormalized(url);
        for (UrlFilter uf : filters) {
            if (uf.matches(url)) {
                result.setFilter(uf.getSource());
                result.setAccepted(uf.accepts());
                return result;
            }
        }
        result.setAccepted(false);
        return result;
    }


    private static class URLNormalizer {

        private String source;
        private String match;
        private String replacement;

        /*
         * Normalizer "a-->>b" changes a with b.
         */
        private static URLNormalizer create(String normalization) {
            URLNormalizer normalizer = new URLNormalizer();
            String[] parts = normalization.split("-->>");
            normalizer.match = parts[0];
            normalizer.replacement = parts.length > 1 ? parts[1] : "";
            normalizer.source = normalization;
            return normalizer;
        }

        private String normalize(String url) {
            if (!Strings.isNullOrEmpty(url)) {
                return url.replaceAll(match, replacement);
            }
            return url;
        }

        public String getSource() {
            return source;
        }
    }

    private static class UrlFilter {

        private String source;
        private Boolean accept;
        private Pattern regex;

        private static UrlFilter create(String signedRegex) {
            UrlFilter filter = new UrlFilter();
            char first = signedRegex.charAt(0);
            switch (first) {
                case '+':
                    filter.accept = true;
                    break;
                case '-':
                    filter.accept = false;
                    break;
                default:
                    throw new RuntimeException("Invalid first character: " + signedRegex);
            }
            filter.regex = Pattern.compile(signedRegex.substring(1));
            filter.source = signedRegex;
            return filter;
        }

        public boolean accepts() {
            return accept;
        }

        public boolean matches(String url) {
            return regex.matcher(url).find();
        }

        public String getSource() {
            return source;
        }
    }

    public static class FilteringResult {
        private String url;
        private String normalized;
        private List<String> normalizers = Lists.newArrayList();
        private Boolean accepted;
        private String filter;

        FilteringResult(String url) {
            this.url = url;
        }

        void addNormalizer(String normalizer) {
            normalizers.add(normalizer);
        }

        void setNormalized(String normalized) {
            this.normalized = normalized;
        }

        void setFilter(String filter) {
            this.filter = filter;
        }

        void setAccepted(Boolean accepted) {
            this.accepted = accepted;
        }

        public Boolean getAccepted() {
            return accepted;
        }

        public String getNormalized() {
            return normalized;
        }

        public List<String> getNormalizers() {
            return normalizers;
        }

        public String getFilter() {
            return filter;
        }

        @Override
        public String toString() {
            return "FilteringResult{" +
                    "url='" + url + '\'' +
                    ", normalized='" + normalized + '\'' +
                    ", normalizers=" + normalizers +
                    ", accepted=" + accepted +
                    ", filter='" + filter + '\'' +
                    '}';
        }
    }
}