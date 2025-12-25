# BFIS GitHub App Integration - Setup Guide

## Architecture Overview

```
User (Browser)
    ↓
BFIS Frontend (Next.js)
    ↓
GET /api/github/connect
    ↓
GitHub App Installation Page
    ↓ (User selects repos)
GitHub App Installed
    ↓
GitHub Webhooks → POST /api/github/webhook
    ↓
BFIS Backend (Verifies + Processes)
    ↓
In-Memory Store (Builds + Failures)
    ↓
Dashboard Updates
```

## 1. Create GitHub App

1. Go to https://github.com/settings/apps/new
2. Fill in:
   - **GitHub App name**: `bfis` (or your preferred name)
   - **Homepage URL**: `http://localhost:3000` (for dev)
   - **Webhook URL**: `https://your-ngrok-url.ngrok.io/api/github/webhook`
   - **Webhook secret**: Generate a random string (save this)
   
3. **Permissions** (Repository permissions):
   - Actions: Read-only (to receive workflow_run events)
   - Contents: Read-only (optional, for push events)
   - Metadata: Read-only (required)

4. **Subscribe to events**:
   - ✅ Workflow run

5. **Where can this GitHub App be installed?**
   - Select "Any account"

6. Click **Create GitHub App**

7. **After creation**:
   - Note the **App ID**
   - Generate a **Client secret** (under OAuth credentials)
   - Generate a **private key** (download the `.pem` file)

## 2. Configure Backend

### Environment Variables

Create `bfis-backend/bfis-backend/bfis-api/.env` (or set in IDE):

```bash
GITHUB_APP_ID=123456
GITHUB_APP_CLIENT_ID=Iv1.abc123def456
GITHUB_APP_CLIENT_SECRET=your_client_secret_here
GITHUB_APP_WEBHOOK_SECRET=your_webhook_secret_from_step_1
GITHUB_APP_PRIVATE_KEY=your_base64_encoded_private_key
```

### Private Key Encoding

```bash
# Convert PEM to base64 (single line)
cat downloaded-key.pem | base64 -w 0 > key.txt
# Use the content of key.txt as GITHUB_APP_PRIVATE_KEY
```

## 3. Local Development with ngrok

GitHub needs a public URL to send webhooks. Use ngrok:

```bash
# Install ngrok: https://ngrok.com/download
# Start ngrok tunnel
ngrok http 8084

# You'll get a URL like: https://abc123.ngrok.io
# Update your GitHub App webhook URL to:
# https://abc123.ngrok.io/api/github/webhook
```

## 4. Start Backend

```bash
cd bfis-backend/bfis-backend
mvn clean install
cd bfis-api
mvn spring-boot:run
```

Backend will start on `http://localhost:8084`

## 5. Start Frontend

```bash
cd front-end
npm install  # or pnpm install
npm run dev  # or pnpm dev
```

Frontend will start on `http://localhost:3000`

## 6. Test the Flow

### Step 1: Connect Repository

1. Open `http://localhost:3000` in browser
2. Click "Connect GitHub Repository" button (to be added to UI)
3. You'll be redirected to GitHub
4. Select one or more repositories to install the app
5. Click "Install"

### Step 2: Trigger a Workflow

1. Push code to the connected repository
2. Ensure a GitHub Actions workflow runs
3. GitHub will send a `workflow_run` webhook to BFIS

### Step 3: Verify Ingestion

Check backend logs:
```
Received GitHub webhook: event=workflow_run, delivery=abc-123
Ingested build from GitHub: repo=owner/repo, status=SUCCESS, duration=45s
```

### Step 4: View Dashboard

- Refresh `http://localhost:3000`
- You should see the new build in metrics and charts

## 7. Webhook Event Flow

```json
{
  "action": "completed",
  "workflow_run": {
    "id": 123456,
    "name": "CI",
    "head_branch": "main",
    "head_sha": "abc123",
    "status": "completed",
    "conclusion": "success",
    "created_at": "2025-12-25T10:00:00Z",
    "updated_at": "2025-12-25T10:05:00Z"
  },
  "repository": {
    "full_name": "owner/repo",
    "name": "repo",
    "owner": {
      "login": "owner"
    }
  }
}
```

BFIS processes this and creates:
- **Build**: repo=owner/repo, status=SUCCESS, duration=300s
- **Failure** (if conclusion=failure): type=TEST, message=workflow failed

## 8. Security Notes

- ✅ Webhook signatures are verified using HMAC SHA-256
- ✅ Only registered repositories are accepted (existing validation)
- ✅ Secrets are loaded from environment variables
- ❌ DO NOT commit `.env` files
- ❌ DO NOT log secrets

## 9. Production Deployment

For production:
1. Deploy backend with public URL (e.g., `https://bfis-api.yourcompany.com`)
2. Update GitHub App webhook URL to production endpoint
3. Set environment variables in production environment
4. Use proper secret management (AWS Secrets Manager, Azure Key Vault, etc.)

## 10. Troubleshooting

### Webhook not received
- Check ngrok is running and URL is correct in GitHub App settings
- Verify webhook secret matches in both GitHub and BFIS
- Check backend logs for signature verification errors

### Build not showing in dashboard
- Verify repository is registered in BFIS via `/api/repos`
- Check backend logs for "untracked repository" warnings
- Ensure workflow_run event is enabled in GitHub App

### Signature verification fails
- Double-check `GITHUB_APP_WEBHOOK_SECRET` matches GitHub App settings
- Ensure payload is not modified in transit

## API Endpoints

### Backend

- `GET /api/github/connect` - Returns GitHub App install URL
- `POST /api/github/webhook` - Receives GitHub webhooks
- `GET /api/github/status` - Connection status
- `POST /api/repos` - Register a repository
- `GET /api/repos` - List tracked repositories
- `GET /api/builds` - List builds
- `GET /api/failures` - List failures
- `GET /api/metrics` - Get metrics

### Frontend

- `http://localhost:3000` - Dashboard
- `http://localhost:3000/failures` - Failure analysis
- `http://localhost:3000/timeline` - Build timeline

## Next Steps

1. Add frontend UI for "Connect GitHub Repository"
2. Display connected repositories
3. Show last webhook received timestamp
4. Add ability to disconnect/reinstall app
5. Implement repository-specific filters in dashboard
