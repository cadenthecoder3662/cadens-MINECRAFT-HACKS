@echo off
title OreRadar + FlyMod Builder
color 0A
echo ============================================
echo   OreRadar + FlyMod Builder
echo   by Cadenthegoat3662
echo ============================================
echo.
java -version >nul 2>&1
if %errorlevel% neq 0 ( color 0C & echo [ERROR] Java not found! & pause & exit /b 1 )
echo [1/3] Java found. Starting build...
echo.
call gradlew.bat build
if %errorlevel% neq 0 ( color 0C & echo. & echo [ERROR] Build failed. & pause & exit /b 1 )
echo.
echo ============================================
echo [SUCCESS] Mod built!
echo.
echo Your .jar is in: build\libs\OreRadar-1.0.0.jar
echo Copy it to: %%APPDATA%%\.minecraft\mods\
echo.
echo H = toggle ore ESP
echo J = toggle flight
echo ============================================
set /p open="Open build\libs folder? (Y/N): "
if /i "%open%"=="Y" explorer build\libs
pause
