# DSMR Binding

The DSMR-binding is targeted towards Dutch, Belgium, Luxembourger and Austrian users having a smart meter (Dutch: 'Slimme meter').
Data of Dutch/Belgium/Luxembourg/Austrian smart meters can be obtained via the P1-port.
When connecting this port from a serial port the data can be read out.

This binding reads the P1-port of:

- Dutch Smart Meters that comply to NTA8130, DSMR v2.1, DSMR v2.2, DSMR v3.0, DSMR v4.0, DSMR v4.04, and DSMR 5.0.
- Belgium Smart Meters that comply to e-MUCS v1.0.
- Luxembourg’s electricity meter "Smarty" that comply to V1.0.
- Austrian electricity meters.

Although DSMR v4.2 is not an official specification, the binding has support for this version.

If you are not living in the Netherlands/Belgium/Luxembourg but do want to read a meter please have look at the [SmartMeter Binding](/addons/bindings/smartmeter/).
Because the Dutch Meter standard is based on the IEC-62056-21 standard it might be desirable to build support for other country metering systems based on that standard in this binding.

## Serial Port Configuration

The P1-port is a serial port. To configure the serial port within openHAB see the [general documentation about serial port configuration](/docs/administration/serial.html).

## Supported Things

### dsmrBridge (The Netherlands/Belgium)

`dsmrBridge`: This is the device that communicated between the binding (serial) and its internal meters.
You always have to have a 'Dutch/Belgium Smart Meter'-bridge.
The bridge contains the serial port configuration.
Specific meters are bound via the bridge to the smart meter.
A smart meter consists typically out of minimal 2 meters.
A generic meter and the electricity meter. Each meter is bound to the DSMR protocol the physical meter supports.
For each meter it is possible to set a refresh rate at which the status is updated.
The physical meter might update with a high frequency per second, while it is desired to have only values per minute.

The Belgium e-MUCS protocol is an extension to the DSMR standard.
Belgium meters have `emucs` in the thing name.
Due to it similarities the bridge for Belgium meters is also a dsmrBridge.

### smartyBridge (Luxembourg, Austria)

`smartyBridge`: This is the device that communicated between the binding (serial) and its internal meters.
You always have to have a 'Smarty Smart Meter'-bridge.
The bridge contains the serial port configuration.

## Discovery

The `dsmrBridge` and meters can be discovered via the discovery process.
The `smartyBridge` can be discovered.
Because the `smartyBridge` requires a decryption key.
You need to set the decryption key when the bridge is added.
After the decryption key is set a new discovery can be started to discover the meter.

If a bridge is manually configured it is possible to auto detect available meters.

### Configuration

The configuration for the `dsmrBridge` consists of the following parameters:

| Parameter           | Description                                                                                                 |
|---------------------|-------------------------------------------------------------------------------------------------------------|
| serialPort          | The serial port where the P1-port is connected to (e.g. Linux: `/dev/ttyUSB1`, Windows: `COM2`) (mandatory) |
| receivedTimeout     | The time out period in which messages are expected to arrive, default is 120 seconds                        |
| baudrate            | Baudrate when no auto detect. valid values: 4800, 9600, 19200, 38400, 57600, 115200                         |
| databits            | Data bits when no auto detect. valid values: 5, 6, 7, 8                                                     |
| parity              | Parity when no auto detect. valid values: E(ven), N(one), O(dd)                                             |
| stopbits            | Stop bits when no auto detect. valid values: 1, 1.5, 2                                                      |

The configuration for the `smartyBridge` consists of the following parameters:

| Parameter           | Description                                                                                                 |
|---------------------|-------------------------------------------------------------------------------------------------------------|
| serialPort          | The serial port where the P1-port is connected to (e.g. Linux: `/dev/ttyUSB1`, Windows: `COM2`) (mandatory) |
| decryptionKey       | The meter specific decryption key (mandatory)                                                               |
| additionalKey       | Additional key for meters that require a secondary key. Some meters in Austria require this                 |
| receivedTimeout     | The time out period in which messages are expected to arrive, default is 120 seconds                        |

**Note:** _The manual configuration is only needed if the DSMR-device requires non DSMR-standard Serial Port parameters (i.e. something different then `115200 8N1` or `9600 7E1`)_

### Troubleshooting

If there are unexpected configuration issues.
For example a meter could not be found or not all channels expected are available.
Than run the discovery process and look into the log file.
There are extra checks that can give more information about what might be wrong.

## Meters

The information in this paragraph in necessary if you choose to configure the meters manually in a `.things` file.

Supported meters:

| Meter Thing                                     | Thing type ID                | M-Bus channel | Refresh rate |
|-------------------------------------------------|------------------------------|---------------|--------------|
| DSMR V2 / V3 Device                             | `device_v2_v3`               | -1            | 10 seconds   |
| DSMR V4 Device                                  | `device_v4`                  | -1            | 10 seconds   |
| DSMR V5 Device                                  | `device_v5`                  | -1            | 10 seconds   |
| e-MUCS V1.0 Device                              | `device_emucs_v1_0`          | -1            | ?            |
| ACE4000 GTMM Mk3 Electricity meter              | `electricity_ace4000`        | 0             | 10 seconds   |
| DSMR V2.1 Electricity meter                     | `electricity_v2_1`           | 0             | 10 seconds   |
| DSMR V2.2 Electricity meter                     | `electricity_v2_2`           | 0             | 10 seconds   |
| DSMR V3 Electricity meter                       | `electricity_v3_0`           | 0             | 10 seconds   |
| DSMR V4.0 Electricity meter                     | `electricity_v4_0`           | 0             | 10 seconds   |
| DSMR V4.0.4 Electricity meter                   | `electricity_v4_0_4`         | 0             | 10 seconds   |
| DSMR V4.2 Electricity meter                     | `electricity_v4_2`           | 0             | 10 seconds   |
| DSMR V5 Electricity meter                       | `electricity_v5_0`           | 0             | 10 seconds   |
| e-MUCS V1.0 Electricity meter                   | `electricity_emucs_v1_0`     | 0             | ?            |
| Smarty V1.0 Electricity Meter                   | `electricity_smarty_v1_0`    | 0             | 10 seconds   |
| ACE4000 GTMM Mk3 Gas meter                      | `gas_ace4000`                | 3             | 1 hour       |
| DSMR V2.1 Gas meter                             | `gas_v2_1`                   | 0             | 24 hours     |
| DSMR V2.2 Gas meter                             | `gas_v2_2`                   | 0             | 24 hours     |
| DSMR V3.0 Gas meter                             | `gas_v3_0`                   | _note 1_      | 1 hour       |
| e-MUCS V1.0 Gas meter                           | `gas_emucs_v1_0`             | _note 1_      | ?            |
| ACE4000 GTMM Mk3 Cooling meter                  | `cooling_ace4000`            | 6             | 1 hour       |
| DSMR V2.2 Cooling meter                         | `cooling_v2_2`               | 0             | 1 hour       |
| ACE4000 GTMM Mk3 Heating meter                  | `heating_ace4000`            | 4             | 1 hour       |
| DSMR V2.2 Heating meter                         | `heating_v2_2`               | 0             | 1 hour       |
| ACE4000 GTMM Mk3 Water meter                    | `water_ace4000`              | 5             | 1 hour       |
| DSMR V2.2 Water meter                           | `water_v2_2`                 | 0             | 1 hour       |
| DSMR V3.0 Water meter                           | `water_v3_0`                 | _note 1_      | 1 hour       |
| ACE4000 GTMM Mk3 1st Slave Electricity meter    | `slave_electricity1_ace4000` | 1             | 1 hour       |
| ACE4000 GTMM Mk3 2nd Slave Electricity meter    | `slave_electricity2_ace4000` | 2             | 1 hour       |
| DSMR V4.x Slave Electricity meter               | `slave_electricity_v4`       | _note 1_      | 1 hour       |
| DSMR V5 Slave Electricity meter                 | `slave_electricity_v5`       | _note 1_      | 5 minutes    |
| DSMR V3.0 Generic meter                         | `generic_v3_0`               | _note 1_      | 1 hour       |
| DSMR V3.0 Giga Joule meter (heating or cooling) | `gj_v3_0`                    | _note 1_      | 1 hour       |
| DSMR V4.x Giga Joule meter (heating or cooling) | `gj_v4`                      | _note 1_      | 1 hour       |
| DSMR V5 Giga Joule meter (heating or cooling)   | `gj_v5_0`                    | _note 1_      | 5 minutes    |
| DSMR V4.x m3 meter (gas or water)               | `m3_v4`                      | _note 1_      | 1 hour       |
| DSMR V5 m3 meter (gas or water)                 | `m3_v5_0`                    | _note 1_      | 5 minutes    |

_note 1_. The channel of these meters is dependent on the physical installation and corresponds to the M-Bus channel.
You can ask your supplier / installer for this information or you can retrieve it from the logfiles (see _Determine M-Bus channel_).

### Configuration

The configuration for the meters consists of the following parameters:

| Parameter           | Description                                                                          |
|---------------------|--------------------------------------------------------------------------------------|
| refresh             | Time in seconds with which the state of the device is updated. Default is 60 seconds |
| channel             | M-Bus channel. See the table above                                                   |

#### Examples

```java
Bridge dsmr:dsmrBridge:myDSMRDevice [serialPort="/dev/ttyUSB0"] {
    Things:
        device_v5 dsmrDeviceV5 [channel=-1]
        electricity_v5_0 electricityV5 [channel=0]
}
```

### Channels

#### Item configuration

Item configuration can be done in the regular way.

Manual configuration:
The following channels are supported:

- Y channel is supported
- \- channel is not supported
- O channel is supported only if the device has this functionality

| Channel Type ID                                  | Item Type                | Description                                                            | Ace4000 | DSMR V2.1 | DSMR V2.2 | DSMR V3.0 | DSMR V4.0 | DSMR V4.0.4 | DSMR V4.2 | DSMR V5 | SMARTY V1.0 | e-MUCS V1.0 | Austian |
|--------------------------------------------------|--------------------------|------------------------------------------------------------------------|---------|-----------|-----------|-----------|-----------|-------------|-----------|---------|-------------|-------------|---------|
|                                                  |                          | **Channels for the generic device**                                    |         |           |           |           |           |             |           |         |             |             |         |
| `p1_text_code`                                   | String                   | Text code from the device                                              | -       | Y         | Y         | Y         | Y         | Y           | Y         | -       | -           | -           | -       |
| `p1_text_string`                                 | String                   | Text string from the device                                            | -       | Y         | Y         | Y         | Y         | Y           | Y         | Y       | Y           | Y           | -       |
| `p1_version_output`                              | String                   | Version information (most times this refers to the DSMR specification) | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | Y       |
| `p1_emucs_version_output`                        | String                   | e-MUCS version information                                             | -       | -         | -         | -         | -         | -           | -         | -       | Y           | y           | -       |
| `p1_timestamp`                                   | DateTime                 | Timestamp of the last device reading                                   | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | Y           | Y       |
|                                                  |                          | **Channels for the cooling meter**                                     |         |           |           |           |           |             |           |         |             |             |         |
| `cmeter_value_v2`                                | Number:Energy            | The total amount of cooling used in the past period (GJ)               | Y       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `cmeter_value_v2_timestamp`                      | DateTime                 | Timestamp of the last meter reading                                    | Y       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `cmeter_equipment_identifier_v2_2`               | String                   | Equipment identifier                                                   | -       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
|                                                  |                          | **Channels for the main electricity meter**                            |         |           |           |           |           |             |           |         |             |             |         |
| `emeter_equipment_identifier_v2_x`               | String                   | Electricity Equipment identifier                                       | -       | Y         | Y         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_equipment_identifier`                    | String                   | Electricity Equipment identifier                                       | -       | -         | -         | Y         | Y         | Y           | Y         | Y       | -           | Y           | -       |
| `emeter_delivery_tariff0`                        | Number:Energy            | Total amount of electricity used for tariff 0 (kWh)                    | Y       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | Y       |
| `emeter_delivery_tariff1`                        | Number:Energy            | Total amount of electricity used for tariff 1 (kWh)                    | Y       | Y         | Y         | Y         | Y         | Y           | Y         | Y       | -           | Y           | Y       |
| `emeter_delivery_tariff2`                        | Number:Energy            | Total amount of electricity used for tariff 2 (kWh)                    | Y       | Y         | Y         | Y         | Y         | Y           | Y         | Y       | -           | Y           | Y       |
| `emeter_production_tariff0`                      | Number:Energy            | Total amount of electricity produced for tariff 0 (kWh)                | Y       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | Y       |
| `emeter_production_tariff1`                      | Number:Energy            | Total amount of electricity produced for tariff 1 (kWh)                | Y       | Y         | Y         | Y         | Y         | Y           | Y         | Y       | -           | Y           | Y       |
| `emeter_production_tariff2`                      | Number:Energy            | Total amount of electricity produced for tariff 2 (kWh)                | Y       | Y         | Y         | Y         | Y         | Y           | Y         | Y       | -           | Y           | Y       |
| `emeter_delivery_tariff0_antifraud`              | Number:Energy            | Total amount of electricity used for tariff 2 [antifraud] (kWh)        | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_delivery_tariff1_antifraud`              | Number:Energy            | Total amount of electricity used for tariff 1 [antifraud] (kWh)        | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_delivery_tariff2_antifraud`              | Number:Energy            | Total amount of electricity used for tariff 2 [antifraud] (kWh)        | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_total_imported_energy_register_q`        | Number:Energy            | Total Imported Energy (Q+) (kvarh)                                     | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | Y       |
| `emeter_total_imported_energy_register_r_rate1`  | Number:Energy            | Total Imported Energy Rate 1 (kvarh)                                   | -       | -         | -         | -         | -         | -           | -         | -       | -           | -           | Y       |
| `emeter_total_imported_energy_register_r_rate2`  | Number:Energy            | Total Imported Energy Rate 2 (kvarh)                                   | -       | -         | -         | -         | -         | -           | -         | -       | -           | -           | Y       |
| `emeter_total_exported_energy_register_q`        | Number:Energy            | Total Exported Energy (Q-) (kvarh)                                     | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | Y       |
| `emeter_total_exported_energy_register_r_rate1`  | Number:Energy            | Total Exported Energy Rate 1 (kvarh)                                   | -       | -         | -         | -         | -         | -           | -         | -       | -           | -           | Y       |
| `emeter_total_exported_energy_register_r_rate2`  | Number:Energy            | Total Exported Energy Rate 2 (kvarh)                                   | -       | -         | -         | -         | -         | -           | -         | -       | -           | -           | Y       |
| `emeter_tariff_indicator`                        | String                   | Current tariff indicator                                               | Y       | Y         | Y         | Y         | Y         | Y           | Y         | Y       | -           | Y           | -       |
| `emeter_treshold_a_v2_1`                         | Number:ElectricCurrent   | Actual threshold (A)                                                   | -       | Y         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_treshold_a`                              | Number:ElectricCurrent   | Actual threshold (A)                                                   | Y       | -         | Y         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_fuse_threshold_a`                        | Number:ElectricCurrent   | Active fuse threshold (A)                                              | -       | -         | -         | -         | -         | -           | -         | -       | -           | Y           | -       |
| `emeter_treshold_kwh`                            | Number:Power             | Actual threshold (kW)                                                  | -       | -         | -         | -         | Y         | Y           | -         | -       | -           | Y           | -       |
| `emeter_switch_position_v2_1`                    | Number                   | Switch position                                                        | -       | Y         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_switch_position`                         | Number                   | Switch position                                                        | Y       | -         | Y         | Y         | Y         | Y           | -         | -       | Y           | Y           | -       |
| `emeter_active_import_power`                     | Number:Power             | Aggregate active import power (W)                                      | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_actual_delivery`                         | Number:Power             | Current power delivery (kW)                                            | -       | Y         | Y         | Y         | Y         | Y           | Y         | Y       | Y           | Y           | Y       |
| `emeter_actual_production`                       | Number:Power             | Current power production (kW)                                          | -       | -         | -         | Y         | Y         | Y           | Y         | Y       | Y           | Y           | Y       |
| `emeter_actual_reactive_delivery`                | Number                   | Actual Reactive Power Delivery (kvar)                                  | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | Y       |
| `emeter_actual_reactive_production`              | Number                   | Actual Reactive Power Production (kvar)                                | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | Y       |
| `emeter_active_threshold_smax`                   | Number                   | Active threshold (SMAX) (kVA)                                          | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_threshold_kw`                            | Number:Power             | Active threshold (SMAX) (kVA)                                          | -       | -         | -         | Y         | Y         | Y           | -         | -       | Y           | Y           | -       |
| `emeter_power_failures`                          | Number                   | Number of power failures                                               | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | -       |
| `emeter_long_power_failures`                     | Number                   | Number of long power failures                                          | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `emeter_power_failure_log_entries`               | Number                   | Number of entries in the power failure log                             | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | -       |
| `emeter_power_failure_log_timestamp[x]` _note 2_ | DateTime                 | Timestamp for entry [x] in the power failure log                       | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | -       |
| `emeter_power_failure_log_duration[x]` _note 2_  | Number:Time              | Duration for entry [x] the power failure log                           | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | -       |
| `emeter_voltage_sags_l1`                         | Number                   | Number of voltage sags L1                                              | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | -       |
| `emeter_voltage_sags_l2`                         | Number                   | Number of voltage sags L2                                              | -       | -         | -         | -         | O         | O           | O         | O       | O           | -           | -       |
| `emeter_voltage_sags_l3`                         | Number                   | Number of voltage sags L3                                              | -       | -         | -         | -         | O         | O           | O         | O       | O           | -           | -       |
| `emeter_voltage_swells_l1`                       | Number                   | Number of voltage swells L1                                            | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `emeter_voltage_swells_l2`                       | Number                   | Number of voltage swells L2                                            | -       | -         | -         | -         | O         | O           | O         | O       | -           | -           | -       |
| `emeter_voltage_swells_l3`                       | Number                   | Number of voltage swells L3                                            | -       | -         | -         | -         | O         | O           | O         | O       | -           | -           | -       |
| `emeter_instant_current_l1`                      | Number:ElectricCurrent   | Instant Current L1 (A)                                                 | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | Y           | -       |
| `emeter_instant_current_l2`                      | Number:ElectricCurrent   | Instant Current L2 (A)                                                 | -       | -         | -         | -         | O         | O           | O         | O       | O           | Y           | -       |
| `emeter_instant_current_l3`                      | Number:ElectricCurrent   | Instant Current L3 (A)                                                 | -       | -         | -         | -         | O         | O           | O         | O       | O           | Y           | -       |
| `emeter_instant_power_delivery_l1`               | Number:Power             | Instant Power Delivery L1 (kW)                                         | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | -       |
| `emeter_instant_power_delivery_l2`               | Number:Power             | Instant Power Delivery L2 (kW)                                         | -       | -         | -         | -         | O         | O           | O         | O       | O           | -           | -       |
| `emeter_instant_power_delivery_l3`               | Number:Power             | Instant Power Delivery L3 (kW)                                         | -       | -         | -         | -         | O         | O           | O         | O       | O           | -           | -       |
| `emeter_instant_power_production_l1`             | Number:Power             | Instant Power Production L1 (kW)                                       | -       | -         | -         | -         | Y         | Y           | Y         | Y       | Y           | -           | -       |
| `emeter_instant_power_production_l2`             | Number:Power             | Instant Power Production L2 (kW)                                       | -       | -         | -         | -         | O         | O           | O         | O       | O           | -           | -       |
| `emeter_instant_power_production_l3`             | Number:Power             | Instant Power Production L3 (kW)                                       | -       | -         | -         | -         | O         | O           | O         | O       | O           | -           | -       |
| `emeter_instant_reactive_power_delivery_l1`      | Number:Power             | Instant Reactive Power Delivery L1 (kvar)                              | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_instant_reactive_power_delivery_l2`      | Number:Power             | Instant Reactive Power Delivery L2 (kvar)                              | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_instant_reactive_power_delivery_l3`      | Number:Power             | Instant Reactive Power Delivery L3 (kvar)                              | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_instant_reactive_power_production_l1`    | Number:Power             | Instant Reactive Power Prodcution L1 (kvar)                            | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_instant_reactive_power_production_l2`    | Number:Power             | Instant Reactive Power Prodcution L2 (kvar)                            | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_instant_reactive_power_production_l3`    | Number:Power             | Instant Reactive Power Prodcution L3 (kvar)                            | -       | -         | -         | -         | -         | -           | -         | -       | Y           | -           | -       |
| `emeter_instant_voltage_l1`                      | Number:ElectricPotential | Instant Voltage L1 (V)                                                 | -       | -         | -         | -         | -         | -           | -         | Y       | -           | Y           | -       |
| `emeter_instant_voltage_l2`                      | Number:ElectricPotential | Instant Voltage L2 (V)                                                 | -       | -         | -         | -         | -         | -           | -         | O       | -           | Y           | -       |
| `emeter_instant_voltage_l3`                      | Number:ElectricPotential | Instant Voltage L3 (V)                                                 | -       | -         | -         | -         | -         | -           | -         | O       | -           | Y           | -       |
|                                                  |                          | **Channels for the slave electricity meter**                           |         |           |           |           |           |             |           |         |             |             |         |
| `meter_device_type`                              | String                   | Slave Electricity Meter Device Type                                    | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `meter_equipment_identifier`                     | String                   | Slave Electricity Meter ID                                             | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `emeter_delivery_tariff0`                        | Number:Energy            | Total amount of slave electricity used for tariff 0 (kWh)              | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_delivery_tariff1`                        | Number:Energy            | Total amount of slave electricity used for tariff 1 (kWh)              | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_delivery_tariff2`                        | Number:Energy            | Total amount of slave electricity used for tariff 2 (kWh)              | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_production_tariff0`                      | Number:Energy            | Total amount of slave electricity produced for tariff 0 (kWh)          | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_production_tariff1`                      | Number:Energy            | Total amount of slave electricity produced for tariff 1 (kWh)          | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_production_tariff2`                      | Number:Energy            | Total amount of slave electricity produced for tariff 2 (kWh)          | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_tariff_indicator`                        | String                   | Current slave tariff indicator                                         | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_treshold_a`                              | Number:ElectricCurrent   | Actual slave threshold (A)                                             | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `meter_switch_position`                          | Number                   | Slave electricity switch position                                      | Y       | -         | -         | Y         | Y         | Y           | -         | -       | -           | -           | -       |
| `emeter_active_import_power`                     | Number:Power             | Slave aggregate active import power (W)                                | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `emeter_value`                                   | Number:Energy            | Slave electricity usage (kWh) in the past period                       | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `emeter_value_timestamp`                         | DateTime                 | Timestamp of the last reading                                          | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
|                                                  |                          | **Channels for the gas meter**                                         |         |           |           |           |           |             |           |         |             |             |         |
| `meter_device_type`                              | String                   | Gas Meter Device Type                                                  | -       | -         | -         | Y         | -         | -           | -         | -       | -           | Y           | -       |
| `meter_equipment_identifier`                     | String                   | Gas Meter ID                                                           | Y       | -         | -         | Y         | -         | -           | -         | -       | -           | Y           | -       |
| `gmeter_equipment_identifier_v2`                 | String                   | Gas Meter ID                                                           | -       | Y         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_24h_delivery_v2`                         | Number:Volume            | Gas Delivery past 24 hours                                             | Y       | Y         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_24h_delivery_v2_timestamp`               | DateTime                 | Timestamp of the last reading                                          | Y       | Y         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_24h_delivery_compensated_v2`             | Number:Volume            | Gas Delivery past 24 hours (compensated)                               | -       | Y         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_24h_delivery_compensated_v2_timestamp`   | DateTime                 | Timestamp of the last reading                                          | -       | Y         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_value_v3`                                | Number:Volume            | Gas Delivery past period                                               | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_value_v3_timestamp`                      | DateTime                 | Timestamp of the last reading                                          | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_last_value`                              | Number:Volume            | Gas Delivery last reading value                                        | -       | -         | -         | -         | -         | -           | -         | -       | -           | Y           | -       |
| `gmeter_last_value_timestamp`                    | DateTime                 | Timesamp of last Gas Delivery reading                                  | -       | -         | -         | -         | -         | -           | -         | -       | -           | Y           | -       |
| `gmeter_valve_position_v2_1`                     | Number                   | Gas Valve position                                                     | -       | Y         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_valve_position_v2_2`                     | Number                   | Gas Valve position                                                     | Y       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `meter_valve_switch_position`                    | Number                   | Gas Valve position                                                     | -       | -         | -         | Y         | -         | -           | -         | -       | -           | Y           | -       |
|                                                  |                          | **Channels for the generic meter**                                     |         |           |           |           |           |             |           |         |             |             |         |
| `meter_device_type`                              | String                   | Generic Meter Device Type                                              | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `gmeter_equipment_identifier`                    | String                   | Generic Meter ID                                                       | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `genmeter_value_v3`                              | Number                   | Delivery past period                                                   | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `meter_valve_switch_position`                    | Number                   | Generic Meter Valve/Switch position                                    | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
|                                                  |                          | **Channels for the GJ meter (Heating or Cooling)**                     |         |           |           |           |           |             |           |         |             | -           | -       |
| `meter_device_type`                              | String                   | GJ Meter Device Type                                                   | -       | -         | -         | Y         | Y         | Y           | Y         | Y       | -           |             |         |
| `meter_equipment_identifier`                     | Number                   | GJ Meter ID                                                            | -       | -         | -         | Y         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `gjmeter_value_v3`                               | Number:Energy            | GJ Delivery past period                                                | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `gjmeter_value_v3_timestamp`                     | DateTime                 | Timestamp of the last reading                                          | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `gjmeter_value_v4`                               | Number:Energy            | GJ Delivery past period                                                | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `gjmeter_value_v4_timestamp`                     | DateTime                 | Timestamp of the last reading                                          | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
|                                                  |                          | **Channels for the heating meter**                                     |         |           |           |           |           |             |           |         |             |             |         |
| `meter_valve_switch_position`                    | Number                   | GJ Meter Valve position                                                | -       | -         | -         | Y         | Y         | Y           | Y         | -       | -           | -           | -       |
| `meter_equipment_identifier`                     | String                   | Heating Meter ID                                                       | Y       | -         | -         | -         | -         | -           | -         | -       | -           | -           | -       |
| `hmeter_equipment_identifier_v2_2`               | String                   | Heating Meter ID                                                       | -       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `hmeter_value_v2`                                | Number:Energy            | Heating Delivery past period                                           | Y       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `hmeter_value_v2_timestamp`                      | DateTime                 | Timestamp of the last reading                                          | Y       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
|                                                  |                          | m3 Meter Device Type                                                   | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `meter_equipment_identifier`                     | String                   | m3 Meter ID                                                            | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `m3meter_value`                                  | Number:Volume            | m3 Delivery past period                                                | -       | -         | -         | -         | Y         | Y           | Y         | Y       | -           | -           | -       |
| `meter_valve_switch_position`                    | Number                   | m3 Meter Valve position                                                | -       | -         | -         | -         | Y         | Y           | Y         | -       | -           | -           | -       |
|                                                  |                          | **Channels for the water meter**                                       |         |           |           |           |           |             |           |         |             |             |         |
| `meter_device_type`                              | String                   | Water Meter Device Type                                                | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `meter_equipment_identifier`                     | String                   | Water Meter ID                                                         | Y       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `wmeter_equipment_identifier_v2_2`               | String                   | Water Meter ID                                                         | -       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `wmeter_value_v2`                                | Number:Volume            | Water Delivery past period                                             | Y       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `wmeter_value_v2_timestamp`                      | DateTime                 | Timestamp of the last reading                                          | Y       | -         | Y         | -         | -         | -           | -         | -       | -           | -           | -       |
| `wmeter_value_v3`                                | Number:Volume            | Water Delivery past period                                             | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |
| `meter_valve_switch_position`                    | Number                   | Water Meter Valve position                                             | -       | -         | -         | Y         | -         | -           | -         | -       | -           | -           | -       |

_note 2_. The power failure log has a dynamic number of entries starting at `0`.
So `emeter_power_failure_log_timestamp0`, `emeter_power_failure_log_duration0` refers to the first entry,
`emeter_power_failure_log_timestamp1`, `emeter_power_failure_log_duration1` refers to the second entry, etc.

Channel identifier: `dsmr:<ThingTypeID>:<bridge id>:<id>:<channel type id>`

- ThingTypeID. See table with supported meters
- BridgeID. The configured id for the bridge
- id. The configured id for the ThingType you want to address
- channel type id. The channel type id

The following configuration must to be added to an item configuration file. E.g. `things/dsmr.items`

```java
ItemType <name> "<description>" (<Group>) {channel="<Channel identifier>"}
```

##### Examples

```java
Number:Energy MeterDeliveryTariff0 "Delivered Low Tariff [%.3f kWh]" {channel="dsmr:electricity_v5_0:mysmartmeter:electricityV5:emeter_delivery_tariff1}
```

## Full configuration example

`things/dsmr.things`

```java
Bridge dsmr:dsmrBridge:mysmartmeter [serialPort="/dev/ttyUSB0"] {
    Things:
        device_v5 dsmrV5Device [channel=-1]
        electricity_v5_0 electricityV5 [channel=0]
}
```

`things/dsmr.items`

```java
String P1Version "P1 Version output" {channel="dsmr:device_v5:mysmartmeter:dsmrV5Device:p1_version_output"}
Number:Energy MeterDeliveryTariff0 "Delivered Low Tariff [%.3f kWh]" {channel="dsmr:electricity_v5_0:mysmartmeter:electricityV5:emeter_delivery_tariff1"}
Number:Energy MeterDeliveryTariff1 "Delivered High Tariff [%.3f kWh]" {channel="dsmr:electricity_v5_0:mysmartmeter:electricityV5:emeter_delivery_tariff2"}
```

## Determine M-Bus channel

By manually trigger the discovery process, you can use the logging to find out a M-Bus channel. Look for the following logfile line:
`<Timestamp> [INFO ] [<class>] - New compatible meter: [Meter type: M3_V5_0, channel: 1, Meter type: ELECTRICITY_V5, channel: 0]`

Here you find the ThingTypeID (it is stated only in capitals) and the M-Bus channel. The above example would lead to the following Thing definition

```java
Bridge definition {
    Things:
        m3_v5_0 mygasmeter [channel=1]
        electricity_v5 [channel=0]
}
```
