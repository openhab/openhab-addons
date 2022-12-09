# ELRO Connects Binding

The ELRO Connects binding provides integration with the [ELRO Connects](https://www.elro.eu/en/smart-home) smart home system.

The system uses a Wi-Fi Hub (K1 Connector) to enable communication with various smart home devices.
The devices communicate with the hub using 868MHz RF.
The binding communicates with the ELRO Connects system and K1 Connector using UDP in the local network.
Optionally, the Elro Connects account can be used to retrieve the available K1 Connectors with their properties from the ELRO Connects cloud.

The binding exposes the devices' status and controls to openHAB.
Console commands support adding and configuring devices on the hub.
The K1 connector allows setting up scenes through a mobile application.
The binding supports selecting a specific scene.

Many of the sensor devices are battery powered.

## Supported Things

The ELRO Connects supported device types are:

- Elro Connects account: `account`
- K1 connector hub: `connector`
- Smoke detector: `smokealarm`
- Carbon monoxide detector: `coalarm`
- Heat detector: `heatalarm`
- Water detector: `wateralarm`
- Windows/door contact: `entrysensor`
- Motion detector: `motionsensor`
- Temperature and humidity monitor: `temperaturesensor`
- Plug-in switch: `powersocket`

`account` is a bridge thing type that will allow automatic discovery and configuration of the available K1 connectors on the specified ELRO Connects account.
This bridge is optional.
It is used to discover the required K1 connector hub(s), using a call to the ELRO Connects cloud.
Without the `account` bridge, the `connector` bridge needs to be defined manually.
If no `account` bridge is defined, all communication between openHAB and the ELRO Connects system will be local, not using the ELRO Connects cloud.

The `connector` is the bridge thing representing a K1 connector.
All other things are connected to the `connector` bridge.

Testing was only done with smoke and water detectors connected to a K1 connector.
The firmware version of the K1 connector was 2.0.3.30 at the time of testing.

## Discovery

The ELRO Connects `account` cannot be auto-discovered.
The `account` bridge is optional, but helpful to discover the K1 connectors on an ELRO Connects account and configure them.
All online K1 connectors configured on the account will be discovered.
Notice that K1 connectors in another network than the LAN will also get discovered, but will not go online when accepted from the inbox without adjusting the `connector` configuration (set the IP address).

The K1 connector `connector` will be auto-discovered when an ELRO Connects `account` bridge has been created and initialized.
It can also be configured manually without first setting up an `account` bridge and linking it to that `account` bridge.
Once the bridge thing representing the K1 connector is correctly set up and online, discovery will allow discovering all devices connected to the K1 connector (as set up in the Elro Connects app).

If devices are outside reliable RF range, devices known to the K1 hub will be discovered but may stay offline when added as a thing.
Alarm devices can still trigger alarms and pass them between each other, even if the connection with the hub is lost.
It will not be possible to receive alarms and control them from openHAB in this case.

## Thing Configuration

### ELRO Connects account

| Parameter                   | Advanced | Description                                            |
|-----------------------------|:--------:|--------------------------------------------------------|
| `username`                  |          | Username for the ELRO Connects cloud account, required |
| `password`                  |          | Password for the ELRO Connects cloud account, required |
| `enableBackgroundDiscovery` | Y        | Enable background discovery of hubs, polling the ELRO Connects cloud account every 5 min. |

### K1 connector hub

| Parameter                   | Advanced | Description                                            |
|-----------------------------|:--------:|--------------------------------------------------------|
| `connectorId`               |          | Required parameter, should be set to ST_xxxxxxxxxxxx with xxxxxxxxxxxx the lowercase MAC address of the connector. It will be discovered when an `account` bridge has been initialized. This parameter can also be found in the ELRO Connects mobile application |
| `ipAdress`                  | Y        | IP address of the ELRO Connects K1 Connector, not required if connector and openHAB server in same subnet |
| `refreshInterval`           | Y        |  This parameter controls the connection refresh heartbeat interval. The default is 60s |
| `legacyFirmware`            | Y        | Flag for legacy firmware, should be set to true if ELRO Connects K1 Connector firmware has version lower or equal to 2.0.14. If the connector is discovered from the account, this parameter will be set automatically. The default is false |

### Devices connected to K1 connected hub

| Parameter                   | Advanced | Description                                            |
|-----------------------------|:--------:|--------------------------------------------------------|
| `deviceId`                  |          | Required parameter, set by discovery. For manual configuration, use the ´elroconnects <connectorId> devices´ console command to get a list of available devices. It should be a number |

## Channels

### ELRO Connects account

The `account` bridge thing does not have any channels.

### K1 connector hub

The `connector` bridge thing has only one channel:

| Channel ID         | Item Type      | Access Mode | Description                                        |
|--------------------|----------------|:-----------:|----------------------------------------------------|
| `scene`            | String         | RW          | current scene                                      |

The `scene` channel has a dynamic state options list with all possible scene choices available in the hub.

The `connector` also has an `alarm` trigger channel that will get triggered when the alarm is triggered for any device connected to the hub.
This will also trigger if an alarm on a device goes off and the thing corresponding to the device is not configured in openHAB.
The payload for the trigger channel is the `deviceId` for the device triggering the alarm.

### Smoke, carbon monoxide, heat and water alarms

All these things have the same channels:

| Channel ID         | Item Type            | Access Mode | Description                                 |
|--------------------|----------------------|:-----------:|---------------------------------------------|
| `muteAlarm`        | Switch               | RW          | mute alarm                                  |
| `testAlarm`        | Switch               | RW          | test alarm                                  |
| `signal`           | Number               | R           | signal strength between 0 and 4, higher is stronger |
| `battery`          | Number               | R           | battery level in %                          |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)              |

Each also has a trigger channel, resp. `smokeAlarm`, `coAlarm`, `heatAlarm` and `waterAlarm`.
The payload for these trigger channels is empty.

### Door/window contact

The `entrysensor` thing has the following channels:

| Channel ID         | Item Type            | Access Mode | Description                                 |
|--------------------|----------------------|:-----------:|---------------------------------------------|
| `entry`            | Contact              | R           | open/closed door/window                     |
| `signal`           | Number               | R           | signal strength between 0 and 4, higher is stronger |
| `battery`          | Number               | R           | battery level in %                          |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)              |

The `entrysensor` thing also has a trigger channel, `entryAlarm`.

### Motion sensor

The `motionsensor` thing has the following channels:

| Channel ID         | Item Type            | Access Mode | Description                                 |
|--------------------|----------------------|:-----------:|---------------------------------------------|
| `motion`           | Switch               | R           | on when motion detected                     |
| `signal`           | Number               | R           | signal strength between 0 and 4, higher is stronger |
| `battery`          | Number               | R           | battery level in %                          |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)               |

The `motionsensor` thing also has a trigger channel, `motionAlarm`.

### Temperature and humidity monitor

The `temperaturesensor` thing has the following channels:

| Channel ID         | Item Type            | Access Mode | Description                                 |
|--------------------|----------------------|:-----------:|---------------------------------------------|
| `temperature`      | Number:Temperature   | R           | temperature                                 |
| `humidity`         | Number:Dimensionless | R           | device status                               |
| `signal`           | Number               | R           | signal strength between 0 and 4, higher is stronger |
| `battery`          | Number               | R           | battery level in %                          |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)              |

### Plug-in switch

The `powersocket` thing has only one channel:

| Channel ID         | Item Type            | Access Mode | Description                                 |
|--------------------|----------------------|:-----------:|---------------------------------------------|
| `powerState`       | Switch               | RW          | power on/off                                |

## Console Commands

A number of console commands allow management of the Elro Connects K1 hub and devices.
This makes it possible to add new devices to the hub, remove, rename or replace devices, without a need to use the Elro Connects mobile application.
The full syntax and help text is available in the console using the `elroconnects` command.

## Full Example

.things:

```java
Bridge elroconnects:connector:myhub [ connectorId="ST_aabbccddaabbccdd", refreshInterval=120 ] {
    smokealarm 1 "LivingRoom" [ deviceId="1" ]
    coalarm 2 "Garage" [ deviceId="2" ]
    heatalarm 3 "Kitchen" [ deviceId="3" ]
    wateralarm 4 "Basement" [ deviceId="4" ]
    entrysensor 5 "Back Door" [ deviceId="5" ]
    motionsensor 6 "Hallway" [ deviceId="6" ]
    temperaturesensor 7 "Family Room" [ deviceId = "7" ]
    powersocket 8 "Television" [ deviceId = "8" ]
}
```

.items:

```java
String Scene            {channel="elroconnects:connector:myhub:scene"}
Number BatteryLevel     {channel="elroconnects:smokealarm:myhub:1:battery"}
Switch AlarmTest        {channel="elroconnects:smokealarm:myhub:1:test"}
```

.sitemap:

```perl
Text item=Scene
Number item=BatteryLevel
Switch item=AlarmTest
```

Example trigger rule:

```java
rule "example trigger rule"
when
    Channel 'elroconnects:smokealarm:myhub:1:smokeAlarm' triggered
then
    logInfo("Smoke alarm living room")
    ...
end
```
