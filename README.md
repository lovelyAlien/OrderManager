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
외부 시스템 연동 및 데이터 변환 중 발생하는 예외를 처리하기 위해 Spring Retry를 활용하여 재시도 로직을 구현하였으며, 재시도 실패 시 Logback을 통해 해당 오류를 전용 로거로 기록하고 지정된 경로의 로그 파일에 저장되도록 설정하였습니다.

외부 시스템 연동과 데이터 변환에서 발생하는 오류를 **전용 로거**를 통해 관리하며, 로그 파일은 지정된 경로에 저장됩니다.   

### 전용 로거 설정   
- **전용 로거 지정**
```java
private static final Logger externalLogger = LoggerFactory.getLogger("externalLogger");
```
- **logback-spring.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- 콘솔에 로그 출력 (모든 로그) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 파일에 로그 저장 (외부 시스템 오류 로그 전용) -->
    <appender name="EXTERNAL_ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 로그 파일 경로 -->
        <file>logs/external-system-error.log</file>

        <!-- 파일이 일정 크기를 넘으면 새로운 파일로 교체 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <!-- 파일 이름 패턴 (날짜와 인덱스 기반으로 롤링) -->
            <fileNamePattern>logs/external-system-error.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <!-- 최대 파일 크기 (예: 10MB) -->
            <maxFileSize>10MB</maxFileSize>
            <!-- 30일 동안 로그 파일 보관 -->
            <maxHistory>30</maxHistory>
            <!-- 전체 로그 파일 크기 제한 (예: 3GB) -->
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>

        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- 외부 시스템과 관련된 오류만 파일에 기록 -->
    <logger name="externalLogger" level="ERROR" additivity="false">
        <appender-ref ref="EXTERNAL_ERROR_FILE" />
    </logger>

    <!-- 기본 콘솔 로그 설정 (모든 로그 콘솔 출력) -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

### 재시도 처리 및 로깅   
예외 발생 시 **재시도 메커니즘**을 적용하여, Spring Retry를 사용해 **최대 3번**의 재시도를 수행하고, 각 재시도 간격을 **2초**로 설정하였습니다.   
Spring Retry는 **Spring 프레임워크**의 일부로, 재시도 패턴을 간단하게 적용할 수 있도록 지원하는 라이브러리입니다.

```java
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
```

### 재시도 실패 처리   
**재시도 실패 시**에는 **복구 메소드**를 통해 대체 로직을 처리합니다. 아래는 `fetchData` 메소드에서 재시도가 실패했을 때 호출되는 `@Recover` 메소드입니다.

```java
// fetchData 재시도 실패 시 호출되는 복구 메소드
@Recover
public String recoverFetchData(ExternalSystemException e, String data) {
externalLogger.error("fetchData 재시도 실패 (데이터: {}): {}", data, e.getMessage());
  return "Fallback response for fetchData";
}
```

`@Recover`는 `@Retryable`에서 설정한 재시도 횟수를 모두 초과했을 때 실행되는 복구 메소드로, 재시도가 불가능할 때 **대체 처리 로직**을 수행합니다.
