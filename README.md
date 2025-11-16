# Dejavoo Middleware - Settlement Webhook Service

A Spring Boot application that receives and processes settlement webhooks from Dejavoo payment terminals.

## Features

- REST API endpoint for receiving settlement webhooks
- Basic Authentication for secure webhook reception
- Duplicate settlement detection
- Automatic settlement record persistence
- H2 in-memory database (easily switchable to PostgreSQL)

## Technologies

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- H2 Database (dev) / PostgreSQL (prod)
- Lombok
- Maven

## API Endpoints

### Webhook Endpoint
```
POST /api/webhook/{merchantId}
```

Receives settlement webhook notifications and stores settlement data.

**Headers:**
- `Authorization: Basic <base64-encoded-credentials>`
- `Content-Type: application/json`

**Path Parameters:**
- `merchantId` - The unique merchant identifier

**Request Body:**
```json
{
  "id": "f5b15dac-359f-439d-8977-a0226c467dc7",
  "eventType": "Settlement",
  "subEventType": "ClosedBatch",
  "batchNumber": "469",
  "settlementDate": "2024-10-08",
  "settlementCount": 2,
  "settlementAmount": 2.6,
  "tpn": "342824380352",
  "settlementTxnDetails": [
    {
      "txnDate": "2024-10-08 04:06:18",
      "transactionType": "SALE",
      "transactionId": "38939820834338035220241008133618",
      "txnAmount": 1.3,
      "baseAmount": 1.1
    }
  ]
}
```

**Response:**

Success (201 Created):
```json
{
  "success": true,
  "message": "Settlement processed successfully",
  "settlementId": "f5b15dac-359f-439d-8977-a0226c467dc7",
  "status": "PROCESSED"
}
```

Duplicate (409 Conflict):
```json
{
  "success": false,
  "message": "Settlement already processed",
  "settlementId": "f5b15dac-359f-439d-8977-a0226c467dc7",
  "status": "DUPLICATE"
}
```

### Health Check
```
GET /api/webhook/health
```

Returns service health status (no authentication required).

## Configuration

### Application Properties

Configure the following in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Database Configuration (H2 for development)
spring.datasource.url=jdbc:h2:mem:dejavoo_db

# Security - Basic Auth Credentials
security.basic.auth.username=clntkey_RnE_XeycipUEOwepG1wRWAOZCQtDC7snQ03
security.basic.auth.password=
```

### PostgreSQL Configuration

For production, uncomment and configure PostgreSQL settings:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dejavoo_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Database Schema

The application creates a `settlements` table with the following structure:

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key (auto-increment) |
| settlement_id | VARCHAR(100) | Unique settlement identifier |
| merchant_id | VARCHAR(100) | Merchant identifier |
| settlement_date | DATE | Settlement date |
| settlement_amount | DECIMAL(19,2) | Settlement amount |
| batch_number | VARCHAR(50) | Batch number |
| settlement_count | INTEGER | Number of transactions |
| tpn | VARCHAR(50) | Terminal ID |
| event_type | VARCHAR(50) | Event type |
| sub_event_type | VARCHAR(50) | Sub-event type |
| created_at | TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | Record update timestamp |

### Indexes
- Unique index on `settlement_id`
- Index on `merchant_id`

## Security

The application uses HTTP Basic Authentication. Configure credentials in `application.properties`:

```properties
security.basic.auth.username=your_username
security.basic.auth.password=your_password
```

## Testing with curl

```bash
curl -X POST http://localhost:8080/api/webhook/MERCHANT123 \
  -H "Authorization: Basic Y2xudGtleV9SbkVfWGV5Y2lwVUVPd2VwRzF3UldBT1pDUXREQzdzblEwMw==" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "f5b15dac-359f-439d-8977-a0226c467dc7",
    "eventType": "Settlement",
    "subEventType": "ClosedBatch",
    "batchNumber": "469",
    "settlementDate": "2024-10-08",
    "settlementCount": 2,
    "settlementAmount": 2.6,
    "tpn": "342824380352"
  }'
```

## H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:dejavoo_db`
- Username: `sa`
- Password: (empty)

## License

Proprietary - All rights reserved
