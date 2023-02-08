# Jellyfin Binding

This is the binding for [Jellyfin](https://jellyfin.org) the volunteer-built media solution that puts you in control of your media.
Stream to any device from your own server, with no strings attached.
Your media, your server, your way.
This binding allows connect to Jellyfin clients that supports remote control, it's build on top of the official Jellyfin kotlin sdk.
Compatible with Jellyfin servers in version 10.8.x.

## Supported Things

This binding was tested against the android tv, and web clients.
The only problem that I found is that the channels play-next-by-terms and play-last-by-terms don't work currently on the android tv client.

Before open an issue please test you are able to correctly control your device from the Jellyfin web ui to identify whetter is an issue on the client itself.

## Discovery

Before you are able to discover clients you should have a bridge to the server so until one is online the discovery will only look for servers on your local network. Once one is online the discovery will detect controllable clients connected to that server.

## Thing Types

| ThingTypeID   | description             |
|----------|------------------------------|
| server (bridge) | Jellyfin server instance |
| client | Jellyfin controllable client instance |

## Authentication

To allow the server thing to go online you should provide valid credentials for the user that the biding will use to interact with the server api (userId and token configuration properties).
Please note that the user should be allowed on the Jellyfin server to remote control devices.

In order to assist you with this process the binding expose a simple login form you can access on \<local openHAB server url\>/jellyfin/\<server thing id\> for example `http://127.0.0.1:8080/jellyfin/2846b8fb60ad444f9ebd085335e3f6bf`.

## Server Thing Configuration

| Config                    | Type    | description                                                                                  |
|---------------------------|---------|----------------------------------------------------------------------------------------------|
| hostname                  | text    | Hostname or IP address of the server (required)                                              |
| port                      | integer | Port of the server (required)                                                                |
| ssl                       | boolean | Connect through https (required)                                                             |
| path                      | text    | Base path of the server                                                                      |
| refreshSeconds            | integer | Interval to pull devices state from the server                                               |
| clientActiveWithInSeconds | integer | Amount off seconds allowed since the last client activity to assert it's online (0 disabled) |
| userId                    | text    | The user id                                                                                  |
| token                     | text    | The user access token                                                                        |

## Channels

| channel                    | type   | description                                                                                                     |
|----------------------------|--------|-----------------------------------------------------------------------------------------------------------------|
| send-notification          | String | Display message in client                                                                                       |
| media-control              | Player | Control media playback                                                                                          |
| playing-item-id            | String | Id of the item currently playing (readonly)                                                                     |
| playing-item-name          | String | Name of the item currently playing (readonly)                                                                   |
| playing-item-series-name   | String | Name of the item's series currently playing, only have value when item is an episode (readonly)                 |
| playing-item-season-name   | String | Name of the item's season currently playing, only have value when item is an episode (readonly)                 |
| playing-item-season        | Number | Number of the item's season currently playing, only have value when item is an episode (readonly)               |
| playing-item-episode       | Number | Number of the episode item currently playing, only have value when item is an episode (readonly)                |
| playing-item-genders       | String | Coma separate list genders of the item currently playing (readonly)                                             |
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

### Terms search:

The terms search has a default behavior that can be modified sending some predefined prefixes.

The default behavior will look for movies, series, or episodes whose name start with the provided text, if it found results the prevalence go as said before.
If the result is a series the binding will try to resume some episode, if not it will look for the next episode to watch and finally will fall back to the first episode.

You can prefix your search with '\<type:movie\>', '\<type:episode\>', '\<type:series\>' to restrict your search to a given type.

Also, you can target a specific series episode by season and episode numbers prefixing your search with '\<season:1\>\<episode:1\>' with the desired values. So '\<season:3\>\<episode:10\>Something' will try to play the episode 10 for the season 3 of the series named 'Something'.

## Full Example

### Example Server (Bridge) - jellyfin.bridge.things

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

- token and userId could be retrieved using the login form at `http://YOUROPENHABIP:PORT/jellyfin/exampleServerId`

### Example Client - jellyfin.clients.things

```java
Thing jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID> "Jellyfin Web client" (jellyfin:server:exampleServerId)
Thing jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID> "Jellyfin Android client" (jellyfin:server:exampleServerId)
```

- I recommend creating the clients using the discovery. For getting the device ids manually I recommend to use the Jellyfin web interface with the web inspector and look for the request that is launched when you click the cast button (<jellyfin url>/Sessions?ControllableByUserId=XXXXXXXXXXXX).

### Example Items - jellyfin.items

```java
String strJellyfinAndroidSendNotification { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:send-notification " }
Player plJellyfinAndroidMediaControl { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:media-control" }
String strJellyfinAndroidPlayingItemId { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-id" }
String strJellyfinAndroidPlayingItemName { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-name" }
String strJellyfinAndroidPlayingItemSeriesName { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-series-name" }
String strJellyfinAndroidPlayingItemSeasonName { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-season-name" }
Number nJellyfinAndroidPlayingItemSeason { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-season" }
Number nJellyfinAndroidPlpayingItemEpisode { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-episode" }
String strJellyfinAndroidPlayingItemGenders { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-genders" }
String strJellyfinAndroidPlayingItemType { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-type" }
Dimmer dJellyfinAndroidPlayingItemPercentage { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-percentage" }
Number nJellyfinAndroidPlayingItemSecond { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-second" }
Number nJellyfinAndroidPlayingItemTotalSeconds { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:playing-item-total-seconds" }
String strJellyfinAndroidPlayByTerms { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:play-by-terms" }
String strJellyfinAndroidPlayByNextTerms { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:play-next-by-terms" }
String strJellyfinAndroidPlayByLastTerms { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:play-last-by-terms" }
String strJellyfinAndroidBrowseByTerms { channel="jellyfin:client:exampleServerId:<JELLYFIN_DEVICE_ID>:browse-by-terms" }
```
