@echo off

SETLOCAL
SET ARGC=0

FOR %%x IN (%*) DO SET /A ARGC+=1

IF %ARGC% NEQ 3 (
	echo Usage: %0 BindingIdInCamelCase Author GithubUser
	exit /B 1
)

SET OpenhabCoreVersion="2.5.0"
SET OpenhabVersion="2.5.5-SNAPSHOT"

SET BindingIdInCamelCase=%~1
SET BindingIdInLowerCase=%BindingIdInCamelCase%
SET Author=%~2
SET GithubUser=%~3

call :LoCase BindingIdInLowerCase

call mvn -s archetype-settings.xml archetype:generate -N -DarchetypeGroupId=org.openhab.core.tools.archetypes -DarchetypeArtifactId=org.openhab.core.tools.archetypes.binding -DarchetypeVersion=%OpenhabCoreVersion% -DgroupId=org.openhab.binding -DartifactId=org.openhab.binding.%BindingIdInLowerCase% -Dpackage=org.openhab.binding.%BindingIdInLowerCase% -Dversion=%OpenhabVersion% -DbindingId=%BindingIdInLowerCase% -DbindingIdCamelCase=%BindingIdInCamelCase% -DvendorName=openHAB -Dnamespace=org.openhab -Dauthor="%Author%" -DgithubUser="%GithubUser%"

COPY ..\src\etc\NOTICE org.openhab.binding.%BindingIdInLowerCase%\

:: temporary fix
:: replace ${project.version} by ${ohc.version} in src/main/feature/feature.xml
@powershell -command "(Get-Content org.openhab.binding.$env:BindingIdInLowerCase/src/main/feature/feature.xml).replace('-core/${project.version}', '-core/${ohc.version}') | Set-Content org.openhab.binding.$env:BindingIdInLowerCase/src/main/feature/feature.xml"

(SET BindingIdInLowerCase=)
(SET BindingIdInCamelCase=)
(SET Author=)
(SET GithubUser=)

GOTO:EOF

:LoCase
:: Subroutine to convert a variable VALUE to all lower case.
:: The argument for this subroutine is the variable NAME.
FOR %%i IN ("A=a" "B=b" "C=c" "D=d" "E=e" "F=f" "G=g" "H=h" "I=i" "J=j" "K=k" "L=l" "M=m" "N=n" "O=o" "P=p" "Q=q" "R=r" "S=s" "T=t" "U=u" "V=v" "W=w" "X=x" "Y=y" "Z=z") DO CALL SET "%1=%%%1:%%~i%%"
GOTO:EOF

ENDLOCAL
