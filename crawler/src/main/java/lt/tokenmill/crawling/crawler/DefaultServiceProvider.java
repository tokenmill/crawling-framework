package lt.tokenmill.crawling.crawler;

import com.digitalpebble.stormcrawler.util.ConfUtils;
import com.google.common.collect.Maps;
import lt.tokenmill.crawling.es.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

public class DefaultServiceProvider implements ServiceProvider, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultServiceProvider.class);

    private static final Map<String, ElasticConnection> ES_CONNECTIONS = Maps.newConcurrentMap();

    public static ElasticConnection getElasticConnection(Map conf) {
        String hostname = ConfUtils.getString(conf, ElasticConstants.ES_HOSTNAME_PARAM);
        int transportPort = ConfUtils.getInt(conf, ElasticConstants.ES_TRANSPORT_PORT_PARAM, 9300);
        if (ES_CONNECTIONS.containsKey(hostname)) {
            return ES_CONNECTIONS.get(hostname);
        } else {
            ElasticConnection elasticConnection = ElasticConnection.getConnection(hostname, transportPort);
            ES_CONNECTIONS.put(hostname, elasticConnection);
            return ES_CONNECTIONS.get(hostname);
        }
    }

    public EsHttpUrlOperations createEsHttpUrlOperations(Map conf) {
        ElasticConnection connection = getElasticConnection(conf);
        String urlsIndexName = ConfUtils.getString(conf, ElasticConstants.ES_URLS_INDEX_NAME_PARAM);
        String urlsDocumentType = ConfUtils.getString(conf, ElasticConstants.ES_URLS_DOC_TYPE_PARAM);
        return EsHttpUrlOperations.getInstance(connection, urlsIndexName, urlsDocumentType);
    }

    public EsHttpSourceOperations createEsHttpSourceOperations(Map conf) {
        ElasticConnection connection = getElasticConnection(conf);
        String sourcesIndexName = ConfUtils.getString(conf, ElasticConstants.ES_HTTP_SOURCES_INDEX_NAME_PARAM);
        String sourcesDocumentType = ConfUtils.getString(conf, ElasticConstants.ES_HTTP_SOURCES_DOC_TYPE_PARAM);
        return EsHttpSourceOperations.getInstance(connection, sourcesIndexName, sourcesDocumentType);
    }

    public EsDocumentOperations creatEsDocumentOperations(Map conf) {
        ElasticConnection connection = getElasticConnection(conf);
        String docsIndexName = ConfUtils.getString(conf, ElasticConstants.ES_DOCS_INDEX_NAME_PARAM);
        String docsDocumentType = ConfUtils.getString(conf, ElasticConstants.ES_DOCS_DOC_TYPE_PARAM);
        return EsDocumentOperations.getInstance(connection, docsIndexName, docsDocumentType);
    }

}
