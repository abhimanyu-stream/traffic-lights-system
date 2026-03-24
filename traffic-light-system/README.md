# Traffic Light Controller API

Enterprise-grade REST API for managing traffic intersections, traffic lights, and state transitions with real-time event processing.

## Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Architecture](#architecture)
- [Contributing](#contributing)

## Features

- **Intersection Management**: Create, update, and manage traffic intersections
- **Traffic Light Control**: Control individual traffic lights with state management
- **Light Sequences**: Define and activate timing sequences for traffic lights
- **Real-time Events**: Kafka-based event streaming for state changes
- **Concurrency Control**: Thread-safe operations with optimistic/pessimistic locking
- **Health Monitoring**: Comprehensive health checks and metrics
- **Audit Trail**: Complete history of state changes and system events
- **Rate Limiting**: Built-in rate limiting using Resilience4j
- **Correlation ID Tracking**: Request tracing across distributed systems

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** with Hibernate
- **MySQL 8.0** - Primary database
- **Apache Kafka** - Event streaming
- **Resilience4j** - Rate limiting
- **Micrometer** - Metrics and monitoring
- **SpringDoc OpenAPI** - API documentation
- **Maven** - Build tool
- **JUnit 5 & jqwik** - Testing framework

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- **Apache Kafka 3.x**
- **Git**

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/traffic-light-controller.git
cd traffic-light-controller
```

### 2. Set Up MySQL Database

Create the database:

```sql
CREATE DATABASE traffic_light_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'traffic_user'@'localhost' IDENTIFIED BY 'traffic_password';
GRANT ALL PRIVILEGES ON traffic_light_db.* TO 'traffic_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Set Up Kafka

Start Zookeeper:

```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```

Start Kafka:

```bash
bin/kafka-server-start.sh config/server.properties
```

Create Kafka topics:

```bash
# Windows PowerShell
.\create-kafka-topics-traffic-light.ps1

# Linux/Mac
./create-kafka-topics-traffic-light.sh
```

Or manually:

```bash
bin/kafka-topics.sh --create --topic traffic-light-state-changed --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
bin/kafka-topics.sh --create --topic intersection-updated --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
bin/kafka-topics.sh --create --topic system-health-check --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### 4. Build the Application

```bash
mvn clean install
```

## Configuration

### Application Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/traffic_light_db
    username: traffic_user
    password: traffic_password
  
  kafka:
    bootstrap-servers: localhost:9092
```

### Environment-Specific Configuration

The application supports three profiles:

- **dev** (default): Development environment
- **test**: Testing environment
- **prod**: Production environment

Activate a profile:

```bash
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Using Java
java -jar target/traffic-light-service-1.0.0.jar --spring.profiles.active=prod
```

### Environment Variables

For production, use environment variables:

```bash
export DB_HOST=mysql-server
export DB_PORT=3306
export DB_NAME=traffic_light_prod
export DB_USERNAME=traffic_user
export DB_PASSWORD=secure_password
export KAFKA_BOOTSTRAP_SERVERS=kafka-server:9092
```

## Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Production Mode

```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/traffic-light-service-1.0.0.jar
```

### Using Docker (Optional)

```bash
# Build Docker image
docker build -t traffic-light-controller:1.0.0 .

# Run container
docker run -p 9900:9900 \
  -e DB_HOST=mysql \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  traffic-light-controller:1.0.0
```

### Verify Application is Running

Check health endpoint:

```bash
curl http://localhost:9900/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

## API Documentation

### Swagger UI

Access interactive API documentation:

```
http://localhost:9900/swagger-ui.html
```

### OpenAPI Specification

JSON format:

```
http://localhost:9900/v3/api-docs
```

YAML format:

```
http://localhost:9900/v3/api-docs.yaml
```

### API Examples

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for detailed API usage examples.

## Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=TrafficLightServiceTest
```

### Run Integration Tests

```bash
mvn verify -P integration-tests
```

### Run Property-Based Tests

```bash
mvn test -Dtest=*Properties
```

### Test Coverage

Generate coverage report:

```bash
mvn jacoco:report
```

View report at: `target/site/jacoco/index.html`

## Architecture

### High-Level Architecture

```
┌─────────────────┐
│   REST API      │
│  (Controllers)  │
└────────┬────────┘
         │
┌────────▼────────┐
│   Service       │
│    Layer        │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼────┐
│  JPA  │ │ Kafka │
│ Repos │ │Events │
└───┬───┘ └───────┘
    │
┌───▼───┐
│ MySQL │
└───────┘
```

### Key Components

- **Controllers**: REST API endpoints
- **Services**: Business logic and coordination
- **Repositories**: Data access layer
- **Events**: Kafka event publishers and listeners
- **Domain**: Entity models and value objects
- **Config**: Application configuration

### Design Patterns

- **Repository Pattern**: Data access abstraction
- **Builder Pattern**: Complex object construction
- **Rate Limiting**: API protection using Resilience4j
- **Event-Driven**: Asynchronous event processing
- **Optimistic Locking**: Concurrent modification handling

## Project Structure

```
traffic-light-system/
├── src/
│   ├── main/
│   │   ├── java/com/trafficlight/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── domain/          # Entity models
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── enums/           # Enumerations
│   │   │   ├── events/          # Kafka events
│   │   │   ├── exception/       # Custom exceptions
│   │   │   ├── filters/         # Servlet filters
│   │   │   ├── health/          # Health indicators
│   │   │   ├── listeners/       # Kafka listeners
│   │   │   ├── metrics/         # Custom metrics
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── service/         # Business services
│   │   │   ├── utils/           # Utility classes
│   │   │   └── validation/      # Custom validators
│   │   └── resources/
│   │       ├── application.yml  # Configuration
│   │       ├── logback-spring.xml
│   │       └── db/migration/    # Flyway migrations
│   └── test/                    # Test classes
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

## Monitoring and Observability

### Actuator Endpoints

Available at `/actuator`:

- `/actuator/health` - Health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/info` - Application info

### Logging

Logs are written to:

- Console (stdout)
- File: `logs/traffic-light-controller.log`

Log levels can be configured in `application.yml`.

### Metrics

Metrics are exposed in Prometheus format at:

```
http://localhost:9900/actuator/prometheus
```

## Troubleshooting

### Common Issues

**Issue**: Application fails to start with database connection error

**Solution**: Verify MySQL is running and credentials are correct

```bash
mysql -u traffic_user -p -h localhost traffic_light_db
```

**Issue**: Kafka connection timeout

**Solution**: Ensure Kafka and Zookeeper are running

```bash
# Check Kafka status
bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

**Issue**: Port 9900 already in use

**Solution**: Change port in `application.yml` or stop the process using the port

```bash
# Windows
netstat -ano | findstr :9900

# Linux/Mac
lsof -i :9900
```

## Performance Tuning

### Database Connection Pool

Adjust HikariCP settings in `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
```

### Thread Pool Configuration

Adjust thread pool settings:

```yaml
traffic-light:
  thread-pool:
    core-size: 20
    max-size: 100
```

### Kafka Performance

Adjust Kafka producer/consumer settings:

```yaml
spring:
  kafka:
    producer:
      batch-size: 32768
    consumer:
      max-poll-records: 500
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Contact

- **Team**: Traffic Light Controller Team
- **Email**: support@trafficlight.com
- **Documentation**: https://docs.trafficlight.com

## Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka for event streaming
- Resilience4j for fault tolerance patterns
