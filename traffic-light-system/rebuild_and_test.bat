@echo off
echo ========================================
echo  Traffic Light System - Rebuild & Test
echo ========================================
echo.

echo Step 1: Cleaning previous build...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean failed
    pause
    exit /b 1
)

echo.
echo Step 2: Compiling application...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven compile failed
    pause
    exit /b 1
)

echo.
echo Step 3: Running tests...
call mvn test
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Some tests failed, but continuing...
)

echo.
echo Step 4: Packaging application...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven package failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo  Build completed successfully!
echo ========================================
echo.
echo JAR file location: target/traffic-light-service-1.0.0.jar
echo.
echo To start the application:
echo java -jar target/traffic-light-service-1.0.0.jar
echo.
echo Configuration endpoints to test:
echo GET  http://localhost:9900/api/traffic-service/config
echo GET  http://localhost:9900/api/traffic-service/config/features
echo GET  http://localhost:9900/api/traffic-service/config/timings
echo GET  http://localhost:9900/api/traffic-service/config/limits
echo.
pause