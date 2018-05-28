# Denon / Marantz Binding

This binding integrates Denon & Marantz AV receivers by using either Telnet or a (undocumented) HTTP API.

## Introduction

This binding is an adaptation of the existing Denon 1.x binding.
It can be fully configured without any configuration files.
In most cases the AVRs can be discovered and will be added to the Inbox of the Paper UI.

## Supported Things

This binding supports Denon and Marantz receivers having a Telnet interface or a web based controller at `http://<AVR IP address>/`.

Tested models: Marantz SR5008, Denon AVR-X2000 / X3000 / X1200W / X2100W / X2200W / X3100W / X3300W

Denon models with HEOS support (`AVR-X..00H`) do not support the HTTP API. They do support Telnet.
During Discovery this is auto-detected and configured.

## Discovery

This binding can discover Denon and Marantz receivers using mDNS.
The serial number (which is the MAC address of the network interface) is used as unique identifier.

It tries to detect the number of zones (when the AVR responds to HTTP). It defaults to 2 zones.

## Binding Configuration

The AVR should be auto-discovered correctly.
In case it does not work you can add the AVR manually.
There are no configuration files for this binding.

## Thing Configuration

The DenonMarantz AVR thing requires the `host` it can connect to.
There are more parameters which all have defaults set.

| Parameter           | Values                                    | Default |
|---------------------|-------------------------------------------|---------|
| host                | hostname / IP address of the AVR          | -       |
| zoneCount           | [1, 2 or 3]                               | 2       |
| telnetEnabled       | true, false                               | false   |
| telnetPort          | port number, e.g. 23                      | 23      |
| httpPort            | port number, e.g. 80                      | 80      |
| httpPollingInterval | polling interval in seconds (minimal 5)   | 5       |

### Static definition in a .things file

Example  `.things` file entry:

```
Thing denonmarantz:avr:0005cd123456 "Receiver" @ "Living room" [host="192.168.1.100"]
```

## Channels

The DenonMarantz AVR supports the following channels (some channels are model specific):

| Channel Type ID         | Item Type    | Description  |
|-------------------------|--------------|--------------|
| General
|  general#power            | Switch (RW) | Power on/off 
|  general#surroundProgram  | String (R) | current surround program (e.g. STEREO)
|  general#artist | String (R) | artist of current track
|  general#album | String (R) |  album of current track
|  general#track | String (R) |  title of current track
|  general#command          | String (W) | Command to send to the AVR (for use in Rules)
|Now Playing
| Main zone
|  mainZone#power    | Switch (RW) | Main zone power on/off
|  mainZone#volume       | Dimmer (RW) | Main zone volume
|  mainZone#volumeDB     | Number (RW) | Main zone volume in dB (-80 offset)
|  mainZone#mute             | Switch (RW) | Main zone mute
|  mainZone#input            | String (RW) | Main zone input (e.g. TV, TUNER, ..)
|  Zone 2
|  zone2#power | Switch (RW) | Zone 2 power on/off
|  zone2#volume | Dimmer (RW) | Zone 2 volume
|  zone2#volumeDB | Number (RW) | Zone 2 volume in dB (-80 offset)
|  zone2#mute | Switch (RW) | Zone 2 mute
|  zone2#input | String (RW) | Zone 2 input
|  Zone 3
|  zone3#power | Switch (RW) | Zone 3 power on/off
|  zone3#volume | Dimmer (RW) | Zone 3 volume
|  zone3#volumeDB | Number (RW) | Zone 3 volume in dB (-80 offset)
|  zone3#mute | Switch (RW) | Zone 3 mute
|  zone3#input | String (RW) | Zone 3 input

(R) = read-only (no updates possible)
(RW) = read-write
(W) = write-only (no feedback)

## Item Configuration

Example of usage in `.items` files.

```
Switch marantz_power    "Receiver" <switch>         {channel="denonmarantz:avr:0006781d58b1:general#power"}
Dimmer marantz_volume   "Volume"   <soundvolume>    {channel="denonmarantz:avr:0006781d58b1:mainZone#volume"}
Number marantz_volumeDB "Volume [%.1f dB]"          {channel="denonmarantz:avr:0006781d58b1:mainzone#volume"}
Switch marantz_mute     "Mute"     <mute>           {channel="denonmarantz:avr:0006781d58b1:mainZone#mute"}
Switch marantz_z2power  "Zone 2"                    {channel="denonmarantz:avr:0006781d58b1:zone2#power"}
String marantz_input    "Input [%s]"                {channel="denonmarantz:avr:0006781d58b1:mainZone#input" }
String marantz_surround "Surround: [%s]"            {channel="denonmarantz:avr:0006781d58b1:general#surroundProgram"}
String marantz_command                              {channel="denonmarantz:avr:0006781d58b1:general#command"}
```

## Sitemap Configuration

Example of displaying the items in a `.sitemap` file.

```
...
Group item=marantz_input label="Receiver" icon="receiver" {
    Default   item=marantz_power
    Default   item=marantz_mute      visibility=[marantz_power==ON]
    Setpoint  item=marantz_volume    label="Volume [%.1f]" minValue=0 maxValue=40 step=0.5  visibility=[marantz_power==ON]
    Default   item-marantz_volumeDB  visibility=[marantz_power==ON]
    Selection item=marantz_input     mappings=[TV=TV,MPLAY=Kodi]  visibility=[marantz_power==ON]
    Default   item=marantz_surround  visibility=[marantz_power==ON]
}
...
```

## Using the command channel

In a `.rules` file you can use the sendCommand function to send a command to the AVR.

```
marantz_command.sendCommand("MSMCH STEREO")
```

## Control protocol documentation

These resources can be useful to learn what to send using the `command`channel:

- [AVR-X2000/E400](http://www2.aerne.com/Public/dok-sw.nsf/0c6187bc750a16fcc1256e3c005a9740/96a2ba120706d10dc1257bdd0033493f/$FILE/AVRX2000_E400_PROTOCOL(10.1.0)_V04.pdf)
- [AVR-X4000](https://usa.denon.com/us/product/hometheater/receivers/avrx4000?docname=AVRX4000_PROTOCOL(10%203%200)_V03.pdf)
- [AVR-3311CI/AVR-3311/AVR-991](http://www.awe-europe.com/documents/Control%20Docs/Denon/Archive/AVR3311CI_AVR3311_991_PROTOCOL_V7.1.0.pdf)
- [CEOL Piccolo DRA-N5/RCD-N8](http://www.audioproducts.com.au/downloadcenter/products/Denon/CEOLPICCOLOBK/Manuals/DRAN5_RCDN8_PROTOCOL_V.1.0.0.pdf)
- [Marantz Control Protocol (2014+)](http://m.us.marantz.com/DocumentMaster/US/Marantz%202014%20NR%20Series%20-%20SR%20Series%20RS232%20IP%20Protocol.xls)

