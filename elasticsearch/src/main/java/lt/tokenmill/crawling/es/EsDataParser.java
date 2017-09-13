package lt.tokenmill.crawling.es;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

class EsDataParser {

    private static final Logger LOG = LoggerFactory.getLogger(EsDataParser.class);


    private static final List<String> ES_DATE_TIME_FORMATS = Lists.newArrayList(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
    );

    static DateTime nullOrDate(Object object) {
        if (object != null) {
            DateTime result = null;
            for (String format : ES_DATE_TIME_FORMATS) {
                SimpleDateFormat formatter = new SimpleDateFormat(format);
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    result = new DateTime(formatter.parse(object.toString()));
                    break;
                } catch (ParseException ignored) {
                }
            }
            if (result == null) {
                LOG.error("Failed to parse date from '{}'", object);
            }
            return result;
        }
        return null;
    }

    static boolean falseOrBoolean(Object object) {
        return (object != null) && Boolean.parseBoolean(object.toString());
    }
}