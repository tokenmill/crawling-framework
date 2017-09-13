package lt.tokenmill.crawling.data;

public class NamedQuery {

    private String name;

    private String stemmedCaseSensitive;
    private String stemmedCaseInSensitive;
    private String notStemmedCaseSensitive;
    private String notStemmedCaseInSensitive;
    private String advanced;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStemmedCaseSensitive() {
        return stemmedCaseSensitive;
    }

    public void setStemmedCaseSensitive(String stemmedCaseSensitive) {
        this.stemmedCaseSensitive = stemmedCaseSensitive;
    }

    public String getStemmedCaseInSensitive() {
        return stemmedCaseInSensitive;
    }

    public void setStemmedCaseInSensitive(String stemmedCaseInSensitive) {
        this.stemmedCaseInSensitive = stemmedCaseInSensitive;
    }

    public String getNotStemmedCaseSensitive() {
        return notStemmedCaseSensitive;
    }

    public void setNotStemmedCaseSensitive(String notStemmedCaseSensitive) {
        this.notStemmedCaseSensitive = notStemmedCaseSensitive;
    }

    public String getNotStemmedCaseInSensitive() {
        return notStemmedCaseInSensitive;
    }

    public void setNotStemmedCaseInSensitive(String notStemmedCaseInSensitive) {
        this.notStemmedCaseInSensitive = notStemmedCaseInSensitive;
    }

    public String getAdvanced() {
        return advanced;
    }

    public void setAdvanced(String advanced) {
        this.advanced = advanced;
    }
}
