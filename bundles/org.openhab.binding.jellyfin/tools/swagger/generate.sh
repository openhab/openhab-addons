#!/usr/bin/bash
clear

# if [ ! -d "swagger-codegen" ]; then
#   echo "⏬ - Pull swagger-codegen repository"
#   git clone https://github.com/swagger-api/swagger-codegen
  
#   cd swagger-codegen
  
#   echo "⚙️ - Create swagger-codegen package"
#   mvn clean package
#   cd ..
# fi

VERSION=$(curl -sL https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json | jq -r .info.version)

echo "ℹ️  - Using latest Jellyfin API: ${VERSION}"

FILENAME="./jellyfin-openapi-$VERSION.json"

if [ ! -e "${FILENAME}" ]; then
    echo "⏬ - Downloading latest OPENAPI definition for Version ${VERSION}..."
    wget                            \
        --no-verbose                \
        --output-document=$FILENAME \
        https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json
fi

yq -p json -o yaml $FILENAME > jellyfin-openapi-$VERSION.yaml

#PREPROCESSED="./jellyfin-openapi-$VERSION-preprocessed.json"

# rm -f $PREPROCESSED
# cp $FILENAME $PREPROCESSED
# node scripts/modify-schema.mjs $PREPROCESSED

# echo "⚙️  - generating code from $PREPROCESSED"

# https://repo1.maven.org/maven2/io/swagger/swagger-codegen-cli/2.4.43/swagger-codegen-cli-2.4.43.jar
# java -Xmx8G -jar swagger-codegen-cli-${CLI}.jar config-help -l java
# export JAVA_OPTS="${JAVA_OPTS} -XX:MaxPermSize=256M -Xmx1024M -DloggerPath=conf/log4j.properties"

# java -Xmx8G -jar swagger-codegen-cli-${CLI}.jar generate \
# java -jar swagger-codegen-cli-${CLI}.jar generate \
#     --verbose \
#     --lang java \
#     --config config.json \
#     --input-spec ${PREPROCESSED}