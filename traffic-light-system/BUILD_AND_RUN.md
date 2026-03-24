# Traffic Light System - Build and Run Guide

## Build Status
✅ **Compilation Successful**
✅ **Package Build Successful**

## Fixed Compilation Errors

### Issues Fixed:
1. **StateHistory Builder Pattern** - Updated to use Builder pattern instead of setters
2. **TrafficLight Builder Pattern** - Updated to use Builder pattern instead of setters
3. **Intersection Builder Pattern** - Updated to use Builder pattern instead of setters
4. **StateChangeResponse Builder** - Fixed builder method names
5. **AsyncTrafficLightService** - Fixed type mismatches (String vs Long for intersectionId)
6. **TransactionalTrafficLightService** - Fixed exception handling and builder usage
7. **IntersectionBuilder** - Updated to use domain entity builders
8. **TrafficLightBuilder** - Updated to use domain entity builders
9. **RateLimitingFilter** - Fixed HTTP status code constant
10. **StateCache** - Fixed type mismatch (String vs Long for intersectionId)
11. **Repository Methods** - Removed non-existent method calls

### Total Errors Fixed: 43 → 0

## Prerequisites

### Required Services:
1. **MySQL 8.0+**
   - Database: `traffic_light_db`
   - Username: `root`
   - Password: `root`
   - Port: `3306`

2. **Apache Kafka**
   - Bootstrap Server: `localhost:9092`
   - Zookeeper: `localhost:2181`

3. **Java 17+**
   - Required for running the application

## Running the Application

### Option 1: Run with Docker Compose (Recommended)

```bash
# Navigate to project directory
cd traffic-light-system

# Start all services (MySQL, Kafka, Zookeeper, Application)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down
```

### Option 2: Run Locally (Manual Setup)

#### Step 1: Start MySQL
```bash
# Using Docker
docker run -d \
  --name traffic-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=traffic_light_db \
  -p 3306:3306 \
  mysql:8.0

# Or use your local MySQL installation
```

#### Step 2: Start Kafka and Zookeeper
```bash
# Start Zookeeper
docker run -d \
  --name traffic-zookeeper \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  confluentinc/cp-zookeeper:7.5.0

# Start Kafka
docker run -d \
  --name traffic-kafka \
  -p 9092:9092 \
  -p 9093:9093 \
  -e KAFKA_BROKER_ID=1 \
  -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,PLAINTEXT_HOST://localhost:9093 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:7.5.0
```

#### Step 3: Run the Application
```bash
# Using Maven
mvn spring-boot:run

# Or using the JAR file
java -jar target/traffic-light-service-1.0.0.jar

# With custom profile
java -jar target/traffic-light-service-1.0.0.jar --spring.profiles.active=prod
```

### Option 3: Run with PowerShell Scripts

```powershell
# Start Kafka and Zookeeper
.\clean-start-kafka.ps1

# Create Kafka topics
.\create-kafka-topics-traffic-light.ps1

# Run the application
mvn spring-boot:run
```

## Verify Application is Running

### Health Check
```bash
curl http://localhost:9900/actuator/health
```

### API Documentation
Open in browser:
- Swagger UI: http://localhost:9900/swagger-ui.html
- API Docs: http://localhost:9900/v3/api-docs

### Monitoring
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)
- Kafka UI: http://localhost:8080

## Application Endpoints

Base URL: `http://localhost:9900/api/traffic-service`

### Traffic Lights
- `GET /lights` - Get all traffic lights
- `GET /lights/{lightId}` - Get traffic light by ID
- `POST /lights` - Create new traffic light
- `PUT /lights/{lightId}/state` - Change traffic light state
- `DELETE /lights/{lightId}` - Delete traffic light

### Intersections
- `GET /intersections` - Get all intersections
- `GET /intersections/{id}` - Get intersection by ID
- `POST /intersections` - Create new intersection
- `PUT /intersections/{id}` - Update intersection
- `DELETE /intersections/{id}` - Delete intersection

### System
- `GET /system/health` - System health check
- `GET /system/events` - Get system events
- `GET /system/metrics` - Get system metrics

## Configuration

### Environment Variables
Create a `.env` file in the project root:

```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=traffic_light_db
MYSQL_USER=root
MYSQL_PASSWORD=root
MYSQL_PORT=3306

# Kafka Configuration
KAFKA_PORT=9092
KAFKA_EXTERNAL_PORT=9093

# Application Configuration
APP_PORT=9900
SPRING_PROFILE=dev
```

### Application Profiles
- `dev` - Development profile (default)
- `prod` - Production profile
- `test` - Testing profile

## Troubleshooting

### Port Already in Use
```bash
# Check what's using port 9900
netstat -ano | findstr :9900

# Kill the process
taskkill /PID <process_id> /F
```

### MySQL Connection Issues
```bash
# Check MySQL is running
docker ps | findstr mysql

# Check MySQL logs
docker logs traffic-mysql

# Test connection
mysql -h localhost -u root -proot -e "SELECT 1"
```

### Kafka Connection Issues
```bash
# Check Kafka is running
docker ps | findstr kafka

# Check Kafka logs
docker logs traffic-kafka

# List topics
docker exec traffic-kafka kafka-topics --list --bootstrap-server localhost:9092
```

### Application Logs
```bash
# View application logs
tail -f logs/traffic-light-service.log

# View Docker logs
docker-compose logs -f app
```

## Build Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package (skip tests)
mvn package -DskipTests

# Clean install
mvn clean install

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## Next Steps

1. ✅ Compilation fixed
2. ✅ Build successful
3. ⏳ Start MySQL and Kafka
4. ⏳ Run the application
5. ⏳ Test the endpoints
6. ⏳ Monitor with Prometheus/Grafana

## Notes

- The application uses port **9900** by default
- MySQL database will be created automatically if it doesn't exist
- Kafka topics will be auto-created when the application starts
- All logs are stored in the `logs/` directory
- External volumes for Docker are mapped to local directories for easy access
