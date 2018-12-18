package controllers;

import java.nio.file.AccessDeniedException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import model.*;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT * FROM orders where id=" + id;

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;

    try {
      if (rs.next()) {

        // Perhaps we could optimize things a bit here and get rid of nested queries.
        User user = UserController.getUser(rs.getInt("user_id"));
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        Address billingAddress = AddressController.getAddress(rs.getInt("billing_address_id"));
        Address shippingAddress = AddressController.getAddress(rs.getInt("shipping_address_id"));

        // Create an object instance of order from the database dataa
        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return order;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "SELECT orders.id AS 'order_id', orders.user_id, " +
            "user.first_name, user.last_name, user.password, user.email, user.salt, user.created_at AS 'user_created', " +
            "line_item.id AS 'line_item_id', line_item.product_id, line_item.price AS 'line_item_price', line_item.quantity, " +
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


    ResultSet rs = dbCon.query(sql);
    ArrayList<Order> orders = new ArrayList<>();

    //Necessary variables/placeholders
    int currentOrderId = 0;
    int lineNo;
    ArrayList<LineItem> currentLineItems = null;
    Order currentOrder = null;


    try {
      while (rs.next()){

        lineNo = rs.getInt("order_id");

        if (lineNo != currentOrderId) {

          //Adding previous order to list, unless this is the first line in the resultSet
          if (currentOrderId != 0) {
            currentOrder.setLineItems(currentLineItems);
            orders.add(currentOrder);
          }
          currentOrderId = lineNo;

          //Resetting placeholders
          currentLineItems = new ArrayList<>();
          currentOrder = new Order();

          //Preparing new order entry because we are at a new order ID
          currentOrder.setId(currentOrderId);
          currentOrder.setOrderTotal(rs.getFloat("order_total"));
          currentOrder.setCreatedAt(rs.getInt("order_created"));
          currentOrder.setUpdatedAt(rs.getInt("updated_at"));

          currentOrder.setCustomer(new User(
                  rs.getInt("user_id"),
                  rs.getString("first_name"),
                  rs.getString("last_name"),
                  rs.getString("password"),
                  rs.getString("email"),
                  rs.getString("salt"))
          );
          currentOrder.setShippingAddress(new Address(
                  rs.getInt("id"),
                  rs.getString("name"),
                  rs.getString("street_address"),
                  rs.getString("city"),
                  rs.getString("zipcode")
          ));
          currentOrder.setBillingAddress(new Address(
                  rs.getInt("id"),
                  rs.getString("name"),
                  rs.getString("street_address"),
                  rs.getString("city"),
                  rs.getString("zipcode")
          ));


          //Adding the first lineItem
          LineItem lineItem = new LineItem(
                  rs.getInt("id"),
                  new Product(
                          rs.getInt("id"),
                          rs.getString("product_name"),
                          rs.getString("sku"),
                          rs.getFloat("price"),
                          rs.getString("description"),
                          rs.getInt("stock")),
                  rs.getInt("quantity"),
                  rs.getFloat("price"));

          currentLineItems.add(lineItem);

        } else {

          //Adding next lineItem to the existing order
          LineItem lineItem = new LineItem(
                  rs.getInt("id"),
                  new Product(
                          rs.getInt("id"),
                          rs.getString("product_name"),
                          rs.getString("sku"),
                          rs.getFloat("price"),
                          rs.getString("description"),
                          rs.getInt("stock")),
                  rs.getInt("quantity"),
                  rs.getFloat("price"));

          currentLineItems.add(lineItem);

        }




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

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Save addresses to database and save them back to initial order instance
    order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
    order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

    // Save the user to the database and save them back to initial order instance
    order.setCustomer(UserController.createUser(order.getCustomer()));

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
      //Update the productid of the product before returning
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