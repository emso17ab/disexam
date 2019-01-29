package com.cbsexam;

import com.google.gson.Gson;
import controllers.ReviewController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Review;
import utils.Encryption;

@Path("search")
public class ReviewEndpoints {

  /**
   * @param reviewTitle
   * @return Responses
   */
  @GET
  @Path("/title/{title}")
  public Response search(@PathParam("title") String reviewTitle) {

    // Call our controller-layer in order to get the order from the DB
    ArrayList<Review> reviews = ReviewController.searchByTitle(reviewTitle);

    // TODO: FIX Add Encryption to JSON
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(reviews);
    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/title/")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response searchThroughForm(@FormParam("searchfield") String searchField) {

    // Call our controller-layer in order to get the order from the DB
    ArrayList<Review> reviews = ReviewController.searchByTitle(searchField);

    // TODO: FIX Add Encryption to JSON
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(reviews);
    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }
}
