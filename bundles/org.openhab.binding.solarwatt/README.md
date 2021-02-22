# Solarwatt Binding

Binding to query a [solarwatt](https://www.solarwatt.de/) [energy manager](https://www.solarwatt.de/energie-management/energymanager) and read the values of all attached devices.

All supported values and devices were discovered while playing with my own energy manager.

## Supported Things

* EnergyManager  
  Location  
  Forecast  
  GridFlow  
  ProfileApp  
  ScheduleApp  
  SimpleSwitcher (seems deprecated)  
  SmartEnergyManagement (seems deprecated)

* BatteryConverter
  
* MyReserve  
  MyReserveInverter  
  MyReservePowerMeter
  
* EVStation (eg. Keba)
* S0Counter
* SunSpecInverter (eg. Fronius)  


## Discovery

You have to enter the hostname or ip-address of the energymanager itself.
The attached devices and supported channels are discovered automatically.

## Binding Configuration

The binding has no configuration settings.

## Thing Configuration

Only the EnergyManager thing requires configuration.
You have to give it the hostname or ip-address.

## Channels

### All devices

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| stateDevice | String | value reflecting the state of the communication from the energy manager to the device. *ON* or *OFFLINE* |

### EnergyManager

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
|timestamp | Number | Milliseconds since the epoch set to the last NTP time sync |
|idTimezone | String | Timezone the energy manager is running in. All  timestamps are milliseconds since the epoch within this timezone |
|fractionCPULoadTotal | Number:Percent | Total CPU load |
|fractionCPULoadUser | Number:Percent | Userspace CPU load |
|fractionCPULoadKernel | Number:Percent | Kernelspace CPU load |
|fractionCPULoadAverageLastMinute | Number:Percent | Average 1 minute CPU load |
|fractionCPULoadAverageLastFiveMinutes | Number:Percent | Average 5 minute CPU load |
|fractionCPULoadAverageLastFifteenMinutes | Number:Percent | Average 15 minute CPU load |

### PVPlant

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerACOut | Number:Energy | Energy produced by the PV in watts |
| workACOut | Number:Energy | Energy produced by the PV in watt hours |

### Location

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerBuffered | Number:Energy | Energy flow into the storage system
| powerSelfConsumed | Number:Energy | Energy consumed direct from PV plus energy stored
| powerSelfSupplied | Number:Energy | Energy consumed direct from PV plus energy consumed from storage
| powerConsumedFromGrid | Number:Energy | Energy consumed from the grid  
| powerConsumedFromStorage | Number:Energy | Energy consumed from storage
| powerConsumed | Number:Energy | Total energy consumed. All inner and outer consumers.
| powerProduced | Number:Energy | Energy produced by the PV
| powerOut | Number:Energy | Energy delivered to the grid  
| powerDirectConsumed | Number:Energy | Energy consumed directly without energy put into storage or taken from storage
| workBuffered | Number:Energy | Energy flow into the storage system
| workSelfConsumed | Number:Energy | Energy consumed direct from PV plus energy stored
| workSelfSupplied | Number:Energy | Energy consumed direct from PV plus energy consumed from storage
| workConsumedFromGrid | Number:Energy | Energy consumed from the grid  
| workConsumedFromStorage | Number:Energy | Energy consumed from storage
| workConsumed | Number:Energy | Total energy consumed. All inner and outer consumers.
| workProduced | Number:Energy | Energy produced by the PV
| workOut | Number:Energy | Energy delivered to the grid  
| workDirectConsumed | Number:Energy | Energy consumed directly without energy put into storage or taken from storage

### PowerMeter, S0Counter, MyReservePowerMeter

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| channelDirectionMetering | String | Representing which energy flow directions are metered. One off *IN*, *OUT*, *BIDIRECTIONAL* 
| powerIn | Number:Energy | Energy metered flowing into the consumer
| powerOut | Number:Energy | Energy metered flowing out of the producer  
| workIn | Number:Energy | Energy metered flowing into the consumer
| workOut | Number:Energy | Energy metered flowing out of the producer  
| consumptionEnergySum | Number:Energy | Total energy in watt hours 

### Inverter, MyReserveInverter, SunSpecInverter

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerACOutMax | Number:Energy | Maximum power production 
| powerACOutLimit | Number:Energy | Limit of power production
| powerACOut | Number:Energy | Energy delivered by the inverter
| workACOut | Number:Energy | Energy delivered by the inverter
| powerInstallledPeak | Number:Energy | Technical peak power available 

### BatteryConverter, MyReserve

All of *Inverter* plus

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerACIn | Number:Energy | Energy fed into battery
| workACIn | Number:Energy | Energy fed into battery
| stateOfCharge | Number | Charging state of battery in percent
| stateOfHealth | Number | Internal health metric in percent
| temperatureBattery | Number:Temperature | Temperature of the battery in celsius
| modeConverter | String | Current mode of converter. *ON* or *OFF*

### EVStation, KebaEv

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerACIn | Number:Energy | Energy consumed by the charger
| workACIn | Number:Energy | Energy consumed by the charger
| workACInSession | Number:Energy | Work consumed during current/last charging session
| modeStation | String | Current mode of the charger. One off *STANDBY*, *CHARGING*, *OFF*
| connectivityStatus | String | Current state of the charging connection. One off *ONLINE* or *OFFLINE*

### Forecast

Nothing yet

### GridFlow

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| feedInLimit | Number | Current derating setting

### ScheduleApp

Nothing yet.

### ProfileApp

Don't know yet what this thing does.
