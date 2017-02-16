#!/bin/bash

camelcaseId=$1
[ $# -eq 0 ] && { echo "Usage: $0 <BindingIdInCamelCase>"; exit 1; }

id=`echo $camelcaseId | tr '[:upper:]' '[:lower:]'` 

mvn archetype:generate -N \
  -DarchetypeGroupId=org.eclipse.smarthome.archetype \
  -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding \
  -DarchetypeVersion=0.9.0-SNAPSHOT \
  -DgroupId=org.openhab.binding \
  -DartifactId=org.openhab.binding.$id \
  -Dpackage=org.openhab.binding.$id \
  -DarchetypeCatalog='file://../archetype-catalog.xml' \
  -Dversion=2.0.0-SNAPSHOT \
  -DbindingId=$id \
  -DbindingIdCamelCase=$camelcaseId \
  -DvendorName=openHAB \
  -Dnamespace=org.openhab
