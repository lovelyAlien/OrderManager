package com.humuson.ordermanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humuson.ordermanager.entity.Order;
import com.humuson.ordermanager.global.ExternalSystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalHttpJsonService extends ExternalSystemTemplate {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private static final Logger externalLogger = LoggerFactory.getLogger("externalLogger");

  @Value("${external.system.url}")
  private String EXTERNAL_SYSTEM_URL;

  @Override
  protected String fetchData() {
    try {
      String response = restTemplate.getForObject(EXTERNAL_SYSTEM_URL, String.class);
      return response;
    } catch (Exception e) {
      // 데이터 연동 실패 시 URL과 예외 로그 기록
      externalLogger.error("데이터 연동 중 오류 발생 (URL: {}): {}", EXTERNAL_SYSTEM_URL, e.getMessage(), e);
      throw new ExternalSystemException("외부 시스템과의 연동 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  protected Order convertDataToOrder(String data) {
    try {
      // ObjectMapper를 사용하여 JSON 데이터를 Order 객체로 변환
      return objectMapper.readValue(data, Order.class);
    } catch (JsonProcessingException e) {
      // 데이터 변환 실패 시 원본 JSON 데이터를 로그로 기록
      externalLogger.error("데이터 변환 중 오류 발생 (JSON 데이터: {}): {}", data, e.getMessage(), e);
      throw new ExternalSystemException("데이터 변환 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  protected String convertOrderToData(Order order) {
    try {
      // ObjectMapper를 사용하여 Order 객체를 JSON 형식으로 변환
      return objectMapper.writeValueAsString(order);
    } catch (JsonProcessingException e) {
      // 네트워크 오류 등 연동 문제 발생 시 로그
      externalLogger.error("Order 객체를 JSON으로 변환 중 오류 발생 (Order: {}): {}", order, e.getMessage(), e);
      throw new ExternalSystemException("데이터 변환 중 오류가 발생했습니다.", e);
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
      externalLogger.error("데이터 전송 중 오류 발생 (전송 데이터: {}): {}", data, e.getMessage(), e);
      throw new ExternalSystemException("외부 시스템과의 연동 중 오류가 발생했습니다.", e);
    }
  }
}
