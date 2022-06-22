# Bosch Indego Binding

This is the Binding for Bosch Indego Connect lawn mowers.
Thank´s to zazaz-de who found out how the API works.
His [Java Library](https://github.com/zazaz-de/iot-device-bosch-indego-controller) made this Binding possible.

## Thing Configuration

Currently the binding supports  ***indego***  mowers as a thing type with these configuration parameters:

| Parameter          | Description                                                     | Default |
|--------------------|-----------------------------------------------------------------|---------|
| username           | Username for the Bosch Indego account                           |         |
| password           | Password for the Bosch Indego account                           |         |
| refresh            | The number of seconds between refreshing device state           | 180     |
| cuttingTimeRefresh | The number of minutes between refreshing last/next cutting time | 60      |

## Channels

| Channel      | Item Type   | Description                                                                                                                         |
|--------------|-------------|-------------------------------------------------------------------------------------------------------------------------------------|
| state        | Number      | You can send commands to this channel to control the mower and read the simplified state from it (1=mow, 2=return to dock, 3=pause) |
| errorcode    | Number      | Error code of the mower (0=no error, readonly)                                                                                      |
| statecode    | Number      | Detailed state of the mower (readonly)                                                                                              |
| textualstate | String      | State as a text. (readonly)                                                                                                         |
| ready        | Number      | Shows if the mower is ready to mow (1=ready, 0=not ready, readonly)                                                                 |
| mowed        | Dimmer      | Cut grass in percent (readonly)                                                                                                     |
| lastCutting  | DateTime    | Last cutting time (readonly)                                                                                                        |
| nextCutting  | DateTime    | Next scheduled cutting time (readonly)                                                                                              |

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

```
boschindego:indego:lawnmower [username="mail@example.com", password="idontneedtocutthelawnagain", refresh=120]
```

### `indego.items` File

```
Number Indego_State { channel="boschindego:indego:lawnmower:state" }
Number Indego_ErrorCode { channel="boschindego:indego:lawnmower:errorcode" }
Number Indego_StateCode { channel="boschindego:indego:lawnmower:statecode" }
String Indego_TextualState { channel="boschindego:indego:lawnmower:textualstate" }
Number Indego_Ready { channel="boschindego:indego:lawnmower:ready" }
Dimmer Indego_Mowed { channel="boschindego:indego:lawnmower:mowed" }
DateTime Indego_LastCutting { channel="boschindego:indego:lawnmower:lastCutting" }
DateTime Indego_NextCutting { channel="boschindego:indego:lawnmower:nextCutting" }
```

### `indego.sitemap` File

```
Switch item=Indego_State mappings=[1="Mow", 2="Return",3="Pause"]
```
