#!/usr/bin/env bash
set -e

# work in binding base directory
cd $(dirname "$0")

if [ ! -e "swagger-codegen-cli.jar" ]; then
    curl http://central.maven.org/maven2/io/swagger/swagger-codegen-cli/2.3.1/swagger-codegen-cli-2.3.1.jar -o swagger-codegen-cli.jar
fi

# work from binding base directory
cd ../../..

libBefore=$(find lib -type f)

mvn clean

java -jar src/main/api/swagger-codegen-cli.jar generate \
  -i src/main/api/tado-api.yaml \
  -l java \
  -o target/swagger \
  -c src/main/api/tado-client-config.json \
  -D apiTests=false,modelTests=false

# There's currently a connection leak during OAuth refresh. This patched version fixes the issue. 
# Can be removed when fix is applied to swagger-codegen master.
cp src/main/api/OAuth.java target/swagger/src/main/java/org/openhab/binding/tado/internal/api/auth/OAuth.java

cd target/swagger/
mvn package
mvn dependency:copy-dependencies -DincludeScope=runtime

rm ../../lib/*
cp target/tado-api-client-1.0.0.jar ../../lib/
cp target/dependency/* ../../lib

# Remove provided JARs again
rm ../../lib/slf4j-*.jar

cd ../../

libAfter=$(find lib -type f)

set +e 
changes=$(diff <(echo "$libBefore") <(echo "$libAfter"))

if [ -n "$changes" ]; then
  echo "Libraries have changed: "
  echo "$changes"
  echo
  echo "Update META-INF/MANIFEST.MF:"
  find lib -type f -exec echo " {}," \; | sed '$ s/.$//'
fi