# Wolf Smartset Binding

This binding communicates with the www.wolf-smartset.de API and provides values readonly. 
Wolf systems are connected with official gateways (Wolf Link Home or Wolf Link Pro) https://www.wolf.eu/produkte/smarthome/ 

## Supported Things

- Account (bridge)
- System (bridge)
- Unit

## Discovery

The configuration via the interface is the recommended way:
1. create an "Account-Thing (bridge)" and enter your credentials
2. if the account bridge is successfully connected, a "System-Thing (bridge)" will appear in the inbox (a manual scan may be necessary). 
3. After the system has been added, discovery will add the unit-things to the inbox.

## Thing configuration

### Account (bridge)

| Parameter       | Type    | Defaut | Description                                                         |
|-----------------|---------|----------|---------------------------------------------------------------------|
| username | text | | username to authenticate to www.wolf-smartset.de |
| password | text  | | password to authenticate to www.wolf-smartset.de |
| refreshIntervalConfiguration | integer | 10 | Specifies the refresh interval to refresh the configuration in minutes |
| refreshIntervalValues | integer | 15 | Specifies time in seconds to refresh values |
| discoveryEnabled | boolean | true | disable the Thing discovery |

### System (bridge)

| Parameter       | Type    | Defaut | Description                                                         |
|-----------------|---------|----------|---------------------------------------------------------------------|
| systemId | integer | | System ID assigned to the system by WolfSmartset webiste |

### Unit

| Parameter       | Type    | Defaut | Description                                                         |
|-----------------|---------|----------|---------------------------------------------------------------------|
| unitId | integer | | The BundleId assigned to the unit by WolfSmartset webiste |

## Tested WOLF-Devices

| WOLF Equipment    | openhab Version | Used gateway  |
|-------------------|-----------------|---------------|
| CSZ (CGB and SM1) | 3.1             | WOLF Link Pro |
| CGB-2             | 3.1             | WOLF Link home|

Note: Please update this table if you did a successfull test


## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| number  | number | a generic number  |
| contact  | Contact | a generic contact  |
| temperature  | Number:Temperature | a generic temperature  |
| string  | String | a generic String  |
| datetime  | nuDateTimember | a generic DateTime  |

## Full Example

### Things
````
Bridge wolfsmartset:account:account "Wolf Smartset Account" [ username="User", password="Password" ] {
    Bridge system 32122305166 "WolfSmartset System CSZ" [ systemId="32122305166" ] {
        Thing unitId uinit0 "CSZ Heizgerät" [ unitId="unit0" ] {
        }
    }
}
````
_You need to use the corrosponding systemId and unitId returned by the discovery_

### Items

````
"Number CSZHeizgerat_Raumtemperatur "Raumtemperatur" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900000"}
Number CSZHeizgerat_Flamme "Flamme" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900001"}
Number CSZHeizgerat_AnalogeFernbedienung "Analoge Fernbedienung" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900002"}
Number CSZHeizgerat_Raumsolltemperatur "Raumsolltemperatur" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900003"}
Number CSZHeizgerat_AusgangA1 "Ausgang A1" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900004"}
String CSZHeizgerat_ZeitprogrammdirekterHeizkreis "Zeitprogramm direkter Heizkreis" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900005"}
Number CSZHeizgerat_Ventil1 "Ventil 1" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900006"}
Number CSZHeizgerat_Ventil2 "Ventil 2" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900007"}
Number CSZHeizgerat_WiSoUmschaltung "Wi/So Umschaltung" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900008"}
Number CSZHeizgerat_Tagtemperatur "Tagtemperatur" { channel="wolfsmartset:unit:account:32122305166:uinit0:1000900009"}
Number CSZHeizgerat_PWMPumpe "PWM Pumpe" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000010"}
Number CSZHeizgerat_Speichersolltemperatur "Speichersolltemperatur" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000011"}
Number CSZHeizgerat_Heizkurve "Heizkurve" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000012"}
Number CSZHeizgerat_Raumeinfluss "Raumeinfluss" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000013"}
Number CSZHeizgerat_TWVorlauf "TW-Vorlauf" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000014"}
Number CSZHeizgerat_Spartemperatur "Spartemperatur" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000015"}
Number CSZHeizgerat_Geblase "Gebläse" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000016"}
Number CSZHeizgerat_Vorlaufsolltemperatur "Vorlaufsolltemperatur" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000017"}
Group CSZHeizgerat "CSZ Heizgerät" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000018"}
Number CSZHeizgerat_ECOABS "ECO/ABS" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000019"}
Number CSZHeizgerat_Netzbetriebstunden "Netzbetriebstunden" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000020"}
Number CSZHeizgerat_TWAbgas "TW-Abgas" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000021"}
Number CSZHeizgerat_HGStatus "HG Status" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000022"}
Number CSZHeizgerat_EingangE1 "Eingang E1" { channel="wolfsmartset:unit:account:32122305166:uinit0:10009000023"}"
````

## Supported Heating-Devices

All devices able to be connected to www.wolf-smartset.de

### Regarding documentation from WOLF

https://www.wolf.eu/fileadmin/Wolf_Daten/Dokumente/FAQ/3065655_201711.pdf

| Heating system                            | WOLF Link home        | WOLF Link pro      |
|-------------------------------------------|-----------------------|--------------------|
| Gas condensing boiler CGB-2, CGW-2, CGS-2 | ✅ | ✅ |
| Oil condensing boiler TOB | ✅ | ✅ |
| MGK-2 gas condensing boiler | ✅ | ✅ |
| split air/water heat pump BWL-1S | ✅ | ✅ |
| Oil condensing boiler COB |  | ✅ |
| gas condensing boiler MGK |   | ✅ |
| Gas condensing boilers CGB, CGW, CGS, FGB |   | ✅ |
| Gas condensing boilers CGG-2, CGU-2 |   | ✅ |
| Boiler controls R2, R3, R21 |   | ✅ |
| Monobloc heat pumps BWW-1, BWL-1, BWS-1 |   | ✅ |
| mixer module MM, MM-2 | ⬜ | ✅ |
| cascade module KM, KM-2 | ⬜ | ✅ |
| solar modules SM1, SM1-2, SM-2, SM2-2 | ⬜ | ✅ |
| Comfort apartment ventilation CWL Excellent | ⬜ | ✅ |
| Air handling units KG Top, CKL Pool``*`` |   | ✅ |
| Air handling units CKL, CFL, CRL``*`` |   | ✅ |
| Combined heat and power units | | ✅ |


Note: 

⬜ possible in connection with a WOLF Link home compatible heater,
full functionality only for devices with current software version.

``*`` Modbus interface required in the device,
Special programming cannot be mapped.