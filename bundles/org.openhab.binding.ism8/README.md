# Ism8 Binding

_This binding can receive measurements from a Wolf heating system._

The ISM8 card can be placed into the Wolf heating system.
The card is usually used in combination with an object server, where the object server forwards those messages into the KNX bus system.
If there is no need to handle the heating system values directly in the KNX system, you can use this binding to monitor and control your heating system without the need to buy an object server.
The system works in such a way that the ISM8 connects to a partner and sends, from time to time, an update. The frequency depends on the change in values.
This binding is listening to those messages.
After the first connection, there is an active command sent to the ISM8 in order to receive all available data points.
The manual for the ISM8 can be downloaded here: (<https://oxomi.com/service/json/catalog/pdf?portal=2024876&user=&roles=&accessToken=&catalog=10572791>)

Note that there are different firmware versions of the ISM8 module.
Some data points are not available with older firmware versions.

## Supported Things

_This binding does only support one Thing - the ISM8-Device._

## Discovery

_Auto-discovery is not supported._

## Thing Configuration

The intention was to have a generic ISM8 binding in order to offer full flexibility for the different heating systems.
For this reason, you need to create a Thing configuration, where basically only the port is required next to the channel configuration.
(`Thing ism8:device:heater "Wolf Heizung" [portNumber=12004]`)

## Channels

You can use any channel supported by the ISM8 as a data point. Please take a look at the official manual from Wolf.
Within this document, you'll find a table containing all supported data points.
Depending on your heating system configuration, different data points are available.
The ISM8 does currently support 4 different devices at the same moment of time (e.g., CHA, CGB-2, CWL Excellent, Solar, etc.).

Once you have an overview of your heating system, you can start to create the channels accordingly.
Each channel should be created in the following way:

| Type               | Name    | Description        | Configuration |
|--------------------|---------|--------------------|---------------|
| Number:Temperature | DpId004 | "Kesseltemperatur" |               |

Name:

- Put here any name you'd like. This name is used for creating the binding.

Description:

- Put here any description you'd like or the description for the data point ID from the Wolf manual.

Configuration:

- id=1            - Please enter here the ID of the data point you'd like to map to this channel.
A list of the available IDs is available within the Wolf manual.

Depending on the firmware version of the ISM8 and the connected systems, the supported IDs differ.

- type="1.001"    - Please enter here the KNX type of the data point.
You can find the data type in the Wolf ISM8 document as well.
- write=true      - This parameter defines if the channel is bidirectional, but the parameter is optional and by default false.

> Note:
Not all available data types of the ISM8 interface are fully supported.
For the moment, the following data types are implemented:

| Channel type    | Datapoint type                           | Item type                 | R/W | KNX-type's                 |
|-----------------|------------------------------------------|---------------------------|-----|----------------------------|
| switch-rw       | Digital DataPoint                        | Switch                    | R/W | 1.001, 1.002, 1.003, 1.009 |
| switch-r        | Digital Readonly DataPoint               | Switch                    | R   | 1.001, 1.002, 1.003, 1.009 |
| percentage-rw   | Percentage  DataPoint                    | Number:Dimensionless      | R/W | 5.001                      |
| percentage-r    | Percentage Readonly DataPoint            | Number:Dimensionless      | R   | 5.001                      |
| number-r        | Numeric Readonly DataPoint               | Number:Dimensionless      | R   | 5.010, 7.001               |
| temperature-rw  | Temperature DataPoint                    | Number:Temperature        | R/W | 9.001,9.002                |
| temperature-r   | Temperature Readonly DataPoint           | Number:Temperature        | R   | 9.002,9.002                |
| pressure-r      | Pressure Readonly DataPoint              | Number:Pressure           | R   | 9.006                      |
| flowrate-r      | Flowrate Readonly DataPoint              | Number:VolumetricFlowRate | R   | 9.025, 13.002              |
| active-energy-r | Active Energy Readonly DataPoint         | Number:Energy             | R   | 13.010, 13.013             |
| mode-rw         | Mode DataPoint                           | Number:Dimensionless      | R/W | 20.102, 20.103, 20.105     |
| mode-r          | Mode Readonly DataPoint                  | Number:Dimensionless      | R   | 20.102, 20.103, 20.105     |

Date and Time types used by for CWL Excellent and CWL2 are currently not supported by the ISM8 add-on.

**Attention:** Due to a bug in the original implementation, the states for DPT 1.009 are inverted (i.e., `1` is mapped to `OPEN` instead of `CLOSE`).
A change would break all existing installations and is therefore not implemented.

## Full Example

### ism8.things

```java
Thing ism8:device:heater "Wolf Heizung"         [portNumber=12004]
    {
        Type switch-r       : DpId001 "Störung Heizgerät"            [id=1, type="1.001"]
        Type mode-r         : DpId002 "Betriebsart"                  [id=2, type="20.105"]
        Type percentage-r   : DpId003 "Brennerleistung"              [id=3, type="5.001"]
        Type temperature-r  : DpId004 "Kesseltemperatur"             [id=4, type="9.001"]
        Type temperature-r  : DpId006 "Rücklauftemperatur"           [id=6, type="9.001"]
        Type temperature-r  : DpId007 "Warmwassertemperatur"         [id=7, type="9.001"]
        Type temperature-r  : DpId008 "Außentemperatur"              [id=8, type="9.001"]
        Type switch-r       : DpId009 "Status Flamme"                [id=9, type="1.001"]
        Type pressure-r     : DpId013 "Anlagendruck"                 [id=13, type="9.006"]
        Type temperature-r  : DpId197 "Abgastemperatur"              [id=197, type="9.001"]

        Type switch-r       : DpId053 "Störung Systemmodul"          [id=53, type="1.001"]
        Type temperature-r  : DpId054 "Außentemperatur Systemmodul"  [id=54, type="9.001"]
        Type switch-rw      : DpId194 "1x Warmwasser"                [id=194, type="1.001"]

        Type temperature-rw : DpId056 "Sollwert Warmwasser"          [id=56, type="9.001"]
        Type mode-rw        : DpId057 "Betriebsart Heizkreis"        [id=57, type="20.102"]
        Type mode-rw        : DpId058 "Betriebsart Warmwasser"       [id=58, type="20.103"]
        Type temperature-rw : DpId065 "Sollwertverschiebung"         [id=65, type="9.002"]

        Type switch-r       : DpId135 "Solar Störung"                [id=135, type="1.001"]
        Type temperature-r  : DpId136 "Solar Wassertemperatur"       [id=136, type="9.001"]
        Type temperature-r  : DpId137 "Solar Kollektortemperatur"    [id=137, type="9.001"]
        Type switch-r       : DpId141 "Solar Status Pumpe"           [id=141, type="1.001"]

        Type switch-rw      : DpId148 "CWL Störung"                  [id=148, type="1.001"]
        Type mode-rw        : DpId149 "CWL Betriebsart"              [id=149, type="20.102"]
        Type percentage-r   : DpId163 "CWL Lüftungsstufe"            [id=163, type="5.001"]
        Type temperature-r  : DpId164 "CWL Ablufttemperatur"         [id=164, type="9.001"]
        Type temperature-r  : DpId165 "CWL Zulufttemperatur"         [id=165, type="9.001"]
        Type flowrate-r     : DpId166 "CWL Luftdurchsatz Zuluft"     [id=166, type="13.002"]
        Type flowrate-r     : DpId167 "CWL Luftdurchsatz Abluft"     [id=167, type="13.002"]
        Type switch-r       : DpId192 "CWL Filterwarnung"            [id=192, type="1.001"]

        Type switch-r       : DpId176 "CHA Störung"                  [id=176, type="1.001"]
        Type mode-r         : DpId177 "CHA Betriebsart"              [id=177, type="20.105"]
        Type power-r        : DpId178 "CHA Heizleistung"             [id=178, type="9.024"]
        Type power-r        : DpId179 "CHA Kühlleistung"             [id=179, type="9.024"]
        Type temperature-r  : DpId180 "CHA Kesseltemperatur"         [id=180, type="9.001"]
        Type temperature-r  : DpId181 "CHA Sammlertemperatur"        [id=181, type="9.001"]
        Type temperature-r  : DpId182 "CHA Rücklauftemperatur"       [id=182, type="9.001"]
        Type temperature-r  : DpId183 "CHA Warmwassertemperatur"     [id=183, type="9.001"]
        Type temperature-r  : DpId184 "CHA Aussentemperatur"         [id=184, type="9.001"]
        Type switch-r       : DpId185 "CHA Status Heizkreispumpe"    [id=185, type="1.001"]
        Type switch-r       : DpId186 "CHA Status Zubringerpumpe"    [id=186, type="1.001"]
        Type switch-r       : DpId187 "CHA 3-Wege-Ventil HZ/WW"      [id=187, type="1.009"]
        Type switch-r       : DpId188 "CHA 3-Wege-Ventil HZ/K"       [id=188, type="1.009"]
        Type switch-r       : DpId189 "CHA Status E-Heizung"         [id=189, type="1.001"]
        Type pressure-r     : DpId190 "CHA Anlagendruck"             [id=190, type="9.006"]
        Type power-r        : DpId191 "CHA Leistung"                 [id=191, type="9.024"]
    }
```

### ism8.items

```java
Switch ISM_HeizungStoerung                            "Störung Heizgerät"                      { channel="ism8:device:heater:DpId001" }
Number ISM_HeizungBetriebsart                         "Betriebsart"                            { channel="ism8:device:heater:DpId002" }
Number ISM_HeizungBrennerleistung                     "Brennerleistung [%.1f %%]"              { channel="ism8:device:heater:DpId003" }
Number:Temperature ISM_HeizungKesseltemperatur        "Kesseltemperatur [%.1f °C]"             { channel="ism8:device:heater:DpId004" }
Number:Temperature ISM_HeizungRuecklauftemperatur     "Rücklauftemperatur [%.1f °C]"           { channel="ism8:device:heater:DpId006" }
Number:Temperature ISM_HeizungWarmwassertemperatur    "Warmwassertemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId007" }
Number:Temperature ISM_HeizungAussentemperatur        "Außentemperatur [%.1f °C]"              { channel="ism8:device:heater:DpId008" }
Switch ISM_HeizungStatusFlamme                        "Status Flamme"                          { channel="ism8:device:heater:DpId009" }
Number:Pressure ISM_HeizungAnlagendruck               "Anlagendruck [%.2f bar]"                { channel="ism8:device:heater:DpId013" }
Number:Temperature ISM_HeizungAbgastemperatur         "Abgastemperatur [%.1f °C]"              { channel="ism8:device:heater:DpId197" }

Switch ISM_HeizungSysStoerung                         "Störung Systemmodul"                    { channel="ism8:device:heater:DpId053" }
Number:Temperature ISM_HeizungSysAussentemperatur     "Außentemperatur Systemmodul [%.1f °C]"  { channel="ism8:device:heater:DpId054" }
Switch ISM_HeizungSys1xWarmwasser                     "1x Warmwasser"                          { channel="ism8:device:heater:DpId194" }

Number:Temperature ISM_HeizungSollwertWarmwasser      "Sollwert Warmwasser [%.1f °C]"          { channel="ism8:device:heater:DpId056" }
Number ISM_HeizungBetriebsartHeizkreis                "Betriebsart Heizkreis"                  { channel="ism8:device:heater:DpId057" }
Number ISM_HeizungBetriebsartWarmwasser               "Betriebsart Warmwasser"                 { channel="ism8:device:heater:DpId058" }
Number:Temperature ISM_HeizungSollwertverschiebung    "Sollwertverschiebung [%.1f K]"          { channel="ism8:device:heater:DpId065" }
Switch ISM_LueftungStoerung                           "CWL Störung"                            { channel="ism8:device:heater:DpId148" }
Number ISM_LueftungBetriebsart                        "CWL Betriebsart"                        { channel="ism8:device:heater:DpId149" }
Number ISM_LueftungLueftungsstufe                     "CWL Lüftungsstufe [%.1f %%]"            { channel="ism8:device:heater:DpId163" }
Number:Temperature ISM_LueftungAblufttemperatur       "CWL Ablufttemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId164" }
Number:Temperature ISM_LueftungZulufttemperatur       "CWL Zulufttemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId165" }
Number:VolumetricFlowRate ISM_LueftungDurchsatzZuluft "CWL Luftdurchsatz Zuluft [%.1f m³/h]"   { channel="ism8:device:heater:DpId166" }
Number:VolumetricFlowRate ISM_LueftungDurchsatzAbluft "CWL Luftdurchsatz Abluft [%.1f m³/h]"   { channel="ism8:device:heater:DpId167" }
Switch ISM_LueftungFilterwarnung                      "CWL Filterwarnung"                      { channel="ism8:device:heater:DpId192" }

Switch ISM_SolarStoerung                              "Solar Störung"                          { channel="ism8:device:heater:DpId135" }
Number:Temperature ISM_SolarWassertemperatur          "Solar Wassertemperatur [%.1f °C]"       { channel="ism8:device:heater:DpId136" }
Number:Temperature ISM_SolarKollektortemperatur       "Solar Kollektortemperatur [%.1f °C]"    { channel="ism8:device:heater:DpId137" }
Switch ISM_SolarStatusPumpe                           "Solar Status Pumpe"                     { channel="ism8:device:heater:DpId141" }

Switch ISM_HeizungChaStoerung                         "CHA Störung"                            { channel="ism8:device:heater:DpId176" }
Number ISM_HeizungChaBetriebsart                      "Betriebsart [MAP(HVACContrMode.map):%s]"{ channel="ism8:device:heater:DpId177" }
Number:Power ISM_HeizungChaLeistungsaufnahme          "CHA Leistungsaufnahme [%.1f kW]"        { channel="ism8:device:heater:DpId191" }
Number:Power ISM_HeizungChaHeizleistung               "CHA Heizleistung [%.1f kW]"             { channel="ism8:device:heater:DpId178" }
Number:Power ISM_HeizungChaKuehlleistung              "CHA Kühlleistung [%.1f kW]"             { channel="ism8:device:heater:DpId179" }
Number:Temperature ISM_HeizungChaKesseltemperatur     "CHA Kesseltemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId180" }
Number:Temperature ISM_HeizungChaKesselsolltemperatur "CHA KesselSOLLtemperatur [%.1f °C]"     { channel="ism8:device:heater:DpId364" }
Number:Temperature ISM_HeizungChaSammlertemperatur    "CHA Sammlertemperatur [%.1f °C]"        { channel="ism8:device:heater:DpId181" }
Number:Temperature ISM_HeizungChaRuecklauftemperatur  "CHA Rücklauftemperatur [%.1f °C]"       { channel="ism8:device:heater:DpId182" }
Number:Temperature ISM_HeizungChaWarmwassertemperatur "CHA Warmwassertemperatur [%.1f °C]"     { channel="ism8:device:heater:DpId183" }
Number:Temperature ISM_HeizungChaAussentemperatur     "CHA Aussentemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId184" }
Switch ISM_HeizungChaStatusHKPumpe                    "CHA Status HK Pumpe"                    { channel="ism8:device:heater:DpId185" }
Switch ISM_HeizungChaStatusZPumpe                     "CHA Status Zubringer Pumpe"             { channel="ism8:device:heater:DpId186" }
Switch ISM_HeizungChaStatusVentilHZWW                 "CHA Status Ventil HZ/WW"                { channel="ism8:device:heater:DpId187" }
Switch ISM_HeizungChaStatusVentilHZK                  "CHA Status Ventil HZ/K"                 { channel="ism8:device:heater:DpId188" }
Switch ISM_HeizungChaStatusEHeizung                   "CHA Status E-Heizung"                   { channel="ism8:device:heater:DpId189" }
Number:Pressure ISM_HeizungChaAnlagendruck            "CHA Anlagendruck [%.2f bar]"            { channel="ism8:device:heater:DpId190" }
```

### ism8.sitemap

```perl
sitemap ism8 label="Wolf ism8"
{
    Frame label="Heizung"
    {
        Text item=ISM_HeizungSysStoerung                icon="siren"
        Text item=ISM_HeizungStoerung                   icon="siren"
        Text item=ISM_HeizungAussentemperatur           icon="temperature"
        Text item=ISM_HeizungBetriebsart                icon="radiator"          label="Modus [MAP(HVACContrMode.map):%s]"
        Text item=ISM_HeizungAnlagendruck               icon="pressure"
        Text item=ISM_HeizungBrennerleistung            icon="chart"
        Selection item=ISM_HeizungBetriebsartHeizkreis  icon="radiator"          mappings=[0="Auto", 1="Komfort", 2="Stand By", 3="Eco", 4="Frost Schutz"]
        Text item=ISM_HeizungStatusFlamme               icon="fire"
        Text item=ISM_HeizungKesseltemperatur           icon="temperature"
        Text item=ISM_HeizungRuecklauftemperatur        icon="temperature_cold"
        Text item=ISM_HeizungAbgastemperatur            icon="temperature"
        Setpoint item=ISM_HeizungSollwertverschiebung   icon="radiator"          minValue=-5 maxValue=5 step=1
    }
    Frame label="Wasser"
    {
        Text item=ISM_HeizungWarmwassertemperatur       icon="temperature_hot"
        Setpoint item=ISM_HeizungSollwertWarmwasser     icon="temperature"       minValue=40 maxValue=60 step=1
        Selection item=ISM_HeizungBetriebsartWarmwasser icon="faucet"            mappings=[0="Auto", 1="Legionellen Schutz", 2="Normal", 3="Eco", 4="Frost Schutz"]
        Switch item=ISM_HeizungSys1xWarmwasser
    }
    Frame label="Solar"
    {
    Text item=ISM_SolarStoerung                      icon="siren"
    Text item=ISM_SolarWassertemperatur              icon="cistern"
    Text item=ISM_SolarKollektortemperatur           icon="solarplant"
    Text item=ISM_SolarStatusPumpe                   icon="switch"
    }
    Frame label="Lüftung"
    {
        Text item=ISM_LueftungStoerung                  icon="siren"
        Selection item=ISM_LueftungBetriebsart          icon="fan"                mappings=[0="Auto", 1="Minimum", 2="Reduziert", 3="Normal", 4="Intensiv"]
        Text item=ISM_LueftungLueftungsstufe            icon="qualityofservice"
        Text item=ISM_LueftungFilterwarnung             icon="siren"
        Text item=ISM_LueftungAblufttemperatur          icon="temperature_hot"
        Text item=ISM_LueftungZulufttemperatur          icon="temperature_cold"
        Text item=ISM_LueftungDurchsatzZuluft           icon="flow"
        Text item=ISM_LueftungDurchsatzAbluft           icon="flow"
    }
    Frame label="Wärmepumpe" {
        Text item=ISM_HeizungChaStoerung                icon="siren"
        Text item=ISM_HeizungChaBetriebsart             icon="radiator"
        Text item=ISM_HeizungChaLeistungsaufnahme       icon="energy"
        Text item=ISM_HeizungChaHeizleistung            icon="fire"
        Text item=ISM_HeizungChaKuehlleistung           icon="fan"
        Text item=ISM_HeizungChaKesseltemperatur        icon="temperature"
        Text item=ISM_HeizungChaKesselsolltemperatur    icon="temperature"
        Text item=ISM_HeizungChaRuecklauftemperatur     icon="temperature_cold"
        Text item=ISM_HeizungChaWarmwassertemperatur    icon="temperature"
        Text item=ISM_HeizungChaAussentemperatur        icon="temperature"
        Text item=ISM_HeizungChaStatusHKPumpe           icon="siren"
        Text item=ISM_HeizungChaStatusZPumpe            icon="siren"
        Text item=ISM_HeizungChaStatusVentilHZWW        icon="siren"
        Text item=ISM_HeizungChaStatusVentilHZK         icon="siren"
        Text item=ISM_HeizungChaStatusEHeizung          icon="siren"
        Text item=ISM_HeizungChaAnlagendruck            icon="pressure"
    }
}
```

### HVACContrMode.map

```text
0=Auto
1=Heizen
2=Aufwärmen
3=Abkühlen
4=Nächtliche Reinigung
5=Vorkühlen
6=Aus
7=Test
8=Notfall Heizen
9=Nur Lüften
10=Freies Kühlen
11=Eis
12=Maximum Heizen
13=Eco Heiz-/Kühlmodus
14=Entfeuchten
15=Kalibriermodus
16=Notfall Kühlmodus
17=Emergency Dampfmodus
20=Reserviert
NULL=Undefiniert
```

_Result_
![Sitemap Example](doc/Sitemap-Example.png)
