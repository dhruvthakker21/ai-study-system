# Deploy to Render (free public link)

Anyone can open your app at a URL like:
`https://ai-study-backend.onrender.com`

## What you need

1. GitHub account (push this project)
2. Free [Render](https://render.com) account
3. Free MySQL (Render does not give free MySQL) — easiest: [Railway](https://railway.app) MySQL, or [Aiven](https://aiven.io) free trial, or [db4free.net](https://www.db4free.net)
4. Your `youtube_api_key` and `groq_api` keys

## Step 1 — Push code to GitHub

```powershell
cd c:\Users\Nirbhay\OneDrive\Desktop\ai-study-system
git add .
git commit -m "Add Render deploy config"
git push
```

(If you want, ask me to commit/push for you.)

## Step 2 — Create free MySQL

Create a MySQL database and copy:

- host
- port (usually 3306)
- database name
- username
- password

Your JDBC URL will look like:

```text
jdbc:mysql://HOST:3306/DATABASE_NAME?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

## Step 3 — Deploy on Render

1. Go to https://dashboard.render.com
2. Click **New** → **Blueprint**
3. Connect your GitHub repo `ai-study-system`
4. Render reads `render.yaml` and creates:
   - `ai-study-flask` (Python)
   - `ai-study-backend` (Java / Docker)
5. Fill in these env vars for **ai-study-backend**:

| Key | Value |
|-----|--------|
| `youtube_api_key` | your YouTube key |
| `groq_api` | your Groq key |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://HOST:3306/DB?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | your MySQL user |
| `SPRING_DATASOURCE_PASSWORD` | your MySQL password |
| `AI_SERVICE_URL` | `https://ai-study-flask.onrender.com` (use your real Flask URL after it deploys) |

6. Click **Apply** / wait for both services to finish building (~10–20 min first time)

## Step 4 — Share the link

Open:

```text
https://ai-study-backend.onrender.com
```

(Use the exact URL shown on the Render dashboard for `ai-study-backend`.)

That is the link you give to anyone.

## Important free-tier notes

- Free services **sleep after ~15 minutes** of no traffic. First open after sleep can take 30–60 seconds.
- Java is heavy on free memory — if the backend crashes, open Render logs and tell me; we can trim memory further.
- YouTube may block cloud IPs for transcripts sometimes; that is a YouTube limit, not your code.

## Manual deploy (if Blueprint fails)

### A) Flask
- New → Web Service
- Root directory: `flask-service`
- Build: `pip install -r requirements.txt`
- Start: `gunicorn wsgi:app --bind 0.0.0.0:$PORT --workers 1 --threads 4 --timeout 120`

### B) Spring Boot
- New → Web Service → Docker
- Dockerfile path: `./Dockerfile`
- Add the env vars from the table above
- Set `AI_SERVICE_URL` to your Flask public URL (https://....onrender.com)
