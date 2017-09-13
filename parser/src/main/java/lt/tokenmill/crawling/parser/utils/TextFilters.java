package lt.tokenmill.crawling.parser.utils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class TextFilters {

    // Normalizer is of format [match regexp]-->>[replacement string]
    // Normalizers that don't match the format are ignored
    // [match regexp]s that don't compile are ignored
    // String t can be null.
    // if textNormalizers is null then t is returned.
    public static String normalizeText(String t, List<String> textNormalizers) {
        t = Objects.toString(t, "");
        if (textNormalizers == null)
            return t;
        return textNormalizers.stream()
                .filter(tn -> tn.contains("-->>"))
                .reduce(t, (a, tn) -> {
                    String[] parts = tn.split("-->>");
                    String match = parts[0];
                    try {
                        Pattern.compile(match);
                    } catch (Exception e) {
                        return a;
                    }
                    String replacement = parts.length > 1 ? parts[1] : "";
                    return a.replaceAll(match, replacement);
                }).trim();
    }

}
