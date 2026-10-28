@echo off
setlocal
title Metro Apex Bank - ATM Simulator Test Suite

echo ========================================================================
echo             METRO APEX BANK - AUTOMATED TEST RUNNER
echo ========================================================================

:: Check for java
where java >nul 2>nul
if %ERRORLEVEL% equ 0 (
    set "JAVA_EXE=java"
    goto :RUN_TEST
)

:: Check for bundled JDK
if exist "%~dp0.jdk\extracted\jdk-21.0.9+10\bin\java.exe" (
    set "JAVA_EXE=%~dp0.jdk\extracted\jdk-21.0.9+10\bin\java.exe"
    goto :RUN_TEST
)

echo [ERROR] Java runtime could not be located.
pause
exit /b 1

:RUN_TEST
"%JAVA_EXE%" -cp "%~dp0bin" com.atm.test.ATMServiceTest
pause
exit /b 0
