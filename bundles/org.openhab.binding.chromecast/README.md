# Chromecast Binding

The binding integrates Google Chromecast streaming devices.
It not only acts as a typical binding, but also registers each Chromecast device as an audio sink that can be used for playback.

When a Chromecast is used as an audio sink, the Chromecast connects to openHAB to get the audio streams.
The binding sends the Chromecast URLs for getting the audio streams based on the Primary Address (Network Settings configuration) and openHAB HTTP port.
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

Auto-discovery is enabled by default, but if needed Chromecast devices can also be manually added.
In case of manually adding a device the configuration parameter `ipAddress` has to be set.
For an audio group also the port is necessary, that is available under advanced configuration.
With manual thing configuration the parameter `port` for audio group must be determined manually.

| Property    | Default | Required | Advanced | Type    | Description                                          |
|-------------|---------|----------|----------|---------|------------------------------------------------------|
| ipAddress   | -       | Yes      | No       | String  | The hostname or IP address of the Chromecast device. |
| port        | 8009    | No       | Yes      | Integer | The port where the Chromecast is listening           |
| refreshRate | 10      | No       | Yes      | Integer | The refresh (poll) interval in seconds.              |

## Channels

| Channel Type ID | Item Type   | R/W | Description                                                                                                                                                                           |
|-----------------|-------------|-----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| control         | Player      | R/W | Player control; currently only supports play/pause/next and does not correctly update, if the state changes on the device itself                                                      |
| stop            | Switch      | R/W | Send `ON` to this channel: Stops the Chromecast. If this channel is `ON`, the Chromecast is stopped, otherwise it is in another state (see control channel)                           |
| volume          | Dimmer      | R/W | Control the volume, this is also updated if the volume is changed by another app                                                                                                      |
| mute            | Switch      | R/W | Mute the audio                                                                                                                                                                        |
| playuri         | String      | R/W | Can be used to tell the Chromecast to play media from a given url                                                                                                                     |
| appName         | String      | R   | Name of currently running application                                                                                                                                                 |
| appId           | String      | R   | ID of currently running application                                                                                                                                                   |
| idling          | Switch      | R   | Indication on whether Chromecast is on idle screen                                                                                                                                    |
| statustext      | String      | R   |                                                                                                                                                                                       |
| currentTime     | Number:Time | R   | Current time of currently playing media                                                                                                                                               |
| duration        | Number:Time | R   | Duration of current track (null if between tracks)                                                                                                                                    |
| metadataType    | String      | R   | Type of metadata, this indicates what metadata may be available.  One of: GenericMediaMetadata, MovieMediaMetadata, TvShowMediaMetadata, MusicTrackMediaMetadata, PhotoMediaMetadata. |
| albumArtist     | String      | R   | (MusicTrackMediaMetadata) Name of the artist associated with the album featuring this track                                                                                           |
| albumName       | String      | R   | (MusicTrackMediaMetadata) Album or collection from which this track is drawn                                                                                                          |
| artist          | String      | R   | (MusicTrackMediaMetadata) Name of the artist associated with the media track                                                                                                          |
| broadcastDate   | DateTime    | R   | (TvShowMediaMetadata) ISO 8601 date and time this episode was released                                                                                                                |
| composer        | String      | R   | (MusicTrackMediaMetadata) Name of the composer associated with the media track                                                                                                        |
| creationDate    | DateTime    | R   | (PhotoMediaMetadata) ISO 8601 date and time this photograph was taken                                                                                                                 |
| discNumber      | Number      | R   | (MusicTrackMediaMetadata) Number of the volume (for example, a disc) of the album                                                                                                     |
| episodeNumber   | Number      | R   | (TvShowMediaMetadata) Episode number (in the season) of the t.v. show                                                                                                                 |
| image           | Image       | R   | (GenericMediaMetadata) Image for current media                                                                                                                                        |
| imageSrc        | String      | R   | (GenericMediaMetadata) URL of image for current media                                                                                                                                 |
| locationName    | String      | R   | (PhotoMediaMetadata) Verbal location where the photograph was taken; for example, "Madrid, Spain."                                                                                    |
| location        | Location    | R   | (PhotoMediaMetadata) Geographical location of where the photograph was taken                                                                                                          |
| releaseDate     | DateTime    | R   | (GenericMediaMetadata) ISO 8601 date and time this content was released                                                                                                               |
| seasonNumber    | Number      | R   | (TvShowMediaMetadata) Season number of the t.v. show                                                                                                                                  |
| seriesTitle     | String      | R   | (TvShowMediaMetadata) Descriptive title of the t.v. series                                                                                                                            |
| studio          | String      | R   | (TvShowMediaMetadata) Studio which released the content                                                                                                                               |
| subtitle        | String      | R   | (GenericMediaMetadata) Descriptive subtitle of the content                                                                                                                            |
| title           | String      | R   | (GenericMediaMetadata) Descriptive title of the content                                                                                                                               |
| trackNumber     | Number      | R   | (MusicTrackMediaMetadata) Number of the track on the album                                                                                                                            |

## Full Example

services.cfg:

```java
binding.chromecast:callbackUrl=http://192.168.30.58:8080
```

demo.things:

```java
Thing chromecast:audio:lounge_chromecast "Lounge Chromecast Audio" [ipAddress="192.168.xxx.xxx", port=8009]
Thing chromecast:chromecast:kitchen_chromecast "Kitchen Home Hub" [ipAddress="192.168.xxx.xxx", port=8009]
Thing chromecast:audiogroup:bathroom  [ ipAddress="192.168..xxx.xxx", port=42139]
```

demo.items:

```java
Dimmer kitchen_chromecast_volume    { channel="chromecast:audio:kitchen_chromecast:volume" }
Player kitchen_chromecast_control   { channel="chromecast:audio:kitchen_chromecast:control" }
String kitchen_chromecast_appName   { channel="chromecast:audio:kitchen_chromecast:appName" }
String kitchen_chromecast_artist    { channel="chromecast:audio:kitchen_chromecast:artist" }
String kitchen_chromecast_albumName { channel="chromecast:audio:kitchen_chromecast:albumName" }
String kitchen_chromecast_title     { channel="chromecast:audio:kitchen_chromecast:title" }
String kitchen_chromecast_image     { channel="chromecast:audio:kitchen_chromecast:image" }

```

demo.rules:

```java
rule "Turn on kitchen speakers when Chromecast starts playing music"
when
    Item kitchen_chromecast_appid changed
then
    logInfo("RULE.AUDIO", "Chromecast app id changed!")

    if (kitchen_chromecast_appid.kitchen_chromecast_appName.state == "Pandora" || kitchen_chromecast_appName.state == "Google Play Music") {
        kitchen_speakersystem_power.sendCommand(ON)
    }
end
```

demo.sitemap:

```perl
sitemap demo label="Main Menu" {
    Frame {
        Default item=Music
        Slider item=Volume icon=soundvolume
        Text item=kitchen_chromecast_appName
    }
}
```

```perl
sitemap chromecast label="Chromecasts" {
    Frame label="Kitchen: What's Playing" {
        Image item=kitchen_chromecast_image
        Text item=kitchen_chromecast_artist label="Artist [%s]"
        Text item=kitchen_chromecast_title label="Title [%s]"
        Text item=kitchen_chromecast_albumName label="Album [%s]"
    }
}
```

## Rule Action

This binding includes rule actions for casting media.

- `playURL(String url)`
- `playURL(String url, String mimeType)`

Examples:

```java
val castActions = getActions("chromecast","chromecast:chromecast:kitchen_chromecast")

rule "Show picture on Kitchen Chromecast on button press"
when
    Item button_item changed from Off to On
then
    val success2 = castActions.playURL("http://192.168.1.160:81/mjpg/front1/video.mjpg", "image/jpeg")
end

```
