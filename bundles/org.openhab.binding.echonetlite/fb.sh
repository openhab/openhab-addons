#!/usr/bin/env bash

mvn spotless:apply -pl :org.openhab.binding.echonetlite && mvn clean install -pl :org.openhab.binding.echonetlite && cp ./org.openhab.binding.echonetlite/target/org.openhab.binding.echonetlite-3.3.0-SNAPSHOT.jar ~/opt/openhab/addons/.
