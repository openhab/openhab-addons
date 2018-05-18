#!/bin/bash

[ $# -lt 2 ] && { echo "Usage: $0 <BindingIdInCamelCase> <Author>"; exit 1; }

bindingVersion=2.3.0-SNAPSHOT
archetypeVersion=0.10.0-SNAPSHOT

camelcaseId=$1
id=`echo $camelcaseId | tr '[:upper:]' '[:lower:]'`

author=$2

mvn -s ../archetype-settings.xml archetype:generate -N \
  -DarchetypeGroupId=org.eclipse.smarthome.archetype \
  -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding \
  -DarchetypeVersion=$archetypeVersion \
  -DgroupId=org.openhab.binding \
  -DartifactId=org.openhab.binding.$id \
  -Dpackage=org.openhab.binding.$id \
  -Dversion=$bindingVersion \
  -DbindingId=$id \
  -DbindingIdCamelCase=$camelcaseId \
  -DvendorName=openHAB \
  -Dnamespace=org.openhab \
  -Dauthor="$author"

directory="org.openhab.binding.$id/"

cp ../../src/etc/about.html "$directory"

