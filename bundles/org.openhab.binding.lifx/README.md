# LIFX Binding

This binding integrates the [LIFX LED Lights](https://www.lifx.com/).
All LIFX lights are directly connected to the WLAN and the binding communicates with them over a UDP protocol.

![LIFX E27](doc/lifx_e27.jpg)

## Supported Things

The following table lists the thing types of the supported LIFX devices:

| Device Type                  | Thing Type    |
|------------------------------|---------------|
| Original 1000                | colorlight    |
| Color 650                    | colorlight    |
| Color 1000                   | colorlight    |
| Color 1000 BR30              | colorlight    |
| LIFX A19                     | colorlight    |
| LIFX BR30                    | colorlight    |
| LIFX Candle                  | colorlight    |
| LIFX Downlight               | colorlight    |
| LIFX GU10                    | colorlight    |
| LIFX Mini Color              | colorlight    |
|                              |               |
| LIFX Clean                   | colorhevlight |
|                              |               |
| LIFX+ A19                    | colorirlight  |
| LIFX+ BR30                   | colorirlight  |
|                              |               |
| LIFX Beam                    | colormzlight  |
| LIFX Z                       | colormzlight  |
|                              |               |
| LIFX Tile                    | tilelight     |
|                              |               |
| White 800 (Low Voltage)      | whitelight    |
| White 800 (High Voltage)     | whitelight    |
| White 900 BR30 (Low Voltage) | whitelight    |
| LIFX Candle Warm to White    | whitelight    |
| LIFX Filament                | whitelight    |
| LIFX Mini Day and Dusk       | whitelight    |
| LIFX Mini White              | whitelight    |

The thing type determines the capability of a device and with that the possible ways of interacting with it.
The following matrix lists the capabilities (channels) for each type:

| Thing Type    | On/Off | Brightness | Color | Color Zone | (Abs) Color Temperature | (Abs) Color Temperature Zone | HEV Cycle | Infrared | Tile Effects |
|---------------|:------:|:----------:|:-----:|:----------:|:-----------------------:|:----------------------------:|:---------:|:--------:|:------------:|
| colorlight    |    X   |            |   X   |            |            X            |                              |           |          |              |
| colorhevlight |    X   |            |   X   |            |            X            |                              |     X     |          |              |
| colorirlight  |    X   |            |   X   |            |            X            |                              |           |     X    |              |
| colormzlight  |    X   |            |   X   |      X     |            X            |               X              |           |          |              |
| tilelight     |    X   |      X     |   X   |            |            X            |                              |           |          |       X      |
| whitelight    |    X   |      X     |       |            |            X            |                              |           |          |              |

## Discovery

The binding is able to auto-discover all lights in a network over the LIFX UDP protocol.
Therefore all lights must be turned on.

_Note:_ To get the binding working, all lights must be added to the WLAN network first with the help of the [LIFX smart phone applications](https://www.lifx.com/pages/app).
The binding is NOT able to add or detect lights outside the network.

## Thing Configuration

Each light needs a Device ID or Host as a configuration parameter.
The device ID is printed as a serial number on the light and can also be found within the native LIFX Android or iOS application.
But usually the discovery works quite reliably, so that a manual configuration is not needed.

However, in the thing file, a manual configuration looks e.g. like

```java
Thing lifx:colorlight:living [ deviceId="D073D5A1A1A1", fadetime=200 ]
```

The _fadetime_ is an optional thing configuration parameter which configures the time to fade to a new color value (in ms).
When the _fadetime_ is not configured, the binding uses 300ms as default.

You can optionally also configure a fixed Host or IP address when lights are in a different subnet and are not discovered.

```java
Thing lifx:colorirlight:porch [ host="10.120.130.4", fadetime=0 ]
```

## Channels

All devices support some of the following channels:

| Channel Type ID    | Item Type          | Description                                                                                                                                                      | Thing Types                                                                  |
|--------------------|--------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------|
| abstemperature     | Number:Temperature | This channel supports adjusting the color temperature in Kelvin.                                                                                                 | colorlight, colorhevlight, colorirlight, colormzlight, tilelight, whitelight |
| abstemperaturezone | Number:Temperature | This channel supports adjusting the zone color temperature in Kelvin.                                                                                            | colormzlight                                                                 |
| brightness         | Dimmer             | This channel supports adjusting the brightness value.                                                                                                            | whitelight                                                                   |
| color              | Color              | This channel supports full color control with hue, saturation and brightness values.                                                                             | colorlight, colorhevlight, colorirlight, colormzlight, tilelight             |
| colorzone          | Color              | This channel supports full zone color control with hue, saturation and brightness values.                                                                        | colormzlight                                                                 |
| effect             | String             | This channel represents a type of light effect (e.g. for tile light: off, morph, flame)                                                                          | tilelight                                                                    |
| hevcycle           | Switch             | This channel supports starting and stopping the HEV clean cycle.                                                                                                 | colorhevlight                                                                |
| infrared           | Dimmer             | This channel supports adjusting the infrared value. _Note:_ IR capable lights only activate their infrared LEDs when the brightness drops below a certain level. | colorirlight                                                                 |
| signalstrength     | Number             | This channel represents signal strength with values 0, 1, 2, 3 or 4; 0 being worst strength and 4 being best strength.                                           | colorlight, colorhevlight, colorirlight, colormzlight, tilelight, whitelight |
| temperature        | Dimmer             | This channel supports adjusting the color temperature from cold (0%) to warm (100%).                                                                             | colorlight, colorhevlight, colorirlight, colormzlight, tilelight, whitelight |
| temperaturezone    | Dimmer             | This channel supports adjusting the zone color temperature from cold (0%) to warm (100%).                                                                        | colormzlight                                                                 |

The _color_ and _brightness_ channels have a "Power On Brightness" configuration option that is used to determine the brightness when a light is switched on.
When it is left empty, the brightness of a light remains unchanged when a light is switched on or off.

The _color_ channels have a "Power On Color" configuration option that is used to determine the hue, saturation, brightness levels when a light is switched on.
When it is left empty, the color of a light remains unchanged when a light is switched on or off.
Configuration options contains 3 comma separated values, where first value is hue (0-360), second  saturation (0-100) and third brightness (0-100).
If both "Power on brightness" and "Power On Color" configuration options are defined, "Power on brightness" option overrides the brightness level defined on the "Power on color" configuration option.

The _temperature_ channels have a "Power On Temperature" configuration option that is used to determine the color temperature when a light is switched on. When it is left empty, the color temperature of a light remains unchanged when a light is switched on or off.

MultiZone lights (_colormzlight_) have several channels (e.g. _colorzone0_, _temperaturezone0_, _abstemperaturezone0_, etc.) that allow for controlling specific zones of the light.
Changing the _color_, _temperature_ and _abstemperature_ channels will update the states of all zones.
The _color_, _temperature_ and _abstemperature_ channels of MultiZone lights always return the same state as _colorzone0_, _temperaturezone0_, _abstemperaturezone0_.

The _hevcycle_ channels have an optional "HEV Cycle Duration" configuration option that can be used to override the cycle duration configured in the light.

LIFX Tile (_tilelight_) supports special tile effects: morph and flame.
These effects are predefined to their appearance using LIFX application.
Each effect has a separate speed configuration option.

## Full Example

In this example **living** is a Color 1000 light that has a _colorlight_ thing type which supports _color_ and _temperature_ channels.

The **desk** light is a LIFX Clean that has a _colorhevlight_ thing type which supports _color_, _temperature_ and _hevcycle_ channels.

The **porch** light is a LIFX+ BR30 that has a _colorirlight_ thing type which supports _color_, _temperature_ and _infrared_ channels.

The **ceiling** light is a LIFX Z with 2 strips (16 zones) that has a _colormzlight_ thing type which supports _color_, _colorzone_, _temperature_ and _temperaturezone_ channels.

Finally, **kitchen** is a White 800 (Low Voltage) light that has a _whitelight_ thing type which supports _brightness_ and _temperature_ channels.

Either create a single _Color_ item linked to the _color_ channel and define _Switch_, _Slider_ and _Colorpicker_ entries with this item in the sitemap.
Or create items for each type (_Color_, _Switch_, _Dimmer_) and define the correspondent entries in the sitemap.

### demo.things:

```java
Thing lifx:colorlight:living [ deviceId="D073D5A1A1A1" ]

Thing lifx:colorlight:living2 [ deviceId="D073D5A2A2A2" ] {
    Channels:
        Type color : color [ powerOnBrightness=50 ]
}

Thing lifx:colorhevlight:desk [ deviceId="D073D5A3A3A3" ] {
    Channels:
        Type hevcycle : hevcycle [ hevCycleDuration=3600 ]
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

```java
// Living
Color Living_Color { channel="lifx:colorlight:living:color" }
Dimmer Living_Temperature { channel="lifx:colorlight:living:temperature" }
Number:Temperature Living_Abs_Temperature "Living Room Lights Color Temperature [%d K]" { channel="lifx:colorlight:living:abstemperature" }

// Living2 (alternative approach)
Color Living2_Color { channel="lifx:colorlight:living2:color" }
Switch Living2_Switch { channel="lifx:colorlight:living2:color" }
Dimmer Living2_Dimmer { channel="lifx:colorlight:living2:color" }
Dimmer Living2_Temperature { channel="lifx:colorlight:living2:temperature" }
Number:Temperature Living2_Abs_Temperature "Living Room Lights Color Temperature [%d K]" { channel="lifx:colorlight:living2:abstemperature" }

// Desk
Color Desk_Color { channel="lifx:colorhevlight:desk:color" }
Dimmer Desk_Temperature { channel="lifx:colorhevlight:desk:temperature" }
Number:Temperature Desk_Abs_Temperature "Desk Lamp Color Temperature [%d K]" { channel="lifx:colorhevlight:desk:abstemperature" }
Switch Desk_HEV_Cycle { channel="lifx:colorhevlight:desk:hevcycle" }

// Porch
Color Porch_Color { channel="lifx:colorirlight:porch:color" }
Dimmer Porch_Infrared { channel="lifx:colorirlight:porch:infrared" }
Dimmer Porch_Temperature { channel="lifx:colorirlight:porch:temperature" }
Number:Temperature Porch_Abs_Temperature "Porch Light Color Temperature [%d K]" { channel="lifx:colorirlight:porch:abstemperature" }
Number Porch_Signal_Strength { channel="lifx:colorirlight:porch:signalstrength" }

// Ceiling
Color Ceiling_Color { channel="lifx:colormzlight:ceiling:color" }
Dimmer Ceiling_Temperature { channel="lifx:colormzlight:ceiling:temperature" }
Number:Temperature Ceiling_Abs_Temperature "Ceiling Light Color Temperature [%d K]" { channel="lifx:colormzlight:ceiling:abstemperature" }
Color Ceiling_Color_Zone_0 { channel="lifx:colormzlight:ceiling:colorzone0" }
Dimmer Ceiling_Temperature_Zone_0 { channel="lifx:colormzlight:ceiling:temperaturezone0" }
Number:Temperature Ceiling_Abs_Temperature_Zone_0 "Ceiling Light 0 Color Temperature [%d K]" { channel="lifx:colormzlight:ceiling:abstemperaturezone0" }
Color Ceiling_Color_Zone_15 { channel="lifx:colormzlight:ceiling:colorzone15" }
Dimmer Ceiling_Temperature_Zone_15 { channel="lifx:colormzlight:ceiling:temperaturezone15" }
Number:Temperature Ceiling_Abs_Temperature_Zone_15 "Ceiling Light 15 Color Temperature [%d K]" { channel="lifx:colormzlight:ceiling:abstemperaturezone15" }

// Kitchen
Switch Kitchen_Toggle { channel="lifx:whitelight:kichen:brightness" }
Dimmer Kitchen_Brightness { channel="lifx:whitelight:kitchen:brightness" }
Dimmer Kitchen_Temperature { channel="lifx:whitelight:kitchen:temperature" }
Number:Temperature Kitchen_Abs_Temperature "Kitchen Light Color Temperature [%d K]" { channel="lifx:whitelight:kitchen:abstemperature" }
```

### demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame label="Living" {
        Switch item=Living_Color
        Slider item=Living_Color
        Colorpicker item=Living_Color
        Slider item=Living_Temperature
        Slider item=Living_Abs_Temperature
    }

    Frame label="Living2" {
        Switch item=Living2_Toggle
        Slider item=Living2_Dimmer
        Colorpicker item=Living2_Color
        Slider item=Living2_Temperature
        Slider item=Living2_Abs_Temperature
    }

    Frame label="Desk" {
        Switch item=Desk_Color
        Slider item=Desk_Color
        Colorpicker item=Desk_Color
        Slider item=Desk_Temperature
        Slider item=Desk_Abs_Temperature
        Switch item=Desk_HEV_Cycle
    }

    Frame label="Porch" {
        Switch item=Porch_Color
        Slider item=Porch_Color
        Colorpicker item=Porch_Color
        Slider item=Porch_Temperature
        Slider item=Porch_Abs_Temperature
        Slider item=Porch_Infrared
        Text item=Porch_Signal_Strength
    }

    Frame label="Ceiling" {
        Switch item=Ceiling_Color
        Slider item=Ceiling_Color
        Colorpicker item=Ceiling_Color
        Slider item=Ceiling_Temperature
        Slider item=Ceiling_Abs_Temperature
        Colorpicker item=Ceiling_Color_Zone_0
        Slider item=Ceiling_Temperature_Zone_0
        Slider item=Ceiling_Abs_Temperature_Zone_0
        Colorpicker item=Ceiling_Color_Zone_15
        Slider item=Ceiling_Temperature_Zone_15
        Slider item=Ceiling_Abs_Temperature_Zone_15
    }

    Frame label="Kitchen" {
        Switch item=Kitchen_Toggle
        Slider item=Kitchen_Brightness
        Slider item=Kitchen_Temperature
        Slider item=Kitchen_Abs_Temperature
    }
}
```
