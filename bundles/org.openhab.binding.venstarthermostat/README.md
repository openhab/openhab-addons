# Venstar Thermostat Binding

The Venstar Thermostat binding supports an interface to WiFi enabled 
ColorTouch and Explorer thermostats manufactured by Venstar[1].

## Prerequisites

Venstar WiFi enabled thermostats provide a local API that this binding uses
to communicate with the thermostat. This binding does not require "cloud" 
access and may be used independently of Venstar's Skyport cloud services.

The Local API is not enabled by default, so you will need to set up your 
thermostat by configuring its WiFi connection and enabling the Local API. In
order for the binding to connect, you will need to enable HTTPS support and 
set a username and password. While it is possible to enable the Local API
without HTTPS and authentication, the binding doesn't support it, in an effort
to provide as secure an installation as possible.

When you've set the username and password, make a note of these, as you'll need
to enter them in the thermostat configuration in OpenHAB.


## Usage

### Discovery

Once the binding is installed it will attempt to auto discovery Venstar thermostats located on the local network.
These will appear as Things in the system Inbox. 
After adding the Inbox item, enter the user name and password from the physical thermostat in the Thing's configuration.

### Channels

| Channel            | Type               | Description                  | Notes                                                  |
|--------------------|--------------------|------------------------------|--------------------------------------------------------|
| systemMode         | String             | System Mode                  |                                                        |
| systemModeRaw      | Number             | System Mode Raw (Read Only)  | 0 (Off) 1 (Heat) 2 (Cool) 3 (Auto)                     |
| systemState        | String             | System State (Read Only)     |                                                        |
| systemStateRaw     | Number             | System State Raw (Read Only) | 0 (Idle) 1 (Heating) 2 (Cooling) 3 (Lockout) 4 (Error) |
| heatingSetpoint    | Number:Temperature | Heating Set Point            |                                                        |
| coolingSetpoint    | Number:Temperature | Cooling Set Point            |                                                        |
| temperature        | Number:Temperature | Current Temperature          |                                                        |
| outdoorTemperature | Number:Temperature | Outdoor Temperature          |                                                        |
| humidity           | Number             | Humidity                     |                                                        |


### Item Configuration


```perl
    Number:Temperature Guest_HVAC_Temperature   "Temperature [%d °F]"   {channel="venstarthermostat:colorTouchThermostat:001122334455:temperature"}
    Number:Temperature Guest_HVAC_HeatSetpoint  "Heat Setpoint [%d °F]" {channel="venstarthermostat:colorTouchThermostat:001122334455:heatingSetpoint"}
    Number:Temperature Guest_HVAC_CoolSetpoint  "Cool Setpoint [%d °F]" {channel="venstarthermostat:colorTouchThermostat:001122334455:coolingSetpoint"}
    Number Guest_HVAC_Mode                      "Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:systemMode"}
    Number Guest_HVAC_Humidity                  "Humidity [%d %%]"      {channel="venstarthermostat:colorTouchThermostat:001122334455:humidity"}
    Number Guest_HVAC_State                     "State [%s]"            {channel="venstarthermostat:colorTouchThermostat:001122334455:systemState"}
```

## References

[1] http://www.venstar.com/thermostats/colortouch/
