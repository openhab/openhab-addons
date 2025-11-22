# Modbus FoxInverter Binding

This binding integrates the FoxESS solar inverters into openHAB.

## Supported Inverters

As FoxESS inverters are sold in slightly different configurations, inverters of several brands might be supported.

This one is tested:

- Solakon ONE

Others are untested, but might work:

- FoxESS Avocado 22 Pro

## Supported Things

The binding supports only one thing:

- `mq2200-inverter`: The FoxESS MQ-2200 inverter (also sold with the product name Solakon ONE or Avocado 22 Pro), connected via Modbus TCP.

## Preparation

The data from the inverter is read via Modbus. So you need to configure a Modbus TCP Slave `tcp` as bridge first.
The inverter can be connected via LAN or WLAN.
As of now, you need to get the IP address of the inverter from your Router.
IP is obtained via DHCP.
I have not found a way to set a static IP on the Solakon ONE.

To troubleshoot, it is recommended to open a telnet connection to port 502. If it connects, you have likely found your device with an active Modbus server.

## Thing Configuration

Once you've configured the Modbus TCP Slave as Bridge, you can configure the mq2200-inverter thing.
You just have to select the configured bridge and optionally configure the polling interval.

The foxinverter binding supports autodiscovery, so if you enable discovery in the bridge options, you can just press the `scan` button to add your inverter to the inbox.

### FoxESS MQ-2200 Inverter (`mq2200-inverter`)

| Name          | Type    | Description                                                                                                                                          | Default | Required | Advanced |
|---------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------|---------|----------|----------|
| pollInterval  | integer | Interval the device is polled in ms.                                                                                                                 | 5000    | yes      | no       |
| maxTries      | integer | Specifies how many times the binding should retry reading data if a read attempt fails. <br/>Set to `1` to disable retries and use a single attempt. | 3       | yes      | no       |

## Channels

> NOTE: All channels are currently read-only. Writing is not yet implemented.

The `mq2200-inverter` thing has channels that serve the current state of the FoxESS inverter, as you are used to from the vendor app.

| Channel Type ID                    | Item Type                | Description                           | Advanced  | Channel Group       |
|------------------------------------|--------------------------|---------------------------------------|-----------|---------------------|
| fi-inverter-power                  | Number:Power             | Inverter Active Power                 | no        | Overview            |
| fi-home-import-power               | Number:Power             | Power flow with home network          | no        | Overview            |
| fi-daily-pv-generation             | Number:Energy            | Daily PV Generation                   | no        | Overview            |
| fi-total-pv-generation             | Number:Energy            | Total PV Generation                   | no        | Overview            |
| fi-internal-temperature            | Number:Temperature       | Internal Temperature                  | yes       | Overview            |
| fi-meter-connected                 | Contact                  | Smart Meter is Connected              | yes       | Overview            |
| fi-status-alarm                    | Contact                  | System Warning                        | no        | Overview            |
| fi-status-operation                | Contact                  | System in Operational Mode            | no        | Overview            |
| fi-status-standby                  | Contact                  | System in Standby Mode (not yet off)  | yes       | Overview            |
| fi-pv-power                        | Number:Power             | PV Power                              | no        | MPPT Information    |
| fi-mppt1-voltage                   | Number:ElectricPotential | MPPT1 Voltage                         | yes       | MPPT Information    |
| fi-mppt1-current                   | Number:ElectricCurrent   | MPPT1 Current                         | yes       | MPPT Information    |
| fi-mppt1-power                     | Number:Power             | MPPT1 Power                           | no        | MPPT Information    |
| fi-mppt2-voltage                   | Number:ElectricPotential | MPPT2 Voltage                         | yes       | MPPT Information    |
| fi-mppt2-current                   | Number:ElectricCurrent   | MPPT2 Current                         | yes       | MPPT Information    |
| fi-mppt2-power                     | Number:Power             | MPPT2 Power                           | no        | MPPT Information    |
| fi-mppt3-voltage                   | Number:ElectricPotential | MPPT3 Voltage                         | yes       | MPPT Information    |
| fi-mppt3-current                   | Number:ElectricCurrent   | MPPT3 Current                         | yes       | MPPT Information    |
| fi-mppt3-power                     | Number:Power             | MPPT3 Power                           | no        | MPPT Information    |
| fi-mppt4-voltage                   | Number:ElectricPotential | MPPT4 Voltage                         | yes       | MPPT Information    |
| fi-mppt4-current                   | Number:ElectricCurrent   | MPPT4 Current                         | yes       | MPPT Information    |
| fi-mppt4-power                     | Number:Power             | MPPT4 Power                           | no        | MPPT Information    |
| fi-battery-voltage                 | Number:ElectricPotential | Battery Voltage                       | yes       | Battery Information |
| fi-battery-current                 | Number:ElectricCurrent   | Battery Current                       | yes       | Battery Information |
| fi-battery-level                   | Number:Dimensionless     | Battery Level                         | no        | Battery Information |
| fi-battery-charging-power          | Number:Power             | Battery Charging Power                | no        | Battery Information |
| fi-battery-temperature             | Number:Temperature       | Internal Temperature                  | no        | Battery Information |
| fi-battery-minimum-soc             | Number:Dimensionless     | Battery Min Charging Level            | yes       | Battery Information |
| fi-battery-maximum-soc             | Number:Dimensionless     | Battery Max Charging Level            | yes       | Battery Information |
| fi-battery-minimum-soc-on-grid     | Number:Dimensionless     | Battery Min Charging Level on Grid    | yes       | Battery Information |
| fi-phase-a-voltage                 | Number:ElectricPotential | Phase A Voltage                       | yes       | Grid Information    |
| fi-phase-b-voltage                 | Number:ElectricPotential | Phase B Voltage                       | yes       | Grid Information    |
| fi-phase-c-voltage                 | Number:ElectricPotential | Phase C Voltage                       | yes       | Grid Information    |
| fi-grid-frequency                  | Number:Frequency         | Grid Frequency                        | yes       | Grid Information    |
| fi-grid-export-power               | Number:Power             | Overall Power Export to Grid          | no        | Grid Information    |
| fi-status-on-grid                  | Contact                  | Power Grid Available                  | yes       | Grid Information    |
| fi-eps-output                      | Switch                   | Enable EPS Output                     | no        | EPS                 |
| fi-eps-export-power                | Number:Power             | EPS Output Power                      | no        | EPS                 |

## Full Example

This example shows how to configure a FoxESS inverter connected via Modbus and uses the most common channels.

### mq2200.things

```java
Bridge modbus:tcp:powerplant [ host="10.0.0.2", port=502, id=1, enableDiscovery=false ] {
    Thing mq2200-inverter inverter "FoxESS Inverter" [ pollInterval=5000 ]
}
```

### mq2200.items

```java
// Groups
Group inverter "FoxESS Inverter" <solarplant> ["Inverter"]
Group overview "Overview" <line> (inverter)
Group mppt "MPPT Information" <solarplant> (inverter)
Group batteryInformation "Battery information" <battery> (inverter)
Group gridInformation "Grid information" <energy> (inverter)
Group eps "Emergency Power Supply" <energy> (inverter)

// Overview
Number:Power active_power "Inverter Power" <line> (overview) ["Measurement", "Power"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-inverter-power"}
Number:Power battery_power "Home Import Power" <line> (batteryInformation,overview) ["Measurement", "Power"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-home-import-power"}
Number:Energy daily_pv_generation "Daily PV Generation" <line> (overview) ["Measurement", "Energy"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-daily-pv-generation"}
Number:Energy total_pv_generation "Total PV Generation" <line> (overview) ["Measurement", "Energy"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-total-pv-generation"}
Number:Temperature internal_temperature "Internal Temperature" <temperature> (overview) ["Measurement", "Temperature"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-internal-temperature"}
Contact meter_connected "Meter is Connected" (overview) ["Measurement"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-meter-connected"}
Contact status_alarm "Status Alarm" (overview) ["Measurement"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-status-alarm"}
Contact status_operation "Status Operation" (overview) ["Measurement"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-status-operation"}
Contact status_standby "Status Standby" (overview) ["Measurement"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-overview#fi-status-standby"}

// MPPT information
Number:Power pv_power "PV Power" <line> (mppt,overview) ["Measurement", "Power"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-pv-power"}
Number:ElectricPotential mppt1_voltage "MPPT1 Voltage" <energy> (mppt) ["Measurement", "Voltage"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt1-voltage"}
Number:ElectricCurrent mppt1_current "MPPT1 Current" <energy> (mppt) ["Measurement", "Current"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt1-current"}
Number:Power mppt1_power "MPPT1 Power" <line> (mppt) ["Measurement", "Power"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt1-power"}
Number:ElectricPotential mppt2_voltage "MPPT2 Voltage" <energy> (mppt) ["Measurement", "Voltage"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt2-voltage"}
Number:ElectricCurrent mppt2_current "MPPT2 Current" <energy> (mppt) ["Measurement", "Current"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt2-current"}
Number:Power mppt2_power "MPPT2 Power" <line> (mppt) ["Measurement", "Power"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt2-power"}
Number:ElectricPotential mppt3_voltage "MPPT3 Voltage" <energy> (mppt) ["Measurement", "Voltage"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt3-voltage"}
Number:ElectricCurrent mppt3_current "MPPT3 Current" <energy> (mppt) ["Measurement", "Current"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt3-current"}
Number:Power mppt3_power "MPPT3 Power" <line> (mppt) ["Measurement", "Power"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt3-power"}
Number:ElectricPotential mppt4_voltage "MPPT4 Voltage" <energy> (mppt) ["Measurement", "Voltage"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt4-voltage"}
Number:ElectricCurrent mppt4_current "MPPT4 Current" <energy> (mppt) ["Measurement", "Current"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt4-current"}
Number:Power mppt4_power "MPPT4 Power" <line> (mppt) ["Measurement", "Power"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-mppt-information#fi-mppt4-power"}

// Battery information
Number:Dimensionless battery_level "Battery Level [%.0f %%]" <battery> (batteryInformation,overview) ["Measurement", "Energy"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-level", unit="%"}
Number:Power battery_charging_power "Battery Charging Power" <line> (batteryInformation,overview) ["Measurement", "Power"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-charging-power"}
Number:ElectricPotential battery_voltage "Battery Voltage" <energy> (batteryInformation) ["Measurement", "Voltage"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-voltage"}
Number:ElectricCurrent battery_current "Battery Current" <energy> (batteryInformation) ["Measurement", "Current"]
{channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-current"}
Number:Temperature battery_temperature "Battery Temperature" <temperature> (overview) ["Measurement", "Temperature"]{channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-temperature"}
Number:Dimensionless battery_minimum_soc "Battery Min Charging Level [%.0f %%]" <battery> (batteryInformation) ["Control"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-minimum-soc", unit="%"}
Number:Dimensionless battery_maximum_soc "Battery Max Charging Level [%.0f %%]" <battery> (batteryInformation) ["Control"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-maximum-soc", unit="%"}
Number:Dimensionless battery_minimum_soc_on_grid "Battery Min Charging Level on Grid" <battery> (batteryInformation) ["Control"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-battery-information#fi-battery-minimum-soc-on-grid", unit="%"}

// Grid information
Number:ElectricPotential phase_a_voltage "Phase A Voltage" <energy> (gridInformation) ["Measurement"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-grid-information#fi-phase-a-voltage"}
Number:ElectricPotential phase_b_voltage "Phase B Voltage" <energy> (gridInformation) ["Measurement"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-grid-information#fi-phase-b-voltage"}
Number:ElectricPotential phase_c_voltage "Phase C Voltage" <energy> (gridInformation) ["Measurement"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-grid-information#fi-phase-c-voltage"}
Number:Frequency grid_frequency "Grid Frequency" <pump> (gridInformation) ["Measurement"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-grid-information#fi-grid-frequency"}
Number:Power grid_export_power "Export Power" <line> (gridInformation) ["Measurement", "Power"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-grid-information#fi-grid-export-power"}
Contact status_on_grid "Status Power Grid Available" (gridInformation) ["Measurement"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-grid-information#fi-status-on-grid"}

// EPS
Switch eps_output "EPS Output Enabled" (eps) ["Control"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-emergency-power-supply#fi-eps-output"}
Number:Power eps_export_power "EPS Export Power" <energy> (eps) ["Measurement", "Power"] {channel="modbus:mq2200-inverter:powerplant:inverter:fi-emergency-power-supply#fi-eps-export-power"}
```
