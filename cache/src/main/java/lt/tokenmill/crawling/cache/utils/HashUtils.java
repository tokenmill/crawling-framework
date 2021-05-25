package lt.tokenmill.crawling.cache.utils;

import org.apache.commons.codec.digest.MurmurHash3;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HashUtils {
    public static String hashKey(String url){
        // MurmurHash64
        return String.valueOf(MurmurHash3.hash128x64(url.getBytes())[0]);
    }

    public static long hashKey(String... s){
        return MurmurHash3.hash128x64(Arrays.asList(s).stream().collect(Collectors.joining("#")).getBytes())[0];
    }

    public static long domainKey(String caseId, String domain){
        return hashKey(caseId, domain);
    }
}
