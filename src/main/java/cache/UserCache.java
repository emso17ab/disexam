package cache;

import controllers.UserController;
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
    }

    //Determine whether the cache needs updating
    private static Boolean requireUpdate() {
        return ((created + ttl) < (System.currentTimeMillis() / 1000L) || users.isEmpty());
    }

    //Cache updating happens here
    private static void updateCache() {
        System.out.println("user-cache is now updating...");
        users = UserController.getUsers();
        created = System.currentTimeMillis() / 1000L;
    }

    //User endpoint calls this method. Cache is updated if required/forced.
    public static ArrayList<User> getUsers(Boolean forceUpdate) {
        if (requireUpdate() || forceUpdate) {
            updateCache();
        }
        //Console printout for testing purposes
        System.out.println("user-cache was used");
        System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
        return users;
    }

    //User endpoint calls this method. Cache is not updated and only used if exist.
    public static User getUser(int userId) {
        if (users == null || users.isEmpty()) {
            return UserController.getUser(userId);
        }
        //Iterating through the cache
        for (User u : users) {
            if (u.getId() == userId){
                //Console printout for testing purposes
                System.out.println("user-cache was used");
                System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
                return u;
            }
        }
        //In case the user does not exist in the cache, we need to call the DB
        return UserController.getUser(userId);
    }

}
