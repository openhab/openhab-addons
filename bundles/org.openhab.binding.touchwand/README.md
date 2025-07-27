# TouchWand Binding

Touchwand Wanderfull™ Hub basic is a plug & play Z-Wave based controller that uses Wi-Fi and Bluetooth to easily connect all smart home components.
TouchWand products are compatible with most major Z-Wave products, IP controlled devices and KNX devices, providing the ideal solution for building all-inclusive full-featured smart homes.
[TouchWand.com](https://www.touchwand.com)

![Touchwand Wanderfull™ Hub](https://www.touchwand.com/wp-content/uploads/2017/12/hub-toch-1.png)

## Supported Things

This binding supports switches, shutters dimmers alarm sensors and wall controllers configured in Touchwand Wanderfull™ Hub Controller.
The binding also supports [AcWand™](https://www.touchwand.com/products/touchwand-acwand/) - smart control for your air conditioner controller.

![AcWand](https://www.touchwand.com/wp-content/uploads/2019/04/AcWand-300x350.png)

## Control and Status

1. **switch**  - control - ON/OFF
1. **shutter** - control - UP/DOWN/STOP
1. **dimmer**  - control - ON/OFF/BRIGHTNESS
1. **wallcontroller** - control - LONG/SHORT
1. **alarmsensor** - status channels depend on alarm sensor type
1. **bsensor** - binary status channels depend on alarm sensor type (motion, door , smoke)
1. **thermostat** - AcWand™ smart control for your air conditioner

## Discovery

After adding TouchWand Hub the auto discovery will add all suppored devuces to inbox.
Auto discovery scans priodically and add to the Inbox new devices added to the Touchwand Wanderfull™ Hub

## Bridge Configuration

**Touchwand Wanderfull™** Hub Controller need to be added manually by IP address. The controller requires **username** and **password**

| Parameter         | Description                                                           | Units   | required |
|-------------------|-----------------------------------------------------------------------|---------|----------|
| username          | Touchwand hub username                                                | string  | yes      |
| password          | Touchwand hub password                                                | string  | yes      |
| ipAddress         | Touchwand hub hostname or IP address                                  | string  | yes      |
| port              | Management port (default 80)                                          | integer | no       |
| addSecondaryUnits | If the controller is primary, add secondary controllers units as well | bool    | no       |

## Thing Configuration

No thing configuration is needed

## Channels

note **Touchwand Wanderfull™** supports various types of alarm sensors such as water leak, door/window sensor and motion sensor.
Alarm Sensor thing represents a generic sensor, relevant sensor channels will be displayed once a sensor is added as a Thing.

## Switch Shutters Channels

| Channel Type ID   | Item Type          | Description                                                           |
|-------------------|--------------------|-----------------------------------------------------------------------|
| switch            | Switch             | This channel supports switching the device on and off.                |
| shutter           | Rollershutter      | This channel controls the shutter position                            |
| brightness        | Dimmer             | This channel supports adjusting the brightness value.                 |
| wallaction        | String             | This channel indicate SHORT or LONG wallcontroller button pressed     |

## Alarm Sensors Channels

| Channel Type ID   | Item Type          | Description                                                           |
|-------------------|--------------------|-----------------------------------------------------------------------|
| illumination      | Number:Illuminance | This channel shows the current illuminance measured by the sensor.    |
| temperature       | Number:Temperature | This channel shows the current temperature measured by the sensor.    |
| leak              | Switch             | This channel alert when water leak is detected by the sensor          |
| motion            | Switch             | This channel alert when motion detected by the sensor.                |
| smoke             | Switch             | This channel alert when smoke detected by the sensor.                 |
| isOpen            | Contact            | This channel shows the status of Door/Window sensor.                  |
| battery_level     | Number             | This channel shows the battery level.                                 |
| battery_low       | Switch             | This channel indicates whether the battery is low or not.             |

## Thermostat Channels

| Channel Type ID   | Item Type          | Description                                                           |
|-------------------|--------------------|-----------------------------------------------------------------------|
| State             | Switch             | Set and read the device state ON or OFF.                              |
| targetTemperature | Number:Temperature | Shows the current set point of the thrermostat.                       |
| roomTemperature   | Number:Temperature | Shows the current termprature measured by the thermostat.             |
| mode              | String             | Set/Read Thermostat mode - Cool, Heat, Fan, Dry, Auto                 |
| fanLevel          | String             | Set/Read fan leval - Low, Medium, High, Auto                          |

## Full Example

### touchwand.things

Things can be defined manually
The syntax for touchwand this is

```java
Thing <binding_id>:<type_id>:<thing_id> "Label" @ "Location"
```

Where <thing_id> is the unit id in touchwand hub.

```java
Bridge touchwand:bridge:1921681116 [ipAddress="192.168.1.116", username="username" , password="password"]{
Thing switch 408 "Strairs light"
Thing switch 411 "South Garden light"
Thing dimmer 415 "Living Room Ceiling dimmer"
Thing switch 418 "Kitchen light"
Thing shutter 345 "Living Room North shutter"
Thing shutter 346 "Living Room South shutter"
}
```

### touchwand.items

```java
/* Shutters */
Rollershutter   Rollershutter_345      "Living Room North shutter"    {channel="touchwand:shutter:1921681116:345:shutter"}
Rollershutter   Rollershutter_346      "Living Room South shutter"    {channel="touchwand:shutter:1921681116:346:shutter"}
```

```java
/* Switches and Dimmers */
Switch  Switch_408      "Stairs light"                  {channel="touchwand:switch:1921681116:408:switch"}
Switch  Switch_411      "South Garden light"            {channel="touchwand:switch:1921681116:411:switch"}
Dimmer  Switch_415      "Living Room Ceiling dimmer"    {channel="touchwand:switch:1921681116:415:switch"}
Switch  Switch_418      "South Garden light"            {channel="touchwand:switch:1921681116:418:switch"}
```
