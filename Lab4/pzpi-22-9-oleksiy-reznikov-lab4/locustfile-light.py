from locust import HttpUser, task, between
import random
import string

class LightElectricMonitorUser(HttpUser):
    wait_time = between(2, 5)  # Больше пауза между запросами
    
    def on_start(self):
        # Простая регистрация без сложной логики
        user_id = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))
        self.user_id = user_id
    
    @task(5)
    def get_health(self):
        """Проверка health endpoint"""
        self.client.get("/health")
    
    @task(3)
    def get_root(self):
        """Проверка корневого endpoint"""
        self.client.get("/")
    
    @task(1)
    def try_register(self):
        """Попытка регистрации"""
        register_data = {
            "username": f"testuser_{self.user_id}",
            "email": f"test_{self.user_id}@example.com",
            "password": "Test123456",
            "firstName": "Test",
            "lastName": "User",
            "phone": "+380501234567"
        }
        self.client.post("/api/auth/register", json=register_data, catch_response=True)
