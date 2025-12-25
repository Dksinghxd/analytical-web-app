"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts"
import type { FailuresByType } from "@/lib/types"

interface FailureTypesChartProps {
  data: FailuresByType
}

const COLORS = {
  test: "oklch(0.58 0.22 264)",
  dependency: "oklch(0.65 0.18 160)",
  docker: "oklch(0.7 0.16 85)",
  infra: "oklch(0.55 0.22 15)",
}

export function FailureTypesChart({ data }: FailureTypesChartProps) {
  const chartData = [
    { name: "Test", value: data.test, color: COLORS.test },
    { name: "Dependency", value: data.dependency, color: COLORS.dependency },
    { name: "Docker", value: data.docker, color: COLORS.docker },
    { name: "Infrastructure", value: data.infra, color: COLORS.infra },
  ]

  return (
    <Card className="border-border bg-card">
      <CardHeader>
        <CardTitle className="text-base font-semibold">Failure Types Distribution</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="oklch(0.24 0.012 264)" />
            <XAxis dataKey="name" stroke="oklch(0.62 0.01 264)" fontSize={12} />
            <YAxis stroke="oklch(0.62 0.01 264)" fontSize={12} />
            <Tooltip
              contentStyle={{
                backgroundColor: "oklch(0.16 0.008 264)",
                border: "1px solid oklch(0.24 0.012 264)",
                borderRadius: "0.75rem",
                color: "oklch(0.98 0.004 264)",
              }}
            />
            <Bar dataKey="value" radius={[8, 8, 0, 0]}>
              {chartData.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}
