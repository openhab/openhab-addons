# PLEX Binding

This binding can read information from multiple PLEX players connected to a PLEX server.

It can be used for multiple scenarios:

- Drive light changes based on player status. For instances turn off the lights when movie starts playing and turn them back on when movie is stopped/paused
- Create a page that displays currently played media of one or more player connected to the server.
- Send social media messages when player plays new media
- Inform what the end time of the currently played media is

The binding can also control `PLAY/PAUSE/NEXT/PREVIOUS` the players which can be used for:

- Start playing some music when someone enters a room
- Pause the movie when motion is detected

## Supported Things

This binding supports 2 things.

- `server`: The PLEX server will act as a bridge to read out the information from all connected players
- `player`: A PLEX client of any type / os connected to the server.

## Discovery

For the auto discovery to work correctly you first need to configure and add the `PLEX Server` Thing.
Next step is to _PLAY_ something on the desired player. Only when media is played on the player it will show up in the auto discovery!

## Thing Configuration

The PLEX Server needs to be configured first. The hostname of the PLEX server is mandatory and the either the PLEX token (recommended) or the username/password of the PLEX server (not recommended).

Then find the PLEX token please follow the instructions from the PLEX support forum:

1. Sign in to your Plex account in Plex Web App
1. Browse to a library item and view the XML for it
1. Look in the URL and find the token as the X-Plex-Token value

### `PLEX Server` Thing Configuration

| Name        | Type    | Description                                                                                                                                                                                               | Default | Required | Advanced |
|-------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|----------|---------|
| host        | text    | PLEX host name or IP address                                                                                                                                                                              | N/A     | yes      | no      |
| portNumber  | integer | Port Number (leave blank if PLEX installed on default port)                                                                                                                                               | 32400   | no       | no      |
| refreshRate | integer | Interval in seconds at which PLEX server status is polled                                                                                                                                                 | 5       | no       | no      |
| username    | text    | If you're using Plex Home you need to supply the username and password of your Plex account here. If you don't want to enter your credentials you can also directly set your account token below instead. | N/A     | no       | no      |
| password    | text    | If you're using Plex Home you need to supply the username and password of your Plex account here. If you don't want to enter your credentials you can also directly set your account token below instead. | N/A     | no       | no      |
| token       | text    | The authentication token when username/password is left blank                                                                                                                                             | N/A     | no       | no      |

### `PLEX Player` Thing Configuration

You can add multiple PLEX players. You can choose to find the player by autodiscovery or add them manually.

#### Autodiscovery

Turn on the player you want to add and _play_ some media on it. Navigate to `/settings/things/add/plex` and start the auto discover.
The player will be found and you can add it.

#### Manual adding a player Thing

When you want to add them manually go to the following url [https://plex.tv/devices.xml] and login when needed.

It will display the following XML file.

```xml
<MediaContainer publicAddress="XXX.XXX.XXX.XXX">
    <Device name="iPhone" publicAddress="XXX.XXX.XXX.XXX" product="Plex for iOS" productVersion="8.4" platform="iOS" platformVersion="15.5" device="iPhone" model="14,5" vendor="Apple" provides="client,controller,sync-target,player,pubsub-player,provider-playback" clientIdentifier="B03466F7-BEEB-405F-A315-C7BBAA2D3FAE" version="8.4" id="547394701" token="XXX" createdAt="1633194400" lastSeenAt="1655715607" screenResolution="1170x2532" screenDensity="3">
        <SyncList itemsCompleteCount="0" totalSize="0" version="2"/>
        <Connection uri="http://192.168.1.194:32500"/>
    </Device>
    <Device name="Chrome" publicAddress="XXX.XXX.XXX.XXX" product="Plex Web" productVersion="4.83.2" platform="Chrome" platformVersion="102.0" device="Linux" model="hosted" vendor="" provides="" clientIdentifier="e29nk766fd48skpm8uuu1x9l" version="4.83.2" id="660497510" token="XXX" createdAt="1655714525" lastSeenAt="1655714526" screenResolution="1920x975,1920x1080" screenDensity=""> </Device>
    <Device name="MY PLEX SERVER" publicAddress="XXX.XXX.XXX.XXX" product="Plex Media Server" productVersion="1.27.0.5897-3940636f2" platform="Linux" platformVersion="20.04.4 LTS (Focal Fossa)" device="PC" model="x86_64" vendor="Ubuntu" provides="server" clientIdentifier="906a992fc4c5722595f36732838bcc330700b2af" version="1.27.0.5897-3940636f2" id="282416069" token="XXX" createdAt="1560609688" lastSeenAt="1655709241" screenResolution="" screenDensity="">
        <Connection uri="http://[2001:1c05:380f:5700:642:1aff:fe08:1c22]:32400"/>
        <Connection uri="http://192.168.1.2:32400"/>
    </Device>
    <Device name="BRAVIA 4K UR2" publicAddress="XXX.XXX.XXX.XXX" product="Plex for Android (TV)" productVersion="8.26.2.29389" platform="Android" platformVersion="9" device="BRAVIA 4K UR2" model="BRAVIA_UR2_4K" vendor="Sony" provides="player,pubsub-player,controller" clientIdentifier="97d2510bd3942159-com-plexapp-android" version="8.26.2.29389" id="403947762" token="XXX" createdAt="1601489892" lastSeenAt="1655701261" screenResolution="1920x1080" screenDensity="320">
        <Connection uri="http://192.168.1.19:32500"/>
    </Device>
    <Device name="SHIELD Android TV" publicAddress="XXX.XXX.XXX.XXX" product="Plex for Android (TV)" productVersion="8.26.2.29389" platform="Android" platformVersion="11" device="SHIELD Android TV" model="mdarcy" vendor="NVIDIA" provides="player,pubsub-player,controller" clientIdentifier="2c098c67afd0ca79-com-plexapp-android" version="8.26.2.29389" id="508114660" token="XXX" createdAt="1625317867" lastSeenAt="1655693424" screenResolution="1920x1080" screenDensity="320">
        <Connection uri="http://192.168.1.6:32500"/>
    </Device>
    <Device name="iPad" publicAddress="XXX.XXX.XXX.XXX" product="Plex for iOS" productVersion="8.0" platform="iOS" platformVersion="14.7.1" device="iPad" model="5,3" vendor="Apple" provides="client,controller,sync-target,player,pubsub-player,provider-playback" clientIdentifier="D9629798-4B91-4375-8844-C62400573E42" version="8.0" id="617442968" token="XXX" createdAt="1646760020" lastSeenAt="1647973844" screenResolution="2048x1536" screenDensity="2">
        <SyncList itemsCompleteCount="0" totalSize="0" version="2"/>
        <Connection uri="http://192.168.1.220:32500"/>
    </Device>
    <Device name="MacBook-Pro.local" publicAddress="XXX.XXX.XXX.XXX" product="Plex for Mac" productVersion="1.41.0.2876-e960c9ca" platform="osx" platformVersion="12.2" device="" model="standalone" vendor="" provides="client,player,pubsub-player" clientIdentifier="5ehipgz2ca60ikqnv9jrgojx" version="1.41.0.2876-e960c9ca" id="507110703" token="XXX" createdAt="1625081186" lastSeenAt="1647973509" screenResolution="1680x1050,1680x1050" screenDensity=""> </Device>
</MediaContainer>
```

Find the `Device` block of the player you want to add and fill in the `clientIdentifier` as `playerID`

| Name        | Type    | Description                                                                                | Default | Required | Advanced |
|-------------|---------|--------------------------------------------------------------------------------------------|---------|----------|---------|
| playerID    | text    | The unique identifier of the player. `clientIdentifier` from [https://plex.tv/devices.xml] | N/A     | yes      | no      |

## Channels

The PLEX Server supports the following channels:

| Channel              | Type     | Read/Write | Description                                                                                                      |
|----------------------|----------|------------|------------------------------------------------------------------------------------------------------------------|
| currentPlayers       | Number   | RO         | The number of players currently configured to watch on PLEX                                                      |
| currentPlayersActive | Number   | RO         | The number of players currently being used on PLEX                                                               |

The PLEX Player supports the following channels:

| Channel              | Type     | Read/Write | Description                                                                                                      |
|----------------------|----------|------------|------------------------------------------------------------------------------------------------------------------|
| state                | String   | RO         | The current state of the Player (BUFFERING, PLAYING, PAUSED, STOPPED)                                            |
| power                | Switch   | RO         | The power status of the player                                                                                   |
| title                | String   | RO         | The title of media that is playing                                                                               |
| type                 | String   | RO         | The current type of playing media                                                                                |
| endtime              | DateTime | RO         | Time at which the media that is playing will end                                                                 |
| progress             | Dimmer   | RO         | The current progress of playing media                                                                            |
| art                  | String   | RO         | The URL of the background art for currently playing media                                                        |
| thumb                | String   | RO         | The URL of the cover art for currently playing media                                                             |
| player               | Player   | RW         | The control channel for the player `PLAY/PAUSE/NEXT/PREVIOUS`                                                    |
| ratingKey            | String   | RO         | The unique key in the Plex library identifying the media that is playing                                         |
| parentRatingKey      | String   | RO         | The unique key in the Plex library identifying the parent (TV show season or album) of the media that is playing |
| grandparentRatingKey | String   | RO         | The unique key in the Plex library identifying the grandparent (TV show) of the media that is playing            |
| user                 | String   | RO         | The user title                                                          |

## Full Example

`.things` file:

```java
Bridge plex:server:plexrServer "Bridge Plex : Plex" [host="IP.Address.Or.Hostname", token="SadhjsajjA3AG", refreshRate=5]
{
    Thing plex:player:MyViewerName01 "My Viewer Name 01" [playerID="ClientIdentifierFromDevices.XML1"]
    Thing plex:player:MyViewerName02 "My Viewer Name 02" [playerID="ClientIdentifierFromDevices.XML2"]
}
```

`.items` file

```java
String    BridgePlexCurrent            "Current players"           {channel="plex:server:plexrServer:currentPlayers"}
String    BridgePlexCurrentActive      "Current players active"    {channel="plex:server:plexrServer:currentPlayersActive"}
Switch    PlexTVPower01                "Power"                     {channel="plex:player:MyViewerName01:power"}
String    PlexTVStatus01               "Status [%s]"               {channel="plex:player:MyViewerName01:state"}
String    PlexTVTitle01                "Title [%s]"                {channel="plex:player:MyViewerName01:title"}
String    PlexTVType01                 "Type [%s]"                 {channel="plex:player:MyViewerName01:type"}
String    PlexTVEndTime01              "End time"                  {channel="plex:player:MyViewerName01:endtime"}
Dimmer    PlexTVProgress01             "Progress [%.1f%%]"         {channel="plex:player:MyViewerName01:progress"}
String    PlexTVCover1                 "Cover"                     {channel="plex:player:MyViewerName01:thumb"}
String    ShellArt01                   "Background art"            {channel="plex:player:MyViewerName01:art"}
Switch    PlexTVPower02                "Power"                     {channel="plex:player:MyViewerName02:power"}
String    PlexTVStatus02               "Status [%s]"               {channel="plex:player:MyViewerName02:state"}
String    PlexTVTitle02                "Title [%s]"                {channel="plex:player:MyViewerName02:title"}
String    PlexTVType02                 "Type [%s]"                 {channel="plex:player:MyViewerName02:type"}
String    PlexTVEndTime02              "End time"                  {channel="plex:player:MyViewerName02:endtime"}
Dimmer    PlexTVProgress02             "Progress [%.1f%%]"         {channel="plex:player:MyViewerName02:progress"}
String    PlexTVCover2                 "Cover"                     {channel="plex:player:MyViewerName02:thumb"}
String    ShellArt02                   "Background art"            {channel="plex:player:MyViewerName02:art"}
```

`.rules` file

```java
rule "Send telegram with title for My Viewer Name 01"
when
    Item PlexTVTitle01 changed
then
    val telegramActionPlexBot = getActions("telegram","telegram:telegramBot:PlexBot")
    telegramActionPlexBot.sendTelegram("Bedroom Roku is watching %s", PlexTVTitle01.state.toString)
end

rule "Send telegram with title for My Viewer Name 02"
when
    Item PlexTVTitle02 changed
then
    val telegramActionPlexBot = getActions("telegram","telegram:telegramBot:PlexBot")
    telegramActionPlexBot.sendTelegram("Bedroom Roku is watching %s", PlexTVTitle02.state.toString)
end
```
