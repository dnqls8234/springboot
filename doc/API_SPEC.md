# UMS API Specification

## Overview
통합 메시징 서비스(UMS) REST API 명세서

- **Base URL**: `https://api.ums.mindshift.com`
- **Version**: v1
- **Authentication**: HMAC-SHA256
- **Content-Type**: application/json

## Authentication

### HMAC Signature
```http
Authorization: HMAC-SHA256 <keyId>:<signature>
X-Date: <RFC3339 timestamp>
```

**Signature Generation**:
```
signature = HMAC-SHA256(
    key=secret,
    message=HTTP-Method + "\n" +
            Request-URI + "\n" +
            X-Date + "\n" +
            Body-Hash
)
```

## Endpoints

### 1. Send Message
메시지 발송 요청 (비동기 처리)

**Endpoint**: `POST /v1/messages`

**Headers**:
```http
Authorization: HMAC-SHA256 mindshift:abc123...
X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
X-Date: 2025-09-23T06:00:00Z
Content-Type: application/json
```

**Request Body**:
```json
{
  "channel": "KAKAO_AT",
  "templateCode": "BOOKING_CONFIRM_01",
  "to": {
    "phone": "01012345678",
    "email": "user@example.com",
    "pushToken": null,
    "kakao": {
      "userId": "kakao_user_123"
    }
  },
  "locale": "ko-KR",
  "variables": {
    "name": "김우빈",
    "bookingNumber": "BK202509230001",
    "date": "2025-09-23",
    "time": "14:00",
    "location": "서울시 강남구"
  },
  "attachments": [
    {
      "type": "pdf",
      "url": "https://s3.amazonaws.com/bucket/booking.pdf",
      "filename": "예약확인서.pdf"
    }
  ],
  "routing": {
    "fallback": ["KAKAO_AT", "SMS"],
    "ttlSeconds": 3600,
    "priority": "HIGH"
  },
  "meta": {
    "tenantId": "mindshift",
    "correlationId": "booking-8421",
    "userId": "user123"
  }
}
```

**Response (202 Accepted)**:
```json
{
  "requestId": "req_9d8f7b6a5c4e3d2a1b",
  "status": "ACCEPTED",
  "timestamp": "2025-09-23T06:00:00.123Z"
}
```

**Error Response (400 Bad Request)**:
```json
{
  "error": {
    "code": "INVALID_TEMPLATE",
    "message": "Template BOOKING_CONFIRM_01 not found",
    "details": {
      "field": "templateCode",
      "value": "BOOKING_CONFIRM_01"
    }
  }
}
```

### 2. Get Message Status
메시지 상태 조회

**Endpoint**: `GET /v1/messages/{requestId}`

**Response (200 OK)**:
```json
{
  "requestId": "req_9d8f7b6a5c4e3d2a1b",
  "status": "DELIVERED",
  "channel": "KAKAO_AT",
  "timestamps": {
    "accepted": "2025-09-23T06:00:00.123Z",
    "sent": "2025-09-23T06:00:01.456Z",
    "delivered": "2025-09-23T06:00:02.789Z"
  },
  "retries": 0,
  "error": null,
  "providerMessageId": "kakao_msg_123456",
  "meta": {
    "tenantId": "mindshift",
    "correlationId": "booking-8421"
  }
}
```

### 3. Render Template Preview
템플릿 미리보기 및 검증

**Endpoint**: `POST /v1/templates/render`

**Request Body**:
```json
{
  "templateCode": "BOOKING_CONFIRM_01",
  "locale": "ko-KR",
  "variables": {
    "name": "김우빈",
    "bookingNumber": "BK202509230001",
    "date": "2025-09-23"
  }
}
```

**Response (200 OK)**:
```json
{
  "rendered": {
    "title": "예약 확인",
    "body": "안녕하세요 김우빈님,\n예약번호 BK202509230001 예약이 확정되었습니다.\n예약일: 2025-09-23",
    "buttons": [
      {
        "type": "WEB_URL",
        "name": "예약 상세보기",
        "url": "https://booking.example.com/BK202509230001"
      }
    ]
  },
  "validation": {
    "valid": true,
    "missingVariables": [],
    "warnings": []
  }
}
```

### 4. Webhook Callback
공급사 배달 결과 콜백 수신

**Endpoint**: `POST /webhook/callback`

**Headers**:
```http
X-UMS-Signature: sha256=abc123...
Content-Type: application/json
```

**Request Body**:
```json
{
  "requestId": "req_9d8f7b6a5c4e3d2a1b",
  "event": "DELIVERED",
  "channel": "KAKAO_AT",
  "at": "2025-09-23T06:00:02.789Z",
  "providerMessageId": "kakao_msg_123456",
  "details": {
    "statusCode": "0000",
    "statusMessage": "성공"
  }
}
```

**Response (200 OK)**:
```json
{
  "received": true
}
```

## Data Models

### Channel Types
- `KAKAO_AT`: 카카오 알림톡
- `SMS`: 문자 메시지
- `EMAIL`: 이메일
- `PUSH`: FCM Push Notification

### Message Status
- `ACCEPTED`: 요청 접수됨
- `PENDING`: 처리 대기중
- `SENT`: 발송 완료 (공급사 전달)
- `DELIVERED`: 배달 완료 (수신 확인)
- `FAILED`: 발송 실패
- `EXPIRED`: 만료됨 (TTL 초과)

### Event Types (Webhook)
- `REQUESTED`: 발송 요청됨
- `DISPATCHED`: 공급사로 전송됨
- `DELIVERED`: 배달 완료
- `BOUNCED`: 반송됨
- `READ`: 읽음 (이메일/푸시)
- `CLICKED`: 링크 클릭 (이메일)
- `FAILED`: 실패

### Priority Levels
- `LOW`: 낮음
- `NORMAL`: 보통 (기본값)
- `HIGH`: 높음
- `URGENT`: 긴급

## Error Codes

### HTTP Status Codes
- `202 Accepted`: 요청 접수 성공
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `429 Too Many Requests`: 요청 한도 초과
- `500 Internal Server Error`: 서버 오류

### Application Error Codes
| Code | Description |
|------|-------------|
| `INVALID_CHANNEL` | 지원하지 않는 채널 |
| `INVALID_TEMPLATE` | 템플릿을 찾을 수 없음 |
| `MISSING_VARIABLES` | 필수 변수 누락 |
| `INVALID_RECIPIENT` | 잘못된 수신자 정보 |
| `RATE_LIMIT_EXCEEDED` | 발송 한도 초과 |
| `DUPLICATE_REQUEST` | 중복 요청 (Idempotency) |
| `QUIET_HOURS` | 발송 제한 시간 |
| `RECIPIENT_OPTED_OUT` | 수신 거부 상태 |
| `INVALID_SIGNATURE` | HMAC 서명 검증 실패 |
| `EXPIRED_REQUEST` | 요청 시간 초과 |

## Rate Limits

### Default Limits
- Per tenant: 1000 requests/minute
- Per endpoint: 100 requests/minute
- Per recipient: 10 messages/hour

### Rate Limit Headers
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1695448860
```

## Idempotency

중복 요청 방지를 위해 `X-Idempotency-Key` 헤더 사용
- Key는 UUID v4 형식 권장
- 24시간 동안 캐시됨
- 동일 Key로 재요청 시 기존 응답 반환

## Versioning

API 버전은 URL path에 포함
- Current: `/v1/`
- Deprecated versions: 6개월 유예 기간

## Examples

### cURL Example - Send Message
```bash
curl -X POST https://api.ums.mindshift.com/v1/messages \
  -H "Authorization: HMAC-SHA256 mindshift:abc123..." \
  -H "X-Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000" \
  -H "X-Date: 2025-09-23T06:00:00Z" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "KAKAO_AT",
    "templateCode": "WELCOME_01",
    "to": {"phone": "01012345678"},
    "locale": "ko-KR",
    "variables": {"name": "김우빈"}
  }'
```

### SDK Examples

#### Java/Kotlin
```kotlin
val client = UmsClient(
    apiKey = "mindshift",
    apiSecret = "secret123"
)

val response = client.sendMessage(
    SendMessageRequest(
        channel = Channel.KAKAO_AT,
        templateCode = "WELCOME_01",
        to = Recipient(phone = "01012345678"),
        variables = mapOf("name" to "김우빈")
    )
)
```

#### Python
```python
from ums_sdk import UmsClient

client = UmsClient(
    api_key="mindshift",
    api_secret="secret123"
)

response = client.send_message(
    channel="KAKAO_AT",
    template_code="WELCOME_01",
    to={"phone": "01012345678"},
    variables={"name": "김우빈"}
)
```

## Sandbox Environment

테스트 환경: `https://sandbox.ums.mindshift.com`
- 실제 발송 없이 시뮬레이션
- 웹훅 콜백 자동 생성
- 테스트 수신번호: 010-0000-0000 ~ 010-0000-9999