@rem Gradle build wrapper script for Windows
@rem Usage: gradlew.bat [tasks]

@echo off
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0

set JAVA_CMD=java
if not "%JAVA_HOME%"=="" set JAVA_CMD=%JAVA_HOME%\bin\java

if exist "%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar" (
    "%JAVA_CMD%" -classpath "%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
) else (
    echo Error: gradle-wrapper.jar not found at %SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar
    exit /b 1
)

