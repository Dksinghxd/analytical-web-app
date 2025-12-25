"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Sparkles, RefreshCw, Check } from "lucide-react"
import { Progress } from "@/components/ui/progress"

export function UIGenerationPanel() {
  const [isGenerating, setIsGenerating] = useState(false)
  const [progress, setProgress] = useState(0)
  const [isComplete, setIsComplete] = useState(false)

  const handleGenerate = () => {
    setIsGenerating(true)
    setIsComplete(false)
    setProgress(0)

    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval)
          setIsGenerating(false)
          setIsComplete(true)
          return 100
        }
        return prev + 10
      })
    }, 300)
  }

  return (
    <Card className="border-border bg-card">
      <CardHeader>
        <CardTitle className="text-base font-semibold">AI-Powered UI Generation</CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-2">
          <p className="text-sm text-muted-foreground">
            Our AI analyzes your recent build data and failure patterns to generate an optimized dashboard layout that
            highlights the most critical metrics for your team.
          </p>
        </div>

        {isGenerating && (
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Analyzing build data...</span>
              <span className="font-medium text-foreground">{progress}%</span>
            </div>
            <Progress value={progress} className="h-2" />
          </div>
        )}

        {isComplete && (
          <div className="p-4 rounded-lg bg-success/10 border border-success/20 flex items-center gap-3">
            <div className="p-2 rounded-full bg-success/20">
              <Check className="h-4 w-4 text-success" />
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-success">Dashboard Generated Successfully</p>
              <p className="text-xs text-muted-foreground mt-0.5">Preview the optimized layout below</p>
            </div>
          </div>
        )}

        <Button
          onClick={handleGenerate}
          disabled={isGenerating}
          className="w-full gap-2 bg-primary text-primary-foreground hover:bg-primary/90"
        >
          {isGenerating ? (
            <>
              <RefreshCw className="h-4 w-4 animate-spin" />
              Generating...
            </>
          ) : (
            <>
              <Sparkles className="h-4 w-4" />
              Generate Optimal UI
            </>
          )}
        </Button>
      </CardContent>
    </Card>
  )
}
