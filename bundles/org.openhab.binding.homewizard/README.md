# HomeWizard Binding

The HomeWizard binding provides access to several HomeWizard devices by using the local API of those devices.

## Installation

There are two important points of attention:

1. For API v1, the local API of each device must be enabled and a fixed address must be configured for the devices.
1. For API v2, a bearer token needs to be obtained from the device. See <https://api-documentation.homewizard.com/> for instructions how to obtain a token.

### Local API v1

The local API of a device can be enabled from the HomeWizard app.
Go to Settings in the app, then Meters and select the device you want to enable.
On this page enable the local API.

### Local API v2

This version is still in beta. Currently hwe-p1 and hwe-bat are supported.

### Fixed Address

The devices support mDNS discovery but the binding does not support that yet.
As a result the devices should be reachable through a hostname or a fixed IP address.
Since the devices themselves have no option to set a fixed IP address you will need a different solution, for instance having your router hand out an IP address based upon the MAC address of the devices.

## Supported Things

The binding offers Things, providing access to all the supported HomeWizard devices.

| Thing         | Device              | Description                                                                                         |
|---------------|---------------------|-----------------------------------------------------------------------------------------------------|
| hwe-p1        | P1 Meter            | Reads total and current energy usage and total gas usage (v1 and v2).                               |
| hwe-skt       | Energy Socket       | Reads total and current energy usage. Controls power switch, lock and ring brightness (v1).         |
| hwe-wtr       | Watermeter          | Reads total and current water usage (v1).                                                           |
| hwe-kwh       | kWh Meter           | Reads total and current energy usage (v1).                                                          |
| hwe-bat       | Plug-In Battery     | Reads total and current energy usage and the current charge (v2).                                   |
| p1_wifi_meter | Wi-Fi P1 Meter      | [Deprecated] Reads total and current energy usage and total gas usage.                              |
| energy_socket | Wi-Fi Energy Socket | [Deprecated] Reads total and current energy usage. Controls power switch, lock and ring brightness. |
| watermeter    | Wi-Fi Watermeter    | [Deprecated] Reads total and current water usage.                                                   |

## Discovery

Auto discovery is not available for this binding.

## Thing Configuration

All devices can be configured through the web interface.

| Parameter    | Required | Default | Description                                                                                       |
|--------------|----------|---------|---------------------------------------------------------------------------------------------------|
| ipAddress    | *        |         | This specifies the IP address (or host name) where the meter can be found.                        |
| refreshDelay |          | 5       | This specifies the interval in seconds used by the binding to read updated values from the meter. |
| apiVersion   | *        | v1      | The API version to be used. v2 is still in beta but is already supported in this binding.         |
| bearerToken  |          |         | The bearer token to be used when using API v2.                                                    |

Note that update rate of the P1 Meter itself depends on the frequency of the telegrams it receives from the Smart Meter.
For DSMR5 meters this is generally once per second, for older versions the frequency is much lower.

## Channels

| Channel ID             | Item Type                 | Description                                                                                | Available                         |
|------------------------|---------------------------|--------------------------------------------------------------------------------------------|-----------------------------------|
| power                  | Number:Power              | This channel provides the active usage in Watt.                                            | hwe-p1, hwe-skt, hwe-kwh, hwe-bat |
| power_l1               | Number:Power              | This channel provides the active usage for phase 1 in Watt.                                | hwe-p1, hwe-kwh                   |
| power_l2               | Number:Power              | This channel provides the active usage for phase 2 in Watt.                                | hwe-p1, hwe-kwh                   |
| power_l3               | Number:Power              | This channel provides the active usage for phase 3 in Watt.                                | hwe-p1, hwe-kwh                   |
| voltage                | Number:ElectricPotential  | This channel provides the active voltage in Volt.                                          | hwe-skt                           |
| voltage_l1             | Number:ElectricPotential  | This channel provides the active usage for phase 1 in Watt.                                | hwe-p1, hwe-kwh, hwe-bat          |
| voltage_l2             | Number:ElectricPotential  | This channel provides the active usage for phase 2 in Watt.                                | hwe-p1, hwe-kwh                   |
| voltage_l3             | Number:ElectricPotential  | This channel provides the active usage for phase 3 in Watt.                                | hwe-p1, hwe-kwh                   |
| current                | Number:ElectricCurrent    | This channel provides the active current in Ampere.                                        | hwe-skt, hwe-kwh, hwe-bat         |
| current_l1             | Number:ElectricCurrent    | This channel provides the active current for phase 1 in Ampere.                            | hwe-p1, hwe-kwh                   |
| current_l2             | Number:ElectricCurrent    | This channel provides the active current for phase 2 in Ampere.                            | hwe-p1, hwe-kwh                   |
| current_l3             | Number:ElectricCurrent    | This channel provides the active current for phase 3 in Ampere.                            | hwe-p1, hwe-kwh                   |
| energy_import          | Number:Energy             | This channel provides the total energy usage meter reading in kWh.                         | hwe-p1, hwe-skt, hwe-kwh, hwe-bat |
| energy_import_t1       | Number:Energy             | This channel provides the energy usage meter reading for tariff 1 in kWh.                  | hwe-p1                            |
| energy_import_t2       | Number:Energy             | This channel provides the energy usage meter reading for tariff 2 in kWh.                  | hwe-p1                            |
| energy_export          | Number:Energy             | This channel provides the total energy feed-in meter reading in kWh.                       | hwe-p1, hwe-skt, hwe-kwh, hwe-bat |
| energy_export_t1       | Number:Energy             | This channel provides the energy feed-in meter reading for tarff 1 in kWh.                 | hwe-p1                            |
| energy_export_t2       | Number:Energy             | This channel provides the energy feed-in meter reading for tarff 2 in kWh.                 | hwe-p1                            |
| reactive_power         | Number                    | This channel provides the active reactive power in Volt-Ampere reactive.                   | hwe-p1, hwe-skt, hwe-kwh          |
| apparent_power         | Number                    | This channel provides the active apparent power in Volt-Ampere.                            | hwe-p1, hwe-skt, hwe-kwh          |
| power_factor           | Number:Dimensionless      | This channel provides the active power factor.                                             | hwe-p1, hwe-skt, hwe-kwh          |
| frequency              | Number:Frequency          | This channel provides the active frequency in Hertz.                                       | hwe-p1, hwe-skt, hwe-kwh, hwe-bat |
| total_gas              | Number:Volume             | This channel provides the most recently reported total imported gas in m^3.                | hwe-p1                            |
| gas_timestamp          | DateTime                  | This channel provides the time stamp of the total_gas measurement.                         | hwe-p1                            |
| power_failures         | Number                    | This channel provides the number of power failures detected by meter.                      | hwe-p1                            |
| long_power_failures    | Number                    | This channel provides the number of 'long' power failures detected by meter.               | hwe-p1                            |
| power_switch           | Switch                    | This channel provides access to the power switch of the Energy Socket.                     | hwe-skt                           |
| power_lock             | Switch                    | This channel provides access to the power lock of the Energy Socket.                       | hwe-skt                           |
| ring_brightness        | Number:Dimensionless      | This channel provides access to the brightness of the ring of the Energy Socket.           | hwe-skt                           |
| total_liter            | Number:Volume             | This channel provides total water usage in cubic meters.                                   | hwe-wtr                           |
| active_liter           | Number:VolumetricFlowRate | This channel provides the active water usage in liters per minute.                         | hwe-wtr                           |
| state_of_charge        | Number:Dimensionless      | This channel provides access to the current state of charge in percent.                    | hwe-bat                           |
| cycles                 | Number:Dimensionless      | This channel provides access to the number of battery cycles.                              | hwe-bat                           |

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
