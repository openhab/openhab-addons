#!/usr/bin/bash

# TODO: Use repository and tag/version to get API definition
SERVER=https://repo.jellyfin.org/releases/openapi/jellyfin-openapi-stable.json
# SERVER=http://nuc.ehrendingen:8096/api-docs/openapi.json

VERSION=$(curl -sL ${SERVER} | jq -r .info.version)

echo "ℹ️  - Using Jellyfin API: ${VERSION}"

# TODO: create input subfolder for .json/.yaml
FILENAME="./jellyfin-openapi-${VERSION}"

if [ ! -e "${FILENAME}.json" ]; then
    echo "⏬ - Downloading OPENAPI definition for Version ${VERSION}..."
    wget                                   \
        --no-verbose                       \
        --output-document=${FILENAME}.json \
        ${SERVER}
fi

# TODO: Replace Server URL in .json or .yaml with a generic one ...
# TODO: Check if .yaml exists 

# Will not work if VS Code is installed as Ubuntu SNAP packet (no permission to stdout)
yq -oy ${FILENAME}.json > "./${FILENAME}.yaml"

openapi-generator-cli generate -g java --global-property models,apis --input-spec ${FILENAME}.yaml -o src/api/${VERSION}

