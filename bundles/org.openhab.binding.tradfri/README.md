# TRÅDFRI Binding

This binding integrates the IKEA TRÅDFRI gateway and devices connected to it (such as dimmable LED bulbs).

## Supported Things

Beside the gateway (thing type "gateway"), the binding currently supports colored bulbs, dimmable warm white bulbs as well as white spectrum bulbs, control outlets and blinds.
The binding also supports read-only data from remote controls and motion sensors (e.g. the battery status).
The TRÅDFRI controller and sensor devices currently cannot be observed right away because they are communicating directly with the bulbs or lamps without routing their commands through the gateway.
This makes it nearly impossible to trigger events for pressed buttons.
We only can access some static data like the present status or battery level.

The thing type ids are defined according to the lighting devices defined for ZigBee LightLink ([see page 24, table 2](https://www.nxp.com/docs/en/user-guide/JN-UG-3091.pdf)).
These are:

| Device type                     | ZigBee Device ID | Thing type |
|---------------------------------|------------------|------------|
| Dimmable Light                  | 0x0100           | 0100       |
| Colour Temperature Light        | 0x0220           | 0220       |
| Extended Colour Light           | 0x0210           | 0210       |
| Occupancy Sensor                | 0x0107           | 0107       |
| Non-Colour Controller           | 0x0820           | 0820       |
| Non-Colour Scene Controller     | 0x0830           | 0830       |
| Control Outlet                  | 0x0010           | 0010       |
| Window Covering Device          | 0x0202           | 0202       |
| Window Covering Controller      | 0x0202           | 0203       |

The following matrix lists the capabilities (channels) for each of the supported lighting device types:

| Thing type  | Brightness | Color | Color Temperature | Battery Level | Battery Low | Power | Position |
|-------------|:----------:|:-----:|:-----------------:|:-------------:|:-----------:|:-----:|:---------|
|  0010       |            |       |                   |               |             |   X   |          |
|  0100       |     X      |       |                   |               |             |       |          |
|  0220       |     X      |       |         X         |               |             |       |          |
|  0210       |            |   X   |         X         |               |             |       |          |
|  0107       |            |       |                   |       X       |      X      |       |          |
|  0820       |            |       |                   |       X       |      X      |       |          |
|  0830       |            |       |                   |       X       |      X      |       |          |
|  0202       |            |       |                   |       X       |      X      |       |     X    |
|  0203       |            |       |                   |       X       |      X      |       |          |

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

Refer to the matrix above.

| Channel Type ID   | Item Type     | Description                                            |
|-------------------|---------------|--------------------------------------------------------|
| brightness        | Dimmer        | The brightness of the bulb in percent                  |
| color_temperature | Dimmer        | color temperature from 0% = cold to 100% = warm        |
| color             | Color         | full color                                             |
| battery_level     | Number        | battery level (in %)                                   |
| battery_low       | Switch        | battery low warning (<=10% = ON, >10% = OFF)           |
| power             | Switch        | power switch                                           |
| position          | Rollershutter | position of the blinds from 0% = open to 100% = closed |

## Full Example

demo.things:

```
Bridge tradfri:gateway:mygateway [ host="192.168.0.177", code="EHPW5rIJKyXFgjH3" ] {
    0100 myDimmableBulb "My Dimmable Bulb" [ id=65537 ]    
    0220 myColorTempBulb "My Color Temp Bulb" [ id=65538 ]
    0210 myColorBulb "My Color Bulb" [ id=65539 ]
    0830 myRemoteControl "My Remote Control" [ id=65545 ]
    0010 myControlOutlet "My Control Outlet" [ id=65542 ]
    0202 myBlinds "My Blinds" [ id=65547 ]
}
```

demo.items:

```
Dimmer Light1 { channel="tradfri:0100:mygateway:myDimmableBulb:brightness" }
Dimmer Light2_Brightness { channel="tradfri:0220:mygateway:myColorTempBulb:brightness" }
Dimmer Light2_ColorTemperature { channel="tradfri:0220:mygateway:myColorTempBulb:color_temperature" }
Color ColorLight { channel="tradfri:0210:mygateway:myColorBulb:color" }
Number RemoteControlBatteryLevel { channel="tradfri:0830:mygateway:myRemoteControl:battery_level" }
Switch RemoteControlBatteryLow { channel="tradfri:0830:mygateway:myRemoteControl:battery_low" }
Switch ControlOutlet { channel="tradfri:0010:mygateway:myControlOutlet:power" }
Rollershutter BlindPosition { channel="tradfri:0202:mygateway:myBlinds:position" }
```

demo.sitemap:

```
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
    }
}
```
