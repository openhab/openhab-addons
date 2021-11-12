# Guntamatic Binding

The Guntamatic Binding can be used to monitor and control Guntamatic Heating Systems.
The Binding is currently under development.
Please give it a try and report issues in order to improve the usablity as well as the number of supported Guntamatic Heating Systems.

## Supported Things

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System running Firmware 3.2d.
It should work for all other Guntamatic Heating Systems as well, that support the same web interface:

- Biostar (tested via 15kW, firmware 3.2d)
- Powerchip (untested)
- Powercorn (untested)
- Biocom (untested)
- Pro (untested)
- Therm (untested)

All the different languages that can be configured in the Guntamatic Heating System are supported.

Control of the Guntamatic Heating System is technical possible but not yet implemented.

## Thing Configuration

| Parameter     | Description                                                               | Default       |
|---------------|---------------------------------------------------------------------------|---------------|
| Hostname      | Hostname or IP address of the Guntamatic Heating System                   |               |
| Key           | Optional, but required to read protected parameters and to control the Guntamatic Heating System. The key needs to be reqested from Guntamatic support, e.g. via https://www.guntamatic.com/en/contact/                                    |               |
| Refresh Interval    | Interval the Guntamatic Heating System is polled in seconds.        | 60            |
| Encoding      | Code page used by the Guntamatic Heating System.                          | windows-1252  |

## Channels

The Binding dynamically generates Channels, derived from the data provided from the actual Guntamatic Heating System.

Example list of Channels using a Guntamatic Biostar 15kW Pellets Heating System running firmware 3.2d and language configured to English:

|	ID	|	Channel	|	Type	|	Unit	|	Security Access Level	|
|-----------------------		|--------------------		|-------------------------------		|-------------		|------------------------------		|
|	0	|	running	|	String	|		|	🔓 W0	|
|	1	|	outsidetemp	|	Number:Temperature	|	°C	|	🔓 W0	|
|	2	|	blrtargettemp	|	Number:Temperature	|	°C	|	🔐 W1	|
|	3	|	boilertemperature	|	Number:Tempera	|	°C	|	🔓 W0	|
|	4	|	fluegasutilisation	|	Number:Dimens	|	%	|	🔐 W1	|
|	5	|	output	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	6	|	returntemp	|	Number:Temperature	|	°C	|	🔐 W1	|
|	7	|	co2target	|	Number:Dimensionless	|	%	|	🔐 W1	|
|	8	|	c02content	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	9	|	returntemptarget	|	Number:Temperat	|	°C	|	🔐 W1	|
|	10	|	statuscode	|	Number	|		|	🔐 W1	|
|	11	|	efficiency	|	Number:Dimensionless	|	%	|	🔐 W1	|
|	13	|	extractorsystem	|	Number:Dimension	|	%	|	🔓 W0	|
|	14	|	feedturbine	|	String	|		|	🔐 W1	|
|	15	|	dischargemotor	|	Number:Dimensionl	|	%	|	🔓 W0	|
|	16	|	g1target	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	17	|	buffertop	|	Number:Temperature	|	°C	|	🔓 W0	|
|	18	|	buffermid	|	Number:Temperature	|	°C	|	🔓 W0	|
|	19	|	bufferbtm	|	Number:Temperature	|	°C	|	🔓 W0	|
|	20	|	pumphp0	|	Switch	|		|	🔓 W0	|
|	21	|	dhw0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	22	|	bdhw0	|	Switch	|		|	🔓 W0	|
|	23	|	dhw1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	24	|	bdhw1	|	Switch	|		|	🔓 W0	|
|	25	|	dhw2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	26	|	bdhw2	|	Switch	|		|	🔓 W0	|
|	27	|	roomtemphc0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	28	|	heatcirc0	|	Switch	|		|	🔓 W0	|
|	29	|	roomtemphc1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	30	|	flowtarget1	|	Number:Temperature	|	°C	|	🔐 W1	|
|	31	|	flowis1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	32	|	mixer1	|	String	|		|	🔐 W1	|
|	33	|	heatcirc1	|	Switch	|		|	🔐 W1	|
|	34	|	roomtemphc2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	35	|	flowtarget2	|	Number:Temperature	|	°C	|	🔐 W1	|
|	36	|	flowis2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	37	|	mixer2	|	String	|		|	🔐 W1	|
|	38	|	heatcirc2	|	Switch	|		|	🔓 W0	|
|	39	|	roomtemphc3	|	Number:Temperature	|	°C	|	🔓 W0	|
|	40	|	heatcirc3	|	Switch	|		|	🔓 W0	|
|	41	|	roomtemphc4	|	Number:Temperature	|	°C	|	🔓 W0	|
|	42	|	flowtarget4	|	Number:Temperature	|	°C	|	🔐 W1	|
|	43	|	flowis4	|	Number:Temperature	|	°C	|	🔓 W0	|
|	44	|	mixer4	|	String	|		|	🔐 W1	|
|	45	|	heatcirc4	|	Switch	|		|	🔓 W0	|
|	46	|	roomtemphc5	|	Number:Temperature	|	°C	|	🔓 W0	|
|	47	|	flowtarget5	|	Number:Temperature	|	°C	|	🔐 W1	|
|	48	|	flowis5	|	Number:Temperature	|	°C	|	🔓 W0	|
|	49	|	mixer5	|	String	|		|	🔐 W1	|
|	50	|	heatcirc5	|	Switch	|		|	🔓 W0	|
|	51	|	roomtemphc6	|	Number:Temperature	|	°C	|	🔓 W0	|
|	52	|	heatcirc6	|	Switch	|		|	🔓 W0	|
|	53	|	roomtemphc7	|	Number:Temperature	|	°C	|	🔓 W0	|
|	54	|	flowtarget7	|	Number:Temperature	|	°C	|	🔐 W1	|
|	55	|	flowis7	|	Number:Temperature	|	°C	|	🔓 W0	|
|	56	|	mixer7	|	String	|		|	🔐 W1	|
|	57	|	heatcirc7	|	Switch	|		|	🔓 W0	|
|	58	|	roomtemphc8	|	Number:Temperature	|	°C	|	🔓 W0	|
|	59	|	flowtarget8	|	Number:Temperature	|	°C	|	🔐 W1	|
|	60	|	flowis8	|	Number:Temperature	|	°C	|	🔓 W0	|
|	61	|	mixer8	|	String	|		|	🔐 W1	|
|	62	|	heatcirc8	|	Switch	|		|	🔓 W0	|
|	65	|	fuellevel	|	String	|		|	🔐 W1	|
|	66	|	stb	|	String	|		|	🔐 W1	|
|	67	|	tks	|	String	|		|	🔐 W1	|
|	68	|	boilerapproval	|	Switch	|		|	🔐 W1	|
|	69	|	programme	|	String	|		|	🔓 W0	|
|	70	|	programhc0	|	String	|		|	🔓 W0	|
|	71	|	programhc1	|	String	|		|	🔓 W0	|
|	72	|	programhc2	|	String	|		|	🔓 W0	|
|	73	|	programhc3	|	String	|		|	🔓 W0	|
|	74	|	programhc4	|	String	|		|	🔓 W0	|
|	75	|	programhc5	|	String	|		|	🔓 W0	|
|	76	|	programhc6	|	String	|		|	🔓 W0	|
|	77	|	programhc7	|	String	|		|	🔓 W0	|
|	78	|	programhc8	|	String	|		|	🔓 W0	|
|	79	|	interuption0	|	String	|		|	🔓 W0	|
|	80	|	interuption1	|	String	|		|	🔓 W0	|
|	81	|	serial	|	Number	|		|	🔓 W0	|
|	82	|	version	|	String	|		|	🔓 W0	|
|	83	|	runningtime	|	Number:Time	|	h	|	🔓 W0	|
|	84	|	servicehrs	|	Number:Time	|	d	|	🔓 W0	|
|	85	|	emptyashin	|	Number:Time	|	h	|	🔓 W0	|
|	86	|	flowis0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	87	|	flowis3	|	Number:Temperature	|	°C	|	🔓 W0	|
|	88	|	flowis6	|	Number:Temperature	|	°C	|	🔓 W0	|
|	89	|	fuelcounter	|	Number:Volume	|	m³	|	🔐 W1	|
|	90	|	bufferload	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	91	|	buffertop0	|	Number:Temperature	|	°C	|	🔐 W1	|
|	92	|	bufferbtm0	|	Number:Temperature	|	°C	|	🔐 W1	|
|	93	|	buffertop1	|	Number:Temperature	|	°C	|	🔐 W1	|
|	94	|	bufferbtm1	|	Number:Temperature	|	°C	|	🔐 W1	|
|	95	|	buffertop2	|	Number:Temperature	|	°C	|	🔐 W1	|
|	96	|	bufferbtm2	|	Number:Temperature	|	°C	|	🔐 W1	|
|	97	|	bextraww0	|	Switch	|		|	🔐 W1	|
|	98	|	bextraww1	|	Switch	|		|	🔐 W1	|
|	99	|	bextraww2	|	Switch	|		|	🔐 W1	|
|	100	|	auxiliarypump0	|	Switch	|		|	🔐 W1	|
|	101	|	auxiliarypump1	|	Switch	|		|	🔐 W1	|
|	102	|	auxiliarypump2	|	Switch	|		|	🔐 W1	|
|	104	|	boilersconditionno	|	String	|		|	🔐 W1	|
|	108	|	buffert5	|	Number:Temperature	|	°C	|	🔓 W0	|
|	109	|	buffert6	|	Number:Temperature	|	°C	|	🔓 W0	|
|	110	|	buffert7	|	Number:Temperature	|	°C	|	🔓 W0	|
|	111	|	extraww0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	112	|	extraww1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	113	|	extraww2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	114	|	grate	|	Number:Dimensionless	|	%	|	🔓 W0	|

Security Access Levels:

- 🔓 W0 ... Open
- 🔐 W1 ... End Customer Key
- 🔒 W2 ... Service Partner

# The binding writes the number of channels that are detected during the initialization into the logs.
# Channels provided by the actual Guntamatic Heating System variant, but not supported by the binding, are logged aswell.
# Please share your Guntamatic Heating System variant, firmware version as well as the logs (supported and unsupported channels) in order to improve the binding.

## Full Example

t.b.d.

## TODOs

- Control of the Guntamatic Heating System
- Testing with / Support of other Guntamatic Heating Systems except from Biostar running firmware 3.2d
