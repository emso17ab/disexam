package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;

import datasource.DbService;
import model.Address;
import utils.Log;

public class AddressController {

  public AddressController() {
  }

  public static Address getAddress(int id) {
    return DbService.getAddress(id);
  }

  public static Address createAddress(Address address) {

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), address, "Actually creating a line item in DB", 0);

    String sql =
            "INSERT INTO address(name, city, zipcode, street_address) VALUES('"
                    + address.getName()
                    + "', '"
                    + address.getCity()
                    + "', '"
                    + address.getZipCode()
                    + "', '"
                    + address.getStreetAddress()
                    + "')";

    // Insert the product in the DB
    int addressID = DbService.createObject(sql);

    if (addressID != 0) {
      //Update the productId of the product before returning
      address.setId(addressID);
    } else{
      // Return null if product has not been inserted into database
      return null;
    }

    // Return product, will be null at this point
    return address;
  }
  
}
