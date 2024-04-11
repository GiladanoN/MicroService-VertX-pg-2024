package com.giladanon.extraTestModule;

import io.vertx.ext.auth.User;

public class OrderToAdd {

  Order orderToAdd; // the order object and details to add.
  User user;   // the relevant user (auth) who made the request.

  public Order getOrderToAdd() {
    return orderToAdd;
  }
  public User getUser() {
    return user;
  }

  public void setOrderToAdd(Order orderToAdd) {
    this.orderToAdd = orderToAdd;
  }
  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public String toString() {
    String asStr = "[OrderToAdd]={";
    if (user != null)
      asStr += "user.subject:(" + user.subject() + ") ,";
    if (orderToAdd != null)
      asStr += "orderToAdd:(" + orderToAdd + ") ,";
    asStr += "}";
    return asStr;
  }

}
