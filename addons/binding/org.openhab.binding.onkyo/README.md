# Onkyo Binding

This binding integrates the Onkyo AV receivers.

## Introduction

Binding should be compatible with Onkyo AV receivers which support ISCP (Integra Serial Control Protocol) over Ethernet (eISCP).

## Supported Things

This binding supports only one thing: The Onkyo AV Receiver.  All supported Onkyo devices are registered as an audio sink in the framework.


## Discovery

This binding can discover the supported Onkyo AV Receivers. At the moment only the following models are supported:

* TX-NR535
* TX-NR626
* TX-NR646

## Binding Configuration

The binding can auto-discover the Onkyo AVRs present on your local network. The auto-discovery is enabled by default. To disable it, you can create a file in the services directory called onkyo.cfg with the following content:

`org.openhab.onkyo:enableAutoDiscovery=false`

This configuration parameter only controls the Onkyo AVR auto-discovery process, not the openHAB auto-discovery. Moreover, if the openHAB auto-discovery is disabled, the Onkyo AVR auto-discovery is disabled too.


The binding has the following configuration options, which can be set for "binding:onkyo":

| Parameter | Name    | Description  | Required |
|-----------------|------------------------|--------------|------------ |
| callbackUrl | Callback URL | URL to use for playing notification sounds, e.g. http://192.168.0.2:8080 | no |

## Thing Configuration

The Onkyo AVR thing requires the ip address and the port to access it on.
In the thing file, this looks e.g. like

`onkyo:onkyoAV:myOnkyo [ipAddress="192.168.1.100", port="60128"]`

Optionally you can specify the refresh interval
`onkyo:onkyoAV:myOnkyo [ipAddress="192.168.1.100", port="60128", refreshInterval=30]`

## Channels

The Onkyo AVR supports the following channels:

| Channel Type ID         | Item Type    | Description  |
|-------------------------|--------------|--------------|
| zone1#power                    | Switch       | Power on/off your device |
| zone1#mute                     | Switch       | Mute/unmute zone 1 |
| zone1#input                    | Number       | The input for zone 1    |
| zone1#volume                   | Dimmer       | Volume of zone 1 |
| zone2#power                    | Switch       | Power on/off zone 2 |
| zone2#mute                     | Switch       | Mute/unmute zone 2 |
| zone2#input                    | Number       | The input for zone 2    |
| zone2#volume                   | Dimmer       | Volume of zone 2 |
| player#control                 | Player       | Control the Zone Player, e.g.  play/pause/next/previous/ffward/rewind (available if playing from Network or USB)|
| player#title                   | String       | Title of the current song (available if playing from Network or USB)|
| player#album                   | String       | Album name of the current song (available if playing from Network or USB)|
| player#artist                  | String       | Artist name of the current song (available if playing from Network or USB)|
| player#currentPlayingTime      | String       | Current playing time of the current song (available if playing from Network or USB)|
| player#listenmode              | Number       | Current listening mode e.g. Stereo, 5.1ch Surround,..|
| player#playuri                 | String       | Plays the URI provided to the channel |
| player#albumArt                | String       | Hyperlink to the current album art image |
| netmenu#title                  | String       | Title of the current NET service |
| netmenu#control                | String       | Control the USB/Net Menu, e.g. Up/Down/Select/Back/PageUp/PageDown/Select[0-9] 
| netmenu#selection              | Number       | The number of the currently selected USB/Net Menu entry (0-9) 
| netmenu#item0                  | String       | The text of USB/Net Menu entry 0
| netmenu#item1                  | String       | The text of USB/Net Menu entry 1
| netmenu#item2                  | String       | The text of USB/Net Menu entry 2 
| netmenu#item3                  | String       | The text of USB/Net Menu entry 3 
| netmenu#item4                  | String       | The text of USB/Net Menu entry 4 
| netmenu#item5                  | String       | The text of USB/Net Menu entry 5 
| netmenu#item6                  | String       | The text of USB/Net Menu entry 6 
| netmenu#item7                  | String       | The text of USB/Net Menu entry 7 
| netmenu#item8                  | String       | The text of USB/Net Menu entry 8 
| netmenu#item9                  | String       | The text of USB/Net Menu entry 9 


## Input Source Mapping

Here after are the ID values of the input sources:

* 00: DVR/VCR
* 01: SATELLITE/CABLE
* 02: GAME
* 03: AUX
* 04: GAME
* 05: PC
* 16: BLURAY/DVD
* 32: TAPE1
* 33: TAPE2
* 34: PHONO
* 35: CD
* 36: FM
* 37: AM
* 38: TUNER
* 39: MUSICSERVER
* 40: INTERNETRADIO
* 41: USB
* 42: USB_BACK
* 43: NETWORK
* 45: AIRPLAY
* 48: MULTICH
* 50: SIRIUS

## Audio Support
+
+All supported Onkyo AVRs are registered as an audio sink in the framework.
+Audio streams are sent to the `playuri` channel.
