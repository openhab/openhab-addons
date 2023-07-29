# TRÅDFRI Binding

This binding integrates the IKEA TRÅDFRI gateway and devices connected to it (such as dimmable LED bulbs).
This binding only supports IKEA TRÅDFRI gateway v1.x, it is **not** compatible with DIRIGERA.

## Supported Things

Beside the gateway (thing type "gateway"), the binding currently supports colored bulbs, dimmable warm white bulbs as well as white spectrum bulbs, control outlets and blinds.
The binding also supports read-only data from remote controls and motion sensors (e.g. the battery status).
The TRÅDFRI controller and sensor devices currently cannot be observed right away because they are communicating directly with the bulbs or lamps without routing their commands through the gateway.
This makes it nearly impossible to trigger events for pressed buttons.
We only can access some static data like the present status or battery level.

The thing type ids are defined according to the lighting devices defined for Zigbee Light Link ([see page 24, table 2](https://www.nxp.com/docs/en/user-guide/JN-UG-3091.pdf)).
These are:

| Device type                     | Zigbee Device ID | Thing type |
|---------------------------------|------------------|------------|
| Dimmable Light                  | 0x0100           | 0100       |
| Colour Temperature Light        | 0x0220           | 0220       |
| Extended Colour Light           | 0x0210           | 0210       |
| Occupancy Sensor                | 0x0107           | 0107       |
| Non-Colour Controller           | 0x0820           | 0820       |
| Non-Colour Scene Controller     | 0x0830           | 0830       |
| Control Outlet                  | 0x0010           | 0010       |

The following matrix lists the capabilities (channels) for each of the supported lighting device types:

| Thing type  | Brightness | Color | Color Temperature | Battery Level | Battery Low | Power |
|-------------|:----------:|:-----:|:-----------------:|:-------------:|:-----------:|:-----:|
|  0010       |            |       |                   |               |             |   X   |
|  0100       |     X      |       |                   |               |             |       |
|  0220       |     X      |       |         X         |               |             |       |
|  0210       |            |   X   |         X         |               |             |       |
|  0107       |            |       |                   |       X       |      X      |       |
|  0820       |            |       |                   |       X       |      X      |       |
|  0830       |            |       |                   |       X       |      X      |       |

The following things are also supported even thought they are not standardized in Zigbee Light Link:

| Device type                     | Zigbee Device ID | Thing type |
|---------------------------------|------------------|------------|
| Window Covering Device          | 0x0202           | 0202       |
| Window Covering Controller      | 0x0203           | 0203       |
| Air Purifier                    | 0x0007           | 0007       |

The following matrix lists the capabilities (channels) for each of the supported non-lighting device types:

| Thing type  | Battery Level | Battery Low | Position | Fan Mode | Lock Button | Disabled LED | Air Quality | Fan Speed | Filter Check | Filter Uptime |
|-------------|:-------------:|:-----------:|:--------:|:--------:|:-----------:|:------------:|:-----------:|:---------:|:------------:|:-------------:|
|  0202       |       X       |      X      |     X    |          |             |              |             |           |              |               |
|  0203       |       X       |      X      |          |          |             |              |             |           |              |               |
|  0007       |               |             |          |    X     |      X      |      X       |      X      |     X     |       X      |       X       |

## Thing Configuration

For first pairing - the gateway requires a `host` parameter for the hostname or IP address and a `code`, which is the security code that is printed on the bottom of the gateway.
Optionally, a `port` can be configured, but any standard gateway uses the default port 5684.
The gateway requires at least firmware version 1.2.42 to connect to this binding.

The `code` is used during the initialization for retrieving unique identity and pre-shared key from the gateway and then it is discarded from the configuration.
The newly created authentication data is stored in advanced parameters `identity` and `preSharedKey`.
On each initialization if the code is present in the thing configuration - the `identity` and `preSharedKey` are recreated and the `code` is again discarded.

The devices require only a single (integer) parameter, which is their instance id. Unfortunately, this is not displayed anywhere in the IKEA app, but it seems that they are sequentially numbered starting with 65537 for the first device. If in doubt, use the auto-discovered things to find out the correct instance ids.

## Channels

The dimmable bulbs support the `brightness` channel.
The white spectrum bulbs additionally also support the `color_temperature` channel.

Full color bulbs support the `color_temperature` and `color` channels.
Brightness can be changed with the `color` channel.

The remote control and the motion sensor supports the `battery_level` and `battery_low` channels for reading the battery status.

The control outlet supports the `power` channel.

A blind or curtain supports, beside `battery_level` and `battery_low` channels,  a `positon` channel.

An air purifier supports:
* `fan_mode` and `fan_speed` channels, which allows for control of the fan and reading of the current speed.
* `disable_led` and `lock_button` channels, to respectively disable the LED's and lock the button on the physical device.
* `air_quality_pm25` and `air_quality_rating` channels, which reads the particulate matter 2.5μm and corresponding indication of air quality (similar to Tradfri app rating).
* `filter_check_next` and `filter_check_alarm` channels, which represents the remaining number of minutes until the next filter check and whether it is time to do the filter check now. Filter check must be completed through the TRÅDFRI app (or on the hardware buttons in case of replacement).
* a `filter_uptime` channel, which represents the current time since last filter change.

Refer to the matrixes above.

| Channel Type ID     | Item Type            | Description                                                                                  |
|---------------------|----------------------|----------------------------------------------------------------------------------------------|
| brightness          | Dimmer               | The brightness of the bulb in percent                                                        |
| color_temperature   | Dimmer               | Color temperature from 0% = cold to 100% = warm                                              |
| color               | Color                | Full color                                                                                   |
| battery_level       | Number               | Battery level (in %)                                                                         |
| battery_low         | Switch               | Battery low warning (<=10% = ON, >10% = OFF)                                                 |
| power               | Switch               | Power switch                                                                                 |
| position            | Rollershutter        | Position of the blinds from 0% = open to 100% = closed                                       |
| fan_mode            | Number               | Fan mode, target speed of the fan (0 = off, 1 = auto, 10..50 = Level 1 to 5)                 |
| fan_speed           | Number               | Current Fan Speed between 0 (off) and 50 (maximum speed)                                     |
| disable_led         | Switch               | Disables the LED's on the device                                                             |
| lock_button         | Switch               | Disables the physical button on the device (applications can still make changes)             |
| air_quality_pm25    | Number:Dimensionless | Density of Particulate Matter of 2.5μm, measured in ppm                                      |
| air_quality_rating  | Number               | Gives a rating about air quality (1 = Good, 2 = OK, 3 = Bad) similar to Tradfri app          |
| filter_check_next   | Number:Time          | Time in minutes before the next filter check if > 0, if < 0 you are late checking the filter |
| filter_check_alarm  | Switch               | When ON, you must perform a filter check (i.e. `filter_check_next` is < 0)                   |
| filter_uptime       | Number:Time          | Time elapsed since the last filter change, in minutes                                        |

## Full Example

demo.things:

```java
Bridge tradfri:gateway:mygateway [ host="192.168.0.177", code="EHPW5rIJKyXFgjH3" ] {
    0100 myDimmableBulb "My Dimmable Bulb" [ id=65537 ]    
    0220 myColorTempBulb "My Color Temp Bulb" [ id=65538 ]
    0210 myColorBulb "My Color Bulb" [ id=65539 ]
    0830 myRemoteControl "My Remote Control" [ id=65545 ]
    0010 myControlOutlet "My Control Outlet" [ id=65542 ]
    0202 myBlinds "My Blinds" [ id=65547 ]
    0007 myAirPurifier "My Air Purifier" [ id=65548 ]
}
```

demo.items:

```java
Dimmer Light1 { channel="tradfri:0100:mygateway:myDimmableBulb:brightness" }
Dimmer Light2_Brightness { channel="tradfri:0220:mygateway:myColorTempBulb:brightness" }
Dimmer Light2_ColorTemperature { channel="tradfri:0220:mygateway:myColorTempBulb:color_temperature" }
Color ColorLight { channel="tradfri:0210:mygateway:myColorBulb:color" }
Number RemoteControlBatteryLevel { channel="tradfri:0830:mygateway:myRemoteControl:battery_level" }
Switch RemoteControlBatteryLow { channel="tradfri:0830:mygateway:myRemoteControl:battery_low" }
Switch ControlOutlet { channel="tradfri:0010:mygateway:myControlOutlet:power" }
Rollershutter BlindPosition { channel="tradfri:0202:mygateway:myBlinds:position" }
Number AirPurifierFanMode { channel="tradfri:0007:mygateway:myAirPurifier:fan_mode" }
Number AirPurifierFanSpeed { channel="tradfri:0007:mygateway:myAirPurifier:fan_speed" }
Switch AirPurifierDisableLED { channel="tradfri:0007:mygateway:myAirPurifier:disable_led" }
Switch AirPurifierLockPhysicalButton { channel="tradfri:0007:mygateway:myAirPurifier:lock_button" }
Number AirPurifierQualityPM25 { channel="tradfri:0007:mygateway:myAirPurifier:air_quality_pm25" }
Number AirPurifierQualityRating { channel="tradfri:0007:mygateway:myAirPurifier:air_quality_rating" }
Number AirPurifierFilterCheckTTL { channel="tradfri:0007:mygateway:myAirPurifier:filter_check_next" }
Switch AirPurifierFilterCheckAlarm { channel="tradfri:0007:mygateway:myAirPurifier:filter_check_alarm" }
Number AirPurifierFilterUptime { channel="tradfri:0007:mygateway:myAirPurifier:filter_uptime" }
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Slider item=Light1 label="Light1 Brightness [%.1f %%]"
        Slider item=Light2_Brightness label="Light2 Brightness [%.1f %%]"
        Slider item=Light2_ColorTemperature label="Light2 Color Temperature [%.1f %%]"
        Colorpicker item=ColorLight label="Color"
        Text item=RemoteControlBatteryLevel label="Battery Level [%d %%]"
        Switch item=RemoteControlBatteryLow label="Battery Low Warning"
        Switch item=ControlOutlet label="Power Switch"
        Switch item=BlindPosition label="Blind Position [%d]"
        Selection item=AirPurifierFanMode label="Fan Mode"
        Text item=AirPurifierFanSpeed label="Current Fan Speed [%d]"
        Switch item=AirPurifierDisableLED label="Disable LEDs"
        Switch item=AirPurifierLockPhysicalButton label="Disable Physical Buttons"
        Text item=AirPurifierQualityPM25 label="PM2.5"
        Text item=AirPurifierQualityRating label="Air Quality"
        Text item=AirPurifierFilterCheckTTL label="TTL before next filter check [%d min]"
        Text item=AirPurifierFilterCheckAlarm label="Need to Check Filter [%s]"
        Text item=AirPurifierFilterUptime label="Current filter uptime [%d min]"
    }
}
```
