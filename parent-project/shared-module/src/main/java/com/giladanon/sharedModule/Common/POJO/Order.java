package com.giladanon.sharedModule.Common.POJO;

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

  @Override
  public String toString() {
    String asStr = "[OrderData]={";
    if (orderID != null)
      asStr += "order.id:(" + orderID + ") ,";
    if (orderName != null)
      asStr += "order.name:(" + orderName + ") ,";
    if (orderDate != null)
      asStr += "order.date:(" + orderDate + ") ,";
    asStr += "}";
    return asStr;
  }

}
