# UpnpControl Binding

This binding acts as a UPnP control point to control UPnP AV media servers and media renderers as defined by the [UPnP Forum](https://openconnectivity.org/developer/specifications/upnp-resources/upnp/).
It discovers UPnP media servers and renderers in the local network.
UPnP AV media servers generally allow selecting content from a content directory.
UPnP AV media renderers take care of playback of the content.

You can select a renderer to play the media served from a server.
The full content hierarchy of the media on the server can be browsed hierarchically.
Searching the media library is also supported using UPnP search syntax.

Controls are available to control the playback of the media on the renderer.
Each discovered renderer will also be registered as an openHAB audio sink.

## Supported Things

Two thing types are supported, a server thing, `upnpserver`, and a renderer thing, `upnprenderer`.

The binding has been tested with the AV Media Server and AV Media Renderer from Intel Developer Tools for UPnP Technology, available [here](https://www.meshcommander.com/upnptools).
A second test set included a [TVersity Media Server](http://tversity.com/).
It complies with part of the UPnP AV Media standard, but has not been verified to comply with the full specification.
Tests have focused on the playback of audio, but if the server and renderer support it, other media types should play as well.


## Discovery

UPnP media servers and media renderers in the network will be discovered automatically.


## Thing Configuration

Both the  `upnprenderer` and `upnpserver` thing require a configuration parameter, `udn` (Universal Device Name).
This `udn` uniquely defines the UPnP device.
It can be retrieved from the thing ID when using auto discovery.

Additionally, a `upnpserver` device has the following optional configuration parameters:

* `filter`: when true, only list content that is playable on the renderer, default is `false`.
* `sortcriteria`: Sort criteria for the titles in the selection list and when sending for playing to a renderer.
The criteria are defined in UPnP sort criteria format, examples: `+dc:title`, `-dc:creator`, `+upnp:album`.
Support for sort criteria will depend on the media server.
The default is to sort ascending on title, `+dc:title`.

The full syntax for manual configuration is:

```
Thing upnpcontrol:upnpserver:<serverId> [udn="<udn of media server>"]
Thing upnpcontrol:upnprenderer:<rendererId> [udn="<udn of media renderer>", filter=<true/false>, sortcriteria="<sort criteria string>"]
```

## Channels

The `upnpserver` has the following channels:

* `upnprenderer`: The renderer to send the media content to for playback.
The channel allows selecting from all discovered media renderers.
This list is dynamically adjusted as media renderers are being added/removed.
* `currentid`: Current ID of media container or entry ready for playback.
This channel can be used to skip to a specific container or entry in the content directory.
This is especially useful in rules.
* `browse`: Browse and serve media content.
The browsing will start at the top of the content directory tree and allows you to go down and up (represented by ..) in the tree.
The list of containers (directories) and media entries for selection in the content hierarchy is updated dynamically when selecting a container or entry.
All media in the selection list, playable on the currently selected `upnprenderer` channel, are automatically queued to the renderer as next media for playback.
* `search`: Search for media content on the server.
Search criteria are defined in UPnP search criteria format.
Examples: `dc:title contains "song"`, `dc:creator contains "SpringSteen"`, `unp:class = "object.item.audioItem"`, `upnp:album contains "Born in"`.
The search starts at the value of the `currentid` channel and searches down from there.
When no `currentid` is selected, the search starts at the top.
All media in the search result list, playable on the current selected `upnprenderer` channel, are automatically queued to the renderer as next media for playback.

The `upnprenderer` has the following channels:

| Channel Type ID | Item Type | Access Mode | Description                                        |
|-----------------|-----------|-------------|----------------------------------------------------|
| `volume`        | Dimmer    |      RW     | playback volume                                    |
| `control`       | Player    |      RW     | play, pause, next, previous control                |
| `stop`          | Switch    |      RW     | stop media playback                                |
| `title`         | String    |      R      | media title                                        |
| `album`         | String    |      R      | media album                                        |
| `albumart`      | Image     |      R      | image for media album                              |
| `creator`       | String    |      R      | media creator                                      |
| `artist`        | String    |      R      | media artist                                       |
| `publisher`     | String    |      R      | media publisher                                    |
| `genre`         | String    |      R      | media genre                                        |
| `tracknumber`   | Number    |      R      | track number of current track in album             |
| `trackduration` | Number:Time    |      R      | track duration of current track in album           |
| `trackposition` | Number:Time    |      R      | current position in track during playback or pause |

## Audio Support

All configured media renderers are registered as an audio sink.
`playSound`and `playStream`commands can be used in rules to play back audio fragments or audio streams to a renderer.

## Limitations

The current version of BasicUI does not support dynamic refreshing of the selection list in the `upnpserver` channels `renderer` and `browse`.
A refresh of the browser will be required to show the adjusted selection list.
The `upnpserver search` channel requires input of a string to trigger a search.
This cannot be done with BasicUI, but can be achieved with rules.

## Full Example

.things:

```
Thing upnpcontrol:upnpserver:mymediaserver [udn="538cf6e8-d188-4aed-8545-73a1b905466e"]
Thing upnpcontrol:upnprenderer:mymediarenderer [udn="0ec457ae-6c50-4e6e-9012-dee7bb25be2d", filter=true, sortcriteria="+dc:title"]
```

.items:

```
Group MediaServer <player>
Group MediaRenderer <player>

Dimmer Volume    "Volume [%.1f %%]" <soundvolume>      (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:volume"}
Switch Mute      "Mute"             <soundvolume_mute> (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:mute"}
Player Controls  "Controller"                          (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:control"}
Switch Stop      "Stop"                                (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:stop"}
String Title     "Now playing [%s]" <text>             (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:title"}
String Album     "Album"            <text>             (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:album"}
Image AlbumArt   "Album Art"                           (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:albumart"}
String Creator   "Creator"          <text>             (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:creator"}
String Artist    "Artist"           <text>             (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:artist"}
String Publisher "Publisher"        <text>             (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:publisher"}
String Genre     "Genre"            <text>             (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:genre"}
Number TrackNumber "Track Number"                      (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:tracknumber"}
Number:Time TrackDuration "Track Duration [%d %unit%]" (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:trackduration"}
Number:Time TrackPosition "Track Position [%d %unit%]" (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:trackposition"}

String Renderer  "Renderer [%s]"    <text>             (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:title"}
String CurrentId "Current Entry [%s]" <text>           (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:currentid"}
String Browse   "Browse"                               (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:browse"}
```

.sitemap:

```
Slider  item=Volume
Switch  item=Mute
Default item=Controls
Switch  item=Stop mappings=[ON="STOP"]
Text    item=Title     
Text    item=Album
Default item=AlbumArt
Text    item=Creator
Text    item=Artist
Text    item=Publisher
Text    item=Genre
Text    item=TrackNumber
Text    item=TrackDuration
Text    item=TrackPosition

Text    item=Renderer
Text    item=CurrentId
Text    item=Browse
```

Audio sink usage examples in rules:

```
playSound(“doorbell.mp3”)
playStream("upnpcontrol:upnprenderer:mymediarenderer", "http://icecast.vrtcdn.be/stubru_tijdloze-high.mp3”)
```
