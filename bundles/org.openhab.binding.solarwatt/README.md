# Solarwatt Binding

Binding to query a [solarwatt](https://www.solarwatt.de/) [energy manager](https://www.solarwatt.de/energie-management/energymanager) and read the values of all attached devices.

All supported values and devices were discovered while playing with my own energy manager.

## Supported Things

| Thing Type ID | Devices |
|------|---------------|
|solarwatt:energymanager| EnergyManager itself
|solarwatt:location| Location part of the EnergyManager 
|solarwatt:pvplant| Power producing part of the EnergyManager
|solarwatt:gridflow| Grid interaction part of the EnergyManager
|solarwatt:inverter| inverter producing AC current; e.g. MyReserve, Fronius
|solarwatt:batteryconverter| battery storage systems; e.g. MyReserve
|solarwatt:powermeter| powermeters; e.g. S0BusCounter, MyReserve
|solarwatt:evstation| electric-vehicle charging station; e.g. Keba Wallbox

## Discovery

You have to enter the hostname or ip-address of the energymanager itself.
The attached devices and supported channels are discovered automatically.

## Thing Configuration

| Property | Default | Required | Description |
|----------|---------|----------|-------------|
| hostname | None | Yes | hostname or ip-address of the energy manager

## Channels

### All devices

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| stateDevice | String | value reflecting the state of the communication from the energy manager to the device. *ON* or *OFFLINE* |

### EnergyManager

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
|timestamp | Number | Milliseconds since the epoch set to the last NTP time sync |
|datetime | DateTime | Date and time of the last NTP time sync in the timezone of the energy manager |
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
| powerACOut | Number:Power | Energy produced by the PV in watts |
| workACOut | Number:Energy | Energy produced by the PV in watt hours |

### Location

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerBuffered | Number:Power | Power flow into the storage system
| powerSelfConsumed | Number:Power | Power consumed direct from PV plus energy stored
| powerSelfSupplied | Number:Power | Power consumed direct from PV plus energy consumed from storage
| powerConsumedFromGrid | Number:Power | Power consumed from the grid  
| powerConsumedFromStorage | Number:Power | Power consumed from storage
| powerConsumedUnmetered | Number:Power | Power consumed in the inner side (outer consumers are subtracted)
| powerConsumed | Number:Power | Total power consumed. All inner and outer consumers.
| powerDirectConsumed | Number:Power | Power consumed directly from PV without buffering
| powerProduced | Number:Power | Power produced by the PV
| powerOut | Number:Power | Power delivered to the grid  
| powerDirectConsumed | Number:Power | Power consumed directly without energy put into storage or taken from storage
| workBuffered | Number:Energy | Energy flow into the storage system
| workSelfConsumed | Number:Energy | Energy consumed direct from PV plus energy stored
| workSelfSupplied | Number:Energy | Energy consumed direct from PV plus energy consumed from storage
| workConsumedFromGrid | Number:Energy | Energy consumed from the grid  
| workConsumedFromStorage | Number:Energy | Energy consumed from storage
| workConsumedUnmetered | Number:Energy | Energy consumed in the inner side (outer consumers are subtracted)
| workConsumed | Number:Energy | Total energy consumed. All inner and outer consumers.
| workDirectConsumed | Number:Energy | Energy consumed directly from PV without buffering
| workProduced | Number:Energy | Energy produced by the PV
| workOut | Number:Energy | Energy delivered to the grid  
| workDirectConsumed | Number:Energy | Energy consumed directly without energy put into storage or taken from storage

### PowerMeter, S0Counter, MyReservePowerMeter

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| channelDirectionMetering | String | Representing which energy flow directions are metered. One off *IN*, *OUT*, *BIDIRECTIONAL* 
| powerIn | Number:Power | Power metered flowing into the consumer
| powerOut | Number:Power | Power metered flowing out of the producer  
| workIn | Number:Energy | Energy metered flowing into the consumer
| workOut | Number:Energy | Energy metered flowing out of the producer  
| consumptionEnergySum | Number:Energy | Total energy in watt hours 

### Inverter, MyReserveInverter, SunSpecInverter

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerACOutMax | Number:Power | Maximum power production 
| powerACOutLimit | Number:Power | Limit of power production
| powerACOut | Number:Power | Power delivered by the inverter
| workACOut | Number:Energy | Energy delivered by the inverter
| powerInstallledPeak | Number:Power | Technical peak power available 

### BatteryConverter, MyReserve

All of *Inverter* plus

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerACIn | Number:Power | Power fed into battery
| workACIn | Number:Energy | Energy fed into battery
| stateOfCharge | Number | Charging state of battery in percent
| stateOfHealth | Number | Internal health metric in percent
| temperatureBattery | Number:Temperature | Temperature of the battery in celsius
| modeConverter | Switch | Current mode of converter. *ON* or *OFF*
| voltageBatteryCellMin | Number:Voltage | minimum voltage of all batteries
| voltageBatteryCellMean | Number:Voltage | mean voltage of all batteries
| voltageBatteryCellMax | Number:Voltage | maximum voltage of all batteries

### EVStation, KebaEv

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| powerACIn | Number:Power | Power consumed by the charger
| workACIn | Number:Energy | Energy consumed by the charger
| workACInSession | Number:Energy | Work consumed during current/last charging session
| modeStation | String | Current mode of the charger. One off *STANDBY*, *CHARGING*, *OFF*
| connectivityStatus | String | Current state of the charging connection. One off *ONLINE* or *OFFLINE*

### GridFlow

| Channel Type ID | Item Type | Description |
|-----------------|-----------|-------------|
| feedInLimit | Number:Dimensionless | Current derating setting in percent
