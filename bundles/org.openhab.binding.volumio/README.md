# volumio Binding

This binding integrates the open-source Music Player [Volumio](https://www.volumio.com).. 

## Supported Things


All available Volumio (playback) modes are supported by this binding.

## Discovery

The volumio devices are discovered through UPnP in the local network and all devices are put in the Inbox.


## Binding Configuration

The binding has the following configuration options, which can be set for "binding:sonos":

| Parameter   | Name             | Description                                                                | Required |
| ----------- | ---------------- | -------------------------------------------------------------------------- | -------- |
| hostname    | Hostanem         | The hostname of the Volumio player.                                        | yes      |
| port        | Port             | The port of your volumio2 device (default is 3000)                         | yes      |
| protocol    | Protocol         | The protocol of your volumio2 device (default is http)                     | yes      |
| timeout     | Timeout          | Connection-Timeout in ms                                                   | no       |


## Thing Configuration

The Volumio Thing requires the hostname, port and protocol as a configuration value in order for the binding to know how to access it.
Additionally, a connection timeout (in ms)can be configured.
In the thing file, this looks e.g. like

```java
Thing volumio:player:VolumioLivingRoom "Volumio" @ "Living Room" [hostname="volumio.local", protocol="http"]
```
### `sample` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | The hostname of the Volumio player.   | N/A     | yes      | no       |
| port            | text    | The port of your volumio2 device.     | 3000    | yes      | no       |
| protocol        | text    | The protocol of your volumio2 device. | http    | yes      | no       |
| timeout         | integer | Connection-Timeout in ms.             | 5000    | no       | yes      |

## Channels

The devices support the following channels:


| Channel        | Type   | Read/Write | Description                                                                                                          |
|----------------|--------|------------|----------------------------------------------------------------------------------------------------------------------|
| title          | String | R          | Title of the song currently playing.                                                                                 |
| artist         | String | R          | Name of the artist currently playing.                                                                                |
| album          | String | R          | Name of the album currently playing.                                                                                 |
| volume         | Dimmer | RW         | Set or get the master volume.                                                                                        |
| player         | Player | RW         | The State channel contains state of the Volumio Player.                                                              |
| albumArt       | Image  | R          | Cover Art for the currently played track.                                                                            |
| trackType      | String | R          | Tracktype of the currently played track.                                                                             |
| playRadioStream| String | RW         | Play the given radio stream.                                                                                         |
| playPlaylist   | String | RW         | Playback a playlist identifed by its name.                                                                           |
| clearQueue     | Switch | RW         | Clear the current queue.                                                                                             | 
| playURI        | Switch | RW         | Play the stream at given uri.                                                                                        |
| playFile       | Switch | RW         | Play a file, located on your Volumio device at the given absolute path, e.g."mnt/INTERNAL/song.mp3"                  |
| random         | Switch | RW         | Activate random mode.                                                                                                |
| repeat         | Switch | RW         | Activate repeat mode.                                                                                                |
| systemCommand  | Switch | RW         | Sends a system command to Volumio. This allows to shutdown/reboot Volumio. Use "Shutdown"/"Reboot" as String command.|
| stopCommand    | Switch | RW         | Sends a Stop Command to Volumio. This allows to stop the player. Use "stop" as string command.                       |


## Full Example

demo.things:

```java
Thing volumio:player:VolumioLivingRoom "Volumio" @ "Living Room" [hostname="volumio.local", protocol="http"]

```

demo.items:

```java
String	Volumio_CurrentTitle	    "Current Titel [%s]"	   <musicnote>      {channel="volumio:player:VolumioLivingRoom:title"}
String	Volumio_CurrentArtist	    "Current Artist [%s]"	                    {channel="volumio:player:VolumioLivingRoom:artist"}
String	Volumio_CurrentAlbum	    "Current Album [%s]"	                    {channel="volumio:player:VolumioLivingRoom:album"}
Dimmer	Volumio_CurrentVolume	    "CurrentVolume [%.1f %%]"  <soundvolume>	{channel="volumio:player:VolumioLivingRoom:volume"}
Player	Volumio	                    "Current Status [%s]"	   <volumiologo>    {channel="volumio:player:VolumioLivingRoom:player"}
String	Volumio_CurrentTrackType	"Current trackType [%s]"   <musicnote>      {channel="volumio:player:VolumioLivingRoom:trackType"}

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

## Any custom content here!
_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
