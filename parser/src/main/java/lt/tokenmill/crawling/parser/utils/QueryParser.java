package lt.tokenmill.crawling.parser.utils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QueryParser {

    public static List<String> parseQuery(String query) {
        List<String> result = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(query)) {
            query = query.replaceAll("(\\s*[+-]\\s*)", "#SPLIT#$1");
            return Arrays.stream(query.split("(#SPLIT#| )"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return result;
    }

}
