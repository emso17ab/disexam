package cache;

import controllers.ProductController;
import controllers.UserController;
import model.User;
import utils.Config;
import java.util.ArrayList;

// TODO FIX: Build this cache and use it.
public class UserCache {

    // List of users
    private ArrayList<User> users;

    // Time cache should live
    private long ttl;

    // Sets when the cache has been created
    private long created;

    public UserCache() {
        this.ttl = Config.getCacheTtl();
        this.created = System.currentTimeMillis() / 1000L;
        this.users = new ArrayList<>();
    }

    private Boolean requireUpdate() {
        return ((this.created + this.ttl) < (System.currentTimeMillis() / 1000L) || this.users.isEmpty());
    }

    public ArrayList<User> getUsers(Boolean forceUpdate) {
        if (requireUpdate() || forceUpdate) {
            updateCache();
        }
        return this.users;
    }

    private void updateCache() {
        System.out.println("cache is now updating...");
        this.users = UserController.getUsers();
        this.created = System.currentTimeMillis() / 1000L;
    }

}
