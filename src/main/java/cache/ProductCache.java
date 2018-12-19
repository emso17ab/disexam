package cache;

import controllers.ProductController;
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
    this.ttl = Config.getCacheTtl();
    this.created = System.currentTimeMillis() / 1000L;
    this.products = new ArrayList<>();
  }

  private Boolean requireUpdate() {
    return ((this.created + this.ttl) < (System.currentTimeMillis() / 1000L) || this.products.isEmpty());
  }

  public ArrayList<Product> getProducts(Boolean forceUpdate) {
    if (requireUpdate() || forceUpdate) {
      updateCache();
    }
    return this.products;
  }

  public Product getProduct(int productId) {
    Product product = null;

    if (this.products.isEmpty()) {
      return ProductController.getProduct(productId);
    }

    for (Product p : this.products) {
      if (p.getId() == productId){
        product = p;
      }
    }
    return product;
  }


  //Method to update cahce i.e. the ProductController can populate the cache with data from recent database call
  private void updateCache() {
    System.out.println("cache is now updating...");
    this.products = ProductController.getProducts();
    this.created = System.currentTimeMillis() / 1000L;
  }
}
