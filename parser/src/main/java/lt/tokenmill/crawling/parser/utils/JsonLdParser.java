package lt.tokenmill.crawling.parser.utils;

import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonLdParser {

    private static final Logger LOG = LoggerFactory.getLogger(JsonLdParser.class);

    private static final String SCHEMA_CLASS_ARTICLE = "http://schema.org/Article";
    private static final String SCHEMA_CLASS_NEWS_ARTICLE = "http://schema.org/NewsArticle";
    private static final String SCHEMA_ATTR_ARTICLE = "http://schema.org/article";
    private static final String SCHEMA_ATTR_ARTICLE_BODY = "http://schema.org/articleBody";
    private static final String SCHEMA_ATTR_HEADLINE = "http://schema.org/headline";
    private static final String SCHEMA_ATTR_PUBLISHED = "http://schema.org/datePublished";

    public static JsonLdArticle parse(List<String> jsons) {
        try {
            for (String jsonLd : jsons) {
                if (!Strings.isNullOrEmpty(jsonLd)) {
                    jsonLd = jsonLd.replaceAll("http://www\\.schema\\.org", "http://schema.org");
                    jsonLd = jsonLd.replaceAll("\"http://schema\\.org\"", "\"http://schema.org/\"");
                    Object jsonObject = JsonUtils.fromString(jsonLd);
                    Map<String, Object> data = JsonLdProcessor.compact(jsonObject, new HashMap(), new JsonLdOptions());
                    String type = Objects.toString(data.get("@type"), null);
                    if (type == null && data.get(SCHEMA_ATTR_ARTICLE) instanceof Map) {
                        data = (Map<String, Object>) data.get(SCHEMA_ATTR_ARTICLE);
                        type = Objects.toString(data.get("@type"), null);
                    }
                    if (SCHEMA_CLASS_ARTICLE.equalsIgnoreCase(type) || SCHEMA_CLASS_NEWS_ARTICLE.equalsIgnoreCase(type)) {
                        JsonLdArticle article = new JsonLdArticle();
                        String headline = Objects.toString(data.get(SCHEMA_ATTR_HEADLINE), null);
                        if (!Strings.isNullOrEmpty(headline)) {
                            headline = StringEscapeUtils.unescapeHtml4(headline);
                        }
                        article.setHeadline(headline);
                        String articleBody = Objects.toString(data.get(SCHEMA_ATTR_ARTICLE_BODY), null);
                        if (!Strings.isNullOrEmpty(articleBody)) {
                            articleBody = StringEscapeUtils.unescapeHtml4(articleBody);
                        }
                        article.setArticleBody(articleBody);
                        Object publishedObject = data.get(SCHEMA_ATTR_PUBLISHED);
                        String datePublished = null;
                        if (publishedObject instanceof String) {
                            datePublished = (String) publishedObject;
                        } else if (publishedObject instanceof Map) {
                            datePublished = Objects.toString(((Map) publishedObject).get("@value"), null);
                        }
                        article.setDatePublished(datePublished);
                        return article;
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse ld+json", e);
        }
        return null;
    }


    public static List<String> extractJsonLdParts(Document document) {
        Elements elements = document.select("script[type=\"application/ld+json\"]");
        return elements.stream()
            .map(Element::html)
            .collect(Collectors.toList());
    }

    public static class JsonLdArticle {

        private String headline;

        private String datePublished;

        private String articleBody;

        public String getHeadline() {
            return headline;
        }

        public void setHeadline(String headline) {
            this.headline = headline;
        }

        public String getDatePublished() {
            return datePublished;
        }

        public void setDatePublished(String datePublished) {
            this.datePublished = datePublished;
        }

        public String getArticleBody() {
            return articleBody;
        }

        public void setArticleBody(String articleBody) {
            this.articleBody = articleBody;
        }
    }

}
