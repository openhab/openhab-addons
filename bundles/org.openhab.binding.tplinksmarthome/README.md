# TP-Link Smart Home Binding

This binding adds support to control TP-Link Smart Home Devices from your local openHAB system.

## Supported Things

The following TP-Link Smart Devices are supported:

### HS100 Smart Wi-Fi Plug

* Switch On/Off
* Led On/Off
* Wi-Fi signal strength (rssi)

### HS103 Smart Wi-Fi Plug Lite

* Switch On/Off
* Led On/Off
* Wi-Fi signal strength (rssi)

### HS105 Smart Wi-Fi Plug

* Switch On/Off
* Led On/Off
* Wi-Fi signal strength (rssi)

### HS107 Smart Wi-Fi Plug, 2-Outlets

* Switch On/Off Group
* Switch On/Off Outlets
* Led On/Off
* Wi-Fi signal strength (rssi)

### HS110 Smart Wi-Fi Plug

* Switch On/Off
* Energy readings
* Led On/Off
* Wi-Fi signal strength (rssi)

### HS200 Smart Wi-Fi Switch

* Switch On/Off
* Led On/Off
* Wi-Fi signal strength (rssi)

### HS210 Smart Wi-Fi Light Switch 3-Way Kit

* Switch On/Off
* Led On/Off
* Wi-Fi signal strength (rssi)

### HS220 Smart Wi-Fi Light Switch, Dimmer

* Switch On/Off
* Adjust the brightness
* Led On/Off
* Wi-Fi signal strength (rssi)

Use the brightness channel on the HS220 with a Switch item can be used to switch the device on and off.
It won't change the brightness value.

The default refresh for switch devices is set to 1 second. So it polls the switch for status changes.
If you don't use the switch manually often, you can set it to a higher refresh.
The refresh is only relevant to detect manual using the switch.
Switching via openHAB activates the switch directly.

### HS300 Smart Wi-Fi Power Strip

* Switch On/Off Group
* Switch On/Off Outlets
* Energy readings Outlets
* Led On/Off
* Wi-Fi signal strength (rssi)

### KB100 Kasa Smart Light Bulb

* Switch On/Off
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching and Brightness is done using the `brightness` channel.

### KB130 Kasa Multi-color Smart Light Bulb

* Switch On/Off
* Fine-tune colors
* Adjust light appearance from soft white (2500k) to daylight (9000k)
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching, Brightness and Color is done using the `color` channel.

### KP100 Kasa Wi-Fi Smart Plug - Slim Edition

* Switch On/Off
* Led On/Off
* Wi-Fi signal strength (rssi)

### KP200 Smart Wi-Fi Power Outlet, 2-Sockets

* Switch On/Off Group
* Switch On/Off Outlets
* Led On/Off
* Wi-Fi signal strength (rssi)

### KP400 Smart Outdoor Plug

* Switch On/Off Group
* Switch On/Off Outlets
* Led On/Off
* Wi-Fi signal strength (rssi)

### LB100 Smart Wi-Fi LED Bulb with Dimmable Light

* Switch On/Off
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching and Brightness is done using the `brightness` channel.

### LB110 Smart Wi-Fi LED Bulb with Dimmable Light

* Switch On/Off
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching and Brightness is done using the `brightness` channel.

### LB120 Smart Wi-Fi LED Bulb with Tunable White Light

* Switch On/Off
* Adjust light appearance from soft white (2700k) to daylight (6500k)
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching and Brightness is done using the `brightness` channel.

### LB130 Smart Wi-Fi LED Bulb with Color Changing Hue

* Switch On/Off
* Fine-tune colors
* Adjust light appearance from soft white (2500k) to daylight (9000k)
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching, Brightness and Color is done using the `color` channel.

### LB200 Smart Wi-Fi LED Bulb with Dimmable Light

* Switch On/Off
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching and Brightness is done using the `brightness` channel.

### LB230 Smart Wi-Fi LED Bulb with Color Changing Hue

* Switch On/Off
* Fine-tune colors
* Adjust light appearance from soft white (2500k) to daylight (9000k)
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching, Brightness and Color is done using the `color` channel.

### KL110 Smart Wi-Fi LED Bulb with Dimmable Light

* Switch On/Off
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching and Brightness is done using the `brightness` channel.

### KL120 Smart Wi-Fi LED Bulb with Tunable White Light

* Switch On/Off
* Adjust light appearance from soft white (2700k) to daylight (6500k)
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching and Brightness is done using the `brightness` channel.

### KL130 Smart Wi-Fi LED Bulb with Color Changing Hue

* Switch On/Off
* Fine-tune colors
* Adjust light appearance from soft white (2500k) to daylight (9000k)
* Adjust the brightness
* Actual power usage
* Wi-Fi signal strength (rssi)

Switching, Brightness and Color is done using the `color` channel.

### RE270K AC750 Wi-Fi Range Extender with Smart Plug

* Switch On/Off (readonly)
* Wi-Fi signal strength (rssi)

### RE370K AC1200 Wi-Fi Range Extender with Smart Plug

* Switch On/Off (readonly)
* Wi-Fi signal strength (rssi)

It's not possible to set the switch state on the Range Extender.
This is because it's not known what command to send to the device to make this possible.

## Prerequisites

Before using Smart Plugs with openHAB the devices must be connected to the Wi-Fi network.
This can be done using the TP-Link provided mobile app Kasa.

## Discovery

Devices can be auto discovered in the same local network as the openHAB application.
It's possible to connect to devices on a different network, but these must be added manually by ipAddress.
It's not possible to connect to devices on a different network using `deviceId` as configuration.

## Thing Configuration

The thingId is the product type in lower case. For example `HS100` has thingId `hs100`.

The thing can be configured by `ipAddress` or by `deviceId`.
If the one of them is used the other is automatically set by the binding.
When manually configured it's preferred to set the `deviceId` because if the ip address of the device would change this will be automatically updated.
Using a configuration with `deviceId` depends on the discovery service of the binding.
The binding supports background discovery and this will update the ip address in case it changes within a minute.
With background discovery disabled the ip address, which is needed to communicate with the device, needs to be set by starting a manual discovery.
It won't update the ip address if background discovery is disabled and the ip address of the device changes.
Manually starting a discovery can also be used to set the ip address directly instead of waiting for the 1 minute background discovery refresh period.

The thing has the following configuration parameters:

| Parameter          | Description                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------|
| deviceId           | The id of the device.                                                                        |
| ipAddress          | IP Address of the device.                                                                    |
| refresh            | Refresh interval in seconds. Optional. The default is 30 seconds, and 1 second for switches. |
| transitionPeriod   | Duration of state changes in milliseconds, only for light bulbs, default 0.                  |

Either `deviceId` or `ipAddress` must be set.

## Channels

All devices support some of the following channels:

| Channel Type ID     | Item Type                | Description                                        | Thing types supporting this channel                                                         |
|---------------------|--------------------------|----------------------------------------------------|---------------------------------------------------------------------------------------------|
| switch              | Switch                   | Switch the Smart Home device on or off.            | HS100, HS103, HS105, HS107, HS110, HS200, HS210, HS300, KP100, KP200, KP400, RE270K, RE370K |
| brightness          | Dimmer                   | Set the brightness of Smart Home device or dimmer. | HS220, KB100, KL110, KL120, LB100, LB110, LB120, LB200                                      |
| colorTemperature    | Dimmer                   | Set the color temperature in percentage.           | KB130, KL120, KL130, LB120, LB130, LB230                                                    |
| colorTemperatureAbs | Number                   | Set the color temperature in Kelvin.               | KB130, KL120, KL130, LB120, LB130, LB230                                                    |
| color               | Color                    | Set the color of the Smart Home light.             | KB130, KL130, LB130, LB230                                                                  |
| power               | Number:Power             | Actual energy usage in Watt.                       | HS110, HS300, KLxxx, LBxxx                                                                  |
| eneryUsage          | Number:Energy            | Energy Usage in kWh.                               | HS110, HS300                                                                                |
| current             | Number:ElectricCurrent   | Actual current usage in Ampere.                    | HS110, HS300                                                                                |
| voltage             | Number:ElectricPotential | Actual voltage usage in Volt.                      | HS110, HS300                                                                                |
| led                 | Switch                   | Switch the status led on the device on or off.     | HS100, HS103, HS105, HS107, HS110, HS200, HS210, HS220, HS300, KP100, KP200, KP400          |
| rssi                | Number:Power             | Wi-Fi signal strength indicator in dBm.            | All                                                                                         |

The outlet devices (HS107, HS300, KP200, KP400) have group channels.
This means the channel is prefixed with the group id.
The following group ids are available:

| Group ID          | Description                                                                                           |
|-------------------|-------------------------------------------------------------------------------------------------------|
| groupSwitch       | General channels. e.g. `groupSwitch#switch`                                                           |
| outlet&lt;number> | The outlet to control. &lt;number> is the number of the outlet (starts with 1). e.g. `outlet1#switch` |

### Channel Refresh

When the thing receives a `RefreshType` command the channel state is updated from an internal cache.
This cache is updated per refresh interval as configured in the thing.
However for some use cases it's preferable to set the refresh interval higher than the default.
For example for switches the 1 second refresh interval may cause a burden to the network traffic.
Therefore if the refresh interval for switches is set to a value higher than 5 seconds, and for the other devices higher than 1 minute.
Than the a `RefreshType` command will fetch the device state and update the internal cache.

## Full Example

### tplinksmarthome.things:

```
tplinksmarthome:hs100:tv      "TV"                 [ deviceId="00000000000000000000000000000001", refresh=60 ]
tplinksmarthome:hs300:laptop  "Laptop"             [ deviceId="00000000000000000000000000000004", refresh=60 ]
tplinksmarthome:lb110:bulb1   "Living Room Bulb 1" [ deviceId="00000000000000000000000000000002", refresh=60, transitionPeriod=2500 ]
tplinksmarthome:lb130:bulb2   "Living Room Bulb 2" [ deviceId="00000000000000000000000000000003", refresh=60, transitionPeriod=2500 ]
```

### tplinksmarthome.items:

```
Switch       TP_L_TV      "TV"                                 { channel="tplinksmarthome:hs100:tv:switch" }
Switch       TP_L_Laptop  "Laptop"                             { channel="tplinksmarthome:hs300:laptop:outlet1#switch" }
Number:Power TP_L_RSSI    "Signal [%d %unit%]"        <signal> { channel="tplinksmarthome:hs100:tv:rssi" }
Dimmer       TP_LB_Bulb   "Dimmer [%d %%]"            <slider> { channel="tplinksmarthome:lb110:bulb1:brightness" }
Dimmer       TP_LB_ColorT "Color Temperature [%d %%]" <slider> { channel="tplinksmarthome:lb130:bulb2:colorTemperature" }
Color        TP_LB_Color  "Color"                     <slider> { channel="tplinksmarthome:lb130:bulb2:color" }
Switch       TP_LB_ColorS "Switch"                             { channel="tplinksmarthome:lb130:bulb2:color" }
```
