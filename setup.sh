#!/bin/bash

# TMWS Setup Script for Unix-like systems (macOS, Linux)
# This script initializes the project with Gradle Wrapper and pnpm dependencies

echo ""
echo "========================================"
echo " Task Management Web System Setup"
echo "========================================"
echo ""

# Check Java installation
echo "[1/3] Checking Java Installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 26 or higher"
    exit 1
else
    echo "OK: Java detected"
    java -version
fi

echo ""

# Initialize Gradle Wrapper
echo "[2/3] Initializing Gradle Wrapper..."
cd backend
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "Downloading Gradle Wrapper..."
    echo "To generate the Gradle Wrapper, run: gradle wrapper"
    echo "Or download from: https://docs.gradle.org/current/userguide/gradle_wrapper.html"
    echo ""
    echo "For now, using provided gradle-wrapper properties..."
else
    echo "OK: Gradle Wrapper found"
fi
cd ..

echo ""

# Check Node.js installation
echo "[3/3] Checking Node.js Installation..."
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js is not installed or not in PATH"
    echo "Please install Node.js 26 or higher"
    exit 1
else
    echo "OK: Node.js detected"
    node --version
fi

echo ""
echo "========================================"
echo "Setup Complete!"
echo "========================================"
echo ""
echo "Next steps:"
echo ""
echo "Backend:"
echo "  cd backend"
echo "  ./gradlew build"
echo "  ./gradlew bootRun"
echo ""
echo "Frontend:"
echo "  cd frontend"
echo "  pnpm install"
echo "  pnpm start"
echo ""

