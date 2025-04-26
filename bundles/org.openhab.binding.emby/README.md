# Emby Binding

The **Emby Binding** integrates [Emby](https://emby.media/), a personal media server, with openHAB.

It allows controlling Emby players and retrieving player status data.

For example, you can monitor the currently playing movie title or automatically dim your lights when playback starts.

This binding supports multiple Emby clients connected to a single Emby Media Server.

It provides functionality similar to the Plex Binding.

---

# Supported Things

This binding defines the following Thing Type IDs:

- **`emby:controller`**  
  Represents a connection to an Emby server (a Bridge Thing).

- **`emby:device`**  
  Represents a client/player device connected to the Emby server.

---

# Discovery

The binding supports automatic discovery for both servers and clients.

## Bridge Discovery

Emby servers are automatically detected on the local network.

They are offered as **`emby:controller`** Things.

## Client Discovery

When an **`emby:controller`** Bridge is online, connected Emby clients are automatically discovered.

They are offered as **`emby:device`** Things under the respective bridge.

---

# Binding Configuration

There is no global binding-level configuration required or supported.

---

# Thing Configuration

## `emby:controller` Bridge Configuration

The following Configuration Parameter Keys are available:

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| **`ipAddress`** | Text | IP address or hostname of the Emby server. | N/A | Yes | No |
| **`api`** | Text | API Key generated from Emby for authorization. | N/A | Yes | No |
| **`bufferSize`** | Integer | WebSocket buffer size in bytes. | N/A | No | No |
| **`refreshInterval`** | Integer | Polling interval for play-state updates (milliseconds). | N/A | No | No |
| **`discovery`** | Boolean | Enable or disable automatic device discovery. | true | No | Yes |

## `emby:device` Thing Configuration

The following Configuration Parameter Key is available:

- **`deviceID`**  
  The unique identifier for the client device connected to the Emby server.

---

# Channels

The following Channel IDs are available for an `emby:device` Thing:

| Channel ID | Item Type | Config Parameters | Description |
|------------|-----------|-------------------|-------------|
| **`control`** | Player | None | Playback control (play, pause, next, previous, fast-forward, rewind). |
| **`stop`** | Switch | None | Indicates playback state; OFF stops playback. |
| **`title`** | String | None | Title of the currently playing song. |
| **`showtitle`** | String | None | Title of the currently playing movie or TV show. |
| **`mute`** | Switch | None | Mute status control. |
| **`imageurl`** | String | `imageurl_maxHeight`, `imageurl_maxWidth`, `imageurl_type` | URL for current media artwork. |
| **`currenttime`** | Number:Time | None | Current playback position. |
| **`duration`** | Number:Time | None | Total media duration. |
| **`mediatype`** | String | None | Type of media (e.g., Movie, Episode). |
| **`sendplay`** | String | None | Sends a JSON command to play a list of item IDs. |

Additionally, custom remote control channels can be created:

- **`generalCommand`**
- **`generalCommand_withArguments`**

These channels are **extensible**.

Multiple instances of **`generalCommand`** and **`generalCommand_withArguments`** can be added and configured by the user.

This allows defining multiple dedicated commands for different control actions.

---

# Full Example

## Thing Configuration Example

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

## Item Configuration Example

```text
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

## Sitemap Configuration Example

```text
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

---

# Using General Commands with Emby Binding

## General Command (Without Arguments)

**Channel Type:** `generalCommand`

**Item Type:** Switch

### How to Use

- Configure the `generalCommand_CommandName`.
- Send `ON` to trigger the command.
- `OFF` is ignored.

### Supported Commands

| Command Name | Description |
|--------------|-------------|
| MoveUp | Move focus up. |
| MoveDown | Move focus down. |
| MoveLeft | Move focus left. |
| MoveRight | Move focus right. |
| PageUp | Page up through a list. |
| PageDown | Page down through a list. |
| PreviousLetter | Scroll to previous letter. |
| NextLetter | Scroll to next letter. |
| ToggleOsdMenu | Show/hide OSD. |
| ToggleContextMenu | Show/hide context menu. |
| ToggleMute | Toggle mute. |
| Select | Confirm selection. |
| Back | Navigate back. |
| TakeScreenshot | Capture screen. |
| GoHome | Return to Home. |
| GoToSettings | Open Settings. |
| VolumeUp | Increase volume. |
| VolumeDown | Decrease volume. |
| ToggleFullscreen | Toggle fullscreen. |
| GoToSearch | Open Search. |

## General Command with Arguments

**Channel Type:** `generalCommand_withArguments`

**Item Type:** String

### How to Use

- Configure the `generalCommand_CommandName`.
- Send the JSON payload (only the inside `{}` part).

### Examples

| Command Name | Required Arguments | Example |
|--------------|--------------------|---------|
| SetVolume | `Volume` (0–100) | `Volume:50` |
| SetAudioStreamIndex | `Index` (integer) | `Index:2` |
| SetSubtitleStreamIndex | `Index` (-1 disables) | `Index:-1` |
| DisplayContent | `ItemName`, `ItemId`, `ItemType` | `ItemName:"Movie",ItemId:"123",ItemType:"Video"` |
| PlayTrailers | `ItemId` | `ItemId:"456"` |
| SendString (Future) | `String` | `String:"Hello"` |
| DisplayMessage (Future) | `Header`, `Text`, `TimeoutMs` optional | `Header:"Alert",Text:"Starting Movie"` |

---

# Example Thing with Custom General Commands

```yaml
Bridge emby:controller:server "Emby Server" [
    api="YOUR_API_KEY",
    ipAddress="192.168.1.100",
    port=8096,
    refreshInterval=5000
] {
    Thing device livingroomtv "Living Room TV" [
        deviceID="device12345"
    ] {
        Channels:
            Type generalCommand : MoveUpButton "Move Up" [
                generalCommand_CommandName="MoveUp"
            ]
            Type generalCommand : ToggleMuteButton "Toggle Mute" [
                generalCommand_CommandName="ToggleMute"
            ]
            Type generalCommand_withArguments : SetVolumeButton "Set Volume" [
                generalCommand_CommandName="SetVolume"
            ]
            Type generalCommand_withArguments : PlayTrailerButton "Play Trailer" [
                generalCommand_CommandName="PlayTrailers"
            ]
    }
}
```

---

# References

- [Emby Remote Control API Documentation](https://github.com/MediaBrowser/Emby/wiki/Remote-control)

---
