# BFIS Complete Setup Guide

Complete walkthrough to set up the Build Failure Intelligence System with GitHub App integration.

---

## Prerequisites

- **Java 17+** (backend)
- **Node.js 18+** (frontend)
- **Maven 3.8+** (build tool)
- **pnpm** (or npm/yarn)
- **GitHub Account** (to create GitHub App)
- **ngrok** (for local webhook testing) - Download from https://ngrok.com/

---

## Part 1: Backend Setup (5 minutes)

### Step 1.1: Start Backend Server

```bash
cd bfis-backend/bfis-backend/bfis-api
mvn spring-boot:run
```

**Expected Output:**
```
Started BfisApplication in 1.763 seconds
Tomcat started on port 8084 (http)
```

✅ Backend is now running at `http://localhost:8084`

### Step 1.2: Test Backend APIs

Open browser or use curl:

```bash
# Check health
curl http://localhost:8084/actuator/health

# View sample data
curl http://localhost:8084/api/builds
curl http://localhost:8084/api/metrics

# Check GitHub App status (will show "not configured" until Step 2)
curl http://localhost:8084/api/github/status
```

---

## Part 2: Create GitHub App (10 minutes)

### Step 2.1: Create New GitHub App

1. Go to https://github.com/settings/apps/new
2. Fill in the form:

**Basic Information:**
- **GitHub App name:** `BFIS CI/CD Tracker` (must be globally unique)
- **Homepage URL:** `http://localhost:3000` (or your deployed URL)
- **Webhook URL:** *Leave blank for now* (we'll add after ngrok setup)

**Permissions:**
- **Repository permissions:**
  - Actions: **Read-only** ✓
  - Contents: **Read-only** ✓
  - Metadata: **Read-only** (auto-selected)

**Subscribe to events:**
- ✓ **Workflow run**

**Where can this GitHub App be installed?**
- Select: **Only on this account**

3. Click **"Create GitHub App"**

### Step 2.2: Save Credentials

After creation, you'll see your app settings page. Copy these values:

1. **App ID** (under "About" section)
   - Example: `123456`

2. **Client ID** (under "About" section)
   - Example: `Iv1.abc123def456`

3. **Generate a client secret:**
   - Click "Generate a new client secret"
   - Copy the secret immediately (you can't see it again)
   - Example: `ghs_abc123...xyz789`

4. **Generate a private key:**
   - Scroll down to "Private keys"
   - Click "Generate a private key"
   - A `.pem` file will download (save it securely)

5. **Create webhook secret:**
   - Open terminal and generate random string:
     ```bash
     # Windows (PowerShell)
     -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | % {[char]$_})
     
     # Linux/Mac
     openssl rand -hex 32
     ```
   - Copy the output
   - Go back to GitHub App settings
   - Scroll to "Webhook" section
   - Paste the secret in "Webhook secret" field
   - Click "Save changes"

### Step 2.3: Encode Private Key

The backend requires the private key in base64 format (single line, no newlines).

**Windows (Command Prompt):**
```cmd
certutil -encode private-key.pem temp.b64
findstr /v /c:- temp.b64 > private-key.b64
type private-key.b64
```

**Windows (PowerShell):**
```powershell
$content = Get-Content -Path "private-key.pem" -Raw
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($content))
```

**Linux/Mac:**
```bash
base64 -w 0 private-key.pem
```

Copy the entire output (single long string).

### Step 2.4: Create Backend .env File

```bash
cd bfis-backend/bfis-backend/bfis-api
copy .env.example .env   # Windows
# cp .env.example .env   # Linux/Mac
```

Edit `.env` and paste your actual values:

```dotenv
GITHUB_APP_ID=123456
GITHUB_APP_CLIENT_ID=Iv1.abc123def456
GITHUB_APP_CLIENT_SECRET=ghs_abc123xyz789...
GITHUB_APP_WEBHOOK_SECRET=your_random_32_char_string
GITHUB_APP_PRIVATE_KEY=LS0tLS1CRUdJTi... (your base64 encoded key)
```

**Important:** No quotes, no spaces, just the raw values.

### Step 2.5: Restart Backend

Stop the backend (Ctrl+C) and restart:

```bash
mvn spring-boot:run
```

Verify configuration:
```bash
curl http://localhost:8084/api/github/status
```

**Expected Response:**
```json
{
  "configured": true,
  "appId": "123456",
  "message": "GitHub App is properly configured"
}
```

✅ GitHub App credentials are configured!

---

## Part 3: Setup Webhook Tunnel (5 minutes)

Since your backend is on `localhost:8084`, GitHub can't reach it. Use ngrok to create a public tunnel.

### Step 3.1: Install ngrok

Download from https://ngrok.com/download and follow their setup instructions.

### Step 3.2: Start ngrok Tunnel

```bash
ngrok http 8084
```

**Expected Output:**
```
Forwarding   https://abc123.ngrok.io -> http://localhost:8084
```

Copy the `https://` URL (e.g., `https://abc123.ngrok.io`).

### Step 3.3: Update GitHub App Webhook URL

1. Go to https://github.com/settings/apps/your-app-name
2. Scroll to "Webhook" section
3. Set **Webhook URL:** `https://abc123.ngrok.io/api/github/webhook`
4. Ensure **Content type:** is set to `application/json`
5. Ensure **Secret** matches your `GITHUB_APP_WEBHOOK_SECRET`
6. Check **Active** checkbox
7. Click **"Save changes"**

### Step 3.4: Test Webhook Delivery

GitHub will send a test ping. Check:

1. In ngrok terminal, you should see:
   ```
   POST /api/github/webhook  200 OK
   ```

2. In backend logs, you should see:
   ```
   Received GitHub webhook: ping
   ```

3. In GitHub App settings, scroll to "Recent Deliveries" - you should see a green checkmark ✓

✅ Webhook connection successful!

---

## Part 4: Frontend Setup (3 minutes)

### Step 4.1: Install Dependencies

```bash
cd front-end
pnpm install  # or: npm install
```

### Step 4.2: Configure Environment

The `.env.local` file already exists with:
```dotenv
NEXT_PUBLIC_API_URL=http://localhost:8084
```

This tells the frontend to connect to your backend.

### Step 4.3: Start Frontend

```bash
pnpm dev  # or: npm run dev
```

**Expected Output:**
```
▲ Next.js 14.x.x
- Local:   http://localhost:3000
- Ready in 2.5s
```

### Step 4.4: Open Application

Navigate to http://localhost:3000

You should see:
- **Top navigation:** "Connect GitHub" button
- **Overview page:** Metrics, charts, and "Tracked Repositories" panel (empty initially)

---

## Part 5: Connect Your First Repository (5 minutes)

### Step 5.1: Install GitHub App

1. In the BFIS frontend (http://localhost:3000), click **"Connect GitHub"** button
2. Click **"Install GitHub App"**
3. You'll be redirected to GitHub installation page
4. Select which repositories to install on:
   - **All repositories** (not recommended for testing)
   - **Only select repositories** ✓ (recommended)
     - Choose one or two test repositories
5. Click **"Install & Authorize"**

### Step 5.2: Register Repository in BFIS

After installation, manually register the repository:

```bash
curl -X POST http://localhost:8084/api/repos \
  -H "Content-Type: application/json" \
  -d '{
    "owner": "your-github-username",
    "repoName": "your-repo-name",
    "defaultBranch": "main"
  }'
```

**Expected Response:**
```json
{
  "id": "uuid-here",
  "owner": "your-github-username",
  "repoName": "your-repo-name",
  "defaultBranch": "main",
  "createdAt": "2025-12-25T18:30:00Z"
}
```

Refresh the frontend - your repository should now appear in the "Tracked Repositories" panel!

---

## Part 6: Test End-to-End Webhook Flow (5 minutes)

### Step 6.1: Trigger GitHub Actions Workflow

In your connected repository:

**Option A: Push a commit**
```bash
git commit --allow-empty -m "Test BFIS webhook"
git push
```

**Option B: Manually trigger workflow**
1. Go to repository → Actions tab
2. Select a workflow
3. Click "Run workflow"

### Step 6.2: Monitor Webhook Processing

Watch the backend logs. You should see:

```
Received GitHub webhook: workflow_run
Action: completed
Workflow: CI Build
Conclusion: success
Repository: your-username/your-repo
Processing workflow_run completed event
Successfully processed workflow_run event
```

### Step 6.3: Verify Build Ingestion

Check that the build was ingested:

```bash
# List all builds
curl http://localhost:8084/api/builds

# Get builds for specific repo
curl http://localhost:8084/api/builds?repo=your-username/your-repo
```

You should see your GitHub Actions workflow as a build entry!

### Step 6.4: View in Frontend

Refresh http://localhost:3000 - your build should appear in:
- **Total Builds** metric (incremented)
- **Build Duration Chart** (new data point)
- **Timeline page** (under /timeline)

✅ **Congratulations!** Your BFIS system is fully operational with GitHub App integration.

---

## Architecture Overview

```
┌─────────────────┐
│  GitHub Actions │
│  (Workflow Run) │
└────────┬────────┘
         │ webhook event
         ▼
    ┌────────┐
    │ ngrok  │ (dev only)
    └───┬────┘
        │
        ▼
┌───────────────────┐      ┌──────────────┐
│   BFIS Backend    │◄────►│   Frontend   │
│   (port 8084)     │      │ (port 3000)  │
│                   │      │              │
│ - GitHub Webhook  │      │ - React UI   │
│ - Signature Verify│      │ - Charts     │
│ - Event Processing│      │ - Analytics  │
│ - In-Memory Store │      │              │
└───────────────────┘      └──────────────┘
```

---

## Troubleshooting

### Backend won't start
- Check Java version: `java -version` (need 17+)
- Check port 8084 is free: `netstat -ano | findstr :8084`
- Rebuild: `mvn clean install -DskipTests`

### GitHub App shows "not configured"
- Verify `.env` file exists in `bfis-api/` directory
- Check all 5 environment variables are set (no quotes, no spaces)
- Restart backend after changing `.env`

### Webhook returns 401 Unauthorized
- Webhook secret mismatch
- Verify `GITHUB_APP_WEBHOOK_SECRET` matches GitHub App settings
- Check backend logs for "Invalid webhook signature"

### Webhook not received
- Check ngrok is running: `curl https://your-ngrok-url.ngrok.io/api/github/webhook`
- Verify webhook URL in GitHub App settings
- Check "Recent Deliveries" in GitHub App settings for errors

### Build not appearing in frontend
- Check repository is registered: `curl http://localhost:8084/api/repos`
- Verify webhook was processed: check backend logs
- Repository name format must be: `owner/repo` (lowercase)

### Frontend shows CORS error
- Backend must be running on port 8084
- Check `NEXT_PUBLIC_API_URL` in `front-end/.env.local`

---

## Production Deployment Notes

When deploying to production:

1. **Backend:**
   - Deploy to cloud provider (AWS, Azure, GCP)
   - Update GitHub App webhook URL to your domain
   - Use environment variables (not .env file)
   - Enable HTTPS

2. **Frontend:**
   - Update `NEXT_PUBLIC_API_URL` to production backend URL
   - Deploy to Vercel, Netlify, or similar

3. **GitHub App:**
   - Update Homepage URL to production frontend
   - Update Webhook URL to production backend
   - Keep webhook secret and private key secure

---

## Next Steps

- **Automated Repository Registration:** Build UI form to register repositories without curl
- **OAuth Callback Handler:** Automatically register repositories after GitHub App installation
- **Persistent Storage:** Replace in-memory store with PostgreSQL/MongoDB
- **Real-time Updates:** Add WebSocket support for live build status updates
- **Advanced Analytics:** Implement ML-based failure prediction and root cause analysis

---

**Need Help?**
- Backend docs: `bfis-backend/bfis-backend/GITHUB_APP_SETUP.md`
- GitHub App docs: https://docs.github.com/en/apps
- Webhook events: https://docs.github.com/en/webhooks/webhook-events-and-payloads

**Happy Building! 🚀**
