#!/usr/bin/bash
clear

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

DOCKER_IMAGE=openapitools/openapi-generator-cli:v7.12.0
DOCKER_VOLUME_CONFIG="/config"
DOCKER_VOLUME_WORK="/config"
# --rm: Automatically remove the container and its associated anonymous 
#      volumes when it exits
docker run --rm --interactive \
    --workdir ${DOCKER_VOLUME_WORK} \
    --volume "${PWD}:${DOCKER_VOLUME_CONFIG}" \
    --volume "${PWD}/poc:${DOCKER_VOLUME_WORK}" \
    $DOCKER_IMAGE validate --input-spec ${DOCKER_VOLUME_CONFIG}/jellyfin-openapi-$VERSION.yaml
docker run --rm --interactive  \
    --workdir ${DOCKER_VOLUME} \
    -volume "${PWD}:${DOCKER_VOLUME_CONFIG}" \
    --volume "${PWD}/poc:${DOCKER_VOLUME_WORK}" \
    $DOCKER_IMAGE generate --config openapitools.json  --generator-name java --input-spec ${DOCKER_VOLUME_CONFIG}/jellyfin-openapi-$VERSION.yaml