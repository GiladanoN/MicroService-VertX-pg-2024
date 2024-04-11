package com.giladanon.extraTestModule;

import java.util.Date;
import java.util.UUID;

public class Order {
  
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

  public void setOrderID(UUID orderID) {
    this.orderID = orderID;
  }
  public void setOrderName(String orderName) {
    this.orderName = orderName;
  }
  public void setOrderDate(Date orderDate) {
    this.orderDate = orderDate;
  }

}
