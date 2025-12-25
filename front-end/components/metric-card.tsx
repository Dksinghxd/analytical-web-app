import { Card, CardContent } from "@/components/ui/card"
import type { LucideIcon } from "lucide-react"
import { cn } from "@/lib/utils"

interface MetricCardProps {
  title: string
  value: string | number
  subtitle?: string
  icon: LucideIcon
  trend?: {
    value: number
    isPositive: boolean
  }
  iconClassName?: string
}

export function MetricCard({ title, value, subtitle, icon: Icon, trend, iconClassName }: MetricCardProps) {
  return (
    <Card className="relative overflow-hidden border-border bg-card">
      <CardContent className="p-6">
        <div className="flex items-start justify-between">
          <div className="space-y-2">
            <p className="text-sm font-medium text-muted-foreground">{title}</p>
            <div className="flex items-baseline gap-2">
              <p className="text-3xl font-bold text-foreground">{value}</p>
              {subtitle && <span className="text-sm text-muted-foreground">{subtitle}</span>}
            </div>
            {trend && (
              <p className={cn("text-xs font-medium", trend.isPositive ? "text-success" : "text-destructive")}>
                {trend.isPositive ? "+" : ""}
                {trend.value}% from last week
              </p>
            )}
          </div>
          <div className={cn("rounded-lg p-3", iconClassName)}>
            <Icon className="h-5 w-5" />
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
