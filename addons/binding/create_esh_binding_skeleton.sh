#!/bin/bash

camelcaseId=$1
[ $# -eq 0 ] && { echo "Usage: $0 <BindingIdInCamelCase>"; exit 1; }

id=`echo $camelcaseId | tr '[:upper:]' '[:lower:]'` 

mvn archetype:generate -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding -DarchetypeVersion=0.7.0-SNAPSHOT -DgroupId=org.eclipse.smarthome.binding -DartifactId=org.eclipse.smarthome.binding.$id -Dpackage=org.eclipse.smarthome.binding.$id -DarchetypeCatalog='file://../archetype-catalog.xml' -Dversion=0.7.0-SNAPSHOT -DbindingId=$id -DbindingIdCamelCase=$camelcaseId
