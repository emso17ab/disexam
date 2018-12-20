package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Config;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  private final UserCache cache = new UserCache();
  private final Boolean cacheUpdate = Config.getCacheForceUpdate();

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = cache.getUser(idUser);

    // TODO: FIX Add Encryption to JSON
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);
    json = Encryption.encryptDecryptXOR(json);

    // TODO: FIX What should happen if something breaks down?
    if (user == null) {
      return Response.status(400).entity("Could not get user").build();
    }

    // Return the user with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = cache.getUsers(cacheUpdate);

    // TODO: FIX Add Encryption to JSON
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    json = Encryption.encryptDecryptXOR(json);

    if (users == null) {
      return Response.status(400).entity("Could not get users").build();
    }

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: FIX Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    // Read the json from body and transfer it to a user class
    User userInput = new Gson().fromJson(body, User.class);
    // Use the controller to verify and login the user
    User verifiedUser = UserController.login(userInput);
    String json = new Gson().toJson(verifiedUser);

    if (verifiedUser != null && verifiedUser.getToken() != null) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Login failed, check your credentials").build();
    }
  }

  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response loginUserThroughForm (@FormParam("username") String email, @FormParam("password") String password) {

    User user = new User();
    user.setEmail(email);
    user.setPassword(password);
    System.out.println(user.getEmail() + user.getPassword());
    String responseHtmlString;

    // Use the controller to verify and login the user
    User activeUser = UserController.login(user);
    if (activeUser != null) {
      responseHtmlString = "/OnSuccessPage.html";
    } else {
      // Return a response with status 200 and JSON as type
      responseHtmlString = "/OnFailurePage.html";
    }

    // Read File and store input
    InputStream input = UserEndpoints.class.getResourceAsStream(responseHtmlString);
    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

    // Go through the lines one by one
    StringBuffer stringBuffer = new StringBuffer();
    String str;

    // Read file one line at a time
    try {
      while ((str = reader.readLine()) != null) {
        stringBuffer.append(str);
      }
    }catch (Exception e){
      e.printStackTrace();
    }

    String htmlString = stringBuffer.toString();

    return Response.status(200).type(MediaType.TEXT_HTML_TYPE).entity(htmlString).build();
  }


  // TODO: FIX Make the system able to delete users
  @POST
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(String body) {

    // Read the json from body and transfer it to a user class
    User deleteMe = new Gson().fromJson(body, User.class);
    Boolean delete;

    //Check first if the user has accepted to delete his/her user profile
    if (deleteMe.isEraseMe()){
      delete = UserController.deleteUser(deleteMe);
      if (delete) {
        return Response.status(200).entity("User was successfully deleted!").build();
      }
    }else {
      return Response.status(200).entity("User was not deleted").build();
    }
    return Response.status(400).entity("Something went wrong!").build();
  }

  // TODO: FIX Make the system able to update users
  @POST
  @Path("/update")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(String body) {

    User updateMe = new Gson().fromJson(body, User.class);
    Boolean update;

    update = UserController.updateUser(updateMe);
    if (update) {
      return Response.status(200).entity("User was successfully updated!").build();
    } else {
      return Response.status(200).entity("User was not updated").build();
    }
  }

  @GET
  @Path("/profile")
  public Response profile() {

    String json = new Gson().toJson(UserController.getActiveUser());
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();

  }

}
