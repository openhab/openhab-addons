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

| Thing Type | Description                                                                   |
|------------|-------------------------------------------------------------------------------|
| a11        | Connection to the Rotel A11 integrated amplifier                              |
| a12        | Connection to the Rotel A12 or A12MKII integrated amplifier                   |
| a14        | Connection to the Rotel A14 or A14MKII integrated amplifier                   |
| c8         | Connection to the Rotel C8 or C8+ distribution amplifier                      |
| cd11       | Connection to the Rotel CD11 CD player                                        |
| cd14       | Connection to the Rotel CD14 or CD14MKII CD player                            |
| m8         | Connection to the Rotel Michi M8 monoblock amplifier                          |
| p5         | Connection to the Rotel Michi P5 stereo preamplifier                          |
| ra11       | Connection to the Rotel RA-11 integrated amplifier                            |
| ra12       | Connection to the Rotel RA-12 integrated amplifier                            |
| ra1570     | Connection to the Rotel RA-1570 integrated amplifier                          |
| ra1572     | Connection to the Rotel RA-1572 or RA-1572MKII integrated amplifier           |
| ra1592     | Connection to the Rotel RA-1592 or RA-1592MKII integrated amplifier           |
| rap1580    | Connection to the Rotel RAP-1580 or RAP-1580MKII surround amplified processor |
| rc1570     | Connection to the Rotel RC-1570 stereo preamplifier                           |
| rc1572     | Connection to the Rotel RC-1572 or RC-1572MKII stereo preamplifier            |
| rc1590     | Connection to the Rotel RC-1590 or RC-1590MKII stereo preamplifier            |
| rcd1570    | Connection to the Rotel RCD-1570 CD player                                    |
| rcd1572    | Connection to the Rotel RCD-1572 or RCD-1572MKII CD player                    |
| rcx1500    | Connection to the Rotel RCX-1500 stereo receiver                              |
| rdd1580    | Connection to the Rotel RDD-1580 stereo DAC                                   |
| rdg1520    | Connection to the Rotel RDG-1520 tuner                                        |
| rsp1066    | Connection to the Rotel RSP-1066 surround processor                           |
| rsp1068    | Connection to the Rotel RSP-1068 surround processor                           |
| rsp1069    | Connection to the Rotel RSP-1069 surround processor                           |
| rsp1098    | Connection to the Rotel RSP-1098 surround processor                           |
| rsp1570    | Connection to the Rotel RSP-1570 surround processor                           |
| rsp1572    | Connection to the Rotel RSP-1572 surround processor                           |
| rsp1576    | Connection to the Rotel RSP-1576 or RSP-1576MKII surround processor           |
| rsp1582    | Connection to the Rotel RSP-1582 surround processor                           |
| rsx1055    | Connection to the Rotel RSX-1055 surround receiver                            |
| rsx1056    | Connection to the Rotel RSX-1056 surround receiver                            |
| rsx1057    | Connection to the Rotel RSX-1057 surround receiver                            |
| rsx1058    | Connection to the Rotel RSX-1058 surround receiver                            |
| rsx1065    | Connection to the Rotel RSX-1065 surround receiver                            |
| rsx1067    | Connection to the Rotel RSX-1067 surround receiver                            |
| rsx1550    | Connection to the Rotel RSX-1550 surround receiver                            |
| rsx1560    | Connection to the Rotel RSX-1560 surround receiver                            |
| rsx1562    | Connection to the Rotel RSX-1562 surround receiver                            |
| rt09       | Connection to the Rotel RT-09 tuner                                           |
| rt11       | Connection to the Rotel RT-11 tuner                                           |
| rt1570     | Connection to the Rotel RT-1570 tuner                                         |
| s5         | Connection to the Rotel Michi S5 stereo amplifier                             |
| t11        | Connection to the Rotel T11 tuner                                             |
| t14        | Connection to the Rotel T14 tuner                                             |
| x3         | Connection to the Rotel Michi X3 integrated amplifier                         |
| x5         | Connection to the Rotel Michi X5 integrated amplifier                         |

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
| power, mainZone#power, allZones#power, zone2#power, zone3#power, zone4#power | Power               | Switch    | Power ON/OFF the equipment or the zone | ON, OFF                            |
| source, mainZone#source, zone1#source, zone2#source, zone3#source, zone4#source | Source Input        | String    | Select the source input               | CD, TUNER, TAPE, VIDEO1, VIDEO2, VIDEO3, VIDEO4, VIDEO5, VIDEO6, VIDEO7, VIDEO8, USB, PCUSB, MULTI, PHONO, BLUETOOTH, AUX, AUX1, AUX2, AUX1_COAX, AUX1_OPTICAL, COAX1, COAX2, COAX3, OPTICAL1, OPTICAL2, OPTICAL3, XLR, RCD, FM, DAB, PLAYFI, IRADIO, NETWORK, INPUTA, INPUTB, INPUTC, INPUTD |
| mainZone#recordSource | Record Source       | String    | Select the source to be recorded      | CD, TUNER, TAPE, VIDEO1, VIDEO2, VIDEO3, VIDEO4, VIDEO5, VIDEO6, USB, MAIN |
| dsp, mainZone#dsp | DSP Mode            | String    | Select the DSP mode                   | NONE, STEREO3, STEREO5, STEREO7, STEREO9, STEREO11, MUSIC1, MUSIC2, MUSIC3, MUSIC4, PROLOGIC, PLIICINEMA, PLIIMUSIC, PLIIGAME, PLIIXCINEMA, PLIIXMUSIC, PLIIXGAME, PLIIZ, NEO6MUSIC, NEO6CINEMA, ATMOS, NEURALX, BYPASS |
| mainZone#volumeUpDown, zone2#volumeUpDown | Volume              | Number    | Increase or decrease the volume       | INCREASE, DECREASE, value |
| volume, mainZone#volume, zone1#volume, zone2#volume, zone3#volume, zone4#volume | Volume              | Dimmer    | Adjust the volume                     | value between 0 and 100 |
| mute, mainZone#mute, zone1#mute, zone2#mute, zone3#mute, zone4#mute | Mute                | Switch    | Mute/unmute the sound                 | ON, OFF                            |
| bass, mainZone#bass, zone1#bass, zone2#bass, zone3#bass, zone4#bass | Bass Adjustment           | Number    | Adjust the bass                                          | INCREASE, DECREASE, value          |
| treble, mainZone#treble, zone1#treble, zone2#treble, zone3#treble, zone4#treble | Treble Adjustment     | Number    | Adjust the treble                                        | INCREASE, DECREASE, value          |
| playControl    | Playback Control               | Player    | Control the playback                                     | PLAY, PAUSE, NEXT, PREVIOUS        |
| track          | Current Track                  | Number    | The current CD track number                              |                                    |
| random         | Random Mode                    | Switch    | The current random mode                                  |                                    |
| repeat         | Repeat Mode                    | String    | The current repeat mode                                  | TRACK, DISC, OFF                   |
| radioPreset    | Radio Preset                   | Number    | Select a radio preset                                    | INCREASE, DECREASE, value between 1 and 30 |
| mainZone#line1 | Front Panel Line 1             | String    | The first line displayed on the device front panel       |                                    |
| mainZone#line2 | Front Panel Line 2             | String    | The second line displayed on the device front panel      |                                    |
| frequency, zone1#frequency, zone2#frequency, zone3#frequency, zone4#frequency | Current Frequency              | Number    | The current frequency (in kHz) for digital source input  |                                    |
| brightness, allZones#brightness | Front Panel Display Brightness | Dimmer    | The backlight brightness level (in %) of the device front panel |                             |
| tcbypass       | Tone Control Bypass            | Switch    | The user's bass-/treble-settings are bypassed            | ON, OFF                            |
| balance, zone1#balance, zone2#balance, zone3#balance, zone4#balance | Stereo Balance Adjustment      | Number    | Adjust the balance                                       | INCREASE, DECREASE, value          |
| speakera       | Speaker-A Adjustment           | Switch    | Turn on/off the speaker group A                          | ON, OFF                            |
| speakerb       | Speaker-B Adjustment           | Switch    | Turn on/off the speaker group B                          | ON, OFF                            |

Here are the list of channels available for each thing type:

| Thing Type | Available channels                                                                                      |
|------------|---------------------------------------------------------------------------------------------------------|
| a11        | power, source, volume, mute, bass, treble, brightness, tcbypass, balance, speakera, speakerb, otherCommand |
| a12        | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, speakera, speakerb, otherCommand |
| a14        | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, speakera, speakerb, otherCommand |
| c8         | allZones#power, allZones#brightness, zone1#source, zone1#volume, zone1#mute, zone1#bass, zone1#treble, zone1#balance, zone1#frequency, zone2#source, zone2#volume, zone2#mute, zone2#bass, zone2#treble, zone2#balance, zone2#frequency, zone3#source, zone3#volume, zone3#mute, zone3#bass, zone3#treble, zone3#balance, zone3#frequency, zone4#source, zone4#volume, zone4#mute, zone4#bass, zone4#treble, zone4#balance, zone4#frequency |
| cd11       | power, playControl, track, random, repeat, brightness, otherCommand                                     |
| cd14       | power, playControl, track, random, repeat, brightness, otherCommand                                     |
| m8         | power, brightness                                                                                       |
| p5         | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, otherCommand       |
| ra11       | power, source, volume, mute, bass, treble, playControl, frequency, brightness, tcbypass, balance, otherCommand |
| ra12       | power, source, volume, mute, bass, treble, playControl, frequency, brightness, tcbypass, balance, otherCommand |
| ra1570     | power, source, volume, mute, bass, treble, playControl, frequency, brightness, tcbypass, balance, speakera, speakerb, otherCommand |
| ra1572     | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, speakera, speakerb, otherCommand |
| ra1592     | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, speakera, speakerb, otherCommand |
| rap1580    | power, source, dsp, volume, mute, brightness, otherCommand                                              |
| rc1570     | power, source, volume, mute, bass, treble, playControl, frequency, brightness, tcbypass, balance, otherCommand |
| rc1572     | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, otherCommand       |
| rc1590     | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, otherCommand       |
| rcd1570    | power, playControl, brightness                                                                          |
| rcd1572    | power, playControl, track, random, repeat, brightness, otherCommand                                     |
| rcx1500    | power, source, volume, mute, playControl, radioPreset                                                   |
| rdd1580    | power, source, playControl, frequency, otherCommand                                                     |
| rdg1520    | power, source, playControl, radioPreset                                                                 |
| rsp1066    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volumeUpDown, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#otherCommand, zone2#power, zone2#source, zone2#volumeUpDown |
| rsp1068    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1069    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1098    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsp1570    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1572    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsp1576    | power, source, dsp, volume, mute, brightness, otherCommand                                              |
| rsp1582    | power, source, dsp, volume, mute, brightness, otherCommand                                              |
| rsx1055    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volumeUpDown, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#otherCommand, zone2#power, zone2#source, zone2#volumeUpDown |
| rsx1056    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsx1057    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsx1058    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsx1065    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volumeUpDown, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volumeUpDown |
| rsx1067    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute |
| rsx1550    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsx1560    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#bass, mainZone#treble, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rsx1562    | mainZone#power, mainZone#source, mainZone#recordSource, mainZone#dsp, mainZone#volume, mainZone#mute, mainZone#line1, mainZone#line2, mainZone#otherCommand, zone2#power, zone2#source, zone2#volume, zone2#mute, zone3#power, zone3#source, zone3#volume, zone3#mute, zone4#power, zone4#source, zone4#volume, zone4#mute |
| rt09       | power, source, playControl, brightness                                                                  |
| rt11       | power, source, radioPreset, brightness                                                                  |
| rt1570     | power, source, radioPreset, brightness                                                                  |
| s5         | power, brightness                                                                                       |
| t11        | power, source, radioPreset, brightness                                                                  |
| t14        | power, source, radioPreset, brightness                                                                  |
| x3         | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, otherCommand       |
| x5         | power, source, volume, mute, bass, treble, frequency, brightness, tcbypass, balance, otherCommand       |

Here are the available commands for the otherCommand channel depending on the thing type:

| Thing Type         | Available commands for the otherCommand channel                               |
|--------------------|-------------------------------------------------------------------------------|
| a11                | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK                                      |
| a12                | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, PCUSB_CLASS                         |
| a14                | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, PCUSB_CLASS                         |
| cd11               | FAST_FWD, FAST_BACK, EJECT, TIME_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0 |
| cd14               | FAST_FWD, FAST_BACK, EJECT, TIME_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0 |
| p5                 | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK                                      |
| ra11               | FAST_FWD, FAST_BACK, RANDOM_TOGGLE, REPEAT_TOGGLE, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0 |
| ra12               | FAST_FWD, FAST_BACK, RANDOM_TOGGLE, REPEAT_TOGGLE, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0 |
| ra1570             | FAST_FWD, FAST_BACK, RANDOM_TOGGLE, REPEAT_TOGGLE, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2 |
| ra1572 (ASCII V1)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2, POWER_MODE, RESET_FACTORY |
| ra1572 (ASCII V2)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2, RESET_FACTORY |
| ra1592 (ASCII V1)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, FAST_FWD, FAST_BACK, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2, POWER_MODE, POWER_MODE_QUICK, POWER_MODE_NORMAL, RESET_FACTORY |
| ra1592 (ASCII V2)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2 |
| rap1580            | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, SUB_LEVEL_UP, SUB_LEVEL_DOWN, C_LEVEL_UP, C_LEVEL_DOWN, SR_LEVEL_UP, SR_LEVEL_DOWN, SL_LEVEL_UP, SL_LEVEL_DOWN, CBR_LEVEL_UP, CBR_LEVEL_DOWN, CBL_LEVEL_UP, CBL_LEVEL_DOWN, CFR_LEVEL_UP, CFR_LEVEL_DOWN, CFL_LEVEL_UP, CFL_LEVEL_DOWN, CRR_LEVEL_UP, CRR_LEVEL_DOWN, CRL_LEVEL_UP, CRL_LEVEL_DOWN, NEXT_MODE, RESET_FACTORY |
| rc1570             | FAST_FWD, FAST_BACK, RANDOM_TOGGLE, REPEAT_TOGGLE, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2 |
| rc1572 (ASCII V1)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2, POWER_MODE, RESET_FACTORY |
| rc1572 (ASCII V2)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2, RESET_FACTORY |
| rc1590 (ASCII V1)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, FAST_FWD, FAST_BACK, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2, POWER_MODE, POWER_MODE_QUICK, POWER_MODE_NORMAL, RESET_FACTORY |
| rc1590 (ASCII V2)  | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2 |
| rcd1572 (ASCII V1) | FAST_FWD, FAST_BACK, EJECT, TIME_TOGGLE, PROGRAM, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0 |
| rcd1572 (ASCII V2) | FAST_FWD, FAST_BACK, EJECT, TIME_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0 |
| rdd1580            | RANDOM_TOGGLE, REPEAT_TOGGLE, PCUSB_CLASS, PCUSB_CLASS_1, PCUSB_CLASS_2       |
| rsp1066            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE |
| rsp1068            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE |
| rsp1069            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE, ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE, OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE |
| rsp1098            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, REMOTE_VOLUME_UP, REMOTE_VOLUME_DOWN |
| rsp1570            | STEREO_BYPASS_TOGGLE, DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE, ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE, OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE, RESET_FACTORY |
| rsp1572            | STEREO_BYPASS_TOGGLE, DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, MENU, EXIT, UP_PRESSED, UP_RELEASED, DOWN_PRESSED, DOWN_RELEASED, LEFT_PRESSED, LEFT_RELEASED, RIGHT_PRESSED, RIGHT_RELEASED, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE, ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE, OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE, ROOM_EQ_TOGGLE, SPEAKER_SETTING_TOGGLE, RESET_FACTORY |
| rsp1576            | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, SUB_LEVEL_UP, SUB_LEVEL_DOWN, C_LEVEL_UP, C_LEVEL_DOWN, SR_LEVEL_UP, SR_LEVEL_DOWN, SL_LEVEL_UP, SL_LEVEL_DOWN, CBR_LEVEL_UP, CBR_LEVEL_DOWN, CBL_LEVEL_UP, CBL_LEVEL_DOWN, CFR_LEVEL_UP, CFR_LEVEL_DOWN, CFL_LEVEL_UP, CFL_LEVEL_DOWN, CRR_LEVEL_UP, CRR_LEVEL_DOWN, CRL_LEVEL_UP, CRL_LEVEL_DOWN, NEXT_MODE, RESET_FACTORY |
| rsp1582            | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK, MENU, EXIT, UP, DOWN, LEFT, RIGHT, ENTER, SUB_LEVEL_UP, SUB_LEVEL_DOWN, C_LEVEL_UP, C_LEVEL_DOWN, SR_LEVEL_UP, SR_LEVEL_DOWN, SL_LEVEL_UP, SL_LEVEL_DOWN, CBR_LEVEL_UP, CBR_LEVEL_DOWN, CBL_LEVEL_UP, CBL_LEVEL_DOWN, NEXT_MODE, RESET_FACTORY |
| rsx1055            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE |
| rsx1056            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, ZONE2_TUNE_UP, ZONE2_TUNE_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3, ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE |
| rsx1057            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, PRESET_UP, PRESET_DOWN, FREQUENCY_UP, FREQUENCY_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, ZONE2_TUNE_UP, ZONE2_TUNE_DOWN, ZONE2_PRESET_UP, ZONE2_PRESET_DOWN, ZONE2_FREQUENCY_UP, ZONE2_FREQUENCY_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3, ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE |
| rsx1058            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, PRESET_UP, PRESET_DOWN, FREQUENCY_UP, FREQUENCY_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, ZONE2_TUNE_UP, ZONE2_TUNE_DOWN, ZONE2_PRESET_UP, ZONE2_PRESET_DOWN, ZONE2_FREQUENCY_UP, ZONE2_FREQUENCY_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE, ZONE3_TUNE_UP, ZONE3_TUNE_DOWN, ZONE3_PRESET_UP, ZONE3_PRESET_DOWN, ZONE3_FREQUENCY_UP, ZONE3_FREQUENCY_DOWN, ZONE3_BAND_TOGGLE, ZONE3_AM, ZONE3_FM, ZONE3_TUNE_PRESET_TOGGLE, ZONE3_TUNING_MODE_SELECT, ZONE3_PRESET_MODE_SELECT, ZONE3_PRESET_SCAN, ZONE3_FM_MONO_TOGGLE, ZONE4_TUNE_UP, ZONE4_TUNE_DOWN, ZONE4_PRESET_UP, ZONE4_PRESET_DOWN, ZONE4_FREQUENCY_UP, ZONE4_FREQUENCY_DOWN, ZONE4_BAND_TOGGLE, ZONE4_AM, ZONE4_FM, ZONE4_TUNE_PRESET_TOGGLE, ZONE4_TUNING_MODE_SELECT, ZONE4_PRESET_MODE_SELECT, ZONE4_PRESET_SCAN, ZONE4_FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3, ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, ZONE3_KEY1, ZONE3_KEY2, ZONE3_KEY3, ZONE3_KEY4, ZONE3_KEY5, ZONE3_KEY6, ZONE3_KEY7, ZONE3_KEY8, ZONE3_KEY9, ZONE3_KEY0, ZONE4_KEY1, ZONE4_KEY2, ZONE4_KEY3, ZONE4_KEY4, ZONE4_KEY5, ZONE4_KEY6, ZONE4_KEY7, ZONE4_KEY8, ZONE4_KEY9, ZONE4_KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE, ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE, OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE |
| rsx1065            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE |
| rsx1067            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, ZONE2_TUNE_UP, ZONE2_TUNE_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3, ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE |
| rsx1550            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, PRESET_UP, PRESET_DOWN, FREQUENCY_UP, FREQUENCY_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, ZONE2_TUNE_UP, ZONE2_TUNE_DOWN, ZONE2_PRESET_UP, ZONE2_PRESET_DOWN, ZONE2_FREQUENCY_UP, ZONE2_FREQUENCY_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE, ZONE3_TUNE_UP, ZONE3_TUNE_DOWN, ZONE3_PRESET_UP, ZONE3_PRESET_DOWN, ZONE3_FREQUENCY_UP, ZONE3_FREQUENCY_DOWN, ZONE3_BAND_TOGGLE, ZONE3_AM, ZONE3_FM, ZONE3_TUNE_PRESET_TOGGLE, ZONE3_TUNING_MODE_SELECT, ZONE3_PRESET_MODE_SELECT, ZONE3_PRESET_SCAN, ZONE3_FM_MONO_TOGGLE, ZONE4_TUNE_UP, ZONE4_TUNE_DOWN, ZONE4_PRESET_UP, ZONE4_PRESET_DOWN, ZONE4_FREQUENCY_UP, ZONE4_FREQUENCY_DOWN, ZONE4_BAND_TOGGLE, ZONE4_AM, ZONE4_FM, ZONE4_TUNE_PRESET_TOGGLE, ZONE4_TUNING_MODE_SELECT, ZONE4_PRESET_MODE_SELECT, ZONE4_PRESET_SCAN, ZONE4_FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3, ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, ZONE3_KEY1, ZONE3_KEY2, ZONE3_KEY3, ZONE3_KEY4, ZONE3_KEY5, ZONE3_KEY6, ZONE3_KEY7, ZONE3_KEY8, ZONE3_KEY9, ZONE3_KEY0, ZONE4_KEY1, ZONE4_KEY2, ZONE4_KEY3, ZONE4_KEY4, ZONE4_KEY5, ZONE4_KEY6, ZONE4_KEY7, ZONE4_KEY8, ZONE4_KEY9, ZONE4_KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE, ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE, OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE, RESET_FACTORY |
| rsx1560            | DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, PRESET_UP, PRESET_DOWN, FREQUENCY_UP, FREQUENCY_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, ZONE2_TUNE_UP, ZONE2_TUNE_DOWN, ZONE2_PRESET_UP, ZONE2_PRESET_DOWN, ZONE2_FREQUENCY_UP, ZONE2_FREQUENCY_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE, ZONE3_TUNE_UP, ZONE3_TUNE_DOWN, ZONE3_PRESET_UP, ZONE3_PRESET_DOWN, ZONE3_FREQUENCY_UP, ZONE3_FREQUENCY_DOWN, ZONE3_BAND_TOGGLE, ZONE3_AM, ZONE3_FM, ZONE3_TUNE_PRESET_TOGGLE, ZONE3_TUNING_MODE_SELECT, ZONE3_PRESET_MODE_SELECT, ZONE3_PRESET_SCAN, ZONE3_FM_MONO_TOGGLE, ZONE4_TUNE_UP, ZONE4_TUNE_DOWN, ZONE4_PRESET_UP, ZONE4_PRESET_DOWN, ZONE4_FREQUENCY_UP, ZONE4_FREQUENCY_DOWN, ZONE4_BAND_TOGGLE, ZONE4_AM, ZONE4_FM, ZONE4_TUNE_PRESET_TOGGLE, ZONE4_TUNING_MODE_SELECT, ZONE4_PRESET_MODE_SELECT, ZONE4_PRESET_SCAN, ZONE4_FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3, ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, ZONE3_KEY1, ZONE3_KEY2, ZONE3_KEY3, ZONE3_KEY4, ZONE3_KEY5, ZONE3_KEY6, ZONE3_KEY7, ZONE3_KEY8, ZONE3_KEY9, ZONE3_KEY0, ZONE4_KEY1, ZONE4_KEY2, ZONE4_KEY3, ZONE4_KEY4, ZONE4_KEY5, ZONE4_KEY6, ZONE4_KEY7, ZONE4_KEY8, ZONE4_KEY9, ZONE4_KEY0, MENU, UP, DOWN, LEFT, RIGHT, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE, ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE, OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE, RESET_FACTORY |
| rsx1562            | STEREO_BYPASS_TOGGLE, DSP_TOGGLE, PROLOGIC_TOGGLE, DOLBY_TOGGLE, PLII_PANORAMA_TOGGLE, PLII_DIMENSION_UP, PLII_DIMENSION_DOWN, PLII_CENTER_WIDTH_UP, PLII_CENTER_WIDTH_DOWN, DDEX_TOGGLE, NEO6_TOGGLE, NEXT_MODE, TUNE_UP, TUNE_DOWN, PRESET_UP, PRESET_DOWN, FREQUENCY_UP, FREQUENCY_DOWN, MEMORY, BAND_TOGGLE, AM, FM, TUNE_PRESET_TOGGLE, TUNING_MODE_SELECT, PRESET_MODE_SELECT, FREQUENCY_DIRECT, PRESET_SCAN, TUNER_DISPLAY, RDS_PTY, RDS_TP, RDS_TA, FM_MONO_TOGGLE, ZONE2_TUNE_UP, ZONE2_TUNE_DOWN, ZONE2_PRESET_UP, ZONE2_PRESET_DOWN, ZONE2_FREQUENCY_UP, ZONE2_FREQUENCY_DOWN, ZONE2_BAND_TOGGLE, ZONE2_AM, ZONE2_FM, ZONE2_TUNE_PRESET_TOGGLE, ZONE2_TUNING_MODE_SELECT, ZONE2_PRESET_MODE_SELECT, ZONE2_PRESET_SCAN, ZONE2_FM_MONO_TOGGLE, ZONE3_TUNE_UP, ZONE3_TUNE_DOWN, ZONE3_PRESET_UP, ZONE3_PRESET_DOWN, ZONE3_FREQUENCY_UP, ZONE3_FREQUENCY_DOWN, ZONE3_BAND_TOGGLE, ZONE3_AM, ZONE3_FM, ZONE3_TUNE_PRESET_TOGGLE, ZONE3_TUNING_MODE_SELECT, ZONE3_PRESET_MODE_SELECT, ZONE3_PRESET_SCAN, ZONE3_FM_MONO_TOGGLE, ZONE4_TUNE_UP, ZONE4_TUNE_DOWN, ZONE4_PRESET_UP, ZONE4_PRESET_DOWN, ZONE4_FREQUENCY_UP, ZONE4_FREQUENCY_DOWN, ZONE4_BAND_TOGGLE, ZONE4_AM, ZONE4_FM, ZONE4_TUNE_PRESET_TOGGLE, ZONE4_TUNING_MODE_SELECT, ZONE4_PRESET_MODE_SELECT, ZONE4_PRESET_SCAN, ZONE4_FM_MONO_TOGGLE, KEY1, KEY2, KEY3, KEY4, KEY5, KEY6, KEY7, KEY8, KEY9, KEY0, ZONE2_KEY1, ZONE2_KEY2, ZONE2_KEY3, ZONE2_KEY4, ZONE2_KEY5, ZONE2_KEY6, ZONE2_KEY7, ZONE2_KEY8, ZONE2_KEY9, ZONE2_KEY0, ZONE3_KEY1, ZONE3_KEY2, ZONE3_KEY3, ZONE3_KEY4, ZONE3_KEY5, ZONE3_KEY6, ZONE3_KEY7, ZONE3_KEY8, ZONE3_KEY9, ZONE3_KEY0, ZONE4_KEY1, ZONE4_KEY2, ZONE4_KEY3, ZONE4_KEY4, ZONE4_KEY5, ZONE4_KEY6, ZONE4_KEY7, ZONE4_KEY8, ZONE4_KEY9, ZONE4_KEY0, MENU, EXIT, UP_PRESSED, UP_RELEASED, DOWN_PRESSED, DOWN_RELEASED, LEFT_PRESSED, LEFT_RELEASED, RIGHT_PRESSED, RIGHT_RELEASED, ENTER, RECORD_FONCTION_SELECT, TONE_CONTROL_SELECT, DYNAMIC_RANGE, DIGITAL_INPUT_SELECT, ZONE_TOGGLE, CENTER_TRIM, SUB_TRIM, SURROUND_TRIM, CINEMA_EQ_TOGGLE, POWER_OFF_ALL_ZONES, PARTY_MODE_TOGGLE, ZONE2_PARTY_MODE_TOGGLE, ZONE3_PARTY_MODE_TOGGLE, ZONE4_PARTY_MODE_TOGGLE, OUTPUT_RESOLUTION, HDMI_AMP_MODE, HDMI_TV_MODE, ROOM_EQ_TOGGLE, SPEAKER_SETTING_TOGGLE, RESET_FACTORY |
| x3                 | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK                                      |
| x5                 | PLAY, PAUSE, STOP, TRACK_FWD, TRACK_BACK                                      |

## Full Example

example.things using serial connection:

```
Thing rotel:rsp1066:preamp "RSP-1066" [ serialPort="COM1", inputLabelVideo1="VID 1", inputLabelVideo2="VID 2", inputLabelVideo3="VID 3", inputLabelVideo4="VID 4", inputLabelVideo5="VID 5" ]

Thing rotel:rsp1570:preamp "RSP-1570" [ serialPort="COM2" ]

Thing rotel:ra1592:preamp "RA-1592" [ serialPort="COM3" ]

Thing rotel:cd14:cd "CD14" [ serialPort="COM4" ]

Thing rotel:a14:amp "A14" [ serialPort="/dev/ttyUSB0" ]
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

Switch amp_bypass "TCBypass" { channel="rotel:a14:amp:tcbypass" }
Number amp_balance "Balance Adjustment [%d]" { channel="rotel:a14:amp:balance" }
Switch amp_speakera "Speaker A" { channel="rotel:a14:amp:speakera" }
Switch amp_speakerb "Speaker B" { channel="rotel:a14:amp:speakerb" }

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
