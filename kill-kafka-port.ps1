# Kill Process Using Port 9092 (Kafka) - Traffic Light Controller

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Kill Kafka Port (9092) - Traffic Light Controller" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check port 9092
Write-Host "Checking port 9092 for Traffic Light Controller..." -ForegroundColor Yellow
$port9092 = netstat -ano | Select-String ":9092"

if ($port9092) {
    Write-Host "✓ Port 9092 is in use" -ForegroundColor Green
    Write-Host ""
    Write-Host "Connections on port 9092:" -ForegroundColor Cyan
    netstat -ano | Select-String ":9092"
    Write-Host ""
    
    # Extract process IDs
    $processIds = $port9092 | ForEach-Object {
        $line = $_.ToString().Trim()
        if ($line -match '\s+(\d+)\s*$') {
            $matches[1]
        }
    } | Select-Object -Unique
    
    Write-Host "Process IDs using port 9092 (Traffic Light Controller Kafka):" -ForegroundColor Yellow
    foreach ($processId in $processIds) {
        try {
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "  PID: $processId - $($process.ProcessName)" -ForegroundColor White
            } else {
                Write-Host "  PID: $processId - (process not found)" -ForegroundColor Gray
            }
        } catch {
            Write-Host "  PID: $processId - (unable to get details)" -ForegroundColor Gray
        }
    }
    
    Write-Host ""
    Write-Host "⚠ This will stop Kafka for Traffic Light Controller system" -ForegroundColor Yellow
    Write-Host "  This will interrupt traffic light event processing!" -ForegroundColor Yellow
    $confirm = Read-Host "Do you want to kill these processes? (y/n)"
    
    if ($confirm -eq 'y' -or $confirm -eq 'Y') {
        Write-Host ""
        Write-Host "Killing Kafka processes..." -ForegroundColor Yellow
        
        foreach ($processId in $processIds) {
            try {
                Stop-Process -Id $processId -Force -ErrorAction Stop
                Write-Host "  ✓ Killed process $processId" -ForegroundColor Green
            } catch {
                Write-Host "  ✗ Failed to kill process $processId : $_" -ForegroundColor Red
            }
        }
        
        # Wait and verify
        Start-Sleep -Seconds 2
        Write-Host ""
        Write-Host "Verifying port 9092..." -ForegroundColor Yellow
        $checkAgain = netstat -ano | Select-String ":9092"
        
        if ($checkAgain) {
            Write-Host "  ⚠ Port 9092 is still in use" -ForegroundColor Yellow
            netstat -ano | Select-String ":9092"
        } else {
            Write-Host "  ✓ Port 9092 is now free" -ForegroundColor Green
            Write-Host "  ✓ Traffic Light Controller Kafka stopped" -ForegroundColor Green
        }
        
        Write-Host ""
        Write-Host "To restart Traffic Light Controller Kafka:" -ForegroundColor Cyan
        Write-Host "  .\clean-start-kafka.ps1" -ForegroundColor White
    } else {
        Write-Host "Cancelled. No processes killed." -ForegroundColor Gray
    }
    
} else {
    Write-Host "✓ Port 9092 is free (not in use)" -ForegroundColor Green
    Write-Host "Traffic Light Controller Kafka is not running" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
Write-Host ""
