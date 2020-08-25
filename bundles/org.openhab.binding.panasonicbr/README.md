# Panasonic Blu-ray Player Binding

This binding connects Panasonic Blu-ray players from 2011/2012 and UHD players from 2019.
Supported Blu-ray models: DMP-BDT110, DMP-BDT210, DMP-BDT310, DMP-BDT-120, DMP-BDT220, DMP-BDT320, DMP-BBT01 & DMP-BDT500. 
Supported UHD models: DP-UB420, DP-UB820 & DP-UB9000.

TBD...

## Supported Things

There is exactly one supported thing type, which represents the player.
It has the `player` id.
Multiple Things can be added if more than one player is to be controlled.

## Discovery

Auto-discovery is supported if the player can be located on the local network using UPNP.
Otherwise the thing must be manually added.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

|    Parameter    | Description                                                                                           |
|-----------------|-------------------------------------------------------------------------------------------------------|
| hostName        | The host name or IP address of the Blu-ray player. Mandatory.                                         |
| refresh         | Overrides the refresh interval of the player status. Optional, the default and minumum is 10 seconds. |

## Channels

The status information that is retrieved from the player is available as these channels:

| Channel ID   | Item Type            | Description                                                                         |
|--------------|----------------------|-------------------------------------------------------------------------------------|
| button       | String               | Sends a remote command to control the player. See list of available commands below. |
| play_mode    | String               | The current playback mode ie: Stopped, Playing, Paused (ReadOnly)                   |
| time_elapsed | Number:Time          | The total number of seconds of playback time elapsed (ReadOnly)                     |

## Full Example

panasonicbr.things:

```java
panasonicbr:player:myplayer1 "My Blu-ray player" [ hostName="192.168.10.1", refresh=10 ]
```

panasonicbr.items:

```java
String Player_Button            "Send Command to Player"    { channel="panasonicbr:player:myplayer1:button" }
String Player_Play_Mode         "Status: [%s]"              { channel="panasonicbr:player:myplayer1:play_mode" }
Number:Time Player_Time_Elapsed "Elapsed Time: [%d %unit%]" { channel="panasonicbr:player:myplayer1:time_elapsed" }

```

panasonicbr.sitemap:

```perl
sitemap panasonicbr label="Panasonic" {
    Frame label="My Blu-ray Player" {
        Selection item=Player_Button
        Text item=Player_Play_Mode
        Text item=Player_Time_Elapsed
    }
}
```
