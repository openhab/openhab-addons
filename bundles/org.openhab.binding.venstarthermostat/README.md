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

Once the binding is installed it will attempt to auto discovery Venstar thermostats located on the local network.
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

| Channel            | Type               | Description                  | Notes                                                  |
|--------------------|--------------------|------------------------------|--------------------------------------------------------|
| awayMode           | String             | Home or Away Mode            |                                                        |
| awayModeRaw        | Number             | Away Mode Raw (Read Only)    | 0 (Home) 1 (Away)                                      |
| systemMode         | String             | System Mode                  |                                                        |
| systemModeRaw      | Number             | System Mode Raw (Read Only)  | 0 (Off) 1 (Heat) 2 (Cool) 3 (Auto)                     |
| systemState        | String             | System State (Read Only)     |                                                        |
| systemStateRaw     | Number             | System State Raw (Read Only) | 0 (Idle) 1 (Heating) 2 (Cooling) 3 (Lockout) 4 (Error) |
| heatingSetpoint    | Number:Temperature | Heating Set Point            |                                                        |
| coolingSetpoint    | Number:Temperature | Cooling Set Point            |                                                        |
| temperature        | Number:Temperature | Current Temperature          |                                                        |
| outdoorTemperature | Number:Temperature | Outdoor Temperature          |                                                        |
| humidity           | Number             | Humidity                     |                                                        |
| fanMode            | String             | Fan Mode                     |                                                        |
| fanModeRaw         | Number             | Fan Mode Raw (Read Only)     | 0 (Auto) 1 (On)                                        |
| fanState           | String             | Fan State (Read Only)        |                                                        |
| fanStateRaw        | Number             | Fan State Raw (Read Only)    | 0 (Off) 1 (On)                                         |
| scheduleMode       | String             | Current Schedule Mode        |                                                        |
| scheduleModeRaw    | Number             | Current Schedule mode Raw (Read Only)| 0(Disabled) 1(Enabled)                         |
| schedulePart       | String             | Current Schedule Part        |                                                        |
| schedulePartRaw    | Number             | Schedule Part Raw (Read Only)|0(Morning) 1(Day) 2(Evening) 3 (Night) 255 (Inactive)   |
| timestampRuntime   | DateTime           | Time Stamp of last RT update |Binding only looks at the last day runtime              |
| heat1Runtime       | Number:Dimensionless| RT in heat1 mode in minutes |                                                        |
| heat2Runtime       | Number:Dimensionless| RT in heat2 mode in minutes |                                                        |
| cool1Runtime       | Number:Dimensionless| RT in cool1 mode in minutes |                                                        |
| cool2Runtime       | Number:Dimensionless| RT in cool2 mode in minutes |                                                        |
| aux1Runtime        | Number:Dimensionless| RT in aux1 mode in minutes  |                                                        |
| aux2Runtime        | Number:Dimensionless| RT in aux2 mode in minutes  |                                                        |
| freecoolRuntime    | Number:Dimensionless| RT in Free Cool in minutes  |                                                        |


## Example

### thermostat.things

```
Thing venstarthermostat:colorTouchThermostat:001122334455 "Venstar Thermostat (Guest)" [ username="admin", password="secret", url="https://192.168.1.100", refresh=30 ]
```

### thermostat.items


```
Number:Temperature Guest_HVAC_Temperature   "Temperature [%d °F]"   {channel="venstarthermostat:colorTouchThermostat:001122334455:temperature"}
Number:Temperature Guest_HVAC_HeatSetpoint  "Heat Setpoint [%d °F]" {channel="venstarthermostat:colorTouchThermostat:001122334455:heatingSetpoint"}
Number:Temperature Guest_HVAC_CoolSetpoint  "Cool Setpoint [%d °F]" {channel="venstarthermostat:colorTouchThermostat:001122334455:coolingSetpoint"}
Number Guest_HVAC_Mode                      "Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:systemMode"}
Number Guest_HVAC_Humidity                  "Humidity [%d %%]"      {channel="venstarthermostat:colorTouchThermostat:001122334455:humidity"}
Number Guest_HVAC_State                     "State [%s]"            {channel="venstarthermostat:colorTouchThermostat:001122334455:systemState"}
Number Guest_Away_Mode                      "Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:awayMode"}
String Guest_Fan_Mode                      "Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:fanMode"}
String Guest_Fan_State                      "Fan State [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:fanState"}
String Guest_Schedule_Mode                      "Schedule Mode [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:scheduleMode"}
String Guest_Schedule_Part                      "Schedule Part [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:schedulePart"}
DateTime Guest_timestampRuntime                 "Date/Time Last Update [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:timestampRuntime"}
Number Guest_heat1Runtime                      "Heat1 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:heat1Runtime"}
Number Guest_heat2Runtime                      "Heat2 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:heat2Runtime"}
Number Guest_cool1Runtime                      "Cool1 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:cool1Runtime"}
Number Guest_cool2Runtime                      "Cool2 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:cool2Runtime"}
Number Guest_aux1Runtime                      "Aux1 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:aux1Runtime"}
Number Guest_aux2Runtime                      "Aux2 Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:aux2Runtime"}
Number Guest_freecoolRuntime                      "Free Cool Run Time [%s]"             {channel="venstarthermostat:colorTouchThermostat:001122334455:freecoolRuntime"}
```

### thermostat.sitemap

```
sitemap demo label="Venstar Color Thermostat Demo"
{
   Frame {
    Setpoint item=Guest_HVAC_HeatSetpoint minValue=50 maxValue=99
    Setpoint item=Guest_HVAC_CoolSetpoint minValue=50 maxValue=99
    Switch item=Guest_HVAC_Mode mappings=[off=Off,heat=Heat,cool=Cool,auto=Auto]
    Switch item=Guest_Away_Mode mappings=[home=Home,away=Away]
    Text item=Guest_HVAC_State
   }
}
```
