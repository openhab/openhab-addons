# LIFX Binding

This binding integrates the [LIFX LED Lights](http://www.lifx.com/). All LIFX lights are directly connected to the WLAN and the binding communicates with them over a UDP protocol.

![LIFX E27](doc/lifx_e27.jpg)

## Supported Things

The following table lists the thing types of the supported LIFX devices:

| Device Type                  | Thing Type   |
|------------------------------|--------------|
| Original 1000                | colorlight   |
| Color 650                    | colorlight   |
| Color 1000                   | colorlight   |
| Color 1000 BR30              | colorlight   |
| LIFX A19                     | colorlight   |
| LIFX BR30                    | colorlight   |
| LIFX Downlight               | colorlight   |
| LIFX GU10                    | colorlight   |
| LIFX Mini Color              | colorlight   |
| LIFX Tile                    | colorlight   |
|                              |              |
| LIFX+ A19                    | colorirlight |
| LIFX+ BR30                   | colorirlight |
|                              |              |
| LIFX Beam                    | colormzlight |
| LIFX Z                       | colormzlight |
|                              |              |
| White 800 (Low Voltage)      | whitelight   |
| White 800 (High Voltage)     | whitelight   |
| White 900 BR30 (Low Voltage) | whitelight   |
| LIFX Mini Day and Dusk       | whitelight   |
| LIFX Mini White              | whitelight   |

The thing type determines the capability of a device and with that the possible ways of interacting with it. The following matrix lists the capabilities (channels) for each type:

| Thing Type   | On/Off | Brightness | Color | Color Zone | Color Temperature | Color Temperature Zone | Infrared |
|--------------|:------:|:----------:|:-----:|:----------:|:-----------------:|:----------------------:|:--------:|
| colorlight   | X      |            | X     |            | X                 |                        |          |
| colorirlight | X      |            | X     |            | X                 |                        | X        |
| colormzlight | X      |            | X     | X          | X                 | X                      |          |
| whitelight   | X      | X          |       |            | X                 |                        |          |

## Discovery

The binding is able to auto-discover all lights in a network over the LIFX UDP protocol. Therefore all lights must be turned on.

*Note:* To get the binding working, all lights must be added to the WLAN network first with the help of the [LIFX smart phone applications](http://www.lifx.com/pages/go). The binding is NOT able to add or detect lights outside the network.

## Thing Configuration

Each light needs a Device ID or Host as a configuration parameter. The device ID is printed as a serial number on the light and can also be found within the native LIFX Android or iOS application. But usually the discovery works quite reliably, so that a manual configuration is not needed.

However, in the thing file, a manual configuration looks e.g. like

```
Thing lifx:colorlight:living [ deviceId="D073D5A1A1A1", fadetime=200 ]
```

The *fadetime* is an optional thing configuration parameter which configures the time to fade to a new color value (in ms). When the *fadetime* is not configured, the binding uses 300ms as default.

You can optionally also configure a fixed Host or IP address when lights are in a different subnet and are not discovered.

```
Thing lifx:colorirlight:porch [ host="10.120.130.4", fadetime=0 ]
```

## Channels

All devices support some of the following channels:

| Channel Type ID | Item Type | Description                                                                                                                                                      | Thing Types                                        |
|-----------------|-----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------|
| brightness      | Dimmer    | This channel supports adjusting the brightness value.                                                                                                            | whitelight                                         |
| color           | Color     | This channel supports full color control with hue, saturation and brightness values.                                                                             | colorlight, colorirlight, colormzlight             |
| colorzone       | Color     | This channel supports full zone color control with hue, saturation and brightness values.                                                                        | colormzlight                                       |
| infrared        | Dimmer    | This channel supports adjusting the infrared value. *Note:* IR capable lights only activate their infrared LEDs when the brightness drops below a certain level. | colorirlight                                       |
| signalstrength  | Number    | This channel represents signal strength with values 0, 1, 2, 3 or 4; 0 being worst strength and 4 being best strength.                                           | colorlight, colorirlight, colormzlight, whitelight |
| temperature     | Dimmer    | This channel supports adjusting the color temperature from cold (0%) to warm (100%).                                                                             | colorlight, colorirlight, colormzlight, whitelight |
| temperaturezone | Dimmer    | This channel supports adjusting the zone color temperature from cold (0%) to warm (100%).                                                                        | colormzlight                                       |

The *color* and *brightness* channels have a "Power on brightness" configuration option that is used to determine the brightness when a light is switched on. When it is left empty, the brightness of a light remains unchanged when a light is switched on or off.

The *color* channels have a "Power on color" configuration option that is used to determine the hue, saturation, brightness levels when a light is switched on. When it is left empty, the color of a light remains unchanged when a light is switched on or off. Configuration options contains 3 comma separated values, where first value is hue (0-360), second  saturation (0-100) and third brightness (0-100). If both "Power on brightness" and "Power on color" configuration options are defined, "Power on brightness" option overrides the brightness level defined on the "Power on color" configuration option.

The *temperature* channels have a "Power on temperature" configuration option that is used to determine the color temperature when a light is switched on. When it is left empty, the color temperature of a light remains unchanged when a light is switched on or off.

MultiZone lights (*colormzlight*) have serveral channels (e.g. *colorzone0*, *temperaturezone0*, etc.) that allow for controlling specific zones of the light. Changing the *color* and *temperature* channels will update the states of all zones. The *color* and *temperature* channels of MultiZone lights always return the same state as *colorzone0*, *temperaturezone0*.


## Full Example

In this example **living** is a Color 1000 light that has a *colorlight* thing type which supports *color* and *temperature* channels.

The **porch** light is a LIFX+ BR30 that has a *colorirlight* thing type which supports *color*, *temperature* and *infrared* channels.

The **ceiling** light is a LIFX Z with 2 strips (16 zones) that has a *colormzlight* thing type which supports *color*, *colorzone*, *temperature* and *temperaturezone* channels.

Finally, **kitchen** is a White 800 (Low Voltage) light that has a *whitelight* thing type which supports *brightness* and *temperature* channels.

Either create a single *Color* item linked to the *color* channel and define *Switch*, *Slider* and *Colorpicker* entries with this item in the sitemap.
Or create items for each type (*Color*, *Switch*, *Dimmer*) and define the correspondent entries in the sitemap.


### demo.things:

```
Thing lifx:colorlight:living [ deviceId="D073D5A1A1A1" ]

Thing lifx:colorlight:living2 [ deviceId="D073D5A2A2A2" ] {
    Channels:
        Type color : color [ powerOnBrightness=50 ]
}

Thing lifx:colorirlight:porch [ deviceId="D073D5B2B2B2", host="10.120.130.4", fadetime=0 ] {
    Channels:
        Type color : color [ powerOnBrightness=75 ]
}

Thing lifx:colorirlight:porch [ deviceId="D073D5B2B2B2", host="10.120.130.4", fadetime=0 ] {
    Channels:
        Type temperature : temperature [ powerOnTemperature=20 ]
}

Thing lifx:colorirlight:porch [ deviceId="D073D5B2B2B2", host="10.120.130.4", fadetime=0 ] {
    Channels:
        Type color : color [ powerOnColor="120,100,50" ] // Deep green, 50% brightness
}

Thing lifx:colormzlight:ceiling [ host="10.120.130.5" ]

Thing lifx:whitelight:kitchen [ deviceId="D073D5D4D4D4", fadetime=150 ]
```

### demo.items:

```
// Living
Color Living_Color { channel="lifx:colorlight:living:color" }
Dimmer Living_Temperature { channel="lifx:colorlight:living:temperature" }

// Living2 (alternative approach)
Color Living2_Color { channel="lifx:colorlight:living2:color" }
Switch Living2_Switch { channel="lifx:colorlight:living2:color" }
Dimmer Living2_Dimmer { channel="lifx:colorlight:living2:color" }
Dimmer Living2_Temperature { channel="lifx:colorlight:living2:temperature" }

// Porch
Color Porch_Color { channel="lifx:colorirlight:porch:color" }
Dimmer Porch_Infrared { channel="lifx:colorirlight:porch:infrared" }
Dimmer Porch_Temperature { channel="lifx:colorirlight:porch:temperature" }
Number Porch_Signal_Strength { channel="lifx:colorirlight:porch:signalstrength" }

// Ceiling
Color Ceiling_Color { channel="lifx:colormzlight:ceiling:color" }
Dimmer Ceiling_Temperature { channel="lifx:colormzlight:ceiling:temperature" }
Color Ceiling_Color_Zone_0 { channel="lifx:colormzlight:ceiling:colorzone0" }
Dimmer Ceiling_Temperature_Zone_0 { channel="lifx:colormzlight:ceiling:temperaturezone0" }
Color Ceiling_Color_Zone_15 { channel="lifx:colormzlight:ceiling:colorzone15" }
Dimmer Ceiling_Temperature_Zone_15 { channel="lifx:colormzlight:ceiling:temperaturezone15" }

// Kitchen
Switch Kitchen_Toggle { channel="lifx:whitelight:kichen:brightness" }
Dimmer Kitchen_Brightness { channel="lifx:whitelight:kitchen:brightness" }
Dimmer Kitchen_Temperature { channel="lifx:whitelight:kitchen:temperature" }
```

### demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="Living" {
        Switch item=Living_Color
        Slider item=Living_Color
        Colorpicker item=Living_Color
        Slider item=Living_Temperature
    }

    Frame label="Living2" {
        Switch item=Living2_Toggle
        Slider item=Living2_Dimmer
        Colorpicker item=Living2_Color
        Slider item=Living2_Temperature
    }

    Frame label="Porch" {
        Switch item=Porch_Color
        Slider item=Porch_Color
        Colorpicker item=Porch_Color
        Slider item=Porch_Temperature
        Slider item=Porch_Infrared
        Text item=Porch_Signal_Strength
    }

    Frame label="Ceiling" {
        Switch item=Ceiling_Color
        Slider item=Ceiling_Color
        Colorpicker item=Ceiling_Color
        Slider item=Ceiling_Temperature
        Colorpicker item=Ceiling_Color_Zone_0
        Slider item=Ceiling_Temperature_Zone_0
        Colorpicker item=Ceiling_Color_Zone_15
        Slider item=Ceiling_Temperature_Zone_15
    }

    Frame label="Kitchen" {
        Switch item=Kitchen_Toggle
        Slider item=Kitchen_Brightness
        Slider item=Kitchen_Temperature
    }
}
```
