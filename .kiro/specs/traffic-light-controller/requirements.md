# Traffic Light Controller API - Requirements

## 1. Overview

This is a coding kata for developing a Traffic Light Controller API system that manages traffic lights at intersections. The system should demonstrate enterprise-level Java development practices with Spring Boot, MySQL, and Kafka integration.

## 2. Business Requirements

### 2.1 Core Functionality
- **BR-1**: The system shall manage traffic light states (red, yellow, green) for multiple directions at an intersection
- **BR-2**: The system shall accept commands to change light sequences, pause, or resume operation
- **BR-3**: The system shall validate that conflicting directions are never green simultaneously
- **BR-4**: The system shall provide current state and timing history via REST API
- **BR-5**: The system shall be designed for concurrency and future expansion to multiple intersections

### 2.2 Safety Requirements
- **SR-1**: No two conflicting directions shall have green lights simultaneously
- **SR-2**: System shall maintain safe default states during failures
- **SR-3**: All state transitions shall be logged and auditable

### 2.3 Performance Requirements
- **PR-1**: System shall handle concurrent requests from multiple clients
- **PR-2**: State changes shall be processed within 100ms
- **PR-3**: System shall support future scaling to 1000+ intersections

## 3. Technical Requirements

### 3.1 Technology Stack
- **Java 17** with Spring Boot 3.1
- **MySQL** database for persistence
- **Kafka** for event-driven architecture
- **No Lombok** - use custom builders
- **Custom logging** implementation
- **RestClient/WebClient** for HTTP calls (no RestTemplate)

### 3.2 Architecture Requirements
- **AR-1**: Implement choreography-based event-driven architecture using Kafka
- **AR-2**: Use SOLID principles and appropriate design patterns
- **AR-3**: Implement separation of concerns with modular programming
- **AR-4**: Use functional programming with Lambda expressions
- **AR-5**: Implement custom data structures for optimization

### 3.3 Concurrency Requirements
- **CR-1**: Use custom thread pools with CompletableFuture
- **CR-2**: Implement ConcurrentMap, ReentrantReadWriteLock, ReentrantLock
- **CR-3**: Use volatile variables where appropriate
- **CR-4**: Implement proper transaction management with isolation levels

### 3.4 Data Requirements
- **DR-1**: Persist traffic light states and history in MySQL
- **DR-2**: Use optimistic and pessimistic locking strategies
- **DR-3**: Implement proper transaction propagation (REQUIRED/REQUIRES_NEW)
- **DR-4**: Store timing history for audit and analysis

### 3.5 Integration Requirements
- **IR-1**: Publish state change events to Kafka topics
- **IR-2**: Implement Kafka DLT (Dead Letter Topic) retry mechanism
- **IR-3**: Use circuit breakers for external service calls
- **IR-4**: Implement event listeners and interceptors

### 3.6 Quality Requirements
- **QR-1**: Code must be test-driven with comprehensive unit and integration tests
- **QR-2**: Tests shall read like documentation
- **QR-3**: Code must compile and run without errors
- **QR-4**: Implement proper exception handling and logging

## 4. User Stories

### 4.1 Traffic Light Management
**As a** traffic control operator  
**I want to** control traffic light states at an intersection  
**So that** I can manage traffic flow safely and efficiently

**Acceptance Criteria:**
- Can change light sequences for different directions
- Can pause and resume traffic light operation
- System prevents conflicting green lights
- All changes are logged with timestamps

### 4.2 State Monitoring
**As a** traffic control operator  
**I want to** view current traffic light states and history  
**So that** I can monitor system operation and troubleshoot issues

**Acceptance Criteria:**
- Can retrieve current state of all lights
- Can view timing history for analysis
- Can filter history by time range and direction
- Real-time updates via events

### 4.3 System Administration
**As a** system administrator  
**I want to** configure and monitor the traffic light system  
**So that** I can ensure reliable operation and plan for expansion

**Acceptance Criteria:**
- Can configure timing parameters
- Can monitor system health and performance
- Can handle multiple intersections
- Can scale system resources

## 5. API Endpoints

### 5.1 Traffic Light Control
- `POST /api/v1/intersections/{id}/lights/change` - Change light sequence
- `POST /api/v1/intersections/{id}/pause` - Pause operation
- `POST /api/v1/intersections/{id}/resume` - Resume operation
- `GET /api/v1/intersections/{id}/status` - Get current status

### 5.2 History and Monitoring
- `GET /api/v1/intersections/{id}/history` - Get timing history
- `GET /api/v1/intersections/{id}/events` - Get event stream
- `GET /api/v1/health` - System health check

## 6. Data Model

### 6.1 Core Entities
- **Intersection**: Represents a traffic intersection
- **TrafficLight**: Individual light with direction and state
- **LightState**: Enumeration (RED, YELLOW, GREEN)
- **Direction**: Enumeration (NORTH, SOUTH, EAST, WEST)
- **StateHistory**: Historical record of state changes
- **LightSequence**: Configuration for light timing

### 6.2 Events
- **LightStateChangedEvent**: Published when light state changes
- **IntersectionPausedEvent**: Published when intersection is paused
- **IntersectionResumedEvent**: Published when intersection is resumed
- **ConflictDetectedEvent**: Published when safety violation is detected

## 7. Non-Functional Requirements

### 7.1 Scalability
- Support horizontal scaling
- Database connection pooling
- Kafka partition strategy for load distribution

### 7.2 Reliability
- Implement retry mechanisms with exponential backoff
- Circuit breaker pattern for external dependencies
- Graceful degradation during failures

### 7.3 Security
- Input validation and sanitization
- Audit logging for all operations
- Rate limiting for API endpoints

### 7.4 Monitoring
- Custom metrics and health checks
- Structured logging with correlation IDs
- Performance monitoring and alerting

## 8. Constraints

### 8.1 Time Constraints
- Development timeboxed to 3-4 hours
- Focus on code quality over feature completeness
- Document any incomplete features in README

### 8.2 Technical Constraints
- Must use specified technology stack
- No external libraries beyond approved list
- Code must compile and demonstrate key concepts

## 9. Success Criteria

### 9.1 Functional Success
- All core traffic light operations work correctly
- Safety constraints are enforced
- API endpoints respond correctly
- Database persistence works

### 9.2 Technical Success
- Clean, readable code that tells a story
- Comprehensive test coverage
- Proper use of design patterns and SOLID principles
- Effective use of concurrency and async programming
- Working Kafka integration

### 9.3 Quality Success
- Tests serve as documentation
- Good Git commit history
- Proper error handling and logging
- Performance considerations addressed