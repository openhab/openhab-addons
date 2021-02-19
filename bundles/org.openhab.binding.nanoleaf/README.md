# Nanoleaf Binding

This binding integrates the [Nanoleaf Light Panels](https://nanoleaf.me/en/consumer-led-lighting/products/smarter-series/nanoleaf-light-panels-smarter-kit/).

![Image](doc/Nanoleaf.jpg)

It enables you to authenticate, control, and obtain information of a Light Panel's device.
The binding uses the [Nanoleaf OpenAPI](https://forum.nanoleaf.me/docs/openapi), which requires firmware version [1.5.0](https://helpdesk.nanoleaf.me/hc/en-us/articles/214006129-Light-Panels-Firmware-Release-Notes) or higher.

![Image](doc/LightPanels2_small.jpg) ![Image](doc/NanoCanvas_small.jpg)

## Supported Things

Nanoleaf provides a bunch of devices of which some are connected to Wifi whereas other use the new Thread Technology. This binding only supports devices that are connected to Wifi.

Currently Nanoleaf's "Light Panels" and "Canvas" devices are supported.
Note that only specific types do support the touch functionality, so the binding needs to check these types.

The binding supports two thing types: controller and lightpanel.

The controller thing is the bridge for the individually attached panels/canvas and can be perceived as the nanoleaf device at the wall as a whole (either called "light panels" or "canvas" by Nanoleaf).
With the controller thing you can control channels which affect all panels, e.g. selecting effects or setting the brightness.

The lightpanel (singular) thing controls one of the individual panels/canvas that are connected to each other.
Each individual panel has therefore its own id assigned to it.
You can set the **color** for each panel or turn it on (white) or off (black) and in the case of a nanoleaf canvas you can even detect single and double **touch events** related to an individual panel which opens a whole new world of controlling any other device within your openHAB environment.


| Nanoleaf Name          | Type | Description                                                | supported | touch support |
| ---------------------- | ---- | ---------------------------------------------------------- | --------- | ------------- |
| Light Panels           | NL22 | Triangles 1st Generation                                   |     X     |      (-)      |  
| Shapes Triangle        | NL42 | Triangles 2nd Generation (rounded edges)                   |     X     |       X       |
| Shapes Hexagon         | NL42 | Triangles 2nd Generation (rounded edges)                   |    (X)    |      (X)      |
| Shapes Mini Triangles  |  ??  | Mini Triangles                                             |     ?     |       ?       |
| Canvas                 | NL29 | Squares                                                    |     X     |       X       |

 x  = Supported  (x) = Supported but only tested by community   (-) = unknown (no device available to test)

Note: In case of major changes of a binding (like adding more features to a thing) it becomes necessary to delete your things due to the things not being compatible anymore.
Don't worry too much though as they will be easily redetected and nothing really is lost.
Just make sure that you delete them and rediscover as described below.

## Discovery

**Adding the Controller as a Thing**

To add a nanoleaf controller, go to your inbox and start a scan.
Then choose "Nanoleaf Binding".
A controller (bridge) device is discovered automatically using mDNS in your local network.
Alternatively, you can also provide a things file (see below for more details).
After the device is discovered and added as a thing, it needs a valid authentication token that must be obtained by pairing it with your openHAB instance.
Without the token the light panels remain in status OFFLINE.

The binding supports pairing of the device with your openHAB instance as follows:

1. Make sure that the authentication token field in your Nanoleaf controller thing configuration is left empty.
2. Hold down the on-off button of the controller for 5-7 seconds until the LED starts flashing/cycling in a pattern, which turns the device in pairing mode, and openHAB will try to request an authentication token for it.

Once your openHAB instance successfully requested and stored the authentication token in the controller's thing configuration, the controller status changes to ONLINE, and you can start linking the channels to your items.

Tip: if you press (2) just before adding the item from the inbox it usually catches the auth token right away and if you are lucky it already automatically starts discovering the panels in one turn (see below).

**Adding the invidual light panels as a thing**

After you have added the controller as a thing and it has been successfully paired as described as above, the individual panels connected to it can be discovered by **starting another scan** for the Nanoleaf binding.
All connected panels will be added as separate things to the inbox.

Troubleshooting: In seldom cases (in particular together with updating the binding) things or items do not work as expected, are offline or may not be detected.
In this case:

- remove the panels (maybe twice by force removing it)
- remove the controller (maybe twice by force removing it)
- stop and then start openHAB
- Rediscover like described above

**Knowing which panel has which id**

Unfortunately it is not easy to find out which panel gets which id, and this becomes pretty important if you have lots of them and want to assign rules.
Don't worry as the binding comes with some helpful support in the background the canvas type (this is only provided for the canvas device because triangles can have weird layouts that are hard to express in a log output)

- Set up a switch item with the channel panelLayout on the controller (see NanoRetrieveLayout below) and set the switch to true
- look out for something like "Panel layout and ids" in the openHAB logs. Below that you will see a panel layout similar to

Compare the following output with the right picture at the beginning of the article

```                                     
            31413                    9162       13276     

55836       56093       48111       38724       17870        5164       64279

                        58086        8134                   39755             

                                    41451                                     

```

Disclaimer: this works best with square devices and not necessarily well with triangles due to the more geometrically flexible layout.

## Thing Configuration

The controller thing has the following parameters:

| Config          | Description                                                                           |
| --------------- | ------------------------------------------------------------------------------------- |
| address         | IP address or hostname of the light panels controller (e.g. 192.168.1.100)            |
| port            | Port number of the light panels contoller. Default is 16021                           |
| authToken       | The authentication token received from the controller after successful pairing.       |
| refreshInterval | Interval in seconds to refresh the state of the light panels settings. Default is 60. |
| deviceType      | (readOnly) defines the type: lightpanels (triangle) or canvas (square)                |

The lightpanel thing has the following parameters:

| Config          | Description                                                                           |
| --------------- | ------------------------------------------------------------------------------------- |
| id              | ID assigned by the controller to the individual panel (e.g. 158)                      |

The IDs of the individual panels can be determined by starting another scan once the controller is configured and online.
This will add all connected panels with their IDs to the inbox.

## Channels

The controller bridge has the following channels:

| Channel             | Item Type | Description                                                            | Read Only |
|---------------------|-----------|------------------------------------------------------------------------|-----------|
| power               | Switch    | Power state of the light panels                                        | No        |
| color               | Color     | Color of all light panels                                              | No        |
| colorTemperature    | Dimmer    | Color temperature (in percent) of all light panels                     | No        |
| colorTemperatureAbs | Number    | Color temperature (in Kelvin, 1200 to 6500) of all light panels        | No        |
| colorMode           | String    | Color mode of the light panels                                         | Yes       |
| effect              | String    | Selected effect of the light panels                                    | No        |
| rhythmState         | Switch    | Connection state of the rhythm module                                  | Yes       |
| rhythmActive        | Switch    | Activity state of the rhythm module                                    | Yes       |
| rhythmMode          | Number    | Sound source for the rhythm module. 0=Microphone, 1=Aux cable          | No        |
| panelLayout         | Switch    | Set to true will log out panel layout (returns to off automatically    | No        |

A lightpanel thing has the following channels:

| Channel             | Item Type | Description                                                            | Read Only |
|---------------------|-----------|------------------------------------------------------------------------|-----------|
| panelColor          | Color     | Color of the individual light panel                                    | No        |
| singleTap           | Switch    | [Canvas Only] Is set when the user taps that panel once (1 second pulse)              | Yes       |
| doubleTap           | Switch    | [Canvas Only] Is set when the user taps that panel twice (1 second pulse)              | Yes       |

**color and panelColor**

The color and panelColor channels support full color control with hue, saturation and brightness values.
For example, brightness of *all* panels at once can be controlled by defining a dimmer item for the color channel of the *controller thing*.
The same applies to the panelColor channel of an individual lightpanel thing.

What might not be obvious and even maybe confusing is the fact that brightness and color use the *same* channel but two different *itemTypes*. While the Color-itemtype controls the color, the Dimmer-itemtype controls the brightness on the same channel.

**Limitations assigning specific colors on individual panels:**

- Due to the way the API of the nanoleaf is designed, each time a color is assigned to a panel, it will be directly sent to that panel. The result is that if you send colors to several panels more or less at the same time, they will not be set at the same time but one after the other and rather appear like a sequence but as a one shot.
- Another important limitation is that individual panels cannot be set while a dynamic effect is running on the panel which means that the following happens
  - As soon as you set an individual panel a so called "static effect" is created which replaces the chosen dynamic effect. You can even see that in the nanoleaf app that shows that a static effect is now running.
  - Unfortunately, at least at the moment, the colors of the current state cannot be retrieved due to the high frequency of color changes that cannot be read quickly enough from the canvas, so all panels go to OFF
  - The the first panelColor command is applied to that panel (and of course then all subsequent commands)
  - The fact that it is called a static effect does not mean that you cannot create animations. The Rainbow rule below shows a good example for the whole canvas. Just replace the controller item with a panel item and you will get the rainbow effect with an individual panel.   

**Touch Support**

Nanoleaf's Canvas introduces a whole new experience by adding touch support to it. This allows single and double taps on individual panels to be detected and then processed via rules to further control any other device!
Note that even gestures like up, down, left, right are sent but can only be detected on the whole set of panels and not on an individual panel. These four gestures are not yet supported by the binding but may be added in a later release.

To detect single and double taps the panel's have been extended to have two additional channels named singleTap and doubleTap which act like switches that are turned on as soon as a tap type is detected.
These switches then act as a pulse to further control anything else via rules.

If a panel is tapped the switch is set to ON and automatically reset to OFF after 1 second (this may be configured in the future) to simulate a pulse. A rule can easily detect the transition from OFF to ON and later detect another tap as it is automatically reset by the binding. See the example below on Panel 2.

Keep in mind that the double tap is used as an already built-in functionality by default when you buy the nanoleaf: it switches all panels (hence the controller) to on or off like a light switch for all the panels at once. To circumvent that

- Within the nanoleaf app go to the dashboard and choose your device. Enter the settings for that device by clicking the cog icon in the upper right corner.
- Enable "Touch Gesture" and assign the gestures you want to happen but set the double tap to unassigned.
- To still have the possibility to switch on the whole canvas device with all its panels by double tapping a specific panel, you can easily write a rule that triggers on the double tap channel of that panel and then toggles the Power Channel of the controller. See the example below on Panel 1.

More details can be found in the full example below.

## Full Example

The following files provide a full example for a configuration (using a things file instead of automatic discovery):

### nanoleaf.things

```
Bridge nanoleaf:controller:MyLightPanels @ "mylocation" [ address="192.168.1.100", port=16021, authToken="AbcDefGhiJk879LmNopqRstUv1234WxyZ", refreshInterval=60 ] {
    Thing lightpanel 135 [ id=135 ]
    Thing lightpanel 158 [ id=158 ]
}
```

If you define your device statically in the thing file, autodiscovery of the same thing is suppressed by using

* the [address="..." ]  of the controller
* and the [id=123] of the lightpanel

in the bracket to identify the uniqueness of the discovered device. Therefore it is recommended to the give the controller a fixed ip address.

Note: To generate the `authToken`:

* On the Nanoleaf controller, hold the on-off button for 5-7 seconds until the LED starts flashing.
* Send a POST request to the authorization endpoint within 30 seconds of activating pairing, like this:

`http://<address>:16021/api/v1/new`

e.g. via command line `curl --location --request POST 'http://<address>:16021/api/v1/new'`

### nanoleaf.items

Note: If you did autodiscover your things and items:

- A controller item looks like nanoleaf:controller:F0ED4F9351AF:power where F0ED4F9351AF is the id of the controller that has been automatically assigned by the binding.
- A panel item looks like nanoleaf:lightpanel:F0ED4F9351AF:39755:singleTap where 39755 is the id of the panel that has been automatically assigned by the binding.

```
Switch NanoleafPower "Nanoleaf" { channel="nanoleaf:controller:MyLightPanels:power" }
Color NanoleafColor "Color" { channel="nanoleaf:controller:MyLightPanels:color" }
Dimmer NanoleafBrightness "Brightness [%.0f]" { channel="nanoleaf:controller:MyLightPanels:color" }
String NanoleafHue "Hue [%s]"
String NanoleafSaturation "Saturation [%s]"
Dimmer NanoleafColorTemp "Color temperature [%.0f]" { channel="nanoleaf:controller:MyLightPanels:colorTemperature" }
Number NanoleafColorTempAbs "Color temperature [%.000f]" { channel="nanoleaf:controller:MyLightPanels:colorTemperatureAbs" }
String NanoleafColorMode "Color mode [%s]" { channel="nanoleaf:controller:MyLightPanels:colorMode" }
String NanoleafEffect "Effect" { channel="nanoleaf:controller:MyLightPanels:effect" }
Switch NanoleafRhythmState "Rhythm connected [MAP(nanoleaf.map):%s]" { channel="nanoleaf:controller:MyLightPanels:rhythmState" }
Switch NanoleafRhythmActive "Rhythm active [MAP(nanoleaf.map):%s]" { channel="nanoleaf:controller:MyLightPanels:rhythmActive" }
Number NanoleafRhythmSource  "Rhythm source [%s]" { channel="nanoleaf:controller:MyLightPanels:rhythmMode" }
Switch NanoRetrieveLayout "Nano Layout" { channel="nanoleaf:controller:D81E7A7E424E:panelLayout" }

// note that the next to items use the exact same channel but the two different types Color and Dimmer to control different parameters
Color Panel1Color "Panel 1" { channel="nanoleaf:lightpanel:MyLightPanels:135:panelColor" }
Dimmer Panel1Brightness "Panel 1" { channel="nanoleaf:lightpanel:MyLightPanels:135:panelColor" }
Switch Panel1DoubleTap "Toggle device on and off" { channel="nanoleaf:lightpanel:MyLightPanels:135:doubleTap" }
Switch Panel2Color "Panel 2" { channel="nanoleaf:lightpanel:MyLightPanels:158:panelColor" }
Switch Panel2SingleTap "Panel 2 Single Tap" { channel="nanoleaf:lightpanel:MyLightPanels:158:singleTap" }
Switch Panel2DoubleTap "Panel 2 Double Tap" { channel="nanoleaf:lightpanel:MyLightPanels:158:doubleTap" }
Switch NanoleafRainbowScene "Show Rainbow Scene"
```

### nanoleaf.sitemap

```
sitemap nanoleaf label="Nanoleaf"
{
    Frame label="Controller" {
            Switch item=NanoleafPower
            Slider item=NanoleafBrightness
            Colorpicker item=NanoleafColor           
            Text item=NanoleafHue
            Text item=NanoleafSaturation
            Slider item=NanoleafColorTemp     
            Setpoint item=NanoleafColorTempAbs step=100 minValue=1200 maxValue=6500            
            Text item=NanoleafColorMode
            Selection item=NanoleafEffect mappings=["Color Burst"="Color Burst", "Fireworks" = "Fireworks", "Flames" = "Flames", "Forest" = "Forest", "Inner Peace" = "Inner Peace", "Meteor Shower" = "Meteor Shower", "Nemo" = "Nemo", "Northern Lights" = "Northern Lights", "Paint Splatter" = "Paint Splatter", "Pulse Pop Beats" = "Pulse Pop Beats", "Rhythmic Northern Lights" = "Rhythmic Northern Lights", "Ripple" = "Ripple", "Romantic" = "Romantic", "Snowfall" = "Snowfall", "Sound Bar" = "Sound Bar", "Streaking Notes" = "Streaking Notes", "moonlight" = "Moonlight", "*Static*" = "Color (single panels)", "*Dynamic*" = "Color (all panels)" ]
            Text item=NanoleafRhythmState
            Text item=NanoleafRhythmActive
            Selection item=NanoleafRhythmSource mappings=[0="Microphone", 1="Aux"]
            Switch item=NanoRetrieveLayout
    }

    Frame label="Panels" {
        Colorpicker item=Panel1Color
        Slider item=Panel1Brightness
        Colorpicker item=Panel2Color
    }

    Frame label="Scenes" {
        Switch item=NanoleafRainbowScene
    }
}
```

Note: The mappings to effects in the selection item are specific for each Nanoleaf installation and should be adapted accordingly.
Only the effects "\*Static\*" and "\*Dynamic\*" are predefined by the controller and should always be present in the mappings.

### nanoleaf.rules

```
rule "UpdateHueAndSat"
when Item NanoleafColor changed
then
    val hsbValues = NanoleafColor.state as HSBType    
    NanoleafHue.postUpdate(hsbValues.hue.intValue)
    NanoleafSaturation.postUpdate(hsbValues.saturation.intValue)
end

rule "ShowRainbowScene"
when Item NanoleafRainbowScene received command ON
then
    val saturation = new PercentType(75)
    val brightness = new PercentType(90)
    val long pause = 200

    var hue = 0
    var direction = 1

    while(NanoleafRainbowScene.state == ON) {        
        Thread::sleep(pause)        
        hue = hue + (5 * direction)
        if(hue >= 359) {
            hue = 359
            direction = direction * -1            
        }
        else if (hue < 0) {
            hue = 0
            direction = direction * -1            
        }        
        // replace NanoleafColor with Panel1Color to run rainbow on a single panel
        NanoleafColor.sendCommand(new HSBType(new DecimalType(hue), saturation, brightness))
    }
end

rule "Nanoleaf canvas touch detection Panel 2"
when
    Item Panel2SingleTap changed from NULL to ON or
    Item Panel2SingleTap changed from OFF to ON
then
    logInfo("CanvasTouch", "Nanoleaf Canvas Panel 2 was touched once")

    if (My_Main_Light.state == OFF) {
        sendCommand(My_Main_Light,ON)
    } else {
        sendCommand(My_Main_Light,OFF)
    }
end

rule "Nanoleaf double tap toggles power of device"
when
    Item Panel1DoubleTap changed from NULL to ON or
    Item Panel1DoubleTap changed from OFF to ON
then
    logInfo("CanvasTouch", "Nanoleaf Canvas Panel 1 was touched twice. Toggle Power of whole canvas.")

    if (NanoleafPower.state == OFF ) {
        sendCommand(NanoleafPower,ON)
    } else {
        sendCommand(NanoleafPower,OFF)
    }
end
```

### nanoleaf.map

```
ON = Yes
OFF = No
effects = Effect
hs = Hue/Saturation
ct = Color Temperature
```
