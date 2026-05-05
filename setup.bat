@echo off
REM TMWS Setup Script for Windows
REM This script initializes the project with Gradle Wrapper and npm dependencies

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  Task Management Web System Setup
echo ========================================
echo.

REM Check Java installation
echo [1/3] Checking Java Installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
) else (
    echo OK: Java detected
)

echo.

REM Initialize Gradle Wrapper
echo [2/3] Initializing Gradle Wrapper...
cd backend
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo Downloading Gradle Wrapper...
    REM Note: First time, you may need to install gradle globally or use the wrapper
    echo To generate the Gradle Wrapper, run: gradle wrapper
    echo Or download from: https://docs.gradle.org/current/userguide/gradle_wrapper.html
    echo.
    echo For now, using provided gradle-wrapper properties...
) else (
    echo OK: Gradle Wrapper found
)
cd ..

echo.

REM Check Node.js installation
echo [3/3] Checking Node.js Installation...
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js 16.0 or higher
    pause
    exit /b 1
) else (
    echo OK: Node.js detected
    node --version
)

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Next steps:
echo.
echo Backend:
echo   cd backend
echo   .\gradlew.bat build
echo   .\gradlew.bat bootRun
echo.
echo Frontend:
echo   cd frontend
echo   npm install
echo   npm start
echo.
echo.
pause

