# Logitech Squeezebox Binding

This binding integrates the [Logitech Media Server](http://www.mysqueezebox.com) and compatible Squeeze players.

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

Taken from: [Wiki](http://en.wikipedia.org/wiki/Squeezebox_%28network_music_player%29)

## Supported Things

At least one Squeeze Server is required to act as a bridge for Squeeze players on the network.
Squeeze players may be official Logitech products or other players like [Squeeze Lites](https://code.google.com/p/squeezelite/).

## Discovery

A Squeeze Server is discovered through UPnP in the local network.
Once it is added as a Thing the Squeeze Server bridge will discover Squeeze Players automatically.

## Binding Configuration

The binding requires no special configuration

## Thing Configuration

The Squeeze Server bridge requires the ip address, web port, and cli port to access it on.
If Squeeze Server authentication is enabled, the userId and password also are required.

Squeeze Players are identified by their MAC address, which is required.
In addition, the notification timeout can be specified.
If omitted, the default timeout value will be used.
A notification volume can be optionally specified, which, if provided, will override the player's current volume level when playing notifications.

Here are some examples of how to define the Squeeze Server and Player things in a things file.

```
Bridge squeezebox:squeezeboxserver:myServer [ ipAddress="192.168.1.10", webport=9000, cliport=9090 ]
{
    Thing squeezeboxplayer myplayer[ mac="00:f1:bb:00:00:f1" ]
}
```

If Squeeze Server authentication is enabled, the user ID and password can be specified for the Squeeze Server:

```
Bridge squeezebox:squeezeboxserver:myServer [ ipAddress="192.168.1.10", webport=9000, cliport=9090, userId="yourid", password="yourpassword" ]
{
    Thing squeezeboxplayer myplayer[ mac="00:f1:bb:00:00:f1" ]
}
```

The notification timeout and/or notification volume can be specified for the Squeeze Player:

```
Bridge squeezebox:squeezeboxserver:myServer [ ipAddress="192.168.1.10", webport=9000, cliport=9090 ]
{
    Thing squeezeboxplayer myplayer[ mac="00:f1:bb:00:00:f1", notificationTimeout=30, notificationVolume=35 ]
}
```

## Server Channels

The Squeezebox server supports the following channel:

| Channel Type ID         | Item Type | Description                                                                            |
|-------------------------|-----------|----------------------------------------------------------------------------------------|
| favoritesList           | String    | Comma-separated list of favorite IDs & names, updated whenever list changes on server  |

## Player Channels

All devices support some of the following channels:

| Channel Type ID         | Item Type | Description                                                                            |
|-------------------------|-----------|----------------------------------------------------------------------------------------|
| power                   | Switch    | Power on/off your device                                                               |
| mute                    | Switch    | Mute/unmute your device                                                                |
| volume                  | Dimmer    | Volume of your device                                                                  |
| stop                    | Switch    | Stop the current title                                                                 |
| control                 | Player    | Control the Zone Player, e.g.  play/pause/next/previous/ffward/rewind                  |
| stream                  | String    | Play the given HTTP or file stream (file:// or http://)                                |
| sync                    | String    | Add another player to your device for synchronized playback (other player mac address) |
| playListIndex           | Number    | Playlist Index                                                                         |
| currentPlayingTime      | Number    | Current Playing Time                                                                   |
| currentPlaylistShuffle  | Number    | Current playlist shuffle mode (0 No Shuffle, 1 Shuffle Songs, 2 Shuffle Albums)        |
| currentPlaylistRepeat   | Number    | Current playlist repeat Mode (0 No Repeat, 1 Repeat Song, 2 Repeat Playlist)           |
| title                   | String    | Title of the current song                                                              |
| remotetitle             | String    | Remote Title (Radio) of the current song                                               |
| album                   | String    | Album name of the current song                                                         |
| artist                  | String    | Artist name of the current song                                                        |
| year                    | String    | Release year of the current song                                                       |
| genre                   | String    | Genre name of the current song                                                         |
| coverartdata            | Image     | Image data of cover art of the current song                                            |
| ircode                  | String    | Received IR code                                                                       |
| numberPlaylistTracks    | Number    | Number of playlist tracks                                                              |
| playFavorite            | String    | ID of Favorite to play (channel's state options contains available favorites)          |

## Playing Favorites

Using the **playFavorite** channel, you can play a favorite from the *Favorites* list on the Logitech Media Server (LMS).
The favorites from the LMS will be populated into the state options of the **playFavorite** channel.
The Selection widget in HABpanel can be used to present the favorites as a choice list.
Selecting from that choice list will play the favorite on the SqueezeBox player.
Currently, only favorites from the root level of the LMS favorites list are exposed on the **playFavorite** channel.

### How to Set Up Favorites

-   Add some favorites to your favorites list in LMS (local music playlists, Pandora, Slacker, Internet radio, etc.).
Keep all favorites at the root level (i.e. favorites in sub-folders will be ignored).

-   If you're on an older openHAB build, you may need to delete and readd your squeezebox server and player things to pick up the new channels.

-   Create a new item on each player

```
String YourPlayer_PlayFavorite "Play Favorite [%s]" { channel="squeezebox:squeezeboxplayer:736549a3:00042016e7a0:playFavorite" }
```

#### For HABpanel (do this for each player)

-   Add a Selection widget to your dashboard

-   In the Selection widget settings

    -  Enter the **YourPlayer_PlayFavorite** item

    -  Select *Choices source* of *Server-provided item options*

    -  Modify other settings to suite your taste

-   When you load the dashboard and click on the selection widget, you should see the favorites.
Selecting a favorite from the list will play it.

#### For Sitemap

-   Currently, the Selection widget in Basic UI doesn’t use the state options.

## Notifications

### How To Set Up

Squeeze Players can be set up as audio sinks in openHAB.
Please follow the [openHAB multimedia documentation](https://www.openhab.org/docs/configuration/multimedia.html) for setup guidance.

You can set the default notification volume in the player thing configuration.

You can override the default notification volume by supplying it as a parameter to `say` and `playSound`.

You can play notifications from within rules.

```
rule "Garage Door Open Notification"
when
    Item GarageDoorOpenNotification received command ON
then
    // Play the notification on the default sink at a specified volume level
    say("The garage door is open!", "voicerss:enUS", 35)
    // Play the notification on a specific sink
    say("The garage door is open!", "voicerss:enUS", "squeezebox:squeezeboxplayer:5919BEA2-764B-4590-BC70-D74DCC15491B:20cfbf221510")
end
```

And, you can play sounds from the `conf/sounds` directory.

```
rule "Play Sounds"
when
    Item PlaySounds received command ON
then
    // Play the sound on the default sink
    playSound("doorbell.mp3")
    // Play the sound on a specific sink at a specified volume level
    playSound("squeezebox:squeezeboxplayer:5919BEA2-764B-4590-BC70-D74DCC15491B:20cfbf221510", "doorbell.mp3", 45)
end
```

### Known Issues

-   There are some versions of squeezelite that will not correctly play very short duration mp3 files.
Versions of squeezelite after v1.7 and before v1.8.6 will not play very short duration mp3 files reliably.
For example, if you're using piCorePlayer (which uses squeezelite), please check your version of squeezelite if you're having trouble playing notifications.
This bug has been fixed in squeezelite version 1.8.6-985, which is included in piCorePlayer version 3.20.

-   When streaming from a remote service (such as Pandora or Spotify), after the notification plays, the Squeezebox Server starts playing a new track, instead of picking up from where it left off on the currently playing track.

-   There have been reports that notifications do not play reliably, or do not play at all, when using Logitech Media Server (LMS) version 7.7.5.
Therefore, it is recommended that the LMS be on a more current version than 7.7.5.

-   There have been reports that the LMS does not play some WAV files reliably.
If you're using a TTS service that produces WAV files, and the notifications are not playing, try using an MP3-formatted TTS notification.
