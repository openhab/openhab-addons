# evcc Binding

This binding integrates [evcc - electric vehicle charging control](https://evcc.io), a project that provides a control center for electric vehicle charging.
The binding requires evcc [version 0.123.1](https://github.com/evcc-io/evcc/releases/tag/0.123.1) or newer and is tested with this version.

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

### General channels

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

### Loadpoint channels

Those channels exist per configured loadpoint.
Please note that you have to replace _N_ with your loadpoint number.

| Channel                             | Type                   | Read/Write | Description                                                                                                       |
|-------------------------------------|------------------------|------------|-------------------------------------------------------------------------------------------------------------------|
| loadpointN#activePhases             | Number                 | R          | Current number of active phases while charging                                                                    |
| loadpointN#chargeCurrent            | Number:ElectricCurrent | R          | Current amperage per connected phase while charging                                                               |
| loadpointN#chargeDuration           | Number:Time            | R          | Charging duration                                                                                                 |
| loadpointN#chargeRemainingDuration  | Number:Time            | R          | Remaining duration until limit SoC is reached                                                                     |
| loadpointN#chargeRemainingEnergy    | Number:Energy          | R          | Remaining energy until limit SoC is reached                                                                       |
| loadpointN#chargePower              | Number:Power           | R          | Current power of charging                                                                                         |
| loadpointN#chargedEnergy            | Number:Energy          | R          | Energy charged since plugged-in                                                                                   |
| loadpointN#charging                 | Switch                 | R          | Loadpoint is currently charging                                                                                   |
| loadpointN#enabled                  | Switch                 | R          | Charging enabled (mode is not "off")                                                                              |
| loadpointN#maxCurrent               | Number:ElectricCurrent | RW         | Maximum amperage per connected phase with which the car should be charged                                         |
| loadpointN#minCurrent               | Number:ElectricCurrent | RW         | Minimum amperage per connected phase with which the car should be charged                                         |
| loadpointN#mode                     | String                 | RW         | Charging mode: "off", "now", "minpv", "pv"                                                                        |
| loadpointN#phases                   | Number                 | RW         | The maximum number of phases which can be used                                                                    |
| loadpointN#limitEnergy              | Number:Energy          | RW         | Amount of energy to charge the vehicle with                                                                       |
| loadpointN#limitSoC                 | Number:Dimensionless   | RW         | Until which state of charge (SoC) should the vehicle be charged                                                   |
| loadpointN#title                    | String                 | R          | Title of loadpoint                                                                                                |
| loadpointN#vehicleConnected         | Switch                 | R          | Whether vehicle is connected to loadpoint                                                                         |
| loadpointN#vehicleConnectedDuration | Number:Time            | R          | Duration the vehicle is connected to loadpoint                                                                    |
| loadpointN#vehicleCapacity          | Number:Energy          | R          | Capacity of EV battery                                                                                            |
| loadpointN#vehicleOdometer          | Number:Length          | R          | Total distance travelled by EV                                                                                    |
| loadpointN#vehiclePresent           | Switch                 | R          | Whether evcc is able to get data from vehicle                                                                     |
| loadpointN#vehicleRange             | Number:Length          | R          | Battery range for EV                                                                                              |
| loadpointN#vehicleSoC               | Number:Dimensionless   | R          | Current State of Charge of EV                                                                                     |
| loadpointN#vehicleName              | String                 | R          | The unique identifier of the EV used in the evcc configuration (containing no whitespaces nor special characters) |

### Vehicle channels

Those channels exist per configured vehicle.
Please note that you have to replace _ID_ with your vehicle id/name.

| Channel                      | Type                 | Read/Write | Description                                                              |
|------------------------------|----------------------|------------|--------------------------------------------------------------------------|
| vehicleID#vehicleTitle       | String               | R          | Title of vehicle                                                         |
| vehicleID#vehicleMinSoC      | Number:Dimensionless | RW         | Minimum state of charge (SoC) a vehicle should have                      |
| vehicleID#vehicleLimitSoC    | Number:Dimensionless | RW         | Until which state of charge (SoC) should the specific vehicle be charged |
| vehicleID#vehiclePlanEnabled | Switch               | RW         | Plan for charging enabled                                                |
| vehicleID#vehiclePlanSoC     | Number:Dimensionless | RW         | Until which state of charge (SoC) should vehicle be charged in plan      |
| vehicleID#vehiclePlanTime    | DateTime             | RW         | When the plan SoC should be reached                                      |

## Full Example

### Thing(s)

```java
Thing evcc:device:demo "evcc Demo" [url="https://demo.evcc.io", refreshInterval=60]
```

### Items

```java
// General
Number:Energy             evcc_batteryCapacity                        "Battery Capacity [%.0f kWh]"                        <energy>          {channel="evcc:device:demo:general#batteryCapacity"}
Number:Power              evcc_batteryPower                           "Battery Power [%.1f kW]"                            <energy>          {channel="evcc:device:demo:general#batteryPower"}
Number:Dimensionless      evcc_batterySoC                             "Battery SoC [%d %%]"                                <batterylevel>    {channel="evcc:device:demo:general#batterySoC"}
Switch                    evcc_batteryDischargeControl                "Battery Discharge Control [%s]"                     <switch>          {channel="evcc:device:demo:general#batteryDischargeControl"}
String                    evcc_batteryMode                            "Battery Mode [%s]"                                  <battery>         {channel="evcc:device:demo:general#batteryMode"}
Number:Dimensionless      evcc_prioritySoC                            "Battery Priority SoC [%d %%]"                       <batterylevel>    {channel="evcc:device:demo:general#prioritySoC"}
Number:Dimensionless      evcc_bufferSoC                              "Battery Buffer SoC [%d %%]"                         <batterylevel>    {channel="evcc:device:demo:general#bufferSoC"}
Number:Dimensionless      evcc_bufferStartSoC                         "Battery Buffer Start SoC [%d %%]"                   <batterylevel>    {channel="evcc:device:demo:general#bufferStartSoC"}
Number:Power              evcc_residualPower                          "Grid Residual Power [%.1f kW]"                      <energy>          {channel="evcc:device:demo:general#residualPower"}
Number:Power              evcc_gridPower                              "Grid Power [%.1f kW]"                               <energy>          {channel="evcc:device:demo:general#gridPower"}
Number:Power              evcc_homePower                              "Home Power [%.1f kW]"                               <energy>          {channel="evcc:device:demo:general#homePower"}
Number:Power              evcc_pvPower                                "PV Power [%.1f kW]"                                 <energy>          {channel="evcc:device:demo:general#pvPower"}

// Loadpoint
Number                    evcc_loadpoint0_activePhases                "Active Phases [%d]"                                                   {channel="evcc:device:demo:loadpoint0#activePhases"}
Number:ElectricCurrent    evcc_loadpoint0_chargeCurrent               "Charging current [%.0f A]"                          <energy>          {channel="evcc:device:demo:loadpoint0#chargeCurrent"}
Number:Time               evcc_loadpoint0_chargeDuration              "Charging duration [%1$tH:%1$tM]"                    <time>            {channel="evcc:device:demo:loadpoint0#chargeDuration"}
Number:Time               evcc_loadpoint0_chargeRemainingDuration     "Charging remaining duration [%1$tH:%1$tM]"          <time>            {channel="evcc:device:demo:loadpoint0#chargeRemainingDuration"}
Number:Energy             evcc_loadpoint0_chargeRemainingEnergy       "Charging remaining energy [%.1f kWh]"               <energy>          {channel="evcc:device:demo:loadpoint0#chargeRemainingEnergy"}
Number:Power              evcc_loadpoint0_chargePower                 "Charging power [%.1f kW]"                           <energy>          {channel="evcc:device:demo:loadpoint0#chargePower"}
Number:Energy             evcc_loadpoint0_chargedEnergy               "Charged energy [%.1f kWh]"                          <energy>          {channel="evcc:device:demo:loadpoint0#chargedEnergy"}
Switch                    evcc_loadpoint0_charging                    "Currently charging [%s]"                            <battery>         {channel="evcc:device:demo:loadpoint0#charging"}
Switch                    evcc_loadpoint0_enabled                     "Charging enabled [%s]"                              <switch>          {channel="evcc:device:demo:loadpoint0#enabled"}
Number:ElectricCurrent    evcc_loadpoint0_maxCurrent                  "Maximum current [%.0f A]"                           <energy>          {channel="evcc:device:demo:loadpoint0#maxCurrent"}
Number:ElectricCurrent    evcc_loadpoint0_minCurrent                  "Minimum current [%.0f A]"                           <energy>          {channel="evcc:device:demo:loadpoint0#minCurrent"}
String                    evcc_loadpoint0_mode                        "Mode [%s]"                                                            {channel="evcc:device:demo:loadpoint0#mode"}
Number                    evcc_loadpoint0_phases                      "Enabled phases [%d]"                                                  {channel="evcc:device:demo:loadpoint0#phases"}
Number:Energy             evcc_loadpoint0_limitEnergy                 "Limit energy [%.1f kWh]"                            <batterylevel>    {channel="evcc:device:demo:loadpoint0#limitEnergy"}
Number:Dimensionless      evcc_loadpoint0_limitSoC                    "Limit SoC [%d %%]"                                  <batterylevel>    {channel="evcc:device:demo:loadpoint0#limitSoC"}
String                    evcc_loadpoint0_title                       "Loadpoint title [%s]"                               <text>            {channel="evcc:device:demo:loadpoint0#title"}
// Vehicle on loadpoint
Switch                    evcc_loadpoint0_vehicleConnected            "Vehicle connected [%s]"                             <switch>          {channel="evcc:device:demo:loadpoint0#vehicleConnected"}
Number:Time               evcc_loadpoint0_vehicleConnectedDuration    "Vehicle connected duration [%.1f h]"                <time>            {channel="evcc:device:demo:loadpoint0#vehicleConnectedDuration"}
Number:Energy             evcc_loadpoint0_vehicleCapacity             "Vehicle capacity [%.0f kWh]"                        <batterylevel>    {channel="evcc:device:demo:loadpoint0#vehicleCapacity"}
Number:Length             evcc_loadpoint0_vehicleOdometer             "Vehicle odometer [%.1f km]"                                           {channel="evcc:device:demo:loadpoint0#vehicleOdometer"}
Switch                    evcc_loadpoint0_vehiclePresent              "Vehicle present [%s]"                               <switch>          {channel="evcc:device:demo:loadpoint0#vehiclePresent"}
Number:Length             evcc_loadpoint0_vehicleRange                "Vehicle Range [%.0f km]"                                              {channel="evcc:device:demo:loadpoint0#vehicleRange"}
Number:Dimensionless      evcc_loadpoint0_vehicleSoC                  "Vehicle SoC [%d %%]"                                <batterylevel>    {channel="evcc:device:demo:loadpoint0#vehicleSoC"}
String                    evcc_loadpoint0_VehicleName                 "Vehicle name [%s]"                                  <text>            {channel="evcc:device:demo:loadpoint0#vehicleName"}

// Vehicle
String                    evcc_vehicle0_vehicleTitle                  "Vehicle title [%s]"                                  <text>           {channel="evcc:device:demo:vehicle0#vehicleTitle"}
Number:Dimensionless      evcc_vehicle0_vehicleMinSoC                 "Vehicle minimum SoC [%d %%]"                         <batterylevel>   {channel="evcc:device:demo:vehicle0#vehicleMinSoC"}
Number:Dimensionless      evcc_vehicle0_vehicleLimitSoC               "Vehicle limit SoC [%d %%]"                           <batterylevel>   {channel="evcc:device:demo:vehicle0#vehicleLimitSoC"}
Switch                    evcc_vehicle0_vehiclePlanEnabled            "Vehicle plan enabled [%s]"                           <switch>         {channel="evcc:device:demo:vehicle0#vehiclePlanEnabled"}
Number:Dimensionless      evcc_vehicle0_vehiclePlanSoC                "Vehicle plan SoC [%d %%]"                            <batterylevel>   {channel="evcc:device:demo:vehicle0#vehiclePlanSoC"}
DateTime                  evcc_vehicle0_vehiclePlanTime               "Vehicle plan time [%1$td.%1$tm.%1$tY, %1$tH:%1$tM]"  <time>           {channel="evcc:device:demo:loadpoint0#targetTime"}
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
            Text item=evcc_loadpoint0_vehicleCapacity
            Text item=evcc_loadpoint0_vehicleOdometer
            Text item=evcc_loadpoint0_vehicleRange
            Text item=evcc_loadpoint0_vehicleSoC
            Text item=evcc_vehicle0_vehicleTitle
            Setpoint item=evcc_vehicle0_vehicleMinSoC minValue=0 maxValue=100 step=5
            Setpoint item=evcc_vehicle0_vehicleLimitSoC minValue=5 maxValue=100 step=5
            Switch item=evcc_vehicle0_vehiclePlanEnabled
            Setpoint item=evcc_vehicle0_vehiclePlanSoC minValue=5 maxValue=100 step=5
            Input item=evcc_vehicle0_vehiclePlanTime
        }
    }
}
```
