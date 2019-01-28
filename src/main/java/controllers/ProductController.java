package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import datasource.DbService;
import model.Product;
import utils.Log;

public class ProductController {

  public ProductController() {
  }

  public static Product getProduct(int id) {
    return DbService.getProduct(id);
  }

  public static ArrayList<Product> getProducts() {
    // TODO FIX: Use caching layer.
    return DbService.getProducts();
  }

  public static Product createProduct(Product product) {

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), product, "Actually creating a product in DB", 0);

    // Set creation time for product.
    product.setCreatedTime(System.currentTimeMillis() / 1000L);

    String sql =
            "INSERT INTO product(product_name, sku, price, description, stock, created_at) VALUES('"
            + product.getName()
            + "', '"
            + product.getSku()
            + "', "
            + product.getPrice()
            + ", '"
            + product.getDescription()
            + "', "
            + product.getStock()
            + ", "
            + product.getCreatedTime()
            + ")";

    // Insert the product in the DB
    int productID = DbService.createObject(sql);

    if (productID != 0) {
      //Update the productId of the product before returning
      product.setId(productID);
    } else{
      // Return null if product has not been inserted into database
      return null;
    }

    // Return product
    return product;
  }
}
