package lt.tokenmill.crawling.commonui;

import lt.tokenmill.crawling.es.*;

public class ElasticSearch {

    private static ElasticConnection CONNECTION;
    private static EsHttpSourceOperations HTTP_SOURCE_OPERATIONS;
    private static EsHttpSourceTestOperations HTTP_SOURCE_TEST_OPERATIONS;
    private static EsNamedQueryOperations NAMED_QUERY_OPERATIONS;
    private static EsDocumentOperations DOCUMENT_OPERATIONS;

    public static EsHttpSourceOperations getHttpSourceOperations() {
        if (HTTP_SOURCE_OPERATIONS == null) {
            String index = Configuration.INSTANCE.getString(ElasticConstants.ES_HTTP_SOURCES_INDEX_NAME_PARAM);
            String type = Configuration.INSTANCE.getString(ElasticConstants.ES_HTTP_SOURCES_DOC_TYPE_PARAM);
            HTTP_SOURCE_OPERATIONS = EsHttpSourceOperations.getInstance(getEsConnection(), index, type);
        }
        return HTTP_SOURCE_OPERATIONS;
    }

    public static EsHttpSourceTestOperations getHttpSourceTestOperations() {
        if (HTTP_SOURCE_TEST_OPERATIONS == null) {
            String index = Configuration.INSTANCE.getString(ElasticConstants.ES_HTTP_SOURCES_TEST_INDEX_NAME_PARAM);
            String type = Configuration.INSTANCE.getString(ElasticConstants.ES_HTTP_SOURCES_TEST_TYPE_PARAM);
            HTTP_SOURCE_TEST_OPERATIONS = EsHttpSourceTestOperations.getInstance(getEsConnection(), index, type);
        }
        return HTTP_SOURCE_TEST_OPERATIONS;
    }

    public static EsNamedQueryOperations getNamedQueryOperations() {
        if (NAMED_QUERY_OPERATIONS == null) {
            String index = Configuration.INSTANCE.getString(ElasticConstants.ES_NAMED_QUERIES_INDEX_PARAM);
            String type = Configuration.INSTANCE.getString(ElasticConstants.ES_NAMED_QUERIES_TYPE_PARAM);
            NAMED_QUERY_OPERATIONS = EsNamedQueryOperations.getInstance(getEsConnection(), index, type);
        }
        return NAMED_QUERY_OPERATIONS;
    }


    public static EsDocumentOperations getDocumentOperations() {
        if (DOCUMENT_OPERATIONS == null) {
            String index = Configuration.INSTANCE.getString(ElasticConstants.ES_DOCS_INDEX_NAME_PARAM);
            String type = Configuration.INSTANCE.getString(ElasticConstants.ES_DOCS_DOC_TYPE_PARAM);
            DOCUMENT_OPERATIONS = EsDocumentOperations.getInstance(getEsConnection(), index, type);
        }
        return DOCUMENT_OPERATIONS;
    }

    private static ElasticConnection getEsConnection() {
        if (CONNECTION == null) {
            String hostname = Configuration.INSTANCE.getString(ElasticConstants.ES_HOSTNAME_PARAM, "localhost");
            int transportPort = Configuration.INSTANCE.getInt(ElasticConstants.ES_TRANSPORT_PORT_PARAM, 9300);
            CONNECTION = ElasticConnection.getConnection(hostname, transportPort);
        }
        return CONNECTION;
    }
}
