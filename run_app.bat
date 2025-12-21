@echo off
setlocal EnableDelayedExpansion

echo Loading environment variables from .env...
if exist .env (
    for /f "usebackq tokens=*" %%A in (".env") do (
        set line=%%A
        if not "!line:~0,1!"=="#" (
            set "%%A"
        )
    )
) else (
    echo WARNING: .env file not found!
)

echo Starting MCP Server...
start "MCP Server" cmd /c "cd mcp-server && mvn spring-boot:run"

echo Waiting for MCP Server to initialize...
timeout /t 15 /nobreak >nul

echo Starting Chatbot App...
mvn spring-boot:run
