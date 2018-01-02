#!/bin/bash

camelcaseId=$1
[ $# -eq 0 ] && { echo "Usage: $0 <BindingIdInCamelCase>"; exit 1; }

id=`echo $camelcaseId | tr '[:upper:]' '[:lower:]'` 

mvn -s ../archetype-settings.xml archetype:generate -N \
  -DarchetypeGroupId=org.eclipse.smarthome.archetype \
  -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding \
  -DarchetypeVersion=0.10.0-SNAPSHOT \
  -DgroupId=org.openhab.binding \
  -DartifactId=org.openhab.binding.$id \
  -Dpackage=org.openhab.binding.$id \
  -Dversion=2.3.0-SNAPSHOT \
  -DbindingId=$id \
  -DbindingIdCamelCase=$camelcaseId \
  -DvendorName=openHAB \
  -Dnamespace=org.openhab

directory=`echo "org.openhab.binding."$id/`

cp ../../src/etc/about.html "$directory"
