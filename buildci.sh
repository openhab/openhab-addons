#!/bin/bash

function prevent_timeout() {
    local i=0
    while [ -e /proc/$1 ]; do
        # print zero width char every 3 minutes while building
        if [ "$i" -eq "180" ]; then printf %b '\u200b'; i=0; else i=$((i+1)); fi
        sleep 1
    done
}

function print_reactor_summary() {
    sed -ne '/\[INFO\] Reactor Summary:/,$ p' "$1" | sed 's/\[INFO\] //'
}

function mvnp() {
    set -o pipefail # exit build with error when pipes fail
    local command=(mvn $@)
    exec "${command[@]}" 2>&1 | # execute, redirect stderr to stdout
	stdbuf -o0 grep -vE "Download(ed|ing) from [a-z.]+: https:" | # filter out downloads
        tee .build.log | # write output to log
        stdbuf -oL grep -E '^\[INFO\] Building .+ \[.+\]$' | # filter progress
        stdbuf -o0 sed -uE 's/^\[INFO\] Building (.*[^ ])[ ]+\[([0-9]+\/[0-9]+)\]$/\2| \1/' | # prefix project name with progress
        stdbuf -o0 sed -e :a -e 's/^.\{1,6\}|/ &/;ta' & # right align progress with padding
    local pid=$!
    prevent_timeout $pid &
    wait $pid
}

# Determine if this is a new addon -> Perform tests + integration tests and all SAT checks with increased warning level
CHANGED_DIR=`git diff --diff-filter=A --dirstat=files,0 master...HEAD bundles/ | sed 's/^[ 0-9.]\+% bundles\///g' | grep -o -P "^([^/]*)" | uniq`
CDIR=`pwd`

if [ ! -z "$CHANGED_DIR" ] && [ -e "bundles/$CHANGED_DIR" ]; then
    echo "New addon pull request: Building $CHANGED_DIR"
    echo "MAVEN_OPTS='-Xms1g -Xmx2g -Dorg.slf4j.simpleLogger.log.org.openhab.tools.analysis.report.ReportUtility=DEBUG -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN'" > ~/.mavenrc
    cd "bundles/$CHANGED_DIR"
    mvn clean install -B 2>&1 | 
	    stdbuf -o0 grep -vE "Download(ed|ing) from [a-z.]+: https:" | # Filter out Download(s)
	    stdbuf -o0 grep -v "target/code-analysis" | # filter out some debug code from reporting utility
	    tee $CDIR/.build.log
    if [ -e "../itests/$CHANGED_DIR" ]; then
      echo "New addon pull request: Building itest $CHANGED_DIR"
      cd "../itests/$CHANGED_DIR"
      mvn clean install -B 2>&1 | 
	      stdbuf -o0 grep -vE "Download(ed|ing) from [a-z.]+: https:" | # Filter out Download(s)
	      stdbuf -o0 grep -v "target/code-analysis" | # filter out some debug code from reporting utility
	      tee -a $CDIR/.build.log
    fi
else
    echo "Build all"
    echo "MAVEN_OPTS='-Xms1g -Xmx2g -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'" > ~/.mavenrc
    mvnp clean install -B -DskipChecks=true -DskipTests=true
    if [ $? -eq 0 ]; then
      print_reactor_summary .build.log
    else
      tail -n 1000 .build.log
    fi
fi
