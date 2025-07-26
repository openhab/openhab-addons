# DaikinMadoka

This extension implements communication with Daikin Madoka BRC1H thermostat over Bluetooth Low Energy (BLE) communication protocol.
The device uses an UART over BLE serial communication protocol over BLE WriteWithoutResponse and Notify characteristics.

[BRC1H on Daikin website (FR)](https://www.daikin.fr/fr_fr/famille-produits/Systemes-commande-intelligents/BRC1H.html)

[BRC1H on Daikin website (EN)](https://www.daikin.eu/en_us/product-group/control-systems/BRC1H.html)

## Supported Things

| Thing Type ID | Description |
| ------------- | ----------- |
| brc1h         | BRC1H BLE Thermostat |

## Discovery

As a pairing of the Thermostat is necessary (Bluetooth), no automatic discovery is implemented.

## Thing Configuration

- address: The Bluetooth MAC Address of the BRC1H controller

Example with a DBusBlueZ Bluetooth Bridge:

```java
Bridge bluetooth:dbusbluez:hci0 [ address="00:1A:7D:DA:71:13" ]

Thing bluetooth:brc1h:hci0:salon (bluetooth:dbusbluez:hci0)     [ address="00:CC:3F:B2:80:CA" ]
```

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel Type ID                  | Item Type           | Access | Description                                                                                                                                                               |
| -------------------------------- | ------------------- | ------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| onOffStatus                      | Switch              | R/W    | Turns the unit on or off.                                                                                                                                                 |
| indoorTemperature                | Number\:Temperature | R      | Current indoor temperature reported by the thermostat.                                                                                                                    |
| outdoorTemperature               | Number\:Temperature | R      | Outdoor temperature from the external unit (may not always be supported).                                                                                                 |
| commCtrlVersion                  | String              | R      | Firmware version of the communication controller.                                                                                                                         |
| remoteCtrlVersion                | String              | R      | Firmware version of the remote controller.                                                                                                                                |
| operationMode                    | String              | R/W    | Current operating mode of the AC unit. Supported values: `HEAT`, `COOL`.                                                                                                  |
| fanSpeed                         | Number              | R/W    | Virtual channel for adjusting fan speed (independent of mode). Values: `1–5`.<br> BRC1H units: `LOW (1)`, `MEDIUM (2–4)`, `MAX (5)`. Some models also support `AUTO (0)`. |
| setpoint                         | Number\:Temperature | R/W    | Virtual channel for adjusting the temperature setpoint (independent of mode).                                                                                             |
| homekitCurrentHeatingCoolingMode | String              | R      | Virtual channel for HomeKit integration (Thermostat thing). Supported values: `Off`, `CoolOn`, `HeatOn`, `Auto`.                                                          |
| homekitTargetHeatingCoolingMode  | String              | R/W    | Virtual channel for HomeKit integration (Thermostat thing). Supported values: `Off`, `CoolOn`, `HeatOn`, `Auto`.                                                          |
| homebridgeMode                   | String              | R/W    | Virtual channel for HomeBridge integration. Supported values: `Off`, `Heating`, `Cooling`, `Auto`.                                                                        |
| eyeBrightness                    | Dimmer              | R/W    | Adjusts the brightness of the blue “eye” indicator (`0–100%`).                                                                                                            |
| indoorPowerHours                 | Number\:Time        | R      | Total hours the indoor unit has been powered (operating or idle).                                                                                                         |
| indoorOperationHours             | Number\:Time        | R      | Total hours the indoor unit has been actively operating.                                                                                                                  |
| indoorFanHours                   | Number\:Time        | R      | Total hours the fan has been running.                                                                                                                                     |
| cleanFilterIndicator             | Switch              | R/W    | Indicates if the filter needs cleaning. Reset by writing `OFF` to this channel.                                                                                           |

## Full Example

### `daikinmadoka.things` Example

```java
Bridge bluetooth:dbusbluez:hci0 [ address="00:1A:7D:DA:71:13" ]

Thing bluetooth:brc1h:hci0:salon (bluetooth:dbusbluez:hci0)     [ address="00:CC:3F:B2:80:CA" ]

```

### `daikinmadoka.items` Example

```java
Group g_climSalon "Salon" [ "Thermostat" ]

Switch climSalon_onOff "Climatisation Salon"                   (g_climSalon) { channel="bluetooth:brc1h:hci0:salon:onOffStatus" }
Number climSalon_indoorTemperature "Température Intérieure"    (g_climSalon) [ "CurrentTemperature" ] { channel="bluetooth:brc1h:hci0:salon:indoorTemperature" }
Number climSalon_outdoorTemperature "Température Extérieure"   (g_climSalon) { channel="bluetooth:brc1h:hci0:salon:outdoorTemperature" }

String climSalon_commCtrlVersion                               (g_climSalon) { channel="bluetooth:brc1h:hci0:salon:commCtrlVersion" }
String climSalon_remoteCtrlVersion                             (g_climSalon) { channel="bluetooth:brc1h:hci0:salon:remoteCtrlVersion" }

Number climSalon_fanSpeed                                      (g_climSalon) { channel="bluetooth:brc1h:hci0:salon:fanSpeed" }

Number climSalon_setpoint                                      (g_climSalon) [ "homekit:TargetTemperature" ] { channel="bluetooth:brc1h:hci0:salon:setpoint" }

String climSalon_operationMode                                 (g_climSalon) { channel="bluetooth:brc1h:hci0:salon:operationMode" }

String climSalon_CurrentHeatingCoolingMode                     (g_climSalon) [ "homekit:CurrentHeatingCoolingMode" ] { channel="bluetooth:brc1h:hci0:salon:homekitCurrentHeatingCoolingMode" }
String climSalon_TargetHeatingCoolingMode                      (g_climSalon) [ "homekit:TargetHeatingCoolingMode" ] { channel="bluetooth:brc1h:hci0:salon:homekitTargetHeatingCoolingMode" }

```

## Pairing the BRC1H

The Daikin Madoka BRC1H Thermostat requires Bluetooth Pairing before it can be used.
This pairing process can be a bit challenging, as it seems the timing is very important for it success.

We suggest that the Bluetooth adapter is not being used by another component during the pairing phase.
As such, if you have other Bluetooth Things in your OpenHAB, it is suggested to stop the openHAB service before doing the pairing.

- Ensure that your BRC1H has Bluetooth enabled in the menu
- Open `bluetoothctl` on your openHAB server - preferably as `root`
- start scanning by typing `scan on`
- After few seconds, stop scanning `scan off`
- Start the pairing process by typing `pair <mac address of your brc1h>`
- On the BRC1H, confirm the pairing request, and quickly confirm as well on your server by typing `yes`

A successful pairing ends with `pairing successful`.

For more information on pairing a device in command line on Linux, refer to [official documentation](https://docs.ubuntu.com/core/en/stacks/bluetooth/bluez/docs/reference/pairing/outbound).
