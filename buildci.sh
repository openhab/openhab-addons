#!/bin/bash

set -o pipefail # exit build with error when pipes fail

function prevent_timeout() {
    local i=0
    while [[ -e /proc/$1 ]]; do
        # print zero width char every 3 minutes while building
        if [[ "$i" -eq "180" ]]; then printf %b '\u200b'; i=0; else i=$((i+1)); fi
        sleep 1
    done
}

function print_reactor_summary() {
    sed -ne '/\[INFO\] Reactor Summary.*:/,$ p' "$1" | sed 's/\[INFO\] //'
}

function mvnp() {
    local command=(mvn $@)
    exec "${command[@]}" 2>&1 | # execute, redirect stderr to stdout
	stdbuf -o0 grep -vE "Download(ed|ing) from [a-z.]+: https:" | # filter out downloads
        tee .build.log | # write output to log
        stdbuf -oL grep -aE '^\[INFO\] Building .+ \[.+\]$' | # filter progress
        stdbuf -o0 sed -uE 's/^\[INFO\] Building (.*[^ ])[ ]+\[([0-9]+\/[0-9]+)\]$/\2| \1/' | # prefix project name with progress
        stdbuf -o0 sed -e :a -e 's/^.\{1,6\}|/ &/;ta' & # right align progress with padding
    local pid=$!
    prevent_timeout ${pid} &
    wait ${pid}
}

COMMITS=${1:-"master...HEAD"}

# Determine if this is a single changed addon -> Perform build with tests + integration tests and all SAT checks
CHANGED_BUNDLE_DIR=`git diff --dirstat=files,0 ${COMMITS} bundles/ | sed 's/^[ 0-9.]\+% bundles\///g' | grep -o -P "^([^/]*)" | uniq`
# Determine if this is a single changed itest -> Perform build with tests + integration tests and all SAT checks
# for this we have to remove '.tests' from the folder name.
CHANGED_ITEST_DIR=`git diff --dirstat=files,0 ${COMMITS} itests/ | sed 's/^[ 0-9.]\+% itests\///g' | sed 's/\.tests\///g' | uniq`
CDIR=`pwd`

# if a bundle and (optionally the linked itests) where changed build the module and its tests
if [[ ! -z "$CHANGED_BUNDLE_DIR"  && -e "bundles/$CHANGED_BUNDLE_DIR"  && ( "$CHANGED_BUNDLE_DIR" == "$CHANGED_ITEST_DIR" || -z "$CHANGED_ITEST_DIR" ) ]]; then
    CHANGED_DIR="$CHANGED_BUNDLE_DIR"
fi

# if no bundle was changed but only itests
if [[ -z "$CHANGED_BUNDLE_DIR" ]] && [[ -e "bundles/$CHANGED_ITEST_DIR" ]]; then
   CHANGED_DIR="$CHANGED_ITEST_DIR"
fi

if [[ ! -z "$CHANGED_DIR" ]] && [[ -e "bundles/$CHANGED_DIR" ]]; then
    echo "Single addon pull request: Building $CHANGED_DIR"
    echo "MAVEN_OPTS='-Xms1g -Xmx2g -Dorg.slf4j.simpleLogger.log.org.openhab.tools.analysis.report.ReportUtility=DEBUG -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN'" > ~/.mavenrc
    cd "bundles/$CHANGED_DIR"
    mvn clean install -B 2>&1 |
	    stdbuf -o0 grep -vE "Download(ed|ing) from [a-z.]+: https:" | # Filter out Download(s)
	    stdbuf -o0 grep -v "target/code-analysis" | # filter out some debug code from reporting utility
	    tee ${CDIR}/.build.log
    if [[ $? -ne 0 ]]; then
        exit 1
    fi

    # add the postfix to make sure we actually find the correct itest
    if [[ -e "../itests/$CHANGED_DIR.tests" ]]; then
        echo "Single addon pull request: Building itest $CHANGED_DIR"
        cd "../itests/$CHANGED_DIR.tests"
        mvn clean install -B 2>&1 |
	      stdbuf -o0 grep -vE "Download(ed|ing) from [a-z.]+: https:" | # Filter out Download(s)
	      stdbuf -o0 grep -v "target/code-analysis" | # filter out some debug code from reporting utility
	      tee -a ${CDIR}/.build.log
        if [[ $? -ne 0 ]]; then
            exit 1
        fi
    fi
else
    echo "Build all"
    echo "MAVEN_OPTS='-Xms1g -Xmx2g -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'" > ~/.mavenrc
    mvnp clean install -B -DskipChecks=true
    if [[ $? -eq 0 ]]; then
      print_reactor_summary .build.log
    else
      tail -n 1000 .build.log
      exit 1
    fi
fi
