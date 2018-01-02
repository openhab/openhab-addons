# Kodi Binding

[Kodi](https://kodi.tv) (formerly known as XBMC) is an free and open source (GPL) software media center for playing videos, music, pictures, games, and more.
Kodi runs on Linux, OS X, BSD, Windows, iOS, and Android.
It allows users to play and view most videos, music, podcasts, and other digital media files from local and network storage media and the internet.

The Kodi Binding integrated Kodi media center support with openHAB, allowing both controlling the player as well as retrieving player status data like the currently played movie title.

The Kodi binding is the successor to the openHAB 1.x xbmc binding.

## Preparation

In order to allow control of Kodi by this binding, you need to enable the Kodi application remote control feature.
Please enable "Allow remote control from applications on this/other systems" in the Kodi settings menu under:

*   Settings ➔ Services ➔ Control ➔ Allow remote control from applications on this/other systems

To make use of the auto-discovery feature, you additionally need to enable "Allow control of Kodi via UPnP" in the Kodi settings menu.

*   Settings ➔ Services ➔ UPnP / DLNA ➔ Allow remote control via UPnP

## Supported Things

This binding provides only one thing type: The Kodi media center.
Create one Kodi thing per Kodi instance available in your home automation system.

All Kodi devices are registered as an audio sink in the ESH/openHAB2 framework.

## Discovery

The binding supports auto-discovery for available and prepared (see above) instances of the Kodi media center on your local network.
Auto-discovery is enabled by default.
To disable it, you can add the following line to `<openHAB-conf>/services/runtime.cfg`:

```
org.openhab.kodi:enableAutoDiscovery=false
```

## Binding Configuration

The following configuration options are available for the Kodi binding:

| Parameter     | Name         | Description                                                                | Required |
|---------------|--------------|----------------------------------------------------------------------------|----------|
| `callbackUrl` | Callback URL | URL to use for playing notification sounds, e.g. `http://192.168.0.2:8080` | no       |

### Thing Configuration

The Kodi thing requires the IP address of the device hosting your Kodi media center instance, the TCP port to access it (default: `9090`) and the HTTP port to build URLs to the Kodi webinterface for downloading thumbnail and fanart images (default: `8080`).
You optionally can define a `httpUser` and a `httpPassword` parameter if the access to your Kodi webinterface is protected.
The IP address will be found by the auto-discovery feature.

A manual setup through a `things/kodi.things` file could look like this:

```
Thing kodi:kodi:myKodi "Kodi" @ "Living Room" [ipAddress="192.168.1.100", port="9090", httpPort="8080"]
```

## Channels

The Kodi thing supports the following channels:

| Channel Type ID  | Item Type | Description                                                                                                                                                                                  |
|------------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mute             | Switch    | Mute/unmute your playback                                                                                                                                                                    |
| volume           | Dimmer    | Read or control the volume of your playback                                                                                                                                                  |
| control          | Player    | Control the Kodi player, e.g.  `PLAY`, `PAUSE`, `NEXT`, `PREVIOUS`, `FASTFORWARD`, `REWIND`                                                                                                  |
| stop             | Switch    | Write `ON` to this channel: Stops the Kodi player. If this channel is `ON`, the player is stopped, otherwise kodi is in another state (see control channel)                                  |
| title            | String    | Title of the currently played song/movie/tv episode                                                                                                                                          |
| showtitle        | String    | Title of the currently played tv-show; empty for other types                                                                                                                                 |
| album            | String    | Album name of the currently played song                                                                                                                                                      |
| artist           | String    | Artist name of the currently played song or director of the currently played movie                                                                                                           |
| playuri          | String    | Plays the file with the provided URI                                                                                                                                                         |
| pvr-open-tv      | String    | Opens the PVR TV channel with the provided name                                                                                                                                              |
| pvr-open-radio   | String    | Opens the PVR Radio channel with the provided name                                                                                                                                           |
| pvr-channel      | String    | Title of the currently played PVR channel                                                                                                                                                    |
| shownotification | String    | Shows the provided notification message on the screen                                                                                                                                        |
| input            | String    | Allows to control Kodi. Valid values are: `Up`, `Down`, `Left`, `Right`, `Select`, `Back`, `Home`, `ContextMenu`, `Info`, `ShowOSD`, `ShowPlayerProcessInfo`, `SendText` and `ExecuteAction` |
| inputtext        | String    | This channel emulates a keyboard input                                                                                                                                                       |
| systemcommand    | String    | This channel allows to send commands to `shutdown`, `suspend`, `hibernate`, `reboot` kodi                                                                                                    |
| mediatype        | String    | The media type of the current file. Valid return values are: `unknown`, `channel`, `episode`, `movie`, `musicvideo`, `picture`, `radio`, `song`, `video`                                     |
| thumbnail        | Image     | The URL to the thumbnail of the current file                                                                                                                                                 |
| fanart           | Image     | The URL to the fanart of the current file                                                                                                                                                    |                                                                          |

### Channel Configuration

**group** The PVR channels can be put into user-defined PVR channel groups.
There are two default PVR channel groups. One for PVR TV channels and one for PVR radio channels. The default labels are "All channels" (in german systems "Alle Kanäle").
You have to adjust this configuration to use the `pvr-open-tv` and `pvr-open-radio` channels properly.
You can optionally configure an user-defined PVR channel group.

A manual setup through a `things/kodi.things` file could look like this:

```
Thing kodi:kodi:myKodi "Kodi" @ "Living Room" [ipAddress="192.168.1.100", port="9090"] {
    Channels:
        Type pvr-open-tv : pvr-open-tv [
            group="All channels"
        ]
}
```

## Item Configuration

demo.items

```
Switch myKodi_mute          "Mute"                  { channel="kodi:kodi:myKodi:mute" }
Dimmer myKodi_volume        "Volume [%d]"           { channel="kodi:kodi:myKodi:volume" }
Player myKodi_control       "Control"               { channel="kodi:kodi:myKodi:control" }
Switch myKodi_stop          "Stop"                  { channel="kodi:kodi:myKodi:stop" }
String myKodi_title         "Title [%s]"            { channel="kodi:kodi:myKodi:title" }
String myKodi_showtitle     "Show title [%s]"       { channel="kodi:kodi:myKodi:showtitle" }
String myKodi_album         "Album [%s]"            { channel="kodi:kodi:myKodi:album" }
String myKodi_artist        "Artist [%s]"           { channel="kodi:kodi:myKodi:artist" }
String myKodi_playuri       "PlayerURI"             { channel="kodi:kodi:myKodi:playuri" }
String myKodi_pvropentv     "PVR TV channel"        { channel="kodi:kodi:myKodi:pvr-open-tv" }
String myKodi_pvropenradio  "PVR Radio channel"     { channel="kodi:kodi:myKodi:pvr-open-radio" }
String myKodi_pvrchannel    "PVR channel [%s]"      { channel="kodi:kodi:myKodi:pvr-channel" }
String myKodi_notification  "Notification"          { channel="kodi:kodi:myKodi:shownotification" }
String myKodi_input         "Input"                 { channel="kodi:kodi:myKodi:input" }
String myKodi_inputtext     "Inputtext"             { channel="kodi:kodi:myKodi:inputtext" }
String myKodi_systemcommand "Systemcommand"         { channel="kodi:kodi:myKodi:systemcommand" }
String myKodi_mediatype     "Mediatype [%s]"        { channel="kodi:kodi:myKodi:mediatype" }
Image  myKodi_thumbnail                             { channel="kodi:kodi:myKodi:thumbnail" }
Image  myKodi_fanart                                { channel="kodi:kodi:myKodi:fanart" }
```

## Sitemap Configuration

demo.sitemap

```
sitemap demo label="myKodi"
{
    Frame label="myKodi" {
        Switch    item=myKodi_mute
        Slider    item=myKodi_volume
        Selection item=myKodi_control mappings=[PLAY='Play', PAUSE='Pause', NEXT='Next', PREVIOUS='Previous', FASTFORWARD='Fastforward', REWIND='Rewind']
        Default   item=myKodi_control
        Switch    item=myKodi_stop
        Text      item=myKodi_title
        Text      item=myKodi_showtitle
        Text      item=myKodi_album
        Text      item=myKodi_artist
        Selection item=myKodi_pvropentv mappings=[Add your PVR TV channels here ...]
        Selection item=myKodi_pvropenchannel mappings=[Add your PVR radio channels here ...]
        Text      item=myKodi_pvrchannel
        Selection item=myKodi_input mappings=[Up='Up', Down='Down', Left='Left', Right='Right', Select='Select', Back='Back', Home='Home', ContextMenu='ContextMenu', Info='Info']
        Selection item=myKodi_systemcommand mappings=[Shutdown='Herunterfahren', Suspend='Bereitschaft', Reboot='Neustart']
        Text      item=myKodi_mediatype
        Image     item=myKodi_thumbnail
        Image     item=myKodi_fanart
    }
}
```

## Audio Support

All supported Kodi instances are registered as an audio sink in the framework.
Audio streams are sent to the `playuri` channel.
