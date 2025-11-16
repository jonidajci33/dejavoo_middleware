# Dejavoo Middleware - Settlement Webhook Service

A Spring Boot application that receives and processes settlement webhooks from Dejavoo payment terminals.

## Features

- REST API endpoint for receiving settlement webhooks
- **HMAC-SHA512 signature verification** for webhook authentication and integrity
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

The application creates the following database tables:

### Settlements Table

The `settlements` table stores settlement records:

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

**Indexes:**
- Unique index on `settlement_id`
- Index on `merchant_id`

### Merchant Secrets Table

See **Security** section below for the `merchant_secrets` table schema (stores HMAC secret keys).

## Security

The application uses **HMAC-SHA512 signature verification** as the sole authentication mechanism. Each webhook request must include a valid HMAC signature that is verified against merchant-specific secret keys stored in the database.

**No Basic Authentication or other authentication is required** - the HMAC signature provides both authentication and integrity verification.

## HMAC Signature Verification

The webhook service implements **HMAC-SHA512 signature verification** to ensure webhook integrity and authenticity. Each webhook request must include a valid HMAC signature in the `signature` field.

### How HMAC Signature Works

1. **Sender (Dejavoo)** generates HMAC-SHA512 signature using:
   - Webhook payload fields in sorted order (TreeMap)
   - Pipe delimiter (`|`) between field values
   - Merchant-specific secret key

2. **Receiver (This service)** verifies signature by:
   - Retrieving merchant's secret key from database
   - Regenerating signature using same algorithm
   - Comparing computed signature with received signature

### HMAC Algorithm

The signature is computed from the following fields in **sorted alphabetical order**:

```
batchNumber | createdDt | eventType | id | requestType | settlementAmount |
settlementCount | settlementDate | settlementTxnDetails | subEventType | tpn | version
```

For `settlementTxnDetails`, each transaction is concatenated with pipe delimiters:
```
txnDate|transactionType|transactionId|txnAmount|baseAmount
```

### Database Schema for Merchant Secrets

| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key (auto-increment) |
| merchant_id | VARCHAR(100) | Unique merchant identifier |
| hmac_secret_key | VARCHAR(500) | Secret key for HMAC-SHA512 |
| active | BOOLEAN | Secret key active status (default: true) |
| created_at | TIMESTAMP | Record creation timestamp |
| updated_at | TIMESTAMP | Record update timestamp |

### Setting Up Merchant Secret Keys

Merchant secret keys must be configured in the `merchant_secrets` table by your external application:

```sql
-- Example: Set HMAC secret key for a merchant
INSERT INTO merchant_secrets (merchant_id, hmac_secret_key, active, created_at, updated_at)
VALUES (
    'MERCHANT123',
    'your-secret-key-here',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

### Request Example with Signature

```json
{
  "id": "46f69dec-fce4-444c-ba7b-64a11b9357a3",
  "eventType": "Settlement",
  "subEventType": "ClosedBatch",
  "requestType": "HTTPS",
  "signature": "464ebd627992eb451b8d8a4075d6869c685e6ce7552535318a607f7ee68cccde0856fc78ae9e84f88778c4f935b4953cc61764e3f4152032d1c0e12dd5280ff0",
  "version": "1.0",
  "versionCreatedDt": "2024-06-04 08:53:39",
  "batchNumber": "470",
  "settlementDate": "2024-10-08",
  "settlementCount": 2,
  "settlementAmount": 3.9,
  "tpn": "342824380352",
  "settlementTxnDetails": [...]
}
```

### Signature Verification Process

1. Request arrives with `signature` field
2. Service retrieves merchant's HMAC secret key from database
3. Service computes HMAC-SHA512 using request payload fields
4. Computed signature is compared with provided signature
5. **If signatures match**: Request is processed
6. **If signatures don't match**: Returns `400 Bad Request` with "Invalid signature" error

**Important**: Without a valid signature, the webhook will be rejected. HMAC signature verification is the only authentication mechanism.

## Testing with curl

```bash
curl -X POST http://localhost:8080/api/webhook/MERCHANT123 \
  -H "Content-Type: application/json" \
  -d '{
    "id": "46f69dec-fce4-444c-ba7b-64a11b9357a3",
    "signature": "464ebd627992eb451b8d8a4075d6869c685e6ce7552535318a607f7ee68cccde0856fc78ae9e84f88778c4f935b4953cc61764e3f4152032d1c0e12dd5280ff0",
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
