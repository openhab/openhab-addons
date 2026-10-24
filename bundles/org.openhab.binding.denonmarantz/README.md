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

| Channel ID                        | Item Type                 | Description                                             | Protocol      |
|-----------------------------------|---------------------------|---------------------------------------------------------|---------------|
| _General_                         |                           |                                                         |               |
| general#power                     | Switch (RW)               | Power on/off                                            | HTTP + Telnet |
| general#surroundProgram           | String (R)                | Current surround program (e.g. STEREO)                  | HTTP + Telnet |
| general#allZoneStereo             | Switch (RW)               | All zone stereo on/off                                  | Telnet        |
| general#speakerPreset             | Number:Dimensionless (RW) | Speaker preset 1/2                                      | Telnet        |
| general#artist                    | String (R)                | Artist of current track                                 | HTTP + Telnet |
| general#album                     | String (R)                | Album of current track                                  | HTTP + Telnet |
| general#track                     | String (R)                | Title of current track                                  | HTTP + Telnet |
| general#command                   | String (W)                | Command to send to the AVR (for use in Rules)           | HTTP + Telnet |
| _Main zone_                       |                           |                                                         |               |
| mainZone#power                    | Switch (RW)               | Main zone power on/off                                  | HTTP + Telnet |
| mainZone#volume                   | Dimmer (RW)               | Main zone volume                                        | HTTP + Telnet |
| mainZone#volumeDB                 | Number:Dimensionless (RW) | Main zone volume in dB (-80 offset)                     | HTTP + Telnet |
| mainZone#mute                     | Switch (RW)               | Main zone mute                                          | HTTP + Telnet |
| mainZone#input                    | String (RW)               | Main zone input (e.g. TV, TUNER, ..)                    | HTTP + Telnet |
| _Zone 2_                          |                           |                                                         |               |
| zone2#power                       | Switch (RW)               | Zone 2 power on/off                                     | HTTP + Telnet |
| zone2#volume                      | Dimmer (RW)               | Zone 2 volume                                           | HTTP + Telnet |
| zone2#volumeDB                    | Number:Dimensionless (RW) | Zone 2 volume in dB (-80 offset)                        | HTTP + Telnet |
| zone2#mute                        | Switch (RW)               | Zone 2 mute                                             | HTTP + Telnet |
| zone2#input                       | String (RW)               | Zone 2 input                                            | HTTP + Telnet |
| _Zone 3_                          |                           |                                                         |               |
| zone3#power                       | Switch (RW)               | Zone 3 power on/off                                     | HTTP + Telnet |
| zone3#volume                      | Dimmer (RW)               | Zone 3 volume                                           | HTTP + Telnet |
| zone3#volumeDB                    | Number:Dimensionless (RW) | Zone 3 volume in dB (-80 offset)                        | HTTP + Telnet |
| zone3#mute                        | Switch (RW)               | Zone 3 mute                                             | HTTP + Telnet |
| zone3#input                       | String (RW)               | Zone 3 input                                            | HTTP + Telnet |
| _Zone 4_                          |                           |                                                         |               |
| zone4#power                       | Switch (RW)               | Zone 4 power on/off                                     | HTTP + Telnet |
| zone4#volume                      | Dimmer (RW)               | Zone 4 volume                                           | HTTP + Telnet |
| zone4#volumeDB                    | Number:Dimensionless (RW) | Zone 4 volume in dB (-80 offset)                        | HTTP + Telnet |
| zone4#mute                        | Switch (RW)               | Zone 4 mute                                             | HTTP + Telnet |
| zone4#input                       | String (RW)               | Zone 4 input                                            | HTTP + Telnet |
| _Channel volume_                  |                           |                                                         |               |
| channelVolume#frontLeft           | Number:Dimensionless (RW) | Channel volume front left in dB (-50 offset)            | Telnet        |
| channelVolume#frontRight          | Number:Dimensionless (RW) | Channel volume front right in dB (-50 offset)           | Telnet        |
| channelVolume#center              | Number:Dimensionless (RW) | Channel volume center in dB (-50 offset)                | Telnet        |
| channelVolume#subwoofer           | Number:Dimensionless (RW) | Channel volume subwoofer in dB (-50 offset)             | Telnet        |
| channelVolume#subwoofer2          | Number:Dimensionless (RW) | Channel volume subwoofer 2 in dB (-50 offset)           | Telnet        |
| channelVolume#subwoofer3          | Number:Dimensionless (RW) | Channel volume subwoofer 3 in dB (-50 offset)           | Telnet        |
| channelVolume#subwoofer4          | Number:Dimensionless (RW) | Channel volume subwoofer 4 in dB (-50 offset)           | Telnet        |
| channelVolume#surroundLeft        | Number:Dimensionless (RW) | Channel volume surround left in dB (-50 offset)         | Telnet        |
| channelVolume#surroundRight       | Number:Dimensionless (RW) | Channel volume surround right in dB (-50 offset)        | Telnet        |
| channelVolume#surroundBackLeft    | Number:Dimensionless (RW) | Channel volume surround back left in dB (-50 offset)    | Telnet        |
| channelVolume#surroundBackRight   | Number:Dimensionless (RW) | Channel volume surround back right in dB (-50 offset)   | Telnet        |
| channelVolume#surroundBack        | Number:Dimensionless (RW) | Channel volume surround back in dB (-50 offset)         | Telnet        |
| channelVolume#frontHeightLeft     | Number:Dimensionless (RW) | Channel volume front height left in dB (-50 offset)     | Telnet        |
| channelVolume#frontHeightRight    | Number:Dimensionless (RW) | Channel volume front height right in dB (-50 offset)    | Telnet        |
| channelVolume#frontWideLeft       | Number:Dimensionless (RW) | Channel volume front wide left in dB (-50 offset)       | Telnet        |
| channelVolume#frontWideRight      | Number:Dimensionless (RW) | Channel volume front wide right in dB (-50 offset)      | Telnet        |
| channelVolume#topFrontLeft        | Number:Dimensionless (RW) | Channel volume top front left in dB (-50 offset)        | Telnet        |
| channelVolume#topFrontRight       | Number:Dimensionless (RW) | Channel volume top front right in dB (-50 offset)       | Telnet        |
| channelVolume#topMiddleLeft       | Number:Dimensionless (RW) | Channel volume top middle left in dB (-50 offset)       | Telnet        |
| channelVolume#topMiddleRight      | Number:Dimensionless (RW) | Channel volume top middle right in dB (-50 offset)      | Telnet        |
| channelVolume#topRearLeft         | Number:Dimensionless (RW) | Channel volume top rear left in dB (-50 offset)         | Telnet        |
| channelVolume#topRearRight        | Number:Dimensionless (RW) | Channel volume top rear right in dB (-50 offset)        | Telnet        |
| channelVolume#rearHeightLeft      | Number:Dimensionless (RW) | Channel volume rear height left in dB (-50 offset)      | Telnet        |
| channelVolume#rearHeightRight     | Number:Dimensionless (RW) | Channel volume rear height right in dB (-50 offset)     | Telnet        |
| channelVolume#frontDolbyLeft      | Number:Dimensionless (RW) | Channel volume front Dolby left in dB (-50 offset)      | Telnet        |
| channelVolume#frontDolbyRight     | Number:Dimensionless (RW) | Channel volume front Dolby right in dB (-50 offset)     | Telnet        |
| channelVolume#surroundDolbyLeft   | Number:Dimensionless (RW) | Channel volume surround Dolby left in dB (-50 offset)   | Telnet        |
| channelVolume#surroundDolbyRight  | Number:Dimensionless (RW) | Channel volume surround Dolby right in dB (-50 offset)  | Telnet        |
| channelVolume#backDolbyLeft       | Number:Dimensionless (RW) | Channel volume back Dolby left in dB (-50 offset)       | Telnet        |
| channelVolume#backDolbyRight      | Number:Dimensionless (RW) | Channel volume back Dolby right in dB (-50 offset)      | Telnet        |
| channelVolume#surroundHeightLeft  | Number:Dimensionless (RW) | Channel volume surround height left in dB (-50 offset)  | Telnet        |
| channelVolume#surroundHeightRight | Number:Dimensionless (RW) | Channel volume surround height right in dB (-50 offset) | Telnet        |
| channelVolume#topSurround         | Number:Dimensionless (RW) | Channel volume top surround in dB (-50 offset)          | Telnet        |
| channelVolume#centerHeight        | Number:Dimensionless (RW) | Channel volume center height in dB (-50 offset)         | Telnet        |
| channelVolume#tactileTransducer   | Number:Dimensionless (RW) | Channel volume tactile transducer in dB (-50 offset)    | Telnet        |

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
Switch               marantz_power                  "Receiver" <switch>           {channel="denonmarantz:avr:1:general#power"}
Dimmer               marantz_volume                 "Volume"   <soundvolume>      {channel="denonmarantz:avr:1:mainZone#volume"}
Number:Dimensionless marantz_volumeDB               "Volume [%.1f dB]"            {channel="denonmarantz:avr:1:mainzone#volume", unit="dB"}
Switch               marantz_mute                   "Mute"     <mute>             {channel="denonmarantz:avr:1:mainZone#mute"}
Switch               marantz_z2power                "Zone 2"                      {channel="denonmarantz:avr:1:zone2#power"}
String               marantz_input                  "Input [%s]"                  {channel="denonmarantz:avr:1:mainZone#input" }
String               marantz_surround               "Surround: [%s]"              {channel="denonmarantz:avr:1:general#surroundProgram"}
Number:Dimensionless marantz_tactileTransducerLevel "Bass Shaker Level [%.1f dB]" {channel="denonmarantz:avr:1:channelVolume#tactileTransducer", unit="dB"}
String               marantz_command                                              {channel="denonmarantz:avr:1:general#command"}
```

`.sitemap` file:

```perl
...
Group item=marantz_input label="Receiver" icon="receiver" {
    Default   item=marantz_power
    Default   item=marantz_mute                   visibility=[marantz_power==ON]
    Setpoint  item=marantz_volume                 label="Volume [%.1f]" minValue=0 maxValue=40 step=0.5  visibility=[marantz_power==ON]
    Default   item=marantz_volumeDB               visibility=[marantz_power==ON]
    Selection item=marantz_input                  mappings=[TV=TV,MPLAY=Kodi]  visibility=[marantz_power==ON]
    Default   item=marantz_surround               visibility=[marantz_power==ON]
    Setpoint  item=marantz_tactileTransducerLevel visibility=[marantz_power==ON] minValue=-12 maxValue=12 step=0.5
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
