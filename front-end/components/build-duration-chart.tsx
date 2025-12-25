"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts"
import type { BuildTrend } from "@/lib/types"

interface BuildDurationChartProps {
  data: BuildTrend[]
}

export function BuildDurationChart({ data }: BuildDurationChartProps) {
  return (
    <Card className="border-border bg-card">
      <CardHeader>
        <CardTitle className="text-base font-semibold">Build Duration Trends</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="oklch(0.24 0.012 264)" />
            <XAxis
              dataKey="date"
              stroke="oklch(0.62 0.01 264)"
              fontSize={12}
              tickFormatter={(value) => new Date(value).toLocaleDateString("en-US", { month: "short", day: "numeric" })}
            />
            <YAxis stroke="oklch(0.62 0.01 264)" fontSize={12} />
            <Tooltip
              contentStyle={{
                backgroundColor: "oklch(0.16 0.008 264)",
                border: "1px solid oklch(0.24 0.012 264)",
                borderRadius: "0.75rem",
                color: "oklch(0.98 0.004 264)",
              }}
              formatter={(value: number) => [`${value}s`, "Duration"]}
              labelFormatter={(label) => new Date(label).toLocaleDateString("en-US", { month: "long", day: "numeric" })}
            />
            <Line
              type="monotone"
              dataKey="duration"
              stroke="oklch(0.58 0.22 264)"
              strokeWidth={2}
              dot={{ fill: "oklch(0.58 0.22 264)", r: 4 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}
