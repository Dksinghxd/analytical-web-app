# DevOps Analytics Web App

This repo contains:
- `front-end/`: Next.js dashboard UI
- `bfis-backend/`: Spring Boot API (BFIS)

## Deploy UI on Vercel (recommended)

1) Go to Vercel → **New Project** → import `Dksinghxd/analytical-web-app`.
2) In Vercel, set **Root Directory** to `front-end`.
3) Set environment variable:
   - `NEXT_PUBLIC_API_URL` = your backend base URL

### Backend URL options

- **Quickest (works today):** run backend locally and expose it via ngrok, then set:
  - `NEXT_PUBLIC_API_URL=https://<your-ngrok-subdomain>.ngrok-free.dev`

- **Permanent:** deploy the backend somewhere public (Render/Railway/Fly/Azure) and use that URL.

## Local run

### Backend

```cmd
cd bfis-backend\bfis-backend
mvn -DskipTests clean package
cd bfis-api
java -jar target\bfis-api-1.0.0-SNAPSHOT.jar
```

### Frontend

```cmd
cd front-end
pnpm install
pnpm dev
```

Open `http://localhost:3000`.
