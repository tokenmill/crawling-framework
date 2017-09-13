package lt.tokenmill.crawling.data;

import com.google.common.base.Splitter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class DataUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static Integer tryParseInteger(Object object) {
        try {
            return (object != null) ? Integer.parseInt(object.toString()) : null;
        } catch (NumberFormatException e) {
        }
        return null;
    }

    public static Long tryParseLong(Object object) {
        try {
            return (object != null) ? Long.parseLong(object.toString()) : null;
        } catch (NumberFormatException e) {
        }
        return null;
    }

    public static List<String> parseStringList(Object object) {
        if (object == null) {
            return null;
        }
        return Splitter.onPattern("(?:\r?\n)+")
                .splitToList(object.toString())
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public static String formatInUTC(DateTime date) {
        return date != null ? FORMATTER.print(date.toDateTime(DateTimeZone.UTC)) : null;
    }

    public static DateTime parseFromUTC(String date) {
        return date != null ? FORMATTER.parseDateTime(date) : null;
    }
}
