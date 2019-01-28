package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import datasource.DbService;
import model.*;
import utils.Log;

public class OrderController {


  private static DbService dbCon;

  public OrderController() {
  }

  public static Order getOrder(int id) {

    //Preparing sql String for join query
    String sql = "SELECT orders.id AS 'order_id', orders.user_id, " +
            "user.first_name, user.last_name, user.password, user.email, user.salt, user.created_at AS 'user_created', " +
            "line_item.id AS 'line_item_id', line_item.price AS 'line_item_price', line_item.quantity, " +
            "product.id AS 'product_id', product.product_name, product.sku, product.price AS 'product_price', product.description, " +
            "product.stock, product.created_at AS 'product_created', orders.billing_address_id, billingAddress.name AS 'billingName', " +
            "billingAddress.city AS 'billingCity', billingAddress.zipcode AS 'billingZipcode', billingAddress.street_address AS 'billingStreet', " +
            "orders.shipping_address_id, shippingAddress.name AS 'shippingName', shippingAddress.city AS 'shippingCity', " +
            "shippingAddress.zipcode AS 'shippingZipcode', shippingAddress.street_address AS 'shippingStreet', " +
            "orders.order_total, orders.created_at AS 'order_created', orders.updated_at " +
            "FROM orders " +
            "LEFT JOIN line_item ON orders.id=line_item.order_id " +
            "LEFT JOIN user ON orders.user_id=user.id " +
            "LEFT JOIN product ON line_item.product_id=product.id " +
            "LEFT JOIN address AS billingAddress ON orders.billing_address_id=billingAddress.id " +
            "LEFT JOIN address AS shippingAddress ON orders.shipping_address_id=shippingAddress.id " +
            "WHERE orders.id=" + id + ";";

    //Executing statement,saving it in ResultSet
    ResultSet rs = dbCon.query(sql);

    //Preparing the order to be returned
    Order currentOrder = null;

    //Necessary variables/placeholders for loop through ResultSet to work
    ArrayList<LineItem> currentLineItems = null;
    int lineNo; //The current order_id value at current iteration through ResultSet
    int currentOrderId = 0; //The current order we are processing in the loop

    try {
      while (rs.next()){

        lineNo = rs.getInt("order_id");

        //If the lineNo is not the currentOrder, that means we have arrived at a new order
        if (lineNo != currentOrderId) {

          //Setting the current order we are processing from the new lineNo we arrived at in the loop
          currentOrderId = lineNo;

          //Resetting placeholders
          currentLineItems = new ArrayList<>();
          currentOrder = new Order();

          //Preparing new order entry because we are at a new order ID
          currentOrder.setId(currentOrderId);
          currentOrder.setOrderTotal(rs.getFloat("order_total"));
          currentOrder.setCreatedAt(rs.getInt("order_created"));
          currentOrder.setUpdatedAt(rs.getInt("updated_at"));

          //Adding the customer (User) to the order
          User user = new User(
                  rs.getInt("user_id"),
                  rs.getString("first_name"),
                  rs.getString("last_name"),
                  rs.getString("password"),
                  rs.getString("email"),
                  rs.getString("salt"));
          user.setCreatedTime(rs.getLong("user_created"));
          currentOrder.setCustomer(user);

          //Adding the addresses to the order
          currentOrder.setShippingAddress(new Address(
                  rs.getInt("shipping_address_id"),
                  rs.getString("shippingName"),
                  rs.getString("shippingStreet"),
                  rs.getString("shippingCity"),
                  rs.getString("shippingZipcode")
          ));
          currentOrder.setBillingAddress(new Address(
                  rs.getInt("billing_address_id"),
                  rs.getString("billingName"),
                  rs.getString("billingStreet"),
                  rs.getString("billingCity"),
                  rs.getString("billingZipcode")
          ));

          //Adding the product for the lineItem
          Product product = new Product(
                  rs.getInt("product_id"),
                  rs.getString("product_name"),
                  rs.getString("sku"),
                  rs.getFloat("product_price"),
                  rs.getString("description"),
                  rs.getInt("stock"));
          product.setCreatedTime(rs.getLong("product_created"));

          //Adding the first lineItem
          LineItem lineItem = new LineItem(
                  rs.getInt("line_item_id"),
                  product,
                  rs.getInt("quantity"),
                  rs.getFloat("line_item_price"));

          currentLineItems.add(lineItem);


          //If the lineNo == current order, that means that we have arrived at a new lineItem in the loop, but for the same order
        } else {

          //Adding the product for the next lineItem
          Product product = new Product(
                  rs.getInt("product_id"),
                  rs.getString("product_name"),
                  rs.getString("sku"),
                  rs.getFloat("product_price"),
                  rs.getString("description"),
                  rs.getInt("stock"));
          product.setCreatedTime(rs.getLong("product_created"));

          //Adding next lineItem to the existing order
          LineItem lineItem = new LineItem(
                  rs.getInt("line_item_id"),
                  product,
                  rs.getInt("quantity"),
                  rs.getFloat("line_item_price"));

          if (currentLineItems != null){
            currentLineItems.add(lineItem);
          }
        }
      }

      //Adding the lineItems after loop finished
      if (currentOrderId != 0) {
        currentOrder.setLineItems(currentLineItems);
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return currentOrder;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    //Preparing sql String for join query
    String sql = "SELECT orders.id AS 'order_id', orders.user_id, " +
            "user.first_name, user.last_name, user.password, user.email, user.salt, user.created_at AS 'user_created', " +
            "line_item.id AS 'line_item_id', line_item.price AS 'line_item_price', line_item.quantity, " +
            "product.id AS 'product_id', product.product_name, product.sku, product.price AS 'product_price', product.description, " +
            "product.stock, product.created_at AS 'product_created', orders.billing_address_id, billingAddress.name AS 'billingName', " +
            "billingAddress.city AS 'billingCity', billingAddress.zipcode AS 'billingZipcode', billingAddress.street_address AS 'billingStreet', " +
            "orders.shipping_address_id, shippingAddress.name AS 'shippingName', shippingAddress.city AS 'shippingCity', " +
            "shippingAddress.zipcode AS 'shippingZipcode', shippingAddress.street_address AS 'shippingStreet', " +
            "orders.order_total, orders.created_at AS 'order_created', orders.updated_at " +
            "FROM orders " +
            "LEFT JOIN line_item ON orders.id=line_item.order_id " +
            "LEFT JOIN user ON orders.user_id=user.id " +
            "LEFT JOIN product ON line_item.product_id=product.id " +
            "LEFT JOIN address AS billingAddress ON orders.billing_address_id=billingAddress.id " +
            "LEFT JOIN address AS shippingAddress ON orders.shipping_address_id=shippingAddress.id " +
            "ORDER BY orders.id ASC;";


    //Executing statement,saving it in ResultSet
    ResultSet rs = dbCon.query(sql);

    //Preparing the order array to be returned
    ArrayList<Order> orders = new ArrayList<>();

    //Necessary variables/placeholders for loop through ResultSet to work
    ArrayList<LineItem> currentLineItems = null;
    Order currentOrder = null;
    int lineNo; //The current order_id value at current iteration through ResultSet
    int currentOrderId = 0; //The current order we are processing in the loop

    try {
      while (rs.next()){

        lineNo = rs.getInt("order_id");

        //If the lineNo is not the currentOrder, that means we have arrived at a new order
        if (lineNo != currentOrderId) {

          //Adding previous order to list, unless this is the first line in the resultSet
          if (currentOrderId != 0) {
            currentOrder.setLineItems(currentLineItems);
            orders.add(currentOrder);
          }

          //Setting the current order we are processing from the new lineNo we arrived at in the loop
          currentOrderId = lineNo;

          //Resetting placeholders
          currentLineItems = new ArrayList<>();
          currentOrder = new Order();

          //Preparing new order entry because we are at a new order ID
          currentOrder.setId(currentOrderId);
          currentOrder.setOrderTotal(rs.getFloat("order_total"));
          currentOrder.setCreatedAt(rs.getInt("order_created"));
          currentOrder.setUpdatedAt(rs.getInt("updated_at"));

          //Adding the customer (User) to the order
          User user = new User(
                  rs.getInt("user_id"),
                  rs.getString("first_name"),
                  rs.getString("last_name"),
                  rs.getString("password"),
                  rs.getString("email"),
                  rs.getString("salt"));
          user.setCreatedTime(rs.getLong("user_created"));
          currentOrder.setCustomer(user);

          //Adding the addresses to the order
          currentOrder.setShippingAddress(new Address(
                  rs.getInt("shipping_address_id"),
                  rs.getString("shippingName"),
                  rs.getString("shippingStreet"),
                  rs.getString("shippingCity"),
                  rs.getString("shippingZipcode")
          ));
          currentOrder.setBillingAddress(new Address(
                  rs.getInt("billing_address_id"),
                  rs.getString("billingName"),
                  rs.getString("billingStreet"),
                  rs.getString("billingCity"),
                  rs.getString("billingZipcode")
          ));

          //Adding the product for the lineItem
          Product product = new Product(
                  rs.getInt("product_id"),
                  rs.getString("product_name"),
                  rs.getString("sku"),
                  rs.getFloat("product_price"),
                  rs.getString("description"),
                  rs.getInt("stock"));
          product.setCreatedTime(rs.getLong("product_created"));

          //Adding the first lineItem
          LineItem lineItem = new LineItem(
                  rs.getInt("line_item_id"),
                  product,
                  rs.getInt("quantity"),
                  rs.getFloat("line_item_price"));

          currentLineItems.add(lineItem);


          //If the lineNo == current order, that means that we have arrived at a new lineItem in the loop, but for the same order
        } else {

          //Adding the product for the next lineItem
          Product product = new Product(
                  rs.getInt("product_id"),
                  rs.getString("product_name"),
                  rs.getString("sku"),
                  rs.getFloat("product_price"),
                  rs.getString("description"),
                  rs.getInt("stock"));
          product.setCreatedTime(rs.getLong("product_created"));

          //Adding next lineItem to the existing order
          LineItem lineItem = new LineItem(
                  rs.getInt("line_item_id"),
                  product,
                  rs.getInt("quantity"),
                  rs.getFloat("line_item_price"));

          if (currentLineItems != null){
            currentLineItems.add(lineItem);
          }
        }
      }

      //Adding the last order after loop finished
      if (currentOrderId != 0) {
        currentOrder.setLineItems(currentLineItems);
        orders.add(currentOrder);
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return orders;
  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    //      FROM HERE THE TRANSACTION SHOULD START

    // Save addresses to database and save them back to initial order instance
    order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
    order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

    // Save the user to the database and save them back to initial order instance
    //But does this make sense? The user making the order would already be logged in and in the DB

    //order.setCustomer(UserController.createUser(order.getCustomer()));

    //Setting the active user to the order
    order.setCustomer(UserController.getActiveUser());

    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.

    // Insert the product in the DB
    int orderID = dbCon.insert(
        "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
            + order.getCustomer().getId()
            + ", "
            + order.getBillingAddress().getId()
            + ", "
            + order.getShippingAddress().getId()
            + ", "
            + order.calculateOrderTotal()
            + ", "
            + order.getCreatedAt()
            + ", "
            + order.getUpdatedAt()
            + ")");

    if (orderID != 0) {
      //Update the order id of the order before returning
      order.setId(orderID);
    }

    // Create an empty list in order to go trough items and then save them back with ID
    ArrayList<LineItem> items = new ArrayList<LineItem>();

    // Save line items to database
    for(LineItem item : order.getLineItems()){
      item = LineItemController.createLineItem(item, order.getId());
      items.add(item);
    }

    order.setLineItems(items);

    // Return order
    return order;
  }
}