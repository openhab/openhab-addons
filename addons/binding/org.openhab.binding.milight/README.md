# Milight/Easybulb/Limitless Binding

This binding is for using your Milight, Easybulb or LimitlessLed bulbs and the iBox.

[![openHAB Milight](http://img.youtube.com/vi/zNe9AkQbfmc/0.jpg)](http://www.youtube.com/watch?v=zNe9AkQbfmc)

## Supported Things

The binding supports Milight/Easybulb bridges from 2014+, iBox from 2016 and iBox2 from 2017 and their respective bulbs.
The Dual White bulbs from 2015 and the new generation of Dual White bulbs is supported.
RGB/White from 2014 and the new generation RGB/White from 2016 as well as RGB/Cold,Warmwhite and iBox bulbs work.

| Bulb Type          | Milight Bridge | iBox  | iBox2 |
|--------------------|:--------------:|:-----:|:-----:|
| Dual White         |       ✓        |       |       |
| RGB/White          |       ✓        |       |       |
| 2016 Dual White    |                |   ✓   |   ✓   |
| 2016 RGB/White     |                |   ✓   |   ✓   |
| RGB/Cold,Warmwhite |                |   ✓   |   ✓   |

Please note that LD382, LD382A, LD686 RGB stripes and bulbs are supported by the
[WifiLed Binding](https://github.com/openhab/openhab2-addons/blob/master/addons/binding/org.openhab.binding.wifiled/README.md).

## Discovery

All supported bridges can be discovered by triggering a search in openHAB's Inbox.
Found bridges will show up and can easily be added as things.
Unfortunately Milight like bulbs have no back channel and can not report their presence, therefore
all possible bulbs are listed as new things after a bridge has been added.
Add the bulbs you actually configured and hide the rest of the detected things.

Your device needs to be connected to your local network (i.e. by using the WPS button connection method or the native App shipped with the device).
Read the device manual for more information about how to connect your device to your network.

## Thing Configuration

Besides adding bridges through Paper-UI, you can also add them manually in your Thing
configuration file.
iBox and iBox2 have the version 6, older milight bridges have the version 3.
The ID is the MAC address of the bridge in hexadecimal digits.

    Bridge milight:bridgeV3:ACCF23A6C0B4 [ ADDR="192.168.0.70", ID="ACCF23A6C0B4" ] {
        Thing whiteLed 0
        Thing rgbwwLed 4
        Thing rgbLed 1
    }

The Thing configuration for the bridge uses the following syntax

*   Bridge milight:bridgeV3:<mac address of bridge> &lsqb ADDR="<IP-Address of bridge>", ID="<mac>" &rsqb
* Bridge milight:bridgeV6:<mac address of bridge> &lsqb ADDR="<IP-Address of bridge>", ID="<mac>" &rsqb

The Thing configuration for the bulbs uses the following syntax:
&lsqbThing&rsqb <type of bulb> <zone>

The following bulb types are valid for configuration:

*   rgbv2Led: The very first available bulb. Not very common anymore.
*   whiteLed: The dual white bulbs (with cold/warm white) used with v3-v5 bridges.
*   rgbLed: The rgb+white bulbs (with cold/warm white) used with v3-v5 bridges. About 4080 colors (255 colors * 16 brightness steps).
*   rgbiboxLed: The iBox bridge integrated color bulb without a dedicated white channel.
*   rgbwLed: The 2016/2017 color bulb without saturation support. About 6630 (255*26) colors.
*   rgbwwLed: The 2016/2017 color bulb with saturation support. About 1.044.480 (255*64*64) different color shades. Use this also for the newer generation of the dual white bulbs.

The zone number is either 0 for meaning all bulbs of the same type or a valid zone number (1-4 with bridges up to and including version 6).
Future bridges may support more zones.

## Features

For dual white bulbs these channels are supported:

    ledbrightness       Controls the brightness of your bulbs
    ledtemperature      Changes from cold white to warm white and vice versa
    lednightmode        Dims your bulbs to a very low level to use them as a night light
    animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget.

For rgbv2Led bulbs these channels are supported:

    ledbrightness       Controls the brightness of your bulbs
    ledcolor            Changes the color and brightness of your rgb bulbs when bound to a colorpicker
                        or just the brightness if bound to a Dimmer or controls On/Off if bound to a switch.
    animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget.

For rgbLed bulbs these channels are supported:

    lednightmode        Dims your bulbs to a very low level to use them as a night light
    ledwhitemode        Disable all color (saturation is 0)
    ledbrightness       Controls the brightness of your bulbs
    ledcolor            Changes the color and brightness of your rgb bulbs when bound to a colorpicker
                        or just the brightness if bound to a Dimmer or controls On/Off if bound to a switch.
    animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget.
    animation_speed_relative  Changes the speed of your chosen animation mode

For rgbwLed/rgbwwLed bulbs these channels are supported:

    lednightmode        Dims your bulbs to a very low level to use them as a night light
    ledwhitemode        Disable all color (saturation is 0)
    ledbrightness       Controls the brightness of your bulbs
    ledsaturation       Controls the saturation of your bulbs (not for rgbwLed!)
    ledtemperature      Changes from cold white to warm white and vice versa (not for rgbwLed!)
    ledcolor            Changes the color and brightness of your rgb bulbs when bound to a colorpicker
                        or just the brightness if bound to a Dimmer or controls On/Off if bound to a switch.
    animation_mode      Changes the animation mode. Chose between animation mode 1 to 9.
    animation_mode_relative   Changes the animation mode. Use an IncreaseDecrease type of widget.
    animation_speed_relative  Changes the speed of your chosen animation mode
    ledlink             Sync bulb to this zone within 3 seconds of light bulb socket power on
    ledunlink           Clear bulb from this zone within 3 seconds of light bulb socket power on

[(See the API)](http://www.limitlessled.com/dev/).

Limitations:

*   Only the rgbww bulbs support changing their saturation, for rgbv2Led/rgbwLed the colorpicker will only set the hue and brightness and change to whitemode if the saturation is under a given threshold of 50%.

## Example

	.items

	Switch Light_Groundfloor    {channel="milight:whiteLed:ACCF23A6C0B4:0:ledbrightness"} # Switch for all white bulbs
	Dimmer Light_LivingroomB    {channel="milight:whiteLed:ACCF23A6C0B4:1:ledbrightness"} # Dimmer changing brightness for bulb in zone 1
	Dimmer Light_LivingroomC    {channel="milight:whiteLed:ACCF23A6C0B4:1:ledtemperature"} # Dimmer changing colorTemperature for bulb in zone 1
	Dimmer RGBW_LivingroomB     {channel="milight:rgbwLed:ACCF23A6C0B4:2:ledbrightness"} # Dimmer changing brightness for RGBW bulb in zone 2
	Color Light_Party           {channel="milight:rgbwLed:ACCF23A6C0B4:1:ledcolor"}# Colorpicker for rgb bulbs

	# You have to link the items to the channels of your prefered group e.g. in paperui after you've saved
	# your items file.

    # The command types nightMode and whiteMode should be configured as pushbuttons as they only support a trigger action:
    Switch Light_GroundfloorN   {channel="milight:whiteLed:ACCF23A6C0B4:0:nightMode"} # Activate the NightMode for all bulbs

	# The command types animation_mode_relative and animation_speed should be configured as pushbuttons as they only support INCREASE and DECREASE commands:

    Dimmer AnimationMode		{channel="milight:rgbLed:ACCF23A6C0B4:5:animation_mode_relative"}
    Dimmer AnimationSpeed	{channel="milight:rgbLed:ACCF23A6C0B4:5:animation_speed"}

    # Animation Mode for RGBWW bulbs is different, it allows to pick a mode directly.

    Switch AnimationModeRgbWW {channel="milight:rgbwwLed:ACCF23A6C0B4:5:animation_mode"}

	.sitemap

    Switch item=AnimationMode mappings=[DECREASE='-', INCREASE='+']
    Switch item=AnimationSpeed mappings=[DECREASE='-', INCREASE='+']
    Switch item=Light_GroundfloorN mappings=[ON='Night Mode']

## Example for Scenes

    .items

    Number Light_scene		"Scenes"
    Color  Light_scene_ColorSelect "Scene Selector"   <colorwheel> (MiLight)
    # Link this item in paperui now.

    .sitemap

    Selection item=Light_scene mappings=[0="weiß", 1="rot", 2="gelb", 3="grün", 4="dunkelgrün", 5="cyan", 6="blau", 7="magenta"]

    .rules
    # [https://en.wikipedia.org/wiki/HSL_and_HSV](https://en.wikipedia.org/wiki/HSL_and_HSV)

    rule "Light Scenes"
    when
    Item Light_scene received command
    then
    if (receivedCommand==0) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(0),new PercentType(0),new PercentType(100)))
    }
    if (receivedCommand==1) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(0),new PercentType(100),new PercentType(100)))
    }
    if (receivedCommand==2) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(60),new PercentType(100),new PercentType(100)))
    }
    if (receivedCommand==3) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(120),new PercentType(100),new PercentType(100)))
    }
    if (receivedCommand==4) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(120),new PercentType(100),new PercentType(50)))
    }
    if (receivedCommand==5) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(180),new PercentType(100),new PercentType(100)))
    }
    if (receivedCommand==6) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(240),new PercentType(100),new PercentType(100)))
    }
    if (receivedCommand==7) {
	    sendCommand(Light_scene_ColorSelect, new HSBType(new DecimalType(300),new PercentType(100),new PercentType(100)))
    }
    end
