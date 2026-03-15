# Emby Binding

The **Emby Binding** integrates [Emby](https://emby.media/), a personal media server, with openHAB.

It allows controlling Emby players and retrieving player status data.
For example, you can monitor the currently playing movie title or automatically dim your lights when playback starts.
This binding supports multiple Emby clients connected to a single Emby Media Server.
It provides functionality similar to the Plex Binding.

## Supported Things

This binding defines the following Thing Type IDs:

- `controller`  
  Represents a connection to an Emby server (a Bridge Thing).

- `device`  
  Represents a client/player device connected to the Emby server.

## Automatic Discovery

The binding supports automatic discovery for both servers (`controller`) and clients (`device`).

## Binding Configuration

There is no global binding-level configuration required or supported.

## Thing Configuration

### `controller` Bridge Configuration

The following Configuration Parameter Keys are available:

| Name            | Type    | Description                                             | Default | Required | Advanced |
|-----------------|---------|---------------------------------------------------------|---------|----------|----------|
| ipAddress       | Text    | IP address or hostname of the Emby server.              | N/A     | Yes      | No       |
| api             | Text    | API Key generated from Emby for authorization.          | N/A     | Yes      | No       |
| bufferSize      | Integer | WebSocket buffer size in bytes.                         | 10,000  | No       | No       |
| refreshInterval | Integer | Polling interval for play-state updates (milliseconds). | 10,000  | No       | No       |
| port            | Integer | Port in which EMBY is listening for communication.      | 8096    | No       | No       |
| discovery       | Boolean | Enable or disable automatic device discovery.           | true    | No       | Yes      |

### `device` Thing Configuration

The following Configuration Parameter Key is available:

- `deviceID`  
  The unique identifier for the client device connected to the Emby server.

## Channels

The following Channel IDs are available for a `device` Thing:

| Channel ID   | Item Type   | Config Parameters                                                     | Description                                                           |
|--------------|-------------|-----------------------------------------------------------------------|-----------------------------------------------------------------------|
| control      | Player      | None                                                                  | Playback control (play, pause, next, previous, fast-forward, rewind). |
| stop         | Switch      | None                                                                  | Indicates playback state; OFF stops playback.                         |
| title        | String      | None                                                                  | Title of the currently playing song.                                  |
| show-title   | String      | None                                                                  | Title of the currently playing movie or TV show.                      |
| mute         | Switch      | None                                                                  | Mute status control.                                                  |
| image-url    | String      | imageUrlMaxHeight, imageMaxWidth, imageUrlType, imageUrlPercentPlayed | URL for current media artwork.                                        |
| current-time | Number:Time | None                                                                  | Current playback position.                                            |
| duration     | Number:Time | None                                                                  | Total media duration.                                                 |
| media-type   | String      | None                                                                  | Type of media (e.g., Movie, Episode).                                 |

## `image-url` Config Parameters

| Parameter Name          | Type    | Default | Description                                                                                                                                                        |
|-------------------------|---------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `imageUrlType`          | Text    | Primary | Specifies the image type to retrieve. Options include `Primary`, `Art`, `Backdrop`, `Banner`, `Logo`, `Thumb`, `Disc`, `Box`, `Screenshot`, `Menu`, and `Chapter`. |
| `imageUrlMaxHeight`     | Text    | None    | The maximum height (in pixels) of the retrieved image.                                                                                                             |
| `imageUrlMaxWidth`      | Text    | None    | The maximum width (in pixels) of the retrieved image.                                                                                                              |
| `imageUrlPercentPlayed` | Boolean | false   | If true, adds an overlay indicating the percent played (e.g., 47%).                                                                                                |

## Full Example

### `emby.things` Example

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

### `emby.items` Example

```java
Switch      Emby_PlayPause   "Play/Pause"        { channel="emby:device:myEmbyServer:myClientDevice:control" }
Switch      Emby_Stop        "Stop"              { channel="emby:device:myEmbyServer:myClientDevice:stop" }
Switch      Emby_Mute        "Mute"              { channel="emby:device:myEmbyServer:myClientDevice:mute" }
String      Emby_Title       "Title [%s]"        { channel="emby:device:myEmbyServer:myClientDevice:title" }
String      Emby_ShowTitle   "Show Title [%s]"   { channel="emby:device:myEmbyServer:myClientDevice:show-title" }
Number:Time Emby_CurrentTime "Current Time [%d %unit%]" { channel="emby:device:myEmbyServer:myClientDevice:current-time" }
Number:Time Emby_Duration    "Duration [%d %unit%]"     { channel="emby:device:myEmbyServer:myClientDevice:duration" }
String      Emby_MediaType   "Media Type [%s]"   { channel="emby:device:myEmbyServer:myClientDevice:media-type" }
String      Emby_ImageURL    "Artwork URL [%s]"  { channel="emby:device:myEmbyServer:myClientDevice:image-url" }
```

### `emby.sitemap` Configuration Example

```perl
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

## Rule Actions

All playback and control commands are now implemented as Rule Actions rather than channels. Use the standard `getActions` API in your rules to invoke these.

### Available Actions

| Action ID                  | Method Signature                                                                                                                                                                        | Description                                                                                             |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| sendPlay                   | `sendPlay(ItemIds: String, PlayCommand: String, StartPositionTicks: Integer?, MediaSourceId: String?, AudioStreamIndex: Integer?, SubtitleStreamIndex: Integer?, StartIndex: Integer?)` | Send a play command with optional parameters to an Emby player.                                         |
| sendGeneralCommand         | `sendGeneralCommand(CommandName: String)`                                                                                                                                               | Send a generic Emby control command (e.g., MoveUp, ToggleMute, GoHome).                                 |
| sendGeneralCommandWithArgs | `sendGeneralCommandWithArgs(CommandName: String, Arguments: String)`                                                                                                                    | Send a generic Emby control command with a JSON arguments blob (e.g., SetVolume, DisplayMessage, etc.). |

### Example Rule (XTend)

```xtend
rule "Play Movie on Emby"
when
    Item MySwitch changed to ON
then
    val embyActions = getActions("emby", "emby:device:myServer:myDevice")
    // Play item IDs "abc,def" immediately
    embyActions.sendPlay("abc,def", "PlayNow", null, null, null, null, null)
end
```

### Example Rule (JavaScript)

```javascript
// inside a JS Scripting rule
let emby = actions.getActions("emby", "emby:device:myServer:myDevice");
emby.sendGeneralCommand("ToggleMute");
```

## References

- [Emby Remote Control API Documentation](https://github.com/MediaBrowser/Emby/wiki/Remote-control)
