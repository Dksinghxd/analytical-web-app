"use client"

import { useEffect, useState } from "react"
import { Github, ExternalLink, Calendar, CheckCircle2, RefreshCw } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { Alert, AlertDescription } from "@/components/ui/alert"

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8084"

interface GitHubRepository {
  fullName: string
  owner: string
  repoName: string
  defaultBranch: string
  description: string | null
  htmlUrl: string
}

interface TrackedRepository {
  id: string
  owner: string
  repoName: string
  defaultBranch: string
  createdAt: string
}

export function TrackedReposPanel() {
  const [githubRepos, setGithubRepos] = useState<GitHubRepository[]>([])
  const [trackedRepos, setTrackedRepos] = useState<TrackedRepository[]>([])
  const [loading, setLoading] = useState(true)
  const [syncing, setSyncing] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchRepos()
  }, [])

  const fetchRepos = async () => {
    setLoading(true)
    setError(null)
    try {
      // First check if GitHub App is installed
      const statusRes = await fetch(`${API_URL}/api/github/status`)
      const status = statusRes.ok ? await statusRes.json() : { hasInstallation: false }
      
      // If installed, fetch from GitHub App API
      if (status.hasInstallation) {
        const githubRes = await fetch(`${API_URL}/api/github/repos`)
        
        if (githubRes.ok) {
          const githubData = await githubRes.json()
          setGithubRepos(Array.isArray(githubData) ? githubData : [])
        }
      }
      
      // Always fetch tracked repos from BFIS
      const trackedRes = await fetch(`${API_URL}/api/repos`)
      if (trackedRes.ok) {
        const trackedData = await trackedRes.json()
        setTrackedRepos(Array.isArray(trackedData) ? trackedData : [])
      }
    } catch (error) {
      console.error("Failed to fetch repositories:", error)
      setError("Failed to load repositories. Check if backend is running.")
    } finally {
      setLoading(false)
    }
  }

  const handleSync = async () => {
    setSyncing(true)
    try {
      const res = await fetch(`${API_URL}/api/github/sync`, { method: "POST" })
      if (res.ok) {
        await fetchRepos()
      } else {
        setError("Failed to sync repositories")
      }
    } catch (error) {
      console.error("Failed to sync repositories:", error)
      setError("Failed to sync repositories")
    } finally {
      setSyncing(false)
    }
  }

  if (loading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Connected Repositories</CardTitle>
          <CardDescription>Loading repositories...</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-16 w-full" />
          ))}
        </CardContent>
      </Card>
    )
  }

  const displayRepos = githubRepos.length > 0 ? githubRepos : trackedRepos

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <Github className="h-5 w-5" />
              Connected Repositories
            </CardTitle>
            <CardDescription>
              {displayRepos.length === 0
                ? "No repositories connected yet"
                : `${displayRepos.length} ${displayRepos.length === 1 ? "repository" : "repositories"} receiving webhooks`}
            </CardDescription>
          </div>
          <div className="flex gap-2">
            {githubRepos.length > 0 && (
              <Button 
                variant="ghost" 
                size="sm" 
                onClick={handleSync}
                disabled={syncing}
              >
                <RefreshCw className={`h-4 w-4 ${syncing ? "animate-spin" : ""}`} />
                Sync
              </Button>
            )}
            <Button variant="ghost" size="sm" onClick={fetchRepos}>
              Refresh
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {error && (
          <Alert variant="destructive" className="mb-4">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}
        
        {displayRepos.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground">
            <Github className="h-12 w-12 mx-auto mb-3 opacity-50" />
            <p className="text-sm">Connect your first repository to start tracking builds</p>
            <p className="text-xs mt-2">Click "Connect GitHub" above to get started</p>
          </div>
        ) : (
          <div className="space-y-3">
            {githubRepos.length > 0
              ? githubRepos.map((repo) => (
                  <div
                    key={repo.fullName}
                    className="flex items-center justify-between p-3 rounded-lg border bg-card hover:bg-accent/50 transition-colors"
                  >
                    <div className="flex items-center gap-3 flex-1">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <Github className="h-5 w-5 text-primary" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="font-medium text-sm truncate">{repo.fullName}</p>
                          <CheckCircle2 className="h-4 w-4 text-green-600 flex-shrink-0" />
                        </div>
                        <div className="flex items-center gap-3 text-xs text-muted-foreground mt-1">
                          <Badge variant="outline" className="text-xs">
                            {repo.defaultBranch}
                          </Badge>
                          {repo.description && (
                            <span className="truncate">{repo.description}</span>
                          )}
                        </div>
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="flex-shrink-0"
                      onClick={() => window.open(repo.htmlUrl, "_blank")}
                    >
                      <ExternalLink className="h-4 w-4" />
                    </Button>
                  </div>
                ))
              : trackedRepos.map((repo) => (
                  <div
                    key={repo.id}
                    className="flex items-center justify-between p-3 rounded-lg border bg-card hover:bg-accent/50 transition-colors"
                  >
                    <div className="flex items-center gap-3 flex-1">
                      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                        <Github className="h-5 w-5 text-primary" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <p className="font-medium text-sm truncate">
                            {repo.owner}/{repo.repoName}
                          </p>
                          <CheckCircle2 className="h-4 w-4 text-green-600 flex-shrink-0" />
                        </div>
                        <div className="flex items-center gap-3 text-xs text-muted-foreground mt-1">
                          <Badge variant="outline" className="text-xs">
                            {repo.defaultBranch}
                          </Badge>
                          <span className="flex items-center gap-1">
                            <Calendar className="h-3 w-3" />
                            {new Date(repo.createdAt).toLocaleDateString()}
                          </span>
                        </div>
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="flex-shrink-0"
                      onClick={() =>
                        window.open(`https://github.com/${repo.owner}/${repo.repoName}`, "_blank")
                      }
                    >
                      <ExternalLink className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}

