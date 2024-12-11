#!/usr/bin/bash
clear

CLI=2.2.1

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


# http://nuc.ehrendingen:8096/api-docs/openapi.json
# OPENAPI=openapi-jellyfin-10.10.3.json

# library
#     library template (sub-template) to use:
#     jersey1 - HTTP client: Jersey client 1.18. JSON processing: Jackson 2.4.2
#     jersey2 - HTTP client: Jersey client 2.6
#     feign - HTTP client: Netflix Feign 8.1.1.  JSON processing: Jackson 2.6.3
#     okhttp-gson (default) - HTTP client: OkHttp 2.4.0. JSON processing: Gson 2.3.1
#     retrofit - HTTP client: OkHttp 2.4.0. JSON processing: Gson 2.3.1 (Retrofit 1.9.0)
#     retrofit2 - HTTP client: OkHttp 2.5.0. JSON processing: Gson 2.4 (Retrofit 2.0.0-beta2)
#     google-api-client - HTTP client: google-api-client 1.23.0. JSON processing: Jackson 2.8.9
#     rest-assured - HTTP client: rest-assured : 3.1.0. JSON processing: Gson 2.6.1. Only for Java8

# java -Xmx8G -jar swagger-codegen-cli-${CLI}.jar generate \ 
#   -i $OPENAPI 
#   -l java
#   -c config.json
#   -o ~Temp/jellyfin/api/$VERSION 