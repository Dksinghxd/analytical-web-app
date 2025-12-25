import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { MetricCard } from "@/components/metric-card"
import { AlertCircle, Zap, TrendingUp } from "lucide-react"

export function GeneratedPreview() {
  return (
    <Card className="border-2 border-primary/30 bg-card">
      <CardHeader className="border-b border-border">
        <div className="flex items-center justify-between">
          <CardTitle className="text-base font-semibold">Generated Dashboard Preview</CardTitle>
          <div className="px-2 py-1 rounded-full bg-primary/10 text-primary text-xs font-medium">AI-Optimized</div>
        </div>
      </CardHeader>
      <CardContent className="p-6">
        <div className="space-y-4 opacity-90">
          <div className="grid gap-4 md:grid-cols-3">
            <MetricCard
              title="Failure Rate"
              value={12.8}
              subtitle="%"
              icon={AlertCircle}
              trend={{ value: 3.2, isPositive: false }}
              iconClassName="bg-destructive/10 text-destructive"
            />
            <MetricCard
              title="Flaky Tests"
              value={23}
              subtitle="detected"
              icon={Zap}
              iconClassName="bg-warning/10 text-warning"
            />
            <MetricCard
              title="Resolution Time"
              value={45}
              subtitle="minutes"
              icon={TrendingUp}
              iconClassName="bg-success/10 text-success"
            />
          </div>

          <div className="h-48 rounded-lg border border-border bg-muted/20 flex items-center justify-center">
            <div className="text-center space-y-2">
              <div className="h-12 w-12 mx-auto rounded-lg bg-primary/10 flex items-center justify-center">
                <TrendingUp className="h-6 w-6 text-primary" />
              </div>
              <p className="text-sm text-muted-foreground">Enlarged Build Duration Chart</p>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="h-32 rounded-lg border border-border bg-muted/20 flex items-center justify-center">
              <p className="text-xs text-muted-foreground">Grouped Failures by Root Cause</p>
            </div>
            <div className="h-32 rounded-lg border border-border bg-muted/20 flex items-center justify-center">
              <p className="text-xs text-muted-foreground">AI-Generated Recommendations</p>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
