@echo off

:: set path to eclipse folder. If local folder, use '.'; otherwise, use c:\path\to\eclipse
set ECLIPSEHOME="runtime\server"

:: set ports for HTTP(S) server
set HTTP_PORT=8080
set HTTPS_PORT=8443

:: get path to equinox jar inside ECLIPSEHOME folder
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSEHOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set EQUINOXJAR=%%c
 
:: debug options
set DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n

:: program params
set PROG_ARGS=-Dlogback.configurationFile=./runtime/etc/logback_debug.xml -DmdnsName=openhab -Dopenhab.logdir=./userdata/logs -Dsmarthome.servicecfg=./runtime/etc/services.cfg -Dsmarthome.servicepid=org.openhab -Dorg.quartz.properties=./runtime/etc/quartz.properties

:: start Eclipse w/ java
echo Launching the openHAB runtime...
java %DEBUG_OPTS% %PROG_ARGS% -Dosgi.clean=true -Declipse.ignoreApp=true -Dosgi.noShutdown=true -Djetty.home.bundle=org.openhab.io.jetty -Djetty.port=%HTTP_PORT% -Djetty.port.ssl=%HTTPS_PORT% -Dfelix.fileinstall.dir=addons -Djava.library.path=lib -Dequinox.ds.block_timeout=240000 -Dequinox.scr.waitTimeOnBlock=60000 -Dfelix.fileinstall.active.level=4 -Djava.awt.headless=true -jar %EQUINOXJAR% %* -console 
