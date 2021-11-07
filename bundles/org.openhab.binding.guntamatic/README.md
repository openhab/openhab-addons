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

Right now the binding supports the monitoring of the pre-defined channels from below. It is planned to use dynamic generated channels, based on the info provided from the actual Guntamatic Heating Systems.

Control of the Guntamatic Heating Systems is possbile but not yet implemented.

## Thing Configuration

| Parameter  | Description                  | Default |
|------------|---------------------------------------|--|
| Hostname    | Hostname or IP address of the Guntamatic Heating System  | |
| Key         | Optional, but required to read protected parameters and to control the Guntamatic Heating System. The key needs to be reqested from Guntamatic e.g. via https://www.guntamatic.com/en/contact/ | |
| Refresh Interval    | Interval the Guntamatic Heating System is polled in sec. | 60 |
| Encoding    | Encoding of the Response from the Heating System  | windows-1252 |

## Channels

Right now all the following channels are supported:

ID	|	Channel	|	Unit	|	Type	|
|---|-----------|-----------|-----------|
|	0	|	Betrieb	|		|	String	|
|	1	|	Aussentemperatur	|	°C	|	Number:Temperature	|
|	2	|	Kesselsolltemp	|	°C	|	Number:Temperature	|
|	3	|	Kesseltemperatur	|	°C	|	Number:Temperature	|
|	4	|	Rauchgasauslastung	|	%	|	Number:Dimensionless	|
|	5	|	Leistung	|	%	|	Number:Dimensionless	|
|	6	|	Ruecklauftemp	|	°C	|	Number:Temperature	|
|	7	|	CO2Soll	|	%	|	Number:Dimensionless	|
|	8	|	CO2Gehalt	|	%	|	Number:Dimensionless	|
|	9	|	RuecklauftempSoll	|	°C	|	Number:Temperature	|
|	10	|	Betriebscode	|		|	String	|
|	11	|	Wirkungsgrad	|	%	|	Number:Dimensionless	|
|	12	|	Leistung	|	%	|	Number:Dimensionless	|
|	13	|	Saugzuggeblaese	|	%	|	Number:Dimensionless	|
|	14	|	Austragungsgeblaese	|		|	String	|
|	15	|	Austragmotor	|	%	|	Number:Dimensionless	|
|	16	|	G1soll	|	%	|	Number:Dimensionless	|
|	17	|	Pufferoben	|	°C	|	Number:Temperature	|
|	18	|	Puffermitte	|	°C	|	Number:Temperature	|
|	19	|	Pufferunten	|	°C	|	Number:Temperature	|
|	20	|	PumpeHP0	|		|	String	|
|	21	|	Warmwasser0	|	°C	|	Number:Temperature	|
|	22	|	PWarmwasser0	|		|	String	|
|	23	|	Warmwasser1	|	°C	|	Number:Temperature	|
|	24	|	PWarmwasser1	|		|	String	|
|	25	|	Warmwasser2	|	°C	|	Number:Temperature	|
|	26	|	PWarmwasser2	|		|	String	|
|	27	|	RaumtempHK0	|	°C	|	Number:Temperature	|
|	28	|	Heizkreis0	|		|	String	|
|	29	|	RaumtempHK1	|	°C	|	Number:Temperature	|
|	30	|	VorlaufSoll1	|	°C	|	Number:Temperature	|
|	31	|	VorlaufIst1	|	°C	|	Number:Temperature	|
|	32	|	Mischer1	|		|	String	|
|	33	|	Heizkreis1	|		|	String	|
|	34	|	RaumtempHK2	|	°C	|	Number:Temperature	|
|	35	|	VorlaufSoll2	|	°C	|	Number:Temperature	|
|	36	|	VorlaufIst2	|	°C	|	Number:Temperature	|
|	37	|	Mischer2	|		|	String	|
|	38	|	Heizkreis2	|		|	String	|
|	39	|	RaumtempHK3	|	°C	|	Number:Temperature	|
|	40	|	Heizkreis3	|		|	String	|
|	41	|	RaumtempHK4	|	°C	|	Number:Temperature	|
|	42	|	VorlaufSoll4	|	°C	|	Number:Temperature	|
|	43	|	VorlaufIst4	|	°C	|	Number:Temperature	|
|	44	|	Mischer4	|		|	String	|
|	45	|	Heizkreis4	|		|	String	|
|	46	|	RaumtempHK5	|	°C	|	Number:Temperature	|
|	47	|	VorlaufSoll5	|	°C	|	Number:Temperature	|
|	48	|	VorlaufIst5	|	°C	|	Number:Temperature	|
|	49	|	Mischer5	|		|	String	|
|	50	|	Heizkreis5	|		|	String	|
|	51	|	RaumtempHK6	|	°C	|	Number:Temperature	|
|	52	|	Heizkreis6	|		|	String	|
|	53	|	RaumtempHK7	|	°C	|	Number:Temperature	|
|	54	|	VorlaufSoll7	|	°C	|	Number:Temperature	|
|	55	|	VorlaufIst7	|	°C	|	Number:Temperature	|
|	56	|	Mischer7	|		|	String	|
|	57	|	Heizkreis7	|		|	String	|
|	58	|	RaumtempHK8	|	°C	|	Number:Temperature	|
|	59	|	VorlaufSoll8	|	°C	|	Number:Temperature	|
|	60	|	VorlaufIst8	|	°C	|	Number:Temperature	|
|	61	|	Mischer8	|		|	String	|
|	62	|	Heizkreis8	|		|	String	|
|	65	|	Fuellstand	|		|	String	|
|	66	|	STB	|		|	String	|
|	67	|	TKS	|		|	String	|
|	68	|	Kesselfreigabe	|		|	String	|
|	69	|	Programm	|		|	String	|
|	70	|	ProgammHK0	|		|	String	|
|	71	|	ProgammHK1	|		|	String	|
|	72	|	ProgammHK2	|		|	String	|
|	73	|	ProgammHK3	|		|	String	|
|	74	|	ProgammHK4	|		|	String	|
|	75	|	ProgammHK5	|		|	String	|
|	76	|	ProgammHK6	|		|	String	|
|	77	|	ProgammHK7	|		|	String	|
|	78	|	ProgammHK8	|		|	String	|
|	79	|	Stoerung0	|		|	String	|
|	80	|	Stoerung1	|		|	String	|
|	81	|	Serial	|		|	String	|
|	82	|	Version	|		|	String	|
|	83	|	Betriebszeit	|	h	|	Number:Time	|
|	84	|	Servicezeit	|	d	|	Number:Time	|
|	85	|	Ascheleerenin	|	h	|	Number:Time	|
|	86	|	VorlaufIst0	|	°C	|	Number:Temperature	|
|	87	|	VorlaufIst3	|	°C	|	Number:Temperature	|
|	88	|	VorlaufIst6	|	°C	|	Number:Temperature	|
|	89	|	Brennstoffzaehler	|	m³	|	Number:Volume	|
|	90	|	Pufferladung	|	%	|	Number:Dimensionless	|
|	91	|	Pufferoben0	|	°C	|	Number:Temperature	|
|	92	|	Pufferunten0	|	°C	|	Number:Temperature	|
|	93	|	Pufferoben1	|	°C	|	Number:Temperature	|
|	94	|	Pufferunten1	|	°C	|	Number:Temperature	|
|	95	|	Pufferoben2	|	°C	|	Number:Temperature	|
|	96	|	Pufferunten2	|	°C	|	Number:Temperature	|
|	97	|	PZusatzwarmw0	|		|	String	|
|	98	|	PZusatzwarmw1	|		|	String	|
|	99	|	PZusatzwarmw2	|		|	String	|
|	100	|	Fernpumpe0	|		|	String	|
|	101	|	Fernpumpe1	|		|	String	|
|	102	|	Fernpumpe2	|		|	String	|
|	104	|	Kesselzustand-Nr	|		|	String	|
|	108	|	PufferT5	|	°C	|	Number:Temperature	|
|	109	|	PufferT6	|	°C	|	Number:Temperature	|
|	110	|	PufferT7	|	°C	|	Number:Temperature	|
|	111	|	Zusatzwarmw0	|	°C	|	Number:Temperature	|
|	112	|	Zusatzwarmw1	|	°C	|	Number:Temperature	|
|	113	|	Zusatzwarmw2	|	°C	|	Number:Temperature	|
|	114	|	Rost	|	%	|	Number:Dimensionless	|

The binding writes the number of channels that are detected during the initialization into the logs.
Channels provided by the actual Guntamatic Heating System variant, but not supported by the binding, are logged aswell.
Please share your Guntamatic Heating System variant, firmware version as well as the logs (supported and unsupported channels) in order to improve the binding.

## Full Example

t.b.d.

## Any custom content here!

t.b.d.
