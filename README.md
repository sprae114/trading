# 중고거래 웹 프로젝트

## 📌 프로젝트 진행 배경

### 동기
중고거래 플랫폼은 사용자들이 물건을 사고팔며, 실시간으로 채팅을 통해 거래를 협의할 수 있는 편리한 환경을 제공합니다. 이 프로젝트는 사용자들이 중고 물품을 쉽고 빠르게 등록, 검색, 거래하고, 실시간 채팅으로 소통할 수 있는 플랫폼을 제공하는 것을 목표로 합니다.

### 문제 정의
이 프로젝트는 다음과 같은 문제를 해결하고자 합니다.
- **인증 및 보안의 복잡성**: 복잡한 로그인 및 회원가입 절차를 간소화하고, 안전한 인증 시스템(JWT, OTP)을 통해 사용자 보안을 강화합니다.

- **실시간 채팅의 안정성**: WebSocket과 Kafka를 활용해 고가용성과 확장성을 갖춘 실시간 채팅 시스템을 구현하여 메시지 손실을 방지합니다.

- **데이터 조회 성능**: Redis를 활용한 캐싱과 Spring Data JPA 쿼리 최적화를 통해 조회수, 좋아요, 검색 기능의 성능을 향상시킵니다.

- **배포 환경의 일관성**: Docker와 Docker Compose를 통해 개발, 테스트, 배포 환경을 일관되게 유지하여 배포 프로세스를 간소화합니다.


## 📌 아키텍쳐 및 기술 스택

### 아키텍쳐
![Image](https://github.com/user-attachments/assets/14772547-39b9-4cde-94be-3abc94c679f4)


### ERD
![Image](https://github.com/user-attachments/assets/69343a1a-cd3a-4451-8968-846e3cb453d5)

### 기술 스택

|   |   |   |
|---|---|---|
|구분|기술 스택|주요 역할|
|프론트엔드|Vue.js (JavaScript), Vuetify|반응형 UI 제공 및 컴포넌트 기반 UI 구성|
|백엔드|Spring Boot (Java)|서버 로직 처리 및 RESTful API 제공|
|백엔드 기능|Spring Security, JWT|인증 및 권한 관리|
||Spring Schedule, Spring Mail|스케줄링 및 인증 메일 전송|
||Spring Data JPA, WebSocket, Spring Kafka|데이터 관리, 실시간 통신, 메시지 브로커|
||Spring Data MongoDB|중고거래 데이터 저장|
|서버|NGINX|로드 밸런싱 및 정적 자원 서빙|
|DB|MySQL|채팅 데이터 저장|
||MongoDB|중고거래 데이터 저장|
||Redis|조회수, 좋아요 인메모리 캐싱|
|스토리지|AWS S3|상품 이미지 저장 및 불러오기|
|컨테이너|Docker, Docker Compose|서비스 컨테이너화 및 배포 환경 관리|

## 📌 프로젝트 주요 기능
### 1) JWT를 이용한 로그인 처리
- Spring Security와 JWT를 통해 stateless 인증 시스템을 구축, 안전하고 간편한 로그인 기능을 제공했습니다.
- 사용자 인증 및 권한 부여 로직을 최적화하여 보안성을 강화했습니다.
  ![Image](https://github.com/user-attachments/assets/5739044a-c667-462b-ba8a-393f1d5422c7)


### 2) Spring Email을 이용한 회원가입 OTP
- Spring Mail을 활용해 회원가입 시 6자리 OTP를 이메일로 전송, 사용자 인증 프로세스를 간소화했습니다.
- Redis에 OTP를 5분 동안 캐싱하여 유효성 검증을 수행, 인증의 안정성을 높였습니다.
  ![Image](https://github.com/user-attachments/assets/c162cef8-034a-4f0c-9927-f2bc569fbbe9)


### 3) Redis를 활용한 조회 성능 최적화
- Redis를 이용해 상품 조회수와 좋아요 수를 인메모리에서 실시간 집계, RDBMS 부하를 약 70% 감소시켰습니다.
- Spring Scheduler를 통해 Redis와 MySQL 간 데이터를 5분 간격으로 동기화, 데이터 일관성을 유지했습니다.
  ![Image](https://github.com/user-attachments/assets/4357ef62-8724-4d75-afb6-fc44852ec088)
  
### 4) WebSocket과 Kafka를 이용한 실시간 채팅
- WebSocket을 통해 실시간 양방향 통신을 구현, 사용자 간 즉각적인 채팅 경험을 제공했습니다.
- Kafka를 도입하여 채팅 메시지의 비동기 처리를 구현, 대규모 사용자 요청에도 안정적인 서비스를 유지했습니다.
  ![Image](https://github.com/user-attachments/assets/0c6b5cd1-0569-4aa9-ba46-467b3b0a2288)

### 5) Docker와 Docker Compose로 배포
- Docker 컨테이너로 서비스를 패키징하여 개발 및 배포 환경의 일관성을 확보했습니다.
- Docker Compose를 통해 Spring Boot, MySQL, MongoDB, Redis, Kafka 등 멀티 컨테이너 환경을 구성, 네트워크와 볼륨 설정을 간소화했습니다.
- NGINX를 활용한 로드 밸런싱으로 서버 부하를 분산, 서비스 안정성을 높였습니다.
