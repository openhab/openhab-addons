# Fronius Wattpilot Binding

This binding integrates the [Fronius Wattpilot EV charging stations](https://www.fronius.com/en-gb/uk/solar-energy/installers-partners/products-solutions/residential-energy-solutions/e-mobility-and-photovoltaic-residential/wattpilot-ev-charging-solution-for-homes)
through their unofficial WebSocket API, which is also used by the [Fronius Solar.Wattpilot app](https://www.fronius.com/en-gb/uk/solar-energy/installers-partners/products-solutions/residential-energy-solutions/e-mobility-and-photovoltaic-residential/wattpilot-ev-charging-solution-for-homes#anc_app).

It should support all Fronius Wattpilot wallboxes and has been tested with the following models:

- Fronius Wattpilot Home 11J
- Fronius Wattpilot Home 22J

## Supported Things

- `wattpilot`: A Fronius Wattpilot wallbox

## Discovery

The binding implements auto-discovery of Wattpilot wallboxes through mDNS.

If the binding discovered a Wattpilot, it is added to the inbox.
After adding it from the inbox, you need to configure the password for accessing the Wattpilot.

## Thing Configuration

### `wattpilot` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| hostname        | text    | Hostname or IP address of the device  | N/A     | yes      | no       |
| password        | text    | Password to access the device         | N/A     | yes      | no       |

## Channels

| Channel                      | Type                   | Read/Write | Description                                                                                             |
|------------------------------|------------------------|------------|---------------------------------------------------------------------------------------------------------|
| control#charging-allowed     | Switch                 | RW         | Allow (`ON`) or forbid (`OFF`) charging                                                                 |
| control#charging-mode        | String                 | RW         | The mode of charging: `DEFAULT`, `ECO`, `NEXT_TRIP`                                                     |
| control#charging-current     | Number:ElectricCurrent | RW         | The current to charge with                                                                              |
| control#pv-surplus-threshold | Number:Power           | RW         | The PV surplus power at which surplus charging starts                                                   |
| control#pv-surplus-soc       | Number:Dimensionless   | RW         | The battery SoC at which PV surplus charging starts                                                     |
| control#boost-enabled        | Switch                 | RW         | Boost charging in Eco or Next Trip mode by using power from the battery                                 |
| control#boost-soc            | Number:Dimensionless   | RW         | Limit SoC to discharge the battery to when boost is enabled                                             |
| status#charging-state        | String                 | R          | Charging state: `NO_CAR`, `CHARGING`, `READY` or `COMPLETE`                                             |
| status#charging-possible     | Switch                 | R          | Whether charging is currently possible, e.g. when using ECO mode, too low PV surplus can block charging |
| status#single-phase          | Switch                 | R          | Whether the wallbox is currently charging single phase only                                             |
| metrics#power                | Number:Power           | R          | Total power                                                                                             |
| metrics#energy-session       | Number:Energy          | R          | Amount of energy charged in the current/last charging session                                           |
| metrics#energy-total         | Number:Energy          | R          | Amount of energy charged in total                                                                       |
| metrics#l1-power             | Number:Power           | R          | Power of phase 1                                                                                        |
| metrics#l2-power             | Number:Power           | R          | Power of phase 2                                                                                        |
| metrics#l3-power             | Number:Power           | R          | Power of phase 3                                                                                        |
| metrics#l1-voltage           | Number:Voltage         | R          | Voltage of phase 1                                                                                      |
| metrics#l2-voltage           | Number:Voltage         | R          | Voltage of phase 2                                                                                      |
| metrics#l3-voltage           | Number:Voltage         | R          | Voltage of phase 3                                                                                      |
| metrics#l1-current           | Number:ElectricCurrent | R          | Current/amperage of phase 1                                                                             |
| metrics#l2-current           | Number:ElectricCurrent | R          | Current/amperage of phase 2                                                                             |
| metrics#l3-current           | Number:ElectricCurrent | R          | Current/amperage of phase 3                                                                             |

## Full Example

### Thing Configuration

```java
Thing froniuswattpilot:wattpilot:garage "Wattpilot Garage" [hostname="xxx.xxx.xxx.xxx", password="secret"]
```

### Item Configuration

```java
Group                     Wattpilot_Garage                             "Wattpilot Garage"                                                          ["Equipment"]

// Control
Switch                    Wattpilot_Garage_Charging_Allowed            "Charging Allowed"                      <BatteryLevel>  (Wattpilot_Garage)  ["Control", "Enabled"]      {channel="froniuswattpilot:wattpilot:garage:control#charging-allowed"}
String                    Wattpilot_Garage_Charging_Mode               "Charging Mode"                         <BatteryLevel>  (Wattpilot_Garage)  ["Control", "Mode"]         {channel="froniuswattpilot:wattpilot:garage:control#charging-mode"}
Number:ElectricCurrent    Wattpilot_Garage_Charging_Current            "Charging Current [%d A]"               <Energy>        (Wattpilot_Garage)  ["Setpoint", "Current"]     {channel="froniuswattpilot:wattpilot:garage:control#charging-current", unit="A"}
Number:Power              Wattpilot_Garage_PV_Surplus_Power_Threshold  "PV Surplus Power Threshold [%.1f kW]"  <SolarPlant>    (Wattpilot_Garage)  ["Setpoint", "Power"]       {channel="froniuswattpilot:wattpilot:garage:control#pv-surplus-threshold", unit="kW"}
Number:Dimensionless      Wattpilot_Garage_PV_Surplus_SoC_Threshold    "PV Surplus SoC Threshold [%d %%]"      <SolarPlant>    (Wattpilot_Garage)  ["Setpoint", "Energy"]      {channel="froniuswattpilot:wattpilot:garage:control#pv-surplus-soc", unit="%"}
Switch                    Wattpilot_Garage_Boost_Enabled               "Boost Enabled"                         <BatteryLevel>  (Wattpilot_Garage)  ["Control", "Enabled"]      {channel="froniuswattpilot:wattpilot:garage:control#boost-enabled"}
Number:Dimensionless      Wattpilot_Garage_Boost_SoC_Limit             "Boost SoC Limit [%d %%]"               <BatteryLevel>  (Wattpilot_Garage)  ["Setpoint", "Energy"]      {channel="froniuswattpilot:wattpilot:garage:control#boost-soc", unit="%"}

// Status
Switch                    Wattpilot_Garage_Charging_Possible           "Charging Possible"                     <BatteryLevel>  (Wattpilot_Garage)  ["Status"]                  {channel="froniuswattpilot:wattpilot:garage:status#charging-possible"}
String                    Wattpilot_Garage_Charging_State              "Charging State"                        <BatteryLevel>  (Wattpilot_Garage)  ["Status"]                  {channel="froniuswattpilot:wattpilot:garage:status#charging-state"}
Switch                    Wattpilot_Garage_Single_Phase_Charging       "Single Phase Charging"                 <BatteryLevel>  (Wattpilot_Garage)  ["Status"]                  {channel="froniuswattpilot:wattpilot:garage:status#single-phase"}

// Metrics total
Number:Power              Wattpilot_Garage_Total_Power                 "Total Power [%.2f kW]"                 <Energy>        (Wattpilot_Garage)  ["Measurement", "Power"]    {channel="froniuswattpilot:wattpilot:garage:metrics#power", unit="kW"}
Number:Energy             Wattpilot_Garage_Charged_Energy              "Charged Energy [%.2f kWh]"             <Energy>        (Wattpilot_Garage)  ["Measurement", "Energy"]   {channel="froniuswattpilot:wattpilot:garage:metrics#energy-session", unit="kWh"}
Number:Energy             Wattpilot_Garage_Total_Charged_Energy        "Total Charged Energy [%.0f kWh]"       <Energy>        (Wattpilot_Garage)  ["Measurement", "Energy"]   {channel="froniuswattpilot:wattpilot:garage:metrics#energy-total", unit="kWh"}
// Metrics phase 1
Number:Power              Wattpilot_Garage_Phase_1_Power               "Phase 1 Power [%.2f kW]"               <Energy>        (Wattpilot_Garage)  ["Measurement", "Power"]    {channel="froniuswattpilot:wattpilot:garage:metrics#l1-power", unit="kW"}
Number:ElectricPotential  Wattpilot_Garage_Phase_1_Voltage             "Phase 1 Voltage [%d V]"                <Energy>        (Wattpilot_Garage)  ["Measurement", "Voltage"]  {channel="froniuswattpilot:wattpilot:garage:metrics#l1-voltage", unit="V"}
Number:ElectricCurrent    Wattpilot_Garage_Phase_1_Current             "Phase 1 Current [%.1f A]"              <Energy>        (Wattpilot_Garage)  ["Measurement", "Current"]  {channel="froniuswattpilot:wattpilot:garage:metrics#l1-current", unit="A"}
// Metrics phase 2
Number:Power              Wattpilot_Garage_Phase_2_Power               "Phase 2 Power [%.2f kW]"               <Energy>        (Wattpilot_Garage)  ["Measurement", "Power"]    {channel="froniuswattpilot:wattpilot:garage:metrics#l2-power", unit="kW"}
Number:ElectricPotential  Wattpilot_Garage_Phase_2_Voltage             "Phase 2 Voltage [%d V]"                <Energy>        (Wattpilot_Garage)  ["Measurement", "Voltage"]  {channel="froniuswattpilot:wattpilot:garage:metrics#l2-voltage", unit="V"}
Number:ElectricCurrent    Wattpilot_Garage_Phase_2_Current             "Phase 2 Current [%.1f A]"              <Energy>        (Wattpilot_Garage)  ["Measurement", "Current"]  {channel="froniuswattpilot:wattpilot:garage:metrics#l2-current", unit="A"}
// Metrics phase 3
Number:Power              Wattpilot_Garage_Phase_3_Power               "Phase 3 Power [%.2f kW]"               <Energy>        (Wattpilot_Garage)  ["Measurement", "Power"]    {channel="froniuswattpilot:wattpilot:garage:metrics#l3-power", unit="kW"}
Number:ElectricPotential  Wattpilot_Garage_Phase_3_Voltage             "Phase 3 Voltage [%d V]"                <Energy>        (Wattpilot_Garage)  ["Measurement", "Voltage"]  {channel="froniuswattpilot:wattpilot:garage:metrics#l3-voltage", unit="V"}
Number:ElectricCurrent    Wattpilot_Garage_Phase_3_Current             "Phase 3 Current [%.1f A]"              <Energy>        (Wattpilot_Garage)  ["Measurement", "Current"]  {channel="froniuswattpilot:wattpilot:garage:metrics#l3-current", unit="A"}
```
