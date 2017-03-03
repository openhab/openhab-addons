# Milight Binding

The openHAB2 Milight binding allows to send commands to multiple Milight bridges.

[![openHAB Milight](http://img.youtube.com/vi/zNe9AkQbfmc/0.jpg)](http://www.youtube.com/watch?v=zNe9AkQbfmc)

## Supported Things

The Milight Binding supports White, and RGB(W) bulbs.

## Discovery

Version 3+ bridges can be discovered by triggering a search in openHAB's inbox. Found bridges
will show up an can easily be added as things.
After a bridge has been added, all possible bridge supported devices will appear
as new things. Unfortunatelly milight leds are only able to receive so there is
no real auto detection for single milight leds but only for the briges possible.
Add the leds you actually configured and hide the rest of the detected things.

## Binding Configuration

When manually adding an older bridge Type (3-), you have to add configuration information for
the bridge IP-Address and the listening port.

## Thing Configuration

Besides adding bridges through Paper-UI, you can also add them manually in your Thing
configuration file.

    Bridge milight:bridge:ACCF23A6C0B4 [ ADDR="192.168.0.70", PORT=8899 ] {
	    Thing whiteLed 0
	    Thing whiteLed 1
	    Thing rgbLed 5
	    Thing rgbLed 8
	    Thing rgbLed 9
	    Thing rgbLed 10
    }

The Thing configuration for the bridge uses the following syntax
Bridge milight:bridge:<mac address of bridge> [ ADDR="<IP-Address of bridge>", PORT=<listening port> ]

The Thing configuration for the bulbs uses the following syntax:
[Thing] <type of bulb> <group number>

The following bulb types are valid for configuration:
whiteLed, rgbLed

The group number corresponds to the bulbs/channels on your bridge, where 0 reflects all white bulbs,
1-4 white bulb channels and 5 all rgb bulbs.

## Features

For white bulbs these channels are supported:

    ledbrightness       controls the brightness of your bulbs
    colorTemperature    changes from cold white to warm white and vice versa
    nightMode           dimms your bulbs to a very low level to use them as a night light

For rgbw bulbs these channels are supported:

    ledbrightness       controls the brightness of your bulbs
    ledcolor            changes the color and brightness of your rgb bulbs
    discomode           changes the discoMode for rgb bulbs
    discospeed          changes the speed of your chosen discoMode

[(See the API)](http://www.limitlessled.com/dev/). 

Limitations:
The rgb bulbs do not support changing their saturation, so the colorpicker will only set the hue and brightness of it.

## Example

	.items 

	Switch Light_Groundfloor    {channel="milight:whiteLed:ACCF23A6C0B4:0:ledbrightness"} # Switch for all white bulbs
	Switch Light_GroundfloorN   {channel="milight:whiteLed:ACCF23A6C0B4:0:nightMode"} # Activate the NightMode for all bulbs 
	Dimmer Light_LivingroomB    {channel="milight:whiteLed:ACCF23A6C0B4:1:ledbrightness"} # Dimmer changing brightness for bulb1
	Dimmer Light_LivingroomC    {channel="milight:whiteLed:ACCF23A6C0B4:1:colorTemperature"} # Dimmer changing colorTemperature for bulb1 
	Dimmer RGBW_LivingroomB     {channel="milight:rgbLed:ACCF23A6C0B4:7:ledbrightness"} # Dimmer changing brightness for RGBW bulb1
	Color Light_Party           {channel="milight:rgbLed:ACCF23A6C0B4:5:rgb"}# Colorpicker for rgb bulbs 

	# You have to link the items to the channels of your prefered group e.g. in paperui after you've saved
	# your items file.
	
	# The command types discomode and discoSpeed should be configured as pushbuttons as they only support INCREASE and DECREASE commands:

    Dimmer DiscoMode		{channel="milight:rgbLed:ACCF23A6C0B4:5:discoMode"}
    Dimmer DiscoSpeed		{channel="milight:rgbLed:ACCF23A6C0B4:5:discoSpeed"}

	.sitemap

    Switch item=DiscoMode mappings=[DECREASE='-', INCREASE='+']
    Switch item=DiscoSpeed mappings=[DECREASE='-', INCREASE='+']

	# Disco Mode for RGBW bulbs can only be stepped in one direction, so please use INCREASE command only for those.


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
  
## Authors

 * David Gräff <david.graeff@tu-dortmund.de>, 2016
 * Hans-Joerg Merk
 * Kai Kreuzer
