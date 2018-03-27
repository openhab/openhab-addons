#!/bin/bash

camelcaseId=$1
[ $# -eq 0 ] && { echo "Usage: $0 <BindingIdInCamelCase>"; exit 1; }

id=`echo $camelcaseId | tr '[:upper:]' '[:lower:]'`

[ -d org.openhab.binding.$id ] || { echo "The binding with the id must exist: org.openhab.binding.$id"; exit 1; }

mvn -s ../archetype-settings.xml archetype:generate -N \
  -DarchetypeGroupId=org.eclipse.smarthome.archetype \
  -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding.test \
  -DarchetypeVersion=0.10.0-SNAPSHOT \
  -DgroupId=org.openhab.binding \
  -DartifactId=org.openhab.binding.$id.test \
  -Dpackage=org.openhab.binding.$id \
  -Dversion=2.3.0-SNAPSHOT \
  -DbindingId=$id \
  -DbindingIdCamelCase=$camelcaseId \
  -DvendorName=openHAB \
  -Dnamespace=org.openhab

directory=`echo "org.openhab.binding."$id".test"/`

cp ../../src/etc/about.html "$directory"
