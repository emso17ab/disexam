package datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import model.*;
import utils.Config;

public class DbService {

  //In this static initialization block we establish the SSH tunnel through which we get our DB connection
  static {
    try {
      //Set connection credentials
      String host = Config.getSshTunnelHost();
      String user = Config.getSshTunnelUsername();
      String password = Config.getSshTunnelPassword();
      int port = Config.getSshTunnelPort();

      int tunnelLocalPort = Config.getDatabasePort();
      String tunnelRemoteHost = Config.getDatabaseHost();
      int tunnelRemotePort = Config.getSshTunnelRemoteport();

      java.util.Properties config = new java.util.Properties();
      config.put("StrictHostKeyChecking", "no");

      JSch jsch=new JSch();
      Session session = jsch.getSession(user, host, port);
      session.setPassword(password);
      session.setConfig(config);
      session.connect();
      session.setPortForwardingL(tunnelLocalPort,tunnelRemoteHost,tunnelRemotePort);
      System.out.println("Connected on " + session.getServerVersion());

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private DbService() {}

  //---------- ALL SERVICES BELOW THIS LINE ----------

  public static int createObject(String sql){

    // Set key to 0 as a start
    int result = 0;

    try {

      // Get connection from pool
      Connection con = DataSource.getConnection();

      // Build the statement up in a safe way
      PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

      // Execute query
      stmt.executeUpdate();

      // Get our key back in order to update the user
      ResultSet generatedKeys = stmt.getGeneratedKeys();

      if (generatedKeys.next()) {
        result = generatedKeys.getInt(1);
      }

      //Return the connection to the pool
      con.close();

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return result;
  }
  public static Order createOrder(Order order){

    //Define key placeholders
    Connection con = null;
    PreparedStatement billingStmt = null;
    PreparedStatement shippingStmt = null;
    PreparedStatement orderStmt = null;
    PreparedStatement lineItemStmt = null;
    int shippingAddressID = 0;
    int billingAddressID = 0;
    int orderId = 0;
    ArrayList<LineItem> items = new ArrayList<LineItem>();


    //Setting up address sql strings
    String billingAddressSql =
            "INSERT INTO address(name, city, zipcode, street_address) VALUES('"
                    + order.getBillingAddress().getName()
                    + "', '"
                    + order.getBillingAddress().getCity()
                    + "', '"
                    + order.getBillingAddress().getZipCode()
                    + "', '"
                    + order.getBillingAddress().getStreetAddress()
                    + "')";

    String shippingAddressSql =
            "INSERT INTO address(name, city, zipcode, street_address) VALUES('"
                    + order.getShippingAddress().getName()
                    + "', '"
                    + order.getShippingAddress().getCity()
                    + "', '"
                    + order.getShippingAddress().getZipCode()
                    + "', '"
                    + order.getShippingAddress().getStreetAddress()
                    + "')";


    //--> START TRANSACTION
    try {

      // Get connection from pool
      con = DataSource.getConnection();

      // Enable transactions
      con.setAutoCommit(false);

      // Build the address statements up in a safe way
      billingStmt = con.prepareStatement(billingAddressSql, Statement.RETURN_GENERATED_KEYS);
      shippingStmt = con.prepareStatement(shippingAddressSql, Statement.RETURN_GENERATED_KEYS);

      //--> SAVE THE ADDRESSES TO THE DB

      // Execute the queries
      billingStmt.executeUpdate();
      shippingStmt.executeUpdate();

      // Get our keys back in order to update the addresses
      ResultSet shippingKeys = shippingStmt.getGeneratedKeys();
      ResultSet billingKeys = billingStmt.getGeneratedKeys();

      // Set the generated addressId's
      if (shippingKeys.next()) {
        shippingAddressID = shippingKeys.getInt(1);
      }
      if (billingKeys.next()) {
        billingAddressID = billingKeys.getInt(1);
      }
      // After addresses are saved to DB they are saved back to initial order instance with ID
      if (shippingAddressID != 0 && billingAddressID != 0) {
        order.getBillingAddress().setId(billingAddressID);
        order.getShippingAddress().setId(shippingAddressID);
      }

    //--> SAVE ORDER TO THE DB

      //Setting up order sql string
      String orderSql =
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
                      + ")";

      // Build the statement up in a safe way
      orderStmt = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);

      // Execute the query
      orderStmt.executeUpdate();

      // Get our key back in order to update the order Id
      ResultSet orderKey = orderStmt.getGeneratedKeys();

      // Set the generated orderId
      if (orderKey.next()) {
        orderId = orderKey.getInt(1);
      }

      if (orderId != 0) {
      //Update the order id of the order
      order.setId(orderId);
      }

      //--> SAVE LINEITEMS TO THE DB

      //Create the Sql String
      String lineItemSql = "INSERT INTO line_item(product_id, order_id, price, quantity) VALUES(?,?,?,?)";

      // Build the statement up in a safe way
      lineItemStmt = con.prepareStatement(lineItemSql, Statement.RETURN_GENERATED_KEYS);

      // Save line items to database one by one
      for(LineItem item : order.getLineItems()){
        lineItemStmt.setInt(1, item.getProduct().getId());
        lineItemStmt.setInt(2, orderId);
        lineItemStmt.setFloat(3, item.getPrice());
        lineItemStmt.setInt(4, item.getQuantity());
        lineItemStmt.executeUpdate();

        ResultSet lineItemKey = lineItemStmt.getGeneratedKeys();
        if (lineItemKey.next()){
          item.setId(lineItemKey.getInt(1));
        }
        items.add(item);
        lineItemKey.close();
      }

      //Transaction is committed
      con.commit();

      //Save all lineItems to order
      order.setLineItems(items);

    } catch (SQLException e) {
      System.out.println(e.getMessage());
      if (con != null){
        try {
          System.err.print("Transaction is being rolled back");
          con.rollback();
        } catch(SQLException ex) {
          System.out.println(ex.getMessage());
        }
      }
    } finally {
      try {
        if (shippingStmt != null) {
          shippingStmt.close();
        }
        if (billingStmt != null){
          billingStmt.close();
        }
        if (orderStmt != null){
          orderStmt.close();
        }
        if (lineItemStmt != null){
          lineItemStmt.close();
        }
        if (con != null) {

          //Enable AutoCommit again
          con.setAutoCommit(true);

          //Return the connection to the pool
          con.close();
        }
      } catch (SQLException e){
        System.out.println(e.getMessage());
      }
    }
    //Finally we return the order that was created
    return order;
  }

  public static Address getAddress(int id){

    Address address = null;

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM address where id=" + id);

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      // Get the first row and build an address object
      if (rs.next()) {
        address =
                new Address(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("street_address"),
                        rs.getString("city"),
                        rs.getString("zipcode")
                );

      } else {
        System.out.println("No address found");
      }

      //Returning the connection to the pool
      con.close();

      //Return the user object
      return address;

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null if we can't find anything.
    return null;
  }
  public static Product getProduct(int id){

    Product product = null;

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM product where id=" + id);

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      // Get first row and create the object and return it
      if (rs.next()) {
        product =
                new Product(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getString("sku"),
                        rs.getFloat("price"),
                        rs.getString("description"),
                        rs.getInt("stock"));

      } else {
        System.out.println("No user found");
      }

      //Returning the connection to the pool
      con.close();

      // Return the product
      return product;

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    // Return empty object
    return product;
  }
  public static User getUser(int id){

    User user = null;

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM user where id=" + id);

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        user = new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("salt"));

      } else {
        System.out.println("No user found");
      }

      //Returning the connection to the pool
      con.close();

      //Return the user object
      return user;

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    //This will be null at this point
    return user;
  }
  public static Order getOrder(int id){

    //Preparing the order to be returned
    Order currentOrder = null;

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

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement(sql);

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      //Necessary variables/placeholders for loop through ResultSet to work
      ArrayList<LineItem> currentLineItems = null;
      int lineNo; //The current order_id value at current iteration through ResultSet
      int currentOrderId = 0; //The current order we are processing in the loop

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

      //Returning the connection to the pool
      con.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return currentOrder;
  }

  public static ArrayList<Product> getProducts(){

    ArrayList<Product> products = new ArrayList<>();

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM product");

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Product product =
                new Product(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getString("sku"),
                        rs.getFloat("price"),
                        rs.getString("description"),
                        rs.getInt("stock"));

        products.add(product);
      }
      //Returning the connection to the pool
      con.close();

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    return products;
  }
  public static ArrayList<User> getUsers(){

    ArrayList<User> users = new ArrayList<User>();

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM user");

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("salt"));

        // Add element to list
        users.add(user);
      }
      //Returning the connection to the pool
      con.close();

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    // Return the list of users
    return users;
  }
  public static ArrayList<Order> getOrders(){

    //Preparing the orders to be returned
    ArrayList<Order> orders = new ArrayList<>();

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

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement(sql);

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      //Necessary variables/placeholders for loop through ResultSet to work
      ArrayList<LineItem> currentLineItems = null;
      Order currentOrder = null;
      int lineNo; //The current order_id value at current iteration through ResultSet
      int currentOrderId = 0; //The current order we are processing in the loop

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

      //Returning the connection to the pool
      con.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
    }
    return orders;
  }

  public static String verifyUserExists(String email){

    String salt = null;

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM user where email='" + email + "'");

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      // Get first object, since we only have one
      if (rs.next()) {
        salt = rs.getString("salt");

      } else {
        System.out.println("No user found");
      }

      //Returning the connection to the pool
      con.close();

      //Return the salt String
      return salt;

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    //This will be null at this point
    return salt;
  }
  public static User login(User user, String hashedPassword){

    String sql = "SELECT * FROM user WHERE email='" + user.getEmail() + "' AND password='" + hashedPassword + "'";
    Boolean userNotFound = false;
    User result = null;

    try {

      // Get connection from the pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement(sql);

      //Execute the statement to the DB
      ResultSet rs = stmt.executeQuery();

      // Get first object, since we only have one
      if (rs.next()) {
        result =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("salt"));

      } else {
        System.out.println("No user found");
        userNotFound = true;
      }

      //Returning the connection to the pool
      con.close();

    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    if (userNotFound) {
      return null;
    }
    return result;
  }
  public static Boolean deleteUser(int id){

    // Set key to 0 as a start
    int rowsAffected = 0;

    try {

      // Get connection from pool
      Connection con = DataSource.getConnection();

      // Build the statement up in a safe way
      PreparedStatement stmt = con.prepareStatement("DELETE FROM user WHERE id=" + id);

      // Execute query
      rowsAffected = stmt.executeUpdate();

      //Return the connection to the pool
      con.close();

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    //If user was successfully deleted, this will return true
    return rowsAffected == 1;
  }
  public static Boolean updateUser(User user){

    // Set key to 0 as a start
    int rowsAffected = 0;

  //Set Sql String
    String sql = "UPDATE user SET " +
            "first_name = '" + user.getFirstname() +
            "', last_name = '" + user.getLastname() +
            "', email = '" + user.getEmail() +
            "' WHERE id=" + user.getId() + ";";

    try {

      // Get connection from pool
      Connection con = DataSource.getConnection();

      // Build the statement up in a safe way
      PreparedStatement stmt = con.prepareStatement(sql);

      // Execute query
      rowsAffected = stmt.executeUpdate();

      //Return the connection to the pool
      con.close();

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    //If user was successfully updated, this will return true
    return rowsAffected == 1;
  }

}

