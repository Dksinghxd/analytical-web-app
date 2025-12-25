import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Lightbulb, BarChart3, Layout, Zap } from "lucide-react"

const insights = [
  {
    icon: BarChart3,
    title: "Prioritized Metrics",
    description:
      "Failure rate and flaky tests are elevated to top position based on your recent spike in test failures",
  },
  {
    icon: Layout,
    title: "Optimized Layout",
    description: "Build duration chart is enlarged to help track the 8% increase in build times over the past week",
  },
  {
    icon: Zap,
    title: "Smart Grouping",
    description: "Related failures are grouped by root cause to reduce cognitive load and speed up debugging",
  },
  {
    icon: Lightbulb,
    title: "Contextual Insights",
    description: "AI-generated recommendations are positioned near relevant metrics to guide immediate action items",
  },
]

export function UIExplanationPanel() {
  return (
    <Card className="border-border bg-card">
      <CardHeader>
        <CardTitle className="text-base font-semibold">Why This Layout?</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-sm text-muted-foreground">
          Our AI engine analyzed your CI/CD patterns and selected these UI elements to maximize visibility of critical
          issues:
        </p>

        <div className="space-y-3">
          {insights.map((insight, index) => {
            const Icon = insight.icon
            return (
              <div key={index} className="flex items-start gap-3 p-3 rounded-lg bg-muted/30">
                <div className="p-2 rounded-lg bg-primary/10">
                  <Icon className="h-4 w-4 text-primary" />
                </div>
                <div className="flex-1 space-y-1">
                  <h4 className="text-sm font-medium text-foreground">{insight.title}</h4>
                  <p className="text-xs text-muted-foreground leading-relaxed">{insight.description}</p>
                </div>
              </div>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
