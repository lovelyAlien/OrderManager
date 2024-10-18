package com.humuson.ordermanager.service;

import com.humuson.ordermanager.entity.Order;
import com.humuson.ordermanager.global.OrderNotFoundException;
import com.humuson.ordermanager.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepository;
  private final ExternalService externalService;

  public Order getOrderById(String id) {
    return orderRepository.findById(id)
      .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found."));
  }

  public List<Order> getAllOrders() {
    return orderRepository.findAll();
  }

  public Order fetchAndSaveOrder() {
    Order order = externalService.fetchOrder();
    orderRepository.save(order);
    return order;
  }

  public void findAndSendOrder(String id) {
    Order order = orderRepository.findById(id)
      .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found."));
    externalService.sendOrder(order);
  }
}
