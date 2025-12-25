"use client"

import { BuildTimelineItem } from "@/components/build-timeline-item"
import { TimelineStats } from "@/components/timeline-stats"
import { FilterPanel } from "@/components/filter-panel"
import { useEffect, useState } from "react"

const API_URL = process.env.NEXT_PUBLIC_API_URL

export default function TimelinePage() {
  const [builds, setBuilds] = useState<import("@/lib/types").Build[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function fetchBuilds() {
      setLoading(true)
      try {
        const res = await fetch(`${API_URL}/api/builds`)
        const data = await res.json()
        // fallback to mockBuilds if data is not an array or is empty
        if (!Array.isArray(data) || data.length === 0 || !data[0]?.status) {
          const { mockBuilds } = await import("@/lib/mock-data")
          setBuilds(mockBuilds)
        } else {
          setBuilds(data)
        }
      } catch (e) {
        // fallback to mockBuilds on error
        import("@/lib/mock-data").then(({ mockBuilds }) => setBuilds(mockBuilds))
      }
      setLoading(false)
    }
    fetchBuilds()
  }, [])

  if (loading) {
    return <div className="p-6">Loading...</div>
  }

  const successCount = builds.filter((b) => b.status === "success").length
  const failedCount = builds.filter((b) => b.status === "failed").length
  const flakyCount = builds.filter((b) => b.status === "flaky").length
  const successRate = builds.length > 0 ? Math.round((successCount / builds.length) * 100) : 0

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-balance">Build Timeline</h1>
        <p className="text-muted-foreground mt-1">Chronological view of recent builds with status and metadata</p>
      </div>

      <TimelineStats
        successCount={successCount}
        failedCount={failedCount}
        flakyCount={flakyCount}
        successRate={successRate}
      />

      <FilterPanel />

      <div className="mt-8">
        {builds.map((build) => (
          <BuildTimelineItem key={build.id} build={build} />
        ))}
      </div>
    </div>
  )
}
