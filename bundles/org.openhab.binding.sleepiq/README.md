# SleepIQ Binding

This binding integrates with the SleepIQ system from Select Comfort for Sleep Number beds.

## Introduction

SleepIQ is a service provided by Select Comfort and sold as an option for Sleep Number beds.
The system collects data about the bed (including individual air chamber data for dual chamber beds).
This information includes whether or not a sleeper is in bed, the current sleep number setting, the pressure of the air chamber, and it's link status.
This data can then be analyzed for any number of purposes, including improving sleep.

## Supported Things

The SleepIQ cloud service acts as the bridge.
Each SleepIQ-enabled bed (regardless of the number of air chambers) is a Thing.
Currently, only dual-chamber beds are supported by this binding.

## Discovery

The SleepIQ cloud thing must be added manually with the username and password used to register with the service.
After that, beds are discovered automatically by querying the service.

## Binding Configuration

The binding requires no special configuration.

## Thing Configuration

### Bridge (Thing ID: "cloud")

The bridge requires a username and a password.
Optionally, you can also specify a polling interval.

To enable verbose logging of HTTP requests and responses regarding the cloud service, enable DEBUG level logging on ```SleepIQCloudHandler```.

| Configuration Parameter | Type    | Description                                            | Default |
|-------------------------|---------|--------------------------------------------------------|---------|
| username                | text    | Username of a registered SleepIQ account owner         |         |
| password                | text    | Password of a registered SleepIQ account owner         |         |
| pollingInterval         | integer | Seconds between fetching values from the cloud service | 60      |

### Dual-Chamber Bed (Thing ID: "dualBed")

Each bed requires a bed ID as defined by the SleepIQ service.

| Configuration Parameter | Type    | Description                                  | Default |
|-------------------------|---------|----------------------------------------------|---------|
| bedId                   | text    | The bed identifier identifies a specific bed |         |

### Sample Thing Configuration

```
Bridge sleepiq:cloud:1 [ username="mail@example.com", password="password", pollingInterval=60, logging=false ]
{
    Thing dualBed master [ bedId="-9999999999999999999" ]
    Thing dualBed guest [ bedId="-8888888888888888888" ]
}
```

## Channels

### Dual-Chamber Bed

| Channel Group ID | Group Type | Description                |
|------------------|------------|----------------------------|
| left             | Chamber    | The left side air chamber  |
| right            | Chamber    | The right side air chamber |

### Chamber Channel Group

All channels within this group are read-only.

| Channel ID           | Item Type | Description                                                                                                         |
|----------------------|-----------|---------------------------------------------------------------------------------------------------------------------|
| inBed                | Switch    | The presence of a person or object on the chamber                                                                   |
| sleepNumber          | Number    | The Sleep Number setting of the chamber                                                                             |
| pressure             | Number    | The current pressure inside the chamber                                                                             |
| lastLink             | String    | The amount of time that has passed since a connection was made from the chamber to the cloud service (D d HH:MM:SS) |
| alertId              | Number    | Identifier for an alert condition with the chamber                                                                  |
| alertDetailedMessage | String    | A detailed message describing an alert condition with the chamber                                                   |

## Items

Here is a sample item configuration:

```
Switch      MasterBedroom_SleepIQ_InBed_Alice          "In Bed [%s]"        { channel="sleepiq:dualBed:1:master:left#inBed" }
Number      MasterBedroom_SleepIQ_SleepNumber_Alice    "Sleep Number [%s]"  { channel="sleepiq:dualBed:1:master:left#sleepNumber" }
Number      MasterBedroom_SleepIQ_Pressure_Alice       "Pressure [%s]"      { channel="sleepiq:dualBed:1:master:left#pressure" }
String      MasterBedroom_SleepIQ_LastLink_Alice       "Last Update [%s]"   { channel="sleepiq:dualBed:1:master:left#lastLink" }
Number      MasterBedroom_SleepIQ_AlertId_Alice        "Alert ID [%s]"      { channel="sleepiq:dualBed:1:master:left#alertId" }
String      MasterBedroom_SleepIQ_AlertMessage_Alice   "Alert Message [%s]" { channel="sleepiq:dualBed:1:master:left#alertDetailedMessage" }

Switch      MasterBedroom_SleepIQ_InBed_Bob            "In Bed [%s]"        { channel="sleepiq:dualBed:1:master:right#inBed" }
Number      MasterBedroom_SleepIQ_SleepNumber_Bob      "Sleep Number [%s]"  { channel="sleepiq:dualBed:1:master:right#sleepNumber" }
Number      MasterBedroom_SleepIQ_Pressure_Bob         "Pressure [%s]"      { channel="sleepiq:dualBed:1:master:right#pressure" }
String      MasterBedroom_SleepIQ_LastLink_Bob         "Last Update [%s]"   { channel="sleepiq:dualBed:1:master:right#lastLink" }
Number      MasterBedroom_SleepIQ_AlertId_Bob          "Alert ID [%s]"      { channel="sleepiq:dualBed:1:master:right#alertId" }
String      MasterBedroom_SleepIQ_AlertMessage_Bob     "Alert Message [%s]" { channel="sleepiq:dualBed:1:master:right#alertDetailedMessage" }
```
