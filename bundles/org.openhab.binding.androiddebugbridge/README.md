# Android Debug Bridge Binding

This binding allows to connect to android devices through the adb protocol.

The device needs to have **usb debugging enabled** and **allow debugging over tcp**, some devices allow to enable this in the device options but others need a previous connection through adb or even be rooted.

If you are not familiar with adb I suggest you to search "How to enable adb over wifi on \<device name\>" or something like that.

## Supported Things

This binding was tested on :

| Device                 | Android version | Comments                           |
|------------------------|-----------------|------------------------------------|
| Fire TV Stick          | 7.1.2           | Volume control not working         |
| Nexus5x                | 8.1.0           | Everything works nice              |
| Freebox Pop Player     | 9               | Everything works nice              |
| ChromeCast Google TV   | 12              | Volume control partially working   |

Please update this document if you tested it with other android versions to reflect the compatibility of the binding.

## Discovery

Android TV and Fire TV devices should be discovered automatically (using mDNS).

Since other Android devices cannot be discovered automatically on the network, the manual discovery scan will try to connect via adb to all reachable IP addresses in the defined range.

You could customize the discovery process through the binding options.

**Your device will prompt a message requesting you to authorize the connection, you should check the option "Always allow connections from this device" (or something similar) and accept**.

## Binding Configuration

| Config              |  Type    | description                                                                       |
|---------------------|----------|-----------------------------------------------------------------------------------|
| discoveryPort       | int      | Port used on discovery to connect to the device through adb                       |
| discoveryReachableMs| int      | Milliseconds to wait while discovering to determine if the ip is reachable        |
| discoveryIpRangeMin | int      | Used to limit the number of IPs checked while discovering                         |
| discoveryIpRangeMax | int      | Used to limit the number of IPs checked while discovering                         |

## Thing Configuration

| ThingTypeID   | Description             |
|---------------|-------------------------|
| android       | Android device          |

| Config               |  Type  | Description                                                                                                            |
|----------------------|--------|------------------------------------------------------------------------------------------------------------------------|
| ip                   | String | Device ip address.                                                                                                     |
| port                 | int    | Device port listening to adb connections. (default: 5555)                                                              |
| refreshTime          | int    | Seconds between device status refreshes. (default: 30)                                                                 |
| timeout              | int    | Command timeout in seconds. (default: 5)                                                                               |
| recordDuration       | int    | Record input duration in seconds.                                                                                      |
| deviceMaxVolume      | int    | Assumed max volume for devices with android versions that do not expose this value.                                    |
| volumeSettingKey     | String | Settings key for android versions where volume is gather using settings command. (>=android 11)                        |
| volumeStepPercent    | int    | Percent to increase/decrease volume.                                                                                   |
| mediaStateJSONConfig | String | Expects a JSON array. Allow to configure the media state detection method per app. Described in the following section. |
| maxADBTimeouts       | int    | Max ADB command consecutive timeouts to force to reset the connection.                                                 |

## Media State Detection

You can configure different modes to detect when the device is playing media depending on the current app.

The available modes are:

- idle: assert not playing, avoid command execution.
- media_state: detect play status by dumping the media_session service. This is the default for not configured apps
- audio: detect play status by dumping the audio service.
- wake_lock: detect play status by comparing the power wake lock state with the values provided in 'wakeLockPlayStates'

The configuration depends on the application, device and version used.

This is a sample of the mediaStateJSONConfig thing configuration - the `label` is optional:

```json
[
    {
        "name": "com.amazon.tv.launcher",
        "mode": "idle",
        "label": "Home"
    },
    {
        "name": "org.jellyfin.androidtv",
        "mode": "wake_lock",
        "label": "Jellyfin",
        "wakeLockPlayStates": [
            2,
            3
        ]
    },
    {
        "name": "com.amazon.firetv.youtube",
        "label": "YouTube",
        "mode": "wake_lock",
        "wakeLockPlayStates": [
            2
        ]
    },
    {
        "name": "com.netflix.ninja",
        "label": "Netflix",
        "mode": "wake_lock",
        "wakeLockPlayStates": [
            4
        ]
    }
]
```

## Record/Send input events

As the execution of key events takes a while, you can use input events as an alternative way to control your device.

They are pretty device specific, so you should use the record-input and recorded-input channels to store/send those events.

An example of what you can do:

- You can send the command `UP` to the `record-input` channel the binding will then capture the events you send through your remote for the defined recordDuration config for the thing, so press once the UP key on your remote and wait a while.
- Now that you have recorded your input, you can send the `UP` command to the `recorded-input` event and it will send the recorded event to the android device.

Please note that events could fail if the input method is removed, for example it could fail if you clone the events of a bluetooth controller and the remote goes offline. This is happening for me when recording the Fire TV remote events but not for my Xiaomi TV which also has a bt remote controller.

## Channels

| channel              | type   | description                                                                                                                   |
|----------------------|--------|-------------------------------------------------------------------------------------------------------------------------------|
| key-event            | String | Send key event to android device. Possible values listed below                                                                |
| text                 | String | Send text to android device                                                                                                   |
| tap                  | String | Send tap event to android device (format x,y)                                                                                 |
| url                  | String | Open url in browser                                                                                                           |
| media-volume         | Dimmer | Set or get media volume level on android device                                                                               |
| media-control        | Player | Control media on android device                                                                                               |
| start-intent         | String | Start application intent. Read bellow section                                                                                 |
| start-package        | String | Run application by package name. The commands for this Channel are populated dynamically based on the `mediaStateJSONConfig`. |
| stop-package         | String | Stop application by package name                                                                                              |
| stop-current-package | String | Stop current application                                                                                                      |
| current-package      | String | Package name of the top application in screen                                                                                 |
| record-input         | String | Capture events, generate the equivalent command and store it under the provided name                                          |
| recorded-input       | String | Emulates previously captured input events by name                                                                             |
| shutdown             | String | Power off/reboot device (allowed values POWER_OFF, REBOOT)                                                                    |
| awake-state          | Switch | Awake state value.                                                                                                            |
| wake-lock            | Number | Power wake lock value                                                                                                         |
| screen-state         | Switch | Screen power state                                                                                                            |

### Start Intent

This channel allows to invoke the 'am start' command, the syntax for it is:
`<package/activity>||<<arg name>> <arg value>||...`

This is a sample:
`com.netflix.ninja/.MainActivity||<a>android.intent.action.VIEW||<d>netflix://title/80025384||<f>0x10000020||<es>amzn_deeplink_data 80025384`

Not all the [arguments](https://developer.android.com/studio/command-line/adb#IntentSpec) are supported. Please open an issue or pull request if you need more.

### Available key-event values:

- KEYCODE_0
- KEYCODE_1
- KEYCODE_11
- KEYCODE_12
- KEYCODE_2
- KEYCODE_3
- KEYCODE_3D_MODE
- KEYCODE_4
- KEYCODE_5
- KEYCODE_6
- KEYCODE_7
- KEYCODE_8
- KEYCODE_9
- KEYCODE_A
- KEYCODE_ALL_APPS
- KEYCODE_ALT_LEFT
- KEYCODE_ALT_RIGHT
- KEYCODE_APOSTROPHE
- KEYCODE_APP_SWITCH
- KEYCODE_ASSIST
- KEYCODE_AT
- KEYCODE_AVR_INPUT
- KEYCODE_AVR_POWER
- KEYCODE_B
- KEYCODE_BACK
- KEYCODE_BACKSLASH
- KEYCODE_BOOKMARK
- KEYCODE_BREAK
- KEYCODE_BRIGHTNESS_DOWN
- KEYCODE_BRIGHTNESS_UP
- KEYCODE_BUTTON_1
- KEYCODE_BUTTON_10
- KEYCODE_BUTTON_11
- KEYCODE_BUTTON_12
- KEYCODE_BUTTON_13
- KEYCODE_BUTTON_14
- KEYCODE_BUTTON_15
- KEYCODE_BUTTON_16
- KEYCODE_BUTTON_2
- KEYCODE_BUTTON_3
- KEYCODE_BUTTON_4
- KEYCODE_BUTTON_5
- KEYCODE_BUTTON_6
- KEYCODE_BUTTON_7
- KEYCODE_BUTTON_8
- KEYCODE_BUTTON_9
- KEYCODE_BUTTON_A
- KEYCODE_BUTTON_B
- KEYCODE_BUTTON_C
- KEYCODE_BUTTON_L1
- KEYCODE_BUTTON_L2
- KEYCODE_BUTTON_MODE
- KEYCODE_BUTTON_R1
- KEYCODE_BUTTON_R2
- KEYCODE_BUTTON_SELECT
- KEYCODE_BUTTON_START
- KEYCODE_BUTTON_THUMBL
- KEYCODE_BUTTON_THUMBR
- KEYCODE_BUTTON_X
- KEYCODE_BUTTON_Y
- KEYCODE_BUTTON_Z
- KEYCODE_C
- KEYCODE_CALCULATOR
- KEYCODE_CALENDAR
- KEYCODE_CALL
- KEYCODE_CAMERA
- KEYCODE_CAPS_LOCK
- KEYCODE_CAPTIONS
- KEYCODE_CHANNEL_DOWN
- KEYCODE_CHANNEL_UP
- KEYCODE_CLEAR
- KEYCODE_COMMA
- KEYCODE_CONTACTS
- KEYCODE_COPY
- KEYCODE_CTRL_LEFT
- KEYCODE_CTRL_RIGHT
- KEYCODE_CUT
- KEYCODE_D
- KEYCODE_DEL
- KEYCODE_DPAD_CENTER
- KEYCODE_DPAD_DOWN
- KEYCODE_DPAD_DOWN_LEFT
- KEYCODE_DPAD_DOWN_RIGHT
- KEYCODE_DPAD_LEFT
- KEYCODE_DPAD_RIGHT
- KEYCODE_DPAD_UP
- KEYCODE_DPAD_UP_LEFT
- KEYCODE_DPAD_UP_RIGHT
- KEYCODE_DVR
- KEYCODE_E
- KEYCODE_EISU
- KEYCODE_ENDCALL
- KEYCODE_ENTER
- KEYCODE_ENVELOPE
- KEYCODE_EQUALS
- KEYCODE_ESCAPE
- KEYCODE_EXPLORER
- KEYCODE_F
- KEYCODE_F1
- KEYCODE_F10
- KEYCODE_F11
- KEYCODE_F12
- KEYCODE_F2
- KEYCODE_F3
- KEYCODE_F4
- KEYCODE_F5
- KEYCODE_F6
- KEYCODE_F7
- KEYCODE_F8
- KEYCODE_F9
- KEYCODE_FOCUS
- KEYCODE_FORWARD
- KEYCODE_FORWARD_DEL
- KEYCODE_FUNCTION
- KEYCODE_G
- KEYCODE_GRAVE
- KEYCODE_GUIDE
- KEYCODE_H
- KEYCODE_HEADSETHOOK
- KEYCODE_HELP
- KEYCODE_HENKAN
- KEYCODE_HOME
- KEYCODE_I
- KEYCODE_INFO
- KEYCODE_INSERT
- KEYCODE_J
- KEYCODE_K
- KEYCODE_KANA
- KEYCODE_KATAKANA_HIRAGANA
- KEYCODE_L
- KEYCODE_LANGUAGE_SWITCH
- KEYCODE_LAST_CHANNEL
- KEYCODE_LEFT_BRACKET
- KEYCODE_M
- KEYCODE_MANNER_MODE
- KEYCODE_MEDIA_AUDIO_TRACK
- KEYCODE_MEDIA_CLOSE
- KEYCODE_MEDIA_EJECT
- KEYCODE_MEDIA_FAST_FORWARD
- KEYCODE_MEDIA_NEXT
- KEYCODE_MEDIA_PAUSE
- KEYCODE_MEDIA_PLAY
- KEYCODE_MEDIA_PLAY_PAUSE
- KEYCODE_MEDIA_PREVIOUS
- KEYCODE_MEDIA_RECORD
- KEYCODE_MEDIA_REWIND
- KEYCODE_MEDIA_SKIP_BACKWARD
- KEYCODE_MEDIA_SKIP_FORWARD
- KEYCODE_MEDIA_STEP_BACKWARD
- KEYCODE_MEDIA_STEP_FORWARD
- KEYCODE_MEDIA_STOP
- KEYCODE_MEDIA_TOP_MENU
- KEYCODE_MENU
- KEYCODE_META_LEFT
- KEYCODE_META_RIGHT
- KEYCODE_MINUS
- KEYCODE_MOVE_END
- KEYCODE_MOVE_HOME
- KEYCODE_MUHENKAN
- KEYCODE_MUSIC
- KEYCODE_MUTE
- KEYCODE_N
- KEYCODE_NAVIGATE_IN
- KEYCODE_NAVIGATE_NEXT
- KEYCODE_NAVIGATE_OUT
- KEYCODE_NAVIGATE_PREVIOUS
- KEYCODE_NOTIFICATION
- KEYCODE_NUM
- KEYCODE_NUMPAD_0
- KEYCODE_NUMPAD_1
- KEYCODE_NUMPAD_2
- KEYCODE_NUMPAD_3
- KEYCODE_NUMPAD_4
- KEYCODE_NUMPAD_5
- KEYCODE_NUMPAD_6
- KEYCODE_NUMPAD_7
- KEYCODE_NUMPAD_8
- KEYCODE_NUMPAD_9
- KEYCODE_NUMPAD_ADD
- KEYCODE_NUMPAD_COMMA
- KEYCODE_NUMPAD_DIVIDE
- KEYCODE_NUMPAD_DOT
- KEYCODE_NUMPAD_ENTER
- KEYCODE_NUMPAD_EQUALS
- KEYCODE_NUMPAD_LEFT_PAREN
- KEYCODE_NUMPAD_MULTIPLY
- KEYCODE_NUMPAD_RIGHT_PAREN
- KEYCODE_NUMPAD_SUBTRACT
- KEYCODE_NUM_LOCK
- KEYCODE_O
- KEYCODE_P
- KEYCODE_PAGE_DOWN
- KEYCODE_PAGE_UP
- KEYCODE_PAIRING
- KEYCODE_PASTE
- KEYCODE_PERIOD
- KEYCODE_PICTSYMBOLS
- KEYCODE_PLUS
- KEYCODE_POUND
- KEYCODE_POWER
- KEYCODE_PROFILE_SWITCH
- KEYCODE_PROG_BLUE
- KEYCODE_PROG_GREEN
- KEYCODE_PROG_RED
- KEYCODE_PROG_YELLOW
- KEYCODE_Q
- KEYCODE_R
- KEYCODE_REFRESH
- KEYCODE_RIGHT_BRACKET
- KEYCODE_RO
- KEYCODE_S
- KEYCODE_SCROLL_LOCK
- KEYCODE_SEARCH
- KEYCODE_SEMICOLON
- KEYCODE_SETTINGS
- KEYCODE_SHIFT_LEFT
- KEYCODE_SHIFT_RIGHT
- KEYCODE_SLASH
- KEYCODE_SLEEP
- KEYCODE_SOFT_LEFT
- KEYCODE_SOFT_RIGHT
- KEYCODE_SOFT_SLEEP
- KEYCODE_SPACE
- KEYCODE_STAR
- KEYCODE_STB_INPUT
- KEYCODE_STB_POWER
- KEYCODE_STEM_1
- KEYCODE_STEM_2
- KEYCODE_STEM_3
- KEYCODE_STEM_PRIMARY
- KEYCODE_SWITCH_CHARSET
- KEYCODE_SYM
- KEYCODE_SYSRQ
- KEYCODE_SYSTEM_NAVIGATION_DOWN
- KEYCODE_SYSTEM_NAVIGATION_LEFT
- KEYCODE_SYSTEM_NAVIGATION_RIGHT
- KEYCODE_SYSTEM_NAVIGATION_UP
- KEYCODE_T
- KEYCODE_TAB
- KEYCODE_THUMBS_DOWN
- KEYCODE_THUMBS_UP
- KEYCODE_TV
- KEYCODE_TV_ANTENNA_CABLE
- KEYCODE_TV_AUDIO_DESCRIPTION
- KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN
- KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP
- KEYCODE_TV_CONTENTS_MENU
- KEYCODE_TV_DATA_SERVICE
- KEYCODE_TV_INPUT
- KEYCODE_TV_INPUT_COMPONENT_1
- KEYCODE_TV_INPUT_COMPONENT_2
- KEYCODE_TV_INPUT_COMPOSITE_1
- KEYCODE_TV_INPUT_COMPOSITE_2
- KEYCODE_TV_INPUT_HDMI_1
- KEYCODE_TV_INPUT_HDMI_2
- KEYCODE_TV_INPUT_HDMI_3
- KEYCODE_TV_INPUT_HDMI_4
- KEYCODE_TV_INPUT_VGA_1
- KEYCODE_TV_MEDIA_CONTEXT_MENU
- KEYCODE_TV_NETWORK
- KEYCODE_TV_NUMBER_ENTRY
- KEYCODE_TV_POWER
- KEYCODE_TV_RADIO_SERVICE
- KEYCODE_TV_SATELLITE
- KEYCODE_TV_SATELLITE_BS
- KEYCODE_TV_SATELLITE_CS
- KEYCODE_TV_SATELLITE_SERVICE
- KEYCODE_TV_TELETEXT
- KEYCODE_TV_TERRESTRIAL_ANALOG
- KEYCODE_TV_TERRESTRIAL_DIGITAL
- KEYCODE_TV_TIMER_PROGRAMMING
- KEYCODE_TV_ZOOM_MODE
- KEYCODE_U
- KEYCODE_UNKNOWN
- KEYCODE_V
- KEYCODE_VOICE_ASSIST
- KEYCODE_VOLUME_DOWN
- KEYCODE_VOLUME_MUTE
- KEYCODE_VOLUME_UP
- KEYCODE_W
- KEYCODE_WAKEUP
- KEYCODE_WINDOW
- KEYCODE_X
- KEYCODE_Y
- KEYCODE_YEN
- KEYCODE_Z
- KEYCODE_ZENKAKU_HANKAKU
- KEYCODE_ZOOM_IN
- KEYCODE_ZOOM_OUT

## Full Example

### Sample Thing

```java
Thing androiddebugbridge:android:xxxxxxxxxxxx "xxxxxxxxxxxx" [ ip="192.168.1.10", port=5555, refreshTime=30 ]
```

### Sample Items

```java
Group   androidDevice    "Android TV"
String  device_SendKey       "Send Key"                            (androidDevice)   {  channel="androiddebugbridge:android:xxxxxxxxxxxx:key-event" }
String  device_CurrentApp       "Current App"                            (androidDevice)   { channel="androiddebugbridge:android:xxxxxxxxxxxx:current-package" }
```
