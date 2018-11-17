package cache;

import model.User;
import utils.Config;
import java.util.ArrayList;

// TODO FIX: Build this cache and use it.
public class UserCache {

    // List of products
    private ArrayList<User> users;

    // Time cache should live
    private long ttl;

    // Sets when the cache has been created
    private long created;

    public UserCache() {
        this.ttl = Config.getProductTtl();
        this.created = System.currentTimeMillis() / 1000L;
        this.users = new ArrayList<>();
    }

    public Boolean requireUpdate() {
        return ((this.created + this.ttl) < (System.currentTimeMillis() / 1000L) || this.users.isEmpty());
    }

    public ArrayList<User> getUsers() {
        return this.users;
    }

    //Method to update cahce i.e. the UserController can populate the cache with data from recent database call
    public void updateCache(ArrayList<User> users) {
        this.users = users;
        this.created = System.currentTimeMillis() / 1000L;
    }

}
