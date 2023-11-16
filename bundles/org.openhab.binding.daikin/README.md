# Daikin Binding

The Daikin binding allows you to control your Daikin air conditioning units with openHAB.

In order to do so, your Daikin air conditioning unit must have a supported Wi-Fi adapter installed.
This may work with the older KRP series of wired adapters, but has not been tested with them.

## Supported Things

| Thing             | Daikin Wi-Fi Adapter Model                 |
| ----------------- | ------------------------------------------ |
| `ac_unit`         | BRP069A81, BRP069B41, BRP072A42, BRP072C42 |
| `airbase_ac_unit` | BRP15B61                                   |

## Discovery

This add-on will broadcast messages on your local network looking for Daikin air conditioning units and adding them to the queue of new items discovered.
You can also manually add a new item if you know the IP address.

Background discovery polls the network every minute for devices.
Background discovery is **enabled** by default.
To **disable** background discovery, add the following line to the _conf/services/runtime.cfg_ file:

```text
discovery.daikin:background=false
```

### BRP072C42 adapter discovery

A BRP072C42 adapter requires a registered UUID to authenticate. Upon discovery, a UUID will be generated but the adapter's key must be entered in the Thing configuration to complete the UUID registration.

## Thing Configuration

- `host` - The hostname of the Daikin air conditioner. Typically you'd use an IP address such as `192.168.0.5` for this field.
- `refresh` - The frequency with which to refresh information from the Daikin air conditioner specified in seconds. Defaults to 60 seconds.

### Additional Thing configurations for BRP072C42 adapter

- `secure` - Must be set to true for BRP072C42 to access it through https.
- `uuid` - A UUID used to access the BRP072C42 adapter. A handy UUID generator can be found at <https://www.uuidgenerator.net/>.
- `key` - The 13-digit key from the Daikin adapter.

## Channels

The temperature channels have a precision of one half degree Celsius.
For the BRP072A42 and BRP072C42:

| Channel Name                | Description                                                                                                                     |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| power                       | Turns the power on/off for the air conditioning unit.                                                                           |
| settemp                     | The temperature set for the air conditioning unit.                                                                              |
| indoortemp                  | The indoor temperature as measured by the unit.                                                                                 |
| outdoortemp                 | The outdoor temperature as measured by the external part of the air conditioning system. May not be available when unit is off. |
| humidity                    | The indoor humidity as measured by the unit. This is not available on all units.                                                |
| mode                        | The mode set for the unit (AUTO, DEHUMIDIFIER, COLD, HEAT, FAN)                                                                 |
| homekitmode                 | A mode that is compatible with homekit/alexa/google home (off, auto, heat, cool). Not tested for BRP069B41                      |
| fanspeed                    | The fan speed set for the unit (AUTO, SILENCE, LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5)                                     |
| fandir                      | The fan blade direction (STOPPED, VERTICAL, HORIZONTAL, VERTICAL_AND_HORIZONTAL)                                                |
| cmpfrequency                | The compressor frequency                                                                                                        |
| specialmode                 | The special mode set for the unit (NORMAL, ECO, POWERFUL). This is not available on all units.                                  |
| streamer                    | Turns the streamer feature on/off for the air conditioning unit. This is not available on all units.                            |
| energyheatingtoday          | The energy consumption when heating for today                                                                                   |
| energyheatingthisweek       | The energy consumption when heating for this week                                                                               |
| energyheatinglastweek       | The energy consumption when heating for last week                                                                               |
| energyheatingcurrentyear-1  | The energy consumption when heating for current year January                                                                    |
| energyheatingcurrentyear-2  | The energy consumption when heating for current year February                                                                   |
| energyheatingcurrentyear-3  | The energy consumption when heating for current year March                                                                      |
| energyheatingcurrentyear-4  | The energy consumption when heating for current year April                                                                      |
| energyheatingcurrentyear-5  | The energy consumption when heating for current year May                                                                        |
| energyheatingcurrentyear-6  | The energy consumption when heating for current year June                                                                       |
| energyheatingcurrentyear-7  | The energy consumption when heating for current year July                                                                       |
| energyheatingcurrentyear-8  | The energy consumption when heating for current year August                                                                     |
| energyheatingcurrentyear-9  | The energy consumption when heating for current year September                                                                  |
| energyheatingcurrentyear-10 | The energy consumption when heating for current year October                                                                    |
| energyheatingcurrentyear-11 | The energy consumption when heating for current year November                                                                   |
| energyheatingcurrentyear-12 | The energy consumption when heating for current year December                                                                   |
| energycoolingtoday          | The energy consumption when cooling for today                                                                                   |
| energycoolingthisweek       | The energy consumption when cooling for this week                                                                               |
| energycoolinglastweek       | The energy consumption when cooling for last week                                                                               |
| energycoolingcurrentyear-1  | The energy consumption when cooling for current year January                                                                    |
| energycoolingcurrentyear-2  | The energy consumption when cooling for current year February                                                                   |
| energycoolingcurrentyear-3  | The energy consumption when cooling for current year March                                                                      |
| energycoolingcurrentyear-4  | The energy consumption when cooling for current year April                                                                      |
| energycoolingcurrentyear-5  | The energy consumption when cooling for current year May                                                                        |
| energycoolingcurrentyear-6  | The energy consumption when cooling for current year June                                                                       |
| energycoolingcurrentyear-7  | The energy consumption when cooling for current year July                                                                       |
| energycoolingcurrentyear-8  | The energy consumption when cooling for current year August                                                                     |
| energycoolingcurrentyear-9  | The energy consumption when cooling for current year September                                                                  |
| energycoolingcurrentyear-10 | The energy consumption when cooling for current year October                                                                    |
| energycoolingcurrentyear-11 | The energy consumption when cooling for current year November                                                                   |
| energycoolingcurrentyear-12 | The energy consumption when cooling for current year December                                                                   |

For the BRP15B61:

| Channel Name    | Description                                                                                                                                                       |
| --------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| power           | Turns the power on/off for the air conditioning unit.                                                                                                             |
| settemp         | The temperature set for the air conditioning unit.                                                                                                                |
| indoortemp      | The indoor temperature as measured by the unit.                                                                                                                   |
| outdoortemp     | The outdoor temperature as measured by the external part of the air conditioning system. May not be available when unit is off.                                   |
| mode            | The mode set for the unit (AUTO, DEHUMIDIFIER, COLD, HEAT, FAN)                                                                                                   |
| homekitmode     | A mode that is compatible with homekit/alexa/google home (off, auto, heat, cool)                                                                                  |
| airbasefanspeed | The fan speed set for the unit (AUTO, AIRSIDE, LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5, AUTO_LEVEL_1, AUTO_LEVEL_2, AUTO_LEVEL_3, AUTO_LEVEL_4, AUTO_LEVEL_5) |
| zone1           | Turns zone 1 on/off for the air conditioning unit (if a zone controller is installed.)                                                                            |
| zone2           | Turns zone 2 on/off for the air conditioning unit.                                                                                                                |
| zone3           | Turns zone 3 on/off for the air conditioning unit.                                                                                                                |
| zone4           | Turns zone 4 on/off for the air conditioning unit.                                                                                                                |
| zone5           | Turns zone 5 on/off for the air conditioning unit.                                                                                                                |
| zone6           | Turns zone 6 on/off for the air conditioning unit.                                                                                                                |
| zone7           | Turns zone 7 on/off for the air conditioning unit.                                                                                                                |
| zone8           | Turns zone 8 on/off for the air conditioning unit.                                                                                                                |

## Full Example

daikin.things:

```java
// for BRP069B41 or BRP072A42
daikin:ac_unit:living_room_ac [ host="192.168.0.5" ]
// for BRP072C42
daikin:ac_unit:living_room_ac [ host="192.168.0.5", secure=true, uuid="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", key="xxxxxxxxxxxxx" ]
// for Airbase (BRP15B61)
daikin:airbase_ac_unit:living_room_ac [ host="192.168.0.5" ]
```

daikin.items:

```java
// for BRP069B41, BRP072A42 or BRP072C42
Switch DaikinACUnit_Power { channel="daikin:ac_unit:living_room_ac:power" }
Number:Temperature DaikinACUnit_SetPoint { channel="daikin:ac_unit:living_room_ac:settemp" }
String DaikinACUnit_Mode { channel="daikin:ac_unit:living_room_ac:mode" }
String DaikinACUnit_HomekitMode { channel="daikin:ac_unit:living_room_ac:homekitmode" }
String DaikinACUnit_Fan { channel="daikin:ac_unit:living_room_ac:fanspeed" }
String DaikinACUnit_Fan_Movement { channel="daikin:ac_unit:living_room_ac:fandir" }
Number:Temperature DaikinACUnit_IndoorTemperature { channel="daikin:ac_unit:living_room_ac:indoortemp" }
Number:Temperature DaikinACUnit_OutdoorTemperature { channel="daikin:ac_unit:living_room_ac:outdoortemp" }


// for Airbase (BRP15B61)
Switch DaikinACUnit_Power { channel="daikin:airbase_ac_unit:living_room_ac:power" }
Number:Temperature DaikinACUnit_SetPoint { channel="daikin:airbase_ac_unit:living_room_ac:settemp" }
String DaikinACUnit_Mode { channel="daikin:airbase_ac_unit:living_room_ac:mode" }
String DaikinACUnit_HomekitMode { channel="daikin:airbase_ac_unit:living_room_ac:homekitmode" }
String DaikinACUnit_Fan { channel="daikin:airbase_ac_unit:living_room_ac:fanspeed" }
Number:Temperature DaikinACUnit_IndoorTemperature { channel="daikin:airbase_ac_unit:living_room_ac:indoortemp" }
Number:Temperature DaikinACUnit_OutdoorTemperature { channel="daikin:airbase_ac_unit:living_room_ac:outdoortemp" }
Switch DaikinACUnit_Zone1 { channel="daikin:airbase_ac_unit:living_room_ac:zone1" }
Switch DaikinACUnit_Zone2 { channel="daikin:airbase_ac_unit:living_room_ac:zone2" }
Switch DaikinACUnit_Zone3 { channel="daikin:airbase_ac_unit:living_room_ac:zone3" }
Switch DaikinACUnit_Zone4 { channel="daikin:airbase_ac_unit:living_room_ac:zone4" }
Switch DaikinACUnit_Zone5 { channel="daikin:airbase_ac_unit:living_room_ac:zone5" }
Switch DaikinACUnit_Zone6 { channel="daikin:airbase_ac_unit:living_room_ac:zone6" }
Switch DaikinACUnit_Zone7 { channel="daikin:airbase_ac_unit:living_room_ac:zone7" }
Switch DaikinACUnit_Zone8 { channel="daikin:airbase_ac_unit:living_room_ac:zone8" }

```

daikin.sitemap:

```perl
// for BRP069B41, BRP072A42 or BRP072C42
Switch item=DaikinACUnit_Power
Setpoint item=DaikinACUnit_SetPoint visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Mode mappings=["AUTO"="Auto", "DEHUMIDIFIER"="Dehumidifier", "COLD"="Cold", "HEAT"="Heat", "FAN"="Fan"] visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Fan mappings=["AUTO"="Auto", "SILENCE"="Silence", "LEVEL_1"="Level 1", "LEVEL_2"="Level 2", "LEVEL_3"="Level 3", "LEVEL_4"="Level 4", "LEVEL_5"="Level 5"] visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Fan_Movement mappings=["STOPPED"="Stopped", "VERTICAL"="Vertical", "HORIZONTAL"="Horizontal", "VERTICAL_AND_HORIZONTAL"="Vertical and Horizontal"] visibility=[DaikinACUnit_Power==ON]
Text item=DaikinACUnit_IndoorTemperature
Text item=DaikinACUnit_OutdoorTemperature

// for Airbase (BRP15B61)
Switch item=DaikinACUnit_Power
Setpoint item=DaikinACUnit_SetPoint visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Mode mappings=["AUTO"="Auto", "DEHUMIDIFIER"="Dehumidifier", "COLD"="Cold", "HEAT"="Heat", "FAN"="Fan"] visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Fan visibility=[DaikinACUnit_Power==ON]
Text item=DaikinACUnit_IndoorTemperature
Text item=DaikinACUnit_OutdoorTemperature
Switch item=DaikinACUnit_Zone1 visibility=[DaikinACUnit_Power==ON]
Switch item=DaikinACUnit_Zone2 visibility=[DaikinACUnit_Power==ON]
Switch item=DaikinACUnit_Zone3 visibility=[DaikinACUnit_Power==ON]
Switch item=DaikinACUnit_Zone4 visibility=[DaikinACUnit_Power==ON]
Switch item=DaikinACUnit_Zone5 visibility=[DaikinACUnit_Power==ON]
Switch item=DaikinACUnit_Zone6 visibility=[DaikinACUnit_Power==ON]
Switch item=DaikinACUnit_Zone7 visibility=[DaikinACUnit_Power==ON]
Switch item=DaikinACUnit_Zone8 visibility=[DaikinACUnit_Power==ON]

```
