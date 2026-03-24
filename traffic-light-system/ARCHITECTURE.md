# Traffic Light Controller - Architecture Documentation

## Table of Contents

1. [System Overview](#system-overview)
2. [Architecture Decisions](#architecture-decisions)
3. [Component Architecture](#component-architecture)
4. [Data Flow](#data-flow)
5. [Concurrency Model](#concurrency-model)
6. [Event-Driven Architecture](#event-driven-architecture)
7. [Database Design](#database-design)
8. [Security Architecture](#security-architecture)
9. [Scalability Considerations](#scalability-considerations)

## System Overview

The Traffic Light Controller is an enterprise-grade system designed to manage traffic intersections, control traffic lights, and process state transitions in real-time. The system follows a layered architecture with clear separation of concerns.

### Key Characteristics

- **Distributed**: Event-driven architecture using Kafka
- **Concurrent**: Thread-safe operations with multiple locking strategies
- **Resilient**: Rate limiting and fault tolerance
- **Observable**: Comprehensive monitoring and health checks
- **Scalable**: Horizontal scaling capability

## Architecture Decisions

### ADR-001: Spring Boot 3.2 Framework

**Status**: Accepted

**Context**: Need a robust, production-ready framework for building REST APIs with enterprise features.

**Decision**: Use Spring Boot 3.2 with Java 17.

**Consequences**:
- ✅ Mature ecosystem with extensive libraries
- ✅ Built-in support for REST, JPA, Kafka, monitoring
- ✅ Strong community and documentation
- ⚠️ Learning curve for team members new to Spring

### ADR-002: Event-Driven Architecture with Kafka

**Status**: Accepted

**Context**: Need to decouple components and enable real-time event processing.

**Decision**: Use Apache Kafka for event streaming.

**Consequences**:
- ✅ Asynchronous processing
- ✅ Event sourcing capability
- ✅ Scalable event processing
- ⚠️ Additional infrastructure complexity
- ⚠️ Eventual consistency model

### ADR-003: Optimistic Locking for Concurrency

**Status**: Accepted

**Context**: Multiple requests may attempt to modify the same traffic light state simultaneously.

**Decision**: Use JPA optimistic locking with @Version annotation.

**Consequences**:
- ✅ Better performance than pessimistic locking
- ✅ Prevents lost updates
- ⚠️ Requires retry logic for conflicts
- ⚠️ Not suitable for high-contention scenarios

### ADR-004: Resilience4j for Rate Limiting

**Status**: Accepted

**Context**: Need rate limiting capabilities to protect the API from abuse.

**Decision**: Use Resilience4j for rate limiting.

**Consequences**:
- ✅ Battle-tested library
- ✅ Comprehensive rate limiting features
- ✅ Spring Boot integration
- ✅ Metrics and monitoring support

### ADR-005: MySQL for Primary Database

**Status**: Accepted

**Context**: Need reliable relational database for transactional data.

**Decision**: Use MySQL 8.0 as primary database.

**Consequences**:
- ✅ ACID compliance
- ✅ Mature and stable
- ✅ Good performance for read-heavy workloads
- ⚠️ Vertical scaling limitations

### ADR-006: Custom Data Structures for Performance

**Status**: Accepted

**Context**: Need optimized data structures for light sequences and state caching.

**Decision**: Implement custom thread-safe data structures.

**Consequences**:
- ✅ Optimized for specific use cases
- ✅ Better performance than generic collections
- ⚠️ Additional maintenance burden
- ⚠️ Requires thorough testing

## Component Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────┐
│              Presentation Layer                  │
│  (REST Controllers, Filters, Exception Handlers) │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│              Service Layer                       │
│  (Business Logic, Coordination, Validation)      │
└────────────────┬────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
┌───────▼──────┐  ┌──────▼────────┐
│ Data Access  │  │ Event Layer   │
│   (JPA)      │  │   (Kafka)     │
└───────┬──────┘  └───────────────┘
        │
┌───────▼──────┐
│   Database   │
│   (MySQL)    │
└──────────────┘
```

### Component Responsibilities

#### Presentation Layer
- **Controllers**: Handle HTTP requests/responses
- **Filters**: Request/response processing (CORS, logging, rate limiting)
- **Exception Handlers**: Global error handling

#### Service Layer
- **Business Services**: Core business logic
- **Transaction Management**: ACID transaction handling
- **Validation**: Business rule validation
- **Async Services**: Asynchronous operations
- **Coordination**: Coordinate operations between repositories and events

#### Data Access Layer
- **Repositories**: Database operations
- **Entities**: Domain models
- **Custom Queries**: Optimized database queries

#### Event Layer
- **Publishers**: Publish events to Kafka
- **Listeners**: Consume events from Kafka
- **Event Models**: Event data structures

## Data Flow

### Synchronous Request Flow

```
Client Request
    │
    ▼
┌─────────────┐
│   Filter    │ (Correlation ID, Logging, Rate Limiting)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Controller  │ (Request validation, DTO mapping)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Service    │ (Business logic, transaction management)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Repository  │ (Database operations)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   MySQL     │
└─────────────┘
```

### Asynchronous Event Flow

```
Service Layer
    │
    ▼
┌─────────────┐
│  Publisher  │ (Publish event to Kafka)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    Kafka    │ (Event broker)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Listener   │ (Consume event)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Service    │ (Process event)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   MySQL     │ (Persist state)
└─────────────┘
```

## Concurrency Model

### Thread Pools

The system uses multiple thread pools for different purposes:

1. **API Thread Pool**: Handles HTTP requests (Tomcat default)
2. **Async Thread Pool**: Asynchronous operations (CompletableFuture)
3. **Kafka Consumer Thread Pool**: Event processing
4. **Scheduled Task Thread Pool**: Background jobs

### Locking Strategies

#### Optimistic Locking
Used for most entity updates:

```java
@Entity
public class TrafficLight {
    @Version
    private Long version;
    // ...
}
```

**When to use**: Low contention scenarios, read-heavy workloads

#### Pessimistic Locking
Used for critical operations:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Intersection> findByUuidWithLock(String uuid);
```

**When to use**: High contention scenarios, critical state changes

#### ReentrantLock
Used for in-memory critical sections:

```java
public class CriticalSectionManager {
    private final ConcurrentHashMap<String, ReentrantLock> locks;
    // ...
}
```

**When to use**: Fine-grained locking, custom synchronization

### Thread Safety

All shared data structures are thread-safe:

- **StateCache**: Uses ReadWriteLock
- **VolatileStateHolder**: Uses volatile fields
- **SequenceNode**: Uses volatile references
- **ConcurrentHashMap**: For caching

## Event-Driven Architecture

### Event Types

1. **StateChangedEvent**: Traffic light state transitions
2. **IntersectionUpdatedEvent**: Intersection configuration changes
3. **SystemHealthEvent**: System health status changes

### Event Flow

```
State Change Request
    │
    ▼
┌─────────────────┐
│ Service Layer   │ (Update database)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Event Publisher │ (Publish to Kafka)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Kafka Topic    │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│Listener│ │Listener│ (Multiple consumers)
│   1    │ │   2    │
└────────┘ └────────┘
```

### Event Processing Guarantees

- **At-least-once delivery**: Kafka consumer manual acknowledgment
- **Idempotency**: Event handlers are idempotent
- **Ordering**: Partition-level ordering guaranteed
- **Dead Letter Queue**: Failed events sent to DLT

## Database Design

### Entity Relationships

```
Intersection (1) ──────< (N) TrafficLight
     │
     │
     └──────< (N) LightSequence
                      │
                      └──────< (N) SequenceStep

TrafficLight (1) ──────< (N) StateHistory
```

### Key Tables

- **intersections**: Traffic intersection data
- **traffic_lights**: Individual traffic lights
- **state_history**: Audit trail of state changes
- **light_sequences**: Timing configurations
- **sequence_steps**: Individual sequence steps
- **system_events**: System event log

### Indexing Strategy

- Primary keys: Clustered index on `id` (BIGINT)
- UUID lookups: Index on `uuid` column
- Foreign keys: Indexed for join performance
- Timestamp queries: Index on `created_at`, `updated_at`
- Status queries: Index on `status`, `is_active`

## Security Architecture

### Current Implementation

1. **CORS Configuration**: Configurable allowed origins
2. **Input Sanitization**: XSS and SQL injection prevention
3. **Rate Limiting**: Per-IP rate limiting
4. **Audit Logging**: Security event tracking
5. **Input Validation**: Bean Validation annotations

### Future Enhancements

- OAuth2/JWT authentication
- Role-based access control (RBAC)
- API key management
- TLS/SSL encryption
- Secret management (Vault)

## Scalability Considerations

### Horizontal Scaling

The application is designed for horizontal scaling:

1. **Stateless Services**: No session state in application
2. **Database Connection Pooling**: HikariCP for efficient connections
3. **Kafka Consumer Groups**: Parallel event processing
4. **Load Balancing**: Can run multiple instances behind load balancer

### Vertical Scaling

Configuration options for vertical scaling:

1. **Thread Pool Sizing**: Adjust based on CPU cores
2. **Connection Pool Sizing**: Adjust based on database capacity
3. **JVM Heap Size**: Tune for memory requirements
4. **Kafka Partitions**: Increase for higher throughput

### Caching Strategy

1. **Application-Level Cache**: StateCache for frequently accessed data
2. **Database Query Cache**: Disabled (prefer application cache)
3. **HTTP Cache Headers**: For read-only endpoints

### Performance Optimization

1. **Batch Operations**: Bulk updates for efficiency
2. **Async Processing**: Non-blocking operations
3. **Connection Pooling**: Reuse database connections
4. **Lazy Loading**: Load data on demand
5. **Pagination**: Limit result set sizes

## Monitoring and Observability

### Metrics

- **Application Metrics**: Micrometer + Prometheus
- **JVM Metrics**: Heap, GC, threads
- **Database Metrics**: Connection pool, query performance
- **Kafka Metrics**: Producer/consumer lag, throughput

### Health Checks

- **Database Health**: Connection validation
- **Kafka Health**: Cluster connectivity
- **Custom Health**: Traffic light system status

### Logging

- **Structured Logging**: JSON format with correlation IDs
- **Log Levels**: DEBUG, INFO, WARN, ERROR
- **Log Aggregation**: Ready for ELK/Splunk integration

### Tracing

- **Correlation ID**: Request tracing across services
- **MDC Context**: Thread-local context propagation

## Deployment Architecture

### Development Environment

```
Developer Machine
├── MySQL (localhost:3306)
├── Kafka (localhost:9092)
└── Application (localhost:9900)
```

### Production Environment

```
Load Balancer
    │
    ├─── App Instance 1
    ├─── App Instance 2
    └─── App Instance 3
         │
         ├─── MySQL Cluster
         └─── Kafka Cluster
```

## Future Enhancements

1. **Microservices**: Split into smaller services
2. **Event Sourcing**: Full event sourcing implementation
3. **CQRS**: Separate read/write models
4. **GraphQL**: Alternative API interface
5. **WebSocket**: Real-time updates to clients
6. **Machine Learning**: Predictive traffic optimization
7. **Multi-Region**: Geographic distribution
8. **Service Mesh**: Istio/Linkerd integration

## Conclusion

The Traffic Light Controller architecture is designed for:

- **Reliability**: Fault tolerance and error handling
- **Performance**: Optimized for high throughput
- **Maintainability**: Clear separation of concerns
- **Scalability**: Horizontal and vertical scaling
- **Observability**: Comprehensive monitoring and logging

The architecture balances complexity with pragmatism, using proven patterns and technologies while remaining flexible for future enhancements.
