# Guntamatic Binding

The Guntamatic Binding can be used to monitor and control Guntamatic Heating Systems (https://www.guntamatic.com/en/).

## Supported Things

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System running Firmware 3.2d.
It should work for all other Guntamatic Heating Systems as well, that support the same web interface.

## Things

Guntamatic Heating Systems supported as Thing Types:

| Name          | Thing Type ID | Status                            |
|---------------|---------------|-----------------------------------|
| Biostar       | `biostar`     | tested via 15kW, firmware 3.2d    |
| Powerchip     | `powerchip`   | untested                          |
| Powercorn     | `powercorn`   | untested                          |
| Biocom        | `biocom`      | untested                          |
| Pro           | `pro`         | untested                          |
| Therm         | `therm`       | untested                          |

### Thing Configuration

| Parameter     | Description                                                               | Default       |
|---------------|---------------------------------------------------------------------------|---------------|
| Hostname      | Hostname or IP address of the Guntamatic Heating System                   |               |
| Key           | Optional, but required to read protected parameters and to control the Guntamatic Heating System. The key needs to be reqested from Guntamatic support, e.g. via https://www.guntamatic.com/en/contact/                                    |               |
| Refresh Interval    | Interval the Guntamatic Heating System is polled in seconds         | 60            |
| Encoding      | Code page used by the Guntamatic Heating System                           | windows-1252  |

### Properties

| Property          | Description                                                   |
|-------------------|---------------------------------------------------------------|
| extraWwHeat       | Parameter used by `controlExtraWwHeat` channels               |
| boilerApproval    | Parameter used by `controlBoilerApproval` channel             |
| heatCircProgram   | Parameter used by `controlHeatCircProgram` channels           |
| program           | Parameter used by `controlProgram` channel                    |
| wwHeat            | Parameter used by `controlWwHeat` channels                    |

## Channels

The Binding dynamically generates Channels, derived from the data provided from the actual Guntamatic Heating System.

Example list of Channels using a Guntamatic Biostar 15kW Pellets Heating System running firmware 3.2d and Guntamatic System Language configured to English:

|	Channel             |	Description                                             | Type	|	Unit	|	Security Access Level	| ReadOnly | Advanced |
|-----------------------|-----------------------------------------------------------|------------|-----------|----------------------|----------|----------|
|	controlBoilerApproval	|	Set Boiler Approval (AUTO, OFF, ON)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlProgram	|	Set Program (OFF, NORMAL, WARMWATER, MANUAL)	|	String	|		|	🔐 W1	|	R/W	|	false	|
|	controlHeatCircProgram0	|	Set Program of Heat Circle 0 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram1	|	Set Program of Heat Circle 1 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram2	|	Set Program of Heat Circle 2 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram3	|	Set Program of Heat Circle 3 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram4	|	Set Program of Heat Circle 4 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram5	|	Set Program of Heat Circle 5 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram6	|	Set Program of Heat Circle 6 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram7	|	Set Program of Heat Circle 7 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlHeatCircProgram8	|	Set Program of Heat Circle 8 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlWwHeat0	|	Trigger Warm Wather Circle 0 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlWwHeat1	|	Trigger Warm Wather Circle 1 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlWwHeat2	|	Trigger Warm Wather Circle 2 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlExtraWwHeat0	|	Trigger Extra Warm Wather Circle 0 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlExtraWwHeat1	|	Trigger Extra Warm Wather Circle 1 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	controlExtraWwHeat2	|	Trigger Extra Warm Wather Circle 2 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	running	|	Running	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	outsideTemp	|	Outside Temp.	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	blrTargetTemp	|	Boiler Target Temp	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	boilerTemperature	|	Boiler Temperature	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flueGasUtilisation	|	Flue gas utilisation	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	output	|	Output	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	returnTemp	|	Return temp	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	co2Target	|	CO2 Target	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	co2Content	|	C02 Content	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	returnTempTarget	|	return temp target	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	statusCode	|	status code	|	Number	|		|	🔐 W1	|	R/O	|	false	|
|	efficiency	|	Efficiency	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	extractorSystem	|	Extractor System	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	feedTurbine	|	Feed Turbine	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	dischargeMotor	|	discharge motor	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	g1Target	|	G1 Target	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	bufferTop	|	Buffer Top	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bufferMid	|	Buffer Mid	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bufferBtm	|	Buffer Btm	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	pumpHp0	|	Pump HP0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw0	|	DHW 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bDhw0	|	B DHW 0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw1	|	DHW 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bDhw1	|	B DHW 1	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw2	|	DHW 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bDhw2	|	B DHW 2	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc0	|	Room Temp:HC 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatCirc0	|	Heat Circ. 0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc1	|	Room Temp:HC 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowTarget1	|	Flow Target 1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowIs1	|	Flow is 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer1	|	Mixer 1	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatCirc1	|	Heat Circ. 1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	roomTempHc2	|	Room Temp:HC 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowTarget2	|	Flow Target 2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowIs2	|	Flow is 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer2	|	Mixer 2	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatCirc2	|	Heat Circ. 2	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc3	|	Room Temp:HC 3	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatCirc3	|	Heat Circ. 3	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc4	|	Room Temp:HC 4	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowTarget4	|	Flow Target 4	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowIs4	|	Flow is 4	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer4	|	Mixer 4	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatCirc4	|	Heat Circ. 4	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc5	|	Room Temp:HC 5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowTarget5	|	Flow Target 5	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowIs5	|	Flow is 5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer5	|	Mixer 5	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatCirc5	|	Heat Circ. 5	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc6	|	Room Temp:HC 6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatCirc6	|	Heat Circ. 6	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc7	|	Room Temp:HC 7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowTarget7	|	Flow Target 7	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowIs7	|	Flow is 7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer7	|	Mixer 7	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatCirc7	|	Heat Circ. 7	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomTempHc8	|	Room Temp:HC 8	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowTarget8	|	Flow Target 8	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowIs8	|	Flow is 8	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer8	|	Mixer 8	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatCirc8	|	Heat Circ. 8	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	fuelLevel	|	Fuel Level	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	stb	|	STB	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	tks	|	TKS	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	boilerApproval	|	Boiler approval	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	programme	|	Programme	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc0	|	Program HC0	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc1	|	Program HC1	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc2	|	Program HC2	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc3	|	Program HC3	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc4	|	Program HC4	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc5	|	Program HC5	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc6	|	Program HC6	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc7	|	Program HC7	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programHc8	|	Program HC8	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	interuption0	|	Interuption 0	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	interuption1	|	Interuption 1	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	serial	|	Serial	|	Number	|		|	🔓 W0	|	R/O	|	false	|
|	version	|	Version	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	runningTime	|	Running Time	|	Number:Time	|	h	|	🔓 W0	|	R/O	|	false	|
|	serviceHrs	|	Service Hrs	|	Number:Time	|	d	|	🔓 W0	|	R/O	|	false	|
|	emptyAshIn	|	Empty ash in	|	Number:Time	|	h	|	🔓 W0	|	R/O	|	false	|
|	flowIs0	|	Flow is 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowIs3	|	Flow is 3	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowIs6	|	Flow is 6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	fuelCounter	|	Fuel counter	|	Number:Volume	|	m³	|	🔐 W1	|	R/O	|	false	|
|	bufferLoad	|	Buffer load.	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	bufferTop0	|	Buffer Top 0	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferBtm0	|	Buffer Btm 0	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferTop1	|	Buffer Top 1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferBtm1	|	Buffer Btm 1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferTop2	|	Buffer Top 2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferBtm2	|	Buffer Btm 2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bExtraWw0	|	B extra-WW. 0	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	bExtraWw1	|	B extra-WW. 1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	bExtraWw2	|	B extra-WW. 2	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliaryPump0	|	Auxiliary pump 0	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliaryPump1	|	Auxiliary pump 1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliaryPump2	|	Auxiliary pump 2	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	boilersConditionNo	|	Boiler´s condition no.	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	bufferT5	|	Buffer T5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bufferT6	|	Buffer T6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bufferT7	|	Buffer T7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraWw0	|	extra-WW. 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraWw1	|	extra-WW. 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraWw2	|	extra-WW. 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	grate	|	grate	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|

**Security Access Levels**

- 🔓 W0 ... Open
- 🔐 W1 ... End Customer Key
- 🔒 W2 ... Service Partner

**Response of Control Channels**

- `{"ack":"confirmation message"}` ... in case of success
- `{"err":"error message"}`        ... in case of error

## Full Example

**Thing File**

```java
Thing   guntamatic:biostar:mybiostar   "Guntamatic Biostar"    [ hostname="192.168.1.100", key="0123456789ABCDEF0123456789ABCDEF0123", refreshInterval=60, encoding="windows-1252" ]
```

**Item File**

```java
String                Biostar_ControlBoilerApproval          "Set Boiler Approval"                 { channel="guntamatic:biostar:mybiostar:controlBoilerApproval" }
String                Biostar_ControlProgram                 "Set Program"                         { channel="guntamatic:biostar:mybiostar:controlProgram" }
String                Biostar_ControlHeatCircProgram0        "Set Program of Heat Circle 0"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram0" }
String                Biostar_ControlHeatCircProgram1        "Set Program of Heat Circle 1"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram1" }
String                Biostar_ControlHeatCircProgram2        "Set Program of Heat Circle 2"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram2" }
String                Biostar_ControlHeatCircProgram3        "Set Program of Heat Circle 3"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram3" }
String                Biostar_ControlHeatCircProgram4        "Set Program of Heat Circle 4"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram4" }
String                Biostar_ControlHeatCircProgram5        "Set Program of Heat Circle 5"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram5" }
String                Biostar_ControlHeatCircProgram6        "Set Program of Heat Circle 6"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram6" }
String                Biostar_ControlHeatCircProgram7        "Set Program of Heat Circle 7"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram7" }
String                Biostar_ControlHeatCircProgram8        "Set Program of Heat Circle 8"        { channel="guntamatic:biostar:mybiostar:controlHeatCircProgram8" }
String                Biostar_ControlWwHeat0                 "Trigger Warm Wather Circle 0"        { channel="guntamatic:biostar:mybiostar:controlWwHeat0" }
String                Biostar_ControlWwHeat1                 "Trigger Warm Wather Circle 1"        { channel="guntamatic:biostar:mybiostar:controlWwHeat1" }
String                Biostar_ControlWwHeat2                 "Trigger Warm Wather Circle 2"        { channel="guntamatic:biostar:mybiostar:controlWwHeat2" }
String                Biostar_ControlExtraWwHeat0            "Trigger Extra Warm Wather Circle 0"  { channel="guntamatic:biostar:mybiostar:controlExtraWwHeat0" }
String                Biostar_ControlExtraWwHeat1            "Trigger Extra Warm Wather Circle 1"  { channel="guntamatic:biostar:mybiostar:controlExtraWwHeat1" }
String                Biostar_ControlExtraWwHeat2            "Trigger Extra Warm Wather Circle 2"  { channel="guntamatic:biostar:mybiostar:controlExtraWwHeat2" }
String                Biostar_Running                        "Running"                             { channel="guntamatic:biostar:mybiostar:running" }
Number:Temperature    Biostar_OutsideTemp                    "OutsideTemp"                         { channel="guntamatic:biostar:mybiostar:outsideTemp" }
Number:Temperature    Biostar_BoilerTargetTemp               "BoilerTargetTemp"                    { channel="guntamatic:biostar:mybiostar:blrTargetTemp" }
Number:Temperature    Biostar_BoilerTemperature              "BoilerTemperature"                   { channel="guntamatic:biostar:mybiostar:boilerTemperature" }
Number:Dimensionless  Biostar_Fluegasutilisation             "Fluegasutilisation"                  { channel="guntamatic:biostar:mybiostar:flueGasUtilisation" }
Number:Dimensionless  Biostar_Output                         "Output"                              { channel="guntamatic:biostar:mybiostar:output" }
Number:Temperature    Biostar_Returntemp                     "Returntemp"                          { channel="guntamatic:biostar:mybiostar:returnTemp" }
Number:Dimensionless  Biostar_CO2Target                      "CO2Target"                           { channel="guntamatic:biostar:mybiostar:co2Target" }
Number:Dimensionless  Biostar_C02Content                     "C02Content"                          { channel="guntamatic:biostar:mybiostar:co2Content" }
Number:Temperature    Biostar_Returntemptarget               "Returntemptarget"                    { channel="guntamatic:biostar:mybiostar:returnTempTarget" }
Number                Biostar_Statuscode                     "Statuscode"                          { channel="guntamatic:biostar:mybiostar:statusCode" }
Number:Dimensionless  Biostar_Efficiency                     "Efficiency"                          { channel="guntamatic:biostar:mybiostar:efficiency" }
Number:Dimensionless  Biostar_ExtractorSystem                "ExtractorSystem"                     { channel="guntamatic:biostar:mybiostar:extractorSystem" }
String                Biostar_FeedTurbine                    "FeedTurbine"                         { channel="guntamatic:biostar:mybiostar:feedTurbine" }
Number:Dimensionless  Biostar_Dischargemotor                 "Dischargemotor"                      { channel="guntamatic:biostar:mybiostar:dischargeMotor" }
Number:Dimensionless  Biostar_G1Target                       "G1Target"                            { channel="guntamatic:biostar:mybiostar:g1Target" }
Number:Temperature    Biostar_BufferTop                      "BufferTop"                           { channel="guntamatic:biostar:mybiostar:bufferTop" }
Number:Temperature    Biostar_BufferMid                      "BufferMid"                           { channel="guntamatic:biostar:mybiostar:bufferMid" }
Number:Temperature    Biostar_BufferBtm                      "BufferBtm"                           { channel="guntamatic:biostar:mybiostar:bufferBtm" }
Switch                Biostar_PumpHP0                        "PumpHP0"                             { channel="guntamatic:biostar:mybiostar:pumpHp0" }
Number:Temperature    Biostar_DHW0                           "DHW0"                                { channel="guntamatic:biostar:mybiostar:dhw0" }
Switch                Biostar_BDHW0                          "BDHW0"                               { channel="guntamatic:biostar:mybiostar:bDhw0" }
Number:Temperature    Biostar_DHW1                           "DHW1"                                { channel="guntamatic:biostar:mybiostar:dhw1" }
Switch                Biostar_BDHW1                          "BDHW1"                               { channel="guntamatic:biostar:mybiostar:bDhw1" }
Number:Temperature    Biostar_DHW2                           "DHW2"                                { channel="guntamatic:biostar:mybiostar:dhw2" }
Switch                Biostar_BDHW2                          "BDHW2"                               { channel="guntamatic:biostar:mybiostar:bDhw2" }
Number:Temperature    Biostar_RoomTempHC0                    "RoomTempHC0"                         { channel="guntamatic:biostar:mybiostar:roomTempHc0" }
Switch                Biostar_HeatCirc0                      "HeatCirc0"                           { channel="guntamatic:biostar:mybiostar:heatCirc0" }
Number:Temperature    Biostar_RoomTempHC1                    "RoomTempHC1"                         { channel="guntamatic:biostar:mybiostar:roomTempHc1" }
Number:Temperature    Biostar_FlowTarget1                    "FlowTarget1"                         { channel="guntamatic:biostar:mybiostar:flowTarget1" }
Number:Temperature    Biostar_Flowis1                        "Flowis1"                             { channel="guntamatic:biostar:mybiostar:flowIs1" }
String                Biostar_Mixer1                         "Mixer1"                              { channel="guntamatic:biostar:mybiostar:mixer1" }
Switch                Biostar_HeatCirc1                      "HeatCirc1"                           { channel="guntamatic:biostar:mybiostar:heatCirc1" }
Number:Temperature    Biostar_RoomTempHC2                    "RoomTempHC2"                         { channel="guntamatic:biostar:mybiostar:roomTempHc2" }
Number:Temperature    Biostar_FlowTarget2                    "FlowTarget2"                         { channel="guntamatic:biostar:mybiostar:flowTarget2" }
Number:Temperature    Biostar_Flowis2                        "Flowis2"                             { channel="guntamatic:biostar:mybiostar:flowIs2" }
String                Biostar_Mixer2                         "Mixer2"                              { channel="guntamatic:biostar:mybiostar:mixer2" }
Switch                Biostar_HeatCirc2                      "HeatCirc2"                           { channel="guntamatic:biostar:mybiostar:heatCirc2" }
Number:Temperature    Biostar_RoomTempHC3                    "RoomTempHC3"                         { channel="guntamatic:biostar:mybiostar:roomTempHc3" }
Switch                Biostar_HeatCirc3                      "HeatCirc3"                           { channel="guntamatic:biostar:mybiostar:heatCirc3" }
Number:Temperature    Biostar_RoomTempHC4                    "RoomTempHC4"                         { channel="guntamatic:biostar:mybiostar:roomTempHc4" }
Number:Temperature    Biostar_FlowTarget4                    "FlowTarget4"                         { channel="guntamatic:biostar:mybiostar:flowTarget4" }
Number:Temperature    Biostar_Flowis4                        "Flowis4"                             { channel="guntamatic:biostar:mybiostar:flowIs4" }
String                Biostar_Mixer4                         "Mixer4"                              { channel="guntamatic:biostar:mybiostar:mixer4" }
Switch                Biostar_HeatCirc4                      "HeatCirc4"                           { channel="guntamatic:biostar:mybiostar:heatCirc4" }
Number:Temperature    Biostar_RoomTempHC5                    "RoomTempHC5"                         { channel="guntamatic:biostar:mybiostar:roomTempHc5" }
Number:Temperature    Biostar_FlowTarget5                    "FlowTarget5"                         { channel="guntamatic:biostar:mybiostar:flowTarget5" }
Number:Temperature    Biostar_Flowis5                        "Flowis5"                             { channel="guntamatic:biostar:mybiostar:flowIs5" }
String                Biostar_Mixer5                         "Mixer5"                              { channel="guntamatic:biostar:mybiostar:mixer5" }
Switch                Biostar_HeatCirc5                      "HeatCirc5"                           { channel="guntamatic:biostar:mybiostar:heatCirc5" }
Number:Temperature    Biostar_RoomTempHC6                    "RoomTempHC6"                         { channel="guntamatic:biostar:mybiostar:roomTempHc6" }
Switch                Biostar_HeatCirc6                      "HeatCirc6"                           { channel="guntamatic:biostar:mybiostar:heatCirc6" }
Number:Temperature    Biostar_RoomTempHC7                    "RoomTempHC7"                         { channel="guntamatic:biostar:mybiostar:roomTempHc7" }
Number:Temperature    Biostar_FlowTarget7                    "FlowTarget7"                         { channel="guntamatic:biostar:mybiostar:flowTarget7" }
Number:Temperature    Biostar_Flowis7                        "Flowis7"                             { channel="guntamatic:biostar:mybiostar:flowIs7" }
String                Biostar_Mixer7                         "Mixer7"                              { channel="guntamatic:biostar:mybiostar:mixer7" }
Switch                Biostar_HeatCirc7                      "HeatCirc7"                           { channel="guntamatic:biostar:mybiostar:heatCirc7" }
Number:Temperature    Biostar_RoomTempHC8                    "RoomTempHC8"                         { channel="guntamatic:biostar:mybiostar:roomTempHc8" }
Number:Temperature    Biostar_FlowTarget8                    "FlowTarget8"                         { channel="guntamatic:biostar:mybiostar:flowTarget8" }
Number:Temperature    Biostar_Flowis8                        "Flowis8"                             { channel="guntamatic:biostar:mybiostar:flowIs8" }
String                Biostar_Mixer8                         "Mixer8"                              { channel="guntamatic:biostar:mybiostar:mixer8" }
Switch                Biostar_HeatCirc8                      "HeatCirc8"                           { channel="guntamatic:biostar:mybiostar:heatCirc8" }
String                Biostar_FuelLevel                      "FuelLevel"                           { channel="guntamatic:biostar:mybiostar:fuelLevel" }
String                Biostar_STB                            "STB"                                 { channel="guntamatic:biostar:mybiostar:stb" }
String                Biostar_TKS                            "TKS"                                 { channel="guntamatic:biostar:mybiostar:tks" }
Switch                Biostar_Boilerapproval                 "Boilerapproval"                      { channel="guntamatic:biostar:mybiostar:boilerApproval" }
String                Biostar_Programme                      "Programme"                           { channel="guntamatic:biostar:mybiostar:programme" }
String                Biostar_ProgramHC0                     "ProgramHC0"                          { channel="guntamatic:biostar:mybiostar:programHc0" }
String                Biostar_ProgramHC1                     "ProgramHC1"                          { channel="guntamatic:biostar:mybiostar:programHc1" }
String                Biostar_ProgramHC2                     "ProgramHC2"                          { channel="guntamatic:biostar:mybiostar:programHc2" }
String                Biostar_ProgramHC3                     "ProgramHC3"                          { channel="guntamatic:biostar:mybiostar:programHc3" }
String                Biostar_ProgramHC4                     "ProgramHC4"                          { channel="guntamatic:biostar:mybiostar:programHc4" }
String                Biostar_ProgramHC5                     "ProgramHC5"                          { channel="guntamatic:biostar:mybiostar:programHc5" }
String                Biostar_ProgramHC6                     "ProgramHC6"                          { channel="guntamatic:biostar:mybiostar:programHc6" }
String                Biostar_ProgramHC7                     "ProgramHC7"                          { channel="guntamatic:biostar:mybiostar:programHc7" }
String                Biostar_ProgramHC8                     "ProgramHC8"                          { channel="guntamatic:biostar:mybiostar:programHc8" }
String                Biostar_Interuption0                   "Interuption0"                        { channel="guntamatic:biostar:mybiostar:interuption0" }
String                Biostar_Interuption1                   "Interuption1"                        { channel="guntamatic:biostar:mybiostar:interuption1" }
Number                Biostar_Serial                         "Serial"                              { channel="guntamatic:biostar:mybiostar:serial" }
String                Biostar_Version                        "Version"                             { channel="guntamatic:biostar:mybiostar:version" }
Number:Time           Biostar_RunningTime                    "RunningTime"                         { channel="guntamatic:biostar:mybiostar:runningTime" }
Number:Time           Biostar_ServiceHrs                     "ServiceHrs"                          { channel="guntamatic:biostar:mybiostar:serviceHrs" }
Number:Time           Biostar_Emptyashin                     "Emptyashin"                          { channel="guntamatic:biostar:mybiostar:emptyAshIn" }
Number:Temperature    Biostar_Flowis0                        "Flowis0"                             { channel="guntamatic:biostar:mybiostar:flowIs0" }
Number:Temperature    Biostar_Flowis3                        "Flowis3"                             { channel="guntamatic:biostar:mybiostar:flowIs3" }
Number:Temperature    Biostar_Flowis6                        "Flowis6"                             { channel="guntamatic:biostar:mybiostar:flowIs6" }
Number:Volume         Biostar_Fuelcounter                    "Fuelcounter"                         { channel="guntamatic:biostar:mybiostar:fuelCounter" }
Number:Dimensionless  Biostar_Bufferload                     "Bufferload"                          { channel="guntamatic:biostar:mybiostar:bufferLoad" }
Number:Temperature    Biostar_BufferTop0                     "BufferTop0"                          { channel="guntamatic:biostar:mybiostar:bufferTop0" }
Number:Temperature    Biostar_BufferBtm0                     "BufferBtm0"                          { channel="guntamatic:biostar:mybiostar:bufferBtm0" }
Number:Temperature    Biostar_BufferTop1                     "BufferTop1"                          { channel="guntamatic:biostar:mybiostar:bufferTop1" }
Number:Temperature    Biostar_BufferBtm1                     "BufferBtm1"                          { channel="guntamatic:biostar:mybiostar:bufferBtm1" }
Number:Temperature    Biostar_BufferTop2                     "BufferTop2"                          { channel="guntamatic:biostar:mybiostar:bufferTop2" }
Number:Temperature    Biostar_BufferBtm2                     "BufferBtm2"                          { channel="guntamatic:biostar:mybiostar:bufferBtm2" }
Switch                Biostar_BextraWW0                      "BextraWW0"                           { channel="guntamatic:biostar:mybiostar:bExtraWw0" }
Switch                Biostar_BextraWW1                      "BextraWW1"                           { channel="guntamatic:biostar:mybiostar:bExtraWw1" }
Switch                Biostar_BextraWW2                      "BextraWW2"                           { channel="guntamatic:biostar:mybiostar:bExtraWw2" }
Switch                Biostar_Auxiliarypump0                 "Auxiliarypump0"                      { channel="guntamatic:biostar:mybiostar:auxiliaryPump0" }
Switch                Biostar_Auxiliarypump1                 "Auxiliarypump1"                      { channel="guntamatic:biostar:mybiostar:auxiliaryPump1" }
Switch                Biostar_Auxiliarypump2                 "Auxiliarypump2"                      { channel="guntamatic:biostar:mybiostar:auxiliaryPump2" }
String                Biostar_Boilersconditionno             "BoilersConditionNo"                  { channel="guntamatic:biostar:mybiostar:boilersConditionNo" }
Number:Temperature    Biostar_BufferT5                       "BufferT5"                            { channel="guntamatic:biostar:mybiostar:bufferT5" }
Number:Temperature    Biostar_BufferT6                       "BufferT6"                            { channel="guntamatic:biostar:mybiostar:bufferT6" }
Number:Temperature    Biostar_BufferT7                       "BufferT7"                            { channel="guntamatic:biostar:mybiostar:bufferT7" }
Number:Temperature    Biostar_ExtraWW0                       "ExtraWW0"                            { channel="guntamatic:biostar:mybiostar:extraWw0" }
Number:Temperature    Biostar_ExtraWW1                       "ExtraWW1"                            { channel="guntamatic:biostar:mybiostar:extraWw1" }
Number:Temperature    Biostar_ExtraWW2                       "ExtraWW2"                            { channel="guntamatic:biostar:mybiostar:extraWw2" }
Number:Dimensionless  Biostar_Grate                          "Grate"                               { channel="guntamatic:biostar:mybiostar:grate" } 
```

**Rule**

```javascript
rule "Example Guntamatic Rule"
when
    Item Season changed
then
    if ( (Season.state != NULL) && (Season.state != UNDEF) )
    {
        if ( Season.state.toString == "WINTER" )
        {
            Biostar_ControlProgram.sendCommand("NORMAL")
        }
        else
        {
            Biostar_ControlProgram.sendCommand("OFF")
        }
    }
end
```

## Open - Your feedback is required

- Testing, using other Guntamatic Heating Systems except from Biostar running firmware 3.2d: Please provide feedback!
