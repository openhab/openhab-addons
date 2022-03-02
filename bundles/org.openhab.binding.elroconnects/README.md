# ELRO Connects Binding

The ELRO Connects binding provides integration with the [ELRO Connects](https://www.elro.eu/en/smart-home) smart home system.

The system uses a Wi-Fi Hub (K1 Connector) to enable communication with various smart home devices.
The devices communicate with the hub using 868MHz RF.
The binding only communicates with the ELRO Connects system and K1 Connector using UDP in the local network.

The binding exposes the devices' status and controls to openHAB.
The K1 connector itself allows setting up scenes through a mobile application.
The binding supports selecting a specific scene.

Many of the sensor devices are battery powered.

## Supported Things

The ELRO Connects supported device types are:

* K1 connector hub: `connector`
* Smoke detector: `smokealarm`
* Carbon monoxide detector: `coalarm`
* Heat detector: `heatalarm`
* Water detector: `wateralarm`
* Windows/door contact: `entrysensor`
* Motion detector: `motionsensor`
* Temperature and humidity monitor: `temperaturesensor`
* Plug-in switch: `powersocket`

The `connector` is the bridge thing.
All other things are connected to the bridge.

Testing was only done with smoke and water detectors connected to a K1 connector.
The firmware version of the K1 connector was 2.0.3.30 at the time of testing.
Older versions of the firmware are known to have differences in the communication protocol.

## Discovery

The K1 connector `connector` cannot be auto-discovered.
Once the bridge thing representing the K1 connector is correctly set up and online, discovery will allow discovering all devices connected to the K1 connector (as set up in the Elro Connects app).

If devices are outside reliable RF range, devices known to the K1 hub will be discovered but may stay offline when added as a thing.
Alarm devices can still trigger alarms and pass them between each other, even if the connection with the hub is lost.
It will not be possible to receive alarms and control them from openHAB in this case.

## Thing Configuration

### K1 connector hub

| Parameter         | Advanced | Description            |
|-------------------|:--------:|------------------------|
| `connectorId` |          | Required parameter, should be set to ST_xxxxxxxxxxxx with xxxxxxxxxxxx the lowercase MAC address of the connector. This parameter can also be found in the ELRO Connects mobile application. |
| `ipAdress`     | Y        | IP address of the ELRO Connects K1 Connector, not required if connector and openHAB server in same subnet. |
| `refreshInterval` | Y      |  This parameter controls the connection refresh heartbeat interval. The default is 60s. |

### Devices connected to K1 connected hub

| Parameter         | Description            |
|--------------------|----------------------|
| `deviceId` | Required parameter, set by discovery and cannot easily be found manually. It should be a number. |

## Channels

### K1 connector hub

The `connector` bridge thing has only one channel:

| Channel ID         | Item Type            | Access Mode | Description                                        |
|--------------------|----------------------|:-----------:|----------------------------------------------------|
| `scene`            | String               | RW          | current scene                                      |

The `scene` channel has a dynamic state options list with all possible scene choices available in the hub.

## Smoke, carbon monoxide, heat and water alarms

All these things have the same channels:

| Channel ID         | Item Type            | Access Mode | Description                                        |
|--------------------|----------------------|:-----------:|----------------------------------------------------|
| `muteAlarm`        | Switch               | RW          | mute alarm                                         |
| `testAlarm`        | Switch               | RW          | test alarm                                         |
| `battery`          | Number               | R           | battery level in %                                 |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)                     |

Each also has a trigger channel, resp. `smokeAlarm`, `coAlarm`, `heatAlarm` and `waterAlarm`.
The payload for these trigger channels is empty.

## Door/window contact

The `entrysensor` thing has the following channels:

| Channel ID         | Item Type            | Access Mode | Description                                        |
|--------------------|----------------------|:-----------:|----------------------------------------------------|
| `entry`            | Contact              | R           | open/closed door/window                            |
| `battery`          | Number               | R           | battery level in %                                 |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)                     |

The `entrysensor` thing also has a trigger channel, `entryAlarm`.

## Motion sensor

The `motionsensor` thing has the following channels:

| Channel ID         | Item Type            | Access Mode | Description                                        |
|--------------------|----------------------|:-----------:|----------------------------------------------------|
| `motion`           | Switch               | R           | on when motion detected                            |
| `battery`          | Number               | R           | battery level in %                                 |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)                     |

The `motionsensor` thing also has a trigger channel, `motionAlarm`.

## Temperature and humidity monitor

The `temperaturesensor` thing has the following channels:

| Channel ID         | Item Type            | Access Mode | Description                                        |
|--------------------|----------------------|:-----------:|----------------------------------------------------|
| `temperature`      | Number:Temperature   | R           | temperature                                        |
| `humidity`         | Number:Dimensionless | R           | device status                                      |
| `battery`          | Number               | R           | battery level in %                                 |
| `lowBattery`       | Switch               | R           | on for low battery (below 15%)                     |

## Plug-in switch

The `powersocket` thing has only one channel:

| Channel ID         | Item Type            | Access Mode | Description                                        |
|--------------------|----------------------|:-----------:|----------------------------------------------------|
| `powerState`       | Switch               | RW          | power on/off                                       |


## Full Example

.things:

```
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

```
String Scene            {channel="elroconnects:connector:myhub:scene"}
Number BatteryLevel     {channel="elroconnects:smokealarm:myhub:1:battery"}
Switch AlarmTest        {channel="elroconnects:smokealarm:myhub:1:test"}
```

.sitemap:

```
Text item=Scene
Number item=BatteryLevel
Switch item=AlarmTest
```

Example trigger rule:

```
rule "example trigger rule"
when
    Channel 'elroconnects:smokealarm:myhub:1:smokeAlarm' triggered
then
    logInfo("Smoke alarm living room")
    ...
end
```


