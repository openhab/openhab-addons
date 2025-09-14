# Modbus Sungrow Binding

This binding integrates the Sungrow inverters into openHAB.
It is based on the Sungrow specification "Communication Protocol of Residential Hybrid Inverter V1.1.15", which can be found here: https://github.com/Gnarfoz/Sungrow-Inverter/blob/main/Modbus%20Information/TI_20240924_Communication%20Protocol%20of%20Residential%20Hybrid%20Inverter-V1.1.5.pdf.

## Supported Inverters

As defined within the spec mentioned above the following inverters are supported, but not all are tested yet:

- SH3.0-6.0RS
- SH8.0-10RS
- SH5.0-10RT
- SH5-25T

Some values may not work, depending on
- the model type of your inverter
- the firmware version of your inverter
- the connection type (internal LAN vs. WiNet-S Communication Module)

## Supported Things

The binding supports only one thing:

- `sungrow-inverter`: The Sungrow inverter

## Preparation

The data from the inverter is read via Modbus. So you need to configure a Modbus Serial Slave `serial` or Modbus TCP Slave `tcp` as bridge first.
If you are using a Modbus TCP Slave and the WiNet-S Communication Module please ensure:

- that you have the correct IP address of your WiNet-S device
- that Modbus is enabled within the Communication Module
- that you have the correct port number
- that the white list is disabled or your openHAB instance IP is listed

Enabling modbus and whitelist setting can be done in WiNet-S Web-UI as shown below:
<img src="./doc/WiNet-S_Modbus.png" alt="WiNet-S Modbus configuration"/>

## Thing Configuration

Once you've configured the Modbus TCP Slave or Modbus Serial Slave as Bridge you can configure the Sungrow inverter thing.
You just have to select the configured bridge and optional configure the polling interval.

### Sungrow Inverter (`sungrow-inverter`)

| Name          | Type    | Description                                                                                                                                          | Default | Required | Advanced |
|---------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------|---------|----------|----------|
| pollInterval  | integer | Interval the device is polled in ms.                                                                                                                 | 5000    | yes      | no       |
| maxTries      | integer | Specifies how many times the binding should retry reading data if a read attempt fails. <br/>Set to `1` to disable retries and use a single attempt. | 3       | yes      | no       |

## Channels

The `sungrow-inverter` thing has channels that serve the current state of the Sungrow inverter, as you are used to from the iSolarCloud Website and App.

| Channel Type ID                          | Item Type                | Description                               | Advanced    | Channel Group       |
|------------------------------------------|--------------------------|-------------------------------------------|-------------|---------------------|
| sg-running-state                         | String                   | Running State                             | no          | Overview            |
| sg-internal-temperature                  | Number:Temperature       | Internal Temperature                      | yes         | Overview            |
| sg-total-dc-power                        | Number:Power             | Total DC Power                            | no          | Overview            |
| sg-phase-a-voltage                       | Number:ElectricPotential | Phase A Voltage                           | yes         | Overview            |
| sg-phase-b-voltage                       | Number:ElectricPotential | Phase B Voltage                           | yes         | Overview            |
| sg-phase-c-voltage                       | Number:ElectricPotential | Phase C Voltage                           | yes         | Overview            |
| sg-daily-pv-generation                   | Number:Energy            | Daily PV Generation                       | no          | Overview            |
| sg-total-pv-generation                   | Number:Energy            | Total PV Generation                       | no          | Overview            |
| sg-reactive-power                        | Number:Power             | Reactive Power                            | yes         | Overview            |
| sg-power-factor                          | Number:Dimensionless     | Power Factor                              | yes         | Overview            |
| sg-phase-a-current                       | Number:ElectricCurrent   | Phase A Current                           | yes         | Overview            |
| sg-phase-b-current                       | Number:ElectricCurrent   | Phase B Current                           | yes         | Overview            |
| sg-phase-c-current                       | Number:ElectricCurrent   | Phase C Current                           | yes         | Overview            |
| sg-total-active-power                    | Number:Power             | Total Active Power                        | no          | Overview            |
| sg-mppt1-voltage                         | Number:ElectricPotential | MPPT1 Voltage                             | yes         | MPPT Information    |
| sg-mppt1-current                         | Number:ElectricCurrent   | MPPT1 Current                             | yes         | MPPT Information    |
| sg-mppt2-voltage                         | Number:ElectricPotential | MPPT2 Voltage                             | yes         | MPPT Information    |
| sg-mppt2-current                         | Number:ElectricCurrent   | MPPT2 Current                             | yes         | MPPT Information    |
| sg-mppt3-voltage                         | Number:ElectricPotential | MPPT3 Voltage                             | yes         | MPPT Information    |
| sg-mppt3-current                         | Number:ElectricCurrent   | MPPT3 Current                             | yes         | MPPT Information    |
| sg-mppt4-voltage                         | Number:ElectricPotential | MPPT4 Voltage                             | yes         | MPPT Information    |
| sg-mppt5-current                         | Number:ElectricCurrent   | MPPT4 Current                             | yes         | MPPT Information    |
| sg-daily-battery-charge                  | Number:Energy            | Daily Battery Charge                      | no          | Battery Information |
| sg-total-battery-charge                  | Number:Energy            | Total Battery Charge                      | no          | Battery Information |
| sg-battery-voltage                       | Number:ElectricPotential | Battery Voltage                           | yes         | Battery Information |
| sg-battery-current                       | Number:ElectricCurrent   | Battery Current                           | yes         | Battery Information |
| sg-battery-power                         | Number:Power             | Battery Power                             | no          | Battery Information |
| sg-battery-power-signed                  | Number:Power             | Battery Power signed                      | no          | Battery Information |
| sg-battery-power-wide-range              | Number:Power             | Battery Power Wide Range                  | yes         | Battery Information |
| sg-battery-level                         | Number:Dimensionless     | Battery Level                             | no          | Battery Information |
| sg-battery-healthy                       | Number:Dimensionless     | Battery Healthy                           | no          | Battery Information |
| sg-battery-temperature                   | Number:Temperature       | Battery Temperature                       | no          | Battery Information |
| sg-daily-battery-discharge-energy        | Number:Energy            | Daily Battery Discharge Energy            | no          | Battery Information |
| sg-total-battery-discharge-energy        | Number:Energy            | Total Battery Discharge Energy            | no          | Battery Information |
| sg-battery-capacity                      | Number:Energy            | Battery Capacity                          | no          | Battery Information |
| sg-battery-capacity-high-precision       | Number:Energy            | Battery Capacity High Precision           | no          | Battery Information |
| sg-daily-charge-energy                   | Number:Energy            | Daily Charge Energy                       | no          | Battery Information |
| sg-total-charge-energy                   | Number:Energy            | Total Charge Energy                       | no          | Battery Information |
| sg-grid-frequency                        | Number:Frequency         | Grid Frequency                            | yes         | Grid Information    |
| sg-grid-frequency-high-precision         | Number:Frequency         | Grid Frequency High Precision             | yes         | Grid Information    |
| sg-daily-import-energy                   | Number:Energy            | Daily Import Energy                       | no          | Grid Information    |
| sg-total-import-energy                   | Number:Energy            | Total Import Energy                       | no          | Grid Information    |
| sg-daily-export-energy                   | Number:Energy            | Daily Export Energy                       | no          | Grid Information    |
| sg-total-export-energy                   | Number:Energy            | Total Export Energy                       | no          | Grid Information    |
| sg-daily-export-power-from-pv            | Number:Power             | Daily Export Power from PV                | no          | Grid Information    |
| sg-total-export-energy-from-pv           | Number:Energy            | Total Export Energy from PV               | no          | Grid Information    |
| sg-export-power                          | Number:Power             | Export Power                              | no          | Grid Information    |
| sg-daily-output-energy                   | Number:Power             | Daily Output Energy                       | yes         | Grid Information    |
| sg-total-output-energy                   | Number:Power             | Total Output Energy                       | yes         | Grid Information    |
| sg-drm-state                             | String                   | DRM State                                 | yes         | Grid Information    |
| sg-load-power                            | Number:Power             | Load Power                                | no          | Load Information    |
| sg-daily-direct-energy-consumption       | Number:Energy            | Daily Direct Energy Consumption           | no          | Load Information    |
| sg-total-direct-energy-consumption       | Number:Energy            | Total Direct Energy Consumption           | no          | Load Information    |
| sg-self-consumption-today                | Number:Dimensionless     | Self Consumption Today                    | no          | Load Information    |
| sg-phase-a-backup-current                | Number:ElectricCurrent   | Phase A Backup Current                    | yes         | Backup Information    |
| sg-phase-b-backup-current                | Number:ElectricCurrent   | Phase B Backup Current                    | yes         | Backup Information    |
| sg-phase-c-backup-current                | Number:ElectricCurrent   | Phase C Backup Current                    | yes         | Backup Information    |
| sg-phase-a-backup-power                  | Number:Power             | Phase A Backup Power                      | yes         | Backup Information    |
| sg-phase-b-backup-power                  | Number:Power             | Phase B Backup Power                      | yes         | Backup Information    |
| sg-phase-c-backup-power                  | Number:Power             | Phase C Backup Power                      | yes         | Backup Information    |
| sg-phase-a-backup-voltage                | Number:ElectricPotential | Phase A Backup Voltage                    | yes         | Backup Information    |
| sg-phase-b-backup-voltage                | Number:ElectricPotential | Phase B Backup Voltage                    | yes         | Backup Information    |
| sg-phase-c-backup-voltage                | Number:ElectricPotential | Phase C Backup Voltage                    | yes         | Backup Information    |
| sg-total-backup-power                    | Number:Power             | Backup Total Power                        | yes         | Backup Information    |
| sg-backup-frequency                      | Number:Frequency         | Backup Frequency                          | yes         | Backup Information    |
| sg-export-limit-min                      | Number:Energy            | Export Limit Min                          | yes         | Settings Information    |
| sg-export-limit-max                      | Number:Energy            | Export Limit Max                          | yes         | Settings Information    |
| sg-bdc-rated-power                       | Number:Power             | BDC Rated Power                           | yes         | Settings Information    |
| sg-max-charging-current-bms              | Number:Energy            | Max. Charging Current                     | yes         | Settings Information    |
| sg-max-discharging-current-bms           | Number:Energy            | Max. Discharging Current                  | yes         | Settings Information    |
| sg-power-flow-status-pv-power            | Switch                   | Power generated from PV                   | yes         | Power Flow Information    |
| sg-power-flow-status-battery-charging    | Switch                   | Battery charging                          | yes         | Power Flow Information    |
| sg-power-flow-status-battery-discharging | Switch                   | Battery discharging                       | yes         | Power Flow Information    |
| sg-power-flow-status-positive-load-power | Switch                   | OFF: Load is reactive, ON: Load is active | yes         | Power Flow Information    |
| sg-power-flow-status-feed-in-power       | Switch                   | Feed in power to grid                     | yes         | Power Flow Information    |
| sg-power-flow-status-import-from-grid    | Switch                   | Import power from grid                    | yes         | Power Flow Information    |
| sg-power-flow-status-negative-load-power | Switch                   | Power generated from Load                 | yes         | Power Flow Information    |

## Full Example

This example shows how to configure a Sungrow inverter connected via modbus and uses the most common channels.

### sungrow.things

```java
Bridge modbus:tcp:sungrowBridge [ host="10.0.0.2", port=502, id=1, enableDiscovery=false ] {
    Thing sungrow-inverter sungrowInverter "Sungrow Inverter" [ pollInterval=5000 ]
}
```

### sungrow.items

```java
// Groups
Group sungrowInverter "Sungrow Inverter" ["Inverter"]
Group overview "Overview" (sungrowInverter)
Group batteryInformation "Battery information" (sungrowInverter)
Group gridInformation "Grid information" (sungrowInverter)
Group loadInformation "Load information" (sungrowInverter)

// Overview
Number:Power total_active_power "Total Active Power" (overview) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-total-active-power"}
Number:Power total_dc_power "Total DC Power" (overview) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-total-dc-power"}
Number:Energy daily_pv_generation "Daily PV Generation" (overview) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-daily-pv-generation"}
Number:Energy total_pv_generation "Total PV Generation" (overview) ["Measurement", "Energy"]  {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-overview#sg-total-pv-generation"}

// Battery information
Number:Power battery_power "Battery Power" (batteryInformation) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-battery-power"}
Number:Dimensionless battery_level "Battery Level" (batteryInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-battery-level"}
Number:Energy daily_charge_energy "Daily Battery Charge Energy" (batteryInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-daily-charge-energy"}
Number:Energy daily_discharge_energy "Daily Battery Discharge Energy" (batteryInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-battery-information#sg-daily-battery-discharge-energy"}

// Grid information
Number:Power export_power "Export Power" (gridInformation) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-grid-information#sg-export-power"}
Number:Energy daily_export_energy "Daily Export Energy" (gridInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-grid-information#sg-daily-export-energy"}
Number:Energy daily_import_energy "Daily Import Energy" (gridInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-grid-information#sg-daily-import-energy"}

// Load information
Number:Power load_power "Load Power" (loadInformation) ["Measurement", "Power"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-load-information#sg-load-power"}
Number:Energy daily_direct_energy_consumption "Daily Direct Energy Consumption" (loadInformation) ["Measurement", "Energy"] {channel="modbus:sungrow-inverter:sungrowBridge:sungrowInverter:sg-load-information#sg-daily-direct-energy-consumption"}
```

### sungrow.sitemap

```perl
sitemap sungrow label="Sungrow Binding"
{
    Frame {
        Text item=total_active_power
        Text item=total_dc_power
        Text item=daily_pv_generation
        Text item=total_pv_generation

        Text item=battery_power
        Text item=battery_level
        Text item=daily_charge_energy
        Text item=daily_discharge_energy

        Text item=export_power
        Text item=daily_export_energy
        Text item=daily_import_energy

        Text item=load_power
        Text item=daily_direct_energy_consumption
    }
}
```
