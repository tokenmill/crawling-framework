package lt.tokenmill.crawling.adminui.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import lt.tokenmill.crawling.data.HttpSourceTest;
import org.junit.Test;

import java.net.URL;
import java.time.Instant;
import java.util.Map;

import static lt.tokenmill.crawling.adminui.utils.HttpSourceTestCSVUtils.CSV_COLUMNS;
import static org.junit.Assert.assertEquals;

public class HttpSourceTestCSVUtilsTest {

    protected String loadHtml(String name) throws Exception {
        URL htmlResource = Resources.getResource(name + ".html");
        return Resources.toString(htmlResource, Charsets.UTF_8);
    }

    @Test
    public void testHttpSourceTestToCsvAndBack() throws Exception {
        HttpSourceTest httpSourceTest = new HttpSourceTest();
        httpSourceTest.setUrl("http://www.tokenmill.lt/");
        httpSourceTest.setSource("http://www.tokenmill.lt/");
        httpSourceTest.setHtml(loadHtml("www.tokenmill.lt"));
        httpSourceTest.setUrlAccepted(true);
        httpSourceTest.setTitle("TokenMill");
        httpSourceTest.setText("Some text");
        httpSourceTest.setDate(Instant.now().toString());

        String[] csvRow = HttpSourceTestCSVUtils.mapHttpSourceTestToCsvRow(httpSourceTest);
        String[] headerLine = CSV_COLUMNS;
        Map<String, Integer> columnIndexes = CSVUtils.resolveColumnIndexes(headerLine, CSV_COLUMNS);
        HttpSourceTest fromRow = HttpSourceTestCSVUtils.mapCsvRowToHttpSourceTest(csvRow, columnIndexes);
        assertEquals(httpSourceTest.getUrl(), fromRow.getUrl());
        assertEquals(httpSourceTest.getSource(), fromRow.getSource());
        assertEquals(httpSourceTest.getHtml(), fromRow.getHtml());
        assertEquals(httpSourceTest.getUrlAccepted(), fromRow.getUrlAccepted());
        assertEquals(httpSourceTest.getTitle(), fromRow.getTitle());
        assertEquals(httpSourceTest.getText(), fromRow.getText());
        assertEquals(httpSourceTest.getDate(), fromRow.getDate());
    }
}
