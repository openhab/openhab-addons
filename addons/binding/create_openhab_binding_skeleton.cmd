@echo off

SETLOCAL
SET ARGC=0

FOR %%x IN (%*) DO SET /A ARGC+=1

IF %ARGC% NEQ 2 (
	echo Usage: %0 BindingIdInCamelCase BindingIdInLowerCase
	exit /B 1
)

call mvn -s ../archetype-settings.xml archetype:generate -N -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding -DarchetypeVersion=0.10.0-SNAPSHOT -DgroupId=org.openhab.binding -DartifactId=org.openhab.binding.%2 -Dpackage=org.openhab.binding.%2 -Dversion=2.3.0-SNAPSHOT -DbindingId=%2 -DbindingIdCamelCase=%1 -DvendorName=openHAB -Dnamespace=org.openhab

COPY ..\..\src\etc\about.html org.openhab.binding.%2%\

ENDLOCAL

