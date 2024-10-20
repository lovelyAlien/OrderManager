# OrderManager

## 클래스 다이어그램
![image](https://github.com/user-attachments/assets/ef9a20f9-285c-4e0a-a080-f11ba3979729)
`Order`      
: 주문 정보를 캡슐화한 도메인 엔티티   
   
`OrderRepository`   
: 주문 데이터를 로컬 저장소에 저장하거나 조회를 담당   
   
`OrderService`   
: 비즈니스 로직을 처리하는 서비스 계층으로, 주문 생성, 조회 등 주문과 관련된 비즈니스 로직을 관리   
   
`ExternalSystemTemplate(데이터 연동 템플릿)`   
: 템플릿 메서드 패턴을 사용하여 외부 시스템과의 통신 흐름을 정의하는 추상 클래스   
   
`ExternalHttpJsonService`
- ExternalSystemTemplate을 구현한 클래스
- 외부 시스템과 HTTP 통신을 통해 데이터를 주고받고 JSON 형식으로 데이터를 처리하는 역할을 수행<br>

## 데이터 연동 인터페이스
**템플릿 메소드 패턴**을 활용하여 데이터 연동 인터페이스를 구성하였습니다.   
<p align="center"><img src="https://github.com/user-attachments/assets/db22a77f-4f5b-4868-9123-f4c46f48150a" width="650" height="700"/></p>   

설계 요구사항 중 다음 두 가지를 고려했습니다:

1. **확장성 고려**: "향후 다른 형태의 외부 시스템과 연동할 가능성을 고려하여 설계의 유연성과 확장성을 확보하세요."
2. **로직 포함**: "인터페이스는 외부 시스템과의 통신 로직과 데이터 변환 로직을 포함해야 합니다."

이 두 요구사항을 분석한 결과, **템플릿 메소드 패턴**이 적합하다고 판단했습니다.

외부 시스템과의 통신 로직과 데이터 변환 로직이 핵심이며, 외부 시스템의 형태가 달라질 경우 **통신 방식**이나 **데이터 변환 방식**이 달라질 수 있습니다. 따라서, 이러한 세부 구현은 **추상 클래스**에서 정의된 추상 메소드를 통해 개별 구현체에서 처리하도록 설계했습니다.

데이터 연동 프로세스는 크게 **데이터 수신 후 변환**하거나, **데이터 변환 후 전송**하는 두 가지 주요 흐름으로 구성됩니다. 외부 시스템의 형태에 관계없이 이 흐름 자체는 동일하기 때문에, **템플릿 메소드**로 `fetchOrder`와 `sendOrder`를 정의하였습니다.

템플릿 메소드 패턴의 핵심은 **알고리즘의 뼈대**를 제공하면서, 구체적인 처리 로직은 하위 클래스에 위임하는 것입니다. 여기서 각 외부 시스템과의 세부적인 통신 방식과 데이터 변환 로직은 **구현체에서 제공**하게 됩니다. 그러나 큰 흐름(데이터 수신 및 전송)은 동일하므로, 이를 오버라이딩하지 않고 그대로 사용했습니다.

이와 같은 설계는 시스템이 확장될 때, 새로운 외부 시스템과의 연동을 쉽게 추가할 수 있는 유연성을 제공합니다.

## 예외 처리 (Exception Handling)

**외부 시스템 연동 및 데이터 변환 과정**에서 발생하는 예외 상황을 처리하기 위해, **Logback**을 사용하여 오류 발생 시 로그를 파일에 기록하도록 구현하였습니다. 

외부 시스템 연동과 데이터 변환에서 발생하는 오류를 **전용 로거**를 통해 관리하며, 로그 파일은 지정된 경로에 저장됩니다.
```
private static final Logger externalLogger = LoggerFactory.getLogger("externalLogger");
```

1. 외부 시스템 연동 오류  처리

```java
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
```

외부 시스템으로 데이터를 전송하는 과정에서 예외가 발생할 경우, **전송 데이터를 포함한 로그**를 남기고 동일한 방식으로 예외를 처리합니다.

```java
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
```

2. **데이터 변환 오류 처리**

```java
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
```

```java
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
```
