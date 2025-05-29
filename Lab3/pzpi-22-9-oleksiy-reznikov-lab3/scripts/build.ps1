param(
    [string]$Tag = "latest",
    [switch]$NoBuildKit
)

$ImageName = "electric-monitor"
$FullImageName = "${ImageName}:${Tag}"

Write-Host "=== Сборка Docker образа для Windows ==="
Write-Host "Образ: $FullImageName"
Write-Host ""

# Проверка Docker
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker не найден! Установите Docker Desktop."
    exit 1
}

# Проверка статуса Docker
try {
    docker info | Out-Null
    Write-Host "✓ Docker Engine запущен"
} catch {
    Write-Host "❌ Docker Engine не запущен! Запустите Docker Desktop."
    exit 1
}

# Настройка BuildKit для Windows
if (-not $NoBuildKit) {
    $env:DOCKER_BUILDKIT = "1"
    Write-Host "✓ Используется Docker BuildKit"
}

# Показываем содержимое .dockerignore для информации
if (Test-Path ".dockerignore") {
    Write-Host "✓ .dockerignore найден"
} else {
    Write-Host "⚠ .dockerignore не найден - рекомендуется создать"
}

Write-Host ""
Write-Host "Начинаем сборку..."

# Сборка образа с подробным выводом
docker build -t $FullImageName . --progress=plain

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Сборка завершена успешно!"
    Write-Host "📦 Образ: $FullImageName"
    
    # Показать размер образа
    Write-Host ""
    Write-Host "📊 Информация об образе:"
    docker images $ImageName --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
    
    Write-Host ""
    Write-Host "🚀 Следующие шаги:"
    Write-Host "  - Тест локально: docker run -p 3000:3000 $FullImageName"
    Write-Host "  - Локальная среда: .\scripts\local-dev.ps1 -Action up"
    Write-Host "  - Развертывание K8s: .\scripts\deploy.ps1"
} else {
    Write-Host ""
    Write-Host "❌ Сборка провалилась!"
    Write-Host "💡 Советы по устранению проблем:"
    Write-Host "  - Проверьте Dockerfile на ошибки"
    Write-Host "  - Убедитесь, что все файлы доступны"
    Write-Host "  - Попробуйте очистить Docker: docker system prune"
    exit 1
}
