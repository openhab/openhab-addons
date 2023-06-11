# Milight/Easybulb/Limitless Binding

This binding is for using your Milight, Easybulb or LimitlessLed bulbs and the iBox.

[![openHAB Milight](https://img.youtube.com/vi/zNe9AkQbfmc/0.jpg)](https://www.youtube.com/watch?v=zNe9AkQbfmc)

## Supported Things

The binding supports Milight/Easybulb bridges from 2014+, iBox from 2016 and iBox2 from 2017 and their respective bulbs.
The Dual White bulbs from 2015 and the new generation of Dual White bulbs are supported.
RGB/White from 2014 and the new generation RGB/White from 2016 as well as RGB/Cold,Warmwhite and iBox bulbs work.

| Bulb Type          | Milight Bridge | iBox  | iBox2 |
|--------------------|:--------------:|:-----:|:-----:|
| Dual White         |       ✓        |       |       |
| RGB/White          |       ✓        |       |       |
| 2016 Dual White    |                |   ✓   |   ✓   |
| 2016 RGB/White     |                |   ✓   |   ✓   |
| RGB/Cold,Warmwhite |                |   ✓   |   ✓   |

Please note that LD382, LD382A, LD686 RGB stripes and bulbs are supported by the
[WifiLed Binding](https://www.openhab.org/addons/bindings/wifiled/).

## Discovery

All supported bridges can be discovered by triggering a search in openHAB's Inbox.
Found bridges will show up and can easily be added as things.
Unfortunately Milight like bulbs have no back channel and can not report their presence, therefore
bulb discovery is not possible.

Your device needs to be connected to your local network (i.e. by using the WPS button connection method or the native App shipped with the device).
Read the device manual for more information about how to connect your device to your network.

## Thing Configuration

Besides adding bridges through the UI, you can also add them manually in your Thing
configuration file.
iBox and iBox2 have the version 6, older Milight bridges have the version 3.
The ID is the MAC address of the bridge in hexadecimal digits.

```java
Bridge milight:bridgeV3:mybridge [ host="192.168.0.70", bridgeid="ACCF23A6C0B4", passwordByte1=0, passwordByte2=0, repeat=2, delayTime=75 ] {
    Thing whiteLed myWhite   [ zone="0" ]
    Thing rgbwwLed myRGB     [ zone="4" ]
    Thing rgbLed   myOldRGB  [ zone="1" ]
}
```

The Thing configuration for the bridge uses the following syntax

- Bridge milight:bridgeV3:<any name> host="<IP-Address of bridge>", bridgeid="<mac>"
- Bridge milight:bridgeV6:<any name> host="<IP-Address of bridge>", bridgeid="<mac>", passwordByte1="<0-255>", passwordByte2="<0-255>"

Optionally, the following parameters can be added

- repeat=<integer> (defaults to 1, if not defined)
  Usually the bridge receives all commands albeit UDP is used. But the actual bulbs might be slightly out of bridge radio range and it sometimes helps to send commands multiple times.
- delayTime=<integer for ms> (defaults to 100, if not defined)
  Time to wait before sending another command to the bridge. It is safe to have a wait time of 1/10s but usually sufficient to just wait 50ms. If the value is too high, commands queue up.

The Thing configuration for the bulbs uses the following syntax:
Thing <type of bulb> <any name> zone="<0-4>"

The following bulb types are valid for configuration:

- rgbv2Led:   The very first available bulb. Not very common anymore.
- whiteLed:   The dual white bulbs (with cold/warm white) used with v3-v5 bridges.
- rgbLed:     The rgb+white bulbs (with cold/warm white) used with v3-v5 bridges. About 4080 colors (255 colors x 16 brightness steps).
- rgbiboxLed: The iBox bridge integrated color bulb without a dedicated white channel.
- rgbwLed:    The 2016/2017 color bulb without saturation support. About 6630 (255x26) colors.
- rgbwwLed:   The 2016/2017 color bulb with saturation support. About 1.044.480 (255x64x64) different color shades. Use this also for the newer generation of the dual white bulbs.

The zone number is either 0 for meaning all bulbs of the same type or a valid zone number (1-4).
Future bridges may support more zones.

## Features

For dual white bulbs these channels are supported:

```text
ledbrightness             Controls the brightness of your bulbs
ledtemperature            Changes from cold white to warm white and vice versa
lednightmode              Dims your bulbs to a very low level to use them as a night light
animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget
```

For rgbv2Led bulbs these channels are supported:

```text
ledbrightness             Controls the brightness of your bulbs
ledcolor                  Changes the color and brightness of your rgb bulbs when bound to a colorpicker
                            or just the brightness if bound to a Dimmer or controls On/Off if bound to a switch.
animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget
```

For rgbLed bulbs these channels are supported:

```text
lednightmode              Dims your bulbs to a very low level to use them as a night light
ledwhitemode              Disable all color (saturation is 0)
ledbrightness             Controls the brightness of your bulbs
ledcolor                  Changes the color and brightness of your rgb bulbs when bound to a colorpicker
                            or just the brightness if bound to a Dimmer or controls On/Off if bound to a switch
animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget
animation_speed_relative  Changes the speed of your chosen animation mode
```

For rgbwLed/rgbwwLed bulbs these channels are supported:

```text
lednightmode              Dims your bulbs to a very low level to use them as a night light
ledwhitemode              Disable all color (saturation is 0)
ledbrightness             Controls the brightness of your bulbs
ledsaturation             Controls the saturation of your bulbs (not for rgbwLed!)
ledtemperature            Changes from cold white to warm white and vice versa (not for rgbwLed!)
ledcolor                  Changes the color and brightness of your rgb bulbs when bound to a colorpicker
                            or just the brightness if bound to a Dimmer or controls On/Off if bound to a switch
animation_mode            Changes the animation mode. Chose between animation mode 1 to 9
animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget
animation_speed_relative  Changes the speed of your chosen animation mode
ledlink                   Sync bulb to this zone within 3 seconds of light bulb socket power on
ledunlink                 Clear bulb from this zone within 3 seconds of light bulb socket power on
```

Limitations:

- Only the rgbww bulbs support changing their saturation, for rgbv2Led/rgbwLed the colorpicker will only set the hue and brightness and change to white mode if the saturation is under a given threshold of 50%.

## Example

.items

```java
Switch Light_Groundfloor {channel="milight:whiteLed:ACCF23A6C0B4:0:ledbrightness"}  //Switch for all white bulbs
Dimmer Light_LivingroomB {channel="milight:whiteLed:ACCF23A6C0B4:1:ledbrightness"}  //Dimmer changing brightness for bulb in zone 1
Dimmer Light_LivingroomC {channel="milight:whiteLed:ACCF23A6C0B4:1:ledtemperature"} //Dimmer changing colorTemperature for bulb in zone 1
Dimmer RGBW_LivingroomB  {channel="milight:rgbwLed:ACCF23A6C0B4:2:ledbrightness"}   //Dimmer changing brightness for RGBW bulb in zone 2
Color Light_Party           {channel="milight:rgbwLed:ACCF23A6C0B4:1:ledcolor"}        //Colorpicker for rgb bulbs

# You have to link the items to the channels of your prefered group.

//The command types nightMode and whiteMode are stateless and should be configured as pushbuttons as they only support a trigger action:
Switch Light_GroundfloorN {channel="milight:whiteLed:ACCF23A6C0B4:0:lednightmode", autoupdate="false"} //Activate the NightMode for all bulbs

//The command types animation_mode_relative and animation_speed are stateless and should be configured as pushbuttons as they only support INCREASE and DECREASE commands:

Dimmer AnimationMode  {channel="milight:rgbLed:ACCF23A6C0B4:5:animation_mode_relative", autoupdate="false"}
Dimmer AnimationSpeed  {channel="milight:rgbLed:ACCF23A6C0B4:5:animation_speed", autoupdate="false"}

//Animation Mode for RGBWW bulbs is different, it allows to pick a mode directly.

Switch AnimationModeRgbWW {channel="milight:rgbwwLed:ACCF23A6C0B4:5:animation_mode"}
```

.sitemap

```perl
Switch item=AnimationMode      mappings=[DECREASE='-', INCREASE='+']
Switch item=AnimationSpeed     mappings=[DECREASE='-', INCREASE='+']
Switch item=Light_GroundfloorN mappings=[ON='Night Mode']
```
