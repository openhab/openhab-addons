# Daikin Airbase Binding

The Daikin binding allows you to control your Daikin Airbase air conditioning units with openHAB. 
In order to do so, your Daikin air conditioning unit must have a BRP15B61 WiFi adapter (Also known as Airbase) installed.

## Supported Things

Daikin air conditioning units with a BRP15B61 (also known as an Airbase) installed. 

## Discovery

This binding broadcasts messages on the local network looking for Daikin Airbase air conditioning units and adding them to the Inbox. 
You can also manually add a new item if you know the IP address.

## Thing Configuration

daikinairbase:ac_unit:living_room_ac [ host="192.168.0.5", refresh=60 ]
* host - The hostname of the Daikin air conditioner. Typically you'd use an IP address such as `192.168.0.5` for this field.
* refresh - The frequency with which to refresh information from the Daikin air conditioner specified in seconds. Defaults to 60 seconds.

## Channels

The temperature channels have a precision of one degree Celsius.

| Channel Name | Description |
|--------------|---------------------------------------------------------------------------------------------|
| power        | Turns the power on/off for the air conditioning unit.                                       |
| settemp      | The temperature set for the air conditioning unit.                                          |
| indoortemp   | The indoor temperature as measured by the unit.                                             |
| outdoortemp  | The outdoor temperature as measured by the external part of the air conditioning system. May not be available when unit is off. |
| mode         | The mode set for the unit (AUTO, DEHUMIDIFIER, COLD, HEAT, FAN)                             |
| fan          | The fan speed set for the unit (AUTO, LEVEL_1, LEVEL_2, LEVEL_3)                            |
| zone1        | Turns zone 1 on/off for the air conditioning unit (if a zoned controller is installed.      |
| zone2        | Turns zone 2 on/off for the air conditioning unit.                                          |
| zone3        | Turns zone 3 on/off for the air conditioning unit.                                          |
| zone4        | Turns zone 4 on/off for the air conditioning unit.                                          |
| zone5        | Turns zone 5 on/off for the air conditioning unit.                                          |
| zone6        | Turns zone 6 on/off for the air conditioning unit.                                          |
| zone7        | Turns zone 7 on/off for the air conditioning unit.                                          |
| zone8        | Turns zone 8 on/off for the air conditioning unit.                                          |


## Full Example

daikinairbase.things:

```
daikinairbase:ac_unit:living_room_ac [ host="192.168.0.5" ]
```

daikinairbase.items:

```
Switch DaikinACUnit_Power { channel="daikinairbase:ac_unit:living_room_ac:power" }
Number:Temperature DaikinACUnit_SetPoint { channel="daikinairbase:ac_unit:living_room_ac:settemp" }
String DaikinACUnit_Mode { channel="daikinairbase:ac_unit:living_room_ac:mode" }
String DaikinACUnit_Fan { channel="daikinairbase:ac_unit:living_room_ac:fanspeed" }
Number:Temperature DaikinACUnit_IndoorTemperature { channel="daikinairbase:ac_unit:living_room_ac:indoortemp" }
Number:Temperature DaikinACUnit_OutdoorTemperature { channel="daikinairbase:ac_unit:living_room_ac:outdoortemp" }
Switch DaikinACUnit_Zone1 { channel="daikinairbase:ac_unit:living_room_ac:zone1" }
Switch DaikinACUnit_Zone2 { channel="daikinairbase:ac_unit:living_room_ac:zone2" }
Switch DaikinACUnit_Zone3 { channel="daikinairbase:ac_unit:living_room_ac:zone3" }
Switch DaikinACUnit_Zone4 { channel="daikinairbase:ac_unit:living_room_ac:zone4" }
Switch DaikinACUnit_Zone5 { channel="daikinairbase:ac_unit:living_room_ac:zone5" }
Switch DaikinACUnit_Zone6 { channel="daikinairbase:ac_unit:living_room_ac:zone6" }
Switch DaikinACUnit_Zone7 { channel="daikinairbase:ac_unit:living_room_ac:zone7" }
Switch DaikinACUnit_Zone8 { channel="daikinairbase:ac_unit:living_room_ac:zone8" }
```

daikinairbase.sitemap:

```
Switch item=DaikinACUnit_Power
Setpoint item=DaikinACUnit_SetPoint visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Mode mappings=["AUTO"="Auto", "DEHUMIDIFIER"="Dehumidifier", "COLD"="Cold", "HEAT"="Heat", "FAN"="Fan"] visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Fan mappings=["AUTO"="Auto", "LEVEL_1"="Level 1", "LEVEL_2"="Level 2", "LEVEL_3"="Level 3"] visibility=[DaikinACUnit_Power==ON]
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
