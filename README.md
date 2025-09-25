# UMS (Unified Messaging Service)

통합 메시징 서비스 - SMS, Email, Push 알림, Kakao 알림톡을 지원하는 마이크로서비스

## 프로젝트 개요

UMSService는 다양한 채널을 통해 메시지를 전송할 수 있는 통합 메시징 서비스입니다. 멀티테넌트 아키텍처를 지원하며, 템플릿 기반 메시지 생성, 수신자 정책 관리, 속도 제한 등의 기능을 제공합니다.

## 주요 기능

### 📱 지원 채널
- **SMS**: CoolSMS API 연동
- **Email**: Spring Mail 기반 SMTP 전송
- **FCM Push**: Firebase Cloud Messaging 푸시 알림
- **Kakao 알림톡**: 카카오 비즈니스 메시지

### 🎯 핵심 기능
- **멀티테넌트**: 테넌트별 독립적인 메시지 관리
- **템플릿 시스템**: Mustache 기반 동적 메시지 생성
- **수신자 정책**: 옵트아웃, 조용한 시간, 일일 전송 제한
- **속도 제한**: 테넌트별 전송 속도 제어
- **메시지 추적**: 전송 상태 및 이력 관리
- **비동기 처리**: Kafka 기반 이벤트 드리븐 아키텍처

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.0, Spring WebFlux
- **Database**: PostgreSQL (JPA/Hibernate)
- **Message Queue**: Apache Kafka
- **Cache**: Redis
- **Build Tool**: Gradle
- **Documentation**: OpenAPI 3.0 (Swagger)

## 빠른 시작

### 사전 요구사항
- Java 21+
- Docker & Docker Compose
- PostgreSQL, Redis, Kafka (로컬 또는 Docker)

### 1. 저장소 클론
```bash
git clone <repository-url>
cd UMSService
```

### 2. 환경 설정
```bash
# application-dev.yml 파일 생성 및 설정
cp src/main/resources/application-example.yml src/main/resources/application-dev.yml
```

## 📧 메시지 제공업체 설정

UMSService는 여러 SMS/Email 제공업체를 지원하며, 자동 전환(failover) 기능을 제공합니다.

### SMS 제공업체

#### Solapi (CoolSMS) - 기본 제공업체
```yaml
ums:
  sms:
    provider: SOLAPI  # 또는 비워두면 자동 선택
    enable-fallback: true

    solapi:
      enabled: true
      api-key: your-api-key
      api-secret: your-api-secret
      from-number: "01012345678"
```

#### Twilio - 글로벌 SMS
```yaml
ums:
  sms:
    provider: TWILIO

    twilio:
      enabled: true
      account-sid: your-account-sid
      auth-token: your-auth-token
      from-number: "+1234567890"
```

#### 다중 제공업체 설정 (자동 전환)
```yaml
ums:
  sms:
    # provider를 비워두면 우선순위에 따라 자동 선택
    enable-fallback: true  # 실패 시 다른 제공업체로 자동 전환

    # 두 제공업체 모두 활성화
    solapi:
      enabled: true
      api-key: your-solapi-key
      api-secret: your-solapi-secret
      from-number: "01012345678"

    twilio:
      enabled: true
      account-sid: your-twilio-sid
      auth-token: your-twilio-token
      from-number: "+1234567890"
```

### Email 제공업체

#### SMTP - 기본 제공업체
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

ums:
  email:
    provider: SMTP  # 또는 비워두면 자동 선택

    smtp:
      enabled: true
      from-email: noreply@yourdomain.com
      from-name: Your Service
```

#### SendGrid - 대용량 이메일
```yaml
ums:
  email:
    provider: SENDGRID

    sendgrid:
      enabled: true
      api-key: your-sendgrid-api-key
      from-email: noreply@yourdomain.com
      from-name: Your Service
```

#### 다중 제공업체 설정 (자동 전환)
```yaml
ums:
  email:
    enable-fallback: true  # 실패 시 자동 전환

    # 두 제공업체 모두 활성화
    smtp:
      enabled: true
      from-email: noreply@yourdomain.com
      from-name: Your Service

    sendgrid:
      enabled: true
      api-key: your-sendgrid-key
      from-email: noreply@yourdomain.com
      from-name: Your Service
```

### 제공업체 우선순위

제공업체는 다음과 같은 우선순위를 가집니다:
- **SMS**: Twilio (10) > Solapi (5)
- **Email**: SendGrid (10) > SMTP (1)

`provider` 필드가 비어있으면 가장 높은 우선순위의 활성화된 제공업체가 자동으로 선택됩니다.

### 3. 데이터베이스 마이그레이션
```bash
# Flyway 마이그레이션 실행
./gradlew flywayMigrate
```

### 4. 애플리케이션 실행
```bash
# 개발 모드로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 5. API 문서 확인
애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## API 사용 예시

### 메시지 전송
```bash
curl -X POST http://localhost:8080/api/v1/messages/send \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: your-tenant-id" \
  -d '{
    "templateCode": "welcome_message",
    "channel": "SMS",
    "to": {
      "phone": "+821012345678"
    },
    "variables": {
      "username": "홍길동",
      "company": "테스트 회사"
    },
    "priority": "HIGH"
  }'
```

### 메시지 상태 조회
```bash
curl -X GET http://localhost:8080/api/v1/messages/{requestId}/status \
  -H "X-Tenant-ID: your-tenant-id"
```

### 템플릿 생성
```bash
curl -X POST http://localhost:8080/api/v1/templates \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: your-tenant-id" \
  -d '{
    "code": "welcome_message",
    "title": "환영합니다!",
    "body": "{{username}}님, {{company}}에 오신 것을 환영합니다!",
    "channel": "SMS",
    "locale": "ko",
    "status": "ACTIVE"
  }'
```

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/mindshift/ums/
│   │   ├── api/                 # REST API Controllers
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   └── exception/      # Exception Handlers
│   │   ├── domain/             # Domain Layer
│   │   │   ├── entity/         # JPA Entities
│   │   │   └── enums/          # Enumerations
│   │   ├── repository/         # Data Access Layer
│   │   ├── service/            # Business Logic
│   │   │   ├── adapter/        # Channel Adapters
│   │   │   ├── security/       # Security Services
│   │   │   └── template/       # Template Services
│   │   ├── config/             # Configuration Classes
│   │   └── UmsApplication.java # Main Application
│   └── resources/
│       ├── application.yml     # Configuration
│       ├── db/migration/       # Database Migrations
│       └── templates/          # Message Templates
└── test/                       # Test Classes
```

## 개발 가이드

### 빌드 및 테스트
```bash
# 프로젝트 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 코드 품질 검사
./gradlew check

# JAR 파일 생성
./gradlew bootJar
```

### 새로운 SMS/Email 제공업체 추가하기

#### 1. SMS 제공업체 추가 예시 (AWS SNS)
```java
@Component
@ConditionalOnProperty(name = "ums.sms.provider", havingValue = "AWS_SNS")
public class AwsSnsSmsProvider implements SmsProvider {

    @Override
    public String getProviderName() {
        return "AWS_SNS";
    }

    @Override
    public boolean isEnabled() {
        // 설정 확인 로직
        return enabled;
    }

    @Override
    public int getPriority() {
        return 8; // Twilio보다 낮고 Solapi보다 높음
    }

    @Override
    public SmsResult sendSms(String phoneNumber, String message,
                            Map<String, Object> metadata) {
        // AWS SNS API 호출 로직
        return SmsResult.success(messageId, metadata);
    }
}
```

#### 2. Email 제공업체 추가 예시 (Mailgun)
```java
@Component
@ConditionalOnProperty(name = "ums.email.provider", havingValue = "MAILGUN")
public class MailgunEmailProvider implements EmailProvider {

    @Override
    public String getProviderName() {
        return "MAILGUN";
    }

    @Override
    public int getPriority() {
        return 5; // SendGrid보다 낮고 SMTP보다 높음
    }

    @Override
    public EmailResult sendEmail(String to, String subject, String body,
                                boolean isHtml, Map<String, Object> metadata) {
        // Mailgun API 호출 로직
        return EmailResult.success(messageId, metadata);
    }
}
```

#### 3. application.yml에 설정 추가
```yaml
ums:
  sms:
    aws-sns:
      enabled: true
      access-key: your-aws-access-key
      secret-key: your-aws-secret-key
      region: ap-northeast-2

  email:
    mailgun:
      enabled: true
      api-key: your-mailgun-api-key
      domain: mg.yourdomain.com
```

### 새로운 채널 어댑터 추가
1. `ChannelAdapter` 인터페이스 구현
2. `@Component` 어노테이션 추가
3. `ChannelType` enum에 새 채널 추가
4. 해당 채널의 구성 정보를 `application.yml`에 추가

### 데이터베이스 마이그레이션 추가
1. `src/main/resources/db/migration/` 디렉토리에 새 SQL 파일 추가
2. 파일명 규칙: `V{version}__{description}.sql`
3. `./gradlew flywayMigrate` 실행

## 모니터링 및 운영

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 메트릭스
```bash
curl http://localhost:8080/actuator/metrics
```

### 로그 레벨 변경
```bash
curl -X POST http://localhost:8080/actuator/loggers/com.mindshift.ums \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

## Docker 실행

### Docker 이미지 빌드
```bash
docker build -t ums-service .
```

### Docker Compose 실행
```bash
# 전체 스택 실행 (PostgreSQL, Redis, Kafka 포함)
docker-compose up -d

# 애플리케이션만 실행
docker-compose up -d ums-service
```

## 배포

### JAR 파일 배포
```bash
# JAR 파일 생성
./gradlew bootJar

# 실행
java -jar build/libs/ums-service-1.0.0.jar --spring.profiles.active=prod
```

### 환경별 설정
- **개발환경**: `application-dev.yml`
- **운영환경**: `application-prod.yml`
- **테스트환경**: `application-test.yml`

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 기여

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 지원

문의사항이나 버그 리포트는 GitHub Issues를 통해 제출해주세요.