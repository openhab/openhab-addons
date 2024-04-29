# Ism8 Binding

_This binding can receive values of the Wolf heating system._

The ISM8 card can be placed into the Wolf heating system.
The card is usually used in combination with an object server, where the object server does forward those messages into the KNX bus system.
In case there is no need to handle the heating system values directly in the KNX system you can use this binding to monitor and control your heating system without the need to buy an object server.
The system works in a way that the ISM8 connects to a partner and sends from time to time an update. The frequency depends on the change of the values.
This binding is listening to those messages.
After the first connection there is an active command send to the ISM8 in order to receive all available data points.
The manual of the ISM8 can be downloaded from the supplier (<https://www.wolf.eu/fileadmin/Wolf_Profi/Downloads/Montage-Bedienungsanleitungen/Regelungen/Zubehoer/3064356_201611_ISM8i_Montage-u.Bedienungsanleitung.pdf>)

## Supported Things

_This binding does only support one Thing - the Ism8-Device._

## Discovery

_Auto-discovery is not supported._

## Thing Configuration

The intention was to have a generic ISM8 binding in order to offer the full flexibilty for the different heating systems.
For this reason you need to create a Thing configuration, where basically only the port is required next to the channel configuration.
(`Thing ism8:device:heater "Wolf Heizung" [portNumber=12004]`)

## Channels

You can use any channel supported by the ISM8 as data point. Please have a look at the official manual from Wolf.
Within this document you'll find a table containing all supported data points.
The available data points are depending on your heating system configuration.
The ISM8 does currently support 4 different devices at the same moment of time (e.g. CGB-2, CWL Excellent, Solar, ...).

Once you have an overview of your heating system set you can start to create the channels accordingly.
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
A list of the available IDs are available within the Wolf manual.
The supported IDs are depending on the firmware version of the ISM8 and the connected systems.
- type="1.001"    - Please enter here the knx type of the data point.
You can find the data type in the Wolf ISM8 document as well.
- write=true      - This parameter defines if the channel is bidirectional, but the parameter is optional and by default false.

Note:
Not all available types of the ISM8 interface are fully supported, but this can be extended.
For the moment the following data types are implemented:

| Channel type   | Datapoint type                 | Item type                 | R/W | KNX-type's                 |
|----------------|--------------------------------|---------------------------|-----|----------------------------|
| switch-rw      | Digital DataPoint              | Switch                    | R/W | 1.001, 1.002, 1.003, 1.009 |
| switch-r       | Digital Readonly DataPoint     | Switch                    | R   | 1.001, 1.002, 1.003, 1.009 |
| percentage-rw  | Percentage  DataPoint          | Number:Dimensionless      | R/W | 5.001                      |
| percentage-r   | Percentage Readonly DataPoint  | Number:Dimensionless      | R   | 5.001                      |
| temperature-rw | Temperature DataPoint          | Number:Temperature        | R/W | 9.001,9.002                |
| temperature-r  | Temperature Readonly DataPoint | Number:Temperature        | R   | 9.002,9.002                |
| pressure-r     | Pressure Readonly DataPoint    | Number:Pressure           | R   | 9.006                      |
| flowrate-r     | Flowrate Readonly DataPoint    | Number:VolumetricFlowRate | R   | 13.002                     |
| mode-rw        | Mode DataPoint                 | Number:Dimensionless      | R/W | 20.102, 20.103, 20.105     |
| mode-r         | Mode Readonly DataPoint        | Number:Dimensionless      | R   | 20.102, 20.103, 20.105     |

## Full Example

### ism8.things

```java
Thing ism8:device:heater "Wolf Heizung"         [portNumber=12004]
    {
        Type switch-r       : DpId001 "Störung Heizgerät"            [id=1, type="1.001"]
        Type number-r       : DpId002 "Betriebsart"                  [id=2, type="20.105"]
        Type percentage-r   : DpId003 "Brennerleistung"              [id=3, type="5.001"] 
        Type temperature-r  : DpId004 "Kesseltemperatur"             [id=4, type="9.001"] 
        Type temperature-r  : DpId006 "Rücklauftemperatur"           [id=6, type="9.001"] 
        Type temperature-r  : DpId007 "Warmwassertemperatur"         [id=7, type="9.001"] 
        Type temperature-r  : DpId008 "Außentemperatur"              [id=8, type="9.001"] 
        Type switch-r       : DpId009 "Status Flamme"                [id=9, type="1.001"] 
        Type temperature-r  : DpId013 "Anlagendruck"                 [id=13, type="9.006"] 
        Type switch-r       : DpId053 "Störung Systemmodul"          [id=53, type="1.001"] 
        Type temperature-r  : DpId054 "Außentemperatur Systemmodul"  [id=54, type="9.001"] 
        Type temperature-rw : DpId056 "Sollwert Warmwasser"          [id=56, type="9.001"] 
        Type mode-rw        : DpId057 "Betriebsart Heizkreis"        [id=57, type="20.102"] 
        Type mode-rw        : DpId058 "Betriebsart Warmwasser"       [id=58, type="20.103"] 
        Type temperature-rw : DpId065 "Sollwertverschiebung"         [id=65, type="9.002"] 
        Type switch-rw      : DpId148 "CML Störung"                  [id=148, type="1.001"] 
        Type mode-rw        : DpId149 "CWL Betriebsart"              [id=149, type="20.102"] 
        Type percentage-r   : DpId163 "CWL Lüftungsstufe"            [id=163, type="5.001"] 
        Type temperature-r  : DpId164 "CWL Ablufttemperatur"         [id=164, type="9.001"] 
        Type temperature-r  : DpId165 "CWL Zulufttemperatur"         [id=165, type="9.001"]
        Type flowrate-r     : DpId166 "CWL Luftdurchsatz Zuluft"     [id=166, type="13.002"]
        Type flowrate-r     : DpId167 "CWL Luftdurchsatz Abluft"     [id=167, type="13.002"]
        Type switch-r       : DpId192 "CML Filterwarnung"            [id=192, type="1.001"]    
    }
```

### ism8.items

```java
Switch ISM_HeizungStoerung              "Störung Heizgerät"                      { channel="ism8:device:heater:DpId001" }
Number ISM_HeizungBetriebsart           "Betriebsart"                            { channel="ism8:device:heater:DpId002" }
Number ISM_HeizungBrennerleistung       "Brennerleistung [%.1f %%]"              { channel="ism8:device:heater:DpId003" }
Number ISM_HeizungKesseltemperatur      "Kesseltemperatur [%.1f °C]"             { channel="ism8:device:heater:DpId004" }
Number ISM_HeizungRuecklauftemperatur   "Rücklauftemperatur [%.1f °C]"           { channel="ism8:device:heater:DpId006" }
Number ISM_HeizungWarmwassertemperatur  "Warmwassertemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId007" }
Number ISM_HeizungAussentemperatur      "Außentemperatur [%.1f °C]"              { channel="ism8:device:heater:DpId008" }
Switch ISM_HeizungStatusFlamme          "Status Flamme"                          { channel="ism8:device:heater:DpId009" }
Number ISM_HeizungAnlagendruck          "Anlagendruck [%.2f bar]"                { channel="ism8:device:heater:DpId013" }
Switch ISM_HeizungSysStoerung           "Störung Systemmodul"                    { channel="ism8:device:heater:DpId053" }
Number ISM_HeizungSysAussentemperatur   "Außentemperatur Systemmodul [%.1f °C]"  { channel="ism8:device:heater:DpId054" }
Number ISM_HeizungSollwertWarmwasser    "Sollwert Warmwasser [%.1f °C]"          { channel="ism8:device:heater:DpId056" }
Number ISM_HeizungBetriebsartHeizkreis  "Betriebsart Heizkreis"                  { channel="ism8:device:heater:DpId057" }
Number ISM_HeizungBetriebsartWarmwasser "Betriebsart Warmwasser"                 { channel="ism8:device:heater:DpId058" }
Number ISM_HeizungSollwertverschiebung  "Sollwertverschiebung [%.1f °C]"         { channel="ism8:device:heater:DpId065" }
Switch ISM_LueftungStoerung             "CML Störung"                            { channel="ism8:device:heater:DpId148" }
Number ISM_LueftungBetriebsart          "CWL Betriebsart"                        { channel="ism8:device:heater:DpId149" }
Number ISM_LueftungLueftungsstufe       "CWL Lüftungsstufe [%.1f %%]"            { channel="ism8:device:heater:DpId163" }
Number ISM_LueftungAblufttemperatur     "CWL Ablufttemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId164" }
Number ISM_LueftungZulufttemperatur     "CWL Zulufttemperatur [%.1f °C]"         { channel="ism8:device:heater:DpId165" }
Number ISM_LueftungLuftdurchsatzZuluft  "CWL Luftdurchsatz Zuluft [%.1f m³/h]"   { channel="ism8:device:heater:DpId166" }
Number ISM_LueftungLuftdurchsatzAbluft  "CWL Luftdurchsatz Abluft [%.1f m³/h]"   { channel="ism8:device:heater:DpId167" }
Switch ISM_LueftungFilterwarnung        "CML Filterwarnung"                      { channel="ism8:device:heater:DpId192" }
```

### demo.sitemap

```perl
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
    Setpoint item=ISM_HeizungSollwertverschiebung   icon="radiator"          minValue=-5 maxValue=5 step=1
}
Frame label="Wasser"
{    
    Text item=ISM_HeizungWarmwassertemperatur       icon="temperature_hot"
    Setpoint item=ISM_HeizungSollwertWarmwasser     icon="temperature"       minValue=40 maxValue=60 step=1
    Selection item=ISM_HeizungBetriebsartWarmwasser icon="faucet"            mappings=[0="Auto", 1="Legionellen Schutz", 2="Normal", 3="Eco", 4="Frost Schutz"]
}
Frame label="Lüftung"
{    
    Text item=ISM_LueftungStoerung                  icon="siren"        
    Selection item=ISM_LueftungBetriebsart          icon="fan"                mappings=[0="Auto", 1="Minimum", 2="Reduziert", 3="Normal", 4="Intensiv"]
    Text item=ISM_LueftungLueftungsstufe            icon="qualityofservice"
    Text item=ISM_LueftungFilterwarnung             icon="siren"
    Text item=ISM_LueftungAblufttemperatur          icon="temperature_hot"
    Text item=ISM_LueftungZulufttemperatur          icon="temperature_cold"
    Text item=ISM_LueftungLuftdurchsatzZuluft       icon="flow"
    Text item=ISM_LueftungLuftdurchsatzAbluft       icon="flow"
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
<img src="doc/Sitemap-Example.png" width="800" height="600">
