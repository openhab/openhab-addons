# Guntamatic Binding

The Guntamatic Binding can be used to monitor and control Guntamatic Heating Systems (https://www.guntamatic.com/en/).

## Supported Things

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System running Firmware 3.2d.
It should work for all other Guntamatic Heating Systems as well, that support the same web interface:

- Biostar (tested via 15kW, firmware 3.2d)
- Powerchip (untested)
- Powercorn (untested)
- Biocom (untested)
- Pro (untested)
- Therm (untested)

## Thing Configuration

| Parameter     | Description                                                               | Default       |
|---------------|---------------------------------------------------------------------------|---------------|
| Hostname      | Hostname or IP address of the Guntamatic Heating System                   |               |
| Key           | Optional, but required to read protected parameters and to control the Guntamatic Heating System. The key needs to be reqested from Guntamatic support, e.g. via https://www.guntamatic.com/en/contact/                                    |               |
| Refresh Interval    | Interval the Guntamatic Heating System is polled in seconds         | 60            |
| Encoding      | Code page used by the Guntamatic Heating System                           | windows-1252  |

## Channels

The Binding dynamically generates Channels, derived from the data provided from the actual Guntamatic Heating System.

Example list of Channels using a Guntamatic Biostar 15kW Pellets Heating System running firmware 3.2d and Guntamatic System Language configured to English:

|	Channel             |	Description                                             | Type	|	Unit	|	Security Access Level	| ReadOnly | Advanced |
|-----------------------|-----------------------------------------------------------|------------|-----------|----------------------|----------|----------|
|	setboilerapproval	|	Set Boiler Approval (AUTO, OFF, ON)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setprogram	|	Set Program (OFF, NORMAL, WARMWATER, MANUAL)	|	String	|		|	🔐 W1	|	R/W	|	false	|
|	setheatcircprogram0	|	Set Program of Heat Circle 0 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram1	|	Set Program of Heat Circle 1 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram2	|	Set Program of Heat Circle 2 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram3	|	Set Program of Heat Circle 3 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram4	|	Set Program of Heat Circle 4 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram5	|	Set Program of Heat Circle 5 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram6	|	Set Program of Heat Circle 6 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram7	|	Set Program of Heat Circle 7 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram8	|	Set Program of Heat Circle 8 (OFF, NORMAL, HEAT, LOWER)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setwwheat0	|	Trigger Warm Wather Circle 0 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setwwheat1	|	Trigger Warm Wather Circle 1 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setwwheat2	|	Trigger Warm Wather Circle 2 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setextrawwheat0	|	Trigger Extra Warm Wather Circle 0 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setextrawwheat1	|	Trigger Extra Warm Wather Circle 1 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setextrawwheat2	|	Trigger Extra Warm Wather Circle 2 (RECHARGE)	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	running	|	Running	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	outsidetemp	|	Outside Temp.	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	blrtargettemp	|	Boiler Target Temp	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	boilertemperature	|	Boiler Temperature	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	fluegasutilisation	|	Flue gas utilisation	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	output	|	Output	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	returntemp	|	Return temp	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	co2target	|	CO2 Target	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	c02content	|	C02 Content	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	returntemptarget	|	return temp target	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	statuscode	|	status code	|	Number	|		|	🔐 W1	|	R/O	|	false	|
|	efficiency	|	Efficiency	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	extractorsystem	|	Extractor System	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	feedturbine	|	Feed Turbine	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	dischargemotor	|	discharge motor	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	g1target	|	G1 Target	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	buffertop	|	Buffer Top	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	buffermid	|	Buffer Mid	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bufferbtm	|	Buffer Btm	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	pumphp0	|	Pump HP0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw0	|	DHW 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bdhw0	|	B DHW 0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw1	|	DHW 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bdhw1	|	B DHW 1	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw2	|	DHW 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bdhw2	|	B DHW 2	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc0	|	Room Temp:HC 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatcirc0	|	Heat Circ. 0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc1	|	Room Temp:HC 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget1	|	Flow Target 1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis1	|	Flow is 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer1	|	Mixer 1	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc1	|	Heat Circ. 1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	roomtemphc2	|	Room Temp:HC 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget2	|	Flow Target 2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis2	|	Flow is 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer2	|	Mixer 2	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc2	|	Heat Circ. 2	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc3	|	Room Temp:HC 3	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatcirc3	|	Heat Circ. 3	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc4	|	Room Temp:HC 4	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget4	|	Flow Target 4	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis4	|	Flow is 4	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer4	|	Mixer 4	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc4	|	Heat Circ. 4	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc5	|	Room Temp:HC 5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget5	|	Flow Target 5	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis5	|	Flow is 5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer5	|	Mixer 5	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc5	|	Heat Circ. 5	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc6	|	Room Temp:HC 6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatcirc6	|	Heat Circ. 6	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc7	|	Room Temp:HC 7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget7	|	Flow Target 7	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis7	|	Flow is 7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer7	|	Mixer 7	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc7	|	Heat Circ. 7	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc8	|	Room Temp:HC 8	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget8	|	Flow Target 8	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis8	|	Flow is 8	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer8	|	Mixer 8	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc8	|	Heat Circ. 8	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	fuellevel	|	Fuel Level	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	stb	|	STB	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	tks	|	TKS	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	boilerapproval	|	Boiler approval	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	programme	|	Programme	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc0	|	Program HC0	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc1	|	Program HC1	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc2	|	Program HC2	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc3	|	Program HC3	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc4	|	Program HC4	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc5	|	Program HC5	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc6	|	Program HC6	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc7	|	Program HC7	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc8	|	Program HC8	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	interuption0	|	Interuption 0	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	interuption1	|	Interuption 1	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	serial	|	Serial	|	Number	|		|	🔓 W0	|	R/O	|	false	|
|	version	|	Version	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	runningtime	|	Running Time	|	Number:Time	|	h	|	🔓 W0	|	R/O	|	false	|
|	servicehrs	|	Service Hrs	|	Number:Time	|	d	|	🔓 W0	|	R/O	|	false	|
|	emptyashin	|	Empty ash in	|	Number:Time	|	h	|	🔓 W0	|	R/O	|	false	|
|	flowis0	|	Flow is 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowis3	|	Flow is 3	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowis6	|	Flow is 6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	fuelcounter	|	Fuel counter	|	Number:Volume	|	m³	|	🔐 W1	|	R/O	|	false	|
|	bufferload	|	Buffer load.	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	buffertop0	|	Buffer Top 0	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferbtm0	|	Buffer Btm 0	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	buffertop1	|	Buffer Top 1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferbtm1	|	Buffer Btm 1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	buffertop2	|	Buffer Top 2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferbtm2	|	Buffer Btm 2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bextraww0	|	B extra-WW. 0	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	bextraww1	|	B extra-WW. 1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	bextraww2	|	B extra-WW. 2	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliarypump0	|	Auxiliary pump 0	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliarypump1	|	Auxiliary pump 1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliarypump2	|	Auxiliary pump 2	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	boilersconditionno	|	Boiler´s condition no.	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	buffert5	|	Buffer T5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	buffert6	|	Buffer T6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	buffert7	|	Buffer T7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraww0	|	extra-WW. 0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraww1	|	extra-WW. 1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraww2	|	extra-WW. 2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	grate	|	grate	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|

Security Access Levels:

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
String                Biostar_SetBoilerApproval              "Set Boiler Approval"                 { channel="guntamatic:biostar:mybiostar:setboilerapproval" }  
String                Biostar_SetProgram                     "Set Program"                         { channel="guntamatic:biostar:mybiostar:setprogram" }         
String                Biostar_SetProgramofHeatCircle0        "Set Program of Heat Circle 0"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram0" }
String                Biostar_SetProgramofHeatCircle1        "Set Program of Heat Circle 1"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram1" }
String                Biostar_SetProgramofHeatCircle2        "Set Program of Heat Circle 2"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram2" }
String                Biostar_SetProgramofHeatCircle3        "Set Program of Heat Circle 3"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram3" }
String                Biostar_SetProgramofHeatCircle4        "Set Program of Heat Circle 4"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram4" }
String                Biostar_SetProgramofHeatCircle5        "Set Program of Heat Circle 5"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram5" }
String                Biostar_SetProgramofHeatCircle6        "Set Program of Heat Circle 6"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram6" }
String                Biostar_SetProgramofHeatCircle7        "Set Program of Heat Circle 7"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram7" }
String                Biostar_SetProgramofHeatCircle8        "Set Program of Heat Circle 8"        { channel="guntamatic:biostar:mybiostar:setheatcircprogram8" }
String                Biostar_TriggerWarmWatherCircle0       "Trigger Warm Wather Circle 0"        { channel="guntamatic:biostar:mybiostar:setwwheat0" }         
String                Biostar_TriggerWarmWatherCircle1       "Trigger Warm Wather Circle 1"        { channel="guntamatic:biostar:mybiostar:setwwheat1" }         
String                Biostar_TriggerWarmWatherCircle2       "Trigger Warm Wather Circle 2"        { channel="guntamatic:biostar:mybiostar:setwwheat2" }         
String                Biostar_TriggerExtraWarmWatherCircle0  "Trigger Extra Warm Wather Circle 0"  { channel="guntamatic:biostar:mybiostar:setextrawwheat0" }    
String                Biostar_TriggerExtraWarmWatherCircle1  "Trigger Extra Warm Wather Circle 1"  { channel="guntamatic:biostar:mybiostar:setextrawwheat1" }    
String                Biostar_TriggerExtraWarmWatherCircle2  "Trigger Extra Warm Wather Circle 2"  { channel="guntamatic:biostar:mybiostar:setextrawwheat2" }    
String                Biostar_Running                        "Running"                             { channel="guntamatic:biostar:mybiostar:running" }            
Number:Temperature    Biostar_OutsideTemp                    "OutsideTemp"                         { channel="guntamatic:biostar:mybiostar:outsidetemp" }        
Number:Temperature    Biostar_BoilerTargetTemp               "BoilerTargetTemp"                    { channel="guntamatic:biostar:mybiostar:blrtargettemp" }      
Number:Temperature    Biostar_BoilerTemperature              "BoilerTemperature"                   { channel="guntamatic:biostar:mybiostar:boilertemperature" }  
Number:Dimensionless  Biostar_Fluegasutilisation             "Fluegasutilisation"                  { channel="guntamatic:biostar:mybiostar:fluegasutilisation" } 
Number:Dimensionless  Biostar_Output                         "Output"                              { channel="guntamatic:biostar:mybiostar:output" }             
Number:Temperature    Biostar_Returntemp                     "Returntemp"                          { channel="guntamatic:biostar:mybiostar:returntemp" }         
Number:Dimensionless  Biostar_CO2Target                      "CO2Target"                           { channel="guntamatic:biostar:mybiostar:co2target" }          
Number:Dimensionless  Biostar_C02Content                     "C02Content"                          { channel="guntamatic:biostar:mybiostar:c02content" }         
Number:Temperature    Biostar_Returntemptarget               "Returntemptarget"                    { channel="guntamatic:biostar:mybiostar:returntemptarget" }   
Number                Biostar_Statuscode                     "Statuscode"                          { channel="guntamatic:biostar:mybiostar:statuscode" }         
Number:Dimensionless  Biostar_Efficiency                     "Efficiency"                          { channel="guntamatic:biostar:mybiostar:efficiency" }         
Number:Dimensionless  Biostar_ExtractorSystem                "ExtractorSystem"                     { channel="guntamatic:biostar:mybiostar:extractorsystem" }    
String                Biostar_FeedTurbine                    "FeedTurbine"                         { channel="guntamatic:biostar:mybiostar:feedturbine" }        
Number:Dimensionless  Biostar_Dischargemotor                 "Dischargemotor"                      { channel="guntamatic:biostar:mybiostar:dischargemotor" }     
Number:Dimensionless  Biostar_G1Target                       "G1Target"                            { channel="guntamatic:biostar:mybiostar:g1target" }           
Number:Temperature    Biostar_BufferTop                      "BufferTop"                           { channel="guntamatic:biostar:mybiostar:buffertop" }          
Number:Temperature    Biostar_BufferMid                      "BufferMid"                           { channel="guntamatic:biostar:mybiostar:buffermid" }          
Number:Temperature    Biostar_BufferBtm                      "BufferBtm"                           { channel="guntamatic:biostar:mybiostar:bufferbtm" }          
Switch                Biostar_PumpHP0                        "PumpHP0"                             { channel="guntamatic:biostar:mybiostar:pumphp0" }            
Number:Temperature    Biostar_DHW0                           "DHW0"                                { channel="guntamatic:biostar:mybiostar:dhw0" }               
Switch                Biostar_BDHW0                          "BDHW0"                               { channel="guntamatic:biostar:mybiostar:bdhw0" }              
Number:Temperature    Biostar_DHW1                           "DHW1"                                { channel="guntamatic:biostar:mybiostar:dhw1" }               
Switch                Biostar_BDHW1                          "BDHW1"                               { channel="guntamatic:biostar:mybiostar:bdhw1" }              
Number:Temperature    Biostar_DHW2                           "DHW2"                                { channel="guntamatic:biostar:mybiostar:dhw2" }               
Switch                Biostar_BDHW2                          "BDHW2"                               { channel="guntamatic:biostar:mybiostar:bdhw2" }              
Number:Temperature    Biostar_RoomTempHC0                    "RoomTempHC0"                         { channel="guntamatic:biostar:mybiostar:roomtemphc0" }        
Switch                Biostar_HeatCirc0                      "HeatCirc0"                           { channel="guntamatic:biostar:mybiostar:heatcirc0" }          
Number:Temperature    Biostar_RoomTempHC1                    "RoomTempHC1"                         { channel="guntamatic:biostar:mybiostar:roomtemphc1" }        
Number:Temperature    Biostar_FlowTarget1                    "FlowTarget1"                         { channel="guntamatic:biostar:mybiostar:flowtarget1" }        
Number:Temperature    Biostar_Flowis1                        "Flowis1"                             { channel="guntamatic:biostar:mybiostar:flowis1" }            
String                Biostar_Mixer1                         "Mixer1"                              { channel="guntamatic:biostar:mybiostar:mixer1" }             
Switch                Biostar_HeatCirc1                      "HeatCirc1"                           { channel="guntamatic:biostar:mybiostar:heatcirc1" }          
Number:Temperature    Biostar_RoomTempHC2                    "RoomTempHC2"                         { channel="guntamatic:biostar:mybiostar:roomtemphc2" }        
Number:Temperature    Biostar_FlowTarget2                    "FlowTarget2"                         { channel="guntamatic:biostar:mybiostar:flowtarget2" }        
Number:Temperature    Biostar_Flowis2                        "Flowis2"                             { channel="guntamatic:biostar:mybiostar:flowis2" }            
String                Biostar_Mixer2                         "Mixer2"                              { channel="guntamatic:biostar:mybiostar:mixer2" }             
Switch                Biostar_HeatCirc2                      "HeatCirc2"                           { channel="guntamatic:biostar:mybiostar:heatcirc2" }          
Number:Temperature    Biostar_RoomTempHC3                    "RoomTempHC3"                         { channel="guntamatic:biostar:mybiostar:roomtemphc3" }        
Switch                Biostar_HeatCirc3                      "HeatCirc3"                           { channel="guntamatic:biostar:mybiostar:heatcirc3" }          
Number:Temperature    Biostar_RoomTempHC4                    "RoomTempHC4"                         { channel="guntamatic:biostar:mybiostar:roomtemphc4" }        
Number:Temperature    Biostar_FlowTarget4                    "FlowTarget4"                         { channel="guntamatic:biostar:mybiostar:flowtarget4" }        
Number:Temperature    Biostar_Flowis4                        "Flowis4"                             { channel="guntamatic:biostar:mybiostar:flowis4" }            
String                Biostar_Mixer4                         "Mixer4"                              { channel="guntamatic:biostar:mybiostar:mixer4" }             
Switch                Biostar_HeatCirc4                      "HeatCirc4"                           { channel="guntamatic:biostar:mybiostar:heatcirc4" }          
Number:Temperature    Biostar_RoomTempHC5                    "RoomTempHC5"                         { channel="guntamatic:biostar:mybiostar:roomtemphc5" }        
Number:Temperature    Biostar_FlowTarget5                    "FlowTarget5"                         { channel="guntamatic:biostar:mybiostar:flowtarget5" }        
Number:Temperature    Biostar_Flowis5                        "Flowis5"                             { channel="guntamatic:biostar:mybiostar:flowis5" }            
String                Biostar_Mixer5                         "Mixer5"                              { channel="guntamatic:biostar:mybiostar:mixer5" }             
Switch                Biostar_HeatCirc5                      "HeatCirc5"                           { channel="guntamatic:biostar:mybiostar:heatcirc5" }          
Number:Temperature    Biostar_RoomTempHC6                    "RoomTempHC6"                         { channel="guntamatic:biostar:mybiostar:roomtemphc6" }        
Switch                Biostar_HeatCirc6                      "HeatCirc6"                           { channel="guntamatic:biostar:mybiostar:heatcirc6" }          
Number:Temperature    Biostar_RoomTempHC7                    "RoomTempHC7"                         { channel="guntamatic:biostar:mybiostar:roomtemphc7" }        
Number:Temperature    Biostar_FlowTarget7                    "FlowTarget7"                         { channel="guntamatic:biostar:mybiostar:flowtarget7" }        
Number:Temperature    Biostar_Flowis7                        "Flowis7"                             { channel="guntamatic:biostar:mybiostar:flowis7" }            
String                Biostar_Mixer7                         "Mixer7"                              { channel="guntamatic:biostar:mybiostar:mixer7" }             
Switch                Biostar_HeatCirc7                      "HeatCirc7"                           { channel="guntamatic:biostar:mybiostar:heatcirc7" }          
Number:Temperature    Biostar_RoomTempHC8                    "RoomTempHC8"                         { channel="guntamatic:biostar:mybiostar:roomtemphc8" }        
Number:Temperature    Biostar_FlowTarget8                    "FlowTarget8"                         { channel="guntamatic:biostar:mybiostar:flowtarget8" }        
Number:Temperature    Biostar_Flowis8                        "Flowis8"                             { channel="guntamatic:biostar:mybiostar:flowis8" }            
String                Biostar_Mixer8                         "Mixer8"                              { channel="guntamatic:biostar:mybiostar:mixer8" }             
Switch                Biostar_HeatCirc8                      "HeatCirc8"                           { channel="guntamatic:biostar:mybiostar:heatcirc8" }          
String                Biostar_FuelLevel                      "FuelLevel"                           { channel="guntamatic:biostar:mybiostar:fuellevel" }          
String                Biostar_STB                            "STB"                                 { channel="guntamatic:biostar:mybiostar:stb" }                
String                Biostar_TKS                            "TKS"                                 { channel="guntamatic:biostar:mybiostar:tks" }                
Switch                Biostar_Boilerapproval                 "Boilerapproval"                      { channel="guntamatic:biostar:mybiostar:boilerapproval" }     
String                Biostar_Programme                      "Programme"                           { channel="guntamatic:biostar:mybiostar:programme" }          
String                Biostar_ProgramHC0                     "ProgramHC0"                          { channel="guntamatic:biostar:mybiostar:programhc0" }         
String                Biostar_ProgramHC1                     "ProgramHC1"                          { channel="guntamatic:biostar:mybiostar:programhc1" }         
String                Biostar_ProgramHC2                     "ProgramHC2"                          { channel="guntamatic:biostar:mybiostar:programhc2" }         
String                Biostar_ProgramHC3                     "ProgramHC3"                          { channel="guntamatic:biostar:mybiostar:programhc3" }         
String                Biostar_ProgramHC4                     "ProgramHC4"                          { channel="guntamatic:biostar:mybiostar:programhc4" }         
String                Biostar_ProgramHC5                     "ProgramHC5"                          { channel="guntamatic:biostar:mybiostar:programhc5" }         
String                Biostar_ProgramHC6                     "ProgramHC6"                          { channel="guntamatic:biostar:mybiostar:programhc6" }         
String                Biostar_ProgramHC7                     "ProgramHC7"                          { channel="guntamatic:biostar:mybiostar:programhc7" }         
String                Biostar_ProgramHC8                     "ProgramHC8"                          { channel="guntamatic:biostar:mybiostar:programhc8" }         
String                Biostar_Interuption0                   "Interuption0"                        { channel="guntamatic:biostar:mybiostar:interuption0" }       
String                Biostar_Interuption1                   "Interuption1"                        { channel="guntamatic:biostar:mybiostar:interuption1" }       
Number                Biostar_Serial                         "Serial"                              { channel="guntamatic:biostar:mybiostar:serial" }             
String                Biostar_Version                        "Version"                             { channel="guntamatic:biostar:mybiostar:version" }            
Number:Time           Biostar_RunningTime                    "RunningTime"                         { channel="guntamatic:biostar:mybiostar:runningtime" }        
Number:Time           Biostar_ServiceHrs                     "ServiceHrs"                          { channel="guntamatic:biostar:mybiostar:servicehrs" }         
Number:Time           Biostar_Emptyashin                     "Emptyashin"                          { channel="guntamatic:biostar:mybiostar:emptyashin" }         
Number:Temperature    Biostar_Flowis0                        "Flowis0"                             { channel="guntamatic:biostar:mybiostar:flowis0" }            
Number:Temperature    Biostar_Flowis3                        "Flowis3"                             { channel="guntamatic:biostar:mybiostar:flowis3" }            
Number:Temperature    Biostar_Flowis6                        "Flowis6"                             { channel="guntamatic:biostar:mybiostar:flowis6" }            
Number:Volume         Biostar_Fuelcounter                    "Fuelcounter"                         { channel="guntamatic:biostar:mybiostar:fuelcounter" }        
Number:Dimensionless  Biostar_Bufferload                     "Bufferload"                          { channel="guntamatic:biostar:mybiostar:bufferload" }         
Number:Temperature    Biostar_BufferTop0                     "BufferTop0"                          { channel="guntamatic:biostar:mybiostar:buffertop0" }         
Number:Temperature    Biostar_BufferBtm0                     "BufferBtm0"                          { channel="guntamatic:biostar:mybiostar:bufferbtm0" }         
Number:Temperature    Biostar_BufferTop1                     "BufferTop1"                          { channel="guntamatic:biostar:mybiostar:buffertop1" }         
Number:Temperature    Biostar_BufferBtm1                     "BufferBtm1"                          { channel="guntamatic:biostar:mybiostar:bufferbtm1" }         
Number:Temperature    Biostar_BufferTop2                     "BufferTop2"                          { channel="guntamatic:biostar:mybiostar:buffertop2" }         
Number:Temperature    Biostar_BufferBtm2                     "BufferBtm2"                          { channel="guntamatic:biostar:mybiostar:bufferbtm2" }         
Switch                Biostar_BextraWW0                      "BextraWW0"                           { channel="guntamatic:biostar:mybiostar:bextraww0" }          
Switch                Biostar_BextraWW1                      "BextraWW1"                           { channel="guntamatic:biostar:mybiostar:bextraww1" }          
Switch                Biostar_BextraWW2                      "BextraWW2"                           { channel="guntamatic:biostar:mybiostar:bextraww2" }          
Switch                Biostar_Auxiliarypump0                 "Auxiliarypump0"                      { channel="guntamatic:biostar:mybiostar:auxiliarypump0" }     
Switch                Biostar_Auxiliarypump1                 "Auxiliarypump1"                      { channel="guntamatic:biostar:mybiostar:auxiliarypump1" }     
Switch                Biostar_Auxiliarypump2                 "Auxiliarypump2"                      { channel="guntamatic:biostar:mybiostar:auxiliarypump2" }     
String                Biostar_Boilersconditionno             "BoilersConditionNo"                  { channel="guntamatic:biostar:mybiostar:boilersconditionno" } 
Number:Temperature    Biostar_BufferT5                       "BufferT5"                            { channel="guntamatic:biostar:mybiostar:buffert5" }           
Number:Temperature    Biostar_BufferT6                       "BufferT6"                            { channel="guntamatic:biostar:mybiostar:buffert6" }           
Number:Temperature    Biostar_BufferT7                       "BufferT7"                            { channel="guntamatic:biostar:mybiostar:buffert7" }           
Number:Temperature    Biostar_ExtraWW0                       "ExtraWW0"                            { channel="guntamatic:biostar:mybiostar:extraww0" }           
Number:Temperature    Biostar_ExtraWW1                       "ExtraWW1"                            { channel="guntamatic:biostar:mybiostar:extraww1" }           
Number:Temperature    Biostar_ExtraWW2                       "ExtraWW2"                            { channel="guntamatic:biostar:mybiostar:extraww2" }           
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
            Biostar_SetProgram.sendCommand("NORMAL")
        }
        else
        {
            Biostar_SetProgram.sendCommand("OFF")
        }
    }
end
```

## Open - Your feedback is required

- Testing, using other Guntamatic Heating Systems except from Biostar running firmware 3.2d: Please provide feedback!
