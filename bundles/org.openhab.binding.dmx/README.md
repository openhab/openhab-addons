# DMX Binding

The DMX binding integrates DMX devices. There are different output devices supported as well as Dimmers and Chasers.

Each output device (bridges) is representing exactly one universe, each thing is bound to a bridge.
At least one bridge and one thing is needed for the binding to work properly.

## Supported Things

### Bridges

Two DMX over Ethernet devices are supported as DMX output:  ArtNet and sACN/E1.31.
The ArtNet bridge can only be operated in unicast mode (broadcast mode is not supported as the specification recommends using it if more than 40 nodes are connected, which is unlikely in the case of a smarthome).
The sACN bridge supports both, unicast and multicast.

Additionally Lib485 devices are supported via the Lib485 bridge.

### Things

The most generic thing is a dimmer.
A dimmer can contain one or more DMX channels.
It can be bound to Switch and Dimmer items.
If more than one DMX channel is defined, the item will be updated according to the state of the first DMX channel.
There are two other things similar to the dimmer thing.
One is the color thing, it can be bound to Switch, Dimmer or Color Items and is best used for RGB lamps.
The second one is the tunable white thing, it allows to control the color temperature of lamps with seperate DMX channels for cool white and warm white.

The last supported thing is a chaser.
It can contain one or more DMX channels and binds to Switch items only.
If the thing receives an ON command all running fades in all channels are either suspended (if resumeAfter is set to true) or cleared and replaced with the fades defined in this thing.
An OFF command stops the fades and either restores the previously suspended fades (if resumeAfter is set to true) or just holds the current values.
If any of the DMX channels in a chaser receives a command from another thing, the status of the chaser is updated to OFF.
Chaser things define a control channel that can be used to dynamically change the chasers fade configuration.

## Discovery

Discovery is not supported at the moment.
You have to add all bridges and things manually.

## Thing Configuration

Since the brightness perception of the human eye is not linear, all bridges support `applycurve`,  a list of channels `applycurve` that have a CIE 1931 lightness correction (cf. [Poynton, C.A.: “Gamma” and its Disguises: The Nonlinear Mappings of Intensity in Perception, CRTs, Film and Video, SMPTE Journal Dec. 1993, pp. 1099 - 1108](https://www.poynton.com/PDFs/SMPTE93_Gamma.pdf)) applied.
This list follows the format of the thing channel definition.
This is used regardless of the thing(s) that are associated to the channel.

All bridges can make use of the `refreshrate` option.
It determines at what frequency the DMX output is refreshed.
The achievable refresh rate depends on the number of channels and the output type.
A value of `0` disables the output, the default value is 30 Hz.

### ArtNet Bridge (`artnet-bridge`)

The ArtNet bridge has one mandatory configuration value: network address (`address`).
The network address defines the IP address of the receiving node, it is also allowed to use a FQDN if DNS resolution is available.
If necessary the default port 6454 can be changed by adding `:<port>` to the address.
Multiple receivers can be added, separated by a comma.

The universe (`universe`) can range from 0-32767, this value defaults to 0.

There are two more configuration values that usually don't need to be touched.
The address and port of the sender will be automatically selected by the kernel, if they need to be set to a fixed value, this can be done with `localaddress`.
The format is identical to the receiver address.
Unlike DMX512-A (E1.11), the ArtNet standard allows to suppress repeated transmissions of unchanged universes for a certain time.
This is enabled by default and will re-transmit unchanged data with a fixed refresh rate of 800ms.
If for some reason continuous transmission is needed, the `refreshmode` can be set to `always`, opposed to the default `standard`.

### Lib485 Bridge (`lib485-bridge`)

The Lib485 bridge has one mandatory configuration value: network address (`address`).
This is the host/port where lib485 is running.
This can be an IP address but it is also allowed to use a FQDN if DNS resolution is available.
If necessary the default port 9020 can be changed by adding `:<port>` to the address.
The default address is localhost.
Multiple receivers can be added, separated by a comma.

### sACN/E1.31 Bridge (`sacn-bridge`)

The sACN bridge has one mandatory configuration value: transmission mode (`mode`).
The transmission mode can be set to either `unicast` or `multicast`, where the later one is the default value.
If unicast mode is selected, it is mandatory to define the network address (`address`) of the receiving node.
This can be an IP address but it is also allowed to use a FQDN if DNS resolution is available.
If necessary the default port 5568 can be changed by adding `:<port>` to the address.
Multiple receivers can be added, separated by a comma.

The universe (`universe`) can range from 1-63999, this value defaults to 1.

There are some more configuration values that usually don't need to be touched.
The address and port of the sender will be automatically selected by the kernel, if they need to be set to a fixed value, this can be done with `localaddress`.
The format is identical to the receiver address.

Unlike DMX512-A (E1.11), the E1.31 standard allows to suppress repeated transmissions of unchanged universes for a certain time.
This is enabled by default and will re-transmit unchanged data with a fixed refresh rate of 800ms.
If for some reason continuous transmission is needed, the `refreshmode` can be set to `always`, opposed to the default `standard`.

### Chaser Thing (`chaser`)

There are two mandatory configuration values for a chaser thing: the `dmxid` and `steps`.

The `dmxid` is a list of DMX channels that are associated with this thing.
There are several possible formats: `channel,channel,channel,...` or `channel/width` or a combination of both.

The `steps` value is a list of steps that shall be run by the chaser.
The format of a single step is `fadetime:value,value2, ...:holdtime`, two or more steps are concatenated by `step1|step2|...`.
In textual configuration line-breaks, spaces and tabs are allowed for readability.
The fadetime is used for fading from the current value to the new value.
In contrast to the dimmer thing, this is an absolute value.
The hold time defines how long this step shall wait before advancing to the next step.
A value of -1 is used to hold forever.
Both times are in ms.

An optional configuration value is `resumeafter`.
It defaults to false but if set to true, the original state of the channel (including running fades) will be suspended until the chaser receives an OFF command.

### Dimmer Thing (`dimmer`)

There is one mandatory configuration value for a dimmer thing.
It is the `dmxid`, a list of DMX channels that are associated with this thing.
There are several possible formats: `channel1,channel2,channel3,...` or `channel/width` or a combination of both.

The `fadetime` option allows a smooth transition from the current to the new value.
The time unit is ms and the interval is for a fade from 0-100%.
If the current value is 25% and the new value is 75% the time needed for this change is half of `fadetime`.
`fadetime`is used for absolute values or ON/OFF commands send to the `brightness` channel.
Related is the `dimtime` option: it defines the time in ms from 0-100% if incremental dimming (`INCREASE`/`DECREASE`) is used.
For convenient use `dimtime` usually is set to a larger value than `fadetime`.
Typical values are 500-1000 ms for `fadetime` and 2000-5000 ms for `dimtime`.

Advanced options are the `turnonvalue`and the `turnoffvalue`.
They default to 255 (equals 100%) and 0 (equals 0%) respectively.
This value can be set individually for all DMX channels, the format is `value1,value2, ...` with values from 0 to 255.
If less values than DMX channels are defined, the values will be re-used from the beginning (i.e. if two values are defined, value1 will be used for channel1, channel3, ... and value2 will be used for channel2, channel4, ...).
These values will be used if the thing receives an ON or OFF command.

The `dynamicturnonvalue` can be set to `true` or `false` (default).
If enabled, thing overwrites the previous turn-on value with the current channel values.
The next `ON` command uses these values instead of the default (or configuration supplied) values.

### Color Thing (`color`)

There is one mandatory configuration value for a dimmer thing.
It is the `dmxid`, a list of DMX channels that are associated with this thing.
There are several possible formats: `channel1,channel2,channel3,...` or `channel/width` or a combination of both.
The number of channels has to be a multiple of three.

The `fadetime` option allows a smooth transition from the current to the new value.
The time unit is ms and the interval is for a fade from 0-100%.
If the current value is 25% and the new value is 75% the time needed for this change is half of `fadetime`.
`fadetime`is used for absolute values or ON/OFF commands send to the `brightness` channel.
Related is the `dimtime` option: it defines the time in ms from 0-100% if incremental dimming (`INCREASE`/`DECREASE`) is used.
For convenient use `dimtime` usually is set to a larger value than `fadetime`.
Typical values are 500-1000 ms for `fadetime` and 2000-5000 ms for `dimtime`.

Advanced options are the `turnonvalue`and the `turnoffvalue`.
They default to 255 (equals 100%) and 0 (equals 0%) respectively.
This value can be set individually for all DMX channels, the format is `value1,value2, ...` with values from 0 to 255.
If less values than DMX channels are defined, the values will be re-used from the beginning (i.e. if two values are defined, value1 will be used for channel1, channel3, ... and value2 will be used for channel2, channel4, ...).
For color things the number of values has to be a multiple of three.
These values will be used if the thing receives an ON or OFF command.

The `dynamicturnonvalue` can be set to `true` or `false` (default).
If enabled, thing overwrites the previous turn-on value with the current channel values.
The next `ON` command uses these values instead of the default (or configuration supplied) values.

### Tunable White Thing (`tunablewhite`)

There is one mandatory configuration value for a dimmer thing.
It is the `dmxid`, a list of DMX channels that are associated with this thing.
There are several possible formats: `channel1,channel2,channel3,...` or `channel/width` or a combination of both.
The number of channels has to be even. In the order "cool white, warm white".
Additionally a channel for cool and warm white brightness as well as color temperature (`0` being the coolest, `100` being the warmest) will be provided.

The `fadetime` option allows a smooth transition from the current to the new value.
The time unit is ms and the interval is for a fade from 0-100%.
If the current value is 25% and the new value is 75% the time needed for this change is half of `fadetime`.
`fadetime`is used for absolute values or ON/OFF commands send to the `brightness` channel.
Related is the `dimtime` option: it defines the time in ms from 0-100% if incremental dimming (`INCREASE`/`DECREASE`) is used.
For convenient use `dimtime` usually is set to a larger value than `fadetime`.
Typical values are 500-1000 ms for `fadetime` and 2000-5000 ms for `dimtime`.

Advanced options are the `turnonvalue`and the `turnoffvalue`.
They default to 255 (equals 100%) and 0 (equals 0%) respectively.
This value can be set individually for all DMX channels, the format is `value1,value2, ...` with values from 0 to 255.
If less values than DMX channels are defined, the values will be re-used from the beginning (i.e. if two values are defined, value1 will be used for channel1, channel3, ... and value2 will be used for channel2, channel4, ...).
For tunable white things the number of values has to be a multiple of two.
These values will be used if the thing receives an ON or OFF command.

The `dynamicturnonvalue` can be set to `true` or `false` (default).
If enabled, thing overwrites the previous turn-on value with the current channel values.
The next `ON` command uses these values instead of the default (or configuration supplied) values.

## Channels

| Type-ID           | Thing                | Item                  | Description                                       |
| ----------------- | -------------------- | --------------------- | ------------------------------------------------- |
| brightness        | dimmer, tunablewhite | Switch, Dimmer        | controls the brightness                           |
| color             | color                | Switch, Dimmer, Color | allows to set the color and brightness            |
| color_temperature | tunablewhite         | Number                | allows to set the color temperature               |
| brightness_r      | color                | Switch, Dimmer        | controls the brightness of the red channel        |
| brightness_g      | color                | Switch, Dimmer        | controls the brightness of the green channel      |
| brightness_b      | color                | Switch, Dimmer        | controls the brightness of the blue channel       |
| brightness_cw     | tunablewhite         | Switch, Dimmer        | controls the brightness of the cool white channel |
| brightness_ww     | tunablewhite         | Switch, Dimmer        | controls the brightness of the warm white channel |
| control           | chaser               | String                | allows to change the chaser steps                 |
| switch            | chaser               | Switch                | turns the chaser ON or OFF                        |
| mute              | (all bridges)        | Switch                | mutes the DMX output of the bridge                |

_Note:_ the string send to the control channel of chaser things has to be formatted like the `steps` configuration of the chaser thing.
If the new string is invalid, the old configuration will be used.

## Rule Actions

This binding includes a rule action, which allows to immediately change DMX channels from within rules.
There is a separate instance for each bridge, which can be retrieved e.g. through

:::: tabs

::: tab DSL

```java
val dmxActions = getActions("dmx","dmx:sacn-bridge:mydmxbridge")
```

where the first parameter always has to be `dmx` and the second is the full Thing UID of the bridge that should be used.
Once this action instance is retrieved, you can invoke the `sendFade(String channels, String fade, Boolean resumeAfter)` method on it:

```java
dmxActions.sendFade("1:41/3","10000:255,255,255:-1", false)
```

The parameters are the same as in a chaser thing configuration.
Defining more than one step in `fadeString` is supported.

:::

::: tab JavaScript

The first parameter always has to be `dmx` and the second is the full Thing UID of the bridge that should be used.

```javascript
actions.get("dmx","dmx:sacn-bridge:mydmxbridge").sendFade("1:41/3", "10000:255,255,255:-1", false);
```

The sendFade parameters are the same as in a chaser thing configuration.
Defining more than one step in `fadeString` is supported.

:::

::: tab JRuby

```ruby
things["dmx:sacn-bridge:mydmxbridge"].send_fade("1:41/3", "10000:255,255,255:-1", false)
```

The `send_fade` parameters are the same as in a chaser thing configuration.
Defining more than one step in `fadeString` is supported.

:::

::::

## Full Example

This example defines a sACN/E1.31 bridge in unicast mode which transmits universe 2 and three things: a three channel dimmer used to control a RGB light, which takes 1s to fade from one color to another and 10s from 0-100% on incremental dim commands, a single channel dimmer which will turn on only to 90% if it receives an ON command and does not fully switch off (to 10%) if it receives an OFF command and chaser which changes the colors like a traffic light.

### `demo.things` Example

```java
Bridge dmx:sacn-bridge:mybridge [ mode="unicast", address="192.168.0.60", universe=2 ] {
 color  rgb    [dmxid="5/3", fadetime=1000, dimtime=10000 ]
 dimmer single [dmxid="50", fadetime=1000, turnonvalue="230", turnoffvalue="25" ]
 chaser ampel  [dmxid="10,12,13", steps="100:255,0,0:1000|100:255,255,0:500|100:0,0,255:1000|100:0,255,0:500" ]
}
```

### `demo.items` Example

```java
Color MyColorItem "My Color Item" { channel="dmx:color:mybridge:rgb:color" }
Dimmer MyDimmerItem "My Dimmer Item" { channel="dmx:dimmer:mybridge:single:brightness" }
Switch MyChaserItem "My Chaser Item" { channel="dmx:chaser:mybridge:ampel:switch" }
```

### `demo.sitemap` Example

```perl
sitemap demo label="Main Menu"
{
    Frame {
        // Color
        Colorpicker item=MyColorItem

        // Dimmer
        Switch item=MyDimmerItem
        Slider item=MyDimmerItem

        // Chaser
        Switch item=MyChaserItem
    }
}
```
