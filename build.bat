@echo off
setlocal enabledelayedexpansion
title Building Car Parking Management System

echo ========================================================
echo   Compiling Car Parking Management System (Java)
echo ========================================================

:: Detect javac
set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot\bin\javac.exe"
    ) else (
        for /d %%i in ("C:\Program Files\Microsoft\jdk*") do (
            if exist "%%i\bin\javac.exe" set "JAVAC_CMD=%%i\bin\javac.exe"
        )
    )
)

echo Using compiler: !JAVAC_CMD!

if not exist bin mkdir bin

:: Find all java files and compile
dir /s /b src\*.java > sources.txt
"!JAVAC_CMD!" -encoding UTF-8 -d bin @sources.txt
set COMPILE_STATUS=%errorlevel%
del sources.txt

if %COMPILE_STATUS% equ 0 (
    echo.
    echo [SUCCESS] Compilation finished successfully! Classes located in bin/
    echo ========================================================
) else (
    echo.
    echo [ERROR] Compilation failed with exit code %COMPILE_STATUS%.
    echo ========================================================
    exit /b %COMPILE_STATUS%
)
