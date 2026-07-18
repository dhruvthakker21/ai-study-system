@echo off
title AI Study System Launcher
cd /d "%~dp0"

echo ========================================
echo   AI Study System - Starting...
echo ========================================
echo.

REM Load API keys from .env if it exists
if exist ".env" (
  for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do (
    if not "%%A"=="" set "%%A=%%B"
  )
)

if "%youtube_api_key%"=="" (
  echo WARNING: youtube_api_key is not set — put it in a .env file
)
if "%groq_api%"=="" (
  echo WARNING: groq_api is not set — put it in a .env file
)

echo [1/2] Starting Flask on port 5000...
start "Flask Transcript Service" /D "%~dp0flask-service" cmd /k python app.py

timeout /t 3 /nobreak >nul

echo [2/2] Starting Spring Boot on port 8080...
start "Spring Boot App" /D "%~dp0" cmd /k mvnw.cmd spring-boot:run

echo.
echo Waiting for Spring Boot, then opening browser...
timeout /t 25 /nobreak >nul
start http://localhost:8080

echo.
echo Done!
echo   App:     http://localhost:8080
echo   Swagger: http://localhost:8080/swagger-ui/index.html
echo.
echo Close the two terminal windows to stop the apps.
pause
