# evcc Binding

This binding integrates [evcc](https://evcc.io), an extensible **E**lectric **V**ehicle **C**harge **C**ontroller and home energy management system.
The binding is compatible to evcc [version 0.123.1](https://github.com/evcc-io/evcc/releases/tag/0.123.1) or newer and was tested with [version 0.205.0](https://github.com/evcc-io/evcc/releases/tag/0.205.0).

You can easily install and upgrade evcc on openHABian using `sudo openhabian-config`.

evcc controls your wallbox(es) with multiple charging modes and allows you to charge your ev with your photovoltaic's excess current.
To provide an intelligent charging control, evcc supports over 30 wallboxes and over 20 energy meters/home energy management systems from many manufacturers as well as electric vehicles from over 20 car manufacturers.
Furthermore, evcc calculates your money savings.

This binding enables openHAB to retrieve status data from your evcc instance and to control the charging process.
For more advanced features like calculated savings, you have to visit the web UI of evcc.

This binding will create a file in your userdata folder, when it detects new datapoints in the response from evcc.
This will help to add them to the binding and make them available with the next version of the binding.

## Supported Things

- `server`: A running evcc instance. It will be used as the bridge for the other things.
- `battery`: A battery configured in your evcc instance.
- `heating`: A heating loadpoint configured in your evcc instance.
- `loadpoint`: A loadpoint configured in your evcc instance.
- `pv`: A photovoltaic system configured in your evcc instance.
- `site`: The relevant site data from your evcc instance.
- `vehicle`: A vehicle configured in your evcc instance.
- `plan`: A One-time or repeating charging plan linked to a vehicle in your evcc instance.

## Discovery

The bridge will discover the things automatically in the background.

## `server` Bridge Configuration

| Parameter       | Type    | Description                                              | Advanced | Required |
|-----------------|---------|----------------------------------------------------------|----------|----------|
| schema          | String  | Schema to connect to your instance (http or https)       | No       | Yes      |
| host            | String  | IP or hostname running your evcc instance                | No       | Yes      |
| port            | Integer | Port of your evcc instance                               | Yes      | Yes      |
| refreshInterval | Number  | Interval the status is polled in seconds (minimum is 15) | Yes      | Yes      |

Default value for _refreshInterval_ is 30 seconds.

## Thing(s) Configuration

Things will be set up automatically when a bridge has been configured and will appear in your inbox.
It is not possible to add things manually in the UI.
Nevertheless, you can add them manually via things file.
All things need to be configured via index, except vehicles, they need to be configured via the database id.

| Thing                                 | Parameter | Type   | Description                               | Advanced | Required |
|---------------------------------------|-----------|--------|-------------------------------------------|----------|----------|
| battery, heating, loadpoint, pv, plan | index     | String | Index of this thing in your evcc instance | No       | Yes      |
| plan, vehicle                         | vehicleID | String | Database ID of your vehicle               | No       | Yes      |

### Thing `plan`

The `plan` Thing represents a charging plan for a vehicle configured in your evcc instance.
Needs the index of the plan and the database id of the vehicle as configuration parameters.
The index 0 is always the One-time plan, higher indices (1...N) are repeating plans.

## Channels

evcc Things can have several channels based corresponding on their capabilities.
These channels are dynamically added to the Thing during their initialization; therefore, there is no list of possible channels in this documentation.

## Full Example

### `demo.things` Example

```java
Bridge evcc:server:demo-server "Demo" [scheme="https", host="demo.evcc.io", port=443, refreshInterval=30] {
    // This thing will only exist once per evcc instance
    Thing site demo-site "Site - evcc Demo"
    // You can define as many Battery things as you have batteries configured in your evcc instance
    Thing battery demo-battery1 "Battery - evcc Demo Battery 1"[index=0]
    ..
    // You can define as many PV things as you have photovoltaics configured in your evcc instance
    Thing pv demo-pv1 "PV - evcc Demo Photovoltaic 1"[index=0]
    ..
    // You can define as many Loadpoint things as you have loadpoints configured in your evcc instance
    Thing loadpoint demo-loadpoint-carport "Loadpoint - evcc Demo Loadpoint 1"[index=0]
    ..
    // You can define as many Vehicle things as you have vehicles configured in your evcc instance
    Thing vehicle demo-vehicle1 "Vehicle - evcc Demo Vehicle 1"[vehicleId="vehicle_1"]
    ..
    // You can define as many Plan things as you have plans for your vehicle configured
    Thing plan demo-one-time-plan-for-vehicle1 "One-time plan for vehicle 1"[index=0, vehicleId="vehicle_1"]
    Thing plan demo-repeating-plan-1-for-vehicle1 "Repeating plan 1 for vehicle 1"[index=1, vehicleId="vehicle_1"]
    ..
}
```

### `demo.items` Example

Here you will find items examples sorted by thing.

#### Battery

```java 
Number:Energy        Evcc_Battery_Capacity "Battery Capacity [%s]"         { channel="evcc:battery:demo-server:demo-battery:battery-capacity" }
Switch               Evcc_Battery_Controllable "Battery Controllable [%s]" { channel="evcc:battery:demo-server:demo-battery:battery-controllable" }
Number:Power         Evcc_Battery_Power "Battery Power [%s]"               { channel="evcc:battery:demo-server:demo-battery:battery-power" }
Number:Dimensionless Evcc_Battery_Soc "Battery SoC [%s]"                   { channel="evcc:battery:demo-server:demo-battery:battery-soc" }
String               Evcc_Battery_Title "Title [%s]"                       { channel="evcc:battery:demo-server:demo-battery:battery-title" }
```

#### Loadpoint and Heating

```java
Number:Temperature       Evcc_Loadpoint_Effective_Limit_Temperature        "Effective Charging Limit Temperature [%s]" { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-limit-temperature" }
Number:Temperature       Evcc_Loadpoint_Effective_Plan_Temperature         "Effective Plan Limit Temperature [%s]"     { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-plan-temperature" }
Number:Temperature       Evcc_Loadpoint_Limit_Temperature                  "Temperature Limit [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-limit-temperature" }
Number:Temperature       Evcc_Loadpoint_Vehicle_Limit_Temperature          "Device Temperature Limit [%s]"             { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-limit-temperature" }
Number:Temperature       Evcc_Loadpoint_Vehicle_Temperature                "Device Temperature [%s]"                   { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-temperature" }
Switch                   Evcc_Loadpoint_Battery_Boost                      "Battery Boost [%s]"                        { channel="evcc:battery:demo-server:demo-battery:loadpoint-battery-boost" }
Number:Time              Evcc_Loadpoint_Charge_Duration                    "Charging Duration [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-duration" }
Number:Power             Evcc_Loadpoint_Charge_Power                       "Charging Power [%s]"                       { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-power" }
Number:Energy            Evcc_Loadpoint_Charge_Total_Import                "Charge Total Import [%s]"                  { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-total-import" }
Number:Time              Evcc_Loadpoint_Charge_Remaining_Duration          "Charging Remaining Duration [%s]"          { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-remaining-duration" }
Number:Energy            Evcc_Loadpoint_Charge_Remaining_Energy            "Charging Remaining Energy [%s]"            { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-remaining-energy" }
Number:Energy            Evcc_Loadpoint_Charged_Energy                     "Charged Energy [%s]"                       { channel="evcc:battery:demo-server:demo-battery:loadpoint-charged-energy" }
String                   Evcc_Loadpoint_Charger_Icon                       "Charger Icon [%s]"                         { channel="evcc:battery:demo-server:demo-battery:loadpoint-charger-icon" }
Switch                   Evcc_Loadpoint_Charger_Feature_Heating            "Heating [%s]"                              { channel="evcc:battery:demo-server:demo-battery:loadpoint-charger-feature-heating" }
Switch                   Evcc_Loadpoint_Charger_Feature_Integrated_Device  "Integrated Device [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-charger-feature-integrated-device" }
Switch                   Evcc_Loadpoint_Charger_Phases1p3p                 "Phase Switching [%s]"                      { channel="evcc:battery:demo-server:demo-battery:loadpoint-charger-phases1p3p" }
Switch                   Evcc_Loadpoint_Charger_Single_Phase               "1 Phase Charging [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-charger-single-phase" }
String                   Evcc_Loadpoint_Charger_Status_Reason              "Charger Status Reason [%s]"                { channel="evcc:battery:demo-server:demo-battery:loadpoint-charger-status-reason" }
Switch                   Evcc_Loadpoint_Charging                           "Charging State [%s]"                       { channel="evcc:battery:demo-server:demo-battery:loadpoint-charging" }
Switch                   Evcc_Loadpoint_Connected                          "Vehicle Connected [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-connected" }
Number:Time              Evcc_Loadpoint_Connected_Duration                 "Vehicle Conn. Duration [%s]"               { channel="evcc:battery:demo-server:demo-battery:loadpoint-connected-duration" }
Number:Time              Evcc_Loadpoint_Disable_Delay                      "Disable Delay [%s]"                        { channel="evcc:battery:demo-server:demo-battery:loadpoint-disable-delay" }
Number:Power             Evcc_Loadpoint_Disable_Threshold                  "Disable Threshold [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-disable-threshold" }
Number:Dimensionless     Evcc_Loadpoint_Effective_Limit_Soc                "Active Charging Limit [%s]"                { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-limit-soc" }
Number:ElectricCurrent   Evcc_Loadpoint_Effective_Max_Current              "Active Maximum Current [%s]"               { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-max-current" }
Number:ElectricCurrent   Evcc_Loadpoint_Effective_Min_Current              "Active Minimum Current [%s]"               { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-min-current" }
Number                   Evcc_Loadpoint_Effective_Plan_Id                  "Active Plan ID [%s]"                       { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-plan-id" }
Number:Dimensionless     Evcc_Loadpoint_Effective_Plan_Soc                 "Active Plan SoC [%s]"                      { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-plan-soc" }
DateTime                 Evcc_Loadpoint_Effective_Plan_Time                "Active Plan Time [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-plan-time" }
DateTime                 Evcc_Loadpoint_Plan_Projected_End                 "Plan Projected End [%s]"                   { channel="evcc:battery:demo-server:demo-battery:loadpoint-plan-projected-end" }
DateTime                 Evcc_Loadpoint_Plan_Projected_Start               "Plan Projected Start [%s]"                 { channel="evcc:battery:demo-server:demo-battery:loadpoint-plan-projected-start" }
Number                   Evcc_Loadpoint_Effective_Priority                 "Active Priority [%s]"                      { channel="evcc:battery:demo-server:demo-battery:loadpoint-effective-priority" }
Number:Time              Evcc_Loadpoint_Enable_Delay                       "Enable Delay [%s]"                         { channel="evcc:battery:demo-server:demo-battery:loadpoint-enable-delay" }
Number:Power             Evcc_Loadpoint_Enable_Threshold                   "Enable Threshold [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-enable-threshold" }
Switch                   Evcc_Loadpoint_Enabled                            "Loadpoint Enabled [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-enabled" }
Number:Energy            Evcc_Loadpoint_Limit_Energy                       "Energy Limit [%s]"                         { channel="evcc:battery:demo-server:demo-battery:loadpoint-limit-energy" }
Number:Dimensionless     Evcc_Loadpoint_Limit_Soc                          "SoC Limit [%s]"                            { channel="evcc:battery:demo-server:demo-battery:loadpoint-limit-soc" }
Number:ElectricCurrent   Evcc_Loadpoint_Max_Current                        "Maximum Current [%s]"                      { channel="evcc:battery:demo-server:demo-battery:loadpoint-max-current" }
Number:ElectricCurrent   Evcc_Loadpoint_Min_Current                        "Minimum Current [%s]"                      { channel="evcc:battery:demo-server:demo-battery:loadpoint-min-current" }
String                   Evcc_Loadpoint_Mode                               "Mode [%s]"                                 { channel="evcc:battery:demo-server:demo-battery:loadpoint-mode" }
Number:ElectricCurrent   Evcc_Loadpoint_Offered_Current                    "Offered Current [%s]"                      { channel="evcc:battery:demo-server:demo-battery:loadpoint-offered-current" }
String                   Evcc_Loadpoint_Phase_Action                       "Phase Scaling [%s]"                        { channel="evcc:battery:demo-server:demo-battery:loadpoint-phase-action" }
Number:Time              Evcc_Loadpoint_Phase_Remaining                    "Phase Remaining Timer [%s]"                { channel="evcc:battery:demo-server:demo-battery:loadpoint-phase-remaining" }
Number:ElectricCurrent   Evcc_Loadpoint_Charge_Current_L1                  "Charge Current 1 [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-current-l1" }
Number:ElectricCurrent   Evcc_Loadpoint_Charge_Current_L2                  "Charge Current 2 [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-current-l2" }
Number:ElectricCurrent   Evcc_Loadpoint_Charge_Current_L3                  "Charge Current 3 [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-current-l3" }
Number:ElectricPotential Evcc_Loadpoint_Charge_Voltage_L1                  "Charge Voltage 1 [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-voltage-l1" }
Number:ElectricPotential Evcc_Loadpoint_Charge_Voltage_L2                  "Charge Voltage 2 [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-voltage-l2" }
Number:ElectricPotential Evcc_Loadpoint_Charge_Voltage_L3                  "Charge Voltage 3 [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-charge-voltage-l3" }
Number                   Evcc_Loadpoint_Phases_Active                      "Active Phases [%s]"                        { channel="evcc:battery:demo-server:demo-battery:loadpoint-phases-active" }
Number                   Evcc_Loadpoint_Phases_Configured                  "Configured Phases [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-phases-configured" }
Switch                   Evcc_Loadpoint_Plan_Active                        "Plan Activated [%s]"                       { channel="evcc:battery:demo-server:demo-battery:loadpoint-plan-active" }
Number:Energy            Evcc_Loadpoint_Plan_Energy                        "Plan Energy [%s]"                          { channel="evcc:battery:demo-server:demo-battery:loadpoint-plan-energy" }
Number:Time              Evcc_Loadpoint_Plan_Overrun                       "Plan Overrun [%s]"                         { channel="evcc:battery:demo-server:demo-battery:loadpoint-plan-overrun" }
Number:Time              Evcc_Loadpoint_Plan_Precondition                  "Plan Precondition [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-plan-precondition" }
Number                   Evcc_Loadpoint_Priority                           "Priority [%s]"                             { channel="evcc:battery:demo-server:demo-battery:loadpoint-priority" }
String                   Evcc_Loadpoint_Pv_Action                          "PV Action [%s]"                            { channel="evcc:battery:demo-server:demo-battery:loadpoint-pv-action" }
Number:Time              Evcc_Loadpoint_Pv_Remaining                       "Pv Remaining [%s]"                         { channel="evcc:battery:demo-server:demo-battery:loadpoint-pv-remaining" }
Number:EmissionIntensity Evcc_Loadpoint_Session_Co2_Per_K_Wh               "Session CO2 Per kWh [%s]"                  { channel="evcc:battery:demo-server:demo-battery:loadpoint-session-co2-per-k-wh" }
Number:Energy            Evcc_Loadpoint_Session_Energy                     "Session Energy [%s]"                       { channel="evcc:battery:demo-server:demo-battery:loadpoint-session-energy" }
Number:Dimensionless     Evcc_Loadpoint_Session_Solar_Percentage           "Session Solar Percentage [%s]"             { channel="evcc:battery:demo-server:demo-battery:loadpoint-session-solar-percentage" }
Number:Currency          Evcc_Loadpoint_Session_Price                      "Session Price [%s]"                        { channel="evcc:battery:demo-server:demo-battery:loadpoint-session-price" }
Number:EnergyPrice       Evcc_Loadpoint_Session_Price_Per_K_Wh             "Session Price Per kWh [%s]"                { channel="evcc:battery:demo-server:demo-battery:loadpoint-session-price-per-k-wh" }
Switch                   Evcc_Loadpoint_Smart_Cost_Active                  "Smart Cost Active [%s]"                    { channel="evcc:battery:demo-server:demo-battery:loadpoint-smart-cost-active" }
Number:EmissionIntensity Evcc_Loadpoint_Smart_Cost_Limit_Co2               "Smart Cost Limit [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-smart-cost-limit-co2" }
Number:EnergyPrice       Evcc_Loadpoint_Smart_Cost_Limit_Price             "Smart Cost Limit [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-smart-cost-limit-price" }
DateTime                 Evcc_Loadpoint_Smart_Cost_Next_Start              "Smart Cost Next Start [%s]"                { channel="evcc:battery:demo-server:demo-battery:loadpoint-smart-cost-next-start" }
Switch                   Evcc_Loadpoint_Smart_Feed_In_Priority_Active      "Smart Feed In Priority Active [%s]"        { channel="evcc:battery:demo-server:demo-battery:loadpoint-smart-feed-in-priority-active" }
String                   Evcc_Loadpoint_Title                              "Title [%s]"                                { channel="evcc:battery:demo-server:demo-battery:loadpoint-title" }
Switch                   Evcc_Loadpoint_Vehicle_Climater_Active            "Vehicle Climate Active [%s]"               { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-climater-active" }
Switch                   Evcc_Loadpoint_Vehicle_Detection_Active           "Vehicle Detection Active [%s]"             { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-detection-active" }
String                   Evcc_Loadpoint_Vehicle_Identity                   "Vehicle Identity [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-identity" }
Number:Dimensionless     Evcc_Loadpoint_Vehicle_Limit_Soc                  "Vehicle API SoC Limit [%s]"                { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-limit-soc" }
String                   Evcc_Loadpoint_Vehicle_Name                       "Vehicle Name [%s]"                         { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-name" }
Number:Length            Evcc_Loadpoint_Vehicle_Odometer                   "Vehicle Odometer [%s]"                     { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-odometer" }
Number:Length            Evcc_Loadpoint_Vehicle_Range                      "Vehicle Range [%s]"                        { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-range" }
Number:Dimensionless     Evcc_Loadpoint_Vehicle_Soc                        "Vehicle API SoC [%s]"                      { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-soc" }
String                   Evcc_Loadpoint_Vehicle_Title                      "Vehicle Title [%s]"                        { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-title" }
Switch                   Evcc_Loadpoint_Vehicle_Welcome_Active             "Vehicle Welcome Active [%s]"               { channel="evcc:battery:demo-server:demo-battery:loadpoint-vehicle-welcome-active" }
```

#### Plan

Channels plan-weekdays and plan-tz are only available for repeating plans. 

```java
Number:Dimensionless Evcc_Plan_Soc            "Plan SoC [%s]"            { channel="evcc:battery:demo-server:demo-battery:plan-soc" }
Number:Time          Evcc_Plan_Precondition   "Precondition Time [%s]"   { channel="evcc:battery:demo-server:demo-battery:plan-precondition" }
DateTime             Evcc_Plan_Time           "Plan Time [%s]"           { channel="evcc:battery:demo-server:demo-battery:plan-time" }
String               Evcc_Plan_Weekdays       "Plan Weekdays [%s]"       { channel="evcc:battery:demo-server:demo-battery:plan-weekdays" }
String               Evcc_Plan_Tz             "Plan Timezone [%s]"       { channel="evcc:battery:demo-server:demo-battery:plan-tz" }
Switch               Evcc_Plan_Active         "Active [%s]"              { channel="evcc:battery:demo-server:demo-battery:plan-active" }
```

#### PV

```java
Number:Power Evcc_Pv_Power "PV Power [%s]" { channel="evcc:battery:demo-server:demo-battery:pv-power" }
String       Evcc_Pv_Title "Title [%s]"    { channel="evcc:battery:demo-server:demo-battery:pv-title" }
```

#### Site

```java
String                   Evcc_Site_Available_Version                 "Available Version [%s]"                     { channel="evcc:battery:demo-server:demo-battery:site-available-version" }
Number:Energy            Evcc_Site_Battery_Capacity                  "Site Battery Capacity [%s]"                 { channel="evcc:battery:demo-server:demo-battery:site-battery-capacity" }
Switch                   Evcc_Site_Battery_Discharge_Control         "Battery Discharge Control [%s]"             { channel="evcc:battery:demo-server:demo-battery:site-battery-discharge-control" }
Number:Energy            Evcc_Site_Battery_Energy                    "Battery Energy [%s]"                        { channel="evcc:battery:demo-server:demo-battery:site-battery-energy" }
Switch                   Evcc_Site_Battery_Grid_Charge_Active        "Grid Charging Active [%s]"                  { channel="evcc:battery:demo-server:demo-battery:site-battery-grid-charge-active" }
Number:EmissionIntensity Evcc_Site_Battery_Grid_Charge_Limit_Co2     "Grid Charging Limit [%s]"                   { channel="evcc:battery:demo-server:demo-battery:site-battery-grid-charge-limit-co2" }
Number:EnergyPrice       Evcc_Site_Battery_Grid_Charge_Limit_Price   "Grid Charging Limit [%s]"                   { channel="evcc:battery:demo-server:demo-battery:site-battery-grid-charge-limit-price" }
String                   Evcc_Site_Battery_Mode                      "Battery Mode [%s]"                          { channel="evcc:battery:demo-server:demo-battery:site-battery-mode" }
String                   Evcc_Site_Battery_Mode_External             "Battery Mode [%s]"                          { channel="evcc:battery:demo-server:demo-battery:site-battery-mode-external" }
Number:Power             Evcc_Site_Battery_Power                     "Battery Power [%s]"                         { channel="evcc:battery:demo-server:demo-battery:site-battery-power" }
Number:Dimensionless     Evcc_Site_Battery_Soc                       "Battery SoC [%s]"                           { channel="evcc:battery:demo-server:demo-battery:site-battery-soc" }
Number:Dimensionless     Evcc_Site_Buffer_Soc                        "Battery Buffer SoC [%s]"                    { channel="evcc:battery:demo-server:demo-battery:site-buffer-soc" }
Number:Dimensionless     Evcc_Site_Buffer_Start_Soc                  "Battery Buffer Start SoC [%s]"              { channel="evcc:battery:demo-server:demo-battery:site-buffer-start-soc" }
String                   Evcc_Site_Currency                          "Currency [%s]"                              { channel="evcc:battery:demo-server:demo-battery:site-currency" }
Switch                   Evcc_Site_Demo_Mode                         "Demo Mode [%s]"                             { channel="evcc:battery:demo-server:demo-battery:site-demo-mode" }
Switch                   Evcc_Site_Eebus                             "EEBUS [%s]"                                 { channel="evcc:battery:demo-server:demo-battery:site-eebus" }
Number                   Evcc_Site_Green_Share_Home                  "Green Share Home [%s]"                      { channel="evcc:battery:demo-server:demo-battery:site-green-share-home" }
Number                   Evcc_Site_Green_Share_Loadpoints            "Green Share Loadpoints [%s]"                { channel="evcc:battery:demo-server:demo-battery:site-green-share-loadpoints" }
Number:ElectricCurrent   Evcc_Site_Grid_Current_L1                   "Grid Current 1 [%s]"                        { channel="evcc:battery:demo-server:demo-battery:site-grid-current-l1" }
Number:ElectricCurrent   Evcc_Site_Grid_Current_L2                   "Grid Current 2 [%s]"                        { channel="evcc:battery:demo-server:demo-battery:site-grid-current-l2" }
Number:ElectricCurrent   Evcc_Site_Grid_Current_L3                   "Grid Current 3 [%s]"                        { channel="evcc:battery:demo-server:demo-battery:site-grid-current-l3" }
Number:ElectricPotential Evcc_Site_Grid_Voltage_L1                   "Charge Voltage 1 [%s]"                      { channel="evcc:battery:demo-server:demo-battery:site-grid-voltage-l1" }
Number:ElectricPotential Evcc_Site_Grid_Voltage_L2                   "Charge Voltage 2 [%s]"                      { channel="evcc:battery:demo-server:demo-battery:site-grid-voltage-l2" }
Number:ElectricPotential Evcc_Site_Grid_Voltage_L3                   "Charge Voltage 3 [%s]"                      { channel="evcc:battery:demo-server:demo-battery:site-grid-voltage-l3" }
Number:Energy            Evcc_Site_Grid_Energy                       "Grid Energy [%s]"                           { channel="evcc:battery:demo-server:demo-battery:site-grid-energy" }
Number:Power             Evcc_Site_Grid_Power                        "Grid Power [%s]"                            { channel="evcc:battery:demo-server:demo-battery:site-grid-power" }
Number:Power             Evcc_Site_Home_Power                        "Home Power [%s]"                            { channel="evcc:battery:demo-server:demo-battery:site-home-power" }
Switch                   Evcc_Site_Messaging                         "Site Messaging [%s]"                        { channel="evcc:battery:demo-server:demo-battery:site-messaging" }
Number:Dimensionless     Evcc_Site_Priority_Soc                      "Battery Priority SoC [%s]"                  { channel="evcc:battery:demo-server:demo-battery:site-priority-soc" }
Number:Time              Evcc_Site_Interval                          "Interval [%s]"                              { channel="evcc:battery:demo-server:demo-battery:site-interval" }
Number:Energy            Evcc_Site_Pv_Energy                         "PV Energy [%s]"                             { channel="evcc:battery:demo-server:demo-battery:site-pv-energy" }
Number:Power             Evcc_Site_Pv_Power                          "PV Power [%s]"                              { channel="evcc:battery:demo-server:demo-battery:site-pv-power" }
Number:Power             Evcc_Site_Residual_Power                    "Grid Residual Power [%s]"                   { channel="evcc:battery:demo-server:demo-battery:site-residual-power" }
Switch                   Evcc_Site_Smart_Cost_Available              "Smart Cost Available [%s]"                  { channel="evcc:battery:demo-server:demo-battery:site-smart-cost-available" }
String                   Evcc_Site_Smart_Cost_Type                   "Smart Cost Type [%s]"                       { channel="evcc:battery:demo-server:demo-battery:site-smart-cost-type" }
Switch                   Evcc_Site_Smart_Feed_In_Priority_Available  "Smart Feed In Priority Available [%s]"      { channel="evcc:battery:demo-server:demo-battery:site-smart-feed-in-priority-available" }
Switch                   Evcc_Site_Startup_Completed                 "Start Up Complete [%s]"                     { channel="evcc:battery:demo-server:demo-battery:site-startup-completed" }
Number:EmissionIntensity Evcc_Site_Tariff_Co2                        "CO2 Concentration [%s]"                     { channel="evcc:battery:demo-server:demo-battery:site-tariff-co2" }
Number:EmissionIntensity Evcc_Site_Tariff_Co2_Home                   "CO2 Concentration Home [%s]"                { channel="evcc:battery:demo-server:demo-battery:site-tariff-co2-home" }
Number:EmissionIntensity Evcc_Site_Tariff_Co2_Loadpoints             "CO2 Concentration Loadpoints [%s]"          { channel="evcc:battery:demo-server:demo-battery:site-tariff-co2-loadpoints" }
Number:EnergyPrice       Evcc_Site_Tariff_Feed_In                    "Feed in price per kWh [%s]"                 { channel="evcc:battery:demo-server:demo-battery:site-tariff-feed-in" }
Number:EnergyPrice       Evcc_Site_Tariff_Grid                       "Grid Price [%s]"                            { channel="evcc:battery:demo-server:demo-battery:site-tariff-grid" }
Number:EnergyPrice       Evcc_Site_Tariff_Price_Home                 "Home Price [%s]"                            { channel="evcc:battery:demo-server:demo-battery:site-tariff-price-home" }
Number:EnergyPrice       Evcc_Site_Tariff_Price_Loadpoints           "Loadpoint price per kWh [%s]"               { channel="evcc:battery:demo-server:demo-battery:site-tariff-price-loadpoints" }
Number:Power             Evcc_Site_Tariff_Solar                      "Solar Tariff [%s]"                          { channel="evcc:battery:demo-server:demo-battery:site-tariff-solar" }
String                   Evcc_Site_Site_Title                        "Site Title [%s]"                            { channel="evcc:battery:demo-server:demo-battery:site-site-title" }
String                   Evcc_Site_Version                           "Version [%s]"                               { channel="evcc:battery:demo-server:demo-battery:site-version" }
```

#### Statistics

Here you need to add the channel group: 30d#,365d#,this-year#,total# for placeholder <group>

```java
Number:EmissionIntensity Evcc_Avg_Co2_Type          "Average CO2 [%s]"      { channel="evcc:battery:demo-server:demo-battery:<group>avg-co2-type" }
Number:EnergyPrice       Evcc_Avg_Price_Type        "Average Price [%s]"    { channel="evcc:battery:demo-server:demo-battery:<group>avg-price-type" }
Number:Energy            Evcc_Charged_Energy_Type   "Charged Energy [%s]"   { channel="evcc:battery:demo-server:demo-battery:<group>charged-energy-type" }
Number:Dimensionless     Evcc_Solar_Percentage_Type "Solar Percentage [%s]" { channel="evcc:battery:demo-server:demo-battery:<group>solar-percentage-type" }
```

#### Vehicle

```java
Number:Energy          Evcc_Vehicle_Capacity          "Vehicle Capacity [%s]"              { channel="evcc:battery:demo-server:demo-battery:vehicle-capacity" }
String                 Evcc_Vehicle_Icon              "Vehicle Icon [%s]"                  { channel="evcc:battery:demo-server:demo-battery:vehicle-icon" }
Number:Dimensionless   Evcc_Vehicle_Limit_Soc         "Vehicle Limit SoC [%s]"             { channel="evcc:battery:demo-server:demo-battery:vehicle-limit-soc" }
Number:Dimensionless   Evcc_Vehicle_Min_Soc           "Vehicle Min SoC [%s]"               { channel="evcc:battery:demo-server:demo-battery:vehicle-min-soc" }
Number:ElectricCurrent Evcc_Vehicle_Max_Current       "Vehicle Max Current [%s]"           { channel="evcc:battery:demo-server:demo-battery:vehicle-max-current" }
Number:ElectricCurrent Evcc_Vehicle_Min_Current       "Vehicle Min Current [%s]"           { channel="evcc:battery:demo-server:demo-battery:vehicle-min-current" }
Number:Time            Evcc_Vehicle_Plan_Precondition "Plan Precondition Duration [%s]"    { channel="evcc:battery:demo-server:demo-battery:vehicle-plan-precondition" }
Number:Dimensionless   Evcc_Vehicle_Plan_Soc          "Planned SoC [%s]"                   { channel="evcc:battery:demo-server:demo-battery:vehicle-plan-soc" }
DateTime               Evcc_Vehicle_Plan_Time         "Plan Datetime [%s]"                 { channel="evcc:battery:demo-server:demo-battery:vehicle-plan-time" }
String                 Evcc_Vehicle_Title             "Vehicle Title [%s]"                 { channel="evcc:battery:demo-server:demo-battery:vehicle-title" }
```

## Troubleshooting

If you need additional data that can be read out of the API response please reach out to the openHAB community.
