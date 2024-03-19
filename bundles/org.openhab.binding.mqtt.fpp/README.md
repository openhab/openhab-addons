# FPP Binding

Binding to Controls Falcon Player (FPP) Devices. Uses MQTT to update status. Manually add an 'player' thing to get status.

## Thing Configuration

| Parameter | Description | Required | Default |
|-|-|-|-|
| `playerIP` | IP Address of FPP Devive | Y | |
| `playerMQTT` | MQTT Topic of FPP Devive Status | Y | |

## Channels

| Channel | Type | Description |
|-|-|-|
| `fppPlayer` | Player | Play/Stop Current Playlist. |
| `fppVolume` | Dimmer | Playback Audio Volume. |
| `fppStatus` | String | Playback Status. |
| `fppVersion` | String | Software Version. |
| `fppMode` | String | Playback Mode. |
| `fppUptime` | Number:Time | Device Uptime. |
| `fppTesting` | Switch | Device is in Testing Mode. |
| `fppCurrentSequence` | String (read only) | Currently Playing Sequence File. |
| `fppCurrentSong` | String (read only) | Currently Playing Audio/Media File. |
| `fppCurrentPlaylist` | String (read only) | Currently Playing Playlist. |
| `fppSecPlayed` | Number:Time | Sequence Playback time in secs. |
| `fppSecRemaining` | Number:Time | Sequence Playback time remaining in secs. |
| `fppLastPlaylist` | String | Lasted Played Playlist. |
| `fppUUID` | String | Device UUID. |
| `fppBridging` | Switch | Is Recieving Bridge Data. |
| `fppMultisync` | Switch | Multisync Mode Enabled. |
| `fppSchedulerCurrentPlaylist` | String (read only) | Scheduler Current Playlist. |
| `fppSchedulerCurrentPlaylistStart` | String (read only) | Scheduler Current Playlist Start Time. |
| `fppSchedulerCurrentPlaylistEnd` | String (read only) | Scheduler Current Playlist End Time. |
| `fppSchedulerCurrentPlaylistStopType` | String (read only) | Scheduler Current Playlist End Type. |
| `fppSchedulerNextPlaylist` | String (read only) | Next Scheduled Playlist. |
| `fppSchedulerNextPlaylistStart` | String (read only) | Next Scheduled Start Time. |


## Full Example

To use these examples for textual configuration, you must already have a configured MQTT `broker` thing, and know its unique ID.
This UID will be used in the things file and will replace the text `myBroker`.
The first line in the things file will create a `broker` thing and this can be removed if you have already setup a broker in another file or via the UI already.

*.things

```java
Bridge mqtt:broker:myBroker [ host="localhost", secure=false, password="*******", qos=1, username="user"]
Thing mqtt:player:myBroker:mainPlayer "Main Player" (mqtt:broker:myBroker) @ "MQTT"
```

*.items

```java
Player FPP_Player "FPP Player" {channel="mqtt:player:myBroker:mainPlayer:fppPlayer"}
Dimmer Audio_Volume "Audio Volume" {channel="mqtt:player:myBroker:mainPlayer:fppVolume"}
String Current_Sequence "Current Sequence" {channel="mqtt:player:myBroker:mainPlayer:fppCurrentSequence"}
String Current_Song "Current Song" {channel="mqtt:player:myBroker:mainPlayer:fppCurrentSong"}
String Current_Playlist "Current Playlist" {channel="mqtt:player:myBroker:mainPlayer:fppCurrentPlaylist"}
String Status "FPP Status" {channel="mqtt:player:myBroker:mainPlayer:fppStatus"}
String Version "FPP Version" {channel="mqtt:player:myBroker:mainPlayer:fppVersion"}
String Mode "FPP Mode" {channel="mqtt:player:myBroker:mainPlayer:fppMode"}
String Last_Playlist "Last Playlist" {channel="mqtt:player:myBroker:mainPlayer:fppLastPlaylist"}
Number:Time Seconds_Played "Seconds Played [%d %unit%]" {channel="mqtt:player:myBroker:mainPlayer:fppSecPlayed"}
Number:Time Seconds_Remaining "Seconds Remaining [%d %unit%]" {channel="mqtt:player:myBroker:mainPlayer:fppSecRemaining"}
Switch Testing "Testing Mode" {channel="mqtt:player:myBroker:mainPlayer:fppTesting"}
Switch Multisync "Multisync" {channel="mqtt:player:myBroker:mainPlayer:fppMultisync"}
```

*.sitemap

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
    Text        item=Version
    Text        item=Mode
    Selection   item=Last_Playlist
    Switch      item=Testing
    Switch      item=Multisync
}
```
