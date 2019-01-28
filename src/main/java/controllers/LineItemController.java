package controllers;

import model.LineItem;
import utils.Log;

public class LineItemController {

  public LineItemController() {
  }

  public static LineItem createLineItem(LineItem lineItem, int orderID) {

    //THIS IS PART OF A BIGGER TRANSACTION IN 'createOrder'

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), lineItem, "Actually creating a line item in DB", 0);

    // Get the ID of the product, since the user will not send it to us.
    // THIS DOES NOT MAKE ANY SENSE??
    //lineItem.getProduct().setId(ProductController.getProductBySku(lineItem.getProduct().getSku()).getId());

    //Making the SQL string
    String sql =
            "INSERT INTO line_item(product_id, order_id, price, quantity) VALUES("
            + lineItem.getProduct().getId()
            + ", "
            + orderID
            + ", "
            + lineItem.getPrice()
            + ", "
            + lineItem.getQuantity()
            + ")";

    /* if (lineItemID != 0) {
      //Update the productid of the product before returning
      lineItem.setId(lineItemID);
    } else{ */

      // Return null if product has not been inserted into database
      return null;
    }
}