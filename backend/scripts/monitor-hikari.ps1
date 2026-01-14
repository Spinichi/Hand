# HikariCP 커넥션 풀 실시간 모니터링 스크립트
# 사용법: .\monitor-hikari.ps1

param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [int]$IntervalSeconds = 5
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "HikariCP Connection Pool Monitor" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl"
Write-Host "Interval: $IntervalSeconds seconds"
Write-Host "Press Ctrl+C to stop"
Write-Host "========================================`n" -ForegroundColor Cyan

# 헤더 출력
Write-Host ("{0,-12} {1,8} {2,8} {3,8} {4,8} {5,8}" -f "Time", "Active", "Idle", "Total", "Max", "Pending") -ForegroundColor Yellow

while ($true) {
    try {
        $time = Get-Date -Format "HH:mm:ss"

        # Active Connections
        $activeResponse = Invoke-RestMethod -Uri "$BaseUrl/actuator/metrics/hikaricp.connections.active" -ErrorAction SilentlyContinue
        $active = if ($activeResponse) { $activeResponse.measurements[0].value } else { "N/A" }

        # Idle Connections
        $idleResponse = Invoke-RestMethod -Uri "$BaseUrl/actuator/metrics/hikaricp.connections.idle" -ErrorAction SilentlyContinue
        $idle = if ($idleResponse) { $idleResponse.measurements[0].value } else { "N/A" }

        # Total Connections
        $totalResponse = Invoke-RestMethod -Uri "$BaseUrl/actuator/metrics/hikaricp.connections" -ErrorAction SilentlyContinue
        $total = if ($totalResponse) { $totalResponse.measurements[0].value } else { "N/A" }

        # Max Connections
        $maxResponse = Invoke-RestMethod -Uri "$BaseUrl/actuator/metrics/hikaricp.connections.max" -ErrorAction SilentlyContinue
        $max = if ($maxResponse) { $maxResponse.measurements[0].value } else { "N/A" }

        # Pending Threads (connection timeout 대기 중인 스레드)
        $pendingResponse = Invoke-RestMethod -Uri "$BaseUrl/actuator/metrics/hikaricp.connections.pending" -ErrorAction SilentlyContinue
        $pending = if ($pendingResponse) { $pendingResponse.measurements[0].value } else { "0" }

        # 출력 (색상 구분)
        $color = "White"
        if ($active -ne "N/A" -and $max -ne "N/A" -and $active -ge $max * 0.8) {
            $color = "Red"  # 80% 이상 사용 시 빨간색
        } elseif ($active -ne "N/A" -and $max -ne "N/A" -and $active -ge $max * 0.5) {
            $color = "Yellow"  # 50% 이상 사용 시 노란색
        }

        Write-Host ("{0,-12} {1,8} {2,8} {3,8} {4,8} {5,8}" -f $time, $active, $idle, $total, $max, $pending) -ForegroundColor $color

    } catch {
        Write-Host "[$time] Error: $_" -ForegroundColor Red
    }

    Start-Sleep -Seconds $IntervalSeconds
}
