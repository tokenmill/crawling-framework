package lt.tokenmill.crawling.cache.providers;

import lt.tokenmill.crawling.cache.datamodel.Pair;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CacheProvider {
    <T> CompletableFuture<Boolean> set(String namespace, String key, T value);
    <T> CompletableFuture<Boolean> setMultiple(String namespace, Set<Pair<String, T>> pairs);

    CompletableFuture<Boolean> addKey(String namespace, String key);
    CompletableFuture<Boolean> addKeyMultiple(String namespace, Collection<String> keys);
    CompletableFuture<Boolean> removeKey(String namespace, String key);
    CompletableFuture<Boolean> moveKey(String source, String dest, String key);
    CompletableFuture<String> findKey(String namespace, String key);

    <T> CompletableFuture<T> get(Class<T> klass, String namespace, String key);
    <T> CompletableFuture<Set<T>> getMultiple(Class<T> klass, String namespace, String... keys);
    <T> CompletableFuture<Set<T>> getMultiple(Class<T> klass, String namespace, Collection<String> keys);

    CompletableFuture<Boolean> contains(String key);
    CompletableFuture<Boolean> contains(String namespace, String key);

    CompletableFuture<Set<String>> keysInNamespace(String namespace);

    void cleanup();
}
