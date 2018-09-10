# <bindingName> Binding

Control a BenQ projector over the RS232 serial port interface.

## Supported Things

This plugin supports the serial console commands of BenQ projectors. It should work with most projectors although some features may be unavailable depending on the projector model.

This plugin was developed and tested with the W1110 projector model. *Please report success with other models.*

## Thing Configuration

Each thing needs a local serial interface to communicate with the projector.
```
# Minimal projector.things definition
benqprojector:projector:living [ serialPort="/dev/ttyUSB0" ]
```  

| setting | default | meaning |
| ------- | ------- | ------- |
| serialPort | (required) | the serial port interface |
| serialSpeed | 115200 | the speed of the serial port |
| refresh | 60 | the refresh (polling) interval in seconds |

**NOTE**: The serial port speed can be changed through the projector menu. Use the same value for the binding and the projector.

**NOTE**: The communication with the projector is a simple text based command/response system. The binding will poll the projector regularly to get the current state and update channels. The 60s default is a compromise between liveness of the data and communication overhead and may require adjustment based on your setup.

## Channels

Many channels are string based as each projector has a fixed set of acceptable values.

Acceptable values can be found by
1. lower the refresh interval, e.g. to `10` seconds
1. bind the channel
1. use the remote to change the setting
1. write down the values (e.g. from `PaperUI`)

### Projector information

| channel | type | description |
| ------- | ---- | ----------- |
| model | String (read only) | the projector model (e.g. `W1110`) |
| serial | String (read only) | the projector serial |
| swversion | String (read only) | the software version |
| bqversion | String (read only) | the BenQ software version |

### Projector settings

| channel | type | description |
| ------- | ---- | ----------- |
| power | Switch | turn the projector on/off |
| blank | Switch | blank the projector screen |

**NOTE**: Turning power on/off can be blocked due to cool-down or power-up.

**NOTE**: Power off means standby. The serial console is available in standby and the binding will thus continue to work. Many values will be undefined while the power is off.

### Audio settings

| channel | type | description |
| ------- | ---- | ----------- |
| mute | Switch | mute the projector |
| volume | Dimmer | adjust the audio volume of the projector |

### Picture settings

| channel | type | description |
| ------- | ---- | ----------- |
| source | String | the input (source) for the projector (e.g. `hdmi`, `hmdi2`, `network`, ...) |
| aspect | String | the aspect ratio (e.g. `AUTO`or `16:9`) |
| colortemp | String | the color temperature of the output (e.g. `NORMAL`, `NATIVE`, `WARM`, `COLD`, ...) |
| gamma | String | the gamma value (e.g. `benq`, `1.8`, `2.2`, ...) |
| picturemode | String | the picture mode (e.g. `vivid` or `game`) |
| contrast | Dimmer | picture contrast adjustment |
| brightness | Dimmer | picture brightness adjustment |
| saturation | Dimmer | picture saturation adjustment |
| sharpness | Dimmer | picture sharpness adjustment |
| hue | Dimmer | color hue adjustment |

### Adding new channels

**Prerequisites**
1. A projector to test
2. The name of the channel on the projector (e.g. `source` is called `sour` in the serial protocol)
  * look for BenQ/RS232 manuals
  * look for other open source projects controlling the projector (e.g. [pyjector](https://github.com/JohnBrodie/pyjector "pyjector project on GitHub.com") )
3. Possible values for the channel
4. Desired thing channel name (e.g. `source` instead of `sour`) and type (usually String, Dimmer or Switch)

The following steps are usually sufficient to add a new channel:
1. Add the thing channel to projector channel mapping to`CHANNEL_PROJECTOR_MAPPING` in `BenqProjectorBindingConstants`
2. Add the channel definition to `thing-types.xml`
3. Update the README 😀
4. Test
5. Open a PR 😍

The binding knows how to transform common channel types from projector values and back. 

## Full Example

Example `projector.things`
```
benqprojector:projector:living [ serialPort="/dev/ttyUSB0" ]
```

Example `projector.items`
```
```
