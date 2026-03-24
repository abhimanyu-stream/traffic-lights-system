# Kill All Zookeeper Ports - Traffic Light Controller

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Kill All Zookeeper Ports - Traffic Light Controller" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Zookeeper ports:
# 2181 - Client port
# 2888 - Follower port (peer communication)
# 3888 - Election port (leader election)
$ports = @(2181, 2888, 3888)
$allProcessIds = @()

foreach ($port in $ports) {
    Write-Host "Checking port $port..." -ForegroundColor Yellow
    $connections = netstat -ano | Select-String ":$port"
    
    if ($connections) {
        Write-Host "  ✓ Port $port is in use" -ForegroundColor Green
        
        # Extract process IDs
        $processIds = $connections | ForEach-Object {
            $line = $_.ToString().Trim()
            if ($line -match '\s+(\d+)\s*$') {
                $matches[1]
            }
        } | Select-Object -Unique
        
        foreach ($processId in $processIds) {
            if ($processId -notin $allProcessIds) {
                $allProcessIds += $processId
            }
        }
    } else {
        Write-Host "  Port $port is free" -ForegroundColor Gray
    }
}

if ($allProcessIds.Count -gt 0) {
    Write-Host ""
    Write-Host "Zookeeper processes to kill (Traffic Light Controller):" -ForegroundColor Yellow
    foreach ($processId in $allProcessIds) {
        try {
            $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
            if ($process) {
                Write-Host "  PID: $processId - $($process.ProcessName)" -ForegroundColor White
            }
        } catch {
            Write-Host "  PID: $processId" -ForegroundColor White
        }
    }
    
    Write-Host ""
    Write-Host "⚠ This will stop Zookeeper services for Traffic Light Controller" -ForegroundColor Yellow
    Write-Host "  This may affect traffic light state coordination!" -ForegroundColor Yellow
    $confirm = Read-Host "Kill all these processes? (y/n)"
    
    if ($confirm -eq 'y' -or $confirm -eq 'Y') {
        Write-Host ""
        Write-Host "Killing Zookeeper processes..." -ForegroundColor Yellow
        
        foreach ($processId in $allProcessIds) {
            try {
                Stop-Process -Id $processId -Force -ErrorAction Stop
                Write-Host "  ✓ Killed PID $processId" -ForegroundColor Green
            } catch {
                Write-Host "  ✗ Failed to kill PID $processId" -ForegroundColor Red
            }
        }
        
        # Verify
        Start-Sleep -Seconds 2
        Write-Host ""
        Write-Host "Verification:" -ForegroundColor Yellow
        
        foreach ($port in $ports) {
            $check = netstat -ano | Select-String ":$port"
            if ($check) {
                Write-Host "  ⚠ Port $port still in use" -ForegroundColor Yellow
            } else {
                Write-Host "  ✓ Port $port is free" -ForegroundColor Green
            }
        }
        
        Write-Host ""
        Write-Host "✓ Traffic Light Controller Zookeeper services stopped" -ForegroundColor Green
        Write-Host "You can restart with: .\clean-start-kafka.ps1" -ForegroundColor Cyan
    } else {
        Write-Host "Cancelled." -ForegroundColor Gray
    }
} else {
    Write-Host ""
    Write-Host "✓ All Zookeeper ports are free (2181, 2888, 3888)" -ForegroundColor Green
    Write-Host "Traffic Light Controller Zookeeper services are not running" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
Write-Host ""
