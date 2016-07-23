# Onkyo Binding

This binding integrates the Onkyo AV receivers.

## Introduction

Binding should be compatible with Onkyo AV receivers which support ISCP (Integra Serial Control Protocol) over Ethernet (eISCP).

## Supported Things

This binding supports only one thing: The Onkyo AV Receiver


## Discovery

This binding can discover the supported Onkyo AV Receivers. At the moment only the following models are supported:

* TX-NR535

## Binding Configuration

The binding can auto-discover the Onkyo AVRs present on your local network. The auto-discovery is enabled by default. To disable it, you can create a file in the services directory called onkyo.cfg with the following content:

```
org.openhab.onkyo:enableAutoDiscovery=false
```

This configuration parameter only controls the Onkyo AVR auto-discovery process, not the openHAB auto-discovery. Moreover, if the openHAB auto-discovery is disabled, the Onkyo AVR auto-discovery is disabled too.


## Thing Configuration

The Onkyo AVR thing requires the ip address and the port to access it on.
In the thing file, this looks e.g. like
```
onkyo:onkyoAV:myOnkyo [ipAddress="192.168.1.100", port="60128"]


##Channels

The Onkyo AVR supports the following channels:

| Channel Type ID         | Item Type    | Description  |
|-------------------------|--------------|--------------|
| power                   | Switch       | Power on/off your device |
| mute                    | Switch       | Mute/unmute your device |
| input                   | Number       | The input for the AVR    |
| volume                  | Dimmer       | Volume of your device |
| control                 | Player       | Control the Zone Player, e.g.  play/pause/next/previous/ffward/rewind (available if playing from Network or USB)|
| title                   | String       | Title of the current song (available if playing from Network or USB)|
| album                   | String       | Album name of the current song (available if playing from Network or USB)|
| artist                  | String       | Artist name of the current song (available if playing from Network or USB)|
| currentPlayingTime      | String       | Current playing time of the current song (available if playing from Network or USB)|
| listenmode              | Number       | Current listening mode e.g. Stero, 5.1ch Surround,..|

##Input Source Mapping

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
* 40: INTERETRADIO
* 41: USB
* 42: USB_BACK
* 43: NETWORK
* 45: AIRPLAY
* 48: MULTICH
* 50: SIRIUS

