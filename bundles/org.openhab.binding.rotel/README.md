# Rotel Binding

This binding can be used to control a Rotel audio device like a surround processor, a surround receiver, a stereo preamplifier, an integrated amplifier, a CD player or a tuner.

The binding supports different kinds of connections:

* serial connection,
* serial over IP connection,
* IP connection (for models providing a network interface).

The binding supports all kinds of Rotel protocols:

* HEX protocol,
* Old ASCII protocol (named v1 in the binding),
* Recent ASCII protocol (named v2 in the binding).

For users without serial connector on server side, of course you can add a serial to USB adapter.

You don't need to have your Rotel device directly connected to your openHAB server.
You can connect it for example to a Raspberry Pi and use [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) to make the serial connection available on LAN (serial over IP).

Recent devices provide a network interface (RJ45 connector).
So you can use the device IP to connect to the device, keeping 9590 as default port.

The binding has been tested with a RSP-1066 and a RSP-1570.

## Supported Things

This binding supports the following thing types:

| Thing Type | Description                                                   |
|------------|---------------------------------------------------------------|
| a11        | Connection to the Rotel A11 integrated amplifier              |
| a12        | Connection to the Rotel A12 integrated amplifier              |
| a14        | Connection to the Rotel A14 integrated amplifier              |
| cd11       | Connection to the Rotel CD11 CD player                        |
| cd14       | Connection to the Rotel CD14 CD player                        |
| ra11       | Connection to the Rotel RA-11 integrated amplifier            |
| ra12       | Connection to the Rotel RA-12 integrated amplifier            |
| ra1570     | Connection to the Rotel RA-1570 integrated amplifier          |
| ra1572     | Connection to the Rotel RA-1572 integrated amplifier          |
| ra1592     | Connection to the Rotel RA-1592 integrated amplifier          |
| rap1580    | Connection to the Rotel RAP-1580 surround amplified processor |
| rc1570     | Connection to the Rotel RC-1570 stereo preamplifier           |
| rc1572     | Connection to the Rotel RC-1572 stereo preamplifier           |
| rc1590     | Connection to the Rotel RC-1590 stereo preamplifier           |
| rcd1570    | Connection to the Rotel RCD-1570 CD player                    |
| rcd1572    | Connection to the Rotel RCD-1572 CD player                    |
| rcx1500    | Connection to the Rotel RCX-1500 stereo receiver              |
| rdd1580    | Connection to the Rotel RDD-1580 stereo DAC                   |
| rdg1520    | Connection to the Rotel RDG-1520 tuner                        |
| rsp1066    | Connection to the Rotel RSP-1066 surround processor           |
| rsp1068    | Connection to the Rotel RSP-1068 surround processor           |
| rsp1069    | Connection to the Rotel RSP-1069 surround processor           |
| rsp1098    | Connection to the Rotel RSP-1098 surround processor           |
| rsp1570    | Connection to the Rotel RSP-1570 surround processor           |
| rsp1572    | Connection to the Rotel RSP-1572 surround processor           |
| rsp1576    | Connection to the Rotel RSP-1576 surround processor           |
| rsp1582    | Connection to the Rotel RSP-1582 surround processor           |
| rsx1055    | Connection to the Rotel RSX-1055 surround receiver            |
| rsx1056    | Connection to the Rotel RSX-1056 surround receiver            |
| rsx1057    | Connection to the Rotel RSX-1057 surround receiver            |
| rsx1058    | Connection to the Rotel RSX-1058 surround receiver            |
| rsx1065    | Connection to the Rotel RSX-1065 surround receiver            |
| rsx1067    | Connection to the Rotel RSX-1067 surround receiver            |
| rsx1550    | Connection to the Rotel RSX-1550 surround receiver            |
| rsx1560    | Connection to the Rotel RSX-1560 surround receiver            |
| rsx1562    | Connection to the Rotel RSX-1562 surround receiver            |
| rt09       | Connection to the Rotel RT-09 tuner                           |
| rt11       | Connection to the Rotel RT-11 tuner                           |
| rt1570     | Connection to the Rotel RT-1570 tuner                         |
| t11        | Connection to the Rotel T11 tuner                             |
| t14        | Connection to the Rotel T14 tuner                             |

## Discovery

Discovery is not supported.
You have to add all things manually.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

The thing requires the following configuration parameters:

| Parameter Label         | Parameter ID     | Description                                           | Accepted values |
|-------------------------|------------------|-------------------------------------------------------|-----------------|
| Serial Port             | serialPort       | Serial port to use for connecting to the Rotel device | |
| Address                 | host             | Host name or IP address of the Rotel device (IP connection) or the machine connected to the Rotel device (serial over IP) | |
| Port                    | port             | Communication port (IP or serial over IP). For IP connection to the Rotel device, keep the default port (9590) | |
| Protocol Version        | Protocol         | Choose one of the two protocol versions (depends on your device firmware). Default is ASCII_V2 | ASCII_V1 or ASCII_V2 |
| Input Label CD          | inputLabelCd     | Label setup for the source CD                         | |
| Input Label Tuner       | inputLabelTuner  | Label setup for the source Tuner                      | |
| Input Label Tape        | inputLabelTape   | Label setup for the source Tape                       | |
| Input Label USB         | inputLabelUsb    | Label setup for the source USB                        | |
| Input Label Video 1     | inputLabelVideo1 | Label setup for the source Video 1                    | |
| Input Label Video 2     | inputLabelVideo2 | Label setup for the source Video 2                    | |
| Input Label Video 3     | inputLabelVideo3 | Label setup for the source Video 3                    | |
| Input Label Video 4     | inputLabelVideo4 | Label setup for the source Video 4                    | |
| Input Label Video 5     | inputLabelVideo5 | Label setup for the source Video 5                    | |
| Input Label Video 6     | inputLabelVideo6 | Label setup for the source Video 6                    | |
| Input Label Multi Input | inputLabelMulti  | Label setup for the source Multi Input                | |

All things have the following parameters: serialPort, host and port.
Some have additional parameters listed in the next table:

| Thing Type | Parameters available in addition to serialPort, host and port   |
|------------|-----------------------------------------------------------------|
| ra1572     | protocol (ASCII_V2 by default); as of firmware V2.65, select V2 |
| ra1592     | protocol (ASCII_V2 by default); as of firmware V1.53, select V2 |
| rc1572     | protocol (ASCII_V2 by default); as of firmware V2.65, select V2 |
| rc1590     | protocol (ASCII_V2 by default); as of firmware V1.40, select V2 |
| rcd1572    | protocol (ASCII_V2 by default); as of firmware V2.33, select V2 |
| rsp1066    | inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsp1068    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsp1069    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsp1098    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsp1570    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5, inputLabelMulti |
| rsp1572    | inputLabelCd, inputLabelTuner, inputLabelUsb, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5, inputLabelVideo6, inputLabelMulti |
| rsx1055    | inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsx1056    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsx1057    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsx1058    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsx1065    | inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsx1067    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5 |
| rsx1550    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5, inputLabelMulti |
| rsx1560    | inputLabelCd, inputLabelTuner, inputLabelTape, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5, inputLabelMulti |
| rsx1562    | inputLabelCd, inputLabelTuner, inputLabelUsb, inputLabelVideo1, inputLabelVideo2, inputLabelVideo3, inputLabelVideo4, inputLabelVideo5, inputLabelVideo6, inputLabelMulti |

Some notes:

* On Linux, you may get an error stating the serial port cannot be opened when the Rotel binding tries to load.  You can get around this by adding the `openhab` user to the `dialout` group like this: `usermod -a -G dialout openhab`.
* Also on Linux you may have issues with the USB if using two serial USB devices e.g. Rotel and RFXcom. See the [general documentation about serial port configuration](/docs/administration/serial.html) for more on symlinking the USB ports.
* Here is an example of ser2net.conf you can use to share your serial port /dev/ttyUSB0 on IP port 4444 using [ser2net Linux tool](https://sourceforge.net/projects/ser2net/) (take care, the baud rate is Rotel device specific):

```
4444:raw:0:/dev/ttyUSB0:19200 8DATABITS NONE 1STOPBIT
```


## Channels

The following channels are available:

| Channel ID   | Label               | Item Type | Description                           | Possible values (depends on model) |
|--------------|---------------------|-----------|---------------------------------------|------------------------------------|
| power, mainZone#power, zone2#power, zone3#power, zone4#power | Power               | Switch    | Power ON/OFF the equipment or the zone | ON, OFF                            |
| source, mainZone#source, zone2#source, zone3#source, zone4#source | Source Input        | String    | Select the source input               | CD, TUNER, TAPE, VIDEO1, VIDEO2, VIDEO3, VIDEO4, VIDEO5, VIDEO6, VIDEO7, VIDEO8, USB, PCUSB, MULTI, PHONO, BLUETOOTH, AUX, AUX1, AUX2, AUX1_COAX, AUX1_OPTICAL, COAX1, COAX2, COAX3, OPTICAL1, OPTICAL2, OPTICAL3, XLR, RCD, FM, DAB, PLAYFI, IRADIO, NETWORK |
| mainZone#recordSource | Record Source       | String    | Select the source to be recorded      | CD, TUNER, TAPE, VIDEO1, VIDEO2, VIDEO3, VIDEO4, VIDEO5, VIDEO6, USB, MAIN |
| dsp, mainZone#dsp | DSP Mode            | String    | Select the DSP mode                   | NONE, STEREO3, STEREO5, STEREO7, STEREO9, STEREO11, MUSIC1, MUSIC2, MUSIC3, MUSIC4, PROLOGIC, PLIICINEMA, PLIIMUSIC, PLIIGAME, PLIIXCINEMA, PLIIXMUSIC, PLIIXGAME, PLIIZ, NEO6MUSIC, NEO6CINEMA, ATMOS, NEURALX, BYPASS |
| mainZone#volumeUpDown, zone2#volumeUpDown | Volume              | Number    | Increase or decrease the volume       | INCREASE, DECREASE, value |
| volume, mainZone#volume, zone2#volume, zone3#volume, zone4#volume | Volume              | Dimmer    | Adjust the volume                     | value between 0 and 100 |
| mute, mainZone#mute, zone2#mute, zone3#mute, zone4#mute | Mute                | Switch    | Mute/unmute the sound                 | ON, OFF                            |
| bass, mainZone#bass | Bass Adjustment           | Number    | Adjust the bass                                          | INCREASE, DECREASE, value          |
| treble, mainZone#treble | Treble Adjustment     | Number    | Adjust the treble                                        | INCREASE, DECREASE, value          |
| playControl    | Playback Control               | Player    | Control the playback                                     | PLAY, PAUSE, NEXT, PREVIOUS        |
| track          | Current Track                  | Number    | The current CD track number                              |                                    |
| mainZone#line1 | Front Panel Line 1             | String    | The first line displayed on the device front panel       |                                    |
| mainZone#line2 | Front Panel Line 2             | String    | The second line displayed on the device front panel      |                                    |
| frequency      | Current Frequency              | Number    | The current frequency (in kHz) for digital source input  |                                    |
| brightness     | Front Panel Display Brightness | Dimmer    | The backlight brightness level (in %) of the device front panel |                             |

Here are the list of channels available for each thing type:

| Thing Type | Available channels                                                                    |
|------------|---------------------------------------------------------------------------------------|
| a11        | power, source, volume, mute, bass, treble, brightness                                 |
| a12        | power, source, volume, mute, bass, treble, frequency, brightness                      |
| a14        | power, source, volume, mute, bass, treble, frequency, brightness                      |
| cd11       | power, playControl, track, brightness                                                 |
| cd14       | power, playControl, track, brightness                                                 |
| ra11       | power, source, volume, mute, bass, treble, playControl, frequency, brightness         |
| ra12       | power, source, volume, mute, bass, treble, playControl, frequency, brightness         |
| ra1570     | power, source, volume, mute, bass, treble, playControl, frequency, brightness         |
| ra1572     | power, source, volume, mute, bass, treble, frequency, brightness                      |
| ra1592     | power, source, volume, mute, bass, treble, frequency, brightness                      |
| rap1580    | power, source, dsp, volume, mute, brightness                                          |
| rc1570     | power, source, volume, mute, bass, treble, playControl, frequency, brightness         |
| rc1572     | power, source, volume, mute, bass, treble, frequency, brightness                      |
| rc1590     | power, source, volume, mute, bass, treble, frequency, brightness                      |
| rcd1570    | power, playControl, brightness                                                        |
| rcd1572    | power, playControl, track, brightness                                                 |
| rcx1500    | power, source, volume, mute, playControl                                              |
| rdd1580    | power, source, playControl, frequency                                                 |
| rdg1520    | power, source, playControl                                                            |
| rsp1066    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volumeUpDown, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, zone2#power, zone2#source, zone2#volumeUpDown |
| rsp1068    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1069    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1098    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsp1570    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1572    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1576    | power, source, dsp, volume, mute, brightness                                          |
| rsp1582    | power, source, dsp, volume, mute, brightness                                          |
| rsx1055    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volumeUpDown, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, zone2#power, zone2#source, zone2#volumeUpDown |
| rsx1056    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsx1057    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsx1058    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsx1065    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volumeUpDown, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volumeUpDown |
| rsx1067    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsx1550    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsx1560    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsx1562    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#line1, mainZone#line2, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rt09       | power, source, playControl, brightness                                                |
| rt11       | power, source, brightness                                                             |
| rt1570     | power, source, brightness                                                             |
| t11        | power, source, brightness                                                             |
| t14        | power, source, brightness                                                             |

## Full Example

example.things using serial connection:

```
Thing rotel:rsp1066:preamp "RSP-1066" [ serialPort="COM1", inputLabelVideo1="VID 1", inputLabelVideo2="VID 2", inputLabelVideo3="VID 3", inputLabelVideo4="VID 4", inputLabelVideo5="VID 5" ]

Thing rotel:rsp1570:preamp "RSP-1570" [ serialPort="COM2" ]

Thing rotel:ra1592:preamp "RA-1592" [ serialPort="COM3" ]

Thing rotel:cd14:cd "CD14" [ serialPort="COM4" ]
```

example.things using serial over IP connection:

```
Thing rotel:rsp1066:preamp "RSP-1066" [ host="192.168.0.200", port=3000, inputLabelVideo1="VID 1", inputLabelVideo2="VID 2", inputLabelVideo3="VID 3", inputLabelVideo4="VID 4", inputLabelVideo5="VID 5" ]

Thing rotel:rsp1570:preamp "RSP-1570" [ host="192.168.0.201", port=3000, inputLabelCd="CD", inputLabelTuner="TUNER", inputLabelTape="TAPE", inputLabelVideo1="VIDEO 1", inputLabelVideo2="VIDEO 2", inputLabelVideo3="VIDEO 3", inputLabelVideo4="VIDEO 4", inputLabelVideo5="VIDEO 5", inputLabelMulti="MULTI" ]

Thing rotel:ra1592:preamp "RA-1592" [ host="192.168.0.202", port=3000, protocol="ASCII_V1" ]

Thing rotel:cd14:cd "CD14" [ host="192.168.0.203", port=3000 ]
```

example.items:

```
Switch preamp_power "Power" { channel="rotel:rsp1066:preamp:mainZone#power" }
String preamp_source "Source Input [%s]" { channel="rotel:rsp1066:preamp:mainZone#source" }
String preamp_rec "Record Source [%s]" { channel="rotel:rsp1066:preamp:mainZone#recordSource" }
String preamp_dsp "DSP [%s]" { channel="rotel:rsp1066:preamp:mainZone#dsp" }
Number preamp_volume "Volume [%d]" { channel="rotel:rsp1066:preamp:mainZone#volumeUpDown" }
Switch preamp_mute "Mute" { channel="rotel:rsp1066:preamp:mainZone#mute" }
Number preamp_bass "Bass Adjustment [%d]" { channel="rotel:rsp1066:preamp:mainZone#bass" }
Number preamp_treble "Treble Adjustment [%d]" { channel="rotel:rsp1066:preamp:mainZone#treble" }
String preamp_panel_display "Display [%s]" { channel="rotel:rsp1066:preamp:mainZone#line1" }
Switch preamp_power_zone2 "Zone 2 Power" { channel="rotel:rsp1066:preamp:zone2#power" }
String preamp_source_zone2 "Zone 2 Source Input [%s]" { channel="rotel:rsp1066:preamp:zone2#source" }
Number preamp_volume_zone2 "Zone 2 Volume [%d]" { channel="rotel:rsp1066:preamp:zone2#volumeUpDown" }

Switch preamp2_power "Power" { channel="rotel:rsp1570:preamp:mainZone#power" }
String preamp2_source "Source Input [%s]" { channel="rotel:rsp1570:preamp:mainZone#source" }
String preamp2_rec "Record Source [%s]" { channel="rotel:rsp1570:preamp:mainZone#recordSource" }
String preamp2_dsp "DSP [%s]" { channel="rotel:rsp1570:preamp:mainZone#dsp" }
Dimmer preamp2_volume "Volume [%d %%]" { channel="rotel:rsp1570:preamp:mainZone#volume" }
Switch preamp2_mute "Mute" { channel="rotel:rsp1570:preamp:mainZone#mute" }
Number preamp2_bass "Bass Adjustment [%d]" { channel="rotel:rsp1570:preamp:mainZone#bass" }
Number preamp2_treble "Treble Adjustment [%d]" { channel="rotel:rsp1570:preamp:mainZone#treble" }
String preamp2_panel_line1 "Display Line1 [%s]" { channel="rotel:rsp1570:preamp:mainZone#line1" }
String preamp2_panel_line2 "Display Line2 [%s]" { channel="rotel:rsp1570:preamp:mainZone#line2" }
Switch preamp2_power_zone2 "Zone 2 Power" { channel="rotel:rsp1570:preamp:zone2#power" }
String preamp2_source_zone2 "Zone 2 Source Input [%s]" { channel="rotel:rsp1570:preamp:zone2#source" }
Dimmer preamp2_volume_zone2 "Zone 2 Volume [%d %%]" { channel="rotel:rsp1570:preamp:zone2#volume" }
Switch preamp2_mute_zone2 "Zone 2 Mute" { channel="rotel:rsp1570:preamp:zone2#mute" }
Switch preamp2_power_zone3 "Zone 3 Power" { channel="rotel:rsp1570:preamp:zone3#power" }
String preamp2_source_zone3 "Zone 3 Source Input [%s]" { channel="rotel:rsp1570:preamp:zone3#source" }
Dimmer preamp2_volume_zone3 "Zone 3 Volume [%d %%]" { channel="rotel:rsp1570:preamp:zone3#volume" }
Switch preamp2_mute_zone3 "Zone 3 Mute" { channel="rotel:rsp1570:preamp:zone3#mute" }
Switch preamp2_power_zone4 "Zone 4 Power" { channel="rotel:rsp1570:preamp:zone4#power" }
String preamp2_source_zone4 "Zone 4 Source Input [%s]" { channel="rotel:rsp1570:preamp:zone4#source" }
Dimmer preamp2_volume_zone4 "Zone 4 Volume [%d %%]" { channel="rotel:rsp1570:preamp:zone4#volume" }
Switch preamp2_mute_zone4 "Zone 4 Mute" { channel="rotel:rsp1570:preamp:zone4#mute" }

Switch amp_power "Power" { channel="rotel:ra1592:preamp:power" }
String amp_source "Source Input [%s]" { channel="rotel:ra1592:preamp:source" }
Dimmer amp_volume "Volume [%d %%]" { channel="rotel:ra1592:preamp:volume" }
Switch amp_mute "Mute" { channel="rotel:ra1592:preamp:mute" }
Number amp_bass "Bass Adjustment [%d]" { channel="rotel:ra1592:preamp:bass" }
Number amp_treble "Treble Adjustment [%d]" { channel="rotel:ra1592:preamp:treble" }
Dimmer amp_brightness "Display brightness" { channel="rotel:ra1592:preamp:brightness" }

Switch cd_power "Power" { channel="rotel:cd14:cd:power" }
Player cd_control "Playback" { channel="rotel:cd14:cd:power" }
Number cd_track "Track [%d]" { channel="rotel:cd14:cd:power" }
Dimmer cd_brightness "Display brightness" { channel="rotel:cd14:cd:brightness" }
```

example.sitemap:

```
Switch item=preamp_power
Selection item=preamp_source
Selection item=preamp_rec
Selection item=preamp_dsp
Setpoint item=preamp_volume minValue=0 maxValue=90
Switch item=preamp_mute
Setpoint item=preamp_bass minValue=-12 maxValue=12 step=2
Setpoint item=preamp_treble minValue=-12 maxValue=12 step=2
Text item=preamp_panel_display
Switch item=preamp_power_zone2
Selection item=preamp_source_zone2

Switch item=preamp2_power
Selection item=preamp2_source
Selection item=preamp2_rec
Selection item=preamp2_dsp
Slider item=preamp2_volume
Switch item=preamp2_mute
Setpoint item=preamp2_bass minValue=-6 maxValue=6
Setpoint item=preamp2_treble minValue=-6 maxValue=6
Text item=preamp2_panel_line1
Text item=preamp2_panel_line2
Switch item=preamp2_power_zone2
Selection item=preamp2_source_zone2
Slider item=preamp2_volume_zone2
Switch item=preamp2_mute_zone2
Switch item=preamp2_power_zone3
Selection item=preamp2_source_zone3
Slider item=preamp2_volume_zone3
Switch item=preamp2_mute_zone3
Switch item=preamp2_power_zone4
Selection item=preamp2_source_zone4
Slider item=preamp2_volume_zone4
Switch item=preamp2_mute_zone4

Switch item=amp_power
Selection item=amp_source
Slider item=amp_volume
Switch item=amp_mute
Slider item=amp_bass minValue=-10 maxValue=10
Slider item=amp_treble minValue=-10 maxValue=10
Slider item=amp_brightness

Switch item=cd_power
Default item=cd_control
Text item=cd_track
Slider item=cd_brightness
```
