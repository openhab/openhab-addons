# Volumio Binding

This binding integrates the open-source Music Player [Volumio](https://www.volumio.com).

## Supported Things

All available Volumio (playback) modes are supported by this binding.

## Discovery

The Volumio devices are discovered through mDNS in the local network and all devices are put in the Inbox.

## Thing Configuration

| Parameter name  | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | The hostname of your Volumio device.  | N/A     | yes      | no       |
| port            | integer | The port of your Volumio device.      | 3000    | no       | no       |
| protocol        | text    | The protocol of your Volumio device.  | http    | no       | no       |
| timeout         | integer | Connection timeout in milliseconds.   | 5000    | no       | yes      |

### `sample` Thing Configuration

```java
Thing volumio:player:VolumioLivingRoom "Volumio" @ "Living Room" [hostname="volumio.local", protocol="http"]
```

## Channels

The devices support the following channels:


| Channel           | Type   | Read/Write | Description                                                                                                          |
|-------------------|--------|------------|----------------------------------------------------------------------------------------------------------------------|
| title             | String | R          | Title of the song currently playing.                                                                                 |
| artist            | String | R          | Name of the artist currently playing.                                                                                |
| album             | String | R          | Name of the album currently playing.                                                                                 |
| volume            | Dimmer | RW         | Set or get the master volume.                                                                                        |
| player            | Player | RW         | Control the state of the Volumio Player.                                                                             |
| albumArt          | Image  | R          | Cover Art for the currently played track.                                                                            |
| track-type        | String | R          | Track type of the currently played track.                                                                            |
| play-radiostream  | String | RW         | Play the given radio stream.                                                                                         |
| play-playlist     | String | RW         | Play a playlist identified  by its name.                                                                             |
| clear-queue       | Switch | RW         | Clear the current queue.                                                                                             | 
| play-uri          | String | RW         | Play the stream at given uri.                                                                                        |
| play-file         | String | RW         | Play a file, located on your Volumio device at the given absolute path, e.g."mnt/INTERNAL/song.mp3"                  |
| random            | Switch | RW         | Activate random mode.                                                                                                |
| repeat            | Switch | RW         | Activate repeat mode.                                                                                                |
| system-command    | String | RW         | Sends a system command to shutdown or reboot the Volumio device. Use "shutdown" or "reboot" as string command.       |
| stop-command      | String | RW         | Sends a Stop command to stop the player. Use "stop" as string command.                                               |


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
