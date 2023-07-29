# Bosch Indego Binding

This is the Binding for Bosch Indego Connect lawn mowers.
Thank´s to zazaz-de who found out how the API works.
His [Java Library](https://github.com/zazaz-de/iot-device-bosch-indego-controller) made this Binding possible.

## Thing Configuration

Currently the binding supports  _**indego**_  mowers as a thing type with these configuration parameters:

| Parameter          | Description                                                       | Default |
|--------------------|-------------------------------------------------------------------|---------|
| username           | Username for the Bosch Indego account                             |         |
| password           | Password for the Bosch Indego account                             |         |
| refresh            | The number of seconds between refreshing device state when idle   | 180     |
| stateActiveRefresh | The number of seconds between refreshing device state when active | 30      |
| cuttingTimeRefresh | The number of minutes between refreshing last/next cutting time   | 60      |

## Channels

| Channel            | Item Type                | Description                                                                                                                         | Writeable |
|--------------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------|-----------|
| state              | Number                   | You can send commands to this channel to control the mower and read the simplified state from it (1=mow, 2=return to dock, 3=pause) | Yes       |
| errorcode          | Number                   | Error code of the mower (0=no error)                                                                                                |           |
| statecode          | Number                   | Detailed state of the mower                                                                                                         |           |
| textualstate       | String                   | State as a text.                                                                                                                    |           |
| ready              | Number                   | Shows if the mower is ready to mow (1=ready, 0=not ready)                                                                           |           |
| mowed              | Dimmer                   | Cut grass in percent                                                                                                                |           |
| lastCutting        | DateTime                 | Last cutting time                                                                                                                   |           |
| nextCutting        | DateTime                 | Next scheduled cutting time                                                                                                         |           |
| batteryVoltage     | Number:ElectricPotential | Battery voltage reported by the device<sup>1</sup>                                                                                  |           |
| batteryLevel       | Number                   | Battery level as a percentage (0-100%)<sup>1</sup>                                                                                  |           |
| lowBattery         | Switch                   | Low battery warning with possible values on (low battery) and off (battery ok)<sup>1</sup>                                          |           |
| batteryTemperature | Number:Temperature       | Battery temperature reported by the device<sup>1</sup>                                                                              |           |
| gardenSize         | Number:Area              | Garden size mapped by the device                                                                                                    |           |
| gardenMap          | Image                    | Garden map created by the device<sup>2</sup>                                                                                        |           |

<sup>1)</sup> This will be updated every six hours when the device is idle. It will wake up the device, which can include turning on its display. When the device is active or charging, this will be updated every two minutes.

<sup>2)</sup> This will be updated as often as specified by the `stateActiveRefresh` thing parameter.

### State Codes

| Code  | Description                                 |
|-------|---------------------------------------------|
| 0     | Reading status                              |
| 257   | Charging                                    |
| 258   | Docked                                      |
| 259   | Docked - Software update                    |
| 260   | Docked                                      |
| 261   | Docked                                      |
| 262   | Docked - Loading map                        |
| 263   | Docked - Saving map                         |
| 266   | Leaving dock                                |
| 513   | Mowing                                      |
| 514   | Relocalising                                |
| 515   | Loading map                                 |
| 516   | Learning lawn                               |
| 517   | Paused                                      |
| 518   | Border cut                                  |
| 519   | Idle in lawn                                |
| 523   | SpotMow                                     |
| 768   | Returning to dock                           |
| 769   | Returning to dock                           |
| 770   | Returning to dock                           |
| 771   | Returning to dock - Battery low             |
| 772   | Returning to dock - Calendar timeslot ended |
| 773   | Returning to dock - Battery temp range      |
| 774   | Returning to dock                           |
| 775   | Returning to dock - Lawn complete           |
| 776   | Returning to dock - Relocalising            |
| 1025  | Diagnostic mode                             |
| 1026  | End of life                                 |
| 1281  | Software update                             |
| 1537  | Energy save mode                            |
| 64513 | Docked                                      |

## Full Example

### `indego.things` File

```java
boschindego:indego:lawnmower [username="mail@example.com", password="idontneedtocutthelawnagain", refresh=120]
```

### `indego.items` File

```java
Number Indego_State { channel="boschindego:indego:lawnmower:state" }
Number Indego_ErrorCode { channel="boschindego:indego:lawnmower:errorcode" }
Number Indego_StateCode { channel="boschindego:indego:lawnmower:statecode" }
String Indego_TextualState { channel="boschindego:indego:lawnmower:textualstate" }
Number Indego_Ready { channel="boschindego:indego:lawnmower:ready" }
Dimmer Indego_Mowed { channel="boschindego:indego:lawnmower:mowed" }
DateTime Indego_LastCutting { channel="boschindego:indego:lawnmower:lastCutting" }
DateTime Indego_NextCutting { channel="boschindego:indego:lawnmower:nextCutting" }
Number:ElectricPotential Indego_BatteryVoltage { channel="boschindego:indego:lawnmower:batteryVoltage" }
Number Indego_BatteryLevel { channel="boschindego:indego:lawnmower:batteryLevel" }
Switch Indego_LowBattery { channel="boschindego:indego:lawnmower:lowBattery" }
Number:Temperature Indego_BatteryTemperature { channel="boschindego:indego:lawnmower:batteryTemperature" }
Number:Area Indego_GardenSize { channel="boschindego:indego:lawnmower:gardenSize" }
Image Indego_GardenMap { channel="boschindego:indego:lawnmower:gardenMap" }
```

### `indego.sitemap` File

```perl
Switch item=Indego_State mappings=[1="Mow", 2="Return",3="Pause"]
```
