# BFIS Backend - Build Failure Intelligence System

Production-grade Spring Boot backend for DevOps analytics dashboard.

## 🏗️ Architecture

### Multi-Module Maven Structure

```
bfis-backend/
├── bfis-core/              # Domain models and enums
├── bfis-analyzer/          # Analysis services and business logic
├── bfis-ingestor/          # Data ingestion and seeding
└── bfis-api/               # REST controllers and API layer
```

### Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Maven** (multi-module project)
- **Docker** (containerization)
- **In-memory storage** (easily replaceable with database)

---

## 📡 API Endpoints

### 1. GET /api/builds

Returns list of recent CI/CD builds.

**Response:**
```json
[
  {
    "id": "uuid",
    "repositoryName": "payment-service",
    "branch": "main",
    "status": "success",
    "durationSeconds": 245,
    "triggeredAt": "2024-01-15T10:30:00.000Z",
    "commitHash": "a3f2c1d"
  }
]
```

**Status values:** `success` | `failed` | `flaky`

---

### 2. GET /api/failures

Returns analyzed failure data with frequency patterns.

**Response:**
```json
[
  {
    "id": "uuid",
    "buildId": "build-uuid",
    "failureType": "test",
    "errorMessage": "AssertionError: Expected payment status...",
    "frequencyCount": 12,
    "firstSeenAt": "2024-01-01T08:00:00.000Z",
    "lastSeenAt": "2024-01-15T14:30:00.000Z"
  }
]
```

**Failure types:** `test` | `dependency` | `docker` | `infra`

---

### 3. GET /api/metrics

Returns aggregated metrics for dashboard cards.

**Response:**
```json
{
  "totalBuilds": 150,
  "failureRate": 25.3,
  "avgBuildTime": 245.7,
  "flakyTestCount": 8
}
```

---

### 4. POST /api/ui/generate

Triggers backend analysis and returns recommendations.

**Request (optional):**
```json
{
  "analysisType": "failures",
  "timeRange": "30d"
}
```

**Response:**
```json
{
  "status": "success",
  "explanation": "Backend analysis completed successfully...",
  "recommendations": "Analyzed 45 failure patterns. High test failure rate detected..."
}
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Docker** (optional, for containerized deployment)

### Option 1: Run with Maven

```bash
# Navigate to project root
cd bfis-backend

# Build all modules
mvn clean install

# Run the application
mvn spring-boot:run -pl bfis-api
```

The backend will start on **http://localhost:8080**

### Option 2: Run with Docker

```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build manually
docker build -t bfis-backend .
docker run -p 8080:8080 bfis-backend
```

---

## 🔗 Connecting Frontend to Backend

### Frontend Configuration

In your Next.js frontend, configure the API base URL:

```typescript
// .env.local
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### Example API Client

```typescript
// lib/api.ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

export async function fetchBuilds() {
  const response = await fetch(`${API_BASE_URL}/api/builds`);
  return response.json();
}

export async function fetchMetrics() {
  const response = await fetch(`${API_BASE_URL}/api/metrics`);
  return response.json();
}

export async function fetchFailures() {
  const response = await fetch(`${API_BASE_URL}/api/failures`);
  return response.json();
}

export async function generateUI(payload?: any) {
  const response = await fetch(`${API_BASE_URL}/api/ui/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload || {}),
  });
  return response.json();
}
```

---

## 🧪 Testing the API

### Using curl

```bash
# Get builds
curl http://localhost:8080/api/builds

# Get metrics
curl http://localhost:8080/api/metrics

# Get failures
curl http://localhost:8080/api/failures

# Generate UI recommendations
curl -X POST http://localhost:8080/api/ui/generate \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Using a REST Client

Import this collection into Postman or Thunder Client:

```json
{
  "name": "BFIS API",
  "requests": [
    {
      "name": "Get Builds",
      "method": "GET",
      "url": "http://localhost:8080/api/builds"
    },
    {
      "name": "Get Metrics",
      "method": "GET",
      "url": "http://localhost:8080/api/metrics"
    },
    {
      "name": "Get Failures",
      "method": "GET",
      "url": "http://localhost:8080/api/failures"
    },
    {
      "name": "Generate UI",
      "method": "POST",
      "url": "http://localhost:8080/api/ui/generate",
      "headers": {
        "Content-Type": "application/json"
      },
      "body": "{}"
    }
  ]
}
```

---

## 📊 Sample Data

The backend automatically seeds realistic data on startup:

- **150 builds** across 8 repositories
- **Multiple failure types** (test, dependency, docker, infra)
- **Realistic patterns** (recurring failures, flaky tests)
- **30-day time range** for historical analysis

### Seeded Repositories:
- `payment-service`
- `user-api`
- `notification-worker`
- `frontend-web`
- `analytics-pipeline`
- `auth-service`
- `inventory-service`
- `email-processor`

---

## 🔧 Configuration

### Application Properties

Located in `bfis-api/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: bfis-backend

logging:
  level:
    com.devops.bfis: DEBUG
```

### CORS Configuration

Frontend origins are configured in `CorsConfig.java`:

```java
.allowedOrigins(
    "http://localhost:3000",  // Next.js dev server
    "http://localhost:3001"
)
```

To add production origins, update this configuration.

---

## 🏛️ Architecture Decisions

### Why Multi-Module Maven?

- **Clear separation of concerns** (domain, business logic, API)
- **Reusable components** (core models shared across modules)
- **Easier testing** (test each module independently)
- **Scalability** (add modules like `bfis-notification`, `bfis-reporting`)

### Why In-Memory Storage?

Current implementation uses concurrent hash maps for simplicity:

```java
@Repository
public class InMemoryBuildRepository implements BuildRepository {
    private final Map<String, Build> buildStore = new ConcurrentHashMap<>();
    // ...
}
```

**Migration path to database:**

1. Add JPA dependencies
2. Convert repositories to extend `JpaRepository`
3. Add `@Entity` annotations to domain models
4. Configure `application.yml` with database connection

No service layer changes needed! 🎯

### Failure Categorization Logic

Rule-based classifier in `FailureAnalysisService`:

```java
public FailureType categorizeFailure(String errorMessage) {
    if (errorMessage.contains("test")) return FailureType.TEST;
    if (errorMessage.contains("dependency")) return FailureType.DEPENDENCY;
    // ...
}
```

**Future enhancement:** ML-based classification with historical pattern learning.

---

## 🔮 Future Enhancements

### Phase 1: Real Data Ingestion
- GitHub Actions webhook receiver
- Log parsing and pattern extraction
- Real-time build status updates

### Phase 2: Database Integration
- PostgreSQL for persistent storage
- Indexed queries for performance
- Historical data retention policies

### Phase 3: Advanced Analytics
- ML-based failure prediction
- Anomaly detection
- Team performance insights

### Phase 4: Notifications
- Slack/Teams integration
- Alert rules for critical failures
- Weekly summary reports

---

## 📁 Project Structure

```
bfis-backend/
│
├── pom.xml                          # Parent POM
├── Dockerfile                       # Container image definition
├── docker-compose.yml               # Local orchestration
├── README.md                        # This file
│
├── bfis-core/                       # Domain Layer
│   ├── pom.xml
│   └── src/main/java/.../core/
│       ├── domain/
│       │   ├── Build.java           # Build domain model
│       │   ├── Failure.java         # Failure domain model
│       │   └── Metrics.java         # Metrics domain model
│       └── enums/
│           ├── BuildStatus.java     # success|failed|flaky
│           └── FailureType.java     # test|dependency|docker|infra
│
├── bfis-analyzer/                   # Business Logic Layer
│   ├── pom.xml
│   └── src/main/java/.../analyzer/
│       ├── repository/
│       │   ├── BuildRepository.java
│       │   ├── FailureRepository.java
│       │   ├── InMemoryBuildRepository.java
│       │   └── InMemoryFailureRepository.java
│       └── service/
│           ├── BuildAnalysisService.java
│           └── FailureAnalysisService.java
│
├── bfis-ingestor/                   # Data Ingestion Layer
│   ├── pom.xml
│   └── src/main/java/.../ingestor/
│       └── service/
│           └── DataSeederService.java
│
└── bfis-api/                        # API Layer
    ├── pom.xml
    └── src/main/
        ├── java/.../api/
        │   ├── BfisApplication.java        # Spring Boot entry point
        │   ├── config/
        │   │   └── CorsConfig.java         # CORS configuration
        │   ├── controller/
        │   │   ├── BuildController.java    # GET /api/builds
        │   │   ├── FailureController.java  # GET /api/failures
        │   │   ├── MetricsController.java  # GET /api/metrics
        │   │   └── UIController.java       # POST /api/ui/generate
        │   └── dto/
        │       ├── UIGenerateRequest.java
        │       └── UIGenerateResponse.java
        └── resources/
            └── application.yml              # Spring configuration
```

---

## 🧪 Health Check

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Response:
{
  "status": "UP"
}
```

---

## 📝 Logging

Application logs to console with custom pattern:

```
2024-01-15 10:30:45 - GET /api/builds - Fetching build data
2024-01-15 10:30:45 - Returning 150 builds
```

Adjust log levels in `application.yml`:

```yaml
logging:
  level:
    com.devops.bfis: DEBUG  # Change to INFO in production
```

---

## 🐛 Troubleshooting

### Port 8080 already in use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### CORS errors from frontend

Verify CORS configuration includes your frontend origin:

```java
.allowedOrigins("http://localhost:3000")
```

### No data returned

Check logs for data seeding:

```
Successfully seeded 150 builds and X failures
```

If not present, restart the application.

---

## 📧 Support

For issues or questions:

1. Check this README
2. Review application logs
3. Test endpoints with curl
4. Verify CORS configuration

---

## 🎯 Success Criteria

✅ Backend starts without errors  
✅ All 4 API endpoints respond correctly  
✅ Frontend can connect and fetch data  
✅ Realistic DevOps data is displayed  
✅ No frontend modifications required  

---

**Built with ☕ by a senior engineer who cares about production quality.**
