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

- `server`: A running evcc instance. It will be used as the bridge for the other things
- `battery`: A battery configured in your evcc instance.
- `heating`: A heating loadpoint configured in your evcc instance.
- `loadpoint`: A loadpoint configured in your evcc instance.
- `pv`: A photovoltaic system configured in your evcc instance.
- `site`: The relevant site data from your evcc instance.
- `vehicle`: A vehicle configured in your evcc instance

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

### Thing Site

The Site Thing represents the overall site data of your evcc instance.
No configuration parameters are required.

### Thing Battery

The Battery Thing represents a battery configured in your evcc instance.
Needs the index of the battery as configuration parameter.

### Thing PV

The PV Thing represents a photovoltaic system configured in your evcc instance.
Needs the index of the photovoltaic system as configuration parameter.

### Thing Loadpoint

The Loadpoint Thing represents a loadpoint configured in your evcc instance.
Needs the index of the loadpoint as configuration parameter.

### Thing Vehicle

The Vehicle Thing represents a vehicle configured in your evcc instance.
Needs the database id of the vehicle as configuration parameter.

### Thing Plan

The Plan Thing represents a charging plan for a vehicle configured in your evcc instance.
Needs the index of the plan and the database id of the vehicle as configuration parameters.
The index 0 is always the One-time plan, higher indices are repeating plans.

Any changes made to the plan channels will not be sent to evcc automatically, but cached.
If you want to update the plan, you have to use the update plan channel of the thing to send it to evcc.
**Updating the plan will only work when setting the update plan channel to state ON**.
Afterwards, the update plan channel will be automatically reset to state OFF.
The weekdays will be localized based on the language settings of your openHAB instance.

Here is an example to update a One-time charging plan via DLSRule script:

```DSLRule
One_time_charging_plan_1_for_BMW_iX3_Plan_Time.sendCommand("2025-12-19T06:00:00.000Z");
One_time_charging_plan_1_for_BMW_iX3_Plan_SoC.sendCommand(85);
One_time_charging_plan_1_for_BMW_iX3_Precondition_Time.sendCommand(1800);
One_time_charging_plan_1_for_BMW_iX3_Update_Plan.sendCommand("ON");
```


Here is an example to update a repeating charging plan via DLSRule script:

```DSLRule
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
    Thing vehicle demo-vehicle1 "Vehicle - evcc Demo Vehicle 1"[vehicle-id="vehicle_1"]
    ..
    // You can define as many Plan things as you have plans for your vehicle configured
    Thing plan demo-one-time-plan-for-vehicle1 "One-time plan for vehicle 1"[index=0, vehicle-id="vehicle_1"]
    Thing plan demo-repeating-plan-1-for-vehicle1 "Repeating plan 1 for vehicle 1"[index=1, vehicle-id="vehicle_1"]
    ..
}
```

### `demo.items` Example

List of all possible channels

```java
Number:Energy Evcc_Battery_Capacity "Battery Capacity [%s]" { channel="evcc:battery:demo-server:demo-battery:Battery_Capacity" }
Switch Evcc_Battery_Controllable "Battery Controllable [%s]" { channel="evcc:battery:demo-server:demo-battery:Battery_Controllable" }
Number:Power Evcc_Battery_Power "Battery Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Battery_Power" }
Number:Dimensionless Evcc_Battery_Soc "Battery SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Battery_Soc" }
String Evcc_Battery_Title "Title [%s]" { channel="evcc:battery:demo-server:demo-battery:Battery_Title" }
Number:Temperature Evcc_Loadpoint_Effective_Limit_Temperature "Effective Charging Limit Temperature [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Limit_Temperature" }
Number:Temperature Evcc_Loadpoint_Effective_Plan_Temperature "Effective Plan Limit Temperature [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Temperature" }
Number:Temperature Evcc_Loadpoint_Limit_Temperature "Temperature Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Limit_Temperature" }
Number:Temperature Evcc_Loadpoint_Vehicle_Limit_Temperature "Device Temperature Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Limit_Temperature" }
Number:Temperature Evcc_Loadpoint_Vehicle_Temperature "Device Temperature [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Temperature" }
Switch Evcc_Loadpoint_Battery_Boost "Battery Boost [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Battery_Boost" }
Number:Time Evcc_Loadpoint_Charge_Duration "Charging Duration [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Duration" }
Number:Power Evcc_Loadpoint_Charge_Power "Charging Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Power" }
Number:Energy Evcc_Loadpoint_Charge_Total_Import "Charge Total Import [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Total_Import" }
Number:Time Evcc_Loadpoint_Charge_Remaining_Duration "Charging Remaining Duration [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Remaining_Duration" }
Number:Energy Evcc_Loadpoint_Charge_Remaining_Energy "Charging Remaining Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Remaining_Energy" }
Number:Energy Evcc_Loadpoint_Charged_Energy "Charged Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charged_Energy" }
String Evcc_Loadpoint_Charger_Icon "Charger Icon [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Icon" }
Switch Evcc_Loadpoint_Charger_Feature_Heating "Heating [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Feature_Heating" }
Switch Evcc_Loadpoint_Charger_Feature_Integrated_Device "Integrated Device [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Feature_Integrated_Device" }
Switch Evcc_Loadpoint_Charger_Phases1p3p "Phase Switching [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Phases1p3p" }
Switch Evcc_Loadpoint_Charger_Single_Phase "1 Phase Charging [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Single_Phase" }
String Evcc_Loadpoint_Charger_Status_Reason "Charger Status Reason [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charger_Status_Reason" }
Switch Evcc_Loadpoint_Charging "Charging State [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charging" }
Switch Evcc_Loadpoint_Connected "Vehicle Connected [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Connected" }
Number:Time Evcc_Loadpoint_Connected_Duration "Vehicle Conn. Duration [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Connected_Duration" }
Number:Time Evcc_Loadpoint_Disable_Delay "Disable Delay [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Disable_Delay" }
Number:Power Evcc_Loadpoint_Disable_Threshold "Disable Threshold [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Disable_Threshold" }
Number:Dimensionless Evcc_Loadpoint_Effective_Limit_Soc "Active Charging Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Limit_Soc" }
Number:ElectricCurrent Evcc_Loadpoint_Effective_Max_Current "Active Maximum Current [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Max_Current" }
Number:ElectricCurrent Evcc_Loadpoint_Effective_Min_Current "Active Minimum Current [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Min_Current" }
Number Evcc_Loadpoint_Effective_Plan_Id "Active Plan ID [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Id" }
Number:Dimensionless Evcc_Loadpoint_Effective_Plan_Soc "Active Plan SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Soc" }
DateTime Evcc_Loadpoint_Effective_Plan_Time "Active Plan Time [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Plan_Time" }
Switch Evcc_Loadpoint_Enabled "Enabled [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Enabled" }
DateTime Evcc_Loadpoint_Plan_Projected_End "Plan Projected End [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Projected_End" }
DateTime Evcc_Loadpoint_Plan_Projected_Start "Plan Projected Start [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Projected_Start" }
Number Evcc_Loadpoint_Effective_Priority "Active Priority [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Effective_Priority" }
Number:Time Evcc_Loadpoint_Enable_Delay "Enable Delay [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Enable_Delay" }
Number:Power Evcc_Loadpoint_Enable_Threshold "Enable Threshold [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Enable_Threshold" }
Switch Evcc_Loadpoint_Enabled "Loadpoint Enabled [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Enabled" }
Number:Energy Evcc_Loadpoint_Limit_Energy "Energy Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Limit_Energy" }
Number:Dimensionless Evcc_Loadpoint_Limit_Soc "SoC Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Limit_Soc" }
Number:ElectricCurrent Evcc_Loadpoint_Max_Current "Maximum Current [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Max_Current" }
Number:ElectricCurrent Evcc_Loadpoint_Min_Current "Minimum Current [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Min_Current" }
String Evcc_Loadpoint_Mode "Mode [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Mode" }
Number:ElectricCurrent Evcc_Loadpoint_Offered_Current "Offered Current [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Offered_Current" }
String Evcc_Loadpoint_Phase_Action "Phase Scaling [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Phase_Action" }
Number:Time Evcc_Loadpoint_Phase_Remaining "Phase Remaining Timer [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Phase_Remaining" }
Number:ElectricCurrent Evcc_Loadpoint_Charge_Current_L1 "Charge Current 1 [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Current_L1" }
Number:ElectricCurrent Evcc_Loadpoint_Charge_Current_L2 "Charge Current 2 [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Current_L2" }
Number:ElectricCurrent Evcc_Loadpoint_Charge_Current_L3 "Charge Current 3 [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Current_L3" }
Number:ElectricPotential Evcc_Loadpoint_Charge_Voltage_L1 "Charge Voltage 1 [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Voltage_L1" }
Number:ElectricPotential Evcc_Loadpoint_Charge_Voltage_L2 "Charge Voltage 2 [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Voltage_L2" }
Number:ElectricPotential Evcc_Loadpoint_Charge_Voltage_L3 "Charge Voltage 3 [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Charge_Voltage_L3" }
Number Evcc_Loadpoint_Phases_Active "Active Phases [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Phases_Active" }
Number Evcc_Loadpoint_Phases_Configured "Configured Phases [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Phases_Configured" }
Switch Evcc_Loadpoint_Plan_Active "Plan Activated [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Active" }
Number:Energy Evcc_Loadpoint_Plan_Energy "Plan Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Energy" }
Number:Time Evcc_Loadpoint_Plan_Overrun "Plan Overrun [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Overrun" }
Number:Time Evcc_Loadpoint_Plan_Precondition "Plan Precondition [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Plan_Precondition" }
Number Evcc_Loadpoint_Priority "Priority [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Priority" }
String Evcc_Loadpoint_Pv_Action "PV Action [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Pv_Action" }
Number:Time Evcc_Loadpoint_Pv_Remaining "Pv Remaining [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Pv_Remaining" }
Number:EmissionIntensity Evcc_Loadpoint_Session_Co2_Per_K_Wh "Session CO2 Per kWh [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Session_Co2_Per_K_Wh" }
Number:Energy Evcc_Loadpoint_Session_Energy "Session Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Session_Energy" }
Number:Dimensionless Evcc_Loadpoint_Session_Solar_Percentage "Session Solar Percentage [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Session_Solar_Percentage" }
Number:Currency Evcc_Loadpoint_Session_Price "Session Price [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Session_Price" }
Number:EnergyPrice Evcc_Loadpoint_Session_Price_Per_K_Wh "Session Price Per kWh [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Session_Price_Per_K_Wh" }
Switch Evcc_Loadpoint_Smart_Cost_Active "Smart Cost Active [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Active" }
Number:EmissionIntensity Evcc_Loadpoint_Smart_Cost_Limit_Co2 "Smart Cost Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Limit_Co2" }
Number:EnergyPrice Evcc_Loadpoint_Smart_Cost_Limit_Price "Smart Cost Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Limit_Price" }
DateTime Evcc_Loadpoint_Smart_Cost_Next_Start "Smart Cost Next Start [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Cost_Next_Start" }
Switch Evcc_Loadpoint_Smart_Feed_In_Priority_Active "Smart Feed In Priority Active [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Smart_Feed_In_Priority_Active" }
String Evcc_Loadpoint_Title "Title [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Title" }
Switch Evcc_Loadpoint_Vehicle_Climater_Active "Vehicle Climate Active [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Climater_Active" }
Switch Evcc_Loadpoint_Vehicle_Detection_Active "Vehicle Detection Active [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Detection_Active" }
String Evcc_Loadpoint_Vehicle_Identity "Vehicle Identity [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Identity" }
Number:Dimensionless Evcc_Loadpoint_Vehicle_Limit_Soc "Vehicle API SoC Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Limit_Soc" }
String Evcc_Loadpoint_Vehicle_Name "Vehicle Name [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Name" }
Number:Length Evcc_Loadpoint_Vehicle_Odometer "Vehicle Odometer [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Odometer" }
Number:Length Evcc_Loadpoint_Vehicle_Range "Vehicle Range [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Range" }
Number:Dimensionless Evcc_Loadpoint_Vehicle_Soc "Vehicle API SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Soc" }
String Evcc_Loadpoint_Vehicle_Title "Vehicle Title [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Title" }
Switch Evcc_Loadpoint_Vehicle_Welcome_Active "Vehicle Welcome Active [%s]" { channel="evcc:battery:demo-server:demo-battery:Loadpoint_Vehicle_Welcome_Active" }
Number:Power Evcc_Pv_Power "PV Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Pv_Power" }
String Evcc_Pv_Title "Title [%s]" { channel="evcc:battery:demo-server:demo-battery:Pv_Title" }
String Evcc_Site_Available_Version "Available Version [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Available_Version" }
Number:Energy Evcc_Site_Battery_Capacity "Site Battery Capacity [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Capacity" }
Switch Evcc_Site_Battery_Discharge_Control "Battery Discharge Control [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Discharge_Control" }
Number:Energy Evcc_Site_Battery_Energy "Battery Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Energy" }
Switch Evcc_Site_Battery_Grid_Charge_Active "Grid Charging Active [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Grid_Charge_Active" }
Number:EmissionIntensity Evcc_Site_Battery_Grid_Charge_Limit_Co2 "Grid Charging Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Grid_Charge_Limit_Co2" }
Number:EnergyPrice Evcc_Site_Battery_Grid_Charge_Limit_Price "Grid Charging Limit [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Grid_Charge_Limit_Price" }
String Evcc_Site_Battery_Mode "Battery Mode [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Mode" }
String Evcc_Site_Battery_Mode_External "Battery Mode [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Mode_External" }
Number:Power Evcc_Site_Battery_Power "Battery Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Power" }
Number:Dimensionless Evcc_Site_Battery_Soc "Battery SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Battery_Soc" }
Number:Dimensionless Evcc_Site_Buffer_Soc "Battery Buffer SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Buffer_Soc" }
Number:Dimensionless Evcc_Site_Buffer_Start_Soc "Battery Buffer Start SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Buffer_Start_Soc" }
String Evcc_Site_Currency "Currency [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Currency" }
Switch Evcc_Site_Demo_Mode "Demo Mode [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Demo_Mode" }
Switch Evcc_Site_Eebus "EEBUS [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Eebus" }
Number Evcc_Site_Green_Share_Home "Green Share Home [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Green_Share_Home" }
Number Evcc_Site_Green_Share_Loadpoints "Green Share Loadpoints [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Green_Share_Loadpoints" }
Number:ElectricCurrent Evcc_Site_Grid_Current_L1 "Grid Current 1 [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Current_L1" }
Number:ElectricCurrent Evcc_Site_Grid_Current_L2 "Grid Current 2 [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Current_L2" }
Number:ElectricCurrent Evcc_Site_Grid_Current_L3 "Grid Current 3 [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Current_L3" }
Number:ElectricPotential Evcc_Site_Grid_Voltage_L1 "Charge Voltage 1 [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Voltage_L1" }
Number:ElectricPotential Evcc_Site_Grid_Voltage_L2 "Charge Voltage 2 [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Voltage_L2" }
Number:ElectricPotential Evcc_Site_Grid_Voltage_L3 "Charge Voltage 3 [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Voltage_L3" }
Number:Energy Evcc_Site_Grid_Energy "Grid Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Energy" }
Number:Power Evcc_Site_Grid_Power "Grid Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Grid_Power" }
Number:Power Evcc_Site_Home_Power "Home Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Home_Power" }
Switch Evcc_Site_Messaging "Site Messaging [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Messaging" }
Number:Dimensionless Evcc_Site_Priority_Soc "Battery Priority SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Priority_Soc" }
Number:Time Evcc_Site_Interval "Interval [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Interval" }
Number:Energy Evcc_Site_Pv_Energy "PV Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Pv_Energy" }
Number:Power Evcc_Site_Pv_Power "PV Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Pv_Power" }
Number:Power Evcc_Site_Residual_Power "Grid Residual Power [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Residual_Power" }
Switch Evcc_Site_Smart_Cost_Available "Smart Cost Available [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Smart_Cost_Available" }
String Evcc_Site_Smart_Cost_Type "Smart Cost Type [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Smart_Cost_Type" }
Switch Evcc_Site_Smart_Feed_In_Priority_Available "Smart Feed In Priority Available [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Smart_Feed_In_Priority_Available" }
Switch Evcc_Site_Startup_Completed "Start Up Complete [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Startup_Completed" }
Number:EmissionIntensity Evcc_Site_Tariff_Co2 "CO2 Concentration [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Co2" }
Number:EmissionIntensity Evcc_Site_Tariff_Co2_Home "CO2 Concentration Home [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Co2_Home" }
Number:EmissionIntensity Evcc_Site_Tariff_Co2_Loadpoints "CO2 Concentration Loadpoints [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Co2_Loadpoints" }
Number:EnergyPrice Evcc_Site_Tariff_Feed_In "Feed in price per kWh [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Feed_In" }
Number:EnergyPrice Evcc_Site_Tariff_Grid "Grid Price [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Grid" }
Number:EnergyPrice Evcc_Site_Tariff_Price_Home "Home Price [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Price_Home" }
Number:EnergyPrice Evcc_Site_Tariff_Price_Loadpoints "Loadpoint price per kWh [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Price_Loadpoints" }
Number:Power Evcc_Site_Tariff_Solar "Solar Tariff [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Tariff_Solar" }
String Evcc_Site_Site_Title "Site Title [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Site_Title" }
String Evcc_Site_Version "Version [%s]" { channel="evcc:battery:demo-server:demo-battery:Site_Version" }
Number:EmissionIntensity Evcc_Avg_Co2_Type "Average CO2 [%s]" { channel="evcc:battery:demo-server:demo-battery:Avg_Co2_Type" }
Number:EnergyPrice Evcc_Avg_Price_Type "Average Price [%s]" { channel="evcc:battery:demo-server:demo-battery:Avg_Price_Type" }
Number:Energy Evcc_Charged_Energy_Type "Charged Energy [%s]" { channel="evcc:battery:demo-server:demo-battery:Charged_Energy_Type" }
Number:Dimensionless Evcc_Solar_Percentage_Type "Solar Percentage [%s]" { channel="evcc:battery:demo-server:demo-battery:Solar_Percentage_Type" }
Number:Energy Evcc_Vehicle_Capacity "Vehicle Capacity [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Capacity" }
String Evcc_Vehicle_Icon "Vehicle Icon [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Icon" }
Number:Dimensionless Evcc_Vehicle_Limit_Soc "Vehicle Limit SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Limit_Soc" }
Number:Dimensionless Evcc_Vehicle_Min_Soc "Vehicle Min SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Min_Soc" }
Number:ElectricCurrent Evcc_Vehicle_Max_Current "Vehicle Max Current [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Max_Current" }
Number:ElectricCurrent Evcc_Vehicle_Min_Current "Vehicle Min Current [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Min_Current" }
Number:Time Evcc_Vehicle_Plan_Precondition "Plan Precondition Duration [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Plan_Precondition" }
Number:Dimensionless Evcc_Vehicle_Plan_Soc "Planned SoC [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Plan_Soc" }
DateTime Evcc_Vehicle_Plan_Time "Plan Datetime [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Plan_Time" }
String Evcc_Vehicle_Title "Vehicle Title [%s]" { channel="evcc:battery:demo-server:demo-battery:Vehicle_Title" }
```

## Migration / Update

Renamed property siteTitle -> site-title
Renamed property id -> vehicle-id and renamed parameter id -> vehicle-id, you may need to parametrize you thing.

## Troubleshooting

If you need additional data that can be read out of the API response please reach out to the openHAB community.
