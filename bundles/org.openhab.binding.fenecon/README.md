# FENECON Binding

The FENECON Binding integrates the [FENECON energy storage system](https://fenecon.de/) device into the openHAB system via [REST-API](https://docs.fenecon.de/_/de/fems/fems-app/OEM_App_REST_JSON.html).

With the binding, it is possible to request status information from FENECON Home to allow you home automation decisions based on the current energy management.

This makes it possible, for example, to switch on other consumers such as the dishwasher or washing machine in the case of power overproduction.

## Supported Things

Only one Thing is supported: The `fenecon` device.

This Binding was tested with an [FENECON HOME 10](https://fenecon.de/fenecon-home-10/) device.

## Discovery

Auto-discovery is not supported.

## Thing Configuration

The FENECON Thing only needs to be configured with the `hostname`, all other parameters are optional and prefilled with the suitable default values:

| Parameter       | Description                                                                      |
|-----------------|----------------------------------------------------------------------------------|
| hostname        | Hostname or IP address of the FENECON device, e.g. 192.168.1.11                  |
| password        | Password of the FENECON device. The password for guest access is set by default. |
| port            | Port of the FENECON device. Default: 8084                                        |
| refreshInterval | Interval the device is polled in sec. Default 30 seconds                         |


## Channels

The FENECON binding currently only provides access to read out the values from the energy storage system. 

| Channel                | Type                 | Read/Write | Description                                                                 |
|------------------------|----------------------|------------|-----------------------------------------------------------------------------|
| state                  | String               | R          | FENECON system state: Ok, Info, Warning or Fault                            |
| lastUpdate             | DateTime             | R          | Last successful update via REST-API from the FENECON system                 |
| essSoc                 | Number:Dimensionless | R          | Battery state of charge in percent                                          |
| chargerPower           | Number:Power         | R          | Current charger power of energy storage system in watt.                     |
| dischargerPower        | Number:Power         | R          | Current discharger power of energy storage system in watt.                  |
| emergencyPowerMode     | Switch               | R          | Indicates if there is grid power is off and the emergency power mode is on. |
| productionActivePower  | Number:Power         | R          | Current active power producer load in watt.                                 |
| sellToGridPower        | Number:Power         | R          | Current sell to grid exchange power in watt.                                |
| sellToGridEnergy       | Number:Energy        | R          | Total energy exported to the grid in watt per hour.                         |
| consumptionActivePower | Number:Power         | R          | Current active power consumer load in watt.                                 |
| buyFromGridPower       | Number:Power         | R          | Current buy from grid exchange power in watt.                               |
| buyFromGridEnergy      | Number:Energy        | R          | Total energy imported from the grid in watt per hour.                       |


## Full Example

### fenecon.things

```java
Thing fenecon:fenecon:1 "FENECON Home" [hostname="192.168.1.11", refreshInterval=5]
```
### demo.items

```java
// Sitemap Items
Group   Home                    "MyHome"              <house>                                 ["Indoor"]
Group   GF                      "GroundFloor"         <groundfloor>          (Home)           ["GroundFloor"]
// Utility room
Group   GF_UtilityRoom          "Utility room"        <energy>               (Home, GF)       ["Room"]
Group   GF_UtilityRoomSolar     "Utility room solar"  <solarplant>           (GF_UtilityRoom) ["Inverter"]

// FENECON items
String               EssState               <text>         (GF_UtilityRoomSolar) ["Status"]                {channel="fenecon:fenecon:1:state"}
DateTime             LastFeneconUpdate      <time>         (GF_UtilityRoomSolar) ["Status"]                {channel="fenecon:fenecon:1:lastUpdate"}
Number:Dimensionless EssSoc                 <batterylevel> (GF_UtilityRoomSolar) ["Measurement"]           {unit="%", channel="fenecon:fenecon:1:essSoc"}
Number:Power         ChargerPower           <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:fenecon:1:chargerPower"}
Number:Power         DischargerPower        <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:fenecon:1:dischargerPower"}
Switch               EmergencyPowerMode     <switch>       (GF_UtilityRoomSolar) ["Switch"]                {channel="fenecon:fenecon:1:emergencyPowerMode"}

Number:Power         ProductionActivePower  <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:fenecon:1:productionActivePower"}
Number:Power         SellToGridPower        <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:fenecon:1:sellToGridPower"}
Number:Energy        TotalSellEnergy        <energy>       (GF_UtilityRoomSolar) ["Measurement", "Energy"] {channel="fenecon:fenecon:1:sellToGridEnergy"}

Number:Power         ConsumptionActivePower <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:fenecon:1:consumptionActivePower"}
Number:Power         BuyFromGridPower       <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:fenecon:1:buyFromGridPower"}
Number:Energy        TotalBuyEnergy         <energy>       (GF_UtilityRoomSolar) ["Measurement", "Energy"] {channel="fenecon:fenecon:1:buyFromGridEnergy"}

// Examples of items for calculating the energy purchased and sold. Look at the demo.rules section. 
Number:Currency      SoldEnergy "Total sold energy [%.2f €]"           <price> (GF_UtilityRoomSolar)
Number:Currency      PurchasedEnergy "Total purchased energy [%.2f €]" <price> (GF_UtilityRoomSolar)


```

### demo.sitemap

```perl
sitemap demo label="FENECON Example Sitemap" {
    Frame label="Groundfloor" icon="groundfloor" {
        Group item=GF_UtilityRoom
    }
}
```

### demo.rules

```java
rule "Blackout detection"
when
  Item EmergencyPowerMode changed to ON
then
    val msg = "🚨 Power blackout detected, emergency power mode running." 
    logInfo("PowerBlackout", msg)
    sendBroadcastNotification(msg)
end

rule "Battery 100 percent"
when
  Item EssSoc changed
then
    var batteryState = (EssSoc.getState() as Number).intValue()
    if(batteryState == 100){
        val msg = "🔋 Full battery, consumers can be activated."
        logInfo("FullBattery", msg)
        sendBroadcastNotification(msg)
    }
end

rule "Calculation sold energy"
when
  Item TotalSellEnergy changed
then
    val sellingPricePerKiloWattHour = 0.07 // €
    var current = (TotalSellEnergy.getState() as Number).intValue()
    var result = current * sellingPricePerKiloWattHour;
    SoldEnergy.postUpdate(result)
end

rule "Calculation purchased energy"
when
  Item TotalBuyEnergy changed
then
    val purchasedPricePerKiloWattHour = 0.32 // €
    var current = (TotalBuyEnergy.getState() as Number).intValue()
    var result = current * purchasedPricePerKiloWattHour;
    PurchasedEnergy.postUpdate(result)
end
```

