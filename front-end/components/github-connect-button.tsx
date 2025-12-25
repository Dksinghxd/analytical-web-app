"use client"

import { useState, useEffect } from "react"
import { Github, Loader2, CheckCircle2, XCircle } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8084"

interface GitHubStatus {
  connected: boolean
  webhookEndpoint: string
  hasInstallation: boolean
}

export function GitHubConnectButton() {
  const [showDialog, setShowDialog] = useState(false)
  const [loading, setLoading] = useState(false)
  const [status, setStatus] = useState<GitHubStatus | null>(null)

  useEffect(() => {
    // Check for callback success in URL
    const params = new URLSearchParams(window.location.search)
    if (params.get("github_connected") === "true") {
      // Show success message
      setShowDialog(true)
      // Refresh status
      checkStatus()
      // Clean up URL
      window.history.replaceState({}, "", window.location.pathname)
    }
  }, [])

  const checkStatus = async () => {
    setLoading(true)
    try {
      const res = await fetch(`${API_URL}/api/github/status`)
      const data = await res.json()
      setStatus(data)
    } catch (error) {
      setStatus({
        connected: false,
        webhookEndpoint: "/api/github/webhook",
        hasInstallation: false,
      })
    } finally {
      setLoading(false)
    }
  }

  const handleConnect = async () => {
    setLoading(true)
    try {
      const res = await fetch(`${API_URL}/api/github/connect`)
      const data = await res.json()
      
      if (data.installUrl) {
        // Redirect to GitHub App installation
        // GitHub will redirect back to /api/github/callback after installation
        window.location.href = data.installUrl
      }
    } catch (error) {
      console.error("Failed to connect to GitHub", error)
    } finally {
      setLoading(false)
    }
  }

  const openDialog = async () => {
    setShowDialog(true)
    await checkStatus()
  }

  return (
    <>
      <Button
        onClick={openDialog}
        variant="outline"
        className="gap-2 bg-black text-white hover:bg-gray-800 border-gray-700"
      >
        <Github className="h-4 w-4" />
        {status?.hasInstallation ? "Manage GitHub" : "Connect GitHub"}
      </Button>

      <Dialog open={showDialog} onOpenChange={setShowDialog}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Github className="h-5 w-5" />
              GitHub App Integration
            </DialogTitle>
            <DialogDescription>
              Connect your GitHub repositories to automatically track CI/CD builds and failures
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
              </div>
            ) : status ? (
              <>
                <Alert variant={status.hasInstallation ? "default" : "destructive"}>
                  <div className="flex items-start gap-2">
                    {status.hasInstallation ? (
                      <CheckCircle2 className="h-4 w-4 mt-0.5 text-green-600" />
                    ) : (
                      <XCircle className="h-4 w-4 mt-0.5" />
                    )}
                    <AlertDescription>
                      {status.hasInstallation
                        ? "GitHub App is connected and ready to receive webhooks"
                        : "No GitHub App installation found. Click below to connect."}
                    </AlertDescription>
                  </div>
                </Alert>

                {status.hasInstallation && (
                  <div className="rounded-lg border p-4 space-y-2">
                    <p className="text-sm font-medium">✓ Connected</p>
                    <p className="text-xs text-muted-foreground">
                      Webhook endpoint: {status.webhookEndpoint}
                    </p>
                  </div>
                )}

                <div className="space-y-3">
                  <h4 className="text-sm font-medium">What happens next:</h4>
                  <ul className="text-sm text-muted-foreground space-y-2 list-disc list-inside">
                    <li>You'll be redirected to GitHub to install the app</li>
                    <li>Select which repositories to connect</li>
                    <li>BFIS will automatically fetch and register all repositories</li>
                    <li>Build events will be received via webhooks</li>
                  </ul>
                </div>

                <Button
                  onClick={handleConnect}
                  disabled={loading}
                  className="w-full gap-2"
                >
                  {loading ? (
                    <Loader2 className="h-4 w-4 animate-spin" />
                  ) : (
                    <Github className="h-4 w-4" />
                  )}
                  {status.hasInstallation ? "Reinstall / Add More Repos" : "Install GitHub App"}
                </Button>
              </>
            ) : null}
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}

