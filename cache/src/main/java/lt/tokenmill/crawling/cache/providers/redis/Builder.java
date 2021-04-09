package lt.tokenmill.crawling.cache.providers.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.Optional;

public class Builder{
    private String host = "localhost";
    private int port = 6379;
    private Optional<String> auth = Optional.empty();
    private int timeout = Protocol.DEFAULT_TIMEOUT;

    public Builder withHost(String host){
        this.host = host;
        return this;
    }

    public Builder withPort(int port){
        this.port = port;
        return this;
    }

    public Builder withAuth(String auth){
        if(auth != null) {
            this.auth = Optional.of(auth);
        }
        return this;
    }

    public Builder withTimeoutMillis(int millis){
        this.timeout = millis;
        return this;
    }

    public RedisProvider build(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        JedisPool pool;
        if(auth.isPresent()){
            pool = new JedisPool(config, host, port, timeout, auth.get());
        }
        else{
            pool = new JedisPool(config, host, port, timeout);
        }

        return new RedisProvider(pool);
    }

}
