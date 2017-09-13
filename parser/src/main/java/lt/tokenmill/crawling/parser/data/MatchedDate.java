package lt.tokenmill.crawling.parser.data;

import org.joda.time.DateTime;

import java.util.Objects;

public class MatchedDate {

    private String value;

    private String match;

    private String pattern;

    private DateTime date;

    public MatchedDate(String value, String match) {
        this.value = value;
        this.match = match;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "MatchedDate{" +
                "value='" + value + '\'' +
                ", match='" + match + '\'' +
                ", pattern='" + pattern + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchedDate that = (MatchedDate) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
