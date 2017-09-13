package lt.tokenmill.crawling.adminui.utils;

import com.google.common.collect.Maps;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '\"';
    private static final char DEFAULT_ESCAPE = '\\';

    public static CSVWriter createDefaultWriter(Writer writer) {
        return new CSVWriter(writer, DEFAULT_SEPARATOR, DEFAULT_QUOTE, DEFAULT_ESCAPE);
    }

    public static CSVReader createDefaultReader(Reader reader) {
        return new CSVReader(reader, DEFAULT_SEPARATOR, DEFAULT_QUOTE, DEFAULT_ESCAPE);
    }

    public static CSVReader createDefaultReader(String csv) {
        return createDefaultReader(new StringReader(csv));
    }

    public static Map<String, Integer> resolveColumnIndexes(String[] columns, String[] headers) {
        Map<String, Integer> result = Maps.newHashMap();
        for (String column : columns) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase(column)) {
                    result.put(column, i);
                }
            }
        }
        return result;
    }
}
