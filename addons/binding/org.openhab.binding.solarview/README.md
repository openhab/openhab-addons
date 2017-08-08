
# Solarview Binding

This binding is for users coming from the photovoltaics monitoring-system "Solarview". This measurement and monitoring
environment provides access to a lot of different energy monitoring devices like inverters and meter readers.

For details about the feature, see the following website ![Solarview](http://www.solarview.info) 

The binding is fairly complete and supports the following monitoing functionalities.

* energy,
* power,
* voltage and
* temperature

at different levels/devices.


## Binding Configuration

The binding can be configured in the file `services/solarview.cfg`.

| Property     | Default   | Required | Description                                        |
|--------------|-----------|:--------:|----------------------------------------------------|
| hostname     | localhost |    No    | Hostname or IP address of the solarview system     |
| tcpPort      | 15000     |    No    | TCP Port number of the SAP of the Solarview system |
| timeoutMsecs | 60        |    No    | TCP Connection timeout in milliseconds             |


## Supported Things

The Solarview system provides an umbrella view onto the energy flow of a complete domestic home.
Therefore three main views exist:
* the energy production (i.e. the photovolatic system),
* the energy import (i.e. any demand not locally satisfied by the production system),
* the energy injection (i.e. any demand not locally needed within the domestic home),

In addition there are nine more production-specific views onto the photovolatic system with

* productionInverterOne,
* productionInverterTwo,
* productionInverterThree,
* productionInverterFour,
* productionInverterFive,
* productionInverterSix,
* productionInverterSeven,
* productionInverterEight,
* productionInverterNine.

## Discovery

Unfortunatelly there is no way to discover the Solarview system in the local network. Beware that all Solarview monitoring sources have to be added to the local Solarview installation as described in the Solarview setup procedure.

## Thing Configuration

The Solarview Thing requires the hostname (or IP address) as a configuration value in order for the binding to know how to access it.
Additionally, a refresh interval, used to poll the Solarview system, can be specified (in seconds).

In the thing file, this looks e.g. like
```
Bridge solarview:bridge:locally      		[ hostname="192.168.1.1" ] {
 Thing	energyInjection		karlsruhe	[ refreshSecs=60 ]
}
```

| Thing type              | Description                                                                     |
|-------------------------|---------------------------------------------------------------------------------|
| energyProduction        | Power generation view: production side i.e. the photovolatic system             |
| energyInjection         | Injection view: energy feed-in towards the public line                          |
| energyImport            | Import view: energy feed from the public line                                   |
| productionInverterOne   | Power generation view restricted to the 1st inverter of the photovolatic system |
| productionInverterTwo   | Power generation view restricted to the 2nd inverter of the photovolatic system |
| productionInverterThree | Power generation view restricted to the 3rd inverter of the photovolatic system |
| productionInverterFour  | Power generation view restricted to the 4th inverter of the photovolatic system |
| productionInverterFive  | Power generation view restricted to the 5th inverter of the photovolatic system |
| productionInverterSix   | Power generation view restricted to the 6th inverter of the photovolatic system |
| productionInverterSeven | Power generation view restricted to the 7th inverter of the photovolatic system |
| productionInverterEight | Power generation view restricted to the 8th inverter of the photovolatic system |
| productionInverterNine  | Power generation view restricted to the 9th inverter of the photovolatic system |


## Channels


The energy information provided by the Solarview system that is retrieved is available as these channels:

| Channel Type ID | Item Type | Description                                             | Thing types                                                         |
|-----------------|-----------|---------------------------------------------------------|---------------------------------------------------------------------|
| KT0             | Number    | The overall energy balance in kilowatt hour (kWh)       | all                                                                 |
| KYR             | Number    | The energy balance of this year in kilowatt hour (kWh)  | all                                                                 |
| KMT             | Number    | The energy balance of this month in kilowatt hour (kWh) | all                                                                 |
| KDY             | Number    | The energy balance of today in kilowatt hour (kWh)      | all                                                                 |
| PAC             | Number    | The power in watt (W)                                   | all                                                                 |
| UDC             | Number    | The voltage of string one in volt (V)                   | productionInverterOne,productionInverterTwo,productionInverterThree |
| IDC             | Number    | The current of string one in ampere (A)                 | productionInverterOne,productionInverterTwo,productionInverterThree |
| UDCB            | Number    | The voltage of string two in volt (V)                   | productionInverterOne,productionInverterTwo,productionInverterThree |
| IDCB            | Number    | The current of string two in ampere (A)                 | productionInverterOne,productionInverterTwo,productionInverterThree |
| UDCC            | Number    | The voltage of string three in volt (V)                 | productionInverterOne,productionInverterTwo,productionInverterThree |
| IDCC            | Number    | The current of string three in ampere (A)               | productionInverterOne,productionInverterTwo,productionInverterThree |
| UL1             | Number    | The line voltage in volt (V)                            | all                                                                 |
| IL1             | Number    | The line current in ampere (A)                          | all                                                                 |
| TKK             | Number    | The temperature of inverter in degree Celsius           | productionInverterOne,productionInverterTwo,productionInverterThree |

## Full Example

### Things

```
Bridge solarview:bridge:locally      		[ hostname="192.168.45.7", tcpPort=15000, timeoutMsecs=2000 ] {
 Thing	energyProduction	karlsruhe	[ refreshSecs=10 ]
 Thing	energyInjection		karlsruhe	[ refreshSecs=60 ]
 Thing	energyImport		karlsruhe	[ refreshSecs=60 ]
 Thing	productionInverterOne	karlsruhe	[ refreshSecs=60 ]
 Thing	productionInverterTwo	karlsruhe	[ refreshSecs=60 ]
}
```

### Items

```
/*
 * Solarview items for openHAB
 */
Group   SV	(All)

/*
 * Group for display charts
 */
Group   SV_Chart_Gesamt	(All)
Group   SV_Chart_WR1	(All)
Group   SV_Chart_WR2	(All)
Group   SV_Chart_UIDC	(All)
Group   SV_Chart_Tage	(All)
Group   SV_Chart_Monate	(All)
Group   SV_Chart_Jahre	(All)

Number	SV_ALL_KDY	"Tagesertrag [%.1f kWh]"	(SV,SV_Chart_Tage)	{ channel="solarview:energyProduction:karlsruhe:KDY" }
Number	SV_ALL_KMT	"Monatsertrag [%.1f kWh]"	(SV,SV_Chart_Monate)	{ channel="solarview:energyProduction:karlsruhe:KMT" }
Number	SV_ALL_KYR	"Jahresertrag [%.1f kWh]"	(SV,SV_Chart_Jahre)	{ channel="solarview:energyProduction:karlsruhe:KYR" }
Number	SV_ALL_KT0	"Gesamtertrag [%.1f kWh]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:KT0" }
Number	SV_ALL_PAC	"Generatorleistung [%.1f W]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:PAC" }
Number	SV_ALL_UDC	"Generatorspannung [%.1f V]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:UDC" }
Number	SV_ALL_IDC	"Generatorstr?me [%.1f A]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:IDC" }
Number	SV_ALL_UDCB	"Generatorspannung [%.1f V]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:UDCB" }
Number	SV_ALL_IDCB	"Generatorstr?me [%.1f A]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:IDCB" }
Number	SV_ALL_UDCC	"Generatorspannung [%.1f V]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:UDCC" }
Number	SV_ALL_IDCC	"Generatorstr?me [%.1f A]"	(SV)			{ channel="solarview:energyProduction:karlsruhe:IDCC" }
Number	SV_ALL_UL1	"Netzspannung [%.1f V]"		(SV)			{ channel="solarview:energyProduction:karlsruhe:UL1" }
Number	SV_ALL_IL1	"Netzstrom [%.1f A]"		(SV)			{ channel="solarview:energyProduction:karlsruhe:IL1" }
Number	SV_ALL_TKK	"Temperatur Wechselrichter [%.1f C]" (SV)		{ channel="solarview:energyProduction:karlsruhe:TKK" }

Number	SV_WR1_KDY	"Tagesertrag [%.1f kWh]"	(SV,SV_Chart_Tage)	{ channel="solarview:productionInverterOne:karlsruhe:KDY" }
Number	SV_WR1_KMT	"Monatsertrag [%.1f kWh]"	(SV,SV_Chart_Monate)	{ channel="solarview:productionInverterOne:karlsruhe:KMT" }
Number	SV_WR1_KYR	"Jahresertrag [%.1f kWh]"	(SV,SV_Chart_Jahre)	{ channel="solarview:productionInverterOne:karlsruhe:KYR" }
Number	SV_WR1_KT0	"Gesamtertrag [%.1f kWh]"	(SV)			{ channel="solarview:productionInverterOne:karlsruhe:KT0" }
Number	SV_WR1_PAC	"Generatorleistung [%.1f W]"	(SV,SV_Chart_WR1)	{ channel="solarview:productionInverterOne:karlsruhe:PAC" }
Number	SV_WR1_UDC	"Generatorspannung [%.1f V]"	(SV,SV_Chart_WR1)	{ channel="solarview:productionInverterOne:karlsruhe:UDC" }
Number	SV_WR1_IDC	"Generatorstr?me [%.1f A]"	(SV,SV_Chart_WR1)	{ channel="solarview:productionInverterOne:karlsruhe:IDC" }
Number	SV_WR1_UDCB	"Generatorspannung [%.1f V]"	(SV,SV_Chart_WR1)	{ channel="solarview:productionInverterOne:karlsruhe:UDCB" }
Number	SV_WR1_IDCB	"Generatorstr?me [%.1f A]"	(SV,SV_Chart_WR1)	{ channel="solarview:productionInverterOne:karlsruhe:IDCB" }
Number	SV_WR1_UDCC	"Generatorspannung [%.1f V]"	(SV,SV_Chart_WR1)	{ channel="solarview:productionInverterOne:karlsruhe:UDCC" }
Number	SV_WR1_IDCC	"Generatorstr?me [%.1f A]"	(SV,SV_Chart_WR1)	{ channel="solarview:productionInverterOne:karlsruhe:IDCC" }
Number	SV_WR1_UL1	"Netzspannung [%.1f V]"		(SV)			{ channel="solarview:productionInverterOne:karlsruhe:UL1" }
Number	SV_WR1_IL1	"Netzstrom [%.1f A]"		(SV)			{ channel="solarview:productionInverterOne:karlsruhe:IL1" }
Number	SV_WR1_TKK	"Temperatur Wechselrichter [%.1f C]" (SV)		{ channel="solarview:productionInverterOne:karlsruhe:TKK" }

Number	SV_WR2_KDY	"Tagesertrag [%.1f kWh]"	(SV,SV_Chart_Tage)	{ channel="solarview:productionInverterTwo:karlsruhe:KDY" }
Number	SV_WR2_KMT	"Monatsertrag [%.1f kWh]"	(SV,SV_Chart_Monate)	{ channel="solarview:productionInverterTwo:karlsruhe:KMT" }
Number	SV_WR2_KYR	"Jahresertrag [%.1f kWh]"	(SV,SV_Chart_Jahre)	{ channel="solarview:productionInverterTwo:karlsruhe:KYR" }
Number	SV_WR2_KT0	"Gesamtertrag [%.1f kWh]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:KT0" }
Number	SV_WR2_PAC	"Generatorleistung [%.1f W]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:PAC" }
Number	SV_WR2_UDC	"Generatorspannung [%.1f V]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:UDC" }
Number	SV_WR2_IDC	"Generatorstr?me [%.1f A]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:IDC" }
Number	SV_WR2_UDCB	"Generatorspannung [%.1f V]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:UDCB" }
Number	SV_WR2_IDCB	"Generatorstr?me [%.1f A]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:IDCB" }
Number	SV_WR2_UDCC	"Generatorspannung [%.1f V]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:UDCC" }
Number	SV_WR2_IDCC	"Generatorstr?me [%.1f A]"	(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:IDCC" }
Number	SV_WR2_UL1	"Netzspannung [%.1f V]"		(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:UL1" }
Number	SV_WR2_IL1	"Netzstrom [%.1f A]"		(SV)			{ channel="solarview:productionInverterTwo:karlsruhe:IL1" }
Number	SV_WR2_TKK	"Temperatur Wechselrichter [%.1f C]" (SV)		{ channel="solarview:productionInverterTwo:karlsruhe:TKK" }

Number	SV_INJ_KDY	"Tageseinspeisung [%.1f kWh]"	(SV)			{ channel="solarview:energyInjection:karlsruhe:KDY" }
Number	SV_INJ_KMT	"Monatseinspeisung [%.1f kWh]"	(SV)			{ channel="solarview:energyInjection:karlsruhe:KMT" }
Number	SV_INJ_KYR	"Jahreseinspeisung [%.1f kWh]"	(SV)			{ channel="solarview:energyInjection:karlsruhe:KYR" }
Number	SV_INJ_KT0	"Gesamteinspeisung [%.1f kWh]"	(SV)			{ channel="solarview:energyInjection:karlsruhe:KT0" }
Number	SV_INJ_PAC	"Generatorleistung [%.1f W]"	(SV)			{ channel="solarview:energyInjection:karlsruhe:PAC" }

Number	SV_IMP_KDY	"Tagesbezug [%.1f kWh]"		(SV)			{ channel="solarview:energyImport:karlsruhe:KDY" }
Number	SV_IMP_KMT	"Monatsbezug [%.1f kWh]"	(SV)			{ channel="solarview:energyImport:karlsruhe:KMT" }
Number	SV_IMP_KYR	"Jahresbezug [%.1f kWh]"	(SV)			{ channel="solarview:energyImport:karlsruhe:KYR" }
Number	SV_IMP_KT0	"Gesamtbezug [%.1f kWh]"	(SV)			{ channel="solarview:energyImport:karlsruhe:KT0" }
Number	SV_IMP_PAC	"Generatorleistung [%.1f W]"	(SV)			{ channel="solarview:energyImport:karlsruhe:PAC" }

/*
 * end-of-items/solarview.items
 */
```

### Sitemap

```
sitemap default label="MyHome"
{

	Frame label="Photovoltaics" {
		Text item=SV_ALL_PAC icon="sun"
		valuecolor=[SV_ALL_PAC=="Uninitialized"="lightgray",SV_ALL_PAC<=50="lightgray",>500="green",>50="yellow"] 
	}
	Frame label="Gesamtanlage" {
				Text item=SV_ALL_PAC				{
					Chart	item=SV_ALL_PAC,SV_WR1_PAC period=d
					Chart	item=SV_ALL_KDY period=d
				}
				Text item=SV_LastUpdate				
		Frame label="History" {
				Text item=SV_ALL_KDY				
				Text item=SV_ALL_KMT				
				Text item=SV_ALL_KYR				
				Text item=SV_ALL_KTO				
		}
	}
	Frame label="Inverter 1" {
		Frame label="History" {
				Text item=SV_WR1_KDY				
				Text item=SV_WR1_KMT				
				Text item=SV_WR1_KYR				
				Text item=SV_WR1_KTO				
		}
		Frame label="Actual" {
				Text item=SV_WR1_PAC
				Text item=SV_WR1_UDC					visibility=[SV_WR1_UL1>0]
				Text item=SV_WR1_IDC					visibility=[SV_WR1_UL1>0]
				Text item=SV_WR1_UDCB					visibility=[SV_WR1_UL1>0]
				Text item=SV_WR1_IDCB					visibility=[SV_WR1_UL1>0]
				Text item=SV_WR1_UL1					visibility=[SV_WR1_UL1>0]
				Text item=SV_WR1_IL1					visibility=[SV_WR1_UL1>0]
				Text item=SV_WR1_TKK					visibility=[SV_WR1_UL1>0]
		}
	}
}
```


## More details about usage

The scenic views come together with a flexible persistent storage (i.e. influxdb) and visualization engine (i.e. grafana) as shown below.

![Solarview Inverter View](/doc/solarview-inverters.png?raw=true "Grafana-based view on Solarview Inverter Information")
![Solarview Overview View](/doc/solarview-photovoltaics.png?raw=true "Grafana-based view on Solarview Energy Flow Information")
![Solarview Production View](/doc/solarview-production.png?raw=true "Grafana-based view on Solarview Photovoltaic Production Information")

