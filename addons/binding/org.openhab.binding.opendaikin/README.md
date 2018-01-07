# OpenDaikin Binding

The OpenDaikin binding allows you to control your Daikin air conditioning units with openHAB. In order to do so, your Daikin air conditioning unit must have a BRP072A42 WiFi adapter installed.

## Supported Things

Daikin air conditioning units with a BRP072A42 installed. This may work with the older KRP series of wired adapters, but has not been tested with them.

## Discovery

This addon does not currently support discovery. You'll need to know the IP address of your air conditioner to add it as a thing.


## Thing Configuration

* host - The hostname of the Daikin air conditioner. Typically you'd use an IP address such as `192.168.0.5` for this field.
* refresh - The frequency with which to refresh information from the Daikin air conditioner specified in milliseconds. Defaults to 60 seconds.


## Channels

For channels that use temperatures, the default is to use Celsius. This can be changed via a configuration on the individual channels. When in Celsius mode, the temperature has a precision of one half degree. When using Fahrenheit, only whole degrees are used. Internally, the Daikin system uses Celsius so all Fahrenheit numbers are converted and rounded.

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
opendaikin:ac_unit:living_room_ac [ host="192.168.0.5" ]
```

daikin.items:

```
Switch DaikinACUnit_Power { channel="opendaikin:ac_unit:living_room_ac:power" }
Text DaikinACUnit_SetPointF { channel="opendaikin:ac_unit:living_room_ac:setpointf" }
Text DaikinACUnit_Mode { channel="opendaikin:ac_unit:living_room_ac:mode" }
Text DaikinACUnit_Fan { channel="opendaikin:ac_unit:living_room_ac:fanspeed" }
Text DaikinACUnit_IndoorTemperatureF { channel="opendaikin:ac_unit:living_room_ac:indoortempf" }
Text DaikinACUnit_OutdoorTemperatureF { channel="opendaikin:ac_unit:living_room_ac:outdoortempf" }
```

daikin.sitemap:

```
Switch item=DaikinACUnit_Power
Setpoint item=DaikinACUnit_SetPointF visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Mode mappings=["AUTO"="Auto", "DEHUMIDIFIER"="Dehumidifier", "COLD"="Cold", "HEAT"="Heat", "FAN"="Fan"] visibility=[DaikinACUnit_Power==ON]
Selection item=DaikinACUnit_Fan mappings=["AUTO"="Auto", "SILENCE"="Silence", "LEVEL_1"="Level 1", "LEVEL_2"="Level 2", "LEVEL_3"="Level 3", "LEVEL_4"="Level 4", "LEVEL_5"="Level 5"] visibility=[DaikinACUnit_Power==ON]
Text item=DaikinACUnit_IndoorTemperatureF
Text item=DaikinACUnit_OutdoorTemperatureF
```
