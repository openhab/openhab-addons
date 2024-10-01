# Jellyfin Binding

This is the binding for [Jellyfin](https://jellyfin.org), the volunteer built media solution that puts you in control of your media.
This binding allows you to connect to Jellyfin clients that supports remote control, it's built on top of the official Jellyfin Kotlin SDK.
It is compatible with Jellyfin servers from version `10.8.1`, recommended is `10.8.13`.

## Discovery

To discover clients, you must first configure a server (bridge).
After that, device discovery will detect controllable clients.

## Thing Types

| ThingTypeID     | Description                           |
|-----------------|---------------------------------------|
| server (bridge) | Jellyfin server instance              |
| client          | Jellyfin controllable client instance |

## Authentication

To allow the server thing to go online, you must provide valid credentials (`userId` and `token`) for the user that the biding will use to interact with the server.
Please note that the user should be allowed on the Jellyfin server to remote control devices.

In order to assist you with this process the binding expose a simple login form you can access on `<local openHAB server url>/jellyfin/<server thing id>` (example `http://127.0.0.1:8080/jellyfin/2846b8fb60ad444f9ebd085335e3f6bf`).

## Server Thing Configuration

| Config                    | Type    | Description                                                                                  |
|---------------------------|---------|----------------------------------------------------------------------------------------------|
| hostname                  | Text    | Hostname or IP address of the server (required)                                              |
| port                      | Integer | Port of the server (required)                                                                |
| ssl                       | Boolean | Connect through https (required)                                                             |
| path                      | Text    | Base path of the server                                                                      |
| refreshSeconds            | Integer | Interval to pull devices state from the server                                               |
| clientActiveWithInSeconds | Integer | Amount of seconds allowed since the last client activity to assert it's online (0 disabled)  |
| userId                    | Text    | The user id                                                                                  |
| token                     | Text    | The user access token                                                                        |

## Channels

| channel                    | Type   | Description                                                                                                     |
|----------------------------|--------|-----------------------------------------------------------------------------------------------------------------|
| send-notification          | String | Display message in client                                                                                       |
| media-control              | Player | Control media playback                                                                                          |
| playing-item-id            | String | Id of the item currently playing (readonly)                                                                     |
| playing-item-name          | String | Name of the item currently playing (readonly)                                                                   |
| playing-item-series-name   | String | Name of the item's series currently playing, only have value when item is an episode (readonly)                 |
| playing-item-season-name   | String | Name of the item's season currently playing, only have value when item is an episode (readonly)                 |
| playing-item-season        | Number | Number of the item's season currently playing, only have value when item is an episode (readonly)               |
| playing-item-episode       | Number | Number of the episode item currently playing, only have value when item is an episode (readonly)                |
| playing-item-genders       | String | Comma-separated list genders of the item currently playing (readonly)                                           |
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

The default behavior is to search for movies, series or episodes whose name starts with the given text.
If it finds results the bind will proceed as said before.
If the result is a series, the binding will try to resume some episode.
If not, it will look for the next episode to watch and finally will fall back to the first episode.

You can prefix your search with `<type:movie>`, `<type:episode>`, `<type:series>` to limit your search to a specific type.

You can also search for a specific series episode by season and episode number by prefixing your search with `<season:1><episode:1>` with the desired values.
So `<season:3><episode:10>Something` will try to play episode `10` of season `3` of the series called `Something`.

## Known Limitations

This binding has been tested with an Android TV and web client.
The only issue that was found is that the `play-next-by-terms` and `play-last-by-terms` channels currently don't work on the Android TV client.

Before opening an issue, please test that you are able to control your device correctly from the Jellyfin web UI to determine if it is a client-side issue.

## Full Example

### Example Server (Bridge) - jellyfin_bridge.things

```java
Bridge jellyfin:server:exampleServerId "Jellyfin Server" [
    clientActiveWithInSeconds=0,
    hostname="192.168.1.177",
    port=8096,
    refreshSeconds=30,
    ssl="false"
    token=XXXXX # Optional, read bellow
    userId=XXXXX # Optional, read bellow
]
```

The `token` and `userId` can be obtained using the login form at `http://YOUROPENHABIP:PORT/jellyfin/exampleServerId`

### Example Client - jellyfin_clients.things

```java
Thing jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID> "Jellyfin Web client"     (jellyfin:server:exampleServerId)
Thing jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID> "Jellyfin Android client" (jellyfin:server:exampleServerId)
```

It is recommended to create the clients using the discovery.
To get the device ids manually, it is possible to use the Jellyfin web interface with the web inspector and look for the request that is launched when you click the cast button (`<jellyfin url>/Sessions?ControllableByUserId=XXXXXXXXXXXX`).

### Example Items - jellyfin.items

```java
String strJellyfinAndroidSendNotification      { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:send-notification " }
Player plJellyfinAndroidMediaControl           { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:media-control" }
String strJellyfinAndroidPlayingItemId         { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-id" }
String strJellyfinAndroidPlayingItemName       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-name" }
String strJellyfinAndroidPlayingItemSeriesName { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-series-name" }
String strJellyfinAndroidPlayingItemSeasonName { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-season-name" }
Number nJellyfinAndroidPlayingItemSeason       { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-season" }
Number nJellyfinAndroidPlpayingItemEpisode     { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-episode" }
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
