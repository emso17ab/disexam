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

public class DbService {

  //The SSH tunnel necessary to connect to DB is established in this static initialization block
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

  /**
   * Do a query in the database
   *
   * @return a ResultSet or Null if Empty
   */
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
}

