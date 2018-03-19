package lt.tokenmill.crawling.parser;

import lt.tokenmill.crawling.data.DataUtils;
import lt.tokenmill.crawling.data.HttpSource;
import lt.tokenmill.crawling.parser.data.MatchedDate;
import org.joda.time.DateTime;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateParserTest {

    @Test
    public void formats() {
        MatchedDate matchedDate = new MatchedDate("2017-01-16 01:45:05 -0500", null);
        MatchedDate parse = DateParser.parse(matchedDate, new HttpSource());
        assertEquals("2017-01-16T06:45:05", DataUtils.formatInUTC(parse.getDate()));

        matchedDate = new MatchedDate("Jan 17, 2017 06:08AM ET", null);
        parse = DateParser.parse(matchedDate, new HttpSource());
        assertEquals("2017-01-17T11:08:00", DataUtils.formatInUTC(parse.getDate()));

        matchedDate = new MatchedDate("January 18, 2017", null);
        parse = DateParser.parse(matchedDate, new HttpSource());
        assertEquals("2017-01-18T00:00:00", DataUtils.formatInUTC(parse.getDate()));

        assertEquals("2017-01-18T13:45:00", parse("Wed Jan 18 13:45:00 GMT 2017"));

        assertEquals("2017-02-14T00:00:00", parse("02142017"));

        assertEquals("2017-02-27T00:00:00", parse("Monday, February 27, 2017 @ 05:02 PM gHale"));
        assertEquals("2017-03-09T00:00:00", parse("March 9, 2017 8:43 AM"));

        assertEquals("2017-03-13T15:49:00", parse("13 Mar, 2017 15:49"));
        assertEquals("2017-03-13T03:14:28", parse("2017-03-13 03:14:28"));
        assertEquals("2017-03-10T00:00:00", parse("10 March 2017"));
        assertEquals("2017-03-09T16:47:21", parse("2017-03-09 16:47:21"));
        assertEquals("2017-03-09T01:00:00", parse("2017-03-09T01:00:00"));
        assertEquals("2017-03-10T08:24:19", parse("Fri, 10 Mar 2017 13:54:19 +0530"));
        assertEquals("2016-08-24T06:42:00", parse("Wed Aug 24, 2016 2:42am EDT"));
        assertEquals("2017-03-10T12:51:00", parse("March 10, 2017, 14:51 IST"));
        assertEquals("2016-08-22T18:14:26", parse("Mon, 22 Aug 2016 18:14:26 +0000"));
        assertEquals("2017-04-13T00:00:00", parse("2017/04/13"));
    }

    private String parse(String text) {
        MatchedDate matchedDate = new MatchedDate(text, null);
        MatchedDate parse = DateParser.parse(matchedDate, new HttpSource());
        return DataUtils.formatInUTC(parse.getDate());
    }

    @Test
    public void parseWithTimewords() {
        MatchedDate matchedDate = new MatchedDate("Wed Feb 24 2016 00:01 UTC+1201", null);
        MatchedDate parse = DateParser.parse(matchedDate, new HttpSource());
        assertEquals("2016-02-24T00:01:00", DataUtils.formatInUTC(parse.getDate()));
        assertEquals("TIMEWORDS", parse.getPattern());
        assertEquals("Wed Feb 24 2016 00:01 UTC+1201", parse.getValue());
    }

    private Date simpleParse(String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        format.setLenient(false);
        Date parsed = format.parse(dateString);
        return parsed;
    }


    private void testDateWithPattern(String dateString, String dateFormat, String expectedDateString) throws ParseException {
        // Try simple parse
        Date simpleParsed = simpleParse(dateString, dateFormat);
        assertNotNull(simpleParsed);
        assertEquals(expectedDateString, DataUtils.formatInUTC(new DateTime(simpleParsed)));

        // Try parsing machinery
        HttpSource httpSource = new HttpSource();
        httpSource.setDateFormats(Arrays.asList(dateFormat));

        MatchedDate matchedDate = new MatchedDate(dateString, null);
        MatchedDate parse = DateParser.parse(matchedDate, httpSource);
        assertNotNull(parse.getDate());
        assertEquals(expectedDateString, DataUtils.formatInUTC(matchedDate.getDate()));

        // Compare if parsing results are the same.
        assertEquals(DataUtils.formatInUTC(new DateTime(simpleParsed)), DataUtils.formatInUTC(parse.getDate()));
    }

    @Test
    public void testHttpSourceDateFormats000() throws ParseException {
        // Input data
        String dateFormat = "'on' MMMM dd, yyyy '|'";
        String dateString = "on April 6, 2017 | News";
        String expectedDateString = "2017-04-06T00:00:00";
        testDateWithPattern(dateString, dateFormat, expectedDateString);
    }

    @Test
    public void testHttpSourceDateFormats001() throws ParseException {
        String dateFormat = "'Posted' MMMM dd, yyyy";
        String dateString = "Posted February 16, 2017";
        String expectedDateString = "2017-02-16T00:00:00";
        testDateWithPattern(dateString, dateFormat, expectedDateString);
    }

    @Test
    public void testHttpSourceDateFormats002() throws ParseException {
        String dateFormat = "'By Scott Simkin on' MMMM dd, yyyy 'Tweet'";
        String dateString = "By Scott Simkin on February 14, 2017 Tweet";
        String expectedDateString = "2017-02-14T00:00:00";
        testDateWithPattern(dateString, dateFormat, expectedDateString);
    }

    @Test
    public void testHttpSourceDateRegexpPattern000() throws ParseException {
        String dateRegexp = "By.*on (.*) Tweet";
        String dateString = "By Scott Simkin on February 14, 2017 Tweet";
        String expectedDateString = "2017-02-14T00:00:00";

        MatchedDate matchedDate = new MatchedDate(dateString, null);
        // Try machinery for parsing by regexp
        HttpSource httpSource = new HttpSource();
        httpSource.setDateRegexps(Arrays.asList(dateRegexp));

        MatchedDate parse = DateParser.parse(matchedDate, httpSource);
        assertNotNull(parse.getDate());
        assertEquals(expectedDateString, DataUtils.formatInUTC(matchedDate.getDate()));
    }

}