# Milight Binding
The openHAB2 Milight binding allows to send commands to multiple Milight bridges.

[![openHAB Milight](http://img.youtube.com/vi/zNe9AkQbfmc/0.jpg)](http://www.youtube.com/watch?v=zNe9AkQbfmc)

## Configuration

Just use the auto discovery feature or add a thing for the binding manually
by providing host and port. Initially a thing for each detected bridge will be created.
After a bridge has been added, all possible bridge supported devices will appear
as new things. Unfortunatelly milight leds are only able to receive so there is
no real auto detection for single milight leds but only for the briges possible.
Add the leds you actually configured and hide the rest of the detected things.

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

	Switch Light_Groundfloor    # Switch for all white bulbs
	Switch Light_GroundfloorN   # Activate the NightMode for all bulbs 
	Dimmer Light_LivingroomB    # Dimmer changing brightness for bulb1
	Dimmer Light_LivingroomC    # Dimmer changing colorTemperature for bulb1 
	Dimmer RGBW_LivingroomB     # Dimmer changing brightness for RGBW bulb1
	Color Light_Party           # Colorpicker for rgb bulbs 

	# You have to link the items to the channels of your prefered group e.g. in paperui after you've saved
	# your items file.
	
	# The command types discomode and discoSpeed should be configured as pushbuttons as they only support INCREASE and DECREASE commands:

    Dimmer DiscoMode		{milight="bridge1;5;discomode"}
    Dimmer DiscoSpeed		{milight="bridge1;5;discospeed"}

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
