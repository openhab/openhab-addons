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

REQUIRED=("wget" "yq" "docker" "curl" "jq" "awk" "sed" "grep" "sort" "tail" "mvn" "docker" "npm")

function checkEnvironment() {
    for i in "${REQUIRED[@]}"; do
        if ! type $i &>/dev/null; then
            echo "⚠️  [${i}] could not be found"
            exit 127
        fi
    done
}

checkEnvironment

OPENAPI_JAVA_CONFIG="tools/generate-sources/scripts/java.config.json"

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

# VERSIONS=("10.8.13" "10.10.7")
# VERSION_ALIAS=("legacy" "current")
VERSIONS=("10.11.3")
VERSION_ALIAS=("current")

DOCKER_VOLUME_WORK="/work"

ROOT=$(pwd)

OPENAPI_SPECIFICATION_DIR="tools/generate-sources/scripts/specifications"

OUTPUT=.

INDEX=0
for i in "${VERSIONS[@]}"; do
    ALIAS=${VERSION_ALIAS[INDEX++]}
    PACKAGE_BASE=org.openhab.binding.jellyfin.internal.api.generated.${ALIAS}
    PACKAGE_API=${PACKAGE_BASE}
    PACKAGE_MODEL=${PACKAGE_BASE}.model

    echo -e "  ➡️  generating Jersey API Version $i as \033[1m${ALIAS}\033[0m: ${PACKAGE_API}"

    FILENAME_JSON=${ROOT}/${OPENAPI_SPECIFICATION_DIR}/json/jellyfin-openapi-${i}.json
    FILENAME_YAML=${ROOT}/${OPENAPI_SPECIFICATION_DIR}/yaml/jellyfin-openapi-${i}.yaml

    mkdir -p logs/endpoints
    mkdir -p ${ROOT}/${OPENAPI_SPECIFICATION_DIR}/json/
    mkdir -p ${ROOT}/${OPENAPI_SPECIFICATION_DIR}/yaml/

    if [ ! -e "${FILENAME_JSON}" ]; then
        echo "  ⏬ - Downloading OPENAPI definition for Version ${i}"

        URL=https://repo.jellyfin.org/files/openapi/stable/jellyfin-openapi-${i}.json

        wget \
            --no-verbose \
            --output-document=${FILENAME_JSON} \
            ${URL} || {
            echo "  ❌ Error: Failed to download API definition from ${URL}"
            rm ${FILENAME_JSON}
            exit 1
        }
    fi

    jq ".paths | to_entries[] | {path: .key, methods: (.value | keys)}" ${FILENAME_JSON} | grep \"path\" >logs/endpoints/${i}-jersey.txt

    if [ ! -e "${FILENAME_YAML}" ]; then
        echo "⚙️: json ➡️  yaml"

        yq -oy ${FILENAME_JSON} >${FILENAME_YAML}
    fi

    # Check if the OpenAPI spec has the malformed TranscodeReasons schema
    # Some Jellyfin API versions (e.g., 10.11.3) incorrectly define TranscodeReasons with
    # both an inline enum AND type:array with $ref, which confuses the OpenAPI generator
    # causing it to generate broken code referencing a non-existent TranscodeReasonsEnum type.
    # Older versions (10.8.13, 10.10.7) don't have this problem and will skip this fix.
    FILENAME_YAML_INPUT="${FILENAME_YAML}"
    FILENAME_YAML_FIXED="${FILENAME_YAML}.fixed"
    
    HAS_ENUM=$(yq '.components.schemas.TranscodingInfo.properties.TranscodeReasons | has("enum")' ${FILENAME_YAML})
    HAS_ARRAY_TYPE=$(yq '.components.schemas.TranscodingInfo.properties.TranscodeReasons.type == "array"' ${FILENAME_YAML})
    
    if [ "$HAS_ENUM" = "true" ] && [ "$HAS_ARRAY_TYPE" = "true" ]; then
        echo "⚙️: Fixing malformed TranscodeReasons schema (has both enum and type:array)"
        # Remove the inline enum definition, keeping only the array type with $ref
        yq 'del(.components.schemas.TranscodingInfo.properties.TranscodeReasons.enum)' ${FILENAME_YAML} > ${FILENAME_YAML_FIXED}
        FILENAME_YAML_INPUT="${FILENAME_YAML_FIXED}"
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
        --config ${OPENAPI_JAVA_CONFIG} \
        --input-spec ${OPENAPI_SPECIFICATION_DIR}/yaml/$(basename ${FILENAME_YAML_INPUT}) -o ${OUTPUT} \
        >/dev/null
    
    # Clean up the temporary fixed file if it was created
    if [ -f "${FILENAME_YAML_FIXED}" ]; then
        rm ${FILENAME_YAML_FIXED}
    fi
done

cd ${ROOT}

MVN_OPT="--quiet"
echo ""
echo "🧹 apply formatting to generated code"
mvn spotless:apply $MVN_OPT
