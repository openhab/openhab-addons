#!/usr/bin/bash

if [[ "${1:-}" == "--debug" ]]; then
    set -x
fi

set -euo pipefail

# INFO:
# If you generate the API on a windows system using git bash some additional software is required:
#
# 1. Install required tools via winget:
# > winget install GNU.Wget2 --silent
# > winget install MikeFarah.yq --silent
# > winget install jqlang.jq --silent
# > winget install Microsoft.OpenJDK.21 --silent
#
# 2. Create shim scripts in ~/bin/ (add ~/bin to PATH in ~/.bashrc if needed).
#    These shims wrap the Windows .exe tools so they work with Unix-style paths
#    (MSYS_NO_PATHCONV=1 prevents Git Bash from converting paths in Docker args,
#    but also causes Windows .exe tools to receive paths they cannot open).
#
#    ~/bin/wget  - wraps wget2.exe, converting --output-document= path
#    ~/bin/yq    - wraps yq.exe, converting Unix-style path arguments
#    ~/bin/jq    - wraps jq.exe, converting Unix-style path arguments
#    ~/bin/mvn   - wraps the repo's ./mvnw, dynamically located by walking up
#                  from $PWD; also sets JAVA_HOME to JDK 21 to avoid SSL
#                  certificate failures that occur with older JRE 8 builds.
#
#    See the session notes or project wiki for the exact shim contents.

# improve compatibility with windows systems (using git bash)
export MSYS_NO_PATHCONV=1
if ! type wget &>/dev/null; then
    alias wget=wget2
fi

REQUIRED=("wget" "yq" "docker" "curl" "jq" "awk" "sed" "grep" "sort" "tail" "mvn" "docker" "npm" "python3" "patch")

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
ROOT=$(pwd)


# Get the latest stable release tag from GitHub for openapi-generator
LATEST_OPENAPI_GENERATOR_CLI_TAG=$(curl -s "https://api.github.com/repos/OpenAPITools/openapi-generator/releases/latest" |
    jq -r '.tag_name' |
    sed 's/^v//')

echo -e "ℹ️  - Latest openapi-generator-cli version: \033[1m$LATEST_OPENAPI_GENERATOR_CLI_TAG\033[0m"

TEMPLATE_DIR="tools/generate-sources/scripts/templates"
PATCHED_POJO_TEMPLATE="${ROOT}/${TEMPLATE_DIR}/pojo.mustache"
POJO_TEMPLATE_PATCH="${ROOT}/${TEMPLATE_DIR}/pojo.mustache.patch"
UPSTREAM_POJO_TEMPLATE_URL="https://raw.githubusercontent.com/OpenAPITools/openapi-generator/v${LATEST_OPENAPI_GENERATOR_CLI_TAG}/modules/openapi-generator/src/main/resources/Java/pojo.mustache"

echo "ℹ️  - Fetch upstream Java pojo.mustache template for ${LATEST_OPENAPI_GENERATOR_CLI_TAG}"
curl -fsSL "${UPSTREAM_POJO_TEMPLATE_URL}" > "${PATCHED_POJO_TEMPLATE}"

echo "ℹ️  - Apply local pojo.mustache patch"
if ! patch -s -u "${PATCHED_POJO_TEMPLATE}" "${POJO_TEMPLATE_PATCH}"; then
    echo "  ❌ Error: Failed to apply ${POJO_TEMPLATE_PATCH} to downloaded pojo.mustache"
    exit 1
fi

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
VERSIONS=("10.11.6")
VERSION_ALIAS=("current")

DOCKER_VOLUME_WORK="/work"

OPENAPI_SPECIFICATION_DIR="tools/generate-sources/scripts/specifications"

OUTPUT=$(mktemp -d "${ROOT}/.gen-temp.XXXXXX")
THIRD_PARTY_OUTPUT_DIR="src/3rdparty/java"

INDEX=0
for i in "${VERSIONS[@]}"; do
    ALIAS=${VERSION_ALIAS[INDEX++]}
    PACKAGE_BASE=org.openhab.binding.jellyfin.internal.gen.${ALIAS}
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
    # Jellyfin API versions 10.11.3+ (including 10.11.6) incorrectly define TranscodeReasons with
    # both an inline enum AND type:array with $ref, which confuses the OpenAPI generator
    # causing it to generate broken code referencing a non-existent TranscodeReasonsEnum type.
    # Older versions (10.8.13, 10.10.7) don't have this problem and will skip this fix.
    FILENAME_YAML_INPUT="${FILENAME_YAML}"
    FILENAME_YAML_FIXED="${FILENAME_YAML}.fixed"

    HAS_ENUM=$(yq '.components.schemas.TranscodingInfo.properties.TranscodeReasons | has("enum")' ${FILENAME_YAML})
    HAS_ARRAY_TYPE=$(yq '.components.schemas.TranscodingInfo.properties.TranscodeReasons.type == "array"' ${FILENAME_YAML})

    if [ "$HAS_ENUM" = "true" ] && [ "$HAS_ARRAY_TYPE" = "true" ]; then
        echo ""
        echo "🔧 fix malformed TranscodeReasons schema (has both enum and type:array)"
        # Remove the inline enum definition, keeping only the array type with $ref
        yq 'del(.components.schemas.TranscodingInfo.properties.TranscodeReasons.enum)' ${FILENAME_YAML} > ${FILENAME_YAML_FIXED}
        FILENAME_YAML_INPUT="${FILENAME_YAML_FIXED}"
    fi

    # Preprocess: sort all enum arrays in the OpenAPI YAML input
    # This keeps existing TranscodeReasons fix behavior (we sort the selected input)
    # Exception list: schema symbols that MUST keep original enum order (do not sort)
    ENUM_SORT_EXCEPTIONS="#sym:DayOfWeek,#sym:DynamicDayOfWeek,#sym:ImageResolution,#sym:LogLevel"
    FILENAME_YAML_SORTED="${FILENAME_YAML}.sorted"
    echo "🔧 sort all enum arrays in OpenAPI YAML input"
    # Always use dedicated Python helper to sort enums, passing exception args
    PY_SCRIPT="tools/generate-sources/scripts/sort_openapi_enums.py"
    # Split comma-separated ENUM_SORT_EXCEPTIONS into multiple --exception args
    PY_ARGS=("${FILENAME_YAML_INPUT}" "${FILENAME_YAML_SORTED}")
    IFS=',' read -ra EXARR <<< "${ENUM_SORT_EXCEPTIONS}"
    for ex in "${EXARR[@]}"; do
        ex_trimmed=$(echo "${ex}" | awk '{gsub(/^ +| +$/,"",$0); print $0}')
        if [ -n "${ex_trimmed}" ]; then
            PY_ARGS+=("--exception" "${ex_trimmed}")
        fi
    done
    python3 "${PY_SCRIPT}" "${PY_ARGS[@]}"
    FILENAME_YAML_INPUT="${FILENAME_YAML_SORTED}"

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
        --input-spec ${OPENAPI_SPECIFICATION_DIR}/yaml/$(basename ${FILENAME_YAML_INPUT}) -o ${DOCKER_VOLUME_WORK}/$(realpath --relative-to=${ROOT} ${OUTPUT}) \
        >/dev/null

    # Clean up the temporary fixed file if it was created
    if [ -f "${FILENAME_YAML_FIXED}" ]; then
        rm ${FILENAME_YAML_FIXED}
    fi
    # Clean up the temporary sorted file if it was created
    if [ -f "${FILENAME_YAML_SORTED}" ]; then
        rm ${FILENAME_YAML_SORTED}
    fi

    # Move generated sources from temp dir to src/3rdparty/java
    GENERATED_SRC="${OUTPUT}/src/main/java/org/openhab/binding/jellyfin/internal/gen/${ALIAS}"
    TARGET_SRC="${ROOT}/${THIRD_PARTY_OUTPUT_DIR}/org/openhab/binding/jellyfin/internal/gen/${ALIAS}"
    rm -rf "${TARGET_SRC}"
    mkdir -p "$(dirname ${TARGET_SRC})"
    mv "${GENERATED_SRC}" "${TARGET_SRC}"
done

# Move shared infrastructure files from gen root (invoker package level)
# These are produced once by the generator and shared across all API versions
GEN_ROOT_SRC="${OUTPUT}/src/main/java/org/openhab/binding/jellyfin/internal/gen"
GEN_ROOT_TARGET="${ROOT}/${THIRD_PARTY_OUTPUT_DIR}/org/openhab/binding/jellyfin/internal/gen"
find "${GEN_ROOT_SRC}" -maxdepth 1 -name "*.java" -type f | while read f; do
    mv "${f}" "${GEN_ROOT_TARGET}/"
done

# Clean up temp output dir
rm -rf "${OUTPUT}"

cd ${ROOT}

MVN_OPT="--quiet"

echo "🔧 fix ServerVariable.enumValues initialization to avoid NullPointerException"
# The upstream generator leaves enumValues uninitialized (null). ServerConfiguration.URL() calls
# enumValues.size() which would throw NPE for non-enum variables. Initialize to empty HashSet
# and add a null-guard in the constructor so re-generation does not reintroduce the bug.
sed -i 's/public HashSet<String> enumValues = null;/public HashSet<String> enumValues = new HashSet<>();/' "${GEN_ROOT_TARGET}/ServerVariable.java"
sed -i 's/this\.enumValues = enumValues;/this.enumValues = enumValues != null ? enumValues : new HashSet<>();/' "${GEN_ROOT_TARGET}/ServerVariable.java"

echo "🔧 fix ApiResponse Javadoc typo: 'response bod' -> 'response body'"
# The upstream generator template contains a truncated word in the @param data Javadoc line.
sed -i 's/response bod\b/response body/' "${GEN_ROOT_TARGET}/ApiResponse.java"

echo "🔧 fix @NonNull annotations on fields to @Nullable (builder pattern compatibility)"
# Replace @NonNull with @Nullable on field declarations in model classes
# This fixes compilation errors where fields are marked @NonNull but not initialized in default constructor
find ${THIRD_PARTY_OUTPUT_DIR}/org/openhab/binding/jellyfin/internal/gen -name "*.java" -type f -exec sed -i 's/^\([[:space:]]*\)@org\.eclipse\.jdt\.annotation\.NonNull\([[:space:]]*\)$/\1@org.eclipse.jdt.annotation.Nullable\2/g' {} \;

echo "🔧 remove @NonNullByDefault from generated classes (covered by package-info.java)"
# Remove @NonNullByDefault annotation and import from all generated classes
# Null-safety is intentionally disabled for generated thirdparty API code via package-info.java
find ${THIRD_PARTY_OUTPUT_DIR}/org/openhab/binding/jellyfin/internal/gen -name "*.java" -type f -exec sed -i '/@NonNullByDefault/d' {} \;
find ${THIRD_PARTY_OUTPUT_DIR}/org/openhab/binding/jellyfin/internal/gen -name "*.java" -type f -exec sed -i '/import org\.eclipse\.jdt\.annotation\.NonNullByDefault;/d' {} \;

echo "🔧 remove redundant annotations from generated model classes"
# - @JsonInclude(USE_DEFAULTS): no-op when no class-level @JsonInclude is present; ALWAYS fields are preserved.
# - @JsonProperty required=false: redundant as false is the Jackson default.
find ${THIRD_PARTY_OUTPUT_DIR}/org/openhab/binding/jellyfin/internal/gen -name "*.java" -type f -exec sed -i -e '/@JsonInclude(value = JsonInclude\.Include\.USE_DEFAULTS)/d' -e 's/, required = false//' {} \;
# Remove the JsonInclude import from files that no longer reference it at all (ALWAYS-annotated files keep theirs)
find ${THIRD_PARTY_OUTPUT_DIR}/org/openhab/binding/jellyfin/internal/gen -name "*.java" -type f | while read f; do
    if ! grep -q "JsonInclude" "$f"; then
        sed -i '/import com\.fasterxml\.jackson\.annotation\.JsonInclude;/d' "$f"
    fi
done

echo "🔧 guard url.replace calls in ServerConfiguration to avoid nullness mismatch"
# Ensure generated ServerConfiguration calls to url.replace are null-guarded to satisfy static null analysis
if [ -f "${GEN_ROOT_TARGET}/ServerConfiguration.java" ]; then
    # Replace return url.replace(...) with a null-guarded ternary and balance parentheses
    sed -i 's/return[[:space:]]\+url\.replace/return (url == null ? null : url.replace/g' "${GEN_ROOT_TARGET}/ServerConfiguration.java"
    # For lines we modified above, the closing ');' must become '));' to close the ternary
    sed -i '/url\.replace/ s/);/));/' "${GEN_ROOT_TARGET}/ServerConfiguration.java"
fi

# Ensure any non-return assignments using the pattern are also balanced, e.g.:
# url = (url == null ? null : url.replace(...);
# -> url = (url == null ? null : url.replace(...));
if [ -f "${GEN_ROOT_TARGET}/ServerConfiguration.java" ]; then
    sed -i '/(url == null ? null : url.replace/ s/;$/);/' "${GEN_ROOT_TARGET}/ServerConfiguration.java"
fi

# Normalize any remaining url.replace lines to a canonical null-guarded form
if [ -f "${GEN_ROOT_TARGET}/ServerConfiguration.java" ]; then
    sed -i '/url\.replace/ c\            url = (url == null ? null : url.replace("{" + name + "}", value != null ? value : ""));' "${GEN_ROOT_TARGET}/ServerConfiguration.java"
fi
echo ""
echo "🧹 apply formatting to generated code"
mvn spotless:apply $MVN_OPT
