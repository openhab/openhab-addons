# Bosch Smart Home Binding

Binding for the Bosch Smart Home.

- [Bosch Smart Home Binding](#bosch-smart-home-binding)
  - [Supported Things](#supported-things)
    - [In-Wall switches & Smart Plugs](#in-wall-switches-smart-plugs)
    - [TwinGuard smoke detector](#twinguard-smoke-detector)
    - [Door/Window contact](#door-window-contact)
    - [Motion Detector](#motion-detector)
    - [Shutter Control](#shutter-control)
    - [Thermostat](#thermostat)
    - [Climate Control](#climate-control)
    - [Wall Thermostat](#wall-thermostat)
    - [Security Camera 360](#security-camera-360)
    - [Security Camera Eyes](#security-camera-eyes)
    - [Intrusion Detection System](#intrusion-detection-system)
  - [Limitations](#limitations)
  - [Discovery](#discovery)
  - [Bridge Configuration](#bridge-configuration)
  - [Getting the device IDs](#getting-the-device-ids)
  - [Thing Configuration](#thing-configuration)
  - [Item Configuration](#item-configuration)

## Supported Things

### In-Wall switches & Smart Plugs

A simple light control.

**Thing Type ID**: `in-wall-switch`

| Channel Type ID    | Item Type     | Writable | Description                                  |
| ------------------ | ------------- | :------: | -------------------------------------------- |
| power-switch       | Switch        | &#9745;  | Current state of the switch.                 |
| power-consumption  | Number:Power  | &#9744;  | Current power consumption (W) of the device. |
| energy-consumption | Number:Energy | &#9744;  | Energy consumption of the device.            |

### TwinGuard smoke detector

The Twinguard smoke detector warns you in case of fire and constantly monitors the air.

**Thing Type ID**: `twinguard`

| Channel Type ID    | Item Type            | Writable | Description                                                                                       |
| ------------------ | -------------------- | :------: | ------------------------------------------------------------------------------------------------- |
| temperature        | Number:Temperature   | &#9744;  | Current measured temperature.                                                                     |
| temperature-rating | String               | &#9744;  | Rating of the currently measured temperature.                                                     |
| humidity           | Number:Dimensionless | &#9744;  | Current measured humidity (0 to 100).                                                             |
| humidity-rating    | String               | &#9744;  | Rating of current measured humidity.                                                              |
| purity             | Number:Dimensionless | &#9744;  | Purity of the air (ppm). Range from 500 to 5500 ppm. A higher value indicates a higher pollution. |
| purity-rating      | String               | &#9744;  | Rating of current measured purity.                                                                |
| air-description    | String               | &#9744;  | Overall description of the air quality.                                                           |
| combined-rating    | String               | &#9744;  | Combined rating of the air quality.                                                               |

### Door/Window contact

Detects open windows and doors.

**Thing Type ID**: `window-contact`

| Channel Type ID | Item Type | Writable | Description                  |
| --------------- | --------- | :------: | ---------------------------- |
| contact         | Contact   | &#9744;  | Contact state of the device. |

### Motion Detector

Detects every movement through an intelligent combination of passive infra-red technology and an additional temperature sensor.

**Thing Type ID**: `motion-detector`

| Channel Type ID | Item Type | Writable | Description                    |
| --------------- | --------- | :------: | ------------------------------ |
| latest-motion   | DateTime  | &#9744;  | The date of the latest motion. |

### Shutter Control

Control of your shutter to take any position you desire.

**Thing Type ID**: `shutter-control`

| Channel Type ID | Item Type     | Writable | Description                              |
| --------------- | ------------- | :------: | ---------------------------------------- |
| level           | Rollershutter | &#9745;  | Current open ratio (0 to 100, Step 0.5). |

### Thermostat

Radiator thermostat

**Thing Type ID**: `thermostat`

| Channel Type ID       | Item Type            | Writable | Description                                    |
| --------------------- | -------------------- | :------: | ---------------------------------------------- |
| temperature           | Number:Temperature   | &#9744;  | Current measured temperature.                  |
| valve-tappet-position | Number:Dimensionless | &#9744;  | Current open ratio of valve tappet (0 to 100). |
| child-lock            | Switch               | &#9745;  | Indicates if child lock is active.             |

### Climate Control

A virtual device which controls up to six Bosch Smart Home radiator thermostats in a room.

**Thing Type ID**: `climate-control`

| Channel Type ID      | Item Type          | Writable | Description                   |
| -------------------- | ------------------ | :------: | ----------------------------- |
| temperature          | Number:Temperature | &#9744;  | Current measured temperature. |
| setpoint-temperature | Number:Temperature | &#9745;  | Desired temperature.          |

### Wall Thermostat

Display of the current room temperature as well as the relative humidity in the room.

**Thing Type ID**: `wall-thermostat`

| Channel Type ID | Item Type            | Writable | Description                           |
| --------------- | -------------------- | :------: | ------------------------------------- |
| temperature     | Number:Temperature   | &#9744;  | Current measured temperature.         |
| humidity        | Number:Dimensionless | &#9744;  | Current measured humidity (0 to 100). |

### Security Camera 360

Indoor security camera with 360° view and motion detection.

**Thing Type ID**: `security-camera-360`

| Channel Type ID       | Item Type            | Writable | Description                                                        |
| --------------------- | -------------------- | :------: | ------------------------------------------------------------------ |
| privacy-mode          | Switch               | &#9745;  | If privacy mode is enabled, the camera is disabled and vice versa. |
| camera-notification   | Switch               | &#9745;  | Enables or disables notifications for the camera.                  |

### Security Camera Eyes

Outdoor security camera with motion detection and light.

**Thing Type ID**: `security-camera-eyes`

| Channel Type ID       | Item Type            | Writable | Description                                                        |
| --------------------- | -------------------- | :------: | ------------------------------------------------------------------ |
| privacy-mode          | Switch               | &#9745;  | If privacy mode is enabled, the camera is disabled and vice versa. |
| camera-notification   | Switch               | &#9745;  | Enables or disables notifications for the camera.                  |

### Intrusion Detection System

Allows to retrieve notifications in case of intrusions. The system can be armed and disarmed and alarms can be muted.

**Thing Type ID**: `intrusion-detection-system`

| Channel Type ID              | Item Type            | Writable | Description                                                    |
| ---------------------------- | -------------------- | :------: | -------------------------------------------------------------- |
| system-availability          | Switch               | &#9744;  | Indicates whether the intrusion detection system is available. |
| arming-state                 | String               | &#9744;  | Read-only channel to retrieve the current arming state. Possible values are `SYSTEM_ARMING`, `SYSTEM_ARMED` and `SYSTEM_DISARMED`. |
| alarm-state                  | String               | &#9744;  | Read-only channel to retrieve the current alarm state. Possible values are `ALARM_OFF`, `PRE_ALARM`, `ALARM_ON`, `ALARM_MUTED` and `UNKNOWN`. |
| active-configuration-profile | String               | &#9744;  | The name of the active configuration profile used for the intrusion detection system. |
| arm-action                   | String               | &#9745;  | Arms the intrusion detection system using the given profile ID (default is "0"). |
| disarm-action                | Switch               | &#9745;  | Disarms the intrusion detection system when an ON command is received. |
| mute-action                  | Switch               | &#9745;  | Mutes the alarm when an ON command is received. |

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

The log can also be called using the following command.

```
tail -f /var/log/openhab/openhab.log /var/log/openhab/events.log
```

Alternatively, the log can be viewed using the OpenHab Log Viewer (frontail) via http://openhab:9001.

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
