# Скрипт для проведення навантажувальних тестів з різною кількістю подів
param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("1", "2", "3", "setup", "test", "cleanup")]
    [string]$Action
)

$SERVICE_URL = "http://localhost:30080"
$NAMESPACE = "electric-monitor"

function Setup-Environment {
    Write-Host "=== Налаштування середовища для тестування ==="
    
    # Перевіряємо Kubernetes
    if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
        Write-Host "❌ kubectl не знайдено!"
        exit 1
    }
    
    # Розгортаємо застосунок
    Write-Host "Розгортання застосунку..."
    kubectl apply -f k8s/namespace.yaml
    kubectl apply -f k8s/configmap.yaml
    kubectl apply -f k8s/secrets.yaml
    kubectl apply -f k8s/mongodb/
    kubectl apply -f k8s/mqtt/
    kubectl apply -f k8s/api/deployment-scaling.yaml
    kubectl apply -f k8s/api/service-loadbalancer.yaml
    
    Write-Host "Очікування готовності подів..."
    kubectl wait --for=condition=ready pod -l app=electric-monitor-api -n $NAMESPACE --timeout=300s
    
    Write-Host "✅ Середовище готове для тестування!"
    Write-Host "Сервіс доступний на: $SERVICE_URL"
}

function Scale-Application {
    param([int]$Replicas)
    
    Write-Host "=== Масштабування до $Replicas реплік ==="
    
    kubectl scale deployment electric-monitor-api --replicas=$Replicas -n $NAMESPACE
    
    Write-Host "Очікування готовності подів..."
    kubectl wait --for=condition=ready pod -l app=electric-monitor-api -n $NAMESPACE --timeout=300s
    
    Write-Host "Поточні поди:"
    kubectl get pods -n $NAMESPACE -l app=electric-monitor-api
    
    Write-Host "✅ Масштабування до $Replicas подів завершено!"
}

function Run-LoadTest {
    param([int]$Pods)
    
    Write-Host "=== Навантажувальне тестування з $Pods подами ==="
    
    # Перевіряємо встановлення Locust
    if (-not (Get-Command locust -ErrorAction SilentlyContinue)) {
        Write-Host "Встановлення Locust..."
        pip install locust
    }
    
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $reportDir = "test-results"
    
    if (-not (Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir
    }
    
    Write-Host "Запуск тесту з параметрами:"
    Write-Host "- Користувачі: 200"
    Write-Host "- Швидкість появи: 25 користувачів/сек"
    Write-Host "- Час тесту: 3 хвилини"
    Write-Host "- URL: $SERVICE_URL"
    
    # Запуск Locust в режимі командного рядка
    locust -f locustfile.py --host=$SERVICE_URL --users=200 --spawn-rate=25 --run-time=3m --html="$reportDir/report_${Pods}pods_${timestamp}.html" --csv="$reportDir/results_${Pods}pods_${timestamp}"
    
    Write-Host "✅ Тест завершено! Результати збережено в $reportDir/"
}

function Cleanup-Environment {
    Write-Host "=== Очищення середовища ==="
    
    kubectl delete namespace $NAMESPACE --ignore-not-found=true
    
    Write-Host "✅ Середовище очищено!"
}

# Основна логіка
switch ($Action) {
    "setup" {
        Setup-Environment
    }
    "1" {
        Scale-Application -Replicas 1
        Start-Sleep -Seconds 30
        Run-LoadTest -Pods 1
    }
    "2" {
        Scale-Application -Replicas 2
        Start-Sleep -Seconds 30
        Run-LoadTest -Pods 2
    }
    "3" {
        Scale-Application -Replicas 3
        Start-Sleep -Seconds 30
        Run-LoadTest -Pods 3
    }
    "test" {
        Write-Host "=== Автоматичне тестування всіх конфігурацій ==="
        
        # Тест з 1 подом
        Scale-Application -Replicas 1
        Start-Sleep -Seconds 30
        Run-LoadTest -Pods 1
        
        # Тест з 2 подами
        Scale-Application -Replicas 2
        Start-Sleep -Seconds 30
        Run-LoadTest -Pods 2
        
        # Тест з 3 подами
        Scale-Application -Replicas 3
        Start-Sleep -Seconds 30
        Run-LoadTest -Pods 3
        
        Write-Host "✅ Усі тести завершено! Перевірте папку test-results/"
    }
    "cleanup" {
        Cleanup-Environment
    }
}