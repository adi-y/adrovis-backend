# Adrovis Backend

A production-ready backend for **Adrovis**, built with **Spring Boot 3**, following clean architecture, DTO-based design, and RESTful API principles.

The backend powers the Adrovis website by providing APIs for Careers, Contact, file uploads, email notifications, and future business modules.

---

# Tech Stack

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- MySQL 8
- Flyway
- Spring Validation
- Spring Mail
- Spring Async
- MapStruct
- Lombok
- springdoc-openapi (Swagger)

---

# Architecture

```
Controller
        │
        ▼
Service
        │
        ▼
Repository
        │
        ▼
Entity
```

- DTO-based architecture
- MapStruct for object mapping
- Global exception handling
- Unified API responses
- Production-ready validation
- Flyway database migrations

---

# Project Structure

```
src/main/java/com/adrovis/adrovis_backend

├── career
├── common
├── contact
├── email
├── storage
└── config
```

---

# Features Implemented

## Foundation

### Global Exception Handling

- AppException
- ErrorCode
- GlobalExceptionHandler
- ResourceNotFoundException
- InvalidStateException
- FileValidationException

---

### Common Module

Includes

- BaseAuditableEntity
- ApiResponse
- ApplicationIdGenerator
- FileValidationUtil

---

### Configuration

- Profile-based configuration
- CORS
- Swagger/OpenAPI
- Async Configuration
- Mail Configuration
- Multipart Upload Configuration

---

### Database

- MySQL
- Flyway Versioning
- UUID Primary Keys
- Optimistic Locking
- Auditing

---

# Careers Module

## Jobs

Implemented APIs

```
GET /api/v1/jobs

GET /api/v1/jobs/{id}

GET /api/v1/jobs/status/{status}
```

Supports

- Active Jobs
- Status Filtering
- DTO Responses

---

## Program Applications

Implemented APIs

```
POST /api/v1/careers/program/applications

PATCH /api/v1/careers/program/applications/{applicationId}/submit
```

Features

- Draft creation
- Resume upload
- PDF validation
- 5 MB limit
- Pending → Submitted workflow
- Confirmation email
- Duplicate protection

---

## Job Applications

Supports

- Resume Upload
- Validation
- Immediate Submission
- Email Notification

---

# Contact Module

Implemented APIs

```
POST /api/v1/contact/callback

POST /api/v1/contact/consultation
```

Features

- Callback Requests
- Consultation Requests
- Lead Management
- Validation
- UUID-based entities
- MapStruct
- Flyway migration
- Swagger documentation

---

# Email Module

Implemented

- JavaMailSender
- Async Email Sending
- HTML Email Templates
- Zoho SMTP Integration

Current Emails

- Program Application Confirmation
- Job Application Confirmation

---

# File Storage

Implemented

- Local Storage
- PDF Validation
- Metadata Persistence

Restrictions

- PDF only
- Maximum 5 MB

---

# API Response Format

Every endpoint returns a unified response.

Success

```json
{
  "success": true,
  "status": 200,
  "message": "Success",
  "data": {}
}
```

Validation Error

```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed.",
  "fieldErrors": {
    "email": "Email is required."
  }
}
```

---

# Database Management

Flyway is used for database versioning.

Migrations are located in

```
src/main/resources/db/migration
```

Hibernate is configured with

```
ddl-auto=validate
```

to prevent accidental schema modifications.

---

# Email

SMTP Provider

```
Zoho Mail
```

Email sending is asynchronous using Spring Async.

---

# Swagger

Development

```
http://localhost:8080/swagger-ui/index.html
```

Swagger is disabled in production.

---

# Running Locally

## Clone

```bash
git clone https://github.com/<your-username>/adrovis-backend.git
```

---

## Configure

Update

```
application-dev.yml
```

with your local

- MySQL credentials
- Zoho SMTP credentials

---

## Start MySQL

Create database

```sql
CREATE DATABASE adrovis_dev;
```

---

## Run

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

---

# Production Configuration

Production uses environment variables.

Example

```
SPRING_PROFILES_ACTIVE=prod

DB_URL=jdbc:mysql://...

DB_USERNAME=...

DB_PASSWORD=...

MAIL_HOST=smtp.zoho.in
MAIL_PORT=587
MAIL_USERNAME=...
MAIL_PASSWORD=...
MAIL_FROM=hello@adrovis.com

APP_BASE_URL=https://api.adrovis.com
```

No production secrets are stored inside the repository.

---

# Development Progress

## Completed

- Foundation
- Common Module
- Configuration
- Flyway
- File Storage
- Careers Module
- Email Module
- Contact Module

---

## Planned

- Newsletter Module
- Admin Module
- Authentication & Authorization
- Dashboard Analytics
- AWS S3 Storage
- Docker Support
- CI/CD Pipeline

---

# Design Principles

- Clean Architecture
- SOLID Principles
- Constructor Injection
- DTO-based APIs
- Global Exception Handling
- Stateless REST APIs
- Production-ready Code
- OpenAPI Documentation
- Async Processing
- Database Versioning

---

# License

Copyright © Adrovis.

All Rights Reserved.