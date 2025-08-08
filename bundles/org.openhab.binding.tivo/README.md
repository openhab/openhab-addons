# TiVo Binding

This binding controls a [TiVo](https://www.tivo.com/) Digital Video Recorder (DVR) that supports the TiVo TCP Control Protocol v1.1 (see TiVo_TCP_Network_Remote_Control_Protocol.pdf).

## Supported Things

Most TiVo DVRs that support network remote control can be managed/supported by this binding.
Please note that beyond sending a full set of control commands, the network control protocol is very limited.
The only feedback provided is the currently tuned channel number and whether or not the channel is recording.

It is possible to control a TiVo at a deeper level through an authenticated API. See the kmttg project for more details.
There are no current plans to add any of the authenticated API features to this binding.

All TiVo devices must:

 1. Be connected to a local area TCP/IP network that can be reached by the openHAB instance (this is not the WAN network interface used by cable service providers on some TiVos to provide the TV signals).
 1. Have the Network Remote Control function enabled to support discovery and control of the device. This setting can be found using the remote control at:

    - TiVo branded boxes - using the remote go to TiVo Central > Messages & Settings > Settings > Remote, CableCARD & Devices > Network Remote Control. Choose Enabled, press Select.
    - Virgin Media branded boxes - using the remote select Home > Help and Settings > Settings > Devices > Network Remote Control. Select the option Allow network based remote controls.

## Discovery

TiVo devices with the network remote control interface enabled will be displayed automatically within the Inbox.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

The thing has the following configuration parameters:

| Parameter         | Display Name                         | Description                                                                                                                                                                                                                                                                        |
|-------------------|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| host              | Address                              | The IP address or hostname of your TiVo DVR.                                                                                                                                                                                                                                       |
| tcpPort           | TCP Port                             | The TCP port number used to connect to the TiVo. **Default: 31339**                                                                                                                                                                                                                |
| numRetry          | Connection Retries                   | The number of times to attempt reconnection to the TiVo DVR, if there is a connection failure. **Default: 5**                                                                                                                                                                      |
| keepConActive     | Keep Connection Open                 | Keep connection to the TiVo open. Recommended for monitoring the TiVo for changes in TV channels. <br><br>Disable if other applications that use the Remote Control Protocol port will also be used e.g. mobile phone remote control applications. **Default: True (Enabled)**     |
| pollForChanges    | Poll for Channel Changes             | Check TiVo for channel changes. Enable if openHAB and a physical remote control (or other services use the Remote Control Protocol) will be used. **Default: True (Enabled)**                                                                                                      |
| pollInterval      | Polling Interval (Seconds)           | Number of seconds between polling jobs to update status information from the TiVo. **Default: 10**                                                                                                                                                                                 |
| cmdWaitInterval   | Command Wait Interval (Milliseconds) | Period to wait _after_ a command is sent to the TiVo in milliseconds, before checking that the command has completed. **Default: 200**                                                                                                                                             |

Some notes:

- If openHAB is the only device or application that you have that makes use of the Network Remote Control functions of your TiVo, enable the **Keep Connection Open** option. This will connect and lock the port in-use preventing any other device from connecting it. If you use some other application, disable this option. Performance is improved if the connection is kept open.
- **Poll for Channel Changes** only needs to be enabled if you also plan to use the TiVo remote control or other application to change channel. If openHAB is your only method of control, you can disable this option. Turning polling off, minimizes the periodic polling overhead on your hardware.

## Channels

All devices support the following channels:

| Channel Type ID | Item Type       | Display Name                          | Description                                                                                                                                                                                                                                                       |
|-----------------|-----------------|---------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| channelSet      | Number (1-9999) | Current Channel - Request (SETCH)     | Displays the current channel number. When changed, tunes the DVR to the specified channel (unless a recording is in progress on all available tuners). The TiVo must be in Live TV mode for this command to work.                                                 |
| channelForce    | Number (1-9999) | Current Channel - Forced (FORCECH)    | Displays the current channel number. When changed, tunes the DVR to the specified channel, **cancelling any recordings in progress if necessary** i.e. when all tuners are already in use / recording. The TiVo must be in Live TV mode for this command to work. |
| isRecording     | Number (0-1)    | Recording                             | Indicates if the current channel is recording.                                                                                                                                                                                                                    |
| menuTeleport    | String          | Change Special/Menu Screen (TELEPORT) | Change to one of the following TiVo menu screens: TIVO (Home), LIVETV, GUIDE, NOWPLAYING (My Shows), SEARCH, NETFLIX.                                                                                                                                             |
| irCommand       | String          | Remote Control Button (IRCOMMAND)     | Send a simulated button push from the remote control to the TiVo. See below for available IR COMMANDS.                                                                                                                                                            |
| kbdCommand      | String          | Keyboard Command (KEYBOARD)           | Sends a code corresponding to a keyboard key press to the TiVo e.g. A-Z. See Appendix A in document TCP Remote Protocol 1.1 for supported characters and special character codes.                                                                                 |
| dvrStatus       | String          | TiVo Status                           | Action return code / channel information returned by the TiVo.                                                                                                                                                                                                    |

- To change channels simply post/send the number of the channel to channelSet or channelForce. For OTA channels, a decimal for the sub-channel must be specified (ie: 2.1), for all others just send the channel as a whole number (ie: 100).
- Keyboard commands must currently be issued one character at a time to the item (this is how the TiVo natively supports this command).
- To send multiple copies of the same keyboard command, append an asterisk with the number of repeats required e.g. NUM2*4 would send the number 2 four times. This is useful for performing searches where the number characters can only be accessed by pressing the keys multiple times in rapid succession i.e. each key press cycles through characters A, B, C, 2.
- Special characters must also be changed to the appropriate command e.g. the comma symbol(`,`) must not be sent it should be replaced by 'COMMA'.

### Available IR Commands to use with `irCommand` channel:

- UP
- DOWN
- LEFT
- RIGHT
- SELECT
- TIVO
- LIVETV
- GUIDE
- BACK
- INFO
- EXIT
- THUMBSUP
- THUMBSDOWN
- CHANNELUP
- CHANNELDOWN
- PLAY
- FORWARD
- REVERSE
- PAUSE
- SLOW
- REPLAY
- ADVANCE
- RECORD
- NUM0
- NUM1
- NUM2
- NUM3
- NUM4
- NUM5
- NUM6
- NUM7
- NUM8
- NUM9
- ENTER
- CLEAR
- ACTION_A
- ACTION_B
- ACTION_C
- ACTION_D
- CC_ON
- CC_OFF
- FIND_REMOTE
- STANDBY
- ASPECT_CORRECTION_FULL
- ASPECT_CORRECTION_PANEL
- ASPECT_CORRECTION_ZOOM
- ASPECT_CORRECTION_WIDE_ZOOM
- VIDEO_MODE_FIXED_480i
- VIDEO_MODE_FIXED_480p
- VIDEO_MODE_FIXED_720p
- VIDEO_MODE_FIXED_1080i
- VIDEO_MODE_HYBRID
- VIDEO_MODE_HYBRID_720p
- VIDEO_MODE_HYBRID_1080i
- VIDEO_MODE_NATIVE

## Full Example

### `tivo.things` Example

```java
tivo:sckt:Living_Room "Living Room TiVo" [ host="192.168.0.19" ]

```

### `tivo.items` Example

```java
String      TiVo_Status         "Status"          {channel="tivo:sckt:Living_Room:dvrStatus"}
String      TiVo_MenuScreen     "Menu Screen"     {channel="tivo:sckt:Living_Room:menuTeleport"}
Number      TiVo_SetChannel     "Current Channel" {channel="tivo:sckt:Living_Room:channelSet"}
Number      TiVo_SetChannelName "Channel Name     [MAP(tivo.map):%s]" {channel="tivo:sckt:Living_Room:channelSet"}
Number      TiVo_ForceChannel   "Force Channel"   {channel="tivo:sckt:Living_Room:channelForce"}
Number      TiVo_Recording      "Recording        [MAP(tivo.map):rec-%s]" {channel="tivo:sckt:Living_Room:isRecording"}
String      TiVo_IRCmd          "Ir Cmd"          {channel="tivo:sckt:Living_Room:irCommand"}
String      TiVo_KbdCmd         "Keyboard Cmd"    {channel="tivo:sckt:Living_Room:kbdCommand"}
String      TiVo_KeyboardStr    "Search String"
```

- The item `TiVo_SetChannelName` depends upon a valid `tivo.map` file to translate channel numbers to channel names. The openHAB **MAP** transformation service must also be installed.
- A simulated remote control widget is available using the Buttongrid sitemap element described below.
- A more advanced simulated remote can also be implemented as described here: (<https://community.openhab.org/t/bogob-big-ol-grid-o-buttons-is-this-even-possible-yes-yes-it-is/115343>).

### `tivo.sitemap` Example

```perl
sitemap tivo label="Tivo Central" {
    Frame label="Tivo" {
        Text        item=TiVo_SetChannel      label="Current Channel [%s]"  icon="screen"
        Text        item=TiVo_SetChannelName  label="Channel Name"   icon="screen"
        Text        item=TiVo_Recording       label="Recording"      icon="screen"
        // Note the TiVo_IRCmd Switch elements are deprecated in favor of the Buttongrid element below
        Switch      item=TiVo_IRCmd           label="Channel"        icon="screen"   mappings=["CHANNELDOWN"="CH -","CHANNELUP"="CH +"]
        Switch      item=TiVo_IRCmd           label="Media"          icon="screen"   mappings=["REVERSE"="⏪", "PAUSE"="⏸", "PLAY"="▶", /*(DVD TiVo only!) "STOP"="⏹",*/ "FORWARD"="⏩", "RECORD"="⏺" ]
        Switch      item=TiVo_MenuScreen      label="Menus"          icon="screen"   mappings=["TIVO"="Home", "LIVETV"="Live Tv", "GUIDE"="Guide", "NOWPLAYING"="My Shows", "NETFLIX"="Netflix", SEARCH="Search" ]
        Switch      item=TiVo_SetChannel      label="Fav TV"         icon="screen"   mappings=[2.1="CBS", 4.1="NBC", 7.1="ABC", 11.1="FOX", 5.2="AntennaTV"]
        Switch      item=TiVo_IRCmd           label="Navigation"     icon="screen"   mappings=["UP"="˄", "DOWN"="˅", "LEFT"="<", "RIGHT"=">", "SELECT"="Select", "EXIT"="Exit" ]
        Switch      item=TiVo_IRCmd           label="Actions"        icon="screen"   mappings=["ACTION_A"="Red","ACTION_B"="Green","ACTION_C"="Yellow","ACTION_D"="Blue"]
        Switch      item=TiVo_IRCmd           label="Likes"          icon="screen"   mappings=["THUMBSUP"="Thumbs Up", "THUMBSDOWN"="Thumbs Down"]
        Switch      item=TiVo_IRCmd           label="Remote"         icon="screen"   mappings=["FIND_REMOTE"="Find Remote"]
        Switch      item=TiVo_IRCmd           label="Standby"        icon="screen"   mappings=["STANDBY"="Standby","TIVO"="Wake Up"]
        Input       item=TiVo_KeyboardStr     label="Search"         staticIcon=zoom inputHint="text"
        Buttongrid  item=TiVo_IRCmd           label="Remote Control" staticIcon=material:tv_remote buttons=[1:1:GUIDE="Guide", 1:2:TIVO="Home", 1:3:LIVETV="LiveTV", 2:2:UP="Up"=f7:arrowtriangle_up, 3:1:LEFT="Left"=f7:arrowtriangle_left, 3:2:SELECT="OK", 3:3:RIGHT="Right"=f7:arrowtriangle_right, 4:2:DOWN="Down"=f7:arrowtriangle_down, 5:1:BACK="Back", 5:2:INFO="Info", 5:3:EXIT="Exit", 6:1:THUMBSUP="Thumbs Up"=f7:hand_thumbsup, 6:3:CHANNELUP="Channel +", 7:1:THUMBSDOWN="Thumbs Down"=f7:hand_thumbsdown, 7:3:CHANNELDOWN="Channel -", 8:2:PLAY="Play"=f7:play, 9:1:REVERSE="Reverse"=f7:backward, 9:2:PAUSE="Pause"=f7:pause, 9:3:FORWARD="Forward"=f7:forward, 10:2:SLOW="Slow"=f7:play_circle, 11:1:REPLAY="Replay", 11:2:RECORD="Record"=f7:circle, 11:3:ADVANCE="Advance", 12:1:ACTION_A="A (Yellow)", 12:2:ACTION_B="B (Blue)", 12:3:ACTION_C="C (Red)", 13:1:ACTION_D="D (Green)", 13:2:CC_ON="CC On",  13:3:CC_OFF="CC Off", 14:1:NUM1="1", 14:2:NUM2="2", 14:3:NUM3="3", 15:1:NUM4="4", 15:2:NUM5="5", 15:3:NUM6="6", 16:1:NUM7="7", 16:2:NUM8="8", 16:3:NUM9="9", 17:1:CLEAR="Clear", 17:2:NUM0="0", 17:3:ENTER="Enter", 18:1:STANDBY="Stand By", 18:3:FIND_REMOTE="Find Remote"]
    }
}
```

- This example does not use the 'Current Channel - Forced (FORCECH)' channel. This method will interrupt your recordings in progress when all your tuners are busy, so it is omitted for safety's sake.

### `tivo.map` Example

```text
NULL=Unknown
-=Unknown
rec-NULL=Unknown
rec-=Unknown
rec-0=Not Recording
rec-1=Recording
100=HBO
101=TNT
102=BBC America
103=ITV
104=Channel 4
105=Channel 5
2.1=CBS
2.2=StartTv
4.1=NBC

etc...
```

### `tivo.rules` Example

- The following rule shows how a string change to the item `TiVo_KeyboardStr` is split into individual characters and sent to the Tivo.

```java
rule "TiVo Search"
when
    Item TiVo_KeyboardStr received update
then
    if (newState != NULL && newState.toString.length > 0) {

        // Command to get us to the TiVo search menu
        sendCommand(TiVo_MenuScreen, "SEARCH")
        Thread::sleep(1000)

        var i = 0
        var char txt = ""
        var srch = newState.toString.toUpperCase
        logInfo("tivo.search"," Searching for: " + srch)

        while (i < (srch.length)) {
            logDebug("tivo.search"," Loop i=: " + i)
            txt = srch.charAt(i)
            logDebug("tivo.search"," txt: " + txt.toString)
            if (txt.toString.matches("[A-Z]")) {
                // Check for upper case A-Z
                sendCommand(TiVo_KbdCmd, txt.toString)
            } else if (txt.toString.matches(" ")) {
                // Check for Space
                sendCommand(TiVo_KbdCmd, "SPACE")
            } else if (txt.toString.matches("[0-9]")) {
                // Check for numbers 0-9
                sendCommand(TiVo_KbdCmd, "NUM" + txt.toString)
            } else {
                logWarn("tivo.search"," Character not supported by script: " + txt)
            }
            i++
        }
    }
end
```
