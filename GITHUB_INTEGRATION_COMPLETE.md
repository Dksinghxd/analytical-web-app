# GitHub App Integration - Setup Complete

## What Was Added

### Backend Services
1. **GitHubJwtService** - Generates JWT tokens for GitHub App authentication
2. **GitHubInstallationStore** - Stores installation IDs and access tokens
3. **GitHubInstallationTokenService** - Manages installation token lifecycle
4. **GitHubRepositoryService** - Fetches repositories from GitHub API

### API Endpoints
1. **GET /api/github/callback** - Handles GitHub redirect after app installation
2. **GET /api/github/repos** - Returns all accessible repositories
3. **POST /api/github/sync** - Manually triggers repository sync
4. **GET /api/github/status** - Returns connection status (updated)

### DTOs
1. **GitHubRepositoryResponse** - GitHub API repository response
2. **GitHubInstallationRepositoriesResponse** - List of repositories response

## Required GitHub App Configuration

### 1. Set Callback URL
Go to: https://github.com/settings/apps/bfis-ci-tracker-dksinghxd

Under **"Identifying and authorizing users"**:
- **Callback URL:** `https://unsatisfied-bridget-unprosperous.ngrok-free.dev/api/github/callback`
- Click "Save changes"

### 2. Webhook URL (Already Set)
- **Webhook URL:** `https://unsatisfied-bridget-unprosperous.ngrok-free.dev/api/github/webhook`

## Installation Flow

```
User clicks "Connect GitHub"
        ↓
Frontend → GET /api/github/connect
        ↓
Backend returns installation URL
        ↓
User redirected to GitHub
        ↓
User selects repositories → Click "Install"
        ↓
GitHub → GET /api/github/callback?installation_id=XXX
        ↓
Backend:
  1. Stores installation_id
  2. Generates JWT token
  3. Gets installation access token
  4. Calls GET /installation/repositories
  5. Auto-registers all repos in BFIS
        ↓
Redirects to: http://localhost:3000?github_connected=true
        ↓
Frontend:
  1. Detects ?github_connected=true
  2. Calls GET /api/github/repos
  3. Displays all repositories
```

## API Usage

### Check Connection Status
```bash
curl http://localhost:8084/api/github/status
```

Response:
```json
{
  "connected": true,
  "webhookEndpoint": "/api/github/webhook",
  "hasInstallation": true
}
```

### Get All Repositories
```bash
curl http://localhost:8084/api/github/repos
```

Response:
```json
[
  {
    "fullName": "Dksinghxd/Deepak-kumar",
    "owner": "Dksinghxd",
    "repoName": "Deepak-kumar",
    "defaultBranch": "main",
    "description": "Project description",
    "htmlUrl": "https://github.com/Dksinghxd/Deepak-kumar"
  }
]
```

### Manual Sync
```bash
curl -X POST http://localhost:8084/api/github/sync
```

## Next Steps

1. **Update GitHub App Callback URL** (REQUIRED)
   - Go to GitHub App settings
   - Add callback URL with your ngrok domain

2. **Rebuild Backend**
   ```bash
   cd bfis-backend/bfis-backend
   mvn clean install
   ```

3. **Restart Backend**
   ```bash
   cd bfis-api
   mvn spring-boot:run
   ```

4. **Test the Flow**
   - Click "Connect GitHub" in UI
   - Install app on repositories
   - Verify repos appear automatically

## Security Notes

- JWT tokens valid for 10 minutes
- Installation tokens valid for 1 hour (auto-refreshed)
- Tokens cached in memory (cleared on restart)
- Webhook signatures verified with HMAC-SHA256
