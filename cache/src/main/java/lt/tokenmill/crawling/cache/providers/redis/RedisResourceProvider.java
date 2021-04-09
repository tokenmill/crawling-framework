package lt.tokenmill.crawling.cache.providers.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RedisResourceProvider {
    private final JedisPool jedisPool;
    private static final Logger LOG = LoggerFactory.getLogger(RedisResourceProvider.class);
    private final static int MAX_RETRIES = 5;
    private final static long RETRY_TIMEOUT = 300;


    protected RedisResourceProvider(JedisPool pool){
        this.jedisPool = pool;
    }

    public<T> CompletableFuture<T> withJedis(Function<Jedis, ? extends T> body){
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            int retryCount = 0;

            while(true) {
                try (Jedis jedis = jedisPool.getResource()) {
                    T result = body.apply(jedis);
                    if(result != null){
                        return result;
                    }
                    else {
                        return null;
                    }
                } catch (JedisConnectionException cex){
                    retryCount++;
                    if(retryCount > MAX_RETRIES) {
                        throw cex;
                    }
                    else {
                        LOG.warn("Redis operation has failed due to connection issue. Retrying in {}ms", RETRY_TIMEOUT * retryCount);
                        try {
                            Thread.sleep(RETRY_TIMEOUT);
                        } catch (InterruptedException iex) {
                            continue;
                        }
                    }

                } catch (Exception ex) {
                    throw ex;
                }
            }
        });

        return future;
    }

    public<T> CompletableFuture<Stream<T>> withPipeline(Function<Pipeline, Stream<Response<T>>> body){
        return withJedis(redis -> {
            Pipeline pipe = redis.pipelined();
            pipe.multi();
            List<Response<T>> results = body.apply(pipe).collect(Collectors.toList());

            Response<List<Object>> resultsResponse = pipe.exec();
            pipe.sync();

            resultsResponse.get();
            return results.stream().map(r -> r.get());
        });
    }
}
