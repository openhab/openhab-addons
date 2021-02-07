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

TODO: Generate list of channels