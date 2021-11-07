# Guntamatic Binding

The Guntamatic Binding can be used to monitor and control Guntamatic Heating Systems.
The Binding is currently under development.
Please give it a try and report issues in order to improve the usablity as well as the number of supported Guntamatic Heating Systems as well as channels.

## Supported Things

The Guntamatic Binding was developed and tested using Guntamatic Biostar 15kW Pellets Heating System running Firmware 3.2d.
It should work for other Guntamatic Heating Systems as well, that support the same web interface:
- Powerchip ?
- Powercorn ?
- Biocom ?
- Pro ?
- Therm ?
- Biostar

Right now the binding supports the monitoring of the pre-defined channels from below. It is planned to use dynamically generated channels, based on the info provided from the actual Guntamatic Heating System.

Control of the Guntamatic Heating System is technical possbile but not yet implemented.

## Thing Configuration

| Parameter     | Description                                                               | Default       |
|---------------|---------------------------------------------------------------------------|---------------|
| Hostname      | Hostname or IP address of the Guntamatic Heating System                   |               |
| Key           | Optional, but required to read protected parameters and to control the Guntamatic Heating System. The key needs to be reqested from Guntamatic support, e.g. via https://www.guntamatic.com/en/contact/                                    |               |
| Refresh Interval    | Interval the Guntamatic Heating System is polled in seconds.        | 60            |
| Encoding      | Code page used by the Guntamatic Heating System.                          | windows-1252  |

## Channels

Right now the following channels are supported:

|	ID	|	Channel	|	Type	|	Unit	|	Security Access Level	|
|-----------------------		|--------------------		|-------------------------------		|-------------		|------------------------------		|
|	0	|	Betrieb	|	String	|		|	W0 (open)	|
|	1	|	Aussentemperatur	|	Number:Temperature	|	°C	|	W0 (open)	|
|	2	|	Kesselsolltemp	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	3	|	Kesseltemperatur	|	Number:Temperature	|	°C	|	W0 (open)	|
|	4	|	Rauchgasauslastung	|	Number:Dimensionless	|	%	|	W1 (End Customer Key)	|
|	5	|	Leistung	|	Number:Dimensionless	|	%	|	W0 (open)	|
|	6	|	Ruecklauftemp	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	7	|	CO2Soll	|	Number:Dimensionless	|	%	|	W1 (End Customer Key)	|
|	8	|	CO2Gehalt	|	Number:Dimensionless	|	%	|	W0 (open)	|
|	9	|	RuecklauftempSoll	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	10	|	Betriebscode	|	String	|		|	W1 (End Customer Key)	|
|	11	|	Wirkungsgrad	|	Number:Dimensionless	|	%	|	W1 (End Customer Key)	|
|	13	|	Saugzuggeblaese	|	Number:Dimensionless	|	%	|	W0 (open)	|
|	14	|	Austragungsgeblaese	|	String	|		|	W1 (End Customer Key)	|
|	15	|	Austragmotor	|	Number:Dimensionless	|	%	|	W0 (open)	|
|	16	|	G1soll	|	Number:Dimensionless	|	%	|	W0 (open)	|
|	17	|	Pufferoben	|	Number:Temperature	|	°C	|	W0 (open)	|
|	18	|	Puffermitte	|	Number:Temperature	|	°C	|	W0 (open)	|
|	19	|	Pufferunten	|	Number:Temperature	|	°C	|	W0 (open)	|
|	20	|	PumpeHP0	|	String	|		|	W0 (open)	|
|	21	|	Warmwasser0	|	Number:Temperature	|	°C	|	W0 (open)	|
|	22	|	PWarmwasser0	|	String	|		|	W0 (open)	|
|	23	|	Warmwasser1	|	Number:Temperature	|	°C	|	W0 (open)	|
|	24	|	PWarmwasser1	|	String	|		|	W0 (open)	|
|	25	|	Warmwasser2	|	Number:Temperature	|	°C	|	W0 (open)	|
|	26	|	PWarmwasser2	|	String	|		|	W0 (open)	|
|	27	|	RaumtempHK0	|	Number:Temperature	|	°C	|	W0 (open)	|
|	28	|	Heizkreis0	|	String	|		|	W0 (open)	|
|	29	|	RaumtempHK1	|	Number:Temperature	|	°C	|	W0 (open)	|
|	30	|	VorlaufSoll1	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	31	|	VorlaufIst1	|	Number:Temperature	|	°C	|	W0 (open)	|
|	32	|	Mischer1	|	String	|		|	W1 (End Customer Key)	|
|	33	|	Heizkreis1	|	String	|		|	W1 (End Customer Key)	|
|	34	|	RaumtempHK2	|	Number:Temperature	|	°C	|	W0 (open)	|
|	35	|	VorlaufSoll2	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	36	|	VorlaufIst2	|	Number:Temperature	|	°C	|	W0 (open)	|
|	37	|	Mischer2	|	String	|		|	W1 (End Customer Key)	|
|	38	|	Heizkreis2	|	String	|		|	W0 (open)	|
|	39	|	RaumtempHK3	|	Number:Temperature	|	°C	|	W0 (open)	|
|	40	|	Heizkreis3	|	String	|		|	W0 (open)	|
|	41	|	RaumtempHK4	|	Number:Temperature	|	°C	|	W0 (open)	|
|	42	|	VorlaufSoll4	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	43	|	VorlaufIst4	|	Number:Temperature	|	°C	|	W0 (open)	|
|	44	|	Mischer4	|	String	|		|	W1 (End Customer Key)	|
|	45	|	Heizkreis4	|	String	|		|	W0 (open)	|
|	46	|	RaumtempHK5	|	Number:Temperature	|	°C	|	W0 (open)	|
|	47	|	VorlaufSoll5	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	48	|	VorlaufIst5	|	Number:Temperature	|	°C	|	W0 (open)	|
|	49	|	Mischer5	|	String	|		|	W1 (End Customer Key)	|
|	50	|	Heizkreis5	|	String	|		|	W0 (open)	|
|	51	|	RaumtempHK6	|	Number:Temperature	|	°C	|	W0 (open)	|
|	52	|	Heizkreis6	|	String	|		|	W0 (open)	|
|	53	|	RaumtempHK7	|	Number:Temperature	|	°C	|	W0 (open)	|
|	54	|	VorlaufSoll7	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	55	|	VorlaufIst7	|	Number:Temperature	|	°C	|	W0 (open)	|
|	56	|	Mischer7	|	String	|		|	W1 (End Customer Key)	|
|	57	|	Heizkreis7	|	String	|		|	W0 (open)	|
|	58	|	RaumtempHK8	|	Number:Temperature	|	°C	|	W0 (open)	|
|	59	|	VorlaufSoll8	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	60	|	VorlaufIst8	|	Number:Temperature	|	°C	|	W0 (open)	|
|	61	|	Mischer8	|	String	|		|	W1 (End Customer Key)	|
|	62	|	Heizkreis8	|	String	|		|	W0 (open)	|
|	65	|	Fuellstand	|	String	|		|	W1 (End Customer Key)	|
|	66	|	STB	|	String	|		|	W1 (End Customer Key)	|
|	67	|	TKS	|	String	|		|	W1 (End Customer Key)	|
|	68	|	Kesselfreigabe	|	String	|		|	W1 (End Customer Key)	|
|	69	|	Programm	|	String	|		|	W0 (open)	|
|	70	|	ProgammHK0	|	String	|		|	W0 (open)	|
|	71	|	ProgammHK1	|	String	|		|	W0 (open)	|
|	72	|	ProgammHK2	|	String	|		|	W0 (open)	|
|	73	|	ProgammHK3	|	String	|		|	W0 (open)	|
|	74	|	ProgammHK4	|	String	|		|	W0 (open)	|
|	75	|	ProgammHK5	|	String	|		|	W0 (open)	|
|	76	|	ProgammHK6	|	String	|		|	W0 (open)	|
|	77	|	ProgammHK7	|	String	|		|	W0 (open)	|
|	78	|	ProgammHK8	|	String	|		|	W0 (open)	|
|	79	|	Stoerung0	|	String	|		|	W0 (open)	|
|	80	|	Stoerung1	|	String	|		|	W0 (open)	|
|	81	|	Serial	|	String	|		|	W0 (open)	|
|	82	|	Version	|	String	|		|	W0 (open)	|
|	83	|	Betriebszeit	|	Number:Time	|	h	|	W0 (open)	|
|	84	|	Servicezeit	|	Number:Time	|	d	|	W0 (open)	|
|	85	|	Ascheleerenin	|	Number:Time	|	h	|	W0 (open)	|
|	86	|	VorlaufIst0	|	Number:Temperature	|	°C	|	W0 (open)	|
|	87	|	VorlaufIst3	|	Number:Temperature	|	°C	|	W0 (open)	|
|	88	|	VorlaufIst6	|	Number:Temperature	|	°C	|	W0 (open)	|
|	89	|	Brennstoffzaehler	|	Number:Volume	|	m³	|	W1 (End Customer Key)	|
|	90	|	Pufferladung	|	Number:Dimensionless	|	%	|	W0 (open)	|
|	91	|	Pufferoben0	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	92	|	Pufferunten0	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	93	|	Pufferoben1	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	94	|	Pufferunten1	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	95	|	Pufferoben2	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	96	|	Pufferunten2	|	Number:Temperature	|	°C	|	W1 (End Customer Key)	|
|	97	|	PZusatzwarmw0	|	String	|		|	W1 (End Customer Key)	|
|	98	|	PZusatzwarmw1	|	String	|		|	W1 (End Customer Key)	|
|	99	|	PZusatzwarmw2	|	String	|		|	W1 (End Customer Key)	|
|	100	|	Fernpumpe0	|	String	|		|	W1 (End Customer Key)	|
|	101	|	Fernpumpe1	|	String	|		|	W1 (End Customer Key)	|
|	102	|	Fernpumpe2	|	String	|		|	W1 (End Customer Key)	|
|	104	|	Kesselzustand-Nr	|	String	|		|	W1 (End Customer Key)	|
|	108	|	PufferT5	|	Number:Temperature	|	°C	|	W0 (open)	|
|	109	|	PufferT6	|	Number:Temperature	|	°C	|	W0 (open)	|
|	110	|	PufferT7	|	Number:Temperature	|	°C	|	W0 (open)	|
|	111	|	Zusatzwarmw0	|	Number:Temperature	|	°C	|	W0 (open)	|
|	112	|	Zusatzwarmw1	|	Number:Temperature	|	°C	|	W0 (open)	|
|	113	|	Zusatzwarmw2	|	Number:Temperature	|	°C	|	W0 (open)	|
|	114	|	Rost	|	Number:Dimensionless	|	%	|	W0 (open)	|

The names of the channels are derived from the data provided from the Guntamatic Heating System. Sorry for the German (Austrian ;-)).

The binding writes the number of channels that are detected during the initialization into the logs.
Channels provided by the actual Guntamatic Heating System variant, but not supported by the binding, are logged aswell.
Please share your Guntamatic Heating System variant, firmware version as well as the logs (supported and unsupported channels) in order to improve the binding.

## Full Example

t.b.d.

## TODOs

- Improve channels: data types (e.g. `Switch` instead of `String`), state descriptions, ... 
- Dynamically generated channels, based on data from Guntamatic Heating System. English Guntamatic Heating Systems should automatically generate English channels in that case
- Control of the Guntamatic Heating System
- Testing with / Support of other Guntamatic Heating Systems