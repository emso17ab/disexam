package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import datasource.DbService;
import model.*;
import utils.Log;

public class OrderController {

  public OrderController() {
  }

  public static Order getOrder(int id) {
    return DbService.getOrder(id);
  }

  public static ArrayList<Order> getOrders() {
    return DbService.getOrders();
  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    //Setting the active user to the order
    order.setCustomer(UserController.getActiveUser());

    // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.

    // Save order to database through a transaction and save it back to initial order instance
    order = DbService.createOrder(order);

    return order;
  }
}