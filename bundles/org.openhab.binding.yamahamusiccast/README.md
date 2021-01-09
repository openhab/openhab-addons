# Yamaha MusicCast Binding

Binding to control Yamaha models via their MusicCast protocol (aka Yamaha Extended Control).
With support for 4 zones : main, zone2, zone3, zone4. Main is always present. Zone2, Zone3, Zone4 are read from the model.

UDP events are captured to reflect changes in the binding for:

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

## Supported Things

Each model (AV Receiver, ...) is a Thing. Things are linked to a Bridge for receiving UDP events.

## Discovery

No auto discovery

## Binding Configuration

N/A

## Thing Configuration

| Parameter              | type    | description                                             | Advanced | Required      |
|------------------------|---------|---------------------------------------------------------|----------|---------------|
| configHost             | String  | IP address of the Yamaha model (AVR, ...)               | false    | true          |
| configSyncVolume       | Boolean | Sync volume across linked models (default=false)        | true     | false         |


## Channels

| channel               | type   | description                                                        |
|-----------------------|--------|--------------------------------------------------------------------|
| channelPower          | Switch | Power ON/OFF                                                       |
| channelMute           | Switch | Mute ON/OFF                                                        |
| channelVolume         | Dimmer | Volume as % (recalculated based on Max Volume Model)               |
| channelVolumeAbs      | Number | Volume as absolute value                                           |
| channelInput          | String | See below for list                                                 |
| channelSoundProgram   | String | See below for list                                                 |
| channelSelectPreset   | String | Select Netradio/USB preset (fetched from Model)                    |
| channelSleep          | Number | Fixed values for Sleep : 0/30/60/90/120                            |
| channelMCServer       | String | Select your MusicCast Server or set to Standalone                  |
| channelUnlinkMCServer | Switch | Disband MusicCast Link on Master                                   |
| channelRecallScene    | Number | Select a scene (create your own dropdown list!)                    |
| channelPlayer         | Player | PLAY/PAUSE/NEXT/PREVIOUS/REWIND/FASTFORWARD                        |
| channelArtist         | String | Artist                                                             |
| channelTrack          | String | Track                                                              |
| channelAlbum          | String | Album                                                              |
| channelAlbumArt       | Image  | Album Art                                                          |
| channelRepeat         | String | Toggle Repeat. Available values: Off, One, All                     |
| channelShuffle        | String | Toggle Shuffle. Availabel values: Off, On, Songs, Album            |


| Zones                | description                                          |
|----------------------|------------------------------------------------------|
| Zone1-4              | Zone 1 to 4 to control Power, Volume, ...            |
| playerControls       | Separate zone for Play, Pause, ...                   |

## Full Example

### Bridge & Thing(s)

```
Bridge yamahamusiccast:bridge:bridge "YXC Bridge" {
Thing yamahamusiccast:Device:Living "YXC Living" [configHost="1.2.3.4"]
}
```

### Basic setup

```
Switch YamahaPower "" {channel="yamahamusiccast:Device:Living:main#channelPower"}
Switch YamahaMute "" {channel="yamahamusiccast:Device:Living:main#channelMute"}
Dimmer YamahaVolume "" {channel="yamahamusiccast:Device:Living:main#channelVolume"}
Number YamahaVolumeAbs "" {channel="yamahamusiccast:Device:Living:main#channelVolumeAbs"}
String YamahaInput "" {channel="yamahamusiccast:Device:Living:main#channelInput"}
String YamahaSelectPreset "" {channel="yamahamusiccast:Device:Living:main#channelSelectPreset"}
String YamahaSoundProgram "" {channel="yamahamusiccast:Device:Living:main#channelSoundProgram"}
```

### Player controls

```
Player YamahaPlayer "" {channel="yamahamusiccast:Device:Living:playerControls#channelPlayer"}
String YamahaArt "" {channel="yamahamusiccast:Device:Living:playerControls#channelAlbumArt"}
String YamahaArtist "" {channel="yamahamusiccast:Device:Living:playerControls#channelArtist"}
String YamahaTrack "" {channel="yamahamusiccast:Device:Living:playerControls#channelTrack"}
String YamahaAlbum "" {channel="yamahamusiccast:Device:Living:playerControls#channelAlbum"}
```

### MusicCast setup

The idea here is to select what device/model will be the master. This needs to be done per device/model which will then be the slave.
If you want the *Living* to be the master for the *Kitchen*, select *Living - zone (IP)* from the thing *Kitchen*.
The binding will check if there is already a group active for which *Living* is the master. If yes, this group will be used and *Kitchen* will be added.
If not, a new group will be created.

*Device A*: Living with IP 192.168.1.1
*Device B*: Kitchen with IP 192.168.1.2

Set **channelMCServer** to *Standalone* to remove the device/model from the current active group. The group will keep on exist with other devices/models.
Use **channelUnlinkMCServer** on the Thing which is currently set to master to disband the group.

```
String YamahaMCServer "[%s]" {channel="yamahamusiccast:Device:Living:main#channelMCServer"}
Switch YamahaUnlinkMC "" {channel="yamahamusiccast:Device:Living:main#channelUnlinkMCServer"}
```

#### How to use this in a rule?

The label uses the format _Thinglabel - zone (IP)_.
The value which is sent to OH uses the format _IP***zone_.

```
sendCommand(Kitchen_YamahaMCServer, "192.168.1.1***main")
```

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

## Tested Models

RX-D485 / WX-010 / WX-030 / ISX-80 / YSP-1600 / RX-A860 / R-N303D / EX-A1080 / WXA-050 / HTR-4068 (RX-V479)
MusicCast 20 / WCX-50 / RX-V6A / YAS-306
