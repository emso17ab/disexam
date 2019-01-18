package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import io.jsonwebtoken.Claims;
import model.User;
import utils.Authenticator;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static User activeUser;
  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
    activeUser = new User();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }


    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("salt"));

        //Making sure we close the connection again
        dbCon.closeConnection();

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return null;
  }

  public static User getActiveUser() {
    return activeUser;
  }

  private static String verifyUserExists(String email) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where email='" + email + "'";

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    String salt;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        salt = rs.getString("salt");

        //Making sure we close the connection again
        dbCon.closeConnection();
        return salt;

      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return null;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
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
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    } finally {
      //Making sure to close the connection again
      dbCon.closeConnection();
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Check that email is not already used
    if (verifyUserExists(user.getEmail()) == null) {


      final String salt = Hashing.salt();

      // Insert the user in the DB
      // TODO: FIX - Hash the user password before saving it.
      int userID = dbCon.insert(
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
                      + "')");

      //Making sure we close the connection again
      dbCon.closeConnection();

      if (userID != 0) {
        //Update the userid of the user before returning
        user.setId(userID);
      } else {
        // Return null if user has not been inserted into database
        return null;
      }
    } else {
      return null;
    }

    // Return user
    return user;
  }

  public static User login(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Verifying user...", 0);

    //Verify that user exists in the database and get salt
    String salt = verifyUserExists(user.getEmail());

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    if (salt != null) {

      //Hashing password input from user
      String hashedPassword = Hashing.sha(user.getPassword() + salt);

      // Build the query for DB
      String sql = "SELECT * FROM user WHERE email='" + user.getEmail() + "' AND password='" + hashedPassword + "'";

      // Actually do the query
      ResultSet rs = dbCon.query(sql);
      User result;
      Boolean userNotFound = false;

      try {
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

          // Store the create object
          activeUser = result;
        } else {
          System.out.println("No user found");
          userNotFound = true;
        }
      } catch (SQLException ex) {
        System.out.println(ex.getMessage());
      } finally {
        //Making sure we close the connection again
        dbCon.closeConnection();
      }

      if (userNotFound){
        return null;
      }

      //Generate token for the user
      String token = Authenticator.createToken(activeUser.getId());
      //Assign token to the user
      activeUser.setToken(token);

      if (token != null) {
        return activeUser;
      } else {
        return null;
      }
    }
    return null;
  }

  public static void logout() {
      activeUser = null;
  }

  public static Boolean deleteUser(User user) throws io.jsonwebtoken.SignatureException {
    try {
      Claims claims = Authenticator.verifyToken(user.getToken());

      if (Integer.parseInt(claims.getId()) == user.getId()) {

        // Check for DB Connection
        if (dbCon == null) {
          dbCon = new DatabaseController();
        }
        int rowsAffected = dbCon.insert("DELETE FROM user WHERE id=" + user.getId());

        //Making sure we close the connection again
        dbCon.closeConnection();

        if (rowsAffected == 1) {
          activeUser = null;
          return true;
        }
      } return false;
    } catch (io.jsonwebtoken.SignatureException exception){ return false; }
  }

  public static Boolean updateUser(User user) throws io.jsonwebtoken.SignatureException {
    try {
      Claims claims = Authenticator.verifyToken(user.getToken());

      if (Integer.parseInt(claims.getId()) == user.getId()) {

        //Check for DB Connection
        if (dbCon == null) {
          dbCon = new DatabaseController();
        }

        int rowsAffected = dbCon.insert("UPDATE user SET " +
                "first_name = '" + user.getFirstname() +
                "', last_name = '" + user.getLastname() +
                "', email = '" + user.getEmail() +
                "' WHERE id=" + user.getId() + ";");

        //Making sure we close the connection again
        dbCon.closeConnection();

        return rowsAffected == 1;
      }

    } catch (io.jsonwebtoken.SignatureException exception){
      return false;
    }
    return false;
  }
}
