# TPLinkSmartHome Binding

This binding adds support to control TP-Link Smart Home Devices from your local openHAB system.

## Supported Things

The following TP-Link Smart Devices are supported:


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


### HS100 Smart Wi-Fi Plug

* Switch On/Off
* Wi-Fi signal strength (rssi)

### HS105 Smart Wi-Fi Plug

* Switch On/Off
* Wi-Fi signal strength (rssi)

### HS110 Smart Wi-Fi Plug

* Switch On/Off
* Energy readings
* Wi-Fi signal strength (rssi)

### HS200 Smart Wi-Fi Switch

* Switch On/Off
* Wi-Fi signal strength (rssi)

The default refresh is set to 1 second. So it polls the switch for status changes. If you don't use the switch manually
often, you can set it to a higher refresh. The refresh is only relevant to detect manual using the switch. Switching
via openHAB activates the switch directly.

### RE270K AC750 Wi-Fi Range Extender with Smart Plug

* Switch On/Off
* Wi-Fi signal strength (rssi)

### RE370K AC1200 Wi-Fi Range Extender with Smart Plug

* Switch On/Off
* Wi-Fi signal strength (rssi)

## Prerequisites

Before using Smart Plugs with openHAB the devices must be connected to the Wi-Fi network. This can be done using the
TP-Link provided mobile app Kasa.

## Discovery

Devices can be auto discovered in the same local network as the openHAB application. It's possible to connect to
devices in a different network, but these must be added manually.

## Thing Configuration

The thing id is the product type in lower case. For example `HS100` has id `hs100`.

The thing has a few configuration parameters:

| Parameter          | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| ipAddress          | IP Address of the device. Mandatory.                                        |
| refresh            | Refresh interval in seconds. Optional, the default value is 30 seconds.     |
| transitionPeriod   | Duration of state changes in milliseconds, only for light bulbs, default 0. |

## Channels

All devices support some of the following channels:

| Channel Type ID  | Item Type | Description                                    | Thing types supporting this channel               |
|------------------|-----------|------------------------------------------------|---------------------------------------------------|
| switch           | Switch    | Switch the Smart Home device on or off.        | KP100, HS100, HS105, HS110, HS200, RE270K, RE370K |
| brightness       | Dimmer    | Set the brightness of Smart Home light.        | KB100, LB100, LB110, LB120, LB200                 |
| colorTemperature | Dimmer    | Set the color temperature of Smart Home light. | KB130, LB120, LB130, LB230                        |
| color            | Color     | Set the color of the Smart Home light.         | KB130, LB130, LB230                               |
| power            | Number    | Actual energy usage in Watt.                   | HS110, LBxxx                                      |
| eneryUsage       | Number    | Energy Usage in kWh.                           | HS110                                             |
| current          | Number    | Actual current usage in Ampere.                | HS110                                             |
| voltage          | Number    | Actual voltage usage in Volt.                  | HS110                                             |
| rssi             | Number    | Wi-Fi signal strength indicator in dBm.        | All                                               |

## Full Example

### tplinksmarthome.things:

```
tplinksmarthome:hs100:home "Living Room"        [ ipAddress="192.168.0.13", refresh=60 ]
tplinksmarthome:lb110:home "Living Room Bulb 1" [ ipAddress="192.168.0.14", refresh=60, transitionPeriod=2500 ]
tplinksmarthome:lb130:home "Living Room Bulb 2" [ ipAddress="192.168.0.15", refresh=60, transitionPeriod=2500 ]
```

### tplinksmarthome.items:

```
Switch   TP_L_Switch  "Switch"                            { channel="tplinksmarthome:hs100:home:switch" }
Number   TP_L_RSSI    "Signal [%d] dB"           <signal> { channel="tplinksmarthome:hs100:home:rssi" }
Dimmer   TP_LB_Bulb   "Dimmer [%d %%]"           <slider> { channel="tplinksmarthome:lb110:home:brightness" }
Dimmer   TP_LB_ColorT "Color Temperature [%d] K" <slider> { channel="tplinksmarthome:lb130:home:color" }
Color    TP_LB_Color  "Color"                    <slider> { channel="tplinksmarthome:lb130:home:color" }
Switch   TP_LB_ColorS "Switch"                            { channel="tplinksmarthome:lb130:home:color" }
```
