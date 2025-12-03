# Book API Service

Spring Boot сервіс для керування книгами та авторами.

## Технології
- Java 21
- Spring Boot 
- Spring Data JPA
- H2 
- Liquibase для управління схемою БД
- Maven
- JUnit 5 + Spring Boot Test для інтеграційних тестів

## Сутності
- **Author (Сутність 2)**: автор книги
    - `id`: Long, PK
    - `name`: String, унікальне ім’я
- **Book (Сутність 1)**: книга
    - `id`: Long, PK
    - `title`: String
    - `yearPublished`: Integer
    - `author`: Many-to-One до Author
    - `genres`: List<String> (через окрему таблицю)


---
## swagger http://localhost:8080/swagger-ui/index.html
## **Base URL:** `http://localhost:8080/api`
### Автори доступні одразу,для того щоб перевірити книги спочатку треба їх додати upload.json.

POST http://localhost:8080/api/book/_upload

У Postman:

Перейти Body

Вибрати form-data

Додати поле:

KEY	TYPE	VALUE
file	File	(вибрати JSON файл)
## **AuthorController — /api/author**

| Method | Endpoint           | Опис                       |
|--------|------------------|----------------------------|
| GET    | `/api/author`     | Отримати всіх авторів      |
| GET    | `/api/author/{id}`| Отримати автора за ID      |
| POST   | `/api/author`     | Створити автора            |
| PUT    | `/api/author/{id}`| Оновити автора             |
| DELETE | `/api/author/{id}`| Видалити автора            |

---

## **BookController — /api/book**

| Method | Endpoint                   | Опис                                 |
|--------|----------------------------|--------------------------------------|
| POST   | `/api/book`                | Створити книгу                       |
| PUT    | `/api/book/{id}`           | Оновити книгу                        |
| GET    | `/api/book/{id}`           | Отримати книгу                       |
| GET    | `/api/book`                | Отримати всі книги                   |
| DELETE | `/api/book/{id}`           | Видалити книгу                       |
| POST | `/api/books/search`           | Пошук книг з фільтрами та пагінацією |
| POST   | `/api/book/_list?page=&size=` | Список книг з фільтрами + пагінація  |
| POST   | `/api/book/_report`        | Згенерувати CSV файл                 |
| POST   | `/api/book/_upload`        | Завантажити JSON файл з книгами      |

---