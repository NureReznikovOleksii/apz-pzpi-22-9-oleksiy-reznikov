# Помощник по установке необходимых компонентов на Windows
Write-Host "=== Установка компонентов для Electric Monitor на Windows ==="

function Install-Chocolatey {
    if (Get-Command choco -ErrorAction SilentlyContinue) {
        Write-Host "✓ Chocolatey уже установлен"
        return
    }
    
    Write-Host "Установка Chocolatey..."
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
}

function Install-DockerDesktop {
    if (Get-Command docker -ErrorAction SilentlyContinue) {
        Write-Host "✓ Docker Desktop уже установлен"
        return
    }
    
    Write-Host "Установка Docker Desktop через Chocolatey..."
    choco install docker-desktop -y
    
    Write-Host "⚠ После установки Docker Desktop:"
    Write-Host "1. Перезагрузите компьютер"
    Write-Host "2. Запустите Docker Desktop"
    Write-Host "3. Дождитесь завершения инициализации"
}

function Install-Kubectl {
    if (Get-Command kubectl -ErrorAction SilentlyContinue) {
        Write-Host "✓ kubectl уже установлен"
        return
    }
    
    Write-Host "Установка kubectl через Chocolatey..."
    choco install kubernetes-cli -y
}

function Install-NodeJS {
    if (Get-Command node -ErrorAction SilentlyContinue) {
        Write-Host "✓ Node.js уже установлен"
        return
    }
    
    Write-Host "Установка Node.js через Chocolatey..."
    choco install nodejs -y
}

# Главное меню
Write-Host ""
Write-Host "Выберите компоненты для установки:"
Write-Host "1. Chocolatey (пакетный менеджер)"
Write-Host "2. Docker Desktop (обязательно)"
Write-Host "3. kubectl (для Kubernetes)"
Write-Host "4. Node.js (опционально)"
Write-Host "5. Установить все"
Write-Host "0. Выход"

$choice = Read-Host "Введите номер"

switch ($choice) {
    "1" { Install-Chocolatey }
    "2" { Install-Chocolatey; Install-DockerDesktop }
    "3" { Install-Chocolatey; Install-Kubectl }
    "4" { Install-Chocolatey; Install-NodeJS }
    "5" { 
        Install-Chocolatey
        Install-DockerDesktop
        Install-Kubectl
        Install-NodeJS
    }
    "0" { exit }
    default { Write-Host "Неверный выбор" }
}

Write-Host ""
Write-Host "✅ Установка завершена!"
Write-Host "Проверьте систему: .\scripts\check-system.ps1"
