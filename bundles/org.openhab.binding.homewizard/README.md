# HomeWizard Binding

The HomeWizard binding provides access to several HomeWizard devices by using the local API of those devices.

## Installation

There are two important points of attention: the local API of each device must be enabled and a fixed address must be configured for the devices.

### Local API

The local API of a device can be enabled from the HomeWizard app.
Go to Settings in the app, then Meters and select the device you want to enable.
On this page enable the local API.

### Fixed Address

The devices support mDNS discovery but the binding does not support that yet.
As a result the devices should be reachable through a hostname or a fixed IP address.
Since the devices themselves have no option to set a fixed IP address you will need a different solution, for instance having your router hand out an IP address based upon the MAC address of the devices.

## Supported Things

The binding offers three Things, providing support for the P1 meter, the Watermeter and the Energy Socket.

| Thing         | Device              | Description                                                                                       |
|---------------|---------------------|---------------------------------------------------------------------------------------------------|
| p1_wifi_meter | Wi-Fi P1 Meter      | Reads total and current energy usage and total gas usage.                                         |
| energy_socket | Wi-Fi Energy Socket | Reads total and current energy usage. Controls power switch, lock and ring brightness.            |
| watermeter    | Wi-Fi Watermeter    | Reads total and current water usage.                                                              |

The HomeWizard kWh meters are not yet officially supported, but they can probably be added as as 'p1_wifi_meter'. However, this has not been tested.

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

| Channel ID             | Item Type                 | Description                                                                                | Available |
|------------------------|---------------------------|--------------------------------------------------------------------------------------------|-----------|
| active_current         | Number:ElectricCurrent    | The combined current in A vor all phases                                                   | P,E       |
| active_current_l1      | Number:ElectricCurrent    | The active current in A for phase 1.                                                       | P         |
| active_current_l2      | Number:ElectricCurrent    | The active current in A for phase 2.                                                       | P         |
| active_current_l3      | Number:ElectricCurrent    | The active current in A for phase 3.                                                       | P         |
| active_power           | Number:Power              | The current net total power in W. It will be below 0 if power is currently being exported. | P,E       |
| active_power_l1        | Number:Power              | The current net total power in W for phase 1.                                              | P         |
| active_power_l2        | Number:Power              | The current net total power in W for phase 2.                                              | P         |
| active_power_l3        | Number:Power              | The current net total power in W for phase 3.                                              | P         |
| active_voltage         | Number:ElectricPotential  | The active voltage in V                                                                    | P         |
| active_voltage_l1      | Number:ElectricPotential  | The active voltage in V for phase 1.                                                       | P         |
| active_voltage_l2      | Number:ElectricPotential  | The active voltage in V for phase 2.                                                       | P         |
| active_voltage_l3      | Number:ElectricPotential  | The active voltage in V for phase 3.                                                       | P         |
| total_energy_import_t1 | Number:Energy             | The most recently reported total imported energy in kWh by counter 1.                      | P,E       |
| total_energy_import_t2 | Number:Energy             | The most recently reported total imported energy in kWh by counter 2.                      | P         |
| total_energy_export_t1 | Number:Energy             | The most recently reported total exported energy in kWh by counter 1.                      | P,E       |
| total_energy_export_t2 | Number:Energy             | The most recently reported total exported energy in kWh by counter 2.                      | P         |
| total_gas              | Number:Volume             | The most recently reported total imported gas in m^3.                                      | P         |
| gas_timestamp          | DateTime                  | The time stamp of the total_gas measurement.                                               | P         |
| total_water            | Number:Volume             | Total water used.                                                                          | W         |
| current_water          | Number:VolumetricFlowRate | Current water usage.                                                                       | W         |
| power_failures         | Number                    | The count of long power failures.                                                          | P         |
| long_power_failures    | Number                    | the count of any power failures.                                                           | P         |
| power_switch           | Switch                    | Controls the power switch of the socket.                                                   | E         |
| power_lock             | Switch                    | Controls the lock of the power switch (un/locking both the API and the physical button)    | E         |
| ring_brightness        | Number:Dimensionless      | Controls the brightness of the ring on the socket                                          | E         |

## Full Example

### `homewizard.things` Example

```java
Thing homewizard:p1_wifi_meter:my_p1 [ ipAddress="192.178.1.67", refreshDelay=5 ]
Thing homewizard:energy_socket:my_socket [ ipAddress="192.178.1.61", refreshDelay=5 ]
Thing homewizard:watermeter:my_water [ ipAddress="192.178.1.27", refreshDelay=15 ]
```

### `homewizard.items` Example

```java
Number:Energy Energy_Import_T1 "Imported Energy T1 [%.0f kWh]" {channel="homewizard:p1_wifi_meter:my_meter:total_energy_import_t1" }
Number:Power  Active_Power_L1  "Active Power Phase 1 [%.1f W]" {channel="homewizard:p1_wifi_meter:my_meter:active_power_l1" }
DateTime      Gas_Update       "Gas Update Time [%1$tH:%1$tM]" {channel="homewizard:p1_wifi_meter:my_meter:gas_timestamp" }
```
