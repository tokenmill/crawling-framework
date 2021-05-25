package lt.tokenmill.crawling.cache.providers.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.tokenmill.crawling.cache.datamodel.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Pipeline;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisKVProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RedisKVProvider.class);
    private final RedisResourceProvider resource;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public RedisKVProvider(RedisResourceProvider resourceProvider){
        this.resource = resourceProvider;
    }

    public CompletableFuture<Boolean> contains(String namespace, String key) {
        return resource.withJedis(redis -> redis.hexists(namespace, key));
    }

    public <T> CompletableFuture<Boolean> set(String namespace, String key, T value) {
        return resource.withJedis(redis -> {
            try {
                redis.hset(namespace, key, objectMapper.writeValueAsString(value));
                return true;
            }
            catch (Exception ex){
                LOG.error("Failed to set value", ex);
                return false;
            }
        });
    }

    public <T> CompletableFuture<Boolean> setMultiple(String namespace, Set<Pair<String, T>> pairs){
        return resource.withJedis(redis -> {
            Pipeline pipe = redis.pipelined();
            pipe.multi();
            for(Pair<String, T> pair : pairs){
                try {
                    pipe.hset(namespace, pair.getKey(), objectMapper.writeValueAsString(pair.getValue()));
                }
                catch (Exception ex){
                    LOG.error("Failed to set value", ex);
                }
            }

            pipe.sync();
            pipe.exec();
            return true;
        });
    }

    public <T> CompletableFuture<T> get(Class<T> klass, String namespace, String key) {
        return resource.withJedis(redis -> {
            String data = redis.hget(namespace, key);
            return parseObj(data, klass);
        });
    }

    private<T> T parseObj(String data, Class<T> klass){
        try {
            return objectMapper.readValue(data, klass);
        }
        catch(Exception ex){
            return null;
        }
    }

    public <T> CompletableFuture<Set<T>> getMultiple(Class<T> klass, String namespace, Set<String> keys) {
        return resource
                .withPipeline(pipe -> keys.stream().map(k -> pipe.hget(namespace, k)))
                .thenApply(responses -> {
                    return responses
                            .map(data -> parseObj(data, klass))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                });
    }
}
