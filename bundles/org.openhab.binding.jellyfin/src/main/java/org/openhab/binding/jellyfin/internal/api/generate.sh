#!/usr/bin/bash

set -e

VERSIONS=("10.8.13" "10.10.3")
REQUIRED=("wget" "yq" "openapi-generator-cli")

function checkEnvironment() {
    for i in "${REQUIRED[@]}"; do
        if ! type $i &>/dev/null; then
            echo "⚠️  [${i}] could not be found"
            exit 127
        fi
    done
}

checkEnvironment

for i in "${VERSIONS[@]}"; do
    echo "ℹ️  - API Version to generate: $i"

    FILENAME_JSON="./specifications/json/jellyfin-openapi-${i}.json"
    FILENAME_YAML="./specifications/yaml/jellyfin-openapi-${i}.yaml"

    if [ ! -e "${FILENAME_JSON}" ]; then
        echo "⏬ - Downloading OPENAPI definition for Version ${i}"

        SERVER=https://repo.jellyfin.org/files/openapi/stable/jellyfin-openapi-${i}.json

        wget \
            --no-verbose \
            --output-document=${FILENAME_JSON} \
            ${SERVER}

        if [ ! -e "${FILENAME_YAML}" ]; then
            echo "⚙️  - json ➡️ yaml"
            yq -oy ${FILENAME_JSON} >${FILENAME_YAML}
        fi
    fi

    echo "⚙️  - generate code for API ${i}"

    # TODO: config.yaml - https://openapi-generator.tech/docs/customization
    openapi-generator-cli generate -g java \
        --global-property models,modelTests=false,apis,apiTests=false,library=native,serializationLibrary=jackson,apiPackage=org.openhab.binding.jellyfin.internal.api.${i} \
        --input-spec ${FILENAME_YAML} -o ./generated/${i} 1>/dev/null
done
