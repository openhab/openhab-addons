# AmpliPi Binding

This binding supports the multi-room audio system [AmpliPi](http://www.amplipi.com/) from [MicroNova](http://www.micro-nova.com/).

## Supported Things

The AmpliPi itself is modeled as a Bridge of type `controller`.
Every available zone as well as group is managed as an individual Thing of type `zone` resp. `group`.

## Discovery

The AmpliPi announces itself through mDNS, so that the bindings is able to find it automatically.

As soon as the AmpliPi is online, its zones and groups are automatically retrieved and added as Things to the Inbox.

## Thing Configuration

The `controller` Bridge has two configuration parameters:

| Parameter       | Required | Description                                                                                        |
|-----------------|----------|----------------------------------------------------------------------------------------------------|
| hostname        | yes      | The hostname or IP address of the AmpliPi on the network                                           |
| refreshInterval | no       | The time to wait between two polling requests for receiving state updates. Defaults to 10 seconds. |

Both the `zone` and `group` Things only require a single configuration parameter `id`, which corresponds to their id on the AmpliPi.

## Channels

These are the channels of the `controller` Bridge:

| Channel  | Type   | Description                                                                                          |
|----------|--------|------------------------------------------------------------------------------------------------------|
| preset   | Number | Allows setting a pre-configured preset. The available options are dynamically read from the AmpliPi. |
| input1   | String | The selected input of source 1                                                                       |
| input2   | String | The selected input of source 2                                                                       |
| input3   | String | The selected input of source 3                                                                       |
| input4   | String | The selected input of source 4                                                                       |

The `zone` and `group` Things have the following channels:

| Channel  | Type   | Description                                        |
|----------|--------|----------------------------------------------------|
| power    | Switch | Whether the zone/group is active or off            |
| volume   | Dimmer | The volume of the zone/group                       |
| mute     | Switch | Mutes the zone/group                               |
| source   | Number | The source (1-4) that this zone/group is playing   |

## Audio Sink

For every AmpliPi controller, an audio sink is registered with the id of the thing.
This audio sink accepts urls and audio files to be played.
It uses the AmpliPi's PA feature for announcements on all available zones.
If no volume value is passed, the current volume of each zone is used, otherwise the provided volume is temporarily set on all zones for the announcement.

## Full Example

amplipi.things:

```java
Bridge amplipi:controller:1 "My AmpliPi" [ hostname="amplipi.local" ] {
    zone zone2 "Living Room" [ id=1 ]
}
```

amplipi.items:

```java
Number      Preset      "Preset"                { channel="amplipi:controller:1:preset" }
String      Input1      "Input 1"               { channel="amplipi:controller:1:input1" }
String      Input2      "Input 2"               { channel="amplipi:controller:1:input2" }
String      Input3      "Input 3"               { channel="amplipi:controller:1:input3" }
String      Input4      "Input 4"               { channel="amplipi:controller:1:input4" }

Switch      PowerZ2     "Power Zone2"           { channel="amplipi:zone:1:zone2:power" }
Dimmer      VolumeZ2    "Volume Zone2"          { channel="amplipi:zone:1:zone2:volume" }
Switch      MuteZ2      "Mute Zone2"            { channel="amplipi:zone:1:zone2::mute" }
Number      SourceZ2    "Source Zone2"          { channel="amplipi:zone:1:zone2::source" }
```

amplipi.sitemap:

```perl
sitemap amplipi label="Main Menu"
{
    Frame label="AmpliPi" {
        Selection item=Preset
        Selection item=Input1
        Selection item=Input2
        Selection item=Input3
        Selection item=Input4
    }
    Frame label="Living Room Zone" {
        Switch item=PowerZ2
        Slider item=VolumeZ2 label="Volume Zone 1 [%.1f %%]"
        Switch item=MuteZ2
        Selection item=SourceZ2
    }
}
```
