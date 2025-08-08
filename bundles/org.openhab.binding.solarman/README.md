# Solarman Logger Binding

Binding used to communicate with Solarman (IGEN-Tech) v5 based solar inverter data loggers in direct-mode over the local network.
More information about the different types of stick loggers is available on the [Solarman](https://www.solarmanpv.com/products/data-logger/stick-logger/) site.

These data loggers are used by inverters from a lot of manufacturers, just to name a few:

- Deye
- Sofar
- Solis
- ZCS Azzurro
- KStar

## Supported Things

The `solarman:logger` thing supports reading data from a Solarman LSW-3 Stick Logger (it might also work with LSE-3 and maybe others) when connected to a supported inverter.

It was tested on a SUN-12K-SG04LP3-EU only, with LAN Stick LSE-3 in RAW MODBUS solarmanLoggerMode and Wifi Stick in V5 MODBUS solarmanLoggerMode but because the implementation uses the inverter definitions created as part of Stephan Joubert's Home Assistant plugin it **might** work with the other inverters supported by the plugin.

## Thing Configuration

To connect the logger you need the IP address of the logger and its serial number.
The IP address can be obtained from your router and the serial number can either be read from the label of the logger, or by connecting to the logger with a browser (default user/pass: admin/admin) and getting it from the Status page.
**Please note** that you need the "Device serial number" from the "Device information" section, not the "Inverter serial number".

### `logger` Thing Configuration

| Name               | Type    | Description                                                                                                       | Default   | Required | Advanced |
|--------------------|---------|-------------------------------------------------------------------------------------------------------------------|-----------|----------|----------|
| hostname           | text    | Hostname or IP address of the Solarman logger                                                                     | N/A       | yes      | no       |
| serialNumber       | text    | Serial number of the Solarman logger                                                                              | N/A       | yes      | no       |
| inverterType       | text    | The type of inverter connected to the logger                                                                      | N/A       | yes      | no       |
| port               | integer | Port of the Solarman logger                                                                                       | 8899      | no       | yes      |
| refreshInterval    | integer | Interval the device is polled in sec.                                                                             | 60        | no       | yes      |
| solarmanLoggerMode | option  | RAW Modbus for LAN Stick LSE-3 and V5 MODBUS for most Wifi Sticks. If your Wifi stick uses Raw Modbus choose RAW. | V5 MODBUS | no       | yes      |
| additionalRequests | text    | Additional requests besides the ones in the definition                                                            | N/A       | no       | yes      |

The `inverterType` parameter governs what registers the binding will read from the logger and what channels it will expose.

Possible values:

| Inverter Type        | Inverters supported                               | Notes                                                            |
|----------------------|---------------------------------------------------|------------------------------------------------------------------|
| deye_hybrid          | DEYE/Sunsynk/SolArk Hybrid inverters              | used when no lookup specified                                    |
| deye_sg04lp3         | DEYE/Sunsynk/SolArk Hybrid 8/12K-SG04LP3          | e.g. 12K-SG04LP3-EU                                              |
| deye_string          | DEYE/Sunsynk/SolArk String inverters              | e.g. SUN-4/5/6/7/8/10/12K-G03 Plus                               |
| deye_2mppt           | DEYE Microinverter with 2 MPPT Trackers           | e.g. SUN600G3-EU-230 / SUN800G3-EU-230 / SUN1000G3-EU-230        |
| deye_4mppt           | DEYE Microinverter with 4 MPPT Trackers           | e.g. SUN1300G3-EU-230 / SUN1600G3-EU-230 / SUN2000G3-EU-230      |
| sofar_lsw3           | SOFAR Inverters                                   |                                                                  |
| sofar_g3hyd          | SOFAR Hybrid Three-Phase inverter                 | HYD 6000 or rebranded (three-phase), ex. ZCS Azzurro 3PH HYD-ZSS |
| sofar_hyd3k-6k-es    | SOFAR Hybrid Single-Phase inverter                | HYD 6000 or rebranded (single-phase), ex. ZCS Azzurro HYD-ZSS    |
| solis_hybrid         | SOLIS Hybrid inverter                             |                                                                  |
| solid_1p8k-5g        | SOLIS 1P8K-5G                                     |                                                                  |
| solis_3p-4g          | SOLIS Three-Phase Inverter 4G Series              |                                                                  |
| solis_s6-gr1p        | SOLIS Single-Phase Inverter S6-GR1P               |                                                                  |
| hyd-zss-hp-3k-6k     | ZCS Azzurro Hybrid HP 3K-6K inverters             | Rebranded Sofar models                                           |
| kstar_hybrid         | KSTAR Hybrid inverters                            |                                                                  |
| sofar_wifikit        | SOFAR WiFi Kit                                    |                                                                  |
| zcs_azzurro-ktl-v3   | ZCS Azzurro KTL-V3 inverters                      | ZCS Azzurro 3.3/4.4/5.5/6.6 KTL-V3 (rebranded Sofar KTLX-G3)     |

The `additionalRequests` allows the user to specify additional address ranges to be polled. The format of the value is `mb_functioncode1:start1-end1, mb_functioncode2:start2-end2,...`
For example `"0x03:0x27D-0x27E"` will issue an additional read for Holding Registers between `0x27D` and `0x27E`.

This is useful when coupled with user defined channels, for example a thing definition like the one below will also read the register for the AC frequency on a Deye inverter, besides the ones pre-defined in the `deye_sg04lp3` inverter definition.

```java
Thing solarman:logger:local [ hostname="x.x.x.x", inverterType="deye_sg04lp3", serialNumber="1234567890", additionalRequests="0x03:0x27D-0x27E" ] {
        Channels:
            Type number : inverter-frequency [scale="0.01", uom="Hz", rule="3", registers="0x27E"]
}
```

**Please note** As of this writing inverter types besides the `deye_sg04lp3` were not tested to work.
If you have one of those inverters and it works, please drop me a message, if it doesn't work, please open an issue and I'll try to fix it.

## Channels

The list of channels is not static, it is generated dynamically based on the inverter type selected.

This is the list you get for the `deye_sg04lp3` inverter type:

| Channel                                  | Type   | Read/Write   | Description                                           |
|------------------------------------------|--------|--------------|-------------------------------------------------------|
| alert-alert                              | Number | R            | Alert \[0x0229,0x022A,0x022B,0x022C,0x022D,0x022E\]   |
| battery-battery-current                  | Number | R            | Battery Current \[0x024F\]                            |
| battery-battery-power                    | Number | R            | Battery Power \[0x024E\]                              |
| battery-battery-soc                      | Number | R            | Battery SOC \[0x024C\]                                |
| battery-battery-temperature              | Number | R            | Battery Temperature \[0x024A\]                        |
| battery-battery-voltage                  | Number | R            | Battery Voltage \[0x024B\]                            |
| battery-daily-battery-charge             | Number | R            | Daily Battery Charge \[0x0202\]                       |
| battery-daily-battery-discharge          | Number | R            | Daily Battery Discharge \[0x0203\]                    |
| battery-total-battery-charge             | Number | R            | Total Battery Charge \[0x0204,0x0205\]                |
| battery-total-battery-discharge          | Number | R            | Total Battery Discharge \[0x0206,0x0207\]             |
| battery-battery-absorption-v             | Number | R            | Battery Absorption V \[0x0064\]                       |
| battery-battery-empty-v                  | Number | R            | Battery Empty V \[0x0066\]                            |
| battery-battery-equalization-v           | Number | R            | Battery Equalization V \[0x0063\]                     |
| battery-battery-float-v                  | Number | R            | Battery Float V \[0x0065\]                            |
| battery-battery-capacity                 | Number | R            | Battery Capacity \[0x0066\]                           |
| battery-battery-max-a-charge             | Number | R            | Battery Max A Charge \[0x006C\]                       |
| battery-battery-max-a-discharge          | Number | R            | Battery Max A Discharge \[0x006D\]                    |
| grid-daily-energy-bought                 | Number | R            | Daily Energy Bought \[0x0208\]                        |
| grid-daily-energy-sold                   | Number | R            | Daily Energy Sold \[0x0209\]                          |
| grid-external-ct-l1-power                | Number | R            | External CT L1 Power \[0x0268\]                       |
| grid-external-ct-l2-power                | Number | R            | External CT L2 Power \[0x0269\]                       |
| grid-external-ct-l3-power                | Number | R            | External CT L3 Power \[0x026A\]                       |
| grid-grid-voltage-l1                     | Number | R            | Grid Voltage L1 \[0x0256\]                            |
| grid-grid-voltage-l2                     | Number | R            | Grid Voltage L2 \[0x0257\]                            |
| grid-grid-voltage-l3                     | Number | R            | Grid Voltage L3 \[0x0258\]                            |
| grid-internal-ct-l1-power                | Number | R            | Internal CT L1 Power \[0x025C\]                       |
| grid-internal-ct-l2-power                | Number | R            | Internal CT L2 Power \[0x025D\]                       |
| grid-internal-ct-l3-power                | Number | R            | Internal CT L3 Power \[0x025E\]                       |
| grid-total-energy-bought                 | Number | R            | Total Energy Bought \[0x020A,0x020B\]                 |
| grid-total-energy-sold                   | Number | R            | Total Energy Sold \[0x020C,0x020D\]                   |
| grid-total-grid-power                    | Number | R            | Total Grid Power \[0x0271\]                           |
| grid-total-grid-production               | Number | R            | Total Grid Production \[0x020C,0x020D\]               |
| inverter-ac-temperature                  | Number | R            | AC Temperature \[0x021D\]                             |
| inverter-communication-board-version-no- | Number | R            | Communication Board Version No \[0x0011\]             |
| inverter-control-board-version-no-       | Number | R            | Control Board Version No \[0x000D\]                   |
| inverter-current-l1                      | Number | R            | Current L1 \[0x0276\]                                 |
| inverter-current-l2                      | Number | R            | Current L2 \[0x0277\]                                 |
| inverter-current-l3                      | Number | R            | Current L3 \[0x0278\]                                 |
| inverter-dc-temperature                  | Number | R            | DC Temperature \[0x021C\]                             |
| inverter-frequency                       | Number | R            | Inverter Frequency \[0x27E\]                          |
| inverter-inverter-id                     | String | R            | Inverter ID \[0x0003,0x0004,0x0005,0x0006,0x0007\]    |
| inverter-inverter-l1-power               | Number | R            | Inverter L1 Power \[0x0279\]                          |
| inverter-inverter-l2-power               | Number | R            | Inverter L2 Power \[0x027A\]                          |
| inverter-inverter-l3-power               | Number | R            | Inverter L3 Power \[0x027B\]                          |
| solar-daily-production                   | Number | R            | Daily Production \[0x0211\]                           |
| solar-pv1-current                        | Number | R            | PV1 Current \[0x02A5\]                                |
| solar-pv1-power                          | Number | R            | PV1 Power \[0x02A0\]                                  |
| solar-pv1-voltage                        | Number | R            | PV1 Voltage \[0x02A4\]                                |
| solar-pv2-current                        | Number | R            | PV2 Current \[0x02A7\]                                |
| solar-pv2-power                          | Number | R            | PV2 Power \[0x02A1\]                                  |
| solar-pv2-voltage                        | Number | R            | PV2 Voltage \[0x02A6\]                                |
| solar-total-production                   | Number | R            | Total Production \[0x0216,0x0217\]                    |
| upload-daily-load-consumption            | Number | R            | Daily Load Consumption \[0x020E\]                     |
| upload-load-l1-power                     | Number | R            | Load L1 Power \[0x028A\]                              |
| upload-load-l2-power                     | Number | R            | Load L2 Power \[0x028B\]                              |
| upload-load-l3-power                     | Number | R            | Load L3 Power \[0x028C\]                              |
| upload-load-voltage-l1                   | Number | R            | Load Voltage L1 \[0x0284\]                            |
| upload-load-voltage-l2                   | Number | R            | Load Voltage L2 \[0x0285\]                            |
| upload-load-voltage-l3                   | Number | R            | Load Voltage L3 \[0x0286\]                            |
| upload-total-load-consumption            | Number | R            | Total Load Consumption \[0x020F,0x0210\]              |
| upload-total-load-power                  | Number | R            | Total Load Power \[0x028D\]                           |

## Full Example

This is an example for a DEYE 12kW (SUN-12K-SG04LP3-EU) hybrid inverter

### `solarman.things`

Please replace the `hostname` and `serialNumber` with the correct values for your logger.

```java
Thing solarman:logger:local [hostname="x.x.x.x",inverterType="deye_sg04lp3",serialNumber="1234567890"]
```

### `solarman.items`

Items file example for a SUN-12K-SG04LP3-EU inverter

```java
Number:Temperature        AC_Temperature                  "AC Temperature [%.1f °C]"             (solarman)  {channel="solarman:logger:local:inverter-ac-temperature", unit="°C"}
Number                    Alert                           "Alert [%s]"                           (solarman)  {channel="solarman:logger:local:alert-alert"}
Number:ElectricPotential  Battery_Absorption_V            "Battery Absorption V [%.2f V]"        (solarman)  {channel="solarman:logger:local:battery-battery-absorption-v", unit="V"}
Number:ElectricCharge     Battery_Capacity                "Battery Capacity [%d Ah]"             (solarman)  {channel="solarman:logger:local:battery-battery-capacity", unit="Ah"}
Number:ElectricCurrent    Battery_Current                 "Battery Current [%.1f A]"             (solarman)  {channel="solarman:logger:local:battery-battery-current", unit="A"}
Number:Energy             Daily_Battery_Charge            "Daily Battery Charge [%.1f kWh]"      (solarman)  {channel="solarman:logger:local:battery-daily-battery-charge", unit="kWh"}
Number:Energy             Daily_Battery_Discharge         "Daily Battery Discharge [%.1f kWh]"   (solarman)  {channel="solarman:logger:local:battery-daily-battery-discharge", unit="kWh"}
Number:ElectricPotential  Battery_Empty_V                 "Battery Empty V [%.2f V]"             (solarman)  {channel="solarman:logger:local:battery-battery-empty-v", unit="V"}
Number:ElectricPotential  Battery_Equalization_V          "Battery Equalization V [%.2f V]"      (solarman)  {channel="solarman:logger:local:battery-battery-equalization-v", unit="V"}
Number:ElectricPotential  Battery_Float_V                 "Battery Float V [%.2f V]"             (solarman)  {channel="solarman:logger:local:battery-battery-float-v", unit="V"}
Number:ElectricCurrent    Battery_Max_A_Charge            "Battery Max A Charge [%d A]"          (solarman)  {channel="solarman:logger:local:battery-battery-max-a-charge", unit="A"}
Number:ElectricCurrent    Battery_Max_A_Discharge         "Battery Max A Discharge [%d A]"       (solarman)  {channel="solarman:logger:local:battery-battery-max-a-discharge", unit="A"}
Number:Dimensionless      Battery_SOC                     "Battery SOC [%d %%]"                  (solarman)  {channel="solarman:logger:local:battery-battery-soc", unit="%"}
Number:Power              Battery_Power                   "Battery Power [%d W]"                 (solarman)  {channel="solarman:logger:local:battery-battery-power", unit="W"}
Number:Temperature        Battery_Temperature             "Battery Temperature [%.1f °C]"        (solarman)  {channel="solarman:logger:local:battery-battery-temperature", unit="°C"}
Number:ElectricPotential  Battery_Voltage                 "Battery Voltage [%.2f V]"             (solarman)  {channel="solarman:logger:local:battery-battery-voltage", unit="V"}
Number:Dimensionless      Communication_Board_Version_No  "Communication Board Version No [%s]"  (solarman)  {channel="solarman:logger:local:inverter-communication-board-version-no-"}
Number:Dimensionless      Control_Board_Version_No        "Control Board Version No [%s]"        (solarman)  {channel="solarman:logger:local:inverter-control-board-version-no-"}
Number:ElectricCurrent    Current_L1                      "Current L1 [%.1f A]"                  (solarman)  {channel="solarman:logger:local:inverter-current-l1", unit="A"}
Number:ElectricCurrent    Current_L2                      "Current L2 [%.1f A]"                  (solarman)  {channel="solarman:logger:local:inverter-current-l2", unit="A"}
Number:ElectricCurrent    Current_L3                      "Current L3 [%.1f A]"                  (solarman)  {channel="solarman:logger:local:inverter-current-l3", unit="A"}
Number:Energy             Daily_Energy_Bought             "Daily Energy Bought [%d kWh]"         (solarman)  {channel="solarman:logger:local:grid-daily-energy-bought", unit="kWh"}
Number:Energy             Daily_Energy_Sold               "Daily Energy Sold [%d Wh]"            (solarman)  {channel="solarman:logger:local:grid-daily-energy-sold", unit="Wh"}
Number:Energy             Daily_Load_Consumption          "Daily Load Consumption [%.1f kWh]"    (solarman)  {channel="solarman:logger:local:upload-daily-load-consumption", unit="kWh"}
Number:Energy             Daily_Production                "Daily Production [%.1f kWh]"          (solarman)  {channel="solarman:logger:local:solar-daily-production", unit="kWh"}
Number:Temperature        DC_Temperature                  "DC Temperature [%.1f °C]"             (solarman)  {channel="solarman:logger:local:inverter-dc-temperature", unit="°C"}
Number:Power              External_CT_L1_Power            "External CT L1 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-external-ct-l1-power", unit="W"}
Number:Power              External_CT_L2_Power            "External CT L2 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-external-ct-l2-power", unit="W"}
Number:Power              External_CT_L3_Power            "External CT L3 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-external-ct-l3-power", unit="W"}
Number:Power              Gen_Port_A_Phase_Power          "Phase Power of Gen Port A [%d W]"     (solarman)  {channel="solarman:logger:local:smartload-phase-power-of-gen-port-a", unit="W"}
Number:Power              Gen_Port_B_Phase_Power          "Phase Power of Gen Port B [%d W]"     (solarman)  {channel="solarman:logger:local:smartload-phase-power-of-gen-port-b", unit="W"}
Number:Power              Gen_Port_C_Phase_Power          "Phase Power of Gen Port C [%d W]"     (solarman)  {channel="solarman:logger:local:smartload-phase-power-of-gen-port-c", unit="W"}
Number:ElectricPotential  Gen_Port_A_Phase_Voltage        "Phase Voltage of Gen Port A [%d V]"   (solarman)  {channel="solarman:logger:local:smartload-phase-voltage-of-gen-port-a", unit="V"}
Number:ElectricPotential  Gen_Port_B_Phase_Voltage        "Phase Voltage of Gen Port B [%d V]"   (solarman)  {channel="solarman:logger:local:smartload-phase-voltage-of-gen-port-b", unit="V"}
Number:ElectricPotential  Gen_Port_C_Phase_Voltage        "Phase Voltage of Gen Port C [%d V]"   (solarman)  {channel="solarman:logger:local:smartload-phase-voltage-of-gen-port-c", unit="V"}
String                    Inverter_Id                     "Inverter Id [%s]"                     (solarman)  {channel="solarman:logger:local:inverter-inverter-id"}
Number:Power              Inverter_L1_Power               "Inverter L1 Power [%d W]"             (solarman)  {channel="solarman:logger:local:inverter-inverter-l1-power", unit="W"}
Number:Power              Inverter_L2_Power               "Inverter L2 Power [%d W]"             (solarman)  {channel="solarman:logger:local:inverter-inverter-l2-power", unit="W"}
Number:Power              Inverter_L3_Power               "Inverter L3 Power [%d W]"             (solarman)  {channel="solarman:logger:local:inverter-inverter-l3-power", unit="W"}
Number:Power              Internal_CT_L1_Power            "Internal CT L1 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-internal-ct-l1-power", unit="W"}
Number:Power              Internal_CT_L2_Power            "Internal CT L2 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-internal-ct-l2-power", unit="W"}
Number:Power              Internal_CT_L3_Power            "Internal CT L3 Power [%d W]"          (solarman)  {channel="solarman:logger:local:grid-internal-ct-l3-power", unit="W"}
Number:ElectricPotential  Load_Voltage_L1                 "Load Voltage L1 [%d V]"               (solarman)  {channel="solarman:logger:local:upload-load-voltage-l1", unit="V"}
Number:ElectricPotential  Load_Voltage_L2                 "Load Voltage L2 [%d V]"               (solarman)  {channel="solarman:logger:local:upload-load-voltage-l2", unit="V"}
Number:ElectricPotential  Load_Voltage_L3                 "Load Voltage L3 [%d V]"               (solarman)  {channel="solarman:logger:local:upload-load-voltage-l3", unit="V"}
Number:Power              Load_L1_Power                   "Load L1 Power [%d W]"                 (solarman)  {channel="solarman:logger:local:upload-load-l1-power", unit="W"}
Number:Power              Load_L2_Power                   "Load L2 Power [%d W]"                 (solarman)  {channel="solarman:logger:local:upload-load-l2-power", unit="W"}
Number:Power              Load_L3_Power                   "Load L3 Power [%d W]"                 (solarman)  {channel="solarman:logger:local:upload-load-l3-power", unit="W"}
Number:ElectricPotential  Grid_Voltage_L1                 "Grid Voltage L1 [%d V]"               (solarman)  {channel="solarman:logger:local:grid-grid-voltage-l1", unit="V"}
Number:ElectricPotential  Grid_Voltage_L2                 "Grid Voltage L2 [%d V]"               (solarman)  {channel="solarman:logger:local:grid-grid-voltage-l2", unit="V"}
Number:ElectricPotential  Grid_Voltage_L3                 "Grid Voltage L3 [%d V]"               (solarman)  {channel="solarman:logger:local:grid-grid-voltage-l3", unit="V"}
Number:Energy             Generator_Daily_Power_Generation "Generator Daily Power Generation [%.1f kWh]" (solarman) {channel="solarman:logger:local:smartload-generator-daily-power-generation", unit="kWh"}
Number:Energy             Generator_Total_Power_Generation "Generator Total Power Generation [%.1f kWh]" (solarman) {channel="solarman:logger:local:smartload-generator-total-power-generation", unit="kWh"}
Number:Dimensionless      Smartload_Enable_Status         "Smartload Enable Status [%d]"         (solarman)  {channel="solarman:logger:local:smartload-smartload-enable-status"}
Number:Energy             Total_Battery_Charge            "Total Battery Charge [%d kWh]"        (solarman)  {channel="solarman:logger:local:battery-total-battery-charge", unit="kWh"}
Number:Energy             Total_Battery_Discharge         "Total Battery Discharge [%d kWh]"     (solarman)  {channel="solarman:logger:local:battery-total-battery-discharge", unit="kWh"}
Number:Energy             Total_Energy_Bought             "Total Energy Bought [%d kWh]"         (solarman)  {channel="solarman:logger:local:grid-total-energy-bought", unit="kWh"}
Number:Energy             Total_Energy_Sold               "Total Energy Sold [%d kWh]"           (solarman)  {channel="solarman:logger:local:grid-total-energy-sold", unit="kWh"}
Number:Power              Total_Gen_Port_Power            "Total Power of Gen Port [%d W]"       (solarman)  {channel="solarman:logger:local:smartload-total-power-of-gen-port", unit="W"}
Number:Power              Total_Grid_Power                "Total Instant Grid Power [%d W]"      (solarman)  {channel="solarman:logger:local:grid-total-grid-power", unit="W"}
Number:Energy             Total_Grid_Production           "Total Grid Feed-in [%.1f kWh]"        (solarman)  {channel="solarman:logger:local:grid-total-grid-production", unit="kWh"}
Number:Energy             Total_Load_Consumption          "Total Load Consumption [%d kWh]"      (solarman)  {channel="solarman:logger:local:upload-total-load-consumption", unit="kWh"}
Number:Power              Total_Load_Power                "Total Load Power [%d W]"              (solarman)  {channel="solarman:logger:local:upload-total-load-power", unit="W"}
Number:Energy             Total_Solar_Production          "Total Solar Production [%.1f kWh]"    (solarman)  {channel="solarman:logger:local:solar-total-production", unit="kWh"}
Number:Power              PV1_Power                       "PV1 Power [%d W]"                     (solarman)  {channel="solarman:logger:local:solar-pv1-power", unit="W"}
Number:ElectricCurrent    PV1_Current                     "PV1 Current [%.1f A]"                 (solarman)  {channel="solarman:logger:local:solar-pv1-current", unit="A"}
Number:ElectricPotential  PV1_Voltage                     "PV1 Voltage [%d V]"                   (solarman)  {channel="solarman:logger:local:solar-pv1-voltage", unit="V"}
Number:Power              PV2_Power                       "PV2 Power [%d W]"                     (solarman)  {channel="solarman:logger:local:solar-pv2-power", unit="W"}
Number:ElectricCurrent    PV2_Current                     "PV2 Current [%.1f A]"                 (solarman)  {channel="solarman:logger:local:solar-pv2-current", unit="A"}
Number:ElectricPotential  PV2_Voltage                     "PV2 Voltage [%d V]"                   (solarman)  {channel="solarman:logger:local:solar-pv2-voltage", unit="V"}

Number:Frequency          Inverter_Frequency              "Inverter Frequency [%.2f Hz]"         (solarman)  {channel="solarman:logger:local:inverter-frequency", unit="Hz"}
```

### `solarman.sitemap`

Sitemap example for a SUN-12K-SG04LP3-EU inverter

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
        Text item=Inverter_Frequency icon="line"
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
        Text item=Battery_Absorption_V icon="voltage"
        Text item=Battery_Equalization_V icon="voltage"
        Text item=Battery_Float_V icon="voltage"
        Text item=Battery_Empty_V icon="voltage"
        Text item=Battery_Capacity icon="battery"
        Text item=Battery_Max_A_Charge icon="battery"
        Text item=Battery_Max_A_Discharge icon="battery"
    }

    Frame label="Solar"{
        Text item=Total_Solar_Production icon="solar"
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

    Frame label="Generator"{
        Text item=Gen_Port_A_Phase_Power icon="poweroutlet"
        Text item=Gen_Port_B_Phase_Power icon="poweroutlet"
        Text item=Gen_Port_C_Phase_Power icon="poweroutlet"
        Text item=Gen_Port_A_Phase_Voltage icon="voltage"
        Text item=Gen_Port_B_Phase_Voltage icon="voltage"
        Text item=Gen_Port_C_Phase_Voltage icon="voltage"
        Text item=Total_Gen_Port_Power icon="power"
        Text item=Generator_Daily_Power_Generation icon="power"
        Text item=Generator_Total_Power_Generation icon="power"
    }

    Frame label="Alert"{
        Text item=Alert icon="alert"
    }
}
```

## Acknowledgments

The code's creation draws significant inspiration from [Stephan Joubert's Home Assistant plugin](https://github.com/StephanJoubert/home_assistant_solarman), which provides the inverter definitions used in the project.
Additionally, the [pysolarmanv5 module](https://pysolarmanv5.readthedocs.io/en/latest/index.html) was a valuable resource, as it offers an excellent explanation of the Solarman V5 protocol.
