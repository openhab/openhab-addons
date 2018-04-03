@echo off

SETLOCAL
SET ARGC=0

FOR %%x IN (%*) DO SET /A ARGC+=1

IF %ARGC% NEQ 2 (
	echo Usage: %0 BindingIdInCamelCase BindingIdInLowerCase
	exit /B 1
)

If NOT exist "org.openhab.binding.%2" (
	echo The binding with the id must exist: org.openhab.binding.%2
	exit /B 1
)

call mvn -s ../archetype-settings.xml archetype:generate -N -DarchetypeGroupId=org.eclipse.smarthome.archetype -DarchetypeArtifactId=org.eclipse.smarthome.archetype.binding.test -DarchetypeVersion=0.10.0-SNAPSHOT -DgroupId=org.openhab.binding -DartifactId=org.openhab.binding.%2.test -Dpackage=org.openhab.binding.%2 -Dversion=2.3.0-SNAPSHOT -DbindingId=%2 -DbindingIdCamelCase=%1 -DvendorName=openHAB -Dnamespace=org.openhab

COPY ..\..\src\etc\about.html org.openhab.binding.%2.test%\

ENDLOCAL

