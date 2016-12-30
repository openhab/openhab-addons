# AllPlay Binding

This binding integrates devices compatible with [Qualcomm AllPlay](https://www.qualcomm.com/products/allplay).
The binding uses native libraries for the AllJoyn framework. Libraries for the following platforms are already included in the binding:

* Linux ARM
* Linux x86 (32 bit)
* Linux x86-64 (64 bit)
* Windows x86 (32 bit)
* Windows x86-64 (64 bit)

If there is need for another architecture/platform, please open a [ticket on GitHub](https://github.com/openhab/openhab/issues) so the missing native library can be added.

## Supported Things

All AllPlay compatible speakers are supported by this binding. This includes for example the [Panasonic ALL series](http://www.panasonic.com/uk/consumer/home-entertainment/wireless-speaker-systems.html).

## Discovery

The AllPlay devices are discovered through the AllJoyn discovery mechanism and are put in the Inbox upon discovery.

## Binding Configuration

The binding does not require any special configuration

## Thing Configuration

AllPlay Players are identified by their device ID.

In the thing file, this looks e.g. like

```
Thing allplay:speaker:mySpeaker [ deviceId="9fbe37ca-d015-47a2-b76e-8fce7bc25687"]
```

## Channels

The devices support the following channels:

| Channel Type ID | Item Type    | Description  |
|-----------------|--------------|--------------|
| control | Player | Control the speaker, play/pause/next/previous/ffward/rewind |
| coverart | Image | Image data of cover art of the current song | 
| coverarturl | String | URL of the cover art of the current song | 
| currentalbum | String | Name of the album currently playing |
| currentartist | String | Name of the artist currently playing |
| currentduration | Number | Duration in seconds of the track currently playing |
| currentgenre | String | Genre of the track currently playing |
| currenttitle | String | Title of the track currently playing |
| currenturl | String | URL of the track or radio station currently playing |
| currentuserdata | String | Custom user data (e.g. name of radio station) of the track currently playing |
| loopmode | String | Loop mode of the speaker (ONE, ALL, NONE) |
| mute | Switch | Set or get the mute state of the master volume of the speaker |
| playstate | String | State of the Speaker, e.g. PLAYING, STOPPED,.. |
| shufflemode | Switch | Toggle the shuffle mode of the speaker |
| stop | Switch | Stop the playback |
| stream | String | Play the given HTTP or file stream (file:// or http://) |
| volume | Dimmer | Get and set the volume of the speaker |
| volumecontrol | Switch | Flag if the volume control is enabled (might be disabled if speaker is not master of the zone) |
| zoneid | String | Id of the Zone the speaker belongs to |


## Full Example

demo.things:

```
Thing allplay:speaker:mySpeaker [ deviceId="9fbe37ca-d015-47a2-b76e-8fce7bc25687"]
```

demo.items:

```
String All2Stream                           {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:stream"}
Player All2Control                          {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:control"}
Dimmer All2Volume    "Volume"               {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:volume"}
String All2Title     "Title [%s]"           {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:currenttitle"}
String All2State     "State [%s]"           {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:playstate"}
String All2Artist    "Artist [%s]"          {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:currentartist"}
String All2CoverUrl  "Cover Art URL [%s]"   {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:coverarturl"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
		Frame label="All2" {
			Default item=All2Control
			Slider item=All2Volume
			Text item=All2Title	
			Text item=All2Artist
			Text item=All2State
		}
}
```

demo.rules:

```
rule "Play Online Radio stream"
when
    Item All2OnlineRadio changed to ON
then
    All2Stream.sendCommand("http://chromaradio.com:8008/listen.pls")
```
