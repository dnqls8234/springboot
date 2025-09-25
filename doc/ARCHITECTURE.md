# UMS Architecture Design

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        External Clients                     │
│         (Web Apps, Mobile Apps, Internal Services)          │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTPS
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway                            │
│                  (Load Balancer + WAF)                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    UMS Core Service                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                  REST API Layer                      │   │
│  │  - Message Controller                                │   │
│  │  - Template Controller                               │   │
│  │  - Webhook Controller                                │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                 Business Logic Layer                 │   │
│  │  - Message Service      - Template Service           │   │
│  │  - Policy Service       - Outbox Service             │   │
│  │  - Routing Service      - Retry Service              │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Data Persistence Layer                  │   │
│  │  - JPA Repositories                                  │   │
│  │  - Redis Cache                                       │   │
│  │  - Transaction Management                            │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                     │                    │
        ┌────────────┴──────────┐  ┌──────┴─────────┐
        │                       │  │                │
        ▼                       ▼  ▼                ▼
┌──────────────┐       ┌──────────────┐    ┌──────────────┐
│  PostgreSQL  │       │    Redis     │    │    Kafka     │
│              │       │              │    │              │
│ - Templates  │       │ - Cache      │    │ - Events     │
│ - Messages   │       │ - Rate Limit │    │ - DLQ        │
│ - Events     │       │ - Idempotency│    │              │
└──────────────┘       └──────────────┘    └──────────────┘
                                                   │
                                                   │ Async
                                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    Channel Workers                          │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────┐ │
│  │   Kakao    │  │    SMS     │  │   Email    │  │  FCM   │ │
│  │  Adapter   │  │  Adapter   │  │  Adapter   │  │Adapter │ │
│  └────────────┘  └────────────┘  └────────────┘  └────────┘ │
└─────────────────────────────────────────────────────────────┘
         │                │                │            │
         ▼                ▼                ▼            ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────┐
│ Kakao API    │  │ SMS Gateway  │  │ Email Service│  │ FCM  │
└──────────────┘  └──────────────┘  └──────────────┘  └──────┘
```

## Component Architecture

### 1. API Gateway Layer
- **Purpose**: Entry point for all external requests
- **Responsibilities**:
  - SSL/TLS termination
  - Load balancing
  - Rate limiting (global)
  - WAF (Web Application Firewall)
  - Request routing

### 2. Core Service Layer

#### REST API Controllers
```kotlin
@RestController
@RequestMapping("/v1/messages")
class MessageController {
    @PostMapping
    fun sendMessage(): ResponseEntity<AcceptResponse> // 202 Accepted

    @GetMapping("/{requestId}")
    fun getStatus(): MessageStatusResponse
}
```

#### Service Layer Components
- **MessageService**: Core message processing logic
- **TemplateService**: Template rendering and validation
- **PolicyService**: Business rules and policies
- **OutboxService**: Transactional outbox pattern
- **RoutingService**: Channel selection and fallback
- **RetryService**: Exponential backoff retry logic

#### Security Components
- **HmacAuthFilter**: HMAC-SHA256 authentication
- **IdempotencyFilter**: Duplicate request prevention
- **RateLimitFilter**: Per-tenant rate limiting

### 3. Data Layer

#### PostgreSQL Schema
```sql
-- Core Tables
templates           -- Message templates
messages           -- Outbox pattern (message queue)
message_events     -- Event sourcing
recipient_prefs    -- User preferences

-- Indexes
idx_messages_status_created
idx_messages_request_id
idx_events_message_id
```

#### Redis Usage
- **Idempotency Keys**: 24-hour TTL
- **Rate Limit Counters**: Token bucket algorithm
- **Template Cache**: Frequently used templates
- **Session Data**: Temporary processing state

#### Kafka Topics
```
ums.message.requested.v1    -- New message requests
ums.message.dispatched.v1   -- Sent to provider
ums.message.delivered.v1    -- Delivery confirmed
ums.message.failed.v1       -- Processing failed
ums.message.dlq             -- Dead letter queue
```

### 4. Channel Adapters

#### Adapter Interface
```kotlin
interface ChannelAdapter {
    fun supports(channel: ChannelType): Boolean
    fun send(request: DispatchRequest): DispatchResult
    fun handleCallback(payload: CallbackPayload)
}
```

#### Implementation Strategy
- **Kakao Alimtalk**: REST API with template pre-registration
- **SMS**: Multiple vendor support (KT, SKT, LG U+)
- **Email**: SES/SendGrid with bounce handling
- **FCM**: Firebase Admin SDK for push notifications

## Data Flow

### 1. Message Send Flow
```
Client Request
    │
    ├─> API Gateway
    │
    ├─> HMAC Validation
    │
    ├─> Idempotency Check
    │
    ├─> Rate Limit Check
    │
    ├─> Template Loading & Validation
    │
    ├─> Variable Rendering
    │
    ├─> Policy Checks (Quiet Hours, Opt-out)
    │
    ├─> Save to Outbox (PENDING)
    │
    ├─> Publish Event to Kafka
    │
    └─> Return 202 Accepted

Async Processing:
    │
    ├─> Worker Consumes Event
    │
    ├─> Select Channel Adapter
    │
    ├─> Call Provider API
    │
    ├─> Update Status (SENT/FAILED)
    │
    ├─> Handle Retry if Failed
    │
    └─> Process Fallback if Needed
```

### 2. Callback Flow
```
Provider Callback
    │
    ├─> Webhook Endpoint
    │
    ├─> Signature Validation
    │
    ├─> Find Message by Provider ID
    │
    ├─> Update Status (DELIVERED/BOUNCED)
    │
    ├─> Publish Status Event
    │
    └─> Return 200 OK
```

## Scalability Design

### Horizontal Scaling
- **API Servers**: Stateless, behind load balancer
- **Workers**: Multiple instances per channel
- **Database**: Read replicas for queries
- **Cache**: Redis cluster for high availability

### Performance Optimizations
- **Connection Pooling**: Database and HTTP clients
- **Batch Processing**: Bulk message sending
- **Async Processing**: Non-blocking I/O
- **Caching Strategy**: Template and configuration cache

### Capacity Planning
- **Target**: 1000 messages/second
- **Database**: 100 connections pool
- **Redis**: 10GB memory allocation
- **Kafka**: 3 brokers, 3 replicas

## Security Architecture

### Authentication & Authorization
```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   Client    │────>│ HMAC Filter  │────>│  Service    │
└─────────────┘     └──────────────┘     └─────────────┘
                           │
                    ┌──────▼──────┐
                    │ Secret Store │
                    └──────────────┘
```

### Data Protection
- **In Transit**: TLS 1.3 minimum
- **At Rest**: AES-256 encryption
- **PII Handling**: Field-level encryption
- **Logging**: Sensitive data masking

### Compliance
- **GDPR**: Data retention and right to be forgotten
- **KISA**: Korean telecommunications regulations
- **PCI DSS**: If payment notifications

## Monitoring & Observability

### Metrics (Micrometer + Prometheus)
```
ums_messages_sent_total{channel, status}
ums_messages_latency_seconds{channel, percentile}
ums_template_render_duration_seconds
ums_provider_api_calls_total{provider, status}
ums_rate_limit_exceeded_total{tenant}
```

### Logging (SLF4J + Logback)
```
- Request ID tracing
- Channel adapter logs
- Provider API responses
- Error stack traces
- Performance logs
```

### Alerting Rules
- Message failure rate > 5%
- Provider API latency > 2s
- Database connection pool exhausted
- Kafka lag > 1000 messages
- Redis memory > 80%

## Deployment Architecture

### Container Structure
```
ums-api:latest          -- API server image
ums-worker-kakao:latest -- Kakao worker
ums-worker-sms:latest   -- SMS worker
ums-worker-email:latest -- Email worker
ums-worker-fcm:latest   -- FCM worker
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ums-api
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
  template:
    spec:
      containers:
      - name: ums-api
        image: ums-api:latest
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Infrastructure as Code
- **Terraform**: AWS/GCP resource provisioning
- **Helm**: Kubernetes application deployment
- **Ansible**: Configuration management

## Disaster Recovery

### Backup Strategy
- **Database**: Daily snapshots, point-in-time recovery
- **Configuration**: Git versioned
- **Secrets**: Encrypted backup in separate region

### High Availability
- **Multi-AZ Deployment**: Across availability zones
- **Database Replication**: Master-slave setup
- **Cache Replication**: Redis sentinel
- **Message Queue**: Kafka cluster with replication

### RTO/RPO Targets
- **RTO (Recovery Time Objective)**: < 1 hour
- **RPO (Recovery Point Objective)**: < 5 minutes

## Development Guidelines

### Code Organization
```
src/main/kotlin/com/mindshift/ums/
├── api/           # REST controllers
├── service/       # Business logic
├── adapter/       # Channel adapters
├── domain/        # Domain models
├── repository/    # Data access
├── config/        # Configuration
├── security/      # Security components
├── event/         # Event handling
└── util/          # Utilities
```

### Testing Strategy
- **Unit Tests**: 80% coverage minimum
- **Integration Tests**: Key workflows
- **Contract Tests**: Provider APIs
- **Performance Tests**: Load testing

### CI/CD Pipeline
```
1. Code Commit
2. Build & Unit Tests
3. Integration Tests
4. Security Scan
5. Docker Build
6. Deploy to Staging
7. Smoke Tests
8. Deploy to Production
9. Health Checks
```