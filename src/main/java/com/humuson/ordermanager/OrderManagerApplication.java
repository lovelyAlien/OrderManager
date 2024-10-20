package com.humuson.ordermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class OrderManagerApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderManagerApplication.class, args);
  }

}
