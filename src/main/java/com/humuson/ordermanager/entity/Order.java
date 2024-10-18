package com.humuson.ordermanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
  private String orderId;
  private String customerName;
  private LocalDate orderDate;
  private OrderStatus orderStatus;
}

