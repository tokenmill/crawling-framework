package lt.tokenmill.crawling.adminui.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import lt.tokenmill.crawling.data.HttpSourceTest;

import java.util.Map;
import java.util.Objects;

public class HttpSourceTestCSVUtils {

    public static final String[] CSV_COLUMNS = new String[]{
            "url", "source", "html", "url_accepted", "title", "text", "date"};

    public static String[] mapHttpSourceTestToCsvRow(HttpSourceTest httpSourceTest) {
        return new String[]{
                httpSourceTest.getUrl(), httpSourceTest.getSource(),
                BaseEncoding.base64().encode(httpSourceTest.getHtml().getBytes(Charsets.UTF_8)),
                Objects.toString(httpSourceTest.getUrlAccepted(), "false"),
                Strings.nullToEmpty(httpSourceTest.getTitle()),
                Strings.nullToEmpty(httpSourceTest.getText()),
                Strings.nullToEmpty(httpSourceTest.getDate())
        };
    }

    public static HttpSourceTest mapCsvRowToHttpSourceTest(String[] row, Map<String, Integer> columnIndexes) {
        HttpSourceTest hst = new HttpSourceTest();
        hst.setUrl(Strings.emptyToNull(row[columnIndexes.get("url")]));
        hst.setSource(Strings.emptyToNull(row[columnIndexes.get("source")]));
        hst.setHtml(new String(BaseEncoding.base64().decode(row[columnIndexes.get("html")]), Charsets.UTF_8));
        hst.setUrlAccepted(Boolean.parseBoolean(row[columnIndexes.get("url_accepted")]));
        hst.setTitle(Strings.emptyToNull(row[columnIndexes.get("title")]));
        hst.setText(Strings.emptyToNull(row[columnIndexes.get("text")]));
        hst.setDate(Strings.emptyToNull(row[columnIndexes.get("date")]));
        return hst;
    }
}
