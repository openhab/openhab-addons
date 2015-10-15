@echo off

:: set path to eclipse folder. If local folder, use '.'; otherwise, use c:\path\to\eclipse
set ECLIPSEHOME="runtime/server"

:: set ports for HTTP(S) server
:check_http_port
IF NOT [%OPENHAB_HTTP_PORT%] == [] GOTO :http_port_set
set HTTP_PORT=8080
goto :check_https_port

:http_port_set
set HTTP_PORT=%OPENHAB_HTTP_PORT%
goto :check_https_port

:check_https_port
IF NOT [%OPENHAB_HTTPS_PORT%] == [] GOTO :https_port_set
set HTTPS_PORT=8443
goto :check_path

:https_port_set
set HTTPS_PORT=%OPENHAB_HTTPS_PORT%
goto :check_path

:: get path to equinox jar inside ECLIPSEHOME folder
:check_path
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSEHOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set EQUINOXJAR=%%c

IF NOT [%EQUINOXJAR%] == [] GOTO :Launch
echo No equinox launcher in path '%ECLIPSEHOME%' found!
goto :eof

:Launch 
:: debug options
set DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n

:: start Eclipse w/ java
echo Launching the openHAB runtime...
java ^
%DEBUG_OPTS% ^
-DmdnsName=openhab ^
-Dopenhab.logdir=./userdata/logs ^
-Dsmarthome.servicecfg=./runtime/etc/services.cfg ^
-Dsmarthome.servicepid=org.openhab ^
-Dsmarthome.userdata=./userdata ^
-Dosgi.clean=true ^
-Declipse.ignoreApp=true ^
-Dosgi.noShutdown=true ^
-Dorg.osgi.service.http.port=%HTTP_PORT% ^
-Dorg.osgi.service.http.port.secure=%HTTPS_PORT% ^
-Djetty.home.bundle=org.openhab.io.jetty ^
-Djetty.keystore.path=./runtime/etc/keystore ^
-Dlogback.configurationFile=./runtime/etc/logback_debug.xml ^
-Dfelix.fileinstall.dir=./addons ^
-Djava.library.path=./lib ^
-Djava.security.auth.login.config=./runtime/etc/login.conf ^
-Dorg.quartz.properties=./runtime/etc/quartz.properties ^
-Dequinox.ds.block_timeout=240000 ^
-Dequinox.scr.waitTimeOnBlock=60000 ^
-Djava.awt.headless=true ^
-Dfelix.fileinstall.active.level=4 ^
-jar %EQUINOXJAR% %* ^
-console 
