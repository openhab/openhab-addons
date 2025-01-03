# HomeWizard Binding

The HomeWizard binding provides access to several HomeWizard devices by using the local API of those devices.

## Installation

There are two important points of attention: the local API of each device must be enabled and a fixed address must be configured for the devices.

### Local API v1

The local API of a device can be enabled from the HomeWizard app.
Go to Settings in the app, then Meters and select the device you want to enable.
On this page enable the local API.

### Local API v2

This version is still in beta and is not yet supported by this add-on.

### Fixed Address

The devices support mDNS discovery but the binding does not support that yet.
As a result the devices should be reachable through a hostname or a fixed IP address.
Since the devices themselves have no option to set a fixed IP address you will need a different solution, for instance having your router hand out an IP address based upon the MAC address of the devices.

## Supported Things

The binding offers Things, providing support for the P1 meter, the kWh meters (1-phase and 3-phase), the Watermeter and the Energy Socket.

| Thing         | Device              | Description                                                                                         |
|---------------|---------------------|-----------------------------------------------------------------------------------------------------|
| HWE-P1        | P1 Meter            | Reads total and current energy usage and total gas usage.                                           |
| HWE-SKT       | Energy Socket       | Reads total and current energy usage. Controls power switch, lock and ring brightness.              |
| HWE-WTR       | Watermeter          | Reads total and current water usage.                                                                |
| HWE-KWH       | kWh Meter           | Reads total and current energy usage.                                                               |
| p1_wifi_meter | Wi-Fi P1 Meter      | [Deprecated] Reads total and current energy usage and total gas usage.                              |
| energy_socket | Wi-Fi Energy Socket | [Deprecated] Reads total and current energy usage. Controls power switch, lock and ring brightness. |
| watermeter    | Wi-Fi Watermeter    | [Deprecated] Reads total and current water usage.                                                   |



## Discovery

Auto discovery is not yet available for this binding.

## Thing Configuration

All devices can be configured through the web interface.

| Parameter    | Required | Default | Description                                                                                       |
|--------------|----------|---------|---------------------------------------------------------------------------------------------------|
| ipAddress    | *        |         | This specifies the IP address (or host name) where the meter can be found.                        |
| refreshDelay |          | 5       | This specifies the interval in seconds used by the binding to read updated values from the meter. |

Note that update rate of the P1 Meter itself depends on the frequency of the telegrams it receives from the Smart Meter.
For DSMR5 meters this is generally once per second, for older versions the frequency is much lower.

## Channels

| Channel ID             | Item Type                 | Description                                                                                | Available                |
|------------------------|---------------------------|--------------------------------------------------------------------------------------------|--------------------------|
| power                  | Number:Power              | This channel provides the active usage in Watt.                                            | HWE-P1, HWE-SKT, HWE-KWH |
| power_l1               | Number:Power              | This channel provides the active usage for phase 1 in Watt.                                | HWE-P1, HWE-KWH          |
| power_l2               | Number:Power              | This channel provides the active usage for phase 2 in Watt.                                | HWE-P1, HWE-KWH          |
| power_l3               | Number:Power              | This channel provides the active usage for phase 3 in Watt.                                | HWE-P1, HWE-KWH          |
| voltage                | Number:ElectricPotential  | This channel provides the active voltage in Volt.                                          | HWE-SKT                  |
| voltage_l1             | Number:ElectricPotential  | This channel provides the active usage for phase 1 in Watt.                                | HWE-P1, HWE-KWH          |
| voltage_l2             | Number:ElectricPotential  | This channel provides the active usage for phase 2 in Watt.                                | HWE-P1, HWE-KWH          |
| voltage_l3             | Number:ElectricPotential  | This channel provides the active usage for phase 3 in Watt.                                | HWE-P1, HWE-KWH          |
| current                | Number:ElectricCurrent    | This channel provides the active current in Ampere.                                        | HWE-SKT, HWE-KWH         |
| current_l1             | Number:ElectricCurrent    | This channel provides the active current for phase 1 in Ampere.                            | HWE-P1, HWE-KWH          |
| current_l2             | Number:ElectricCurrent    | This channel provides the active current for phase 2 in Ampere.                            | HWE-P1, HWE-KWH          |
| current_l3             | Number:ElectricCurrent    | This channel provides the active current for phase 3 in Ampere.                            | HWE-P1, HWE-KWH          |
| energy_import          | Number:Energy             | This channel provides the total energy usage meter reading in kWh.                         | HWE-P1, HWE-SKT, HWE-KWH |
| energy_import_t1       | Number:Energy             | This channel provides the energy usage meter reading for tariff 1 in kWh.                  | HWE-P1                   |
| energy_import_t2       | Number:Energy             | This channel provides the energy usage meter reading for tariff 2 in kWh.                  | HWE-P1                   |
| energy_export          | Number:Energy             | This channel provides the total energy feed-in meter reading in kWh.                       | HWE-P1, HWE-SKT, HWE-KWH |
| energy_export_t1       | Number:Energy             | This channel provides the energy feed-in meter reading for tarff 1 in kWh.                 | HWE-P1                   |
| energy_export_t2       | Number:Energy             | This channel provides the energy feed-in meter reading for tarff 2 in kWh.                 | HWE-P1                   |
| reactive_power         | Number                    | This channel provides the active reactive power in Volt-Ampere reactive.                                            | HWE-P1, HWE-SKT, HWE-KWH |
| apparent_power         | Number                    | This channel provides the active apparent power in Volt-Ampere.                                            | HWE-P1, HWE-SKT, HWE-KWH |
| power_factor           | Number:Dimensionless      | This channel provides the active power factor.                                            | HWE-P1, HWE-SKT, HWE-KWH |
| frequency              | Number:Frequency          | This channel provides the active frequency in Hertz.                                          | HWE-P1, HWE-SKT, HWE-KWH |
| total_gas              | Number:Volume             | This channel provides the most recently reported total imported gas in m^3.                | HWE-P1                   |
| gas_timestamp          | DateTime                  | This channel provides the time stamp of the total_gas measurement.                         | HWE-P1                   |
| power_failures         | Number                    | This channel provides the number of power failures detected by meter.                      | HWE-P1                   |
| long_power_failures    | Number                    | This channel provides the number of 'long' power failures detected by meter.               | HWE-P1                   |
| power_switch           | Switch                    | This channel provides access to the power switch of the Energy Socket.                     | HWE-SKT                  |
| power_lock             | Switch                    | This channel provides access to the power lock of the Energy Socket.                       | HWE-SKT                  |
| ring_brightness        | Number:Dimensionless      | This channel provides access to the brightness of the ring of the Energy Socket.           | HWE-SKT                  |
| total_liter            | Number:Volume             | This channel provides total water usage in cubic meters.                                   | HWE-WTR                  |
| active_liter           | Number:VolumetricFlowRate | This channel provides the active water usage in liters per minute.                         | HWE-WTR                  |

## Full Example

### `homewizard.things` Example

```java
Thing homewizard:HWE-P1:my_p1 [ ipAddress="192.178.1.67", refreshDelay=5, apiVersion=1 ]
Thing homewizard:HWE-SKT:my_socket [ ipAddress="192.178.1.61", refreshDelay=5, apiVersion=1 ]
Thing homewizard:HWE-WTR:my_water [ ipAddress="192.178.1.27", refreshDelay=15, apiVersion=1 ]
```

### `homewizard.items` Example

```java
Number:Energy Energy_Import_T1 "Imported Energy T1 [%.0f kWh]" {channel="homewizard:HWE-P1:my_meter:energy#energy_import_t1" }
Number:Power  Active_Power_L1  "Active Power Phase 1 [%.1f W]" {channel="homewizard:HWE-P1:my_meter:energy#power_l1" }
DateTime      Gas_Update       "Gas Update Time [%1$tH:%1$tM]" {channel="homewizard:HWE-P1:my_meter:energy#gas_timestamp" }
```
