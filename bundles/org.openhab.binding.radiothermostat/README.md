# RadioThermostat Binding

This binding connects RadioThermostat/3M Filtrete models CT30, CT50/3M50, CT80, etc. with built-in Wi-Fi module to openHAB.
Thermostats using a Z-Wave module are not supported but can be used via the openHAB ZWave binding.

The binding retrieves and periodically updates all basic system information from the thermostat.
The main thermostat functions such as thermostat mode, fan mode, temperature set point and hold mode can be controlled.
System run-time information and humidity readings are polled less frequently and can be disabled completely if not desired.
The heating and cooling program schedules on the thermostat can also be configured.

## Supported Things

There is exactly one supported thing type, which represents any of the supported thermostat models.
It has the `rtherm` id.
Multiple Things can be added if more than one thermostat is to be controlled.

## Discovery

Auto-discovery is supported if the thermostat can be located on the local network using SSDP.
Otherwise the thing must be manually added.

## Thing Configuration

The thing has a few configuration parameters:

|    Parameter    | Description                                                                                                                                                                                                                                                                                                                |
|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| hostName        | The host name or IP address of the thermostat. Mandatory.                                                                                                                                                                                                                                                                  |
| refresh         | Overrides the refresh interval of the thermostat data. Optional, the default is 2 minutes.                                                                                                                                                                                                                                 |
| logRefresh      | Overrides the refresh interval of the run-time logs & humidity data. Optional, the default is 10 minutes.                                                                                                                                                                                                                  |
| isCT80          | Flag to enable additional features only available on the CT80 thermostat. Optional, the default is false.                                                                                                                                                                                                                  |
| disableLogs     | Disable retrieval of run-time logs from the thermostat. Optional, the default is false.                                                                                                                                                                                                                                    |
| setpointMode    | Controls temporary or absolute setpoint mode. In "temporary" mode the thermostat will temporarily maintain the given setpoint until the next scheduled setpoint time period. In "absolute" mode the thermostat will ignore its program and maintain the given setpoint indefinitely. Optional, the default is "temporary". |
| clockSync       | Flag to enable the binding to sync the internal clock on the thermostat to match the openHAB host's system clock. Sync occurs at binding startup and every hour thereafter. Optional, the default is **true**.                                                                                                             |

### Schedule Configuration

The heating and cooling program schedules that persist on the thermostat can be configured by the binding.
Click the 'Show advanced' checkbox on the Thing configuration page to display the schedule.
For both the heating and cooling programs, the 7-day repeating schedule has 4 setpoint periods per day (Morning, Day, Evening, Night).
In order for the heating or cooling program to be valid, all time and setpoint fields must be populated.
The time is expressed in 24-hour (HH:mm) format and the time value for each successive period within a day must be greater than the previous period.
Once the schedule is populated and the configuration saved, the new schedule will be sent to the thermostat each time the binding is initialized, overwriting its existing schedule.
If the thermostat's current setpoint was overridden, it will be reset to the applicable program setpoint.

If one or more time or setpoint fields are left blank in a given schedule and the configuration saved, the Thing will display a configuration error until the entries are corrected.
A heating or cooling schedule with all fields left blank will be ignored by the binding.
In that case, the existing schedule on the thermostat will remain untouched.

### Cloud Service Discontinued

The MyRadioThermostat/EnergyHub cloud service that previously enabled remote control and scheduling of the thermostat is now defunct.
As such, disabling cloud connectivity on a thermostat that was previously connected to the cloud service may slightly improve the speed and reliability of accessing the local API.

The thermostat can de-provisioned from the cloud by issuing the following `curl` commands:

```shell
curl http://$THERMOSTAT_IP/cloud -d '{"enabled":0}'
curl http://$THERMOSTAT_IP/cloud -d '{"authkey":""}'
```

### Some notes

- The main caveat for using this binding is to keep in mind that the web server in the thermostat is very slow. Do not over load it with excessive amounts of simultaneous commands.
- When changing the thermostat mode, the current temperature set point is cleared and a refresh of the thermostat data is done to get the new mode's set point.
- Since retrieving the thermostat's data is the slowest operation, it will take several seconds after changing the mode before the new set point is displayed.
- Clock sync will not occur while the `override` flag is on (i.e. the program setpoint has been manually overridden) because syncing time will reset the temperature back to the program setpoint.
- The `override` flag is not reported correctly on older thermostat versions (i.e. /tstat/model reports v1.09)
- The 'Program Mode' command is untested and according to the published API is only available on a CT80 Rev B.
- Humidity information is available only when using a CT80 thermostat.
- If `remote_temp` or `message` channels are used, their values in the thermostat will be cleared during binding shutdown.

## Channels

The thermostat information that is retrieved is available as these channels:

| Channel ID             | Item Type            | Description                                                                                                                             |
|------------------------|----------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| temperature            | Number:Temperature   | The current temperature reading (°F) of the thermostat                                                                                  |
| humidity               | Number:Dimensionless | The current humidity reading of the thermostat (CT80 only)                                                                              |
| mode                   | Number               | The current operating mode of the HVAC system                                                                                           |
| fan_mode               | Number               | The current operating mode of the fan                                                                                                   |
| program_mode           | Number               | The program schedule that the thermostat is running (CT80 Rev B only)                                                                   |
| set_point              | Number:Temperature   | The current temperature set point (°F) of the thermostat                                                                                |
| status                 | Number               | Indicates the current running status of the HVAC system                                                                                 |
| fan_status             | Number               | Indicates the current fan status of the HVAC system                                                                                     |
| override               | Number               | Indicates if the normal program set-point has been manually overridden                                                                  |
| hold                   | Switch               | Indicates if the current set point temperature is to be held indefinitely                                                               |
| remote_temp            | Number:Temperature   | Override the internal temperature (°F) as read by the thermostat's temperature sensor; Set to -1 to return to internal temperature mode |
| day                    | Number               | The current day of the week reported by the thermostat (0 = Monday)                                                                     |
| hour                   | Number               | The current hour of the day reported by the thermostat (24 hr)                                                                          |
| minute                 | Number               | The current minute past the hour reported by the thermostat                                                                             |
| dt_stamp               | String               | The current day of the week and time reported by the thermostat (E HH:mm)                                                               |
| today_heat_runtime     | Number:Time          | The total number of minutes of heating run-time today                                                                                   |
| today_cool_runtime     | Number:Time          | The total number of minutes of cooling run-time today                                                                                   |
| yesterday_heat_runtime | Number:Time          | The total number of minutes of heating run-time yesterday                                                                               |
| yesterday_cool_runtime | Number:Time          | The total number of minutes of cooling run-time yesterday                                                                               |
| message                | String (Write Only)  | Used to display a number in the upper left 'price message' area of the thermostat's screen where the time is normally displayed         |

## Full Example

radiotherm.map:

```text
UNDEF_stus=-
NULL_stus=-
-_stus=-
0_stus=Off
1_stus=Heating
2_stus=Cooling
UNDEF_fstus=-
NULL_fstus=-
-_fstus=-
0_fstus=Off
1_fstus=On
UNDEF_mode=-
NULL_mode=-
-_mode=-
0_mode=Off
1_mode=Heat
2_mode=Cool
3_mode=Auto
UNDEF_fan=-
NULL_fan=-
-_fan=-
0_fan=Auto
1_fan=Auto/Circulate
2_fan=On
UNDEF_pgm=-
NULL_pgm=-
-_pgm=-
-1_pgm=None
0_pgm=Program A
1_pgm=Program B
2_pgm=Vacation
3_pgm=Holiday
UNDEF_over=-
NULL_over=-
-_over=-
0_over=No
1_over=Yes

```

radiotherm.things:

```java
radiothermostat:rtherm:mytherm1 "My 1st floor thermostat" [ hostName="192.168.10.1", refresh=2, logRefresh=10, isCT80=false, disableLogs=false, setpointMode="temporary" ]
radiothermostat:rtherm:mytherm2 "My 2nd floor thermostat" [ hostName="mythermhost2", refresh=1, logRefresh=20, isCT80=true, disableLogs=false, setpointMode="absolute" ]
```

radiotherm.items:

```java
Number:Temperature  Therm_Temp  "Current Temperature [%.1f °F] " <temperature>  { channel="radiothermostat:rtherm:mytherm1:temperature" }
// Humidity only supported on CT80
Number Therm_Hum                "Current Humidity [%d %%]" <humidity>           { channel="radiothermostat:rtherm:mytherm1:humidity" }
Number Therm_Mode               "Thermostat Mode [MAP(radiotherm.map):%s_mode]" { channel="radiothermostat:rtherm:mytherm1:mode" }
// The Auto/Circulate option will only appear for CT80
Number Therm_Fmode              "Fan Mode [MAP(radiotherm.map):%s_fan]"         { channel="radiothermostat:rtherm:mytherm1:fan_mode" }
// Program Mode only supported on CT80 Rev B
Number Therm_Pmode              "Program Mode [MAP(radiotherm.map):%s_pgm]"     { channel="radiothermostat:rtherm:mytherm1:program_mode" }
Number:Temperature Therm_Setpt  "Set Point [%d]" <temperature>                  { channel="radiothermostat:rtherm:mytherm1:set_point" }
Number Therm_Status             "Status [MAP(radiotherm.map):%s_stus]"          { channel="radiothermostat:rtherm:mytherm1:status" }
Number Therm_FanStatus          "Fan Status [MAP(radiotherm.map):%s_fstus]"     { channel="radiothermostat:rtherm:mytherm1:fan_status" }
Number Therm_Override           "Override [MAP(radiotherm.map):%s_over]"        { channel="radiothermostat:rtherm:mytherm1:override" }
Switch Therm_Hold               "Hold"                                          { channel="radiothermostat:rtherm:mytherm1:hold" }

Number Therm_Day                "Thermostat Day [%d]"                           { channel="radiothermostat:rtherm:mytherm1:day" }
Number Therm_Hour               "Thermostat Hour [%d]"                          { channel="radiothermostat:rtherm:mytherm1:hour" }
Number Therm_Minute             "Thermostat Minute [%d]"                        { channel="radiothermostat:rtherm:mytherm1:minute" }
String Therm_Dstmp              "Thermostat DateStamp [%s]" <time>              { channel="radiothermostat:rtherm:mytherm1:dt_stamp" }

Number:Time Therm_todayheat     "Today's Heating Runtime [%d %unit%]"           { channel="radiothermostat:rtherm:mytherm1:today_heat_runtime", unit="min" }
Number:Time Therm_todaycool     "Today's Cooling Runtime [%d %unit%]"           { channel="radiothermostat:rtherm:mytherm1:today_cool_runtime", unit="min" }
Number:Time Therm_yesterdayheat "Yesterday's Heating Runtime [%d %unit%]"       { channel="radiothermostat:rtherm:mytherm1:yesterday_heat_runtime", unit="min" }
Number:Time Therm_yesterdaycool "Yesterday's Cooling Runtime [%d %unit%]"       { channel="radiothermostat:rtherm:mytherm1:yesterday_cool_runtime", unit="min" }
String Therm_Message            "Message: [%s]"                                 { channel="radiothermostat:rtherm:mytherm1:message" }

// Override the thermostat's temperature reading with a value from an external sensor, set to -1 to revert to internal temperature mode
Number:Temperature Therm_Rtemp  "Remote Temperature [%d]" <temperature>         { channel="radiothermostat:rtherm:mytherm1:remote_temp" }

// A virtual switch used to trigger a rule to send a json command to the thermostat
Switch Therm_mysetting   "Send my preferred setting"
```

radiotherm.sitemap:

```perl
sitemap radiotherm label="My Thermostat" {
    Frame label="My 1st floor thermostat" {
        Text item=Therm_Temp icon="temperature" valuecolor=[>76="orange",>67.5="green",<=67.5="blue"]
        // Humidity only supported on CT80
        Text item=Therm_Hum icon="humidity"
        Setpoint item=Therm_Setpt label="Target temperature [%d °F]" visibility=[Therm_Mode==1,Therm_Mode==2] icon="temperature" minValue=60 maxValue=85 step=1
        Selection item=Therm_Mode icon="climate"
        Selection item=Therm_Fmode icon="fan"
        // Program Mode only supported on CT80 Rev B
        Selection item=Therm_Pmode icon="smoke"
        Text item=Therm_Status icon="climate"
        Text item=Therm_FanStatus icon="flow"
        Text item=Therm_Override icon="smoke"
        Switch item=Therm_Hold icon="smoke"

        // Example of overriding the thermostat's temperature reading
        Switch item=Therm_Rtemp label="Remote Temp" icon="temperature" mappings=[60="60", 75="75", 80="80", -1="Reset"]

        // Virtual switch/button to trigger a rule to send a custom command
        // The ON value displays in the button
        Switch item=Therm_mysetting mappings=[ON="Heat, 68, hold"]

        Text item=Therm_Day
        Text item=Therm_Hour
        Text item=Therm_Minute
        Text item=Therm_Dstmp

        Text item=Therm_todayheat
        Text item=Therm_todaycool
        Text item=Therm_yesterdayheat
        Text item=Therm_yesterdaycool
    }
}
```

radiotherm.rules:

```java
rule "Send my thermostat command"
when
  Item Therm_mysetting received command
then
  val actions = getActions("radiothermostat","radiothermostat:rtherm:mytherm1")
  if(null === actions) {
      logInfo("actions", "Actions not found, check thing ID")
      return
  }
  // JSON to send directly to the thermostat's '/tstat' endpoint
  // See RadioThermostat_CT50_Honeywell_Wifi_API_V1.3.pdf for more detail
  actions.sendRawCommand('{"hold":1, "t_heat":' + "68" + ', "tmode":1}')

  // Also a command can be sent to a specific endpoint on the thermostat by
  // specifying it as the second argument to sendRawCommand():

  // Reboot the thermostat
  // actions.sendRawCommand('{"command": "reboot"}', 'sys/command')

  // Control the energy LED (CT80 only) [0 = off, 1 = green, 2 = yellow, 4 = red]
  // actions.sendRawCommand('{"energy_led": 1}', 'tstat/led')

  // Send a message to the User Message Area (CT80 only)
  // actions.sendRawCommand('{"line": 0, "message": "Hello World!"}', 'tstat/uma')

end

rule "Display outside temp in thermostat message area"
when
  // An item containing the current outside temperature
  Item OutsideTemp changed
then
  // Display up to 5 numbers in the thermostat's Price Message Area (PMA)
  // A decimal point can be used. CT80 can display a negative '-' number
  // Sends empty string to clear the number and restore the time display if OutsideTemp is undefined
  var temp = ""

  if (newState != null && newState != UNDEF) {
      temp = Math.round((newState as DecimalType).doubleValue).intValue.toString
  }

  Therm_Message.sendCommand(temp)
end
```
