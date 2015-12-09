#!/bin/sh

rem DIRNAME is the directory of karaf, setenv, etc.
set "OPENHAB_HOME=%DIRNAME%..\..\.."

set "OPENHAB_CONF=%OPENHAB_HOME%\conf"
set "OPENHAB_RUNTIME=%OPENHAB_HOME%\runtime"
set "OPENHAB_USERDATA=%OPENHAB_HOME%\userdata"

set "KARAF_HOME=%OPENHAB_RUNTIME%\karaf"
set "KARAF_DATA=%OPENHAB_USERDATA%"
set "KARAF_BASE=%OPENHAB_USERDATA%"
set "KARAF_ETC=%OPENHAB_RUNTIME%\karaf\etc"

