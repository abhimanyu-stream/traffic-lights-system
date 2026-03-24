# Create Kafka Topics for Traffic Light Controller System
# This script creates all required Kafka topics with appropriate configurations

Write-Host "Creating Kafka topics for Traffic Light Controller..." -ForegroundColor Green

# Kafka broker address
$KAFKA_BROKER = "localhost:9092"

# Topic configurations
$topics = @(
    @{
        Name = "traffic-light.state-changed"
        Partitions = 3
        ReplicationFactor = 1
        Config = "cleanup.policy=delete,retention.ms=604800000"
        Description = "Traffic light state change events"
    },
    @{
        Name = "intersection.updated"
        Partitions = 3
        ReplicationFactor = 1
        Config = "cleanup.policy=delete,retention.ms=604800000"
        Description = "Intersection update events"
    },
    @{
        Name = "system.health"
        Partitions = 1
        ReplicationFactor = 1
        Config = "cleanup.policy=delete,retention.ms=259200000"
        Description = "System health events"
    },
    @{
        Name = "traffic-light.state-changed.DLT"
        Partitions = 1
        ReplicationFactor = 1
        Config = "cleanup.policy=delete,retention.ms=2592000000"
        Description = "Dead Letter Topic for failed state change events"
    },
    @{
        Name = "intersection.updated.DLT"
        Partitions = 1
        ReplicationFactor = 1
        Config = "cleanup.policy=delete,retention.ms=2592000000"
        Description = "Dead Letter Topic for failed intersection update events"
    },
    @{
        Name = "system.events"
        Partitions = 2
        ReplicationFactor = 1
        Config = "cleanup.policy=delete,retention.ms=604800000"
        Description = "General system events"
    }
)

# Function to create a topic
function Create-KafkaTopic {
    param (
        [string]$TopicName,
        [int]$Partitions,
        [int]$ReplicationFactor,
        [string]$Config,
        [string]$Description
    )
    
    Write-Host "`nCreating topic: $TopicName" -ForegroundColor Cyan
    Write-Host "  Description: $Description"
    Write-Host "  Partitions: $Partitions"
    Write-Host "  Replication Factor: $ReplicationFactor"
    Write-Host "  Config: $Config"
    
    $command = "kafka-topics.bat --create --bootstrap-server $KAFKA_BROKER --topic $TopicName --partitions $Partitions --replication-factor $ReplicationFactor --config $Config"
    
    try {
        Invoke-Expression $command 2>&1 | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ Topic created successfully" -ForegroundColor Green
        } else {
            Write-Host "  ⚠ Topic may already exist or creation failed" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  ✗ Error creating topic: $_" -ForegroundColor Red
    }
}

# Create all topics
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "Creating Kafka Topics" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

foreach ($topic in $topics) {
    Create-KafkaTopic -TopicName $topic.Name `
                      -Partitions $topic.Partitions `
                      -ReplicationFactor $topic.ReplicationFactor `
                      -Config $topic.Config `
                      -Description $topic.Description
}

# List all topics to verify
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "Verifying Created Topics" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

try {
    $listCommand = "kafka-topics.bat --list --bootstrap-server $KAFKA_BROKER"
    $topicList = Invoke-Expression $listCommand 2>&1
    
    Write-Host "Available topics:" -ForegroundColor Cyan
    $topicList | Where-Object { $_ -match "traffic-light|intersection|system" } | ForEach-Object {
        Write-Host "  • $_" -ForegroundColor White
    }
} catch {
    Write-Host "Error listing topics: $_" -ForegroundColor Red
}

# Describe topics for detailed information
Write-Host "`n========================================" -ForegroundColor Yellow
Write-Host "Topic Details" -ForegroundColor Yellow
Write-Host "========================================`n" -ForegroundColor Yellow

foreach ($topic in $topics) {
    Write-Host "`nTopic: $($topic.Name)" -ForegroundColor Cyan
    try {
        $describeCommand = "kafka-topics.bat --describe --bootstrap-server $KAFKA_BROKER --topic $($topic.Name)"
        Invoke-Expression $describeCommand 2>&1
    } catch {
        Write-Host "  Error describing topic: $_" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Kafka Topics Creation Complete!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "  • Total topics created: $($topics.Count)" -ForegroundColor White
Write-Host "  • Main event topics: 3" -ForegroundColor White
Write-Host "  • Dead Letter Topics (DLT): 2" -ForegroundColor White
Write-Host "  • System event topics: 1" -ForegroundColor White
Write-Host "`nNote: Topics are configured with 7-day retention (except DLT with 30 days)" -ForegroundColor Gray
