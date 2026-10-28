@echo off
setlocal
title Metro Apex Bank - ATM Simulator Build System

echo ========================================================================
echo             METRO APEX BANK - PROJECT BUILD & PACKAGING
echo ========================================================================

:: Check for javac
where javac >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set "JAVAC_EXE=javac"
    set "JAR_EXE=jar"
    goto :BUILD
)

:: Check for bundled JDK
if exist "%~dp0.jdk\extracted\jdk-21.0.9+10\bin\javac.exe" (
    set "JAVAC_EXE=%~dp0.jdk\extracted\jdk-21.0.9+10\bin\javac.exe"
    set "JAR_EXE=%~dp0.jdk\extracted\jdk-21.0.9+10\bin\jar.exe"
    goto :BUILD
)

echo [ERROR] JDK compiler (javac) could not be located.
pause
exit /b 1

:BUILD
if not exist "%~dp0bin" mkdir "%~dp0bin"

echo [1/3] Compiling Java source files...
dir /s /b "%~dp0src\*.java" > "%~dp0sources.txt"
"%JAVAC_EXE%" -d "%~dp0bin" @"%~dp0sources.txt"
del "%~dp0sources.txt"

if %ERRORLEVEL% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)

echo [2/3] Generating executable JAR manifest...
echo Main-Class: com.atm.Main> "%~dp0manifest.txt"
echo.>> "%~dp0manifest.txt"

echo [3/3] Packaging atm-simulator.jar...
"%JAR_EXE%" cfm "%~dp0atm-simulator.jar" "%~dp0manifest.txt" -C "%~dp0bin" .
del "%~dp0manifest.txt"

echo.
echo [SUCCESS] Build completed successfully: atm-simulator.jar
echo Run 'run.bat' to launch the ATM simulator application.
pause
exit /b 0
