package lt.tokenmill.crawling.parser.data;

import java.util.Objects;

public class MatchedString {

    private String value;

    private String match;

    public MatchedString(String value, String match) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchedString that = (MatchedString) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
