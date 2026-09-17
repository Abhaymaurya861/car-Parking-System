@echo off
setlocal enabledelayedexpansion
title Packaging Standalone Executable JAR

:: Detect jar
set JAR_CMD=jar
where jar >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\jar.exe" (
        set "JAR_CMD=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\jar.exe"
    ) else (
        for /d %%i in ("C:\Program Files\Microsoft\jdk*") do (
            if exist "%%i\bin\jar.exe" set "JAR_CMD=%%i\bin\jar.exe"
        )
    )
)

if not exist bin\com\carparking\Main.class (
    echo [INFO] Building classes first...
    call build.bat
    if %errorlevel% neq 0 exit /b %errorlevel%
)

echo Main-Class: com.carparking.Main > manifest.txt
"!JAR_CMD!" cfm CarParkingApp.jar manifest.txt -C bin com
del manifest.txt

if exist CarParkingApp.jar (
    echo.
    echo ========================================================
    echo [SUCCESS] Packaged successfully into CarParkingApp.jar!
    echo You can run it with: java -jar CarParkingApp.jar
    echo Or by double-clicking CarParkingApp.jar
    echo ========================================================
) else (
    echo [ERROR] Failed to create JAR file.
    exit /b 1
)
