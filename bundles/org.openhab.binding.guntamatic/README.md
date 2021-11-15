# Guntamatic Binding

The Guntamatic Binding can be used to monitor and control Guntamatic Heating Systems.

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

t.b.d.

## Open - Your feedback is required

- Testing, using other Guntamatic Heating Systems except from Biostar running firmware 3.2d: Please provide feedback!
