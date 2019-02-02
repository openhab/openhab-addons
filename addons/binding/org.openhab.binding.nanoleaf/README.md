# Nanoleaf Binding

This binding integrates the [Nanoleaf Light Panels](https://nanoleaf.me/en/consumer-led-lighting/products/smarter-series/nanoleaf-light-panels-smarter-kit/). 

![Image](doc/Nanoleaf.jpg)

It enables you to authenticate, control, and obtain information of a Light Panels device. The binding uses the [Nanoleaf OpenAPI](http://forum.nanoleaf.me/docs/openapi), which requires firmware version [1.5.0](https://helpdesk.nanoleaf.me/hc/en-us/articles/214006129-Light-Panels-Firmware-Release-Notes) or higher.

![Image](doc/LightPanels2.jpg)

## Supported Things

The binding supports two thing types: controller and lightpanel.

The controller thing is the bridge for the individual panels connected to it. With the controller thing you can control channels which affect all panels, e.g. selecting effects or setting the brightness. 

The lightpanel things control the individual panels. You can set the color for each panel or turn it on (white) or off (black). 

## Discovery

A controller (bridge) device is discovered automatically through mDNS in the local network. Alternatively, you can also provide a things file (see below for more details). After the device is discovered and added as a thing, it needs a valid authentication token that must be obtained by pairing it with your openHAB instance. Without the token the light panels remain in status OFFLINE.

The binding supports pairing of the device with your openHAB instance as follows:

1. Make sure that the authentication token field in your Nanoleaf controller thing configuration is left empty.
2. Hold down the on-off button of the controller for 5-7 seconds until the LED starts flashing in a pattern. This turns the device in pairing mode, and openHAB will try to request an authentication token for it.

Once your openHAB instance successfully requested and stored the authentication token in the thing configuration, the controller status changes to ONLINE, and you can start linking the channels to your items.

For a paired controller, discovery of the individual panels connected to it can be activated. By default, it is turned off. To turn it on, go to Paper UI, select the controller thing, open the configuration settings and turn "Discover Panels" switch on. All connected panels will be added a separate things to the inbox. Afterwards, you can turn "Discover Panels" configuration off.

## Thing Configuration

The controller thing has the following parameters:

| Config          | Description                                                                           |
| --------------- | ------------------------------------------------------------------------------------- |
| address         | IP address or hostname of the light panels controller (e.g. 192.168.1.100)            |
| port            | Port number of the light panels contoller. Default is 16021                           |
| authToken       | The authentication token received from the controller after successful pairing.       |
| refreshInterval | Interval in seconds to refresh the state of the light panels settings. Default is 60. |
| discoverPanels  | Enables or disables discovery of the connected panels as things of type lightpanel    |

The lightpanel thing has the following parameters:

| Config          | Description                                                                           |
| --------------- | ------------------------------------------------------------------------------------- |
| id              | ID assigned by the controller to the individual panel (e.g. 158)                      |

The IDs of the individual panels can be determined by switching the discoverPanels configuration to ON for the controller. This will add all panels with their IDs to the inbox.

## Channels

The controller bridge has the following channels:

| Channel             | Item Type | Description                                                            | Read Only |
|---------------------|-----------|------------------------------------------------------------------------|-----------|
| power               | Switch    | Power state of the light panels                                        | No        |
| brightness          | Dimmer    | Brightness of all light panels (0 to 100)                              | No        |
| hue                 | Number    | Hue of all light panels (0 to 360)                                     | No        |
| saturation          | Dimmer    | Saturation of all light panels (0 to 100)                              | No        |
| color               | Color     | Color of all light panels                                              | No        |
| colorTemperature    | Number    | Color temperature (in Kelvin, 1200 to 6500) of all light panels        | No        |
| colorMode           | String    | Color mode of the light panels                                         | Yes       |
| effect              | String    | Selected effect of the light panels                                    | No        |
| rhythmState         | Switch    | Connection state of the rhythm module                                  | Yes       |
| rhythmActive        | Switch    | Activity state of the rhythm module                                    | Yes       |
| rhythmMode          | Number    | Sound source for the rhythm module. 1=Microphone, 2=Aux cable          | No        |

A lightpanel thing has the following channels:

| Channel             | Item Type | Description                                                            | Read Only |
|---------------------|-----------|------------------------------------------------------------------------|-----------|
| panelColor          | Color     | Color of the individual light panel                                    | No        |
| panelBrightness     | Dimmer    | Brightness of the individual light panel                               | No        |

## Full Example

Below is a full example for a configuration (using things file instead of automatic discovery):

### nanoleaf.things

```
Bridge nanoleaf:controller:MyLightPanels [ address="192.168.1.100", port=16021, authToken="AbcDefGhiJk879LmNopqRstUv1234WxyZ", refreshInterval=61, discoverPanels="false" ] {
	Thing nanoleaf:lightpanel:MyLightPanels:135 [ id=135 ]
	Thing nanoleaf:lightpanel:MyLightPanels:158 [ id=158 ]
}
```

### nanoleaf.items

```
Switch NanoleafPower "Nanoleaf" { channel="nanoleaf:controller:MyLightPanels:power" }
Dimmer NanoleafBrightness "Helligkeit [%.0f]" { channel="nanoleaf:controller:MyLightPanels:brightness" }
Number NanoleafHue "Farbton [%.00f]" { channel="nanoleaf:controller:MyLightPanels:hue" }
Dimmer NanoleafSaturation "Sättigung [%.0f]" { channel="nanoleaf:controller:MyLightPanels:saturation" }
Color NanoleafColor "Farbe" { channel="nanoleaf:controller:MyLightPanels:color" }
Number NanoleafColorTemp "Farbtemperatur [%.000f]" { channel="nanoleaf:controller:MyLightPanels:colorTemperature" }
String NanoleafColorMode "Farbmodus [MAP(nanoleaf.map):%s]" { channel="nanoleaf:controller:MyLightPanels:colorMode" }
String NanoleafEffect "Effekt" { channel="nanoleaf:controller:MyLightPanels:effect" }
Switch NanoleafRhythmState "Rhythm verbunden [MAP(nanoleaf.map):%s]" { channel="nanoleaf:controller:MyLightPanels:rhythmState" }
Switch NanoleafRhythmActive "Rhythm aktiv [MAP(nanoleaf.map):%s]" { channel="nanoleaf:controller:MyLightPanels:rhythmActive" }
Number NanoleafRhythmSource  "Rhythm Quelle [%s]" { channel="nanoleaf:controller:MyLightPanels:rhythmMode" }
Color PanelColor1 "Panel 1" { channel="nanoleaf:lightpanel:MyLightPanels:135:panelColor" }
Dimmer PanelBrightness1 "Panel 1" { channel="nanoleaf:lightpanel:MyLightPanels:135:panelBrightness" }
Color PanelColor2 "Panel 2" { channel="nanoleaf:lightpanel:MyLightPanels:158:panelColor" }
```

### nanoleaf.sitemap

```
sitemap nanoleaf label="Nanoleaf"
{
    Frame label="Controller" {
            Switch item=NanoleafPower
            Slider item=NanoleafBrightness 
            Colorpicker item=NanoleafColor           
            Slider item=NanoleafSaturation
            Setpoint item=NanoleafColorTemp step=100 minValue=1200 maxValue=6500
            Setpoint item=NanoleafHue step=10 minValue=0 maxValue=360
            Text item=NanoleafColorMode
            Selection item=NanoleafEffect mappings=["Color Burst"="Color Burst", "Fireworks" = "Feuerwerk", "Flames" = "Flammen", "Forest" = "Wald", "Inner Peace" = "Innerer Frieden", "Meteor Shower" = "Meteorregen", "Nemo" = "Nemo", "Northern Lights" = "Nordlichter", "Paint Splatter" = "Farbspritzer", "Pulse Pop Beats" = "Pop Beats", "Rhythmic Northern Lights" = "Rhytmische Nordlichter", "Ripple" = "Welle", "Romantic" = "Romantik", "Snowfall" = "Schneefall", "Sound Bar" = "Sound Bar", "Streaking Notes" = "Streaking Notes", "moonlight" = "Mondlicht" ]
            Text item=NanoleafRhythmState
            Text item=NanoleafRhythmActive
            Selection item=NanoleafRhythmSource mappings=[0="Mikrofon", 1="Aux"]
	}
	
	Frame label="Panels" {
		Colorpicker item=PanelColor1
		Slider item=PanelBrightness1
		Colorpicker item=PanelColor2
	}
}
```

### nanoleaf.map

```
ON=Ja
OFF=Nein
effects=Effekte
```
