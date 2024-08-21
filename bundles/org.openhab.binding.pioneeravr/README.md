# Pioneer AVR Binding

This binding integrates Pioneer AVRs.

## Binding configuration

The binding can auto-discover the Pioneer AVRs present on your local network.
The auto-discovery is enabled by default.
To disable it, you can create a file in the services directory called pioneeravr.cfg with the following content:

```text
#Put your configuration here
org.openhab.pioneeravr:enableAutoDiscovery=false
```

This configuration parameter only control the PioneerAVR auto-discovery process, not the openHAB auto-discovery.
Moreover, if the openHAB auto-discovery is disabled, the PioneerAVR auto-discovery is disabled too.

## Thing configuration

In the things folder, create a file called pioneeravr.things (or any other name) and configure your AVRs inside.

The binding can control AVRs through the local network (ipAvr/ipAvrUnsupported thing type) or through a Serial connection (serialAvr) if the AVR is directly connected to your computer.

Configuration of ipAvr/ipAvrUnsupported:

- address: the hostname/ipAddress of the AVR on the local network. (mandatory)
- tcpPort: the port number to use to connect to the AVR. (optional, default to 23)

Configuration of serialAvr:

- serialPort: the name of the serial port on your computer. (mandatory)

Example:

```java
pioneeravr:ipAvr:vsx921IP [ address="192.168.1.25" ]
pioneeravr:ipAvr:vsx921IP [ address="192.168.1.25", tcpPort=32 ]
pioneeravr:serialAvr:vsx921Serial [ serialPort="COM9" ]
```

## Channels

- power: power On/Off the AVR. Receive power events.
- volumeDimmer: Increase/Decrease the volume on the AVR or set the volume as %. Receive volume change events (in %).
- volumeDb: Set the volume of the AVR in dB (from -80.0 to 12 with 0.5 dB steps). Receive volume change events (in dB).
- mute: Mute/Unmute the AVR. Receive mute events.
- setInputSource: Set the input source of the AVR. See input source mapping for more details. Receive source input change events with the input source ID.
- displayInformation: Receive display events. Reflect the display on the AVR front panel.

## Input Source Mapping

Here after are the ID values of the input sources (depending on you AVR input sources might not be available):

- 04: DVD
- 25: BD
- 05: TV/SAT
- 06: SAT/CBL
- 15: DVR/BDR
- 10: VIDEO 1(VIDEO)
- 14: VIDEO 2
- 19: HDMI 1
- 20: HDMI 2
- 21: HDMI 3
- 22: HDMI 4
- 23: HDMI 5
- 24: HDMI 6
- 34: HDMI 7
- 35: HDMI 8
- 26: HOME MEDIA GALLERY(Internet Radio)
- 44: Media Server
- 38: Internet Radio
- 17: iPod/USB
- 48: MHL
- 01: CD
- 03: CD-R/TAPE
- 02: TUNER
- 00: PHONO
- 13: USB-DAC
- 12: MULTI CH IN
- 33: ADAPTER PORT (BT)
- 18: XM RADIO
- 27: SIRIUS
- 40: SiriusXM
- 41: PANDORA
- 45: Favourites
- 57: Spotify
- 31: HDMI (cyclic)

## Listening Modes

The _Listening Mode_ is set by user to instruct the AVR how to treat the audio signal and do upscaling, downscaling and amplification. This settings corresponds to the settings made with the remote control or front panel. What the AVR actually does with each setting/input-signal-combination can be read out using the _Playing Listening Mode_ channel.

- 0001: STEREO (cyclic)
- 0010: STANDARD (cyclic)
- 0009: STEREO (direct set)
- 0011: (2ch source)
- 0013: PRO LOGIC2 MOVIE
- 0018: PRO LOGIC2x MOVIE
- 0014: PRO LOGIC2 MUSIC
- 0019: PRO LOGIC2x MUSIC
- 0015: PRO LOGIC2 GAME
- 0020: PRO LOGIC2x GAME
- 0031: PRO LOGIC2z HEIGHT
- 0032: WIDE SURROUND MOVIE
- 0033: WIDE SURROUND MUSIC
- 0012: PRO LOGIC
- 0016: Neo:6 CINEMA
- 0017: Neo:6 MUSIC
- 0037: Neo:X CINEMA
- 0038: Neo:X MUSIC
- 0039: Neo:X GAME
- 0040: Dolby Surround
- 0041: EXTENDED STEREO
- 0021: (Multi ch source) Channel base straight decode
- 0022: (Multi ch source)+DOLBY EX
- 0023: (Multi ch source)+PRO LOGIC2x MOVIE
- 0024: (Multi ch source)+PRO LOGIC2x MUSIC
- 0034: (Multi-ch Source)+PRO LOGIC2z HEIGHT
- 0035: (Multi-ch Source)+WIDE SURROUND MOVIE
- 0036: (Multi-ch Source)+WIDE SURROUND MUSIC
- 0025: (Multi ch source)DTS-ES Neo:6
- 0026: (Multi ch source)DTS-ES matrix
- 0027: (Multi ch source)DTS-ES discrete
- 0030: (Multi ch source)DTS-ES 8ch discrete
- 0043: (Multi ch source)+Neo:X CINEMA
- 0044: (Multi ch source)+Neo:X MUSIC
- 0045: (Multi ch source)+Neo:X GAME
- 0050: (Multi ch source)+Dolby Surround
- 0100: ADVANCED SURROUND (cyclic)
- 0101: ACTION
- 0103: DRAMA
- 0118: ADVANCED GAME
- 0117: SPORTS
- 0107: CLASSICAL
- 0110: ROCK/POP
- 0003: Front Stage Surround Advance
- 0200: ECO MODE (cyclic)
- 0212: ECO MODE 1
- 0213: ECO MODE 2
- 0153: RETRIEVER AIR
- 0113: PHONES SURROUND
- 0005: AUTO SURR/STREAM DIRECT (cyclic)
- 0006: AUTO SURROUND
- 0151: Auto Level Control (A.L.C.)
- 0007: DIRECT
- 0008: PURE DIRECT
- 0152: OPTIMUM SURROUND

## Playing Listening Modes

The _Playing Listening Mode_ is the Listening Mode that is actually playing as opposed to the _Listening Mode_ set by the user. The _Playing Listening Mode_ is what the display on the device shows.

- 0101: [)(]PLIIx MOVIE
- 0102: [)(]PLII MOVIE
- 0103: [)(]PLIIx MUSIC
- 0104: [)(]PLII MUSIC
- 0105: [)(]PLIIx GAME
- 0106: [)(]PLII GAME
- 0107: [)(]PROLOGIC
- 0108: Neo:6 CINEMA
- 0109: Neo:6 MUSIC
- 010c: 2ch Straight Decode
- 010d: [)(]PLIIz HEIGHT
- 010e: WIDE SURR MOVIE
- 010f: WIDE SURR MUSIC
- 0110: STEREO
- 0111: Neo:X CINEMA
- 0112: Neo:X MUSIC
- 0113: Neo:X GAME
- 0117: Dolby Surround
- 0118: EXTENDED STEREO
- 1101: [)(]PLIIx MOVIE
- 1102: [)(]PLIIx MUSIC
- 1103: [)(]DIGITAL EX
- 1104: DTS Neo:6
- 1105: ES MATRIX
- 1106: ES DISCRETE
- 1107: DTS-ES 8ch
- 1108: multi ch Channel base Straight Decode
- 1109: [)(]PLIIz HEIGHT
- 110a: WIDE SURR MOVIE
- 110b: WIDE SURR MUSIC
- 110c: Neo:X CINEMA
- 110d: Neo:X MUSIC
- 110e: Neo:X GAME
- 110f: Dolby Surround
- 0201: ACTION
- 0202: DRAMA
- 0208: ADVANCEDGAME
- 0209: SPORTS
- 020a: CLASSICAL
- 020b: ROCK/POP
- 020e: PHONES SURR.
- 020f: FRONT STAGE SURROUND ADVANCE
- 0211: SOUND RETRIEVER AIR
- 0212: ECO MODE 1
- 0213: ECO MODE 2
- 0401: STEREO
- 0402: [)(]PLII MOVIE
- 0403: [)(]PLIIx MOVIE
- 0405: AUTO SURROUND Straight Decode
- 0406: [)(]DIGITAL EX
- 0407: [)(]PLIIx MOVIE
- 0408: DTS +Neo:6
- 0409: ES MATRIX
- 040a: ES DISCRETE
- 040b: DTS-ES 8ch
- 040e: RETRIEVER AIR
- 040f: Neo:X CINEMA
- 0411: Dolby Surround
- 0501: STEREO
- 0502: [)(]PLII MOVIE
- 0503: [)(]PLIIx MOVIE
- 0504: DTS/DTS-HD
- 0505: ALC Straight Decode
- 0506: [)(]DIGITAL EX
- 0507: [)(]PLIIx MOVIE
- 0508: DTS +Neo:6
- 0509: ES MATRIX
- 050a: ES DISCRETE
- 050b: DTS-ES 8ch
- 050e: RETRIEVER AIR
- 050f: Neo:X CINEMA
- 0601: STEREO
- 0602: [)(]PLII MOVIE
- 0603: [)(]PLIIx MOVIE
- 0604: Neo:6 CINEMA
- 0605: STREAM DIRECT NORMAL Straight Decode
- 0606: [)(]DIGITAL EX
- 0607: [)(]PLIIx MOVIE
- 0609: ES MATRIX
- 060a: ES DISCRETE
- 060b: DTS-ES 8ch
- 060c: Neo:X CINEMA
- 060e: NORMAL DIRECT Dolby Surround
- 0701: STREAM DIRECT PURE 2ch
- 0702: [)(]PLII MOVIE
- 0703: [)(]PLIIx MOVIE
- 0704: Neo:6 CINEMA
- 0705: STREAM DIRECT PURE Straight Decode
- 0706: [)(]DIGITAL EX
- 0707: [)(]PLIIx MOVIE
- 0708: (nothing)
- 0709: ES MATRIX
- 070a: ES DISCRETE
- 070b: DTS-ES 8ch
- 070c: Neo:X CINEMA
- 070e: PURE DIRECT Dolby Surround
- 0881: OPTIMUM
- 0e01: HDMI THROUGH
- 0f01: MULTI CH IN

## Example

*demo.Things:

```java
pioneeravr:ipAvr:vsx921 [ address="192.168.188.89" ]
```

*demo.items:

```java
/* Pioneer AVR Items */
Switch vsx921PowerSwitch        "Power"                                (All)    { channel="pioneeravr:ipAvr:vsx921:power" }
Switch vsx921MuteSwitch            "Mute"                    <none>        (All)    { channel="pioneeravr:ipAvr:vsx921:mute" }
Dimmer vsx921VolumeDimmer        "Volume [%.1f] %"        <none>        (All)    { channel="pioneeravr:ipAvr:vsx921:volumeDimmer" }
Number vsx921VolumeNumber        "Volume [%.1f] dB"        <none>        (All)    { channel="pioneeravr:ipAvr:vsx921:volumeDb" }
String vsx921InputSourceSet        "Input"                    <none>        (All)    { channel="pioneeravr:ipAvr:vsx921:setInputSource" }
String vsx921InformationDisplay "Information [%s]"        <none>         (All)    { channel="pioneeravr:ipAvr:vsx921:displayInformation" }
String vsx921ListeningMode "Listening Mode [%s]"        <none>         (All)    { channel="pioneeravr:ipAvr:vsx921:listeningMode" }
String vsx921PlayingListeningMode "Playing Listening Mode [%s]"        <none>         (All)    { channel="pioneeravr:ipAvr:vsx921:playingListeningMode" }
```

*demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame label="Pioneer AVR" {
        Switch item=vsx921PowerSwitch
        Switch item=vsx921MuteSwitch mappings=[ON="Mute", OFF="Un-Mute"]
        Slider item=vsx921VolumeDimmer
        Setpoint item=vsx921VolumeNumber minValue="-80" maxValue="12" step="0.5"
        Switch item=vsx921InputSourceSet mappings=[04="DVD", 15="DVR/BDR", 25="BD"]
        Text item=vsx921InformationDisplay
        Switch item=vsx921ListeningMode mappings=["0009"="Stereo", "0040"="Dolby Surround", "0010"="next"]
        Text item=vsx921PlayingListeningMode
    }
}
```
