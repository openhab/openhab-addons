# HomeWizard Binding

The HomeWizard binding provides access to several HomeWizard devices by using the local API of those devices.

## Installation

There are two important points to note:

1. For API v1, the local API of each device must be enabled.
1. For API v2, a bearer token needs to be obtained from the device.

See <https://api-documentation.homewizard.com/docs/getting-started> for a more detailed description of the API and for instructions on how to enable/use the API versions.

### Fixed Address

The devices support mDNS discovery, but the binding does not support that yet.
As a result, the devices should be reachable through a hostname or a fixed IP address.
Since the devices themselves have no option to set a fixed IP address you will need a different solution, for instance having your router hand out an IP address based upon the MAC address of the devices.

## Supported Things

The binding offers Things, providing access to all the supported HomeWizard devices.

| Thing         | Device              | Description                                                                                         |
|---------------|---------------------|-----------------------------------------------------------------------------------------------------|
| hwe-p1        | P1 Meter            | Reads total and current energy usage and total gas usage (v1 and v2).                               |
| hwe-skt       | Energy Socket       | Reads total and current energy usage. Controls power switch, lock, and ring brightness (v1).        |
| hwe-wtr       | Watermeter          | Reads total and current water usage (v1).                                                           |
| hwe-kwh       | kWh Meter           | Reads total and current energy usage (v1).                                                          |
| hwe-bat       | Plug-In Battery     | Reads total and current energy usage and the current charge (v2).                                   |
| p1_wifi_meter | Wi-Fi P1 Meter      | [Deprecated] Reads total and current energy usage and total gas usage.                              |
| energy_socket | Wi-Fi Energy Socket | [Deprecated] Reads total and current energy usage. Controls power switch, lock, and ring brightness.|
| watermeter    | Wi-Fi Watermeter    | [Deprecated] Reads total and current water usage.                                                   |

## Discovery

Auto discovery is not available for this binding.

## Thing Configuration

All devices can be configured through the web interface.

| Parameter    | Required | Default | Description                                                                                       |
|--------------|----------|---------|---------------------------------------------------------------------------------------------------|
| ipAddress    | *        |         | This specifies the IP address (or host name) where the meter can be found.                        |
| refreshDelay |          | 5       | This specifies the interval in seconds used by the binding to read updated values from the meter. |
| apiVersion   | *        | v1      | The API version to be used.                                                                       |
| bearerToken  |          |         | The bearer token to be used when using API v2.                                                    |

Note that update rate of the P1 Meter itself depends on the frequency of the telegrams it receives from the Smart Meter.
For DSMR5 meters this is generally once per second, for older versions the frequency is much lower.

## Channels

### HWE-P1

| Channel ID                | Item Type                 | Description                                                                            | Availability |
|---------------------------|---------------------------|----------------------------------------------------------------------------------------|--------------|
| power                     | Number:Power              | The active power in Watt.                                                              | v1, 2.0.0    |
| power_l1                  | Number:Power              | The active power for phase 1 in Watt.                                                  | v1, 2.0.0    |
| power_l2                  | Number:Power              | The active power for phase 2 in Watt.                                                  | v1, 2.0.0    |
| power_l3                  | Number:Power              | The active power for phase 3 in Watt.                                                  | v1, 2.0.0    |
| voltage_l1                | Number:ElectricPotential  | The active voltage for phase 1 in Volt.                                                | v1, 2.0.0    |
| voltage_l2                | Number:ElectricPotential  | The active voltage for phase 2 in Volt.                                                | v1, 2.0.0    |
| voltage_l3                | Number:ElectricPotential  | The active voltage for phase 3 in Volt.                                                | v1, 2.0.0    |
| current_l1                | Number:ElectricCurrent    | The active current for phase 1 in Ampere.                                              | v1, 2.0.0    |
| current_l2                | Number:ElectricCurrent    | The active current for phase 2 in Ampere.                                              | v1, 2.0.0    |
| current_l3                | Number:ElectricCurrent    | The active current for phase 3 in Ampere.                                              | v1, 2.0.0    |
| energy_import             | Number:Energy             | The total energy usage meter reading in kWh.                                           | v1, 2.0.0    |
| energy_import_t1          | Number:Energy             | The energy usage meter reading for tariff 1 in kWh.                                    | v1, 2.0.0    |
| energy_import_t2          | Number:Energy             | The energy usage meter reading for tariff 2 in kWh.                                    | v1, 2.0.0    |
| energy_export             | Number:Energy             | The total energy feed-in meter reading in kWh.                                         | v1, 2.0.0    |
| energy_export_t1          | Number:Energy             | The energy feed-in meter reading for tariff 1 in kWh.                                  | v1, 2.0.0    |
| energy_export_t2          | Number:Energy             | The energy feed-in meter reading for tariff 2 in kWh.                                  | v1, 2.0.0    |
| reactive_power            | Number                    | The active reactive power in Volt-Ampere reactive.                                     | v1, 2.0.0    |
| apparent_power            | Number                    | The active apparent power in Volt-Ampere.                                              | v1, 2.0.0    |
| power_factor              | Number:Dimensionless      | The active power factor.                                                               | v1, 2.0.0    |
| frequency                 | Number:Frequency          | The active frequency in Hertz.                                                         | v1, 2.0.0    |
| total_gas                 | Number:Volume             | The most recently reported total imported gas in m^3.                                  | v1, 2.0.0    |
| gas_timestamp             | DateTime                  | The time stamp of the total_gas measurement.                                           | v1, 2.0.0    |
| power_failures            | Number                    | The number of power failures detected by meter.                                        | v1, 2.0.0    |
| long_power_failures       | Number                    | The number of 'long' power failures detected by meter.                                 | v1, 2.0.0    |
| tariff                    | Number                    | The active tariff, matches one of the totals.                                          | v1, 2.0.0    |
| batteries_mode            | String                    | The control mode of the Plug-In Battery. Supported modes: "zero", "standby" and "to_full". In 2.2.0, modes "zero_charge_only" and "zero_discharge_only" are also supported. | v1, 2.1.0    |
| batteries_count           | Number                    | The number of connected Plug-In Batteries.                                             | v1, 2.2.0    |
| batteries_power           | Number:Power              | The current combined power consumption/production of the controlled Plug-In Batteries. | v1, 2.1.0    |
| batteries_target_power    | Number:Power              | The target power consumption/production of the controlled Plug-In Batteries.           | v1, 2.1.0    |
| batteries_max_consumption | Number:Power              | The maximum allowed consumption power of the controlled Plug-In Batteries.             | v1, 2.1.0    |
| batteries_max_production  | Number:Power              | The maximum allowed production power of the controlled Plug-In Batteries.              | v1, 2.1.0    |

### HWE-KWH

| Channel ID                | Item Type                 | Description                                                                            | Availability |
|---------------------------|---------------------------|----------------------------------------------------------------------------------------|--------------|
| power                     | Number:Power              | The active power in Watt.                                                              | v1, 2.0.0    |
| power_l1                  | Number:Power              | The active power for phase 1 in Watt.                                                  | v1, 2.0.0    |
| power_l2                  | Number:Power              | The active power for phase 2 in Watt.                                                  | v1, 2.0.0    |
| power_l3                  | Number:Power              | The active power for phase 3 in Watt.                                                  | v1, 2.0.0    |
| voltage_l1                | Number:ElectricPotential  | The active voltage for phase 1 in Volt.                                                | v1, 2.0.0    |
| voltage_l2                | Number:ElectricPotential  | The active voltage for phase 2 in Volt.                                                | v1, 2.0.0    |
| voltage_l3                | Number:ElectricPotential  | The active voltage for phase 3 in Volt.                                                | v1, 2.0.0    |
| current                   | Number:ElectricCurrent    | The active current in Ampere.                                                          | v1, 2.0.0    |
| current_l1                | Number:ElectricCurrent    | The active current for phase 1 in Ampere.                                              | v1, 2.0.0    |
| current_l2                | Number:ElectricCurrent    | The active current for phase 2 in Ampere.                                              | v1, 2.0.0    |
| current_l3                | Number:ElectricCurrent    | The active current for phase 3 in Ampere.                                              | v1, 2.0.0    |
| energy_import             | Number:Energy             | The total energy usage meter reading in kWh.                                           | v1, 2.0.0    |
| energy_export             | Number:Energy             | The total energy feed-in meter reading in kWh.                                         | v1, 2.0.0    |
| reactive_power            | Number                    | The active reactive power in Volt-Ampere reactive.                                     | v1, 2.0.0    |
| apparent_power            | Number                    | The active apparent power in Volt-Ampere.                                              | v1, 2.0.0    |
| power_factor              | Number:Dimensionless      | The active power factor.                                                               | v1, 2.0.0    |
| frequency                 | Number:Frequency          | The active frequency in Hertz.                                                         | v1, 2.0.0    |
| batteries_mode            | String                    | The control mode of the Plug-In Battery. Supported modes: "zero", "standby" and "to_full". In 2.2.0, modes "zero_charge_only" and "zero_discharge_only" are also supported. | v1, 2.1.0    |
| batteries_count           | Number                    | The number of connected Plug-In Batteries.                                             | v1, 2.2.0    |
| batteries_power           | Number:Power              | The current combined power consumption/production of the controlled Plug-In Batteries. | v1, 2.1.0    |
| batteries_target_power    | Number:Power              | The target power consumption/production of the controlled Plug-In Batteries.           | v1, 2.1.0    |
| batteries_max_consumption | Number:Power              | The maximum allowed consumption power of the controlled Plug-In Batteries.             | v1, 2.1.0    |
| batteries_max_production  | Number:Power              | The maximum allowed production power of the controlled Plug-In Batteries.              | v1, 2.1.0    |

### HWE-SKT

| Channel ID                | Item Type                 | Description                                                                            | Availability |
|---------------------------|---------------------------|----------------------------------------------------------------------------------------|--------------|
| power                     | Number:Power              | The active power in Watt.                                                              | v1           |
| voltage                   | Number:ElectricPotential  | The active voltage in Volt.                                                            | v1           |
| current                   | Number:ElectricCurrent    | The active current in Ampere.                                                          | v1           |
| energy_import             | Number:Energy             | The total energy usage meter reading in kWh.                                           | v1           |
| energy_export             | Number:Energy             | The total energy feed-in meter reading in kWh.                                         | v1           |
| reactive_power            | Number                    | The active reactive power in Volt-Ampere reactive.                                     | v1           |
| apparent_power            | Number                    | The active apparent power in Volt-Ampere.                                              | v1           |
| power_factor              | Number:Dimensionless      | The active power factor.                                                               | v1           |
| frequency                 | Number:Frequency          | The active frequency in Hertz.                                                         | v1           |
| power_switch              | Switch                    | Access to the power switch of the Energy Socket.                                       | v1           |
| power_lock                | Switch                    | Access to the power lock of the Energy Socket.                                         | v1           |
| ring_brightness           | Number:Dimensionless      | Access to the brightness of the ring of the Energy Socket.                             | v1           |

### HWE-BAT

| Channel ID                | Item Type                 | Description                                                                            | Availability |
|---------------------------|---------------------------|----------------------------------------------------------------------------------------|--------------|
| power                     | Number:Power              | The active power in Watt.                                                              | 2.0.0        |
| voltage                   | Number:ElectricPotential  | The active voltage in Volt.                                                            | 2.0.0        |
| current                   | Number:ElectricCurrent    | The active current in Ampere.                                                          | 2.0.0        |
| energy_import             | Number:Energy             | The total energy usage meter reading in kWh.                                           | 2.0.0        |
| energy_export             | Number:Energy             | The total energy feed-in meter reading in kWh.                                         | 2.0.0        |
| frequency                 | Number:Frequency          | The active frequency in Hertz.                                                         | 2.0.0        |
| state_of_charge           | Number:Dimensionless      | Access to the current state of charge in percent.                                      | 2.0.0        |
| cycles                    | Number:Dimensionless      | Access to the number of battery cycles.                                                | 2.0.0        |

### HWE-WTR

| Channel ID                | Item Type                 | Description                                                                            | Availability |
|---------------------------|---------------------------|----------------------------------------------------------------------------------------|--------------|
| total_liter               | Number:Volume             | Total water usage in cubic meters.                                                     | v1           |
| active_liter              | Number:VolumetricFlowRate | The active water usage in liters per minute.                                           | v1           |


## Full Example

### `homewizard.things` Example

```java
Thing homewizard:hwe-p1:my_p1 [ ipAddress="192.178.1.67", refreshDelay=5, apiVersion=1 ]
Thing homewizard:hwe-skt:my_socket [ ipAddress="192.178.1.61", refreshDelay=5, apiVersion=1 ]
Thing homewizard:hwe-wtr:my_water [ ipAddress="192.178.1.27", refreshDelay=15, apiVersion=1 ]
```

### `homewizard.items` Example

```java
Number:Energy Energy_Import_T1 "Imported Energy T1 [%.0f kWh]" {channel="homewizard:hwe-p1:my_meter:energy#energy_import_t1" }
Number:Power  Active_Power_L1  "Active Power Phase 1 [%.1f W]" {channel="homewizard:hwe-p1:my_meter:energy#power_l1" }
DateTime      Gas_Update       "Gas Update Time [%1$tH:%1$tM]" {channel="homewizard:hwe-p1:my_meter:energy#gas_timestamp" }
```
