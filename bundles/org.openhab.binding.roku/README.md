# Roku Binding

This binding connects Roku streaming media players and Roku TVs to openHAB.
The Roku device must support the Roku ECP protocol REST API.

## Supported Things

There are two supported thing types, which represent either a standalone Roku device or a Roku TV.
A supported Roku streaming media player or streaming stick uses the `roku_player` id and a supported Roku TV uses the `roku_tv` id.
The binding functionality is the same for both types, but the Roku TV type adds additional button commands to the button channel dropdown.
Multiple Things can be added if more than one Roku is to be controlled.

## Discovery

Auto-discovery is supported if the Roku can be located on the local network using SSDP.
Otherwise the thing must be manually added.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                                                                |
|-----------|------------------------------------------------------------------------------------------------------------|
| hostName  | The host name or IP address of the Roku device. Mandatory.                                                 |
| port      | The port on the Roku that listens for http connections. Default 8060                                       |
| refresh   | Overrides the refresh interval for player status updates. Optional, the default and minimum is 10 seconds. |

## Channels

The following channels are available:

| Channel ID      | Item Type   | Description                                                                                                                                             |
|-----------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| activeApp       | String      | A dropdown containing a list of all apps installed on the Roku. The app currently running is automatically selected. The list updates every 10 minutes. |
| button          | String      | Sends a remote control command the Roku. See list of available commands below.                                                                          |
| playMode        | String      | The current playback mode ie: stop, play, pause (ReadOnly).                                                                                             |
| timeElapsed     | Number:Time | The total number of seconds of playback time elapsed for the current playing title (ReadOnly).                                                          |
| timeTotal       | Number:Time | The total length of the current playing title in seconds (ReadOnly). This data is not provided by all streaming apps.                                   |

Some Notes:

* The values for `activeApp`, `playMode`, `timeElapsed` & `timeTotal` refresh automatically per the configured `refresh` interval (10 seconds minimum).

**List of available button commands for Roku streaming devices:**  
Home  
Rev  
Fwd  
Play  
Select  
Left  
Right  
Up  
Down  
Back  
InstantReplay  
Info  
Backspace  
Search  
Enter  
FindRemote  
  
**List of additional button commands for Roku TVs:**  
ChannelUp  
ChannelDown  
VolumeUp  
VolumeDown  
VolumeMute  
InputTuner  
InputHDMI1  
InputHDMI2  
InputHDMI3  
InputHDMI4  
InputAV1  
PowerOff  

## Full Example

roku.things:

```java
roku:roku_player:myplayer1 "My Roku" [ hostName="192.168.10.1", refresh=10 ]
roku:roku_tv:myplayer1 "My Roku TV" [ hostName="192.168.10.1", refresh=10 ]
```

roku.items:

```java
String Player_ActiveApp         "Current App: [%s]"         { channel="roku:roku_player:myplayer1:activeApp" }
String Player_Button            "Send Command to Roku"      { channel="roku:roku_player:myplayer1:button" }
String Player_PlayMode         "Status: [%s]"              { channel="roku:roku_player:myplayer1:playMode" }
Number:Time Player_TimeElapsed "Elapsed Time: [%d %unit%]" { channel="roku:roku_player:myplayer1:timeElapsed" }
Number:Time Player_TimeTotal   "Total Time: [%d %unit%]"   { channel="roku:roku_player:myplayer1:timeTotal" }
```

roku.sitemap:

```perl
sitemap roku label="Roku" {
    Frame label="My Roku" {
        Selection item=Player_ActiveApp icon="screen"
        Selection item=Player_Button icon="screen"
        Text item=Player_PlayMode
        Text item=Player_TimeElapsed icon="time"
        Text item=Player_TimeTotal icon="time"
    }
}
```
