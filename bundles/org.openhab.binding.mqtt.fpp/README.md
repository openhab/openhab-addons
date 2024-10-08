# FPP Binding

Binding to control Falcon Player (FPP) Devices using MQTT and HTTP. Status messages are recieved over MQTT and Commands are HTTP Commands.

## Discovery

Autodiscovering is not supported. We have to define the things manually.

## Supported Things

The binding supports one Thing `player` that represents the Falcon Player.
## Thing Configuration

| Parameter    | Description                             | Required | Default |
|--------------|-----------------------------------------|----------|---------|
| `playerIP`   | IP Address or Host Name of FPP Devive   | Y        |         |
| `playerMQTT` | MQTT Topic of FPP Devive Status Updates | Y        |         |

## Channels

| Channel                                | Type               | Description                               |
|----------------------------------------|--------------------|-------------------------------------------|
| `player`                               | Player             | Play/Stop Current Playlist.               |
| `volume`                               | Dimmer             | Playback Audio Volume.                    |
| `status`                               | String             | Playback Status.                          |
| `mode`                                 | String             | Playback Mode.                            |
| `uptime`                               | Number:Time        | Device Uptime.                            |
| `testing-enabled`                      | Switch             | Enabled/Disable Sending Testing Data.     |
| `current-sequence`                     | String (read only) | Currently Playing Sequence File.          |
| `current-song`                         | String (read only) | Currently Playing Audio/Media File.       |
| `current-playlist`                     | String (read only) | Currently Playing Playlist.               |
| `seconds-played`                       | Number:Time        | Sequence Playback time in secs.           |
| `seconds-remaining`                    | Number:Time        | Sequence Playback time remaining in secs. |
| `last-playlist`                        | String             | Lasted Played Playlist.                   |
| `bridging-enabled`                     | Switch             | Is Recieving Bridge Data.                 |
| `multisync-enabled`                    | Switch             | Multisync Mode Enabled.                   |
| `scheduler-current-playlist`           | String (read only) | Scheduler Current Playlist.               |
| `scheduler-current-playlist-start`     | String (read only) | Scheduler Current Playlist Start Time.    |
| `scheduler-current-playlist-end`       | String (read only) | Scheduler Current Playlist End Time.      |
| `scheduler-current-playlist-stop-type` | String (read only) | Scheduler Current Playlist End Type.      |
| `scheduler-next-playlist`              | String (read only) | Next Scheduled Playlist.                  |
| `scheduler-next-playlist-start`        | String (read only) | Next Scheduled Start Time.                |

## Full Example

To use these examples for textual configuration, you must already have a configured MQTT `broker` thing, and know its unique ID.
This UID will be used in the things file and will replace the text `myBroker`.
The first line in the things file will create a `broker` thing and this can be removed if you have already setup a broker in another file or via the UI already.

### fpp.things

```java
Bridge mqtt:broker:myBroker [ host="localhost", secure=false, password="*******", qos=1, username="user"]
Thing mqtt:player:myBroker:mainPlayer "Main Player" (mqtt:broker:myBroker) @ "MQTT"
```

### fpp.items

```java
Player FPP_Player "FPP Player" {channel="mqtt:player:myBroker:mainPlayer:player"}
Dimmer Audio_Volume "Audio Volume" {channel="mqtt:player:myBroker:mainPlayer:volume"}
String Current_Sequence "Current Sequence" {channel="mqtt:player:myBroker:mainPlayer:current-sequence"}
String Current_Song "Current Song" {channel="mqtt:player:myBroker:mainPlayer:current-song"}
String Current_Playlist "Current Playlist" {channel="mqtt:player:myBroker:mainPlayer:current-playlist"}
String Status "FPP Status" {channel="mqtt:player:myBroker:mainPlayer:status"}
String Mode "FPP Mode" {channel="mqtt:player:myBroker:mainPlayer:mode"}
String Last_Playlist "Last Playlist" {channel="mqtt:player:myBroker:mainPlayer:last-playlist"}
Number:Time Seconds_Played "Seconds Played [%d %unit%]" {channel="mqtt:player:myBroker:mainPlayer:seconds-played"}
Number:Time Seconds_Remaining "Seconds Remaining [%d %unit%]" {channel="mqtt:player:myBroker:mainPlayer:seconds-remaining"}
Switch Testing "Testing Mode" {channel="mqtt:player:myBroker:mainPlayer:testing-enabled"}
Switch Multisync "Multisync" {channel="mqtt:player:myBroker:mainPlayer:multisync-enabled"}
```

### fpp.sitemap

```perl
Text label="Main Player"
{
    Player      item=FPP_Player
    Switch      item=Testing
    Slider      item=Audio_Volume
    Text        item=Current_Sequence
    Text        item=Current_Song
    Text        item=Current_Playlist
    Text        item=Status
    Text        item=Mode
    Selection   item=Last_Playlist
    Switch      item=Testing
    Switch      item=Multisync
}
```
