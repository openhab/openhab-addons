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
All things need to be configured via index, except vehicles, the need to be configured via the database id.

| Thing                                                  | Parameter | Type   | Description                               | Advanced | Required |
|--------------------------------------------------------|-----------|--------|-------------------------------------------|----------|----------|
| battery<br/>heating<br/>loadpoint<br/>pv<br/>plan</br> | index     | String | Index of this thing in your evcc instance | No       | Yes      |
| plan<br/>vehicle                                       | vehicleID | String | Database ID of your vehicle               | No       | Yes      |

### Thing Plan

The Plan Thing represents a charging plan for a vehicle configured in your evcc instance.
Needs the index of the plan and the database id of the vehicle as configuration parameters.
The index 0 is always the One-time plan, higher indices are repeating plans.

Any changes made to the plan channels will not be sent to evcc automatically, but cached.
If you want to update the plan, you have to use the update plan channel of the thing to send it to evcc.
**Updating the plan will only work when setting the update plan channel to state ON**.
Afterward, the update plan channel will be automatically reset to state OFF.
The weekdays will be localized based on the language settings of your openHAB instance.

Here is an example to update a One-time charging plan via DLSRule script:

```xtend
One_time_charging_plan_1_for_BMW_iX3_Plan_Time.sendCommand("2025-12-19T06:00:00.000Z");
One_time_charging_plan_1_for_BMW_iX3_Plan_SoC.sendCommand(85);
One_time_charging_plan_1_for_BMW_iX3_Precondition_Time.sendCommand(1800);
One_time_charging_plan_1_for_BMW_iX3_Update_Plan.sendCommand("ON");
```

Here is an example to update a repeating charging plan via DLSRule script:

```xtend
Repeating_charging_plan_1_for_BMW_iX3_Plan_Weekdays.sendCommand("Monday;Tuesday;Wednesday");
Repeating_charging_plan_1_for_BMW_iX3_Plan_Time.sendCommand("09:00");
Repeating_charging_plan_1_for_BMW_iX3_Plan_SoC.sendCommand(85);
Repeating_charging_plan_1_for_BMW_iX3_Precondition_Time.sendCommand(1800);
Repeating_charging_plan_1_for_BMW_iX3_Update_Plan.sendCommand("ON");
```

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

List of all possible channels

| Name                                             | Item Type                | Label                                     | Channel                                                                           | Unit              |
|--------------------------------------------------|--------------------------|-------------------------------------------|-----------------------------------------------------------------------------------|-------------------|
| Evcc_Battery_Controllable                        | Switch                   | Battery Controllable [%s]                 | evcc:battery:demo-server:demo-battery:Battery_Controllable                        | –                 |
| Evcc_Battery_Power                               | Number:Power             | Battery Power [%s]                        | evcc:battery:demo-server:demo-battery:Battery_Power                               | Power             |
| Evcc_Battery_Soc                                 | Number:Dimensionless     | Battery SoC [%s]                          | evcc:battery:demo-server:demo-battery:Battery_Soc                                 | Dimensionless     |
| Evcc_Battery_Title                               | String                   | Title [%s]                                | evcc:battery:demo-server:demo-battery:Battery_Title                               | –                 |
| Evcc_Loadpoint_Effective_Limit_Temperature       | Number:Temperature       | Effective Charging Limit Temperature [%s] | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Limit_Temperature       | Temperature       |
| Evcc_Loadpoint_Effective_Plan_Temperature        | Number:Temperature       | Effective Plan Limit Temperature [%s]     | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Temperature        | Temperature       |
| Evcc_Loadpoint_Limit_Temperature                 | Number:Temperature       | Temperature Limit [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Limit_Temperature                 | Temperature       |
| Evcc_Loadpoint_Vehicle_Limit_Temperature         | Number:Temperature       | Device Temperature Limit [%s]             | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Limit_Temperature         | Temperature       |
| Evcc_Loadpoint_Vehicle_Temperature               | Number:Temperature       | Device Temperature [%s]                   | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Temperature               | Temperature       |
| Evcc_Loadpoint_Battery_Boost                     | Switch                   | Battery Boost [%s]                        | evcc:battery:demo-server:demo-battery:Loadpoint_Battery_Boost                     | –                 |
| Evcc_Loadpoint_Charge_Duration                   | Number:Time              | Charging Duration [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Duration                   | Time              |
| Evcc_Loadpoint_Charge_Power                      | Number:Power             | Charging Power [%s]                       | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Power                      | Power             |
| Evcc_Loadpoint_Charge_Total_Import               | Number:Energy            | Charge Total Import [%s]                  | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Total_Import               | Energy            |
| Evcc_Loadpoint_Charge_Remaining_Duration         | Number:Time              | Charging Remaining Duration [%s]          | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Remaining_Duration         | Time              |
| Evcc_Loadpoint_Charge_Remaining_Energy           | Number:Energy            | Charging Remaining Energy [%s]            | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Remaining_Energy           | Energy            |
| Evcc_Loadpoint_Charged_Energy                    | Number:Energy            | Charged Energy [%s]                       | evcc:battery:demo-server:demo-battery:Loadpoint_Charged_Energy                    | Energy            |
| Evcc_Loadpoint_Charger_Icon                      | String                   | Charger Icon [%s]                         | evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Icon                      | –                 |
| Evcc_Loadpoint_Charger_Feature_Heating           | Switch                   | Heating [%s]                              | evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Feature_Heating           | –                 |
| Evcc_Loadpoint_Charger_Feature_Integrated_Device | Switch                   | Integrated Device [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Feature_Integrated_Device | –                 |
| Evcc_Loadpoint_Charger_Phases1p3p                | Switch                   | Phase Switching [%s]                      | evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Phases1p3p                | –                 |
| Evcc_Loadpoint_Charger_Single_Phase              | Switch                   | 1 Phase Charging [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Single_Phase              | –                 |
| Evcc_Loadpoint_Charger_Status_Reason             | String                   | Charger Status Reason [%s]                | evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Status_Reason             | –                 |
| Evcc_Loadpoint_Charging                          | Switch                   | Charging State [%s]                       | evcc:battery:demo-server:demo-battery:Loadpoint_Charging                          | –                 |
| Evcc_Loadpoint_Connected                         | Switch                   | Vehicle Connected [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Connected                         | –                 |
| Evcc_Loadpoint_Connected_Duration                | Number:Time              | Vehicle Conn. Duration [%s]               | evcc:battery:demo-server:demo-battery:Loadpoint_Connected_Duration                | Time              |
| Evcc_Loadpoint_Disable_Delay                     | Number:Time              | Disable Delay [%s]                        | evcc:battery:demo-server:demo-battery:Loadpoint_Disable_Delay                     | Time              |
| Evcc_Loadpoint_Disable_Threshold                 | Number:Power             | Disable Threshold [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Disable_Threshold                 | Power             |
| Evcc_Loadpoint_Effective_Limit_Soc               | Number:Dimensionless     | Active Charging Limit [%s]                | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Limit_Soc               | Dimensionless     |
| Evcc_Loadpoint_Effective_Max_Current             | Number:ElectricCurrent   | Active Maximum Current [%s]               | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Max_Current             | ElectricCurrent   |
| Evcc_Loadpoint_Effective_Min_Current             | Number:ElectricCurrent   | Active Minimum Current [%s]               | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Min_Current             | ElectricCurrent   |
| Evcc_Loadpoint_Effective_Plan_Id                 | Number                   | Active Plan ID [%s]                       | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Id                 | –                 |
| Evcc_Loadpoint_Effective_Plan_Soc                | Number:Dimensionless     | Active Plan SoC [%s]                      | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Soc                | Dimensionless     |
| Evcc_Loadpoint_Effective_Plan_Time               | DateTime                 | Active Plan Time [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Time               | –                 |
| Evcc_Loadpoint_Enabled                           | Switch                   | Enabled [%s]                              | evcc:battery:demo-server:demo-battery:Loadpoint_Enabled                           | –                 |
| Evcc_Loadpoint_Plan_Projected_End                | DateTime                 | Plan Projected End [%s]                   | evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Projected_End                | –                 |
| Evcc_Loadpoint_Plan_Projected_Start              | DateTime                 | Plan Projected Start [%s]                 | evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Projected_Start              | –                 |
| Evcc_Loadpoint_Effective_Priority                | Number                   | Active Priority [%s]                      | evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Priority                | –                 |
| Evcc_Loadpoint_Enable_Delay                      | Number:Time              | Enable Delay [%s]                         | evcc:battery:demo-server:demo-battery:Loadpoint_Enable_Delay                      | Time              |
| Evcc_Loadpoint_Enable_Threshold                  | Number:Power             | Enable Threshold [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Enable_Threshold                  | Power             |
| Evcc_Loadpoint_Limit_Energy                      | Number:Energy            | Energy Limit [%s]                         | evcc:battery:demo-server:demo-battery:Loadpoint_Limit_Energy                      | Energy            |
| Evcc_Loadpoint_Limit_Soc                         | Number:Dimensionless     | SoC Limit [%s]                            | evcc:battery:demo-server:demo-battery:Loadpoint_Limit_Soc                         | Dimensionless     |
| Evcc_Loadpoint_Max_Current                       | Number:ElectricCurrent   | Maximum Current [%s]                      | evcc:battery:demo-server:demo-battery:Loadpoint_Max_Current                       | ElectricCurrent   |
| Evcc_Loadpoint_Min_Current                       | Number:ElectricCurrent   | Minimum Current [%s]                      | evcc:battery:demo-server:demo-battery:Loadpoint_Min_Current                       | ElectricCurrent   |
| Evcc_Loadpoint_Mode                              | String                   | Mode [%s]                                 | evcc:battery:demo-server:demo-battery:Loadpoint_Mode                              | –                 |
| Evcc_Loadpoint_Offered_Current                   | Number:ElectricCurrent   | Offered Current [%s]                      | evcc:battery:demo-server:demo-battery:Loadpoint_Offered_Current                   | ElectricCurrent   |
| Evcc_Loadpoint_Phase_Action                      | String                   | Phase Scaling [%s]                        | evcc:battery:demo-server:demo-battery:Loadpoint_Phase_Action                      | –                 |
| Evcc_Loadpoint_Phase_Remaining                   | Number:Time              | Phase Remaining Timer [%s]                | evcc:battery:demo-server:demo-battery:Loadpoint_Phase_Remaining                   | Time              |
| Evcc_Loadpoint_Charge_Current_L1                 | Number:ElectricCurrent   | Charge Current 1 [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Current_L1                 | ElectricCurrent   |
| Evcc_Loadpoint_Charge_Current_L2                 | Number:ElectricCurrent   | Charge Current 2 [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Current_L2                 | ElectricCurrent   |
| Evcc_Loadpoint_Charge_Current_L3                 | Number:ElectricCurrent   | Charge Current 3 [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Current_L3                 | ElectricCurrent   |
| Evcc_Loadpoint_Charge_Voltage_L1                 | Number:ElectricPotential | Charge Voltage 1 [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Voltage_L1                 | ElectricPotential |
| Evcc_Loadpoint_Charge_Voltage_L2                 | Number:ElectricPotential | Charge Voltage 2 [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Voltage_L2                 | ElectricPotential |
| Evcc_Loadpoint_Charge_Voltage_L3                 | Number:ElectricPotential | Charge Voltage 3 [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Voltage_L3                 | ElectricPotential |
| Evcc_Loadpoint_Phases_Active                     | Number                   | Active Phases [%s]                        | evcc:battery:demo-server:demo-battery:Loadpoint_Phases_Active                     | –                 |
| Evcc_Loadpoint_Phases_Configured                 | Number                   | Configured Phases [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Phases_Configured                 | –                 |
| Evcc_Loadpoint_Plan_Active                       | Switch                   | Plan Activated [%s]                       | evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Active                       | –                 |
| Evcc_Loadpoint_Plan_Energy                       | Number:Energy            | Plan Energy [%s]                          | evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Energy                       | Energy            |
| Evcc_Loadpoint_Plan_Overrun                      | Number:Time              | Plan Overrun [%s]                         | evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Overrun                      | Time              |
| Evcc_Loadpoint_Plan_Precondition                 | Number:Time              | Plan Precondition [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Precondition                 | Time              |
| Evcc_Loadpoint_Priority                          | Number                   | Priority [%s]                             | evcc:battery:demo-server:demo-battery:Loadpoint_Priority                          | –                 |
| Evcc_Loadpoint_Pv_Action                         | String                   | PV Action [%s]                            | evcc:battery:demo-server:demo-battery:Loadpoint_Pv_Action                         | –                 |
| Evcc_Loadpoint_Pv_Remaining                      | Number:Time              | Pv Remaining [%s]                         | evcc:battery:demo-server:demo-battery:Loadpoint_Pv_Remaining                      | Time              |
| Evcc_Loadpoint_Session_Co2_Per_K_Wh              | Number:EmissionIntensity | Session CO2 Per kWh [%s]                  | evcc:battery:demo-server:demo-battery:Loadpoint_Session_Co2_Per_K_Wh              | EmissionIntensity |
| Evcc_Loadpoint_Session_Energy                    | Number:Energy            | Session Energy [%s]                       | evcc:battery:demo-server:demo-battery:Loadpoint_Session_Energy                    | Energy            |
| Evcc_Loadpoint_Session_Solar_Percentage          | Number:Dimensionless     | Session Solar Percentage [%s]             | evcc:battery:demo-server:demo-battery:Loadpoint_Session_Solar_Percentage          | Dimensionless     |
| Evcc_Loadpoint_Session_Price                     | Number:Currency          | Session Price [%s]                        | evcc:battery:demo-server:demo-battery:Loadpoint_Session_Price                     | Currency          |
| Evcc_Loadpoint_Session_Price_Per_K_Wh            | Number:EnergyPrice       | Session Price Per kWh [%s]                | evcc:battery:demo-server:demo-battery:Loadpoint_Session_Price_Per_K_Wh            | EnergyPrice       |
| Evcc_Loadpoint_Smart_Cost_Active                 | Switch                   | Smart Cost Active [%s]                    | evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Active                 | –                 |
| Evcc_Loadpoint_Smart_Cost_Limit_Co2              | Number:EmissionIntensity | Smart Cost Limit [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Limit_Co2              | EmissionIntensity |
| Evcc_Loadpoint_Smart_Cost_Limit_Price            | Number:EnergyPrice       | Smart Cost Limit [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Limit_Price            | EnergyPrice       |
| Evcc_Loadpoint_Smart_Cost_Next_Start             | DateTime                 | Smart Cost Next Start [%s]                | evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Next_Start             | –                 |
| Evcc_Loadpoint_Smart_Feed_In_Priority_Active     | Switch                   | Smart Feed In Priority Active [%s]        | evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Feed_In_Priority_Active     | –                 |
| Evcc_Loadpoint_Title                             | String                   | Title [%s]                                | evcc:battery:demo-server:demo-battery:Loadpoint_Title                             | –                 |
| Evcc_Loadpoint_Vehicle_Climater_Active           | Switch                   | Vehicle Climate Active [%s]               | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Climater_Active           | –                 |
| Evcc_Loadpoint_Vehicle_Detection_Active          | Switch                   | Vehicle Detection Active [%s]             | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Detection_Active          | –                 |
| Evcc_Loadpoint_Vehicle_Identity                  | String                   | Vehicle Identity [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Identity                  | –                 |
| Evcc_Loadpoint_Vehicle_Limit_Soc                 | Number:Dimensionless     | Vehicle API SoC Limit [%s]                | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Limit_Soc                 | Dimensionless     |
| Evcc_Loadpoint_Vehicle_Name                      | String                   | Vehicle Name [%s]                         | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Name                      | –                 |
| Evcc_Loadpoint_Vehicle_Odometer                  | Number:Length            | Vehicle Odometer [%s]                     | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Odometer                  | Length            |
| Evcc_Loadpoint_Vehicle_Range                     | Number:Length            | Vehicle Range [%s]                        | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Range                     | Length            |
| Evcc_Loadpoint_Vehicle_Soc                       | Number:Dimensionless     | Vehicle API SoC [%s]                      | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Soc                       | Dimensionless     |
| Evcc_Loadpoint_Vehicle_Title                     | String                   | Vehicle Title [%s]                        | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Title                     | –                 |
| Evcc_Loadpoint_Vehicle_Welcome_Active            | Switch                   | Vehicle Welcome Active [%s]               | evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Welcome_Active            | –                 |
| Evcc_Pv_Power                                    | Number:Power             | PV Power [%s]                             | evcc:battery:demo-server:demo-battery:Pv_Power                                    | Power             |
| Evcc_Pv_Title                                    | String                   | Title [%s]                                | evcc:battery:demo-server:demo-battery:Pv_Title                                    | –                 |
| Evcc_Site_Available_Version                      | String                   | Available Version [%s]                    | evcc:battery:demo-server:demo-battery:Site_Available_Version                      | –                 |
| Evcc_Site_Battery_Capacity                       | Number:Energy            | Site Battery Capacity [%s]                | evcc:battery:demo-server:demo-battery:Site_Battery_Capacity                       | Energy            |
| Evcc_Site_Battery_Discharge_Control              | Switch                   | Battery Discharge Control [%s]            | evcc:battery:demo-server:demo-battery:Site_Battery_Discharge_Control              | –                 |
| Evcc_Site_Battery_Energy                         | Number:Energy            | Battery Energy [%s]                       | evcc:battery:demo-server:demo-battery:Site_Battery_Energy                         | Energy            |
| Evcc_Site_Battery_Grid_Charge_Active             | Switch                   | Grid Charging Active [%s]                 | evcc:battery:demo-server:demo-battery:Site_Battery_Grid_Charge_Active             | –                 |
| Evcc_Site_Battery_Grid_Charge_Limit_Co2          | Number:EmissionIntensity | Grid Charging Limit [%s]                  | evcc:battery:demo-server:demo-battery:Site_Battery_Grid_Charge_Limit_C            |                   |

## Troubleshooting

If you need additional data that can be read out of the API response please reach out to the openHAB community.
