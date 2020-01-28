package lt.tokenmill.crawling.es;

import org.elasticsearch.client.RequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.UUID;

public class BaseElasticOps {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final RequestOptions requestOptions;
    private ElasticConnection connection;
    private String index;
    private String type;

    protected BaseElasticOps(ElasticConnection connection, String index, String type) {
        this.connection = connection;
        this.index = index;
        this.type = type;
        requestOptions = RequestOptions.DEFAULT;
    }

    protected ElasticConnection getConnection() {
        return connection;
    }

    protected String getIndex() {
        return index;
    }

    protected String getType() {
        return type;
    }

    protected RequestOptions getRequestOptions() { return requestOptions; }

    public void close() {
        if (connection != null) {
            connection.close();
        }
    }

    protected static String formatId(String url) {
        try {
            String urlId = URLEncoder.encode(url.toLowerCase(), "utf-8");
            if (urlId.length() > 511) {
                urlId = urlId.substring(0, 511);
            }
            return urlId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UUID.randomUUID().toString();
    }
}
