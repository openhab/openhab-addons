# evcc Binding

This binding integrates [evcc](https://evcc.io), an extensible **E**lectric **V**ehicle **C**harge **C**ontroller and home energy management system.
The binding is compatible to evcc [version 0.123.1](https://github.com/evcc-io/evcc/releases/tag/0.123.1) or newer and was tested with [version 0.125.0](https://github.com/evcc-io/evcc/releases/tag/0.125.0).

You can easily install and upgrade evcc on openHABian using `sudo openhabian-config`.

evcc controls your wallbox(es) with multiple charging modes and allows you to charge your ev with your photovoltaik's excess current.
To provide an intelligent charging control, evcc supports over 30 wallboxes and over 20 energy meters/home energy management systems from many manufacturers as well as electric vehicles from over 20 car manufacturers.
Furthermore, evcc calculates your money savings.

This binding enables openHAB to retrieve status data from your evcc installation and to control the charging process.
For more advanced features like calculated savings, you have to visit the web UI of evcc.

## Supported Things

- `device`: A running evcc installation.

## Discovery

No auto discovery supported.

## Thing Configuration

### `device` Thing Configuration

| Parameter       | Type   | Description                                              | Advanced | Required |
|-----------------|--------|----------------------------------------------------------|----------|----------|
| url             | String | URL of evcc web UI, e.g. `https://demo.evcc.io`          | No       | Yes      |
| refreshInterval | Number | Interval the status is polled in seconds (minimum is 15) | Yes      | No       |

Default value for _refreshInterval_ is 60 seconds.

## Channels

### General Channels

Those channels exist only once.
Please note that some of them are only available when evcc is properly configured.

| Channel                         | Type                 | Read/Write | Description                                                                                                                                                        |
|---------------------------------|----------------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| general#batteryCapacity         | Number:Energy        | R          | Capacity of (home) battery                                                                                                                                         |
| general#batteryPower            | Number:Power         | R          | Current power from battery                                                                                                                                         |
| general#batterySoC              | Number:Dimensionless | R          | Current State of Charge of battery                                                                                                                                 |
| general#batteryDischargeControl | Switch               | RW         | Enable or disable battery discharge control                                                                                                                        |
| general#batteryMode             | String               | R          | Current battery mode                                                                                                                                               |
| general#prioritySoC             | Number:Dimensionless | RW         | State of State of Charge for which the battery has priority over charging the ev when charging mode is "pv"                                                        |
| general#bufferSoC               | Number:Dimensionless | RW         | Until this State of Charge the discharging of a house battery is allowed in "pv" mode, when there is insufficient solar surplus (below the minimum charging power) |
| general#bufferStartSoC          | Number:Dimensionless | RW         | State of Charge for which a charging session in "pv" mode is started, even if there is insufficient solar surplus                                                  |
| general#residualPower           | Number:Power         | RW         | Target operating point of the surplus regulation at the grid connection (grid meter)                                                                               |
| general#gridPower               | Number:Power         | R          | Current power from grid (negative means feed-in)                                                                                                                   |
| general#homePower               | Number:Power         | R          | Current power taken by home                                                                                                                                        |
| general#pvPower                 | Number:Power         | R          | Current power from photovoltaik                                                                                                                                    |
| general#version                 | String               | R          | Current evcc version                                                                                                                                               |
| general#availableVersion        | String               | R          | Available evcc update version                                                                                                                                      |

### Loadpoint Channels

Those channels exist per configured loadpoint.
Please note that you have to replace _\<N\>_ with your loadpoint id/number.

| Channel                                       | Type                   | Read/Write | Description                                                                                                       |
|-----------------------------------------------|------------------------|------------|-------------------------------------------------------------------------------------------------------------------|
| loadpoint\<N\>#activePhases                   | Number                 | R          | Current number of active phases while charging                                                                    |
| loadpoint\<N\>#chargeCurrent                  | Number:ElectricCurrent | R          | Current amperage per connected phase while charging                                                               |
| loadpoint\<N\>#chargeDuration                 | Number:Time            | R          | Charging duration                                                                                                 |
| loadpoint\<N\>#chargeRemainingDuration        | Number:Time            | R          | Remaining duration until limit SoC is reached                                                                     |
| loadpoint\<N\>#chargeRemainingEnergy          | Number:Energy          | R          | Remaining energy until limit SoC is reached                                                                       |
| loadpoint\<N\>#chargePower                    | Number:Power           | R          | Current power of charging                                                                                         |
| loadpoint\<N\>#chargedEnergy                  | Number:Energy          | R          | Energy charged since plugged-in                                                                                   |
| loadpoint\<N\>#charging                       | Switch                 | R          | Loadpoint is currently charging                                                                                   |
| loadpoint\<N\>#enabled                        | Switch                 | R          | Charging enabled (mode is not "off")                                                                              |
| loadpoint\<N\>#maxCurrent                     | Number:ElectricCurrent | RW         | Maximum amperage per connected phase with which the car should be charged                                         |
| loadpoint\<N\>#minCurrent                     | Number:ElectricCurrent | RW         | Minimum amperage per connected phase with which the car should be charged                                         |
| loadpoint\<N\>#mode                           | String                 | RW         | Charging mode: "off", "now", "minpv", "pv"                                                                        |
| loadpoint\<N\>#phases                         | Number                 | RW         | The maximum number of phases which can be used                                                                    |
| loadpoint\<N\>#limitEnergy                    | Number:Energy          | RW         | Amount of energy to charge the vehicle with                                                                       |
| loadpoint\<N\>#title                          | String                 | R          | Title of loadpoint                                                                                                |
| loadpoint\<N\>#vehicleConnected               | Switch                 | R          | Whether vehicle is connected to loadpoint                                                                         |
| loadpoint\<N\>#vehicleConnectedDuration       | Number:Time            | R          | Duration the vehicle is connected to loadpoint                                                                    |
| loadpoint\<N\>#vehicleOdometer                | Number:Length          | R          | Total distance travelled by EV                                                                                    |
| loadpoint\<N\>#vehiclePresent                 | Switch                 | R          | Whether evcc is able to get data from vehicle                                                                     |
| loadpoint\<N\>#vehicleRange                   | Number:Length          | R          | Battery range for EV                                                                                              |
| loadpoint\<N\>#vehicleName                    | String                 | R          | The unique identifier of the EV used in the evcc configuration (containing no whitespaces nor special characters) |
| loadpoint\<N\>#chargerFeatureHeating          | Switch                 | R          | 'True' for heating device: State of Charge in Degree instead of Percent                                           |
| loadpoint\<N\>#chargerFeatureIntegratedDevice | Switch                 | R          | 'True' for integrated device: Operate without a "vehicle" (e.g. heat pump, eBike)                                 |

#### Loadpoint Channels Specific for Vehicles

| Channel                          | Type                 | Read/Write | Description                                                             |
|----------------------------------|----------------------|------------|-------------------------------------------------------------------------|
| loadpoint\<N\>#limitSoC          | Number:Dimensionless | RW         | Until which state of charge (SoC) should the vehicle be charged         |
| loadpoint\<N\>#effectiveLimitSoC | Number:Dimensionless | R          | Effective state of charge (SoC) until which the vehicle will be charged |
| loadpoint\<N\>#vehicleSoC        | Number:Dimensionless | R          | Current State of Charge of EV                                           |

#### Loadpoint Channels Specific for Heating Devices

| Channel                                  | Type               | Read/Write | Description                                                          |
|------------------------------------------|--------------------|------------|----------------------------------------------------------------------|
| loadpoint\<N\>#limitTemperature          | Number:Temperature | RW         | Until which Temperature should the heating device be charged         |
| loadpoint\<N\>#effectiveLimitTemperature | Number:Temperature | R          | Effective Temperature until which the heating device will be charged |
| loadpoint\<N\>#vehicleTemperature        | Number:Temperature | R          | Current Temperature of the heating device                            |

### Vehicle Channels

Those channels exist:

- 1 per configured loadpoint with `chargerFeatureHeating = false`:
  - These channels point to the heating device that is currently active/connected at/to the loadpoint
  - Please note that you have to replace _\<N\>_ with your loadpoint id/number
- 1 per configured vehicle:
  - Please note that you have to replace _\<ID\>_ with your vehicle id/name

| Channel                                            | Type                 | Read/Write | Description                                                              |
|----------------------------------------------------|----------------------|------------|--------------------------------------------------------------------------|
| [loadpoint\<N\>\|vehicle\<ID\]>#vehicleTitle       | String               | R          | Title of vehicle                                                         |
| [loadpoint\<N\>\|vehicle\<ID\]>#vehicleMinSoC      | Number:Dimensionless | RW         | Minimum state of charge (SoC) a vehicle should have                      |
| [loadpoint\<N\>\|vehicle\<ID\]>#vehicleLimitSoC    | Number:Dimensionless | RW         | Until which state of charge (SoC) should the specific vehicle be charged |
| [loadpoint\<N\>\|vehicle\<ID\]>#vehicleCapacity    | Number:Energy        | R          | Capacity of EV battery                                                   |
| [loadpoint\<N\>\|vehicle\<ID\]>#vehiclePlanEnabled | Switch               | RW         | Plan for charging enabled                                                |
| [loadpoint\<N\>\|vehicle\<ID\]>#vehiclePlanSoC     | Number:Dimensionless | RW         | Until which state of charge (SoC) should vehicle be charged in plan      |
| [loadpoint\<N\>\|vehicle\<ID\]>#vehiclePlanTime    | DateTime             | RW         | When the plan SoC should be reached                                      |

### Heating Channels

Those channels exist:

- 1 per configured loadpoint with `chargerFeatureHeating = true`:
  - These channels point to the heating device that is currently active/connected at/to the loadpoint
  - Please note that you have to replace _\<N\>_ with your loadpoint id/number
- 1 per configured heating device:
  - Please note that you have to replace _\<ID\>_ with your heating device id/name

| Channel                                                 | Type               | Read/Write | Description                                                           |
|---------------------------------------------------------|--------------------|------------|-----------------------------------------------------------------------|
| [loadpoint\<N\>\|heating\<ID\]>#heatingTitle            | String             | R          | Title of heating device                                               |
| [loadpoint\<N\>\|heating\<ID\]>#heatingMinTemperature   | Number:Temperature | RW         | Minimum Temperature a heating device should have                      |
| [loadpoint\<N\>\|heating\<ID\]>#heatingLimitTemperature | Number:Temperature | RW         | Until which Temperature should the specific heating device be charged |
| [loadpoint\<N\>\|heating\<ID\]>#heatingCapacity         | Number:Energy      | R          | Capacity of heating device                                            |
| [loadpoint\<N\>\|heating\<ID\]>#heatingPlanEnabled      | Switch             | RW         | Plan for charging enabled                                             |
| [loadpoint\<N\>\|heating\<ID\]>#heatingPlanTemperature  | Number:Temperature | RW         | Until which Temperature should heating device be charged in plan      |
| [loadpoint\<N\>\|heating\<ID\]>#heatingPlanTime         | DateTime           | RW         | When the plan Temperature should be reached                           |

## Full Example

### Thing(s)

```java
Thing evcc:device:demo "evcc Demo" [url="https://demo.evcc.io", refreshInterval=60]
```

### Items

```java
// General
Number:Energy          evcc_batteryCapacity                           "Battery Capacity [%.0f kWh]"                        <energy>       {channel="evcc:device:demo:general#batteryCapacity"}
Number:Power           evcc_batteryPower                              "Battery Power [%.1f kW]"                            <energy>       {channel="evcc:device:demo:general#batteryPower"}
Number:Dimensionless   evcc_batterySoC                                "Battery SoC [%d %%]"                                <batterylevel> {channel="evcc:device:demo:general#batterySoC"}
Switch                 evcc_batteryDischargeControl                   "Battery Discharge Control [%s]"                     <switch>       {channel="evcc:device:demo:general#batteryDischargeControl"}
String                 evcc_batteryMode                               "Battery Mode [%s]"                                  <battery>      {channel="evcc:device:demo:general#batteryMode"}
Number:Dimensionless   evcc_prioritySoC                               "Battery Priority SoC [%d %%]"                       <batterylevel> {channel="evcc:device:demo:general#prioritySoC"}
Number:Dimensionless   evcc_bufferSoC                                 "Battery Buffer SoC [%d %%]"                         <batterylevel> {channel="evcc:device:demo:general#bufferSoC"}
Number:Dimensionless   evcc_bufferStartSoC                            "Battery Buffer Start SoC [%d %%]"                   <batterylevel> {channel="evcc:device:demo:general#bufferStartSoC"}
Number:Power           evcc_residualPower                             "Grid Residual Power [%.1f kW]"                      <energy>       {channel="evcc:device:demo:general#residualPower"}
Number:Power           evcc_gridPower                                 "Grid Power [%.1f kW]"                               <energy>       {channel="evcc:device:demo:general#gridPower"}
Number:Power           evcc_homePower                                 "Home Power [%.1f kW]"                               <energy>       {channel="evcc:device:demo:general#homePower"}
Number:Power           evcc_pvPower                                   "PV Power [%.1f kW]"                                 <energy>       {channel="evcc:device:demo:general#pvPower"}
String                 evcc_version                                   "Version [%s]"                                       <text>         {channel="evcc:device:demo:general#version"}
String                 evcc_availableVersion                          "Available Version [%s]"                             <text>         {channel="evcc:device:demo:general#availableVersion"}

// Loadpoint
Number                 evcc_loadpoint0_activePhases                   "Active Phases [%d]"                                                {channel="evcc:device:demo:loadpoint0#activePhases"}
Number:ElectricCurrent evcc_loadpoint0_chargeCurrent                  "Charging current [%.0f A]"                          <energy>       {channel="evcc:device:demo:loadpoint0#chargeCurrent"}
Number:Time            evcc_loadpoint0_chargeDuration                 "Charging duration [%1$tH:%1$tM]"                    <time>         {channel="evcc:device:demo:loadpoint0#chargeDuration"}
Number:Time            evcc_loadpoint0_chargeRemainingDuration        "Charging remaining duration [%1$tH:%1$tM]"          <time>         {channel="evcc:device:demo:loadpoint0#chargeRemainingDuration"}
Number:Energy          evcc_loadpoint0_chargeRemainingEnergy          "Charging remaining energy [%.1f kWh]"               <energy>       {channel="evcc:device:demo:loadpoint0#chargeRemainingEnergy"}
Number:Power           evcc_loadpoint0_chargePower                    "Charging power [%.1f kW]"                           <energy>       {channel="evcc:device:demo:loadpoint0#chargePower"}
Number:Energy          evcc_loadpoint0_chargedEnergy                  "Charged energy [%.1f kWh]"                          <energy>       {channel="evcc:device:demo:loadpoint0#chargedEnergy"}
Switch                 evcc_loadpoint0_charging                       "Currently charging [%s]"                            <battery>      {channel="evcc:device:demo:loadpoint0#charging"}
Switch                 evcc_loadpoint0_enabled                        "Charging enabled [%s]"                              <switch>       {channel="evcc:device:demo:loadpoint0#enabled"}
Number:ElectricCurrent evcc_loadpoint0_maxCurrent                     "Maximum current [%.0f A]"                           <energy>       {channel="evcc:device:demo:loadpoint0#maxCurrent"}
Number:ElectricCurrent evcc_loadpoint0_minCurrent                     "Minimum current [%.0f A]"                           <energy>       {channel="evcc:device:demo:loadpoint0#minCurrent"}
String                 evcc_loadpoint0_mode                           "Mode [%s]"                                                         {channel="evcc:device:demo:loadpoint0#mode"}
Number                 evcc_loadpoint0_phases                         "Enabled phases [%d]"                                               {channel="evcc:device:demo:loadpoint0#phases"}
Number:Energy          evcc_loadpoint0_limitEnergy                    "Limit energy [%.1f kWh]"                            <batterylevel> {channel="evcc:device:demo:loadpoint0#limitEnergy"}
Number:Dimensionless   evcc_loadpoint0_limitSoC                       "Limit SoC [%d %%]"                                  <batterylevel> {channel="evcc:device:demo:loadpoint0#limitSoC"}
Number:Dimensionless   evcc_loadpoint0_effectiveLimitSoC              "Effective Limit SoC [%d %%]"                        <batterylevel> {channel="evcc:device:demo:loadpoint0#effectiveLimitSoC"}
String                 evcc_loadpoint0_title                          "Loadpoint title [%s]"                               <text>         {channel="evcc:device:demo:loadpoint0#title"}
Switch                 evcc_loadpoint0_chargerFeatureHeating          "Feature: Heating [%s]"                              <switch>       {channel="evcc:device:demo:loadpoint0#chargerFeatureHeating"}
Switch                 evcc_loadpoint0_chargerFeatureIntegratedDevice "Feature: Integrated Device [%s]"                    <switch>       {channel="evcc:device:demo:loadpoint0#chargerFeatureIntegratedDevice"}

// Loadpoint vehicle channels
Switch                 evcc_loadpoint0_vehicleConnected               "Vehicle connected [%s]"                             <switch>       {channel="evcc:device:demo:loadpoint0#vehicleConnected"}
Number:Time            evcc_loadpoint0_vehicleConnectedDuration       "Vehicle connected duration [%.1f h]"                <time>         {channel="evcc:device:demo:loadpoint0#vehicleConnectedDuration"}
Number:Length          evcc_loadpoint0_vehicleOdometer                "Vehicle odometer [%.1f km]"                                        {channel="evcc:device:demo:loadpoint0#vehicleOdometer"}
Switch                 evcc_loadpoint0_vehiclePresent                 "Vehicle present [%s]"                               <switch>       {channel="evcc:device:demo:loadpoint0#vehiclePresent"}
Number:Length          evcc_loadpoint0_vehicleRange                   "Vehicle Range [%.0f km]"                                           {channel="evcc:device:demo:loadpoint0#vehicleRange"}
Number:Dimensionless   evcc_loadpoint0_vehicleSoC                     "Vehicle SoC [%d %%]"                                <batterylevel> {channel="evcc:device:demo:loadpoint0#vehicleSoC"}
String                 evcc_loadpoint0_VehicleName                    "Vehicle name [%s]"                                  <text>         {channel="evcc:device:demo:loadpoint0#vehicleName"}

// Vehicle on loadpoint
String                 evcc_loadpoint0current_vehicleTitle            "Vehicle title [%s]"                                 <text>         {channel="evcc:device:demo:loadpoint0current#vehicleTitle"}
Number:Dimensionless   evcc_loadpoint0current_vehicleMinSoC           "Vehicle minimum SoC [%d %%]"                        <batterylevel> {channel="evcc:device:demo:loadpoint0current#vehicleMinSoC"}
Number:Dimensionless   evcc_loadpoint0current_vehicleLimitSoC         "Vehicle limit SoC [%d %%]"                          <batterylevel> {channel="evcc:device:demo:loadpoint0current#vehicleLimitSoC"}
Number:Energy          evcc_loadpoint0current_vehicleCapacity         "Vehicle capacity [%.0f kWh]"                        <batterylevel> {channel="evcc:device:demo:loadpoint0current#vehicleCapacity"}
Switch                 evcc_loadpoint0current_vehiclePlanEnabled      "Vehicle plan enabled [%s]"                          <switch>       {channel="evcc:device:demo:loadpoint0current#vehiclePlanEnabled"}
Number:Dimensionless   evcc_loadpoint0current_vehiclePlanSoC          "Vehicle plan SoC [%d %%]"                           <batterylevel> {channel="evcc:device:demo:loadpoint0current#vehiclePlanSoC"}
DateTime               evcc_loadpoint0current_vehiclePlanTime         "Vehicle plan time [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]" <time>         {channel="evcc:device:demo:loadpoint0current#vehiclePlanTime"}
```

### Sitemap

```perl
sitemap evcc label="evcc Demo" {
    Frame label="General" {
        Text item=evcc_batteryPower
        Text item=evcc_batterySoC
        Text item=evcc_gridPower
        Text item=evcc_homePower
        Text item=evcc_pvPower
    }
    Frame label="Loadpoint 0" {
        Text item=evcc_loadpoint0_title
        Text item=evcc_loadpoint0_enabled label="Charging" {
            Text item=evcc_loadpoint0_charging
            Text item=evcc_loadpoint0_chargePower
            Text item=evcc_loadpoint0_chargeCurrent
            Text item=evcc_loadpoint0_activePhases
            Text item=evcc_loadpoint0_chargeDuration
            Text item=evcc_loadpoint0_chargeRemainingDuration
            Text item=evcc_loadpoint0_chargeRemainingEnergy
        }
        Switch item=evcc_loadpoint0_mode mappings=["off"="Stop","now"="Now","minpv"="Min + PV", "pv"="Only PV"]
        Text label="Charging settings" icon="settings" {
            Setpoint item=evcc_loadpoint0_limitEnergy minValue=5 maxValue=100 step=5
            Setpoint item=evcc_loadpoint0_limitSoC minValue=5 maxValue=100 step=5
            Setpoint item=evcc_loadpoint0_minCurrent minValue=6 maxValue=96 step=2
            Setpoint item=evcc_loadpoint0_maxCurrent minValue=6 maxValue=96 step=2
            Setpoint item=evcc_loadpoint0_minSoC minValue=0 maxValue=100 step=5
            Setpoint item=evcc_loadpoint0_phases minValue=1 maxValue=3 step=2
        }
        Text item=evcc_loadpoint0_vehicleName label="Vehicle" {
            Text item=evcc_loadpoint0current_vehicleCapacity
            Text item=evcc_loadpoint0_vehicleOdometer
            Text item=evcc_loadpoint0_vehicleRange
            Text item=evcc_loadpoint0_vehicleSoC
            Text item=evcc_loadpoint0current_vehicleTitle
            Setpoint item=evcc_loadpoint0current_vehicleMinSoC minValue=0 maxValue=100 step=5
            Setpoint item=evcc_loadpoint0current_vehicleLimitSoC minValue=5 maxValue=100 step=5
            Switch item=evcc_loadpoint0current_vehiclePlanEnabled
            Setpoint item=evcc_loadpoint0current_vehiclePlanSoC minValue=5 maxValue=100 step=5
            Input item=evcc_loadpoint0current_vehiclePlanTime
        }
    }
}
```
