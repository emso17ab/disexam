package controllers;

import java.util.ArrayList;
import datasource.DbService;
import io.jsonwebtoken.Claims;
import model.User;
import utils.Authenticator;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static User activeUser;

  public UserController() {
    activeUser = new User();
  }

  public static User getActiveUser() {
    return activeUser;
  }

  public static User getUser(int id) {
    return DbService.getUser(id);
  }

  private static String verifyUserExists(String email) {
    return DbService.verifyUserExists(email);
  }

  public static ArrayList<User> getUsers() {
    return DbService.getUsers();
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check that email is not already used
    if (verifyUserExists(user.getEmail()) == null) {

      final String salt = Hashing.salt();

      // TODO: FIX - Hash the user password before saving it.
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

      // Create the user in the DB
      int userID = DbService.createObject(sql);

      if (userID != 0) {
        //Update the userId of the user before returning
        user.setId(userID);
      } else {
        // Return null if user has not been inserted into database
        return null;
      }
    } else {
      //Return null if email is already used
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

    if (salt != null) {

      //Hashing password input from user
      String hashedPassword = Hashing.sha(user.getPassword() + salt);

      // Execute query in DB
      activeUser = DbService.login(user, hashedPassword);

      if (activeUser != null) {

        //Generate token for the user
        String token = Authenticator.createToken(activeUser.getId());
        //Assign token to the user
        activeUser.setToken(token);

        if (token != null) {
          return activeUser;
        }
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

      if (Integer.parseInt(claims.getId()) == user.getId() && DbService.deleteUser(user.getId())) {

        activeUser = null;
        return true;

      } return false;
    } catch (io.jsonwebtoken.SignatureException exception){ return false; }
  }

  public static Boolean updateUser(User user) throws io.jsonwebtoken.SignatureException {

    try {
      Claims claims = Authenticator.verifyToken(user.getToken());

      if (Integer.parseInt(claims.getId()) == user.getId()) {
        //Returns true if user was successfully updated
        return DbService.updateUser(user);
      }
    } catch (io.jsonwebtoken.SignatureException exception){
      return false;
    }
    return false;
  }
}
