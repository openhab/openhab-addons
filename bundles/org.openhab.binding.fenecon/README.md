# FENECON Binding

The FENECON Binding integrates the [FENECON energy storage system](https://fenecon.de/) device into the openHAB system via [REST-API](https://docs.fenecon.de/_/de/fems/fems-app/OEM_App_REST_JSON.html).

With the binding, it is possible to request status information from FENECON Home to allow you home automation decisions based on the current energy management.

This makes it possible, for example, to switch on other consumers such as the dishwasher or washing machine in the case of power overproduction.

## Supported Things

Currently only one Thing is supported: The `home-device` connection to the FENECON energy storage system.

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

| Channel                       | Type                       | Read/Write | Description                                                                    |
|-------------------------------|----------------------------|------------|--------------------------------------------------------------------------------|
| state                         | String                     | R          | FENECON system state: Ok, Info, Warning or Fault                               |
| fems-version                  | String                     | R          | FENECON energy management system (FEMS) version - e.g 2025.2.3                 |
| last-update                   | DateTime                   | R          | Last successful update via REST-API from the FENECON system                    |
| ess-soc                       | Number:Dimensionless       | R          | Battery state of charge.                                                       |
| batt-tower-soh                | Number:Dimensionless       | R          | Battery state of health.                                                       |
| charger-power                 | Number:Power               | R          | Current charger power of energy storage system.                                |
| discharger-power              | Number:Power               | R          | Current discharger power of energy storage system.                             |
| emergency-power-mode          | Switch                     | R          | Indicates if there is grid power is off and the emergency power mode is on.    |
| production-active-power       | Number:Power               | R          | Current active power producer load.                                            |
| production-max-active-power   | Number:Power               | R          | Maximum active production power that was measured.                             |
| export-to-grid-power          | Number:Power               | R          | Current export power to grid.                                                  |
| exported-to-grid-energy       | Number:Energy              | R          | Total energy exported to the grid.                                             |
| consumption-active-power      | Number:Power               | R          | Current active power consumer load.                                            |
| consumption-max-active-power  | Number:Power               | R          | Maximum active consumption power that was measured.                            |
| consumption-active-power-l1   | Number:Power               | R          | Current active power consumer load on phase 1.                                 |
| consumption-active-power-l2   | Number:Power               | R          | Current active power consumer load on phase 2.                                 |
| consumption-active-power-l3   | Number:Power               | R          | Current active power consumer load on phase 3.                                 |
| import-from-grid-power        | Number:Power               | R          | Current import power from grid.                                                |
| imported-from-grid-energy     | Number:Energy              | R          | Total energy imported from the grid.                                           |
| inverter-air-temperature      | Number:Temperature         | R          | Air temperature at the inverter.                                               |
| inverter-radiator-temperature | Number:Temperature         | R          | Radiator temperature of the inverter.                                          |
| bms-pack-temperature          | Number:Temperature         | R          | Temperature in the battery management system (BMS) box.                        |
| batt-tower-voltage            | Number:ElectricPotential   | R          | Battery voltage of the FENECON energy management system (FEMS).                |
| batt-tower-current            | Number:ElectricCurrent     | R          | Battery current of the FENECON energy management system (FEMS).                |
| charger0-actual-power         | Number:Power               | R          | Charger actual power on the charger 0 - e.g west roof, if available.           |
| charger0-voltage              | Number:ElectricPotential   | R          | Charger voltage on the charger 0 - e.g west roof, if available.                |
| charger0-current              | Number:ElectricCurrent     | R          | Charger current on the charger 0 - e.g west roof, if available.                |
| charger1-actual-power         | Number:Power               | R          | Charger actual power on the charger 1 - e.g east roof, if available.           |
| charger1-voltage              | Number:ElectricPotential   | R          | Charger voltage on the charger 1 - e.g east roof, if available.                |
| charger1-current              | Number:ElectricCurrent     | R          | Charger current on the charger 1 - e.g east roof, if available.                |
| charger2-actual-power         | Number:Power               | R          | Charger actual power on the charger 2 - e.g south roof, if available.          |
| charger2-voltage              | Number:ElectricPotential   | R          | Charger voltage on the charger 2 - e.g south roof, if available.               |
| charger2-current              | Number:ElectricCurrent     | R          | Charger current on the charger 2 - e.g south roof, if available.               |

## Full Example

### fenecon.things

```java
Thing fenecon:home-device:local "FENECON Home" [hostname="192.168.1.11", refreshInterval=30]
```

### demo.items

```java
// Sitemap Items
Group   Home                    "MyHome"              <house>                                 ["Indoor"]
Group   GF                      "GroundFloor"         <groundfloor>          (Home)           ["GroundFloor"]
// Utility room
Group   GF_UtilityRoom          "Utility room"        <energy>               (GF)             ["Room"]
Group   GF_UtilityRoomSolar     "Utility room solar"  <solarplant>           (GF_UtilityRoom) ["Inverter"]

// FENECON items
String                     EssState                       <text>         (GF_UtilityRoomSolar) ["Status"]                {channel="fenecon:home-device:local:state"}
String                     FemsVersion                    <text>         (GF_UtilityRoomSolar) ["Status"]                {channel="fenecon:home-device:local:fems-version"}
DateTime                   LastFeneconUpdate              <time>         (GF_UtilityRoomSolar) ["Status"]                {channel="fenecon:home-device:local:last-update"}

Number:Dimensionless       EssSoc                         <batterylevel> (GF_UtilityRoomSolar) ["Measurement"]           {unit="%", channel="fenecon:home-device:local:ess-soc"}
Number:Dimensionless       BattSoh                        <batterylevel> (GF_UtilityRoomSolar) ["Measurement"]           {unit="%", channel="fenecon:home-device:local:batt-tower-soh"}
Number:Power               ChargerPower                   <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:charger-power"}
Number:Power               DischargerPower                <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:discharger-power"}
Switch                     EmergencyPowerMode             <switch>       (GF_UtilityRoomSolar) ["Switch"]                {channel="fenecon:home-device:local:emergency-power-mode"}

Number:Power               ProductionActivePower          <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:production-active-power"}
Number:Power               ProductionMaxActivePower       <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:production-max-active-power"}
Number:Power               SellToGridPower                <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:export-to-grid-power"}
Number:Energy              TotalSellEnergy                <energy>       (GF_UtilityRoomSolar) ["Measurement", "Energy"] {channel="fenecon:home-device:local:exported-to-grid-energy"}

Number:Power               ConsumptionActivePower         <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:consumption-active-power"}
Number:Power               ConsumptionMaxActivePower      <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:consumption-max-active-power"}
Number:Power               ConsumptionActivePowerPhase1   <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:consumption-active-power-l1"}
Number:Power               ConsumptionActivePowerPhase2   <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:consumption-active-power-l2"}
Number:Power               ConsumptionActivePowerPhase3   <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:consumption-active-power-l3"}

Number:Power               BuyFromGridPower               <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]  {channel="fenecon:home-device:local:import-from-grid-power"}
Number:Energy              TotalBuyEnergy                 <energy>       (GF_UtilityRoomSolar) ["Measurement", "Energy"] {channel="fenecon:home-device:local:imported-from-grid-energy"}

Number:Temperature         InverterAirTemp                <temperature>  (GF_UtilityRoomSolar) ["Measurement", "Temperature"] {channel="fenecon:home-device:local:inverter-air-temperature"}
Number:Temperature         InverterRadiatorTemp           <temperature>  (GF_UtilityRoomSolar) ["Measurement", "Temperature"] {channel="fenecon:home-device:local:inverter-radiator-temperature"}
Number:Temperature         BmsBoxTemp                     <temperature>  (GF_UtilityRoomSolar) ["Measurement", "Temperature"] {channel="fenecon:home-device:local:bms-pack-temperature"}

Number:ElectricPotential   BattTowerVoltage               <energy>       (GF_UtilityRoomSolar) ["Measurement", "Voltage"] {channel="fenecon:home-device:local:batt-tower-voltage"}
Number:ElectricCurrent     BattTowerCurrent               <energy>       (GF_UtilityRoomSolar) ["Measurement", "Current"] {channel="fenecon:home-device:local:batt-tower-current"}

// Charger corresponds to the solar power plant on the roof.
Number:Power               ChargerWestActualPower         <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]    {channel="fenecon:home-device:local:charger0-actual-power"}
Number:ElectricPotential   ChargerWestVoltage             <energy>       (GF_UtilityRoomSolar) ["Measurement", "Voltage"]  {channel="fenecon:home-device:local:charger0-voltage"}
Number:ElectricCurrent     ChargerWestCurrent             <energy>       (GF_UtilityRoomSolar) ["Measurement", "Current"]  {channel="fenecon:home-device:local:charger0-current"}
Number:Power               ChargerEastActualPower         <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]    {channel="fenecon:home-device:local:charger1-actual-power"}
Number:ElectricPotential   ChargerEastVoltage             <energy>       (GF_UtilityRoomSolar) ["Measurement", "Voltage"]  {channel="fenecon:home-device:local:charger1-voltage"}
Number:ElectricCurrent     ChargerEastCurrent             <energy>       (GF_UtilityRoomSolar) ["Measurement", "Current"]  {channel="fenecon:home-device:local:charger1-current"}
Number:Power               ChargerSouthActualPower        <energy>       (GF_UtilityRoomSolar) ["Measurement", "Power"]    {channel="fenecon:home-device:local:charger2-actual-power"}
Number:ElectricPotential   ChargerSouthVoltage            <energy>       (GF_UtilityRoomSolar) ["Measurement", "Voltage"]  {channel="fenecon:home-device:local:charger2-voltage"}
Number:ElectricCurrent     ChargerSouthCurrent            <energy>       (GF_UtilityRoomSolar) ["Measurement", "Current"]  {channel="fenecon:home-device:local:charger2-current"}

```

### demo.sitemap

```perl
sitemap demo label="FENECON Example Sitemap" {
    Frame label="Groundfloor" icon="groundfloor" {
        Group item=GF_UtilityRoom
    }
}
```

### rrd4j.persist

```perl
Strategies {
    everyMinute : "0 * * * * ?"
    default = everyChange
}

Items {
    ProductionActivePower: strategy = everyUpdate, everyMinute, restoreOnStartup
    ConsumptionActivePower: strategy = everyUpdate, everyMinute, restoreOnStartup
    BuyFromGridPower: strategy = everyUpdate, everyMinute, restoreOnStartup
}
```

### demo.rules

:::: tabs

::: tab DSL

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

// !!! This is only designed as a demonstration, the calculation should only be executed every 30 or 60 minutes if necessary. And for the calculation, be sure to consider the persistence example: rrd4j.persist!
rule "Calculation daily power values"
when
  Item LastFeneconUpdate changed
then    
    var dailyMax = (ProductionActivePower.maximumSince(now.with(LocalTime.of(0,0,0,0))).state as Number).floatValue()
    MaxProductionActivePowerOfTheDay.postUpdate(dailyMax)

    var dailyProduction = (ProductionActivePower.sumSince(now.with(LocalTime.of(0,0,0,0))) as Number).floatValue() / 60 / 1000
    ProductionActivePowerOfTheDay.postUpdate(dailyProduction)

    var dailyConsumption = (ConsumptionActivePower.sumSince(now.with(LocalTime.of(0,0,0,0))) as Number).floatValue() / 60 / 1000
    ConsumptionActivePowerOfTheDay.postUpdate(dailyConsumption)

    var dailyBuyFromGrid = (BuyFromGridPower.sumSince(now.with(LocalTime.of(0,0,0,0))) as Number).floatValue() / 60 / 1000
    BuyFromGridPowerOfTheDay.postUpdate(dailyBuyFromGrid)

end
```

:::

::::
