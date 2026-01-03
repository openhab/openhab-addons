# Bosch Smart Home Binding

Binding for Bosch Smart Home devices.

- [Bosch Smart Home Binding](#bosch-smart-home-binding)
  - [Supported Things](#supported-things)
    - [Smart Home Controller](#smart-home-controller)
    - [In-Wall Switch](#in-wall-switch)
    - [Compact Smart Plug](#compact-smart-plug)
    - [Twinguard Smoke Detector](#twinguard-smoke-detector)
    - [Door/Window Contact](#door-window-contact)
    - [Door/Window Contact II](#door-window-contact-ii)
    - [Door/Window Contact II Plus](#door-window-contact-ii-plus)
    - [Door/Window Contact II [+M]](#door-window-contact-ii-m)
    - [Light Control II](#light-control-ii)
    - [Motion Detector](#motion-detector)
    - [Presence Simulation](#presence-simulation)
    - [Shutter Control](#shutter-control)
    - [Shutter Control II](#shutter-control-ii)
    - [Radiator Thermostat](#radiator-thermostat)
    - [Radiator Thermostat II](#radiator-thermostat-ii)
    - [Radiator Thermostat II [+M]](#radiator-thermostat-ii-m)
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

| Channel ID         | Item Type | Writable | Description                                                             |
|--------------------|-----------|:--------:|-------------------------------------------------------------------------|
| scenario-triggered | String    | no       | Name of the triggered scenario (e.g. by the Universal Switch Flex)      |
| trigger-scenario   | String    | yes      | Name of a scenario to be triggered on the Bosch Smart Home Controller.  |

### In-Wall Switch

A simple light control.

**Thing Type ID**: `in-wall-switch`

| Channel ID         | Item Type     | Writable | Description                                      |
| ------------------ | ------------- | :------: | ------------------------------------------------ |
| power-switch       | Switch        | yes      | Current state of the switch.                     |
| power-consumption  | Number:Power  | no       | Current power consumption (W) of the device.     |
| energy-consumption | Number:Energy | no       | Cumulated energy consumption (Wh) of the device. |

### Compact Smart Plug

A compact smart plug with energy monitoring capabilities.

**Thing Type ID**: `smart-plug-compact`

| Channel ID         | Item Type     | Writable | Description                                      |
| ------------------ | ------------- | :------: | ------------------------------------------------ |
| power-switch       | Switch        | yes      | Current state of the switch.                     |
| power-consumption  | Number:Power  | no       | Current power consumption (W) of the device.     |
| energy-consumption | Number:Energy | no       | Cumulated energy consumption (Wh) of the device. |

### Dimmer

Smart dimmer capable of controlling any dimmable lamp.

**Thing Type ID**: `dimmer`

| Channel ID       | Item Type     | Writable | Description                                                    |
| ---------------- | ------------- | :------: | -------------------------------------------------------------- |
| power-switch     | Switch        | yes      | Current state of the switch.                                   |
| brightness       | Dimmer        | yes      | Regulates the brightness on a percentage scale from 0 to 100%. |
| signal-strength  | Number        | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| child-protection | Switch        | yes      | Indicates whether the child protection is active.              |

### Twinguard Smoke Detector

The Twinguard smoke detector warns you in case of fire and constantly monitors the air.

**Thing Type ID**: `twinguard`

| Channel ID         | Item Type            | Writable | Description                                                                                       |
| ------------------ | -------------------- | :------: | ------------------------------------------------------------------------------------------------- |
| temperature        | Number:Temperature   | no       | Current measured temperature.                                                                     |
| temperature-rating | String               | no       | Rating of the currently measured temperature.                                                     |
| humidity           | Number:Dimensionless | no       | Current measured humidity (0 to 100).                                                             |
| humidity-rating    | String               | no       | Rating of current measured humidity.                                                              |
| purity             | Number:Dimensionless | no       | Purity of the air (ppm). Range from 500 to 5500 ppm. A higher value indicates a higher pollution. |
| purity-rating      | String               | no       | Rating of current measured purity.                                                                |
| air-description    | String               | no       | Overall description of the air quality.                                                           |
| combined-rating    | String               | no       | Combined rating of the air quality.                                                               |
| battery-level      | Number               | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery        | Switch               | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`). |
| smoke-check        | String               | yes      | State of the smoke check. Also used to request a new smoke check.                                 |

### Door/Window Contact

Detects open windows and doors.

**Thing Type ID**: `window-contact`

| Channel ID    | Item Type | Writable | Description                  |
| ------------- | --------- | :------: | ---------------------------- |
| contact       | Contact   | no       | Contact state of the device. |
| battery-level | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery   | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`). |

### Door/Window Contact II

Detects open windows and doors and features an additional button.

**Thing Type ID**: `window-contact-2`

| Channel ID      | Item Type | Writable | Description                                                                                                                                                                                                                 |
| --------------- | --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| contact         | Contact   | no       | Contact state of the device.                                                                                                                                                                                                |
| battery-level   | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery     | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |
| bypass          | Switch    | no       | Indicates whether the device is currently bypassed. Possible values are `ON`,`OFF` and `UNDEF` if the bypass state cannot be determined.                                                                                    |
| signal-strength | Number    | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                             |

### Door/Window Contact II Plus

Detects open windows and doors, provides a configurable button and a vibration sensor.

**Thing Type ID**: `window-contact-2-plus`

| Channel ID                   | Item Type | Writable | Description                                                                                                                                                                                                                 |
| -----------------------------| --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| contact                      | Contact   | no       | Contact state of the device.                                                                                                                                                                                                |
| battery-level                | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery                  | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |
| bypass                       | Switch    | no       | Indicates whether the device is currently bypassed. Possible values are `ON`,`OFF` and `UNDEF` if the bypass state cannot be determined.                                                                                    |
| signal-strength              | Number    | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                             |
| vibration-sensor-enabled     | Switch    | yes      | Channel to enable or disable the vibration sensor.                                                                                                                                                                          |
| vibration-sensor-sensitivity | String    | yes      | The sensitivity of the vibration sensor. Possible values are `VERY_HIGH`, `HIGH`, `MEDIUM`, `LOW` and `VERY_LOW`.                                                                                                           |
| vibration-sensor-state       | String    | no       | Indicates whether vibrations were detected by the sensor. Possible values are `NO_VIBRATION`, `VIBRATION_DETECTED` and `UNKNOWN`.                                                                                           |

### Door/Window Contact II [+M]

Detects open windows and doors and features an additional button. This version of the sensor supports the Matter standard.

**Thing Type ID**: `window-contact-2-matter`

| Channel ID      | Item Type | Writable | Description                                                                                                                                                                                                                 |
| --------------- | --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| contact         | Contact   | no       | Contact state of the device.                                                                                                                                                                                                |
| battery-level   | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery     | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |
| bypass          | Switch    | no       | Indicates whether the device is currently bypassed. Possible values are `ON`,`OFF` and `UNDEF` if the bypass state cannot be determined.                                                                                    |
| signal-strength | Number    | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                             |

### Light Control II

This thing type is used if Light/Shutter Control II devices are configured as light controls.

**Thing Type ID**: `light-control-2`

| Channel ID         | Item Type     | Writable | Description                                                   |
| ------------------ | ------------- | :------: | ------------------------------------------------------------- |
| signal-strength    | Number        | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| power-consumption  | Number:Power  | no       | Current power consumption (W) of the device.                  |
| energy-consumption | Number:Energy | no       | Cumulated energy consumption (Wh) of the device.              |
| power-switch-1     | Switch        | yes      | Switches the light on or off (circuit 1).                     |
| child-protection-1 | Switch        | yes      | Indicates whether the child protection is active (circuit 1). |
| power-switch-2     | Switch        | yes      | Switches the light on or off (circuit 2).                     |
| child-protection-2 | Switch        | yes      | Indicates whether the child protection is active (circuit 2). |

### Motion Detector

Detects every movement through an intelligent combination of passive infra-red technology and an additional temperature sensor.

**Thing Type ID**: `motion-detector`

| Channel ID    | Item Type | Writable | Description                    |
| ------------- | --------- | :------: | ------------------------------ |
| latest-motion | DateTime  | no       | The date of the latest motion. |
| illuminance   | Number    | no       | The illuminance level measured by the sensor as integer value in the range 0 to 1000. Note that the sensor only reports the value if the motion light service is activated or if the illuminance state is used in a scenario trigger condition. |
| battery-level | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery   | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`). |

### Presence Simulation

Simulates that someone is home.

**Thing Type ID**: `presence-simulation`

| Channel ID                  | Item Type | Writable | Description                                  |
| --------------------------- | --------- | :------: | -------------------------------------------- |
| presence-simulation-enabled | Switch    | yes      | Enables or disables the presence simulation. |

### Shutter Control

Control of your shutter to take any position you desire.

**Thing Type ID**: `shutter-control`

| Channel ID | Item Type     | Writable | Description                              |
| ---------- | ------------- | :------: | ---------------------------------------- |
| level      | Rollershutter | yes      | Current open ratio (0 to 100, Step 0.5). |

### Shutter Control II

This thing type is used if Light/Shutter Control II devices are configured as shutter controls.

**Thing Type ID**: `shutter-control-2`

| Channel ID         | Item Type     | Writable | Description                                       |
| ------------------ | ------------- | :------: | ------------------------------------------------- |
| level              | Rollershutter | yes      | Current open ratio (0 to 100, Step 0.5).          |
| signal-strength    | Number        | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| child-protection   | Switch        | yes      | Indicates whether the child protection is active. |
| power-consumption  | Number:Power  | no       | Current power consumption (W) of the device.      |
| energy-consumption | Number:Energy | no       | Cumulated energy consumption (Wh) of the device.  |

### Radiator Thermostat

Radiator thermostat

**Thing Type ID**: `thermostat`

| Channel ID            | Item Type            | Writable | Description                                                                                                                                                                                                                 |
| --------------------- | -------------------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| temperature           | Number:Temperature   | no       | Current measured temperature.                                                                                                                                                                                               |
| valve-tappet-position | Number:Dimensionless | no       | Current open ratio of valve tappet (0 to 100).                                                                                                                                                                              |
| child-lock            | Switch               | yes      | Indicates if child lock is active.                                                                                                                                                                                          |
| silent-mode           | Switch               | yes      | Enables or disables silent mode on thermostats. When enabled, the battery usage is higher.                                                                                                                                  |
| battery-level         | Number               | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery           | Switch               | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |

### Radiator Thermostat II

Second generation radiator thermostat

**Thing Type ID**: `thermostat-2`

| Channel ID            | Item Type            | Writable | Description                                                                                                                                                                                                                 |
| --------------------- | -------------------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| temperature           | Number:Temperature   | no       | Current measured temperature.                                                                                                                                                                                               |
| valve-tappet-position | Number:Dimensionless | no       | Current open ratio of valve tappet (0 to 100).                                                                                                                                                                              |
| child-lock            | Switch               | yes      | Indicates if child lock is active.                                                                                                                                                                                          |
| silent-mode           | Switch               | yes      | Enables or disables silent mode on thermostats. When enabled, the battery usage is higher.                                                                                                                                  |
| battery-level         | Number               | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery           | Switch               | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |

### Radiator Thermostat II [+M]

Second generation radiator thermostat with Matter support.

**Thing Type ID**: `thermostat-2-matter`

| Channel ID            | Item Type            | Writable | Description                                                                                                                                                                                                                 |
| --------------------- | -------------------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| temperature           | Number:Temperature   | no       | Current measured temperature.                                                                                                                                                                                               |
| valve-tappet-position | Number:Dimensionless | no       | Current open ratio of valve tappet (0 to 100).                                                                                                                                                                              |
| child-lock            | Switch               | yes      | Indicates if child lock is active.                                                                                                                                                                                          |
| silent-mode           | Switch               | yes      | Enables or disables silent mode on thermostats. When enabled, the battery usage is higher.                                                                                                                                  |
| battery-level         | Number               | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery           | Switch               | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |

### Climate Control

A virtual device which controls up to six Bosch Smart Home radiator thermostats in a room.

**Thing Type ID**: `climate-control`

| Channel ID           | Item Type          | Writable | Description                   |
| -------------------- | ------------------ | :------: | ----------------------------- |
| temperature          | Number:Temperature | no       | Current measured temperature. |
| setpoint-temperature | Number:Temperature | yes      | Desired temperature.          |

### Wall Thermostat

Display of the current room temperature as well as the relative humidity in the room.

**Thing Type ID**: `wall-thermostat`

| Channel ID    | Item Type            | Writable | Description                           |
| ------------- | -------------------- | :------: | ------------------------------------- |
| temperature   | Number:Temperature   | no       | Current measured temperature.         |
| humidity      | Number:Dimensionless | no       | Current measured humidity (0 to 100). |
| battery-level | Number               | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery   | Switch               | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`). |

### Relay

The smart switching relay is your universal all-rounder for smart switching.

**Thing Type ID**: `relay`

| Channel ID              | Item Type   | Writable | Description                                                                        |
| ----------------------- | ----------- | :------: | ---------------------------------------------------------------------------------- |
| signal-strength         | Number      | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength). |
| child-protection        | Switch      | yes      | Indicates whether the child protection is active.                                  |
| power-switch            | Switch      | yes      | Switches the relay on or off. Only available if the relay is in power switch mode. |
| impulse-switch          | Switch      | yes      | Channel to send impulses by means of `ON` events. After the time specified by `impulse-length`, the relay will switch off automatically and the state will be reset to `OFF`. Only available if the relay is in impulse switch mode.  |
| impulse-length          | Number:Time | yes      | Channel to configure how long the relay will stay on after receiving an impulse switch event. If raw numbers (without time unit) are provided, the default unit is tenth seconds (deciseconds), e.g. 15 means 1.5 seconds. If quantities with time units are provided, the quantity will be converted to deciseconds internally, discarding any fraction digits that are more precise than expressible in whole deciseconds (e.g. 1.58 seconds will be converted to 15 ds). Only available if the relay is in impulse switch mode. |
| instant-of-last-impulse | DateTime    | no       | Timestamp indicating when the last impulse was triggered. Only available if the relay is in impulse switch mode. |

If the device mode is changed from power switch to impulse switch mode or vice versa, the corresponding thing has to be deleted and re-added in openHAB.

### Security Camera 360

Indoor security camera with 360° view and motion detection.

**Thing Type ID**: `security-camera-360`

| Channel ID          | Item Type            | Writable | Description                                                        |
| ------------------- | -------------------- | :------: | ------------------------------------------------------------------ |
| privacy-mode        | Switch               | yes      | If privacy mode is enabled, the camera is disabled and vice versa. |
| camera-notification | Switch               | yes      | Enables or disables notifications for the camera.                  |

### Security Camera Eyes

Outdoor security camera with motion detection and light.

**Thing Type ID**: `security-camera-eyes`

| Channel ID          | Item Type            | Writable | Description                                                        |
| ------------------- | -------------------- | :------: | ------------------------------------------------------------------ |
| privacy-mode        | Switch               | yes      | If privacy mode is enabled, the camera is disabled and vice versa. |
| camera-notification | Switch               | yes      | Enables or disables notifications for the camera.                  |

### Intrusion Detection System

Allows to retrieve notifications in case of intrusions. The system can be armed and disarmed and alarms can be muted.

**Thing Type ID**: `intrusion-detection-system`

| Channel ID                   | Item Type            | Writable | Description                                                    |
| ---------------------------- | -------------------- | :------: | -------------------------------------------------------------- |
| system-availability          | Switch               | no       | Indicates whether the intrusion detection system is available. |
| arming-state                 | String               | no       | Read-only channel to retrieve the current arming state. Possible values are `SYSTEM_ARMING`, `SYSTEM_ARMED` and `SYSTEM_DISARMED`. |
| alarm-state                  | String               | no       | Read-only channel to retrieve the current alarm state. Possible values are `ALARM_OFF`, `PRE_ALARM`, `ALARM_ON`, `ALARM_MUTED` and `UNKNOWN`. |
| active-configuration-profile | String               | no       | The name of the active configuration profile used for the intrusion detection system. |
| arm-action                   | String               | yes      | Arms the intrusion detection system using the given profile ID (default is "0"). |
| disarm-action                | Switch               | yes      | Disarms the intrusion detection system when an ON command is received. |
| mute-action                  | Switch               | yes      | Mutes the alarm when an ON command is received. |

### Smart Bulb

A smart bulb connected to the bridge via Zigbee such as a Ledvance Smart+ bulb.

**Thing Type ID**: `smart-bulb`

| Channel ID   | Item Type | Writable | Description                                                    |
| ------------ | --------- | :------: | -------------------------------------------------------------- |
| power-switch | Switch    | yes      | Switches the light on or off.                                  |
| brightness   | Dimmer    | yes      | Regulates the brightness on a percentage scale from 0 to 100%. |
| color        | Color     | yes      | The color of the emitted light.                                |

### Smoke Detector

The smoke detector warns you in case of fire.

**Thing Type ID**: `smoke-detector`

| Channel ID    | Item Type | Writable | Description                                                                                                                                                                                                                             |
| --------------| --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| alarm         | String    | yes      | Alarm state of the smoke detector. Possible values to read are: `IDLE_OFF`, `PRIMARY_ALARM`, `SECONDARY_ALARM` and `INTRUSION_ALARM`. Possible values to write are: `INTRUSION_ALARM_ON_REQUESTED` and `INTRUSION_ALARM_OFF_REQUESTED`. |
| smoke-check   | String    | yes      | State of the smoke check. Also used to request a new smoke check.                                                                                                                                                                       |
| battery-level | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`.             |
| low-battery   | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                              |

### Smoke Detector II

The smoke detector warns you in case of fire.

**Thing Type ID**: `smoke-detector-2`

| Channel ID      | Item Type | Writable | Description                                                                                                                                                                                                                             |
| --------------- | --------- | :------: | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| alarm           | String    | yes      | Alarm state of the smoke detector. Possible values to read are: `IDLE_OFF`, `PRIMARY_ALARM`, `SECONDARY_ALARM` and `INTRUSION_ALARM`. Possible values to write are: `INTRUSION_ALARM_ON_REQUESTED` and `INTRUSION_ALARM_OFF_REQUESTED`. |
| smoke-check     | String    | yes      | State of the smoke check. Also used to request a new smoke check.                                                                                                                                                                       |
| battery-level   | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`.             |
| low-battery     | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                              |
| signal-strength | Number    | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                                         |

### User-defined States

User-defined states enable automations to be better adapted to specific needs and everyday situations.
Individual states can be activated/deactivated and can be used as triggers, conditions and actions in automations.

**Thing Type ID**: `user-defined-state`

| Channel ID | Item Type | Writable | Description                                |
|------------|-----------| :------: |--------------------------------------------|
| user-state | Switch    | yes      | Switches the User-defined state on or off. |

### Universal Switch

A universally configurable switch with two buttons.

**Thing Type ID**: `universal-switch`

| Channel ID          | Item Type            | Writable | Description                               |
| ------------------- | -------------------- | :------: | ----------------------------------------- |
| key-code            | Number:Dimensionless | no       | Integer code of the key that was pressed. |
| key-name            | String               | no       | Name of a key pressed on a device. Possible values for Universal Switch: `LOWER_BUTTON`, `UPPER_BUTTON`. |
| key-event-type      | String               | no       | Indicates how the key was pressed. Possible values are `PRESS_SHORT`, `PRESS_LONG` and `PRESS_LONG_RELEASED`. |
| key-event-timestamp | DateTime             | no       | Timestamp indicating when the key was pressed. |

### Universal Switch II

A universally configurable switch with four buttons.

**Thing Type ID**: `universal-switch-2`

| Channel ID          | Item Type            | Writable | Description                               |
| ------------------- | -------------------- | :------: | ----------------------------------------- |
| key-code            | Number:Dimensionless | no       | Integer code of the key that was pressed. |
| key-name            | String               | no       | Name of the key that was pressed. Possible values for Universal Switch II: `LOWER_LEFT_BUTTON`, `LOWER_RIGHT_BUTTON`, `UPPER_LEFT_BUTTON`, `UPPER_RIGHT_BUTTON`. |
| key-event-type      | String               | no       | Indicates how the key was pressed. Possible values are `PRESS_SHORT`, `PRESS_LONG` and `PRESS_LONG_RELEASED`. |
| key-event-timestamp | DateTime             | no       | Timestamp indicating when the key was pressed. |

### Water Detector

Smart water leakage detector.

**Thing Type ID**: `water-detector`

| Channel ID                 | Item Type | Writable | Description                                       |
| -------------------------- | --------- | :------: | ------------------------------------------------- |
| battery-level              | Number    | no       | Current battery level percentage as integer number. Bosch-specific battery levels are mapped to numbers as follows: `OK`: 100, `LOW_BATTERY`: 10, `CRITICAL_LOW`: 1, `CRITICALLY_LOW_BATTERY`: 1, `NOT_AVAILABLE`: `UNDEF`. |
| low-battery                | Switch    | no       | Indicates whether the battery is low (`ON`) or OK (`OFF`).                                                                                                                                                                  |
| signal-strength            | Number    | no       | Communication quality between the device and the Smart Home Controller. Possible values range between 0 (unknown) and 4 (best signal strength).                                                                             |
| water-leakage              | Switch    | no       | Indicates whether a water leakage was detected.               |
| push-notifications         | Switch    | yes      | Indicates whether push notifications are enabled.             |
| acoustic-signals           | Switch    | yes      | Indicates whether acoustic signals are enabled.               |
| water-leakage-sensor-check | String    | no       | Provides the result of the last water leakage sensor check.   |
| sensor-moved               | Trigger   | no       | Triggered when the sensor is moved.                           |

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
