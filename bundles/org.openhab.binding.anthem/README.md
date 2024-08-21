# Anthem Binding

The binding allows control of Anthem AV processors over an IP connection to the processor.

## Supported Things

The following thing type is supported:

| Thing    | ID       | Discovery | Description |
|----------|----------|-----------|-------------|
| Anthem   | anthem   | Manual    | Represents a Anthem AV processor |

Tested models include the AVM-60 11.2-channel preamp/processor.


## Thing Configuration

The following configuration parameters are available on the Anthem thing:

| Parameter           | Parameter ID              | Required/Optional | Description |
|---------------------|---------------------------|-------------------|-------------|
| Host                | host                      | Required          | IP address or host name of the Anthem AV processor |
| Port                | port                      | Optional          | Port number used by the Anthem |
| Reconnect Interval  | reconnectIntervalMinutes  | Optional          | The time to wait between reconnection attempts (in minutes) |
| Command Delay       | commandDelayMsec          | Optional          | The delay between commands sent to the processor (in milliseconds) |

## Channels

The Anthem AV processor supports the following channels (some zones/channels are model specific):

| Channel                 | Type    | Description  |
|-------------------------|---------|--------------|
| *General*               |         |                                          |
| general#command         | String  | Send a custom command                    |
| *Main Zone*             |         |                                          |
| 1#power                 | Switch  | Power the zone on or off                 |
| 1#volume                | Dimmer  | Increase or decrease the volume level    |
| 1#volumeDB              | Number  | The actual volume setting                |
| 1#mute                  | Switch  | Mute the volume                          |
| 1#activeInput           | Number  | The currently active input source        |
| 1#activeInputShortName  | String  | Short friendly name of the active input  |
| 1#activeInputLongName   | String  | Long friendly name of the active input   |
| *Zone 2*                |         |                                          |
| 2#power                 | Switch  | Power the zone on or off                 |
| 2#volume                | Dimmer  | Increase or decrease the volume level    |
| 2#volumeDB              | Number  | The actual volume setting                |
| 2#mute                  | Switch  | Mute the volume                          |
| 2#activeInput           | Number  | The currently active input source        |
| 2#activeInputShortName  | String  | Short friendly name of the active input  |
| 2#activeInputLongName   | String  | Long friendly name of the active input   |


## Full Example

### Things

```
Thing anthem:anthem:mediaroom "Anthem AVM 60" [ host="192.168.1.100" ]
```

### Items

```
String  Anthem_Command                    "Command [%s]"                           { channel="anthem:anthem:mediaroom:general#command" }

Switch  Anthem_Z1_Power                   "Zone 1 Power [%s]"                      { channel="anthem:anthem:mediaroom:1#power" }
Dimmer  Anthem_Z1_Volume                  "Zone 1 Volume [%s]"                     { channel="anthem:anthem:mediaroom:1#volume" }
Number  Anthem_Z1_Volume_DB               "Zone 1 Volume dB [%.0f]"                { channel="anthem:anthem:mediaroom:1#volumeDB" }
Switch  Anthem_Z1_Mute                    "Zone 1 Mute [%s]"                       { channel="anthem:anthem:mediaroom:1#mute" }
Number  Anthem_Z1_ActiveInput             "Zone 1 Active Input [%.0f]"             { channel="anthem:anthem:mediaroom:1#activeInput" }
String  Anthem_Z1_ActiveInputShortName    "Zone 1 Active Input Short Name [%s]"    { channel="anthem:anthem:mediaroom:1#activeInputShortName" }
String  Anthem_Z1_ActiveInputLongName     "Zone 1 Active Input Long Name [%s]"     { channel="anthem:anthem:mediaroom:1#activeInputLongName" }

Switch  Anthem_Z2_Power                   "Zone 2 Power [%s]"                      { channel="anthem:anthem:mediaroom:1#power" }
Dimmer  Anthem_Z2_Volume                  "Zone 2 Volume [%s]"                     { channel="anthem:anthem:mediaroom:1#volume" }
Number  Anthem_Z2_Volume_DB               "Zone 2 Volume dB [%.0f]"                { channel="anthem:anthem:mediaroom:1#volumeDB" }
Switch  Anthem_Z2_Mute                    "Zone 2 Mute [%s]"                       { channel="anthem:anthem:mediaroom:1#mute" }
Number  Anthem_Z2_ActiveInput             "Zone 2 Active Input [%.0f]"             { channel="anthem:anthem:mediaroom:1#activeInput" }
String  Anthem_Z2_ActiveInputShortName    "Zone 2 Active Input Short Name [%s]"    { channel="anthem:anthem:mediaroom:1#activeInputShortName" }
String  Anthem_Z2_ActiveInputLongName     "Zone 2 Active Input Long Name [%s]"     { channel="anthem:anthem:mediaroom:1#activeInputLongName" }
```
