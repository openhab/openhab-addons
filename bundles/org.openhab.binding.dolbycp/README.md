# DolbyCP Binding

This binding is intended to connect to a _Dolby Digital Cinema Processor CP750_.
Support for CP950 may be added in future (if someone who owns one helps to test it).

It uses ASCII commands send over a TCP connection on port 61408.
Please note that the CP750 only accepts up to 20 simultaneous connection and will discard the oldest connection if a 21st connection is established.
So be sure to grateful shutdown each connection using the client's close() method, or it's AutoCloseable functionality.

This binding wraps the CP750 Java library from https://github.com/Cybso/cp750-java/.

This project is NOT affiliated with, funded, or in any way associated with Dolby Laboratories, Inc.

## Supported Things

- `cp750` - The Dolby Digital Cinema Processor CP750 Device.

## Thing Configuration

Normally, only the hostname or IP address must be configured.

### `cp750` Thing Configuration

| Name              | Type    | Description                                                  | Default | Required | Advanced |
|-------------------|---------|--------------------------------------------------------------|---------|----------|----------|
| hostname          | text    | Hostname or IP address of the device                         | N/A     | yes      | no       |
| port              | integer | TCP port if different from 61408                             | 61408   | no       | no       |
| refreshInterval   | integer | Interval the device is polled in seconds                     | 5       | no       | no       |
| reconnectInterval | integer | Interval a new connection is tried after IO error in seconds | 10      | no       | no       |

## Properties

| Name              | Description                                              |
|-------------------|----------------------------------------------------------|
| osVersion         | The operating system's version as returned by the device |

## Channels

These channels can be used to retrieve the current device state and change the controls.

The input mode can be either be controlled by the "input" string channel, or by writing an ON value to the dedicated switch channels, which represents the physical buttons on the CP750 device.

| Channel    | Type   | Read/Write | Description                                                                                                     |
|------------|--------|------------|-----------------------------------------------------------------------------------------------------------------|
| fader      | Dimmer | RW         | Fader value (0 to 100)                                                                                          |
| mute       | Switch | RW         | Mute (ON or OFF)                                                                                                |
| input      | String | RW         | Input channel as string (one of "analog", "dig_1", "dig_2", "dig_3", "dig_4", "non-sync" or "mic")              |
| analog     | Switch | RW         | Is ON if input mode is 'analog'. When an ON command is retrieved, input mode will be changed to this channel.   |
| dig1       | Switch | RW         | Is ON if input mode is 'dig_1'. When an ON command is retrieved, input mode will be changed to this channel.    |
| dig2       | Switch | RW         | Is ON if input mode is 'dig_2'. When an ON command is retrieved, input mode will be changed to this channel.    |
| dig3       | Switch | RW         | Is ON if input mode is 'dig_3'. When an ON command is retrieved, input mode will be changed to this channel.    |
| dig4       | Switch | RW         | Is ON if input mode is 'dig_4'. When an ON command is retrieved, input mode will be changed to this channel.    |
| nonsync    | Switch | RW         | Is ON if input mode is 'non-sync'. When an ON command is retrieved, input mode will be changed to this channel. |
| mic        | Switch | RW         | Is ON if input mode is 'mic'. When an ON command is retrieved, input mode will be changed to this channel.      |

## Full Example

The following example is for a device connected at IP 192.168.1.135 on port 61408 with all channels linked to items.

demo.things:

```java
Thing dolbycp:cp750:myCp750 "CP750" @ "Projector Room" [hostname="192.168.1.135", port=61408, refreshInterval=5, reconnectInterval=10] {
    Channels:
        Type fader : myFader "Fader control"
        Type mute : myMute "Mute control"
        Type input : myInput "Input mode control"
        Type analog : myAnalogBtn "Input Mode 'analog' switch control"
        Type dig1 : myDig1Btn "Input Mode 'Digital 1' switch control"
        Type dig2 : myDig2Btn "Input Mode 'Digital 2' switch control"
        Type dig3 : myDig3Btn "Input Mode 'Digital 3' switch control"
        Type dig4 : myDig4Btn "Input Mode 'Digital 4' switch control"
        Type nonsync : myNonSyncBtn "Input Mode 'Non-Sync' switch control"
        Type mic : myMicBtn "Input Mode 'Microphone' switch control"
}
```

demo.items:

```java
Dimmer mycp750_volume                "Volume [%d]"               { channel="dolbycp:cp750:myCp750:myFader" }
Switch mycp750_mute                  "Mute"                      { channel="dolbycp:cp750:myCp750:myMute" }
String mycp750_input                 "Input Mode [%s]"           { channel="dolbycp:cp750:myCp750:myInput" }
Switch mycp750_analog                "Input Mode Analog"         { channel="dolbycp:cp750:myCp750:myAnalogBtn" }
Switch mycp750_dig1                  "Input Mode Digital 1"      { channel="dolbycp:cp750:myCp750:myDig1Btn" }
Switch mycp750_dig2                  "Input Mode Digital 2"      { channel="dolbycp:cp750:myCp750:myDig2Btn" }
Switch mycp750_dig3                  "Input Mode Digital 3"      { channel="dolbycp:cp750:myCp750:myDig3Btn" }
Switch mycp750_dig4                  "Input Mode Digital 4"      { channel="dolbycp:cp750:myCp750:myDig4Btn" }
Switch mycp750_nonsyc                "Input Mode Non-Sync"       { channel="dolbycp:cp750:myCp750:myNonSyncBtn" }
Switch mycp750_mic                   "Input Mode Microphone"     { channel="dolbycp:cp750:myCp750:myMicBtn" }
```
