"use client"


import { useEffect, useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { cn } from "@/lib/utils"

const days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
const hours = ["00", "04", "08", "12", "16", "20"]

// Mock heatmap data - in real app this would come from API

const generateHeatmapData = () => {
  const data: number[][] = []
  for (let day = 0; day < 7; day++) {
    const dayData: number[] = []
    for (let hour = 0; hour < 6; hour++) {
      dayData.push(Math.floor(Math.random() * 20))
    }
    data.push(dayData)
  }
  return data
}


export function FailureHeatmap() {
  const [heatmapData, setHeatmapData] = useState<number[][]>([])

  useEffect(() => {
    setHeatmapData(generateHeatmapData())
  }, [])

  const getHeatColor = (value: number) => {
    if (value === 0) return "bg-muted/30"
    if (value < 5) return "bg-chart-1/30"
    if (value < 10) return "bg-chart-1/50"
    if (value < 15) return "bg-chart-1/70"
    return "bg-chart-1"
  }

  return (
    <Card className="border-border bg-card">
      <CardHeader>
        <CardTitle className="text-base font-semibold">Failure Frequency Heatmap</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          <div className="flex gap-2">
            <div className="w-12" />
            {hours.map((hour) => (
              <div key={hour} className="flex-1 text-center text-xs text-muted-foreground">
                {hour}
              </div>
            ))}
          </div>
          {heatmapData.length > 0 && days.map((day, dayIndex) => (
            <div key={day} className="flex items-center gap-2">
              <div className="w-12 text-xs text-muted-foreground">{day}</div>
              <div className="flex flex-1 gap-2">
                {heatmapData[dayIndex].map((value, hourIndex) => (
                  <div
                    key={`${dayIndex}-${hourIndex}`}
                    className={cn(
                      "flex-1 rounded aspect-square flex items-center justify-center text-xs font-medium transition-colors",
                      getHeatColor(value),
                    )}
                    title={`${day} ${hours[hourIndex]}:00 - ${value} failures`}
                  >
                    {value > 0 ? value : ""}
                  </div>
                ))}
              </div>
            </div>
          ))}
          <div className="flex items-center justify-end gap-2 pt-4">
            <span className="text-xs text-muted-foreground">Less</span>
            <div className="flex gap-1">
              <div className="h-4 w-4 rounded bg-muted/30" />
              <div className="h-4 w-4 rounded bg-chart-1/30" />
              <div className="h-4 w-4 rounded bg-chart-1/50" />
              <div className="h-4 w-4 rounded bg-chart-1/70" />
              <div className="h-4 w-4 rounded bg-chart-1" />
            </div>
            <span className="text-xs text-muted-foreground">More</span>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
