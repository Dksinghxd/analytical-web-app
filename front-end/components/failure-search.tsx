"use client"

import { Search } from "lucide-react"
import { Input } from "@/components/ui/input"

export function FailureSearch() {
  return (
    <div className="relative flex-1">
      <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <Input
        placeholder="Search error messages, build IDs, or repositories..."
        className="pl-10 bg-card border-border"
      />
    </div>
  )
}
