# Configuration Guide - Traffic Light Controller API

## Table of Contents

1. [Configuration Overview](#configuration-overview)
2. [Application Properties](#application-properties)
3. [Database Configuration](#database-configuration)
4. [Kafka Configuration](#kafka-configuration)
5. [Thread Pool Configuration](#thread-pool-configuration)
6. [Resilience4j Configuration](#resilience4j-configuration)
7. [Logging Configuration](#logging-configuration)
8. [Security Configuration](#security-configuration)
9. [Monitoring Configuration](#monitoring-configuration)
10. [Environment-Specific Configuration](#environment-specific-configuration)

## Configuration Overview

The application uses Spring Boot's configuration system with support for:

- **YAML files**: `application.yml`, `application-{profile}.yml`
- **Environment variables**: Override any property
- **Command-line arguments**: Highest priority
- **External configuration**: Load from external locations

### Configuration Precedence (highest to lowest)

1. Command-line arguments
2. Environment variables
3. Profile-specific properties (`application-prod.yml`)
4. Default properties (`application.yml`)

## Application Properties

### Core Application Settings

```yaml
spring:
  application:
    name: traffic-light-service
  
  profiles:
    active: dev  # Options: dev, test, prod
```

### Server Configuration

```yaml
server:
  port: 9900  # Application port
  servlet:
    context-path: /  # Base path for all endpoints
  compression:
    enabled: true  # Enable response compression
    mime-types: application/json,application/xml,text/html
  error:
    include-message: always  # Include error messages in responses
    include-binding-errors: always
    include-stacktrace: on_param  # Include stacktrace when ?trace=true
```

**Environment Variables**:
```bash
SERVER_PORT=9900
SERVER_SERVLET_CONTEXT_PATH=/api
```

## Database Configuration

### Connection Settings

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/traffic_light_db
    username: traffic_user
    password: secure_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**Environment Variables**:
```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=traffic_light_db
DB_USERNAME=traffic_user
DB_PASSWORD=secure_password

# Constructed URL
SPRING_DATASOURCE_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
```

### HikariCP Connection Pool

```yaml
spring:
  datasource:
    hikari:
      pool-name: traffic-light-pool
      maximum-pool-size: 20  # Max connections
      minimum-idle: 5  # Min idle connections
      connection-timeout: 30000  # 30 seconds
      idle-timeout: 600000  # 10 minutes
      max-lifetime: 1800000  # 30 minutes
      leak-detection-threshold: 60000  # 60 seconds
      auto-commit: true
```

**Tuning Guidelines**:
- **Development**: `maximum-pool-size: 10`
- **Production**: `maximum-pool-size: 30-50` (based on load)
- **Formula**: `pool_size = (core_count * 2) + effective_spindle_count`

### JPA/Hibernate Configuration

```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: validate  # Options: none, validate, update, create, create-drop
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: false  # Set to true for debugging
    open-in-view: false  # Disable for better performance
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
          batch_versioned_data: true
        order_inserts: true
        order_updates: true
```

**DDL Auto Options**:
- `none`: No schema management
- `validate`: Validate schema, no changes
- `update`: Update schema (development only)
- `create`: Create schema on startup
- `create-drop`: Create and drop on shutdown

## Kafka Configuration

### Bootstrap Servers

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    client-id: traffic-light-controller
```

**Environment Variables**:
```bash
KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092,kafka3:9092
```

### Producer Configuration

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all  # Options: 0, 1, all
      retries: 3
      batch-size: 16384  # 16 KB
      linger-ms: 5  # Wait time before sending
      buffer-memory: 33554432  # 32 MB
      enable-idempotence: true
      transaction-id-prefix: traffic-light-tx-
```

**Acks Options**:
- `0`: No acknowledgment (fastest, least reliable)
- `1`: Leader acknowledgment
- `all`: All replicas acknowledgment (slowest, most reliable)

### Consumer Configuration

```yaml
spring:
  kafka:
    consumer:
      group-id: traffic-light-controller
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest  # Options: earliest, latest, none
      enable-auto-commit: false  # Manual acknowledgment
      max-poll-records: 100
      session-timeout-ms: 30000
      heartbeat-interval-ms: 10000
      isolation-level: read_committed
```

### Topic Configuration

```yaml
kafka:
  topics:
    state-changed: traffic-light-state-changed
    intersection-updated: intersection-updated
    system-health-check: system-health-check
    state-changed-dlt: traffic-light-state-changed-DLT
    intersection-updated-dlt: intersection-updated-DLT
    system-events-dlt: system-events-DLT
```

## Thread Pool Configuration

### Custom Thread Pools

```yaml
traffic-light:
  thread-pool:
    core-size: 10  # Core threads
    max-size: 50  # Maximum threads
    queue-capacity: 100  # Queue size
    keep-alive-seconds: 60  # Thread keep-alive time
    thread-name-prefix: "traffic-light-"
```

**Tuning Guidelines**:
- **CPU-bound tasks**: `core_size = CPU_cores`
- **I/O-bound tasks**: `core_size = CPU_cores * 2`
- **Queue capacity**: `100-1000` (based on load)

## Resilience4j Configuration

### Rate Limiter

```yaml
resilience4j:
  ratelimiter:
    configs:
      default:
        registerHealthIndicator: true
        limitForPeriod: 100  # Requests per period
        limitRefreshPeriod: 1s  # Period duration
        timeoutDuration: 500ms  # Wait time for permission
    instances:
      apiEndpoints:
        baseConfig: default
        limitForPeriod: 200
      kafkaPublisher:
        baseConfig: default
        limitForPeriod: 500
```

## Logging Configuration

### Log Levels

```yaml
logging:
  level:
    root: INFO
    com.trafficlight: DEBUG  # Application logs
    org.springframework.kafka: INFO
    org.hibernate.SQL: DEBUG  # SQL queries
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE  # SQL parameters
    org.springframework.transaction: DEBUG
    org.springframework.orm.jpa: DEBUG
```

### Log Pattern

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId:-}] %logger{36} - %msg%n"
```

### Log File Configuration

```yaml
logging:
  file:
    name: logs/traffic-light-controller.log
    max-size: 10MB  # Max file size before rotation
    max-history: 30  # Keep 30 days of logs
```

**Environment Variables**:
```bash
LOGGING_LEVEL_COM_TRAFFICLIGHT=INFO
LOGGING_FILE_NAME=/var/log/traffic-light/application.log
```

## Security Configuration

### CORS Configuration

```yaml
cors:
  allowed-origins: "*"  # Comma-separated list or "*"
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
  allowed-headers: "*"
  allow-credentials: false
  max-age: 3600  # Preflight cache duration (seconds)
```

**Production Settings**:
```yaml
cors:
  allowed-origins: "https://app.example.com,https://admin.example.com"
  allow-credentials: true
```

### Rate Limiting

Configured via Resilience4j (see above).

## Monitoring Configuration

### Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,kafka,ratelimiters
      base-path: /actuator
  endpoint:
    health:
      show-details: always  # Options: never, when-authorized, always
      show-components: always
  health:
    kafka:
      enabled: true
    db:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

**Security Note**: In production, restrict actuator endpoints:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

## Environment-Specific Configuration

### Development Profile (`application-dev.yml`)

```yaml
spring:
  config:
    activate:
      on-profile: dev
  
  datasource:
    url: jdbc:mysql://localhost:3306/traffic_light_dev
    hikari:
      maximum-pool-size: 10
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  kafka:
    bootstrap-servers: localhost:9092

logging:
  level:
    com.trafficlight: DEBUG
```

### Test Profile (`application-test.yml`)

```yaml
spring:
  config:
    activate:
      on-profile: test
  
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
  
  kafka:
    bootstrap-servers: ${spring.embedded.kafka.brokers}

logging:
  level:
    com.trafficlight: INFO
```

### Production Profile (`application-prod.yml`)

```yaml
spring:
  config:
    activate:
      on-profile: prod
  
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}

logging:
  level:
    com.trafficlight: INFO
  file:
    name: /var/log/traffic-light-controller/application.log
```

## Custom Application Properties

### System Configuration

```yaml
traffic-light:
  system:
    default-state-duration:
      red: 30  # seconds
      yellow: 5
      green: 25
    max-intersections: 1000
    state-change-timeout: 5000  # milliseconds
    validation-enabled: true
```

### Cache Configuration

```yaml
traffic-light:
  cache:
    enabled: true
    ttl: 300  # seconds
    max-size: 1000  # entries
```

### Analytics Configuration

```yaml
traffic-light:
  analytics:
    enabled: true
    batch-size: 100
    flush-interval: 30000  # milliseconds
    retention-days: 90
```

## Configuration Best Practices

### 1. Use Environment Variables for Secrets

❌ **Bad**:
```yaml
spring:
  datasource:
    password: my_secret_password
```

✅ **Good**:
```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
```

### 2. Profile-Specific Configuration

Keep environment-specific settings in separate files:
- `application-dev.yml`
- `application-test.yml`
- `application-prod.yml`

### 3. External Configuration

For production, use external configuration:

```bash
java -jar app.jar --spring.config.location=file:/etc/traffic-light/application.yml
```

### 4. Configuration Validation

Validate configuration on startup:

```java
@ConfigurationProperties(prefix = "traffic-light")
@Validated
public class TrafficLightProperties {
    @NotNull
    @Min(1)
    private Integer maxIntersections;
    // ...
}
```

### 5. Sensitive Data

Never commit sensitive data to version control. Use:
- Environment variables
- Secret management systems (Vault, AWS Secrets Manager)
- Encrypted configuration files

## Configuration Reference

### Complete Example

See `application.yml` for the complete configuration reference with all available options and their default values.

### Configuration Documentation

For detailed Spring Boot configuration options:
- [Spring Boot Common Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [Kafka Configuration](https://kafka.apache.org/documentation/#configuration)
- [Resilience4j Configuration](https://resilience4j.readme.io/docs/getting-started-3)

## Troubleshooting Configuration

### Check Active Configuration

```bash
# View all properties
curl http://localhost:9900/actuator/configprops

# View environment
curl http://localhost:9900/actuator/env
```

### Override at Runtime

```bash
# Command line
java -jar app.jar --server.port=8080 --spring.profiles.active=prod

# Environment variable
export SERVER_PORT=8080
java -jar app.jar
```

### Configuration Validation Errors

Check logs for configuration validation errors:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Binding to target org.springframework.boot.context.properties.bind.BindException: Failed to bind properties under 'spring.datasource' to javax.sql.DataSource
```

## Support

For configuration support:
- Documentation: https://docs.trafficlight.com/configuration
- Email: support@trafficlight.com
