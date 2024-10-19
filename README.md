# OrderManager

## Class Diagram
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
- 외부 시스템과 HTTP 통신을 통해 데이터를 주고받고 JSON 형식으로 데이터를 처리하는 역할을 수행    
