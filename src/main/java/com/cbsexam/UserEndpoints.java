package com.cbsexam;

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

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: FIX Add Encryption to JSON
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);
    json = Encryption.encryptDecryptXOR(json);

    if (user == null) {
      return Response.status(400).entity("Could not create user").build();
    }

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down?
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = UserController.getUsers();

    // TODO: FIX Add Encryption to JSON
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    json = Encryption.encryptDecryptXOR(json);

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

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    // Read the json from body and transfer it to a user class
    User logonUser = new Gson().fromJson(body, User.class);

    // Use the controller to verify and login the user //TODO (OWN TODO) This should return a token not a boolean value!
    if (UserController.login(logonUser)) {
      return Response.status(400).entity("You have been successfully logged in!").build();
    } else {
      // Return a response with status 200 and JSON as type
      return Response.status(400).entity("Login failed, check your credentials").build();
    }
  }


  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response loginUserThroughForm (@FormParam("username") String email, @FormParam("password") String password) {

    User user = new User(email, password);
    System.out.println(user.getEmail() + user.getPassword());
    String responseHtmlString;

    // Use the controller to verify and login the user //TODO (OWN TODO) This should return a token not a boolean value!
    if (UserController.login(user)) {
      System.out.println("SUCCESS");
      responseHtmlString = "/OnSuccessPage.html";
    } else {
      // Return a response with status 200 and JSON as type
      System.out.println("FAILURE");
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
      System.out.println(e);
    }

    String htmlString = stringBuffer.toString();

    return Response.status(200).type(MediaType.TEXT_HTML_TYPE).entity(htmlString).build();
  }

  // TODO: Make the system able to delete users
  @POST
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }

  // TODO: Make the system able to update users
  @POST
  @Path("/update")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }
}
