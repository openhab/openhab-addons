# Panasonic Blu-ray Player Binding

This binding connects Panasonic Blu-ray players from 2011/2012 and UHD Blu-ray players from 2018 to openHAB.  
**Supported Blu-ray models:** DMP-BDT110, DMP-BDT210, DMP-BDT310, DMP-BDT120, DMP-BDT220, DMP-BDT320, DMP-BBT01 & DMP-BDT500.  
**Supported UHD models:** DP-UB420/424, DP-UB820/824 & DP-UB9000/9004.  

**Please Note:** The player must be on the same IP subnet as the openHAB server for this binding to function.
If the connection to the player originates from a different subnet, 404 response errors are sent in response to all requests.

To enable network remote control of the Blu-ray model players, configure the following settings:  
**Player Settings/Network/Network Settings/Remote Device Settings**  
Then make sure you have the following values set:  
**Remote Device Operation: On**  
**Registration Type: Automatic**  

For the UHD models, Voice Control must be enabled for the player's http interface to be active:
**Player Settings/Network/Voice Control: On**

To enable the binding to control the player while off (network active while off), Quick Start mode must be On as follows:  
**Player Settings/System/Quick Start: On**  

**UHD Model Command Authentication:**  
The UHD models require authentication to use the control API.
A player key must be specified in the thing configuration in order for the `power`, `button` and `control` channels to work.
UHD model players that are patched do not require a player key.
See the online discussions of the DP-UB420/820/9000 players for more information.

## Supported Things

There are two supported thing types, which represent either a BD player or a UHD player.
A supported Blu-ray player uses the `bd_player` id and a supported UHD Blu-ray player uses the `uhd_player` id.
Multiple Things can be added if more than one player is to be controlled.

## Discovery

Auto-discovery is supported if the player can be located on the local network using UPNP.
Otherwise the thing must be manually added.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                                                           |
|-----------|-------------------------------------------------------------------------------------------------------|
| hostName  | The host name or IP address of the player. Mandatory.                                                 |
| refresh   | Overrides the refresh interval of the player status. Optional, the default and minimum is 5 seconds.  |
| playerKey | For UHD models, to enable authentication of control commands, a key for the player must be specified. |

Some notes:

* The control protocol of these players is undocumented and may not work consistently in all situations
* The UHD models only support playback elapsed time (not title total time or chapter information) reporting
* The time and chapter information is only available when playing a Blu-ray disc (not DVD or CD)
* There are reports in forum postings that not all commands work on all of the older models (i.e.: Power does not work on DMP-BDT110)
* Not all status information is available from all BD models (i.e.: playback elapsed time not reported by some models)

## Channels

The following channels are available:

| Channel ID     | Item Type   | Description                                                                                      |
|----------------|-------------|--------------------------------------------------------------------------------------------------|
| power          | Switch      | Turn the power for the player on or off.                                                         |
| button         | String      | Sends a command to the player. (WriteOnly) See lists of available commands in Appendix A below.  |
| control        | Player      | Control Playback e.g. Play/Pause/Next/Previous/FForward/Rewind                                   |
| playerStatus   | String      | The player status i.e.: Power Off, Tray Open, Stopped, Playback, Pause Playback, etc. (ReadOnly) |
| timeElapsed    | Number:Time | The total number of seconds of playback time elapsed. (ReadOnly)                                 |
| timeTotal      | Number:Time | The total length of the current playing title in seconds. (ReadOnly) Not on UHD models.          |
| chapterCurrent | Number      | The current chapter number. (ReadOnly) Not on UHD models.                                        |
| chapterTotal   | Number      | The total number of chapters in the current title. (ReadOnly) Not on UHD models.                 |

## Full Example

panasonicbr.things:

```java
panasonicbr:bd_player:mybdplayer "My Blu-ray player" [ hostName="192.168.10.1", refresh=5 ]
panasonicbr:uhd_player:myuhdplayer "My UHD Blu-ray player" [ hostName="192.168.10.1", refresh=5, playerKey="ABCDEF1234567890abcdef0123456789" ]
```

panasonicbr.items:

```java
// BD Player
Switch Player_Power            "Power"                     { channel="panasonicbr:bd_player:mybdplayer:power" }
String Player_Button           "Send Command"              { channel="panasonicbr:bd_player:mybdplayer:button", autoupdate="false" }
Player Player_Control          "Control"                   { channel="panasonicbr:bd_player:mybdplayer:control" }
String Player_PlayerStatus     "Status: [%s]"              { channel="panasonicbr:bd_player:mybdplayer:playerStatus" }
Number:Time Player_TimeElapsed "Elapsed Time: [%d %unit%]" { channel="panasonicbr:bd_player:mybdplayer:timeElapsed" }
Number:Time Player_TimeTotal   "Total Time: [%d %unit%]"   { channel="panasonicbr:bd_player:mybdplayer:timeTotal" }
Number Player_ChapterCurrent   "Current Chapter: [%d]"     { channel="panasonicbr:bd_player:mybdplayer:chapterCurrent" }
Number Player_ChapterTotal     "Total Chapters: [%d]"      { channel="panasonicbr:bd_player:mybdplayer:chapterTotal" }

// UHD Player
Switch Player_Power            "Power"                     { channel="panasonicbr:uhd_player:myuhdplayer:power" }
String Player_Button           "Send Command"              { channel="panasonicbr:uhd_player:myuhdplayer:button", autoupdate="false" }
Player Player_Control          "Control"                   { channel="panasonicbr:uhd_player:myuhdplayer:control" }
String Player_PlayerStatus     "Status: [%s]"              { channel="panasonicbr:uhd_player:myuhdplayer:playerStatus" }
Number:Time Player_TimeElapsed "Elapsed Time: [%d %unit%]" { channel="panasonicbr:uhd_player:myuhdplayer:timeElapsed" }
```

panasonicbr.sitemap:

```perl
sitemap panasonicbr label="Panasonic Blu-ray" {
    Frame label="Blu-ray Player" {
        Switch item=Player_Power
        Selection item=Player_Button
        Default item=Player_Control
        Text item=Player_PlayerStatus
        Text item=Player_TimeElapsed
        // The following three channels are not available on UHD models
        Text item=Player_TimeTotal
        Text item=Player_ChapterCurrent
        Text item=Player_ChapterTotal
    }
}
```

### Appendix A - 'button' channel command codes:

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
