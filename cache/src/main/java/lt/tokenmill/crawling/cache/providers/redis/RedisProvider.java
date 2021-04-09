package lt.tokenmill.crawling.cache.providers.redis;

import lt.tokenmill.crawling.cache.datamodel.Pair;
import lt.tokenmill.crawling.cache.providers.CacheProvider;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisProvider implements CacheProvider {
    private final RedisResourceProvider resourceProvider;
    private final RedisKVProvider kvProvider;
    private final RedisSetProvider setProvider;

    public static Builder builder(){
        return new Builder();
    }

    protected RedisProvider(JedisPool pool){
        this.resourceProvider = new RedisResourceProvider(pool);
        this.kvProvider = new RedisKVProvider(resourceProvider);
        this.setProvider = new RedisSetProvider(resourceProvider);
    }

    @Override
    public <T> CompletableFuture<Boolean> set(String namespace, String key, T value) {
        return kvProvider.set(namespace, key, value);
    }

    @Override
    public <T> CompletableFuture<Boolean> setMultiple(String namespace, Set<Pair<String, T>> pairs) {
        return kvProvider.setMultiple(namespace, pairs);
    }


    @Override
    public <T> CompletableFuture<T> get(Class<T> klass, String namespace, String key) {
        return kvProvider.get(klass, namespace, key);
    }

    @Override
    public <T> CompletableFuture<Set<T>> getMultiple(Class<T> klass, String namespace, String... keys) {
        return getMultiple(klass, namespace, Arrays.asList(keys));
    }

    @Override
    public <T> CompletableFuture<Set<T>> getMultiple(Class<T> klass, String namespace, Collection<String> initialKeys) {
        return kvProvider.getMultiple(klass, namespace, initialKeys.stream().collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Boolean> addKey(String namespace, String key) {
        return setProvider.addKey(namespace, key);
    }

    @Override
    public CompletableFuture<Boolean> addKeyMultiple(String namespace, Collection<String> keys) {
        return setProvider.addKeys(namespace, keys);
    }

    @Override
    public CompletableFuture<Boolean> removeKey(String namespace, String key) {
        return setProvider.removeKey(namespace, key);
    }

    @Override
    public CompletableFuture<Boolean> moveKey(String source, String dest, String key) {
        return setProvider.moveKey(source, dest, key);
    }

    @Override
    public CompletableFuture<String> findKey(String namespace, String key) {
        return setProvider.findKey(namespace, key);
    }

    @Override
    public CompletableFuture<Boolean> contains(String key) {
        return resourceProvider.withJedis(redis -> redis.exists(key));
    }

    @Override
    public CompletableFuture<Boolean> contains(String namespace, String key) {
        return kvProvider.contains(namespace, key);
    }

    @Override
    public CompletableFuture<Set<String>> keysInNamespace(String namespace) {
        return setProvider.keysInNamespace(namespace);
    }

    @Override
    public void cleanup() {
        resourceProvider.withJedis(redis -> redis.flushAll());
    }
}
