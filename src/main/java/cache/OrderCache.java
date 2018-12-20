package cache;

import controllers.OrderController;
import model.Order;
import utils.Config;
import java.util.ArrayList;

//TODO FIX: Build this cache and use it.
public class OrderCache {

    // List of products
    private static ArrayList<Order> orders;

    // Time cache should live
    private static long ttl;

    // Sets when the cache has been created
    private static long created;

    private OrderCache() {
        ttl = Config.getCacheTtl();
        created = System.currentTimeMillis() / 1000L;
        orders = new ArrayList<>();
    }

    private static Boolean requireUpdate() {
        return ((created + ttl) < (System.currentTimeMillis() / 1000L) || orders.isEmpty());
    }

    public static ArrayList<Order> getOrders(Boolean forceUpdate) {
        if (requireUpdate() || forceUpdate) {
            updateCache();
        }

        System.out.println("order-cache was used");
        System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
        return orders;
    }

    private static void updateCache() {
        System.out.println("order-cache is now updating...");
        ttl = Config.getCacheTtl();
        orders = OrderController.getOrders();
        created = System.currentTimeMillis() / 1000L;
    }

}
