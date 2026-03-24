# Docker Compose Configuration Changes

## Changes Made

### 1. MySQL - Changed to root/root credentials
- `MYSQL_USER`: root (overridable via `${MYSQL_USER:-root}`)
- `MYSQL_PASSWORD`: root (overridable via `${MYSQL_PASSWORD:-root}`)
- Added `command` section with overridable MySQL settings
- Added external volume: `./mysql-data` for database backup/restore

### 2. Kafka Logs Storage
Kafka logs are stored in **3 locations**:
- **Internal volume**: `kafka-logs:/var/log/kafka` (Docker managed)
- **External volume**: `./kafka-logs:/var/log/kafka-external` (accessible from host)
- **Data volume**: `kafka-data:/var/lib/kafka/data` (message data)

### 3. All Services Now Have Overridable Commands
- **MySQL**: Character set, collation, max connections
- **Kafka**: Log retention, segment size, log level
- **Zookeeper**: Port, log level
- **App**: JVM options, Spring profile

### 4. External Volumes Added
- `./mysql-data` - MySQL database files (for backup/restore)
- `./kafka-logs` - Kafka logs (accessible from host)
- `./zookeeper-logs` - Zookeeper logs
- `./app-logs` - Application logs

### 5. Environment Variables
All values can be overridden via `.env` file or environment variables:
- **MySQL**: `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_PORT`
- **Kafka**: `KAFKA_PORT`, `KAFKA_LOG_LEVEL`, `KAFKA_LOG_RETENTION_HOURS`
- **App**: `APP_PORT`, `SPRING_PROFILE`

## Usage

```bash
# Create .env file from example
cp .env.example .env

# Edit .env with your values
# Then run
docker-compose up -d
```

## Override Examples

### Override via .env file
```bash
# .env
MYSQL_USER=myuser
MYSQL_PASSWORD=mypassword
KAFKA_LOG_LEVEL=INFO
APP_PORT=8080
```

### Override via command line
```bash
# Override MySQL credentials
MYSQL_USER=admin MYSQL_PASSWORD=secret docker-compose up -d

# Override Kafka port
KAFKA_PORT=9094 docker-compose up -d

# Override application port and profile
APP_PORT=8080 SPRING_PROFILE=dev docker-compose up -d
```

### Override app command in docker-compose
```yaml
services:
  app:
    command:
      - -Xms1g
      - -Xmx2g
      - -jar
      - app.jar
      - --spring.profiles.active=dev
```

## Accessing External Volumes

### MySQL Data
```bash
# Backup database
docker exec traffic-mysql mysqldump -u root -proot traffic_light_prod > ./mysql-data/backup.sql

# Restore database
docker exec -i traffic-mysql mysql -u root -proot traffic_light_prod < ./mysql-data/backup.sql
```

### Kafka Logs
```bash
# View Kafka logs from host
tail -f ./kafka-logs/server.log

# View all Kafka logs
ls -la ./kafka-logs/
```

### Application Logs
```bash
# View application logs from host
tail -f ./app-logs/traffic-light-service.log

# View all application logs
ls -la ./app-logs/
```

## Default Credentials

| Service | Username | Password | Port |
|---------|----------|----------|------|
| MySQL | root | root | 3306 |
| Kafka UI | - | - | 8080 |
| Grafana | admin | admin | 3000 |
| Prometheus | - | - | 9090 |
| Application | - | - | 9900 |

## Volume Locations

| Volume Name | Internal Path | External Path | Purpose |
|-------------|---------------|---------------|---------|
| mysql-data | /var/lib/mysql | ./mysql-data | MySQL database files |
| kafka-data | /var/lib/kafka/data | - | Kafka message data |
| kafka-logs | /var/log/kafka | ./kafka-logs | Kafka logs |
| zookeeper-data | /var/lib/zookeeper/data | - | Zookeeper data |
| zookeeper-logs | /var/lib/zookeeper/log | ./zookeeper-logs | Zookeeper logs |
| app-logs | /app/logs | ./app-logs | Application logs |
| prometheus-data | /prometheus | - | Prometheus metrics |
| grafana-data | /var/lib/grafana | - | Grafana dashboards |
