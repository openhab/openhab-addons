@echo off

setlocal
set DIRNAME=%~dp0%
"%DIRNAME%runtime\karaf\bin\karaf.bat" %*
