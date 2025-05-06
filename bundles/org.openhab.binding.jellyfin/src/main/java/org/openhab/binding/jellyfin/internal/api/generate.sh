#!/usr/bin/bash

# INFO:
# If you generate the API on a windows system using git bash some additional software is required:
#
# > winget install GNU.Wget2 --silent
# > winget install MikeFarah.yq --silent
# > winget install jqlang.jq --silent

# improve compatibility with windows systems (using git bash)
export MSYS_NO_PATHCONV=1
if ! type wget &>/dev/null; then
  alias wget=wget2
fi


REQUIRED=("wget" "yq" "docker" "curl" "jq" "realpath")

function checkEnvironment() {
    for i in "${REQUIRED[@]}"; do
        if ! type $i &>/dev/null; then
            echo "⚠️  [${i}] could not be found"
            exit 127
        fi
    done
}

checkEnvironment

DOCKER_IMAGE_OPENAPI=openapitools/openapi-generator-cli:v7.12.0
DOCKER_IMAGE_KIOTA=mcr.microsoft.com/openapi/kiota:1.25.1

echo "ℹ️ - Get openAPI builder docker image(s)"
echo ""
docker pull $DOCKER_IMAGE_OPENAPI
docker pull $DOCKER_IMAGE_KIOTA
echo ""

LATEST=$(curl -sL https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json | jq -r .info.version)
echo "ℹ️  - Latest stable Jellyfin API - Version: ${LATEST}"
echo ""
# VERSIONS=("10.8.13" "10.10.7" "${LATEST}")

VERSIONS=("10.8.13" "10.10.7")
VERSION_ALIAS=("legacy" "current")

DOCKER_VOLUME_WORK="/work"
ROOT=$(realpath "../../../../../../../../../")

INDEX=0
for i in "${VERSIONS[@]}"; do
    PACKAGE=org.openhab.binding.jellyfin.internal.api.version.${VERSION_ALIAS[INDEX++]}
    PACKAGE_API=$PACKAGE
    PACKAGE_MODEL=$PACKAGE.model

    OUTPUT=.

    echo "ℹ➡️  API Version to generate: $i ($PACKAGE_API)"

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
            echo "⚙️: json ➡️  yaml"
            yq -oy ${FILENAME_JSON} >${FILENAME_YAML}
        fi
    fi

    docker run --rm --interactive               \
        --user $(id -u):$(id -g)                \
        --volume "${ROOT}:${DOCKER_VOLUME_WORK}" \
        --workdir ${DOCKER_VOLUME_WORK}         \
        --env-file environment.ini              \
        $DOCKER_IMAGE_OPENAPI generate                  \
          --generator-name java                 \
          --global-property apiDocs=false,modelDocs=false,apiTests=false,modelTests=false \
          --api-package   ${PACKAGE_API}   \
          --model-package ${PACKAGE_MODEL} \
          --config src/main/java/org/openhab/binding/jellyfin/internal/api/java.config.json \
          --input-spec src/main/java/org/openhab/binding/jellyfin/internal/api/${FILENAME_YAML} -o $OUTPUT \
        > /dev/null

done

cd ${ROOT}

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# MVN_OPT="--quiet"
MVN_OPT=""

mvn spotless:apply $MVN_OPT
# mvn compile $MVN_OPT