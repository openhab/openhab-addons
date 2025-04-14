# Panasonic Blu-ray Player Binding

This binding connects Panasonic Blu-ray players from 2011/2012 and UHD Blu-ray players from 2018 to openHAB.

**Supported Blu-ray models:** DMP-BDT110, DMP-BDT210, DMP-BDT310, DMP-BDT120, DMP-BDT220, DMP-BDT320, DMP-BBT01 & DMP-BDT500.

**Supported UHD models:** DP-UB420/424, DP-UB820/824 & DP-UB9000/9004.

**Please note:** The player must be on the same IP subnet as the openHAB server for this binding to function.
If the connection to the player originates from a different subnet, 404 response errors are sent in response to all requests.

To enable network remote control of the Blu-ray model players, configure the following settings:

- **Player Settings/Network/Network Settings/Remote Device Settings**

Then make sure you have the following values set:

- **Remote Device Operation: On**
- **Registration Type: Automatic**

For the UHD models, Voice Control must be enabled for the player's http interface to be active:

- **Player Settings/Network/Voice Control: On**

To enable the binding to control the player while off (network active while off), Quick Start mode must be On as follows:

- **Player Settings/System/Quick Start: On**

**UHD Model Command Authentication:**

The UHD models require authentication to use the control API.
A player key must be specified in the thing configuration in order for the `power`, `button` and `control` channels to work.

UHD model players that are patched do not require a player key.
See the [AVForums discussions](https://www.avforums.com/forums/blu-ray-dvd-player-multiregion-hacks.126/) of the DP-UB420/820/9000 players for more information.

## Supported Things

There are two supported thing types, which represent either a BD player or a UHD player.
A supported Blu-ray player uses the `bd-player` id and a supported UHD Blu-ray player uses the `uhd-player` id.
Multiple Things can be added if more than one player is to be controlled.

## Discovery

Auto-discovery is supported if the player can be located on the local network using UPnP.
Otherwise the Thing must be manually added.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The Thing has a few configuration parameters:

| Name      | Type    | Description                                                                                           | Default | Required |
|-----------|---------|-------------------------------------------------------------------------------------------------------|---------|----------|
| hostName  | text    | The host name or IP address of the player.                                                            | N/A     | yes      |
| refresh   | integer | Overrides the refresh interval of the player status. Minimum interval is 5 seconds.                   | 5       | no       |
| playerKey | text    | For UHD models, to enable authentication of control commands, a key for the player must be specified. | N/A     | no       |

Some notes:

- The control protocol of these players is undocumented and may not work consistently in all situations
- The UHD models only support playback elapsed time (not title total time or chapter information) reporting
- The time and chapter information is only available when playing a Blu-ray disc (not DVD or CD)
- There are reports in forum postings that not all commands work on all of the older models (i.e.: Power does not work on DMP-BDT110)
- Not all status information is available from all BD models (i.e.: playback elapsed time not reported by some models)
- Netflix is no longer supported on the BD models.

## Channels

The following channels are available:

| Channel ID      | Item Type   | Read/Write | Description                                                                           |
|-----------------|-------------|------------|---------------------------------------------------------------------------------------|
| power           | Switch      | RW         | Turn the power for the player ON or OFF.                                              |
| button          | String      | W          | Sends a command to the player. See lists of available commands in Appendix A below.   |
| control         | Player      | RW         | Control Playback e.g. Play/Pause/Next/Previous/FForward/Rewind.                       |
| player-status   | String      | R          | The player status i.e.: Power Off, Tray Open, Stopped, Playback, Pause Playback, etc. |
| time-elapsed    | Number:Time | R          | The total playback time elapsed.                                                      |
| time-total      | Number:Time | R          | The total length of the current playing title. Not on UHD models.                     |
| chapter-current | Number      | R          | The current chapter number. Not on UHD models.                                        |
| chapter-total   | Number      | R          | The total number of chapters in the current title. Not on UHD models.                 |

## Full Example

### `panasonicbdp.things` Example

```java
panasonicbdp:bd-player:mybdplayer "My Blu-ray player" [ hostName="192.168.10.1", refresh=5 ]
panasonicbdp:uhd-player:myuhdplayer "My UHD Blu-ray player" [ hostName="192.168.10.1", refresh=5, playerKey="ABCDEF1234567890abcdef0123456789" ]
```

### `panasonicbdp.items` Example

```java
// BD Player
Switch Player_Power            "Power"                     { channel="panasonicbdp:bd-player:mybdplayer:power" }
String Player_Button           "Send Command"              { channel="panasonicbdp:bd-player:mybdplayer:button" }
Player Player_Control          "Control"                   { channel="panasonicbdp:bd-player:mybdplayer:control" }
String Player_PlayerStatus     "Status: [%s]"              { channel="panasonicbdp:bd-player:mybdplayer:player-status" }
Number:Time Player_TimeElapsed "Elapsed Time: [%s]"        { channel="panasonicbdp:bd-player:mybdplayer:time-elapsed" }
Number:Time Player_TimeTotal   "Total Time: [%s]"          { channel="panasonicbdp:bd-player:mybdplayer:time-total" }
Number Player_ChapterCurrent   "Current Chapter: [%d]"     { channel="panasonicbdp:bd-player:mybdplayer:chapter-current" }
Number Player_ChapterTotal     "Total Chapters: [%d]"      { channel="panasonicbdp:bd-player:mybdplayer:chapter-total" }

// UHD Player
Switch Player_Power            "Power"                     { channel="panasonicbdp:uhd-player:myuhdplayer:power" }
String Player_Button           "Send Command"              { channel="panasonicbdp:uhd-player:myuhdplayer:button" }
Player Player_Control          "Control"                   { channel="panasonicbdp:uhd-player:myuhdplayer:control" }
String Player_PlayerStatus     "Status: [%s]"              { channel="panasonicbdp:uhd-player:myuhdplayer:player-status" }
Number:Time Player_TimeElapsed "Elapsed Time: [%s]"        { channel="panasonicbdp:uhd-player:myuhdplayer:time-elapsed" }
```

### `panasonicbdp.sitemap` Example

```perl
sitemap panasonicbdp label="Panasonic Blu-ray" {
    Frame label="Blu-ray Player" {
        Switch item=Player_Power
        // This Selection is deprecated in favor of the Buttongrid element below
        Selection item=Player_Button icon="player"
        Default item=Player_Control
        Text item=Player_PlayerStatus
        Text item=Player_TimeElapsed icon="time"
        // The following three channels are not available on UHD models
        Text item=Player_TimeTotal icon="time"
        Text item=Player_ChapterCurrent icon="time"
        Text item=Player_ChapterTotal icon="time"
        Buttongrid label="Remote Control" staticIcon=material:tv_remote item=Player_Button buttons=[1:1:POWER="Power"=switch-off, 1:3:OP_CL="Open"=f7:eject, 2:1:D1="1", 2:2:D2="2", 2:3:D3="3", 3:1:D4="4", 3:2:D5="5", 3:3:D6="6", 4:1:D7="7", 4:2:D8="8", 4:3:D9="9", 5:1:SHARP="# [_]", 5:2:D0="0", 5:3:CLEAR="* Cancel", 6:1:NETFLIX="Netflix", 6:2:MLTNAVI="Home"=f7:house, 6:3:NETWORK="Internet", 7:1:PUPMENU="Pop-Up Menu", 7:2:UP="UP"=f7:arrowtriangle_up, 7:3:TITLE="Top Menu", 8:1:LEFT="Left"=f7:arrowtriangle_left, 8:2:SELECT="OK", 8:3:RIGHT="Right"=f7:arrowtriangle_right, 9:1:MENU="Option", 9:2:DOWN="Down"=f7:arrowtriangle_down, 9:3:RETURN="Return", 10:1:REV="Rev"=f7:backward, 10:2:PLAYBACK="Play"=f7:play, 10:3:CUE="Fwd"=f7:forward,  11:1:SKIPREV="Prev"=f7:backward_end_alt, 11:2:PAUSE="Pause"=f7:pause, 11:3:SKIPFWD="Next"=f7:forward_end_alt, 12:1:CLOSED_CAPTION="CC", 12:2:STOP="Stop"=f7:stop, 12:3:MIRACAST="Mirroring", 13:1:RED="Red", 13:2:GREEN="Green", 13:3:YELLOW="Yellow", 14:1:BLUE="Blue", 14:2:DSPSEL="Status", 14:3:PLAYBACKINFO="Playback Info", 15:1:TITLEONOFF="Subtitle", 15:2:AUDIOSEL="Audio", 15:3:PICTURESETTINGS="Video Setting", 16:1:HDR_PICTUREMODE="HDR Setting", 16:2:SOUNDEFFECT="Sound Effect", 16:3:HIGHCLARITY="High Clarity", 17:2:SKIP_THE_TRAILER="Skip The Trailer"]
    }
}
```

### Appendix A - 'button' channel command codes

**List of available button commands for BD players:**

| Function                 | Command  |
|--------------------------|----------|
| Power On                 | POWERON  |
| Power Off                | POWEROFF |
| Power Toggle             | POWER    |
| Play                     | PLAYBACK |
| Pause                    | PAUSE    |
| Stop                     | STOP     |
| Fast Forward             | CUE      |
| Reverse                  | REV      |
| Skip Forward             | SKIPFWD  |
| Skip Back                | SKIPREV  |
| Open/Close               | OP_CL    |
| Status                   | DSPSEL   |
| Top Menu                 | TITLE    |
| Pop-Up Menu              | PUPMENU  |
| Up                       | UP       |
| Down                     | DOWN     |
| Left                     | LEFT     |
| Right                    | RIGHT    |
| OK                       | SELECT   |
| Submenu                  | MENU     |
| Return                   | RETURN   |
| 1 (@.)                   | D1       |
| 2 (ABC)                  | D2       |
| 3 (DEF)                  | D3       |
| 4 (GHI)                  | D4       |
| 5 (JKL)                  | D5       |
| 6 (MNO)                  | D6       |
| 7 (PQRS)                 | D7       |
| 8 (TUV)                  | D8       |
| 9 (WXYZ)                 | D9       |
| 0 (-,)                   | D0       |
| 12                       | D12      |
| &#42; (Cancel)           | CLEAR    |
| &#35; ([_])              | SHARP    |
| Red                      | RED      |
| Green                    | GREEN    |
| Blue                     | BLUE     |
| Yellow                   | YELLOW   |
| Home                     | MLTNAVI  |
| Netflix (broken/too old) | NETFLIX  |
| VIERA Cast               | V_CAST   |
| Network                  | NETWORK  |
| Setup                    | SETUP    |
| Exit                     | EXIT     |
| Audio                    | AUDIOSEL |
| 3D                       | 3D       |
| Playback View (buttons not in other views)||
| PIP                      | P_IN_P   |
| OSD (DISPLAY)            | OSDONOFF |
| Shuttle(BD) View (buttons not in other views)||
| Swipe in CW circle       | SHFWD2   |
| Swipe in CCW circle      | SHREV2   |

**List of available button commands for UHD players:**

| Function         | Command          |
|------------------|------------------|
| Power On         | POWERON          |
| Power Off        | POWEROFF         |
| Power Toggle     | POWER            |
| Play             | PLAYBACK         |
| Pause            | PAUSE            |
| Stop             | STOP             |
| Fast Forward     | CUE              |
| Reverse          | REV              |
| Skip Forward     | SKIPFWD          |
| Skip Back        | SKIPREV          |
| Manual Skip +60s | MNSKIP           |
| Manual Skip -10s | MNBACK           |
| Open/Close       | OP_CL            |
| Status           | DSPSEL           |
| Top Menu         | TITLE            |
| Pop-up Menu      | PUPMENU          |
| Up               | UP               |
| Down             | DOWN             |
| Left             | LEFT             |
| Right            | RIGHT            |
| OK               | SELECT           |
| Submenu          | MENU             |
| Return           | RETURN           |
| 1 (@.)           | D1               |
| 2 (ABC)          | D2               |
| 3 (DEF)          | D3               |
| 4 (GHI)          | D4               |
| 5 (JKL)          | D5               |
| 6 (MNO)          | D6               |
| 7 (PQRS)         | D7               |
| 8 (TUV)          | D8               |
| 9 (WXYZ)         | D9               |
| 0 (-,)           | D0               |
| 12               | D12              |
| &#42; (Cancel)   | CLEAR            |
| &#35; ([_])      | SHARP            |
| Red              | RED              |
| Green            | GREEN            |
| Blue             | BLUE             |
| Yellow           | YELLOW           |
| Home             | MLTNAVI          |
| Netflix          | NETFLIX          |
| VIERA Cast       | V_CAST           |
| Network          | NETWORK          |
| Setup            | SETUP            |
| Exit             | EXIT             |
| Audio            | AUDIOSEL         |
| Subtitle         | TITLEONOFF       |
| Closed Caption   | CLOSED_CAPTION   |
| Playback Info    | PLAYBACKINFO     |
| HDR Picture Mode | HDR_PICTUREMODE  |
| Mirroring        | MIRACAST         |
| Picture Setting  | PICTURESETTINGS  |
| Sound Effect     | SOUNDEFFECT      |
| High Clarity     | HIGHCLARITY      |
| Skip The Trailer | SKIP_THE_TRAILER |
