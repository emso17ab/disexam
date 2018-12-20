package cache;

import controllers.ProductController;
import controllers.UserController;
import model.Product;
import model.User;
import utils.Config;
import java.util.ArrayList;

// TODO FIX: Build this cache and use it.
public class UserCache {

    // List of users
    private static ArrayList<User> users;

    // Time cache should live
    private static long ttl;

    // Sets when the cache has been created
    private static long created;

    public UserCache() {
        ttl = Config.getCacheTtl();
        created = System.currentTimeMillis() / 1000L;
        users = new ArrayList<>();
        System.out.println("Usercache was created");
    }

    private static Boolean requireUpdate() {
        return ((created + ttl) < (System.currentTimeMillis() / 1000L) || users.isEmpty());
    }

    public static ArrayList<User> getUsers(Boolean forceUpdate) {
        if (requireUpdate() || forceUpdate) {
            updateCache();
        }
        System.out.println("user-cache was used");
        System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
        return users;
    }

    public static User getUser(int userId) {
        User user = null;

        if (users.isEmpty()) {
            return UserController.getUser(userId);
        }

        for (User u : users) {
            if (u.getId() == userId){
                user = u;
            }
        }
        return user;
    }

    private static void updateCache() {
        System.out.println("user-cache is now updating...");
        users = UserController.getUsers();
        created = System.currentTimeMillis() / 1000L;
        ttl = Config.getCacheTtl();
    }

}
