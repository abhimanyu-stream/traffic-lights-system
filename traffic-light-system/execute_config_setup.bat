@echo off
echo Setting up Configuration Tables for Traffic Light System
echo.

REM Check if MySQL is available
where mysql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: MySQL client not found in PATH
    echo Please ensure MySQL is installed and added to your PATH
    echo Or run the SQL script manually in your MySQL client
    pause
    exit /b 1
)

echo Connecting to MySQL and executing configuration setup...
echo.

REM Execute the SQL script
mysql -u root -proot -h localhost -P 3306 < create_configuration_tables.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS: Configuration tables created successfully!
    echo.
    echo Tables created:
    echo - system_configurations
    echo - feature_flags  
    echo - configuration_history
    echo - feature_flag_history
    echo.
    echo Default data has been inserted.
    echo You can now rebuild and restart the application.
) else (
    echo.
    echo ERROR: Failed to execute SQL script
    echo Please check your MySQL connection and try again
    echo Or execute the SQL script manually
)

echo.
pause