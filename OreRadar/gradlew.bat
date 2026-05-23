@echo off
setlocal
set GRADLE_VERSION=8.11
set GRADLE_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin
set GRADLE_ZIP=%TEMP%\gradle-%GRADLE_VERSION%-bin.zip
set GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip
if exist "%GRADLE_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat" goto :run
echo [Setup] Downloading Gradle %GRADLE_VERSION%...
powershell -Command "& { (New-Object Net.WebClient).DownloadFile('%GRADLE_URL%', '%GRADLE_ZIP%') }"
if %errorlevel% neq 0 ( echo [ERROR] Failed to download Gradle. & exit /b 1 )
echo [Setup] Extracting Gradle...
mkdir "%GRADLE_DIR%" 2>nul
powershell -Command "& { Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%GRADLE_DIR%' -Force }"
del "%GRADLE_ZIP%"
:run
"%GRADLE_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat" %*
endlocal
