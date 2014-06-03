@echo off

:: set path to eclipse folder. If local folder, use '.'; otherwise, use c:\path\to\eclipse
set ECLIPSEHOME=server

:: set ports for HTTP(S) server
set HTTP_PORT=8080
set HTTPS_PORT=8443
 
:: get path to equinox jar inside ECLIPSEHOME folder
for /f "delims= tokens=1" %%c in ('dir /B /S /OD %ECLIPSEHOME%\plugins\org.eclipse.equinox.launcher_*.jar') do set EQUINOXJAR=%%c
 
:: start Eclipse w/ java
echo Launching the openHAB runtime...
java -Dosgi.clean=true -Declipse.ignoreApp=true -Dosgi.noShutdown=true -Djetty.home.bundle=org.eclipse.jetty.osgi.boot -Djetty.port=%HTTP_PORT% -Djetty.port.ssl=%HTTPS_PORT% -Dsmarthome.configfile=configurations/openhab.cfg -Dlogback.configurationFile=configurations/logback_debug.xml -Dfelix.fileinstall.dir=addons -Djava.library.path=lib -Dorg.quartz.properties=./runtime/etc/quartz.properties -Dsmarthome.configfile=openhab.cfg -DmdnsName=openhab -Dopenhab.logdir=./userdata/logs -Dequinox.ds.block_timeout=240000 -Dequinox.scr.waitTimeOnBlock=60000 -Dfelix.fileinstall.active.level=4 -Djava.awt.headless=true -jar %EQUINOXJAR% %* -console 
