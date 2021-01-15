# HomeWizard Binding

The HomeWizard binding retrieves measurements from the HomeWizard Wi-Fi P1 meter.
The meter itself is attached to a DSMR Smart Meter and reads out the telegrams, which 
it will forward to cloud storage. However, recently HomeWizard also added an interface 
that can be queried locally.

This binding uses that local interface to make the measurements available.

## Supported Things

The binding provides the P1 Meter thing.

## Discovery

Auto discovery is not available for this binding.

## Thing Configuration

The P1 Meter thing can be configured through the web interface.

`Network Address (required)`

This should specify the IP address / host where the meter can be found.

`Refresh Interval`

This specifies the interval in seconds used by the binding to read updated values from the meter.

Note that update rate of the P1 Meter itself depends on the telegrams it receives from the Smart Meter.

## Channels

| Channel ID            | Item Type     | Description                                                                                |
|-----------------------|---------------|--------------------------------------------------------------------------------------------|
| total_power_import_t1 | Number:Power  | The most recently reported total imported power in KWh by counter 1                        |
| total_power_import_t2 | Number:Power  | The most recently reported total imported power in KWh by counter 2                        |
| total_power_export_t1 | Number:Power  | The most recently reported total exported power in KWh by counter 1                        |
| total_power_export_t2 | Number:Power  | The most recently reported total exported power in KWh by counter 2                        |
| active_power          | Number:Power  | The current net total power in W. It will be below 0 if power is currently being exported. |
| active_power_l1       | Number:Power  | The current net total power in W for phase 1.                                              |
| active_power_l2       | Number:Power  | The current net total power in W for phase 2.                                              |
| active_power_l3       | Number:Power  | The current net total power in W for phase 3.                                              |
| total_gas             | Number:Volume | The most recently reported total imported gas in m^3.                                      |
| gas_timestamp         | Number        | The time stamp of the total_gas measurement.                                               |
