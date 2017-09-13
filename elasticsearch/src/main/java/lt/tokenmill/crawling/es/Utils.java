package lt.tokenmill.crawling.es;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

public class Utils {

    private static final Splitter LINE_SPLITTER = Splitter.on('\n');
    private static final Joiner LINE_JOINER = Joiner.on('\n');

    public static List<String> linesToList(String text) {
        return LINE_SPLITTER.splitToList(text).stream()
                .map(String::trim)
                .filter(l -> !isNullOrEmpty(l))
                .collect(Collectors.toList());
    }

    public static String listToText(List lines) {
        return lines != null ? LINE_JOINER.join(lines) : "";
    }

    public static Object formatFieldValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            return listToText((List) value);
        } else if (value instanceof DateTime) {
            return ((DateTime) value).toDate();
        } else if (value instanceof Enum) {
            return Objects.toString(value, null);
        } else {
            return value;
        }
    }
}
