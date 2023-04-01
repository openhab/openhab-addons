# Philips Hue Binding Configuration for API v1

[Back to Overview](../README.md#philips-hue-binding)

## Supported Things

Almost all available Hue devices are supported by this binding.
This includes not only the "Friends of Hue", but also products like the LivingWhites adapter.
Additionally, it is possible to use OSRAM Lightify devices as well as other Zigbee Light Link compatible products, including the IKEA TRÅDFRI lights (when updated).
Beside bulbs and luminaires the Hue binding also supports some Zigbee sensors. Currently only Hue specific sensors are tested successfully (Hue Motion Sensor and Hue Dimmer Switch).
Please note that the devices need to be registered with the Hue Bridge before it is possible for this binding to use them.

The Hue binding supports all seven types of lighting devices defined for Zigbee Light Link ([see page 24, table 2](https://www.nxp.com/docs/en/user-guide/JN-UG-3091.pdf).
These are:

| Device type              | Zigbee Device ID | Thing type |
|--------------------------|------------------|------------|
| On/Off Light             | 0x0000           | 0000       |
| On/Off Plug-in Unit      | 0x0010           | 0010       |
| Dimmable Light           | 0x0100           | 0100       |
| Dimmable Plug-in Unit    | 0x0110           | 0110       |
| Colour Light             | 0x0200           | 0200       |
| Extended Colour Light    | 0x0210           | 0210       |
| Colour Temperature Light | 0x0220           | 0220       |

All different models of Hue, OSRAM, or other bulbs nicely fit into one of these seven types.
This type also determines the capability of a device and with that the possible ways of interacting with it.
The following matrix lists the capabilities (channels) for each type:

| Thing type  | On/Off | Brightness | Color | Color Temperature |
|-------------|:------:|:----------:|:-----:|:-----------------:|
|  0000       |    X   |            |       |                   |
|  0010       |    X   |            |       |                   |
|  0100       |    X   |     X      |       |                   |
|  0110       |    X   |     X      |       |                   |
|  0200       |    X   |            |   X   |                   |
|  0210       |    X   |            |   X   |          X        |
|  0220       |    X   |     X      |       |          X        |

Beside bulbs and luminaires the Hue binding supports some Zigbee sensors.
Currently only Hue specific sensors are tested successfully (e.g. Hue Motion Sensor, Hue Dimmer Switch, Hue Tap, CLIP Sensor).
The Hue Motion Sensor registers a `ZLLLightLevel` sensor (0106), a `ZLLPresence` sensor (0107) and a `ZLLTemperature` sensor (0302) in one device.
The Hue CLIP Sensor saves scene states with status or flag for HUE rules.
They are presented by the following Zigbee Device ID and _Thing type_:

| Device type                 | Zigbee Device ID | Thing type     |
|-----------------------------|------------------|----------------|
| Light Sensor                | 0x0106           | 0106           |
| Occupancy Sensor            | 0x0107           | 0107           |
| Temperature Sensor          | 0x0302           | 0302           |
| Non-Colour Controller       | 0x0820           | 0820           |
| Non-Colour Scene Controller | 0x0830           | 0830           |
| CLIP Generic Status Sensor  | 0x0840           | 0840           |
| CLIP Generic Flag Sensor    | 0x0850           | 0850           |
| Geofence Sensor             |                  | geofencesensor |

The Hue Dimmer Switch has 4 buttons and registers as a Non-Colour Controller switch, while the Hue Tap (also 4 buttons) registers as a Non-Colour Scene Controller in accordance with the ZLL standard.

Also, Hue Bridge support CLIP Generic Status Sensor and CLIP Generic Flag Sensor.
These sensors save state for rules and calculate what actions to do.
CLIP Sensor set or get by JSON through IP.

Finally, the Hue binding also supports the groups of lights and rooms set up on the Hue Bridge.

## Thing Configuration

The Hue Bridge requires the IP address as a configuration value in order for the binding to know where to access it.
In the thing file, this looks e.g. like

```java
Bridge hue:bridge:1 [ ipAddress="192.168.0.64" ]
```

A user to authenticate against the Hue Bridge is automatically generated.
Please note that the generated user name cannot be written automatically to the `.things` file, and has to be set manually.
The generated user name can be found, after pressing the authentication button on the bridge, with the following console command: `hue <bridgeUID> username`.
The user name can be set using the `userName` configuration value, e.g.:

```java
Bridge hue:bridge:1 [ ipAddress="192.168.0.64", userName="qwertzuiopasdfghjklyxcvbnm1234" ]
```

| Parameter                | Description                                                                                                                                                                                                                                                                                                                   |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ipAddress                | Network address of the Hue Bridge. **Mandatory**.                                                                                                                                                                                                                                                                             |
| port                     | Port of the Hue Bridge. Optional, default value is 80 or 443, derived from protocol, otherwise user-defined.                                                                                                                                                                                                                  |
| protocol                 | Protocol to connect to the Hue Bridge ("http" or "https"), default value is "https").                                                                                                                                                                                                                                         |
| useSelfSignedCertificate | Use self-signed certificate for HTTPS connection to Hue Bridge. **Advanced**, default value is `true`.                                                                                                                                                                                                                        |
| userName                 | Name of a registered Hue Bridge user, that allows to access the API. **Mandatory**                                                                                                                                                                                                                                            |
| pollingInterval          | Seconds between fetching light values from the Hue Bridge. Optional, the default value is 10 (min="1", step="1").                                                                                                                                                                                                             |
| sensorPollingInterval    | Milliseconds between fetching sensor-values from the Hue Bridge. A higher value means more delay for the sensor values, but a too low value can cause congestion on the bridge. Optional, the default value is 500. Default value will be considered if the value is lower than 50. Use 0 to disable the polling for sensors. |

### Devices

The devices are identified by the number that the Hue Bridge assigns to them (also shown in the Hue App as an identifier).
Thus, all it needs for manual configuration is this single value like

```java
0210 bulb1 "Lamp 1" @ "Office" [ lightId="1" ]
```

or

```java
0107 motion-sensor "Motion Sensor" @ "Entrance" [ sensorId="4" ]
```

You can freely choose the thing identifier (such as motion-sensor), its name (such as "Motion Sensor") and the location (such as "Entrance").

The following device types also have an optional configuration value to specify the fade time in milliseconds for the transition to a new state:

- Dimmable Light
- Dimmable Plug-in Unit
- Colour Light
- Extended Colour Light
- Colour Temperature Light

| Parameter | Description                                                                   |
|-----------|-------------------------------------------------------------------------------|
| lightId   | Number of the device provided by the Hue Bridge. **Mandatory**                |
| fadetime  | Fade time in Milliseconds to a new state (min="0", step="100", default="400") |


### Groups

The groups are identified by the number that the Hue Bridge assigns to them.
Thus, all it needs for manual configuration is this single value like

```java
group kitchen-bulbs "Kitchen Lamps" @ "Kitchen" [ groupId="1" ]
```

You can freely choose the thing identifier (such as kitchen-bulbs), its name (such as "Kitchen Lamps") and the location (such as "Kitchen").

The group type also have an optional configuration value to specify the fade time in milliseconds for the transition to a new state.

| Parameter | Description                                                                   |
|-----------|-------------------------------------------------------------------------------|
| groupId   | Number of the group provided by the Hue Bridge. **Mandatory**                 |
| fadetime  | Fade time in Milliseconds to a new state (min="0", step="100", default="400") |


## Channels

The devices support some of the following channels:

| Channel Type ID       | Item Type          | Description                                                                                                                             | Thing types supporting this channel      |
|-----------------------|--------------------|-----------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|
| switch                | Switch             | This channel supports switching the device on and off.                                                                                  | 0000, 0010, group                        |
| color                 | Color              | This channel supports full color control with hue, saturation and brightness values.                                                    | 0200, 0210, group                        |
| brightness            | Dimmer             | This channel supports adjusting the brightness value. Note that this is not available, if the color channel is supported.               | 0100, 0110, 0220, group                  |
| color_temperature     | Dimmer             | This channel supports adjusting the color temperature from cold (0%) to warm (100%).                                                    | 0210, 0220, group                        |
| color_temperature_abs | Number:Temperature | This channel supports adjusting the color temperature in Kelvin. 
**Advanced**                                                           | 0210, 0220, group                        |
| alert                 | String             | This channel supports displaying alerts by flashing the bulb either once or multiple times. Valid values are: NONE, SELECT and LSELECT. | 0000, 0100, 0200, 0210, 0220, group      |
| effect                | Switch             | This channel supports color looping.                                                                                                    | 0200, 0210, 0220                         |
| dimmer_switch         | Number             | This channel shows which button was last pressed on the dimmer switch.                                                                  | 0820                                     |
| illuminance           | Number:Illuminance | This channel shows the current illuminance measured by the sensor.                                                                      | 0106                                     |
| light_level           | Number             | This channel shows the current light level measured by the sensor. **Advanced**                                                         | 0106                                     |
| dark                  | Switch             | This channel indicates whether the light level is below the darkness threshold or not.                                                  | 0106                                     |
| daylight              | Switch             | This channel indicates whether the light level is below the daylight threshold or not.                                                  | 0106                                     |
| presence              | Switch             | This channel indicates whether a motion is detected by the sensor or not.                                                               | 0107                                     |
| enabled               | Switch             | This channel activated or deactivates the sensor                                                                                        | 0107                                     |
| temperature           | Number:Temperature | This channel shows the current temperature measured by the sensor.                                                                      | 0302                                     |
| flag                  | Switch             | This channel save flag state for a CLIP sensor.                                                                                         | 0850                                     |
| status                | Number             | This channel save status state for a CLIP sensor.                                                                                       | 0840                                     |
| last_updated          | DateTime           | This channel the date and time when the sensor was last updated.                                                                        | 0820, 0830, 0840, 0850, 0106, 0107, 0302 |
| battery_level         | Number             | This channel shows the battery level.                                                                                                   | 0820, 0106, 0107, 0302                   |
| battery_low           | Switch             | This channel indicates whether the battery is low or not.                                                                               | 0820, 0106, 0107, 0302                   |
| scene                 | String             | This channel activates the scene with the given ID String. The ID String of each scene is assigned by the Hue Bridge.                   | bridge, group                            |

To load a hue scene inside a rule for example, the ID of the scene will be required.
You can list all the scene IDs with the following console commands: `hue <bridgeUID> scenes` and `hue <groupThingUID> scenes`.

### Trigger Channels

The dimmer switch additionally supports a trigger channel.

| Channel ID          | Description                      | Thing types supporting this channel |
|---------------------|----------------------------------|-------------------------------------|
| dimmer_switch_event | Event for dimmer switch pressed. | 0820                                |
| tap_switch_event    | Event for tap switch pressed.    | 0830                                |

The `dimmer_switch_event` can trigger one of the following events:

| Button              | State           | Event |
|---------------------|-----------------|-------|
| Button 1 (ON)       | INITIAL_PRESSED | 1000  |
|                     | HOLD            | 1001  |
|                     | SHORT RELEASED  | 1002  |
|                     | LONG RELEASED   | 1003  |
| Button 2 (DIM UP)   | INITIAL_PRESSED | 2000  |
|                     | HOLD            | 2001  |
|                     | SHORT RELEASED  | 2002  |
|                     | LONG RELEASED   | 2003  |
| Button 3 (DIM DOWN) | INITIAL_PRESSED | 3000  |
|                     | HOLD            | 3001  |
|                     | SHORT RELEASED  | 3002  |
|                     | LONG RELEASED   | 3003  |
| Button 4 (OFF)      | INITIAL_PRESSED | 4000  |
|                     | HOLD            | 4001  |
|                     | SHORT RELEASED  | 4002  |
|                     | LONG RELEASED   | 4003  |

The `tap_switch_event` can trigger one of the following events:

| Button   | State    | Event |
|----------|----------|-------|
| Button 1 | Button 1 | 34    |
| Button 2 | Button 2 | 16    |
| Button 3 | Button 3 | 17    |
| Button 4 | Button 4 | 18    |


## Rule Actions

This binding includes a rule action, which allows to change a light channel with a specific fading time from within rules.
There is a separate instance for each light or light group, which can be retrieved e.g. through

```php
val hueActions = getActions("hue","hue:0210:00178810d0dc:1")
```

where the first parameter always has to be `hue` and the second is the full Thing UID of the light that should be used.
Once this action instance is retrieved, you can invoke the `fadingLightCommand(String channel, Command command, DecimalType fadeTime)` method on it:

```php
hueActions.fadingLightCommand("color", new PercentType(100), new DecimalType(1000))
```

| Parameter | Description                                                                                      |
|-----------|--------------------------------------------------------------------------------------------------|
| channel   | The following channels have fade time support: **brightness, color, color_temperature, switch**  |
| command   | All commands supported by the channel can be used                                                |
| fadeTime  | Fade time in milliseconds to a new light value (min="0", step="100")                             |

## Full Example

In this example **bulb1** is a standard Philips Hue bulb (LCT001) which supports `color` and `color_temperature`.
Therefore it is a thing of type **0210**.
**bulb2** is an OSRAM tunable white bulb (PAR16 50 TW) supporting `color_temperature` and so the type is **0220**.
And there is one Hue Motion Sensor (represented by three devices) and a Hue Dimmer Switch **dimmer-switch** with a Rule to trigger an action when a key has been pressed.

### demo.things:

```java
Bridge hue:bridge:1         "Hue Bridge"                    [ ipAddress="192.168.0.64" ] {
    0210  bulb1              "Lamp 1"        @ "Kitchen"    [ lightId="1" ]
    0220  bulb2              "Lamp 2"        @ "Kitchen"    [ lightId="2" ]
    group kitchen-bulbs      "Kitchen Lamps" @ "Kitchen"    [ groupId="1" ]
    0106  light-level-sensor "Light-Sensor"  @ "Entrance"   [ sensorId="3" ]
    0107  motion-sensor      "Motion-Sensor" @ "Entrance"   [ sensorId="4" ]
    0302  temperature-sensor "Temp-Sensor"   @ "Entrance"   [ sensorId="5" ]
    0820  dimmer-switch      "Dimmer-Switch" @ "Entrance"   [ sensorId="6" ]
}
```

### demo.items:

```java
// Bulb1
Switch  Light1_Toggle       { channel="hue:0210:1:bulb1:color" }
Dimmer  Light1_Dimmer       { channel="hue:0210:1:bulb1:color" }
Color   Light1_Color        { channel="hue:0210:1:bulb1:color" }
Dimmer  Light1_ColorTemp    { channel="hue:0210:1:bulb1:color_temperature" }
String  Light1_Alert        { channel="hue:0210:1:bulb1:alert" }
Switch  Light1_Effect       { channel="hue:0210:1:bulb1:effect" }

// Bulb2
Switch  Light2_Toggle       { channel="hue:0220:1:bulb2:brightness" }
Dimmer  Light2_Dimmer       { channel="hue:0220:1:bulb2:brightness" }
Dimmer  Light2_ColorTemp    { channel="hue:0220:1:bulb2:color_temperature" }

// Kitchen
Switch  Kitchen_Switch      { channel="hue:group:1:kitchen-bulbs:switch" }
Dimmer  Kitchen_Dimmer      { channel="hue:group:1:kitchen-bulbs:brightness" }
Color   Kitchen_Color       { channel="hue:group:1:kitchen-bulbs:color" }
Dimmer  Kitchen_ColorTemp   { channel="hue:group:1:kitchen-bulbs:color_temperature" }

// Light Level Sensor
Number:Illuminance LightLevelSensorIlluminance { channel="hue:0106:1:light-level-sensor:illuminance" }

// Motion Sensor
Switch   MotionSensorPresence     { channel="hue:0107:1:motion-sensor:presence" }
DateTime MotionSensorLastUpdate   { channel="hue:0107:1:motion-sensor:last_updated" }
Number   MotionSensorBatteryLevel { channel="hue:0107:1:motion-sensor:battery_level" }
Switch   MotionSensorLowBattery   { channel="hue:0107:1:motion-sensor:battery_low" }

// Temperature Sensor
Number:Temperature TemperatureSensorTemperature { channel="hue:0302:1:temperature-sensor:temperature" }

// Scenes
String LightScene { channel="hue:bridge:1:scene"}
```

Note: The bridge ID is in this example **1** but can be different in each system.
Also, if you are doing all your configuration through files, you may add the full bridge id to the channel definitions (e.g. `channel="hue:0210:00178810d0dc:bulb1:color`) instead of the short version (e.g. `channel="hue:0210:1:bulb1:color`) to prevent frequent discovery messages in the log file.

### demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        // Bulb1
        Switch      item=       Light1_Toggle
        Slider      item=       Light1_Dimmer
        Colorpicker item=       Light1_Color
        Slider      item=       Light1_ColorTemp
        Switch      item=       Light1_Alert        mappings=[NONE="None", SELECT="Alert", LSELECT="Long Alert"]
        Switch      item=       Light1_Effect

        // Bulb2
        Switch      item=       Light2_Toggle
        Slider      item=       Light2_Dimmer
        Slider      item=       Light2_ColorTemp

        // Kitchen
        Switch      item=       Kitchen_Switch
        Slider      item=       Kitchen_Dimmer
        Colorpicker item=       Kitchen_Color
        Slider      item=       Kitchen_ColorTemp

        // Motion Sensor
        Switch item=MotionSensorPresence
        Text item=MotionSensorLastUpdate
        Text item=MotionSensorBatteryLevel
        Switch item=MotionSensorLowBattery

        // Light Scenes
        Default item=LightScene label="Scene []"
    }
}
```

### Events

 ```php
rule "example trigger rule"
when
    Channel "hue:0820:1:dimmer-switch:dimmer_switch_event" triggered <EVENT>
then
    ...
end
```

The optional `<EVENT>` represents one of the button events that are generated by the Hue Dimmer Switch.
If ommited the rule gets triggered by any key action and you can determine the event that triggered it with the `receivedEvent` method.
Be aware that the events have a '.0' attached to them, like `2001.0` or `34.0`.
So, testing for specific events looks like this:

```php
if (receivedEvent == "1000.0") {
    //do stuff
}
```

[Back to Overview](../README.md#philips-hue-binding)
