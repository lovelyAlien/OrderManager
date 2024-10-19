package com.humuson.ordermanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humuson.ordermanager.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ExternalHttpJsonService extends ExternalSystemTemplate {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${external.system.url}")
  private String EXTERNAL_SYSTEM_URL;

  @Override
  protected String fetchData() {
    try {
      String response = restTemplate.getForObject(EXTERNAL_SYSTEM_URL, String.class);
      return response;
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch data from external system", e);
    }
  }

  @Override
  protected Order convertDataToOrder(String data) {
    try {
      // ObjectMapper를 사용하여 JSON 데이터를 Order 객체로 변환
      return objectMapper.readValue(data, Order.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert JSON to Order", e);
    }
  }

  @Override
  protected String convertOrderToData(Order order) {
    try {
      // ObjectMapper를 사용하여 Order 객체를 JSON 형식으로 변환
      return objectMapper.writeValueAsString(order);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to convert Order to JSON", e);
    }
  }

  @Override
  protected void sendData(String data) {
    try {
      // HTTP 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // HTTP 요청 본문 생성
      HttpEntity<String> request = new HttpEntity<>(data, headers);

      // 외부 시스템으로 POST 요청을 전송하고 응답 처리
      restTemplate.postForObject(EXTERNAL_SYSTEM_URL, request, String.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to send data to external system", e);
    }
  }
}
