package com.humuson.ordermanager.controller;

import com.humuson.ordermanager.entity.Order;
import com.humuson.ordermanager.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  // 주문 조회 API
  @GetMapping("/{id}")
  public ResponseEntity<Order> getOrder(@PathVariable String id) {
    Order order = orderService.getOrderById(id);
    return ResponseEntity.ok(order);
  }

  // 주문 전체 조회 API
  @GetMapping
  public ResponseEntity<List<Order>> getAllOrders() {
    List<Order> orders = orderService.getAllOrders();
    return ResponseEntity.ok(orders);
  }

  // 외부 시스템에서 주문을 가져오는 API
  @GetMapping("/external")
  public ResponseEntity<Order> fetchOrdersFromExternal() {
    Order externalOrders = orderService.fetchAndSaveOrder();

    return ResponseEntity.ok(externalOrders);
  }

  // 외부 시스템으로 주문을 전송하는 API
  @PostMapping("/external")
  public ResponseEntity<String> sendOrdersToExternal(@PathVariable String id) {
    orderService.findAndSendOrder(id);
    return ResponseEntity.ok("Orders sent to external system successfully.");
  }
}
