#! /bin/bash

# Create the Android environment needed by run-tests.py
set -e
ANDROID_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. $ANDROID_DIR/config.sh
cd $ANDROID_DIR

# Download the android emulator and adb
if [ ! -d adt ]; then
  wget -c https://dl.google.com/android/adt/adt-bundle-linux-$(uname -p)-20131030.zip -O adt.zip
  unzip adt.zip > /dev/null
  ls -l adt*
  mv adt-bundle-linux-* adt
fi
export ADB="$ANDROID_DIR/adt/sdk/platform-tools/adb -e"
export EMULATOR=$ANDROID_DIR/adt/sdk/tools/emulator
export AVD=$ANDROID_DIR/adt/sdk/tools/android

# Create an Android Virtual Device
if [ ! -e avd ]; then
  echo "" | $AVD --verbose create avd -n Android-Chrome --target 1 --force --path avd
fi

# See if the emulator is running?  The emulator occasionally segfaults when
# starting, so we try this in a loop.
while true; do
  EMULATOR_RUNNING=false
  if [ -e $EMULATOR_PIDFILE ]; then
    EMULATOR_PID=$(cat $EMULATOR_PIDFILE)
    if kill -0 $EMULATOR_PID ; then
      EMULATOR_RUNNING=true
    else
      rm $EMULATOR_PIDFILE
    fi
  fi
  if $EMULATOR_RUNNING ; then
    echo "Android emulator running at $EMULATOR_PID"
  else
    # The emulator needs an xserver, start xvfb if none are running.
    if [ "x$DISPLAY" == x ]; then
      export DISPLAY=:99.0
      # xvfb must use 24bits otherwise you get a "failed to create drawable" error
      # from the emulator. See the following bug for more information
      # https://bugs.freedesktop.org/show_bug.cgi?id=62742
      /sbin/start-stop-daemon --start --quiet --pidfile $XVFB_PIDFILE --make-pidfile --background --exec \
        /usr/bin/Xvfb -- :99 +extension GLX -ac -screen 0 1280x1024x24
      sleep 15
    fi

    $ADB kill-server
    $ADB start-server
    if [ $($ADB devices | wc -l) -gt 2 ]; then
      echo "Multiple Android devices found, bailing."
      exit 1
    fi
    # "-no-snapshot-save -wipe-data" is needed so not state is saved.
    # "-gpu on" is needed as Chrome segfaults without a GPU.
    /sbin/start-stop-daemon --verbose --start --quiet --pidfile $EMULATOR_PIDFILE --make-pidfile --background --exec /bin/bash -- -c \
      "exec $EMULATOR -verbose -gpu on -no-audio -no-boot-anim -partition-size 1024 -no-snapshot-save -wipe-data @Android-Chrome >> $OLD_PWD/emulator.log 2>&1"

    # The emulator crashes if you access it too fast :/
    sleep 5
    continue
  fi

  $ADB wait-for-device shell true

  BOOTED=$($ADB shell getprop sys.boot_completed | sed -e's/[^0-9]*//g')
  BOOTANIM=$($ADB shell getprop init.svc.bootanim | sed -e's/[^a-zA-Z]*//g')
  echo "Waiting for emulator to boot... Booted? $BOOTED Animation? $BOOTANIM"

  if [ x$BOOTED == x1 -a x$BOOTANIM == xstopped ]; then

    # Make localhost refer to the host machine, not the emulator.
    # See http://developer.android.com/tools/devices/emulator.html#emulatornetworking
    echo "Redirecting localhost"
    $ADB shell mount -o remount,rw -t yaffs2 /dev/block/mtdblock0 /system
    $ADB shell echo "10.0.2.2 localhost" \> /etc/hosts

    break
  else
    sleep 5
  fi
done

# Download and install the chrome apk.
if [ -e Chrome.apk ]; then
  CHROME_APK=$ANDROID_DIR/Chrome.apk
  CHROME_APP=com.google.android.apps.chrome
  CHROME_ACT=.Main
else
  if [ ! -e chrome-android/apks/ChromeShell.apk ]; then
    LATEST_APK=`curl -s http://commondatastorage.googleapis.com/chromium-browser-continuous/Android/LAST_CHANGE`
    REMOTE_APK=http://commondatastorage.googleapis.com/chromium-browser-continuous/Android/$LATEST_APK/chrome-android.zip
    wget -c $REMOTE_APK
    unzip chrome-android.zip
  fi
  CHROME_APK=$ANDROID_DIR/chrome-android/apks/ChromeShell.apk
  CHROME_APP=org.chromium.chrome.shell
  CHROME_ACT=.ChromeShellActivity
fi

function start_chrome () {
  $ADB shell am start -a android.intent.action.MAIN -n $CHROME_APP/$CHROME_ACT -W
}

if start_chrome | grep -q "does not exist"; then
  $ADB install $CHROME_APK
fi

# Check the chrome binary actually starts without segfaulting
start_chrome
sleep 2
if start_chrome | grep -q "its current task has been brought to the front"; then
  echo "Chrome seems to have started okay."
else
  echo "Chrome seems to have crashed!"
  $ADB shell dumpsys activity # FIXME: Need to grep for running Chrome.
  exit 1
fi


# Download and start the chromedriver binary
if [ ! -e chromedriver ]; then
  # TODO: Use the latest release of chromedriver instead of this custom build once version 3.0 comes out.
  # LATEST_CHROMEDRIVER=`curl -s http://chromedriver.storage.googleapis.com/LATEST_RELEASE`
  # wget -c http://chromedriver.storage.googleapis.com/$LATEST_CHROMEDRIVER/chromedriver_linux64.zip -O chromedriver.zip
  # unzip chromedriver.zip

  # This version of chromedriver was build from the Chromium repository at r262639 on 2014-04-09.
  wget https://googledrive.com/host/0B6C-LL9qmW-IYVFrdURCMHZlM1U -O chromedriver
  chmod 0755 chromedriver
fi

CHROMEDRIVER_NOTRUNNING=true
if [ -e $CHROMEDRIVER_PIDFILE ]; then
  CHROMEDRIVER_PID=$(cat $CHROMEDRIVER_PIDFILE)
  if kill -0 $CHROMEDRIVER_PID ; then
    echo "Chrome Driver running at $CHROMEDRIVER_PID"
    CHROMEDRIVER_NOTRUNNING=false
  else
    rm $CHROMEDRIVER_PIDFILE
  fi
fi
if $CHROMEDRIVER_NOTRUNNING; then
  /sbin/start-stop-daemon --verbose --start --quiet --pidfile $CHROMEDRIVER_PIDFILE --make-pidfile --background --exec \
    $ANDROID_DIR/chromedriver
  sleep 5
fi

cd $OLD_PWD
