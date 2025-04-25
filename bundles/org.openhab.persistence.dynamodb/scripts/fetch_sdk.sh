#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Copy these to pom.xml"
echo ""
for dependency in $(mvn -f "$DIR/fetch_sdk_pom.xml" clean process-sources dependency:tree|grep -E ':(test|compile)$' | grep -o '[[:lower:]].*'|sort); do
    readarray -d : -t components <<< "$dependency"
    scope_without_newline="$(echo "${components[4]}"|tr -d '\n')"
    cat << EOF
    <dependency>
      <groupId>${components[0]}</groupId>
      <artifactId>${components[1]}</artifactId>
      <version>${components[3]}</version>
EOF
    if [[ "${components[2]}" != "jar" ]]; then
        echo "      <type>${components[2]}</type>"
    fi
    if [[ "${scope_without_newline}" != "compile" ]]; then
        echo "      <scope>${scope_without_newline}</scope>"
    fi
    cat << EOF    
    </dependency>
EOF

done

