package cache;

import model.Order;
import utils.Config;
import java.util.ArrayList;

//TODO FIX: Build this cache and use it.
public class OrderCache {

    // List of products
    private ArrayList<Order> orders;

    // Time cache should live
    private long ttl;

    // Sets when the cache has been created
    private long created;

    public OrderCache() {
        this.ttl = Config.getCacheTtl();
        this.created = System.currentTimeMillis() / 1000L;
        this.orders = new ArrayList<>();
    }

    public Boolean requireUpdate() {
        return ((this.created + this.ttl) < (System.currentTimeMillis() / 1000L) || this.orders.isEmpty());
    }

    public ArrayList<Order> getOrders() {
        return this.orders;
    }

    //Method to update cahce i.e. the OrderController can populate the cache with data from recent database call
    public void updateCache(ArrayList<Order> orders) {
        this.orders = orders;
        this.created = System.currentTimeMillis() / 1000L;
    }

}
