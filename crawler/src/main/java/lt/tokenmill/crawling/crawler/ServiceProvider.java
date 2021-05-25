package lt.tokenmill.crawling.crawler;

import lt.tokenmill.crawling.cache.UrlProcessingCache;
import lt.tokenmill.crawling.es.EsDocumentOperations;
import lt.tokenmill.crawling.es.EsHttpSourceOperations;
import lt.tokenmill.crawling.es.EsHttpUrlOperations;

import java.util.Map;

/***
 * Interface for external service factory.
 */
public interface ServiceProvider {

    EsHttpUrlOperations createEsHttpUrlOperations(Map conf);

    EsHttpSourceOperations createEsHttpSourceOperations(Map conf);

    EsDocumentOperations creatEsDocumentOperations(Map conf);

    UrlProcessingCache createUrlProcessingCache(Map conf);
}
