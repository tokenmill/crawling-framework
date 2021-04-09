package lt.tokenmill.crawling.cache.providers.redis;

import lt.tokenmill.crawling.cache.datamodel.Pair;
import lt.tokenmill.crawling.cache.utils.FutureUtils;
import lt.tokenmill.crawling.cache.utils.KeyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.ScanResult;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RedisSetProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RedisKVProvider.class);
    private final RedisResourceProvider resource;

    public RedisSetProvider(RedisResourceProvider resourceProvider){
        this.resource = resourceProvider;
    }

    public CompletableFuture<Boolean> addKey(String namespace, String key) {
        return addKeys(namespace, Arrays.asList(key));
    }

    public CompletableFuture<Boolean> addKeys(String namespace, Collection<String> keys){
        return resource.withJedis(redis -> {
            Pipeline pipe = redis.pipelined();

            pipe.multi();
            for(String key : keys){
                pipe.sadd(namespace, key);
            }

            pipe.exec();
            pipe.sync();
            return true;
        });
    }

    public CompletableFuture<Boolean> removeKey(String namespace, String key) {
        return resource.withJedis(redis -> {
            Set<String> keys = KeyUtils.resolveKeys(namespace, redis);

            Pipeline pipe = redis.pipelined();
            pipe.multi();
            List<Response<Long>> responses = keys
                    .stream()
                    .map(k -> pipe.srem(k, key))
                    .collect(Collectors.toList());

            pipe.exec();
            pipe.sync();

            responses.forEach(response -> response.get());
            return true;
        });
    }

    public CompletableFuture<Boolean> moveKey(String source, String dest, String key){
        return resource.withJedis(redis -> redis.smove(source, dest, key) == 1);
    }

    public CompletableFuture<String> findKey(String namespace, String key){
        return resource.withJedis(redis -> {
            Set<String> keys = KeyUtils.resolveKeys(namespace, redis);

            Pipeline pipe = redis.pipelined();
            pipe.multi();
            List<Pair<String, Response<Boolean>>> responses = keys
                    .stream()
                    .map(k -> new Pair<String, Response<Boolean>>(k, pipe.sismember(k, key)))
                    .collect(Collectors.toList());

            pipe.exec();
            pipe.sync();

            Optional<String> result = responses
                    .stream()
                    .filter(pair -> pair.getValue().get())
                    .map(pair -> pair.getKey())
                    .findFirst();

            if(result.isPresent()){
                return result.get();
            }
            else{
                return "-1";
            }
        });
    }

    public CompletableFuture<Set<String>> keysInNamespace(String namespace) {
        Set<String> keys = FutureUtils.tryGet(resource.withJedis(redis -> KeyUtils.resolveKeys(namespace, redis)), () -> Collections.emptySet());
        if(keys.size() == 0){
            return CompletableFuture.completedFuture(Collections.emptySet());
        }

        return resource
                .withPipeline(pipe -> keys.stream().map(k -> pipe.smembers(k)))
                .thenApply(results -> results.flatMap(Collection::stream).collect(Collectors.toSet()));
    }

    private Long countKeysInNamespaces(Collection<String> namespaces, Jedis redis){
        Pipeline pipe = redis.pipelined();
        pipe.multi();
        List<Response<Long>> responses = namespaces.stream().map(k -> pipe.scard(k)).collect(Collectors.toList());

        pipe.exec();
        pipe.sync();

        return responses
                .stream()
                .map(response -> response.get())
                .filter(Objects::nonNull)
                .reduce(0L, Long::sum);
    }

    public CompletableFuture<Long> countKeysInNamespace(String namespace) {
        return resource.withJedis(redis -> {
            Set<String> keys = redis.keys(namespace);
            if(keys.size() == 0){
                return 0L;
            }

            return countKeysInNamespaces(keys, redis);
        });
    }

    public CompletableFuture<Long> countKeysInNamespaces(Collection<String> namespaces) {
        return resource.withJedis(redis -> countKeysInNamespaces(namespaces, redis));
    }

    public CompletableFuture<Boolean> removeSet(String namespace) {
        return resource.withJedis(redis -> {
            String cursor = "0";
            while(true){
                ScanResult<String> result = redis.sscan(namespace, cursor);
                cursor = result.getCursor();
                List<String> members = result.getResult();
                redis.srem(namespace, members.toArray(new String[0]));

                if(cursor.equalsIgnoreCase("0")){
                    break;
                }
            }

            return true;
        });
    }
}
