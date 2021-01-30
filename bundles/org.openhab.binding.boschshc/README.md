# Bosch Smart Home Binding

Binding for the Bosch Smart Home Controller.

- [Bosch Smart Home Binding](#bosch-smart-home-binding)
  - [Supported Things](#supported-things)
    - [Bosch In-Wall switches & Bosch Smart Plugs](#bosch-in-wall-switches--bosch-smart-plugs)
    - [Bosch TwinGuard smoke detector](#bosch-twinguard-smoke-detector)
    - [Bosch Window/Door contacts](#bosch-windowdoor-contacts)
    - [Bosch Motion Detector](#bosch-motion-detector)
    - [Bosch Shutter Control in-wall](#bosch-shutter-control-in-wall)
    - [Bosch Thermostat](#bosch-thermostat)
    - [Bosch Climate Control](#bosch-climate-control)
  - [Limitations](#limitations)
  - [Discovery](#discovery)
  - [Bridge Configuration](#bridge-configuration)
  - [Getting the device IDs](#getting-the-device-ids)
  - [Thing Configuration](#thing-configuration)
  - [Item Configuration](#item-configuration)

## Supported Things

### Bosch In-Wall switches & Bosch Smart Plugs

**Thing Type ID**: `in-wall-switch`

| Channel Type ID    | Item Type     | Description                                  |
|--------------------|---------------|----------------------------------------------|
| power-switch       | Switch        | Current state of the switch.                 |
| power-consumption  | Number:Power  | Current power consumption (W) of the device. |
| energy-consumption | Number:Energy | Energy consumption of the device.            |

### Bosch TwinGuard smoke detector

**Thing Type ID**: `twinguard`

| Channel Type ID    | Item Type            | Description                                                                                       |
|--------------------|----------------------|---------------------------------------------------------------------------------------------------|
| temperature        | Number:Temperature   | Current measured temperature.                                                                     |
| temperature-rating | String               | Rating of the currently measured temperature.                                                     |
| humidity           | Number:Dimensionless | Current measured humidity.                                                                        |
| humidity-rating    | String               | Rating of current measured humidity.                                                              |
| purity             | Number:Dimensionless | Purity of the air (ppm). Range from 500 to 5500 ppm. A higher value indicates a higher pollution. |
| purity-rating      | String               | Rating of current measured purity.                                                                |
| air-description    | String               | Overall description of the air quality.                                                           |
| combined-rating    | String               | Combined rating of the air quality.                                                               |

### Bosch Window/Door contacts

**Thing Type ID**: `window-contact`

| Channel Type ID | Item Type | Description                  |
|-----------------|-----------|------------------------------|
| contact         | Contact   | Contact state of the device. |

### Bosch Motion Detector

**Thing Type ID**: `motion-detector`

| Channel Type ID | Item Type | Description                    |
|-----------------|-----------|--------------------------------|
| latest-motion   | DateTime  | The date of the latest motion. |

### Bosch Shutter Control in-wall

**Thing Type ID**: `shutter-control`

| Channel Type ID | Item Type     | Description                              |
|-----------------|---------------|------------------------------------------|
| level           | Rollershutter | Current open ratio (0 to 100, Step 0.5). |

### Bosch Thermostat

**Thing Type ID**: `thermostat`

| Channel Type ID       | Item Type            | Description                                    |
|-----------------------|----------------------|------------------------------------------------|
| temperature           | Number:Temperature   | Current measured temperature.                  |
| valve-tappet-position | Number:Dimensionless | Current open ratio of valve tappet (0 to 100). |

### Bosch Climate Control

**Thing Type ID**: `climate-control`

| Channel Type ID      | Item Type          | Description                   |
|----------------------|--------------------|-------------------------------|
| temperature          | Number:Temperature | Current measured temperature. |
| setpoint-temperature | Number:Temperature | Desired temperature.          |

## Limitations

- Discovery of Things
- Discovery of Bridge

## Discovery

Configuration via configuration files or UI (see below).

## Bridge Configuration

You need to provide the IP address and the system password of your Bosch Smart Home Controller.
The IP address of the controller is visible in the Bosch Smart Home Mobile App (More -> System -> Smart Home Controller) or in your network router UI.
The system password is set by you during your initial registration steps in the _Bosch Smart Home App_.

A keystore file with a self-signed certificate is created automatically.
This certificate is used for pairing between the Bridge and the Bosch Smart Home Controller.

*Press and hold the Bosch Smart Home Controller Bridge button until the LED starts blinking after you save your settings for pairing*.

## Getting the device IDs

Bosch IDs for found devices are displayed in the openHAB log on bootup (`OPENHAB_FOLDER/userdata/logs/openhab.log`)

Example:

```
2020-08-11 12:42:49.490 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Heizung id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.495 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-RoomClimateControl- id=roomClimateControl_hz_1
2020-08-11 12:42:49.497 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-VentilationService- id=ventilationService
2020-08-11 12:42:49.498 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Großes Fenster id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.501 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-IntrusionDetectionSystem- id=intrusionDetectionSystem
2020-08-11 12:42:49.502 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Rollladen id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.502 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Heizung id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.503 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Heizung Haus id=hdm:ICom:819410185:HC1
2020-08-11 12:42:49.503 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=-RoomClimateControl- id=roomClimateControl_hz_6
2020-08-11 12:42:49.504 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=PhilipsHueBridgeManager id=hdm:PhilipsHueBridge:PhilipsHueBridgeManager
2020-08-11 12:42:49.505 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Rollladen id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.506 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Rollladen id=hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX
2020-08-11 12:42:49.507 [INFO ] [chshc.internal.BoschSHCBridgeHandler] - Found device: name=Central Heating id=hdm:ICom:819410185
```

## Thing Configuration

You define your Bosch devices by adding them either to a `.things` file in your `$OPENHAB_CONF/things` folder like this:

```
Bridge boschshc:shc:1 [ ipAddress="192.168.x.y", password="XXXXXXXXXX" ] {
  Thing in-wall-switch bathroom "Bathroom" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch bedroom "Bedroom" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch kitchen "Kitchen" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch corridor "Corridor" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]
  Thing in-wall-switch livingroom "Living Room" [ id="hdm:HomeMaticIP:3014F711A000XXXXXXXXXXXX" ]

  Thing in-wall-switch coffeemachine "Coffee Machine" [ id="hdm:HomeMaticIP:3014F711A0000XXXXXXXXXXXX" ]

  Thing twinguard      tg-corridor    "Twinguard Smoke Detector" [ id="hdm:ZigBee:000d6f000XXXXXXX" ]
  Thing window-contact window-kitchen "Window Kitchen"           [ id="hdm:HomeMaticIP:3014F711A00000XXXXXXXXXX" ]
  Thing window-contact entrance       "Entrance door"            [ id="hdm:HomeMaticIP:3014F711A00000XXXXXXXXXX" ]

  Thing motion-detector  motion-corridor "Bewegungsmelder"      [ id="hdm:ZigBee:000d6f000XXXXXXX" ]
}
```

Or by adding them via UI: Settings -> Things -> "+" -> Bosch Smart Home Binding.

## Item Configuration

You define the items which should be linked to your Bosch devices via a `.items` file in your `$OPENHAB_CONF/items` folder like this:

```
Switch Bosch_Bathroom    "Bath Room"    { channel="boschshc:in-wall-switch:1:bathroom:power-switch" }
Switch Bosch_Bedroom     "Bed Room"     { channel="boschshc:in-wall-switch:1:bedroom:power-switch" }
Switch Bosch_Kitchen     "Kitchen"      { channel="boschshc:in-wall-switch:1:kitchen:power-switch" }
Switch Bosch_Corridor    "Corridor"     { channel="boschshc:in-wall-switch:1:corridor:power-switch" }
Switch Bosch_Living_Room "Living Room"  { channel="boschshc:in-wall-switch:1:livingroom:power-switch" }

Switch Bosch_Lelit       "Lelit"        { channel="boschshc:in-wall-switch:1:coffeemachine:power-switch" }
```

Or by adding them via UI: Settings -> Items -> "+".
