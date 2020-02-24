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

| Channel ID                         | Item Type                | Label                             | Description                                                                                 | Advanced | Read Only |
|------------------------------------|--------------------------|-----------------------------------|---------------------------------------------------------------------------------------------|----------|-----------|
| bindingControl#activate            | Switch                   | Activate Binding Control          | Activate (control through openHAB) or deactivate (return control to CCEase) binding control | no       | false     |
| ccease#fanLevel                    | Number                   | Fan Level                         | Fan level                                                                                   | no       | false     |
| ccease#targetTemperature           | Number:Temperature       | Target Temperature                | Target (comfort) temperature                                                                | no       | false     |
| ccease#filterError                 | Switch                   | Filter Error                      | Filter full                                                                                 | no       | true      |
| ccease#errorMessage                | String                   | Error Message                     | Current errors                                                                              | no       | true      |
| ccease#filterReset                 | Switch                   | Filter Reset                      | Reset filter operating hours                                                                | no       | false     |
| ccease#errorReset                  | Switch                   | Error Reset                       | Reset errors                                                                                | no       | false     |
| ventilation#fanOut0                | Number                   | Fan Out Level 0 (away)            | Fan level 0 performance (%) of outgoing fan                                                 | yes      | false     |
| ventilation#fanOut1                | Number                   | Fan Out Level 1                   | Fan level 1 performance (%) of outgoing fan                                                 | yes      | false     |
| ventilation#fanOut2                | Number                   | Fan Out Level 2                   | Fan level 2 performance (%) of outgoing fan                                                 | yes      | false     |
| ventilation#fanOut3                | Number                   | Fan Out Level 3                   | Fan level 3 performance (%) of outgoing fan                                                 | yes      | false     |
| ventilation#fanIn0                 | Number                   | Fan In Level 0 (away)             | Fan level 0 performance (%) of incoming fan                                                 | yes      | false     |
| ventilation#fanIn1                 | Number                   | Fan In Level 1                    | Fan level 1 performance (%) of incoming fan                                                 | yes      | false     |
| ventilation#fanIn2                 | Number                   | Fan In Level 2                    | Fan level 2 performance (%) of incoming fan                                                 | yes      | false     |
| ventilation#fanIn3                 | Number                   | Fan In Level 3                    | Fan level 3 performance (%) of incoming fan                                                 | yes      | false     |
| ventilation#fanInPercent           | Number                   | Fan In (%)                        | Current relative speed (%) of incoming fan                                                  | no       | true      |
| ventilation#fanOutPercent          | Number                   | Fan Out (%)                       | Current relative speed (%) of outgoing fan                                                  | no       | true      |
| ventilation#fanInRPM               | Number                   | Fan In (rpm)                      | Current absolute speed (rpm) of incoming fan                                                | yes      | true      |
| ventilation#fanOutRPM              | Number                   | Fan Out (rpm)                     | Current absolute speed (rpm) of outgoing fan                                                | yes      | true      |
| temperatures#outdoorTemperatureIn  | Number:Temperature       | Outdoor Temperature Incoming      | Intake air temperature (outside)                                                            | no       | true      |
| temperatures#outdoorTemperatureOut | Number:Temperature       | Outdoor Temperature Outgoing      | Outlet air temperature (outside)                                                            | no       | true      |
| temperatures#indoorTemperatureIn   | Number:Temperature       | Indoor Temperature Incoming       | Inlet air temperature (inside)                                                              | no       | true      |
| temperatures#indoorTemperatureOut  | Number:Temperature       | Indoor Temperature Outgoing       | Uptake air temperature (inside)                                                             | no       | true      |
| temperatures#isT1Sensor            | Switch                   | Sensor T1 Available               | Availability of temperature sensor T1 (outdoor in)                                          | yes      | true      |
| temperatures#isT2Sensor            | Switch                   | Sensor T2 Available               | Availability of temperature sensor T2 (indoor in)                                           | yes      | true      |
| temperatures#isT3Sensor            | Switch                   | Sensor T3 Available               | Availability of temperature sensor T3 (indoor out)                                          | yes      | true      |
| temperatures#isT4Sensor            | Switch                   | Sensor T4 Available               | Availability of temperature sensor T4 (outdoor out)                                         | yes      | true      |
| temperatures#isEWTSensor           | Switch                   | EWT Sensor Available              | Availability of EWT temperature sensor                                                      | yes      | true      |
| temperatures#isHeaterSensor        | Switch                   | Heater Sensor Available           | Availability of heater temperature sensor                                                   | yes      | true      |
| temperatures#isCookerhoodSensor    | Switch                   | Cookerhood Sensor Available       | Availability of cookerhood temperature sensor                                               | yes      | true      |
| temperatures#ewtTemperature        | Number:Temperature       | EWT Temperature                   | Temperature of geothermal heat exchanger sensor                                             | yes      | true      |
| temperatures#heaterTemperature     | Number:Temperature       | Heater Temperature                | Temperature of heater sensor                                                                | yes      | true      |
| temperatures#cookerhoodTemperature | Number:Temperature       | Cookerhood Temperature            | Temperature of cookerhood sensor                                                            | yes      | true      |
| times#level0Time                   | Number                   | Level 0 duration                  | Operating hours at level 0 (away)                                                           | yes      | true      |
| times#level1Time                   | Number                   | Level 1 duration                  | Operating hours at level 1                                                                  | yes      | true      |
| times#level2Time                   | Number                   | Level 2 duration                  | Operating hours at level 2                                                                  | yes      | true      |
| times#level3Time                   | Number                   | Level 3 duration                  | Operating hours at level 3                                                                  | yes      | true      |
| times#freezeTime                   | Number                   | Antifrost Duration                | Operating hours of antifrost                                                                | yes      | true      |
| times#preheaterTime                | Number                   | Preheater Duration                | Operating hours of preheater                                                                | yes      | true      |
| times#bypassTime                   | Number                   | Bypass Duration                   | Hours of bypass open                                                                        | yes      | true      |
| times#filterHours                  | Number                   | Filter Duration                   | Operating hours of the filter                                                               | no       | true      |
| bypass#bypassFactor                | Number                   | Bypass Factor                     | Bypass factor value                                                                         | yes      | true      |
| bypass#bypassLevel                 | Number                   | Bypass Level                      | Bypass level state                                                                          | yes      | true      |
| bypass#bypassCorrection            | Number                   | Bypass Correction                 | Bypass correction state                                                                     | yes      | true      |
| bypass#bypassSummer                | Switch                   | Bypass Summer Mode                | Bypass summer mode                                                                          | yes      | true      |
| preheater#preheaterValve           | Number                   | Preheater Valve                   | State of the preheater valve                                                                | yes      | true      |
| preheater#preheaterFrostProtect    | Switch                   | Frost Protection                  | State of the frost protection                                                               | yes      | true      |
| preheater#preheaterHeating         | Switch                   | Preheater                         | State of the preheater                                                                      | yes      | true      |
| preheater#preheaterFrostTime       | Number                   | Preheater Frost Time              | Frost minutes                                                                               | yes      | true      |
| preheater#preheaterSafety          | Number                   | Preheater Frost Safety            | Frost safety setting                                                                        | yes      | true      |
| ewt#ewtTemperatureLow              | Number:Temperature       | EWT Temperature (low)             | Lower temperature of the geothermal heat exchanger                                          | yes      | true      |
| ewt#ewtTemperatureHigh             | Number:Temperature       | EWT Temperature (high)            | Upper temperature of the geothermal heat exchanger                                          | yes      | true      |
| ewt#ewtSpeed                       | Number                   | EWT Speed Up (%)                  | Speed up of the geothermal heat exchanger                                                   | yes      | true      |
| heater#heaterPower                 | Number                   | Heater Power                      | Heater power value                                                                          | yes      | true      |
| heater#heaterPowerI                | Number                   | Heater Power I-parameter          | Heater power I-parameter value                                                              | yes      | true      |
| heater#heaterTargetTemperature     | Number:Temperature       | Heater Target Temperature         | Target temperature of the heater                                                            | yes      | true      |
| cookerhood#cookerhoodSpeed         | Number                   | Cookerhood Speed Up (%)           | Speed up of the cookerhood                                                                  | yes      | true      |
| enthalpy#enthalpyTemperature       | Number:Temperature       | Enthalpy Sensor Temperature       | Temperature of the enthalpy sensor                                                          | yes      | true      |
| enthalpy#enthalpyHumidity          | Number                   | Enthalpy Sensor Humidity          | Humidity of the enthalpy sensor                                                             | yes      | true      |
| enthalpy#enthalpyLevel             | Number                   | Enthalpy Sensor Level             | Level of the enthalpy sensor                                                                | yes      | true      |
| enthalpy#enthalpyTime              | Number                   | Enthalpy Sensor Timer             | Timer state of the enthalpy sensor                                                          | yes      | true      |
| options#isPreheater                | Switch                   | Preheater                         | Preheater option installed                                                                  | yes      | false     |
| options#isBypass                   | Switch                   | Bypass                            | Bypass option installed                                                                     | yes      | false     |
| options#recuType                   | Number                   | Comfoair Type                     | Type of the ComfoAir (1 = left / 2 = right)                                                 | yes      | false     |
| options#recuSize                   | Number                   | Comfoair Size                     | Size of the ComfoAir (1 = big / 2 = small)                                                  | yes      | false     |
| options#isChimney                  | Switch                   | Chimney                           | Chimney option installed                                                                    | yes      | false     |
| options#isCookerhood               | Switch                   | Cookerhood                        | Cookerhood option installed                                                                 | yes      | false     |
| options#isHeater                   | Switch                   | Heater                            | Heater option installed                                                                     | yes      | false     |
| options#isEnthalpy                 | Switch                   | Enthalpy                          | Enthalpy option installed                                                                   | yes      | false     |
| options#isEWT                      | Switch                   | EWT                               | EWT option installed                                                                        | yes      | false     |
| menuP1#menu20Mode                  | Switch                   | Menu 20 Mode (P10)                | State of menu 20 mode (P10)                                                                 | yes      | true      |
| menuP1#menu21Mode                  | Switch                   | Menu 21 Mode (P11)                | State of menu 21 mode (P11)                                                                 | yes      | true      |
| menuP1#menu22Mode                  | Switch                   | Menu 22 Mode (P12)                | State of menu 22 mode (P12)                                                                 | yes      | true      |
| menuP1#menu23Mode                  | Switch                   | Menu 23 Mode (P13)                | State of menu 23 mode (P13)                                                                 | yes      | true      |
| menuP1#menu24Mode                  | Switch                   | Menu 24 Mode (P14)                | State of menu 24 mode (P14)                                                                 | yes      | true      |
| menuP1#menu25Mode                  | Switch                   | Menu 25 Mode (P15)                | State of menu 25 mode (P15)                                                                 | yes      | true      |
| menuP1#menu26Mode                  | Switch                   | Menu 26 Mode (P16)                | State of menu 26 mode (P16)                                                                 | yes      | true      |
| menuP1#menu27Mode                  | Switch                   | Menu 27 Mode (P17)                | State of menu 27 mode (P17)                                                                 | yes      | true      |
| menuP1#menu28Mode                  | Switch                   | Menu 28 Mode (P18)                | State of menu 28 mode (P18)                                                                 | yes      | true      |
| menuP1#menu29Mode                  | Switch                   | Menu 29 Mode (P19)                | State of menu 29 mode (P19)                                                                 | yes      | true      |
| menuP2#bathroomStartDelay          | Number                   | Bathroom Switch Start Delay (P21) | Start delay for bathroom switch (min)                                                       | yes      | false     |
| menuP2#bathroomEndDelay            | Number                   | Bathroom Switch End Delay (P22)   | End delay for bathroom switch (min)                                                         | yes      | false     |
| menuP2#L1EndDelay                  | Number                   | L1 Switch End Delay (P23)         | End delay for L1 switch (min)                                                               | yes      | false     |
| menuP2#pulseVentilation            | Number                   | Pulse Ventilation Period (P27)    | Period for pulse ventilation (min)                                                          | yes      | false     |
| menuP2#filterWeeks                 | Number                   | Filter Period (P24)               | Usage period until filter pollution message (weeks)                                         | yes      | false     |
| menuP2#RFShortDelay                | Number                   | RF Short Delay (P25)              | End delay (RF short actuation) for ventilation level 3 (min)                                | yes      | false     |
| menuP2#RFLongDelay                 | Number                   | RF Long Delay (P26)               | End delay (RF long actuation) for ventilation level 3 (min)                                 | yes      | false     |
| menuP2#cookerhoodDelay             | Number                   | Cookerhood Delay (P20)            | End delay for cooker hood control (min)                                                     | yes      | false     |
| menuP9#chimneyState                | Switch                   | Chimney Control State             | State of the chimney control                                                                | yes      | true      |
| menuP9#bypassState                 | Switch                   | Bypass State                      | State of the bypass (ON = open / OFF = closed)                                              | yes      | true      |
| menuP9#ewtState                    | Switch                   | EWT State                         | State of the EWT valve (ON = open / OFF = closed)                                           | yes      | true      |
| menuP9#heaterState                 | Switch                   | Heater State                      | State of the heater                                                                         | yes      | true      |
| menuP9#vControlState               | Switch                   | 0-10V Control State               | State of the 0-10V control                                                                  | yes      | true      |
| menuP9#frostState                  | Switch                   | Antifrost State                   | State of the antifrost control                                                              | yes      | true      |
| menuP9#cookerhoodState             | Switch                   | Cookerhood State                  | State of the cookerhood control                                                             | yes      | true      |
| menuP9#enthalpyState               | Switch                   | Enthalpy State                    | State of the enthalpy module                                                                | yes      | true      |
| inputs#isL1Switch                  | Switch                   | L1 Switch                         | Availability of L1 step switch                                                              | yes      | true      |
| inputs#isL2Switch                  | Switch                   | L2 Switch                         | Availability of L2 step switch                                                              | yes      | true      |
| inputs#isBathroomSwitch            | Switch                   | Bathroom Switch                   | Availability of bathroom switch                                                             | yes      | true      |
| inputs#isCookerhoodSwitch          | Switch                   | Cookerhood Switch                 | Availability of cookerhood switch                                                           | yes      | true      |
| inputs#isExternalFilter            | Switch                   | External Filter                   | Availability of external filter                                                             | yes      | true      |
| inputs#isWTW                       | Switch                   | Heat Recovery                     | Availability of heat recovery (WTW)                                                         | yes      | true      |
| inputs#isBathroom2Switch           | Switch                   | Bathroom Switch 2                 | Availability of bathroom switch 2 (luxe)                                                    | yes      | true      |
| analog#isAnalog1                   | Switch                   | Analog Input 1 Availability       | Availability of analog input 1                                                              | yes      | false     |
| analog#isAnalog2                   | Switch                   | Analog Input 2 Availability       | Availability of analog input 2                                                              | yes      | false     |
| analog#isAnalog3                   | Switch                   | Analog Input 3 Availability       | Availability of analog input 3                                                              | yes      | false     |
| analog#isAnalog4                   | Switch                   | Analog Input 4 Availability       | Availability of analog input 4                                                              | yes      | false     |
| analog#isRF                        | Switch                   | RF Input Availability             | Availability of RF input                                                                    | yes      | false     |
| analog#analog1Mode                 | Switch                   | Analog Input 1 State              | State of analog input 1                                                                     | yes      | false     |
| analog#analog2Mode                 | Switch                   | Analog Input 2 State              | State of analog input 2                                                                     | yes      | false     |
| analog#analog3Mode                 | Switch                   | Analog Input 3 State              | State of analog input 3                                                                     | yes      | false     |
| analog#analog4Mode                 | Switch                   | Analog Input 4 State              | State of analog input 1                                                                     | yes      | false     |
| analog#RFMode                      | Switch                   | RF Input State                    | State of RF input                                                                           | yes      | false     |
| analog#analog1Negative             | Switch                   | Analog Input 1 Postive/Negative   | Postive/Negative state of analog input 1                                                    | yes      | false     |
| analog#analog2Negative             | Switch                   | Analog Input 2 Postive/Negative   | Postive/Negative state of analog input 2                                                    | yes      | false     |
| analog#analog3Negative             | Switch                   | Analog Input 3 Postive/Negative   | Postive/Negative state of analog input 3                                                    | yes      | false     |
| analog#analog4Negative             | Switch                   | Analog Input 4 Postive/Negative   | Postive/Negative state of analog input 1                                                    | yes      | false     |
| analog#RFNegative                  | Switch                   | RF Input Postive/Negative         | Postive/Negative state of RF input                                                          | yes      | false     |
| analog#analog1Volt                 | Number:ElectricPotential | Analog Input 1 Voltage Level      | Voltage level of analog input 1                                                             | yes      | true      |
| analog#analog1Min                  | Number                   | Analog Input 1 Min                | Minimum setting for analog input 1                                                          | yes      | false     |
| analog#analog1Max                  | Number                   | Analog Input 1 Max                | Maximum setting for analog input 1                                                          | yes      | false     |
| analog#analog1Value                | Number                   | Analog Input 1 Target             | Target setting for analog input 1                                                           | yes      | false     |
| analog#analog2Volt                 | Number:ElectricPotential | Analog Input 2 Voltage Level      | Voltage level of analog input 2                                                             | yes      | true      |
| analog#analog2Min                  | Number                   | Analog Input 2 Min                | Minimum setting for analog input 2                                                          | yes      | false     |
| analog#analog2Max                  | Number                   | Analog Input 2 Max                | Maximum setting for analog input 2                                                          | yes      | false     |
| analog#analog2Value                | Number                   | Analog Input 2 Target             | Target setting for analog input 2                                                           | yes      | false     |
| analog#analog3Volt                 | Number:ElectricPotential | Analog Input 3 Voltage Level      | Voltage level of analog input 3                                                             | yes      | true      |
| analog#analog3Min                  | Number                   | Analog Input 3 Min                | Minimum setting for analog input 3                                                          | yes      | false     |
| analog#analog3Max                  | Number                   | Analog Input 3 Max                | Maximum setting for analog input 3                                                          | yes      | false     |
| analog#analog3Value                | Number                   | Analog Input 3 Target             | Target setting for analog input 3                                                           | yes      | false     |
| analog#analog4Volt                 | Number:ElectricPotential | Analog Input 4 Voltage Level      | Voltage level of analog input 4                                                             | yes      | true      |
| analog#analog4Min                  | Number                   | Analog Input 4 Min                | Minimum setting for analog input 4                                                          | yes      | false     |
| analog#analog4Max                  | Number                   | Analog Input 4 Max                | Maximum setting for analog input 4                                                          | yes      | false     |
| analog#analog4Value                | Number                   | Analog Input 4 Target             | Target setting for analog input 4                                                           | yes      | false     |
| analog#RFMin                       | Number                   | RF Input Min                      | Minimum setting for RF input                                                                | yes      | false     |
| analog#RFMax                       | Number                   | RF Input Max                      | Maximum setting for RF input                                                                | yes      | false     |
| analog#RFValue                     | Number                   | RF Input Target                   | Target setting for RF input                                                                 | yes      | false     |
| error#errorACurrent                | String                   | Error A Current                   | Current error A                                                                             | no       | true      |
| error#errorECurrent                | String                   | Error E Current                   | Current error E                                                                             | no       | true      |
| error#errorALast                   | String                   | Error A Last                      | Last error A                                                                                | no       | true      |
| error#errorELast                   | String                   | Error E Last                      | Last error E                                                                                | no       | true      |
| error#errorAPrelast                | String                   | Error A Prelast                   | Prelast error A                                                                             | no       | true      |
| error#errorEPrelast                | String                   | Error E Prelast                   | Prelast error E                                                                             | no       | true      |
| error#errorAPrePrelast             | String                   | Error A Pre-Prelast               | Pre-Prelast error A                                                                         | no       | true      |
| error#errorEPrePrelast             | String                   | Error E Pre-Prelast               | Pre-Prelast error E                                                                         | no       | true      |
| error#errorEACurrent               | String                   | Error EA Current                  | Current error EA                                                                            | no       | true      |
| error#errorEALast                  | String                   | Error EA Last                     | Last error EA                                                                               | no       | true      |
| error#errorEAPrelast               | String                   | Error EA Prelast                  | Prelast error EA                                                                            | no       | true      |
| error#errorEAPrePrelast            | String                   | Error EA Pre-Prelast              | Pre-Prelast error EA                                                                        | no       | true      |
| error#errorAHighCurrent            | String                   | Error A (high) Current            | Current error A (high)                                                                      | no       | true      |
| error#errorAHighLast               | String                   | Error A (high) Last               | Last error A (high)                                                                         | no       | true      |
| error#errorAHighPrelast            | String                   | Error A (high) Prelast            | Prelast error A (high)                                                                      | no       | true      |
| error#errorAHighPrePrelast         | String                   | Error A (high) Pre-Prelast        | Pre-Prelast error A (high)                                                                  | no       | true      |
| software#softwareMainVersion       | Number                   | Software Main Version             | Main version of the device software                                                         | yes      | true      |
| software#softwareMinorVersion      | Number                   | Software Minor Version            | Minor version of the device software                                                        | yes      | true      |
| software#softwareBetaVersion       | Number                   | Software Beta Version             | Beta version of the device software                                                         | yes      | true      |

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

`comfoair.rules` file:

```
import java.lang.Math
import java.util.Date
import java.util.List
import java.util.ArrayList

// variable for auto_mode state
var Boolean isAutoMode = false

// based capacity for absent level
// absent level is used for supply/exhaust mode
var Number comfoairFanIn0BaseValue = 15
var Number comfoairFanOut0BaseValue = 15
var Number comfoairFanLevelLastValue
var Number comfoairFanIn0LastValue
var Number comfoairFanOut0LastValue


rule "setAutoModeForStart"
when
	System started
then
	if( comfoairControl.state == ON ) {
		comfoairMode.postUpdate( 1 )
		isAutoMode = true
		comfoairFanMode.postUpdate( 0 )
	}
end


rule "setControlToCCEaseWhenSystemClose"
when
	System shuts down
then
	comfoairControl.sendCommand( OFF )
end


rule "messageWhenFrozen"
when
	Item comfoairControl changed to ON
	or
	Item comfoairIncomingFan changed
	or
	Item comfoairOutgoingFan changed
	or
	Item comfoairOutdoorIncomingTemperature changed
then
	if( comfoairControl.state == ON ) {
		var String msg = ""

		if( comfoairIncomingFan.state instanceof DecimalType && comfoairOutgoingFan.state instanceof DecimalType && comfoairOutdoorIncomingTemperature.state instanceof QuantityType<Temperature> ) {

			if( comfoairOutdoorIncomingTemperature.state < "0|°C" && ( comfoairIncomingFan.state == 0 || comfoairOutgoingFan.state == 0 ) ) {
				msg = "FROZEN"
			} else msg = "OK"

		} else msg = "communication failed"

		comfoairFrozenError.postUpdate( msg )
	}
end

rule "showMessage"
when
	Item comfoairControl changed to ON
	or
	Item comfoairError changed
	or
	Item comfoairInletError changed
	or
	Item comfoairFreezeError changed
then
	if( comfoairControl.state == ON ) {
		var String msg = "" + callScript( "comfoair_errorMsg" )
		var String msgToSend = "" + callScript( "comfoair_errorMsgToSend" )

		comfoairError_Message.postUpdate( msg )

		if( comfoairError.state != "Ok" || comfoairInletError.state != "OK" || comfoairFrozenError.state != "OK" ) {
			sendMail( "xyz@gmail.com", "ComfoAir message", msgToSend )
		}
	}
end


rule "showFilterTime"
when
	Item comfoairFilterRuntime changed
	or
	Item comfoairFilterPeriod changed
	or
	Item comfoairControl changed to ON
then
	if( comfoairFilterRuntime.state instanceof DecimalType && comfoairFilterPeriod.state instanceof DecimalType ) {
		var Number filterPeriodWeeks = comfoairFilterPeriod.state
		var Number filterPeriodHours = filterPeriodWeeks * 7 * 24

		if( comfoairFilterRuntime.state instanceof DecimalType ){
			var Number passedHours = comfoairFilterRuntime.state as DecimalType
			var Boolean isTimeExceeded

			if( filterPeriodHours - passedHours < 0) {
				isTimeExceeded = true
			} else isTimeExceeded = false

			var Number passedWeeks = Math::floor( ( passedHours / 168 ).doubleValue )
			var Number passedDays = Math::floor( ( ( passedHours - (passedWeeks * 168 ) ) / 24).doubleValue )
			var Number remainedHours = Math::abs( ( filterPeriodHours - passedHours ).intValue )
			var Number remainedWeeks = Math::floor( ( remainedHours / 168 ).doubleValue )
			var Number remainedDays = Math::floor( ( ( remainedHours - ( remainedWeeks * 168 ) ) / 24).doubleValue )

			var String msg = "Passed: "

			if( passedWeeks > 0 ) {

				if( passedWeeks == 1 ) msg = msg + "1 week"
				else msg = msg + passedWeeks.intValue + " weeks"

				if( passedDays > 0) msg = msg + " "
				else msg = msg + ","
			}
			if( passedDays > 0 ) {
				if( passedDays == 1 ) msg = msg + "1 day,"
				else msg = msg + passedDays.intValue + " days,"
			}
			if( passedWeeks == 0 && passedDays == 0 ) msg = msg + "0 days, "

			if( isTimeExceeded ) msg = msg + " Exceeded: "
			else msg = msg + " Remained: "

			if( remainedWeeks != 0 ) {

				if( remainedWeeks == 1 ) msg = msg + "1 week"
				else msg = msg + remainedWeeks.intValue + " weeks"

				if( remainedDays > 0 ) msg = msg + " "
			}
			if( remainedDays != 0 ) {
				if( remainedDays == 1 ) msg = msg + "1 day"
				else msg = msg + remainedDays.intValue + " days"
			}

			if( remainedWeeks == 0 && remainedDays == 0 ) msg = msg + "0 days"

			comfoairFilterRuntime_Message.postUpdate( msg )

		} else {
			comfoairFilterRuntime_Message.postUpdate( "communication failed" )
		}
	}
end


rule "errorReset"
when
	Item comfoairReset changed
then
	var String error

	if( comfoairControl.state == ON ) {

		if( comfoairReset.state == "0" ) {
			comfoairFilterReset.sendCommand( ON )
			logInfo( "ComfoAir", "Filter was reset !" )

		} else if( comfoairReset.state == "1" ) {
			comfoairErrorReset.sendCommand( ON )
			logInfo( "ComfoAir", "Error was reset !" )
		}

		createTimer(now.plusSeconds( 5 ),  [ |
			error = "" + callScript( "comfoair_errorMsg" )
			comfoairError_Message.postUpdate( error )
		])
	} else {
		if( comfoairReset.state == "0" || comfoairReset.state == "1" ) {
			comfoairError_Message.postUpdate( "PC control is not active" )

			createTimer(now.plusSeconds( 10 ),  [ |
				error = "" + callScript( "comfoair_errorMsg" )
				comfoairError_Message.postUpdate( error )
			])
		}
	}
end


rule "calculateEfficiency"
when
	Item comfoairOutdoorIncomingTemperature changed
	or
	Item comfoairIndoorOutgoingTemperature changed
	or
	Item comfoairIndoorIncomingTemperature changed
	or
	Item comfoairControl changed to ON
then
	var Number efficiency

	if( comfoairOutdoorIncomingTemperature.state instanceof QuantityType<Temperature> && comfoairIndoorOutgoingTemperature.state instanceof QuantityType<Temperature> && comfoairIndoorIncomingTemperature.state instanceof QuantityType<Temperature> && comfoairBypassMode.state == OFF ) {
		var Number tempOutIn = comfoairOutdoorIncomingTemperature.state as DecimalType
		var Number tempInOut = comfoairIndoorOutgoingTemperature.state as DecimalType
		var Number tempInIn = comfoairIndoorIncomingTemperature.state as DecimalType

		if( tempInOut != tempOutIn ) {
			efficiency = ( tempInIn - tempOutIn ) / ( tempInOut - tempOutIn ) * 100
			efficiency = Math::round( efficiency.doubleValue );
		} else efficiency = null
	} else efficiency = null

	comfoairEfficiency.postUpdate( efficiency )
end


rule "changeSupply-Exhaust"
when
	Item comfoairFanMode_Message changed
then
	var Number newFanMode = comfoairFanMode_Message.state as DecimalType

	if( comfoairControl.state == ON ) {

		if( comfoairChimney.state == OFF ) {

			if( comfoairFanMode_Message.state != comfoairFanMode.state ) {
				comfoairFanMode.sendCommand( newFanMode )
			}

		} else if( comfoairFanMode_Message.state != comfoairFanMode.state ) {
			comfoairError_Message.postUpdate( "Fire programme is on" )

			createTimer(now.plusMillis( 200 ),  [ |
				comfoairFanMode_Message.postUpdate( comfoairFanMode.state )
			])

			createTimer(now.plusSeconds( 10 ),  [ |
				var String error = "" + callScript( "comfoair_errorMsg" )
				comfoairError_Message.postUpdate( error )
			])
		}
	} else if( comfoairFanMode_Message.state != comfoairFanMode.state ) {
		comfoairError_Message.postUpdate( "PC control is not active" )

		createTimer(now.plusMillis( 200 ),  [ |
			comfoairFanMode_Message.postUpdate( comfoairFanMode.state )
		])

		createTimer(now.plusSeconds( 10 ),  [ |
			var String error = "" + callScript( "comfoair_errorMsg" )
			comfoairError_Message.postUpdate( error )
		])
	}
end


rule "changeSupply-ExhaustSwitch"
when
	Item comfoairFanMode changed
then
	if( comfoairFanMode.state instanceof DecimalType ) {
		comfoairFanMode_Message.postUpdate( comfoairFanMode.state )
	}
end


rule "changeSupply-ExhaustMode"
when
	Item comfoairFanMode changed
then
	if( comfoairControl.state instanceof OnOffType && comfoairChimney.state instanceof OnOffType ) {

		if( comfoairFanMode.state instanceof Number ) {

			if( comfoairFanLevel.state > 1 ) {
				comfoairFanLevelLastValue = comfoairFanLevel.state

				Thread::sleep( 100 )
				comfoairFanIn0LastValue = comfoairIncomingFan.state
				comfoairFanOut0LastValue = comfoairOutgoingFan.state
			}

			// Supply + Exhaust
			if( comfoairFanMode.state == 0 ) {

				if( comfoairIncomingFan.state == 0 || comfoairOutgoingFan.state == 0 ) {
					comfoairFanIn0.sendCommand( comfoairFanIn0BaseValue )

					createTimer(now.plusSeconds( 1 ),  [ |
						comfoairFanOut0.sendCommand( comfoairFanOut0BaseValue )
					])

					if( comfoairFanLevel.state < 2 ) {
						comfoairFanLevel.sendCommand( comfoairFanLevelLastValue.toString )
					}
				}
			} else {
				comfoairFanLevel.sendCommand( "1" )

				// Supply
				if( comfoairFanMode.state == 1 ) {

					if( comfoairIncomingFan.state == 0 ) {
						comfoairFanIn0.sendCommand( comfoairFanIn0LastValue )
					} else {
						comfoairFanIn0.sendCommand( comfoairIncomingFan.state )
					}

					createTimer( now.plusSeconds( 1 ),  [ | comfoairFanOut0.sendCommand( 0 ) ])

					comfoairMode.postUpdate( 0 )
					isAutoMode = false
				}

				// Exhaust
				if( comfoairFanMode.state == 2 ) {

					comfoairFanIn0.sendCommand( 0 )

					createTimer(now.plusSeconds( 1 ),  [ |
						if( comfoairOutgoingFan.state == 0 ) {
							comfoairFanOut0.sendCommand( comfoairFanOut0LastValue )
						} else {
							comfoairFanOut0.sendCommand( comfoairOutgoingFan.state )
						}
					])

					comfoairMode.postUpdate( 0 )
					isAutoMode = false
				}
			}
		}
	}
end


rule "changeAuto-Manual"
when
	Item comfoairMode changed
then
	if( comfoairControl.state == 0 ) {
		comfoairError_Message.postUpdate( "PC control is not active" )

		createTimer(now.plusSeconds( 10 ),  [ |
			var String error = "" + callScript( "comfoair_errorMsg" )
			comfoairError_Message.postUpdate( error )
		])
	}
end


rule "changeVentilationLevel"
when
	Item comfoairFanLevel_Message changed
then
	var Number newLevel = comfoairFanLevel_Message.state as DecimalType

	if( comfoairControl.state == ON && comfoairFanLevel_Message.state != comfoairFanLevel.state ) {
		comfoairFanLevel.sendCommand( newLevel )

		comfoairMode.postUpdate( 0 )
		isAutoMode = false

		if( newLevel > 1 ) {
			comfoairFanMode.postUpdate( 0 )
		}
	} else {

		if( comfoairFanLevel_Message.state != comfoairFanLevel.state ) {
			comfoairError_Message.postUpdate( "PC control is not active" )

			createTimer(now.plusMillis( 200 ),  [ |
				comfoairFanLevel_Message.postUpdate( comfoairFanLevel.state )
			])

			createTimer(now.plusSeconds( 10 ),  [ |
				var String error = "" + callScript( "comfoair_errorMsg" )
				comfoairError_Message.postUpdate( error )
			])
		}
	}
end


rule "changeVentilationLevelSwitch"
when
	Item comfoairFanLevel changed
then
	if( comfoairFanLevel.state instanceof DecimalType ) {
		comfoairFanLevel_Message.postUpdate( comfoairFanLevel.state )
	}
end


rule "changeComfortTemperature"
when
	Item comfoairTargetTemperature_Message changed
then
	var Number newTemp = comfoairTargetTemperature_Message.state as DecimalType

	if( comfoairControl.state == ON ) {

		if( comfoairTargetTemperature_Message.state != comfoairTargetTemperature.state ) {
			comfoairTargetTemperature.sendCommand( newTemp )
		}
	} else {

		if( comfoairTargetTemperature_Message.state != comfoairTargetTemperature.state ) {
			comfoairError_Message.postUpdate( "PC control is not active" )

			createTimer( now.plusMillis( 200 ),  [ |
				comfoairTargetTemperature_Message.postUpdate( comfoairTargetTemperature.state )
			])

			createTimer( now.plusSeconds( 10 ),  [ |
				var String error = "" + callScript( "comfoair_errorMsg" )
				comfoairError_Message.postUpdate( error )
			])
	   }
	}
end


rule "changeComfortTemperatureSetpoint"
when
	Item comfoairTargetTemperature changed
then
	if( comfoairTargetTemperature.state instanceof DecimalType ) {
		comfoairTargetTemperature_Message.postUpdate( comfoairTargetTemperature.state )
	}
end


rule "AutoProgram"
when
	Item comfoairMode changed
	or
	Time cron "0 0/1 * * * ?"
then
	if( comfoairControl.state instanceof OnOffType && comfoairMode.state instanceof DecimalType && comfoairFanLevel.state instanceof DecimalType ) {
		var Number day    = now.getDayOfWeek
		var Number hour   = now.getHourOfDay
		var Number minute = now.getMinuteOfHour

		if( comfoairControl.state == ON && comfoairMode.state == 1 ) {
			var Number currentLevel = comfoairFanLevel.state as DecimalType
			var Number newLevel

			// Summer energy time - from 1.04 to 30.09
			if( EnergySummerTime.state == ON ) {

				// First set level 1 and 2

				// All days a week
				// if you want Monday to Friday remove day==6 and day==7
				if( day == 1 || day == 2 || day == 3 || day == 4 || day == 5 || day == 6 || day == 7 ) {

					// Level 2 : 5:00 - 7:00, 16:00 - 23:00, level 1 : in the other time
					if( ( hour >= 5 && hour < 7 )
						||
						( hour >= 16 && hour < 23 )
					) {
						newLevel = 3
					} else newLevel = 2

				// Saterdey and Sunday
				} else if( day == 6 || day == 7 ) {

					if( ( hour >= 12 && hour < 14 )
					) {
						newLevel = 3
					} else newLevel = 2
				}

				// Now set level 3

				// All days a week
				if( day == 1 || day == 2 || day == 3 || day == 4 || day == 5 || day == 6 || day == 7 ) {

					// Level 3 : 13:00 - 13:30
					if( ( hour == 13 && minute < 30 )
					) {
						newLevel = 4
					}
				}

			// Winter energy time - from 1.10 to 31.03
			} else {

				// All days a week
				// podwyzszenie 0:00-2:00, 3:00-5:00, 7:00-8:00, 10:00-11:00, 13:00-15:00, 16:30- 18:30, 20:00-21:00, 22:00-23:00
				if( day == 1 || day == 2 || day == 3 || day == 4 || day == 5 || day == 6 || day == 7 ) {

					// Level 2 : 6:00 - 8:00, 16:00 - 21:00, level 1 : in the other time
					if( ( hour >= 6 && hour < 8 )
						||
						( hour >= 16 && hour < 21 )
					) {
						newLevel = 3
					} else newLevel = 2

				// Saterdey and Sunday
				} else if( day == 6 || day == 7 ) {

					if( ( hour >= 12 && hour < 14 )
					) {
						newLevel = 3
					} else newLevel = 2
				}

				// Now set level 3

				// All days a week
				if( day == 1 || day == 2 || day == 3 || day == 4 || day == 5 || day == 6 || day == 7 ) {

					// Level 3 : 13:00 - 13:30
					if( ( hour == 13 && minute < 30 )
					) {
						newLevel = 4
					}
				}
			}

			if( newLevel != currentLevel ) {
				comfoairFanLevel.sendCommand( newLevel )
				isAutoMode = true
				comfoairFanMode.postUpdate( 0 )
			}

			logInfo( "comfoair", "ComfoAir - AUTO Mode" )

		} else logInfo( "comfoair", "ComfoAir - MANUAL Mode" )
	}
end
```

## Control Protocol Reference

For reference the protocol description used can be found here (german version only):
[Protokollbeschreibung Zehnder ComfoAir](http://www.see-solutions.de/sonstiges/Protokollbeschreibung_ComfoAir.pdf)
