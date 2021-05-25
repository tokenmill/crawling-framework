package lt.tokenmill.crawling.cache.utils;

import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyUtils {
    public static boolean isWildCard(String key){
        return key.contains("*");
    }

    public static Set<String> resolveKeys(String key, Jedis jedis){
        if(isWildCard(key)){
            return jedis.keys(key);
        }
        else{
            return Arrays.asList(key).stream().collect(Collectors.toSet());
        }
    }
}
