# Проверка системных требований для Windows
Write-Host "=== Проверка системы Windows ==="

# Проверка Windows версии
$osVersion = [System.Environment]::OSVersion.Version
Write-Host "Windows версия: $($osVersion.Major).$($osVersion.Minor)"

# Проверка архитектуры
$arch = $env:PROCESSOR_ARCHITECTURE
Write-Host "Архитектура: $arch"

# Проверка памяти
$memory = Get-CimInstance Win32_PhysicalMemory | Measure-Object -Property capacity -Sum
$memoryGB = [math]::round($memory.sum / 1GB, 2)
Write-Host "Оперативная память: $memoryGB GB"

if ($memoryGB -lt 4) {
    Write-Host "⚠ Рекомендуется минимум 4GB RAM для Docker"
}

# Проверка свободного места
$disk = Get-CimInstance -ClassName Win32_LogicalDisk -Filter "DriveType=3" | Where-Object {$_.DeviceID -eq "C:"}
$freeSpaceGB = [math]::round($disk.FreeSpace / 1GB, 2)
Write-Host "Свободное место на диске C: $freeSpaceGB GB"

if ($freeSpaceGB -lt 10) {
    Write-Host "⚠ Рекомендуется минимум 10GB свободного места"
}

# Проверка Hyper-V (для Docker Desktop)
$hyperv = Get-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V-All
if ($hyperv.State -eq "Enabled") {
    Write-Host "✓ Hyper-V включен"
} else {
    Write-Host "⚠ Hyper-V отключен (может потребоваться для Docker Desktop)"
}

Write-Host ""
Write-Host "=== Проверка Docker ==="

if (Get-Command docker -ErrorAction SilentlyContinue) {
    try {
        $dockerVersion = docker --version
        Write-Host "✓ Docker: $dockerVersion"
        
        $dockerStatus = docker info 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Docker Engine запущен"
        } else {
            Write-Host "❌ Docker Engine не запущен - запустите Docker Desktop"
        }
    } catch {
        Write-Host "❌ Проблема с Docker"
    }
} else {
    Write-Host "❌ Docker не установлен"
    Write-Host "Скачайте с: https://www.docker.com/products/docker-desktop/"
}

if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
    $composeVersion = docker-compose --version
    Write-Host "✓ Docker Compose: $composeVersion"
} else {
    Write-Host "❌ Docker Compose не найден"
}
