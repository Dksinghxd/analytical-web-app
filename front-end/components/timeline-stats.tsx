import { Card, CardContent } from "@/components/ui/card"
import { CheckCircle2, XCircle, AlertTriangle, TrendingUp } from "lucide-react"

interface TimelineStatsProps {
  successCount: number
  failedCount: number
  flakyCount: number
  successRate: number
}

export function TimelineStats({ successCount, failedCount, flakyCount, successRate }: TimelineStatsProps) {
  const stats = [
    {
      icon: CheckCircle2,
      label: "Successful",
      value: successCount,
      color: "text-success",
      bgColor: "bg-success/10",
    },
    {
      icon: XCircle,
      label: "Failed",
      value: failedCount,
      color: "text-destructive",
      bgColor: "bg-destructive/10",
    },
    {
      icon: AlertTriangle,
      label: "Flaky",
      value: flakyCount,
      color: "text-warning",
      bgColor: "bg-warning/10",
    },
    {
      icon: TrendingUp,
      label: "Success Rate",
      value: `${successRate}%`,
      color: "text-primary",
      bgColor: "bg-primary/10",
    },
  ]

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      {stats.map((stat) => {
        const Icon = stat.icon
        return (
          <Card key={stat.label} className="border-border bg-card">
            <CardContent className="p-4">
              <div className="flex items-center gap-3">
                <div className={`p-2 rounded-lg ${stat.bgColor}`}>
                  <Icon className={`h-5 w-5 ${stat.color}`} />
                </div>
                <div>
                  <p className="text-2xl font-bold text-foreground">{stat.value}</p>
                  <p className="text-xs text-muted-foreground">{stat.label}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}
