package lt.tokenmill.crawling.parser;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.parser.data.MatchedDate;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DateParser {

    private static List<String> FORMATS = Lists.newArrayList(
            "yyyy-MM-dd'T'HH:mm:ssZZZ", //2016-08-24T06:42:58+0000
            "yyyy-MM-dd'T'HH:mm:ssXXX", //2017-01-02T10:30:56+00:00
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", //2016-09-08T23:14:29.036Z
            "yyyy-MM-dd'T'HH:mm:ss", //2017-03-09T01:00:00
            "yyyy-MM-dd HH:mm:ss Z", //2017-01-16 01:45:05 -0500
            "yyyy-MM-dd HH:mm:ss", //2017-03-09 16:47:21
            "EEE MMM dd HH:mm:ss z yyyy", //Wed Jan 18 13:45:00 GMT 2017
            "EEE, dd MMM yyyy HH:mm:ss Z", //Fri, 10 Mar 2017 13:54:19 +0530
            "EEEE, dd MMMM yyyy h:mma z", //Thursday, 28 Jul 2016 11:35am EDT
            "MMM dd, yyyy hh:mma z", //Jan 17, 2017 06:08AM ET
            "EEE MMM dd, yyyy H:mma z", //Wed Aug 24, 2016 2:42am EDT
            "MMddyyyy", //02142017
            "dd MMMM yyyy", //10 March 2017
            "dd MMM, yyyy HH:mm", //13 Mar, 2017 15:49
            "MMMM dd, yyyy, HH:mm z", //March 10, 2017, 14:51 IST
            "MMMM dd, yyyy", //January 18, 2017
            "EEE, MMMM dd, yyyy",
            "yyyy/MM/dd"
    );
    private static List<SimpleDateFormat> FORMATTERS;

    private static List<SimpleDateFormat> formatsToFormatters(List<String> formats) {
        return formats.stream()
                .map(DateParser::createFormatter)
                .collect(Collectors.toList());
    }
    static {
        FORMATTERS = formatsToFormatters(FORMATS);
    }

    public static MatchedDate parse(MatchedDate matchedText, HttpSource source) {
        String value = Strings.nullToEmpty(matchedText.getValue()).trim();
        for (String regexp : source.getDateRegexps()) {
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches() && matcher.groupCount() > 0) {
                value = matcher.group(1);
            }
        }
        matchedText.setValue(value.replace("ET", "EST"));
        List<SimpleDateFormat> customFormatters = formatsToFormatters(source.getDateFormats());
        return Stream.concat(customFormatters.stream(), FORMATTERS.stream())
                .map(dateFormat -> parse(matchedText, dateFormat))
                .filter(d -> d.getDate() != null)
                .findFirst().orElse(matchedText);
    }

    private static SimpleDateFormat createFormatter(String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        formatter.setLenient(false);
        return formatter;
    }

    private static MatchedDate parse(MatchedDate matchedText, SimpleDateFormat format) {
        try {
            Date parsed = format.parse(matchedText.getValue());
            matchedText.setDate(new DateTime(parsed));
            matchedText.setPattern(format.toPattern());
            return matchedText;
        } catch (Exception e) {
            return matchedText;
        }
    }

    private static final List<String> DATE_META_KEYS = Lists.newArrayList(
            "og:article:published_time", "article:published_time",
            "om.content_publish_dte", "sailthru.date",
            "revision_date", "parsely-pub-date",
            "pubdate", "date", "published_at", "publish_date");

    private static final List<String> ITEMPROP_SELECTORS = Lists.newArrayList("[itemprop*=datePublished]", "[itemprop*=dateModified]");

    public static List<MatchedDate> extractFromMeta(Document document) {
        Map<String, String> metaValues = Maps.newHashMap();
        document.select("meta").forEach(m -> {
            String name = m.attr("name");
            String property = m.attr("property");
            String content = m.attr("content");
            if (!Strings.isNullOrEmpty(name)) {
                metaValues.put(name.toLowerCase(), content);
            } else if (!Strings.isNullOrEmpty(property)) {
                metaValues.put(property.toLowerCase(), content);
            }
        });
        return DATE_META_KEYS.stream()
                .filter(k -> metaValues.get(k) != null)
                .map(k -> new MatchedDate(metaValues.get(k), "META:" + k))
                .collect(Collectors.toList());
    }

    public static List<MatchedDate> extractFromProperties(Document document) {
        List<MatchedDate> result = Lists.newArrayList();

        for (String selector : ITEMPROP_SELECTORS) {
            document.select(selector).forEach(m -> {
                String datetime = m.attr("datetime");
                String content = m.attr("content");
                String title = m.attr("title");
                if (!Strings.isNullOrEmpty(datetime)) {
                    result.add(new MatchedDate(datetime, selector));
                } else if (!Strings.isNullOrEmpty(content)) {
                    result.add(new MatchedDate(content, selector));
                } else if (!Strings.isNullOrEmpty(title)) {
                    result.add(new MatchedDate(title, selector));
                }
            });
        }

        return result;
    }
}