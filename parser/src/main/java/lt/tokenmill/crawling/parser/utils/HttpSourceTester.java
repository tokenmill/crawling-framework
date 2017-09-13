package lt.tokenmill.crawling.parser.utils;

import com.google.common.collect.Maps;
import lt.tokenmill.crawling.data.*;
import lt.tokenmill.crawling.parser.ArticleExtractor;
import lt.tokenmill.crawling.parser.urls.UrlFilters;

import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

public class HttpSourceTester {

    public static final String URL_ACCEPTED = "url_accepted";
    public static final String TITLE = "title";
    public static final String TEXT = "text";
    public static final String DATE = "date";

    public static Map<String, Difference> test(HttpSource source, HttpSourceTest data) {
        TestResult result = new TestResult();

        String url = data.getUrl();
        UrlFilters urlFilters = UrlFilters.create(source.getUrlNormalizers(), source.getUrlFilters());
        UrlFilters.FilteringResult filteringResult = urlFilters.filterWithDetails(url);
        result.acceptedUrl(filteringResult.getAccepted(), data.getUrlAccepted());

        String html = nullToEmpty(data.getHtml()).trim();
        HttpArticleParseResult parseResult = ArticleExtractor.extractArticleWithDetails(html, url, source, null);
        HttpArticle article = parseResult.getArticle();
        result.title(nullToEmpty(article.getTitle()), nullToEmpty(data.getTitle()));
        result.text(nullToEmpty(article.getText()), nullToEmpty(data.getText()));
        result.date(article.getPublished() != null ? DataUtils.formatInUTC(article.getPublished()) : "", nullToEmpty(data.getDate()));

        return result.difference();
    }

    public static class Difference {

        private String actual;

        private String expected;

        public Difference(String actual, String expected) {
            this.actual = actual;
            this.expected = expected;
        }

        public String getActual() {
            return actual;
        }

        public String getExpected() {
            return expected;
        }

        @Override
        public String toString() {
            return "Difference{" +
                    "actual='" + actual + '\'' +
                    ", expected='" + expected + '\'' +
                    '}';
        }
    }

    public static class TestResult {

        private boolean expectedUrlAccepted;
        private boolean actualUrlAccepted;
        private String expectedTitle;
        private String actualTitle;
        private String expectedText;
        private String actualText;
        private String expectedDate;
        private String actualDate;

        void acceptedUrl(boolean actual, boolean expected) {
            this.expectedUrlAccepted = expected;
            this.actualUrlAccepted = actual;
        }

        public void title(String actual, String expected) {
            this.expectedTitle = expected.trim();
            this.actualTitle = actual.trim();
        }

        public void text(String actual, String expected) {
            this.expectedText = expected.trim();
            this.actualText = actual.trim();
        }

        public void date(String actual, String expected) {
            this.expectedDate = expected.trim();
            this.actualDate = actual.trim();
        }

        public Map<String, Difference> difference() {
            Map<String, Difference> result = Maps.newLinkedHashMap();
            if (expectedUrlAccepted != actualUrlAccepted) {
                result.put(URL_ACCEPTED,
                        new Difference(String.valueOf(actualUrlAccepted), String.valueOf(expectedUrlAccepted)));
            }
            if (!expectedTitle.equals(actualTitle)) {
                result.put(TITLE, new Difference(actualTitle, expectedTitle));
            }
            if (!expectedText.equals(actualText)) {
                result.put(TEXT, new Difference(actualText, expectedText));
            }
            if (!expectedDate.equals(actualDate)) {
                result.put(DATE, new Difference(actualDate, expectedDate));
            }
            return result;
        }
    }

}
