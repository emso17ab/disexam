package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UIController;
import controllers.UserController;
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

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the cache
    User user = UserCache.getUser(idUser);

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
    ArrayList<User> users = UserCache.getUsers(Config.getCacheForceUpdate());

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
    User createUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User newUser = UserController.createUser(createUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(newUser);

    // Return the data to the user
    if (newUser != null) {
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
    User unAuthUser = new Gson().fromJson(body, User.class);

    // Use the controller to verify user and login
    User verifiedUser = UserController.login(unAuthUser);
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

    User unAuthUser = new User();
    unAuthUser.setEmail(email);
    unAuthUser.setPassword(password);
    String responseHtmlString;

    // Use the controller to verify user and login
    User verifiedUser = UserController.login(unAuthUser);

    if (verifiedUser != null && verifiedUser.getToken() != null) {
      responseHtmlString = "/OnSuccessPage.html";
    } else {
      responseHtmlString = "/OnFailurePage.html";
    }
    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.TEXT_HTML_TYPE).entity(UIController.getPage(responseHtmlString)).build();
  }


  @GET
  @Path("/logout")
  public Response logout() {

    UserController.logout();

    return Response.status(200).type(MediaType.TEXT_HTML_TYPE).entity(UIController.getPage("/LoginPage.html")).build();

  }


  // TODO: FIX Make the system able to delete users
  @POST
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(String body) {

    // Read the json from body and transfer it to a user class
    User deleteMe = new Gson().fromJson(body, User.class);
    Boolean wasDeleted;

    //Check first if the user has accepted to delete his/her user profile
    if (deleteMe.isEraseMe()){
        wasDeleted = UserController.deleteUser(deleteMe);
      if (wasDeleted) {
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
    Boolean wasUpdated;

    wasUpdated = UserController.updateUser(updateMe);
    if (wasUpdated) {
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
