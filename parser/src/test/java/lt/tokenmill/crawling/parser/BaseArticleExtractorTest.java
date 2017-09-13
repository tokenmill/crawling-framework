package lt.tokenmill.crawling.parser;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.net.URL;

public abstract class BaseArticleExtractorTest {

    protected String loadArticle(String name) throws Exception {
        URL htmlResource = Resources.getResource("articles/" + name + ".html");
        return Resources.toString(htmlResource, Charsets.UTF_8);
    }
}
