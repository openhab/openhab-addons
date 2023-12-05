# Squeezebox Binding

This binding integrates the [Logitech Media Server](https://www.mysqueezebox.com) and compatible Squeeze players.

## Introduction

Slim Devices was established in 2000, and was first known for its SlimServer used for streaming music, but launched a hardware player named SliMP3 able to play these streams in 2001.
Although the first player was fairly simple only supporting wired Ethernet and MP3 natively, it was followed two years later by a slightly more advanced player which was renamed to Squeezebox.
Other versions followed, gradually adding native support for additional file formats, Wi-Fi-support, gradually adding larger and more advanced displays as well as a version targeting audiophile users.
Support for playing music from external streaming platforms such as Pandora, Napster, Last.fm and Sirius were also added.
The devices in general have two operating modes; either standalone where the device connects to an internet streaming service directly, or to a local computer running the Logitech Media Server or a network-attached storage device.
Both the server software and large parts of the firmware on the most recent players are released under open source licenses.

In 2006, Slim Devices was acquired by Logitech for $20 million USD.
Logitech continued the development of the player until they announced in August 2012 that it would be discontinued.
Given the cross-platform nature of the server and software client, some users have ensured the continued use of the platform by utilizing the Raspberry Pi as dedicated Squeezebox device (both client and server).

Taken from: [Wiki](https://en.wikipedia.org/wiki/Squeezebox_%28network_music_player%29)

## Supported Things

At least one Squeeze Server is required to act as a bridge for Squeeze players on the network.
Squeeze players may be official Logitech products or other players like [Squeeze Lites](https://code.google.com/p/squeezelite/).

## Discovery

A Squeeze Server is discovered through UPnP in the local network.
Once it is added as a Thing the Squeeze Server bridge will discover Squeeze Players automatically.
If your Squeeze Server is not discovered automatically, you can add it manually by creating a .thing file containing something like this (more example [below](https://www.openhab.org/addons/bindings/squeezebox/#thing-configuration)):

```java
Bridge squeezebox:squeezeboxserver:myServer [ ipAddress="192.168.1.10", webport=9000, cliport=9090 ]
```

## Binding Configuration

The binding has the following configuration options, which can be set for "binding:squeezebox":

| Parameter   | Name         | Description                                                                | Required |
| ----------- | ------------ | -------------------------------------------------------------------------- | -------- |
| callbackUrl | Callback URL | URL to use for playing notification sounds, e.g. `http://192.168.0.2:8080` | no       |

When a SqueezeBox is used as an audio sink, the SqueezeBox player connects to openHAB to get the audio stream.
By default, the binding sends the SqueezeBox the URL for getting the audio stream based on the Primary
Address (Network Settings configuration) and the openHAB HTTP port.
Sometimes it is necessary to use the Callback URL to override the default, such as when using a reverse proxy or with some Docker implementations.

## Thing Configuration

The Squeeze Server bridge requires the ip address, web port, and cli port to access it on.
If Squeeze Server authentication is enabled, the userId and password also are required.

Squeeze Players are identified by their MAC address, which is required.
In addition, the notification timeout can be specified.
If omitted, the default timeout value will be used.
A notification volume can be optionally specified, which, if provided, will override the player's current volume level when playing notifications.

Here are some examples of how to define the Squeeze Server and Player things in a things file.

```java
Bridge squeezebox:squeezeboxserver:myServer [ ipAddress="192.168.1.10", webport=9000, cliport=9090 ]
{
    Thing squeezeboxplayer myplayer[ mac="00:f1:bb:00:00:f1" ]
}
```

If Squeeze Server authentication is enabled, the user ID and password can be specified for the Squeeze Server:

```java
Bridge squeezebox:squeezeboxserver:myServer [ ipAddress="192.168.1.10", webport=9000, cliport=9090, userId="yourid", password="yourpassword" ]
{
    Thing squeezeboxplayer myplayer[ mac="00:f1:bb:00:00:f1" ]
}
```

The notification timeout and/or notification volume can be specified for the Squeeze Player:

```java
Bridge squeezebox:squeezeboxserver:myServer [ ipAddress="192.168.1.10", webport=9000, cliport=9090 ]
{
    Thing squeezeboxplayer myplayer[ mac="00:f1:bb:00:00:f1", notificationTimeout=30, notificationVolume=35 ]
}
```

## Server Channels

The Squeezebox server supports the following channel:

| Channel Type ID | Item Type | Description                                                                           |
| --------------- | --------- | ------------------------------------------------------------------------------------- |
| favoritesList   | String    | Comma-separated list of favorite IDs & names, updated whenever list changes on server |

## Player Channels

All devices support some of the following channels:

| Channel Type ID        | Item Type   | Description                                                                                  |
| ---------------------- | ----------- | -------------------------------------------------------------------------------------------- |
| power                  | Switch      | Power on/off your device                                                                     |
| mute                   | Switch      | Mute/unmute your device                                                                      |
| volume                 | Dimmer      | Volume of your device                                                                        |
| stop                   | Switch      | Stop the current title                                                                       |
| control                | Player      | Control the Zone Player, e.g.  play/pause/next/previous/ffward/rewind                        |
| stream                 | String      | Play the given HTTP or file stream (file:// or http://)                                      |
| source                 | String      | Shows the source of the currently playing playlist entry. (i.e. `http://radio.org/radio.mp3` |
| sync                   | String      | Add another player to your device for synchronized playback (other player mac address)       |
| playListIndex          | Number      | Playlist Index                                                                               |
| currentPlayingTime     | Number:Time | Current Playing Time                                                                         |
| duration               | Number:Time | Duration of currently playing track (in seconds)                                             |
| currentPlaylistShuffle | Number      | Current playlist shuffle mode (0 No Shuffle, 1 Shuffle Songs, 2 Shuffle Albums)              |
| currentPlaylistRepeat  | Number      | Current playlist repeat Mode (0 No Repeat, 1 Repeat Song, 2 Repeat Playlist)                 |
| title                  | String      | Title of the current song                                                                    |
| remotetitle            | String      | Remote Title (Radio) of the current song                                                     |
| album                  | String      | Album name of the current song                                                               |
| artist                 | String      | Artist name of the current song                                                              |
| year                   | String      | Release year of the current song                                                             |
| genre                  | String      | Genre name of the current song                                                               |
| albumArtist            | String      | Main artist of the entire album                                                              |
| trackArtist            | String      | Main artist of the track                                                                     |
| band                   | String      | Band/orchestra that performed the work                                                       |
| composer               | String      | Original composer of the work                                                                |
| conductor              | String      | Person who conducted the performance                                                         |
| coverartdata           | Image       | Image data of cover art of the current song                                                  |
| ircode                 | String      | Received IR code                                                                             |
| numberPlaylistTracks   | Number      | Number of playlist tracks                                                                    |
| playFavorite           | String      | ID of Favorite to play (channel's state options contains available favorites)                |
| rate                   | Switch      | "Like" or "unlike" the currently playing song (if supported by the streaming service)        |
| sleep                  | Number      | Power off the player in the specified number of minutes. Sending 0 cancels the timer         |

## Example .Items File

Add the items you want to use to an .items file, for example:

```java
Switch YourPlayer_Power    "Squeezebox Power"  {channel="squeezebox:squeezeboxplayer:736549a3:00042016e7a0:power"}
Dimmer YourPlayer_Volume   "Squeezebox Volume" {channel="squeezebox:squeezeboxplayer:736549a3:00042016e7a0:volume"}
Image  YourPlayer_AlbumArt "Squeezebox Cover"  {channel="squeezebox:squeezeboxplayer:736549a3:00042016e7a0:coverartdata"}
```

## Playing Favorites

Using the **playFavorite** channel, you can play a favorite from the _Favorites_ list on the Logitech Media Server (LMS).
The favorites from the LMS will be populated into the state options of the **playFavorite** channel.
The Selection widget in HABpanel can be used to present the favorites as a choice list.
Selecting from that choice list will play the favorite on the SqueezeBox player.
Currently, only favorites from the root level of the LMS favorites list are exposed on the **playFavorite** channel.

### How to Set Up Favorites

- Add some favorites to your favorites list in LMS (local music playlists, Pandora, Slacker, Internet radio, etc.).
    Keep all favorites at the root level (i.e. favorites in sub-folders will be ignored).
- If you're on an older openHAB build, you may need to delete and re-add your squeezebox server and player things to pick up the new channels.
- Create a new item on each player

```java
String YourPlayer_PlayFavorite "Play Favorite [%s]" { channel="squeezebox:squeezeboxplayer:736549a3:00042016e7a0:playFavorite" }
```

#### For HABpanel (do this for each player)

- Add a Selection widget to your dashboard
- In the Selection widget settings
  - Enter the **YourPlayer_PlayFavorite** item
  - Select _Choices source_ of _Server-provided item options_
  - Modify other settings to suite your taste
- When you load the dashboard and click on the selection widget, you should see the favorites. Selecting a favorite from the list will play it.

#### For Sitemap

- To use state options on the playFavorite channel, simply omit the _mappings_ from the Selection widget.

```java
Selection item=YourPlayer_PlayFavorite label="Play Favorite"
```

## Notifications

### How To Set Up

Squeeze Players can be set up as audio sinks in openHAB.
Please follow the [openHAB multimedia documentation](https://www.openhab.org/docs/configuration/multimedia.html) for setup guidance.

You can set the default notification volume in the player thing configuration.

You can override the default notification volume by supplying it as a parameter to `say` and `playSound`.

You can play notifications from within rules.

```java
rule "Garage Door Open Notification"
when
    Item GarageDoorOpenNotification received command ON
then
    // Play the notification on the default sink at a specified volume level
    say("The garage door is open!", "voicerss:enUS", new PercentType(35))
    // Play the notification on a specific sink
    say("The garage door is open!", "voicerss:enUS", "squeezebox:squeezeboxplayer:5919BEA2-764B-4590-BC70-D74DCC15491B:20cfbf221510")
end
```

And, you can play sounds from the `conf/sounds` directory.

```java
rule "Play Sounds"
when
    Item PlaySounds received command ON
then
    // Play the sound on the default sink
    playSound("doorbell.mp3")
    // Play the sound on a specific sink at a specified volume level
    playSound("squeezebox:squeezeboxplayer:5919BEA2-764B-4590-BC70-D74DCC15491B:20cfbf221510", "doorbell.mp3", new PercentType(45))
end
```

## Rating Songs

Some streaming services, such as Pandora and Slacker, all songs to be rated.
When playing from these streaming services, sending commands to the `rate` channel can be used to _like_ or _unlike_ the currently playing song.
Sending the ON command will _like_ the song.
Sending the OFF command will _unlike_ the song.
If the streaming service doesn't support rating, sending commands to the `rate` channel has no effect.

### Known Issues

- There are some versions of squeezelite that will not correctly play very short duration mp3 files.
    Versions of squeezelite after v1.7 and before v1.8.6 will not play very short duration mp3 files reliably.
    For example, if you're using piCorePlayer (which uses squeezelite), please check your version of squeezelite if you're having trouble playing notifications.
    This bug has been fixed in squeezelite version 1.8.6-985, which is included in piCorePlayer version 3.20.
- When streaming from a remote service (such as Pandora or Spotify), after the notification plays, the Squeezebox Server starts playing a new track, instead of picking up from where it left off on the currently playing track.
- There have been reports that notifications do not play reliably, or do not play at all, when using Logitech Media Server (LMS) version 7.7.5.
    Therefore, it is recommended that the LMS be on a more current version than 7.7.5.
- There have been reports that the LMS does not play some WAV files reliably. If you're using a TTS service that produces WAV files, and the notifications are not playing, try using an MP3-formatted TTS notification.
    This issue reportedly was [fixed in the LMS](https://github.com/Logitech/slimserver/issues/307) by accepting additional MIME types for WAV files.
- The LMS treats player MAC addresses as case-sensitive.
    Therefore, the case of MAC addresses in the Squeeze Player thing configuration must match the case displayed on the _Information_ tab in the LMS Settings.
