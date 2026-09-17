@echo off
setlocal enabledelayedexpansion
title Running Automated Billing Tests

:: Detect java
set JAVA_CMD=java
where java >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\java.exe"
    ) else (
        for /d %%i in ("C:\Program Files\Microsoft\jdk*") do (
            if exist "%%i\bin\java.exe" set "JAVA_CMD=%%i\bin\java.exe"
        )
    )
)

if not exist bin\com\carparking\test\BillingTest.class (
    echo [INFO] Binaries not found, running build first...
    call build.bat
    if %errorlevel% neq 0 exit /b %errorlevel%
)

"!JAVA_CMD!" -Dfile.encoding=UTF-8 -cp bin com.carparking.test.BillingTest
