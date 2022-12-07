# Bose SoundTouch Binding

This binding supports the Bose SoundTouch multiroom system.

## Supported Things

The following Bose devices are supported:

| Name                                  | Thing Type                  |
|---------------------------------------|-----------------------------|
| Bose SoundTouch 10                    | 10                          |
| Bose SoundTouch 20                    | 20                          |
| Bose SoundTouch 30                    | 30                          |
| Bose SoundTouch 300                   | 300                         |
| Bose Wave SoundTouch Music System IV  | waveSoundTouchMusicSystemIV |
| Bose SoundTouch Wireless Link Adapter | wirelessLinkAdapter         |
| Bose SoundTouch SA-5 Amplifier        | sa5Amplifier                |
| Any other Bose SoundTouch device      | device                      |

## Discovery

Speakers are automatically discovered using mDNS in the local network.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

All thing types have the same configuration parameters:

| Parameter Name      | Type   | Required | Description                                                  |
|---------------------|--------|----------|--------------------------------------------------------------|
| host                | String | Yes      | The host name or IP address of the device                    |
| macAddress          | String | Yes      | The MAC address of the used interface (format "123456789ABC")|
| appKey              | String |  No      | An authorization key used to identify the client application |

The required properties are set when using discovery. For manual configuration, these values can be found in the Bose smartphone app (Settings -> About -> Device Name).
Note that the device might have two MAC addresses, one for ethernet and one for Wifi.

The authorization key is used to identify the client application when using the Notification API. It must be requested from the developer portal.

## Channels

All devices share the same set of channels, while some of them might not be available on all devices.

| Channel ID                | Item Type | Description                                                  |
|---------------------------|-----------|--------------------------------------------------------------|
| keyCode                   | String    | Simulates pushing a remote control button                    |
| mute                      | Switch    | Mutes the sound                                              |
| notificationsound         | String    | Play a notification sound by a given URI                     |
| nowPlayingAlbum           | String    | Current playing album name                                   |
| nowPlayingArtist          | String    | Current playing artist name                                  |
| nowPlayingArtwork         | Image     | Artwork for the current playing song                         |
| nowPlayingDescription     | String    | Description to current playing song                          |
| nowPlayingGenre           | String    | Genre of current playing song                                |
| nowPlayingItemName        | String    | Visible description shown in display                         |
| nowPlayingStationLocation | String    | Location of current playing radio station                    |
| nowPlayingStationName     | String    | Name of current playing radio station                        |
| nowPlayingTrack           | String    | Track currently playing                                      |
| operationMode             | String    | Current Operation Mode                                       |
| playerControl             | Player    | Control the Player                                           |
| power                     | Switch    | SoundTouch power state                                       |
| preset                    | Number    | 1-6 Preset of Soundtouch, >7 Binding Presets                 |
| rateEnabled               | Switch    | Current source allows rating                                 |
| saveAsPreset              | Number    | A selected presetable item is saved as preset with number >6 |
| skipEnabled               | Switch    | Current source allows skipping to next track                 |
| skipPreviousEnabled       | Switch    | Current source allows scrolling through tracks               |
| volume                    | Dimmer    | Set or get the volume                                        |
| bass                      | Number    | Bass (-9 minimum, 0 maximum)                                 |

The _notificationsound_ channel has the following optional configuration parameters:

- notificationVolume - Desired volume level while playing the notification, it must be between 10 and 70 (inclusive). A value outside this range will result in an error and not play the notification.
- notificationService - The service providing the notification
- notificationReason - The reason for the notification
- notificationMessage - Further details about the notification

The texts for the notification service, reason and message appear on the device display (when available) and the SoundTouch application screen.
Upon completion of the notification, the speaker volume returns to its original value. If not present, the notification will play at the existing volume level.

## Full Example

Things:

```java
bosesoundtouch:device:demo @ "Living"  [ host="192.168.1.2", macAddress="123456789ABC" ]
```

Items:

```java
Switch  Bose1_Power                      "Power: [%s]"          <switch>      { channel="bosesoundtouch:device:demo:power" }
Dimmer  Bose1_Volume                     "Volume: [%d %%]"      <volume>      { channel="bosesoundtouch:device:demo:volume" }
Number  Bose1_Bass                       "Bass: [%d %%]"        <volume>      { channel="bosesoundtouch:device:demo:bass" }
Switch  Bose1_Mute                       "Mute: [%s]"           <volume_mute> { channel="bosesoundtouch:device:demo:mute" }
String  Bose1_OperationMode              "OperationMode: [%s]"  <text>        { channel="bosesoundtouch:device:demo:operationMode" }
String  Bose1_PlayerControl              "Player Control: [%s]" <text>        { channel="bosesoundtouch:device:demo:playerControl" }
Number  Bose1_Preset                     "Preset: [%d]"         <text>        { channel="bosesoundtouch:device:demo:preset" }
Number  Bose1_SaveAsPreset               "Save as Preset: [%d]" <text>        { channel="bosesoundtouch:device:demo:saveAsPreset" }
String  Bose1_KeyCode                    "Key Code: [%s]"       <text>        { channel="bosesoundtouch:device:demo:keyCode" }
Switch  Bose1_RateEnabled                "Rate: [%s]"           <switch>      { channel="bosesoundtouch:device:demo:rateEnabled" }
Switch  Bose1_SkipEnabled                "Skip: [%s]"           <switch>      { channel="bosesoundtouch:device:demo:skipEnabled" }
Switch  Bose1_SkipPreviousEnabled        "SkipPrevious: [%s]"   <switch>      { channel="bosesoundtouch:device:demo:skipPreviousEnabled" }
String  Bose1_nowPlayingAlbum            "Album: [%s]"          <text>        { channel="bosesoundtouch:device:demo:nowPlayingAlbum" }
String  Bose1_nowPlayingArtist           "Artist: [%s]"         <text>        { channel="bosesoundtouch:device:demo:nowPlayingArtist" }
Image   Bose1_nowPlayingArtwork          "Artwork"              <text>        { channel="bosesoundtouch:device:demo:nowPlayingArtwork" }
String  Bose1_nowPlayingDescription      "Description: [%s]"    <text>        { channel="bosesoundtouch:device:demo:nowPlayingDescription" }
String  Bose1_nowPlayingGenre            "Genre: [%s]"          <text>        { channel="bosesoundtouch:device:demo:nowPlayingGenre" }
String  Bose1_nowPlayingItemName         "Playing: [%s]"        <text>        { channel="bosesoundtouch:device:demo:nowPlayingItemName" }
String  Bose1_nowPlayingStationLocation  "Radio Location: [%s]" <text>        { channel="bosesoundtouch:device:demo:nowPlayingStationLocation" }
String  Bose1_nowPlayingStationName      "Radio Name: [%s]"     <text>        { channel="bosesoundtouch:device:demo:nowPlayingStationName" }
String  Bose1_nowPlayingTrack            "Track: [%s]"          <text>        { channel="bosesoundtouch:device:demo:nowPlayingTrack" }
```

Sitemap:

```perl
sitemap demo label="Bose Test Items"
{
 Frame label="Bose 1" {
        Switch item=Bose1_Power
  Slider item=Bose1_Volume
  Text item=Bose1_Bass
  Switch item=Bose1_Mute
  Text item=Bose1_OperationMode
  Text item=Bose1_PlayerControl
  Text item=Bose1_Preset
  Text item=Bose1_SaveAsPreset
  Text item=Bose1_KeyCode
  Text item=Bose1_nowPlayingAlbum
  Text item=Bose1_nowPlayingArtist
  Text item=Bose1_nowPlayingArtwork
  Text item=Bose1_nowPlayingDescription
  Text item=Bose1_nowPlayingGenre
  Text item=Bose1_nowPlayingItemName
  Text item=Bose1_nowPlayingStationLocation
  Text item=Bose1_nowPlayingTrack
 }
}
```
