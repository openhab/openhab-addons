# Solarman Logger Binding

[![Build Status](https://app.travis-ci.com/catalinsanda/org.openhab.binding.solarman.svg?branch=main)](https://app.travis-ci.com/catalinsanda/org.openhab.binding.solarman)

Binding used to communicate with Solarman (IGEN-Tech) v5 based solar inverter data loggers in direct-mode over the local
network.
More information about the different types of stick loggers is available on the
[Solarman](https://www.solarmanpv.com/products/data-logger/stick-logger/) site.

These data loggers are used by inverters from a lot of manufacturers, just to name a few:

- Deye
- Sofar
- Solis
- ZCS Azzurro
- KStar

## Supported Things

The `solarman:logger` thing supports reading data from a Solarman LSW-3 Stick Logger (it might also work with LSE-3 and
maybe others)
when connected to a supported inverter.

It was tested on a SUN-10K-SG04LP3-EU only, but because the implementation uses the inverter definitions created as part
of
[Stephan Joubert's Home Assistant plugin](https://github.com/StephanJoubert/home_assistant_solarman)
it **might** work with the other inverters supported by the plugin.

## Thing Configuration

To connect the logger you need the IP address of the logger and its serial number. The IP address can be obtained from
your router and the serial number can either be read from the label of the logger, or by connecting to the logger with
a browser (default user/pass: admin/admin) and getting it from the Status page. Please note that you need the
"Device serial number" from the "Device information" section, not the "Inverter serial number".

### `solarman:logger` Thing Configuration

| Name               | Type    | Description                                             | Default      | Required | Advanced |
|--------------------|---------|---------------------------------------------------------|--------------|----------|----------|
| hostname           | text    | Hostname or IP address of the Solarman logger           | N/A          | yes      | no       |
| serialNumber       | text    | Serial number of the Solarman logger                    | N/A          | yes      | no       |
| inverterType       | text    | The type of inverter connected to the logger            | deye_sg04lp3 | no       | no       |
| port               | integer | Port of the Solarman logger                             | 8899         | no       | yes      |
| refreshInterval    | integer | Interval the device is polled in sec.                   | 60           | no       | yes      |
| additionalRequests | text    | Additional requests besides the ones in the deffinition | N/A          | no       | yes      |


The `inverterType` parameter governs what registers the binding will read from the logger and what channels it will
expose.

Possible values:

| Inverter Type      | Inverters supported                         | Notes                                                            |
|--------------------|---------------------------------------------|------------------------------------------------------------------|
| deye_hybrid        | DEYE/Sunsynk/SolArk Hybrid inverters        | used when no lookup specified                                    |
| deye_sg04lp3       | DEYE/Sunsynk/SolArk Hybrid 8/12K-SG04LP3    | e.g. 12K-SG04LP3-EU                                              |
| deye_string        | DEYE/Sunsynk/SolArk String inverters        | e.g. SUN-4/5/6/7/8/10/12K-G03 Plus                               |
| deye_2mppt         | DEYE Microinverter with 2 MPPT Trackers     | e.g. SUN600G3-EU-230 / SUN800G3-EU-230 / SUN1000G3-EU-230        |
| deye_4mppt         | DEYE Microinverter with 4 MPPT Trackers     | e.g. SUN1300G3-EU-230 / SUN1600G3-EU-230 / SUN2000G3-EU-230      |
| sofar_lsw3         | SOFAR Inverters                             |                                                                  |
| sofar_g3hyd        | SOFAR Hybrid Three-Phase inverter           | HYD 6000 or rebranded (three-phase), ex. ZCS Azzurro 3PH HYD-ZSS |
| sofar_hyd3k-6k-es  | SOFAR Hybrid Single-Phase inverter          | HYD 6000 or rebranded (single-phase), ex. ZCS Azzurro HYD-ZSS    |
| solis_hybrid       | SOLIS Hybrid inverter                       |                                                                  |
| solid_1p8k-5g      | SOLIS 1P8K-5G                               |                                                                  |
| zcs_azzurro-ktl-v3 | ZCS Azzurro KTL-V3 inverters                | ZCS Azzurro 3.3/4.4/5.5/6.6 KTL-V3 (rebranded Sofar KTLX-G3)     |

The `additionalRequests` allows the user to specify additional address ranges to be polled. The format of the value is `mb_functioncode1:start1-end1, mb_functioncode2:start2-end2,...`
For example `"0x03:0x27D-0x27E"` will issue an additional read for Holding Registers between `0x27D` and `0x27E`.

This is useful when coupled with user defined channels, for example a thing definition like the one below will also read the register
for the AC frequency on a Deye inverter, besides the ones pre-defined in the `deye_sg04lp3` inverter definition.
```java
Thing solarman:logger:local [ hostname="x.x.x.x", inverterType="deye_sg04lp3", serialNumber="1234567890", additionalRequests="0x03:0x27D-0x27E" ] {
        Channels:
        Type number : Inverter_Frequency [scale="0.01", uom="Hz", rule="3", registers="0x27E"]
}
```

**Please note**

As of this writing inverter types besides the `deye_sg04lp3` were not tested to work. If you have one of those inverters and it
works,
please drop me a message, if it doesn't work, please open an issue and I'll try to fix it.

## Channels

The list of channels is not static, it is generated dynamically based on the inverter type selected.

This is the list you get for the `deye_sg04lp3` inverter type:

| Channel                          | Type   | Read/Write | Description                                        |
|----------------------------------|--------|------------|----------------------------------------------------|
| daily_battery_discharge          | Number | R          | Daily Battery Discharge \[0x0203\]                 |
| internal_ct_l3_power             | Number | R          | Internal CT L3 Power \[0x025E\]                    |
| total_battery_discharge          | Number | R          | Total Battery Discharge \[0x0206,0x0207\]          |
| daily_production                 | Number | R          | Daily Production \[0x0211\]                        |
| ac_temperature                   | Number | R          | AC Temperature \[0x021D\]                          |
| pv1_current                      | Number | R          | PV1 Current \[0x02A5\]                             |
| inverter_l2_power                | Number | R          | Inverter L2 Power \[0x027A\]                       |
| pv2_voltage                      | Number | R          | PV2 Voltage \[0x02A6\]                             |
| total_grid_production            | Number | R          | Total Grid Production \[0x020C,0x020D\]            |
| load_voltage_l3                  | Number | R          | Load Voltage L3 \[0x0286\]                         |
| load_voltage_l2                  | Number | R          | Load Voltage L2 \[0x0285\]                         |
| daily_load_consumption           | Number | R          | Daily Load Consumption \[0x020E\]                  |
| daily_energy_bought              | Number | R          | Daily Energy Bought \[0x0208\]                     |
| load_voltage_l1                  | Number | R          | Load Voltage L1 \[0x0284\]                         |
| grid_voltage_l2                  | Number | R          | Grid Voltage L2 \[0x0257\]                         |
| grid_voltage_l1                  | Number | R          | Grid Voltage L1 \[0x0256\]                         |
| communication_board_version_no   | Number | R          | Communication Board Version No. \[0x0011\]         |
| inverter_id                      | String | R          | Inverter ID \[0x0003,0x0004,0x0005,0x0006,0x0007\] |
| battery_current                  | Number | R          | Battery Current \[0x024F\]                         |
| battery_temperature              | Number | R          | Battery Temperature \[0x024A\]                     |
| daily_energy_sold                | Number | R          | Daily Energy Sold \[0x0209\]                       |
| grid_voltage_l3                  | Number | R          | Grid Voltage L3 \[0x0258\]                         |
| battery_power                    | Number | R          | Battery Power \[0x024E\]                           |
| load_l3_power                    | Number | R          | Load L3 Power \[0x028C\]                           |
| pv2_current                      | Number | R          | PV2 Current \[0x02A7\]                             |
| external_ct_l2_power             | Number | R          | External CT L2 Power \[0x0269\]                    |
| total_energy_bought              | Number | R          | Total Energy Bought \[0x020A,0x020B\]              |
| total_production                 | Number | R          | Total Production \[0x0216,0x0217\]                 |
| load_l2_power                    | Number | R          | Load L2 Power \[0x028B\]                           |
| load_l1_power                    | Number | R          | Load L1 Power \[0x028A\]                           |
| daily_grid_consumption           | Number | R          | Daily Grid Consumption \[0x0210\]                  |
| external_ct_l1_power             | Number | R          | External CT L1 Power \[0x0268\]                    |
| inverter_l3_power                | Number | R          | Inverter L3 Power \[0x027B\]                       |
| pv1_voltage                      | Number | R          | PV1 Voltage \[0x02A4\]                             |
| total_load_consumption           | Number | R          | Total Load Consumption \[0x0212,0x0213\]           |
| inverter_l1_power                | Number | R          | Inverter L1 Power \[0x0279\]                       |
| external_ct_l3_power             | Number | R          | External CT L3 Power \[0x026A\]                    |
| daily_energy_consumption         | Number | R          | Daily Energy Consumption \[0x020F\]                |
| total_energy_sold                | Number | R          | Total Energy Sold \[0x0201,0x0202\]                |
| total_grid_consumption           | Number | R          | Total Grid Consumption \[0x0214,0x0215\]           |
| inverter_temperature             | Number | R          | Inverter Temperature \[0x027D\]                    |
| ac_frequency                     | Number | R          | AC Frequency \[0x021C\]                            |
| battery_voltage                  | Number | R          | Battery Voltage \[0x0248\]                         |
| battery_soc                      | Number | R          | Battery SOC \[0x0249\]                             |
| ac_output_power                  | Number | R          | AC Output Power \[0x021B\]                         |
| total_energy_consumption         | Number | R          | Total Energy Consumption \[0x0218,0x0219\]         |

## Full Example

This example is what I use for my DEYE 12kW (SUN-12K-SG04LP3-EU) hybrid inverter

### Thing Configuration

Please replace the `hostname` and `serialNumber` with the correct values for your logger.

```java
Thing solarman:logger:local[hostname="x.x.x.x",inverterType="deye_sg04lp3",serialNumber="1234567890"]{
        }
```

### Item Configuration

Items file I use for my SUN-12K-SG04LP3-EU inverter

```text
    Number Communication_Board_Version_No "Communication Board Version No [%s]" (solarman) {channel="solarman:logger:local:inverter_communication_board_version_no"}
    Number Control_Board_Version_No "Control Board Version No [%s]" (solarman) {channel="solarman:logger:local:inverter_control_board_version_no"}
    String Inverter_Id "Inverter Id [%s]" (solarman) {channel="solarman:logger:local:inverter_inverter_id"}
    Number Daily_Battery_Charge "Daily Battery Charge [%.1f kWh]" (solarman) {channel="solarman:logger:local:battery_daily_battery_charge"}
    Number Daily_Battery_Discharge "Daily Battery Discharge [%.1f kWh]" (solarman) {channel="solarman:logger:local:battery_daily_battery_discharge"}
    Number Total_Battery_Charge "Total Battery Charge [%d kWh]" (solarman) {channel="solarman:logger:local:battery_total_battery_charge"}
    Number Total_Production "Total Production [%d kWh]" (solarman) {channel="solarman:logger:local:solar_total_production"}
    Number Total_Grid_Production "Total Grid Feed-in [%.1f kWh]" (solarman) {channel="solarman:logger:local:grid_total_grid_production"}
    Number Daily_Energy_Sold "Daily Energy Sold [%d Wh]" (solarman) {channel="solarman:logger:local:grid_daily_energy_sold"}
    Number Daily_Load_Consumption "Daily Load Consumption [%.1f kWh]" (solarman) {channel="solarman:logger:local:upload_daily_load_consumption"}
    Number AC_Temperature "AC Temperature [%.1f °C]" (solarman) {channel="solarman:logger:local:inverter_ac_temperature"}
    Number Total_Energy_Sold "Total Energy Sold [%d kWh]" (solarman) {channel="solarman:logger:local:grid_total_energy_sold"}
    Number Total_Load_Consumption "Total Load Consumption [%d kWh]" (solarman) {channel="solarman:logger:local:upload_total_load_consumption"}
    Number DC_Temperature "DC Temperature [%.1f °C]" (solarman) {channel="solarman:logger:local:inverter_dc_temperature"}
    Number Daily_Energy_Bought "Daily Energy Bought [%d kWh]" (solarman) {channel="solarman:logger:local:grid_daily_energy_bought"}
    Number Daily_Production "Daily Production [%.1f kWh]" (solarman) {channel="solarman:logger:local:solar_daily_production"}
    Number Alert "Alert [%s]" (solarman) {channel="solarman:logger:local:alert_alert"}
    Number Total_Battery_Discharge "Total Battery Discharge [%d kWh]" (solarman) {channel="solarman:logger:local:battery_total_battery_discharge"}
    Number Total_Energy_Bought "Total Energy Bought [%d kWh]" (solarman) {channel="solarman:logger:local:grid_total_energy_bought"}
    Number Battery_SOC "Battery SOC [%d %%]" (solarman) {channel="solarman:logger:local:battery_battery_soc"}
    Number Battery_Current "Battery Current [%.1f A]" (solarman) {channel="solarman:logger:local:battery_battery_current"}
    Number Battery_Power "Battery Power [%d W]" (solarman) {channel="solarman:logger:local:battery_battery_power"}
    Number Battery_Voltage "Battery Voltage [%.2f V]" (solarman) {channel="solarman:logger:local:battery_battery_voltage"}
    Number Battery_Temperature "Battery Temperature [%.1f °C]" (solarman) {channel="solarman:logger:local:battery_battery_temperature"}
    Number Inverter_L2_Power "Inverter L2 Power [%d W]" (solarman) {channel="solarman:logger:local:inverter_inverter_l2_power"}
    Number External_CT_L1_Power "External CT L1 Power [%d W]" (solarman) {channel="solarman:logger:local:grid_external_ct_l1_power"}
    Number External_CT_L2_Power "External CT L2 Power [%d W]" (solarman) {channel="solarman:logger:local:grid_external_ct_l2_power"}
    Number Grid_Voltage_L1 "Grid Voltage L1 [%d W]" (solarman) {channel="solarman:logger:local:grid_grid_voltage_l1"}
    Number Total_Grid_Power "Total Instant Grid Power [%d W]" (solarman) {channel="solarman:logger:local:grid_total_grid_power"}
    Number Current_L2 "Current L2 [%.1f A]" (solarman) {channel="solarman:logger:local:inverter_current_l2"}
    Number Internal_CT_L1_Power "Internal CT L1 Power [%d W]" (solarman) {channel="solarman:logger:local:grid_internal_ct_l1_power"}
    Number Current_L1 "Current L1 [%.1f A]" (solarman) {channel="solarman:logger:local:inverter_current_l1"}
    Number Inverter_L1_Power "Inverter L1 Power [%d W]" (solarman) {channel="solarman:logger:local:inverter_inverter_l1_power"}
    Number Internal_CT_L2_Power "Internal CT L2 Power [%d W]" (solarman) {channel="solarman:logger:local:grid_internal_ct_l2_power"}
    Number Grid_Voltage_L3 "Grid Voltage L3 [%d V]" (solarman) {channel="solarman:logger:local:grid_grid_voltage_l3"}
    Number Inverter_L3_Power "Inverter L3 Power [%d W]" (solarman) {channel="solarman:logger:local:inverter_inverter_l3_power"}
    Number Internal_CT_L3_Power "Internal CT L3 Power [%d W]" (solarman) {channel="solarman:logger:local:grid_internal_ct_l3_power"}
    Number Current_L3 "Current L3 [%.1f A]" (solarman) {channel="solarman:logger:local:inverter_current_l3"}
    Number Grid_Voltage_L2 "Grid Voltage L2 [%d V]" (solarman) {channel="solarman:logger:local:grid_grid_voltage_l2"}
    Number External_CT_L3_Power "External CT L3 Power [%d W]" (solarman) {channel="solarman:logger:local:grid_external_ct_l3_power"}
    Number Load_L1_Power "Load L1 Power [%d W]" (solarman) {channel="solarman:logger:local:upload_load_l1_power"}
    Number Total_Load_Power "Total Load Power [%d W]" (solarman) {channel="solarman:logger:local:upload_total_load_power"}
    Number Load_L2_Power "Load L2 Power [%d W]" (solarman) {channel="solarman:logger:local:upload_load_l2_power"}
    Number Load_L3_Power "Load Load L3 Power [%d W]" (solarman) {channel="solarman:logger:local:upload_load_l3_power"}
    Number Load_Voltage_L1 "Load Voltage L1 [%d V]" (solarman) {channel="solarman:logger:local:upload_load_voltage_l1"}
    Number Load_Voltage_L3 "Load Voltage L3 [%d V]" (solarman) {channel="solarman:logger:local:upload_load_voltage_l3"}
    Number Load_Voltage_L2 "Load Voltage L2 [%d V]" (solarman) {channel="solarman:logger:local:upload_load_voltage_l2"}
    Number PV1_Current "PV1 Current [%.1f A]" (solarman) {channel="solarman:logger:local:solar_pv1_current"}
    Number PV1_Power "PV1 Power [%d W]" (solarman) {channel="solarman:logger:local:solar_pv1_power"}
    Number PV2_Voltage "PV2 Voltage [%d V]" (solarman) {channel="solarman:logger:local:solar_pv2_voltage"}
    Number PV2_Current "PV2 Current [%.1f A]" (solarman) {channel="solarman:logger:local:solar_pv2_current"}
    Number PV2_Power "PV2 Power [%d W]" (solarman) {channel="solarman:logger:local:solar_pv2_power"}
    Number PV1_Voltage "PV1 Voltage [%d V]" (solarman) {channel="solarman:logger:local:solar_pv1_voltage"}
```

### Sitemap Configuration

Sitemap I use for my SUN-12K-SG04LP3-EU inverter

```perl
sitemap solarman label="Solarman"
{
    Frame label="Inverter"{
        Text item=Communication_Board_Version_No icon="solar"
        Text item=Control_Board_Version_No icon="solar"
        Text item=Inverter_Id icon="solar"
        Text item=AC_Temperature icon="temperature"
        Text item=DC_Temperature icon="temperature"
        Text item=Inverter_L1_Power icon="poweroutlet"
        Text item=Inverter_L2_Power icon="poweroutlet"
        Text item=Inverter_L3_Power icon="poweroutlet"
        Text item=Current_L1 icon="line"
        Text item=Current_L2 icon="line"
        Text item=Current_L3 icon="line"
    }
    
    Frame label="Battery"{
        Text item=Battery_SOC icon="battery"
        Text item=Battery_Current icon="current"
        Text item=Battery_Power icon="power"
        Text item=Battery_Voltage icon="voltage"
        Text item=Battery_Temperature icon="temperature"
        Text item=Daily_Battery_Charge icon="renewable"
        Text item=Daily_Battery_Discharge icon="battery"
        Text item=Total_Battery_Charge icon="renewable"
        Text item=Total_Battery_Discharge icon="battery"
    }
    
    Frame label="Solar"{
        Text item=Total_Production icon="solar"
        Text item=Daily_Production icon="solar"
        Text item=PV1_Current icon="solar"
        Text item=PV1_Power icon="solar"
        Text item=PV1_Voltage icon="solar"
        Text item=PV2_Current icon="solar"
        Text item=PV2_Power icon="solar"
        Text item=PV2_Voltage icon="solar"
    }
    
    Frame label="Grid"{
        Text item=Total_Grid_Production icon="power"
        Text item=Total_Grid_Power icon="power"
        Text item=External_CT_L1_Power icon="power"
        Text item=External_CT_L2_Power icon="power"
        Text item=External_CT_L3_Power icon="power"
        Text item=Internal_CT_L1_Power icon="power"
        Text item=Internal_CT_L2_Power icon="power"
        Text item=Internal_CT_L3_Power icon="power"
        Text item=Grid_Voltage_L1 icon="power"
        Text item=Grid_Voltage_L2 icon="power"
        Text item=Grid_Voltage_L3 icon="power"
        Text item=Daily_Energy_Sold icon="power"
        Text item=Total_Energy_Sold icon="power"
        Text item=Daily_Energy_Bought icon="power"
        Text item=Total_Energy_Bought icon="power"
    }
    
    Frame label="Load"{
        Text item=Daily_Load_Consumption icon="power"
        Text item=Total_Load_Consumption icon="power"
        Text item=Load_L1_Power icon="power"
        Text item=Load_L2_Power icon="power"
        Text item=Load_L3_Power icon="power"
        Text item=Load_Voltage_L1 icon="power"
        Text item=Load_Voltage_L2 icon="power"
        Text item=Load_Voltage_L3 icon="power"
        Text item=Total_Load_Power icon="power"
    }

        Frame label="Alert"{
            Text item=Alert icon="alert"
    }
}
```

## Acknowledgments

The code's creation draws significant inspiration
from [Stephan Joubert's Home Assistant plugin](https://github.com/StephanJoubert/home_assistant_solarman), which
provides the inverter definitions used in the project.
Additionally, the [pysolarmanv5 module](https://pysolarmanv5.readthedocs.io/en/latest/index.html) was a valuable
resource,
as it offers an excellent explanation of the Solarman V5 protocol.
