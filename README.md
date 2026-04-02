# Email Notification Microservice

A Spring Boot microservice for sending email notifications with template support, queue-based processing, and delivery tracking.

## Features

- **REST API** - Trigger emails via HTTP endpoints
- **Email Templates** - Thymeleaf-based templates with variable substitution
- **Queue Processing** - Database-backed queue for reliable delivery
- **Delivery Tracking** - Track email status and delivery history
- **Retry Logic** - Automatic retries for failed deliveries

## Tech Stack

- Java 17
- Spring Boot 3.2.4
- PostgreSQL
- Flyway (migrations)
- Thymeleaf (templates)
- Docker

## Quick Start

### Using Docker Compose (Recommended)

```bash
docker-compose up -d
```

This starts:
- **App**: http://localhost:8085
- **PostgreSQL**: localhost:5433
- **MailHog** (test SMTP): http://localhost:8025

### Local Development

1. Start PostgreSQL:
```bash
docker run -d --name postgres -e POSTGRES_DB=email_service -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:15-alpine
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

## API Endpoints

### Emails

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/emails/send` | Queue an email |
| GET | `/api/v1/emails/{id}/status` | Get delivery status |
| GET | `/api/v1/emails` | List emails with filters |

### Templates

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/templates` | Create template |
| GET | `/api/v1/templates` | List all templates |
| GET | `/api/v1/templates/{id}` | Get template by ID |
| GET | `/api/v1/templates/name/{name}` | Get template by name |
| PUT | `/api/v1/templates/{id}` | Update template |
| DELETE | `/api/v1/templates/{id}` | Delete template |

## Usage Examples

### Send Email with Template

```bash
curl -X POST http://localhost:8085/api/v1/emails/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "templateName": "welcome",
    "variables": {
      "userName": "John",
      "appName": "MyApp"
    }
  }'
```

### Send Email without Template

```bash
curl -X POST http://localhost:8085/api/v1/emails/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Hello!",
    "body": "<h1>Welcome!</h1><p>This is a test email.</p>"
  }'
```

### Check Email Status

```bash
curl http://localhost:8085/api/v1/emails/{email-id}/status
```

### Create Template

```bash
curl -X POST http://localhost:8085/api/v1/templates \
  -H "Content-Type: application/json" \
  -d '{
    "name": "order-confirmation",
    "subjectTemplate": "Order #{{orderId}} Confirmed",
    "bodyTemplate": "<h1>Thank you, {{customerName}}!</h1><p>Your order #{{orderId}} has been confirmed.</p>"
  }'
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | PostgreSQL username | postgres |
| `DB_PASSWORD` | PostgreSQL password | postgres |
| `SMTP_HOST` | SMTP server host | localhost |
| `SMTP_PORT` | SMTP server port | 1025 |
| `SMTP_USERNAME` | SMTP username | (empty) |
| `SMTP_PASSWORD` | SMTP password | (empty) |
| `SMTP_AUTH` | Enable SMTP auth | false |
| `SMTP_STARTTLS` | Enable STARTTLS | false |
| `EMAIL_FROM` | Sender email | noreply@example.com |
| `EMAIL_FROM_NAME` | Sender name | Email Service |

### Queue Settings

```yaml
email:
  queue:
    poll-interval: 5000    # ms between queue checks
    batch-size: 10         # emails per batch
    max-retries: 3         # retry attempts
    retry-delay-ms: 60000  # delay between retries
```

## Built-in Templates

The service includes these default templates:

- `welcome` - New user welcome
- `password-reset` - Password reset request
- `notification` - Generic notification

## Project Structure

```
src/main/java/com/notification/email/
├── controller/      # REST controllers
├── dto/            # Request/Response DTOs
├── entity/         # JPA entities
├── exception/      # Exception handlers
├── repository/     # Data repositories
├── scheduler/      # Queue processor
└── service/        # Business logic
```

## License

MIT
