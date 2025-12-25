export interface Build {
  id: string
  repositoryName: string
  branch: string
  status: "success" | "failed" | "flaky"
  durationSeconds: number
  triggeredAt: string
  commitHash: string
}

export interface Failure {
  id: string
  buildId: string
  failureType: "test" | "dependency" | "docker" | "infra"
  errorMessage: string
  frequencyCount: number
  firstSeenAt: string
  lastSeenAt: string
}

export interface Metrics {
  totalBuilds: number
  failureRate: number
  avgBuildTime: number
  flakyTestCount: number
}

export interface FailuresByType {
  test: number
  dependency: number
  docker: number
  infra: number
}

export interface BuildTrend {
  date: string
  duration: number
  status: "success" | "failed" | "flaky"
}
