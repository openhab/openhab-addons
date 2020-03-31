# RadioThermostat Binding

This binding connects RadioThermostat/3M Filtrete models CT30, CT50/3M50, CT80, etc. with built-in Wi-Fi module to openHAB.

The binding retrieves and periodically updates all basic system information from the thermostat. The main thermostat functions such 
as thermostat mode, fan mode, temperature set point and hold mode can be controlled. System run-time information and humidity readings 
are polled less frequently and can be disabled completely if not desired. Humidity information is available only when using a CT80 
thermostat and I have noticed that the humidity reported is very inaccurate.

The main caveat for using this binding is to keep in mind that the web server in the thermostat is very slow. Do not over load it 
with excessive amounts of simultaneous commands. When changing the thermostat mode, the current temperature set point is cleared and 
a refresh of the thermostat data is done to get the new mode's set point. Since retrieving the thermostat's data is the slowest 
operation, it will take several seconds after changing the mode before the new set point is displayed. The 'Program Mode' command 
is untested and according to the published API is only available on a CT80 Rev B.

## Supported Things

There is exactly one supported thing type, which represents the thermostat.
It has the `rtherm` id.
Multiple Things can be added if more than one thermostat is to be controlled.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

|    Parameter    | Description                                                                                              |
|-----------------|----------------------------------------------------------------------------------------------------------|
| hostName        | The host name or IP address of the thermostat. Mandatory.                                                |
| refresh         | Overrides the refresh interval of the thermostat data. Optional, the default is 2 minutes.               |
| logRefresh      | Overrides the refresh interval of the run-time logs & humidity data. Optional, the default is 10 minutes. |
| disableLogs     | Disable retrieval of run-time logs from the thermostat. Optional, the default is 0.                       |
| disableHumidity | Disable retrieval of humidity information from the thermostat. Optional, the default is 0.               |

## Channels

The thermostat information that is retrieved is available as these channels:

| Channel ID            | Item Type            | Description                                                               |
|-----------------------|----------------------|---------------------------------------------------------------------------|
| name                  | String               | The name of the thermostat                                                |
| model                 | String               | The model number and firmware version of the thermostat                   |
| temperature           | Number:Temperature   | The current temperature reading of the thermostat                         |
| humidity              | Number               | The current humidity reading of the thermostat (CT80 only)                |
| mode                  | Number               | The current operating mode of the HVAC system                             |
| fan_mode              | Number               | The current operating mode of the fan                                     |
| program_mode          | Number               | The program schedule that the thermostat is running (CT80 Rev B only)     |
| set_point             | Number:Temperature   | The current temperature set point of the thermostat                       |
| status                | Number               | Indicates the current running status of the HVAC system                   |
| fan_status            | Number               | Indicates the current fan status of the HVAC system                       |
| override              | Number               | Indicates if the normal program set-point has been manually overridden    |
| hold                  | Number               | Indicates if the current set point temperature is to be held indefinitely |
| day                   | Number               | The current day of the week reported by the thermostat (0 = Monday)       |
| hour                  | Number               | The current hour of the day reported by the thermostat  (24 hr)           |
| minute                | Number               | The current minute past the hour reported by the thermostat               |
| dt_stamp              | String               | The current day of the week and time reported by the thermostat (E HH:mm) |
| last_update           | DateTime             | Last successful contact with thermostat                                   |
| today_heat_hour       | Number               | The number of hours of heating run-time today                             |
| today_heat_minute     | Number               | The number of minutes of heating run-time today                           |
| today_cool_hour       | Number               | The number of hours of cooling run-time today                             |
| today_cool_minute     | Number               | The number of minutes of cooling run-time today                           |
| yesterday_heat_hour   | Number               | The number of hours of heating run-time yesterday                         |
| yesterday_heat_minute | Number               | The number of minutes of heating run-time yesterday                       |
| yesterday_cool_hour   | Number               | The number of hours of cooling run-time yesterday                         |
| yesterday_cool_minute | Number               | The number of minutes of cooling run-time yesterday                       |

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
UNDEF_hold=-
NULL_hold=-
-_hold=-
0_hold=Off
1_hold=On

```

radiotherm.things:

```java
radiothermostat:rtherm:mytherm1 "My 1st floor thermostat" [ hostName="192.168.10.1", refresh=2, logRefresh=10, disableLogs=0, disableHumidity=0 ]
radiothermostat:rtherm:mytherm2 "My 2nd floor thermostat" [ hostName="mythermhost2", refresh=1, logRefresh=20, disableLogs=1, disableHumidity=1 ]
```

radiotherm.items:

```java
String Therm_Name               "Thermostat Name [%s]"  { channel="radiothermostat:rtherm:mytherm1:name" }
String Therm_Model              "Thermostat Model [%s]" { channel="radiothermostat:rtherm:mytherm1:model" }

Number:Temperature  Therm_Temp  "Current Temperature [%.1f °F] " <temperature>  { channel="radiothermostat:rtherm:mytherm1:temperature" }
// Humidity only supported on CT80
Number Therm_Hum                "Current Humidity [%d %%]" <temperature>        { channel="radiothermostat:rtherm:mytherm1:humidity" }
Number Therm_Mode               "Thermostat Mode [MAP(radiotherm.map):%s_mode]" { channel="radiothermostat:rtherm:mytherm1:mode" }
// The Auto/Circulate option will only appear for CT80
Number Therm_Fmode              "Fan Mode [MAP(radiotherm.map):%s_fan]"         { channel="radiothermostat:rtherm:mytherm1:fan_mode" }
// Program Mode only supported on CT80 Rev B
Number Therm_Pmode              "Program Mode [MAP(radiotherm.map):%s_pgm]"     { channel="radiothermostat:rtherm:mytherm1:program_mode" }
Number:Temperature  Therm_Setpt "Set Point [%d °F]" <temperature>               { channel="radiothermostat:rtherm:mytherm1:set_point" }
Number Therm_Status             "Status [MAP(radiotherm.map):%s_stus]"          { channel="radiothermostat:rtherm:mytherm1:status" }
Number Therm_FanStatus          "Fan Status [MAP(radiotherm.map):%s_fstus]"     { channel="radiothermostat:rtherm:mytherm1:fan_status" }
Number Therm_Override           "Override [MAP(radiotherm.map):%s_over]"        { channel="radiothermostat:rtherm:mytherm1:override" }
Number Therm_Hold               "Hold [MAP(radiotherm.map):%s_hold]"            { channel="radiothermostat:rtherm:mytherm1:hold" }

Number Therm_Day       "Thermostat Day [%s]"                                 { channel="radiothermostat:rtherm:mytherm1:day" }
Number Therm_Hour      "Thermostat Hour [%s]"                                { channel="radiothermostat:rtherm:mytherm1:hour" }
Number Therm_Minute    "Thermostat Minute [%s]"                              { channel="radiothermostat:rtherm:mytherm1:minute" }
String Therm_Dstmp     "Thermostat DateStamp [%s]" <time>                    { channel="radiothermostat:rtherm:mytherm1:dt_stamp" }
DateTime Therm_Lastupd "Thermostat Last Updated  [%1$tl:%1$tM %1$tp]" <time> { channel="radiothermostat:rtherm:mytherm1:last_update" }

Number Therm_thh "Today's Heating Hours [%s]"       { channel="radiothermostat:rtherm:mytherm1:today_heat_hour" }
Number Therm_thm "Today's Heating Minutes [%s]"     { channel="radiothermostat:rtherm:mytherm1:today_heat_minute" }
Number Therm_tch "Today's Cooling Hours [%s]"       { channel="radiothermostat:rtherm:mytherm1:today_cool_hour" }
Number Therm_tcm "Today's Cooling Minutes [%s]"     { channel="radiothermostat:rtherm:mytherm1:today_cool_minute" }
Number Therm_yhh "Yesterday's Heating Hours [%s]"   { channel="radiothermostat:rtherm:mytherm1:yesterday_heat_hour" }
Number Therm_yhm "Yesterday's Heating Minutes [%s]" { channel="radiothermostat:rtherm:mytherm1:yesterday_heat_minute" }
Number Therm_ych "Yesterday's Cooling Hours [%s]"   { channel="radiothermostat:rtherm:mytherm1:yesterday_cool_hour" }
Number Therm_ycm "Yesterday's Cooling Minutes [%s]" { channel="radiothermostat:rtherm:mytherm1:yesterday_cool_minute" }
```

radiotherm.sitemap:

```perl
sitemap radiotherm label="My Thermostat" {
	Frame label="My 1st floor thermostat" {
		Text item=Therm_Name
		Text item=Therm_Model
		
		Text item=Therm_Temp icon="temperature" valuecolor=[>76="orange",>67.5="green",<=67.5="blue"]
		Text item=Therm_Hum icon="humidity"
		Setpoint item=Therm_Setpt label="Target temperature [%d °F]" visibility=[Therm_Mode==1,Therm_Mode==2] icon="temperature" minValue=60 maxValue=85 step=1
		Switch item=Therm_Mode icon="climate"
		Switch item=Therm_Fmode icon="fan"
		Switch item=Therm_Pmode icon="smoke"
		Text item=Therm_Status icon="climate"
		Text item=Therm_FanStatus icon="flow"
		Text item=Therm_Override icon="smoke"
		Switch item=Therm_Hold icon="smoke"
		
		Text item=Therm_Day
		Text item=Therm_Hour
		Text item=Therm_Minute
		Text item=Therm_Dstmp
		Text item=Therm_Lastupd
		
		Text item=Therm_thh
		Text item=Therm_thm
		Text item=Therm_tch
		Text item=Therm_tcm
		Text item=Therm_yhh
		Text item=Therm_yhm
		Text item=Therm_ych
    }
}
```

