# UMS Deployment Guide

## Prerequisites

### System Requirements
- Java 21 or higher
- PostgreSQL 14+
- Redis 6+
- Apache Kafka 3.x
- Docker & Docker Compose (optional)

### External Service Accounts
- Kakao Business API account
- SMS provider account (KT/SKT/LG U+)
- Email service (AWS SES/SendGrid)
- Firebase project for FCM

## Environment Configuration

### 1. Application Properties

#### application.yml (Default)
```yaml
spring:
  application:
    name: ums-service

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:ums_db}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    lettuce:
      pool:
        max-active: 10
        max-idle: 5

  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9093}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ums-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

server:
  port: ${SERVER_PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### application-dev.yml (Development)
```yaml
spring:
  profiles: dev

  datasource:
    url: jdbc:postgresql://dev-db.mindshift.com:5432/ums_dev

  redis:
    host: dev-redis.mindshift.com

  kafka:
    bootstrap-servers: dev-kafka.mindshift.com:9093

logging:
  level:
    com.mindshift.ums: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG

ums:
  sandbox-mode: true
  webhook:
    retry-enabled: false
```

#### application-prod.yml (Production)
```yaml
spring:
  profiles: prod

  datasource:
    url: jdbc:postgresql://prod-db.mindshift.com:5432/ums_prod
    hikari:
      maximum-pool-size: 50

  redis:
    host: prod-redis.mindshift.com
    cluster:
      nodes:
        - prod-redis-01.mindshift.com:6379
        - prod-redis-02.mindshift.com:6379
        - prod-redis-03.mindshift.com:6379

  kafka:
    bootstrap-servers:
      - prod-kafka-01.mindshift.com:9093
      - prod-kafka-02.mindshift.com:9093
      - prod-kafka-03.mindshift.com:9093

logging:
  level:
    com.mindshift.ums: INFO

ums:
  sandbox-mode: false
  rate-limit:
    enabled: true
    default-limit: 1000
```

### 2. Provider Configuration

#### Kakao Alimtalk
```yaml
ums:
  providers:
    kakao:
      channel-id: ${KAKAO_CHANNEL_ID}
      api-key: ${KAKAO_API_KEY}
      sender-key: ${KAKAO_SENDER_KEY}
      api-url: https://alimtalk-api.bizmsg.kr/v2
      timeout: 10000
```

#### SMS
```yaml
ums:
  providers:
    sms:
      vendor: ${SMS_VENDOR:KT}  # KT, SKT, LGU
      api-url: ${SMS_API_URL}
      api-key: ${SMS_API_KEY}
      api-secret: ${SMS_API_SECRET}
      sender-numbers:
        - "1588-0000"
        - "02-123-4567"
```

#### Email
```yaml
ums:
  providers:
    email:
      type: ${EMAIL_TYPE:SES}  # SES, SENDGRID, SMTP

      # AWS SES
      ses:
        region: ap-northeast-2
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}

      # SMTP
      smtp:
        host: ${SMTP_HOST}
        port: ${SMTP_PORT:587}
        username: ${SMTP_USERNAME}
        password: ${SMTP_PASSWORD}

      from:
        address: noreply@mindshift.com
        name: MindShift
```

#### FCM
```yaml
ums:
  providers:
    fcm:
      service-account-file: ${FCM_SERVICE_ACCOUNT_FILE:/keys/fcm-service-account.json}
      database-url: ${FCM_DATABASE_URL}
```

### 3. Security Configuration

```yaml
ums:
  security:
    hmac:
      keys:
        - key-id: mindshift
          secret: ${HMAC_SECRET_MINDSHIFT}
        - key-id: partner-01
          secret: ${HMAC_SECRET_PARTNER_01}

      time-window: 300  # 5 minutes

    encryption:
      enabled: true
      algorithm: AES
      key: ${ENCRYPTION_KEY}
```

## Deployment Methods

### 1. JAR Deployment

#### Build
```bash
# Build the application
./gradlew clean build

# Run tests
./gradlew test

# Create executable JAR
./gradlew bootJar
```

#### Run
```bash
# Development
java -jar -Dspring.profiles.active=dev build/libs/ums-service-1.0.0.jar

# Production with environment variables
export DB_PASSWORD=secret123
export REDIS_PASSWORD=redis456
export KAFKA_SERVERS=kafka1:9093,kafka2:9093
java -jar -Dspring.profiles.active=prod \
  -Xms1g -Xmx2g \
  build/libs/ums-service-1.0.0.jar
```

### 2. Docker Deployment

#### Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --chown=spring:spring build/libs/ums-service-*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

#### docker-compose.yml
```yaml
version: '3.8'

services:
  ums-api:
    image: mindshift/ums-service:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_HOST=postgres
      - DB_PASSWORD=postgres123
      - REDIS_HOST=redis
      - KAFKA_SERVERS=kafka:9092
    depends_on:
      - postgres
      - redis
      - kafka
    networks:
      - ums-network

  postgres:
    image: postgres:14
    environment:
      - POSTGRES_DB=ums_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres123
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    networks:
      - ums-network

  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"
    networks:
      - ums-network

  kafka:
    image: confluentinc/cp-kafka:latest
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    networks:
      - ums-network

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181
      - ZOOKEEPER_TICK_TIME=2000
    ports:
      - "2181:2181"
    networks:
      - ums-network

networks:
  ums-network:
    driver: bridge

volumes:
  postgres-data:
  redis-data:
```

#### Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f ums-api

# Stop services
docker-compose down

# Remove volumes
docker-compose down -v
```

### 3. Kubernetes Deployment

#### deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ums-api
  namespace: ums
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ums-api
  template:
    metadata:
      labels:
        app: ums-api
    spec:
      containers:
      - name: ums-api
        image: mindshift/ums-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ums-secrets
              key: db-password
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ums-secrets
              key: redis-password
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

#### service.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: ums-api-service
  namespace: ums
spec:
  selector:
    app: ums-api
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
  type: LoadBalancer
```

#### configmap.yaml
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ums-config
  namespace: ums
data:
  application.yaml: |
    spring:
      profiles: prod
      datasource:
        url: jdbc:postgresql://postgres-service:5432/ums_db
      redis:
        host: redis-service
      kafka:
        bootstrap-servers: kafka-service:9092
```

#### Deploy to Kubernetes
```bash
# Create namespace
kubectl create namespace ums

# Create secrets
kubectl create secret generic ums-secrets \
  --from-literal=db-password=secret123 \
  --from-literal=redis-password=redis456 \
  -n ums

# Apply configurations
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Check status
kubectl get pods -n ums
kubectl get svc -n ums

# View logs
kubectl logs -f deployment/ums-api -n ums
```

## Database Setup

### 1. Create Database
```sql
-- Create database
CREATE DATABASE ums_db;

-- Create user
CREATE USER ums_user WITH PASSWORD 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ums_db TO ums_user;
```

### 2. Run Migrations
```bash
# Using Flyway
./gradlew flywayMigrate

# Or manually
psql -h localhost -U postgres -d ums_db -f migrations/V1__Initial_Schema.sql
```

## Monitoring Setup

### 1. Prometheus Configuration
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'ums-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['ums-api:8080']
```

### 2. Grafana Dashboard
Import dashboard JSON from `monitoring/grafana-dashboard.json`

### 3. Health Checks
```bash
# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Full health check
curl http://localhost:8080/actuator/health
```

## Operational Tasks

### 1. Backup
```bash
# Database backup
pg_dump -h localhost -U postgres -d ums_db > backup_$(date +%Y%m%d).sql

# Redis backup
redis-cli --rdb /backup/redis_$(date +%Y%m%d).rdb
```

### 2. Log Management
```bash
# Application logs location
/var/log/ums/application.log

# Rotate logs
logrotate -f /etc/logrotate.d/ums

# Stream logs
tail -f /var/log/ums/application.log | grep ERROR
```

### 3. Scaling
```bash
# Kubernetes horizontal scaling
kubectl scale deployment ums-api --replicas=5 -n ums

# Auto-scaling
kubectl autoscale deployment ums-api --cpu-percent=70 --min=3 --max=10 -n ums
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check connectivity
telnet postgres-host 5432

# Verify credentials
psql -h postgres-host -U ums_user -d ums_db

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

#### 2. Kafka Connection Issues
```bash
# List topics
kafka-topics --list --bootstrap-server localhost:9092

# Check consumer group
kafka-consumer-groups --describe --group ums-service --bootstrap-server localhost:9092

# Reset offset
kafka-consumer-groups --reset-offsets --group ums-service --topic ums.message.requested.v1 --to-earliest --execute --bootstrap-server localhost:9092
```

#### 3. Redis Issues
```bash
# Check Redis
redis-cli ping

# Monitor commands
redis-cli monitor

# Flush cache (careful!)
redis-cli FLUSHDB
```

## Performance Tuning

### JVM Options
```bash
-Xms2g
-Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/ums/
-Dspring.profiles.active=prod
```

### Database Tuning
```sql
-- PostgreSQL configuration
max_connections = 200
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
```

### Redis Tuning
```bash
# redis.conf
maxmemory 2gb
maxmemory-policy allkeys-lru
```

## Security Checklist

- [ ] SSL/TLS enabled for all endpoints
- [ ] Database credentials encrypted
- [ ] API keys rotated regularly
- [ ] WAF configured
- [ ] Rate limiting enabled
- [ ] Audit logging enabled
- [ ] Backup encryption enabled
- [ ] Network policies configured
- [ ] Security scanning in CI/CD