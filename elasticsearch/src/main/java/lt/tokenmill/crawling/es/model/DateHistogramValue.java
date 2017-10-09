package lt.tokenmill.crawling.es.model;

public class DateHistogramValue {

    private Long value;

    private String date;

    public DateHistogramValue(String date, Long value) {
        this.value = value;
        this.date = date;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
