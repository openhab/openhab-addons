# AM43

This extension adds support for [AM43 Blind Drive Motors](https://www.a-okmotors.com/am-43/).

## Supported Things

Following thing types are supported by this extension:

| Thing Type ID | Description                   |
|---------------|-------------------------------|
| am43          | AM43 Blind Drive Motor        |

## Discovery

As any other Bluetooth device, AM43 Blind Drive Motors are discovered automatically by the corresponding bridge.

## Thing Configuration

Supported configuration parameters `AM43 Blind Drive Motor` thing:

| Property                        | Type    | Default | Required | Description                                                              |
|---------------------------------|---------|---------|----------|--------------------------------------------------------------------------|
| address                         | String  |         | Yes      | Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX")          |
| refreshInterval                 | Integer | 60      | No       | How often a refresh shall occur in seconds                               |
| invertPosition                  | Boolean | false   | No       | Inverts the blinds percentages such that 0 becomes 100 and 100 becomes 0 |

## Channels

Following channels are supported for `AM43 Blind Drive Motor` thing:

| Channel ID     | Item Type            | Description                                                                               |
|----------------|----------------------|-------------------------------------------------------------------------------------------|
| direction      | String               | The direction of the motor for UP/DOWN controls. Is either 'Forward' or 'Reverse'         |
| topLimitSet    | Switch               | Whether or not the top limit of the blinds has been set                                   |
| bottomLimitSet | Switch               | Whether or not the bottom limit of the blinds has been set                                |
| hasLightSensor | Switch               | Whether or not the solar sensor was detected                                              |
| operationMode  | String               | Controls behavior of motor on manual button presses. Is either 'Inching' or 'Continuous'  |
| position       | Rollershutter        | Main rollershutter controls                                                               |
| speed          | Number:Dimensionless | The speed, in RPMs, that the motor will move the blinds                                   |
| length         | Number:Length        | The length of the blinds in millimeters. (Mostly useless)                                 |
| diameter       | Number:Length        | The diameter of the motor pulley. (Mostly useless)                                        |
| type           | Number:Dimensionless | The type of blinds that the motor is connected to. (Mostly useless)                       |
| lightLevel     | Number:Dimensionless | The light level detected by the solar sensor. Will range from 0-10                        |
| electric       | Number:Dimensionless | The current percent charge of the motor's battery                                         |

## Example

am43.things (assuming you have a Bluetooth bridge with the ID `bluetooth:bluegiga:adapter1`:

```java
bluetooth:am43:adapter1:motor1  "AM43 Blind Drive Motor 1" (bluetooth:bluegiga:adapter1) [ address="12:34:56:78:9A:BC", refreshInterval=300, invertPosition=false ]
```

am43.items:

```java
String                  direction       "Direction [%s]"            { channel="bluetooth:am43:adapter1:motor1:direction" }
Switch                  topLimitSet     "Top Limit Set"             { channel="bluetooth:am43:adapter1:motor1:topLimitSet" }
Switch                  bottomLimitSet  "Bottom Limit Set"          { channel="bluetooth:am43:adapter1:motor1:bottomLimitSet" }
Switch                  hasLightSensor  "Has Light Sensor"          { channel="bluetooth:am43:adapter1:motor1:hasLightSensor" }
String                  operationMode   "Operation Mode [%s]"       { channel="bluetooth:am43:adapter1:motor1:operationMode" }
Rollershutter           position        "Position [%.0f %%]"        { channel="bluetooth:am43:adapter1:motor1:position" }
Number:Dimensionless    speed           "Speed [%.0f RPM]"          { channel="bluetooth:am43:adapter1:motor1:speed" }
Number:Length           length          "Length [%.0f %unit%]"      { channel="bluetooth:am43:adapter1:motor1:length" }
Number:Length           diameter        "Diameter [%.0f %unit%]"    { channel="bluetooth:am43:adapter1:motor1:diameter" }
Number:Dimensionless    type            "Type [%.0f]"               { channel="bluetooth:am43:adapter1:motor1:type" }
Number:Dimensionless    light_level     "Light Level [%.0f]"        { channel="bluetooth:am43:adapter1:motor1:lightLevel" }
Number:Dimensionless    battery_level   "Battery Level [%.0f %%]"   { channel="bluetooth:am43:adapter1:motor1:electric" }
```
