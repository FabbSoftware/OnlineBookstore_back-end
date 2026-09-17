# Online Bookstore Backend

A RESTful API backend for the Online Bookstore application, built with **Spring Boot 3**, **Java 17/25**, **Spring Security with JWT**, **Spring Data JPA**, and **H2 In-Memory Database**. Developed strictly using **Test-Driven Development (TDD)** and clean architecture principles.

---

## Features

- **Authentication & Authorization**:
  - Stateless JWT authentication via Spring Security filter chain
  - User registration and login endpoints with BCrypt password hashing
  - Role-based authorization (`ROLE_USER`, `ROLE_ADMIN`)
- **Book Catalog**:
  - Browse available books and search by title or author
  - Fetch detailed book information by UUID
  - Pre-seeded sample tech books via `DataInitializer`
- **Shopping Cart**:
  - User-specific shopping cart
  - Add items to cart with real-time stock validation
  - Update item quantities and remove individual items
  - Clear cart
  - Automatic calculation of item subtotals and cart total
- **Checkout & Orders**:
  - Convert active cart to completed order with price snapshots
  - Automatic inventory deduction
  - Order history retrieval
  - Detailed order lookup by order UUID with user isolation
- **Reliability & Consistency**:
  - UUID identifiers across all domain entities (`User`, `Book`, `Cart`, `CartItem`, `Order`, `OrderItem`)
  - Global exception handling with structured RFC-compliant error payloads
  - CORS configuration driven by `application.yml`

---

## Tech Stack

- **Framework**: Spring Boot 3.4.3
- **Language**: Java 17+ (JDK 25 compatible)
- **Build Tool**: Gradle with Gradle Wrapper (`gradlew`)
- **Database**: H2 In-Memory (`jdbc:h2:mem:bookstoredb`)
- **Object Mapping**: MapStruct 1.5.5.Final
- **Security**: Spring Security 6 + JJWT 0.12.6
- **Testing**: JUnit 5, Mockito, AssertJ, Spring MockMvc

---

## Getting Started

### Prerequisites

- Java 17 or higher (Java 25 supported)
- Git

### Build and Run Tests

Run the full automated test suite:

```powershell
./gradlew test
```

### Run Application

Start the Spring Boot backend server on port `8080`:

```powershell
./gradlew bootRun
```

The server will be available at `http://localhost:8080`.

### H2 Database Console

- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:bookstoredb`
- **User Name**: `sa`
- **Password**: *(leave empty)*

---

## API Reference

### 1. Authentication

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | Public |
| `POST` | `/api/auth/login` | Authenticate and obtain JWT token | Public |

#### `POST /api/auth/register`
**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123",
  "fullName": "Jane Doe"
}
```
**Response (`201 Created`)**:
```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "user": {
    "id": "c1f7a4de-8e43-4e86-9a29-768ad1df1bb0",
    "email": "user@example.com",
    "fullName": "Jane Doe",
    "role": "ROLE_USER"
  }
}
```

#### `POST /api/auth/login`
**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
**Response (`200 OK`)**:
```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "user": {
    "id": "c1f7a4de-8e43-4e86-9a29-768ad1df1bb0",
    "email": "user@example.com",
    "fullName": "Jane Doe",
    "role": "ROLE_USER"
  }
}
```

---

### 2. Books

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/books` | Get all books (supports optional `?q=` search query) | Public |
| `GET` | `/api/books/{id}` | Get book details by UUID | Public |

#### `GET /api/books`
**Response (`200 OK`)**:
```json
[
  {
    "id": "e8a719c2-55fb-4293-9c86-1eb95627a85a",
    "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
    "author": "Robert C. Martin",
    "price": 34.99,
    "description": "Even bad code can function...",
    "isbn": "978-0132350884",
    "coverImageUrl": "https://images.unsplash.com/...",
    "stockQuantity": 25
  }
]
```

---

### 3. Shopping Cart

*(Requires header: `Authorization: Bearer <token>`)*

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `GET` | `/api/cart` | Get current user's shopping cart | Bearer JWT |
| `POST` | `/api/cart/items` | Add book to cart | Bearer JWT |
| `PUT` | `/api/cart/items/{itemId}` | Update item quantity | Bearer JWT |
| `DELETE` | `/api/cart/items/{itemId}` | Remove item from cart | Bearer JWT |
| `DELETE` | `/api/cart` | Clear entire cart | Bearer JWT |

#### `POST /api/cart/items`
**Request Body**:
```json
{
  "bookId": "e8a719c2-55fb-4293-9c86-1eb95627a85a",
  "quantity": 2
}
```
**Response (`200 OK`)**:
```json
{
  "id": "b34e44e2-6bfb-47e0-b747-d1cb859424c5",
  "items": [
    {
      "id": "92f70eb1-ff73-455b-b9d9-0b04a08bb231",
      "bookId": "e8a719c2-55fb-4293-9c86-1eb95627a85a",
      "bookTitle": "Clean Code: A Handbook of Agile Software Craftsmanship",
      "bookAuthor": "Robert C. Martin",
      "bookPrice": 34.99,
      "bookCoverImageUrl": "https://images.unsplash.com/...",
      "quantity": 2,
      "subtotal": 69.98
    }
  ],
  "totalItems": 2,
  "totalAmount": 69.98
}
```

---

### 4. Orders & Checkout

*(Requires header: `Authorization: Bearer <token>`)*

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| `POST` | `/api/orders` | Checkout cart to create an order | Bearer JWT |
| `GET` | `/api/orders` | Retrieve user order history | Bearer JWT |
| `GET` | `/api/orders/{id}` | Retrieve specific order summary | Bearer JWT |

#### `POST /api/orders`
**Request Body**:
```json
{
  "shippingAddress": "123 Main Street, Apt 4B, Springfield, OR",
  "contactPhone": "+1-555-0199"
}
```
**Response (`201 Created`)**:
```json
{
  "id": "d74beaf9-236b-4e1b-9f94-817ec7dc4853",
  "items": [
    {
      "id": "3ec3bbca-e544-42f1-aa56-11f421946fe8",
      "bookId": "e8a719c2-55fb-4293-9c86-1eb95627a85a",
      "bookTitle": "Clean Code: A Handbook of Agile Software Craftsmanship",
      "bookAuthor": "Robert C. Martin",
      "bookCoverImageUrl": "https://images.unsplash.com/...",
      "price": 34.99,
      "quantity": 2,
      "subtotal": 69.98
    }
  ],
  "totalAmount": 69.98,
  "status": "CONFIRMED",
  "shippingAddress": "123 Main Street, Apt 4B, Springfield, OR",
  "contactPhone": "+1-555-0199",
  "createdAt": "2026-09-17T18:08:00Z"
}
```
