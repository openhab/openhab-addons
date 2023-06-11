# MPD Binding

[Music Player Daemon (MPD)](https://www.musicpd.org/) is a flexible, powerful, server-side application for playing music. Through plugins and libraries it can play a variety of sound files while being controlled by its network protocol.

With the openHAB MPD binding you can control Music Player Daemons.

## Supported Things

This binding supports one ThingType: mpd

## Discovery

If zeroconf is enabled in the Music Player Daemon, it is discovered. Each Music Player daemon requires a unique zeroconf_name for correct discovery.

## Thing Configuration

The ThingType mpd requires the following configuration parameters:

| Parameter Label | Parameter ID | Description                                                              | Required |
|-----------------|--------------|--------------------------------------------------------------------------|----------|
| IP Address      | ipAddress    | Host name or IP address of the Music Player Daemon                       | yes      |
| Port            | port         | Port number on which the Music Player Daemon is listening. Default: 6600 | yes      |
| Password        | password     | Password to access the Music Player Daemon                               | no       |

## Channels

The following channels are currently available:

| Channel Type ID | Item Type | Description               |
|-----------------|-----------|---------------------------|
| control         | Player    | Start/Pause/Next/Previous |
| volume          | Dimmer    | Volume in percent         |
| stop            | Switch    | Stop playback             |
| currentalbum    | String    | Current album             |
| currentartist   | String    | Current artist            |
| currentname     | String    | Current name              |
| currentsong     | Number    | Current song              |
| currentsongid   | Number    | Current song id           |
| currenttitle    | String    | Current title             |
| currenttrack    | Number    | Current track             |

## Full Example

### Thing

```java
mpd:mpd:music  [ ipAddress="192.168.1.2", port=6600 ]
```

### Items

```java
Switch morning_music "Morning music"

Player mpd_music_player "Player" { channel = "mpd:mpd:music:control" }
Dimmer mpd_music_volume "Volume [%d %%]" { channel = "mpd:mpd:music:volume" }
Switch mpd_music_stop "Stop" { channel = "mpd:mpd:music:stop" }
String mpd_music_album "Album [%s]" { channel = "mpd:mpd:music:currentalbum" }
String mpd_music_artist "Artist [%s]" { channel = "mpd:mpd:music:currentartist" }
String mpd_music_name "Name [%s]" { channel = "mpd:mpd:music:currentname" }
Number mpd_music_song "Song [%d]" { channel = "mpd:mpd:music:currentsong" }
Number mpd_music_song_id "Song Id [%d]" { channel = "mpd:mpd:music:currentsongid" }
String mpd_music_title "Title [%s]" { channel = "mpd:mpd:music:currenttitle" }
Number mpd_music_track "Track [%d]" { channel = "mpd:mpd:music:currenttrack" }
```

### Sitemap

```perl
Frame label="Music" {
    Default item=mpd_music_player
    Slider item=mpd_music_volume
    Switch item=mpd_music_stop
    Text item=mpd_music_album
    Text item=mpd_music_artist
    Text item=mpd_music_name
    Text item=mpd_music_song
    Text item=mpd_music_song_id
    Text item=mpd_music_title
    Text item=mpd_music_track
}
```

### Rule

```java
rule "turn on morning music"
when
        Item morning_music changed to ON
then
        val actions = getActions("mpd","mpd:mpd:music")
        if(actions === null) {
                logWarn("myLog", "actions is null")
                return
        }

        actions.sendCommand("clear")
        actions.sendCommand("load", "MorningMusic");
        actions.sendCommand("shuffle");
        actions.sendCommand("play");
end
```
