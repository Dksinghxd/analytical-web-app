"use client"

import { Activity, AlertCircle, Clock, Zap } from "lucide-react"
import { MetricCard } from "@/components/metric-card"
import { BuildDurationChart } from "@/components/build-duration-chart"
import { FailureTypesChart } from "@/components/failure-types-chart"
import { FailureHeatmap } from "@/components/failure-heatmap"
import { FilterPanel } from "@/components/filter-panel"
import { TrackedReposPanel } from "@/components/tracked-repos-panel"
import { Button } from "@/components/ui/button"
import { useEffect, useState } from "react"

const API_URL = process.env.NEXT_PUBLIC_API_URL

export default function OverviewPage() {
  const [metrics, setMetrics] = useState(null)
  const [buildTrends, setBuildTrends] = useState([])
  const [failuresByType, setFailuresByType] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function fetchData() {
      setLoading(true)
      try {
        const [metricsRes, buildsRes, failuresRes] = await Promise.all([
          fetch(`${API_URL}/api/metrics`),
          fetch(`${API_URL}/api/builds`),
          fetch(`${API_URL}/api/failures`),
        ])
        const metricsData = await metricsRes.json()
        const buildsData = await buildsRes.json()
        const failuresData = await failuresRes.json()

        setMetrics(metricsData)
        // Build trends: group by date and average duration
        const trends = buildsData.reduce((acc, build) => {
          const date = build.triggeredAt.split("T")[0]
          if (!acc[date]) acc[date] = { date, duration: 0, count: 0, status: build.status }
          acc[date].duration += build.durationSeconds
          acc[date].count += 1
          return acc
        }, {})
        setBuildTrends(Object.values(trends).map((t: any) => ({
          date: t.date,
          duration: Math.round(t.duration / t.count),
          status: t.status,
        })))

        // Failures by type
        const byType = failuresData.reduce((acc, f) => {
          acc[f.failureType] = (acc[f.failureType] || 0) + 1
          return acc
        }, {})
        setFailuresByType(byType)
      } catch (e) {
        // fallback or error handling
      }
      setLoading(false)
    }
    fetchData()
  }, [])

  if (loading || !metrics || !failuresByType) {
    return <div className="p-6">Loading...</div>
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-balance">CI/CD Health Overview</h1>
          <p className="text-muted-foreground mt-1">
            Monitor build performance and failure patterns across your infrastructure
          </p>
        </div>
        <Button className="gap-2 bg-primary text-primary-foreground hover:bg-primary/90">
          <Zap className="h-4 w-4" />
          Generate Optimal UI
        </Button>
      </div>

      <FilterPanel />

      <TrackedReposPanel />

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          title="Total Builds"
          value={metrics.totalBuilds?.toLocaleString()}
          subtitle="this month"
          icon={Activity}
          trend={{ value: 12.5, isPositive: true }}
          iconClassName="bg-primary/10 text-primary"
        />
        <MetricCard
          title="Failure Rate"
          value={metrics.failureRate}
          subtitle="%"
          icon={AlertCircle}
          trend={{ value: 3.2, isPositive: false }}
          iconClassName="bg-destructive/10 text-destructive"
        />
        <MetricCard
          title="Avg Build Time"
          value={metrics.avgBuildTime}
          subtitle="seconds"
          icon={Clock}
          trend={{ value: 8.1, isPositive: false }}
          iconClassName="bg-warning/10 text-warning"
        />
        <MetricCard
          title="Flaky Tests"
          value={metrics.flakyTestCount}
          subtitle="detected"
          icon={Zap}
          iconClassName="bg-chart-2/10 text-chart-2"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <BuildDurationChart data={buildTrends} />
        <FailureTypesChart data={failuresByType} />
      </div>

      <FailureHeatmap />
    </div>
  )
}
