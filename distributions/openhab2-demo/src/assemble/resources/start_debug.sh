#!/bin/sh

cd `dirname $0`

# set path to eclipse folder. If local folder, use '.'; otherwise, use /path/to/eclipse/
eclipsehome="runtime/server";

# set ports for HTTP(S) server
if [ ! -z ${OPENHAB_HTTP_PORT} ]
then
    HTTP_PORT=${OPENHAB_HTTP_PORT}
else
    HTTP_PORT=8080
fi

if [ ! -z ${OPENHAB_HTTPS_PORT} ]
then
    HTTPS_PORT=${OPENHAB_HTTPS_PORT}
else
    HTTPS_PORT=8443
fi

# get path to equinox jar inside $eclipsehome folder
cp=$(find $eclipsehome -name "org.eclipse.equinox.launcher_*.jar" | sort | tail -1);

if [ -z "$cp" ]; then
	echo "Error: Could not find equinox launcher in path $eclipsehome" 1>&2
	exit 1
fi

# debug options
debug_opts="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n"

# program args
prog_args="-Dlogback.configurationFile=./runtime/etc/logback_debug.xml -DmdnsName=openhab -Dopenhab.logdir=./userdata/logs -Dsmarthome.servicecfg=./runtime/etc/services.cfg -Dsmarthome.servicepid=org.openhab -Dsmarthome.userdata=./userdata -Dorg.quartz.properties=./runtime/etc/quartz.properties -Djetty.etc.config.urls=etc/jetty.xml,etc/jetty-ssl.xml,etc/jetty-deployer.xml,etc/jetty-https.xml,etc/jetty-selector.xml"

echo Launching the openHAB runtime in debug mode...
java $debug_opts $prog_args \
	-Dosgi.clean=true \
	-Declipse.ignoreApp=true \
	-Dosgi.noShutdown=true \
	-Djetty.home.bundle=org.openhab.io.jetty \
	-Djetty.keystore.path=./runtime/etc/keystore \
	-Dorg.osgi.service.http.port=$HTTP_PORT \
	-Dorg.osgi.service.http.port.secure=$HTTPS_PORT \
	-Dfelix.fileinstall.dir=addons \
	-Djava.library.path=lib \
	-Dequinox.ds.block_timeout=240000 \
	-Dequinox.scr.waitTimeOnBlock=60000 \
	-Dfelix.fileinstall.active.level=4 \
	-Djava.awt.headless=true \
	-jar $cp $* \
	-console 
