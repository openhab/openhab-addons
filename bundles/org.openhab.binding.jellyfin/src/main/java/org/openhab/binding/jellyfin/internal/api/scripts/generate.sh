#!/usr/bin/bash

if [[ "${1:-}" == "--debug" ]]; then
    set -x
fi

set -euo pipefail

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

REQUIRED=("wget" "yq" "docker" "curl" "jq" "awk" "sed" "grep" "sort" "tail" "mvn" "docker")

function checkEnvironment() {
    for i in "${REQUIRED[@]}"; do
        if ! type $i &>/dev/null; then
            echo "⚠️  [${i}] could not be found"
            exit 127
        fi
    done
}

checkEnvironment

# Get the latest stable release tag from GitHub for openapi-generator
LATEST_OPENAPI_GENERATOR_CLI_TAG=$(curl -s "https://api.github.com/repos/OpenAPITools/openapi-generator/releases/latest" |
    jq -r '.tag_name' |
    sed 's/^v//')

echo -e "ℹ️  - Latest openapi-generator-cli version: \033[1m$LATEST_OPENAPI_GENERATOR_CLI_TAG\033[0m"

DOCKER_IMAGE_OPENAPI="openapitools/openapi-generator-cli:v${LATEST_OPENAPI_GENERATOR_CLI_TAG}"

if docker images --format '{{.Repository}}:{{.Tag}}' | grep -q "^openapitools/openapi-generator-cli:v${LATEST_OPENAPI_GENERATOR_CLI_TAG}$"; then
    echo "ℹ️  - Local docker image found"
else
    echo "ℹ️  - Get openAPI builder docker image for version ${LATEST_OPENAPI_GENERATOR_CLI_TAG}"
    docker pull $DOCKER_IMAGE_OPENAPI
fi

LATEST=$(curl -sL https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json | jq -r .info.version)
echo -e "ℹ️  - Latest stable Jellyfin API - Version: \033[1m${LATEST}\033[0m"

VERSIONS=("10.8.13" "10.10.7")
VERSION_ALIAS=("legacy" "current")

DOCKER_VOLUME_WORK="/work"

cd "../../../../../../../../../../"
ROOT=$(pwd)

OPENAPI_JAVA_CONFIG="scripts/java.config.json"
OPENAPI_SPECIFICATION_DIR="src/main/java/org/openhab/binding/jellyfin/internal/api/scripts/specifications"

OUTPUT=.

INDEX=0
for i in "${VERSIONS[@]}"; do
    ALIAS=${VERSION_ALIAS[INDEX++]}
    PACKAGE=org.openhab.binding.jellyfin.internal.api.generated.${ALIAS}
    PACKAGE_API=$PACKAGE
    PACKAGE_MODEL=$PACKAGE.model

    echo -e "  ➡️  generating API Version $i as \033[1m${ALIAS}\033[0m: ${PACKAGE_API}"

    FILENAME_JSON=${ROOT}/${OPENAPI_SPECIFICATION_DIR}/json/jellyfin-openapi-${i}.json
    FILENAME_YAML=${ROOT}/${OPENAPI_SPECIFICATION_DIR}/yaml/jellyfin-openapi-${i}.yaml

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

    docker run --rm --interactive \
        --user $(id -u):$(id -g) \
        --volume ${ROOT}:${DOCKER_VOLUME_WORK} \
        --workdir ${DOCKER_VOLUME_WORK} \
        --env JAVA_POST_PROCESS_FILE=/usr/local/bin/clang-format -i \
        $DOCKER_IMAGE_OPENAPI generate \
        --generator-name java \
        --global-property apiDocs=false,modelDocs=false,apiTests=false,modelTests=false \
        --api-package ${PACKAGE_API} \
        --model-package ${PACKAGE_MODEL} \
        --config src/main/java/org/openhab/binding/jellyfin/internal/api/${OPENAPI_JAVA_CONFIG} \
        --input-spec ${OPENAPI_SPECIFICATION_DIR}/yaml/jellyfin-openapi-${i}.yaml -o ${OUTPUT} \
        >/dev/null
done

cd ${ROOT}

export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

MVN_OPT="--quiet"
echo ""
echo "🧹 apply formatting to generated code"
mvn spotless:apply $MVN_OPT
