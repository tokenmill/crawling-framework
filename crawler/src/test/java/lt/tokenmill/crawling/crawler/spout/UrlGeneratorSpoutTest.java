package lt.tokenmill.crawling.crawler.spout;

import lt.tokenmill.crawling.crawler.DefaultServiceProvider;
import org.junit.Test;

public class UrlGeneratorSpoutTest {


    @Test
    public void test() {
        UrlGeneratorSpout spout = new UrlGeneratorSpout(new DefaultServiceProvider());
    }

}
