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
|	0	|	Betrieb	|	String	|		|	🔓 W0	|
|	1	|	Aussentemperatur	|	Number:Temperature	|	°C	|	🔓 W0	|
|	2	|	Kesselsolltemp	|	Number:Temperature	|	°C	|	🔐 W1	|
|	3	|	Kesseltemperatur	|	Number:Temperature	|	°C	|	🔓 W0	|
|	4	|	Rauchgasauslastung	|	Number:Dimensionless	|	%	|	🔐 W1	|
|	5	|	Leistung	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	6	|	Ruecklauftemp	|	Number:Temperature	|	°C	|	🔐 W1	|
|	7	|	CO2Soll	|	Number:Dimensionless	|	%	|	🔐 W1	|
|	8	|	CO2Gehalt	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	9	|	RuecklauftempSoll	|	Number:Temperature	|	°C	|	🔐 W1	|
|	10	|	Betriebscode	|	String	|		|	🔐 W1	|
|	11	|	Wirkungsgrad	|	Number:Dimensionless	|	%	|	🔐 W1	|
|	13	|	Saugzuggeblaese	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	14	|	Austragungsgeblaese	|	String	|		|	🔐 W1	|
|	15	|	Austragmotor	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	16	|	G1soll	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	17	|	Pufferoben	|	Number:Temperature	|	°C	|	🔓 W0	|
|	18	|	Puffermitte	|	Number:Temperature	|	°C	|	🔓 W0	|
|	19	|	Pufferunten	|	Number:Temperature	|	°C	|	🔓 W0	|
|	20	|	PumpeHP0	|	String	|		|	🔓 W0	|
|	21	|	Warmwasser0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	22	|	PWarmwasser0	|	String	|		|	🔓 W0	|
|	23	|	Warmwasser1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	24	|	PWarmwasser1	|	String	|		|	🔓 W0	|
|	25	|	Warmwasser2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	26	|	PWarmwasser2	|	String	|		|	🔓 W0	|
|	27	|	RaumtempHK0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	28	|	Heizkreis0	|	String	|		|	🔓 W0	|
|	29	|	RaumtempHK1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	30	|	VorlaufSoll1	|	Number:Temperature	|	°C	|	🔐 W1	|
|	31	|	VorlaufIst1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	32	|	Mischer1	|	String	|		|	🔐 W1	|
|	33	|	Heizkreis1	|	String	|		|	🔐 W1	|
|	34	|	RaumtempHK2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	35	|	VorlaufSoll2	|	Number:Temperature	|	°C	|	🔐 W1	|
|	36	|	VorlaufIst2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	37	|	Mischer2	|	String	|		|	🔐 W1	|
|	38	|	Heizkreis2	|	String	|		|	🔓 W0	|
|	39	|	RaumtempHK3	|	Number:Temperature	|	°C	|	🔓 W0	|
|	40	|	Heizkreis3	|	String	|		|	🔓 W0	|
|	41	|	RaumtempHK4	|	Number:Temperature	|	°C	|	🔓 W0	|
|	42	|	VorlaufSoll4	|	Number:Temperature	|	°C	|	🔐 W1	|
|	43	|	VorlaufIst4	|	Number:Temperature	|	°C	|	🔓 W0	|
|	44	|	Mischer4	|	String	|		|	🔐 W1	|
|	45	|	Heizkreis4	|	String	|		|	🔓 W0	|
|	46	|	RaumtempHK5	|	Number:Temperature	|	°C	|	🔓 W0	|
|	47	|	VorlaufSoll5	|	Number:Temperature	|	°C	|	🔐 W1	|
|	48	|	VorlaufIst5	|	Number:Temperature	|	°C	|	🔓 W0	|
|	49	|	Mischer5	|	String	|		|	🔐 W1	|
|	50	|	Heizkreis5	|	String	|		|	🔓 W0	|
|	51	|	RaumtempHK6	|	Number:Temperature	|	°C	|	🔓 W0	|
|	52	|	Heizkreis6	|	String	|		|	🔓 W0	|
|	53	|	RaumtempHK7	|	Number:Temperature	|	°C	|	🔓 W0	|
|	54	|	VorlaufSoll7	|	Number:Temperature	|	°C	|	🔐 W1	|
|	55	|	VorlaufIst7	|	Number:Temperature	|	°C	|	🔓 W0	|
|	56	|	Mischer7	|	String	|		|	🔐 W1	|
|	57	|	Heizkreis7	|	String	|		|	🔓 W0	|
|	58	|	RaumtempHK8	|	Number:Temperature	|	°C	|	🔓 W0	|
|	59	|	VorlaufSoll8	|	Number:Temperature	|	°C	|	🔐 W1	|
|	60	|	VorlaufIst8	|	Number:Temperature	|	°C	|	🔓 W0	|
|	61	|	Mischer8	|	String	|		|	🔐 W1	|
|	62	|	Heizkreis8	|	String	|		|	🔓 W0	|
|	65	|	Fuellstand	|	String	|		|	🔐 W1	|
|	66	|	STB	|	String	|		|	🔐 W1	|
|	67	|	TKS	|	String	|		|	🔐 W1	|
|	68	|	Kesselfreigabe	|	String	|		|	🔐 W1	|
|	69	|	Programm	|	String	|		|	🔓 W0	|
|	70	|	ProgammHK0	|	String	|		|	🔓 W0	|
|	71	|	ProgammHK1	|	String	|		|	🔓 W0	|
|	72	|	ProgammHK2	|	String	|		|	🔓 W0	|
|	73	|	ProgammHK3	|	String	|		|	🔓 W0	|
|	74	|	ProgammHK4	|	String	|		|	🔓 W0	|
|	75	|	ProgammHK5	|	String	|		|	🔓 W0	|
|	76	|	ProgammHK6	|	String	|		|	🔓 W0	|
|	77	|	ProgammHK7	|	String	|		|	🔓 W0	|
|	78	|	ProgammHK8	|	String	|		|	🔓 W0	|
|	79	|	Stoerung0	|	String	|		|	🔓 W0	|
|	80	|	Stoerung1	|	String	|		|	🔓 W0	|
|	81	|	Serial	|	String	|		|	🔓 W0	|
|	82	|	Version	|	String	|		|	🔓 W0	|
|	83	|	Betriebszeit	|	Number:Time	|	h	|	🔓 W0	|
|	84	|	Servicezeit	|	Number:Time	|	d	|	🔓 W0	|
|	85	|	Ascheleerenin	|	Number:Time	|	h	|	🔓 W0	|
|	86	|	VorlaufIst0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	87	|	VorlaufIst3	|	Number:Temperature	|	°C	|	🔓 W0	|
|	88	|	VorlaufIst6	|	Number:Temperature	|	°C	|	🔓 W0	|
|	89	|	Brennstoffzaehler	|	Number:Volume	|	m³	|	🔐 W1	|
|	90	|	Pufferladung	|	Number:Dimensionless	|	%	|	🔓 W0	|
|	91	|	Pufferoben0	|	Number:Temperature	|	°C	|	🔐 W1	|
|	92	|	Pufferunten0	|	Number:Temperature	|	°C	|	🔐 W1	|
|	93	|	Pufferoben1	|	Number:Temperature	|	°C	|	🔐 W1	|
|	94	|	Pufferunten1	|	Number:Temperature	|	°C	|	🔐 W1	|
|	95	|	Pufferoben2	|	Number:Temperature	|	°C	|	🔐 W1	|
|	96	|	Pufferunten2	|	Number:Temperature	|	°C	|	🔐 W1	|
|	97	|	PZusatzwarmw0	|	String	|		|	🔐 W1	|
|	98	|	PZusatzwarmw1	|	String	|		|	🔐 W1	|
|	99	|	PZusatzwarmw2	|	String	|		|	🔐 W1	|
|	100	|	Fernpumpe0	|	String	|		|	🔐 W1	|
|	101	|	Fernpumpe1	|	String	|		|	🔐 W1	|
|	102	|	Fernpumpe2	|	String	|		|	🔐 W1	|
|	104	|	Kesselzustand-Nr	|	String	|		|	🔐 W1	|
|	108	|	PufferT5	|	Number:Temperature	|	°C	|	🔓 W0	|
|	109	|	PufferT6	|	Number:Temperature	|	°C	|	🔓 W0	|
|	110	|	PufferT7	|	Number:Temperature	|	°C	|	🔓 W0	|
|	111	|	Zusatzwarmw0	|	Number:Temperature	|	°C	|	🔓 W0	|
|	112	|	Zusatzwarmw1	|	Number:Temperature	|	°C	|	🔓 W0	|
|	113	|	Zusatzwarmw2	|	Number:Temperature	|	°C	|	🔓 W0	|
|	114	|	Rost	|	Number:Dimensionless	|	%	|	🔓 W0	|

Security Access Level: 🔓 W0 ... Open, 🔐 W1 ...End Customer Key, 🔒 W2 ... Service Partner

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