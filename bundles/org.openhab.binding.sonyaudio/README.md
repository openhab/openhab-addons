# SonyAudio Binding

This binding integrates the [Sony Audio Control API](https://developer.sony.com/develop/audio-control-api/).

## Supported Things

For the moment the devices that are supported by this binding are
- STR-DN1080
- HT-CT800
- SRS-ZR5
- HT-ST5000
- HT-Z9F
- HT-ZF9
- HT-MT500

When being defined in a \*.things file, the specific thing types
STR-DN1080, HT-ST5000, HT-ZF9, HT-Z9F, HT-CT800, HT-MT500 and SRS-ZR5 should be used.

Please note that these thing types are case sensitive (you need to define them in upper case).

## Discovery

The SonyAudio devices are discovered through UPnP in the local network and all devices are put in the Inbox.

## Thing Configuration

The SonyAudio Thing requires the network address, port and path as a configuration value in order for the binding to know how to access the device.
Additionally, a refresh interval, used to poll the Sony Audio device, can be specified (in seconds).

```java
Thing sonyaudio:HT-ST5000:1 [ipAddress="192.168.123.123", port=10000, path="/sony", refresh=60]
```

## Channels

The devices support the following channels:

| Channel Type ID            | Item Type | Access Mode | Description                                                                           | Thing types                                            |
| -------------------------- | --------- | ----------- | ------------------------------------------------------------------------------------- | ------------------------------------------------------ |
| power                      | Switch    | RW          | Main power on/off                                                                     | HT-CT800, SRS-ZR5, HT-ST5000, HT-ZF9, HT-Z9F, HT-MT500 |
| input                      | String    | RW          | Set or get the input source                                                           | HT-CT800, SRS-ZR5, HT-ST5000, HT-ZF9, HT-Z9F, HT-MT500 |
| volume                     | Dimmer    | RW          | Set or get the master volume                                                          | HT-CT800, SRS-ZR5, HT-ST5000, HT-ZF9, HT-Z9F, HT-MT500 |
| mute                       | Switch    | RW          | Set or get the mute state of the master volume                                        | HT-CT800, SRS-ZR5, HT-ST5000, HT-ZF9, HT-Z9F, HT-MT500 |
| soundField                 | String    | RW          | Sound field                                                                           | HT-CT800, SRS-ZR5, HT-ST5000, HT-ZF9, HT-Z9F, HT-MT500 |
| master#power               | Switch    | RW          | Main power on/off                                                                     | STR-1080                                               |
| master#soundField          | String    | RW          | Sound field                                                                           | STR-1080                                               |
| zone1#power                | Switch    | RW          | Power for zone1 for devices supporting multizone                                      | STR-1080                                               |
| zone1#input                | String    | RW          | Set or get the input source for zone1 for devices supporting multizone                | STR-1080                                               |
| zone1#volume               | Dimmer    | RW          | Set or get the zone1 volume for devices supporting multizone                          | STR-1080                                               |
| zone1#mute                 | Switch    | RW          | Set or get the mute state for zone1 volume                                            | STR-1080                                               |
| zone2#power                | Switch    | RW          | Power for zone2 for devices supporting multizone                                      | STR-1080                                               |
| zone2#input                | String    | RW          | Set or get the input source for zone2 for devices supporting multizone                | STR-1080                                               |
| zone2#volume               | Dimmer    | RW          | Set or get the zone2 volume for devices supporting multizone                          | STR-1080                                               |
| zone2#mute                 | Switch    | RW          | Set or get the mute state for zone2 volume                                            | STR-1080                                               |
| zone3#power                | Switch    | RW          | Power for zone3 for devices supporting multizone                                      | none                                                   |
| zone3#input                | String    | RW          | Set or get the input source for zone3 for devices supporting multizone                | none                                                   |
| zone3#volume               | Dimmer    | RW          | Set or get the zone3 volume for devices supporting multizone                          | none                                                   |
| zone3#mute                 | Switch    | RW          | Set or get the mute state for zone3 volume                                            | none                                                   |
| zone4#power                | Switch    | RW          | Power for zone4 for devices supporting multizone                                      | STR-1080                                               |
| zone4#input                | String    | RW          | Set or get the input source for zone4 for devices supporting multizone                | STR-1080                                               |
| zone4#volume               | Dimmer    | RW          | Set or get the zone4 volume for devices supporting multizone                          | STR-1080                                               |
| zone4#mute                 | Switch    | RW          | Set or get the mute state for zone4 volume                                            | STR-1080                                               |
| radio#broadcastFreq        | Number    | R           | Current radio frequency                                                               | STR-1080                                               |
| radio#broadcastStation     | Number    | RW          | Set or get current preset radio station                                               | STR-1080                                               |
| radio#broadcastSeekStation | String    | W           | Seek for new broadcast station, forward search "fwdSeeking" and backward "bwdSeeking" | STR-1080                                               |
| nightMode                  | Switch    | RW          | Set or get the Night Mode state                                                       | HT-ZF9                                                 |

## Full Example

demo.things:

```java
Thing sonyaudio:HT-ST5000:living [ipAddress="192.168.123.123"]
```

demo.items:

```java
Group  SonyAudio <sonyaudio>

Dimmer Sony_Volume       "Volume [%.0f %%]"  <soundvolume>      (SonyAudio) {channel="sonyaudio:HT-ST5000:living:volume"}
Switch Sony_Mute         "Mute"              <soundvolume_mute> (SonyAudio) {channel="sonyaudio:HT-ST5000:living:mute"}
String Sony_Sound_Field  "Sound Field: [%s]" <text>             (SonyAudio) {channel="sonyaudio:HT-ST5000:living:master#soundField"}
```

demo.sitemap:

```perl
sitemap demo label="Main Menu" {
    Frame label="Sony" {
        Text label="Volume" icon="soundvolume" {
            Slider item=Sony_Volume
            Switch item=Sony_Mute
        }
        Text item=Sony_Sound_Field
    }
}
```
