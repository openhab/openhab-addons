#!/usr/bin/bash
clear

VERSION=$(curl -sL https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json | jq -r .info.version)

echo "ℹ️  - Latest Jellyfin API: ${VERSION}"

VERSION=10.8.13

echo "ℹ️  - Generate API for: ${VERSION}"

FILENAME="./jellyfin-openapi-$VERSION.json"

if [ ! -e "${FILENAME}" ]; then
    echo "⏬ - Downloading latest OPENAPI definition for Version ${VERSION}..."
    wget                            \
        --no-verbose                \
        --output-document=$FILENAME \
        https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json
fi

yq -p json -o yaml $FILENAME > jellyfin-openapi-$VERSION.yaml

DOCKER_IMAGE=openapitools/openapi-generator-cli:v7.12.0
DOCKER_VOLUME_CONFIG="/config"
DOCKER_VOLUME_WORK="/work"

# --rm: Automatically remove the container and its associated anonymous 
#      volumes when it exits
# docker run --rm --interactive \
#     --user $(id -u):$(id -g)  \
#     --workdir ${DOCKER_VOLUME_WORK} \
#     --volume "${PWD}:${DOCKER_VOLUME_CONFIG}" \
#     --volume "${PWD}/poc:${DOCKER_VOLUME_WORK}" \
#     $DOCKER_IMAGE validate \
#         --input-spec ${DOCKER_VOLUME_CONFIG}/jellyfin-openapi-$VERSION.yaml \
#         > console-validate.log

docker run --rm --interactive  \
    --user $(id -u):$(id -g)  \
    --workdir ${DOCKER_VOLUME_WORK} \
    --volume "${PWD}:${DOCKER_VOLUME_CONFIG}" \
    --volume "${PWD}/poc:${DOCKER_VOLUME_WORK}" \
    --env-file image.ini \
    $DOCKER_IMAGE generate \
        --generator-name java \
        --output ${DOCKER_VOLUME_WORK} \
        --config ${DOCKER_VOLUME_CONFIG}/codegen.config.json \
        --input-spec ${DOCKER_VOLUME_CONFIG}/jellyfin-openapi-$VERSION.yaml \
        > console-generate.log

# docker run --rm --interactive  \
#     --user $(id -u):$(id -g)  \
#     --workdir ${DOCKER_VOLUME_WORK} \
#     --volume "${PWD}:${DOCKER_VOLUME_CONFIG}" \
#     --volume "${PWD}/poc:${DOCKER_VOLUME_WORK}" \
#     --env-file image.ini \
#     $DOCKER_IMAGE generate \
#         --generator-name java \
#         --config ${DOCKER_VOLUME_CONFIG}/codegen.config.json \
#         > console-generate.log