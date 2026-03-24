# Clean Start Kafka - Traffic Light Controller System
#
# This script manages Kafka and ZooKeeper startup with optional data cleanup
# for the Traffic Light Controller API system.
#
# LOG LOCATIONS (All on E: drive):
# - Application Logs: E:\Installation\Logs\kafkalogs\application-logs\
#   * server.log          - Main Kafka/ZooKeeper logs
#   * controller.log      - Kafka controller operations
#   * state-change.log    - Partition state changes
#   * kafka-request.log   - Client request logs
#   * log-cleaner.log     - Log compaction operations
#   * kafka-authorizer.log - Authorization decisions
#
# - Kafka Data: E:\Installation\Logs\kafkalogs\kafka-logs\
# - ZooKeeper Data: E:\Installation\Logs\kafkalogs\zookeeper\
# - Kafka Streams: E:\Installation\Logs\kafkalogs\kafka-streams-state\
# - KRaft Logs: E:\Installation\Logs\kafkalogs\kraft-*\
#
# Configuration:
# - Application logs configured in: C:\kafka\bin\windows\kafka-run-class.bat
# - Kafka data configured in: C:\kafka\config\server.properties
# - ZooKeeper data configured in: C:\kafka\config\zookeeper.properties

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Clean Start Kafka - Traffic Light Controller" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stop all processes
Write-Host "Step 1: Stopping all Kafka/Zookeeper processes..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "  Stopping $($javaProcesses.Count) Java process(es)..." -ForegroundColor Gray
    $javaProcesses | Stop-Process -Force
    Start-Sleep -Seconds 3
    Write-Host "  ✓ Stopped" -ForegroundColor Green
} else {
    Write-Host "  No Java processes running" -ForegroundColor Gray
}

# Step 2: Confirm data cleanup
Write-Host "`nStep 2: Data cleanup confirmation..." -ForegroundColor Yellow
Write-Host "  This will delete all Kafka and Zookeeper data from:" -ForegroundColor White
Write-Host "    - E:\Installation\Logs\kafkalogs\kafka-logs" -ForegroundColor Gray
Write-Host "    - E:\Installation\Logs\kafkalogs\kafka-streams-state\traffic-light-streams" -ForegroundColor Gray
Write-Host "    - E:\Installation\Logs\kafkalogs\zookeeper" -ForegroundColor Gray
Write-Host "    - E:\Installation\Logs\kafkalogs\kraft-broker-logs" -ForegroundColor Gray
Write-Host "    - E:\Installation\Logs\kafkalogs\kraft-controller-logs" -ForegroundColor Gray
Write-Host "    - E:\Installation\Logs\kafkalogs\kraft-combined-logs" -ForegroundColor Gray
Write-Host "    - E:\Installation\Logs\kafkalogs\application-logs (server.log, etc.)" -ForegroundColor Gray
Write-Host "    - C:\kafka\logs (old application logs)" -ForegroundColor Gray
Write-Host ""
Write-Host "  ⚠ This will reset all traffic light system data!" -ForegroundColor Yellow
Write-Host ""
$cleanData = Read-Host "  Do you want to clean all data? (y/n)"

if ($cleanData -eq 'y' -or $cleanData -eq 'Y') {
    Write-Host "`nStep 3: Cleaning Kafka data..." -ForegroundColor Yellow

    $kafkaDataPaths = @(
        "E:\Installation\Logs\kafkalogs\kafka-logs",
        "E:\Installation\Logs\kafkalogs\kafka-streams-state\traffic-light-streams",
        "E:\Installation\Logs\kafkalogs\kraft-broker-logs",
        "E:\Installation\Logs\kafkalogs\kraft-controller-logs",
        "E:\Installation\Logs\kafkalogs\kraft-combined-logs",
        "E:\Installation\Logs\kafkalogs\application-logs",
        "C:\kafka\logs"
    )

    foreach ($path in $kafkaDataPaths) {
        if (Test-Path $path) {
            Write-Host "  Cleaning $path..." -ForegroundColor Gray
            try {
                Remove-Item -Path "$path\*" -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host "  ✓ Cleaned $path" -ForegroundColor Green
            } catch {
                Write-Host "  ⚠ Could not clean $path" -ForegroundColor Yellow
            }
        }
    }

    # Step 4: Clean Zookeeper data
    Write-Host "`nStep 4: Cleaning Zookeeper data..." -ForegroundColor Yellow

    $zookeeperDataPaths = @(
        "E:\Installation\Logs\kafkalogs\zookeeper"
    )

    foreach ($path in $zookeeperDataPaths) {
        if (Test-Path $path) {
            Write-Host "  Cleaning $path..." -ForegroundColor Gray
            try {
                Remove-Item -Path "$path\*" -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host "  ✓ Cleaned $path" -ForegroundColor Green
            } catch {
                Write-Host "  ⚠ Could not clean $path" -ForegroundColor Yellow
            }
        }
    }
} else {
    Write-Host "  Skipping data cleanup" -ForegroundColor Yellow
}

# Step 5: Verify ports are free
Write-Host "`nStep 5: Verifying ports are free..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

$port9092 = netstat -ano | Select-String ":9092"
$port2181 = netstat -ano | Select-String ":2181"

if ($port9092) {
    Write-Host "  ⚠ Port 9092 still in use" -ForegroundColor Yellow
} else {
    Write-Host "  ✓ Port 9092 is free" -ForegroundColor Green
}

if ($port2181) {
    Write-Host "  ⚠ Port 2181 still in use" -ForegroundColor Yellow
} else {
    Write-Host "  ✓ Port 2181 is free" -ForegroundColor Green
}

# Step 5.5: Ensure log directories exist
Write-Host "`nStep 5.5: Ensuring log directories exist..." -ForegroundColor Yellow

$logDirs = @(
    "E:\Installation\Logs\kafkalogs\kafka-logs",
    "E:\Installation\Logs\kafkalogs\kafka-streams-state\traffic-light-streams",
    "E:\Installation\Logs\kafkalogs\zookeeper",
    "E:\Installation\Logs\kafkalogs\kraft-broker-logs",
    "E:\Installation\Logs\kafkalogs\kraft-controller-logs",
    "E:\Installation\Logs\kafkalogs\kraft-combined-logs",
    "E:\Installation\Logs\kafkalogs\application-logs"
)

foreach ($dir in $logDirs) {
    if (-not (Test-Path $dir)) {
        Write-Host "  Creating $dir..." -ForegroundColor Gray
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "  ✓ Created $dir" -ForegroundColor Green
    } else {
        Write-Host "  ✓ $dir exists" -ForegroundColor Green
    }
}

# Step 6: Start Zookeeper
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Starting Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 6: Starting Zookeeper..." -ForegroundColor Yellow
Write-Host "  Opening new window for Zookeeper..." -ForegroundColor Gray
Write-Host "  Keep that window open!" -ForegroundColor Yellow
Write-Host ""

Start-Process cmd -ArgumentList "/k", "cd /d C:\kafka && .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties"

Write-Host "  ✓ Zookeeper window opened" -ForegroundColor Green
Write-Host "  Waiting for Zookeeper to start..." -ForegroundColor Gray

# Check Zookeeper with retries
$zkStarted = $false
for ($i = 1; $i -le 8; $i++) {
    Start-Sleep -Seconds 3
    $zkCheck = netstat -ano | Select-String ":2181" | Select-String "LISTENING"
    if ($zkCheck) {
        $zkStarted = $true
        Write-Host "  ✓ Zookeeper is running on port 2181 (after $($i*3) seconds)" -ForegroundColor Green
        break
    }
    Write-Host "  Checking... ($i/8)" -ForegroundColor Gray
}

if (-not $zkStarted) {
    Write-Host "  ✗ Zookeeper failed to start after 24 seconds" -ForegroundColor Red
    Write-Host "  Check the Zookeeper window for errors" -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "Continue to start Kafka anyway? (y/n)"
    if ($continue -ne 'y' -and $continue -ne 'Y') {
        exit 1
    }
} else {
    Write-Host "  Waiting additional 5 seconds for Zookeeper to stabilize..." -ForegroundColor Gray
    Start-Sleep -Seconds 5
}

# Step 7: Start Kafka
Write-Host "`nStep 7: Starting Kafka..." -ForegroundColor Yellow
Write-Host "  Opening new window for Kafka..." -ForegroundColor Gray
Write-Host "  Keep that window open!" -ForegroundColor Yellow
Write-Host ""

Start-Process cmd -ArgumentList "/k", "cd /d C:\kafka && .\bin\windows\kafka-server-start.bat .\config\server.properties"

Write-Host "  ✓ Kafka window opened" -ForegroundColor Green
Write-Host "  Waiting for Kafka to start..." -ForegroundColor Gray

# Check Kafka with retries
$kafkaStarted = $false
for ($i = 1; $i -le 10; $i++) {
    Start-Sleep -Seconds 3
    $kafkaCheck = netstat -ano | Select-String ":9092" | Select-String "LISTENING"
    if ($kafkaCheck) {
        $kafkaStarted = $true
        Write-Host "  ✓ Kafka is running on port 9092 (after $($i*3) seconds)" -ForegroundColor Green
        break
    }
    Write-Host "  Checking... ($i/10)" -ForegroundColor Gray
}

if (-not $kafkaStarted) {
    Write-Host "  ⚠ Kafka may not have started after 30 seconds" -ForegroundColor Yellow
    Write-Host "  Check the Kafka window for 'started' message or errors" -ForegroundColor Yellow
    Write-Host "  Common issues:" -ForegroundColor Yellow
    Write-Host "    - Zookeeper session timeout (check Zookeeper window)" -ForegroundColor Gray
    Write-Host "    - Port conflicts or firewall blocking" -ForegroundColor Gray
    Write-Host "    - Check logs: E:\Installation\Logs\kafkalogs\kafka-logs" -ForegroundColor Gray
}

# Final status
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Status" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$finalZk = netstat -ano | Select-String ":2181" | Select-String "LISTENING"
$finalKafka = netstat -ano | Select-String ":9092" | Select-String "LISTENING"

if ($finalZk) {
    Write-Host "✓ Zookeeper: Running on port 2181" -ForegroundColor Green
} else {
    Write-Host "✗ Zookeeper: Not Running" -ForegroundColor Red
}

if ($finalKafka) {
    Write-Host "✓ Kafka: Running on port 9092" -ForegroundColor Green
} else {
    Write-Host "✗ Kafka: Not Running" -ForegroundColor Red
}

Write-Host ""
Write-Host "Check the Kafka window for:" -ForegroundColor Cyan
Write-Host "  [KafkaServer id=0] started" -ForegroundColor White
Write-Host ""
Write-Host "Application logs location:" -ForegroundColor Cyan
Write-Host "  E:\Installation\Logs\kafkalogs\application-logs\" -ForegroundColor White
Write-Host "  Files: server.log, controller.log, state-change.log," -ForegroundColor Gray
Write-Host "         kafka-request.log, log-cleaner.log, kafka-authorizer.log" -ForegroundColor Gray
Write-Host ""
Write-Host "If you see connection errors, the issue may be:" -ForegroundColor Yellow
Write-Host "  1. Firewall blocking localhost" -ForegroundColor White
Write-Host "  2. Antivirus interfering" -ForegroundColor White
Write-Host "  3. Need to use Docker instead" -ForegroundColor White
Write-Host ""
Write-Host "To test Kafka:" -ForegroundColor Cyan
Write-Host "  cd C:\kafka" -ForegroundColor White
Write-Host "  .\bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092" -ForegroundColor White
Write-Host ""
Write-Host "To view logs:" -ForegroundColor Cyan
Write-Host "  Get-Content E:\Installation\Logs\kafkalogs\application-logs\server.log -Tail 20" -ForegroundColor White
Write-Host ""
Write-Host "Next steps for Traffic Light Controller:" -ForegroundColor Cyan
Write-Host "  1. Run: .\create-kafka-topics.ps1" -ForegroundColor White
Write-Host "  2. Start the Traffic Light Controller API" -ForegroundColor White
Write-Host "  3. Test the system endpoints" -ForegroundColor White
Write-Host ""
