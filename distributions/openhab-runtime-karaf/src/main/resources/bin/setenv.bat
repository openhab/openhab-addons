@echo off
rem
rem
rem    Licensed to the Apache Software Foundation (ASF) under one or more
rem    contributor license agreements.  See the NOTICE file distributed with
rem    this work for additional information regarding copyright ownership.
rem    The ASF licenses this file to You under the Apache License, Version 2.0
rem    (the "License"); you may not use this file except in compliance with
rem    the License.  You may obtain a copy of the License at
rem
rem       http://www.apache.org/licenses/LICENSE-2.0
rem
rem    Unless required by applicable law or agreed to in writing, software
rem    distributed under the License is distributed on an "AS IS" BASIS,
rem    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem    See the License for the specific language governing permissions and
rem    limitations under the License.
rem

rem
rem handle specific scripts; the SCRIPT_NAME is exactly the name of the Karaf
rem script; for example karaf.bat, start.bat, stop.bat, admin.bat, client.bat, ...
rem
rem if "%KARAF_SCRIPT%" == "SCRIPT_NAME" (
rem   Actions go here...
rem )

rem
rem general settings which should be applied for all scripts go here; please keep
rem in mind that it is possible that scripts might be executed more than once, e.g.
rem in example of the start script where the start script is executed first and the
rem karaf script afterwards.
rem

rem
rem The following section shows the possible configuration options for the default
rem karaf scripts
rem
rem Window name of the windows console
rem SET KARAF_TITLE
rem Location of Java installation
rem SET JAVA_HOME
rem Minimum memory for the JVM
rem SET JAVA_MIN_MEM
rem Maximum memory for the JVM
rem SET JAVA_MAX_MEM
rem Minimum perm memory for the JVM
rem SET JAVA_PERM_MEM
rem Maximum perm memory for the JVM
rem SET JAVA_MAX_PERM_MEM
rem Karaf home folder
rem SET KARAF_HOME
rem Karaf data folder
rem SET KARAF_DATA
rem Karaf base folder
rem SET KARAF_BASE
rem Karaf etc folder
rem SET KARAF_ETC
rem Additional available Karaf options
rem SET KARAF_OPTS
rem Enable debug mode
rem SET KARAF_DEBUG

:: Use openHAB 2 directory layout
call "%DIRNAME%oh2_dir_layout.bat"

:: set ports for HTTP(S) server
:check_http_port
IF NOT [%OPENHAB_HTTP_PORT%] == [] GOTO :http_port_set
set HTTP_PORT=8080
goto :http_port_done

:http_port_set
set HTTP_PORT=%OPENHAB_HTTP_PORT%
goto :http_port_done

:http_port_done

:check_https_port
IF NOT [%OPENHAB_HTTPS_PORT%] == [] GOTO :https_port_set
set HTTPS_PORT=8443
goto :https_port_done

:https_port_set
set HTTPS_PORT=%OPENHAB_HTTPS_PORT%
goto :https_port_done

:https_port_done

:: set java options
set JAVA_OPTS=%JAVA_OPTS% ^
  -Dopenhab.home=%OPENHAB_HOME% ^
  -Dopenhab.conf=%OPENHAB_CONF% ^
  -Dopenhab.runtime=%OPENHAB_RUNTIME% ^
  -Dopenhab.userdata=%OPENHAB_USERDATA% ^
  -Dorg.osgi.service.http.port=%HTTP_PORT% ^
  -Dorg.osgi.service.http.port.secure=%HTTPS_PORT%

:: set jvm options
set EXTRA_JAVA_OPTS=-XX:+UseG1GC
