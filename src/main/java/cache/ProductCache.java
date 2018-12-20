package cache;

import controllers.ProductController;
import model.Product;
import sun.awt.ConstrainableGraphics;
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
    created = System.currentTimeMillis() / 1000L;
    products = new ArrayList<>();
  }

  private static Boolean requireUpdate() {
    return ((created + ttl) < (System.currentTimeMillis() / 1000L) || products.isEmpty());
  }

  public static ArrayList<Product> getProducts(Boolean forceUpdate) {
    if (requireUpdate() || forceUpdate) {
      updateCache();
    }
    System.out.println("product-cache was used");
    System.out.println("Time to update, sec: " + (created+ttl-System.currentTimeMillis()/1000L));
    return products;
  }

  public static Product getProduct(int productId) {
    Product product = null;

    if (products.isEmpty()) {
      return ProductController.getProduct(productId);
    }

    for (Product p : products) {
      if (p.getId() == productId){
        product = p;
      }
    }
    return product;
  }

  private static void updateCache() {
    System.out.println("product-cache is now updating...");
    products = ProductController.getProducts();
    created = System.currentTimeMillis() / 1000L;
    ttl = Config.getCacheTtl();
  }
}
