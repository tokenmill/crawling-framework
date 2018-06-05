package lt.tokenmill.crawling.parser;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lt.tokenmill.crawling.data.HttpArticle;
import lt.tokenmill.crawling.data.HttpArticleParseResult;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.parser.data.MatchedDate;
import lt.tokenmill.crawling.parser.data.MatchedString;
import lt.tokenmill.crawling.parser.urls.UrlExtractor;
import lt.tokenmill.crawling.parser.utils.JsonLdParser;
import lt.tokenmill.crawling.parser.utils.TextFilters;
import lt.tokenmill.crawling.parser.utils.TextProfileSignature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.stream.Collectors;

public class ArticleExtractor {

    private static TextProfileSignature textProfileSignature = new TextProfileSignature();

    public static HttpArticle extractArticle(String html, String url, HttpSource source, String publishedHint) {
        return extractArticleWithDetails(html, url, source, publishedHint).getArticle();
    }

    public static HttpArticleParseResult extractArticleWithDetails(String html, String url, HttpSource source, String publishedHint) {
        Document document = Jsoup.parse(html, url);
        HttpArticleParseResult result = new HttpArticleParseResult();
        HttpArticle article = new HttpArticle();
        article.setUrl(UrlExtractor.extract(url, document));
        article.setSource(source.getUrl());
        article.setLanguage(source.getLanguage());
        article.setAppIds(source.getAppIds());
        article.setCategories(source.getCategories());

        List<String> ldJsons = JsonLdParser.extractJsonLdParts(document);
        JsonLdParser.JsonLdArticle ldJsonArticle = JsonLdParser.parse(ldJsons);

        List<MatchedString> titles = extractTitlesWithJsoup(document, ldJsonArticle, source);
        article.setTitle(titles.stream().map(MatchedString::getValue).collect(Collectors.joining("\n")));
        result.setTitleMatches(titles.stream().map(MatchedString::getMatch).collect(Collectors.toList()));

        List<MatchedString> texts = extractTextsWithJsoup(document, source);
        article.setText(texts.stream()
                .map(MatchedString::getValue)
                .map(t -> TextFilters.normalizeText(t, source.getTextNormalizers()))
                .collect(Collectors.joining("\n")));
        article.setTextSignature(textProfileSignature.getSignature(article.getText()));
        result.setTextMatches(texts.stream().map(MatchedString::getMatch).distinct().collect(Collectors.toList()));

        List<MatchedDate> publicationDates = extractPublicationDates(html, document, ldJsonArticle, source, publishedHint);
        MatchedDate publicationDate = publicationDates.stream().filter(d -> d.getDate() != null).findFirst().orElse(null);
        article.setPublished(publicationDate != null ? publicationDate.getDate() : null);
        result.setPublishedPattern(publicationDate != null ? publicationDate.getPattern() : null);
        List<String> publishedTexts = publicationDate != null ?
                Lists.newArrayList(publicationDate.getValue()) : publicationDates.stream().map(MatchedDate::getValue).collect(Collectors.toList());
        result.setPublishedTexts(publishedTexts);
        List<String> publishedMatches = publicationDate != null ?
                Lists.newArrayList(publicationDate.getMatch()) : publicationDates.stream().map(MatchedDate::getMatch).collect(Collectors.toList());

        result.setPublishedMatches(publishedMatches);

        result.setArticle(article);
        return result;
    }

    private static List<MatchedDate> extractPublicationDates(String html, Document document,
                                                             JsonLdParser.JsonLdArticle ldJsonArticle,
                                                             HttpSource source, String publishedHint) {
        List<MatchedDate> dates = Lists.newArrayList();
        if (publishedHint != null) {
            dates.add(new MatchedDate(publishedHint, "HINT"));
        }
        for (String selector : source.getDateSelectors()) {
            document.select(selector).forEach(e -> dates.add(new MatchedDate(e.text(), selector)));
        }
        if (ldJsonArticle != null && !Strings.isNullOrEmpty(ldJsonArticle.getDatePublished())) {
            dates.add(new MatchedDate(ldJsonArticle.getDatePublished(), "LD+JSON"));
        }
        dates.addAll(DateParser.extractFromMeta(document));
        dates.addAll(DateParser.extractFromProperties(document));
        return dates.stream()
                .map(d -> DateParser.parse(d, source))
                .filter(d -> d != null)
                .collect(Collectors.toList());
    }

    private static MatchedDate parseDate(MatchedDate matchedText, HttpSource source) {
        return DateParser.parse(matchedText, source);
    }

    private static List<MatchedString> extractTextsWithJsoup(Document document, HttpSource source) {
        List<MatchedString> texts = Lists.newArrayList();
        for (String selector : source.getTextSelectors()) {
            document.select(selector).forEach(e -> texts.add(new MatchedString(e.text(), selector)));
        }
        if (!texts.isEmpty()) {
            return texts;
        }
        String itemPropValue = document.select("[itemprop*=articleBody] p").text();
        if (itemPropValue != null && !itemPropValue.trim().isEmpty()) {
            return Lists.newArrayList(new MatchedString(itemPropValue, "[itemprop*=articleBody] p"));
        }
        return document.select("p").stream()
                .map(e -> new MatchedString(e.text(), "p"))
                .collect(Collectors.toList());
    }

    private static List<MatchedString> extractTitlesWithJsoup(Document document,
                                                              JsonLdParser.JsonLdArticle ldJsonArticle,
                                                              HttpSource source) {
        List<MatchedString> titles = Lists.newArrayList();
        if (source.getTitleSelectors().size() > 0) {
            for (String selector : source.getTitleSelectors()) {
                document.select(selector).forEach(e -> titles.add(new MatchedString(e.text(), selector)));
            }
        } else {
            if (ldJsonArticle != null && Strings.isNullOrEmpty(ldJsonArticle.getHeadline())) {
                titles.add(new MatchedString(ldJsonArticle.getHeadline(), "LD+JSON"));
            }
            titles.addAll(TitleParser.extractFromMeta(document));
        }
        if (titles.isEmpty()) {
            titles.addAll(document.select("h1").stream().map(e -> new MatchedString(e.text(), "h1")).collect(Collectors.toList()));
            titles.addAll(document.select("title").stream().map(e -> new MatchedString(e.text(), "title")).collect(Collectors.toList()));
        }
        return titles.stream()
                .map(mv -> {
                    //Drop endings like ' | Reuters.com'
                    mv.setValue(mv.getValue().replaceAll("\\s*\\|.+", ""));
                    return mv;
                })
                .filter(mv -> !mv.getValue().contains("${")) //Drop titles which contain variables
                .distinct()
                .limit(1)
                .collect(Collectors.toList());
    }


}