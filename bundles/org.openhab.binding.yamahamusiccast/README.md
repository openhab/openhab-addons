# Yamaha MusicCast Binding

Binding to control Yamaha models via their MusicCast protocol (aka Yamaha Extended Control).
With support for 4 zones: main, zone2, zone3, zone4. Main is always present. Zone2, Zone3, Zone4 are read from the model.

UDP events are captured to reflect changes in the binding for

- Power
- Mute
- Volume
- Input
- Presets
- Sleep
- Artist
- Track
- Album
- Album Art
- Repeat
- Shuffle
- Play Time
- Total Time
- Musiccast Link

If your Yamaha model doesn't support the MusicCast protocol, please try the [Yamaha Receiver Binding](https://www.openhab.org/addons/bindings/yamahareceiver/#yamaha-receiver-binding) instead.

## Supported Things

Each model (AV Receiver, ...) is a Thing (Thing Type ID: yamahamusiccast:device). Things are linked to a Bridge (Thing Type ID: yamahamusiccast:bridge) for receiving UDP events.

## Discovery

No auto discovery

## Thing Configuration

| Parameter          | Type    | Description                                             | Advanced | Required      |
|--------------------|---------|---------------------------------------------------------|----------|---------------|
| host               | String  | IP address of the Yamaha model (AVR, ...)               | false    | true          |
| syncVolume         | Boolean | Sync volume across linked models (default=false)        | false    | false         |
| defaultAfterMCLink | String  | Default Input value for client when MC Link is broken   | false    | false         |
| volumeDbMin        | Number  | Lowest volume in dB.                                    | true     | false         |
| volumeDbMax        | Number  | Highest volume in dB.                                   | true     | false         |

Default value for _defaultAfterMCLink_ is _NET RADIO_ (as _net_radio_) as most of the models have this on board.
You can also use _RADIO / TUNER_ (as _tuner_).

## Channels

| channel        | type                 | description                                                         |
|----------------|----------------------|---------------------------------------------------------------------|
| power          | Switch               | Power ON/OFF                                                        |
| mute           | Switch               | Mute ON/OFF                                                         |
| volume         | Dimmer               | Volume as % (recalculated based on Max Volume Model)                |
| volumeAbs      | Number               | Volume as absolute value                                            |
| volumeDB       | Number:Dimensionless | Volume in decibel (dB) (availability depends on device)             |
| input          | String               | See below for list                                                  |
| soundProgram   | String               | See below for list                                                  |
| selectPreset   | String               | Select Netradio/USB preset (fetched from Model)                     |
| sleep          | Number               | Fixed values for Sleep: 0/30/60/90/120 in minutes                   |
| recallScene    | Number               | Select a scene (8 defaults scenes are foreseen)                     |
| player         | Player               | PLAY/PAUSE/NEXT/PREVIOUS/REWIND/FASTFORWARD                         |
| artist         | String               | Artist                                                              |
| track          | String               | Track                                                               |
| album          | String               | Album                                                               |
| albumArt       | Image                | Album Art                                                           |
| repeat         | String               | Toggle Repeat. Available values: Off, One, All                      |
| shuffle        | String               | Toggle Shuffle. Available values: Off, On, Songs, Album             |
| playTime       | String               | Play time of current selection: radio, song, track, ...             |
| totalTime      | String               | Total time of current selection: radio, song, track, ...            |
| mclinkStatus   | String               | Select your Musiccast Server or set to Standalone, Server or Client |

| Zones                | description                                          |
|----------------------|------------------------------------------------------|
| zone1-4              | Zone 1 to 4 to control Power, Volume, ...            |
| playerControls       | Separate zone for Play, Pause, ...                   |

## Input List

Firmware v1

cd / tuner / multi_ch / phono / hdmi1 / hdmi2 / hdmi3 / hdmi4 / hdmi5 / hdmi6 / hdmi7 /
hdmi8 / hdmi / av1 / av2 / av3 / av4 / av5 / av6 / av7 / v_aux / aux1 / aux2 / aux / audio1 /
audio2 / audio3 / audio4 / audio_cd / audio / optical1 / optical2 / optical / coaxial1 / coaxial2 /
coaxial / digital1 / digital2 / digital / line1 / line2 / line3 / line_cd / analog / tv / bd_dvd /
usb_dac / usb / bluetooth / server / net_radio / rhapsody / napster / pandora / siriusxm /
spotify / juke / airplay / radiko / qobuz / mc_link / main_sync / none

Firmware v2

cd / tuner / multi_ch / phono / hdmi1 / hdmi2 / hdmi3 / hdmi4 / hdmi5 / hdmi6 / hdmi7 /
hdmi8 / hdmi / av1 / av2 / av3 / av4 / av5 / av6 / av7 / v_aux / aux1 / aux2 / aux / audio1 /
audio2 / audio3 / audio4 / **audio5** / audio_cd / audio / optical1 / optical2 / optical / coaxial1 / coaxial2 /
coaxial / digital1 / digital2 / digital / line1 / line2 / line3 / line_cd / analog / tv / bd_dvd /
usb_dac / usb / bluetooth / server / net_radio / ~~rhapsody~~ /napster / pandora / siriusxm /
spotify / juke / airplay / radiko / qobuz / **tidal** / **deezer** / mc_link / main_sync / none

## Sound Program

munich_a / munich_b / munich / frankfurt / stuttgart / vienna / amsterdam / usa_a / usa_b /
tokyo / freiburg / royaumont / chamber / concert / village_gate / village_vanguard /
warehouse_loft / cellar_club / jazz_club / roxy_theatre / bottom_line / arena / sports /
action_game / roleplaying_game / game / music_video / music / recital_opera / pavilion /
disco / standard / spectacle / sci-fi / adventure / drama / talk_show / tv_program /
mono_movie / movie / enhanced / 2ch_stereo / 5ch_stereo / 7ch_stereo / 9ch_stereo /
11ch_stereo / stereo / surr_decoder / my_surround / target / straight / off

## Full Example

### Bridge & Thing(s)

```java
Bridge yamahamusiccast:bridge:virtual "YXC Bridge" {
    Thing device Living "YXC Living" [host="1.2.3.4", defaultAfterMCLink="none", syncVolume=false, volumeDbMin=-80, volumeDbMax=-10]
}
```

### Basic setup

```java
Switch YamahaPower "" {channel="yamahamusiccast:device:virtual:Living:main#power"}
Switch YamahaMute "" {channel="yamahamusiccast:device:virtual:Living:main#mute"}
Dimmer YamahaVolume "" {channel="yamahamusiccast:device:virtual:Living:main#volume"}
Number YamahaVolumeAbs "" {channel="yamahamusiccast:device:virtual:Living:main#volumeAbs"}
Number:Dimensionless YamahaVolumeDb  "" {channel="yamahamusiccast:device:virtual:Living:main#volumeDB"}
String YamahaInput "" {channel="yamahamusiccast:device:virtual:Living:main#input"}
String YamahaSelectPreset "" {channel="yamahamusiccast:device:virtual:Living:main#selectPreset"}
String YamahaSoundProgram "" {channel="yamahamusiccast:device:virtual:Living:main#soundProgram"}
```

### Player controls

```java
Player YamahaPlayer "" {channel="yamahamusiccast:device:virtual:Living:playerControls#player"}
String YamahaArt "" {channel="yamahamusiccast:device:virtual:Living:playerControls#albumArt"}
String YamahaArtist "" {channel="yamahamusiccast:device:virtual:Living:playerControls#artist"}
String YamahaTrack "" {channel="yamahamusiccast:device:virtual:Living:playerControls#track"}
String YamahaAlbum "" {channel="yamahamusiccast:device:virtual:Living:playerControls#album"}
```

### MusicCast setup

The idea here is to select what device/model will be the master. This needs to be done per device/model which will then be the slave.
If you want the _Living_ to be the master for the _Kitchen_, select _Living - zone (IP)_ from the thing _Kitchen_.
The binding will check if there is already a group active for which _Living_ is the master. If yes, this group will be used and _Kitchen_ will be added.
If not, a new group will be created.

_Device A_: Living with IP 192.168.1.1
_Device B_: Kitchen with IP 192.168.1.2

Set **mclinkStatus** to _Standalone_ to remove the device/model from the current active group. The group will keep on exist with other devices/models.
If the device/model is the server, the group will be disbanded.

```java
String YamahaMCLinkStatus "" {channel="yamahamusiccast:device:Living:main#mclinkStatus"}
```

During testing with the Yamaha Musiccast app, when removing a slave from the group, the status of the client remained _client_ and **input** stayed on _mclink_. Only when changing input, the slave was set to _standalone_. Therefor you can set the parameter **defaultAfterMCLink** to an input value supported by your device to break the whole Musiccast Link in OH.

#### How to use this in a rule?

The label uses the format _Thinglabel - zone (IP)_.
The value which is sent to OH uses the format _IP***zone_.

```java
sendCommand(Kitchen_YamahaMCServer, "192.168.1.1***main")
sendCommand(Kitchen_YamahaMCServer, "")
sendCommand(Kitchen_YamahaMCServer, "server")
sendCommand(Kitchen_YamahaMCServer, "client")
```

## Tested Models

RX-D485 / WX-010 / WX-030 / ISX-80 / YSP-1600 / RX-A860 / R-N303D / EX-A1080 / WXA-050 / HTR-4068 (RX-V479)
MusicCast 20 / WCX-50 / RX-V6A / YAS-306 / ISX-18D / WX-021 / YAS-408
