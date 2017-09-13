# Fronius Binding

This binding allows openHAB to gather data from Fronius inverters.
Currently Fronius Symo and Fronious Symo Hybrid are supported via the Solar API v1. 

## Supported Things

The two different kind of Fronius inverters can be configured as ```symo``` and ```symo_hybrid```


## Thing Configuration

Thin configuration is quite simple:

A hostname ist required for each thing.
If there are more than one Fronius devices are available with this hostname, the device can be selected.

## Channels

| Channel Type ID     | Item Type | Description                  | Available on thing |
|---------------------|-----------|------------------------------|--------------------|
| day_energy          | Number    | Wh                           | realtime_data      |
| year_energy         | Number    | Wh                           | realtime_data      |
| total_energy        | Number    | Wh                           | realtime_data      |
| pac                 | Number    | W - actual output power      | realtime_data      |
| iac                 | Number    | A - actual output current    | realtime_data      |
| uac                 | Number    | V - actual output voltage    | realtime_data      |
| fac                 | Number    | Hz - actual output frequency | realtime_data      |
| idc                 | Number    | A - acutal input current     | realtime_data      |
| udc                 | Number    | V - acutal input voltage     | realtime_data      |
| status_code         | Number    | Device state                 | all                |
| timestamp           | String    | Date & Time                  | all                |
| current             | Number    | A - storage current          | storage_data       |
| voltage             | Number    | V - storage voltage          | storage_data       |
| charge              | Number    | % - storage charge state     | storage_data       |
| capacity            | Number    | Wh - storage capacity        | storage_data       |
| temperature         | Number    | storage temperature          | storage_data       |
| code                | Number    | Device state                 | storage_data       |
| timestamp           | String    | Date & Time                  | storage_data       |


## Full Example

demo.things:

```
Bridge fronius:symo:speicer [ hostname="my.fronius.device" ] {
    Thing device_info info
    Thing inverter_data inverter [ device=1 ]
    Thing storage_data storage [ device=0 ]
}
```

demo.items:

```
Number FSH_UDC { channel="fronius:symo_hybrid:hybrid_inverter:udc" }
Number FSH_IDC { channel="fronius:symo_hybrid:hybrid_inverter:idc" }
Number SFSH_PAC { channel="fronius:symo_hybrid:hybrid_inverter:pac" }
Number FSH_UAC { channel="fronius:symo_hybrid:hybrid_inverter:uac" }
Number FSH_IAC { channel="fronius:symo_hybrid:hybrid_inverter:iac" }
Number FSH_FAC { channel="fronius:symo_hybrid:hybrid_inverter:fac" }
Number FSH_DAY_ENERGY { channel="fronius:symo_hybrid:hybrid_inverter:day_energy" }
Number FSH_YEAR_ENERGY { channel="fronius:symo_hybrid:hybrid_inverter:year_energy" }
Number FSH_TOTAL_ENERGY { channel="fronius:symo_hybrid:hybrid_inverter:total_energy" }
Number FSH_CODE { channel="fronius:symo_hybrid:hybrid_inverter:status_code" }
String FSH_TIMESTAMP { channel="fronius:symo_hybrid:hybrid_inverter:timestamp" }
Number FSH_Storage_Current { channel="fronius:symo_hybrid:hybrid_inverter:storage_current" }
Number FSH_Storage_Voltage { channel="fronius:symo_hybrid:hybrid_inverter:storage_voltage" }
Number FSH_Storage_Charge { channel="fronius:symo_hybrid:hybrid_inverter:storage_charge" }
Number FSH_Storage_Capacity { channel="fronius:symo_hybrid:hybrid_inverter:storage_capacity" }
Number FSH_Storage_Temperature { channel="fronius:symo_hybrid:hybrid_inverter:storage_temperature" }
Number FSH_Storage_CODE { channel="fronius:symo_hybrid:hybrid_inverter:storage_code" }
String FSH_Storage_TIMESTAMP { channel="fronius:symo_hybrid:hybrid_inverter:storage_timestamp" }
```
