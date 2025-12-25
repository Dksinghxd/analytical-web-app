import type { Build, Failure, Metrics, FailuresByType, BuildTrend } from "./types"

export const mockMetrics: Metrics = {
  totalBuilds: 1247,
  failureRate: 12.8,
  avgBuildTime: 342,
  flakyTestCount: 23,
}

export const mockBuilds: Build[] = [
  {
    id: "1",
    repositoryName: "frontend-app",
    branch: "main",
    status: "success",
    durationSeconds: 324,
    triggeredAt: "2025-12-25T10:30:00Z",
    commitHash: "a1b2c3d",
  },
  {
    id: "2",
    repositoryName: "backend-api",
    branch: "develop",
    status: "failed",
    durationSeconds: 456,
    triggeredAt: "2025-12-25T09:15:00Z",
    commitHash: "e4f5g6h",
  },
  {
    id: "3",
    repositoryName: "mobile-app",
    branch: "main",
    status: "flaky",
    durationSeconds: 612,
    triggeredAt: "2025-12-25T08:45:00Z",
    commitHash: "i7j8k9l",
  },
  {
    id: "4",
    repositoryName: "frontend-app",
    branch: "feature/auth",
    status: "success",
    durationSeconds: 298,
    triggeredAt: "2025-12-25T07:20:00Z",
    commitHash: "m0n1o2p",
  },
  {
    id: "5",
    repositoryName: "data-pipeline",
    branch: "main",
    status: "failed",
    durationSeconds: 523,
    triggeredAt: "2025-12-25T06:10:00Z",
    commitHash: "q3r4s5t",
  },
]

export const mockFailures: Failure[] = [
  {
    id: "1",
    buildId: "2",
    failureType: "test",
    errorMessage: 'TypeError: Cannot read property "length" of undefined at AuthService.test.ts:45',
    frequencyCount: 12,
    firstSeenAt: "2025-12-20T10:00:00Z",
    lastSeenAt: "2025-12-25T09:15:00Z",
  },
  {
    id: "2",
    buildId: "5",
    failureType: "dependency",
    errorMessage: "npm ERR! Failed to resolve dependency tree for @types/node@18.0.0",
    frequencyCount: 5,
    firstSeenAt: "2025-12-23T14:30:00Z",
    lastSeenAt: "2025-12-25T06:10:00Z",
  },
  {
    id: "3",
    buildId: "3",
    failureType: "test",
    errorMessage: "Test timeout exceeded: jest.setTimeout 30000ms",
    frequencyCount: 23,
    firstSeenAt: "2025-12-18T08:00:00Z",
    lastSeenAt: "2025-12-25T08:45:00Z",
  },
]

export const mockFailuresByType: FailuresByType = {
  test: 145,
  dependency: 32,
  docker: 18,
  infra: 12,
}

export const mockBuildTrends: BuildTrend[] = [
  { date: "2025-12-19", duration: 380, status: "success" },
  { date: "2025-12-20", duration: 420, status: "failed" },
  { date: "2025-12-21", duration: 310, status: "success" },
  { date: "2025-12-22", duration: 450, status: "success" },
  { date: "2025-12-23", duration: 290, status: "success" },
  { date: "2025-12-24", duration: 520, status: "failed" },
  { date: "2025-12-25", duration: 342, status: "success" },
]
