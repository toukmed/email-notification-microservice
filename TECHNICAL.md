# Email Notification Microservice - Technical Documentation

## Overview

A production-ready email notification microservice built with Spring Boot that provides reliable, queue-based email delivery with template support and comprehensive delivery tracking.

---

## Technical Stack

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Framework** | Spring Boot | 3.2.4 | Application framework |
| **Language** | Java | 17 | Programming language |
| **Database** | PostgreSQL | 15 | Primary data store |
| **ORM** | Spring Data JPA / Hibernate | 6.x | Object-relational mapping |
| **Migrations** | Flyway | 10.10.0 | Database schema versioning |
| **Email** | Spring Mail (JavaMailSender) | - | SMTP email sending |
| **Templates** | Thymeleaf | - | Email template rendering |
| **API Docs** | SpringDoc OpenAPI | 2.3.0 | Swagger UI documentation |
| **Validation** | Jakarta Validation | - | Request validation |
| **Build Tool** | Maven | 3.x | Dependency management & build |
| **Containerization** | Docker & Docker Compose | - | Deployment |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT APPLICATIONS                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              REST API LAYER                                  │
│  ┌─────────────────────┐    ┌─────────────────────┐                        │
│  │   EmailController   │    │  TemplateController │                        │
│  │  /api/v1/emails/*   │    │  /api/v1/templates/*│                        │
│  └─────────────────────┘    └─────────────────────┘                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                             SERVICE LAYER                                    │
│  ┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────┐ │
│  │  EmailQueueService  │    │EmailTemplateService │    │EmailSenderService│ │
│  │  - Queue emails     │    │  - CRUD templates   │    │  - SMTP sending │ │
│  │  - Track status     │    │  - Render variables │    │  - Error handling│ │
│  └─────────────────────┘    └─────────────────────┘    └─────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                    ┌──────────────────┴──────────────────┐
                    ▼                                      ▼
┌───────────────────────────────┐    ┌────────────────────────────────────────┐
│       SCHEDULER LAYER         │    │           DATA ACCESS LAYER            │
│  ┌─────────────────────────┐  │    │  ┌─────────────────────────────────┐  │
│  │  EmailQueueProcessor    │  │    │  │     JPA Repositories            │  │
│  │  - Poll pending emails  │  │    │  │  - EmailQueueRepository         │  │
│  │  - Process in batches   │  │    │  │  - EmailTemplateRepository      │  │
│  │  - Retry failed emails  │  │    │  │  - EmailDeliveryLogRepository   │  │
│  └─────────────────────────┘  │    │  └─────────────────────────────────┘  │
└───────────────────────────────┘    └────────────────────────────────────────┘
                    │                                      │
                    ▼                                      ▼
┌───────────────────────────────┐    ┌────────────────────────────────────────┐
│         SMTP SERVER           │    │            PostgreSQL                  │
│   (MailHog for development)   │    │  ┌──────────────────────────────────┐ │
└───────────────────────────────┘    │  │  email_templates                 │ │
                                     │  │  email_queue                     │ │
                                     │  │  email_delivery_log              │ │
                                     │  └──────────────────────────────────┘ │
                                     └────────────────────────────────────────┘
```

---

## Features Implemented

### 1. Email Queue System
- **Database-backed queue** - Emails are persisted to PostgreSQL ensuring no data loss
- **Batch processing** - Configurable batch size for efficient processing
- **Automatic retries** - Failed emails are retried up to a configurable limit
- **Scheduled polling** - Background scheduler processes pending emails at configurable intervals
- **Optimistic locking** - Prevents duplicate processing in multi-instance deployments

### 2. Email Templates
- **Template management** - Full CRUD operations for email templates
- **Variable substitution** - Mustache-style `{{variable}}` placeholders
- **Thymeleaf rendering** - Powerful template engine support
- **Pre-built templates** - Includes welcome, password-reset, and notification templates

### 3. Delivery Tracking
- **Status tracking** - PENDING → PROCESSING → SENT/FAILED lifecycle
- **Delivery history** - Complete audit trail of all delivery attempts
- **Error logging** - Detailed error messages for failed deliveries
- **Query capabilities** - Filter emails by status, recipient, date range

### 4. REST API
- **Send emails** - Queue emails with or without templates
- **Check status** - Get real-time delivery status with history
- **List emails** - Paginated list with filtering options
- **Manage templates** - Create, read, update, delete templates
- **OpenAPI documentation** - Interactive Swagger UI

### 5. Configuration Options
- **SMTP settings** - Configurable mail server connection
- **Queue tuning** - Adjust polling interval, batch size, retry limits
- **Environment variables** - Externalized configuration for deployment

---

## Database Schema

### email_templates
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| name | VARCHAR(100) | Unique template identifier |
| subject_template | VARCHAR(500) | Email subject with placeholders |
| body_template | TEXT | HTML body with placeholders |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

### email_queue
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| template_id | UUID | FK to email_templates (nullable) |
| recipient | VARCHAR(255) | Recipient email address |
| cc | VARCHAR(1000) | CC recipients (comma-separated) |
| bcc | VARCHAR(1000) | BCC recipients (comma-separated) |
| subject | VARCHAR(500) | Rendered email subject |
| body | TEXT | Rendered email body |
| variables | JSONB | Template variables used |
| status | VARCHAR(20) | PENDING, PROCESSING, SENT, FAILED |
| retry_count | INT | Number of retry attempts |
| error_message | TEXT | Last error message |
| scheduled_at | TIMESTAMP | When to send the email |
| created_at | TIMESTAMP | Queue timestamp |
| updated_at | TIMESTAMP | Last status change |

### email_delivery_log
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| email_id | UUID | FK to email_queue |
| status | VARCHAR(20) | Status at this point |
| error_message | TEXT | Error details if failed |
| created_at | TIMESTAMP | Log entry timestamp |

---

## How It Works

### Email Sending Flow

```
1. CLIENT REQUEST
   POST /api/v1/emails/send
   {
     "recipient": "user@example.com",
     "templateName": "welcome",
     "variables": {"userName": "John"}
   }

2. VALIDATION & QUEUING
   ├── Validate request (recipient format, required fields)
   ├── Load template (if templateName provided)
   ├── Render subject and body with variables
   ├── Create EmailQueue record with status=PENDING
   └── Return email ID to client (HTTP 202 Accepted)

3. BACKGROUND PROCESSING (every 5 seconds by default)
   ├── Query: SELECT * FROM email_queue WHERE status='PENDING' AND scheduled_at <= NOW()
   ├── For each email:
   │   ├── Set status = PROCESSING (optimistic lock)
   │   ├── Build MimeMessage with subject, body, recipients
   │   ├── Send via JavaMailSender (SMTP)
   │   ├── On success: status = SENT, log delivery
   │   └── On failure: increment retry_count, status = PENDING or FAILED

4. STATUS CHECK
   GET /api/v1/emails/{id}/status
   └── Returns current status + full delivery history
```

### Template Variable Substitution

Templates use `{{variableName}}` syntax:

```html
<!-- Template -->
<h1>Welcome, {{userName}}!</h1>
<p>Thanks for joining {{appName}}.</p>

<!-- Variables -->
{"userName": "John", "appName": "MyService"}

<!-- Rendered Output -->
<h1>Welcome, John!</h1>
<p>Thanks for joining MyService.</p>
```

### Retry Logic

```
Attempt 1: Send email → FAILED (SMTP timeout)
           retry_count = 1, status = PENDING

Attempt 2: Send email → FAILED (recipient rejected)
           retry_count = 2, status = PENDING

Attempt 3: Send email → FAILED (server error)
           retry_count = 3, status = FAILED (max retries reached)
           
Each attempt is logged in email_delivery_log for audit trail.
```

---

## API Reference

### Send Email
```http
POST /api/v1/emails/send
Content-Type: application/json

{
  "recipient": "user@example.com",
  "templateName": "welcome",           // Optional: use template
  "subject": "Custom Subject",         // Required if no template
  "body": "<h1>HTML Body</h1>",        // Required if no template
  "cc": "cc@example.com",              // Optional
  "bcc": "bcc@example.com",            // Optional
  "variables": {                       // Optional: template variables
    "userName": "John",
    "appName": "MyApp"
  },
  "scheduledAt": "2024-12-01T10:00:00Z" // Optional: delayed send
}
```

### Get Email Status
```http
GET /api/v1/emails/{id}/status

Response:
{
  "id": "uuid",
  "recipient": "user@example.com",
  "subject": "Welcome to MyApp!",
  "currentStatus": "SENT",
  "retryCount": 0,
  "createdAt": "2024-01-01T12:00:00Z",
  "deliveryHistory": [
    {
      "status": "SENT",
      "errorMessage": null,
      "timestamp": "2024-01-01T12:00:05Z"
    }
  ]
}
```

### List Emails
```http
GET /api/v1/emails?status=SENT&recipient=user@example.com&page=0&size=20
```

### Template Operations
```http
GET    /api/v1/templates              # List all templates
POST   /api/v1/templates              # Create template
GET    /api/v1/templates/{id}         # Get by ID
GET    /api/v1/templates/name/{name}  # Get by name
PUT    /api/v1/templates/{id}         # Update template
DELETE /api/v1/templates/{id}         # Delete template
```

---

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | postgres | Database username |
| `DB_PASSWORD` | postgres | Database password |
| `SMTP_HOST` | localhost | SMTP server hostname |
| `SMTP_PORT` | 1025 | SMTP server port |
| `SMTP_USERNAME` | (empty) | SMTP authentication username |
| `SMTP_PASSWORD` | (empty) | SMTP authentication password |
| `SMTP_AUTH` | false | Enable SMTP authentication |
| `SMTP_STARTTLS` | false | Enable STARTTLS |
| `EMAIL_FROM` | noreply@example.com | Sender email address |
| `EMAIL_FROM_NAME` | Email Service | Sender display name |

### Queue Configuration (application.yml)

```yaml
email:
  queue:
    poll-interval: 5000      # Milliseconds between queue checks
    batch-size: 10           # Emails processed per batch
    max-retries: 3           # Maximum retry attempts
    retry-delay-ms: 60000    # Delay before retry (not yet implemented)
```

---

## Project Structure

```
email-notification-microservice/
├── src/main/java/com/notification/email/
│   ├── EmailNotificationApplication.java    # Main entry point
│   ├── config/
│   │   └── OpenApiConfig.java               # Swagger configuration
│   ├── controller/
│   │   ├── EmailController.java             # Email API endpoints
│   │   └── TemplateController.java          # Template API endpoints
│   ├── dto/
│   │   ├── SendEmailRequest.java            # Email request DTO
│   │   ├── EmailResponse.java               # Email response DTO
│   │   ├── EmailStatusResponse.java         # Status response DTO
│   │   ├── EmailTemplateRequest.java        # Template request DTO
│   │   ├── EmailTemplateResponse.java       # Template response DTO
│   │   └── PageResponse.java                # Pagination wrapper
│   ├── entity/
│   │   ├── EmailQueue.java                  # Email queue entity
│   │   ├── EmailTemplate.java               # Template entity
│   │   ├── EmailDeliveryLog.java            # Delivery log entity
│   │   └── EmailStatus.java                 # Status enum
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java      # Central error handling
│   │   ├── ResourceNotFoundException.java   # 404 errors
│   │   ├── DuplicateResourceException.java  # 409 errors
│   │   └── ValidationException.java         # 400 errors
│   ├── repository/
│   │   ├── EmailQueueRepository.java        # Queue data access
│   │   ├── EmailTemplateRepository.java     # Template data access
│   │   └── EmailDeliveryLogRepository.java  # Log data access
│   ├── scheduler/
│   │   └── EmailQueueProcessor.java         # Background queue processor
│   └── service/
│       ├── EmailQueueService.java           # Queue business logic
│       ├── EmailTemplateService.java        # Template business logic
│       └── EmailSenderService.java          # SMTP sending logic
├── src/main/resources/
│   ├── application.yml                      # Main configuration
│   ├── application-docker.yml               # Docker profile config
│   └── db/migration/
│       └── V1__Initial_schema.sql           # Database schema
├── Dockerfile                               # Multi-stage Docker build
├── docker-compose.yml                       # Full stack deployment
├── deploy.sh                                # Deployment script
├── pom.xml                                  # Maven dependencies
└── README.md                                # Quick start guide
```

---

## Deployment

### Docker Compose (Recommended)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

**Services:**
- **App**: http://localhost:8085 (API)
- **Swagger UI**: http://localhost:8085/swagger
- **PostgreSQL**: localhost:5433
- **MailHog**: http://localhost:8025 (captured emails)

### Production Considerations

1. **Environment Variables** - Set secure passwords and real SMTP credentials
2. **Database** - Use managed PostgreSQL with backups
3. **SMTP** - Configure real mail server (SendGrid, AWS SES, etc.)
4. **Scaling** - Run multiple instances; optimistic locking prevents duplicates
5. **Monitoring** - Add health checks and metrics (Spring Actuator)
6. **Security** - Add authentication (Spring Security) for production

---

## License

MIT License - See LICENSE file for details.
