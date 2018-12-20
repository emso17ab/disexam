package cache;

import controllers.OrderController;
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

    private Boolean requireUpdate() {
        return ((this.created + this.ttl) < (System.currentTimeMillis() / 1000L) || this.orders.isEmpty());
    }

    public ArrayList<Order> getOrders(Boolean forceUpdate) {
        if (requireUpdate() || forceUpdate) {
            updateCache();
        }
        return this.orders;
    }

    private void updateCache() {
        System.out.println("cache is now updating...");
        this.orders = OrderController.getOrders();
        this.created = System.currentTimeMillis() / 1000L;
    }

}
