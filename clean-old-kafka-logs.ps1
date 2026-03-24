# Clean Old Kafka Logs for Traffic Light Controller

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Clean Old Kafka Logs - Traffic Light Controller" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$oldLogsPath = "C:\kafka\logs"

# Check if directory exists
if (-not (Test-Path $oldLogsPath)) {
    Write-Host "✓ No old logs directory found at $oldLogsPath" -ForegroundColor Green
    Write-Host "Nothing to clean!" -ForegroundColor Gray
    exit 0
}

# List files in old logs directory
Write-Host "Files in old logs directory:" -ForegroundColor Yellow
$files = Get-ChildItem $oldLogsPath -ErrorAction SilentlyContinue
if ($files) {
    $files | Select-Object Name, Length, LastWriteTime | Format-Table -AutoSize
    Write-Host ""
    Write-Host "Total files: $($files.Count)" -ForegroundColor Gray
    $totalSize = ($files | Measure-Object -Property Length -Sum).Sum
    Write-Host "Total size: $([math]::Round($totalSize / 1MB, 2)) MB" -ForegroundColor Gray
} else {
    Write-Host "  Directory is empty" -ForegroundColor Gray
    exit 0
}

Write-Host ""
Write-Host "⚠ WARNING: This will delete all files in $oldLogsPath" -ForegroundColor Yellow
Write-Host "This includes traffic light system logs and Kafka operational data" -ForegroundColor Yellow
Write-Host ""
$confirm = Read-Host "Do you want to delete these old log files? (y/n)"

if ($confirm -eq 'y' -or $confirm -eq 'Y') {
    Write-Host ""
    Write-Host "Cleaning old logs..." -ForegroundColor Yellow
    
    try {
        Remove-Item -Path "$oldLogsPath\*" -Recurse -Force -ErrorAction Stop
        Write-Host "✓ Successfully cleaned $oldLogsPath" -ForegroundColor Green
        
        # Verify cleanup
        $remainingFiles = Get-ChildItem $oldLogsPath -ErrorAction SilentlyContinue
        if ($remainingFiles) {
            Write-Host "⚠ Warning: Some files could not be deleted" -ForegroundColor Yellow
            $remainingFiles | Select-Object Name | Format-Table -AutoSize
        } else {
            Write-Host "✓ All files removed successfully" -ForegroundColor Green
            Write-Host "✓ Ready for fresh Traffic Light Controller logs" -ForegroundColor Green
        }
    } catch {
        Write-Host "✗ Error cleaning logs: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "Cleanup cancelled" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan
