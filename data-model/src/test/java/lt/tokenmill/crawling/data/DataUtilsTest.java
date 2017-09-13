package lt.tokenmill.crawling.data;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataUtilsTest {

    @Test
    public void normalizerSplitter() {
        assertEquals(Lists.newArrayList("\\?.*$-->>", "a-->>b"),
                DataUtils.parseStringList("\\?.*$-->>\na-->>b\r\r\n\n"));
    }

    @Test
    public void dateFormatInUTC() {
        Long DATE_2017_01_04_12_26_00 = 1483532760805L;
        assertEquals("2017-01-04T12:26:00", DataUtils.formatInUTC(new DateTime(DATE_2017_01_04_12_26_00)));
    }
}
