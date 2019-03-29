# Daikin Binding

The Daikin binding allows you to control your Daikin air conditioning units with openHAB. In order to do so, your Daikin air conditioning unit must have a BRP072A42 WiFi adapter installed.

## Supported Things

Daikin air conditioning units with a BRP072A42 installed. This may work with the older KRP series of wired adapters, but has not been tested with them.

## Discovery

This addon will broadcast messages on your local network looking for Daikin air conditioning units and adding them to the queue of new items discovered. You can also manually add a new item if you know the IP address.

## Thing Configuration

* host - The hostname of the Daikin air conditioner. Typically you'd use an IP address such as `192.168.0.5` for this field.
* refresh - The frequency with which to refresh information from the Daikin air conditioner specified in seconds. Defaults to 60 seconds.

## Channels

The temperature channels have a precision of one half degree Celsius.

| Channel Name | Description |
|--------------|---------------------------------------------------------------------------------------------|
| power        | Turns the power on/off for the air conditioning unit.                                       |
| settemp      | The temperature set for the air conditioning unit.                                          |
| indoortemp   | The indoor temperature as measured by the unit.                                             |
| outdoortemp  | The outdoor temperature as measured by the external part of the air conditioning system. May not be available when unit is off. |
| humidity     | The indoor humidity as measured by the unit. This is not available on all units.            |
| mode         | The mode set for the unit (AUTO, DEHUMIDIFIER, COLD, HEAT, FAN)                             |
| fan          | The fan speed set for the unit (AUTO, SILENCE, LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, LEVEL_5) |


## Full Example

daikin.things:

```
daikin:ac_unit:living_room_ac [ host="192.168.0.5" ]
```

daikin.items:

```
Switch DaikinACUnit_Power { channel="daikin:ac_unit:living_room_ac:power" }
Number:Temperature DaikinACUnit_SetPoint { channel="daikin:ac_unit:living_room_ac:setpoint" }
String DaikinACUnit_Mode { channel="daikin:ac_unit:living_room_ac:mode" }
String DaikinACUnit_Fan { channel="daikin:ac_unit:living_room_ac:fanspeed" }
Number:Temperature DaikinACUnit_IndoorTemperature { channel="daikin:ac_unit:living_room_ac:indoortemp" }
Number:Temperature DaikinACUnit_OutdoorTemperature { channel="daikin:ac_unit:living_room_ac:outdoortemp" }
```

daikin.sitemap:

```
Switch item=DaikinACUnit_Power
Setpoint item=DaikinACUnit_SetPoint visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Mode mappings=["AUTO"="Auto", "DEHUMIDIFIER"="Dehumidifier", "COLD"="Cold", "HEAT"="Heat", "FAN"="Fan"] visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Fan mappings=["AUTO"="Auto", "SILENCE"="Silence", "LEVEL_1"="Level 1", "LEVEL_2"="Level 2", "LEVEL_3"="Level 3", "LEVEL_4"="Level 4", "LEVEL_5"="Level 5"] visibility=[DaikinACUnit_Power==ON]
Text item=DaikinACUnit_IndoorTemperature
Text item=DaikinACUnit_OutdoorTemperature
```
