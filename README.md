# Library Management System

Spring Boot библиотечна система с REST API.

## 🚀 Технологии

- Java 25
- Spring Boot 4.0.3
- PostgreSQL / H2
- Maven
- Docker
- Swagger/OpenAPI

## 📋 Изисквания

- Java 25
- Maven
- PostgreSQL (за development) или Docker
- DBeaver (по избор)

## 🛠️ Конфигурация

### Локална разработка (без Docker)

1. Създай PostgreSQL база данни:
```sql
CREATE DATABASE library_dev;
CREATE USER library_user WITH PASSWORD 'library_pass';
GRANT ALL PRIVILEGES ON DATABASE library_dev TO library_user;\

##Стартирай приложението:

mvn spring-boot:run -Dspring-boot.run.profiles=dev

## С Docker
# Build
mvn clean package

# Стартирай
docker-compose up -d

## API Endpoints
Книги (Books)
GET /api/books - всички книги

GET /api/books/{id} - книга по ID

POST /api/books - създай книга

PUT /api/books/{id} - обнови книга

DELETE /api/books/{id} - изтрий книга

GET /api/books/search/title?title= - търсене по заглавие

GET /api/books/available - налични книги

Читатели (Patrons)
GET /api/patrons - всички читатели

GET /api/patrons/{id} - читател по ID

POST /api/patrons - създай читател

PUT /api/patrons/{id} - обнови читател

DELETE /api/patrons/{id} - изтрий читател

GET /api/patrons/overdue - читатели с просрочени книги

Заемания (Borrowings)
POST /api/borrowings/borrow/{bookId}/patron/{patronId} - заеми книга

PUT /api/borrowings/return/{bookId}/patron/{patronId} - върни книга

GET /api/borrowings/overdue - всички просрочени заемания

📖 Документация
Swagger UI: http://localhost:8080/swagger-ui.html

OpenAPI JSON: http://localhost:8080/api-docs

🧪 Тестове
mvn test


## Docker команди

# Стартирай
docker-compose up -d

# Спри
docker-compose down

# Логове
docker-compose logs -f library-app

##DBeaver
Свържи се с PostgreSQL:

Host: localhost

Port: 5432

Database: library_dev

Username: library_user

Password: library_pass

 Автор
Твоето име


