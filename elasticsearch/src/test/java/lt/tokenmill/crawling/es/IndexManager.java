package lt.tokenmill.crawling.es;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class IndexManager {

    private static final Logger LOG = LoggerFactory.getLogger(IndexManager.class);

    private static final String INDEX_VERSION_INFIX = "_v";

    private RestClient restClient;

    public IndexManager(RestClient restClient) {
        this.restClient = restClient;
    }

    public IndexManager(String hostname, int port) {
        this.restClient = RestClient.builder(
                new HttpHost(hostname, port, "http")
        ).build();
    }

    private int getIndexVersion(String indexName) {
        return Integer.parseInt(indexName.substring(indexName.lastIndexOf(INDEX_VERSION_INFIX) + 2));
    }

    private void deleteAlias(String indexName, String aliasName) throws IOException {
        restClient.performRequest("DELETE", indexName.concat("/_alias/").concat(aliasName));
    }

    private void addAlias(String indexName, String aliasName) throws IOException {
        restClient.performRequest("PUT", indexName.concat("/_aliases/").concat(aliasName));
    }

    public void prepare(String aliasName, String indexConf, boolean update) {
        try {
            if (update) {
                String indexName = findIndex(aliasName);
                if (indexName == null) {
                    // no index exists, create version 1
                    createIndex(aliasName, 1, indexConf);
                } else {
                    // get current index version
                    int version = getIndexVersion(indexName);
                    // create new index with version bump
                    createIndex(aliasName, version + 1, indexConf);
                    // delete of alias
                    deleteAlias(indexName, aliasName);
                }
            } else {
                String index = findIndex(aliasName);
                if (index == null) {
                    createIndex(aliasName, 1, indexConf);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Probably there is a better way to get the index name with the alias.
     * If there is no such index, than null is returned.
     * @param aliasName
     * @return
     */
    public String findIndex(String aliasName) {
        String indexName = null;
        try {
            Response response = this.restClient.performRequest("HEAD", aliasName);
            if (response.getStatusLine().getStatusCode() != 404) {
                // Index with aliasName exists, so we need to get real index name from settings
                response = this.restClient.performRequest("GET", aliasName.concat("/_settings"));
//                JSONObject json = new JSONObject(EntityUtils.toString(response.getEntity()));
//                indexName = json.keySet().stream().findFirst().orElse("");
            }
        } catch (IOException e) {
            LOG.debug("OK. No index with a given alias exists.");
        }
        return indexName;
    }

    /**
     * Creates an index and adds an alias to it.
     * @param aliasName
     * @param version
     * @param indexConf must be a valid ES config JSON.
     */
    public void createIndex(String aliasName, int version, String indexConf) {
        HttpEntity entity = new NStringEntity(indexConf, ContentType.APPLICATION_JSON);
        String indexName = aliasName + INDEX_VERSION_INFIX + version;
        try {
            restClient.performRequest("PUT", indexName, Collections.emptyMap(), entity);
            addAlias(indexName, aliasName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
