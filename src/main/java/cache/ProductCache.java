package cache;

import model.Product;
import utils.Config;
import java.util.ArrayList;

public class ProductCache {

  // List of products
  private ArrayList<Product> products;

  // Time cache should live
  private long ttl;

  // Sets when the cache has been created
  private long created;

  public ProductCache() {
    this.ttl = Config.getProductTtl();
    this.created = System.currentTimeMillis() / 1000L;
    this.products = new ArrayList<>();
  }

  public Boolean requireUpdate() {
    return ((this.created + this.ttl) < (System.currentTimeMillis() / 1000L) || this.products.isEmpty());
  }

  public ArrayList<Product> getProducts() {
    return this.products;
  }

  //Method to update cahce i.e. the ProductController can populate the cache with data from recent database call
  public void updateCache(ArrayList<Product> products) {
    this.products = products;
    this.created = System.currentTimeMillis() / 1000L;
  }
}
