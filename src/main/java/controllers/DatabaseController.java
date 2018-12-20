package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import utils.Config;

public class DatabaseController {

  private static Connection connection;
  private static Session session;

  public DatabaseController() {
    session = getSession();
    connection = getConnection();
  }

  /**
   * Get database connection
   *
   * @return a Connection object
   */
  private Connection getConnection() {
    try {
      // Set the dataabase connect with the data from the config
      String url =
          "jdbc:mysql://"
              + Config.getDatabaseHost()
              + ":"
              + Config.getDatabasePort()
              + "/"
              + Config.getDatabaseName()
              + "?serverTimezone=CET";

      String user = Config.getDatabaseUsername();
      String password = Config.getDatabasePassword();

      // Register the driver in order to use it
      DriverManager.registerDriver(new com.mysql.jdbc.Driver());

      // create a connection to the database
      connection = DriverManager.getConnection(url, user, password);

    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    return connection;
  }

  private Session getSession() {
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
      session = jsch.getSession(user, host, port);
      session.setPassword(password);
      session.setConfig(config);
      session.connect();
      session.setPortForwardingL(tunnelLocalPort,tunnelRemoteHost,tunnelRemotePort);
      System.out.println("Connected on " + session.getServerVersion());

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return session;
  }

  /**
   * Do a query in the database
   *
   * @return a ResultSet or Null if Empty
   */
  public ResultSet query(String sql) {

    // Get connection
      connection = getConnection();


    // We set the resultset as empty.
    ResultSet rs = null;

    try {
      // Build the statement as a prepared statement
      PreparedStatement stmt = connection.prepareStatement(sql);

      // Actually fire the query to the DB
      rs = stmt.executeQuery();

      // Return the results
      return rs;
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    // Return the resultset which at this point will be null
    return rs;
  }

  public int insert(String sql) {

    // Set key to 0 as a start
    int result = 0;

    // Get connection
      connection = getConnection();

    try {
      // Build the statement up in a safe way
      PreparedStatement statement =
          connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

      // Execute query
      result = statement.executeUpdate();

      // Get our key back in order to update the user
      ResultSet generatedKeys = statement.getGeneratedKeys();
      if (generatedKeys.next()) {
        return generatedKeys.getInt(1);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    // Return the resultset which at this point will be null
    return result;
  }

  public void closeConnection() {
    try {
      //Make sure we close the connection again
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
