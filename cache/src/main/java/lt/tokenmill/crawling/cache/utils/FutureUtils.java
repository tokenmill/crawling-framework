package lt.tokenmill.crawling.cache.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class FutureUtils {
    public static<T> T tryGet(Future<T> future, Callable<T> onFail){
        try{
            T result = future.get();
            if(result != null){
                return result;
            }
            else {
                return onFail.call();
            }
        } catch (Exception ex){
            ex.printStackTrace();
            try {
                return onFail.call();
            } catch (Exception ex2){
                ex2.printStackTrace();
                return null;
            }
        }
    }

    public static<T> CompletableFuture<T> transformFuture(Future<T> future){
        CompletableFuture<T> newFuture = new CompletableFuture<>();
        try{
            newFuture.complete(future.get());
        } catch(Exception ex){
            newFuture.completeExceptionally(ex);
        }
        return newFuture;
    }

    public static<T> boolean waitFor(Future<T> future){
        try{
            future.get();
            return true;
        } catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }
}