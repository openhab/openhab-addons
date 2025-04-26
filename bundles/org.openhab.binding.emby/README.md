<!--
    Title: Emby Binding
    Description: Integrates Emby Media Server clients and devices into openHAB
    Author: Zachary Christiansen
    Binding ID: emby
-->

# Emby Binding

The **Emby Binding** integrates [Emby](https://emby.media/), a personal media server, with openHAB.

It allows controlling Emby players and retrieving player status data, such as the currently playing movie title.

This binding supports multiple clients connected to an Emby Media Server.

It enables similar integration and control to the Plex Binding.

For example, you can automatically dim your lights when a video starts playing.

## Supported Things

- **`emby:controller`**  
  A bridge to an instance of an Emby server you want to connect to.

- **`emby:device`**  
  A player device connected to the Emby server that you want to monitor.

## Discovery

This binding supports automatic discovery of both Emby servers and client devices.

- **Bridge Discovery**  
  Automatically finds Emby servers on your local network and offers them as **`emby:controller`** Things.

- **Client Discovery**  
  Once a bridge is online, all connected Emby client devices are detected and offered as **`emby:device`** Things under that bridge.

## Binding Configuration

No global binding-level configuration is required or supported.

## Thing Configuration

There are two types of Things in this binding: the bridge (**`emby:controller`**) and the device (**`emby:device`**).

The bridge must be created before any device Things can be generated.

### `emby:controller` Bridge Configuration

| Name                | Type       | Description                                             | Default | Required | Advanced |
|---------------------|------------|---------------------------------------------------------|---------|----------|----------|
| **`ipAddress`**      | Text       | IP address or hostname of the Emby server.              | N/A     | Yes      | No       |
| **`api`**            | Text       | API Key generated from Emby for authorization.          | N/A     | Yes      | No       |
| **`bufferSize`**     | Integer    | WebSocket buffer size in bytes.                         | N/A     | No       | No       |
| **`refreshInterval`**| Integer    | Polling interval for play-state updates (milliseconds). | N/A     | No       | No       |
| **`discovery`**      | Boolean    | Enable or disable automatic device discovery.           | true    | No       | Yes      |

## Channels

### Preconfigured Channels for Discovered `emby:device`

An automatically discovered **`emby:device`** Thing will include the following preconfigured channels:

| Channel ID        | Item Type       | Config Parameters                                          | Description                                                                                                                                  |
|-------------------|-----------------|------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| **`control`**      | Player           | None                                                       | Indicates and controls playback status (play/pause/next/previous/fast-forward/rewind).                                                       |
| **`stop`**         | Switch           | None                                                       | ON when media is playing; sending OFF stops playback.                                                                                        |
| **`title`**        | String           | None                                                       | Displays the title of the currently playing song.                                                                                            |
| **`mute`**         | Switch           | None                                                       | Indicates mute status; sending ON mutes the player.                                                                                           |
| **`showtitle`**    | String           | None                                                       | Displays the title of the currently playing movie or TV show.                                                                                 |
| **`imageurl`**     | String           | `imageurl_maxHeight`, `imageurl_maxWidth`, `imageurl_type`  | URL to the current media’s artwork. See the [Emby Images documentation](https://github.com/MediaBrowser/Emby/wiki/Images) for details.         |
| **`currenttime`**  | Number:Time      | None                                                       | Current playback position of the media.                                                                                                      |
| **`duration`**     | Number:Time      | None                                                       | Total duration of the current media.                                                                                                         |
| **`mediatype`**    | String           | None                                                       | Media type of the current media (e.g., Movie, Episode).                                                                                       |
| **`sendplay`**     | String           | None                                                       | Send a JSON-formatted command to play a list of item IDs on the client. See the [Emby Remote Control API](https://github.com/MediaBrowser/Emby/wiki/Remote-control). |

## Full Example

### Thing Configuration

```java
Bridge emby:controller:myEmbyServer [
    ipAddress="192.168.1.100",
    api="YOUR_EMBY_API_KEY",
    bufferSize=16384,
    refreshInterval=2000,
    discovery=true
] {
    Thing emby:device:myClientDevice [
        deviceID="YOUR_CLIENT_DEVICE_ID"
    ]
}
```
### Item Configuration
```
Switch      Emby_PlayPause   "Play/Pause"        { channel="emby:device:myEmbyServer:myClientDevice:control" }
Switch      Emby_Stop        "Stop"              { channel="emby:device:myEmbyServer:myClientDevice:stop" }
Switch      Emby_Mute        "Mute"              { channel="emby:device:myEmbyServer:myClientDevice:mute" }
String      Emby_Title       "Title [%s]"        { channel="emby:device:myEmbyServer:myClientDevice:title" }
String      Emby_ShowTitle   "Show Title [%s]"   { channel="emby:device:myEmbyServer:myClientDevice:showtitle" }
Number:Time Emby_CurrentTime "Current Time [%d %unit%]" { channel="emby:device:myEmbyServer:myClientDevice:currenttime" }
Number:Time Emby_Duration    "Duration [%d %unit%]"     { channel="emby:device:myEmbyServer:myClientDevice:duration" }
String      Emby_MediaType   "Media Type [%s]"   { channel="emby:device:myEmbyServer:myClientDevice:mediatype" }
String      Emby_ImageURL    "Artwork URL [%s]"  { channel="emby:device:myEmbyServer:myClientDevice:imageurl" }
String      Emby_SendPlay    "Send Play [%s]"    { channel="emby:device:myEmbyServer:myClientDevice:sendplay" }

```

### Sitemap Configuration
```
sitemap emby label="Emby Control"
{
    Frame label="Controls" {
        Switch item=Emby_PlayPause
        Switch item=Emby_Stop
        Switch item=Emby_Mute
    }
    Frame label="Now Playing" {
        Text item=Emby_Title
        Text item=Emby_ShowTitle
        Text item=Emby_MediaType
        Text item=Emby_CurrentTime
        Text item=Emby_Duration
        Text item=Emby_ImageURL
    }
}

```

## Advanced
You can also use the generalCommand and generalCommand_withArguments channels to send arbitrary commands supported by the Emby API.

These channels allow you to implement custom interactions beyond the predefined set of controls.