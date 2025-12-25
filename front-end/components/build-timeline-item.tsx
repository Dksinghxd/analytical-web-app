import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Clock, GitBranch, GitCommit, ExternalLink } from "lucide-react"
import type { Build } from "@/lib/types"
import { cn } from "@/lib/utils"

interface BuildTimelineItemProps {
  build: Build
}

const statusConfig = {
  success: {
    badge: "bg-success/10 text-success border-success/20",
    dot: "bg-success",
    label: "Success",
  },
  failed: {
    badge: "bg-destructive/10 text-destructive border-destructive/20",
    dot: "bg-destructive",
    label: "Failed",
  },
  flaky: {
    badge: "bg-warning/10 text-warning border-warning/20",
    dot: "bg-warning",
    label: "Flaky",
  },
}

export function BuildTimelineItem({ build }: BuildTimelineItemProps) {
  const config = statusConfig[build.status];
  if (!config) {
    // If build.status is invalid, skip rendering this item
    return null;
  }

  return (
    <div className="relative pl-8 pb-8 group">
      {/* Timeline line */}
      <div className="absolute left-2 top-0 bottom-0 w-px bg-border group-last:hidden" />

      {/* Timeline dot */}
      <div className={cn("absolute left-0 top-2 h-5 w-5 rounded-full border-4 border-background", config.dot)} />

      <Card className="border-border bg-card hover:bg-accent/30 transition-colors">
        <CardContent className="p-4">
          <div className="flex items-start justify-between gap-4">
            <div className="flex-1 space-y-3">
              <div className="flex items-center gap-3 flex-wrap">
                <h3 className="text-base font-semibold text-foreground">{build.repositoryName}</h3>
                <Badge variant="outline" className={config.badge}>
                  {config.label}
                </Badge>
                <div className="flex items-center gap-1.5 text-sm text-muted-foreground">
                  <Clock className="h-3.5 w-3.5" />
                  <span>{build.durationSeconds}s</span>
                </div>
              </div>

              <div className="flex items-center gap-4 text-sm">
                <div className="flex items-center gap-1.5 text-muted-foreground">
                  <GitBranch className="h-3.5 w-3.5" />
                  <span className="font-mono">{build.branch}</span>
                </div>
                <div className="flex items-center gap-1.5 text-muted-foreground">
                  <GitCommit className="h-3.5 w-3.5" />
                  <span className="font-mono">{build.commitHash}</span>
                </div>
              </div>

              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <span>Triggered at</span>
                <span className="font-medium">
                  {new Date(build.triggeredAt).toLocaleString("en-US", {
                    month: "short",
                    day: "numeric",
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </span>
              </div>
            </div>

            <div className="flex gap-2">
              <Button variant="outline" size="sm" className="gap-2 bg-transparent">
                <ExternalLink className="h-3.5 w-3.5" />
                View Logs
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
