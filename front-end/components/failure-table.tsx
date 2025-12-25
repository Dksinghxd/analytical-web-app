
import React from "react"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { ChevronDown, ChevronRight, AlertTriangle } from "lucide-react"
import type { Failure } from "@/lib/types"
import { cn } from "@/lib/utils"

interface FailureTableProps {
  failures: Failure[]
}

const failureTypeColors = {
  test: "bg-chart-1/10 text-chart-1 border-chart-1/20",
  dependency: "bg-chart-2/10 text-chart-2 border-chart-2/20",
  docker: "bg-chart-3/10 text-chart-3 border-chart-3/20",
  infra: "bg-chart-5/10 text-chart-5 border-chart-5/20",
}

export function FailureTable({ failures }: FailureTableProps) {

  const [expandedRows, setExpandedRows] = useState<Set<string>>(new Set());

  const toggleRow = (id: string) => {
    setExpandedRows((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(id)) {
        newSet.delete(id);
      } else {
        newSet.add(id);
      }
      return newSet;
    });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Failure Analytics</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr>
                <th className="p-4 text-left"> </th>
                <th className="p-4 text-left">Type</th>
                <th className="p-4 text-left">Error</th>
                <th className="p-4 text-left">Occurrences</th>
                <th className="p-4 text-left">Last Seen</th>
              </tr>
            </thead>
            <tbody>
              {failures.map((failure) => {
                const isExpanded = expandedRows.has(failure.id);
                return (
                  <React.Fragment key={failure.id}>
                    <tr
                      className="border-b border-border hover:bg-accent/50 transition-colors cursor-pointer"
                      onClick={() => toggleRow(failure.id)}
                    >
                      <td className="p-4">
                        <Button variant="ghost" size="icon" className="h-6 w-6">
                          {isExpanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                        </Button>
                      </td>
                      <td className="p-4">
                        <Badge variant="outline" className={cn("capitalize", failureTypeColors[failure.failureType])}>
                          {failure.failureType}
                        </Badge>
                      </td>
                      <td className="p-4">
                        <div className="flex items-start gap-2 max-w-md">
                          <AlertTriangle className="h-4 w-4 text-destructive mt-0.5 shrink-0" />
                          <span className="text-sm text-foreground line-clamp-2">{failure.errorMessage}</span>
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-medium text-foreground">{failure.frequencyCount}</span>
                          <span className="text-xs text-muted-foreground">occurrences</span>
                        </div>
                      </td>
                      <td className="p-4">
                        <span className="text-sm text-muted-foreground">
                          {new Date(failure.lastSeenAt).toLocaleDateString("en-US", {
                            month: "short",
                            day: "numeric",
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </span>
                      </td>
                    </tr>
                    {isExpanded && (
                      <tr className="bg-muted/30">
                        <td colSpan={5} className="p-6">
                          <div className="space-y-4">
                            <div>
                              <h4 className="text-sm font-semibold text-foreground mb-2">Full Error Message</h4>
                              <pre className="bg-background border border-border rounded-lg p-4 text-xs text-foreground overflow-x-auto font-mono">
                                {failure.errorMessage}
                              </pre>
                            </div>
                            <div className="grid grid-cols-3 gap-4">
                              <div>
                                <p className="text-xs text-muted-foreground mb-1">First Seen</p>
                                <p className="text-sm font-medium text-foreground">
                                  {new Date(failure.firstSeenAt).toLocaleDateString("en-US", {
                                    month: "long",
                                    day: "numeric",
                                    year: "numeric",
                                    hour: "2-digit",
                                    minute: "2-digit",
                                  })}
                                </p>
                              </div>
                              <div>
                                <p className="text-xs text-muted-foreground mb-1">Build ID</p>
                                <p className="text-sm font-medium text-foreground font-mono">{failure.buildId}</p>
                              </div>
                              <div>
                                <p className="text-xs text-muted-foreground mb-1">Total Occurrences</p>
                                <p className="text-sm font-medium text-foreground">{failure.frequencyCount}</p>
                              </div>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
}
