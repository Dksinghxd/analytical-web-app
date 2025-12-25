import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { AlertCircle, TrendingUp, Layers } from "lucide-react"

export function RootCauseAnalysis() {
  const insights = [
    {
      icon: AlertCircle,
      title: "Test Timeout Pattern Detected",
      description: "23 test failures are caused by timeout issues in the AuthService test suite",
      severity: "high",
    },
    {
      icon: TrendingUp,
      title: "Increasing Dependency Conflicts",
      description: "npm dependency resolution failures have increased by 40% this week",
      severity: "medium",
    },
    {
      icon: Layers,
      title: "Docker Build Cache Issues",
      description: "Inefficient layer caching is causing 15% longer build times",
      severity: "low",
    },
  ]

  const severityColors = {
    high: "text-destructive",
    medium: "text-warning",
    low: "text-chart-2",
  }

  return (
    <Card className="border-border bg-card">
      <CardHeader>
        <CardTitle className="text-base font-semibold">Root Cause Analysis</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {insights.map((insight, index) => {
          const Icon = insight.icon
          return (
            <div
              key={index}
              className="flex items-start gap-4 p-4 rounded-lg border border-border bg-muted/30 hover:bg-muted/50 transition-colors"
            >
              <div className={`p-2 rounded-lg ${severityColors[insight.severity as keyof typeof severityColors]}`}>
                <Icon className="h-5 w-5" />
              </div>
              <div className="flex-1 space-y-1">
                <h4 className="text-sm font-semibold text-foreground">{insight.title}</h4>
                <p className="text-sm text-muted-foreground">{insight.description}</p>
              </div>
            </div>
          )
        })}
      </CardContent>
    </Card>
  )
}
