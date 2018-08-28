package lt.tokenmill.crawling.data;

import java.io.Serializable;
import java.util.List;

public class PageableList<T> implements Serializable {

    private long totalCount;

    private List<T> items;

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public static <V> PageableList<V> create(List<V> items, long totalCount) {
        PageableList<V> pageableList = new PageableList<>();
        pageableList.setItems(items);
        pageableList.setTotalCount(totalCount);
        return pageableList;
    }
}