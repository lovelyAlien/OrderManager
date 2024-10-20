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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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
  @Retryable(
    retryFor = ExternalSystemException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000)
  )
  protected String fetchData() {
    try {
      String response = restTemplate.getForObject(EXTERNAL_SYSTEM_URL, String.class);
      return response;
    } catch (Exception e) {
      throw new ExternalSystemException("외부 시스템과의 연동 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  @Retryable(
    retryFor = ExternalSystemException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000)
  )
  protected Order convertDataToOrder(String data) {
    try {
      // ObjectMapper를 사용하여 JSON 데이터를 Order 객체로 변환
      return objectMapper.readValue(data, Order.class);
    } catch (JsonProcessingException e) {
      throw new ExternalSystemException("데이터 변환 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  @Retryable(
    retryFor = ExternalSystemException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000)
  )
  protected String convertOrderToData(Order order) {
    try {
      // ObjectMapper를 사용하여 Order 객체를 JSON 형식으로 변환
      return objectMapper.writeValueAsString(order);
    } catch (JsonProcessingException e) {
      throw new ExternalSystemException("데이터 변환 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  @Retryable(
    retryFor = ExternalSystemException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000)
  )
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
      throw new ExternalSystemException("외부 시스템과의 연동 중 오류가 발생했습니다.", e);
    }
  }

  // fetchData 재시도 실패 시 호출되는 복구 메소드
  @Recover
  public String recoverFetchData(ExternalSystemException e, String data) {
    externalLogger.error("fetchData 재시도 실패 (데이터: {}): {}", data, e.getMessage());
    return "Fallback response for fetchData";
  }

  // convertDataToOrder 재시도 실패 시 호출되는 복구 메소드
  @Recover
  public Order recoverConvertDataToOrder(ExternalSystemException e, String data) {
    externalLogger.error("convertDataToOrder 재시도 실패 (데이터: {}): {}", data, e.getMessage());
    return new Order(); // 기본 Order 객체 반환 또는 대체 처리
  }

  // convertOrderToData 재시도 실패 시 호출되는 복구 메소드
  @Recover
  public String recoverConvertOrderToData(ExternalSystemException e, Order order) {
    externalLogger.error("convertOrderToData 재시도 실패 (Order 객체: {}): {}", order, e.getMessage());
    return "Fallback JSON for convertOrderToData";
  }

  // sendData 재시도 실패 시 호출되는 복구 메소드
  @Recover
  public String recoverSendData(ExternalSystemException e, String data) {
    externalLogger.error("sendData 재시도 실패 (전송 데이터: {}): {}", data, e.getMessage());
    return "Fallback response for sendData";
  }
}
