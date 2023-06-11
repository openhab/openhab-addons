# HomeWizard Binding

The HomeWizard binding retrieves measurements from the HomeWizard Wi-Fi P1 meter.
The meter itself is attached to a DSMR Smart Meter and reads out the telegrams, which it will forward to cloud storage.
However, recently HomeWizard also added an interface that can be queried locally.

This binding uses that local interface to make the measurements available.

## Supported Things

The binding provides the P1 Meter thing.

## Discovery

Auto discovery is not available for this binding.

## Thing Configuration

The P1 Meter thing can be configured through the web interface.

| Parameter    | Required | Default | Description                                                                                       |
|--------------|----------|---------|---------------------------------------------------------------------------------------------------|
| ipAddress    | *        |         | This specifies the IP address (or host name) where the meter can be found.                        |
| refreshDelay |          | 5       | This specifies the interval in seconds used by the binding to read updated values from the meter. |

Note that update rate of the P1 Meter itself depends on the frequency of the telegrams it receives from the Smart Meter.
For DSMR5 meters this is generally once per second, for older versions the frequency is much lower.

Example of configuration through a .thing file:

```java
Thing homewizard:p1_wifi_meter:my_meter [ ipAddress="192.178.1.67", refreshDelay=5 ]
```

## Channels

| Channel ID             | Item Type     | Description                                                                                |
|------------------------|---------------|--------------------------------------------------------------------------------------------|
| total_energy_import_t1 | Number:Energy | The most recently reported total imported energy in kWh by counter 1.                      |
| total_energy_import_t2 | Number:Energy | The most recently reported total imported energy in kWh by counter 2.                      |
| total_energy_export_t1 | Number:Energy | The most recently reported total exported energy in kWh by counter 1.                      |
| total_energy_export_t2 | Number:Energy | The most recently reported total exported energy in kWh by counter 2.                      |
| active_power           | Number:Power  | The current net total power in W. It will be below 0 if power is currently being exported. |
| active_power_l1        | Number:Power  | The current net total power in W for phase 1.                                              |
| active_power_l2        | Number:Power  | The current net total power in W for phase 2.                                              |
| active_power_l3        | Number:Power  | The current net total power in W for phase 3.                                              |
| total_gas              | Number:Volume | The most recently reported total imported gas in m^3.                                      |
| gas_timestamp          | DateTime      | The time stamp of the total_gas measurement.                                               |

Example of configuration through a .items file:

```java
Number:Energy Energy_Import_T1 "Imported Energy T1 [%.0f kWh]" {channel="homewizard:p1_wifi_meter:my_meter:total_energy_import_t1" }
Number:Power  Active_Power_L1  "Active Power Phase 1 [%.1f W]" {channel="homewizard:p1_wifi_meter:my_meter:active_power_l1" }
DateTime      Gas_Update       "Gas Update Time [%1$tH:%1$tM]" {channel="homewizard:p1_wifi_meter:my_meter:gas_timestamp" }
```
