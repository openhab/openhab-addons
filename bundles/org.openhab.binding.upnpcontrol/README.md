# UpnpControl Binding

This binding acts as a UPnP control point to control UPnP AV media servers and media renderers as defined by the [UPnP Forum](https://openconnectivity.org/developer/specifications/upnp-resources/upnp/).
It discovers UPnP media servers and renderers in the local network.
UPnP AV media servers generally allow selecting content from a content directory.
UPnP AV media renderers take care of playback of the content.

You can select a renderer to play the media served from a server.
The full content hierarchy of the media on the server can be browsed hierarchically.
Searching the media library is also supported using UPnP search syntax.
Playlists can be created and maintained.

Controls are available to control the playback of the media on the renderer.
Currently playing media can be stored as a favorite.
Each discovered renderer will also be registered as an openHAB audio sink.

## Supported Things

Two thing types are supported, a server thing, `upnpserver`, and a renderer thing, `upnprenderer`.

The binding has been tested with the AV Media Server and AV Media Renderer from [Intel Developer Tools](https://www.meshcommander.com/upnptools) for UPnP Technology.
A second test set included a [TVersity Media Server](http://tversity.com/).
It complies with part of the UPnP AV Media standard, but has not been verified to comply with the full specification.
Tests have focused on the playback of audio, but if the server and renderer support it, other media types should play as well.

## Binding Configuration

The binding has one configuration parameter, `path`.
This is used as the disk location for storing and retrieving playlists and favorites.
The default location is `$OPENHAB_USERDATA/upnpcontrol`.

## Discovery

UPnP media servers and media renderers in the network will be discovered automatically.

## Thing Configuration

Both the  `upnprenderer` and `upnpserver` thing require a configuration parameter, `udn` (Universal Device Name).
This `udn` uniquely defines the UPnP device.
It can be retrieved from the thing ID when using auto discovery.

Both also have `refresh` configuration parameter. This parameter defines a polling interval for polling the state of the `upnprenderer` or `upnpserver`.
The default polling interval is 60s.
0 turns off polling.

An advanced configuration parameter `responseTimeout` permits tweaking how long the `upnprenderer` and `upnpserver` will wait for GENA events from the UPnP device.
This timeout is checked when there is a dependency between an action invocation and an event with expected result.
The default is 2500ms.
It should not be changed in normal circumstances.

Additionally, a `upnpserver` device has the following optional configuration parameters:

- `filter`: when true, only list content that is playable on the renderer, default is `false`.

- `sortCriteria`: sort criteria for the titles in the selection list and when sending for playing to a renderer.

  The criteria are defined in UPnP sort criteria format, examples: `+dc:title`, `-dc:creator`, `+upnp:album`.
  Support for sort criteria will depend on the media server.
  The default is to sort ascending on title, `+dc:title`.

- `browseDown`: when browse or search results in exactly one container entry, iteratively browse down until the result contains multiple container entries or at least one media entry, default is `true`.

- `searchFromRoot`: always start search from root instead of the current id, default is `false`.

A `upnprenderer` has the following optional configuration parameters:

- `seekStep`: step in seconds when sending fast forward or rewind command on the player control, default 5s.

- `notificationVolumeAdjustment`: volume adjustment from current volume in percent (range -100 to +100) for notifications when no volume is set in `playSound` command, default 10.

- `maxNotificationDuration`: maximum duration for notifications (default 15s), no maximum duration when set to 0s.

The full syntax for manual configuration is:

```java
Thing upnpcontrol:upnpserver:<serverId> [udn="<udn of media server>", refresh=<polling interval>, filter=<true/false>, sortCriteria="<sort criteria string>", browseDown=<true/false>, searchFromRoot=<true/false>, responseTimeout=<UPnP timeout in milliseconds>]
Thing upnpcontrol:upnprenderer:<rendererId> [udn="<udn of media renderer>", refresh=<polling interval>, notificationVolumeAdjustment=<signed percent>, maxNotificationDuration=<duration in seconds>, seekStep=<step>, responseTimeout=<UPnP timeout in milliseconds>]
```

## Channels

### `upnpserver`

The `upnpserver` has the following channels (item type and access mode indicated in brackets):

- `upnprenderer` (String, RW): The renderer to receive media content for playback.

  The channel allows selecting from all discovered media renderers.
  This list is dynamically adjusted as media renderers are being added/removed.

- `currenttitle` (String, R): Current title of media container or entry ready for playback.

- `browse` (String, RW): Browse and serve media content, current ID of media container or entry ready for playback.

  The browsing will start at the top of the content directory tree and allows you to go down and up (represented by ..) in the tree.
  The list of containers (directories) and media entries for selection in the content hierarchy is updated dynamically when selecting a container or entry.

  This channel can also be used to skip to a specific container or entry in the content directory.
  Setting it to 0 will reposition to the top of the content hierarchy.

  All media in the selection list, playable on the currently selected `upnprenderer` channel, are automatically queued to the renderer as next media for playback.

  The `browseDown` configuration parameter influences the result in such a way that, for `browseDown = true`, if the result only contains exactly one container entry, the result will be the content of the container and not the container itself.

- `search` (String, W): Search for media content on the server.

  Search criteria are defined in UPnP search criteria format.
  Examples: `dc:title contains "song"`, `dc:creator contains "SpringSteen"`, `unp:class = "object.item.audioItem"`, `upnp:album contains "Born in"`.

  The search, by default, starts at the value of the `currentid` and searches down from there unless the `searchfromroot` thing configuration parameter is set to `true`.
  The result (media and containers) will be available in the `browse` command option list.
  The `currentid` channel will be put to the id of the top container where the search started.

  All media in the search result list, playable on the current selected `upnprenderer` channel, are automatically queued to the renderer as next media for playback.

  The `browseDown` configuration parameter influences the result in such a way that, for `browseDown = true`, if the result only contains exactly one container entry, the result   will be the content of the container and not the container itself.

- `playlistselect` (String, W): Select a playlist from the available playlists currently saved on disk.

  This will also update `playlist` with the selected value.

- `playlist` (String, RW): Name of existing or new playlist.

- `playlistaction` (String, W): action to perform with `playlist`.

  Possible command options are:

  - `RESTORE`: restore the playlist from `playlist`.

    If the restored playlist contains content from the current server, this content will update the `browse` command option list.
    Note that playlists can contain a mix of media entries and container references.

    All media in the result list, playable on the current selected `upnprenderer` channel, are automatically queued to the renderer as next media for playback.

  - `SAVE`: save the current `browse` command option list into `playlist`.

    If `playlist` already exists, it will be overwritten.

  - `APPEND`: append the current `browse` command option list to `playlist`.

    If `playlist` does not exist yet, a new playlist will be created.

  - `DELETE`: delete `playlist` from disk and remove from `playlistselect` command option list.

A number of convenience channels replicate the basic control channels from the `upnprenderer` thing for the currently selected renderer on the `upnprenderer` channel.
These channels are `volume`, `mute` and `control`.

### `upnprenderer`

The `upnprenderer` has the following default channels:

| Channel Type ID    | Item Type   | Access Mode | Description                                        |
|--------------------|-------------|-------------|----------------------------------------------------|
| `volume`           | Dimmer      | RW          | playback master volume                             |
| `mute`             | Switch      | RW          | playback master mute                               |
| `control`          | Player      | RW          | play, pause, next, previous, fast forward, rewind  |
| `stop`             | Switch      | W           | stop media playback                                |
| `repeat`           | Switch      | RW          | continuous play of media queue, restart at end     |
| `shuffle`          | Switch      | RW          | continuous random play of media queue              |
| `onlyplayone`      | Switch      | RW          | only play one media entry from the queue at a time |
| `uri`              | String      | RW          | URI of currently playing media                     |
| `favoriteselect`   | String      | W           | play favorite from list of saved favorites         |
| `favorite`         | String      | RW          | set name for existing of new favorite              |
| `favoriteaction`   | String      | W           | `SAVE` or `DELETE` `favorite`                      |
| `playlistselect`   | String      | W           | play playlist from list of saved playlists        |
| `title`            | String      | R           | media title                                        |
| `album`            | String      | R           | media album                                        |
| `albumart`         | Image       | R           | image for media album                              |
| `creator`          | String      | R           | media creator                                      |
| `artist`           | String      | R           | media artist                                       |
| `publisher`        | String      | R           | media publisher                                    |
| `genre`            | String      | R           | media genre                                        |
| `tracknumber`      | Number      | R           | track number of current track in album             |
| `trackduration`    | Number:Time | R           | track duration of current track in album           |
| `trackposition`    | Number:Time | RW          | current position in track during playback or pause |
| `reltrackposition` | Dimmer      | RW          | current position relative to track duration        |

A numer of `upnprenderer` audio control channels may be dynamically created depending on the specific renderer capabilities.
Examples of these are:

| Channel Type ID    | Item Type   | Access Mode | Description                                        |
|--------------------|-------------|-------------|----------------------------------------------------|
| `loudness`         | Switch      | RW          | playback master loudness                           |
| `lfvolume`         | Dimmer      | RW          | playback front left volume                         |
| `lfmute`           | Switch      | RW          | playback front left mute                           |
| `rfvolume`         | Dimmer      | RW          | playback front right volume                        |
| `rfmute`           | Switch      | RW          | playback front right mute                          |

## Audio Support

Two audio sinks are registered for each media renderer.
`playSound` and `playStream` commands can be used in rules to play back audio fragments or audio streams to a renderer.

The first audio sink has the renderer id as a name.
It is used for normal playback of a sound or stream.

The second audio sink has `-notify` appended to the renderer id for its name, and has a special behavior.
This audio sink is used to play notifications.
When setting the volume parameter in the `playSound` command, the volume of the renderer will only change for the duration of playing the notification.
The `maxNotificationDuration` configuration parameter of the renderer will limit the notification duration the value of the parameter in seconds.
Normal playing will resume after the notification has played or when the maximum notification duration has been reached, whichever happens first.
Longer sounds or streams will be cut off.

## Managing a Playback Queue

There are multiple ways to serve content to a renderer for playback.

- Directly provide a URI on the `URI` channel or through `playSound` or `playStream` actions:

  Playing will start immediately, interrupting currently playing media.
  No metadata for the media is available, therefore will be provided in the media channels for metadata (e.g. `title`, `album`, ...).

- Content served from one or multiple `upnpserver` servers:

  This is done on the `upnpserver` thing with the `upnprenderer` set the the renderer for playback.
  The media at any point in time in the `upnpserver browse` option list (result from browse, search or restoring a playlist), will be queued to the `upnprenderer` for playback.
  Playback does not start automatically if not yet playing.
  When already playing a queue, the first entry of the new queue will be playing as the next entry.
  When playing an URI or media provided through an action, playback will immediately switch to the new queue.

  The `upnprenderer` will use that queue until it is replaced by another queue from the same or another `upnpserver`.
  Note that querying the content hierarchy on the `upnpserver` will update the `upnpserver browse` option list each time, and therefore the queue on the `upnprenderer` will be updated each time as long as `upnprenderer` is selected on `upnpserver`.

- Selecting a favorite or playlist on the renderer.

  Playback of the favorite or playlist will start immediately.

When playing from a directly provided URI, at the end of the media, the renderer will try to move to the next entry in a queue previously provided by a server.
Playing will stop when no such entry is available.

Multiple renderers can be sent the same or different playback queue from the same server sequentially.
Select content on the server and select the first renderer for playback.
The content queue will be served to the renderer, a play command on the renderer will start playing the queue.
Select another renderer on the server.
The same or new (after another content selection) queue will be served to the second renderer.
Both renderers will keep on playing the full queue they received.

When serving a queue from a server, the renderer can be put in "only play one" mode by putting the `onlyplayone` channel to true.
A subsequent play command will only play one media entry from the queue while respecting `shuffle` and `repeat`.
To play the next media from the queue, a new play command will be required after the player stopped.
An example of usage could be playing a single random sound from a playlist when you are away from home and an intrusion is detected.
A script could put the player in `shuffle` and `onlyplayone` mode and serve a playlist.
Only one random sound from the playlist would be played.

### Favorites

Currently playing media can be saved as favorites on the renderer.
This is especially useful when playing streams, such as online radio, but is valid for any media.
If the currently playing media has metadata, it will be saved with the favorite.

A favorite only contains one media item.
Selecting the favorite will only play that one item.
The favorite will start playing immediately.
Playing the server queue will resume after playing the favorite.

### Playlists

Playlists provide a way to define lists of server content for playback.

A new playlist can be created on a server thing from the selection in the `upnpserver browse` selection list.
When restoring a playlist on the server, the media in the playlist from the `upnpserver` thing used for restoring, will be put in the `upnpserver browse` selection list.

The current selection of media playable on the currently selected renderer will automatically be stored as a playlist with name `current`.

A playlist can contain media from different servers.
Only the media from the current server will be visible in the server when restoring.
It is possible to append content to a playlist that already contains content from a different server.
That way, it is possible to combine multiple sources for playback.

When selecting a playlist on a renderer, the playlist will be queued for playback, replacing the current queue.
Playback will start immediately.

## Using Search

Searching content on a media server may take a lot of time, depending on the functionality and the performance of the media server.
Therefore, it may very well be that media server searches time out.

Rather than searching for individual items, it is therefore often better to search for containers or playlists.

For example:

- `upnp:class derivedfrom "object.item.audioItem.musicTrack" and dc:title contains "Fight For Your Right"` would search for all music tracks with "Fight For Your Right" in the title.
  This search is potentially slow.

- `dc:title contains "Evening" and upnp:class = "object.container.playlistContainer"` would search for all playlists with "Evening" in the name.

- `dc:title = "Donnie Darko" and upnp:class = "object.container.playlistContainer"` would search for a playlist with a specific name.

With the last example, if the `browseDown` configuration parameter is `true`, the result will not be the playlist, but the content of the playlist.
This allows immediately starting a play command without having to browse down to the first result of the list (the unique container).
This is especially useful when doing searches and starting to play in scripts, as the play command can immediately follow the search for a unique container, without a need to browse down to a media ID that is hidden in the browse option list.
For interactive use through a UI, you may opt to switch the `browseDown` configuration parameter to `false` to see all levels in the browsing hierarchy.

The `searchfromroot` configuration parameter always forces searching to start from the directory root.
This will also always reset the `browse` channel to the root.
This option is helpful if you do not want to limit search to a selected container in the directory.

## Limitations

BasicUI has a number of limitations that impact the way some of the channels can be used from it:

- BasicUI does not support dynamic refreshing of the selection list in the `upnpserver` channels `renderer`, `browse`, `playlistselect` and in the `upnprenderer` channel `favoriteselect`.
  A refresh of the browser will be required to show the adjusted selection list.

- The `upnpserver search` channel requires input of a string to trigger a search.
  The `upnpserver playlist` channel and `upnprenderer favorite` channel require input of a string to set a playlist or favorite.
  This cannot be done with BasicUI, but can be achieved with rules.

- The player control in BasicUI does not support fast forward or rewind.

None of these are limitations when using the main UI.

## Full Example

.things:

```java
Thing upnpcontrol:upnpserver:mymediaserver [udn="0ec457ae-6c50-4e6e-9012-dee7bb25be2d", refresh=120, filter=true, sortCriteria="+dc:title"]
Thing upnpcontrol:upnprenderer:mymediarenderer [udn="538cf6e8-d188-4aed-8545-73a1b905466e", refresh=600, seekStep=1]
```

.items:

```java
Group MediaServer <player>
Group MediaRenderer <player>

Dimmer Volume    "Volume [%.1f %%]" <soundvolume>      (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:volume"}
Switch Mute      "Mute"             <soundvolume_mute> (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:mute"}
Switch Loudness  "Loudness"                            (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:loudness"}
Dimmer LeftVolume "Volume [%.1f %%]" <soundvolume>     (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:lfvolume"}
Dimmer RightVolume "Volume [%.1f %%]" <soundvolume>    (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:rfvolume"}
Player Controls  "Controller"                          (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:control"}
Switch Stop      "Stop"                                (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:stop"}
Switch Repeat    "Repeat"                              (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:repeat"}
Switch Shuffle   "Shuffle"                             (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:shuffle"}
String URI       "URI"                                 (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:uri"}
String FavoriteSelect "Favorite"                       (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:favoriteselect"}
String Favorite  "Favorite"                            (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:favorite"}
String FavoriteAction "Favorite Action"                (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:favoriteaction"}
String PlaylistPlay "Playlist"                         (MediaRenderer)   {channel="upnpcontrol:upnprenderer:mymediarenderer:playlistselect"}
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
Dimmer RelTrackPosition "Relative Track Position ´[%d %%]" (MediaRenderer) {channel="upnpcontrol:upnprenderer:mymediarenderer:reltrackposition"}

String Renderer  "Renderer [%s]"    <text>             (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:title"}
String CurrentTitle "Current Entry [%s]" <text>        (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:currenttitle"}
String Browse    "Browse"                              (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:browse"}
String Search    "Search"                              (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:search"}
String PlaylistSelect "Playlist"                       (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:playlistselect"}
String Playlist  "Playlist"                            (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:playlist"}
String PlaylistAction "Playlist Action"                (MediaServer)   {channel="upnpcontrol:upnpserver:mymediaserver:playlistaction"}
```

.sitemap:

```perl
Slider    item=Volume
Switch    item=Mute
Switch    item=Loudness
Slider    item=LeftVolume
Slider    item=RightVolume
Default   item=Controls
Switch    item=Stop mappings=[ON="STOP"]
Switch    item=Repeat
Switch    item=Shuffle
Text      item=URI
Selection item=FavoriteSelect
Text      item=Favorite
Switch    item=FavoriteAction
Selection item=PlaylistPlay
Text      item=Title
Text      item=Album
Default   item=AlbumArt
Text      item=Creator
Text      item=Artist
Text      item=Publisher
Text      item=Genre
Text      item=TrackNumber
Text      item=TrackDuration
Text      item=TrackPosition
Slider    item=RelTrackPosition

Selection item=Renderer
Text      item=CurrentTitle
Selection item=Browse
Text      item=Search
Selection item=PlaylistSelect
Text      item=Playlist
Switch    item=PlaylistAction
```

Audio sink usage examples in rules:

```java
playSound(“doorbell.mp3”)
playStream("upnpcontrol:upnprenderer:mymediarenderer", "http://icecast.vrtcdn.be/stubru_tijdloze-high.mp3”)
playSound("upnpcontrol:upnprenderer:mymediarenderer-notify", "doorbell.mp3", new PercentType(80))

```
