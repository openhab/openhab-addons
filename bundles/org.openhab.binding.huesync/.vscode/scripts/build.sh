#!/usr/bin/bash
set -e
PROJECT_ROOT=$1
POM=$PROJECT_ROOT/pom.xml

VERSION=`yq ".project.parent.version" $POM`
BINDING=`yq ".project.artifactId" $POM`

MVN_OPT="--quiet -f ${POM}"

echo "➡️ $BINDING-$VERSION"
echo ""
echo "🧹 clean"
mvn clean $MVN_OPT

echo "📃 format"
mvn spotless:apply $MVN_OPT

echo "🔎 verify"
mvn verify $MVN_OPT

echo "💬 update translations"
mvn i18n:generate-default-translations $MVN_OPT

echo "📦 package"
mvn package $MVN_OPT

DOCKER_OPENHAB_VERSION=4.2.0-debian
DOCKER_MOUNT=$HOME/Temp/openhab/$DOCKER_OPENHAB_VERSION

LOGS=$DOCKER_MOUNT/userdata/logs/*
ARTIFACT=$PROJECT_ROOT/target/$BINDING-$VERSION.jar
ADDONS=$DOCKER_MOUNT/addons/

sudo truncate --size 0 $LOGS

cp --force $ARTIFACT $ADDONS