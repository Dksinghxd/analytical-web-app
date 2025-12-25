# Frontend Integration Guide

## Quick Start

### 1. Start the Backend

```bash
cd bfis-backend
./start.sh
```

Backend will be available at: `http://localhost:8080`

### 2. Configure Your Frontend

In your Next.js project, create or update `.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### 3. Create API Client

Create `lib/api.ts` in your frontend project:

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

interface Build {
  id: string;
  repositoryName: string;
  branch: string;
  status: 'success' | 'failed' | 'flaky';
  durationSeconds: number;
  triggeredAt: string;
  commitHash: string;
}

interface Failure {
  id: string;
  buildId: string;
  failureType: 'test' | 'dependency' | 'docker' | 'infra';
  errorMessage: string;
  frequencyCount: number;
  firstSeenAt: string;
  lastSeenAt: string;
}

interface Metrics {
  totalBuilds: number;
  failureRate: number;
  avgBuildTime: number;
  flakyTestCount: number;
}

interface UIGenerateResponse {
  status: string;
  explanation: string;
  recommendations?: string;
}

export async function fetchBuilds(): Promise<Build[]> {
  const response = await fetch(`${API_BASE_URL}/api/builds`);
  if (!response.ok) {
    throw new Error('Failed to fetch builds');
  }
  return response.json();
}

export async function fetchFailures(): Promise<Failure[]> {
  const response = await fetch(`${API_BASE_URL}/api/failures`);
  if (!response.ok) {
    throw new Error('Failed to fetch failures');
  }
  return response.json();
}

export async function fetchMetrics(): Promise<Metrics> {
  const response = await fetch(`${API_BASE_URL}/api/metrics`);
  if (!response.ok) {
    throw new Error('Failed to fetch metrics');
  }
  return response.json();
}

export async function generateUI(payload?: any): Promise<UIGenerateResponse> {
  const response = await fetch(`${API_BASE_URL}/api/ui/generate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload || {}),
  });
  
  if (!response.ok) {
    throw new Error('Failed to generate UI');
  }
  
  return response.json();
}
```

### 4. Use in Your Components

#### Server Component Example (Next.js 14 App Router)

```typescript
// app/dashboard/page.tsx
import { fetchBuilds, fetchMetrics } from '@/lib/api';

export default async function DashboardPage() {
  const [builds, metrics] = await Promise.all([
    fetchBuilds(),
    fetchMetrics(),
  ]);

  return (
    <div>
      <h1>BFIS Dashboard</h1>
      <div className="metrics">
        <div>Total Builds: {metrics.totalBuilds}</div>
        <div>Failure Rate: {metrics.failureRate}%</div>
        <div>Avg Build Time: {metrics.avgBuildTime}s</div>
        <div>Flaky Tests: {metrics.flakyTestCount}</div>
      </div>
      
      <div className="builds">
        {builds.map(build => (
          <div key={build.id}>
            <span>{build.repositoryName}</span>
            <span className={`status-${build.status}`}>
              {build.status}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
```

#### Client Component Example (with React Query)

```typescript
'use client';

import { useQuery } from '@tanstack/react-query';
import { fetchBuilds, fetchMetrics, fetchFailures } from '@/lib/api';

export function DashboardClient() {
  const { data: builds, isLoading: buildsLoading } = useQuery({
    queryKey: ['builds'],
    queryFn: fetchBuilds,
  });

  const { data: metrics, isLoading: metricsLoading } = useQuery({
    queryKey: ['metrics'],
    queryFn: fetchMetrics,
  });

  const { data: failures, isLoading: failuresLoading } = useQuery({
    queryKey: ['failures'],
    queryFn: fetchFailures,
  });

  if (buildsLoading || metricsLoading || failuresLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      {/* Render your dashboard */}
    </div>
  );
}
```

---

## API Contract Details

### Build Object

```typescript
interface Build {
  id: string;                    // UUID
  repositoryName: string;        // e.g., "payment-service"
  branch: string;                // e.g., "main", "develop"
  status: BuildStatus;           // "success" | "failed" | "flaky"
  durationSeconds: number;       // Build duration in seconds
  triggeredAt: string;           // ISO 8601 timestamp
  commitHash: string;            // Short git commit hash
}
```

### Failure Object

```typescript
interface Failure {
  id: string;                    // UUID
  buildId: string;               // Associated build UUID
  failureType: FailureType;      // "test" | "dependency" | "docker" | "infra"
  errorMessage: string;          // Error message from logs
  frequencyCount: number;        // How many times this occurred
  firstSeenAt: string;           // ISO 8601 timestamp
  lastSeenAt: string;            // ISO 8601 timestamp
}
```

### Metrics Object

```typescript
interface Metrics {
  totalBuilds: number;           // Total build count
  failureRate: number;           // Percentage (0-100)
  avgBuildTime: number;          // Seconds (decimal)
  flakyTestCount: number;        // Count of flaky tests
}
```

---

## Testing Your Integration

### 1. Verify Backend is Running

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 2. Test Each Endpoint

```bash
# Get builds
curl http://localhost:8080/api/builds

# Get metrics
curl http://localhost:8080/api/metrics

# Get failures
curl http://localhost:8080/api/failures

# Generate UI
curl -X POST http://localhost:8080/api/ui/generate \
  -H "Content-Type: application/json" \
  -d '{}'
```

### 3. Check CORS

If you get CORS errors in the browser console:

1. Verify your frontend URL is in `CorsConfig.java`
2. Check that you're using the correct port (3000 by default)
3. Restart the backend after changing CORS config

---

## Common Issues

### Issue: "Failed to fetch"

**Cause:** Backend not running or wrong URL

**Solution:**
```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# Verify NEXT_PUBLIC_API_URL in .env.local
echo $NEXT_PUBLIC_API_URL
```

### Issue: CORS Error

**Cause:** Frontend origin not allowed

**Solution:** Add your frontend URL to `bfis-api/src/main/java/com/devops/bfis/api/config/CorsConfig.java`:

```java
.allowedOrigins(
    "http://localhost:3000",
    "http://your-frontend-url:port"
)
```

### Issue: Empty Data

**Cause:** Data not seeded

**Solution:** Check backend logs for:
```
Successfully seeded 150 builds and X failures
```

If not present, restart the backend.

---

## Data Refresh

The backend seeds data on startup. To refresh data:

1. Stop the backend (Ctrl+C)
2. Restart: `./start.sh`
3. Data will be regenerated with new timestamps

---

## Production Considerations

### Environment Variables

```bash
# .env.production
NEXT_PUBLIC_API_URL=https://your-api-domain.com
```

### Backend Configuration

Update `application.yml` for production:

```yaml
server:
  port: ${PORT:8080}

spring:
  profiles:
    active: production

logging:
  level:
    com.devops.bfis: INFO  # Change from DEBUG
```

### CORS Production Settings

```java
.allowedOrigins(
    "https://your-frontend-domain.com",
    "https://www.your-frontend-domain.com"
)
```

---

## Need Help?

1. Check backend logs for errors
2. Verify API endpoints with curl
3. Check browser console for CORS errors
4. Ensure environment variables are set correctly

---

**Your frontend should now connect seamlessly to the backend! 🚀**
