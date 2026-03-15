# Digiplex/EVO Binding

This binding provides integration with Digiplex/EVO alarm systems from [Paradox](https://paradox.com).
It utilizes [PRT3 module](https://www.paradox.com/Products/default.asp?CATID=7&SUBCATID=75&PRD=234) for serial communication.

## Supported Things

### PRT3 Module

Before the binding can be used, a serial adapter must be added manually. Select `PRT3 Module` and enter serial port parameters.
Please refer to PRT3 module manual for instructions how to modify baudrate (default setting is 2400)

## Discovery

Once `PRT3 Module` is added and communication with the alarm system confirmed by its `online` status, please start discovery process to automatically discover (and add as new Things) all zones and areas defined in the alarm system.

## Binding Configuration

There is no binding level configuration required.

## Thing Configuration

### PRT3 Module Configuration

The following section lists the PRT3 Module configuration. If using manual configuration in text files, the parameter names are given in the square brackets.

#### Serial Port [port]

Sets the serial port name for the communication with the alarm system

#### Baud Rate [baudrate]

Baud rate to use for serial port communication

### Area configuration

#### Refresh time of area status (in seconds) [refreshPeriod]

Controls how often area status is refreshed from the alarm system.

## Channels

### PTR3 Module Channels

The table below summarizes all the channels available from the `PTR3 Module` thing.

| Channel            | Description                                                             |
|--------------------|-------------------------------------------------------------------------|
| messages_sent      | Counts the number of messages sent to the module                        |
| responses_received | Counts the number of responses received from the module                 |
| events_received    | Counts the number of events received from the module                    |

### Zone Channels

The table below summarizes all the channels available from the `zone` thing.

| Channel          | Description                                                             |
|------------------|-------------------------------------------------------------------------|
| status           | Simple zone status (open/closed)                                        |
| extended_status  | Extended zone status as a String (Open/Closed/Tampered/Fire Loop Alarm) |
| alarm            | Information whether zone is in alarm (open/closed)                      |
| fire_alarm       | Same as above for fire alarm                                            |
| supervision_lost | Information whether supervision has been lost (open/closed)             |
| low_battery      | Low battery warning (open/closed)                                       |

### Area Channels

The table below summarizes all the channels available from the `area` thing.

| Channel          | Description                                                                       |
|------------------|-----------------------------------------------------------------------------------|
| status           | Area status available as a String                                                 |
| armed            | Simple (open/closed) information whether zone is armed                            |
| zone_in_memory   | Information whether there are zones in the memory (after alarm has been triggered |
| trouble          | Information whether some of the zones are in 'trouble' (malfunctioning)           |
| ready            | Information whether area is ready (no open zones)                                 |
| in_programming   | Checks for programming mode enabled                                               |
| alarm            | Information whether area is in alarm                                              |
| strobe           | Information whether area is in strobe alarm                                       |
| control          | Channel for controlling area                                                      |

User is able to send commands through `control` channel to arm/quick arm/disarm the zone.
Every sent message is followed by the channel state change to either `Ok` or `Failed` depending whether command has been accepted by the alarm system.
Note that PRT3 module is capable of handling more kinds of messages, but those are not yet supported by this binding.
Message format is as follows:

| Command           | String sent to the `control` channel |
|-------------------|--------------------------------------|
| Regular Arm       | AA`<pin>`                            |
| Force Arm         | AF`<pin>`                            |
| Stay Arm          | AS`<pin>`                            |
| Instant Arm       | AI`<pin>`                            |
| Regular Quick Arm | QA                                   |
| Force Quick Arm   | QF                                   |
| Stay Quick Arm    | QS                                   |
| Instant Quick Arm | QI                                   |
| Disarm            | D`<pin>`                             |

`<pin>` is your PIN as entered on a keypad.

**Note**: For security reasons please consider not storing your PIN in openHAB configuration files.

**Note2**: Please consult your alarm system manual how to enable `Quick Arm` feature. It is not enabled by default.

For example, the following sitemap item can be used to send commands to the area and receive response status as modified color of a label:

```java
Switch item=areaControl label="Actions[]" mappings=[QA="Regular Quick Arm",QS="Stay Quick Arm",D1111="Disarm"] labelcolor=[Ok="green",Fail="red"]
```
