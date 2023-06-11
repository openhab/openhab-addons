# Feican Binding

This binding adds support for the Feican Wi-Fi version of the smart light LED Bulb, the WiFi RGBW Bulb.

With this binding the light bulb can be switched on or off, set the color or set color based on color temperature.
Set the brightness. And it contains a set of preset programs, where for some the program speed can be set.

## Supported Things

This binding supports the Feican smart smart light LED Bulb, WiFi RGBW Bulb.
This bulb supports color, color temperature, brightness.
It also has a number of preset programs, with static color, jumping color(s), gradient color(s) and flashing color(s).
With the program_speed the speed of some of the programs can be set.

Although it has not been tested, the Feican LED strips may also be supported as it seems they can be controlled using the same app.

### Limitations

It is not possible to get the state of the bulb from the bulb itself.
Therefore the state visible to the user only reflects what was set in openHAB and may not correspond with the actual state.

## Prerequisites

Before using the Feican bulb with openHAB the devices must be connected to the Wi-Fi network.
This can be done using the Feican Android or iPhone DreamColor app.

## Discovery

Devices can be auto discovered within the local network.
It is possible to connect to devices in a different network, but these must be added manually.

## Thing Configuration

The thing has one configuration parameter:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| ipAddress | IP Address of the device. Mandatory.                                     |

## Channels

The following channels are available:

| Channel Type ID   | Item Type | Description                                                                                |
|-------------------|-----------|--------------------------------------------------------------------------------------------|
| color             | Color     | This channel supports switching, brightness and adjusting the color of a light.            |
| color_temperature | Dimmer    | This channel supports adjusting the color temperature from cold (0%) to warm (100%).       |
| program           | String    | This channel supports setting the bulb to a static, jumping, gradient or flashing light.   |
| program_speed     | Dimmer    | This channel supports adjusting speed of jump, gradient or flash programs                  |

The program channel supports the following values:

| Value | Description         |
|-------|---------------------|
| 1     | Static red          |
| 2     | Static blue         |
| 3     | Static green        |
| 4     | Static cyan         |
| 5     | Static yellow       |
| 6     | Static purple       |
| 7     | Static white        |
| 8     | Tricolor jump       |
| 9     | 7-color jump        |
| 10    | Tricolor gradient   |
| 11    | 7-color gradient    |
| 12    | Red gradient        |
| 13    | Green gradient      |
| 14    | Blue gradient       |
| 15    | Yellow gradient     |
| 16    | Cyan gradient       |
| 17    | Purple gradient     |
| 18    | White gradient      |
| 19    | Red-Green gradient  |
| 20    | Red-Blue gradient   |
| 21    | Green-Blue gradient |
| 22    | 7-color flash       |
| 23    | Red flash           |
| 24    | Green flash         |
| 25    | Blue flash          |
| 26    | Yellow flash        |
| 27    | Cyan flash          |
| 28    | Purple flash        |
| 29    | White flash         |

## Full Example

**feican.things:**

```java
feican:bulb:home "Living Room" [ ipAddress="192.168.0.13" ]
```

**feican.items:**

```java
Switch   FC_1_Switch  "Switch"                    { channel="feican:bulb:home:color" }
Color    FC_1_Color   "Color"            <slider> { channel="feican:bulb:home:color" }
Dimmer   FC_1_Dimmer  "Brightness [%d]"  <slider> { channel="feican:bulb:home:color" }
```
