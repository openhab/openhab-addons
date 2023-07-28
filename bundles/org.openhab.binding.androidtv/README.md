# AndroidTV Binding

This binding is designed to emulate different protocols to interact with the AndroidTV platform.
Currently it emulates both the Google Video App to interact with a variety of AndroidTVs for purposes of remote control.
It also currently emulates the Nvidia ShieldTV Android App to interact with an Nvidia ShieldTV for purposes of remote control.

## Supported Things

This binding supports two thing types:

- **googletv** - An AndroidTV running Google Video
- **shieldtv** - An Nvidia ShieldTV

## Discovery

Both GoogleTVs and ShieldTVs should be added automatically to the inbox through the mDNS discovery process.  

In the case of the ShieldTV, openHAB will likely create an inbox entry for both a GoogleTV and a ShieldTV device.
Only the ShieldTV device should be configured, the GoogleTV can be ignored.
There is no benefit to configuring two things for a ShieldTV device.
This could cause undesired effects.

## Binding Configuration

This binding does not require any special configuration files.  

This binding does require a PIN login process (documented below) upon first connection.

This binding requires GoogleTV to be installed on the device (https://play.google.com/store/apps/details?id=com.google.android.videos)

## Thing Configuration

There are three required fields to connect successfully to a ShieldTV.

| Name             | Type    | Description                           | Default | Required | Advanced |
|------------------|---------|---------------------------------------|---------|----------|----------|
| ipAddress        | text    | IP address of the device              | N/A     | yes      | no       |
| keystore         | text    | Location of the Java Keystore         | N/A     | no       | no       |
| keystorePassword | text    | Password of the Java Keystore         | N/A     | no       | no       |

```java
Thing androidtv:shieldtv:livingroom [ ipAddress="192.168.1.2" ]
Thing androidtv:googletv:theater [ ipAddress="192.168.1.3" ]
```

## Channels

| Channel    | Type   | Description                 | GoogleTV | ShieldTV |
|------------|--------|-----------------------------|----------|----------|
| keyboard   | String | Keyboard Data Entry         |    RW    |    RW    | 
| keypress   | String | Manual Key Press Entry      |    RW    |    RW    |
| keycode    | String | Direct KEYCODE Entry        |    RW    |    RW    |
| pincode    | String | PIN Code Entry              |    RW    |    RW    |
| app        | String | App Control                 |    RO    |    RW    |
| appname    | String | App Name                    |    N/A   |    RW    |
| appurl     | String | App URL                     |    N/A   |    RW    |
| player     | Player | Player Control              |    RW    |    RW    |
| power      | Switch | Power Control               |    RW    |    RW    |
| volume     | Dimmer | Volume Control              |    RO    |    RO    |
| mute       | Switch | Mute Control                |    RW    |    RW    |


```java
String ShieldTV_KEYBOARD "KEYBOARD [%s]" { channel = "androidtv:shieldtv:livingroom:keyboard" }
String ShieldTV_KEYPRESS "KEYPRESS [%s]" { channel = "androidtv:shieldtv:livingroom:keypress" }
String ShieldTV_KEYCODE "KEYCODE [%s]" { channel = "androidtv:shieldtv:livingroom:keycode" }
String ShieldTV_PINCODE  "PINCODE [%s]" { channel = "androidtv:shieldtv:livingroom:pincode" }
String ShieldTV_APP "APP [%s]" { channel = "androidtv:shieldtv:livingroom:app" }
String ShieldTV_APPNAME "APPNAME [%s]" { channel = "androidtv:shieldtv:livingroom:appname" }
String ShieldTV_APPURL "APPURL [%s]" { channel = "androidtv:shieldtv:livingroom:appurl" }
Player ShieldTV_PLAYER "PLAYER [%s]" { channel = "androidtv:shieldtv:livingroom:player" }
Switch ShieldTV_POWER "POWER [%s]" { channel = "androidtv:shieldtv:livingroom:power" }
Dimmer ShieldTV_VOLUME "VOLUME [%s]" { channel = "androidtv:shieldtv:livingroom:volume" }
Switch ShieldTV_MUTE "MUTE [%s]" { channel = "androidtv:shieldtv:livingroom:mute" }

String GoogleTV_KEYBOARD "KEYBOARD [%s]" { channel = "androidtv:googletv:theater:keyboard" }
String GoogleTV_KEYPRESS "KEYPRESS [%s]" { channel = "androidtv:googletv:theater:keypress" }
String GoogleTV_KEYCODE "KEYCODE [%s]" { channel = "androidtv:googletv:theater:keycode" }
String GoogleTV_PINCODE  "PINCODE [%s]" { channel = "androidtv:googletv:theater:pincode" }
String GoogleTV_APP "APP [%s]" { channel = "androidtv:googletv:theater:app" }
Player GoogleTV_PLAYER "PLAYER [%s]" { channel = "androidtv:googletv:theater:player" }
Switch GoogleTV_POWER "POWER [%s]" { channel = "androidtv:googletv:theater:power" }
Dimmer GoogleTV_VOLUME "VOLUME [%s]" { channel = "androidtv:googletv:theater:volume" }
Switch GoogleTV_MUTE "MUTE [%s]" { channel = "androidtv:googletv:theater:mute" }
```

KEYPRESS will accept the following commands as strings (case sensitive):

- KEY_UP
- KEY_DOWN
- KEY_RIGHT
- KEY_LEFT
- KEY_ENTER
- KEY_HOME
- KEY_BACK
- KEY_MENU
- KEY_PLAY
- KEY_PAUSE
- KEY_PLAYPAUSE
- KEY_STOP
- KEY_NEXT
- KEY_PREVIOUS
- KEY_REWIND
- KEY_FORWARD
- KEY_POWER
- KEY_GOOGLE
- KEY_VOLUP
- KEY_VOLDOWN
- KEY_MUTE
- KEY_SUBMIT

The list above causes an instantanious "press and release" of each button.  
If you would like to manually control the press and release of each you may append _PRESS and _RELEASE to the end of each.
(e.g. KEY_FORWARD_PRESS or KEY_FORWARD_RELEASE)

You may also send an ASCII character as a single letter to simulate a key entry (e.g KEY_A, KEY_1, KEY_z).
Use KEY_SUBMIT when full text entry is complete to tell the shield to process the line.
KEY_SUBMIT is automatically sent by KEYBOARD when a command is sent to the channel.

APP will display the currently active app as presented by the AndroidTV.  
You may also send it a command of the app package name (e.g. com.google.android.youtube.tv) to start/change-to that app.

KEYCODE values are listed at the bottom of this README.
NOTE: Not all KEYCODES work on all devices.  Keycodes above 255 have not been tested.

## Command Line Access

All String type channels may receive commands from inside the karaf cli, even if there are no items configured.

This can be particularly useful for the Pin Code Process as well as for testing.

Syntax:

```shell
openhab> openhab:androidtv <thingUID> <channel> <command>
```

Example usage:

```shell
openhab> openhab:androidtv androidtv:googletv:theater keypress KEY_POWER
```

## Pin Code Process

For the AndroidTV to be successfully accessed an on-screen PIN authentication is required on the first connection.  

To begin the PIN process, send the text "REQUEST" to the pincode channel while watching your AndroidTV.

CLI Example Usage: 

```shell
openhab> openhab:androidtv androidtv:googletv:theater pincode REQUEST
```

A 6 digit PIN should be displayed on the screen.

To complete the PIN process, send the PIN displayed to the pincode channel.

CLI Example Usage:

```shell
openhab> openhab:androidtv androidtv:googletv:theater pincode abc123
```

The display should return back to where it was originally.

If you are on a ShieldTV you must run that process a second time to authenticate the GoogleTV protocol stack.

This completes the PIN process.

Upon reconnection (either from reconfiguration or a restart of OpenHAB), you should now see a message of "Login Successful" in openhab.log

## Full Example

```java
Thing androidtv:shieldtv:livingroom [ ipAddress="192.168.1.2" ]
Thing androidtv:googletv:theater [ ipAddress="192.168.1.3" ]
```

```java
String ShieldTV_KEYBOARD "KEYBOARD [%s]" { channel = "androidtv:shieldtv:livingroom:keyboard" }
String ShieldTV_KEYPRESS "KEYPRESS [%s]" { channel = "androidtv:shieldtv:livingroom:keypress" }
String ShieldTV_KEYCODE "KEYCODE [%s]" { channel = "androidtv:shieldtv:livingroom:keycode" }
String ShieldTV_PINCODE  "PINCODE [%s]" { channel = "androidtv:shieldtv:livingroom:pincode" }
String ShieldTV_APP "APP [%s]" { channel = "androidtv:shieldtv:livingroom:app" }
String ShieldTV_APPNAME "APPNAME [%s]" { channel = "androidtv:shieldtv:livingroom:appname" }
String ShieldTV_APPURL "APPURL [%s]" { channel = "androidtv:shieldtv:livingroom:appurl" }
Player ShieldTV_PLAYER "PLAYER [%s]" { channel = "androidtv:shieldtv:livingroom:player" }
Switch ShieldTV_POWER "POWER [%s]" { channel = "androidtv:shieldtv:livingroom:power" }
Dimmer ShieldTV_VOLUME "VOLUME [%s]" { channel = "androidtv:shieldtv:livingroom:volume" }
Switch ShieldTV_MUTE "MUTE [%s]" { channel = "androidtv:shieldtv:livingroom:mute" }

String GoogleTV_KEYBOARD "KEYBOARD [%s]" { channel = "androidtv:googletv:theater:keyboard" }
String GoogleTV_KEYPRESS "KEYPRESS [%s]" { channel = "androidtv:googletv:theater:keypress" }
String GoogleTV_KEYCODE "KEYCODE [%s]" { channel = "androidtv:googletv:theater:keycode" }
String GoogleTV_PINCODE  "PINCODE [%s]" { channel = "androidtv:googletv:theater:pincode" }
String GoogleTV_APP "APP [%s]" { channel = "androidtv:googletv:theater:app" }
Player GoogleTV_PLAYER "PLAYER [%s]" { channel = "androidtv:googletv:theater:player" }
Switch GoogleTV_POWER "POWER [%s]" { channel = "androidtv:googletv:theater:power" }
Dimmer GoogleTV_VOLUME "VOLUME [%s]" { channel = "androidtv:googletv:theater:volume" }
Switch GoogleTV_MUTE "MUTE [%s]" { channel = "androidtv:googletv:theater:mute" }
```

## Google Keycodes

| CODE | BUTTON |
|------|--------|
| 0 | KEYCODE_UNKNOWN |
| 1 | KEYCODE_SOFT_LEFT |
| 2 | KEYCODE_SOFT_RIGHT |
| 3 | KEYCODE_HOME |
| 4 | KEYCODE_BACK |
| 5 | KEYCODE_CALL |
| 6 | KEYCODE_ENDCALL |
| 7 | KEYCODE_0 |
| 8 | KEYCODE_1 |
| 9 | KEYCODE_2 |
| 10 | KEYCODE_3 |
| 11 | KEYCODE_4 |
| 12 | KEYCODE_5 |
| 13 | KEYCODE_6 |
| 14 | KEYCODE_7 |
| 15 | KEYCODE_8 |
| 16 | KEYCODE_9 |
| 17 | KEYCODE_STAR |
| 18 | KEYCODE_POUND |
| 19 | KEYCODE_DPAD_UP |
| 20 | KEYCODE_DPAD_DOWN |
| 21 | KEYCODE_DPAD_LEFT |
| 22 | KEYCODE_DPAD_RIGHT |
| 23 | KEYCODE_DPAD_CENTER |
| 24 | KEYCODE_VOLUME_UP |
| 25 | KEYCODE_VOLUME_DOWN |
| 26 | KEYCODE_POWER |
| 27 | KEYCODE_CAMERA |
| 28 | KEYCODE_CLEAR |
| 29 | KEYCODE_A |
| 30 | KEYCODE_B |
| 31 | KEYCODE_C |
| 32 | KEYCODE_D |
| 33 | KEYCODE_E |
| 34 | KEYCODE_F |
| 35 | KEYCODE_G |
| 36 | KEYCODE_H |
| 37 | KEYCODE_I |
| 38 | KEYCODE_J |
| 39 | KEYCODE_K |
| 40 | KEYCODE_L |
| 41 | KEYCODE_M |
| 42 | KEYCODE_N |
| 43 | KEYCODE_O |
| 44 | KEYCODE_P |
| 45 | KEYCODE_Q |
| 46 | KEYCODE_R |
| 47 | KEYCODE_S |
| 48 | KEYCODE_T |
| 49 | KEYCODE_U |
| 50 | KEYCODE_V |
| 51 | KEYCODE_W |
| 52 | KEYCODE_X |
| 53 | KEYCODE_Y |
| 54 | KEYCODE_Z |
| 55 | KEYCODE_COMMA |
| 56 | KEYCODE_PERIOD |
| 57 | KEYCODE_ALT_LEFT |
| 58 | KEYCODE_ALT_RIGHT |
| 59 | KEYCODE_SHIFT_LEFT |
| 60 | KEYCODE_SHIFT_RIGHT |
| 61 | KEYCODE_TAB |
| 62 | KEYCODE_SPACE |
| 63 | KEYCODE_SYM |
| 64 | KEYCODE_EXPLORER |
| 65 | KEYCODE_ENVELOPE |
| 66 | KEYCODE_ENTER |
| 67 | KEYCODE_DEL |
| 68 | KEYCODE_GRAVE |
| 69 | KEYCODE_MINUS |
| 70 | KEYCODE_EQUALS |
| 71 | KEYCODE_LEFT_BRACKET |
| 72 | KEYCODE_RIGHT_BRACKET |
| 73 | KEYCODE_BACKSLASH |
| 74 | KEYCODE_SEMICOLON |
| 75 | KEYCODE_APOSTROPHE |
| 76 | KEYCODE_SLASH |
| 77 | KEYCODE_AT |
| 78 | KEYCODE_NUM |
| 79 | KEYCODE_HEADSETHOOK |
| 80 | KEYCODE_FOCUS |
| 81 | KEYCODE_PLUS |
| 82 | KEYCODE_MENU |
| 83 | KEYCODE_NOTIFICATION |
| 84 | KEYCODE_SEARCH |
| 85 | KEYCODE_MEDIA_PLAY_PAUSE |
| 86 | KEYCODE_MEDIA_STOP |
| 87 | KEYCODE_MEDIA_NEXT |
| 88 | KEYCODE_MEDIA_PREVIOUS |
| 89 | KEYCODE_MEDIA_REWIND |
| 90 | KEYCODE_MEDIA_FAST_FORWARD |
| 91 | KEYCODE_MUTE |
| 92 | KEYCODE_PAGE_UP |
| 93 | KEYCODE_PAGE_DOWN |
| 94 | KEYCODE_PICTSYMBOLS |
| 95 | KEYCODE_SWITCH_CHARSET |
| 96 | KEYCODE_BUTTON_A |
| 97 | KEYCODE_BUTTON_B |
| 98 | KEYCODE_BUTTON_C |
| 99 | KEYCODE_BUTTON_X |
| 100 | KEYCODE_BUTTON_Y |
| 101 | KEYCODE_BUTTON_Z |
| 102 | KEYCODE_BUTTON_L1 |
| 103 | KEYCODE_BUTTON_R1 |
| 104 | KEYCODE_BUTTON_L2 |
| 105 | KEYCODE_BUTTON_R2 |
| 106 | KEYCODE_BUTTON_THUMBL |
| 107 | KEYCODE_BUTTON_THUMBR |
| 108 | KEYCODE_BUTTON_START |
| 109 | KEYCODE_BUTTON_SELECT |
| 110 | KEYCODE_BUTTON_MODE |
| 111 | KEYCODE_ESCAPE |
| 112 | KEYCODE_FORWARD_DEL |
| 113 | KEYCODE_CTRL_LEFT |
| 114 | KEYCODE_CTRL_RIGHT |
| 115 | KEYCODE_CAPS_LOCK |
| 116 | KEYCODE_SCROLL_LOCK |
| 117 | KEYCODE_META_LEFT |
| 118 | KEYCODE_META_RIGHT |
| 119 | KEYCODE_FUNCTION |
| 120 | KEYCODE_SYSRQ |
| 121 | KEYCODE_BREAK |
| 122 | KEYCODE_MOVE_HOME |
| 123 | KEYCODE_MOVE_END |
| 124 | KEYCODE_INSERT |
| 125 | KEYCODE_FORWARD |
| 126 | KEYCODE_MEDIA_PLAY |
| 127 | KEYCODE_MEDIA_PAUSE |
| 128 | KEYCODE_MEDIA_CLOSE |
| 129 | KEYCODE_MEDIA_EJECT |
| 130 | KEYCODE_MEDIA_RECORD |
| 131 | KEYCODE_F1 |
| 132 | KEYCODE_F2 |
| 133 | KEYCODE_F3 |
| 134 | KEYCODE_F4 |
| 135 | KEYCODE_F5 |
| 136 | KEYCODE_F6 |
| 137 | KEYCODE_F7 |
| 138 | KEYCODE_F8 |
| 139 | KEYCODE_F9 |
| 140 | KEYCODE_F10 |
| 141 | KEYCODE_F11 |
| 142 | KEYCODE_F12 |
| 143 | KEYCODE_NUM_LOCK |
| 144 | KEYCODE_NUMPAD_0 |
| 145 | KEYCODE_NUMPAD_1 |
| 146 | KEYCODE_NUMPAD_2 |
| 147 | KEYCODE_NUMPAD_3 |
| 148 | KEYCODE_NUMPAD_4 |
| 149 | KEYCODE_NUMPAD_5 |
| 150 | KEYCODE_NUMPAD_6 |
| 151 | KEYCODE_NUMPAD_7 |
| 152 | KEYCODE_NUMPAD_8 |
| 153 | KEYCODE_NUMPAD_9 |
| 154 | KEYCODE_NUMPAD_DIVIDE |
| 155 | KEYCODE_NUMPAD_MULTIPLY |
| 156 | KEYCODE_NUMPAD_SUBTRACT |
| 157 | KEYCODE_NUMPAD_ADD |
| 158 | KEYCODE_NUMPAD_DOT |
| 159 | KEYCODE_NUMPAD_COMMA |
| 160 | KEYCODE_NUMPAD_ENTER |
| 161 | KEYCODE_NUMPAD_EQUALS |
| 162 | KEYCODE_NUMPAD_LEFT_PAREN |
| 163 | KEYCODE_NUMPAD_RIGHT_PAREN |
| 164 | KEYCODE_VOLUME_MUTE |
| 165 | KEYCODE_INFO |
| 166 | KEYCODE_CHANNEL_UP |
| 167 | KEYCODE_CHANNEL_DOWN |
| 168 | KEYCODE_ZOOM_IN |
| 169 | KEYCODE_ZOOM_OUT |
| 170 | KEYCODE_TV |
| 171 | KEYCODE_WINDOW |
| 172 | KEYCODE_GUIDE |
| 173 | KEYCODE_DVR |
| 174 | KEYCODE_BOOKMARK |
| 175 | KEYCODE_CAPTIONS |
| 176 | KEYCODE_SETTINGS |
| 177 | KEYCODE_TV_POWER |
| 178 | KEYCODE_TV_INPUT |
| 179 | KEYCODE_STB_POWER |
| 180 | KEYCODE_STB_INPUT |
| 181 | KEYCODE_AVR_POWER |
| 182 | KEYCODE_AVR_INPUT |
| 183 | KEYCODE_PROG_RED |
| 184 | KEYCODE_PROG_GREEN |
| 185 | KEYCODE_PROG_YELLOW |
| 186 | KEYCODE_PROG_BLUE |
| 187 | KEYCODE_APP_SWITCH |
| 188 | KEYCODE_BUTTON_1 |
| 189 | KEYCODE_BUTTON_2 |
| 190 | KEYCODE_BUTTON_3 |
| 191 | KEYCODE_BUTTON_4 |
| 192 | KEYCODE_BUTTON_5 |
| 193 | KEYCODE_BUTTON_6 |
| 194 | KEYCODE_BUTTON_7 |
| 195 | KEYCODE_BUTTON_8 |
| 196 | KEYCODE_BUTTON_9 |
| 197 | KEYCODE_BUTTON_10 |
| 198 | KEYCODE_BUTTON_11 |
| 199 | KEYCODE_BUTTON_12 |
| 200 | KEYCODE_BUTTON_13 |
| 201 | KEYCODE_BUTTON_14 |
| 202 | KEYCODE_BUTTON_15 |
| 203 | KEYCODE_BUTTON_16 |
| 204 | KEYCODE_LANGUAGE_SWITCH |
| 205 | KEYCODE_MANNER_MODE |
| 206 | KEYCODE_3D_MODE |
| 207 | KEYCODE_CONTACTS |
| 208 | KEYCODE_CALENDAR |
| 209 | KEYCODE_MUSIC |
| 210 | KEYCODE_CALCULATOR |
| 211 | KEYCODE_ZENKAKU_HANKAKU |
| 212 | KEYCODE_EISU |
| 213 | KEYCODE_MUHENKAN |
| 214 | KEYCODE_HENKAN |
| 215 | KEYCODE_KATAKANA_HIRAGANA |
| 216 | KEYCODE_YEN |
| 217 | KEYCODE_RO |
| 218 | KEYCODE_KANA |
| 219 | KEYCODE_ASSIST |
| 220 | KEYCODE_BRIGHTNESS_DOWN |
| 221 | KEYCODE_BRIGHTNESS_UP |
| 222 | KEYCODE_MEDIA_AUDIO_TRACK |
| 223 | KEYCODE_SLEEP |
| 224 | KEYCODE_WAKEUP |
| 225 | KEYCODE_PAIRING |
| 226 | KEYCODE_MEDIA_TOP_MENU |
| 227 | KEYCODE_11 |
| 228 | KEYCODE_12 |
| 229 | KEYCODE_LAST_CHANNEL |
| 230 | KEYCODE_TV_DATA_SERVICE |
| 231 | KEYCODE_VOICE_ASSIST |
| 232 | KEYCODE_TV_RADIO_SERVICE |
| 233 | KEYCODE_TV_TELETEXT |
| 234 | KEYCODE_TV_NUMBER_ENTRY |
| 235 | KEYCODE_TV_TERRESTRIAL_ANALOG |
| 236 | KEYCODE_TV_TERRESTRIAL_DIGITAL |
| 237 | KEYCODE_TV_SATELLITE |
| 238 | KEYCODE_TV_SATELLITE_BS |
| 239 | KEYCODE_TV_SATELLITE_CS |
| 240 | KEYCODE_TV_SATELLITE_SERVICE |
| 241 | KEYCODE_TV_NETWORK |
| 242 | KEYCODE_TV_ANTENNA_CABLE |
| 243 | KEYCODE_TV_INPUT_HDMI_1 |
| 244 | KEYCODE_TV_INPUT_HDMI_2 |
| 245 | KEYCODE_TV_INPUT_HDMI_3 |
| 246 | KEYCODE_TV_INPUT_HDMI_4 |
| 247 | KEYCODE_TV_INPUT_COMPOSITE_1 |
| 248 | KEYCODE_TV_INPUT_COMPOSITE_2 |
| 249 | KEYCODE_TV_INPUT_COMPONENT_1 |
| 250 | KEYCODE_TV_INPUT_COMPONENT_2 |
| 251 | KEYCODE_TV_INPUT_VGA_1 |
| 252 | KEYCODE_TV_AUDIO_DESCRIPTION |
| 253 | KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP |
| 254 | KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN |
| 255 | KEYCODE_TV_ZOOM_MODE |
| 256 | KEYCODE_TV_CONTENTS_MENU |
| 257 | KEYCODE_TV_MEDIA_CONTEXT_MENU |
| 258 | KEYCODE_TV_TIMER_PROGRAMMING |
| 259 | KEYCODE_HELP |
| 260 | KEYCODE_NAVIGATE_PREVIOUS |
| 261 | KEYCODE_NAVIGATE_NEXT |
| 262 | KEYCODE_NAVIGATE_IN |
| 263 | KEYCODE_NAVIGATE_OUT |
| 264 | KEYCODE_STEM_PRIMARY |
| 265 | KEYCODE_STEM_1 |
| 266 | KEYCODE_STEM_2 |
| 267 | KEYCODE_STEM_3 |
| 268 | KEYCODE_DPAD_UP_LEFT |
| 269 | KEYCODE_DPAD_DOWN_LEFT |
| 270 | KEYCODE_DPAD_UP_RIGHT |
| 271 | KEYCODE_DPAD_DOWN_RIGHT |
| 272 | KEYCODE_MEDIA_SKIP_FORWARD |
| 273 | KEYCODE_MEDIA_SKIP_BACKWARD |
| 274 | KEYCODE_MEDIA_STEP_FORWARD |
| 275 | KEYCODE_MEDIA_STEP_BACKWARD |
| 276 | KEYCODE_SOFT_SLEEP |
| 277 | KEYCODE_CUT |
| 278 | KEYCODE_COPY |
| 279 | KEYCODE_PASTE |
| 280 | KEYCODE_SYSTEM_NAVIGATION_UP |
| 281 | KEYCODE_SYSTEM_NAVIGATION_DOWN |
| 282 | KEYCODE_SYSTEM_NAVIGATION_LEFT |
| 283 | KEYCODE_SYSTEM_NAVIGATION_RIGHT |
| 284 | KEYCODE_ALL_APPS |
| 285 | KEYCODE_REFRESH |
| 286 | KEYCODE_THUMBS_UP |
| 287 | KEYCODE_THUMBS_DOWN |
| 288 | KEYCODE_PROFILE_SWITCH |
| 289 | KEYCODE_VIDEO_APP_1 |
| 290 | KEYCODE_VIDEO_APP_2 |
| 291 | KEYCODE_VIDEO_APP_3 |
| 292 | KEYCODE_VIDEO_APP_4 |
| 293 | KEYCODE_VIDEO_APP_5 |
| 294 | KEYCODE_VIDEO_APP_6 |
| 295 | KEYCODE_VIDEO_APP_7 |
| 296 | KEYCODE_VIDEO_APP_8 |
| 297 | KEYCODE_FEATURED_APP_1 |
| 298 | KEYCODE_FEATURED_APP_2 |
| 299 | KEYCODE_FEATURED_APP_3 |
| 300 | KEYCODE_FEATURED_APP_4 |
| 301 | KEYCODE_DEMO_APP_1 |
| 302 | KEYCODE_DEMO_APP_2 |
| 303 | KEYCODE_DEMO_APP_3 |
| 304 | KEYCODE_DEMO_APP_4 |

