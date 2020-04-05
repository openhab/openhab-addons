#!/bin/bash

if [[ $_ != $0 ]]
then
    echo "Script has to be executed, not sourced"

else

    set -e

    mvn clean install -pl :org.openhab.binding.boschshc -DskipChecks -DskipTests

    sudo cp target/org.openhab.binding.boschshc-2.5.1-SNAPSHOT.jar /usr/share/openhab2/addons/org.openhab.binding.boschshc-2.5.1-SNAPSHOT.jar
    sudo chown openhab:openhab /usr/share/openhab2/addons/org.openhab.binding.boschshc-2.5.1-SNAPSHOT.jar

    md5sum target/org.openhab.binding.boschshc-2.5.1-SNAPSHOT.jar
    md5sum /usr/share/openhab2/addons/org.openhab.binding.boschshc-2.5.1-SNAPSHOT.jar

    (
	echo "Deleting old Bosch bundle from openhab"
	openhab-cli console -p habopen "bundle:list" | grep -i bosch
	openhab-cli console -p habopen "bundle:uninstall org.openhab.binding.boschshc"

	echo "After deleting it .. "
	openhab-cli console -p habopen "bundle:list" | grep -i bosch
    ) || true

    echo "Stopping OpenHab"
    sudo systemctl stop openhab2
    echo "Starting Openhab"
    sudo systemctl start openhab2

    echo "Waiting 30 secs.."
    sleep 60
    grep 'Initializing' /var/log/openhab2/openhab.log
fi
