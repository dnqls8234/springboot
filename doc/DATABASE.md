# UMS Database Schema Design

## Overview
PostgreSQL 기반 데이터베이스 스키마 설계
- JPA/Hibernate 엔티티 매핑
- Flyway 마이그레이션 관리
- 인덱스 최적화

## Entity Relationship Diagram

```
┌──────────────────┐        ┌──────────────────┐
│    templates     │        │     messages     │
├──────────────────┤        ├──────────────────┤
│ id (PK)          │        │ id (PK)          │
│ code (UNIQUE)    │        │ request_id (UNQ) │
│ channel          │◄───────┤ template_id (FK) │
│ locale           │        │ tenant_id        │
│ title            │        │ channel          │
│ body             │        │ to_json          │
│ variables_schema │        │ status           │
│ status           │        │ error_code       │
│ created_at       │        │ created_at       │
│ updated_at       │        │ updated_at       │
└──────────────────┘        └──────────────────┘
                                     │
                                     │ 1:N
                                     ▼
                            ┌──────────────────┐
                            │  message_events  │
                            ├──────────────────┤
                            │ id (PK)          │
                            │ message_id (FK)  │
                            │ type             │
                            │ payload          │
                            │ occurred_at      │
                            └──────────────────┘

┌──────────────────┐        ┌──────────────────┐
│ recipient_prefs  │        │ tenant_configs   │
├──────────────────┤        ├──────────────────┤
│ tenant_id (PK)   │        │ tenant_id (PK)   │
│ recipient_key(PK)│        │ api_key          │
│ channel_prefs    │        │ api_secret       │
│ quiet_hours      │        │ rate_limits      │
│ opted_out        │        │ allowed_channels │
│ created_at       │        │ webhook_url      │
│ updated_at       │        │ created_at       │
└──────────────────┘        └──────────────────┘
```

## Table Definitions

### 1. templates
메시지 템플릿 관리

```sql
CREATE TABLE templates (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    channel VARCHAR(20) NOT NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'ko-KR',
    title VARCHAR(500),
    body TEXT NOT NULL,
    variables_schema JSONB NOT NULL DEFAULT '{}',

    -- Channel specific fields
    kakao_template_id VARCHAR(100),
    kakao_template_type VARCHAR(20), -- BA (Basic), EX (Extra), AD (Ad)
    kakao_buttons JSONB,

    sms_type VARCHAR(10), -- SMS, LMS, MMS
    sms_sender_no VARCHAR(20),

    email_subject_template TEXT,
    email_from_name VARCHAR(100),
    email_from_address VARCHAR(255),
    email_reply_to VARCHAR(255),

    fcm_title_template TEXT,
    fcm_icon VARCHAR(255),
    fcm_color VARCHAR(7),
    fcm_sound VARCHAR(50),

    -- Metadata
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_channel CHECK (channel IN ('KAKAO_AT', 'SMS', 'EMAIL', 'PUSH')),
    CONSTRAINT chk_status CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE', 'DELETED'))
);

CREATE INDEX idx_templates_code ON templates(code);
CREATE INDEX idx_templates_channel_locale ON templates(channel, locale);
CREATE INDEX idx_templates_status ON templates(status) WHERE status = 'ACTIVE';
```

### 2. messages
발송 메시지 (Outbox Pattern)

```sql
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(50) NOT NULL UNIQUE,
    tenant_id VARCHAR(50) NOT NULL,

    -- Template & Channel
    template_id BIGINT REFERENCES templates(id),
    template_code VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'ko-KR',

    -- Recipient
    to_json JSONB NOT NULL,
    -- Format: {"phone":"01012345678", "email":"user@example.com", "pushToken":"...", "kakao":{"userId":"..."}}

    -- Rendered content
    rendered_title TEXT,
    rendered_body TEXT NOT NULL,
    rendered_buttons JSONB,

    -- Routing & Policy
    routing JSONB,
    -- Format: {"fallback":["KAKAO_AT","SMS"], "ttlSeconds":3600, "priority":"HIGH"}
    ttl_expires_at TIMESTAMPTZ,
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',

    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    provider_message_id VARCHAR(255),
    provider_status_code VARCHAR(50),
    retries INTEGER NOT NULL DEFAULT 0,
    last_retry_at TIMESTAMPTZ,

    -- Error handling
    error_code VARCHAR(50),
    error_message TEXT,
    error_details JSONB,

    -- Attachments
    attachments JSONB,

    -- Metadata
    meta JSONB,
    idempotency_key VARCHAR(100),
    correlation_id VARCHAR(100),

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,

    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'DELIVERED', 'FAILED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX idx_messages_request_id ON messages(request_id);
CREATE INDEX idx_messages_tenant_id ON messages(tenant_id);
CREATE INDEX idx_messages_status_created ON messages(status, created_at);
CREATE INDEX idx_messages_idempotency ON messages(idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE INDEX idx_messages_phone ON messages((to_json->>'phone')) WHERE to_json->>'phone' IS NOT NULL;
CREATE INDEX idx_messages_email ON messages((to_json->>'email')) WHERE to_json->>'email' IS NOT NULL;
CREATE INDEX idx_messages_ttl ON messages(ttl_expires_at) WHERE status IN ('PENDING', 'PROCESSING');
```

### 3. message_events
이벤트 소싱 로그

```sql
CREATE TABLE message_events (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id),
    request_id VARCHAR(50) NOT NULL,

    type VARCHAR(30) NOT NULL,
    -- REQUESTED, VALIDATED, RENDERED, QUEUED, DISPATCHED, SENT, DELIVERED, BOUNCED, FAILED, EXPIRED, RETRIED

    channel VARCHAR(20),
    provider VARCHAR(50),

    payload JSONB,
    error_code VARCHAR(50),
    error_message TEXT,

    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_event_type CHECK (type IN (
        'REQUESTED', 'VALIDATED', 'RENDERED', 'QUEUED',
        'DISPATCHED', 'SENT', 'DELIVERED', 'BOUNCED',
        'READ', 'CLICKED', 'FAILED', 'EXPIRED', 'RETRIED', 'CANCELLED'
    ))
);

CREATE INDEX idx_events_message_id ON message_events(message_id);
CREATE INDEX idx_events_request_id ON message_events(request_id);
CREATE INDEX idx_events_type_occurred ON message_events(type, occurred_at);
```

### 4. recipient_prefs
수신자 설정 및 수신거부

```sql
CREATE TABLE recipient_prefs (
    tenant_id VARCHAR(50) NOT NULL,
    recipient_key VARCHAR(255) NOT NULL,
    recipient_type VARCHAR(20) NOT NULL, -- PHONE, EMAIL, USER_ID

    -- Channel preferences
    channel_prefs JSONB NOT NULL DEFAULT '{}',
    -- Format: {"KAKAO_AT":true, "SMS":true, "EMAIL":false, "PUSH":true}

    -- Quiet hours (PostgreSQL range type)
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50) DEFAULT 'Asia/Seoul',

    -- Opt-out status
    opted_out BOOLEAN NOT NULL DEFAULT FALSE,
    opted_out_at TIMESTAMPTZ,
    opted_out_reason TEXT,

    -- Subscription categories
    subscriptions JSONB DEFAULT '{}',
    -- Format: {"marketing":true, "transaction":true, "newsletter":false}

    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (tenant_id, recipient_key),
    CONSTRAINT chk_recipient_type CHECK (recipient_type IN ('PHONE', 'EMAIL', 'USER_ID'))
);

CREATE INDEX idx_prefs_recipient ON recipient_prefs(recipient_key);
CREATE INDEX idx_prefs_opted_out ON recipient_prefs(opted_out) WHERE opted_out = TRUE;
```

### 5. tenant_configs
테넌트별 설정

```sql
CREATE TABLE tenant_configs (
    tenant_id VARCHAR(50) PRIMARY KEY,
    tenant_name VARCHAR(200) NOT NULL,

    -- API Credentials
    api_key VARCHAR(100) NOT NULL UNIQUE,
    api_secret VARCHAR(500) NOT NULL, -- encrypted
    api_key_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Rate Limits
    rate_limits JSONB NOT NULL DEFAULT '{}',
    -- Format: {"per_minute":1000, "per_hour":10000, "per_day":100000}

    -- Allowed channels
    allowed_channels JSONB NOT NULL DEFAULT '["KAKAO_AT","SMS","EMAIL","PUSH"]',

    -- Webhook configuration
    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(255),
    webhook_events JSONB DEFAULT '["DELIVERED","FAILED","BOUNCED"]',

    -- Provider configurations
    provider_configs JSONB DEFAULT '{}',
    -- Format: {"kakao":{"channelId":"..."}, "sms":{"vendor":"KT"}}

    -- Billing
    billing_plan VARCHAR(50) DEFAULT 'STANDARD',
    credits_remaining INTEGER,

    -- Features
    features JSONB DEFAULT '{}',
    -- Format: {"sandbox":false, "priority_queue":true}

    -- Metadata
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tenant_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_tenant_api_key ON tenant_configs(api_key) WHERE status = 'ACTIVE';
```

### 6. template_versions
템플릿 버전 관리 (Audit)

```sql
CREATE TABLE template_versions (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version INTEGER NOT NULL,

    -- Snapshot of template at this version
    code VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    locale VARCHAR(10) NOT NULL,
    title VARCHAR(500),
    body TEXT NOT NULL,
    variables_schema JSONB NOT NULL,

    -- Change tracking
    change_type VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE
    change_description TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(template_id, version)
);

CREATE INDEX idx_template_versions ON template_versions(template_id, version DESC);
```

### 7. provider_callbacks
공급사 콜백 로그

```sql
CREATE TABLE provider_callbacks (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,
    provider_message_id VARCHAR(255),

    -- HTTP Request Info
    method VARCHAR(10) NOT NULL,
    url TEXT NOT NULL,
    headers JSONB,
    body TEXT,

    -- Processing
    status VARCHAR(20) NOT NULL, -- RECEIVED, PROCESSED, FAILED, IGNORED
    message_id BIGINT REFERENCES messages(id),

    -- Response
    response_status INTEGER,
    response_body TEXT,

    -- Timestamps
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ
);

CREATE INDEX idx_callbacks_provider_msg_id ON provider_callbacks(provider, provider_message_id);
CREATE INDEX idx_callbacks_received ON provider_callbacks(received_at DESC);
```

## JPA Entity Examples

### Template Entity
```kotlin
@Entity
@Table(name = "templates")
class Template(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val code: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val channel: ChannelType,

    @Column(nullable = false)
    val locale: String = "ko-KR",

    val title: String? = null,

    @Column(columnDefinition = "TEXT", nullable = false)
    val body: String,

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    val variablesSchema: Map<String, Any> = emptyMap(),

    @Enumerated(EnumType.STRING)
    val status: TemplateStatus = TemplateStatus.ACTIVE,

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
```

### Message Entity
```kotlin
@Entity
@Table(name = "messages")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val requestId: String,

    @Column(nullable = false)
    val tenantId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    val template: Template? = null,

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", nullable = false)
    val toJson: Map<String, Any>,

    @Enumerated(EnumType.STRING)
    var status: MessageStatus = MessageStatus.PENDING,

    var retries: Int = 0,

    var errorCode: String? = null,
    var errorMessage: String? = null,

    @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL])
    val events: MutableList<MessageEvent> = mutableListOf()
)
```

## Migration Scripts

### V1__Initial_Schema.sql
```sql
-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create all tables as defined above
-- ...

-- Insert default data
INSERT INTO tenant_configs (tenant_id, tenant_name, api_key, api_secret)
VALUES ('system', 'System', 'system_key', 'encrypted_secret');
```

### V2__Add_Indexes.sql
```sql
-- Performance optimization indexes
CREATE INDEX CONCURRENTLY idx_messages_pending
ON messages(created_at)
WHERE status = 'PENDING';

CREATE INDEX CONCURRENTLY idx_messages_retry
ON messages(last_retry_at)
WHERE status = 'FAILED' AND retries < 3;
```

## Performance Considerations

### Partitioning Strategy
```sql
-- Partition messages table by month
CREATE TABLE messages_2025_01 PARTITION OF messages
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- Partition events table by month
CREATE TABLE message_events_2025_01 PARTITION OF message_events
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

### Index Optimization
- Covering indexes for frequent queries
- Partial indexes for status filtering
- JSONB GIN indexes for JSON queries
- BRIN indexes for time-series data

### Query Optimization Tips
```sql
-- Use prepared statements
PREPARE get_pending AS
SELECT * FROM messages
WHERE status = 'PENDING'
AND created_at < NOW() - INTERVAL '5 minutes'
LIMIT $1;

-- Batch updates
UPDATE messages
SET status = 'PROCESSING'
WHERE id = ANY($1::bigint[]);
```

## Maintenance

### Regular Tasks
```sql
-- Vacuum and analyze
VACUUM ANALYZE messages;

-- Reindex periodically
REINDEX INDEX CONCURRENTLY idx_messages_status_created;

-- Archive old data
INSERT INTO messages_archive
SELECT * FROM messages
WHERE created_at < NOW() - INTERVAL '90 days';

DELETE FROM messages
WHERE created_at < NOW() - INTERVAL '90 days';
```

### Monitoring Queries
```sql
-- Check table sizes
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Check slow queries
SELECT
    query,
    calls,
    mean_exec_time,
    total_exec_time
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;
```