package lt.tokenmill.crawling.cache.datamodel;

public class Pair<T, U> {
    private final T key;
    private final U value;
    public Pair(T key, U value){
        this.key = key;
        this.value = value;
    }

    public T getKey(){
        return key;
    }

    public U getValue(){
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Pair){
            Pair<T, U> otherPair = (Pair<T, U>)other;

            return key.equals(otherPair.getKey()) && value.equals(otherPair.getValue());
        }

        return false;
    }
}
