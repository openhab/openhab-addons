# DSMR Binding
## Introduction
The DSMR-binding is targeted towards Dutch users having a smart meter (Dutch: 'Slimme meter'). Data of Dutch smart meters can be obtained via the P1-port. When connecting this port from a serial port the data can be read out.

This binding reads the P1-port of the Dutch Smart Meters that comply to NTA8130, DSMR v2.1, DSMR v2.2, DSMR v3.0, DSMR v4.0, DSMR v4.04, and DSMR 5.0.  
Although DSMR v4.2 is not an official specification, the binding has support for this version.

If you are not living in the Netherlands but do want to read a meter please have look at the [IEC-62056-21 Meter Binding](https://github.com/openhab/openhab1-addons/wiki/IEC-62056---21-Meter-Binding).

## Supported Things
### Bridge
- Dutch Smart Meter. This is the device that communicated between the binding (serial) and its internal meters. You always have to have a 'Dutch Smart Meter'-bridge.

#### Configuration
The configuration consists of the following parameters:
- `serialPort` (mandatory). The serial port where the P1-port is connected to (e.g. Linux: `/dev/ttyUSB1`, Windows: `COM2`)
- `serialPortSettings` (optional, default: &lt;empty&gt;). Serial Port parameters in the format `<speed> <nr of bits><parity [E(ven)/N(one)/O(dd)]><stop bits (1/1.5/2)>`.  
E.g. `115200 8N1` or `9600 E71`  
Setting a value will disable the serial port autodetection functionality of the binding, clearing the value will enable the serial port autodetection functionality again.  
**Note:** *This parameter is only needed if the DSMR-device requires non DSMR-standard Serial Port parameters (i.e. something different then `115200 8N1` or `9600 7E1`)*  
- `lenientMode` (optional, default: `false`). In lenient mode the binding will handle communication problems more silently and gracefully. If your experiencing lots of errors in the log and don't receive data, enabling lenient mode could help.  
This option is targeted for embedded platforms with limited CPU power.    
**Note:** *With lenientMode enabled receiving data is not guaranteed to be steady.*

#### Installation
PaperUI: Scan for devices on the DSMR binding or add the DSMR Bridge thing manually (you need to set the serialPort manual in this case)

Manual configuration:
The following configuration need to be added to a thing-configuration file. E.g. `things/dsmr.things`
```
Bridge dsmr:dsmrBridge:<id> [serialPort="<com port>"] {
    Things:
        * Thing configuration *
}
```
**Examples**
Default configuration:
```
Bridge dsmr:dsmrBridge:myDSMRDevice [serialPort="/dev/ttyUSB0"] {
    Things:
    ... thing configuration ...
}
```

Advanced configuration:
```
dsmr:dsmrBridge:myEmbDSMRDevice [serialPort="/dev/ttyUSB0", serialPortSettings="115200 8N1", lenientMode=true] {
    Things:
    ... thing configuration ...
}
```
### Meters
The information in this paragraph in necessary if you choose to configure the meters manually in a `.things` file.

Supported meters:  

| Meter Thing | Thing type ID | M-Bus channel | Refresh rate |
| ----------- | ------------- | ------------- | ------------ |
| DSMR V2 / V3 Device | `device_v2_v3` | -1 | 10 seconds |
| DSMR V4 Device | `device_v4` | -1 | 10 seconds |
| DSMR V5 Device | `device_v5` | -1 | 10 seconds |
| ACE4000 GTMM Mk3 Electricity meter | `electricity_ace4000` | 0 | 10 seconds |
| DSMR V2.1 Electricity meter | `electricity_v2_1` | 0 | 10 seconds |
| DSMR V2.2 Electricity meter | `electricity_v2_2` | 0 | 10 seconds |
| DSMR V3 Electricity meter | `electricity_v3_0` | 0 | 10 seconds |
| DSMR V4.0 Electricity meter | `electricity_v4_0` | 0 | 10 seconds |
| DSMR V4.0.4 Electricity meter | `electricity_v4_0_4` | 0 | 10 seconds |
| DSMR V4.2 Electricity meter | `electricity_v4_2` | 0 | 10 seconds |
| DSMR V5 Electricity meter | `electricity_v5_0` | 0 | 10 seconds |
| ACE4000 GTMM Mk3 Gas meter | `gas_ace4000` | 3 | 1 hour |
| DSMR V2.1 Gas meter | `gas_v2_1` | 0 | 24 hours |
| DSMR V2.2 Gas meter | `gas_v2_2` | 0 | 24 hours |
| DSMR V3.0 Gas meter | `gas_v3_0` | *note 1* | 1 hour |
| ACE4000 GTMM Mk3 Cooling meter | `cooling_ace4000` | 6 | 1 hour |
| DSMR V2.2 Cooling meter | `cooling_v2_2` | 0 | 1 hour |
| ACE4000 GTMM Mk3 Heating meter | `heating_ace4000` | 4 | 1 hour |
| DSMR V2.2 Heating meter | `heating_v2_2` | 0 | 1 hour |
| ACE4000 GTMM Mk3 Water meter | `water_ace4000` | 5 | 1 hour |
| DSMR V2.2 Water meter | `water_v2_2` | 0 | 1 hour |
| DSMR V3.0 Water meter | `water_v3_0` | *note 1* | 1 hour |
| ACE4000 GTMM Mk3 1st Slave Electricity meter | `slave_electricity1_ace4000` | 1 | 1 hour |
| ACE4000 GTMM Mk3 2nd Slave Electricity meter | `slave_electricity2_ace4000` | 2 | 1 hour |
| DSMR V4.x Slave Electricity meter | `slave_electricity_v4` | *note 1* | 1 hour |
| DSMR V5 Slave Electricity meter | `slave_electricity_v5` | *note 1* | 5 minutes |
| DSMR V3.0 Generic meter | `generic_v3_0` | *note 1* | 1 hour |
| DSMR V3.0 Giga Joule meter (heating or cooling) | `gj_v3_0` | *note 1* | 1 hour |
| DSMR V4.x Giga Joule meter (heating or cooling) | `gj_v4` | *note 1* | 1 hour |
| DSMR V5 Giga Joule meter (heating or cooling) | `gj_v5_0` | *note 1* | 5 minutes |
| DSMR V4.x m3 meter (gas or water) | `m3_v4` | *note 1* | 1 hour |
| DSMR V5 m3 meter (gas or water) | `m3_v5_0` | *note 1* | 5 minutes |

*note 1*. The channel of these meters is dependent on the physical installation and corresponds to the M-Bus channel. You can ask your supplier / installer for this information or you can retrieve it from the logfiles (see *Determine M-Bus channel*).

#### Configuration
PaperUI: Not needed. This is done automatically for you

Manual configuration:
The following configuration parameters are mandatory
- Thing type ID. See table above
- id. The id for this Thing
- Thing type ID in capitals. The Thing type ID in capitals
- M-Bus channel. See the table above

#### Installation
The following configuration must to be added to a thing-configuration file. E.g. `things/dsmr.things`
```
Bridge configuration {
    Things:
        <Thing type ID> <id> [channel=<M-Bus channel>] {
}
```
**Examples**
```
Bridge dsmr:dsmrBridge:myDSMRDevice [serialPort="/dev/ttyUSB0"] {
    Things:
        device_v5 dsmrDeviceV5 [channel=-1]
        electricity_v5_0 electricityV5 [channel=0]
}
```
### Channels
#### Item configuration
Paper UI. Item configuration can be done in the regular way.

Manual configuration:
The following channels are supported:
- Y channel is supported
- \- channel is not supported
- O channel is supported only if the device has this functionality


| Channel Type ID | Item Type | Description | Ace4000 | DSMR V2.1 | DSMR V2.2 | DSMR V3.0 | DSMR V4.0 | DSMR V4.0.4 | DSMR V4.2 | DSMR V5 |
| --------------- | --------- | ----------- | ------- | --------- | --------- | --------- | --------- | ----------- | --------- | ------- |
|  |  | **Channels for the generic device** |  |  |  |  |  |  |  |  |
| `p1_text_code` | String | Text code from the device | - | Y | Y | Y | Y | Y | Y | - |
| `p1_text_string` | String | Text string from the device | - | Y | Y | Y | Y | Y | Y | Y |
| `p1_version_output` | String | Version information (most times this refers to the DSMR specification) | - | - | - | - | Y | Y | Y | Y |
| `p1_timestamp` | DateTime| Timestamp of the last device reading | - | - | - | - | Y | Y | Y | Y |
|  |  | **Channels for the cooling meter** |  |  |  |  |  |  |  |  |
| `cmeter_value_v2` | Number | The total amount of cooling used in the past period (GJ) | Y | - | Y | - | - | - | - | - |
| `cmeter_value_v2_timestamp` | DateTime | Timestamp of the last meter reading | Y | - | Y | - | - | - | - | - |
| `cmeter_equipment_identifier_v2_2` | String | Equipment identifier | - | - | Y | - | - | - | - | - |
|  |  | **Channels for the main electricity meter** |  |  |  |  |  |  |  |  |
| `emeter_equipment_identifier_v2_x` | String | Electricity Equipment identifier | - | Y | Y | - | - | - | - | - |
| `emeter_equipment_identifier` | String | Electricity Equipment identifier | - | - | - | Y | Y | Y | Y | Y |
| `emeter_delivery_tariff0` | Number | Total amount of electricity used for tariff 0 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_delivery_tariff1` | Number | Total amount of electricity used for tariff 1 (kWh) | Y | Y | Y | Y | Y | Y | Y | Y |
| `emeter_delivery_tariff2` | Number | Total amount of electricity used for tariff 2 (kWh) | Y | Y | Y | Y | Y | Y | Y | Y |
| `emeter_production_tariff0` | Number | Total amount of electricity produced for tariff 0 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_production_tariff1` | Number | Total amount of electricity produced for tariff 1 (kWh)| Y | Y | Y | Y | Y | Y | Y | Y |
| `emeter_production_tariff2` | Number | Total amount of electricity produced for tariff 2 (kWh)| Y | Y | Y | Y | Y | Y | Y | Y |
| `emeter_delivery_tariff0_antifraud` | Number | Total amount of electricity used for tariff 2 [antifraud] (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_delivery_tariff1_antifraud` | Number | Total amount of electricity used for tariff 1 [antifraud] (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_delivery_tariff2_antifraud` | Number | Total amount of electricity used for tariff 2 [antifraud] (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_tariff_indicator` | String | Current tariff indicator | Y | Y | Y | Y | Y | Y | Y | Y |
| `emeter_treshold_a_v2_1` | Number | Actual treshold (A) | - | Y | - | - | - | - | - | - |
| `emeter_treshold_a` | Number | Actual treshold (A) | Y | - | Y | Y | - | - | - | - |
| `emeter_treshold_kwh` | Number | Actual treshold (kW) | - | - | - | - | Y | Y | - | - |
| `emeter_switch_position_v2_1` | Number | Switch position | - | Y | - | - | - | - | - | - |
| `emeter_switch_position` | Number | Switch position | Y | - | Y | Y | Y | Y | - | - |
| `emeter_active_import_power` | Number | Aggregate active import power (W) | Y | - | - | - | - | - | - | - | 
| `emeter_actual_delivery` | Number | Current power delivery (kW) | - | Y | Y | Y | Y | Y | Y | Y |
| `emeter_actual_production` | Number | Current power production (kW) | - | - | - | Y | Y | Y | Y | Y |
| `emeter_power_failures` | Number | Number of power failures | - | - | - | - | Y | Y | Y | Y |
| `emeter_long_power_failures` | Number | Number of long power failures | - | - | - | - | Y | Y | Y | Y |
| `emeter_power_failure_log_entries` | Number | Number of entries in the power failure log | - | - | - | - | Y | Y | Y | Y |
| `emeter_power_failure_log_timestamp[x]` *note 2* | Number | Number of entries in the power failure log | - | - | - | - | Y | Y | Y | Y |
| `emeter_power_failure_log_duration[x]` *note 2* | Number | Number of entries in the power failure log | - | - | - | - | Y | Y | Y | Y |
| `emeter_voltage_sags_l1` | Number | Number of voltage sags L1 | - | - | - | - | Y | Y | Y | Y |
| `emeter_voltage_sags_l2` | Number | Number of voltage sags L2 | - | - | - | - | O | O | O | O |
| `emeter_voltage_sags_l3` | Number | Number of voltage sags L3 | - | - | - | - | O | O | O | O |
| `emeter_voltage_swells_l1` | Number | Number of voltage swells L1 | - | - | - | - | Y | Y | Y | Y |
| `emeter_voltage_swells_l2` | Number | Number of voltage swells L2 | - | - | - | - | O | O | O | O |
| `emeter_voltage_swells_l3` | Number | Number of voltage swells L3 | - | - | - | - | O | O | O | O |
| `emeter_instant_current_l1` | Number | Instant Current L1 (A) | - | - | - | - | Y | Y | Y | Y |
| `emeter_instant_current_l2` | Number | Instant Current L2 (A) | - | - | - | - | O | O | O | O |
| `emeter_instant_current_l3` | Number | Instant Current L3 (A) | - | - | - | - | O | O | O | O |
| `emeter_instant_power_delivery_l1` | Number | Instant Power Delivery L1 (kW) | - | - | - | - | Y | Y | Y | Y |
| `emeter_instant_power_delivery_l2` | Number | Instant Power Delivery L2 (kW) | - | - | - | - | O | O | O | O |
| `emeter_instant_power_delivery_l3` | Number | Instant Power Delivery L3 (kW) | - | - | - | - | O | O | O | O |
| `emeter_instant_power_production_l1` | Number | Instant Power Production L1 (kW) | - | - | - | - | Y | Y | Y | Y |
| `emeter_instant_power_production_l2` | Number | Instant Power Production L2 (kW) | - | - | - | - | O | O | O | O |
| `emeter_instant_power_production_l3` | Number | Instant Power Production L3 (kW) | - | - | - | - | O | O | O | O |
| `emeter_instant_voltage_l1` | Number | Instant Voltage L1 (V) | - | - | - | - | - | - | - | Y |
| `emeter_instant_voltage_l2` | Number | Instant Voltage L2 (V) | - | - | - | - | - | - | - | O |
| `emeter_instant_voltage_l3` | Number | Instant Voltage L3 (V) | - | - | - | - | - | - | - | O |
|  |  | **Channels for the slave electricity meter** |  |  |  |  |  |  |  |  |
| `meter_device_type` | String | Slave Electricity Meter Device Type | - | - | - | - | Y | Y | Y | Y |
| `meter_equipment_identifier` | String | Slave Electricity Meter ID | - | - | - | - | Y | Y | Y | Y |
| `emeter_delivery_tariff0`  | Number | Total amount of slave electricity used for tariff 0 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_delivery_tariff1` | Number | Total amount of slave electricity used for tariff 1 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_delivery_tariff2` | Number | Total amount of slave electricity used for tariff 2 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_production_tariff0` | Number | Total amount of slave electricity produced for tariff 0 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_production_tariff1` | Number | Total amount of slave electricity produced for tariff 1 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_production_tariff2` | Number | Total amount of slave electricity produced for tariff 2 (kWh) | Y | - | - | - | - | - | - | - |
| `emeter_tariff_indicator` | String | Current slave tariff indicator | Y | - | - | - | - | - | - | - |
| `emeter_treshold_a` | Number | Actual slave treshold (A) | Y | - | - | - | - | - | - | - |
| `meter_switch_position` | Number | Slave electricity switch position | Y | - | - | Y | Y | Y | - | - |
| `emeter_active_import_power` | Number | Slave aggregate active import power (W) | Y | - | - | - | - | - | - | - | 
| `emeter_value` | Number | Slave electricity usage (kWh) in the past period | - | - | - | - | Y | Y | Y | Y |
| `emeter_value_timestamp` | DateTime | Timestamp of the last reading | - | - | - | - | Y | Y | Y | Y |
| `meter_device_type` | String | Gas Meter Device Type | - | - | - | Y | - | - | - | - |
| `meter_equipment_identifier` | String | Gas Meter ID | Y | - | - | Y | - | - | - | - |
|  |  | **Channels for the gas meter** |  |  |  |  |  |  |  |  |
| `gmeter_equipment_identifier_v2` | String | Gas Meter ID | - | Y | Y | - | - | - | - | - |
| `gmeter_24h_delivery_v2` | Number | Gas Delivery past 24 hours | Y | Y | Y | - | - | - | - | - |
| `gmeter_24h_delivery_v2_timestamp` | DateTime | Timestamp of the last reading | Y | Y | Y | - | - | - | - | - |
| `gmeter_24h_delivery_compensated_v2` | Number | Gas Delivery past 24 hours (compensated) | - | Y | Y | - | - | - | - | - |
| `gmeter_24h_delivery_compensated_v2_timestamp` | DateTime | Timestamp of the last reading | - | Y | Y | - | - | - | - | - |
| `gmeter_value_v3` | Number | Gas Delivery past period | - | - | - | Y | - | - | - | - |
| `gmeter_value_v3_timestamp` | DateTime | Timestamp of the last reading | - | - | - | Y | - | - | - | - |
| `gmeter_valve_position_v2_1` | Number | Gas Valve position | - | Y | - | - | - | - | - | - |
| `gmeter_valve_position_v2_2` | Number | Gas Valve position | Y | - | Y | - | - | - | - | - |
|  |  | **Channels for the generic meter** |  |  |  |  |  |  |  |  |
| `meter_valve_switch_position` | Number | Gas Valve position | - | - | - | Y | - | - | - | - |
| `meter_device_type` | String | Generic Meter Device Type | - | - | - | Y | - | - | - | - |
| `gmeter_equipment_identifier` | String | Generic Meter ID | - | - | - | Y | - | - | - | - |
| `genmeter_value_v3` | Number | Delivery past period | - | - | - | Y | - | - | - | - |
| `meter_valve_switch_position` | Number | Generic Meter Valve/Switch position | - | - | - | Y | - | - | - | - |
|  |  | **Channels for the GJ meter (Heating or Cooling)** |  |  |  |  |  |  |  |  |
| `meter_device_type` | String | GJ Meter Device Type | - | - | - | Y | Y | Y | Y | Y |
| `meter_equipment_identifier` | Number | GJ Meter ID | - | - | - | Y | Y | Y | Y | Y |
| `gjmeter_value_v3` | Number | GJ Delivery past period | - | - | - | Y | - | - | - | - |
| `gjmeter_value_v3_timestamp` | DateTime | Timestamp of the last reading | - | - | - | Y | - | - | - | - |
| `gjmeter_value_v4` | Number | GJ Delivery past period | - | - | - | - | Y | Y | Y | Y |
| `gjmeter_value_v4_timestamp` | DateTime | Timestamp of the last reading | - | - | - | - | Y | Y | Y | Y |
|  |  | **Channels for the heating meter** |  |  |  |  |  |  |  |  |
| `meter_valve_switch_position` | Number | GJ Meter Valve position | - | - | - | Y | Y | Y | Y | - |
| `meter_equipment_identifier` | String | Heating Meter ID | Y | - | - | - | - | - | - | - |
| `hmeter_equipment_identifier_v2_2` | String | Heating Meter ID | - | - | Y | - | - | - | - | - |
| `hmeter_value_v2` | Number | Heating Delivery past period | Y | - | Y | - | - | - | - | - |
| `hmeter_value_v2_timestamp` | DateTime | Timestamp of the last reading | Y | - | Y | - | - | - | - | - |
|  |  | **Channels for the m3 meter (Gas or Water)** |  |  |  |  |  |  |  |  |
| `meter_device_type` | String | m3 Meter Device Type | - | - | - | - | Y | Y | Y | Y |
| `meter_equipment_identifier` | String | m3 Meter ID | - | - | - | - | Y | Y | Y | Y |
| `m3meter_value` | Number | m3 Delivery past period | - | - | - | - | Y | Y | Y | Y |
| `meter_valve_switch_position` | Number | m3 Meter Valve position | - | - | - | - | Y | Y | Y | - |
|  |  | **Channels for the water meter** |  |  |  |  |  |  |  |  |
| `meter_device_type` | String | Water Meter Device Type | - | - | - | Y | - | - | - | - |
| `meter_equipment_identifier` | String | Water Meter ID | Y | - | - | Y | - | - | - | - |
| `wmeter_equipment_identifier_v2_2` | String | Water Meter ID | - | - | Y | - | - | - | - | - |
| `wmeter_value_v2` | Number | Water Delivery past period | Y | - | Y | - | - | - | - | - |
| `wmeter_value_v2_timestamp` | DateTime | Timestamp of the last reading | Y | - | Y | - | - | - | - | - |
| `wmeter_value_v3` | Number | Water Delivery past period | - | - | - | Y | - | - | - | - |
| `meter_valve_switch_position` | Number | Water Meter Valve position | - | - | - | Y | - | - | - | - |

*note 2*. The power failure log has a dynamic number of entries starting at `0`. 
So `emeter_power_failure_log_timestamp0`, `emeter_power_failure_log_duration0` refers to the first entry, 
`emeter_power_failure_log_timestamp1`, `emeter_power_failure_log_duration1` refers to the second entry, etc.

Channel identifier: `dsmr:<ThingTypeID>:<bridge id>:<id>:<channel type id>`
- ThingTypeID. See table with supported meters
- BridgeID. The configured id for the bridge
- id. The configured id for the ThingType you want to address
- channel type id. The channel type id

#### Installation
The following configuration must to be added to a item configuration file. E.g. `things/dsmr.items`
```
ItemType <name> "<description>" (<Group>) {channel="<Channel identifier>"}
```
**Examples**
```
Number MeterDeliveryTariff0 "Total electricity delivered to the resident during low tariff period [%.3f kWh]" {channel="dsmr:electricity_v5_0:mysmartmeter:electricityV5:emeter_delivery_tariff1}
```

## Full configuration example
`things/dsmr.things`
```
Bridge dsmr:dsmrBridge:mysmartmeter [serialPort="/dev/ttyUSB0"] {
    Things:
        device_v5 dsmrV5Device [channel=-1]
        electricity_v5_0 electricityV5 [channel=0]
}
```
`things/dsmr.items`
```
String P1Version "P1 Version output" {channel="dsmr:device_v5:mysmartmeter:dsmrV5Device:p1_version_output"}
Number MeterDeliveryTariff0 "Total electricity delivered to the resident during low tariff period [%.3f kWh]" {channel="dsmr:device_v5:mysmartmeter:electricityV5:emeter_delivery_tariff1}
Number MeterDeliveryTariff1 "Total electricity delivered to the resident during high tariff period [%.3f kWh]" {channel="dsmr:device_v5:mysmartmeter:electricityV5:emeter_delivery_tariff2}
```

## Determine M-Bus channel
Since autodetecting meters is always active, you can use the logging to find out a M-Bus channel.  
Look for the following logfile line:  
`<Timestamp> [INFO ] [enhab.binding.dsmr.device.DSMRDevice] - Detected the following new meters: [Meter type: M3_V5_0, channel: 1, Meter type: ELECTRICITY_V5, channel: 0]`

Here you find the ThingTypeID (it is stated only in capitals) and the M-Bus channel. The above example would lead to the following Thing definition
```
Bridge definition {
    Things:
        m3_v5_0 mygasmeter [channel=1]
        electricity_v5 [channel=0]
}
```
