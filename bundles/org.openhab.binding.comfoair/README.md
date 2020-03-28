# ComfoAir Binding

This binding allows to monitor and control Zehnder ComfoAir serial controlled ventilation systems.

## Supported Things

The binding supports ComfoAir ventilation systems supporting control via RS232 serial connection.
Though the binding is developed based on the protocol description for Zehnder ComfoAir devices it should also work for mostly identical systems from different manufacturers, like StorkAir WHR930, Wernig G90-380 and Paul Santos 370 DC.
It was also successfully tested on a Wernig G90-160.

### Prerequisites

Computer communication between ComfoAir device and openHAB via RS232 connection has to be set up.
The connection should be made with a 3-wire cable connecting pins: GND, TX, RX of RS232 sockets, but RX and TX pins should be crossed (TX of ComfoAir to RX of PC, RX of ComfoAir to TX of PC).

### Serial Port Access Rights

* Take care that the user that runs openHAB has rights to access the serial port
* On Ubuntu/Debian based systems (incl. openHABian) that usually means adding the user (e.g. openhab) to the group "dialout", i.e.

```
sudo usermod -a -G dialout openhab
```

### Limitations

* Either the ComfoAir binding or the CCEase Comfocontrol can be active, but not together.
* You must implement auto mode by yourself with rules. But it is more powerful.

## Discovery

Discovery is not supported.

## Thing Configuration

For the thing creation, the appropriate serial port has to be set.

|Parameter      |Values                                   |Default        |
|---------------|-----------------------------------------|---------------|
|serialPort     |`/dev/ttyUSB0`, `COM1`, etc.             |-              |
|refreshInterval|Refresh interval in seconds (10...65535) |60             |

## Channels

The ComfoAir binding supports the following channels

| Channel ID                         | Item Type                | Label                           | Description                                                                                 | Read Only | Advanced |
|------------------------------------|--------------------------|---------------------------------|---------------------------------------------------------------------------------------------|-----------|----------|
| Binding Control                    |                          |                                 |                                                                                             |           |          |
| bindingControl#activate            | Switch                   | Activate Binding Control        | Activate (control through openHAB) or deactivate (return control to CCEase) binding control | false     | no       |
| CC Ease Functions                  |                          |                                 |                                                                                             |           |          |
| ccease#fanLevel                    | Number                   | Fan Level                       | Fan level                                                                                   | false     | no       |
| ccease#targetTemperature           | Number:Temperature       | Target Temperature              | Target (comfort) temperature                                                                | false     | no       |
| ccease#filterError                 | Switch                   | Filter Error                    | Filter full                                                                                 | false     | no       |
| ccease#errorMessage                | String                   | Error Message                   | Current errors                                                                              | false     | no       |
| ccease#filterReset                 | Switch                   | Filter Reset                    | Reset filter uptime                                                                         | false     | no       |
| ccease#errorReset                  | Switch                   | Error Reset                     | Reset errors                                                                                | false     | no       |
| Ventilation Values                 |                          |                                 |                                                                                             |           |          |
| ventilation#fanOut0                | Number                   | Fan Out Level 0 (away)          | Fan level 0 performance (%) of outgoing fan                                                 | true      | yes      |
| ventilation#fanOut1                | Number                   | Fan Out Level 1                 | Fan level 1 performance (%) of outgoing fan                                                 | true      | yes      |
| ventilation#fanOut2                | Number                   | Fan Out Level 2                 | Fan level 2 performance (%) of outgoing fan                                                 | true      | yes      |
| ventilation#fanOut3                | Number                   | Fan Out Level 3                 | Fan level 3 performance (%) of outgoing fan                                                 | true      | yes      |
| ventilation#fanIn0                 | Number                   | Fan In Level 0 (away)           | Fan level 0 performance (%) of incoming fan                                                 | true      | yes      |
| ventilation#fanIn1                 | Number                   | Fan In Level 1                  | Fan level 1 performance (%) of incoming fan                                                 | true      | yes      |
| ventilation#fanIn2                 | Number                   | Fan In Level 2                  | Fan level 2 performance (%) of incoming fan                                                 | true      | yes      |
| ventilation#fanIn3                 | Number                   | Fan In Level 3                  | Fan level 3 performance (%) of incoming fan                                                 | true      | yes      |
| ventilation#fanInPercent           | Number                   | Fan In (%)                      | Current relative speed (%) of incoming fan                                                  | true      | yes      |
| ventilation#fanOutPercent          | Number                   | Fan Out (%)                     | Current relative speed (%) of outgoing fan                                                  | true      | yes      |
| ventilation#fanInRPM               | Number                   | Fan In (rpm)                    | Current absolute speed (rpm) of incoming fan                                                | true      | yes      |
| ventilation#fanOutRPM              | Number                   | Fan Out (rpm)                   | Current absolute speed (rpm) of outgoing fan                                                | true      | yes      |
| Temperature Values                 |                          |                                 |                                                                                             |           |          |
| temperatures#outdoorTemperatureIn  | Number:Temperature       | Outdoor Temperature Incoming    | Intake air temperature (outside)                                                            | true      | yes      |
| temperatures#outdoorTemperatureOut | Number:Temperature       | Outdoor Temperature Outgoing    | Outlet air temperature (outside)                                                            | true      | yes      |
| temperatures#indoorTemperatureIn   | Number:Temperature       | Indoor Temperature Incoming     | Inlet air temperature (inside)                                                              | true      | yes      |
| temperatures#indoorTemperatureOut  | Number:Temperature       | Indoor Temperature Outgoing     | Uptake air temperature (inside)                                                             | true      | yes      |
| temperatures#isT1Sensor            | Switch                   | Sensor T1 Available             | Availability of temperature sensor T1 (outdoor in)                                          | true      | yes      |
| temperatures#isT2Sensor            | Switch                   | Sensor T2 Available             | Availability of temperature sensor T2 (indoor in)                                           | true      | yes      |
| temperatures#isT3Sensor            | Switch                   | Sensor T3 Available             | Availability of temperature sensor T3 (indoor out)                                          | true      | yes      |
| temperatures#isT4Sensor            | Switch                   | Sensor T4 Available             | Availability of temperature sensor T4 (outdoor out)                                         | true      | yes      |
| temperatures#isEWTSensor           | Switch                   | EWT Sensor Available            | Availability of EWT temperature sensor                                                      | true      | yes      |
| temperatures#isHeaterSensor        | Switch                   | Heater Sensor Available         | Availability of heater temperature sensor                                                   | true      | yes      |
| temperatures#isCookerhoodSensor    | Switch                   | Cookerhood Sensor Available     | Availability of cookerhood temperature sensor                                               | true      | yes      |
| temperatures#ewtTemperature        | Number:Temperature       | EWT Temperature                 | Temperature of geothermal heat exchanger sensor                                             | true      | yes      |
| temperatures#heaterTemperature     | Number:Temperature       | Heater Temperature              | Temperature of heater sensor                                                                | true      | yes      |
| temperatures#cookerhoodTemperature | Number:Temperature       | Cookerhood Temperature          | Temperature of cookerhood sensor                                                            | true      | yes      |
| Uptimes                            |                          |                                 |                                                                                             | true      | yes      |
| times#level0Time                   | Number                   | Level 0 duration                | Uptime at level 0 (away)                                                                    | true      | no       |
| times#level1Time                   | Number                   | Level 1 duration                | Uptime at level 1                                                                           | true      | no       |
| times#level2Time                   | Number                   | Level 2 duration                | Uptime at level 2                                                                           | true      | no       |
| times#level3Time                   | Number                   | Level 3 duration                | Uptime at level 3                                                                           | true      | no       |
| times#freezeTime                   | Number                   | Antifrost Duration              | Uptime of antifrost                                                                         | true      | no       |
| times#preheaterTime                | Number                   | Preheater Duration              | Uptime of preheater                                                                         | true      | no       |
| times#bypassTime                   | Number                   | Bypass Duration                 | Hours of bypass open                                                                        | true      | no       |
| times#filterHours                  | Number                   | Filter Duration                 | Uptime of the filter                                                                        | true      | no       |
| Menu P1: Control States            |                          |                                 |                                                                                             |           |          |
| menuP1#menu20Mode                  | Switch                   | Menu 20 Mode (P10)              | State of menu 20 mode (P10)                                                                 | true      | yes      |
| menuP1#menu21Mode                  | Switch                   | Menu 21 Mode (P11)              | State of menu 21 mode (P11)                                                                 | true      | yes      |
| menuP1#menu22Mode                  | Switch                   | Menu 22 Mode (P12)              | State of menu 22 mode (P12)                                                                 | true      | yes      |
| menuP1#menu23Mode                  | Switch                   | Menu 23 Mode (P13)              | State of menu 23 mode (P13)                                                                 | true      | yes      |
| menuP1#menu24Mode                  | Switch                   | Menu 24 Mode (P14)              | State of menu 24 mode (P14)                                                                 | true      | yes      |
| menuP1#menu25Mode                  | Switch                   | Menu 25 Mode (P15)              | State of menu 25 mode (P15)                                                                 | true      | yes      |
| menuP1#menu26Mode                  | Switch                   | Menu 26 Mode (P16)              | State of menu 26 mode (P16)                                                                 | true      | yes      |
| menuP1#menu27Mode                  | Switch                   | Menu 27 Mode (P17)              | State of menu 27 mode (P17)                                                                 | true      | yes      |
| menuP1#menu28Mode                  | Switch                   | Menu 28 Mode (P18)              | State of menu 28 mode (P18)                                                                 | true      | yes      |
| menuP1#menu29Mode                  | Switch                   | Menu 29 Mode (P19)              | State of menu 29 mode (P19)                                                                 | true      | yes      |
| Menu P2: Delay Settings            |                          |                                 |                                                                                             |           |          |
| menuP2#bathroomStartDelay          | Number                   | Menu P21                        | Start delay for bathroom switch (min)                                                       | false     | yes      |
| menuP2#bathroomEndDelay            | Number                   | Menu P22                        | End delay for bathroom switch (min)                                                         | false     | yes      |
| menuP2#L1EndDelay                  | Number                   | Menu P23                        | End delay for L1 switch (min)                                                               | false     | yes      |
| menuP2#pulseVentilation            | Number                   | Menu P27                        | Period for pulse ventilation (min)                                                          | false     | yes      |
| menuP2#filterWeeks                 | Number                   | Menu P24                        | Usage period until filter pollution message (weeks)                                         | false     | yes      |
| menuP2#RFShortDelay                | Number                   | Menu P25                        | End delay (RF short actuation) for ventilation level 3 (min)                                | false     | yes      |
| menuP2#RFLongDelay                 | Number                   | Menu P26                        | End delay (RF long actuation) for ventilation level 3 (min)                                 | false     | yes      |
| menuP2#cookerhoodDelay             | Number                   | Menu P20                        | End delay for cooker hood control (min)                                                     | false     | yes      |
| Menu P9: Option States             |                          |                                 |                                                                                             |           |          |
| menuP9#chimneyState                | Switch                   | Chimney Control State           | State of the chimney control                                                                | true      | yes      |
| menuP9#bypassState                 | Switch                   | Bypass State                    | State of the bypass (ON = open / OFF = closed)                                              | true      | yes      |
| menuP9#ewtState                    | Switch                   | EWT State                       | State of the EWT valve (ON = open / OFF = closed)                                           | true      | yes      |
| menuP9#heaterState                 | Switch                   | Heater State                    | State of the heater                                                                         | true      | yes      |
| menuP9#vControlState               | Switch                   | 0-10V Control State             | State of the 0-10V control                                                                  | true      | yes      |
| menuP9#frostState                  | Switch                   | Antifrost State                 | State of the antifrost control                                                              | true      | yes      |
| menuP9#cookerhoodState             | Switch                   | Cookerhood State                | State of the cookerhood control                                                             | true      | yes      |
| menuP9#enthalpyState               | Switch                   | Enthalpy State                  | State of the enthalpy module                                                                | true      | yes      |
| Error States                       |                          |                                 |                                                                                             |           |          |
| error#errorACurrent                | String                   | Error A Current                 | Current error A                                                                             | true      | no       |
| error#errorECurrent                | String                   | Error E Current                 | Current error E                                                                             | true      | no       |
| error#errorALast                   | String                   | Error A Last                    | Last error A                                                                                | true      | no       |
| error#errorELast                   | String                   | Error E Last                    | Last error E                                                                                | true      | no       |
| error#errorAPrelast                | String                   | Error A Prelast                 | Prelast error A                                                                             | true      | no       |
| error#errorEPrelast                | String                   | Error E Prelast                 | Prelast error E                                                                             | true      | no       |
| error#errorAPrePrelast             | String                   | Error A Pre-Prelast             | Pre-Prelast error A                                                                         | true      | no       |
| error#errorEPrePrelast             | String                   | Error E Pre-Prelast             | Pre-Prelast error E                                                                         | true      | no       |
| error#errorEACurrent               | String                   | Error EA Current                | Current error EA                                                                            | true      | no       |
| error#errorEALast                  | String                   | Error EA Last                   | Last error EA                                                                               | true      | no       |
| error#errorEAPrelast               | String                   | Error EA Prelast                | Prelast error EA                                                                            | true      | no       |
| error#errorEAPrePrelast            | String                   | Error EA Pre-Prelast            | Pre-Prelast error EA                                                                        | true      | no       |
| error#errorAHighCurrent            | String                   | Error A (high) Current          | Current error A (high)                                                                      | true      | no       |
| error#errorAHighLast               | String                   | Error A (high) Last             | Last error A (high)                                                                         | true      | no       |
| error#errorAHighPrelast            | String                   | Error A (high) Prelast          | Prelast error A (high)                                                                      | true      | no       |
| error#errorAHighPrePrelast         | String                   | Error A (high) Pre-Prelast      | Pre-Prelast error A (high)                                                                  | true      | no       |
| Bypass Values                      |                          |                                 |                                                                                             |           |          |
| bypass#bypassFactor                | Number                   | Bypass Factor                   | Bypass factor value                                                                         | true      | yes      |
| bypass#bypassLevel                 | Number                   | Bypass Level                    | Bypass level state                                                                          | true      | yes      |
| bypass#bypassCorrection            | Number                   | Bypass Correction               | Bypass correction state                                                                     | true      | yes      |
| bypass#bypassSummer                | Switch                   | Bypass Summer Mode              | Bypass summer mode                                                                          | true      | yes      |
| Preheater Values                   |                          |                                 |                                                                                             |           |          |
| preheater#preheaterValve           | Number                   | Preheater Valve                 | State of the preheater valve                                                                | true      | yes      |
| preheater#preheaterFrostProtect    | Switch                   | Frost Protection                | State of the frost protection                                                               | true      | yes      |
| preheater#preheaterHeating         | Switch                   | Preheater                       | State of the preheater                                                                      | true      | yes      |
| preheater#preheaterFrostTime       | Number                   | Preheater Frost Time            | Frost minutes                                                                               | true      | yes      |
| preheater#preheaterSafety          | Number                   | Preheater Frost Safety          | Frost safety setting                                                                        | true      | yes      |
| EWT Values                         |                          |                                 |                                                                                             |           |          |
| ewt#ewtTemperatureLow              | Number:Temperature       | EWT Temperature (low)           | Lower temperature of the geothermal heat exchanger                                          | true      | yes      |
| ewt#ewtTemperatureHigh             | Number:Temperature       | EWT Temperature (high)          | Upper temperature of the geothermal heat exchanger                                          | true      | yes      |
| ewt#ewtSpeed                       | Number                   | EWT Speed Up (%)                | Speed up of the geothermal heat exchanger                                                   | true      | yes      |
| Heater Values                      |                          |                                 |                                                                                             |           |          |
| heater#heaterPower                 | Number                   | Heater Power                    | Heater power value                                                                          | true      | yes      |
| heater#heaterPowerI                | Number                   | Heater Power I-parameter        | Heater power I-parameter value                                                              | true      | yes      |
| heater#heaterTargetTemperature     | Number:Temperature       | Heater Target Temperature       | Target temperature of the heater                                                            | true      | yes      |
| Cookerhood Values                  |                          |                                 |                                                                                             |           |          |
| cookerhood#cookerhoodSpeed         | Number                   | Cookerhood Speed Up (%)         | Speed up of the cookerhood                                                                  | true      | yes      |
| Enthalpy Values                    |                          |                                 |                                                                                             |           |          |
| enthalpy#enthalpyTemperature       | Number:Temperature       | Enthalpy Sensor Temperature     | Temperature of the enthalpy sensor                                                          | true      | yes      |
| enthalpy#enthalpyHumidity          | Number                   | Enthalpy Sensor Humidity        | Humidity of the enthalpy sensor                                                             | true      | yes      |
| enthalpy#enthalpyLevel             | Number                   | Enthalpy Sensor Level           | Level of the enthalpy sensor                                                                | true      | yes      |
| enthalpy#enthalpyTime              | Number                   | Enthalpy Sensor Timer           | Timer state of the enthalpy sensor                                                          | true      | yes      |
| Option States                      |                          |                                 |                                                                                             |           |          |
| options#isPreheater                | Switch                   | Preheater                       | Preheater option installed                                                                  | false     | yes      |
| options#isBypass                   | Switch                   | Bypass                          | Bypass option installed                                                                     | false     | yes      |
| options#recuType                   | Number                   | Comfoair Type                   | Type of the ComfoAir (1 = left / 2 = right)                                                 | false     | yes      |
| options#recuSize                   | Number                   | Comfoair Size                   | Size of the ComfoAir (1 = big / 2 = small)                                                  | false     | yes      |
| options#isChimney                  | Switch                   | Chimney                         | Chimney option installed                                                                    | false     | yes      |
| options#isCookerhood               | Switch                   | Cookerhood                      | Cookerhood option installed                                                                 | false     | yes      |
| options#isHeater                   | Switch                   | Heater                          | Heater option installed                                                                     | false     | yes      |
| options#isEnthalpy                 | Switch                   | Enthalpy                        | Enthalpy option installed                                                                   | false     | yes      |
| options#isEWT                      | Switch                   | EWT                             | EWT option installed                                                                        | false     | yes      |
| Inputs                             |                          |                                 |                                                                                             |           |          |
| inputs#isL1Switch                  | Switch                   | L1 Switch                       | Availability of L1 step switch                                                              | true      | yes      |
| inputs#isL2Switch                  | Switch                   | L2 Switch                       | Availability of L2 step switch                                                              | true      | yes      |
| inputs#isBathroomSwitch            | Switch                   | Bathroom Switch                 | Availability of bathroom switch                                                             | true      | yes      |
| inputs#isCookerhoodSwitch          | Switch                   | Cookerhood Switch               | Availability of cookerhood switch                                                           | true      | yes      |
| inputs#isExternalFilter            | Switch                   | External Filter                 | Availability of external filter                                                             | true      | yes      |
| inputs#isWTW                       | Switch                   | Heat Recovery                   | Availability of heat recovery (WTW)                                                         | true      | yes      |
| inputs#isBathroom2Switch           | Switch                   | Bathroom Switch 2               | Availability of bathroom switch 2 (luxe)                                                    | true      | yes      |
| Analog Inputs                      |                          |                                 |                                                                                             |           |          |
| analog#isAnalog1                   | Switch                   | Analog Input 1 Availability     | Availability of analog input 1                                                              | false     | yes      |
| analog#isAnalog2                   | Switch                   | Analog Input 2 Availability     | Availability of analog input 2                                                              | false     | yes      |
| analog#isAnalog3                   | Switch                   | Analog Input 3 Availability     | Availability of analog input 3                                                              | false     | yes      |
| analog#isAnalog4                   | Switch                   | Analog Input 4 Availability     | Availability of analog input 4                                                              | false     | yes      |
| analog#isRF                        | Switch                   | RF Input Availability           | Availability of RF input                                                                    | false     | yes      |
| analog#analog1Mode                 | Switch                   | Analog Input 1 State            | State of analog input 1                                                                     | false     | yes      |
| analog#analog2Mode                 | Switch                   | Analog Input 2 State            | State of analog input 2                                                                     | false     | yes      |
| analog#analog3Mode                 | Switch                   | Analog Input 3 State            | State of analog input 3                                                                     | false     | yes      |
| analog#analog4Mode                 | Switch                   | Analog Input 4 State            | State of analog input 1                                                                     | false     | yes      |
| analog#RFMode                      | Switch                   | RF Input State                  | State of RF input                                                                           | false     | yes      |
| analog#analog1Negative             | Switch                   | Analog Input 1 Postive/Negative | Postive/Negative state of analog input 1                                                    | false     | yes      |
| analog#analog2Negative             | Switch                   | Analog Input 2 Postive/Negative | Postive/Negative state of analog input 2                                                    | false     | yes      |
| analog#analog3Negative             | Switch                   | Analog Input 3 Postive/Negative | Postive/Negative state of analog input 3                                                    | false     | yes      |
| analog#analog4Negative             | Switch                   | Analog Input 4 Postive/Negative | Postive/Negative state of analog input 1                                                    | false     | yes      |
| analog#RFNegative                  | Switch                   | RF Input Postive/Negative       | Postive/Negative state of RF input                                                          | false     | yes      |
| analog#analog1Volt                 | Number:ElectricPotential | Analog Input 1 Voltage Level    | Voltage level of analog input 1                                                             | false     | yes      |
| analog#analog1Min                  | Number                   | Analog Input 1 Min              | Minimum setting for analog input 1                                                          | false     | yes      |
| analog#analog1Max                  | Number                   | Analog Input 1 Max              | Maximum setting for analog input 1                                                          | false     | yes      |
| analog#analog1Value                | Number                   | Analog Input 1 Target           | Target setting for analog input 1                                                           | false     | yes      |
| analog#analog2Volt                 | Number:ElectricPotential | Analog Input 2 Voltage Level    | Voltage level of analog input 2                                                             | false     | yes      |
| analog#analog2Min                  | Number                   | Analog Input 2 Min              | Minimum setting for analog input 2                                                          | false     | yes      |
| analog#analog2Max                  | Number                   | Analog Input 2 Max              | Maximum setting for analog input 2                                                          | false     | yes      |
| analog#analog2Value                | Number                   | Analog Input 2 Target           | Target setting for analog input 2                                                           | false     | yes      |
| analog#analog3Volt                 | Number:ElectricPotential | Analog Input 3 Voltage Level    | Voltage level of analog input 3                                                             | false     | yes      |
| analog#analog3Min                  | Number                   | Analog Input 3 Min              | Minimum setting for analog input 3                                                          | false     | yes      |
| analog#analog3Max                  | Number                   | Analog Input 3 Max              | Maximum setting for analog input 3                                                          | false     | yes      |
| analog#analog3Value                | Number                   | Analog Input 3 Target           | Target setting for analog input 3                                                           | false     | yes      |
| analog#analog4Volt                 | Number:ElectricPotential | Analog Input 4 Voltage Level    | Voltage level of analog input 4                                                             | false     | yes      |
| analog#analog4Min                  | Number                   | Analog Input 4 Min              | Minimum setting for analog input 4                                                          | false     | yes      |
| analog#analog4Max                  | Number                   | Analog Input 4 Max              | Maximum setting for analog input 4                                                          | false     | yes      |
| analog#analog4Value                | Number                   | Analog Input 4 Target           | Target setting for analog input 4                                                           | false     | yes      |
| analog#RFMin                       | Number                   | RF Input Min                    | Minimum setting for RF input                                                                | false     | yes      |
| analog#RFMax                       | Number                   | RF Input Max                    | Maximum setting for RF input                                                                | false     | yes      |
| analog#RFValue                     | Number                   | RF Input Target                 | Target setting for RF input                                                                 | false     | yes      |
| Software Version                   |                          |                                 |                                                                                             |           |          |
| software#softwareMainVersion       | Number                   | Software Main Version           | Main version of the device software                                                         | true      | yes      |
| software#softwareMinorVersion      | Number                   | Software Minor Version          | Minor version of the device software                                                        | true      | yes      |
| software#softwareBetaVersion       | Number                   | Software Beta Version           | Beta version of the device software                                                         | true      | yes      |

## Full Example

`.things` file:

```
Thing comfoair:comfoair:myComfoAir "ComfoAir" [serialPort="/dev/ttyUSB0", refreshInterval="60"]
```

`.items` file:

```
// ComfoAir
Group	ComfoAir				"ComfoAir"					<recu>		(devices)

// Temperatures chart
Group	comfoairTemps_Chart									<temperature>	(ComfoAir)
Number	comfoairTemps_Chart_Period		"Period"

// Control
Switch	comfoairControl				"Activate"					<computer>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:bindingControl#activate"}
Number	comfoairFanLevel			"Ventilation level [%d]"			<chart>		(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ccease#fanLevel"}
Switch	comfoairErrorReset			"Error reset"					<service>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ccease#errorReset"}
Switch	comfoairFilterReset			"Filter reset"					<service>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ccease#filterReset"}
Number	comfoairReset				"Reset"						<reset>		(ComfoAir)
Number	comfoairMode				"Manual - Auto [%d]"				<controlMode>	(ComfoAir)
Switch	comfoairControl_Switch			"Activate"					<control>	(ComfoAir)
Number	comfoairFilterPeriod			"Filter period [%d weeks]"			<clock>		(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP2#filterWeeks"}
Switch	comfoairChimney				"Fire programme [MAP(comfoair_is-not.map):%s]"				<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:options#isChimney"}
Switch	comfoairPreheater			"Preheater [MAP(comfoair_is-not.map):%s]"	<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:options#isPreheater"}
Switch	comfoairCookerHood			"Extractor hood [MAP(comfoair_is-not.map):%s]"	<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:options#isCookerhood"}
Switch	comfoairBypass				"Bypass [MAP(comfoair_is-not.map):%s]"					<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:options#isBypass"}
Switch	comfoairEWT				"EWT"					<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:options#isEWT"}
Switch	comfoairEnthalpy			"Enthalpy"	<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:options#isEnthalpy"}

// Messages
String	comfoairError												(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ccease#errorMessage"}
String	comfoairError_Message			"Messages [%s]"					<attention>	(ComfoAir)
Number	comfoairFilterRuntime											(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:times#filterHours"}
String	comfoairFilterRuntime_Message		"Filter time [%s]"				<clock>		(ComfoAir)
String	comfoairFrozenError
String	comfoairInletError

// State
Number:Temperature	comfoairTargetTemperature		"Comfort temperature [%.1f °C]"			<temperature>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ccease#targetTemperature"}
Number:Temperature	comfoairOutdoorIncomingTemperature	"Inlet air temperature [%.1f °C]"		<temperature>	(ComfoAir, comfoairTemps_Chart)	{channel="comfoair:comfoair:myComfoAir:temperatures#outdoorTemperatureIn"}
Number:Temperature	comfoairIndoorIncomingTemperature	"Supply air temperature [%.1f °C]"		<temperature>	(ComfoAir, comfoairTemps_Chart)	{channel="comfoair:comfoair:myComfoAir:temperatures#indoorTemperatureIn"}
Number:Temperature	comfoairIndoorOutgoingTemperature	"Return air temperature [%.1f °C]"		<temperature>	(ComfoAir, comfoairTemps_Chart)	{channel="comfoair:comfoair:myComfoAir:temperatures#indoorTemperatureOut"}
Number:Temperature	comfoairOutdoorOutgoingTemperature	"Exhaust air temperature [%.1f °C]"		<temperature>	(ComfoAir, comfoairTemps_Chart)	{channel="comfoair:comfoair:myComfoAir:temperatures#outdoorTemperatureOut"}
Number	comfoairIncomingFan			"Supply capacity [%d %%]"			<fan_in>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ventilation#fanInPercent"}
Number	comfoairOutgoingFan			"Exhaust capasity [%d %%]"			<fan_out>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ventilation#fanOutPercent"}
Number	comfoairFanIn0				"Supply capacity - level 0 [%d %%]"		<fan_in>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ventilation#fanIn0"}
Number	comfoairFanOut0				"Exhaust capacity - level 0 [%d %%]"		<fan_out>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:ventilation#fanOut0"}
Number	comfoairEfficiency			"Efficiency [%.1f %%]"				<efficiency>	(ComfoAir)
Switch	comfoairBypassMode			"Bypass [MAP(comfoair_bypass.map):%s]"		<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP9#bypassState"}
Switch	comfoairEWTMode 			"EWT [MAP(comfoair_on-off.map):%s]"		<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP9#ewtState"}
Switch	comfoairChimneyMode			"Fire programme [MAP(comfoair_on-off.map):%s]"	<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP9#chimneyState"}
Switch	comfoairHeaterMode			"Heater [MAP(comfoair_on-off.map):%s]"	<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP9#heaterState"}
Switch	comfoairCookerHoodMode			"Extractor hood [MAP(comfoair_on-off.map):%s]"	<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP9#cookerhoodState"}
Switch	comfoairEnthalpyMode			"Enthalpy [MAP(comfoair_on-off.map):%s]"	<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP9#enthalpyState"}
Switch	comfoairFreezeMode			"Freeze [MAP(comfoair_freeze.map):%s]"		<climate>	(ComfoAir)			{channel="comfoair:comfoair:myComfoAir:menuP9#frostState"}
```

`.sitemap` file:

```
sitemap comfoair label="ComfoAir" {
	Frame label="Main" {
		Text item=comfoairError_Message labelcolor=[!="OK"="red"] valuecolor=[!="OK"="red"]
		Switch item=comfoairControl mappings=[0="CCEase", 1="Computer"]
		Switch item=comfoairErrorReset mappings=[ON="Reset"]
		Switch item=comfoairFilterReset mappings=[ON="Reset"]
	}
	Frame label="Control" {
		Switch item=comfoairMode mappings=[0="Manual", 1="Auto"]
		Switch item=comfoairFanMode_Message mappings=[0="Sup + Exh", 1="Supply", 2="Exhaust"]
		Switch item=comfoairFanLevel_Message mappings=[2="Level 1", 3="Level 2", 4="Level 3"]
		Setpoint item=comfoairTargetTemperature_Message step=0.5 minValue=15 maxValue=28 valuecolor=["black"]
	}
	Frame label="State" {
		Text item=comfoairOutdoorIncomingTemperature valuecolor=["black"]
		Text item=comfoairOutdoorOutgoingTemperature valuecolor=["black"]
		Text item=comfoairIndoorIncomingTemperature valuecolor=["black"]
		Text item=comfoairIndoorOutgoingTemperature valuecolor=["black"]
	}
	Frame {
		Text item=comfoairIncomingFan valuecolor=["black"]
		Text item=comfoairBypassMode valuecolor=["black"]
		Text item=comfoairOutgoingFan valuecolor=["black"]
		Text item=comfoairEWTMode valuecolor=[OFF="silver", ON="black"]
		Text item=comfoairEfficiency valuecolor=["black"]
		Text item=comfoairFreezeMode valuecolor=[OFF="black", ON="red"]
		Text item=comfoairFilterRuntime_Message valuecolor=["black"]
		Text item=comfoairChimneyMode valuecolor=[OFF="silver", ON="black"]
	}
	Frame label="Results" {
		Text label="Charts" icon="chart" {
			Switch item=comfoairTemps_Chart_Period mappings=[0="Day", 1="Week", 2="Month", 3="Year"]
			Chart item=comfoairTemps_Chart period=D refresh=10000 visibility=[comfoairTemps_Chart_Period==0]
			Chart item=comfoairTemps_Chart period=W refresh=60000 visibility=[comfoairTemps_Chart_Period==1, comfoairTemps_Chart_Period=="Uninitialized"]
			Chart item=comfoairTemps_Chart period=M refresh=60000 visibility=[comfoairTemps_Chart_Period==2]
			Chart item=comfoairTemps_Chart period=Y refresh=600000 visibility=[comfoairTemps_Chart_Period==3]
		}
	}
}
```

`comfoair_bypass.map` file:

```
1=Opened
0=Closed
undefined=unknown
-=unknown
```

`comfoair_on-off.map` file:

```
ON=active
OFF=inactive
undefined=unknown
-=unknown
```

`comfoair_is-not.map` file:

```
ON=installed
OFF=not installed
undefined=unknown
-=unknown
```

`comfoair_freeze.map` file:

```
ON=frozen
OFF=OK
undefined=unknown
-=unknown
```

## Control Protocol Reference

For reference the protocol description used can be found here (german version only):
[Protokollbeschreibung Zehnder ComfoAir](http://www.see-solutions.de/sonstiges/Protokollbeschreibung_ComfoAir.pdf)
