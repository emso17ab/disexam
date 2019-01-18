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
    }

    //Determine whether the cache needs updating
    private static Boolean requireUpdate() {
        return ((created + ttl) < (System.currentTimeMillis() / 1000L) || orders.isEmpty());
    }

    //Cache updating happens here
    private static void updateCache() {
        System.out.println("order-cache is now updating...");
        orders = OrderController.getOrders();
        created = System.currentTimeMillis() / 1000L;
    }

    //Order endpoint calls this method. Cache is updated if required/forced.
    public static ArrayList<Order> getOrders(Boolean forceUpdate) {
        if (requireUpdate() || forceUpdate) {
            updateCache();
        }
        //Console printout for testing purposes
        System.out.println("order-cache was used");
        System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
        return orders;
    }

    //Order endpoint calls this method. Cache is not updated and only used if exist.
    public static Order getOrder(int orderId) {
        if (orders.isEmpty()) {
            return OrderController.getOrder(orderId);
        }
        //Iterating through the cache
        for (Order o : orders) {
            if (o.getId() == orderId){
                //Console printout for testing purposes
                System.out.println("order-cache was used");
                System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
                return o;
            }
        }
        //In case the product does not exist in the cache, we need to call the DB
        return OrderController.getOrder(orderId);
    }

}
