# Denon / Marantz Binding

This binding integrates Denon & Marantz AV receivers by using either Telnet or a (undocumented) HTTP API.

## Supported Things

This binding supports Denon and Marantz receivers having a Telnet interface or a web based controller at `http://<AVR IP address>/`.
The thing type for all of them is `avr`.

Tested models: Marantz SR5008, Denon AVR-3808 / AVR-4520 / AVR-X2000 / X3000 / X1200W / X2100W / X2200W / X3100W / X3300W / X4400H / X4800H

## Discovery

This binding can discover Denon and Marantz receivers using mDNS.
The serial number (which is the MAC address of the network interface) is used as unique identifier.

The protocol will be auto-detected.
The HTTP port as well as slight variations in the API will be auto-detected as well.

It tries to detect the number of zones (when the AVR responds to HTTP).
It defaults to two zones.

## Thing Configuration

The DenonMarantz AVR thing requires the `host` it can connect to.
There are more parameters which all have defaults set.

| Parameter           | Values                                    | Default |
|---------------------|-------------------------------------------|---------|
| host                | hostname / IP address of the AVR          | -       |
| zoneCount           | [1, 2, 3 or 4]                            | 2       |
| telnetEnabled       | true, false                               | false   |
| telnetPort          | port number, e.g. 23                      | 23      |
| httpPort            | port number, e.g. 80                      | 80 (1)  |
| httpPollingInterval | polling interval in seconds (minimal 5)   | 5       |

(1) Models >= 2016 use port 8080 and have a slightly different API

## Channels

The DenonMarantz AVR supports the following channels (some channels are model specific):

| Channel ID                | Item Type                 | Description                                   |
|---------------------------|---------------------------|-----------------------------------------------|
| _General_                 |                           |                                               |
|  general#power            | Switch (RW)               | Power on/off                                  |
|  general#surroundProgram  | String (R)                | Current surround program (e.g. STEREO)        |
|  general#artist           | String (R)                | Artist of current track                       |
|  general#album            | String (R)                | Album of current track                        |
|  general#track            | String (R)                | Title of current track                        |
|  general#command          | String (W)                | Command to send to the AVR (for use in Rules) |
| _Main zone_               |                           |                                               |
|  mainZone#power           | Switch (RW)               | Main zone power on/off                        |
|  mainZone#volume          | Dimmer (RW)               | Main zone volume                              |
|  mainZone#volumeDB        | Number:Dimensionless (RW) | Main zone volume in dB (-80 offset)           |
|  mainZone#mute            | Switch (RW)               | Main zone mute                                |
|  mainZone#input           | String (RW)               | Main zone input (e.g. TV, TUNER, ..)          |
|  _Zone 2_                 |                           |                                               |
|  zone2#power              | Switch (RW)               | Zone 2 power on/off                           |
|  zone2#volume             | Dimmer (RW)               | Zone 2 volume                                 |
|  zone2#volumeDB           | Number:Dimensionless (RW) | Zone 2 volume in dB (-80 offset)              |
|  zone2#mute               | Switch (RW)               | Zone 2 mute                                   |
|  zone2#input              | String (RW)               | Zone 2 input                                  |
|  _Zone 3_                 |                           |                                               |
|  zone3#power              | Switch (RW)               | Zone 3 power on/off                           |
|  zone3#volume             | Dimmer (RW)               | Zone 3 volume                                 |
|  zone3#volumeDB           | Number:Dimensionless (RW) | Zone 3 volume in dB (-80 offset)              |
|  zone3#mute               | Switch (RW)               | Zone 3 mute                                   |
|  zone3#input              | String (RW)               | Zone 3 input                                  |
|  _Zone 4_                 |                           |                                               |
|  zone4#power              | Switch (RW)               | Zone 4 power on/off                           |
|  zone4#volume             | Dimmer (RW)               | Zone 4 volume                                 |
|  zone4#volumeDB           | Number:Dimensionless (RW) | Zone 4 volume in dB (-80 offset)              |
|  zone4#mute               | Switch (RW)               | Zone 4 mute                                   |
|  zone4#input              | String (RW)               | Zone 4 input                                  |

(R) = read-only (no updates possible),
(RW) = read-write,
(W) = write-only (no feedback)

## Full Example

`.things` file:

```java
Thing denonmarantz:avr:1 "Receiver" @ "Living room" [host="192.168.1.100"]
```

`.items` file:

```java
Switch               marantz_power    "Receiver" <switch>         {channel="denonmarantz:avr:1:general#power"}
Dimmer               marantz_volume   "Volume"   <soundvolume>    {channel="denonmarantz:avr:1:mainZone#volume"}
Number:Dimensionless marantz_volumeDB "Volume [%.1f dB]"          {channel="denonmarantz:avr:1:mainzone#volume", unit="dB"}
Switch               marantz_mute     "Mute"     <mute>           {channel="denonmarantz:avr:1:mainZone#mute"}
Switch               marantz_z2power  "Zone 2"                    {channel="denonmarantz:avr:1:zone2#power"}
String               marantz_input    "Input [%s]"                {channel="denonmarantz:avr:1:mainZone#input" }
String               marantz_surround "Surround: [%s]"            {channel="denonmarantz:avr:1:general#surroundProgram"}
String               marantz_command                              {channel="denonmarantz:avr:1:general#command"}
```

`.sitemap` file:

```perl
...
Group item=marantz_input label="Receiver" icon="receiver" {
    Default   item=marantz_power
    Default   item=marantz_mute      visibility=[marantz_power==ON]
    Setpoint  item=marantz_volume    label="Volume [%.1f]" minValue=0 maxValue=40 step=0.5  visibility=[marantz_power==ON]
    Default   item=marantz_volumeDB  visibility=[marantz_power==ON]
    Selection item=marantz_input     mappings=[TV=TV,MPLAY=Kodi]  visibility=[marantz_power==ON]
    Default   item=marantz_surround  visibility=[marantz_power==ON]
}
...
```

## Control Protocol Reference

These resources can be useful to learn what to send using the `command`channel:

- [AVR-X2000/E400](https://assets.denon.com/documentmaster/uk/avrx2000_e400_protocol(1010)_v03.pdf)
- [AVR-X4000](https://usa.denon.com/us/product/hometheater/receivers/avrx4000?docname=AVRX4000_PROTOCOL(10%203%200)_V03.pdf)
- [AVR-3311CI/AVR-3311/AVR-991](https://www.awe-europe.com/documents/Control%20Docs/Denon/Archive/AVR3311CI_AVR3311_991_PROTOCOL_V7.1.0.pdf)
- [Denon/Marantz Control Protocol](https://assets.eu.denon.com/DocumentMaster/DE/AVR1713_AVR1613_PROTOCOL_V8.6.0.pdf)
- [Denon DRA-100 Control Protocol](https://assets.denon.com/DocumentMaster/RU/DRA-100_PROTOCOL_Ver100.pdf)
