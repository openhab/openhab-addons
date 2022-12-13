# Chromecast Binding

The binding integrates Google Chromecast streaming devices.
It not only acts as a typical binding, but also registers each Chromecast device as an audio sink that can be used for playback.

When a Chromecast is used as an audio sink, the Chromecast connects to the runtime to get the audio streams.
The binding sends the Chromecast URLs for getting the audio streams based on the Primary Address (Network Settings configuration) and the runtime HTTP port.
These URL defaults can be overridden with the Callback URL configuration parameter.

This can be configured on the binding level:

| Configuration Parameter | Type | Description                                                                                        |
|-------------------------|------|----------------------------------------------------------------------------------------------------|
| callbackUrl             | text | optional Callback URL - url to use for playing notification sounds, e.g. <http://192.168.0.2:8080> |

Configure a Callback URL when the Chromecast cannot connect using the Primary Address or Port, e.g. when:

- proxying HTTP (port 80/443) using Apache/NGINX to openHAB (port 8080)
- openHAB is running inside a Docker container that has its own IP Address

## Supported Things

| Things           | Description                                                                  | Thing Type |
|------------------|------------------------------------------------------------------------------|------------|
| Chromecast       | Classic HDMI video Chromecasts and Google Homes                              | chromecast |
| Chromecast Audio | The Chromecast which only does audio streaming and offers a headphone jack   | audio      |
| Audio Group      | A Chromecast audio group for multi-room audio defined via the Chromecast app | audiogroup |

## Discovery

Chromecast devices are discovered on the network using mDNS.
No authentication is required for accessing the devices on the network.
Auto-discovery is enabled by default.
To disable it, you can add the following line to `<openHAB-conf>/services/runtime.cfg`:

```shell
discovery.chromecast:background=false
```

## Thing Configuration

Chromecast devices can also be manually added.
The only configuration parameter is the `ipAddress`.
For an audio group also the port is necessary.
The auto-discovery process finds the port automatically.
With manual thing configuration the parameter `port` must be determined manually.

Example for audio group:

```java
Thing chromecast:audiogroup:bathroom  [ ipAddress="192.168.0.23", port=42139]
```

## Channels

| Channel Type ID | Item Type   | Description                                                                                                                                                                           |
|-----------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| control         | Player      | Player control; currently only supports play/pause/next and does not correctly update, if the state changes on the device itself                                                           |
| stop            | Switch      | Send `ON` to this channel: Stops the Chromecast. If this channel is `ON`, the Chromecast is stopped, otherwise it is in another state (see control channel)                           |
| volume          | Dimmer      | Control the volume, this is also updated if the volume is changed by another app                                                                                                      |
| mute            | Switch      | Mute the audio                                                                                                                                                                        |
| playuri         | String      | Can be used to tell the Chromecast to play media from a given url                                                                                                                     |
| appName         | String      | Name of currently running application                                                                                                                                                 |
| appId           | String      | ID of currently running application                                                                                                                                                   |
| idling          | Switch      | Read-only indication on whether Chromecast is on idle screen                                                                                                                          |
| statustext      | String      |                                                                                                                                                                                       |
| currentTime     | Number:Time | Current time of currently playing media                                                                                                                                               |
| duration        | Number:Time | Duration of current track (null if between tracks)                                                                                                                                    |
| metadataType    | String      | Type of metadata, this indicates what metadata may be available.  One of: GenericMediaMetadata, MovieMediaMetadata, TvShowMediaMetadata, MusicTrackMediaMetadata, PhotoMediaMetadata. |
| subtitle        | String      | (GenericMediaMetadata) Descriptive subtitle of the content                                                                                                                            |
| title           | String      | (GenericMediaMetadata) Descriptive title of the content                                                                                                                               |
| image           | Image       | (GenericMediaMetadata) Image for current media                                                                                                                                        |
| imageSrc        | String      | (GenericMediaMetadata) URL of image for current media                                                                                                                                 |
| releaseDate     | DateTime    | (GenericMediaMetadata) ISO 8601 date and time this content was released                                                                                                               |
| albumArtist     | String      | (MusicTrackMediaMetadata) Name of the artist associated with the album featuring this track                                                                                           |
| albumName       | String      | (MusicTrackMediaMetadata) Album or collection from which this track is drawn                                                                                                          |
| artist          | String      | (MusicTrackMediaMetadata) Name of the artist associated with the media track                                                                                                          |
| composer        | String      | (MusicTrackMediaMetadata) Name of the composer associated with the media track                                                                                                        |
| discNumber      | Number      | (MusicTrackMediaMetadata) Number of the volume (for example, a disc) of the album                                                                                                     |
| trackNumber     | Number      | (MusicTrackMediaMetadata) Number of the track on the album                                                                                                                            |
| creationDate    | DateTime    | (PhotoMediaMetadata) ISO 8601 date and time this photograph was taken                                                                                                                 |
| locationName    | String      | (PhotoMediaMetadata) Verbal location where the photograph was taken; for example, "Madrid, Spain."                                                                                    |
| location        | Location    | (PhotoMediaMetadata) Geographical location of where the photograph was taken                                                                                                          |
| broadcastDate   | DateTime    | (TvShowMediaMetadata) ISO 8601 date and time this episode was released                                                                                                                |
| episodeNumber   | Number      | (TvShowMediaMetadata) Episode number (in the season) of the t.v. show                                                                                                                 |
| seasonNumber    | Number      | (TvShowMediaMetadata) Season number of the t.v. show                                                                                                                                  |
| seriesTitle     | String      | (TvShowMediaMetadata) Descriptive title of the t.v. series                                                                                                                            |
| studio          | String      | (TvShowMediaMetadata) Studio which released the content                                                                                                                               |

## Full Example

services.cfg:

```java
binding.chromecast:callbackUrl=http://192.168.30.58:8080
```

demo.things:

```java
Thing chromecast:audio:myCC "Lounge Chromecast Audio" [ipAddress="192.168.xxx.xxx", port=xxxx]
Thing chromecast:chromecast:KitchenHomeHub "Kitchen Home Hub" [ipAddress="192.168.xxx.xxx", port=8009]
```

demo.items:

```java
Dimmer Volume { channel="chromecast:audio:myCC:volume" }
Player Music { channel="chromecast:audio:myCC:control" }
```

demo.rules:

```javascript
rule "Turn on kitchen speakers when Chromecast starts playing music"
when
    Item chromecast_chromecast_38e621581281c7675a777e7b474811ed_appId changed
then
    logInfo("RULE.AUDIO", "Chromecast id changed!")

    // 36061251 Pandora
    // 2872939A Google Play Music

    if (chromecast_chromecast_38e621581281c7675a777e7b474811ed_appId.state == "36061251"
    || chromecast_chromecast_38e621581281c7675a777e7b474811ed_appId.state == "2872939A") {
        kitchen_audio_power.sendCommand(ON)
        kitchen_audio_source.sendCommand(1)
    }
end
```

demo.sitemap:

```perl
sitemap demo label="Main Menu" {
    Frame {
        Default item=Music
        Slider item=Volume icon=soundvolume
    }
}
```

```perl
sitemap chromecast label="Chromecasts" {
    Frame label="Family Room: What's Playing" {
        Image item=chromecast_chromecast_38e621581281c7675a777e7b474811ed_image
        Text item=chromecast_chromecast_38e621581281c7675a777e7b474811ed_artist label="Artist [%s]"
        Text item=chromecast_chromecast_38e621581281c7675a777e7b474811ed_title label="Title [%s]"
        Text item=chromecast_chromecast_38e621581281c7675a777e7b474811ed_albumName label="Album [%s]"
    }
}
```

## Rule Action

This binding includes rule actions for casting media.

- `playURL(String url)`
- `playURL(String url, String mimeType)`

Examples:

```java
val castActions = getActions("chromecast","chromecast:chromecast:29fcf535da")
val success  = castActions.playURL("http://192.168.1.160:81/mjpg/front1/video.mjpg")
val success2 = castActions.playURL("http://192.168.1.160:81/mjpg/front1/video.mjpg", "image/jpeg")
```
