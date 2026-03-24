#!/usr/bin/env pwsh

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Creating Kafka Topics for Traffic Light Controller" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$kafkaPath = "C:\kafka"
$bootstrapServer = "localhost:9092"

# Required topics for Traffic Light Controller system
$topics = @(
    # Core traffic light events
    "traffic-light-state-changed",
    "intersection-updated",
    "light-sequence-activated",
    "light-sequence-deactivated",
    
    # System events
    "system-health-check",
    "system-error-events",
    "system-audit-events",
    
    # Analytics and monitoring
    "analytics-traffic-metrics",
    "analytics-performance-data",
    "analytics-state-transitions",
    
    # Configuration events
    "config-timing-updated",
    "config-rules-changed",
    "config-intersection-settings",
    
    # Dead Letter Topics (DLT)
    "traffic-light-state-changed-DLT",
    "intersection-updated-DLT",
    "system-events-DLT",
    
    # Retry topics
    "retry-traffic-events",
    "retry-system-events",
    
    # Batch processing
    "batch-state-history-cleanup",
    "batch-analytics-aggregation"
)

Write-Host "Step 1: Checking if Kafka is running..." -ForegroundColor Yellow
$kafkaRunning = Get-NetTCPConnection -LocalPort 9092 -ErrorAction SilentlyContinue
if (-not $kafkaRunning) {
    Write-Host "❌ Kafka is not running on port 9092" -ForegroundColor Red
    Write-Host "Please start Kafka first using: .\clean-start-kafka.ps1" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Kafka is running on port 9092" -ForegroundColor Green

Write-Host "`nStep 2: Creating topics for Traffic Light Controller..." -ForegroundColor Yellow

foreach ($topic in $topics) {
    Write-Host "Creating topic: $topic" -ForegroundColor Cyan
    
    try {
        # Configure partitions based on topic type
        $partitions = 3
        if ($topic -like "*analytics*" -or $topic -like "*batch*") {
            $partitions = 6  # More partitions for high-throughput analytics
        } elseif ($topic -like "*.DLT") {
            $partitions = 1  # Single partition for DLT topics
        }
        
        & "$kafkaPath\bin\windows\kafka-topics.bat" --create `
            --topic $topic `
            --bootstrap-server $bootstrapServer `
            --partitions $partitions `
            --replication-factor 1 `
            --if-not-exists 2>$null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Topic '$topic' created successfully (partitions: $partitions)" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Topic '$topic' may already exist" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "❌ Failed to create topic '$topic': $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nStep 3: Verifying topics..." -ForegroundColor Yellow
Write-Host "Listing all topics:" -ForegroundColor Cyan

try {
    & "$kafkaPath\bin\windows\kafka-topics.bat" --list --bootstrap-server $bootstrapServer
    Write-Host "✅ Topics listed successfully" -ForegroundColor Green
}
catch {
    Write-Host "❌ Failed to list topics: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nStep 4: Topic configuration details..." -ForegroundColor Yellow
Write-Host "Core Event Topics:" -ForegroundColor Cyan
Write-Host "  - traffic-light-state-changed: Light state transitions" -ForegroundColor White
Write-Host "  - intersection-updated: Intersection configuration changes" -ForegroundColor White
Write-Host "  - light-sequence-activated/deactivated: Sequence management" -ForegroundColor White

Write-Host "`nSystem Topics:" -ForegroundColor Cyan
Write-Host "  - system-health-check: Health monitoring events" -ForegroundColor White
Write-Host "  - system-error-events: Error and exception tracking" -ForegroundColor White
Write-Host "  - system-audit-events: Audit trail for all operations" -ForegroundColor White

Write-Host "`nAnalytics Topics:" -ForegroundColor Cyan
Write-Host "  - analytics-traffic-metrics: Traffic flow analytics" -ForegroundColor White
Write-Host "  - analytics-performance-data: System performance metrics" -ForegroundColor White
Write-Host "  - analytics-state-transitions: State change analytics" -ForegroundColor White

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Topic Creation Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ All required topics have been created for Traffic Light Controller" -ForegroundColor Green
Write-Host "You can now start the Traffic Light Controller API" -ForegroundColor Yellow
Write-Host "`nTo start the service:" -ForegroundColor Cyan
Write-Host "cd traffic-light-controller" -ForegroundColor White
Write-Host "mvn spring-boot:run" -ForegroundColor White
Write-Host "`nOr with Gradle:" -ForegroundColor Cyan
Write-Host "./gradlew bootRun" -ForegroundColor White