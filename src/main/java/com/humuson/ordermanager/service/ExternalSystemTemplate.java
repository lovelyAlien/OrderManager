package com.humuson.ordermanager.service;

import com.humuson.ordermanager.entity.Order;

public abstract class ExternalSystemTemplate {

  public final Order fetchOrder() {
    String rawData = fetchData();
    Order order = convertDataToOrder(rawData);
    return order;
  }

  protected abstract String fetchData();

  protected abstract Order convertDataToOrder(String data);

  public final void sendOrder(Order order) {
    String data = convertOrderToData(order);
    sendData(data);
  }

  protected abstract String convertOrderToData(Order order);

  protected abstract void sendData(String data);
}
