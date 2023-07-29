# Solarwatt Binding

Binding to query a [solarwatt](https://www.solarwatt.de/) [energy manager](https://www.solarwatt.de/energie-management/energymanager) and read the values of all attached devices.

All supported values and devices were discovered while playing with my own energy manager.

## Supported devices

* Solarwatt Energymanager; ie. the DIN rail mounted device in your house distribution.

## Not supported by this binding

* Solarwatt Manager/Manager Flex; ie. the black square device that is wall mounted.
 
The Solarwatt Manager already contains an OpenHAB installation which can be connected to
other installations via [Remote openHAB Binding](https://www.openhab.org/addons/bindings/remoteopenhab/).


## Supported Things

| Thing Type ID    | Devices                                                |
|------------------|--------------------------------------------------------|
| energymanager    | EnergyManager itself.                                  |
| location         | Location part of the EnergyManager.                    |
| pvplant          | Power producing part of the EnergyManager.             |
| gridflow         | Grid interaction part of the EnergyManager.            |
| inverter         | inverter producing AC current; e.g. MyReserve, Fronius |
| batteryconverter | battery storage systems; e.g. MyReserve                |
| powermeter       | powermeters; e.g. S0BusCounter, MyReserve              |
| evstation        | electric-vehicle charging station; e.g. Keba Wallbox   |
| smartheater      | Radiators for PV systems; e.g. EGO SmartHeater         |

## Discovery

You have to enter the hostname or ip-address of the energymanager itself.
The attached devices and supported channels are discovered automatically.

## Thing Configuration

### EnergyManager

| Property | Default | Required | Description                                                             |
| -------- | ------- | -------- | ----------------------------------------------------------------------- |
| hostname | None    | Yes      | hostname or ip-address of the energy manager.                           |
| refresh  | 30      | No       | Refresh interval in seconds for the current values of the channels.     |
| rescan   | 5       | No       | Rescan interval in minutes for the redetection of channgels and things. |

### Child Things

| Property | Default | Required | Description                                                |
| -------- | ------- | -------- | ---------------------------------------------------------- |
| guid     | None    | Yes      | Guid of the device as used by the solarwatt energymanager. |

## Channels

### EnergyManager

| Channel Type ID                          | Item Type            | Description                                                                                                      |
| ---------------------------------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------- |
| timestamp                                | Number               | Milliseconds since the epoch set to the last NTP time sync                                                       |
| datetime                                 | DateTime             | Date and time of the last NTP time sync in the timezone of the energy manager                                    |
| idTimezone                               | String               | Timezone the energy manager is running in. All  timestamps are milliseconds since the epoch within this timezone |
| fractionCPULoadTotal                     | Number:Dimensionless | Total CPU load in %                                                                                              |
| fractionCPULoadUser                      | Number:Dimensionless | Userspace CPU load in %                                                                                          |
| fractionCPULoadKernel                    | Number:Dimensionless | Kernelspace CPU load in %                                                                                        |
| fractionCPULoadAverageLastMinute         | Number:Dimensionless | Average 1 minute CPU load in %                                                                                   |
| fractionCPULoadAverageLastFiveMinutes    | Number:Dimensionless | Average 5 minute CPU load in %                                                                                   |
| fractionCPULoadAverageLastFifteenMinutes | Number:Dimensionless | Average 15 minute CPU load in %                                                                                  |

### PVPlant

| Channel Type ID | Item Type     | Description                             |
| --------------- | ------------- | --------------------------------------- |
| powerACOut      | Number:Power  | Energy produced by the PV in watts      |
| workACOut       | Number:Energy | Energy produced by the PV in watt hours |

### Location

| Channel Type ID          | Item Type     | Description                                                                    |
| ------------------------ | ------------- | ------------------------------------------------------------------------------ |
| powerBuffered            | Number:Power  | Power flow into the storage system                                             |
| powerSelfConsumed        | Number:Power  | Power consumed direct from PV plus energy stored                               |
| powerSelfSupplied        | Number:Power  | Power consumed direct from PV plus energy consumed from storage                |
| powerConsumedFromGrid    | Number:Power  | Power consumed from the grid                                                   |
| powerConsumedFromStorage | Number:Power  | Power consumed from storage                                                    |
| powerConsumedUnmetered   | Number:Power  | Power consumed in the inner side (outer consumers are subtracted)              |
| powerConsumed            | Number:Power  | Total power consumed. All inner and outer consumers.                           |
| powerDirectConsumed      | Number:Power  | Power consumed directly from PV without buffering                              |
| powerProduced            | Number:Power  | Power produced by the PV                                                       |
| powerOut                 | Number:Power  | Power delivered to the grid                                                    |
| powerDirectConsumed      | Number:Power  | Power consumed directly without energy put into storage or taken from storage  |
| workBuffered             | Number:Energy | Energy flow into the storage system                                            |
| workSelfConsumed         | Number:Energy | Energy consumed direct from PV plus energy stored                              |
| workSelfSupplied         | Number:Energy | Energy consumed direct from PV plus energy consumed from storage               |
| workConsumedFromGrid     | Number:Energy | Energy consumed from the grid                                                  |
| workConsumedFromStorage  | Number:Energy | Energy consumed from storage                                                   |
| workConsumedUnmetered    | Number:Energy | Energy consumed in the inner side (outer consumers are subtracted)             |
| workConsumed             | Number:Energy | Total energy consumed. All inner and outer consumers.                          |
| workDirectConsumed       | Number:Energy | Energy consumed directly from PV without buffering                             |
| workProduced             | Number:Energy | Energy produced by the PV                                                      |
| workOut                  | Number:Energy | Energy delivered to the grid                                                   |
| workDirectConsumed       | Number:Energy | Energy consumed directly without energy put into storage or taken from storage |

### PowerMeter, S0Counter, MyReservePowerMeter

| Channel Type ID          | Item Type     | Description                                                                                 |
| ------------------------ | ------------- | ------------------------------------------------------------------------------------------- |
| channelDirectionMetering | String        | Representing which energy flow directions are metered. One off _IN_, _OUT_, _BIDIRECTIONAL_ |
| powerIn                  | Number:Power  | Power metered flowing into the consumer                                                     |
| powerOut                 | Number:Power  | Power metered flowing out of the producer                                                   |
| workIn                   | Number:Energy | Energy metered flowing into the consumer                                                    |
| workOut                  | Number:Energy | Energy metered flowing out of the producer                                                  |
| consumptionEnergySum     | Number:Energy | Total energy in watt hours                                                                  |

### Inverter, MyReserveInverter, SunSpecInverter

| Channel Type ID     | Item Type     | Description                      |
| ------------------- | ------------- | -------------------------------- |
| powerACOutMax       | Number:Power  | Maximum power production         |
| powerACOutLimit     | Number:Power  | Limit of power production        |
| powerACOut          | Number:Power  | Power delivered by the inverter  |
| workACOut           | Number:Energy | Energy delivered by the inverter |
| powerInstallledPeak | Number:Power  | Technical peak power available   |

### BatteryConverter, MyReserve

All of _Inverter_ plus

| Channel Type ID        | Item Type          | Description                              |
| ---------------------- | ------------------ | ---------------------------------------- |
| powerACIn              | Number:Power       | Power fed into battery                   |
| workACIn               | Number:Energy      | Energy fed into battery                  |
| stateOfCharge          | Number             | Charging state of battery in percent     |
| stateOfHealth          | Number             | Internal health metric in percent        |
| temperatureBattery     | Number:Temperature | Temperature of the battery in celsius    |
| modeConverter          | Switch             | Current mode of converter. _ON_ or _OFF_ |
| voltageBatteryCellMin  | Number:Voltage     | minimum voltage of all batteries         |
| voltageBatteryCellMean | Number:Voltage     | mean voltage of all batteries            |
| voltageBatteryCellMax  | Number:Voltage     | maximum voltage of all batteries         |

### EVStation, KebaEv

| Channel Type ID    | Item Type     | Description                                                             |
| ------------------ | ------------- | ----------------------------------------------------------------------- |
| powerACIn          | Number:Power  | Power consumed by the charger                                           |
| workACIn           | Number:Energy | Energy consumed by the charger                                          |
| workACInSession    | Number:Energy | Work consumed during current/last charging session                      |
| modeStation        | String        | Current mode of the charger. One off _STANDBY_, _CHARGING_, _OFF_       |
| connectivityStatus | String        | Current state of the charging connection. One off _ONLINE_ or _OFFLINE_ |

### GridFlow

| Channel Type ID | Item Type            | Description                         |
| --------------- | -------------------- | ----------------------------------- |
| feedInLimit     | Number:Dimensionless | Current derating setting in percent |

### SmartHeater

| Channel Type ID   | Item Type          | Description                            |
|-------------------|--------------------|----------------------------------------|
| workACIn          | Number:Energy      | Energy fed into smart heater           |
| powerACIn         | Number:Power       | Power fed into smart heater            |
| temperature       | Number:Temperature | Current heating temperature in celsius |
| temperatureBoiler | Number:Temperature | Current boiler temperature in celsius  |
| temperatureSet    | Number:Temperature | Set temperature                        |
| temperatureSetMin | Number:Temperature | Minimum adjustable temperature         |
| temperatureSetMax | Number:Temperature | Maximum adjustable temperature         |

## Example

demo.things:

```java
Bridge solarwatt:energymanager:56f4ac2fa2 [hostname="192.168.0.64", refresh=30, rescan=5]
// the individual things configured with their energy manager guid
Thing solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5 [guid="5c7d5929-8fa4-42c5-8737-48bef77b61f5"] (solarwatt:energymanager:56f4ac2fa2)
Thing solarwatt:gridflow:56f4ac2fa2:urn-kiwigrid-gridflow-ERC05-000008007 [guid="urn:kiwigrid:gridflow:ERC05-000008007"] (solarwatt:energymanager:56f4ac2fa2)
Thing solarwatt:evstation:56f4ac2fa2:urn-keba-evstation-20652876 [guid="urn:keba:evstation:2065287"] (solarwatt:energymanager:56f4ac2fa2)
```

demo.items:

```java
// Location DeviceClass com.kiwigrid.devices.location.Location Guid b4e4978b96404e61977bfacd3eab299d
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerBuffered "PowerBuffered [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerBuffered"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerBufferedFromGrid "PowerBufferedFromGrid [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerBufferedFromGrid"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerBufferedFromProducers "PowerBufferedFromProducers [%.2f W]" <energy> ["Measurement", "Power"]  {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerBufferedFromProducers"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerConsumed "PowerConsumed [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerConsumed"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerConsumedUnmetered "PowerConsumedUnmetered [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:PowerConsumedUnmetered"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerDirectConsumed "PowerDirectConsumed [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerDirectConsumed"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerConsumedFromGrid "PowerConsumedFromGrid [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerConsumedFromGrid"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerConsumedFromStorage "PowerConsumedFromStorage [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerConsumedFromStorage"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerConsumedFromProducers "PowerConsumedFromProducers [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerConsumedFromProducers"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerIn "PowerIn [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerIn"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerProduced "PowerProduced [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerProduced"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerOut "PowerOut [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerOut"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerOutFromProducers "PowerOutFromProducers [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerOutFromProducers"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerOutFromStorage "PowerOutFromStorage [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerOutFromStorage"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerReleased "PowerReleased [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerReleased"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerSelfConsumed "PowerSelfConsumed [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerSelfConsumed"}
Number:Power Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_PowerSelfSupplied "PowerSelfSupplied [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:powerSelfSupplied"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkBuffered "WorkBuffered [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workBuffered"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkBufferedFromGrid "WorkBufferedFromGrid [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workBufferedFromGrid"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkBufferedFromProducers "WorkBufferedFromProducers [%.2f Wh]" <energy> [""]  {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workBufferedFromProducers"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkConsumed "WorkConsumed [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workConsumed"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkConsumedUnmetered "WorkConsumedUnmetered [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:WorkConsumedUnmetered"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkDirectConsumed "WorkDirectConsumed [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workDirectConsumed"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkConsumedFromGrid "WorkConsumedFromGrid [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workConsumedFromGrid"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkConsumedFromStorage "WorkConsumedFromStorage [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workConsumedFromStorage"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkConsumedFromProducers "WorkConsumedFromProducers [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workConsumedFromProducers"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkIn "WorkIn [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workIn"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkProduced "WorkProduced [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workProduced"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkOut "WorkOut [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workOut"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkOutFromProducers "WorkOutFromProducers [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workOutFromProducers"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkOutFromStorage "WorkOutFromStorage [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workOutFromStorage"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkReleased "WorkReleased [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workReleased"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkSelfConsumed "WorkSelfConsumed [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workSelfConsumed"}
Number:Energy Solarwatt_Location_b4e4978b96404e61977bfacd3eab299d_WorkSelfSupplied "WorkSelfSupplied [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:location:56f4ac2fa2:b4e4978b-9640-4e61-977b-facd3eab299d:workSelfSupplied"}

// Inverter Fronius com.kiwigrid.devices.inverter.Inverter
Number:Power Solarwatt_Inverter_UrnSunspecFroniusInverter31414368_PowerACOut "PowerACOut [%.2f W]" <energy> ["Measurement", "Power"]  {channel="solarwatt:inverter:56f4ac2fa2:urn-sunspec-fronius-inverter-31414368:powerACOut"}
Number:Power Solarwatt_Inverter_UrnSunspecFroniusInverter31414368_PowerACOutLimit "PowerACOutLimit [%.2f W]" <energy> ["Point", "Power"]  {channel="solarwatt:inverter:56f4ac2fa2:urn-sunspec-fronius-inverter-31414368:powerACOutLimit"}
Number:Energy Solarwatt_Inverter_UrnSunspecFroniusInverter31414368_WorkACOut "WorkACOut [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:inverter:56f4ac2fa2:urn-sunspec-fronius-inverter-31414368:workACOut"}

// MyReserve BatteryInverter com.kiwigrid.devices.bat…verter.BatteryConverter
Switch Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_ModeConverter "ModeConverter [%s]" <switch> ["Switch"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:modeConverter"}
Number Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_StateOfCharge "StateOfCharge [%.2f %%]" <status> ["Status"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:stateOfCharge"}
Number Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_StateOfHealth "StateOfHealth [%.2f %%]" <status> ["Status"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:stateOfHealth"}
Number:Temperature Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_TemperatureBattery "TemperatureBattery [%.1f °C]" <temperature> ["Measurement", "Temperature"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:temperatureBattery"}
Number:Power Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_PowerACIn "PowerACIn [%.2f W]" <energy> ["Measurement", "Power"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:powerACIn"}
Number:Power Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_PowerACOut "PowerACOut [%.2f W]" <energy> ["Measurement", "Power"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:powerACOut"}
Number:Energy Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_WorkACIn "WorkACIn [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:workACIn"}
Number:Energy Solarwatt_BatteryInverter_5c7d59298fa442c5873748bef77b61f5_WorkACOut "WorkACOut [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:batteryconverter:56f4ac2fa2:5c7d5929-8fa4-42c5-8737-48bef77b61f5:workACOut"}

// S0Bus Meter com.kiwigrid.devices.powermeter.PowerMeter / com.kiwigrid.devices.s0counter.S0Counter
String Solarwatt_Meter_46bb4ee8a9744ecea11ef43c68263ae9_DirectionMetering "DirectionMetering [%s]" <status> ["Status"]  {channel="solarwatt:powermeter:56f4ac2fa2:46bb4ee8-a974-4ece-a11e-f43c68263ae9:directionMetering"}
Number:Power Solarwatt_Meter_46bb4ee8a9744ecea11ef43c68263ae9_PowerIn "PowerIn [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:powermeter:56f4ac2fa2:46bb4ee8-a974-4ece-a11e-f43c68263ae9:powerIn"}
Number:Energy Solarwatt_Meter_46bb4ee8a9744ecea11ef43c68263ae9_WorkIn "WorkIn [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:powermeter:56f4ac2fa2:46bb4ee8-a974-4ece-a11e-f43c68263ae9:workIn"}
Number:Energy Solarwatt_Meter_46bb4ee8a9744ecea11ef43c68263ae9_ConsumptionEnergySum "ConsumptionEnergySum [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:powermeter:56f4ac2fa2:46bb4ee8-a974-4ece-a11e-f43c68263ae9:consumptionEnergySum"}

// MyReservePowermeter Meter com.kiwigrid.devices.powermeter.PowerMeter
String Solarwatt_Meter_2c5d089b98854f40ba8a3303bfb53e36_DirectionMetering "DirectionMetering [%s]" <status> ["Status"]  {channel="solarwatt:powermeter:56f4ac2fa2:2c5d089b-9885-4f40-ba8a-3303bfb53e36:directionMetering"}
Number:Power Solarwatt_Meter_2c5d089b98854f40ba8a3303bfb53e36_PowerIn "PowerIn [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:powermeter:56f4ac2fa2:2c5d089b-9885-4f40-ba8a-3303bfb53e36:powerIn"}
Number:Power Solarwatt_Meter_2c5d089b98854f40ba8a3303bfb53e36_PowerOut "PowerOut [%.2f W]" <energy> ["Measurement", "Power"] {channel="solarwatt:powermeter:56f4ac2fa2:2c5d089b-9885-4f40-ba8a-3303bfb53e36:powerOut"}
Number:Energy Solarwatt_Meter_2c5d089b98854f40ba8a3303bfb53e36_WorkIn "WorkIn [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:powermeter:56f4ac2fa2:2c5d089b-9885-4f40-ba8a-3303bfb53e36:workIn"}
Number:Energy Solarwatt_Meter_2c5d089b98854f40ba8a3303bfb53e36_WorkOut "WorkOut [%.2f Wh]" <energy> ["Measurement", "Energy"] {channel="solarwatt:powermeter:56f4ac2fa2:2c5d089b-9885-4f40-ba8a-3303bfb53e36:workOut"}

// Inverter MyReserve com.kiwigrid.devices.inverter.Inverter
Number:Power Solarwatt_Inverter_4af659938b1149408a77ff87556389f3_PowerACOut "PowerACOut [%.2f W]" <energy> ["Measurement", "Power"]  {channel="solarwatt:inverter:56f4ac2fa2:4af65993-8b11-4940-8a77-ff87556389f3:powerACOut"}
Number:Energy Solarwatt_Inverter_4af659938b1149408a77ff87556389f3_WorkACOut "WorkACOut [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:inverter:56f4ac2fa2:4af65993-8b11-4940-8a77-ff87556389f3:workACOut"}

// EVStation Keba com.kiwigrid.devices.evstation.EVStation
String Solarwatt_EVStation_UrnKebaEvstation20652876_ModeStation "ModeStation [%s]" <status> ["Status"]  {channel="solarwatt:evstation:56f4ac2fa2:urn-keba-evstation-20652876:modeStation"}
String Solarwatt_EVStation_UrnKebaEvstation20652876_ConnectivityStatus "ConnectivityStatus [%s]" <status> ["Status"]  {channel="solarwatt:evstation:56f4ac2fa2:urn-keba-evstation-20652876:connectivityStatus"}
Number:Power Solarwatt_EVStation_UrnKebaEvstation20652876_PowerACIn "PowerACIn [%.2f W]" <energy> ["Measurement", "Power"]  {channel="solarwatt:evstation:56f4ac2fa2:urn-keba-evstation-20652876:powerACIn"}
Number:Energy Solarwatt_EVStation_UrnKebaEvstation20652876_WorkACIn "WorkACIn [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:evstation:56f4ac2fa2:urn-keba-evstation-20652876:workACIn"}
Number:Energy Solarwatt_EVStation_UrnKebaEvstation20652876_WorkACInSession "WorkACInSession [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:evstation:56f4ac2fa2:urn-keba-evstation-20652876:workACInSession"}

// PVPlant com.kiwigrid.devices.pvplant.PVPlant
Number:Power Solarwatt_PVPlant_4575c19cfa0a4f21839a5db2ae06b4d_PowerACOut "PowerACOut [%.2f W]" <energy> ["Measurement", "Power"]  {channel="solarwatt:pvplant:56f4ac2fa2:4575c19c-fa0a-4f21-839a-5db2ae06b4dc:powerACOut"}
Number:Energy Solarwatt_PVPlant_4575c19cfa0a4f21839a5db2ae06b4d_WorkACOut "WorkACOut [%.2f Wh]" <energy> ["Measurement", "Energy"]  {channel="solarwatt:pvplant:56f4ac2fa2:4575c19c-fa0a-4f21-839a-5db2ae06b4dc:workACOut"}

// Manager com.kiwigrid.devices.em.EnergyManager
String Solarwatt_Manager_ERC05000008007_IdFirmware "IdFirmware [%s]" <status> ["Status"]  {channel="solarwatt:energymanager:56f4ac2fa2:idFirmware"}
Number Solarwatt_Manager_ERC05000008007_Timestamp "Timestamp [%d]" <time> ["Status", "Timestamp"]  {channel="solarwatt:energymanager:56f4ac2fa2:timestamp"}
DateTime Solarwatt_Manager_ERC05000008007_Datetime  "Update [%1$tH:%1$tM:%1$tS]" <time> ["Status", "Timestamp"] {channel="solarwatt:energymanager:56f4ac2fa2:datetime"}
Number Solarwatt_Manager_ERC05000008007_FractionCPULoadTotal "FractionCPULoadUser [%.2f]" <status> ["Measurement"]  {channel="solarwatt:energymanager:56f4ac2fa2:fractionCPULoadTotal"}
Number Solarwatt_Manager_ERC05000008007_FractionCPULoadUser "FractionCPULoadUser [%.2f]" <status> ["Measurement"]  {channel="solarwatt:energymanager:56f4ac2fa2:fractionCPULoadUser"}
Number Solarwatt_Manager_ERC05000008007_FractionCPULoadKernel "FractionCPULoadKernel [%.2f]" <status> ["Measurement"]  {channel="solarwatt:energymanager:56f4ac2fa2:fractionCPULoadKernel"}
Number Solarwatt_Manager_ERC05000008007_FractionCPULoadAverageLastMinute "FractionCPULoadAverageLastMinute [%.2f]" <status> ["Measurement"]  {channel="solarwatt:energymanager:56f4ac2fa2:fractionCPULoadAverageLastMinute"}
Number Solarwatt_Manager_ERC05000008007_FractionCPULoadAverageLastFiveMinutes "FractionCPULoadAverageLastFiveMinutes [%.2f]" <status> ["Measurement"]  {channel="solarwatt:energymanager:56f4ac2fa2:fractionCPULoadAverageLastFiveMinutes"}
Number Solarwatt_Manager_ERC05000008007_FractionCPULoadAverageLastFifteenMinutes "FractionCPULoadAverageLastFifteenMinutes [%.2f]" <status> ["Measurement"]  {channel="solarwatt:energymanager:56f4ac2fa2:fractionCPULoadAverageLastFifteenMinutes"}

// Gridflow com.kiwigrid.kiwiapp.gridflow.GridFlow
Number Solarwatt_Gridflow_UrnKiwigridGridflowERC05000008007_CurrentLimit "CurrentLimit [%d A]" <energy> ["Point"]  {channel="solarwatt:gridflow:56f4ac2fa2:urn-kiwigrid-gridflow-ERC05-000008007:currentLimit"}
Number Solarwatt_Gridflow_UrnKiwigridGridflowERC05000008007_FeedInLimit "FeedInLimit [%d %%]" <status> ["Point"]  {channel="solarwatt:gridflow:56f4ac2fa2:urn-kiwigrid-gridflow-ERC05-000008007:feedInLimit"}

// SmartHeater com.kiwigrid.devices.smartheater.SmartHeater
Number:Power Solarwatt_Smartheater_UrnEgoSmartheater62018833_PowerACIn "SmartHeater powerACIn [%.2f W]" <energy> { channel="solarwatt:smartheater:urn-ego-smartheater-62018833:powerACIn" }
Number:Energy Solarwatt_Smartheater_UrnEgoSmartheater62018833_WorkACIn "SmartHeater workACIn [%.2f Wh]" <energy> { channel="solarwatt:smartheater:urn-ego-smartheater-62018833:workACIn" }
Number:Temperature Solarwatt_Smartheater_UrnEgoSmartheater62018833_TemperatureBoiler "SmartHeater temperatureBoiler [%.1f °C]" <temperature> { channel="solarwatt:smartheater:urn-ego-smartheater-62018833:temperatureBoiler" }
Number:Temperature Solarwatt_Smartheater_UrnEgoSmartheater62018833_Temperature "SmartHeater temperature [%.1f °C]" <temperature> { channel="solarwatt:smartheater:urn-ego-smartheater-62018833:temperature" }
Number:Temperature Solarwatt_Smartheater_UrnEgoSmartheater62018833_TemperatureSet "SmartHeater temperatureSet [%.1f °C]" <temperature> { channel="solarwatt:smartheater:urn-ego-smartheater-62018833:temperatureSet" }
Number:Temperature Solarwatt_Smartheater_UrnEgoSmartheater62018833_TemperatureSetMax "SmartHeater temperatureSetMax [%.1f °C]" <temperature> { channel="solarwatt:smartheater:urn-ego-smartheater-62018833:temperatureSetMax" }
Number:Temperature Solarwatt_Smartheater_UrnEgoSmartheater62018833_TemperatureSetMin "SmartHeater temperatureSetMin [%.1f °C]" <temperature> { channel="solarwatt:smartheater:urn-ego-smartheater-62018833:temperatureSetMin" }
```
