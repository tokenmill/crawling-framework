package lt.tokenmill.crawling.parser.utils;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueryParserTest {

    @Test
    public void parseQuery() {
        List<String> parts = QueryParser.parseQuery("+Turkey-Inflation");
        assertEquals(Lists.newArrayList("+Turkey", "-Inflation"), parts);

        parts = QueryParser.parseQuery("+Turkey -Inflation");
        assertEquals(Lists.newArrayList("+Turkey", "-Inflation"), parts);

        parts = QueryParser.parseQuery("Turkey -Inflation");
        assertEquals(Lists.newArrayList("Turkey", "-Inflation"), parts);

        parts = QueryParser.parseQuery("+Turkey attack");
        assertEquals(Lists.newArrayList("+Turkey", "attack"), parts);
    }

}
