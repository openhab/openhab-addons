# Vera Binding

Vera (lite/edge/plus/etc version) is a popular Z-Wave controller. Vera binding automatically finds the addresses of all such controllers on the local network, and through HTTP api loads all their devices and scenes.

## Supported Things

Vera binding provides three thing types: controller, devices and scenes. There may be more than one controller.
Each device usually has one channel. But some devices has additional channels, such as battery level, power consumption and others.
Each scene has only one channel, to start scene.

## Discovery

A discovery service for Vera controller scans local network and must always be started manually.

Another discovery service provides available devices. The device discovery service is performed at a specified interval, but can also be started manually.

Note: devices with type "controller" and "interface" not loaded.

## Binding Configuration

No configuration is necessary.

## Thing Configuration

The textual configuration (via \*.thing files) isn't useful because the resulting elements are read-only. But the configuration and properties of things are changed at runtime and channels are dynamically added and removed.

### Vera Controller (Bridge)

| Name                | Type          | Description                                                                                              |
|---------------------|---------------|----------------------------------------------------------------------------------------------------------|
| veraIpAddress       | string        | The IP address or hostname of the Vera controller.                                                       |
| veraPort            | int           | The port of the Vera controller                                                                          |
| pollingInterval     | int           | Refresh all values (name, room, state) for all devices and scenes.                                       |
| clearNames          | boolean       | Remove digits, slashes and double spaces from all names. Good for some voice recognition services.       |
| defaulRoomName      | string        | Default name for room, if no room specified.                                                             |
| homekitIntegration  | boolean       | If enabled, homekit tags are created for all supported devices. Please read Homekit add-on instructions. |

### Vera Device

| Name            | Type          | Description              |
|-----------------|---------------|--------------------------|
| deviceId        | string        | Id of the Vera device    |

### Vera Scene

| Name            | Type          | Description              |
|-----------------|---------------|--------------------------|
| sceneId         | string        | Id of the Vera scene     |

## Channels

### Channels with detailed information for the devices

The following channels are currently supported.

| Channel Type ID           | Item Type     | Category      | Vera category-subcategory    |
| ------------------------- | ------------- | ------------- | ---------------------------- |
| switchMultilevel          | Dimmer        | Switch        | 2-(1-3): Dimmable Light      |
| switchColor               | Color         | ColorLight    | 2-4: Dimmable Light - TODO   |
| switchBinary              | Switch        | Switch        | 3: Switch                    |
| sensorDoorWindow          | Contact       | Contact       | 4-1: Security Sensor         |
| sensorFlood               | Switch        | Water         | 4-2: Security Sensor         |
| sensorMotion              | Switch        | Motion        | 4-3: Security Sensor         |
| sensorSmoke               | Switch        | Smoke         | 4-4: Security Sensor         |
| sensorCo                  | Switch        | Gas           | 4-5: Security Sensor         |
| sensorBinary              | Switch        | Switch        | 4-6: Security Sensor         |
| doorlock                  | Switch        | Door          | 7: Door Lock                 |
| switchBlinds              | Rollershutter | Blinds        | 8: Window Covering           |
| sensorBinary              | Switch        | Switch        | 12: Generic Sensor           |
| switchBinary              | Switch        | Switch        | 14: Scene Controller         |
| sensorHumidity            | Number        | Humidity      | 16: Humidity Sensor          |
| sensorTemperature         | Number        | Temperature   | 17: Temperature Sensor       |
| sensorLuminosity          | Number        | Light         | 18: Light Sensor             |
| sensorEnergy              | Number        | Energy        | 21: Power Meter              |
| sensorUltraviolet         | Number        | Light         | 28: UV Sensor                |
| sensorMeterKWh            | Number        | Energy        | no category                  |
| sensorMeterW              | Number        | Energy        | no category                  |
| thermostatMode            | Switch        | Temperature   | TODO                         |
| thermostatSetPoint        | Number        | Temperature   | TODO                         |
| thermostatModeCC          | Number        | Temperature   | TODO                         |
| sceneButton               | Switch        | Switch        |                              |

### Unsupported Vera device categories

- 6: Camera
- 9: Remote Control
- 10: IR Transmitter
- 11: Generic IO
- 13: Serial Port
- 15: A/V
- 19: Z-Wave Interface
- 20: Insteon Interface
- 22: Alarm Panel
- 23: Alarm Partition
- 24: Siren
- 25: Weather
- 26: Philips Controller
- 27: Appliance

The integration for most of these types isn't planned.

Thermostats and colored lamps are also not supported, but will be implemented later.

## Locations

The locations are loaded during the discovery and based on the Vera devices rooms.

## Developer stuff

### Known issues

No known issues at that moment.

### Features

- Discovery of the Vera controller and devices
- Control of the Vera devices in openHAB
- Receive updates of sensor data and actuator states in openHAB
