# Volumio Binding

This binding integrates the open-source Music Player [Volumio](https://www.volumio.com).

## Supported Things


All available Volumio (playback) modes are supported by this binding.

## Discovery

The Volumio devices are discovered through mDNS in the local network and all devices are put in the Inbox.


## Binding Configuration

The binding has the following configuration options, which can be set:

| Parameter   | Name             | Description                                                                | Required |
| ----------- | ---------------- | -------------------------------------------------------------------------- | -------- |
| hostname    | Hostname         | The hostname of the Volumio player.                                        | yes      |
| port        | Port             | The port of your volumio2 device (default is 3000)                         | yes      |
| protocol    | Protocol         | The protocol of your volumio2 device (default is http)                     | yes      |
| timeout     | Timeout          | Connection-Timeout in ms                                                   | no       |


## Thing Configuration

The Volumio Thing requires the hostname, port and protocol as a configuration value in order for the binding to know how to access it.
Additionally, a connection timeout (in ms) can be configured.
In the thing file, this looks e.g. like

```java
Thing volumio:player:VolumioLivingRoom "Volumio" @ "Living Room" [hostname="volumio.local", protocol="http"]
```

### `sample` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | The hostname of the Volumio player.   | N/A     | yes      | no       |
| port            | text    | The port of your Volumio device.      | 3000    | yes      | no       |
| protocol        | text    | The protocol of your Volumio device.  | http    | yes      | no       |
| timeout         | integer | Connection-Timeout in ms.             | 5000    | no       | yes      |

## Channels

The devices support the following channels:


| Channel           | Type   | Read/Write | Description                                                                                                          |
|-------------------|--------|------------|----------------------------------------------------------------------------------------------------------------------|
| title             | String | R          | Title of the song currently playing.                                                                                 |
| artist            | String | R          | Name of the artist currently playing.                                                                                |
| album             | String | R          | Name of the album currently playing.                                                                                 |
| volume            | Dimmer | RW         | Set or get the master volume.                                                                                        |
| player            | Player | RW         | The State channel contains state of the Volumio Player.                                                              |
| albumArt          | Image  | R          | Cover Art for the currently played track.                                                                            |
| track-type        | String | R          | Tracktype of the currently played track.                                                                             |
| play-radiostream  | String | RW         | Play the given radio stream.                                                                                         |
| play-playlist     | String | RW         | Playback a playlist identified  by its name.                                                                           |
| clear-queue       | Switch | RW         | Clear the current queue.                                                                                             | 
| play-uri          | Switch | RW         | Play the stream at given uri.                                                                                        |
| play-file         | Switch | RW         | Play a file, located on your Volumio device at the given absolute path, e.g."mnt/INTERNAL/song.mp3"                  |
| random            | Switch | RW         | Activate random mode.                                                                                                |
| repeat            | Switch | RW         | Activate repeat mode.                                                                                                |
| system-command    | Switch | RW         | Sends a system command to Volumio. This allows to shutdown/reboot Volumio. Use "Shutdown"/"Reboot" as String command.|
| stop-command      | Switch | RW         | Sends a Stop Command to Volumio. This allows to stop the player. Use "stop" as string command.                       |


## Full Example

demo.things:

```java
Thing volumio:player:VolumioLivingRoom "Volumio" @ "Living Room" [hostname="volumio.local", protocol="http"]
```

demo.items:

```java
String	Volumio_CurrentTitle	    "Current Title [%s]"	    <musicnote>      {channel="volumio:player:VolumioLivingRoom:title"}
String	Volumio_CurrentArtist	    "Current Artist [%s]"	                     {channel="volumio:player:VolumioLivingRoom:artist"}
String	Volumio_CurrentAlbum	    "Current Album [%s]"	                     {channel="volumio:player:VolumioLivingRoom:album"}
Dimmer	Volumio_CurrentVolume	    "Current Volume [%.1f %%]"  <soundvolume>	 {channel="volumio:player:VolumioLivingRoom:volume"}
Player	Volumio	                    "Current Status [%s]"	    <volumiologo>    {channel="volumio:player:VolumioLivingRoom:player"}
String	Volumio_CurrentTrackType	"Current Track Type [%s]"   <musicnote>      {channel="volumio:player:VolumioLivingRoom:track-type"}
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame label="Volumio" {
        Slider item=Volumio_CurrentVolume
        Text item=Volumio
		Text item=Volumio_CurrentTitle
    }
}
```
