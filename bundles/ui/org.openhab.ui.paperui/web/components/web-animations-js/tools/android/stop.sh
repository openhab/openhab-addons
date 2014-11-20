#! /bin/bash

# Stop all the stuff started by setup.sh
set -x
set -e
ANDROID_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. $ANDROID_DIR/config.sh
cd $ANDROID_DIR

for SERVER in $SERVERS; do
  PIDFILE_VAR=${SERVER}_PIDFILE
  PIDFILE=${!PIDFILE_VAR}
  echo "$SERVER $PIDFILE"
  if [ -e $PIDFILE ]; then
    echo "Stopping $SERVER at $(cat $PIDFILE)"
    /sbin/start-stop-daemon --stop --pidfile $PIDFILE --retry TERM/30/KILL/5 || rm $PIDFILE
  fi
done

cd $OLD_PWD
