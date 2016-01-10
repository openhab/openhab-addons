#!/bin/bash

#
# Define some functions first
#

log() {
  echo "${@}"
}

log_err() {
  echo "${@}" 2>&1
}

cleanup() {
  if [ -n "${COMMIT_ID_WC}" ]; then
    log "restore working copy..."
    git reset --hard "${COMMIT_ID_WC}"
  fi
}

die() {
  if [ ${#} -gt 0 ]; then
    log_err "${@}"
  fi
  cleanup
  exit 1
}

get_abs() {
  local ARG_PATH="${1}"; shift

  if [ ! -e "${ARG_PATH}" ]; then
    log_err "Cannot resolve path (${ARG_PATH})"
    return 1
  fi

  if [ -d "${ARG_PATH}" ]; then
    cd "${ARG_PATH}"
    echo "${PWD}"
    cd "${OLDPWD}"
  else
    local DIRNAME="$(dirname "${ARG_PATH}")"
    local BASENAME="$(basename "${ARG_PATH}")"
    cd "${DIRNAME}"
    echo "${PWD}/${BASENAME}"
    cd "${OLDPWD}"
  fi
}

#
# Begin
#

MY_CMD="${0}"
MY_CMD_ABS="$(get_abs "${MY_CMD}")" || die "Cannot resolve path"
MY_DIRNAME_ABS="$(dirname "${MY_CMD_ABS}")"

REMOTE=origin
unset COMMIT_ID_WC

#
# Parse command line arguments
#
VERSION_POSTFIX="${1}"; shift
COMMIT_ID="${1}"; shift

if [ -z "${VERSION_POSTFIX}" ]; then
  die "version postfix missing"
fi

if [ -z "${COMMIT_ID}" ]; then
  die "commit id missing"
fi

#
# Give the information to the user and wait for accept.
#

log "version postfix: ${VERSION_POSTFIX}"
log "commit id: ${COMMIT_ID}"

log "Enter 'Y' to proceed"
read PROCEED
if [ x"${PROCEED}" != x"Y" ]; then
  exit 1
fi

# Clone only if necessary
#if [ ! -d "${REPO_DIR}"/.git ]; then
#  git clone -o "${REMOTE}" "git@github.com:eclipse/smarthome.git"
#fi
# Goto git clone
#cd "${REPO_DIR}"
cd "${MY_DIRNAME_ABS}"/../.. || die "Cannot enter working copy's root directory of the repository"

#
# Check working copy
#
GIT_STATUS="$(git status -s)"

if [ -n "${GIT_STATUS}" ]; then
  log "${GIT_STATUS}"
  die "Your working copy is not clean"
fi

# Store commit ID of current working copy
COMMIT_ID_WC="$(git rev-parse HEAD)"
log "To restore your working copy (if script does not finish correctly), use:"
log "git reset --hard ${COMMIT_ID_WC}"

# Fetch all from repos
git fetch "${REMOTE}" || die "Cannot fetch."

# Reset current working copy to given commit id
git reset --hard "${COMMIT_ID}" || die "Reset working copy failed."

# Clean working copy
git clean -x -d -f || die "Git clean failed."

# Parse version of the current working copy
VERSION_OLD="$(cat pom.xml | grep '<version>.*</version>' | head -n1 | sed 's:<version>\(.*\)</version>:\1:g' | awk '{print $1}')"
case "${VERSION_OLD}" in
  *-SNAPSHOT) log "version old: ${VERSION_OLD}"
    ;;
  *) die "version old is no snapshot"
    ;;
esac

# Generate new version
VERSION_NEW="${VERSION_OLD%-SNAPSHOT}.${VERSION_POSTFIX}"
log "version new: ${VERSION_NEW}"

# Use tycho to set version of pom and manifest files
log "set new version using tycho"
mvn tycho-versions:set-version -DnewVersion="${VERSION_NEW}" || die "Tycho set-version failed."

# Change version that is hardcoded in some files
log "change hardcoded version string in files"
# sed 's:\("version"\: *"\).*\(".*\):\1'"${VERSION_NEW}"'\2:g' -i extensions/ui/org.eclipse.smarthome.ui.paper/bower.json

# Check if maven could build
mvn clean install || die "mvn clean install failed"

# Commit changes done in the working copy
git add . || die "git add failed"
git commit -s -m "[ref] set version to ${VERSION_NEW}" || die "git commit failed"

# Generate tag name
TAG_NAME=ref-"${VERSION_NEW}"

# Create tag
git tag "${TAG_NAME}" || die "create tag failed"

# Push tag to remote
git push --tags "${REMOTE}" "${TAG_NAME}" || die "push tag failed"

# Now, do a build with additional deploy of the artifacts
#mvn clean install deploy

# Cleanup (e.g. restore working copy)
cleanup
