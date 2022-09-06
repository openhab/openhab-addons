# Arcam Binding

This binding add support for [Arcam](https://www.arcam.co.uk/) audio products.
These products have their own protocol on top of TCP which is what this binding uses to communicate.

## Supported Things

This binding supports the following models:

| Model               | ThingTypeUID   | Tested | 
|---------------------|----------------|--------|
| AVR5                | AVR5           | No     |
| AVR10               | AVR10          | No     |
| AVR20               | AVR20          | No     |
| AVR30               | AVR30          | No     |
| AVR40               | AVR40          | No     |
| SA10                | SA10           | No     |
| SA20                | SA20           | Yes    |
| SA30                | SA30           | No     |

Each device seems to have different options and the protocol differs between each series.
Which is why not all Arcam models are supported yet.
If you own an Arcam device you can help out by validating whether this binding works correctly with your device.
You can get in touch via the OpenHAB forum by either sending a direct message or start a post and mention me (joepadmiraal).
Also let me know if you have an Arcam device which is not in the list, I'm happy to add support for more models.

## Discovery

The Arcam devices are discovered through UPnP in the local network and all devices are put in the Inbox.

## Binding Configuration

This binding does not require any configuration.

## Thing Configuration

The most easy way of using this binding is via UPnP discovery.
This will automatically configure the `hostname` for your device.
You can manually configure this if you don't want to use the discovery mechanism.

In theory the Arcam devices support pushing data to the binding whenever the values change.
However during testing I found that this often does not happen.
In order to work around this you can enable polling by setting the polling interval.
A good starting value for this setting is 5 seconds.

### `sample` Thing Configuration

| Name             | Type    | Description                           | Default | Required |
|------------------|---------|---------------------------------------|---------|----------|
| hostname         | text    | Hostname or IP address of the device  | N/A     | yes      |
| Polling interval | integer | The amount of seconds to wait before polling the device for updated information. When set to 0 no polling is done and the binding relies on data being pushed from the device to the binding.  | 0       | yes      |

## Channels

Dependent on the model that you are using, these channels will be available.
All generic channels are placed in the masterZone.

| Channel ID                            | Item Type | Access Mode | Description                                                           | Thing types                            |
|---------------------------------------|-----------|-------------|-----------------------------------------------------------------------|----------------------------------------|
| masterZone#balance                    | Number    | W           | Get or set the balance, min/max value differs per model               | all                                    |
| masterZone#dacFilter                  | String    | W           | Get or set the DAC filter, available filters differ per model         | SA10/SA20/SA30                         |
| masterZone#dcOffset                   | Switch    | R           | Get the output DC offset status                                       | SA10/SA20/SA30                         |
| masterZone#directMode                 | Switch    | R           | Get the Analogue input direct mode of the current input               | SA20/SA30/AVR5/AVR10/AVR20/AVR30/AVR40 |
| masterZone#displaybrightness          | String    | W           | Get or set the display brightness                                     | all                                    |
| masterZone#headphones                 | Switch    | R           | Get whether headphones are connected                                  | all                                    |
| masterZone#incomingSampleRate         | String    | R           | Get the incoming audio sample rate                                    | all                                    |
| masterZone#input                      | String    | W           | Get or set the input source, available options differ per model       | all                                    |
| masterZone#inputDetect                | Switch    | R           | Get the status of the active input                                    | SA10/SA20/SA30                         |
| masterZone#lifterTemperature          | Number    | R           | Get the temperature of the lifter                                     | SA20/SA30                              |
| masterZone#mute                       | Switch    | W           | Get or set whether the device should be muted                         | all                                    |
| masterZone#nowPlayingTitle            | String    | R           | Get the title of the song that is currently playing                   | SA30                                   |
| masterZone#nowPlayingArtist           | String    | R           | Get the artist of the song that is currently playing                  | SA30                                   |
| masterZone#masterZone#nowPlayingAlbum | String    | R           | Get the ablum of the song that is currently playing                   | SA30                                   |
| masterZone#nowPlayingApplication      | String    | R           | Get the GoogleCast source application                                 | SA30                                   |
| masterZone#nowPlayingSampleRate       | String    | R           | Get the sample rate of the song that is currently playing             | SA30                                   |
| masterZone#nowPlayingAudioEncoder     | String    | R           | Get the audio encoder of the song that is currently playing           | SA30                                   |
| masterZone#outputTemperature          | Number    | R           | Get the temperature of the output stage                               | SA10/SA20/SA30                         |
| masterZone#power                      | Switch    | W           | Get or set the power status of the device or the master zone          | all                                    |
| masterZone#reboot                     | Switch    | W           | Forces a reboot of the device                                         | all                                    |
| masterZone#roomEqualisation           | String    | W           | Get or set the room equalisation preset or turn it off                | SA30/AVR5/AVR10/AVR20/AVR30/AVR40      |
| masterZone#shortCircuit               | Switch    | R           | Get the short circuit status                                          | SA20/SA30                              |
| masterZone#softwareVersion            | String    | R           | Get the software (firmware) version                                   | all                                    |
| masterZone#timeoutCounter             | Number    | R           | Get the time left (in minutes) until unit enters auto standby         | SA10/SA20/SA30                         |
| masterZone#volume                     | Number    | W           | Get or set the volume                                                 | all                                    |
| zone2#balance                         | Number    | W           | Get or set the balance, min/max value differs per model               | AVR20/AVR30/AVR40                      |
| zone2#directMode                      | Switch    | R           | Get the Analogue input direct mode of the current input               | AVR20/AVR30/AVR40                      |
| zone2#input                           | String    | W           | Get or set the input source, available options differ per model       | AVR20/AVR30/AVR40                      |
| zone2#mute                            | Switch    | W           | Get or set whether the device should be muted                         | AVR20/AVR30/AVR40                      |
| zone2#power                           | Switch    | W           | Get or set the power status of zone 2                                 | AVR20/AVR30/AVR40                      |
| zone2#roomEqualisation                | String    | W           | Get or set the room equalisation preset or turn it off                | AVR20/AVR30/AVR40                      |
| zone2#volume                          | Number    | W           | Get or set the volume                                                 | AVR20/AVR30/AVR40                      |

## Full Example

`.things` file:

```
Thing arcam:SA30:living [ hostname="192.168.0.10"]
```

`.items` file:

```
Dimmer Arcam_Volume       "Volume [%.1f %%]" <soundvolume>      {channel="arcam:SA30:living:sa30MasterZone#volume"}
Switch Arcam_Mute         "Mute"             <soundvolume_mute> {channel="arcam:SA30:living:sa30MasterZone#mute"}
String Arcam_Input        "Input"            <mediacontrol>     {channel="arcam:SA30:living:sa30MasterZone#input"}
```

`.sitemap` file:

```
Frame label="Arcam" {
    Default item=Arcam_Volume
    Default item=Arcam_Mute
    Default item=Arcam_Input
}
```

## Development 

When adding a new Arcam device you need to do the following.

- Add the device to ArcamDeviceUtil.java and ArcamBindingConstants.java
- Create an Arcam<model>.java and Arcam<model>ChannelTypeProvider.java vile
- Create an <model>.xml file in the resources directory
