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

|	Channel             |	Type	|	Unit	|	Security Access Level	| ReadOnly | Advanced |
|-----------------------|-----------|-----------|---------------------------|----------|----------|
|	setboilerapproval	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setprogram	        |	String	|		|	🔐 W1	|	R/W	|	false	|
|	setheatcircprogram0	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram1	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram2	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram3	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram4	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram5	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram6	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram7	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setheatcircprogram8	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setwwheat0	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setwwheat1	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setwwheat2	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setextrawwheat0	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setextrawwheat1	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	setextrawwheat2	|	String	|		|	🔐 W1	|	R/W	|	true	|
|	running	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	outsidetemp	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	blrtargettemp	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	boilertemperature	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	fluegasutilisation	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	output	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	returntemp	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	co2target	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	c02content	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	returntemptarget	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	statuscode	|	Number	|		|	🔐 W1	|	R/O	|	false	|
|	efficiency	|	Number:Dimensionless	|	%	|	🔐 W1	|	R/O	|	false	|
|	extractorsystem	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	feedturbine	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	dischargemotor	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	g1target	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	buffertop	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	buffermid	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bufferbtm	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	pumphp0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bdhw0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bdhw1	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	dhw2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	bdhw2	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatcirc0	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer1	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	roomtemphc2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer2	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc2	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc3	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatcirc3	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc4	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget4	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis4	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer4	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc4	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget5	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer5	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc5	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	heatcirc6	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget7	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer7	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc7	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	roomtemphc8	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowtarget8	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	flowis8	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	mixer8	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	heatcirc8	|	Switch	|		|	🔓 W0	|	R/O	|	false	|
|	fuellevel	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	stb	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	tks	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	boilerapproval	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	programme	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc0	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc1	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc2	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc3	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc4	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc5	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc6	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc7	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	programhc8	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	interuption0	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	interuption1	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	serial	|	Number	|		|	🔓 W0	|	R/O	|	false	|
|	version	|	String	|		|	🔓 W0	|	R/O	|	false	|
|	runningtime	|	Number:Time	|	h	|	🔓 W0	|	R/O	|	false	|
|	servicehrs	|	Number:Time	|	d	|	🔓 W0	|	R/O	|	false	|
|	emptyashin	|	Number:Time	|	h	|	🔓 W0	|	R/O	|	false	|
|	flowis0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowis3	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	flowis6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	fuelcounter	|	Number:Volume	|	m³	|	🔐 W1	|	R/O	|	false	|
|	bufferload	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|
|	buffertop0	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferbtm0	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	buffertop1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferbtm1	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	buffertop2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bufferbtm2	|	Number:Temperature	|	°C	|	🔐 W1	|	R/O	|	false	|
|	bextraww0	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	bextraww1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	bextraww2	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliarypump0	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliarypump1	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	auxiliarypump2	|	Switch	|		|	🔐 W1	|	R/O	|	false	|
|	boilersconditionno	|	String	|		|	🔐 W1	|	R/O	|	false	|
|	buffert5	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	buffert6	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	buffert7	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraww0	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraww1	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	extraww2	|	Number:Temperature	|	°C	|	🔓 W0	|	R/O	|	false	|
|	grate	|	Number:Dimensionless	|	%	|	🔓 W0	|	R/O	|	false	|


Security Access Levels:

- 🔓 W0 ... Open
- 🔐 W1 ... End Customer Key
- 🔒 W2 ... Service Partner

## Full Example

t.b.d.

## Open - Your feedback is required

- Testing, using other Guntamatic Heating Systems except from Biostar running firmware 3.2d: Please provide feedback!
