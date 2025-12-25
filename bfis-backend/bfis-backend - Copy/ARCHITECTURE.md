# BFIS Backend Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Next.js)                      │
│                     http://localhost:3000                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP/JSON
                             │ CORS Enabled
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      BFIS BACKEND API                           │
│                     http://localhost:8080                       │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              API LAYER (bfis-api)                       │  │
│  │                                                         │  │
│  │  GET  /api/builds      → BuildController               │  │
│  │  GET  /api/failures    → FailureController             │  │
│  │  GET  /api/metrics     → MetricsController             │  │
│  │  POST /api/ui/generate → UIController                  │  │
│  │                                                         │  │
│  │  ┌────────────┐                                        │  │
│  │  │ CorsConfig │ - Allows localhost:3000                │  │
│  │  └────────────┘                                        │  │
│  └──────────────────────┬──────────────────────────────────┘  │
│                         │                                      │
│  ┌──────────────────────▼──────────────────────────────────┐  │
│  │        BUSINESS LOGIC LAYER (bfis-analyzer)            │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  BuildAnalysisService                           │  │  │
│  │  │  - computeMetrics()                             │  │  │
│  │  │  - getAllBuilds()                               │  │  │
│  │  │  - getRecentBuilds()                            │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  FailureAnalysisService                         │  │  │
│  │  │  - getAllFailures()                             │  │  │
│  │  │  - categorizeFailure()                          │  │  │
│  │  │  - generateFailureInsights()                    │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                         │                              │  │
│  └──────────────────────────┬─────────────────────────────┘  │
│                             │                                 │
│  ┌──────────────────────────▼─────────────────────────────┐  │
│  │         DATA ACCESS LAYER (bfis-analyzer)             │  │
│  │                                                        │  │
│  │  InMemoryBuildRepository    InMemoryFailureRepository │  │
│  │  ┌──────────────────┐      ┌──────────────────┐      │  │
│  │  │ ConcurrentHashMap│      │ ConcurrentHashMap│      │  │
│  │  │  <String, Build> │      │ <String, Failure>│      │  │
│  │  └──────────────────┘      └──────────────────┘      │  │
│  │                                                        │  │
│  │  (Easily replaceable with JPA/PostgreSQL)             │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │        DATA INGESTION LAYER (bfis-ingestor)            │ │
│  │                                                         │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │  DataSeederService (@PostConstruct)             │  │ │
│  │  │  - Seeds 150 realistic builds                   │  │ │
│  │  │  - Generates failure patterns                   │  │ │
│  │  │  - 8 sample repositories                        │  │ │
│  │  │                                                  │  │ │
│  │  │  Future: GitHub Actions webhook receiver        │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └─────────────────────────────────────────────────────────┘ │
│                                                                │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │           DOMAIN MODELS (bfis-core)                    │ │
│  │                                                         │ │
│  │  Build         Failure        Metrics                  │ │
│  │  BuildStatus   FailureType                             │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Module Dependencies

```
bfis-api (Spring Boot App)
    ├── depends on → bfis-analyzer
    ├── depends on → bfis-ingestor
    └── depends on → bfis-core

bfis-analyzer (Business Logic)
    └── depends on → bfis-core

bfis-ingestor (Data Seeding)
    ├── depends on → bfis-analyzer
    └── depends on → bfis-core

bfis-core (Domain Models)
    └── no dependencies (pure domain)
```

## Data Flow

### GET /api/builds Request Flow

```
Frontend
    │
    │ fetch('http://localhost:8080/api/builds')
    ▼
BuildController.getBuilds()
    │
    │ calls
    ▼
BuildAnalysisService.getAllBuilds()
    │
    │ calls
    ▼
InMemoryBuildRepository.findAll()
    │
    │ returns List<Build>
    ▼
BuildController
    │
    │ converts to JSON
    ▼
Frontend receives JSON array
```

### POST /api/ui/generate Request Flow

```
Frontend
    │
    │ POST fetch('http://localhost:8080/api/ui/generate', {...})
    ▼
UIController.generateUI()
    │
    │ calls
    ▼
FailureAnalysisService.generateFailureInsights()
    │
    │ analyzes failure patterns
    │ generates recommendations
    ▼
UIController
    │
    │ wraps in UIGenerateResponse
    │ converts to JSON
    ▼
Frontend receives {status, explanation, recommendations}
```

## Failure Categorization Algorithm

```
Error Message
    │
    ▼
FailureAnalysisService.categorizeFailure()
    │
    ├── contains "test" → FailureType.TEST
    ├── contains "dependency" → FailureType.DEPENDENCY
    ├── contains "docker" → FailureType.DOCKER
    └── default → FailureType.INFRA
    │
    ▼
Failure object with categorized type
```

## Metrics Computation Algorithm

```
All Builds
    │
    ├─→ count() → totalBuilds
    │
    ├─→ filter(status == FAILED or FLAKY)
    │   └─→ count() / totalBuilds * 100 → failureRate
    │
    ├─→ map(b → b.durationSeconds)
    │   └─→ average() → avgBuildTime
    │
    └─→ filter(status == FLAKY)
        └─→ count() → flakyTestCount
```

## Thread Safety

- Uses `ConcurrentHashMap` for in-memory storage
- Spring singleton services are thread-safe
- No shared mutable state between requests
- Stateless controllers

## Future Architecture Evolution

### Phase 1: Database Integration

```
InMemoryBuildRepository
    ↓ Replace with
JpaBuildRepository implements BuildRepository
    ↓ Uses
PostgreSQL Database
```

### Phase 2: Real-Time Ingestion

```
GitHub Actions
    │ webhook
    ▼
WebhookController
    │
    ▼
LogParsingService
    │
    ▼
BuildRepository.save()
```

### Phase 3: Caching Layer

```
Controller
    │
    ▼
Redis Cache
    │ cache miss
    ▼
Service Layer
    │
    ▼
Database
```

## Technology Stack Summary

- **Java 17**: Modern Java features
- **Spring Boot 3.2**: Latest stable version
- **Maven**: Multi-module build
- **Lombok**: Reduce boilerplate
- **Jackson**: JSON serialization
- **Actuator**: Health checks and metrics
- **Docker**: Containerization ready

## Port Configuration

- **Backend API**: 8080
- **Frontend (Next.js)**: 3000
- **Health Check**: 8080/actuator/health

## Environment Variables (Future)

```bash
# Database
DATABASE_URL=postgresql://localhost:5432/bfis
DATABASE_USERNAME=bfis
DATABASE_PASSWORD=secure_password

# GitHub Integration
GITHUB_WEBHOOK_SECRET=your_secret
GITHUB_API_TOKEN=ghp_xxx

# CORS
ALLOWED_ORIGINS=http://localhost:3000,https://your-domain.com
```

## Scalability Considerations

### Current (In-Memory)
- Single instance
- Data lost on restart
- Good for: Development, demos, prototypes

### With Database
- Multiple instances possible
- Persistent data
- Horizontal scaling with load balancer

### With Caching
- Reduced database load
- Faster response times
- Redis cluster for HA

## Security Hardening (Production)

1. **HTTPS Only**: Use SSL/TLS certificates
2. **Rate Limiting**: Prevent abuse
3. **Authentication**: JWT or OAuth2
4. **Input Validation**: Sanitize all inputs
5. **CORS**: Restrict to known domains
6. **Secrets Management**: Use env vars or vault
7. **Monitoring**: Log analysis and alerts

---

**This architecture is designed for evolution, not replacement.**
