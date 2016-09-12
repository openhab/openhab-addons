# Z-Way Binding

Z-Way is a home automation software to configure and control Z-Wave networks. The ZAutomation interface provides all Z-Wave devices and handles incoming commands. The Z-Way Binding uses this HTTP interface to load all devices and make them available during the discovery process. Besides continuous polling there is the opportunity to register openHAB items in Z-Way as observer (therefore the Z-Way App _OpenHAB Connector_ is currently required).

The Binding uses the Z-Way library for Java ([Github](https://github.com/pathec/ZWay-library-for-Java)).

For more information about Z-Way see [Z-Wave.Me](https://www.z-wave.me/index.php?id=22)

## Supported Things

The Z-Way Binding provides different thing types. The core component is the bridge which represents the Z-Way server. For the integration of devices, two thing types are available. In Z-Way there are devices which represent physical devices and (virtual) devices which are defined in Apps. The difference is that physical devices usually have several functions.
Z-Way server constructs (virtual) devices for each function, such as motion sensors or door contacts. In openHAB, these functions are bundled into physical devices and represented as things with channels for each function. This type of thing is a *Z-Wave Device*. On the other hand all virtual devices are mapped to *Z-Way Virtual Devices* with exactly one channel.

- *Z-Way Server* (Bridge) represents a bridge with general settings and communication tasks.
- *Z-Way Virtual Device* represents one (virtual) device with the corresponding channel. A bridge is necessary as an intermediary between openHAB thing and Z-Way device.
- *Z-Wave Device* represents a device of real world. Each device function will be mapped to a separate channel. The bridge is necessary as an intermediary between openHAB thing and Z-Way device.

## Discovery

A discovery service for Z-Way servers in local network starts immediately after the installation of the binding. Z-Way doesn't support any discovery protocol like UPnP for this purpose. That's why first all IP addresses in local network are checked on port 8083. If the server answers, a ZAutomation request (*/ZAutomation/api/v1/status*) is performed to ensure, the found server runs Z-Way.

Another discovery service provides available devices (a configured bridge is necessary). Both discovery services are performed at a specified interval, but can also be started manually.

## Binding Configuration

No configuration is necessary.

## Thing Configuration

The textual configuration (via \*.thing files) isn't useful because the resulting elements are read-only. But the configuration and properties of things are changed at runtime and channels are dynamically added and removed.

### Z-Way Server (Bridge)

The information about accessing openHAB are needed so that the Observer mechanism works. Besides the username and password all required Z-Way information are found during discovery.

| Configuration Name        | Mandatory | Default | Desciption |
| ------------------------- | --------- | ------- | ---------- |
| openHabAlias              |   |                   | By default, the alias is generated during initialization or configuration update of thing handler. |
| openHabIpAddress          | X | localhost         | The IP address or hostname of the openHAB server. If Z-Way and openHAB are running on the same machine, the default value can be used. |
| openHabPort               |   | 8080              | The port of the openHAB server (0 to 65335) |
| openHabProtocol           |   | http              | Protocol to connect to the openHAB server (http or https) |
| zwayServerIpAddress       | X | localhost         | The IP address or hostname of the Z-Way server. If Z-Way and openHAB are running on the same machine, the default value can be used. |
| zwayServerPort            |   | 8083              | The port of the Z-Way server (0 to 65335) |
| zwayServerProtocol        |   | http              | Protocol to connect to the Z-Way server (http or https) |
| zwayServerUsername        | X | admin             | Username to access the Z-Way server. |
| zwayServerPassword        | X |                   | Password to access the Z-Way server. |
| pollingInterval           |   | 3600              | Refresh device states and registration from Z-Way server in seconds (at least 60). |
| observerMechanismEnabled  |   | true              | The observer functionality is responsible for the item registration as observer in Z-Way. Attention: if disable this option, you have to setup an other synchronization mechanism like MQTT. |

Only the Z-Way server can be configured textual (Attention! *openHabAlias* has to be set because the bridge configuration can not be changed at runtime):

```
Bridge zway:zwayServer:192_168_2_42 [ openHabAlias="development", openHabIpAddress="localhost", openHabPort=8080, openHabProtocol="http", zwayServerIpAddress="localhost", zwayServerPort=8083, zwayServerProtocol="http", zwayServerUsername="admin", zwayServerPassword="admin", pollingInterval=3600, observerMechanismEnabled=true ] {
    // associated things have to be created with the Paper UI
}
```

### Z-Way Virtual Device

| Configuration Name    | Mandatory | Default | Description |
| ------------------    | ----------| ------- | ---------- |
| deviceId              | X         |         | Device ID of virtual device |
| bridge reference      | X         |         |            ||

### Z-Wave Device

| Configuration Name    | Mandatory | Default | Description |
| ------------------    | ----------| ------- | ---------- |
| nodeId                | X         |         | Node ID of the Z-Wave device |
| bridge reference      | X         |         |            ||

## Channels

The following channels are currently supported.

| Channel Type ID | Item Type |
| --------------- | --------- |
| sensorTemperature | Number |
| sensorLuminosity  | Number |
| sensorHumidity    | Number |
| sensorUltraviolet | Number |
| sensorCO2         | Number |
| sensorEnergy      | Number |
| sensorMeterKWh    | Number |
| sensorMeterW      | Number |
| sensorMotion      | Switch |
| switchPowerOutlet | Switch ||

The following channels represent universial channels if no further device information are available, only depending on the Z-Way device types (for available device types see [Z-Way Documentation](http://docs.zwayhomeautomation.apiary.io/#reference/devices/device)).

| Channel Type ID | Item Type |
| --------------- | --------- |
| battery | Number  |
| sensorBinary      | Switch |
| sensorMultilevel  | Number |
| switchBinary      | Switch |
| switchMultilevel  | Number |
| switchControl     | Switch ||

## Full Example

Because textual configuration isn't useful, follow the instructions in the [Getting Started](doc/GETTING_STARTED.md) document.

## Developer stuff

### Structure of Z-Way Binding

![Z-Way Binding](doc/images/Z-Way-Binding.png)

### Features

- Discover Z-Way server (Bridge) and Z-Way devices
- Control the Z-Way devices
- Receive updates in openHAB by a Observer mechanism

### Restrictions

- Z-Way App "OpenHAB Connector" is required. Further versions will contain other mechanisms under usage of the WebSocket implementation of Z-Way or MQTT.
- No configuration of the Z-Wave network is currently possible (like device inclusion or (phyiscal) device configuration.
- Z-Way device types (especially the probe types) supported by openHAB channels with detailed information (scale types and so on) are not complete.
