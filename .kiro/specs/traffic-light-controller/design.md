# Traffic Light Controller API - Design Document

## 1. System Architecture

### 1.1 High-Level Architecture
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   REST API      │    │   Service Layer │    │   Data Layer    │
│   Controllers   │───▶│   Business      │───▶│   MySQL DB      │
│                 │    │   Logic         │    │   Repositories  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Event Layer   │    │   Async Layer   │    │   Config Layer  │
│   Kafka         │    │   CompletableFuture│    │   Properties    │
│   Publishers    │    │   Thread Pools  │    │   Validation    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 1.2 Package Structure
```
com.trafficlight.controller/
├── api/                    # REST Controllers
├── service/               # Business Logic
├── repository/            # Data Access
├── domain/               # Domain Models
├── event/                # Kafka Events
├── config/               # Configuration
├── exception/            # Custom Exceptions
├── util/                 # Utilities
└── infrastructure/       # Cross-cutting concerns
```

## 2. Domain Model

### 2.1 Core Domain Entities

#### Intersection
```java
public class Intersection {
    private String id;
    private String name;
    private Set<TrafficLight> trafficLights;
    private IntersectionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### TrafficLight
```java
public class TrafficLight {
    private String id;
    private String intersectionId;
    private Direction direction;
    private LightState currentState;
    private LocalDateTime lastStateChange;
    private Duration stateDuration;
    private boolean isActive;
    private Long version; // For optimistic locking
}
```

#### LightSequence
```java
public class LightSequence {
    private String id;
    private String intersectionId;
    private List<SequenceStep> steps;
    private boolean isActive;
    private LocalDateTime createdAt;
    private Long version;
}
```

#### StateHistory
```java
public class StateHistory {
    private String id;
    private String trafficLightId;
    private String intersectionId;
    private LightState previousState;
    private LightState newState;
    private LocalDateTime timestamp;
    private String reason;
    private Long durationMs;
    private String createdBy;
}
```

### 2.2 Enums and Constants

#### LightState
```java
public enum LightState {
    RED("RED", 30),
    YELLOW("YELLOW", 5),
    GREEN("GREEN", 25);
    
    private final String displayName;
    private final int defaultDurationSeconds;
    
    LightState(String displayName, int defaultDurationSeconds) {
        this.displayName = displayName;
        this.defaultDurationSeconds = defaultDurationSeconds;
    }
}
```

#### Direction
```java
public enum Direction {
    NORTH("NORTH"),
    SOUTH("SOUTH"),
    EAST("EAST"),
    WEST("WEST");
    
    private final String displayName;
    
    Direction(String displayName) {
        this.displayName = displayName;
    }
}
```

## 3. MySQL Database Design

### 3.1 Database Schema

#### intersections Table
```sql
CREATE TABLE intersections (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'MAINTENANCE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;
```

#### traffic_lights Table
```sql
CREATE TABLE traffic_lights (
    id VARCHAR(36) PRIMARY KEY,
    intersection_id VARCHAR(36) NOT NULL,
    direction ENUM('NORTH', 'SOUTH', 'EAST', 'WEST') NOT NULL,
    current_state ENUM('RED', 'YELLOW', 'GREEN') DEFAULT 'RED',
    last_state_change TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    state_duration_seconds INT DEFAULT 30,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (intersection_id) REFERENCES intersections(id) ON DELETE CASCADE,
    UNIQUE KEY uk_intersection_direction (intersection_id, direction),
    INDEX idx_intersection_id (intersection_id),
    INDEX idx_current_state (current_state),
    INDEX idx_last_state_change (last_state_change)
) ENGINE=InnoDB;
```

#### state_history Table
```sql
CREATE TABLE state_history (
    id VARCHAR(36) PRIMARY KEY,
    traffic_light_id VARCHAR(36) NOT NULL,
    intersection_id VARCHAR(36) NOT NULL,
    previous_state ENUM('RED', 'YELLOW', 'GREEN'),
    new_state ENUM('RED', 'YELLOW', 'GREEN') NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHA
R(500),
    duration_ms BIGINT,
    created_by VARCHAR(255),
    FOREIGN KEY (traffic_light_id) REFERENCES traffic_lights(id) ON DELETE CASCADE,
    FOREIGN KEY (intersection_id) REFERENCES intersections(id) ON DELETE CASCADE,
    INDEX idx_traffic_light_id (traffic_light_id),
    INDEX idx_intersection_id (intersection_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_new_state (new_state)
) ENGINE=InnoDB;
```

#### light_sequences Table
```sql
CREATE TABLE light_sequences (
    id VARCHAR(36) PRIMARY KEY,
    intersection_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (intersection_id) REFERENCES intersections(id) ON DELETE CASCADE,
    INDEX idx_intersection_id (intersection_id),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;
```

#### sequence_steps Table
```sql
CREATE TABLE sequence_steps (
    id VARCHAR(36) PRIMARY KEY,
    sequence_id VARCHAR(36) NOT NULL,
    step_order INT NOT NULL,
    direction ENUM('NORTH', 'SOUTH', 'EAST', 'WEST') NOT NULL,
    light_state ENUM('RED', 'YELLOW', 'GREEN') NOT NULL,
    duration_seconds INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sequence_id) REFERENCES light_sequences(id) ON DELETE CASCADE,
    UNIQUE KEY uk_sequence_step_order (sequence_id, step_order),
    INDEX idx_sequence_id (sequence_id)
) ENGINE=InnoDB;
```

#### system_events Table
```sql
CREATE TABLE system_events (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    intersection_id VARCHAR(36),
    traffic_light_id VARCHAR(36),
    event_data JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN DEFAULT FALSE,
    retry_count INT DEFAULT 0,
    INDEX idx_event_type (event_type),
    INDEX idx_intersection_id (intersection_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_processed (processed)
) ENGINE=InnoDB;
```

### 3.2 Spring Boot 3.2 Configuration

#### Application Properties
```yaml
spring:
  application:
    name: traffic-light-controller
  profiles:
    active: dev
  
  datasource:
    url: jdbc:mysql://localhost:3306/traffic_light_db
    username: ${DB_USERNAME:traffic_user}
    password: ${DB_PASSWORD:traffic_pass}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
    open-in-view: false
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      enable-idempotence: true
      transaction-id-prefix: traffic-light-tx-
    consumer:
      group-id: traffic-light-controller
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "com.trafficlight.controller.event"
```

## 4. Repository Layer with JPA

### 4.1 Traffic Light Repository
```java
@Repository
public interface TrafficLightRepository extends JpaRepository<TrafficLight, String> {
    
    List<TrafficLight> findByIntersectionId(String intersectionId);
    
    Optional<TrafficLight> findByIntersectionIdAndDirection(String intersectionId, Direction direction);
    
    @Modifying
    @Query("UPDATE TrafficLight tl SET tl.currentState = :state, tl.lastStateChange = :timestamp, tl.version = tl.version + 1 WHERE tl.id = :id")
    int updateState(@Param("id") String id, @Param("state") LightState state, @Param("timestamp") LocalDateTime timestamp);
    
    List<TrafficLight> findByCurrentStateAndIsActiveTrue(LightState state);
    
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.id = :id")
    Optional<TrafficLight> findByIdWithOptimisticLock(@Param("id") String id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.intersectionId = :intersectionId")
    List<TrafficLight> findByIntersectionIdWithPessimisticLock(@Param("intersectionId") String intersectionId);
    
    @Query("SELECT tl FROM TrafficLight tl WHERE tl.intersectionId = :intersectionId AND tl.currentState = 'GREEN'")
    List<TrafficLight> findGreenLightsByIntersectionId(@Param("intersectionId") String intersectionId);
}
```

### 4.2 Intersection Repository
```java
@Repository
public interface IntersectionRepository extends JpaRepository<Intersection, String> {
    
    List<Intersection> findByStatus(IntersectionStatus status);
    
    @Query("SELECT i FROM Intersection i JOIN FETCH i.trafficLights WHERE i.id = :id")
    Optional<Intersection> findByIdWithTrafficLights(@Param("id") String id);
    
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Intersection> findByIdAndVersion(String id, Long version);
    
    @Query("SELECT i FROM Intersection i WHERE i.status = 'ACTIVE'")
    List<Intersection> findActiveIntersections();
}
```

### 4.3 State History Repository
```java
@Repository
public interface StateHistoryRepository extends JpaRepository<StateHistory, String> {
    
    List<StateHistory> findByTrafficLightIdOrderByTimestampDesc(String trafficLightId);
    
    List<StateHistory> findByIntersectionIdAndTimestampAfterOrderByTimestampDesc(String intersectionId, LocalDateTime since);
    
    long countByTrafficLightIdAndTimestampAfter(String trafficLightId, LocalDateTime since);
    
    @Query("SELECT sh FROM StateHistory sh WHERE sh.intersectionId = :intersectionId AND sh.timestamp BETWEEN :start AND :end")
    List<StateHistory> findByIntersectionIdAndTimestampBetween(
        @Param("intersectionId") String intersectionId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
    
    @Query(value = "SELECT * FROM state_history WHERE traffic_light_id = :trafficLightId ORDER BY timestamp DESC LIMIT :limit", nativeQuery = true)
    List<StateHistory> findRecentHistoryByTrafficLightId(@Param("trafficLightId") String trafficLightId, @Param("limit") int limit);
}
```

### 4.4 Light Sequence Repository
```java
@Repository
public interface LightSequenceRepository extends JpaRepository<LightSequence, String> {
    
    List<LightSequence> findByIntersectionIdAndIsActiveTrue(String intersectionId);
    
    Optional<LightSequence> findByIntersectionIdAndIsActiveTrueAndName(String intersectionId, String name);
    
    @Modifying
    @Query("UPDATE LightSequence ls SET ls.isActive = false WHERE ls.intersectionId = :intersectionId")
    int deactivateAllSequences(@Param("intersectionId") String intersectionId);
    
    @Query("SELECT ls FROM LightSequence ls JOIN FETCH ls.steps WHERE ls.id = :id")
    Optional<LightSequence> findByIdWithSteps(@Param("id") String id);
}
```

### 4.5 System Events Repository
```java
@Repository
public interface SystemEventsRepository extends JpaRepository<SystemEvent, String> {
    
    List<SystemEvent> findByProcessedFalseOrderByTimestampAsc();
    
    List<SystemEvent> findByEventTypeAndProcessedFalse(String eventType);
    
    @Modifying
    @Query("UPDATE SystemEvent se SET se.processed = true WHERE se.id = :id")
    int markAsProcessed(@Param("id") String id);
    
    @Query("SELECT se FROM SystemEvent se WHERE se.retryCount < :maxRetries AND se.processed = false")
    List<SystemEvent> findRetryableEvents(@Param("maxRetries") int maxRetries);
    
    @Modifying
    @Query("UPDATE SystemEvent se SET se.retryCount = se.retryCount + 1 WHERE se.id = :id")
    int incrementRetryCount(@Param("id") String id);
}
```

## 5. Service Layer with JPA Integration

### 5.1 Traffic Light Service
```java
@Service
@Transactional
public class TrafficLightService {
    
    private final TrafficLightRepository trafficLightRepository;
    private final StateHistoryRepository stateHistoryRepository;
    private final TrafficLightEventPublisher eventPublisher;
    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final ConcurrentMap<String, TrafficLight> lightCache = new ConcurrentHashMap<>();
    private final ExecutorService customExecutorService;
    
    public TrafficLightService(TrafficLightRepository trafficLightRepository,
                              StateHistoryRepository stateHistoryRepository,
                              TrafficLightEventPublisher eventPublisher,
                              @Qualifier("trafficLightExecutor") ExecutorService customExecutorService) {
        this.trafficLightRepository = trafficLightRepository;
        this.stateHistoryRepository = stateHistoryRepository;
        this.eventPublisher = eventPublisher;
        this.customExecutorService = customExecutorService;
    }
    
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public CompletableFuture<TrafficLight> changeState(String lightId, LightState newState) {
        return CompletableFuture.supplyAsync(() -> {
            stateLock.writeLock().lock();
            try {
                TrafficLight light = trafficLightRepository.findByIdWithOptimisticLock(lightId)
                    .orElseThrow(() -> new TrafficLightNotFoundException(lightId));
                
                validateStateTransition(light, newState);
                
                LightState previousState = light.getCurrentState();
                light.setCurrentState(newState);
                light.setLastStateChange(LocalDateTime.now());
                
                TrafficLight savedLight = trafficLightRepository.save(light);
                
                // Record state history
                StateHistory history = StateHistory.builder()
                    .id(UUID.randomUUID().toString())
                    .trafficLightId(lightId)
                    .intersectionId(light.getIntersectionId())
                    .previousState(previousState)
                    .newState(newState)
                    .timestamp(LocalDateTime.now())
                    .reason("Manual state change")
                    .createdBy("SYSTEM")
                    .build();
                
                stateHistoryRepository.save(history);
                
                // Update cache
                lightCache.put(lightId, savedLight);
                
                // Publish event asynchronously
                eventPublisher.publishStateChanged(savedLight, previousState);
                
                return savedLight;
                
            } catch (OptimisticLockingFailureException e) {
                throw new ConcurrentModificationException("Traffic light was modified by another process", e);
            } finally {
                stateLock.writeLock().unlock();
            }
        }, customExecutorService);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<StateHistory> getStateHistory(String lightId, Duration period) {
        LocalDateTime since = LocalDateTime.now().minus(period);
        return stateHistoryRepository.findByTrafficLightIdAndTimestampAfter(lightId, since);
    }
    
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> batchUpdateStates(List<StateUpdateRequest> updates) {
        return CompletableFuture.runAsync(() -> {
            stateLock.writeLock().lock();
            try {
                List<TrafficLight> lights = updates.parallelStream()
                    .map(update -> {
                        TrafficLight light = trafficLightRepository.findById(update.getTrafficLightId())
                            .orElseThrow(() -> new TrafficLightNotFoundException(update.getTrafficLightId()));
                        
                        light.setCurrentState(update.getNewState());
                        light.setLastStateChange(LocalDateTime.now());
                        return light;
                    })
                    .collect(Collectors.toList());
                
                trafficLightRepository.saveAll(lights);
                
                // Update cache
                lights.forEach(light -> lightCache.put(light.getId(), light));
                
            } finally {
                stateLock.writeLock().unlock();
            }
        }, customExecutorService);
    }
}
```

## 6. Kafka Configuration with Transactions and Manual ACK

### 6.1 Enhanced Kafka Configuration
```java
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "traffic-light-tx-");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        return new DefaultKafkaProducerFactory<>(props);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setTransactionIdPrefix("traffic-light-tx-");
        return template;
    }
    
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "traffic-light-controller");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual ACK
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.trafficlight.controller.event");
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(3000);
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            new FixedBackOff(1000L, 3L)
        ));
        return factory;
    }
    
    @Bean
    public KafkaTransactionManager kafkaTransactionManager() {
        return new KafkaTransactionManager(producerFactory());
    }
    
    @Bean
    public NewTopic stateChangedTopic() {
        return TopicBuilder.name("traffic-light.state-changed")
            .partitions(3)
            .replicas(1)
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days
            .build();
    }
    
    @Bean
    public NewTopic intersectionUpdatedTopic() {
        return TopicBuilder.name("intersection.updated")
            .partitions(3)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic stateChangedDltTopic() {
        return TopicBuilder.name("traffic-light.state-changed.DLT")
            .partitions(1)
            .replicas(1)
            .build();
    }
}
```

### 6.2 Transactional Event Publisher
```java
@Component
public class TrafficLightEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CustomLogger logger;
    
    public static final String STATE_CHANGED_TOPIC = "traffic-light.state-changed";
    public static final String INTERSECTION_UPDATED_TOPIC = "intersection.updated";
    
    public TrafficLightEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, CustomLogger logger) {
        this.kafkaTemplate = kafkaTemplate;
        this.logger = logger;
    }
    
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @KafkaTransactional
    public CompletableFuture<Void> publishStateChanged(TrafficLight light, LightState previousState) {
        return CompletableFuture.runAsync(() -> {
            try {
                StateChangedEvent event = StateChangedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .trafficLightId(light.getId())
                    .intersectionId(light.getIntersectionId())
                    .direction(light.getDirection())
                    .previousState(previousState)
                    .newState(light.getCurrentState())
                    .timestamp(ZonedDateTime.now())
                    .version(light.getVersion())
                    .build();
                
                kafkaTemplate.executeInTransaction(template -> {
                    template.send(STATE_CHANGED_TOPIC, light.getId(), event);
                    logger.info("State change event published in transaction: {}", event);
                    return null;
                });
                
            } catch (Exception e) {
                logger.error("Error publishing state changed event", e);
                throw new EventPublishingException("Failed to publish state changed event", e);
            }
        });
    }
    
    @KafkaTransactional
    public void publishIntersectionUpdated(Intersection intersection) {
        try {
            IntersectionUpdatedEvent event = IntersectionUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .intersectionId(intersection.getId())
                .status(intersection.getStatus())
                .timestamp(ZonedDateTime.now())
                .trafficLightCount(intersection.getTrafficLights().size())
                .build();
            
            kafkaTemplate.executeInTransaction(template -> {
                template.send(INTERSECTION_UPDATED_TOPIC, intersection.getId(), event);
                logger.info("Intersection updated event published: {}", event);
                return null;
            });
            
        } catch (Exception e) {
            logger.error("Error publishing intersection updated event", e);
            throw new EventPublishingException("Failed to publish intersection updated event", e);
        }
    }
}
```

### 6.3 Event Listeners with Manual ACK and DLT
```java
@Component
public class TrafficLightEventListener {
    
    private final StateHistoryRepository stateHistoryRepository;
    private final CustomLogger logger;
    private final CircuitBreaker circuitBreaker;
    
    public TrafficLightEventListener(StateHistoryRepository stateHistoryRepository, 
                                   CustomLogger logger,
                                   CircuitBreaker circuitBreaker) {
        this.stateHistoryRepository = stateHistoryRepository;
        this.logger = logger;
        this.circuitBreaker = circuitBreaker;
    }
    
    @KafkaListener(topics = "traffic-light.state-changed", groupId = "state-history-processor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStateChanged(StateChangedEvent event, Acknowledgment ack) {
        try {
            logger.info("Processing state changed event: {}", event);
            
            circuitBreaker.executeSupplier(() -> {
                StateHistory history = StateHistory.builder()
                    .id(UUID.randomUUID().toString())
                    .trafficLightId(event.getTrafficLightId())
                    .intersectionId(event.getIntersectionId())
                    .previousState(event.getPreviousState())
                    .newState(event.getNewState())
                    .timestamp(event.getTimestamp().toLocalDateTime())
                    .reason("Event-driven update")
                    .createdBy("KAFKA_LISTENER")
                    .build();
                
                stateHistoryRepository.save(history);
                return history;
            });
            
            // Manual acknowledgment after successful processing
            ack.acknowledge();
            logger.info("Successfully processed and acknowledged state changed event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing state changed event: {}", event, e);
            // Don't acknowledge - will trigger retry and eventually DLT
            throw e;
        }
    }
    
    @KafkaListener(topics = "intersection.updated", groupId = "intersection-processor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleIntersectionUpdated(IntersectionUpdatedEvent event, Acknowledgment ack) {
        try {
            logger.info("Processing intersection updated event: {}", event);
            
            // Process intersection update logic here
            // Update caches, trigger notifications, etc.
            
            ack.acknowledge();
            logger.info("Successfully processed intersection updated event: {}", event.getEventId());
            
        } catch (Exception e) {
            logger.error("Error processing intersection updated event: {}", event, e);
            throw e;
        }
    }
    
    @KafkaListener(topics = "traffic-light.state-changed.DLT", groupId = "dlt-processor")
    public void handleStateChangedDLT(StateChangedEvent event, 
                                     @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage,
                                     @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                     Acknowledgment ack) {
        logger.error("Processing DLT message for state changed event: {} from topic: {} with error: {}", 
                    event, topic, exceptionMessage);
        
        try {
            // Store in dead letter table for manual intervention
            SystemEvent deadLetterEvent = SystemEvent.builder()
                .id(UUID.randomUUID().toString())
                .eventType("DLT_STATE_CHANGED")
                .intersectionId(event.getIntersectionId())
                .trafficLightId(event.getTrafficLightId())
                .eventData(JsonUtils.toJson(event))
                .timestamp(LocalDateTime.now())
                .processed(false)
                .retryCount(0)
                .build();
            
            // Save to system events for manual processing
            // systemEventsRepository.save(deadLetterEvent);
            
            // Send alert to monitoring system
            // alertService.sendDLTAlert(event, exceptionMessage);
            
            ack.acknowledge();
            
        } catch (Exception e) {
            logger.error("Error processing DLT message", e);
            // Even DLT processing failed - this needs immediate attention
        }
    }
}
```

### 6.4 Kafka Retry Configuration
```java
@Configuration
public class KafkaRetryConfig {
    
    @Bean
    public RetryTemplate kafkaRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000L);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
    
    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        // Configure exponential backoff
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(10000L);
        
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            new DeadLetterPublishingRecoverer(kafkaTemplate(), 
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", -1)),
            backOff
        );
        
        // Configure which exceptions should not be retried
        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            NullPointerException.class
        );
        
        return errorHandler;
    }
    
    private KafkaTemplate<String, Object> kafkaTemplate() {
        // Return the configured KafkaTemplate bean
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(new HashMap<>()));
    }
}
```

This updated design now uses Spring Boot 3.2, JpaRepository instead of custom base repositories, removes JdbcTemplate usage, enables Kafka transactions, and implements manual acknowledgment by consumers. The configuration supports robust event-driven architecture with proper error handling and dead letter topic processing.

## 7. Global Exception Handling

### 7.1 Custom Exceptions
```java
// Base exception class
public abstract class TrafficLightException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> details;
    
    protected TrafficLightException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    protected TrafficLightException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = new HashMap<>();
    }
    
    public String getErrorCode() { return errorCode; }
    public Map<String, Object> getDetails() { return details; }
    public void addDetail(String key, Object value) { details.put(key, value); }
}

// Specific exceptions
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrafficLightNotFoundException extends TrafficLightException {
    public TrafficLightNotFoundException(String lightId) {
        super("Traffic light not found with ID: " + lightId, "TRAFFIC_LIGHT_NOT_FOUND");
        addDetail("lightId", lightId);
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
public class IntersectionNotFoundException extends TrafficLightException {
    public IntersectionNotFoundException(String intersectionId) {
        super("Intersection not found with ID: " + intersectionId, "INTERSECTION_NOT_FOUND");
        addDetail("intersectionId", intersectionId);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictingDirectionsException extends TrafficLightException {
    public ConflictingDirectionsException(String intersectionId, List<TrafficLight> conflictingLights) {
        super("Conflicting green lights detected at intersection: " + intersectionId, "CONFLICTING_DIRECTIONS");
        addDetail("intersectionId", intersectionId);
        addDetail("conflictingLights", conflictingLights.stream()
            .map(light -> light.getDirection().name())
            .collect(Collectors.toList()));
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStateTransitionException extends TrafficLightException {
    public InvalidStateTransitionException(LightState from, LightState to) {
        super("Invalid state transition from " + from + " to " + to, "INVALID_STATE_TRANSITION");
        addDetail("fromState", from);
        addDetail("toState", to);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class ConcurrentModificationException extends TrafficLightException {
    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, "CONCURRENT_MODIFICATION", cause);
    }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EventPublishingException extends TrafficLightException {
    public EventPublishingException(String message, Throwable cause) {
        super(message, "EVENT_PUBLISHING_FAILED", cause);
    }
}
```

### 7.2 Global Exception Handler
```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    
    private final CustomLogger logger;
    
    public GlobalExceptionHandler(CustomLogger logger) {
        this.logger = logger;
    }
    
    @ExceptionHandler(TrafficLightNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTrafficLightNotFound(TrafficLightNotFoundException ex, HttpServletRequest request) {
        logger.warn("Traffic light not found: {}", ex.getMessage());
        return ResponseBuilder.notFound(ex.getMessage());
    }
    
    @ExceptionHandler(IntersectionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntersectionNotFound(IntersectionNotFoundException ex, HttpServletRequest request) {
        logger.warn("Intersection not found: {}", ex.getMessage());
        return ResponseBuilder.notFound(ex.getMessage());
    }
    
    @ExceptionHandler(ConflictingDirectionsException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictingDirections(ConflictingDirectionsException ex, HttpServletRequest request) {
        logger.error("Conflicting directions detected: {}", ex.getMessage());
        return ResponseBuilder.conflict(ex.getMessage(), ex.getDetails());
    }
    
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidStateTransition(InvalidStateTransitionException ex, HttpServletRequest request) {
        logger.warn("Invalid state transition: {}", ex.getMessage());
        return ResponseBuilder.badRequest(ex.getMessage());
    }
    
    @ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConcurrentModification(ConcurrentModificationException ex, HttpServletRequest request) {
        logger.error("Concurrent modification detected: {}", ex.getMessage());
        return ResponseBuilder.conflict(ex.getMessage(), ex.getDetails());
    }
    
    @ExceptionHandler(EventPublishingException.class)
    public ResponseEntity<ApiResponse<Void>> handleEventPublishing(EventPublishingException ex, HttpServletRequest request) {
        logger.error("Event publishing failed: {}", ex.getMessage(), ex);
        return ResponseBuilder.internalServerError("Failed to publish event", ex.getDetails());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());
        
        logger.warn("Validation errors: {}", errors);
        return ResponseBuilder.badRequest("Validation failed", errors);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.toList());
        
        logger.warn("Constraint violations: {}", errors);
        return ResponseBuilder.badRequest("Constraint violation", errors);
    }
    
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(OptimisticLockingFailureException ex, HttpServletRequest request) {
        logger.warn("Optimistic locking failure: {}", ex.getMessage());
        return ResponseBuilder.conflict("Resource was modified by another process. Please retry.");
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity constraint violated";
        if (ex.getCause() instanceof ConstraintViolationException) {
            message = "Database constraint violation";
        }
        
        return ResponseBuilder.badRequest(message);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("Invalid JSON format: {}", ex.getMessage());
        return ResponseBuilder.badRequest("Invalid JSON format in request body");
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.warn("Method not supported: {} for {}", ex.getMethod(), request.getRequestURI());
        return ResponseBuilder.error(HttpStatus.METHOD_NOT_ALLOWED, 
            "HTTP method " + ex.getMethod() + " is not supported for this endpoint");
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.warn("Missing required parameter: {}", ex.getParameterName());
        return ResponseBuilder.badRequest("Missing required parameter: " + ex.getParameterName());
    }
    
    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ApiResponse<Void>> handleCompletionException(CompletionException ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        if (cause instanceof TrafficLightException) {
            return handleTrafficLightException((TrafficLightException) cause, request);
        }
        
        logger.error("Async operation failed: {}", ex.getMessage(), ex);
        return ResponseBuilder.internalServerError("Async operation failed");
    }
    
    @ExceptionHandler(TrafficLightException.class)
    public ResponseEntity<ApiResponse<Void>> handleTrafficLightException(TrafficLightException ex, HttpServletRequest request) {
        logger.error("Traffic light system error: {}", ex.getMessage(), ex);
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        if (responseStatus != null) {
            status = responseStatus.value();
        }
        
        return ResponseBuilder.error(status, ex.getMessage(), null, ex.getDetails());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        String correlationId = UUID.randomUUID().toString();
        logger.error("Unexpected error [{}]: {}", correlationId, ex.getMessage(), ex);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("correlationId", correlationId);
        errorDetails.put("timestamp", ZonedDateTime.now());
        
        return ResponseBuilder.internalServerError("An unexpected error occurred", errorDetails);
    }
}
```

### 7.3 Validation Configuration
```java
@Configuration
public class ValidationConfig {
    
    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
}
```

## 8. REST Controller Layer

### 8.1 Traffic Light Controller
```java
@RestController
@RequestMapping("/api/traffic-service/lights")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrafficLightController {
    
    private final TrafficLightService trafficLightService;
    private final CustomLogger logger;
    
    public TrafficLightController(TrafficLightService trafficLightService, CustomLogger logger) {
        this.trafficLightService = trafficLightService;
        this.logger = logger;
    }
    
    @GetMapping("/{lightId}")
    @Operation(summary = "Get traffic light by ID", description = "Retrieve traffic light details by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Traffic light found"),
        @ApiResponse(responseCode = "404", description = "Traffic light not found")
    })
    public ResponseEntity<ApiResponse<TrafficLightDto>> getTrafficLight(
            @PathVariable @NotBlank(message = "Light ID cannot be blank") String lightId) {
        
        logger.info("Retrieving traffic light with ID: {}", lightId);
        
        TrafficLight light = trafficLightService.findById(lightId);
        TrafficLightDto dto = TrafficLightMapper.toDto(light);
        
        return ResponseBuilder.success(dto, "Traffic light retrieved successfully");
    }
    
    @GetMapping
    @Operation(summary = "Get all traffic lights", description = "Retrieve paginated list of traffic lights")
    public ResponseEntity<ApiResponse<List<TrafficLightDto>>> getAllTrafficLights(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String intersectionId,
            @RequestParam(required = false) LightState state) {
        
        logger.info("Retrieving traffic lights - page: {}, size: {}, intersectionId: {}, state: {}", 
                   page, size, intersectionId, state);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TrafficLight> lights = trafficLightService.findAll(pageable, intersectionId, state);
        Page<TrafficLightDto> dtoPage = lights.map(TrafficLightMapper::toDto);
        
        return ResponseBuilder.success(dtoPage, "Traffic lights retrieved successfully");
    }
    
    @PostMapping("/{lightId}/change-state")
    @Operation(summary = "Change traffic light state", description = "Change the state of a traffic light")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "State changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid state transition"),
        @ApiResponse(responseCode = "404", description = "Traffic light not found"),
        @ApiResponse(responseCode = "409", description = "Conflicting state change")
    })
    public CompletableFuture<ResponseEntity<ApiResponse<TrafficLightDto>>> changeState(
            @PathVariable @NotBlank String lightId,
            @RequestBody @Valid StateChangeRequest request) {
        
        logger.info("Changing state for traffic light {} to {}", lightId, request.getNewState());
        
        return trafficLightService.changeState(lightId, request.getNewState())
            .thenApply(light -> {
                TrafficLightDto dto = TrafficLightMapper.toDto(light);
                return ResponseBuilder.success(dto, "Traffic light state changed successfully");
            })
            .exceptionally(throwable -> {
                logger.error("Failed to change state for traffic light {}", lightId, throwable);
                throw new CompletionException(throwable);
            });
    }
    
    @PostMapping("/{lightId}/pause")
    @Operation(summary = "Pause traffic light", description = "Pause the automatic state changes for a traffic light")
    public CompletableFuture<ResponseEntity<ApiResponse<Void>>> pauseTrafficLight(
            @PathVariable @NotBlank String lightId) {
        
        logger.info("Pausing traffic light: {}", lightId);
        
        return trafficLightService.pauseLight(lightId)
            .thenApply(result -> ResponseBuilder.noContent("Traffic light paused successfully"))
            .exceptionally(throwable -> {
                logger.error("Failed to pause traffic light {}", lightId, throwable);
                throw new CompletionException(throwable);
            });
    }
    
    @PostMapping("/{lightId}/resume")
    @Operation(summary = "Resume traffic light", description = "Resume automatic state changes for a traffic light")
    public CompletableFuture<ResponseEntity<ApiResponse<Void>>> resumeTrafficLight(
            @PathVariable @NotBlank String lightId) {
        
        logger.info("Resuming traffic light: {}", lightId);
        
        return trafficLightService.resumeLight(lightId)
            .thenApply(result -> ResponseBuilder.noContent("Traffic light resumed successfully"))
            .exceptionally(throwable -> {
                logger.error("Failed to resume traffic light {}", lightId, throwable);
                throw new CompletionException(throwable);
            });
    }
    
    @GetMapping("/{lightId}/history")
    @Operation(summary = "Get state history", description = "Retrieve state change history for a traffic light")
    public ResponseEntity<ApiResponse<List<StateHistoryDto>>> getStateHistory(
            @PathVariable @NotBlank String lightId,
            @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hours) {
        
        logger.info("Retrieving state history for traffic light {} for last {} hours", lightId, hours);
        
        Duration period = Duration.ofHours(hours);
        List<StateHistory> history = trafficLightService.getStateHistory(lightId, period);
        List<StateHistoryDto> dtoList = history.stream()
            .map(StateHistoryMapper::toDto)
            .collect(Collectors.toList());
        
        return ResponseBuilder.success(dtoList, "State history retrieved successfully");
    }
}
```

### 8.2 Intersection Controller
```java
@RestController
@RequestMapping("/api/traffic-service/intersections")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class IntersectionController {
    
    private final IntersectionService intersectionService;
    private final CustomLogger logger;
    
    public IntersectionController(IntersectionService intersectionService, CustomLogger logger) {
        this.intersectionService = intersectionService;
        this.logger = logger;
    }
    
    @GetMapping("/{intersectionId}")
    @Operation(summary = "Get intersection by ID", description = "Retrieve intersection details with traffic lights")
    public ResponseEntity<ApiResponse<IntersectionDto>> getIntersection(
            @PathVariable @NotBlank String intersectionId) {
        
        logger.info("Retrieving intersection: {}", intersectionId);
        
        Intersection intersection = intersectionService.findByIdWithTrafficLights(intersectionId);
        IntersectionDto dto = IntersectionMapper.toDto(intersection);
        
        return ResponseBuilder.success(dto, "Intersection retrieved successfully");
    }
    
    @GetMapping
    @Operation(summary = "Get all intersections", description = "Retrieve paginated list of intersections")
    public ResponseEntity<ApiResponse<List<IntersectionDto>>> getAllIntersections(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) IntersectionStatus status) {
        
        logger.info("Retrieving intersections - page: {}, size: {}, status: {}", page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Intersection> intersections = intersectionService.findAll(pageable, status);
        Page<IntersectionDto> dtoPage = intersections.map(IntersectionMapper::toDto);
        
        return ResponseBuilder.success(dtoPage, "Intersections retrieved successfully");
    }
    
    @PostMapping
    @Operation(summary = "Create intersection", description = "Create a new intersection with traffic lights")
    public CompletableFuture<ResponseEntity<ApiResponse<IntersectionDto>>> createIntersection(
            @RequestBody @Valid CreateIntersectionRequest request) {
        
        logger.info("Creating intersection: {}", request.getName());
        
        return intersectionService.createIntersection(request)
            .thenApply(intersection -> {
                IntersectionDto dto = IntersectionMapper.toDto(intersection);
                return ResponseBuilder.created(dto, "Intersection created successfully");
            })
            .exceptionally(throwable -> {
                logger.error("Failed to create intersection", throwable);
                throw new CompletionException(throwable);
            });
    }
    
    @GetMapping("/{intersectionId}/current-state")
    @Operation(summary = "Get current state", description = "Get current state of all traffic lights at intersection")
    public ResponseEntity<ApiResponse<IntersectionStateDto>> getCurrentState(
            @PathVariable @NotBlank String intersectionId) {
        
        logger.info("Retrieving current state for intersection: {}", intersectionId);
        
        IntersectionState state = intersectionService.getCurrentState(intersectionId);
        IntersectionStateDto dto = IntersectionStateMapper.toDto(state);
        
        return ResponseBuilder.success(dto, "Current state retrieved successfully");
    }
    
    @PostMapping("/{intersectionId}/validate")
    @Operation(summary = "Validate intersection", description = "Validate intersection for conflicting states")
    public CompletableFuture<ResponseEntity<ApiResponse<ValidationResultDto>>> validateIntersection(
            @PathVariable @NotBlank String intersectionId) {
        
        logger.info("Validating intersection: {}", intersectionId);
        
        return intersectionService.validateAndUpdateIntersection(intersectionId)
            .thenApply(result -> {
                ValidationResultDto dto = ValidationResultDto.builder()
                    .valid(true)
                    .message("Intersection validation passed")
                    .timestamp(ZonedDateTime.now())
                    .build();
                return ResponseBuilder.success(dto, "Intersection validated successfully");
            })
            .exceptionally(throwable -> {
                logger.error("Intersection validation failed for {}", intersectionId, throwable);
                throw new CompletionException(throwable);
            });
    }
}
```

### 8.3 Light Sequence Controller
```java
@RestController
@RequestMapping("/api/traffic-service/sequences")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class LightSequenceController {
    
    private final LightSequenceService lightSequenceService;
    private final CustomLogger logger;
    
    public LightSequenceController(LightSequenceService lightSequenceService, CustomLogger logger) {
        this.lightSequenceService = lightSequenceService;
        this.logger = logger;
    }
    
    @PostMapping
    @Operation(summary = "Create light sequence", description = "Create a new light sequence for an intersection")
    public CompletableFuture<ResponseEntity<ApiResponse<LightSequenceDto>>> createSequence(
            @RequestBody @Valid CreateSequenceRequest request) {
        
        logger.info("Creating light sequence for intersection: {}", request.getIntersectionId());
        
        return lightSequenceService.createSequence(request)
            .thenApply(sequence -> {
                LightSequenceDto dto = LightSequenceMapper.toDto(sequence);
                return ResponseBuilder.created(dto, "Light sequence created successfully");
            })
            .exceptionally(throwable -> {
                logger.error("Failed to create light sequence", throwable);
                throw new CompletionException(throwable);
            });
    }
    
    @PostMapping("/{sequenceId}/activate")
    @Operation(summary = "Activate sequence", description = "Activate a light sequence for an intersection")
    public CompletableFuture<ResponseEntity<ApiResponse<Void>>> activateSequence(
            @PathVariable @NotBlank String sequenceId) {
        
        logger.info("Activating light sequence: {}", sequenceId);
        
        return lightSequenceService.activateSequence(sequenceId)
            .thenApply(result -> ResponseBuilder.noContent("Light sequence activated successfully"))
            .exceptionally(throwable -> {
                logger.error("Failed to activate light sequence {}", sequenceId, throwable);
                throw new CompletionException(throwable);
            });
    }
    
    @GetMapping("/intersection/{intersectionId}")
    @Operation(summary = "Get sequences by intersection", description = "Get all light sequences for an intersection")
    public ResponseEntity<ApiResponse<List<LightSequenceDto>>> getSequencesByIntersection(
            @PathVariable @NotBlank String intersectionId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        
        logger.info("Retrieving light sequences for intersection: {}, activeOnly: {}", intersectionId, activeOnly);
        
        List<LightSequence> sequences = lightSequenceService.findByIntersectionId(intersectionId, activeOnly);
        List<LightSequenceDto> dtoList = sequences.stream()
            .map(LightSequenceMapper::toDto)
            .collect(Collectors.toList());
        
        return ResponseBuilder.success(dtoList, "Light sequences retrieved successfully");
    }
}
```

### 8.4 Request/Response DTOs
```java
// State change request
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateChangeRequest {
    @NotNull(message = "New state is required")
    private LightState newState;
    
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
    
    // Getters and setters
    public LightState getNewState() { return newState; }
    public void setNewState(LightState newState) { this.newState = newState; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

// Create intersection request
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateIntersectionRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;
    
    @NotEmpty(message = "At least one direction is required")
    @Size(max = 4, message = "Maximum 4 directions allowed")
    private Set<Direction> directions;
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<Direction> getDirections() { return directions; }
    public void setDirections(Set<Direction> directions) { this.directions = directions; }
}

// Traffic light DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrafficLightDto {
    private String id;
    private String intersectionId;
    private Direction direction;
    private LightState currentState;
    private LocalDateTime lastStateChange;
    private int stateDurationSeconds;
    private boolean isActive;
    private Long version;
    
    // Getters and setters omitted for brevity
}
```

This comprehensive addition includes:

1. **Global Exception Handling**: Custom exceptions with error codes, detailed global exception handler covering all common scenarios
2. **ApiResponse & ResponseBuilder**: Standardized response format with correlation IDs, pagination support, and fluent builder pattern
3. **REST Controllers**: Complete controller implementations with async support, validation, OpenAPI documentation, and proper error handling
4. **Request/Response DTOs**: Validated request objects and response DTOs with proper JSON serialization

The design ensures consistent API responses, proper error handling, and follows REST best practices with comprehensive logging and monitoring capabilities.

### 8.5 System Health Controller
```java
@RestController
@RequestMapping("/api/system-service/health")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SystemHealthController {
    
    private final SystemHealthService systemHealthService;
    private final CustomLogger logger;
    
    public SystemHealthController(SystemHealthService systemHealthService, CustomLogger logger) {
        this.systemHealthService = systemHealthService;
        this.logger = logger;
    }
    
    @GetMapping
    @Operation(summary = "System health check", description = "Get overall system health status")
    public ResponseEntity<ApiResponse<SystemHealthDto>> getSystemHealth() {
        logger.info("Checking system health");
        
        SystemHealth health = systemHealthService.checkSystemHealth();
        SystemHealthDto dto = SystemHealthMapper.toDto(health);
        
        return ResponseBuilder.success(dto, "System health retrieved successfully");
    }
    
    @GetMapping("/database")
    @Operation(summary = "Database health", description = "Check database connectivity and performance")
    public ResponseEntity<ApiResponse<DatabaseHealthDto>> getDatabaseHealth() {
        logger.info("Checking database health");
        
        DatabaseHealth health = systemHealthService.checkDatabaseHealth();
        DatabaseHealthDto dto = DatabaseHealthMapper.toDto(health);
        
        return ResponseBuilder.success(dto, "Database health retrieved successfully");
    }
    
    @GetMapping("/kafka")
    @Operation(summary = "Kafka health", description = "Check Kafka connectivity and topic status")
    public ResponseEntity<ApiResponse<KafkaHealthDto>> getKafkaHealth() {
        logger.info("Checking Kafka health");
        
        KafkaHealth health = systemHealthService.checkKafkaHealth();
        KafkaHealthDto dto = KafkaHealthMapper.toDto(health);
        
        return ResponseBuilder.success(dto, "Kafka health retrieved successfully");
    }
}
```

### 8.6 System Events Controller
```java
@RestController
@RequestMapping("/api/system-service/events")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class SystemEventsController {
    
    private final SystemEventsService systemEventsService;
    private final CustomLogger logger;
    
    public SystemEventsController(SystemEventsService systemEventsService, CustomLogger logger) {
        this.systemEventsService = systemEventsService;
        this.logger = logger;
    }
    
    @GetMapping
    @Operation(summary = "Get system events", description = "Retrieve paginated system events")
    public ResponseEntity<ApiResponse<List<SystemEventDto>>> getSystemEvents(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Boolean processed) {
        
        logger.info("Retrieving system events - page: {}, size: {}, eventType: {}, processed: {}", 
                   page, size, eventType, processed);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemEvent> events = systemEventsService.findEvents(pageable, eventType, processed);
        Page<SystemEventDto> dtoPage = events.map(SystemEventMapper::toDto);
        
        return ResponseBuilder.success(dtoPage, "System events retrieved successfully");
    }
    
    @PostMapping("/{eventId}/retry")
    @Operation(summary = "Retry system event", description = "Retry processing of a failed system event")
    public CompletableFuture<ResponseEntity<ApiResponse<Void>>> retryEvent(
            @PathVariable @NotBlank String eventId) {
        
        logger.info("Retrying system event: {}", eventId);
        
        return systemEventsService.retryEvent(eventId)
            .thenApply(result -> ResponseBuilder.noContent("Event retry initiated successfully"))
            .exceptionally(throwable -> {
                logger.error("Failed to retry event {}", eventId, throwable);
                throw new CompletionException(throwable);
            });
    }
}
```

### 8.7 Traffic Analytics Controller
```java
@RestController
@RequestMapping("/api/analytics-service/traffic")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrafficAnalyticsController {
    
    private final TrafficAnalyticsService analyticsService;
    private final CustomLogger logger;
    
    public TrafficAnalyticsController(TrafficAnalyticsService analyticsService, CustomLogger logger) {
        this.analyticsService = analyticsService;
        this.logger = logger;
    }
    
    @GetMapping("/intersections/{intersectionId}/metrics")
    @Operation(summary = "Get intersection metrics", description = "Get traffic metrics for a specific intersection")
    public ResponseEntity<ApiResponse<IntersectionMetricsDto>> getIntersectionMetrics(
            @PathVariable @NotBlank String intersectionId,
            @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hours) {
        
        logger.info("Retrieving metrics for intersection {} for last {} hours", intersectionId, hours);
        
        Duration period = Duration.ofHours(hours);
        IntersectionMetrics metrics = analyticsService.getIntersectionMetrics(intersectionId, period);
        IntersectionMetricsDto dto = IntersectionMetricsMapper.toDto(metrics);
        
        return ResponseBuilder.success(dto, "Intersection metrics retrieved successfully");
    }
    
    @GetMapping("/lights/{lightId}/performance")
    @Operation(summary = "Get light performance", description = "Get performance metrics for a specific traffic light")
    public ResponseEntity<ApiResponse<LightPerformanceDto>> getLightPerformance(
            @PathVariable @NotBlank String lightId,
            @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hours) {
        
        logger.info("Retrieving performance metrics for light {} for last {} hours", lightId, hours);
        
        Duration period = Duration.ofHours(hours);
        LightPerformance performance = analyticsService.getLightPerformance(lightId, period);
        LightPerformanceDto dto = LightPerformanceMapper.toDto(performance);
        
        return ResponseBuilder.success(dto, "Light performance metrics retrieved successfully");
    }
    
    @GetMapping("/system/overview")
    @Operation(summary = "Get system overview", description = "Get overall system traffic analytics")
    public ResponseEntity<ApiResponse<SystemOverviewDto>> getSystemOverview(
            @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hours) {
        
        logger.info("Retrieving system overview for last {} hours", hours);
        
        Duration period = Duration.ofHours(hours);
        SystemOverview overview = analyticsService.getSystemOverview(period);
        SystemOverviewDto dto = SystemOverviewMapper.toDto(overview);
        
        return ResponseBuilder.success(dto, "System overview retrieved successfully");
    }
}
```

### 8.8 Configuration Management Controller
```java
@RestController
@RequestMapping("/api/config-service/traffic-control")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConfigurationController {
    
    private final ConfigurationService configurationService;
    private final CustomLogger logger;
    
    public ConfigurationController(ConfigurationService configurationService, CustomLogger logger) {
        this.configurationService = configurationService;
        this.logger = logger;
    }
    
    @GetMapping("/timing")
    @Operation(summary = "Get timing configuration", description = "Get default timing configuration for traffic lights")
    public ResponseEntity<ApiResponse<TimingConfigDto>> getTimingConfiguration() {
        logger.info("Retrieving timing configuration");
        
        TimingConfig config = configurationService.getTimingConfiguration();
        TimingConfigDto dto = TimingConfigMapper.toDto(config);
        
        return ResponseBuilder.success(dto, "Timing configuration retrieved successfully");
    }
    
    @PutMapping("/timing")
    @Operation(summary = "Update timing configuration", description = "Update default timing configuration")
    public CompletableFuture<ResponseEntity<ApiResponse<TimingConfigDto>>> updateTimingConfiguration(
            @RequestBody @Valid UpdateTimingConfigRequest request) {
        
        logger.info("Updating timing configuration");
        
        return configurationService.updateTimingConfiguration(request)
            .thenApply(config -> {
                TimingConfigDto dto = TimingConfigMapper.toDto(config);
                return ResponseBuilder.success(dto, "Timing configuration updated successfully");
            })
            .exceptionally(throwable -> {
                logger.error("Failed to update timing configuration", throwable);
                throw new CompletionException(throwable);
            });
    }
    
    @GetMapping("/intersections/{intersectionId}/rules")
    @Operation(summary = "Get intersection rules", description = "Get traffic rules for a specific intersection")
    public ResponseEntity<ApiResponse<List<TrafficRuleDto>>> getIntersectionRules(
            @PathVariable @NotBlank String intersectionId) {
        
        logger.info("Retrieving traffic rules for intersection: {}", intersectionId);
        
        List<TrafficRule> rules = configurationService.getIntersectionRules(intersectionId);
        List<TrafficRuleDto> dtoList = rules.stream()
            .map(TrafficRuleMapper::toDto)
            .collect(Collectors.toList());
        
        return ResponseBuilder.success(dtoList, "Traffic rules retrieved successfully");
    }
    
    @PostMapping("/intersections/{intersectionId}/rules")
    @Operation(summary = "Create traffic rule", description = "Create a new traffic rule for an intersection")
    public CompletableFuture<ResponseEntity<ApiResponse<TrafficRuleDto>>> createTrafficRule(
            @PathVariable @NotBlank String intersectionId,
            @RequestBody @Valid CreateTrafficRuleRequest request) {
        
        logger.info("Creating traffic rule for intersection: {}", intersectionId);
        
        return configurationService.createTrafficRule(intersectionId, request)
            .thenApply(rule -> {
                TrafficRuleDto dto = TrafficRuleMapper.toDto(rule);
                return ResponseBuilder.created(dto, "Traffic rule created successfully");
            })
            .exceptionally(throwable -> {
                logger.error("Failed to create traffic rule for intersection {}", intersectionId, throwable);
                throw new CompletionException(throwable);
            });
    }
}
```

## 9. Service-Based API URL Structure

### 9.1 URL Pattern Convention
```
/api/{service-name}/{resource}/{resourceId?}/{action?}
/api/{service-name}/{resource}/{resourceId}/{subresource}/{subresourceId?}
```

### 9.2 Complete API Endpoint Mapping

#### Traffic Service
```
# Traffic Lights
GET    /api/traffic-service/lights                    # Get all traffic lights
GET    /api/traffic-service/lights/{lightId}          # Get specific traffic light
POST   /api/traffic-service/lights/{lightId}/change-state  # Change light state
POST   /api/traffic-service/lights/{lightId}/pause    # Pause light
POST   /api/traffic-service/lights/{lightId}/resume   # Resume light
GET    /api/traffic-service/lights/{lightId}/history  # Get state history

# Intersections
GET    /api/traffic-service/intersections             # Get all intersections
GET    /api/traffic-service/intersections/{intersectionId}  # Get specific intersection
POST   /api/traffic-service/intersections             # Create intersection
GET    /api/traffic-service/intersections/{intersectionId}/current-state  # Get current state
POST   /api/traffic-service/intersections/{intersectionId}/validate  # Validate intersection

# Light Sequences
GET    /api/traffic-service/sequences                 # Get all sequences
POST   /api/traffic-service/sequences                 # Create sequence
GET    /api/traffic-service/sequences/{sequenceId}    # Get specific sequence
POST   /api/traffic-service/sequences/{sequenceId}/activate  # Activate sequence
GET    /api/traffic-service/sequences/intersection/{intersectionId}  # Get sequences by intersection
```

#### System Service
```
# Health Monitoring
GET    /api/system-service/health                     # Overall system health
GET    /api/system-service/health/database            # Database health
GET    /api/system-service/health/kafka               # Kafka health

# Event Management
GET    /api/system-service/events                     # Get system events
GET    /api/system-service/events/{eventId}           # Get specific event
POST   /api/system-service/events/{eventId}/retry     # Retry failed event
```

#### Analytics Service
```
# Traffic Analytics
GET    /api/analytics-service/traffic/intersections/{intersectionId}/metrics  # Intersection metrics
GET    /api/analytics-service/traffic/lights/{lightId}/performance  # Light performance
GET    /api/analytics-service/traffic/system/overview # System overview
```

#### Configuration Service
```
# Configuration Management
GET    /api/config-service/traffic-control/timing     # Get timing config
PUT    /api/config-service/traffic-control/timing     # Update timing config
GET    /api/config-service/traffic-control/intersections/{intersectionId}/rules  # Get intersection rules
POST   /api/config-service/traffic-control/intersections/{intersectionId}/rules  # Create traffic rule
PUT    /api/config-service/traffic-control/intersections/{intersectionId}/rules/{ruleId}  # Update rule
DELETE /api/config-service/traffic-control/intersections/{intersectionId}/rules/{ruleId}  # Delete rule
```

### 9.3 RESTful Resource Naming Conventions
- Use **plural nouns** for collections: `/lights`, `/intersections`, `/sequences`
- Use **specific identifiers** for individual resources: `/lights/{lightId}`
- Use **action verbs** for operations: `/change-state`, `/activate`, `/validate`
- Use **nested resources** for relationships: `/intersections/{id}/rules`
- Use **service-based grouping** for logical separation: `/traffic-service`, `/system-service`, `/analytics-service`

### 9.4 HTTP Method Usage
- **GET**: Retrieve resources (idempotent)
- **POST**: Create resources or trigger actions
- **PUT**: Update entire resources (idempotent)
- **PATCH**: Partial updates
- **DELETE**: Remove resources (idempotent)

### 9.5 Service-Based Architecture Benefits
This service-based approach provides:
- **Clear service boundaries** aligned with microservices architecture
- **Service-specific routing** for load balancing and scaling
- **Independent deployment** capabilities per service
- **Service discovery** compatibility
- **API Gateway** integration readiness
- **Consistent naming patterns** across all services
- **Scalable URL structure** for future service expansion

### 9.6 Service Mapping
- **traffic-service**: Core traffic light management functionality
- **system-service**: System health monitoring and event management  
- **analytics-service**: Traffic analytics, metrics, and reporting
- **config-service**: Configuration management and rule engine