param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("up", "down", "restart", "logs", "status", "build", "clean")]
    [string]$Action
)

$ErrorActionPreference = "Continue"

function Test-DockerRunning {
    try {
        docker info | Out-Null
        return $true
    } catch {
        Write-Host "❌ Docker не запущен! Запустите Docker Desktop."
        return $false
    }
}

function Start-LocalEnvironment {
    Write-Host "=== Запуск локальной среды разработки ==="
    
    if (-not (Test-DockerRunning)) { exit 1 }
    
    # Проверяем docker-compose.yml
    if (-not (Test-Path "docker-compose.yml")) {
        Write-Host "❌ docker-compose.yml не найден!"
        exit 1
    }
    
    Write-Host "Запуск контейнеров..."
    docker-compose up -d --build
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "✅ Среда запущена успешно!"
        Write-Host ""
        Write-Host "🌐 Доступные сервисы:"
        Write-Host "  - API Server: http://localhost:3000"
        Write-Host "  - Health Check: http://localhost:3000/health"
        Write-Host "  - MongoDB: localhost:27017"
        Write-Host "  - MQTT: localhost:1883"
        Write-Host "  - MQTT WebSocket: localhost:9001"
        Write-Host ""
        Write-Host "📋 Полезные команды:"
        Write-Host "  - Логи: .\scripts\local-dev.ps1 -Action logs"
        Write-Host "  - Статус: .\scripts\local-dev.ps1 -Action status"
        Write-Host "  - Остановка: .\scripts\local-dev.ps1 -Action down"
        
        # Ждем пока сервис запустится
        Write-Host ""
        Write-Host "⏳ Ожидание запуска сервиса..."
        for ($i = 1; $i -le 30; $i++) {
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:3000/health" -UseBasicParsing -TimeoutSec 2
                if ($response.StatusCode -eq 200) {
                    Write-Host "✅ API сервер отвечает!"
                    break
                }
            } catch {
                Write-Host "." -NoNewline
                Start-Sleep -Seconds 2
            }
        }
        Write-Host ""
    } else {
        Write-Host "❌ Не удалось запустить среду!"
        Write-Host "Проверьте логи: docker-compose logs"
        exit 1
    }
}

function Stop-LocalEnvironment {
    Write-Host "=== Остановка локальной среды ==="
    
    docker-compose down
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Среда остановлена!"
    } else {
        Write-Host "❌ Ошибка при остановке!"
        exit 1
    }
}

function Restart-LocalEnvironment {
    Write-Host "=== Перезапуск локальной среды ==="
    
    docker-compose restart
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Среда перезапущена!"
    } else {
        Write-Host "❌ Ошибка при перезапуске!"
        exit 1
    }
}

function Show-Logs {
    Write-Host "=== Логи (Ctrl+C для выхода) ==="
    docker-compose logs -f --tail=100
}

function Show-Status {
    Write-Host "=== Статус локальной среды ==="
    Write-Host ""
    
    Write-Host "📊 Контейнеры:"
    docker-compose ps
    
    Write-Host ""
    Write-Host "🔍 Детали контейнеров:"
    docker-compose top
    
    Write-Host ""
    Write-Host "💾 Использование ресурсов:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}"
}

function Build-LocalImage {
    Write-Host "=== Сборка локального образа ==="
    
    if (-not (Test-DockerRunning)) { exit 1 }
    
    docker-compose build --no-cache
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Сборка завершена!"
    } else {
        Write-Host "❌ Ошибка сборки!"
        exit 1
    }
}

function Clean-LocalEnvironment {
    Write-Host "=== Очистка локальной среды ==="
    
    Write-Host "Остановка контейнеров..."
    docker-compose down -v
    
    Write-Host "Удаление образов проекта..."
    docker images | Select-String "electric-monitor" | ForEach-Object {
        $imageName = ($_ -split '\s+')[0] + ":" + ($_ -split '\s+')[1]
        docker rmi $imageName -f
    }
    
    Write-Host "Очистка неиспользуемых ресурсов..."
    docker system prune -f
    
    Write-Host "✅ Очистка завершена!"
}

# Проверка docker-compose
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "❌ docker-compose не найден!"
    Write-Host "Установите Docker Desktop с https://www.docker.com/products/docker-desktop/"
    exit 1
}

switch ($Action) {
    "up" { Start-LocalEnvironment }
    "down" { Stop-LocalEnvironment }
    "restart" { Restart-LocalEnvironment }
    "logs" { Show-Logs }
    "status" { Show-Status }
    "build" { Build-LocalImage }
    "clean" { Clean-LocalEnvironment }
}
