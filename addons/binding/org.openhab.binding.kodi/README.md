# Kodi Binding

This binding integrates the Kodi players (which used to be XBMC).

## Introduction


## Supported Things

This binding supports only one thing: The Kodi player. All kodi devices are registered as an audio sink in the framework.


## Discovery

This binding can discover the Kodi players. 

## Binding Configuration

The binding can auto-discover the Kodi players present on your local network. The auto-discovery is enabled by default. To disable it, you can create a file in the services directory called Kodi.cfg with the following content:

```
org.openhab.Kodi:enableAutoDiscovery=false
```

This configuration parameter only controls the Kodi player auto-discovery process, not the openHAB auto-discovery. Moreover, if the openHAB auto-discovery is disabled, the Kodi player auto-discovery is disabled too.

The binding has the following configuration option, which can be set for "binding:kodi":

| Parameter | Name    | Description  | Required |
|-----------------|------------------------|--------------|------------ |
| callbackUrl | Callback URL | URL to use for playing notification sounds, e.g. http://192.168.0.2:8080 | no |


## Thing Configuration

The Kodi player thing requires the ip address and the port to access it on. 
In the thing file, this looks e.g. like
```
Kodi:Kodi:myKodi [ipAddress="192.168.1.100", port="9090"]


##Channels

The Kodi thing supports the following channels:

| Channel Type ID         | Item Type    | Description  |
|-------------------------|--------------|--------------|
| mute                    | Switch       | Mute/unmute your device |
| volume                  | Dimmer       | Volume of your device |
| control                 | Player       | Control the Kodi player, e.g.  play/pause/next/previous/ffward/rewind |
| title                   | String       | Title of the current song|
| album                   | String       | Album name of the current song|
| artist                  | String       | Artist name of the current song|
| playuri                 | String       | Plays the file with the provided URI|
| shownotification        | String       | Shows the provided notification message on the screen|
| input                   | String       | Allows to navigate on the screen. Valid values are: Up, Down, Left, Right, Select, Back, Home, ContextMenu, Info, ShowCodec, ShowOSD|
| inputtext               | String       | This channel emulates the keyboard entry|
| mediatype               | String       | The media type of the current file. e.g. song, movie| 


## Audio Support

All supported kodi devices are registered as an audio sink in the framework.
Audio streams are sent to the `playuri` channel.
