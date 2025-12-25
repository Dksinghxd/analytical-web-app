"use client";

import { FilterPanel } from "@/components/filter-panel"
import { FailureTable } from "@/components/failure-table"
import { RootCauseAnalysis } from "@/components/root-cause-analysis"
import { FailureImpactChart } from "@/components/failure-impact-chart"
import { FailureSearch } from "@/components/failure-search"
import { useEffect, useState } from "react"

const API_URL = process.env.NEXT_PUBLIC_API_URL

export default function FailuresPage() {
  const [failures, setFailures] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function fetchFailures() {
      setLoading(true)
      try {
        const res = await fetch(`${API_URL}/api/failures`)
        const data = await res.json()
        setFailures(data)
      } catch (e) {
        // fallback or error handling
      }
      setLoading(false)
    }
    fetchFailures()
  }, [])

  if (loading) {
    return <div className="p-6">Loading...</div>
  }

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-balance">Failure Intelligence</h1>
        <p className="text-muted-foreground mt-1">Deep dive into build failures with AI-powered root cause analysis</p>
      </div>

      <FilterPanel />

      <div className="flex gap-4">
        <FailureSearch />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <RootCauseAnalysis />
        </div>
        <FailureImpactChart />
      </div>

      <FailureTable failures={failures} />
    </div>
  )
}
