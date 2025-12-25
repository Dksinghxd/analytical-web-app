"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts"

const data = [
  { name: "Critical", value: 45, color: "oklch(0.55 0.22 15)" },
  { name: "High", value: 78, color: "oklch(0.7 0.16 85)" },
  { name: "Medium", value: 62, color: "oklch(0.65 0.18 160)" },
  { name: "Low", value: 22, color: "oklch(0.58 0.22 264)" },
]

export function FailureImpactChart() {
  return (
    <Card className="border-border bg-card">
      <CardHeader>
        <CardTitle className="text-base font-semibold">Failure Impact Distribution</CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie data={data} cx="50%" cy="50%" labelLine={false} outerRadius={100} fill="#8884d8" dataKey="value">
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                backgroundColor: "oklch(0.16 0.008 264)",
                border: "1px solid oklch(0.24 0.012 264)",
                borderRadius: "0.75rem",
                color: "oklch(0.98 0.004 264)",
              }}
            />
            <Legend
              wrapperStyle={{
                fontSize: "12px",
              }}
            />
          </PieChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}
