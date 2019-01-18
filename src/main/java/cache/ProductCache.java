package cache;

import controllers.ProductController;
import model.Product;
import utils.Config;
import java.util.ArrayList;

public class ProductCache {

  // List of products
  private static ArrayList<Product> products;

  // Time cache should live
  private static long ttl;

  // Sets when the cache has been created
  private static long created;

  public ProductCache() {
    ttl = Config.getCacheTtl();
  }

  //Determine whether the cache needs updating
  private static Boolean requireUpdate() {
    return ((created + ttl) < (System.currentTimeMillis() / 1000L) || products.isEmpty());
  }

  //Cache updating happens here
  private static void updateCache() {
    System.out.println("product-cache is now updating...");
    products = ProductController.getProducts();
    created = System.currentTimeMillis() / 1000L;
  }

  //Product endpoint calls this method. Cache is updated if required/forced.
  public static ArrayList<Product> getProducts(Boolean forceUpdate) {
    if (requireUpdate() || forceUpdate) {
      updateCache();
    }
    //Console printout for testing purposes
    System.out.println("product-cache was used");
    System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
    return products;
  }

  //Product endpoint calls this method. Cache is not updated and only used if exist.
  public static Product getProduct(int productId) {
    if (products.isEmpty()) {
      return ProductController.getProduct(productId);
    }
    //Iterating through the cache
    for (Product p : products) {
      if (p.getId() == productId){
        //Console printout for testing purposes
        System.out.println("product-cache was used");
        System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
        return p;
      }
    }
    //In case the product does not exist in the cache, we need to call the DB
    return ProductController.getProduct(productId);
  }

}
