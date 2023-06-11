# TP-Link Smart Home Binding

This binding adds support to control TP-Link Smart Home Devices from your local openHAB system.

## Supported Things

The following TP-Link Smart Devices are supported:

### EP10 Kasa Smart Wi-Fi Plug Mini

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### EP40 Kasa Smart Wi-Fi Outdoor Plug

- Power On/Off Group
- Power On/Off 2 Outlets
- LED On/Off
- Wi-Fi signal strength (RSSI)

## ES20M Smart Wi-Fi Light Switch, Motion-Activated

- Power On/Off
- Dimmer
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS100 Kasa Smart Wi-Fi Plug

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS103 Kasa Smart Wi-Fi Plug Lite

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS105 Kasa Smart Wi-Fi Plug

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS107 Kasa Smart Wi-Fi Plug, 2-Outlets

- Power On/Off Group
- Power On/Off 2 Outlets
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS110 Kasa Smart Wi-Fi Plug

- Power On/Off
- Energy readings
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS200 Kasa Smart Wi-Fi Switch

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS210 Kasa Smart Wi-Fi Light Switch 3-Way Kit

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### HS220 Kasa Smart Wi-Fi Light Switch, Dimmer

- Power On/Off
- Adjust the brightness
- LED On/Off
- Wi-Fi signal strength (RSSI)

Use the brightness channel on the HS220 with a Switch item can be used to switch the device on and off.
It will not change the brightness value.

The default refresh for switch devices is set to 1 second. So it polls the switch for status changes.
If you don't use the switch manually often, you can set it to a higher refresh.
The refresh is only relevant to detect manual using the switch.
Switching via openHAB activates the switch directly.

### HS300 Kasa Smart Wi-Fi Power Strip

- Power On/Off Group
- Power On/Off 6 Outlets
- Energy readings 6 Outlets
- LED On/Off
- Wi-Fi signal strength (RSSI)

### LB100 Kasa Smart Wi-Fi LED Bulb with Dimmable Light

- Power On/Off
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### LB110 Kasa Smart Wi-Fi LED Bulb with Dimmable Light

- Power On/Off
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### LB120 Kasa Smart Wi-Fi LED Bulb with Tunable White Light

- Power On/Off
- Adjust light appearance from soft white (2700k) to daylight (6500k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### LB130 Kasa Smart Wi-Fi LED Bulb with Color Changing Hue

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (9000k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching, Brightness and Color is done using the `color` channel.

### LB200 Kasa Smart Wi-Fi LED Bulb with Dimmable Light

- Power On/Off
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### LB230 Kasa Smart Wi-Fi LED Bulb with Color Changing Hue

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (9000k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching, Brightness and Color is done using the `color` channel.

### KB100 Kasa Smart Light Bulb

- Power On/Off
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### KB130 Kasa Multi-color Smart Light Bulb

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (9000k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching, Brightness and Color is done using the `color` channel.

### KL50 Kasa Filament Smart Bulb, Soft White

- Power On/Off
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### KL60 Kasa Filament Smart Bulb, Warm Amber

- Power On/Off
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### KL110 Kasa Smart Wi-Fi LED Bulb with Dimmable Light

- Power On/Off
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### KL120 Kasa Smart Wi-Fi LED Bulb with Tunable White Light

- Power On/Off
- Adjust light appearance from soft white (2700k) to daylight (6500k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching and Brightness is done using the `brightness` channel.

### KL125 Kasa Smart Wi-Fi Bulb Multicolor

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (6500k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching, Brightness and Color is done using the `color` channel.

### KL130 Kasa Smart Wi-Fi LED Bulb with Color Changing Hue

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (9000k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching, Brightness and Color is done using the `color` channel.

### KL135 Kasa Smart Wi-Fi Bulb Multicolor

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (6500k)
- Adjust the brightness
- Actual power usage
- Wi-Fi signal strength (RSSI)

Switching, Brightness and Color is done using the `color` channel.

### KL400 Kasa Smart LED Light Strip

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (9000k)
- Adjust the brightness
- Wi-Fi signal strength (RSSI)

### KL430 Kasa Smart LED Light Strip, 16 Color Zones

- Power On/Off
- Fine-tune colors
- Adjust light appearance from soft white (2500k) to daylight (9000k)
- Adjust the brightness
- Wi-Fi signal strength (RSSI)

Switching, Brightness and Color is done using the `color` channel.

### KP100 Kasa Wi-Fi Smart Plug - Slim Edition

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP105 Kasa Wi-Fi Smart Plug - Slim Edition

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP115 Kasa Wi-Fi Smart Plug with Energy Monitoring - Slim Edition

- Power On/Off
- Energy readings
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP125 Kasa Smart WiFi Plug Slim with Energy Monitoring

- Power On/Off
- Energy readings
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP200 Kasa Smart Wi-Fi Power Outlet, 2-Sockets

- Power On/Off Group
- Power On/Off 2 Outlets
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP303 Kasa Smart Wi-Fi Power Outlet, 3-Sockets

- Power On/Off Group
- Power On/Off 3 Outlets
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP400 Kasa Smart Outdoor Plug

- Power On/Off Group
- Power On/Off 2 Outlets
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP401 Kasa Smart WiFi Outdoor Plug

- Power On/Off
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KP405 Kasa Smart Wi-Fi Outdoor Plug-In Dimmer

- Power On/Off
- Dimmer
- LED On/Off
- Wi-Fi signal strength (RSSI)

### KS230 Kasa Smart Wi-Fi Dimmer Switch 3-Way Kit

- Power On/Off
- Adjust the brightness
- LED On/Off
- Wi-Fi signal strength (RSSI)

### RE270K AC750 Wi-Fi Range Extender with Smart Plug

- Power On/Off (readonly)
- Wi-Fi signal strength (RSSI)

### RE370K AC1200 Wi-Fi Range Extender with Smart Plug

- Power On/Off (readonly)
- Wi-Fi signal strength (RSSI)

It is not possible to set the switch state on the Range Extender.
This is because it is not known what command to send to the device to make this possible.

## Prerequisites

Before using Smart Plugs with openHAB the devices must be connected to the Wi-Fi network.
This can be done using the TP-Link provided mobile app Kasa.

## Discovery

Devices can be auto discovered in the same local network as the openHAB application.
It is possible to connect to devices on a different network, but these must be added manually by `ipAddress`.
It is not possible to connect to devices on a different network using `deviceId` as configuration.

## Thing Configuration

The thingId is the product type in lower case. For example `HS100` has thingId `hs100`.

The thing can be configured by `ipAddress` or by `deviceId`.
If the one of them is used the other is automatically set by the binding.
When manually configured it is preferred to set the `deviceId` because if the ip address of the device would change this will be automatically updated.
The `deviceId` is the unique identifier each TP-Link device has.
The `deviceId` can be seen when using discovery in openHAB.
Discovery will set the `deviceId` automatically.

Using a configuration with `deviceId` depends on the discovery service of the binding.
The binding supports background discovery and this will update the ip address in case it changes within a minute.
With background discovery disabled the ip address, which is needed to communicate with the device, needs to be set by starting a manual discovery.
It will not update the ip address if background discovery is disabled and the ip address of the device changes.
Manually starting a discovery can also be used to set the ip address directly instead of waiting for the 1 minute background discovery refresh period.

The thing has the following configuration parameters:

| Parameter          | Description                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------|
| deviceId           | The TP-Link id of the device.                                                                |
| ipAddress          | IP Address of the device.                                                                    |
| refresh            | Refresh interval in seconds. Optional. The default is 30 seconds, and 1 second for switches. |
| transitionPeriod   | Duration of state changes in milliseconds, only for light bulbs, default 0.                  |

Either `deviceId` or `ipAddress` must be set.

## Channels

All devices support some of the following channels:

| Channel Type ID     | Item Type                | Description                                    | Thing types supporting this channel                                                                                                                    |
|---------------------|--------------------------|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| switch              | Switch                   | Power the device on or off.                    | EP10, EP40, HS100, HS103, HS105, HS107, HS110, HS200, HS210, HS300, KP100, KP105, KP115, KP200, KP303, KP400, KP401, KS230, RE270K, RE370K             |
| brightness          | Dimmer                   | Set the brightness of device or dimmer.        | ES20M, HS220, KB100, KL50, KL60, KL110, KL120, KP405, LB100, LB110, LB120, LB200                                                                       |
| colorTemperature    | Dimmer                   | Set the color temperature in percentage.       | KB130, KL120, KL125, KL130, KL135, KL400, KL430, LB120, LB130, LB230                                                                                   |
| colorTemperatureAbs | Number                   | Set the color temperature in Kelvin.           | KB130, KL120, KL125, KL130, KL135, KL400, KL430, LB120, LB130, LB230                                                                                   |
| color               | Color                    | Set the color of the light.                    | KB130, KL125, KL130, KL135, KL400, KL430, LB130, LB230                                                                                                 |
| power               | Number:Power             | Actual energy usage in Watt.                   | HS110, HS300, KLxxx, KP115, KP125, LBxxx,                                                                                                              |
| eneryUsage          | Number:Energy            | Energy Usage in kWh.                           | HS110, HS300, KP115, KP125                                                                                                                             |
| current             | Number:ElectricCurrent   | Actual current usage in Ampere.                | HS110, HS300, KP115, KP125                                                                                                                             |
| voltage             | Number:ElectricPotential | Actual voltage usage in Volt.                  | HS110, HS300, KP115, KP125                                                                                                                             |
| led                 | Switch                   | Switch the status LED on the device on or off. | ES20M, EP10, EP40, HS100, HS103, HS105, HS107, HS110, HS200, HS210, HS220, HS300, KP100, KP105, KP115, KP125, KP303, KP200, KP400, KP401, KP405, KS230 |
| rssi                | Number:Power             | Wi-Fi signal strength indicator in dBm.        | All                                                                                                                                                    |

The outlet devices (EP40, HS107, HS300, KP200, KP400) have group channels.
This means the channel is prefixed with the group id.
The following group ids are available:

| Group ID          | Description                                                                                           |
|-------------------|-------------------------------------------------------------------------------------------------------|
| groupSwitch       | Group id for all general channels. e.g. `groupSwitch#switch`                                          |
| outlet&lt;number> | The outlet to control. &lt;number> is the number of the outlet (starts with 1). e.g. `outlet1#switch` |

### Channel Refresh

When the thing receives a `RefreshType` command the channel state is updated from an internal cache.
This cache is updated per refresh interval as configured in the thing.
However for some use cases it is preferable to set the refresh interval higher than the default.
For example for switches the 1 second refresh interval may cause a burden to the network traffic.
Therefore if the refresh interval for switches is set to a value higher than 5 seconds, and for the other devices higher than 1 minute.
Than the a `RefreshType` command will fetch the device state and update the internal cache.

## Full Example

### tplinksmarthome.things:

```java
tplinksmarthome:hs100:tv      "TV"                 [ deviceId="00000000000000000000000000000001", refresh=60 ]
tplinksmarthome:hs300:laptop  "Laptop"             [ deviceId="00000000000000000000000000000004", refresh=60 ]
tplinksmarthome:lb110:bulb1   "Living Room Bulb 1" [ deviceId="00000000000000000000000000000002", refresh=60, transitionPeriod=2500 ]
tplinksmarthome:lb130:bulb2   "Living Room Bulb 2" [ deviceId="00000000000000000000000000000003", refresh=60, transitionPeriod=2500 ]
tplinksmarthome:kp401:outlet  "Outdoor Outlet"     [ ipAddress="192.168.1.101" ]
```

### tplinksmarthome.items:

```java
Switch       TP_L_TV      "TV"                                 { channel="tplinksmarthome:hs100:tv:switch" }
Switch       TP_L_Laptop  "Laptop"                             { channel="tplinksmarthome:hs300:laptop:outlet1#switch" }
Number:Power TP_L_RSSI    "Signal [%d %unit%]"        <signal> { channel="tplinksmarthome:hs100:tv:rssi" }
Dimmer       TP_LB_Bulb   "Dimmer [%d %%]"            <slider> { channel="tplinksmarthome:lb110:bulb1:brightness" }
Dimmer       TP_LB_ColorT "Color Temperature [%d %%]" <slider> { channel="tplinksmarthome:lb130:bulb2:colorTemperature" }
Color        TP_LB_Color  "Color"                     <slider> { channel="tplinksmarthome:lb130:bulb2:color" }
Switch       TP_LB_ColorS "Switch"                             { channel="tplinksmarthome:lb130:bulb2:color" }
Switch       TP_O_OUTLET  "Outdoor Outlet"                     { channel="tplinksmarthome:kp401:outlet:switch" }
```

## Sending Raw Commands to Devices (Advanced Usage)

TPLinkSmarthome Things can be sent a raw JSON string to control a device in a way not directly supported by this binding.
You can find several JSON commands in the [test fixtures](https://github.com/openhab/openhab-addons/tree/main/bundles/org.openhab.binding.tplinksmarthome/src/test/resources/org/openhab/binding/tplinksmarthome/internal/model) for this binding.

As an example, you might want to change the brightness level of a dimmer without turning it on or off.
Given the dimmer Thing has an id of `tplinksmarthome:hs220:123ABC`, you could accomplish just that with the following rule:

`example.rules`

```java
rule "Directly set the dimmer level when desired dimmer level changes, without turning the light on/off"
when
    Item Room_DesiredDimmerLevel changed
then
    val cmd = '{"smartlife.iot.dimmer":{"set_brightness":{"brightness":' + Room_DesiredDimmerLevel.state + '}}}'
    val actions = getActions("tplinksmarthome", "tplinksmarthome:hs220:123ABC")
    actions.send(cmd)
end
```
