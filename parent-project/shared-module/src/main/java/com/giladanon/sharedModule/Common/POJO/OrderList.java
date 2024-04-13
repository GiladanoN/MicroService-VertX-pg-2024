package com.giladanon.sharedModule.Common.POJO;

import java.io.Serializable;
import java.util.List;

public class OrderList implements Serializable {
  
  private List<Order> ordersList;

  public List<Order> getOrdersList() {
    return ordersList;
  }
  public void setOrdersList(List<Order> list) {
    this.ordersList = list;
  }
  
}

