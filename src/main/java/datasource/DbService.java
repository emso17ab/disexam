package datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import datasource.DataSource;
import model.User;
import utils.Config;
import utils.Hashing;

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

  public static ResultSet query(String sql) {

    ResultSet rs = null;

    try {

      // Get connection from pool
      Connection con = DataSource.getConnection();

      // Build the statement as a prepared statement
      PreparedStatement stmt = con.prepareStatement(sql);

      //Execute the statement to the DB
      rs = stmt.executeQuery();

      //Return the connection to the pool
      con.close();

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    // Return the resultset which at this point will be null
    return rs;
  }

  public static int insert(String sql) {

    // Set key to 0 as a start
    int result = 0;

    try {

      // Get connection from pool
      Connection con = DataSource.getConnection();

      // Build the statement up in a safe way
      PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

      // Execute query
      result = stmt.executeUpdate();

      // Get our key back in order to update the user
      ResultSet generatedKeys = stmt.getGeneratedKeys();
      if (generatedKeys.next()) {
        return generatedKeys.getInt(1);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    // Return the resultset which at this point will be null
    return result;
  }

  //ALL SERVICES ARE BELOW

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

  public static int createUser(User user, String salt){

    // Set key to 0 as a start
    int result = 0;

    //Set Sql String
    String sql =
            "INSERT INTO user(first_name, last_name, password, email, created_at, salt) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    + Hashing.sha(user.getPassword() + salt) //Adding salt before hashing and saving
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ", '"
                    + salt //Saving the salt in the database
                    + "')";

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

  public static int deleteUser(int id){

    // Set key to 0 as a start
    int result = 0;

    try {

      // Get connection from pool
      Connection con = DataSource.getConnection();

      // Build the statement up in a safe way
      PreparedStatement stmt = con.prepareStatement("DELETE FROM user WHERE id=" + id, Statement.RETURN_GENERATED_KEYS);

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

