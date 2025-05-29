# Быстрая подготовка Electric Monitor для лабораторной работы
Write-Host "=== Быстрая подготовка Electric Monitor для масштабирования ==="

# 1. Проверка Docker образа
Write-Host "1. Проверка Docker образа..."
$imageExists = docker images electric-monitor --format "table {{.Repository}}" | Select-String "electric-monitor"

if (-not $imageExists) {
    Write-Host "Docker образ не найден. Сборка..."
    .\scripts\build.ps1
} else {
    Write-Host "✅ Docker образ electric-monitor найден"
}

# 2. Проверка .env файла
Write-Host "2. Проверка конфигурации..."
if (Test-Path ".env") {
    $envContent = Get-Content ".env" -Raw
    if ($envContent -match "MONGODB_URI=mongodb\+srv://") {
        Write-Host "✅ Найден внешний MongoDB Atlas в .env"
    } else {
        Write-Host "⚠ Проверьте MongoDB URI в .env файле"
    }
} else {
    Write-Host "❌ .env файл не найден!"
    exit 1
}

# 3. Проверка Kubernetes
Write-Host "3. Проверка Kubernetes..."
try {
    kubectl cluster-info | Out-Null
    Write-Host "✅ Kubernetes кластер доступен"
} catch {
    Write-Host "❌ Kubernetes кластер недоступен!"
    Write-Host "Запустите Docker Desktop и включите Kubernetes"
    exit 1
}

# 4. Проверка Locust
Write-Host "4. Проверка Locust..."
if (Get-Command locust -ErrorAction SilentlyContinue) {
    Write-Host "✅ Locust установлен"
} else {
    Write-Host "Установка Locust..."
    pip install locust
}

Write-Host ""
Write-Host "🚀 Система готова! Можете запускать:"
Write-Host ".\scripts\load-testing.ps1 -Action setup"
Write-Host ".\scripts\load-testing.ps1 -Action test"
