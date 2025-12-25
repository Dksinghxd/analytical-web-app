"use client"

import { Card, CardContent } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "lucide-react"

export function FilterPanel() {
  return (
    <Card className="border-border bg-card">
      <CardContent className="p-4">
        <div className="flex flex-wrap items-center gap-3">
          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Repository" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Repositories</SelectItem>
              <SelectItem value="frontend">frontend-app</SelectItem>
              <SelectItem value="backend">backend-api</SelectItem>
              <SelectItem value="mobile">mobile-app</SelectItem>
            </SelectContent>
          </Select>

          <Button variant="outline" className="gap-2 bg-transparent">
            <Calendar className="h-4 w-4" />
            Last 7 days
          </Button>

          <Select defaultValue="all">
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Failure Type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Types</SelectItem>
              <SelectItem value="test">Test Failures</SelectItem>
              <SelectItem value="dependency">Dependencies</SelectItem>
              <SelectItem value="docker">Docker Issues</SelectItem>
              <SelectItem value="infra">Infrastructure</SelectItem>
            </SelectContent>
          </Select>

          <Button variant="outline" className="ml-auto bg-transparent">
            Reset Filters
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
