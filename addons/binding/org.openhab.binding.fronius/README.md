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
| day_energy          | Number    | Wh                           | Symo, Symo Hybrid  |
| year_energy         | Number    | Wh                           | Symo, Symo Hybrid  |
| total_energy        | Number    | Wh                           | Symo, Symo Hybrid  |
| pac                 | Number    | W - actual output power      | Symo, Symo Hybrid  |
| iac                 | Number    | A - actual output current    | Symo, Symo Hybrid  |
| uac                 | Number    | V - actual output voltage    | Symo, Symo Hybrid  |
| fac                 | Number    | Hz - actual output frequency | Symo, Symo Hybrid  |
| idc                 | Number    | A - acutal input current     | Symo, Symo Hybrid  |
| udc                 | Number    | V - acutal input voltage     | Symo, Symo Hybrid  |
| status_code         | Number    | Device state                 | Symo, Symo Hybrid  |
| timestamp           | String    | Date & Time                  | Symo, Symo Hybrid  |
| storage_current     | Number    | A - storage current          | Symo Hybrid        |
| storage_voltage     | Number    | V - storage voltage          | Symo Hybrid        |
| storage_charge      | Number    | % - storage charge state     | Symo Hybrid        |
| storage_capacity    | Number    | Wh - storage capacity        | Symo Hybrid        |
| storage_temperature | Number    | storage temperature          | Symo Hybrid        |
| storage_code        | Number    | Device state.                | Symo Hybrid        |
| storage_timestamp   | String    | Date & Time                  | Symo Hybrid        |


## Full Example

demo.things:

```
fronius:symo_hybrid:hybrid_inverter [ hostname="my.hybrid.inverter" ]
fronius:symo:inverter_1 [ hostname="my.inverter", device=1 ]
fronius:symo:inverter_2 [ hostname="my.inverter", device=2 ]
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
