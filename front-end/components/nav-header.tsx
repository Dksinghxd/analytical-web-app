"use client"

import Link from "next/link"
import { ChevronDown, User, Bell, Settings } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Badge } from "@/components/ui/badge"
import { GitHubConnectButton } from "@/components/github-connect-button"

const environments = ["Dev", "Staging", "Prod"]

export function NavHeader() {
  return (
    <header className="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex h-14 items-center gap-4 px-4">
        <Link href="/" className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary">
            <span className="text-lg font-bold text-primary-foreground">B</span>
          </div>
          <span className="text-lg font-semibold">BFIS</span>
        </Link>

        <nav className="flex flex-1 items-center gap-6 text-sm">
          <Link href="/" className="text-muted-foreground hover:text-foreground transition-colors">
            Overview
          </Link>
          <Link href="/failures" className="text-muted-foreground hover:text-foreground transition-colors">
            Failures
          </Link>
          <Link href="/timeline" className="text-muted-foreground hover:text-foreground transition-colors">
            Timeline
          </Link>
          <Link href="/generator" className="text-muted-foreground hover:text-foreground transition-colors">
            UI Generator
          </Link>
        </nav>

        <div className="flex items-center gap-2">
          <GitHubConnectButton />
          
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm" className="gap-2 bg-transparent">
                <Badge variant="secondary" className="bg-success text-success-foreground">
                  Prod
                </Badge>
                <ChevronDown className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>Environment</DropdownMenuLabel>
              <DropdownMenuSeparator />
              {environments.map((env) => (
                <DropdownMenuItem key={env}>{env}</DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>

          <Button variant="ghost" size="icon" className="relative">
            <Bell className="h-4 w-4" />
            <span className="absolute right-1 top-1 h-2 w-2 rounded-full bg-destructive" />
          </Button>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon">
                <User className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>My Account</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem>
                <Settings className="mr-2 h-4 w-4" />
                Settings
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem>Sign out</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  )
}
