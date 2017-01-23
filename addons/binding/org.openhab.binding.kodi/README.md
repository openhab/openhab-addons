# Kodi Binding

[Kodi](https://kodi.tv) (formerly known as XBMC) is an free and open source (GPL) software media center for playing videos, music, pictures, games, and more.
Kodi runs on Linux, OS X, Windows, iOS, and Android.
It allows users to play and view most videos, music, podcasts, and other digital media files from local and network storage media and the internet.

The Kodi Binding integrated Kodi media center support with openHAB, allowing both controlling the player as well as retrieving player status data like the currently played movie title.

The Kodi binding is the successor to the openHAB 1.x xbmc binding.

## Preparation

In order to allow control of Kodi by this binding, you need to enable the Kodi application remote control feature.
Please enable "Allow remote control form applications" in the Kodi Setting menu under:

* Settings ➔ Services ➔ Control ➔ Allow remote control from applications on this/other systems

## Supported Things

This binding provides only one thing type: The Kodi media center.
Create one Kodi thing per Kodi instance available in your home automation system.

All Kodi devices are registered as an audio sink in the ESH/openHAB 2 framework.


## Discovery

The binding supports auto-discovery for available and prepared (see above) instances of the Kodi media center on your local network.
Auto-discovery is enabled by default.
To disable it, you can add the following line to `<openHAB-conf>/services/runtime.cfg`:

```
org.openhab.kodi:enableAutoDiscovery=false
```

## Binding Configuration

The following configuration options are available for the Kodi binding:

| Parameter | Name | Description | Required |
|-----------|------|-------------|----------|
| `callbackUrl` | Callback URL | URL to use for playing notification sounds, e.g. `http://192.168.0.2:8080` | no |


## Thing Configuration

The Kodi thing requires the IP address of the device hosting your Kodi media center instance and the TCP port to access it on (default: `9090`).
These parameters will be found by the auto-discovery feature.

A manual setup through a `things/kodi.things` file could look like this:

```
kodi:kodi:myKodi [ipAddress="192.168.1.100", port="9090"]
```

## Channels

The Kodi thing supports the following channels:

| Channel Type ID         | Item Type    | Description  |
|-------------------------|--------------|--------------|
| mute                    | Switch       | Mute/unmute your playback |
| volume                  | Dimmer       | Read or control the volume of your playback |
| control                 | Player       | Control the Kodi player, e.g.  play/pause/next/previous/ffward/rewind |
| stop                    | Switch       | Stops the Kodi player |
| title                   | String       | Title of the currently played song/movie/tv episode |
| showtitle               | String       | Title of the currently played tv-show; empty for other types |
| album                   | String       | Album name of the currently played song |
| artist                  | String       | Artist name of the currently played song or director of the currently played movie|
| playuri                 | String       | Plays the file with the provided URI |
| shownotification        | String       | Shows the provided notification message on the screen |
| input                   | String       | Allows to control Kodi. Valid values are: `Up`, `Down`, `Left`, `Right`, `Select`, `Back`, `Home`, `ContextMenu`, `Info`, `ShowCodec`, `ShowOSD` |
| inputtext               | String       | This channel emulates a keyboard input |
| systemcommand           | String       | This channel allows to send commands to shutdown/suspend/hibernate/reboot kodi |
| mediatype               | String       | The media type of the current file. e.g. song or movie | 


## Audio Support

All supported Kodi instances are registered as an audio sink in the framework.
Audio streams are sent to the `playuri` channel.
