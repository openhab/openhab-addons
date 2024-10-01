# AllPlay Binding

This binding integrates devices compatible with [Qualcomm AllPlay](https://www.qualcomm.com/products/allplay).
The binding uses native libraries for the AllJoyn framework.
Libraries for the following platforms are already included in the binding:

- Linux ARM
- Linux x86 (32 bit, AllJoyn v16.04a)
- Linux x86-64 (64 bit, AllJoyn v16.04a)
- Windows x86 (32 bit, AllJoyn v16.04a)
- Windows x86-64 (64 bit, AllJoyn v16.04a)

The Windows libraries have a dependency on the [Visual C++ Redistributable for Visual Studio 2015](https://www.microsoft.com/en-US/download/details.aspx?id=48145).
If you are using Windows, please make sure to install these components before using the AllPlay binding.

If there is need for another architecture/platform, please open a [ticket on GitHub](https://github.com/openhab/openhab-addons/issues) so the missing native library can be added.

## Supported Things

All AllPlay compatible speakers are supported by this binding.
This includes for example the [Panasonic ALL series](https://www.panasonic.com/uk/consumer/home-entertainment/wireless-speaker-systems.html).
All AllPlay speakers are registered as an audio sink in the framework.

## Discovery

The AllPlay devices are discovered through the AllJoyn discovery mechanism and are put in the Inbox upon discovery.

## Binding Configuration

The binding has the following configuration options, which can be set for "binding:allplay":

| Parameter                | Name                       | Description                                                          | Required |
|--------------------------|----------------------------|----------------------------------------------------------------------|----------|
| rewindSkipTimeInSec      | Rewind skip time (s)       | Seconds to jump backwards if the rewind command is executed          | yes      |
| fastForwardSkipTimeInSec | Fast forward skip time (s) | Seconds to jump forward if the fastforward command is executed       | yes      |
| callbackUrl              | Callback URL               | URL to use for playing audio streams, e.g. <http://192.168.0.2:8080> | no       |

## Thing Configuration

AllPlay Players are identified by their device ID (e.g. 9fbe37ca-d015-47a2-b76e-8fce7bc25687). Available configuration parameters are:

| Parameter Label       | Parameter ID        | Description                                                                         | Required | Default |
|-----------------------|---------------------|-------------------------------------------------------------------------------------|----------|---------|
| Device ID             | deviceId            | The device identifier identifies one certain speaker                                | true     |         |
| Device Name           | deviceName          | The device name of the speaker                                                      | false    |         |
| Volume step size      | volumeStepSize      | Step size to use if the volume is changed using the increase/decrease command       | true     | 1       |
| Zone Member Separator | zoneMemberSeparator | Separator which is used when sending multiple zone members to channel 'zonemembers' | true     | ,       |

## Channels

The devices support the following channels:

| Channel Type ID | Item Type | Description                                                                                    |
|-----------------|-----------|------------------------------------------------------------------------------------------------|
| clearzone       | Switch    | Remove the current speaker from the zone                                                       |
| control         | Player    | Control the speaker, play/pause/next/previous/ffward/rewind                                    |
| coverart        | Image     | Image data of cover art of the current song                                                    |
| coverarturl     | String    | URL of the cover art of the current song                                                       |
| currentalbum    | String    | Name of the album currently playing                                                            |
| currentartist   | String    | Name of the artist currently playing                                                           |
| currentduration | Number    | Duration in seconds of the track currently playing                                             |
| currentgenre    | String    | Genre of the track currently playing                                                           |
| currenttitle    | String    | Title of the track currently playing                                                           |
| currenturl      | String    | URL of the track or radio station currently playing                                            |
| currentuserdata | String    | Custom user data (e.g. name of radio station) of the track currently playing                   |
| input           | String    | Input of the speaker, e.g. Line-In (not supported by all speakers)                             |
| loopmode        | String    | Loop mode of the speaker (ONE, ALL, NONE)                                                      |
| mute            | Switch    | Set or get the mute state of the master volume of the speaker (not supported by all speakers)  |
| playstate       | String    | State of the Speaker, e.g. PLAYING, STOPPED,..                                                 |
| shufflemode     | Switch    | Toggle the shuffle mode of the speaker                                                         |
| stop            | Switch    | Stop the playback                                                                              |
| stream          | String    | Play the given HTTP or file stream (file:// or http://)                                        |
| volume          | Dimmer    | Get and set the volume of the speaker                                                          |
| volumecontrol   | Switch    | Flag if the volume control is enabled (might be disabled if speaker is not master of the zone) |
| zoneid          | String    | Id of the Zone the speaker belongs to                                                          |
| zonemembers     | String    | Set the zone members by providing a comma-separated list of device names.                      |
|                 |           | (This channel is currently only for setting the zone members.                                  |
|                 |           | It does not update automatically if the zone members are changed from another source)          |

## Audio Support

All AllPlay speakers are registered as an audio sink in the framework.
Audio streams are sent to the `stream` channel.

## Full Example

demo.things:

```java
Thing allplay:speaker:mySpeaker [ deviceId="9fbe37ca-d015-47a2-b76e-8fce7bc25687"]
```

demo.items:

```java
String All2Stream                           {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:stream"}
Player All2Control                          {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:control"}
Dimmer All2Volume    "Volume"               {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:volume"}
String All2Title     "Title [%s]"           {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:currenttitle"}
String All2State     "State [%s]"           {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:playstate"}
String All2Artist    "Artist [%s]"          {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:currentartist"}
String All2CoverUrl  "Cover Art URL [%s]"   {channel="allplay:speaker:9fbe37ca-d015-47a2-b76e-8fce7bc25687:coverarturl"}
```

demo.sitemap:

```perl
sitemap demo label="Main Menu" {
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

```java
rule "Play Online Radio stream"
when
    Item All2OnlineRadio changed to ON
then
    All2Stream.sendCommand("http://chromaradio.com:8008/listen.pls")
```
