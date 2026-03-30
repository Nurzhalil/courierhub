# 🚚 CourierHub

**Распределенная система управления курьерской службой доставки**

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-8-blue?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=for-the-badge)

---

## 📋 Описание проекта

CourierHub — это комплексное веб-приложение для управления курьерской службой доставки. Система позволяет автоматизировать процессы приема заказов, распределения их между курьерами, отслеживания доставки и обработки платежей.

### ✨ Основные возможности

- 👤 **Управление пользователями** — регистрация, аутентификация и профили
- 🚶 **Управление курьерами** — добавление, редактирование, отслеживание курьеров
- 📦 **Управление заказами** — создание, отслеживание, смена статуса заказов
- 💳 **Обработка платежей** — различные методы платежей, истории платежей
- 🗺️ **Отслеживание на карте** — одновременное отслеживание курьеров в реальном времени
- 🛡️ **Безопасность** — аутентификация и авторизация с использованием Spring Security
- ⚙️ **Административная панель** — управление системой и мониторинг

---

## 🏗️ Архитектура проекта

```
courierhub/
├── src/main/java/com/example/courierhub/
│   ├── config/              # Конфигурация приложения
│   │   ├── SecurityConfig   # Конфигурация безопасности
│   │   └── WebConfig        # Веб-конфигурация
│   ├── controller/          # REST контроллеры
│   │   ├── AuthController   # Аутентификация и регистрация
│   │   ├── CourierController
│   │   ├── OrderController
│   │   ├── PaymentController
│   │   ├── MapController
│   │   ├── UserController
│   │   └── AdminController
│   ├── model/               # JPA сущности
│   │   ├── User
│   │   ├── Courier
│   │   ├── Order
│   │   ├── Payment
│   │   ├── CourierStatus
│   │   ├── OrderStatus
│   │   └── PaymentStatus
│   ├── dto/                 # DTO классы
│   ├── repository/          # JPA репозитории
│   ├── service/             # Бизнес-логика
│   ├── security/            # Компоненты безопасности
│   └── CourierhubApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── templates/           # Thymeleaf шаблоны
│       ├── login.html
│       ├── register.html
│       ├── home.html
│       ├── admin/
│       ├── courier/
│       ├── orders/
│       └── payments/
└── pom.xml
```

---

## 🛠️ Требования

Перед началом работы убедитесь, что у вас установлены:

- **Java 17** или выше
- **Maven 3.8+**
- **MySQL 8.0+**
- **Git**

---

## 📦 Установка и запуск

### 1️⃣ Клонирование репозитория

```bash
git clone https://github.com/yourusername/courierhub.git
cd courierhub
```

### 2️⃣ Конфигурация базы данных

Создайте базу данных MySQL:

```sql
CREATE DATABASE courierhub;
```

### 3️⃣ Настройка конфигурации

Отредактируйте файл `src/main/resources/application.properties`:

```properties
spring.application.name=courierhub
server.port=8080

# Подключение к MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/courierhub?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root

# JPA настройки
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> ⚠️ **Важно:** Измените пароль базы данных в production-среде!

### 4️⃣ Сборка проекта

```bash
# Для Windows
mvnw clean install

# Для Linux/Mac
./mvnw clean install
```

### 5️⃣ Запуск приложения

```bash
# Для Windows
mvnw spring-boot:run

# Для Linux/Mac
./mvnw spring-boot:run
```

Приложение будет доступно по адресу: **http://localhost:8080**

---

## 🔐 Аутентификация

### Процесс входа

1. Перейдите на страницу входа: `http://localhost:8080/login`
2. Введите email и пароль
3. После успешной аутентификации будет установлена сессия

### Регистрация

1. На странице входа нажмите "Зарегистрироваться"
2. Заполните форму регистрации
3. Аккаунт будет создан с базовой ролью

---

## 📚 API Эндпоинты

### Аутентификация
- `POST /auth/register` — Регистрация нового пользователя
- `POST /auth/login` — Вход в систему
- `POST /auth/logout` — Выход из системы

### Пользователи
- `GET /users/{id}` — Получить информацию о пользователе
- `PUT /users/{id}` — Обновить профиль пользователя
- `DELETE /users/{id}` — Удалить пользователя

### Курьеры
- `GET /couriers` — Список всех курьеров
- `POST /couriers` — Добавить нового курьера
- `GET /couriers/{id}` — Получить информацию о курьере
- `PUT /couriers/{id}` — Обновить данные курьера
- `DELETE /couriers/{id}` — Удалить курьера

### Заказы
- `GET /orders` — Список заказов
- `POST /orders` — Создать новый заказ
- `GET /orders/{id}` — Получить информацию о заказе
- `PUT /orders/{id}` — Обновить заказ
- `DELETE /orders/{id}` — Удалить заказ

### Платежи
- `GET /payments` — История платежей
- `POST /payments` — Создать платеж
- `GET /payments/{id}` — Получить информацию о платеже

### Карта
- `GET /map/couriers` — Получить расположение всех курьеров в реальном времени

### Администрирование
- `GET /admin/dashboard` — Администраторская панель
- `GET /admin/couriers/list` — Список курьеров для администратора
- `GET /admin/couriers/form` — Форма для добавления курьера

---

## 🗃️ Модели данных

### User (Пользователь)
```
- id: Long (Primary Key)
- email: String (Unique)
- password: String (Encrypted)
- firstName: String
- lastName: String
- phone: String
- role: UserRole
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Courier (Курьер)
```
- id: Long (Primary Key)
- userId: Long (Foreign Key)
- status: CourierStatus
- currentLocation: String
- rating: Double
- completedOrders: Integer
- breaks: Set<CourierBreak>
- createdAt: LocalDateTime
```

### Order (Заказ)
```
- id: Long (Primary Key)
- userId: Long (Foreign Key to User)
- courierId: Long (Foreign Key to Courier)
- pickupLocation: String
- deliveryLocation: String
- status: OrderStatus
- amount: BigDecimal
- createdAt: LocalDateTime
- deliveredAt: LocalDateTime
```

### Payment (Платеж)
```
- id: Long (Primary Key)
- orderId: Long (Foreign Key)
- amount: BigDecimal
- method: PaymentMethod
- status: PaymentStatus
- transactionId: String
- createdAt: LocalDateTime
```

---

## 🔧 Используемые технологии

| Технология | Описание |
|-----------|----------|
| **Spring Boot 3.4.4** | Фреймворк для создания приложений |
| **Spring Security** | Аутентификация и авторизация |
| **Spring Data JPA** | ORM и управление данными |
| **MySQL** | Релационная база данных |
| **Thymeleaf** | Шаблончик для веб-интерфейса |
| **Maven** | Система управления зависимостями |
| **Java 17** | Язык программирования |

---

## 📝 Структура файлов конфигурации

### application.properties
Основные настройки приложения, подключение БД, JPA конфигурация

### SecurityConfig
- Конфигурация Spring Security
- Правила доступа для разных URL
- Шифрование паролей

### WebConfig
- Конфигурация веб-страниц
- CORS настройки
- Интерцепторы

---

## 🚀 Особенности реализации

✅ **Spring Security** — Защита от CSRF атак, управление сеансами
✅ **JPA/Hibernate** — Автоматическое управление схемой БД
✅ **Thymeleaf** — Динамические веб-страницы с шаблонами
✅ **Валидация данных** — Spring Validation для входных данных
✅ **Статусы и перечисления** — Enum для управления статусами
✅ **Отличный дизайн** — Адаптивный веб-интерфейс

---

## 📋 Возможные улучшения

- 🔐 OAuth2 аутентификация
- 📱 Мобильное приложение
- 🔔 Система уведомлений (WebSocket, Push)
- 📊 Аналитика и отчеты
- 🗺️ Интеграция с Google Maps API
- 💬 Система чатов между курьером и клиентом
- 🧪 Написание unit-тестов
- 🐳 Docker контеинеризация

---

## 🤝 Вклад

Мы приветствуем вклад в развитие проекта! Для этого:

1. Форкните репозиторий
2. Создайте ветвь для вашей функции (`git checkout -b feature/AmazingFeature`)
3. Коммитьте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Отправьте изменения (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

---

## 📄 Лицензия

Этот проект распространяется под лицензией MIT License. Смотрите файл `LICENSE` для деталей.

---

## 📞 Контакты и поддержка

- **Email:** support@courierhub.com
- **Проблемы:** [GitHub Issues](https://github.com/yourusername/courierhub/issues)
- **Обсуждения:** [GitHub Discussions](https://github.com/yourusername/courierhub/discussions)

---

## 📚 Дополнительные ресурсы

- [Spring Boot документация](https://spring.io/projects/spring-boot)
- [Spring Security документация](https://spring.io/projects/spring-security)
- [Thymeleaf документация](https://www.thymeleaf.org/)
- [MySQL документация](https://dev.mysql.com/doc/)

---

<div align="center">

**Спасибо за использование CourierHub! 🎉**

Если проект был полезным, поставьте ⭐ звезду репозиторию!

</div>
