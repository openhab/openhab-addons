# Guntamatic Binding

The Guntamatic Binding can be used to monitor and control [Guntamatic Heating Systems](https://www.guntamatic.com/en/).

## Supported Things

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System, running Firmware 3.2d.
It should work for all other Guntamatic Heating Systems as well, that support the same web interface (Pellets, WoodChips, EnergyGrain as well as Log Heating Systems).

## Things

Guntamatic Heating Systems supported as Thing Types:

| Name          | Thing Type ID | Heating System Type  | Binding Development Status                       |
|---------------|---------------|----------------------|--------------------------------------------------|
| Biostar       | `biostar`     | Pellets              | tested via 15kW, firmware 3.2d, German & English |
| Biosmart      | `biosmart`    | Logs                 | tested via 22kW, firmware 3.2f, German           |
| Powerchip     | `powerchip`   | WoodChips            | tested via 100kW, firmware 3.2d, French          |
| Powercorn     | `powercorn`   | EnergyGrain          | untested                                         |
| Biocom        | `biocom`      | Pellets              | untested                                         |
| Pro           | `pro`         | Pellets or WoodChips | untested                                         |
| Therm         | `therm`       | Pellets              | untested                                         |
| Generic       | `generic`     | -                    | use, if none from above                          |

### Thing Configuration

| Parameter          | Description                                                                 | Default         |
|--------------------|-----------------------------------------------------------------------------|-----------------|
| `hostname`         | Hostname or IP address of the Guntamatic Heating System                     |                 |
| `key`              | Optional, but required to read protected parameters and to control the Guntamatic Heating System.<br/>The key needs to be requested from Guntamatic support, e.g. via https://www.guntamatic.com/en/contact/.                                          |                 |
| `refreshInterval`  | Interval the Guntamatic Heating System is polled in seconds                 | `60`            |
| `encoding`         | Code page used by the Guntamatic Heating System                             | `windows-1252`  |

### Properties

| Property            | Description                                                   | Supported                                         |
|---------------------|---------------------------------------------------------------|---------------------------------------------------|
| `extraWwHeat`       | Parameter used by `controlExtraWwHeat` channels               | all                                               |
| `boilerApproval`    | Parameter used by `controlBoilerApproval` channel             | Biostar, Powerchip, Powercorn, Biocom, Pro, Therm |
| `heatCircProgram`   | Parameter used by `controlHeatCircProgram` channels           | all                                               | 
| `program`           | Parameter used by `controlProgram` channel                    | all                                               |
| `wwHeat`            | Parameter used by `controlWwHeat` channels                    | all                                               |

## Channels

### Control Channels

The Guntamatic Heating System can be controlled using the following channels:

|	Channel             |	Description                                                             | Type	|	Unit	|	Security Access Level	| ReadOnly | Advanced |
|-----------------------|---------------------------------------------------------------------------|-------|:---------:|:-------------------------:|:--------:|:--------:|
|	`controlBoilerApproval`	|	Set Boiler Approval (`AUTO`, `OFF`, `ON`)	                        |	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlProgram`	|	Set Program (`OFF`, `NORMAL`, `WARMWATER`, `MANUAL`<sup id="a1">[1](#f1)</sup>)	|	`String`	|		|	🔐 W1	|	R/W	|	false	|
|	`controlHeatCircProgram0`	|	Set Heat Circle 0 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram1`	|	Set Heat Circle 1 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram2`	|	Set Heat Circle 2 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram3`	|	Set Heat Circle 3 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram4`	|	Set Heat Circle 4 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram5`	|	Set Heat Circle 5 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram6`	|	Set Heat Circle 6 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram7`	|	Set Heat Circle 7 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlHeatCircProgram8`	|	Set Heat Circle 8 Program (`OFF`, `NORMAL`, `HEAT`, `LOWER`)	|	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlWwHeat0`	        |	Trigger Warm Water Circle 0 (`RECHARGE`)	                    |	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlWwHeat1`            |	Trigger Warm Water Circle 1 (`RECHARGE`)	                    |	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlWwHeat2`            |	Trigger Warm Water Circle 2 (`RECHARGE`)	                    |	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlExtraWwHeat0`       |	Trigger Extra Warm Water Circle 0 (`RECHARGE`)	                |	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlExtraWwHeat1`       |	Trigger Extra Warm Water Circle 1 (`RECHARGE`)	                |	`String`	|		|	🔐 W1	|	R/W	|	true	|
|	`controlExtraWwHeat2`       |	Trigger Extra Warm Water Circle 2 (`RECHARGE`)	                |	`String`	|		|	🔐 W1	|	R/W	|	true	|

- <b id="f1">1)</b> ... `MANUAL` is supported by Biostar, Powerchip, Powercorn, Biocom, Pro as well as Therm only [↩](#a1)

#### Response of Control Channels

- `{"ack":"confirmation message"}` ... in case of success
- `{"err":"error message"}`        ... in case of error

The reaction of the Guntamatic Heating System can be monitored via the corresponding data channel. E.g. `programHc1` if you triggered `controlHeatCircProgram1`. The data channel gets updated with the next cyclic update (according to the `refreshInterval` configuration).

### Monitoring Channels

The Binding dynamically generates Channels, derived from the data provided from the actual Guntamatic Heating System.

Example list of Channels using a Guntamatic Biostar 15kW Pellets Heating System running firmware 3.2d and Guntamatic System Language configured to English:

|	Channel             |	Description                                             | Type	|	Unit	|	Security Access Level	| ReadOnly | Advanced |
|-----------------------|-----------------------------------------------------------|-------|:---------:|:-------------------------:|:--------:|:--------:|
|	`running`	|	Running	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`outsideTemp`	|	Outside Temp.	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`blrTargetTemp`	|	Blr.Target Temp	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`boilerTemperature`	|	Boiler Temperature	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flueGasUtilisation`	|	Flue gas utilisation	|	`Number:Dimensionless`	|	`%`	|	🔐 W1	|	R/O	|	false	|
|	`output`	|	Output	|	`Number:Dimensionless`	|	`%`	|	🔓 W0	|	R/O	|	false	|
|	`returnTemp`	|	Return temp	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`co2Target`	|	CO2 Target	|	`Number:Dimensionless`	|	`%`	|	🔐 W1	|	R/O	|	false	|
|	`co2Content`	|	CO2 Content	|	`Number:Dimensionless`	|	`%`	|	🔓 W0	|	R/O	|	false	|
|	`returnTempTarget`	|	Return temp target	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`statusCode`	|	Status code	|	`Number`	|		|	🔐 W1	|	R/O	|	false	|
|	`efficiency`	|	Efficiency	|	`Number:Dimensionless`	|	`%`	|	🔐 W1	|	R/O	|	false	|
|	`extractorSystem`	|	Extractor System	|	`Number:Dimensionless`	|	`%`	|	🔓 W0	|	R/O	|	false	|
|	`feedTurbine`	|	Feed Turbine	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`dischargeMotor`	|	Discharge motor	|	`Number:Dimensionless`	|	`%`	|	🔓 W0	|	R/O	|	false	|
|	`g1Target`	|	G1 Target	|	`Number:Dimensionless`	|	`%`	|	🔓 W0	|	R/O	|	false	|
|	`bufferTop`	|	Buffer Top	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`bufferMid`	|	Buffer Mid	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`bufferBtm`	|	Buffer Btm	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`pumpHp0`	|	Pump HP0	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`dhw0`	|	DHW 0	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`bDhw0`	|	B DHW 0	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`dhw1`	|	DHW 1	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`bDhw1`	|	B DHW 1	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`dhw2`	|	DHW 2	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`bDhw2`	|	B DHW 2	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc0`	|	Room Temp:HC 0	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`heatCirc0`	|	Heat Circ. 0	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc1`	|	Room Temp:HC 1	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowTarget1`	|	Flow Target 1	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`flowIs1`	|	Flow is 1	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`mixer1`	|	Mixer 1	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`heatCirc1`	|	Heat Circ. 1	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`roomTempHc2`	|	Room Temp:HC 2	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowTarget2`	|	Flow Target 2	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`flowIs2`	|	Flow is 2	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`mixer2`	|	Mixer 2	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`heatCirc2`	|	Heat Circ. 2	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc3`	|	Room Temp:HC 3	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`heatCirc3`	|	Heat Circ. 3	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc4`	|	Room Temp:HC 4	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowTarget4`	|	Flow Target 4	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`flowIs4`	|	Flow is 4	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`mixer4`	|	Mixer 4	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`heatCirc4`	|	Heat Circ. 4	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc5`	|	Room Temp:HC 5	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowTarget5`	|	Flow Target 5	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`flowIs5`	|	Flow is 5	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`mixer5`	|	Mixer 5	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`heatCirc5`	|	Heat Circ. 5	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc6`	|	Room Temp:HC 6	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`heatCirc6`	|	Heat Circ. 6	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc7`	|	Room Temp:HC 7	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowTarget7`	|	Flow Target 7	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`flowIs7`	|	Flow is 7	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`mixer7`	|	Mixer 7	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`heatCirc7`	|	Heat Circ. 7	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`roomTempHc8`	|	Room Temp:HC 8	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowTarget8`	|	Flow Target 8	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`flowIs8`	|	Flow is 8	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`mixer8`	|	Mixer 8	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`heatCirc8`	|	Heat Circ. 8	|	`Switch`	|		|	🔓 W0	|	R/O	|	false	|
|	`fuelLevel`	|	Fuel Level	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`stb`	|	STB	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`tks`	|	TKS	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`boilerApproval`	|	Boiler approval	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`programme`	|	Programme	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc0`	|	Program HC0	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc1`	|	Program HC1	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc2`	|	Program HC2	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc3`	|	Program HC3	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc4`	|	Program HC4	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc5`	|	Program HC5	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc6`	|	Program HC6	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc7`	|	Program HC7	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`programHc8`	|	Program HC8	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`interuption0`	|	Interuption 0	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`interuption1`	|	Interuption 1	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`serial`	|	Serial	|	`Number`	|		|	🔓 W0	|	R/O	|	false	|
|	`version`	|	Version	|	`String`	|		|	🔓 W0	|	R/O	|	false	|
|	`runningTime`	|	Running Time	|	`Number:Time`	|	`h`	|	🔓 W0	|	R/O	|	false	|
|	`serviceHrs`	|	Service Hrs	|	`Number:Time`	|	`d`	|	🔓 W0	|	R/O	|	false	|
|	`emptyAshIn`	|	Empty ash in	|	`Number:Time`	|	`h`	|	🔓 W0	|	R/O	|	false	|
|	`flowIs0`	|	Flow is 0	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowIs3`	|	Flow is 3	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`flowIs6`	|	Flow is 6	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`fuelCounter`	|	Fuel counter	|	`Number:Volume`	|	`m³`	|	🔐 W1	|	R/O	|	false	|
|	`bufferLoad`	|	Buffer load.	|	`Number:Dimensionless`	|	`%`	|	🔓 W0	|	R/O	|	false	|
|	`bufferTop0`	|	Buffer Top 0	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`bufferBtm0`	|	Buffer Btm 0	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`bufferTop1`	|	Buffer Top 1	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`bufferBtm1`	|	Buffer Btm 1	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`bufferTop2`	|	Buffer Top 2	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`bufferBtm2`	|	Buffer Btm 2	|	`Number:Temperature`	|	`°C`	|	🔐 W1	|	R/O	|	false	|
|	`bExtraWw0`	|	B extra-WW. 0	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`bExtraWw1`	|	B extra-WW. 1	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`bExtraWw2`	|	B extra-WW. 2	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`auxiliaryPump0`	|	Auxiliary pump 0	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`auxiliaryPump1`	|	Auxiliary pump 1	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`auxiliaryPump2`	|	Auxiliary pump 2	|	`Switch`	|		|	🔐 W1	|	R/O	|	false	|
|	`boilersConditionNo`	|	Boiler´s condition no.	|	`String`	|		|	🔐 W1	|	R/O	|	false	|
|	`bufferT5`	|	Buffer T5	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`bufferT6`	|	Buffer T6	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`bufferT7`	|	Buffer T7	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`extraWw0`	|	Extra-WW. 0	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`extraWw1`	|	Extra-WW. 1	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`extraWw2`	|	Extra-WW. 2	|	`Number:Temperature`	|	`°C`	|	🔓 W0	|	R/O	|	false	|
|	`grate`	|	Grate	|	`Number:Dimensionless`	|	`%`	|	🔓 W0	|	R/O	|	false	|

#### Security Access Levels

- 🔓 W0 ... Open
- 🔐 W1 ... End Customer Key
- 🔒 W2 ... Service Partner

## Full Example

**Thing File**

```java
Thing   guntamatic:biostar:mybiostar   "Guntamatic Biostar"    [ hostname="192.168.1.100", key="0123456789ABCDEF0123456789ABCDEF0123", refreshInterval=60, encoding="windows-1252" ]
```

**Item File**

```java
String              	Biostar_ControlBoilerApproval  	"Set Boiler Approval"               	{ channel="guntamatic:biostar:mybiostar:controlBoilerApproval" }
String              	Biostar_ControlProgram         	"Set Program"                       	{ channel="guntamatic:biostar:mybiostar:controlProgram" }
String              	Biostar_ControlHeatCircProgram0	"Set Heat Circle 0 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram0" }
String              	Biostar_ControlHeatCircProgram1	"Set Heat Circle 1 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram1" }
String              	Biostar_ControlHeatCircProgram2	"Set Heat Circle 2 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram2" }
String              	Biostar_ControlHeatCircProgram3	"Set Heat Circle 3 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram3" }
String              	Biostar_ControlHeatCircProgram4	"Set Heat Circle 4 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram4" }
String              	Biostar_ControlHeatCircProgram5	"Set Heat Circle 5 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram5" }
String              	Biostar_ControlHeatCircProgram6	"Set Heat Circle 6 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram6" }
String              	Biostar_ControlHeatCircProgram7	"Set Heat Circle 7 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram7" }
String              	Biostar_ControlHeatCircProgram8	"Set Heat Circle 8 Program"      	{ channel="guntamatic:biostar:mybiostar:controlHeatCircProgram8" }
String              	Biostar_ControlWwHeat0         	"Trigger Warm Water Circle 0"      	{ channel="guntamatic:biostar:mybiostar:controlWwHeat0" }
String              	Biostar_ControlWwHeat1         	"Trigger Warm Water Circle 1"      	{ channel="guntamatic:biostar:mybiostar:controlWwHeat1" }
String              	Biostar_ControlWwHeat2         	"Trigger Warm Water Circle 2"      	{ channel="guntamatic:biostar:mybiostar:controlWwHeat2" }
String              	Biostar_ControlExtraWwHeat0    	"Trigger Extra Warm Water Circle 0"	{ channel="guntamatic:biostar:mybiostar:controlExtraWwHeat0" }
String              	Biostar_ControlExtraWwHeat1    	"Trigger Extra Warm Water Circle 1"	{ channel="guntamatic:biostar:mybiostar:controlExtraWwHeat1" }
String              	Biostar_ControlExtraWwHeat2    	"Trigger Extra Warm Water Circle 2"	{ channel="guntamatic:biostar:mybiostar:controlExtraWwHeat2" }
String              	Biostar_Running                	"Running"                           	{ channel="guntamatic:biostar:mybiostar:running" }
Number:Temperature  	Biostar_OutsideTemp            	"Outside Temp."                     	{ channel="guntamatic:biostar:mybiostar:outsideTemp" }
Number:Temperature  	Biostar_BlrTargetTemp          	"Blr.Target Temp"                   	{ channel="guntamatic:biostar:mybiostar:blrTargetTemp" }
Number:Temperature  	Biostar_BoilerTemperature      	"Boiler Temperature"                	{ channel="guntamatic:biostar:mybiostar:boilerTemperature" }
Number:Dimensionless	Biostar_FlueGasUtilisation     	"Flue gas utilisation"              	{ channel="guntamatic:biostar:mybiostar:flueGasUtilisation" }
Number:Dimensionless	Biostar_Output                 	"Output"                            	{ channel="guntamatic:biostar:mybiostar:output" }
Number:Temperature  	Biostar_ReturnTemp             	"Return temp"                       	{ channel="guntamatic:biostar:mybiostar:returnTemp" }
Number:Dimensionless	Biostar_Co2Target              	"CO2 Target"                        	{ channel="guntamatic:biostar:mybiostar:co2Target" }
Number:Dimensionless	Biostar_Co2Content             	"CO2 Content"                       	{ channel="guntamatic:biostar:mybiostar:co2Content" }
Number:Temperature  	Biostar_ReturnTempTarget       	"Return temp target"                	{ channel="guntamatic:biostar:mybiostar:returnTempTarget" }
Number              	Biostar_StatusCode             	"Status code"                       	{ channel="guntamatic:biostar:mybiostar:statusCode" }
Number:Dimensionless	Biostar_Efficiency             	"Efficiency"                        	{ channel="guntamatic:biostar:mybiostar:efficiency" }
Number:Dimensionless	Biostar_ExtractorSystem        	"Extractor System"                  	{ channel="guntamatic:biostar:mybiostar:extractorSystem" }
String              	Biostar_FeedTurbine            	"Feed Turbine"                      	{ channel="guntamatic:biostar:mybiostar:feedTurbine" }
Number:Dimensionless	Biostar_DischargeMotor         	"Discharge motor"                   	{ channel="guntamatic:biostar:mybiostar:dischargeMotor" }
Number:Dimensionless	Biostar_G1Target               	"G1 Target"                         	{ channel="guntamatic:biostar:mybiostar:g1Target" }
Number:Temperature  	Biostar_BufferTop              	"Buffer Top"                        	{ channel="guntamatic:biostar:mybiostar:bufferTop" }
Number:Temperature  	Biostar_BufferMid              	"Buffer Mid"                        	{ channel="guntamatic:biostar:mybiostar:bufferMid" }
Number:Temperature  	Biostar_BufferBtm              	"Buffer Btm"                        	{ channel="guntamatic:biostar:mybiostar:bufferBtm" }
Switch              	Biostar_PumpHp0                	"Pump HP0"                          	{ channel="guntamatic:biostar:mybiostar:pumpHp0" }
Number:Temperature  	Biostar_Dhw0                   	"DHW 0"                             	{ channel="guntamatic:biostar:mybiostar:dhw0" }
Switch              	Biostar_BDhw0                  	"B DHW 0"                           	{ channel="guntamatic:biostar:mybiostar:bDhw0" }
Number:Temperature  	Biostar_Dhw1                   	"DHW 1"                             	{ channel="guntamatic:biostar:mybiostar:dhw1" }
Switch              	Biostar_BDhw1                  	"B DHW 1"                           	{ channel="guntamatic:biostar:mybiostar:bDhw1" }
Number:Temperature  	Biostar_Dhw2                   	"DHW 2"                             	{ channel="guntamatic:biostar:mybiostar:dhw2" }
Switch              	Biostar_BDhw2                  	"B DHW 2"                           	{ channel="guntamatic:biostar:mybiostar:bDhw2" }
Number:Temperature  	Biostar_RoomTempHc0            	"Room Temp:HC 0"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc0" }
Switch              	Biostar_HeatCirc0              	"Heat Circ. 0"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc0" }
Number:Temperature  	Biostar_RoomTempHc1            	"Room Temp:HC 1"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc1" }
Number:Temperature  	Biostar_FlowTarget1            	"Flow Target 1"                     	{ channel="guntamatic:biostar:mybiostar:flowTarget1" }
Number:Temperature  	Biostar_FlowIs1                	"Flow is 1"                         	{ channel="guntamatic:biostar:mybiostar:flowIs1" }
String              	Biostar_Mixer1                 	"Mixer 1"                           	{ channel="guntamatic:biostar:mybiostar:mixer1" }
Switch              	Biostar_HeatCirc1              	"Heat Circ. 1"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc1" }
Number:Temperature  	Biostar_RoomTempHc2            	"Room Temp:HC 2"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc2" }
Number:Temperature  	Biostar_FlowTarget2            	"Flow Target 2"                     	{ channel="guntamatic:biostar:mybiostar:flowTarget2" }
Number:Temperature  	Biostar_FlowIs2                	"Flow is 2"                         	{ channel="guntamatic:biostar:mybiostar:flowIs2" }
String              	Biostar_Mixer2                 	"Mixer 2"                           	{ channel="guntamatic:biostar:mybiostar:mixer2" }
Switch              	Biostar_HeatCirc2              	"Heat Circ. 2"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc2" }
Number:Temperature  	Biostar_RoomTempHc3            	"Room Temp:HC 3"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc3" }
Switch              	Biostar_HeatCirc3              	"Heat Circ. 3"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc3" }
Number:Temperature  	Biostar_RoomTempHc4            	"Room Temp:HC 4"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc4" }
Number:Temperature  	Biostar_FlowTarget4            	"Flow Target 4"                     	{ channel="guntamatic:biostar:mybiostar:flowTarget4" }
Number:Temperature  	Biostar_FlowIs4                	"Flow is 4"                         	{ channel="guntamatic:biostar:mybiostar:flowIs4" }
String              	Biostar_Mixer4                 	"Mixer 4"                           	{ channel="guntamatic:biostar:mybiostar:mixer4" }
Switch              	Biostar_HeatCirc4              	"Heat Circ. 4"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc4" }
Number:Temperature  	Biostar_RoomTempHc5            	"Room Temp:HC 5"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc5" }
Number:Temperature  	Biostar_FlowTarget5            	"Flow Target 5"                     	{ channel="guntamatic:biostar:mybiostar:flowTarget5" }
Number:Temperature  	Biostar_FlowIs5                	"Flow is 5"                         	{ channel="guntamatic:biostar:mybiostar:flowIs5" }
String              	Biostar_Mixer5                 	"Mixer 5"                           	{ channel="guntamatic:biostar:mybiostar:mixer5" }
Switch              	Biostar_HeatCirc5              	"Heat Circ. 5"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc5" }
Number:Temperature  	Biostar_RoomTempHc6            	"Room Temp:HC 6"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc6" }
Switch              	Biostar_HeatCirc6              	"Heat Circ. 6"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc6" }
Number:Temperature  	Biostar_RoomTempHc7            	"Room Temp:HC 7"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc7" }
Number:Temperature  	Biostar_FlowTarget7            	"Flow Target 7"                     	{ channel="guntamatic:biostar:mybiostar:flowTarget7" }
Number:Temperature  	Biostar_FlowIs7                	"Flow is 7"                         	{ channel="guntamatic:biostar:mybiostar:flowIs7" }
String              	Biostar_Mixer7                 	"Mixer 7"                           	{ channel="guntamatic:biostar:mybiostar:mixer7" }
Switch              	Biostar_HeatCirc7              	"Heat Circ. 7"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc7" }
Number:Temperature  	Biostar_RoomTempHc8            	"Room Temp:HC 8"                    	{ channel="guntamatic:biostar:mybiostar:roomTempHc8" }
Number:Temperature  	Biostar_FlowTarget8            	"Flow Target 8"                     	{ channel="guntamatic:biostar:mybiostar:flowTarget8" }
Number:Temperature  	Biostar_FlowIs8                	"Flow is 8"                         	{ channel="guntamatic:biostar:mybiostar:flowIs8" }
String              	Biostar_Mixer8                 	"Mixer 8"                           	{ channel="guntamatic:biostar:mybiostar:mixer8" }
Switch              	Biostar_HeatCirc8              	"Heat Circ. 8"                      	{ channel="guntamatic:biostar:mybiostar:heatCirc8" }
String              	Biostar_FuelLevel              	"Fuel Level"                        	{ channel="guntamatic:biostar:mybiostar:fuelLevel" }
String              	Biostar_Stb                    	"STB"                               	{ channel="guntamatic:biostar:mybiostar:stb" }
String              	Biostar_Tks                    	"TKS"                               	{ channel="guntamatic:biostar:mybiostar:tks" }
Switch              	Biostar_BoilerApproval         	"Boiler approval"                   	{ channel="guntamatic:biostar:mybiostar:boilerApproval" }
String              	Biostar_Programme              	"Programme"                         	{ channel="guntamatic:biostar:mybiostar:programme" }
String              	Biostar_ProgramHc0             	"Program HC0"                       	{ channel="guntamatic:biostar:mybiostar:programHc0" }
String              	Biostar_ProgramHc1             	"Program HC1"                       	{ channel="guntamatic:biostar:mybiostar:programHc1" }
String              	Biostar_ProgramHc2             	"Program HC2"                       	{ channel="guntamatic:biostar:mybiostar:programHc2" }
String              	Biostar_ProgramHc3             	"Program HC3"                       	{ channel="guntamatic:biostar:mybiostar:programHc3" }
String              	Biostar_ProgramHc4             	"Program HC4"                       	{ channel="guntamatic:biostar:mybiostar:programHc4" }
String              	Biostar_ProgramHc5             	"Program HC5"                       	{ channel="guntamatic:biostar:mybiostar:programHc5" }
String              	Biostar_ProgramHc6             	"Program HC6"                       	{ channel="guntamatic:biostar:mybiostar:programHc6" }
String              	Biostar_ProgramHc7             	"Program HC7"                       	{ channel="guntamatic:biostar:mybiostar:programHc7" }
String              	Biostar_ProgramHc8             	"Program HC8"                       	{ channel="guntamatic:biostar:mybiostar:programHc8" }
String              	Biostar_Interuption0           	"Interuption 0"                     	{ channel="guntamatic:biostar:mybiostar:interuption0" }
String              	Biostar_Interuption1           	"Interuption 1"                     	{ channel="guntamatic:biostar:mybiostar:interuption1" }
Number              	Biostar_Serial                 	"Serial"                            	{ channel="guntamatic:biostar:mybiostar:serial" }
String              	Biostar_Version                	"Version"                           	{ channel="guntamatic:biostar:mybiostar:version" }
Number:Time         	Biostar_RunningTime            	"Running Time"                      	{ channel="guntamatic:biostar:mybiostar:runningTime" }
Number:Time         	Biostar_ServiceHrs             	"Service Hrs"                       	{ channel="guntamatic:biostar:mybiostar:serviceHrs" }
Number:Time         	Biostar_EmptyAshIn             	"Empty ash in"                      	{ channel="guntamatic:biostar:mybiostar:emptyAshIn" }
Number:Temperature  	Biostar_FlowIs0                	"Flow is 0"                         	{ channel="guntamatic:biostar:mybiostar:flowIs0" }
Number:Temperature  	Biostar_FlowIs3                	"Flow is 3"                         	{ channel="guntamatic:biostar:mybiostar:flowIs3" }
Number:Temperature  	Biostar_FlowIs6                	"Flow is 6"                         	{ channel="guntamatic:biostar:mybiostar:flowIs6" }
Number:Volume       	Biostar_FuelCounter            	"Fuel counter"                      	{ channel="guntamatic:biostar:mybiostar:fuelCounter" }
Number:Dimensionless	Biostar_BufferLoad             	"Buffer load."                      	{ channel="guntamatic:biostar:mybiostar:bufferLoad" }
Number:Temperature  	Biostar_BufferTop0             	"Buffer Top 0"                      	{ channel="guntamatic:biostar:mybiostar:bufferTop0" }
Number:Temperature  	Biostar_BufferBtm0             	"Buffer Btm 0"                      	{ channel="guntamatic:biostar:mybiostar:bufferBtm0" }
Number:Temperature  	Biostar_BufferTop1             	"Buffer Top 1"                      	{ channel="guntamatic:biostar:mybiostar:bufferTop1" }
Number:Temperature  	Biostar_BufferBtm1             	"Buffer Btm 1"                      	{ channel="guntamatic:biostar:mybiostar:bufferBtm1" }
Number:Temperature  	Biostar_BufferTop2             	"Buffer Top 2"                      	{ channel="guntamatic:biostar:mybiostar:bufferTop2" }
Number:Temperature  	Biostar_BufferBtm2             	"Buffer Btm 2"                      	{ channel="guntamatic:biostar:mybiostar:bufferBtm2" }
Switch              	Biostar_BExtraWw0              	"B extra-WW. 0"                     	{ channel="guntamatic:biostar:mybiostar:bExtraWw0" }
Switch              	Biostar_BExtraWw1              	"B extra-WW. 1"                     	{ channel="guntamatic:biostar:mybiostar:bExtraWw1" }
Switch              	Biostar_BExtraWw2              	"B extra-WW. 2"                     	{ channel="guntamatic:biostar:mybiostar:bExtraWw2" }
Switch              	Biostar_AuxiliaryPump0         	"Auxiliary pump 0"                  	{ channel="guntamatic:biostar:mybiostar:auxiliaryPump0" }
Switch              	Biostar_AuxiliaryPump1         	"Auxiliary pump 1"                  	{ channel="guntamatic:biostar:mybiostar:auxiliaryPump1" }
Switch              	Biostar_AuxiliaryPump2         	"Auxiliary pump 2"                  	{ channel="guntamatic:biostar:mybiostar:auxiliaryPump2" }
String              	Biostar_BoilersConditionNo     	"Boiler´s condition no."            	{ channel="guntamatic:biostar:mybiostar:boilersConditionNo" }
Number:Temperature  	Biostar_BufferT5               	"Buffer T5"                         	{ channel="guntamatic:biostar:mybiostar:bufferT5" }
Number:Temperature  	Biostar_BufferT6               	"Buffer T6"                         	{ channel="guntamatic:biostar:mybiostar:bufferT6" }
Number:Temperature  	Biostar_BufferT7               	"Buffer T7"                         	{ channel="guntamatic:biostar:mybiostar:bufferT7" }
Number:Temperature  	Biostar_ExtraWw0               	"Extra-WW. 0"                       	{ channel="guntamatic:biostar:mybiostar:extraWw0" }
Number:Temperature  	Biostar_ExtraWw1               	"Extra-WW. 1"                       	{ channel="guntamatic:biostar:mybiostar:extraWw1" }
Number:Temperature  	Biostar_ExtraWw2               	"Extra-WW. 2"                       	{ channel="guntamatic:biostar:mybiostar:extraWw2" }
Number:Dimensionless	Biostar_Grate                  	"Grate"                             	{ channel="guntamatic:biostar:mybiostar:grate" }
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

## Your feedback is required

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System, running Firmware 3.2d.
Please provide feedback (👍 as well as 👎) when using the Binding for other Guntamatic Heating Systems.

Forum topic for feedback:

 - [openHAB community #128451](https://community.openhab.org/t/guntamatic-new-binding-for-guntamatic-heating-systems-biostar-powerchip-powercorn-biocom-pro-therm/128451 "openHAB community #128451")
