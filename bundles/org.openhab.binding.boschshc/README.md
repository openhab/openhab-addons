# Bosch Smart Home Binding

Binding for Bosch Smart Home devices.

- [Bosch Smart Home Binding](#bosch-smart-home-binding)
  - [Supported Things](#supported-things)
    - [Smart Home Controller](#smart-home-controller)
    - [In-Wall Switch](#in-wall-switch)
    - [Compact Smart Plug](#compact-smart-plug)
    - [Twinguard Smoke Detector](#twinguard-smoke-detector)
    - [Door/Window Contact](#doorwindow-contact)
    - [Door/Window Contact II](#doorwindow-contact-ii)
    - [Light Control II](#light-control-ii)
    - [Motion Detector](#motion-detector)
    - [Shutter Control](#shutter-control)
    - [Shutter Control II](#shutter-control-ii)
    - [Thermostat](#thermostat)
    - [Climate Control](#climate-control)
    - [Wall Thermostat](#wall-thermostat)
    - [Relay](#relay)
    - [Security Camera 360](#security-camera-360)
    - [Security Camera Eyes](#security-camera-eyes)
    - [Intrusion Detection System](#intrusion-detection-system)
    - [Smart Bulb](#smart-bulb)
    - [Smoke Detector](#smoke-detector)
    - [Smoke Detector II](#smoke-detector-ii)
    - [User-defined States](#user-defined-states)
    - [Universal Switch](#universal-switch)
    - [Universal Switch II](#universal-switch-ii)
    - [Water Detector](#water-detector)
  - [Limitations](#limitations)
  - [Discovery](#discovery)
  - [Bridge Configuration](#bridge-configuration)
  - [Getting the device IDs](#getting-the-device-ids)
  - [Thing Configuration](#thing-configuration)
  - [Item Configuration](#item-configuration)

## Supported Things

### Smart Home Controller

The Smart Home Controller is the central hub that allows you to monitor and control your smart home devices from one place.

**Bridge Type ID**: ``shc``

| Channel Type ID    | Item Type | Writable | Description                                                             |
|--------------------|-----------|:--------:|-------------------------------------------------------------------------|
| scenario-triggered | String    | &#9744;  | Name of the triggered scenario (e.g. by the Universal Switch Flex)      |
| trigger-scenario   | String    | &#9745;  | Name of a scenario to be triggered on the Bosch Smart Home Controller.  |

### In-Wall Switch

A simple light control.

**Thing Type ID**: `in-wall-switch`

| Channel Type ID    | Item Type     | Writable | Description                                      |
| ------------------ | ------------- | :------: | ------------------------------------------------ |
| power-switch       | Switch        | &#9745;  | Current state of the switch.                     |
| power-consumption  | Number:Power  | &#9744;  | Current power consumption (W) of the device.     |
| energy-consumption | Number:Energy | &#9744;  | Cumulated energy consumption (Wh) of the device. |

### Compact Smart Plug

A compact smart plug with energy monitoring capabilities.

**Thing Type ID**: `smart-plug-compact`

| Channel Type ID    | Item Type     | Writable | Description                                      |
| ------------------ | ------------- | :------: | ------------------------------------------------ |
| power-switch       | Switch        | &#9745;  | Current state of the switch.                     |
| power-consumption  | Number:Power  | &#9744;  | Current power consumption (W) of the device.     |
| energy-consumption | Number:Energy | &#9744;  | Cumulated energy consumption (Wh) of the device. |

### Dimmer

Smart dimmer capable of controlling any dimmable lamp.

**Thing Type ID**: `dimmer`

| Channel Type ID    | Item Type     | Writable | Description                                                    |
| ------------------ | ------------- | :------: | -------------------------------------------------------------- |
| power-switch       | Switch        | &#9745;  | Current state of the switch.                                   |
| brightness         | Dimmer        | &#9745;  | Regulates the brightness on a percentage scale from 0 to 100%. |
| signal-strength    | Number        | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| child-protection   | Switch        | &#9745;  | Indicates whether the child protection is active.              |

### Twinguard Smoke Detector

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
| battery-level      | Number               | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery        | Switch               | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`). |
| smoke-check        | String               | &#9745;  | State of the smoke check. Also used to request a new smoke check.                                 |

### Door/Window Contact

Detects open windows and doors.

**Thing Type ID**: `window-contact`

| Channel Type ID | Item Type | Writable | Description                  |
| --------------- | --------- | :------: | ---------------------------- |
| contact         | Contact   | &#9744;  | Contact state of the device. |
| battery-level   | Number    | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery     | Switch    | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`). |

### Door/Window Contact II

Detects open windows and doors and features an additional button.

**Thing Type ID**: `window-contact-2`

| Channel Type ID | Item Type | Writable | Description                  |
| ----------------| --------- | :------: | ---------------------------- |
| contact         | Contact   | &#9744;  | Contact state of the device. |
| battery-level   | Number    | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery     | Switch    | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`). |
| bypass          | Switch    | &#9744;  | Indicates whether the device is currently bypassed. Possible values are `ON`,`OFF` and `UNDEF` if the bypass state cannot be determined. |
| signal-strength | Number    | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |

### Door/Window Contact II Plus

Detects open windows and doors, provides a configurable button and a vibration sensor.

**Thing Type ID**: `window-contact-2-plus`

| Channel Type ID              | Item Type | Writable | Description                                                                                                                                                                                                                 |
| -----------------------------| --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| contact                      | Contact   | &#9744;  | Contact state of the device.                                                                                                                                                                                                |
| battery-level                | Number    | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery                  | Switch    | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |
| bypass                       | Switch    | &#9744;  | Indicates whether the device is currently bypassed. Possible values are `ON`,`OFF` and `UNDEF` if the bypass state cannot be determined.                                                                                    |
| signal-strength              | Number    | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                             |
| vibration-sensor-enabled     | Switch    | &#9745;  | Channel to enable or disable the vibration sensor.                                                                                                                                                                          |
| vibration-sensor-sensitivity | String    | &#9745;  | The sensitivity of the vibration sensor. Possible values are `VERY_HIGH`, `HIGH`, `MEDIUM`, `LOW` and `VERY_LOW`.                                                                                                           |
| vibration-sensor-state       | String    | &#9744;  | Indicates whether vibrations were detected by the sensor. Possible values are `NO_VIBRATION`, `VIBRATION_DETECTED` and `UNKNOWN`.                                                                                           |

### Light Control II

This thing type is used if Light/Shutter Control II devices are configured as light controls.

**Thing Type ID**: `light-control-2`

| Channel Type ID    | Item Type     | Writable | Description                                                   |
| ------------------ | ------------- | :------: | ------------------------------------------------------------- |
| signal-strength    | Number        | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| power-consumption  | Number:Power  | &#9744;  | Current power consumption (W) of the device.                  |
| energy-consumption | Number:Energy | &#9744;  | Cumulated energy consumption (Wh) of the device.              |
| power-switch-1     | Switch        | &#9745;  | Switches the light on or off (circuit 1).                     |
| child-protection-1 | Switch        | &#9745;  | Indicates whether the child protection is active (circuit 1). |
| power-switch-2     | Switch        | &#9745;  | Switches the light on or off (circuit 2).                     |
| child-protection-2 | Switch        | &#9745;  | Indicates whether the child protection is active (circuit 2). |

### Motion Detector

Detects every movement through an intelligent combination of passive infra-red technology and an additional temperature sensor.

**Thing Type ID**: `motion-detector`

| Channel Type ID | Item Type | Writable | Description                    |
| --------------- | --------- | :------: | ------------------------------ |
| latest-motion   | DateTime  | &#9744;  | The date of the latest motion. |
| illuminance     | Number    | &#9744;  | The illuminance level measured by the sensor as integer value in the range 0 to 1000. Note that the sensor only reports the value if the motion light service is activated or if the illuminance state is used in a scenario trigger condition. |
| battery-level   | Number    | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery     | Switch    | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`). |

### Shutter Control

Control of your shutter to take any position you desire.

**Thing Type ID**: `shutter-control`

| Channel Type ID | Item Type     | Writable | Description                              |
| --------------- | ------------- | :------: | ---------------------------------------- |
| level           | Rollershutter | &#9745;  | Current open ratio (0 to 100, Step 0.5). |

### Shutter Control II

This thing type is used if Light/Shutter Control II devices are configured as shutter controls.

**Thing Type ID**: `shutter-control-2`

| Channel Type ID    | Item Type     | Writable | Description                                       |
| ------------------ | ------------- | :------: | ------------------------------------------------- |
| level              | Rollershutter | &#9745;  | Current open ratio (0 to 100, Step 0.5).          |
| signal-strength    | Number        | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| child-protection   | Switch        | &#9745;  | Indicates whether the child protection is active. |
| power-consumption  | Number:Power  | &#9744;  | Current power consumption (W) of the device.      |
| energy-consumption | Number:Energy | &#9744;  | Cumulated energy consumption (Wh) of the device.  |

### Thermostat

Radiator thermostat

**Thing Type ID**: `thermostat`

| Channel Type ID       | Item Type            | Writable | Description                                    |
| --------------------- | -------------------- | :------: | ---------------------------------------------- |
| temperature           | Number:Temperature   | &#9744;  | Current measured temperature.                  |
| valve-tappet-position | Number:Dimensionless | &#9744;  | Current open ratio of valve tappet (0 to 100). |
| child-lock            | Switch               | &#9745;  | Indicates if child lock is active.             |
| silent-mode           | Switch               | &#9745;  | Enables or disables silent mode on thermostats. When enabled, the battery usage is higher. |
| battery-level         | Number               | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery           | Switch               | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`). |

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
| battery-level   | Number               | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery     | Switch               | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`). |

### Relay

The smart switching relay is your universal all-rounder for smart switching.

**Thing Type ID**: `relay`

| Channel Type ID         | Item Type   | Writable | Description                                                                        |
| ----------------------- | ----------- | :------: | ---------------------------------------------------------------------------------- |
| signal-strength         | Number      | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| child-protection        | Switch      | &#9745;  | Indicates whether the child protection is active.                                  |
| power-switch            | Switch      | &#9745;  | Switches the relay on or off. Only available if the relay is in power switch mode. |
| impulse-switch          | Switch      | &#9745;  | Channel to send impulses by means of `ON` events. After the time specified by `impulse-length`, the relay will switch off automatically and the state will be reset to `OFF`. Only available if the relay is in impulse switch mode.  |
| impulse-length          | Number:Time | &#9745;  | Channel to configure how long the relay will stay on after receiving an impulse switch event. If raw numbers (without time unit) are provided, the default unit is tenth seconds (deciseconds), e.g. 15 means 1.5 seconds. If quantities with time units are provided, the quantity will be converted to deciseconds internally, discarding any fraction digits that are more precise than expressible in whole deciseconds (e.g. 1.58 seconds will be converted to 15 ds). Only available if the relay is in impulse switch mode. |
| instant-of-last-impulse | DateTime    | &#9744;  | Timestamp indicating when the last impulse was triggered. Only available if the relay is in impulse switch mode. |

If the device mode is changed from power switch to impulse switch mode or vice versa, the corresponding thing has to be deleted and re-added in openHAB.

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

### Smart Bulb

A smart bulb connected to the bridge via Zigbee such as a Ledvance Smart+ bulb.

**Thing Type ID**: `smart-bulb`

| Channel Type ID  | Item Type | Writable | Description                                                    |
| ---------------- | --------- | :------: | -------------------------------------------------------------- |
| power-switch     | Switch    | &#9745;  | Switches the light on or off.                                  |
| brightness       | Dimmer    | &#9745;  | Regulates the brightness on a percentage scale from 0 to 100%. |
| color            | Color     | &#9745;  | The color of the emitted light.                                |

### Smoke Detector

The smoke detector warns you in case of fire.

**Thing Type ID**: `smoke-detector`

| Channel Type ID  | Item Type | Writable | Description                                                                                                                                                                                                                             |
| ---------------- | --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| alarm            | String    | &#9745;  | Alarm state of the smoke detector. Possible values to read are: `IDLE_OFF`, `PRIMARY_ALARM`, `SECONDARY_ALARM` and `INTRUSION_ALARM`. Possible values to write are: `INTRUSION_ALARM_ON_REQUESTED` and `INTRUSION_ALARM_OFF_REQUESTED`. |
| smoke-check      | String    | &#9745;  | State of the smoke check. Also used to request a new smoke check.                                                                                                                                                                       |
| battery-level    | Number    | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`.             |
| low-battery      | Switch    | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                              |

### Smoke Detector II

The smoke detector warns you in case of fire.

**Thing Type ID**: `smoke-detector-2`

| Channel Type ID | Item Type | Writable | Description                                                                                                                                                                                                                             |
| --------------- | --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| alarm           | String    | &#9745;  | Alarm state of the smoke detector. Possible values to read are: `IDLE_OFF`, `PRIMARY_ALARM`, `SECONDARY_ALARM` and `INTRUSION_ALARM`. Possible values to write are: `INTRUSION_ALARM_ON_REQUESTED` and `INTRUSION_ALARM_OFF_REQUESTED`. |
| smoke-check     | String    | &#9745;  | State of the smoke check. Also used to request a new smoke check.                                                                                                                                                                       |
| battery-level   | Number    | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`.             |
| low-battery     | Switch    | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                              |
| signal-strength | Number    | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                                         |

### User-defined States

User-defined states enable automations to be better adapted to specific needs and everyday situations.
Individual states can be activated/deactivated and can be used as triggers, conditions and actions in automations.

**Thing Type ID**: `user-defined-state`

| Channel Type ID | Item Type | Writable | Description                                |
|-----------------|-----------| :------: |--------------------------------------------|
| user-state      | Switch    | &#9745;  | Switches the User-defined state on or off. |

### Universal Switch

A universally configurable switch with two buttons.

**Thing Type ID**: `universal-switch`

| Channel Type ID     | Item Type            | Writable | Description                               |
| ------------------- | -------------------- | :------: | ----------------------------------------- |
| key-code            | Number:Dimensionless | &#9744;  | Integer code of the key that was pressed. |
| key-name            | String               | &#9744;  | Name of a key pressed on a device. Possible values for Universal Switch: `LOWER_BUTTON`, `UPPER_BUTTON`. |
| key-event-type      | String               | &#9744;  | Indicates how the key was pressed. Possible values are `PRESS_SHORT`, `PRESS_LONG` and `PRESS_LONG_RELEASED`. |
| key-event-timestamp | DateTime             | &#9744;  | Timestamp indicating when the key was pressed. |

### Universal Switch II

A universally configurable switch with four buttons.

**Thing Type ID**: `universal-switch-2`

| Channel Type ID     | Item Type            | Writable | Description                               |
| ------------------- | -------------------- | :------: | ----------------------------------------- |
| key-code            | Number:Dimensionless | &#9744;  | Integer code of the key that was pressed. |
| key-name            | String               | &#9744;  | Name of the key that was pressed. Possible values for Universal Switch II: `LOWER_LEFT_BUTTON`, `LOWER_RIGHT_BUTTON`, `UPPER_LEFT_BUTTON`, `UPPER_RIGHT_BUTTON`. |
| key-event-type      | String               | &#9744;  | Indicates how the key was pressed. Possible values are `PRESS_SHORT`, `PRESS_LONG` and `PRESS_LONG_RELEASED`. |
| key-event-timestamp | DateTime             | &#9744;  | Timestamp indicating when the key was pressed. |

### Water Detector

Smart water leakage detector.

**Thing Type ID**: `water-detector`

| Channel Type ID            | Item Type | Writable | Description                                       |
| -------------------------- | --------- | :------: | ------------------------------------------------- |
| battery-level              | Number    | &#9744;  | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery                | Switch    | &#9744;  | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |
| signal-strength            | Number    | &#9744;  | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                             |
| water-leakage              | Switch    | &#9744;  | Indicates whether a water leakage was detected.               |
| push-notifications         | Switch    | &#9745;  | Indicates whether push notifications are enabled.             |
| acoustic-signals           | Switch    | &#9745;  | Indicates whether acoustic signals are enabled.               |
| water-leakage-sensor-check | String    | &#9744;  | Provides the result of the last water leakage sensor check.   |
| sensor-moved               | Trigger   | &#9744;  | Triggered when the sensor is moved.                           |

## Limitations

No major limitation known.
Check list of [openhab issues with "boshshc"](https://github.com/openhab/openhab-addons/issues?q=is%3Aissue+boschshc+)

## Discovery

Bridge discovery is supported via mDNS.
Things discovery is started after successful pairing.

Configuration via configuration files or UI supported too (see below).

## Bridge Configuration

You need to provide the IP address and the system password of your Bosch Smart Home Controller.
The IP address of the controller is visible in the Bosch Smart Home Mobile App (More -> System -> Smart Home Controller) or in your network router UI.
The system password is set by you during your initial registration steps in the _Bosch Smart Home App_.

A keystore file with a self-signed certificate is created automatically.
This certificate is used for pairing between the Bridge and the Bosch Smart Home Controller.

On the Smart Home Controller Bridge, paring mode must be enabled after the `shc` Thing was created:

- Smart Home Controller: _Press and hold the button until the LED starts blinking to enable pairing mode_.
- Smart Home Controller II: _Press the button briefly to enable pairing mode_.

## Getting the device IDs

Bosch IDs for found devices are displayed in the openHAB log on bootup (`OPENHAB_FOLDER/userdata/logs/openhab.log`)

The log can also be called using the following command.

```bash
tail -f /var/log/openhab/openhab.log /var/log/openhab/events.log
```

Alternatively, the log can be viewed using the openHAB Log Viewer (frontail) via <http://openhab:9001>.

Example:

```bash
2023-03-20 20:30:48.026 [INFO ] [g.discovery.internal.PersistentInbox] - Added new thing 'boschshc:security-camera-eyes:yourBridgeName:hdm_Cameras_XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX' to inbox.
2023-03-20 20:30:48.026 [INFO ] [g.discovery.internal.PersistentInbox] - Added new thing 'boschshc:smoke-detector:yourBridgeName:hdm_HomeMaticIP_XXXXXXXXXXXXXXXXXXXXXXXX' to inbox.
2023-03-20 20:30:48.027 [INFO ] [g.discovery.internal.PersistentInbox] - Added new thing 'boschshc:twinguard:yourBridgeName:hdm_ZigBee_XXXXXXXXXXXXXXXX' to inbox.
2023-03-20 20:30:48.028 [INFO ] [g.discovery.internal.PersistentInbox] - Added new thing 'boschshc:smart-bulb:yourBridgeName:hdm_PhilipsHueBridge_HueLight_XXXXXXXXXXXXXXXX-XX_XXXXXXXXXXXX' to inbox.
```

## Thing Configuration

You define your Bosch devices by adding them either to a `.things` file in your `$OPENHAB_CONF/things` folder like this:

```java
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

```java
Switch Bosch_Bathroom    "Bath Room"    { channel="boschshc:in-wall-switch:1:bathroom:power-switch" }
Switch Bosch_Bedroom     "Bed Room"     { channel="boschshc:in-wall-switch:1:bedroom:power-switch" }
Switch Bosch_Kitchen     "Kitchen"      { channel="boschshc:in-wall-switch:1:kitchen:power-switch" }
Switch Bosch_Corridor    "Corridor"     { channel="boschshc:in-wall-switch:1:corridor:power-switch" }
Switch Bosch_Living_Room "Living Room"  { channel="boschshc:in-wall-switch:1:livingroom:power-switch" }

Switch Bosch_Lelit       "Lelit"        { channel="boschshc:in-wall-switch:1:coffeemachine:power-switch" }
```

Or by adding them via UI: Settings -> Items -> "+".
