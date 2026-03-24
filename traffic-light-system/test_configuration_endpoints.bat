@echo off
echo ========================================
echo  Testing Configuration Endpoints
echo ========================================
echo.

set BASE_URL=http://localhost:9900/api/traffic-service/config

echo Testing configuration endpoints...
echo Make sure the application is running on port 9900
echo.
pause

echo.
echo 1. Testing GET all configurations...
curl -X GET "%BASE_URL%" -H "Content-Type: application/json"
echo.
echo.

echo 2. Testing GET feature flags...
curl -X GET "%BASE_URL%/features" -H "Content-Type: application/json"
echo.
echo.

echo 3. Testing GET timing configurations...
curl -X GET "%BASE_URL%/timings" -H "Content-Type: application/json"
echo.
echo.

echo 4. Testing GET system limits...
curl -X GET "%BASE_URL%/limits" -H "Content-Type: application/json"
echo.
echo.

echo 5. Testing GET specific configuration...
curl -X GET "%BASE_URL%/redLightDuration" -H "Content-Type: application/json"
echo.
echo.

echo 6. Testing UPDATE configuration...
curl -X PUT "%BASE_URL%/redLightDuration" -H "Content-Type: application/json" -d "35"
echo.
echo.

echo 7. Testing UPDATE feature flag...
curl -X PUT "%BASE_URL%/features/enableAnalytics?enabled=false" -H "Content-Type: application/json"
echo.
echo.

echo 8. Testing configuration after update...
curl -X GET "%BASE_URL%/redLightDuration" -H "Content-Type: application/json"
echo.
echo.

echo ========================================
echo  Configuration endpoint testing complete
echo ========================================
pause