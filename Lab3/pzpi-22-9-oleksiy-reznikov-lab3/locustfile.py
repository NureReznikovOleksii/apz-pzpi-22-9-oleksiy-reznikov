from locust import HttpUser, task, between
import json
import random
import string

class ElectricMonitorUser(HttpUser):
    wait_time = between(1, 3)
    
    def on_start(self):
        """Реєстрація та авторизація користувача для Electric Monitor API"""
        # Генеруємо унікальні дані для кожного користувача
        user_id = ''.join(random.choices(string.ascii_lowercase + string.digits, k=8))
        
        # Реєстрація користувача
        register_data = {
            "username": f"testuser_{user_id}",
            "email": f"test_{user_id}@example.com",
            "password": "Test123456",
            "firstName": "Навантажувальний",
            "lastName": "Тест",
            "phone": "+380501234567"
        }
        
        response = self.client.post("/api/auth/register", json=register_data)
        
        if response.status_code == 201:
            # Авторизація
            login_data = {
                "identifier": f"test_{user_id}@example.com",
                "password": "Test123456"
            }
            
            auth_response = self.client.post("/api/auth/login", json=login_data)
            
            if auth_response.status_code == 200:
                self.token = auth_response.json().get('data', {}).get('token')
                self.headers = {"Authorization": f"Bearer {self.token}"}
                self.user_data = register_data
            else:
                self.headers = {}
        else:
            # Якщо користувач вже існує, пробуємо увійти
            login_data = {
                "identifier": f"test_{user_id}@example.com", 
                "password": "Test123456"
            }
            auth_response = self.client.post("/api/auth/login", json=login_data)
            if auth_response.status_code == 200:
                self.token = auth_response.json().get('data', {}).get('token')
                self.headers = {"Authorization": f"Bearer {self.token}"}
            else:
                self.headers = {}
    
    @task(5)
    def get_health(self):
        """Перевірка health endpoint - основний тест продуктивності"""
        self.client.get("/health")
    
    @task(3)
    def get_root(self):
        """Перевірка кореневого endpoint"""
        self.client.get("/")
    
    @task(2)
    def get_profile(self):
        """Отримання профілю користувача"""
        if hasattr(self, 'headers') and self.headers:
            self.client.get("/api/auth/profile", headers=self.headers)
    
    @task(2) 
    def verify_token(self):
        """Перевірка токена"""
        if hasattr(self, 'headers') and self.headers:
            self.client.get("/api/auth/verify-token", headers=self.headers)
    
    @task(1)
    def get_devices(self):
        """Отримання списку пристроїв"""
        if hasattr(self, 'headers') and self.headers:
            self.client.get("/api/devices", headers=self.headers)
    
    @task(1)
    def create_device(self):
        """Створення нового пристрою Electric Monitor"""
        if hasattr(self, 'headers') and self.headers:
            device_data = {
                "name": f"Тестовий Пристрій {random.randint(1000, 9999)}",
                "description": "Пристрій для навантажувального тестування Electric Monitor",
                "maxPower": random.choice([500, 750, 1000, 1500]),
                "location": {
                    "address": "Тестова Адреса 123",
                    "city": "Харків",
                    "country": "Україна"
                }
            }
            self.client.post("/api/devices", json=device_data, headers=self.headers)
    
    @task(1)
    def get_power_data_comparison(self):
        """Тест endpoint порівняння даних потужності"""
        if hasattr(self, 'headers') and self.headers:
            self.client.get("/api/power-data/comparison", headers=self.headers)
    
    @task(1)
    def get_alerts(self):
        """Отримання алертів користувача"""
        if hasattr(self, 'headers') and self.headers:
            self.client.get("/api/alerts", headers=self.headers)