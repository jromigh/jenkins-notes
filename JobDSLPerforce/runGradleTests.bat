@echo off
cls
echo Requires installation of Gradle and Groovy 2.4+
rd /s /q build
gradle clean test --info %*
echo.
echo.
echo.
echo Check the build\reports\tests folder for reports.
echo Check the build\xml folder for generated jobs.
