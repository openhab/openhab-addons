#!/usr/bin/bash

set -e
REQUIRED=("wget" "yq" "docker" "curl" "jq")

function checkEnvironment() {
    for i in "${REQUIRED[@]}"; do
        if ! type $i &>/dev/null; then
            echo "⚠️  [${i}] could not be found"
            exit 127
        fi
    done
}

checkEnvironment

LATEST=$(curl -sL https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json | jq -r .info.version)
echo "ℹ️  - Latest stable API - Version: ${LATEST}"
echo ""
VERSIONS=("10.8.13" "10.10.3" "${LATEST}")

DOCKER_IMAGE=openapitools/openapi-generator-cli:v7.12.0
DOCKER_VOLUME_WORK="/work"

for i in "${VERSIONS[@]}"; do
    echo "ℹ➡️ API Version to generate: $i"

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
            echo "⚙️: json ➡️ yaml"
            yq -oy ${FILENAME_JSON} >${FILENAME_YAML}
        fi
    fi

    docker run --rm --interactive               \
        --user $(id -u):$(id -g)                \
        --volume "${PWD}:${DOCKER_VOLUME_WORK}" \
        --workdir ${DOCKER_VOLUME_WORK}         \
        --env-file environment.ini              \
        $DOCKER_IMAGE generate --generator-name java --global-property apis,apiTests=false,apiDocs=false,models,modelDocs=false,modelTests=false,apiPackage=org.openhab.binding.jellyfin.internal.api.${i} --config java.config.json --input-spec ${FILENAME_YAML} -o ./generated/${i} \
        > /dev/null
done
