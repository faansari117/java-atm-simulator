@echo off
setlocal
title Metro Apex Bank - ATM Simulator

echo ========================================================================
echo             METRO APEX BANK - ATM TERMINAL LAUNCHER
echo ========================================================================

:: Check for system java
where java >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set "JAVA_EXE=java"
    goto :RUN
)

:: Check for bundled portable JDK
if exist "%~dp0.jdk\extracted\jdk-21.0.9+10\bin\java.exe" (
    set "JAVA_EXE=%~dp0.jdk\extracted\jdk-21.0.9+10\bin\java.exe"
    goto :RUN
)

echo [ERROR] Java runtime could not be located.
echo Please install Java 17+ or ensure the bundled JDK is extracted in .jdk/
pause
exit /b 1

:RUN
echo Starting ATM Graphical User Interface...
start "" "%JAVA_EXE%" -jar "%~dp0atm-simulator.jar"
exit /b 0
