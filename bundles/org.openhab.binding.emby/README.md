# Emby Binding

The Emby Binding integrates EMBY  support with openHAB, allowing both controlling the player as well as retrieving player status data like the currently played movie title.

This binding supports multiple clients connected to a EMBY Media Server. This binding allows simlar integration and control in the same manner as the Plex Binding. For example with this binding, it's possible to dim your lights when a video starts playing. 

## Supported Things

_Please describe the different supported things / devices within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Discovery

The binding will autodiscover all clients which are conneted to your EMBY media server.  These clients will be added to the inbox after the first time they come online and begin playing media.  This however only works once a connection to an EMBY media server has been established by creating the bridge thing type for the EMBY media server.


## Thing Configuration

There are two types of things for this binding.  The bridge type and the device.  The bridge must be created before any device types will be generated.

The bridge should be configured as follows:

| Parameter | Description                                              |
|---------|----------------------------------------------------------|
| API Key       | This is the API key generated from EMBY used for Authorization. (Generated from your emby server at Dashboard -> Expert -> Advanced -> Security)                           |
| Web Socket Buffer Size       | Here you can define a custom size for the websocket buffer size. Default is 100000. Increasing this can descrease server timeouts in certain cases |
| ipAddress       | This is the ip address of the EMBY Server. |
| Port       | This is the port of the EMBY server. |
| Refresh Parameter       | This is the refresh interval in milliseconds that will be sent to the websocket. |

## Channels

An emby device that is automatically discovered will come with several preconfigured channels:

| Channel Type | Item Type            | Config Parameters                                  | Description                                                                                                                                                                                                                                   |
|--------------|----------------------|----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Control | Player            | None                                  | This channel will indicate the playing status of the device. It can also be used to send play/pause commands                                                                                                                                                                                                                                   |
| Stop | Switch            | None                                  | This channel will indicate whether there is media currently playing on the device.  When media is playing the channel will indicate ON.  Sending a command of OFF to the channel will send a command to stop any currently playing media.                                                                                                                                                                                                                                     |
| Title | String            | None                                  | Will show the title of the currently playing song                                                                                                                                                                                                                                   |
| Mute | Switch            | None                                  | Indicates whether the device is muted.  Sending a command of ON will send a mute command to the currently playing media.                                                                                                                                                                                                                                   |
| ShowTitle | String            | None                                  | Will show the title of the currently playing movie or TV show.                                                                                                                                                                                                                                   |
| Image URL | String            | Image Max Height
                                  Image Max Width
                                  Image Type
                                  Percent Played                                  | This will produce a URL to the currently playing media. More information about the config parameters can be found at https://github.com/MediaBrowser/Emby/wiki/Images.                                                                                                                                                                                                                                   |
| Current Time | Number:Time            | None                                  | The current play time of the playing media.                                                                                                                                                                                                                                   |
| Duration | Number:Time            | None                                  | The length of time left in the current playing media item.                                                                                                                                                                                                                                   |
| Media Type | String            | None                                  | Description                                                                                                                                                                                                                                   |
| Send Play | String            | None                                  | Description                                                                                                                                                                                                                                   |


## Full Example

*.things 
*.items
String EMBYWMCPosterImage_URL "URL: [%s]" (EMBY) {channel="emby:device:416bcb51:d96138c30b0b404cba7513ae09db7966:imageurl"}
String EMBYWMCTVStatus      "Status [%s]"   <video>  (EMBY)
Switch EMBYWMC_HTPC_Stop (EMBY,EMBY_HTPC_Stop){channel="emby:device:416bcb51:d96138c30b0b404cba7513ae09db7966:stop"}
String EMBYWMCItemPlayed "[%s]" (EMBY) {channel="emby:device:416bcb51:d96138c30b0b404cba7513ae09db7966:showtitle" }
Switch EMBYWMC_IsMute (EMBY, EMBY_IsMute){channel="emby:device:416bcb51:d96138c30b0b404cba7513ae09db7966:mute"}
Player EMBYWMC_Player (EMBY) {channel="emby:device:416bcb51:d96138c30b0b404cba7513ae09db7966:control"} 

*.sitemap
Image     item=EMBYWMCPosterImage_URL      label=""




