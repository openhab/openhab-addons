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

You have to enter the hostname or ip-address of the energymanager itself. The attached devices and supported channels
are discovered automatically.

## Binding Configuration

The binding has no configuration settings.

## Thing Configuration

Only the EnergyManager thing requires configuration.
You have to give it the hostname or ip-address.

## Channels

### All devices

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| StateDevice | String | value reflecting the state of the communication from the energy manager to the device. *ON* or *OFFLINE* |

### EnergyManager

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
|Timestamp | Number | Milliseconds since the epoch set to the last NTP time sync |
|IdTimezone | String | Timezone the energy manager is running in. All  timestamps are milliseconds since the epoch within this timezone |
|FractionCPULoadTotal | Number:Percent | Total CPU load |
|FractionCPULoadUser | Number:Percent | Userspace CPU load |
|FractionCPULoadKernel | Number:Percent | Kernelspace CPU load |
|FractionCPULoadAverageLastMinute | Number:Percent | Average 1 minute CPU load |
|FractionCPULoadAverageLastFiveMinutes | Number:Percent | Average 5 minute CPU load |
|FractionCPULoadAverageLastFifteenMinutes | Number:Percent | Average 15 minute CPU load |

### PVPlant

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| PowerACOut | Number:Energy | Energy produced by the PV in watts |
| WorkACOut | Number:Energy | Energy produced by the PV in watt hours |

### Location

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| PowerBuffered | Number:Energy | Energy flow into the storage system
| PowerSelfConsumed | Number:Energy | Energy consumed direct from PV plus energy stored
| PowerSelfSupplied | Number:Energy | Energy consumed direct from PV plus energy consumed from storage
| PowerConsumedFromGrid | Number:Energy | Energy consumed from the grid  
| PowerConsumedFromStorage | Number:Energy | Energy consumed from storage
| PowerConsumed | Number:Energy | Total energy consumed. All inner and outer consumers.
| PowerProduced | Number:Energy | Energy produced by the PV
| PowerOut | Number:Energy | Energy delivered to the grid  
| PowerDirectConsumed | Number:Energy | Energy consumed directly without energy put into storage or taken from storage
| WorkBuffered | Number:Energy | Energy flow into the storage system
| WorkSelfConsumed | Number:Energy | Energy consumed direct from PV plus energy stored
| WorkSelfSupplied | Number:Energy | Energy consumed direct from PV plus energy consumed from storage
| WorkConsumedFromGrid | Number:Energy | Energy consumed from the grid  
| WorkConsumedFromStorage | Number:Energy | Energy consumed from storage
| WorkConsumed | Number:Energy | Total energy consumed. All inner and outer consumers.
| WorkProduced | Number:Energy | Energy produced by the PV
| WorkOut | Number:Energy | Energy delivered to the grid  
| WorkDirectConsumed | Number:Energy | Energy consumed directly without energy put into storage or taken from storage

### PowerMeter, S0Counter, MyReservePowerMeter

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| ChannelDirectionMetering | String | Representing which energy flow directions are metered. One off *IN*, *OUT*, *BIDIRECTIONAL* 
| PowerIn | Number:Energy | Energy metered flowing into the consumer
| PowerOut | Number:Energy | Energy metered flowing out of the producer  
| WorkIn | Number:Energy | Energy metered flowing into the consumer
| WorkOut | Number:Energy | Energy metered flowing out of the producer  
| ConsumptionEnergySum | Number:Energy | Total energy in watt hours 

### Inverter, MyReserveInverter, SunSpecInverter

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| PowerACOutMax | Number:Energy | Maximum power production 
| PowerACOutLimit | Number:Energy | Limit of power production
| PowerACOut | Number:Energy | Energy delivered by the inverter
| WorkACOut | Number:Energy | Energy delivered by the inverter
| PowerInstallledPeak | Number:Energy | Technical peak power available 

### BatteryConverter, MyReserve

All of *Inverter* plus

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| PowerACIn | Number:Energy | Energy fed into battery
| WorkACIn | Number:Energy | Energy fed into battery
| StateOfCharge | Number | Charging state of battery in percent
| StateOfHealth | Number | Internal health metric in percent
| TemperatureBattery | Number:Temperature | Temperature of the battery in celsius
| ModeConverter | String | Current mode of converter. *ON* or *OFF*

### EVStation, KebaEv

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| PowerACIn | Number:Energy | Energy consumed by the charger
| WorkACIn | Number:Energy | Energy consumed by the charger
| WorkACInSession | Number:Energy | Work consumed during current/last charging session
| ModeStation | String | Current mode of the charger. One off *STANDBY*, *CHARGING*, *OFF*
| ConnectivityStatus | String | Current state of the charging connection. One off *ONLINE* or *OFFLINE*

### Forecast

Nothing yet

### GridFlow

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| FeedInLimit | Number | Current derating setting

### ScheduleApp

Nothing yet.

### ProfileApp

Don't know yet what this thing does.