package com.humuson.ordermanager.entity;

import java.time.LocalDate;

public class Order {
  private String orderId;
  private String customerName;
  private LocalDate orderDate;
  private OrderStatus orderStatus; // 처리 중, 배송 중, 완료

  // 생성자, getter, setter 메서드 추가
}

