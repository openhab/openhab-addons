# Venstar Thermostat Binding

The Venstar Thermostat binding supports an interface to WiFi enabled ColorTouch and Explorer thermostats manufactured by [Venstar](https://www.venstar.com).

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
to enter them in the thermostat configuration in openHAB.

## Supported Things

| Thing Type           | Description                                                                       |
|----------------------|-----------------------------------------------------------------------------------|
| colorTouchThermostat | A Venstar [ColorTouch](https://www.venstar.com/thermostats/colortouch/) thermostat |

## Discovery

Once the binding is installed it will attempt to auto discover Venstar thermostats located on the local network.
These will appear as Things in the system Inbox.
After adding the Inbox item, enter the user name and password from the physical thermostat in the Thing's configuration.

## Thing Configuration

### ColorTouch Thermostat

| Parameter | Description                                                                  | Required |
|-----------|------------------------------------------------------------------------------|----------|
| username  | The username set on the thermostats configuration screen (typically 'admin') | yes      |
| password  | The password set set on the thermostats configuration screen                 | yes      |
| url       | URL of the thermostat in the format 'proto://host'                           | yes      |
| refresh   | The frequency in which the binding will pool for update information          | no       |

### Channels

| Channel            | Type               | Description                           | Notes                                                  |
|--------------------|--------------------|---------------------------------------|--------------------------------------------------------|
| awayMode           | String             | Home or Away Mode                     |                                                        |
| awayModeRaw        | Number             | Away Mode Raw (Read Only)             | 0 (Home) 1 (Away)                                      |
| systemMode         | String             | System Mode                           |                                                        |
| systemModeRaw      | Number             | System Mode Raw (Read Only)           | 0 (Off) 1 (Heat) 2 (Cool) 3 (Auto)                     |
| systemState        | String             | System State (Read Only)              |                                                        |
| systemStateRaw     | Number             | System State Raw (Read Only)          | 0 (Idle) 1 (Heating) 2 (Cooling) 3 (Lockout) 4 (Error) |
| heatingSetpoint    | Number:Temperature | Heating Set Point                     |                                                        |
| coolingSetpoint    | Number:Temperature | Cooling Set Point                     |                                                        |
| temperature        | Number:Temperature | Current Temperature                   |                                                        |
| outdoorTemperature | Number:Temperature | Outdoor Temperature                   |                                                        |
| humidity           | Number             | Humidity                              |                                                        |
| fanMode            | String             | Fan Mode                              |                                                        |
| fanModeRaw         | Number             | Fan Mode Raw (Read Only)              | 0 (Auto) 1 (On)                                        |
| fanState           | Switch             | Fan State (Read Only)                 |                                                        |
| fanStateRaw        | Number             | Fan State Raw (Read Only)             | 0 (Off) 1 (On)                                         |
| scheduleMode       | String             | Current Schedule Mode                 |                                                        |
| scheduleModeRaw    | Number             | Current Schedule mode Raw (Read Only) | 0(Disabled) 1(Enabled)                                 |
| schedulePart       | String             | Current Schedule Part                 |                                                        |
| schedulePartRaw    | Number             | Schedule Part Raw (Read Only)         | 0(Morning) 1(Day) 2(Evening) 3 (Night) 255 (Inactive)  |

### Runtime data

The Venstar thermostat provides data about how many minutes the system has been running in each of the different modes (heat1, heat2, cool1, cool2, aux1, aux2, free cool) every day for the last 7 days.
A time stamp is provided with each runtime data set which represents the end of each day.
The binding reads the runtime data and time stamps and provides them all as separate channels.

| Channel                | Type                 | Description                                  | Notes                                                      |
|------------------------|----------------------|----------------------------------------------|------------------------------------------------------------|
| timestampDay0          | DateTime             | Time Stamp of last runtime update            | This is always the current time today                      |
| timestampDay1          | DateTime             | Time Stamp of 00:00, end of yesterday        | This represents the end of 1 day ago                       |
| timestampDay2          | DateTime             | Time Stamp of 00:00 end of 2 days ago        | This represents the end of 2 days ago                      |
| timestampDay3          | DateTime             | Time Stamp of 00:00, end of 3 days ago       | This represents the end of 3 days ago                      |
| timestampDay4          | DateTime             | Time Stamp of 00:00, end of 4 days ago       | This represents the end of 4 days ago                      |
| timestampDay5          | DateTime             | Time Stamp of 00:00, end of 5 days ago       | This represents the end of 5 days ago                      |
| timestampDay6          | DateTime             | Time Stamp of 00:00, end of 6 days ago       | This represents the end of 6 days ago                      |
| heat1RuntimeDay0       | Number:Dimensionless | Runtime in heat1 mode (minutes) today        | This is the runtime between the Day 1 and Day 0 timestamps |
| heat1RuntimeDay1       | Number:Dimensionless | Runtime in heat1 mode (minutes) yesterday    | This is the runtime between the Day 2 and Day 1 timestamps |
| heat1RuntimeDay2       | Number:Dimensionless | Runtime in heat1 mode (minutes) 2 days ago   | This is the runtime between the Day 3 and Day 2 timestamps |
| heat1RuntimeDay3       | Number:Dimensionless | Runtime in heat1 mode (minutes) 3 days ago   | This is the runtime between the Day 4 and Day 3 timestamps |
| heat1RuntimeDay4       | Number:Dimensionless | Runtime in heat1 mode (minutes) 4 days ago   | This is the runtime between the Day 5 and Day 4 timestamps |
| heat1RuntimeDay5       | Number:Dimensionless | Runtime in heat1 mode (minutes) 5 days ago   | This is the runtime between the Day 6 and Day 5 timestamps |
| heat1RuntimeDay6       | Number:Dimensionless | Runtime in heat1 mode (minutes) 6 days ago   | This is the runtime in the 24hrs up to the Day 6 timestamp |
|                        |                      |                                              |                                                            |
| heat2RuntimeDay0..6    | Number:Dimensionless | Similar Runtimes in heat2 mode (minutes)     |                                                            |
|                        |                      |                                              |                                                            |
| cool1RuntimeDay0..6    | Number:Dimensionless | Similar Runtimes in cool1 mode (minutes)     |                                                            |
|                        |                      |                                              |                                                            |
| cool2RuntimeDay0..6    | Number:Dimensionless | Similar Runtimes in cool2 mode (minutes)     |                                                            |
|                        |                      |                                              |                                                            |
| aux1RuntimeDay0..6     | Number:Dimensionless | Similar Runtimes in aux1 mode (minutes)      |                                                            |
|                        |                      |                                              |                                                            |
| aux2RuntimeDay0..6     | Number:Dimensionless | Similar Runtimes in aux2 mode (minutes)      |                                                            |
|                        |                      |                                              |                                                            |
| freeCoolRuntimeDay0..6 | Number:Dimensionless | Similar Runtimes in free cool mode (minutes) |                                                            |

## Example

### thermostat.things

```java
Thing venstarthermostat:colorTouchThermostat:001122334455 "Venstar Thermostat (Guest)" [ username="admin", password="secret", url="https://192.168.1.100", refresh=30 ]
```

### thermostat.items

```java
Number:Temperature Guest_HVAC_Temperature   "Temperature [%d °F]"   {channel="venstarthermostat:colorTouchThermostat:001122334455:temperature"}
Number:Temperature Guest_HVAC_HeatSetpoint  "Heat Setpoint [%d °F]" {channel="venstarthermostat:colorTouchThermostat:001122334455:heatingSetpoint"}
Number:Temperature Guest_HVAC_CoolSetpoint  "Cool Setpoint [%d °F]" {channel="venstarthermostat:colorTouchThermostat:001122334455:coolingSetpoint"}
String Guest_HVAC_Mode                      "System Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:systemMode"}
Number Guest_HVAC_Humidity                  "Humidity [%d %%]"      {channel="venstarthermostat:colorTouchThermostat:001122334455:humidity"}
String Guest_HVAC_State                     "State [%s]"            {channel="venstarthermostat:colorTouchThermostat:001122334455:systemState"}
String Guest_Away_Mode                      "Away Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:awayMode"}
String Guest_Fan_Mode                      "Fan Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:fanMode"}
Switch Guest_Fan_State                      "Fan State"             {channel="venstarthermostat:colorTouchThermostat:001122334455:fanState"}
String Guest_Schedule_Mode                      "Schedule Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:scheduleMode"}
String Guest_Schedule_Part                      "Schedule Part [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:schedulePart"}
DateTime Guest_timestampDay0                 "Date/Time Last Update [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:timestampDay0"}
Number Guest_heat1RuntimeDay0                      "Heat1 Day0 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:heat1RuntimeDay0"}
Number Guest_heat2RuntimeDay0                      "Heat2 Day 0 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:heat2RuntimeDay0"}
Number Guest_cool1RuntimeDay0                      "Cool1 Day 0 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:cool1RuntimeDay0"}
Number Guest_cool2RuntimeDay0                      "Cool2 Day 0 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:cool2RuntimeDay0"}
Number Guest_aux1RuntimeDay0                      "Aux1 Day 0 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:aux1RuntimeDay0"}
Number Guest_aux2RuntimeDay0                      "Aux2 Day 0 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:aux2RuntimeDay0"}
Number Guest_freeCoolRuntimeDay0                      "Free Cool Day 0 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:freeCoolRuntimeDay0"}
```

### thermostat.sitemap

```perl
sitemap demo label="Venstar Color Thermostat Demo"
{
   Frame {
    Setpoint item=Guest_HVAC_HeatSetpoint minValue=50 maxValue=99
    Setpoint item=Guest_HVAC_CoolSetpoint minValue=50 maxValue=99
    Switch item=Guest_HVAC_Mode mappings=[off=Off,heat=Heat,cool=Cool,auto=Auto]
    Switch item=Guest_Away_Mode mappings=[home=Home,away=Away]
    Text item=Guest_HVAC_State
    Switch item=Guest_Fan_Mode mappings=[auto=Auto, on=On]
    Switch item=Guest_Fan_State mappings=[on=On,off=Off]
    Switch item=Guest_Schedule_Mode mappings=[enabled=Enabled,disabled=Disabled]
    Text item=Guest_Schedule_Part
    Text item=Guest_timestampDay0
    Text item=Guest_heat1RuntimeDay0
    Text item=Guest_heat2RuntimeDay0
    Text item=Guest_cool1RuntimeDay0
    Text item=Guest_cool2RuntimeDay0
    Text item=Guest_aux1RuntimeDay0
    Text item=Guest_aux2RuntimeDay0
    Text item=Guest_freeCoolRuntimeDay0

   }
}
```
