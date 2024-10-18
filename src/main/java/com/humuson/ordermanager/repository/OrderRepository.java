package com.humuson.ordermanager.repository;

import com.humuson.ordermanager.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OrderRepository {
  private final Map<String, Order> orderStorage = new HashMap<>();

  public void save(Order order) {
    orderStorage.put(order.getOrderId(), order);
  }

  public Optional<Order> findById(String orderId) {
    return Optional.ofNullable(orderStorage.get(orderId));
  }

  public List<Order> findAll() {
    return new ArrayList<>(orderStorage.values());
  }
}
