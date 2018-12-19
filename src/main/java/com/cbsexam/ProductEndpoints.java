package com.cbsexam;

import cache.ProductCache;
import com.google.gson.Gson;
import controllers.ProductController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.Product;
import utils.Config;
import utils.Encryption;

@Path("product")
public class ProductEndpoints {

  private final ProductCache cache = new ProductCache();
  private final Boolean cacheUpdate = Config.getCacheForceUpdate();

  /**
   * @param idProduct
   * @return Responses
   */
  @GET
  @Path("/{idProduct}")
  public Response getProduct(@PathParam("idProduct") int idProduct) {

    // Call our controller-layer in order to get the order from the DB
    Product product = cache.getProduct(idProduct);

    // TODO: FIX Add Encryption to JSON
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(product);

    json = Encryption.encryptDecryptXOR(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getProducts() {

    // Call our controller-layer in order to get the order from the DB
    ArrayList<Product> products = cache.getProducts(cacheUpdate);

    // TODO: FIX Add Encryption to JSON
    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(products);
    json = Encryption.encryptDecryptXOR(json);

    System.out.println(json);

    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createProduct(String body) {

    // Read the json from body and transfer it to a product class
    Product newProduct = new Gson().fromJson(body, Product.class);

    // Use the controller to add the user
    Product createdProduct = ProductController.createProduct(newProduct);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdProduct);

    // Return the data to the user
    if (createdProduct != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create product").build();
    }
  }
}
