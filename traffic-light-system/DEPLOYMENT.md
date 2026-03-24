# Deployment Guide - Traffic Light Controller API

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Database Setup](#database-setup)
4. [Kafka Setup](#kafka-setup)
5. [Application Deployment](#application-deployment)
6. [Docker Deployment](#docker-deployment)
7. [Kubernetes Deployment](#kubernetes-deployment)
8. [Monitoring Setup](#monitoring-setup)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

**Minimum Requirements**:
- CPU: 2 cores
- RAM: 4 GB
- Disk: 20 GB
- OS: Linux (Ubuntu 20.04+), Windows Server 2019+, macOS 11+

**Recommended Requirements**:
- CPU: 4+ cores
- RAM: 8+ GB
- Disk: 50+ GB SSD
- OS: Linux (Ubuntu 22.04 LTS)

### Software Requirements

- Java 17 or higher
- Maven 3.8+
- MySQL 8.0+
- Apache Kafka 3.x
- Docker 20.10+ (optional)
- Kubernetes 1.24+ (optional)

## Environment Setup

### 1. Install Java 17

**Ubuntu/Debian**:
```bash
sudo apt update
sudo apt install openjdk-17-jdk
java -version
```

**CentOS/RHEL**:
```bash
sudo yum install java-17-openjdk-devel
java -version
```

**Windows**:
Download and install from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [Adoptium](https://adoptium.net/)

### 2. Install Maven

**Ubuntu/Debian**:
```bash
sudo apt install maven
mvn -version
```

**Windows**:
Download from [Apache Maven](https://maven.apache.org/download.cgi) and add to PATH

### 3. Set Environment Variables

**Linux/Mac** (`~/.bashrc` or `~/.zshrc`):
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export MAVEN_HOME=/usr/share/maven
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH

# Application specific
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=traffic_light_prod
export DB_USERNAME=traffic_user
export DB_PASSWORD=secure_password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Windows** (System Environment Variables):
```
JAVA_HOME=C:\Program Files\Java\jdk-17
MAVEN_HOME=C:\Program Files\Apache\maven
PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
```

## Database Setup

### 1. Install MySQL

**Ubuntu/Debian**:
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
sudo mysql_secure_installation
```

**Docker**:
```bash
docker run -d \
  --name mysql-traffic \
  -e MYSQL_ROOT_PASSWORD=root_password \
  -e MYSQL_DATABASE=traffic_light_prod \
  -e MYSQL_USER=traffic_user \
  -e MYSQL_PASSWORD=secure_password \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  mysql:8.0
```

### 2. Create Database and User

```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE traffic_light_prod CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'traffic_user'@'%' IDENTIFIED BY 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON traffic_light_prod.* TO 'traffic_user'@'%';
FLUSH PRIVILEGES;

-- Verify
SHOW DATABASES;
SELECT User, Host FROM mysql.user WHERE User='traffic_user';
```

### 3. Run Database Migrations

Flyway migrations run automatically on application startup. To run manually:

```bash
mvn flyway:migrate \
  -Dflyway.url=jdbc:mysql://localhost:3306/traffic_light_prod \
  -Dflyway.user=traffic_user \
  -Dflyway.password=secure_password
```

## Kafka Setup

### 1. Install Kafka

**Download and Extract**:
```bash
wget https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz
tar -xzf kafka_2.13-3.6.0.tgz
cd kafka_2.13-3.6.0
```

**Docker Compose**:
```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

### 2. Start Kafka

**Manual Start**:
```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties &

# Start Kafka
bin/kafka-server-start.sh config/server.properties &
```

**Docker Compose**:
```bash
docker-compose up -d
```

### 3. Create Kafka Topics

**Windows PowerShell**:
```powershell
.\create-kafka-topics-traffic-light.ps1
```

**Linux/Mac**:
```bash
chmod +x create-kafka-topics-traffic-light.sh
./create-kafka-topics-traffic-light.sh
```

**Manual Creation**:
```bash
# Core event topics
bin/kafka-topics.sh --create --topic traffic-light-state-changed \
  --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

bin/kafka-topics.sh --create --topic intersection-updated \
  --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

bin/kafka-topics.sh --create --topic system-health-check \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

# Dead Letter Topics
bin/kafka-topics.sh --create --topic traffic-light-state-changed-DLT \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

bin/kafka-topics.sh --create --topic intersection-updated-DLT \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

bin/kafka-topics.sh --create --topic system-events-DLT \
  --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

### 4. Verify Topics

```bash
bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

## Application Deployment

### 1. Build Application

```bash
# Clone repository
git clone https://github.com/your-org/traffic-light-controller.git
cd traffic-light-controller

# Build with tests
mvn clean package

# Build without tests (faster)
mvn clean package -DskipTests
```

### 2. Configure Application

Create `application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:traffic_light_prod}
    username: ${DB_USERNAME:traffic_user}
    password: ${DB_PASSWORD:secure_password}
  
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    com.trafficlight: INFO
  file:
    name: /var/log/traffic-light-controller/application.log
```

### 3. Run Application

**Development**:
```bash
java -jar target/traffic-light-service-1.0.0.jar
```

**Production**:
```bash
java -jar target/traffic-light-service-1.0.0.jar \
  --spring.profiles.active=prod \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200
```

**As Systemd Service** (`/etc/systemd/system/traffic-light.service`):
```ini
[Unit]
Description=Traffic Light Controller API
After=network.target mysql.service

[Service]
Type=simple
User=traffic
WorkingDirectory=/opt/traffic-light
ExecStart=/usr/bin/java -jar /opt/traffic-light/traffic-light-service-1.0.0.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

Environment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
Environment="DB_HOST=localhost"
Environment="DB_USERNAME=traffic_user"
Environment="DB_PASSWORD=secure_password"
Environment="KAFKA_BOOTSTRAP_SERVERS=localhost:9092"

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable traffic-light
sudo systemctl start traffic-light
sudo systemctl status traffic-light
```

### 4. Verify Deployment

```bash
# Check health
curl http://localhost:9900/actuator/health

# Check metrics
curl http://localhost:9900/actuator/metrics

# Test API
curl http://localhost:9900/api/intersections
```

## Docker Deployment

### 1. Create Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="traffic-light-team@example.com"
LABEL version="1.0.0"
LABEL description="Traffic Light Controller API"

# Create app directory
WORKDIR /app

# Copy JAR file
COPY target/traffic-light-service-1.0.0.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port
EXPOSE 9900

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9900/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--spring.profiles.active=prod"]
```

### 2. Build Docker Image

```bash
# Build image
docker build -t traffic-light-controller:1.0.0 .

# Tag for registry
docker tag traffic-light-controller:1.0.0 your-registry/traffic-light-controller:1.0.0

# Push to registry
docker push your-registry/traffic-light-controller:1.0.0
```

### 3. Docker Compose Deployment

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: traffic-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: traffic_light_prod
      MYSQL_USER: traffic_user
      MYSQL_PASSWORD: secure_password
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - traffic-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: traffic-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - traffic-network

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: traffic-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    networks:
      - traffic-network
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    image: traffic-light-controller:1.0.0
    container_name: traffic-app
    depends_on:
      mysql:
        condition: service_healthy
      kafka:
        condition: service_healthy
    ports:
      - "9900:9900"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: traffic_light_prod
      DB_USERNAME: traffic_user
      DB_PASSWORD: secure_password
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    networks:
      - traffic-network
    restart: unless-stopped

volumes:
  mysql-data:

networks:
  traffic-network:
    driver: bridge
```

### 4. Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Kubernetes Deployment

### 1. Create Kubernetes Manifests

**Namespace** (`k8s/namespace.yaml`):
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: traffic-light
```

**ConfigMap** (`k8s/configmap.yaml`):
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: traffic-light-config
  namespace: traffic-light
data:
  application.yml: |
    spring:
      profiles:
        active: prod
```

**Secret** (`k8s/secret.yaml`):
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: traffic-light-secret
  namespace: traffic-light
type: Opaque
stringData:
  db-username: traffic_user
  db-password: secure_password
```

**Deployment** (`k8s/deployment.yaml`):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: traffic-light-controller
  namespace: traffic-light
spec:
  replicas: 3
  selector:
    matchLabels:
      app: traffic-light-controller
  template:
    metadata:
      labels:
        app: traffic-light-controller
    spec:
      containers:
      - name: app
        image: your-registry/traffic-light-controller:1.0.0
        ports:
        - containerPort: 9900
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          value: "mysql-service"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: traffic-light-secret
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: traffic-light-secret
              key: db-password
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: "kafka-service:9092"
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 9900
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 9900
          initialDelaySeconds: 30
          periodSeconds: 5
```

**Service** (`k8s/service.yaml`):
```yaml
apiVersion: v1
kind: Service
metadata:
  name: traffic-light-service
  namespace: traffic-light
spec:
  type: LoadBalancer
  selector:
    app: traffic-light-controller
  ports:
  - protocol: TCP
    port: 80
    targetPort: 9900
```

### 2. Deploy to Kubernetes

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Create configmap and secret
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

# Check status
kubectl get pods -n traffic-light
kubectl get svc -n traffic-light

# View logs
kubectl logs -f deployment/traffic-light-controller -n traffic-light
```

## Monitoring Setup

### 1. Prometheus

**prometheus.yml**:
```yaml
scrape_configs:
  - job_name: 'traffic-light-controller'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:9900']
```

### 2. Grafana Dashboard

Import dashboard ID: 4701 (JVM Micrometer)

Custom metrics:
- `traffic_light_state_changes_total`
- `traffic_light_intersections_created_total`
- `traffic_light_validation_errors_total`

## Troubleshooting

### Application Won't Start

**Check logs**:
```bash
# Systemd
sudo journalctl -u traffic-light -f

# Docker
docker logs traffic-app

# Kubernetes
kubectl logs deployment/traffic-light-controller -n traffic-light
```

### Database Connection Issues

```bash
# Test connection
mysql -h localhost -u traffic_user -p traffic_light_prod

# Check if MySQL is running
sudo systemctl status mysql

# Check firewall
sudo ufw status
sudo ufw allow 3306/tcp
```

### Kafka Connection Issues

```bash
# Check Kafka status
bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# List topics
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

# Check consumer groups
bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

### Performance Issues

```bash
# Check JVM metrics
curl http://localhost:9900/actuator/metrics/jvm.memory.used

# Check thread pool
curl http://localhost:9900/actuator/metrics/executor.active

# Check database connections
curl http://localhost:9900/actuator/metrics/hikaricp.connections.active
```

## Rollback Procedure

### Application Rollback

```bash
# Systemd
sudo systemctl stop traffic-light
sudo cp /opt/traffic-light/backup/traffic-light-service-0.9.0.jar /opt/traffic-light/traffic-light-service-1.0.0.jar
sudo systemctl start traffic-light

# Docker
docker-compose down
docker-compose up -d --force-recreate

# Kubernetes
kubectl rollout undo deployment/traffic-light-controller -n traffic-light
```

### Database Rollback

```bash
# Flyway rollback (if configured)
mvn flyway:undo

# Manual rollback
mysql -u traffic_user -p traffic_light_prod < backup.sql
```

## Backup and Recovery

### Database Backup

```bash
# Full backup
mysqldump -u traffic_user -p traffic_light_prod > backup_$(date +%Y%m%d).sql

# Automated backup script
0 2 * * * /usr/bin/mysqldump -u traffic_user -p'password' traffic_light_prod > /backup/traffic_$(date +\%Y\%m\%d).sql
```

### Application Backup

```bash
# Backup configuration
tar -czf config_backup_$(date +%Y%m%d).tar.gz /opt/traffic-light/config/

# Backup logs
tar -czf logs_backup_$(date +%Y%m%d).tar.gz /var/log/traffic-light-controller/
```

## Security Checklist

- [ ] Change default passwords
- [ ] Enable SSL/TLS for MySQL
- [ ] Enable SSL/TLS for Kafka
- [ ] Configure firewall rules
- [ ] Enable audit logging
- [ ] Set up log rotation
- [ ] Configure backup retention
- [ ] Enable monitoring alerts
- [ ] Review security vulnerabilities
- [ ] Update dependencies regularly

## Support

For deployment support:
- Email: devops@trafficlight.com
- Documentation: https://docs.trafficlight.com/deployment
- Issue Tracker: https://github.com/your-org/traffic-light-controller/issues
