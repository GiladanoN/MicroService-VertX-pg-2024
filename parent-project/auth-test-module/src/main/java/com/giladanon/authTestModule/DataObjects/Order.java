package com.giladanon.authTestModule.DataObjects;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Order implements Serializable {
  
  UUID orderID;
  String orderName;
  Date orderDate;

  public UUID getOrderID() {
    return orderID;
  }
  public String getOrderName() {
    return orderName;
  }
  public Date getOrderDate() {
    return orderDate;
  }

  public Order setOrderID(UUID orderID) {
    this.orderID = orderID;
    return this;
  }
  public Order setOrderName(String orderName) {
    this.orderName = orderName;
    return this;
  }
  public Order setOrderDate(Date orderDate) {
    this.orderDate = orderDate;
    return this;
  }

}
