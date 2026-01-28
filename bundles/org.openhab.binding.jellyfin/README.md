# Jellyfin Binding

This is the binding for [Jellyfin](https://jellyfin.org), the volunteer built media solution that puts you in control of your media.
This binding allows you to interact with Jellyfin clients that supports remote control.
It is compatible and tested with Jellyfin servers from version `10.10.7`.
Earlier versions use a different API and are not supported.

## Binding - Configuration

Parameters to fine-tune server discovery.
For most users the default configuration will work.

| Parameter | Description                                        | Value      |
| --------- | -------------------------------------------------- | ---------- |
| Port      | Port used to query servers on local network(s)     | 0 - 65535  |
| Timeout   | Maximum amount of time to wait for a response [ms] | 500 - 5000 |

## Discovery

Servers can be discovered using a local network broadcast.
This discovery needs to be triggered manually and is designed for local networks.
Broadcasts typically do not traverse subnets or VLANs without specific network configurations.
If your Jellyfin servers are on different subnets, auto-discovery will likely not work.
In this case you need to configure the server manually using its IP address or hostname.

Once a Jellyfin server bridge has been added, clients will be detected automatically.

## Thing Types

| ThingTypeID     | Description                           |
| --------------- | ------------------------------------- |
| server (bridge) | Jellyfin server instance              |
| client          | Jellyfin controllable client instance |

## Authentication

To allow the server thing to go online, you must provide a valid access token for the user that the binding will use to interact with the server.
Please note that the user should be allowed on the Jellyfin server to remote control devices.

In order to assist you with this process the binding exposes a simple login form you can access on `<local openHAB server url>/jellyfin/<server thing id>` (example `http://127.0.0.1:8080/jellyfin/2846b8fb60ad444f9ebd085335e3f6bf`).

## Server Thing Configuration

| Config                    | Type    | Description                                                                                 |
| ------------------------- | ------- | ------------------------------------------------------------------------------------------- |
| hostname                  | Text    | Hostname or IP address of the server (required)                                             |
| port                      | Integer | Port of the server (required)                                                               |
| ssl                       | Boolean | Connect through https (required)                                                            |
| path                      | Text    | Base path of the server                                                                     |
| refreshSeconds            | Integer | Interval to pull devices state from the server                                              |
| clientActiveWithInSeconds | Integer | Amount of seconds allowed since the last client activity to assert it's online (0 disabled) |
| token                     | Text    | The user access token                                                                       |
| useWebSocket              | Boolean | Enable WebSocket connection for real-time updates (default: true)                           |

### WebSocket Real-Time Updates

By default, the binding uses WebSocket connections to receive real-time updates from the Jellyfin server.
This provides instant notifications when media playback state changes, eliminating the need for constant polling.

**WebSocket Connection Behavior:**

- **Automatic reconnection**: If the WebSocket connection is lost, the binding automatically attempts to reconnect
- **Exponential backoff**: Reconnection attempts use increasing delays: 1s → 2s → 4s → 8s → 16s → 32s → 60s (capped)
- **Maximum retries**: After 10 failed reconnection attempts, the binding falls back to polling mode
- **Automatic fallback**: When WebSocket fails permanently, the binding seamlessly switches to periodic polling (using `refreshSeconds` interval)
- **Polling mode**: You can disable WebSocket entirely by setting `useWebSocket=false`, which uses only periodic polling

WebSocket connections require network connectivity and may not work correctly if:

- Your Jellyfin server is behind a reverse proxy that doesn't support WebSocket upgrades
- Network firewalls block WebSocket connections
- The server is temporarily unreachable

In these cases, the automatic fallback to polling ensures the binding continues to function.

## Channels

| channel                    | Type   | Description                                                                                                     |
| -------------------------- | ------ | --------------------------------------------------------------------------------------------------------------- |
| send-notification          | String | Display message in client                                                                                       |
| media-control              | Player | Control media playback: play, pause, next, previous, fast-forward, rewind (standard openHAB Player channel)     |
| media-stop                 | Switch | Stop playback: send ON to stop (complements media-control which doesn't have native stop command)               |
| media-shuffle              | Switch | Control shuffle mode (ON=random order, OFF=sequential order)                                                    |
| media-repeat               | String | Control repeat mode (off=play once, one=repeat item, all=repeat queue)                                          |
| media-quality              | Number | Set maximum streaming bitrate in Kbps (140-8000)                                                                |
| media-audio-track          | Number | Select audio track by zero-based index                                                                          |
| media-subtitle             | Number | Select subtitle stream by index (-1=disable, 0+=track selection)                                                |
| playing-item-id            | String | Id of the item currently playing (readonly)                                                                     |
| playing-item-name          | String | Name of the item currently playing (readonly)                                                                   |
| playing-item-series-name   | String | Name of the item's series currently playing, only have value when item is an episode (readonly)                 |
| playing-item-season-name   | String | Name of the item's season currently playing, only have value when item is an episode (readonly)                 |
| playing-item-season        | Number | Number of the item's season currently playing, only have value when item is an episode (readonly)               |
| playing-item-episode       | Number | Number of the episode item currently playing, only have value when item is an episode (readonly)                |
| playing-item-genders       | String | Comma-separated list of the item's genres currently playing (readonly)                                          |
| playing-item-type          | String | Type of the item currently playing (readonly)                                                                   |
| playing-item-percentage    | Dimmer | Played percentage for the item currently playing, allow seek                                                    |
| playing-item-second        | Number | Current second for the item currently playing, allow seek                                                       |
| playing-item-total-seconds | Number | Total seconds for the item currently playing (readonly)                                                         |
| play-by-terms              | String | Play media by terms, works for series, episodes and movies; terms search is explained bellow                    |
| play-next-by-terms         | String | Add to playback queue as next by terms, works for series, episodes and movies; terms search is explained bellow |
| play-last-by-terms         | String | Add to playback queue as last by terms, works for series, episodes and movies; terms search is explained bellow |
| browse-by-terms            | String | Browse media by terms, works for series, episodes and movies; terms search is explained bellow                  |
| play-by-id                 | String | Play media by id, works for series, episodes and movies; id search is explained bellow                          |
| play-next-by-id            | String | Add to playback queue as next by id, works for series, episodes and movies                                      |
| play-last-by-id            | String | Add to playback queue as last by id, works for series, episodes and movies                                      |
| browse-by-id               | String | Browse media by id, works for series, episodes and movies                                                       |

### Terms Search

The terms search has a default behavior that can be modified by sending some predefined prefixes.

The default behavior is to search for movies, series, or episodes whose name starts with the given text.
If it finds results the binding will proceed as described above.
If the result is a series, the binding will try to resume some episode.
If not, it will look for the next episode to watch and finally will fall back to the first episode.

You can prefix your search with `<type:movie>`, `<type:episode>`, `<type:series>` to limit your search to a specific type.

You can also search for a specific series episode by season and episode number by prefixing your search with `<season:1><episode:1>` with the desired values.
So `<season:3><episode:10>Something` will try to play episode `10` of season `3` of the series called `Something`.

## Known Limitations

This binding has been tested with an Android TV client and the web client.
The only issue that was found is that the `play-next-by-terms` and `play-last-by-terms` channels currently don't work on the Android TV client.

Before opening an issue, please test that you are able to control your device correctly from the Jellyfin web UI to determine if it is a client-side issue.

## Troubleshooting

### WebSocket Connection Issues

If you experience issues with real-time updates or see repeated connection attempts in the logs:

**Check the logs** for WebSocket-related messages:

```text
INFO: WebSocket connection established
WARN: WebSocket connection failed, attempt X/10, retrying in Ys
INFO: WebSocket max retries exceeded, falling back to polling
```

**Common solutions:**

1. **Reverse proxy configuration**: If your Jellyfin server is behind a reverse proxy (nginx, Apache, Caddy), ensure WebSocket upgrades are properly configured:
   - nginx: Add `proxy_set_header Upgrade $http_upgrade;` and `proxy_set_header Connection "upgrade";`
   - Apache: Enable `mod_proxy_wstunnel`
   - Caddy: WebSocket support is enabled by default

2. **Firewall rules**: Ensure WebSocket connections (typically on the same port as HTTP/HTTPS) are not blocked

3. **Server compatibility**: Verify your Jellyfin server version is 10.10.7 or newer

4. **Disable WebSocket**: If WebSocket connections continue to fail, you can disable them and use polling only:

   ```java
   Bridge jellyfin:server:exampleServerId "Jellyfin Server" [
       hostname="192.168.1.177",
       port=8096,
       ssl=false,
       useWebSocket=false,  // Disable WebSocket, use polling only
       refreshSeconds=5      // Increase polling frequency if needed
   ]
   ```

### Delayed Updates

If you notice delayed state updates:

- **Check `refreshSeconds`**: Lower values mean more frequent polling (minimum recommended: 5 seconds)
- **Verify WebSocket status**: Check logs to confirm WebSocket is connected (not in fallback mode)
- **Network latency**: High network latency between openHAB and Jellyfin may cause delays

## Full Example

### Example Server (Bridge) - jellyfin_bridge.things

```java
Bridge jellyfin:server:exampleServerId "Jellyfin Server" [
    clientActiveWithInSeconds=0,
    hostname="192.168.1.177",
    port=8096,
    refreshSeconds=30,
    ssl="false"
    token=XXXXX # Optional, read below
    userId=XXXXX # Optional, read below
]
```

The `token` and `userId` can be obtained using the login form at `http://YOUROPENHABIP:PORT/jellyfin/exampleServerId`

### Example Client - jellyfin_clients.things

```java
Thing jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID> "Jellyfin Web client"     (jellyfin:server:exampleServerId)
Thing jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID> "Jellyfin Android client" (jellyfin:server:exampleServerId)
```

It is recommended to create clients using discovery.
To get the device ids manually, it is possible to use the Jellyfin web interface with the web inspector and look for the request that is launched when you click the cast button (`<jellyfin url>/Sessions?ControllableByUserId=XXXXXXXXXXXX`).

### Example Items - jellyfin.items

```java
String strJellyfinAndroidSendNotification      { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:send-notification" }
Player plJellyfinAndroidMediaControl           { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:media-control" }
String strJellyfinAndroidPlayingItemId         { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-id" }
String strJellyfinAndroidPlayingItemName       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-name" }
String strJellyfinAndroidPlayingItemSeriesName { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-series-name" }
String strJellyfinAndroidPlayingItemSeasonName { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-season-name" }
Number nJellyfinAndroidPlayingItemSeason       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-season" }
Number nJellyfinAndroidPlayingItemEpisode      { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-episode" }
String strJellyfinAndroidPlayingItemGenders    { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-genders" }
String strJellyfinAndroidPlayingItemType       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-type" }
Dimmer dJellyfinAndroidPlayingItemPercentage   { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-percentage" }
Number nJellyfinAndroidPlayingItemSecond       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-second" }
Number nJellyfinAndroidPlayingItemTotalSeconds { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-total-seconds" }
String strJellyfinAndroidPlayByTerms           { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:play-by-terms" }
String strJellyfinAndroidPlayByNextTerms       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:play-next-by-terms" }
String strJellyfinAndroidPlayByLastTerms       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:play-last-by-terms" }
String strJellyfinAndroidBrowseByTerms         { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:browse-by-terms" }
```
