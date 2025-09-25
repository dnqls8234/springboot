-- UMS Database Schema
-- Version: 1.0.0

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create ENUM types
CREATE TYPE channel_type AS ENUM ('KAKAO_AT', 'SMS', 'EMAIL', 'PUSH');
CREATE TYPE message_status AS ENUM ('PENDING', 'PROCESSING', 'SENT', 'DELIVERED', 'FAILED', 'EXPIRED', 'CANCELLED');
CREATE TYPE message_priority AS ENUM ('LOW', 'NORMAL', 'HIGH', 'URGENT');
CREATE TYPE template_status AS ENUM ('DRAFT', 'ACTIVE', 'INACTIVE', 'DELETED');
CREATE TYPE event_type AS ENUM (
    'REQUESTED', 'VALIDATED', 'RENDERED', 'QUEUED',
    'DISPATCHED', 'SENT', 'DELIVERED', 'BOUNCED',
    'READ', 'CLICKED', 'FAILED', 'EXPIRED', 'RETRIED', 'CANCELLED'
);

-- Templates table
CREATE TABLE templates (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    channel channel_type NOT NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'ko-KR',
    title VARCHAR(500),
    body TEXT NOT NULL,
    variables_schema JSONB NOT NULL DEFAULT '{}',

    -- Channel specific fields
    kakao_template_id VARCHAR(100),
    kakao_template_type VARCHAR(20),
    kakao_buttons JSONB,

    sms_type VARCHAR(10),
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
    status template_status NOT NULL DEFAULT 'ACTIVE',
    version INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Messages table (Outbox Pattern)
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(50) NOT NULL UNIQUE,
    tenant_id VARCHAR(50) NOT NULL,

    -- Template & Channel
    template_id BIGINT REFERENCES templates(id),
    template_code VARCHAR(100) NOT NULL,
    channel channel_type NOT NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'ko-KR',

    -- Recipient
    to_json JSONB NOT NULL,

    -- Rendered content
    rendered_title TEXT,
    rendered_body TEXT NOT NULL,
    rendered_buttons JSONB,

    -- Routing & Policy
    routing JSONB,
    ttl_expires_at TIMESTAMPTZ,
    priority message_priority NOT NULL DEFAULT 'NORMAL',

    -- Status tracking
    status message_status NOT NULL DEFAULT 'PENDING',
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
    failed_at TIMESTAMPTZ
);

-- Message Events table
CREATE TABLE message_events (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL REFERENCES messages(id),
    request_id VARCHAR(50) NOT NULL,

    type event_type NOT NULL,
    channel channel_type,
    provider VARCHAR(50),

    payload JSONB,
    error_code VARCHAR(50),
    error_message TEXT,

    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Recipient Preferences table
CREATE TABLE recipient_prefs (
    tenant_id VARCHAR(50) NOT NULL,
    recipient_key VARCHAR(255) NOT NULL,
    recipient_type VARCHAR(20) NOT NULL,

    -- Channel preferences
    channel_prefs JSONB NOT NULL DEFAULT '{}',

    -- Quiet hours
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR(50) DEFAULT 'Asia/Seoul',

    -- Opt-out status
    opted_out BOOLEAN NOT NULL DEFAULT FALSE,
    opted_out_at TIMESTAMPTZ,
    opted_out_reason TEXT,

    -- Subscription categories
    subscriptions JSONB DEFAULT '{}',

    -- Metadata
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (tenant_id, recipient_key),
    CONSTRAINT chk_recipient_type CHECK (recipient_type IN ('PHONE', 'EMAIL', 'USER_ID'))
);

-- Tenant Configs table
CREATE TABLE tenant_configs (
    tenant_id VARCHAR(50) PRIMARY KEY,
    tenant_name VARCHAR(200) NOT NULL,

    -- API Credentials
    api_key VARCHAR(100) NOT NULL UNIQUE,
    api_secret VARCHAR(500) NOT NULL,
    api_key_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Rate Limits
    rate_limits JSONB NOT NULL DEFAULT '{}',

    -- Allowed channels
    allowed_channels JSONB NOT NULL DEFAULT '["KAKAO_AT","SMS","EMAIL","PUSH"]',

    -- Webhook configuration
    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(255),
    webhook_events JSONB DEFAULT '["DELIVERED","FAILED","BOUNCED"]',

    -- Provider configurations
    provider_configs JSONB DEFAULT '{}',

    -- Billing
    billing_plan VARCHAR(50) DEFAULT 'STANDARD',
    credits_remaining INTEGER,

    -- Features
    features JSONB DEFAULT '{}',

    -- Metadata
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_tenant_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

-- Template Versions table (Audit)
CREATE TABLE template_versions (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL,
    version INTEGER NOT NULL,

    -- Snapshot of template
    code VARCHAR(100) NOT NULL,
    channel channel_type NOT NULL,
    locale VARCHAR(10) NOT NULL,
    title VARCHAR(500),
    body TEXT NOT NULL,
    variables_schema JSONB NOT NULL,

    -- Change tracking
    change_type VARCHAR(20) NOT NULL,
    change_description TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(template_id, version)
);

-- Provider Callbacks table
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
    status VARCHAR(20) NOT NULL,
    message_id BIGINT REFERENCES messages(id),

    -- Response
    response_status INTEGER,
    response_body TEXT,

    -- Timestamps
    received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ
);

-- Create Indexes
CREATE INDEX idx_templates_code ON templates(code);
CREATE INDEX idx_templates_channel_locale ON templates(channel, locale);
CREATE INDEX idx_templates_status ON templates(status) WHERE status = 'ACTIVE';

CREATE INDEX idx_messages_request_id ON messages(request_id);
CREATE INDEX idx_messages_tenant_id ON messages(tenant_id);
CREATE INDEX idx_messages_status_created ON messages(status, created_at);
CREATE INDEX idx_messages_idempotency ON messages(idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE INDEX idx_messages_phone ON messages((to_json->>'phone')) WHERE to_json->>'phone' IS NOT NULL;
CREATE INDEX idx_messages_email ON messages((to_json->>'email')) WHERE to_json->>'email' IS NOT NULL;
CREATE INDEX idx_messages_ttl ON messages(ttl_expires_at) WHERE status IN ('PENDING', 'PROCESSING');

CREATE INDEX idx_events_message_id ON message_events(message_id);
CREATE INDEX idx_events_request_id ON message_events(request_id);
CREATE INDEX idx_events_type_occurred ON message_events(type, occurred_at);

CREATE INDEX idx_prefs_recipient ON recipient_prefs(recipient_key);
CREATE INDEX idx_prefs_opted_out ON recipient_prefs(opted_out) WHERE opted_out = TRUE;

CREATE INDEX idx_tenant_api_key ON tenant_configs(api_key) WHERE status = 'ACTIVE';

CREATE INDEX idx_template_versions ON template_versions(template_id, version DESC);

CREATE INDEX idx_callbacks_provider_msg_id ON provider_callbacks(provider, provider_message_id);
CREATE INDEX idx_callbacks_received ON provider_callbacks(received_at DESC);

-- Insert default tenant for development
INSERT INTO tenant_configs (
    tenant_id,
    tenant_name,
    api_key,
    api_secret,
    rate_limits,
    features
) VALUES (
    'mindshift',
    'MindShift Development',
    'mindshift_dev_key',
    '$2a$10$dummyHashedSecretForDevelopment',
    '{"per_minute": 100, "per_hour": 1000, "per_day": 10000}',
    '{"sandbox": true, "priority_queue": false}'
);

-- Insert sample templates
INSERT INTO templates (code, channel, locale, title, body, variables_schema, status)
VALUES
    ('WELCOME_01', 'SMS', 'ko-KR', NULL,
     '{{name}}님 환영합니다! MindShift 가입을 축하드립니다.',
     '{"name": {"required": true, "type": "string"}}',
     'ACTIVE'),

    ('ORDER_CONFIRM_01', 'KAKAO_AT', 'ko-KR', '주문 확인',
     '{{name}}님의 주문이 확인되었습니다.\n주문번호: {{orderNumber}}\n금액: {{amount}}원',
     '{"name": {"required": true}, "orderNumber": {"required": true}, "amount": {"required": true}}',
     'ACTIVE'),

    ('PASSWORD_RESET_01', 'EMAIL', 'ko-KR', '비밀번호 재설정',
     '<p>{{name}}님,</p><p>비밀번호 재설정 링크입니다: <a href="{{resetLink}}">재설정하기</a></p>',
     '{"name": {"required": true}, "resetLink": {"required": true}}',
     'ACTIVE');