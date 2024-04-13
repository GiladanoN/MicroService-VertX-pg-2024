package com.giladanon.sharedModule.Common.POJO;

import java.io.Serializable;
import io.vertx.ext.auth.User;
import com.giladanon.sharedModule.Common.POJO.Order;

public class OrderToAdd implements Serializable {

  Order orderToAdd; // the order object and details to add.
  String user;   // the relevant user (auth) who made the request.

  public Order getOrderToAdd() {
    return orderToAdd;
  }
  public String getUser() {
    return user;
  }

  public OrderToAdd setOrderToAdd(Order orderToAdd) {
    this.orderToAdd = orderToAdd;
    return this;
  }
  public OrderToAdd setUser(String user) {
    this.user = user;
    return this;
  }
  public OrderToAdd setUser(User user) {
    this.user = user.subject();
    return this;
  }

  @Override
  public String toString() {
    String asStr = "[OrderToAdd]={";
    if (user != null)
      asStr += "user.subject:(" + user + ") ,";
    if (orderToAdd != null)
      asStr += "orderToAdd:(" + orderToAdd + ") ,";
    asStr += "}";
    return asStr;
  }

}
