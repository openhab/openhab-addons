# Modbus Saia Burgess Controls Binding

This binding interfaces the energy meter series ALD1 by Saia Burgess Controls (SBC) via Modbus.

## Supported Things

The following Things are supported:

- `ald1Unidirectional`: 1-phase 32A one-way energy meter ALD1D5FD00A3A00
- `ald1Bidirectional`:  1-phase 32A two-way energy meter ALD1B5FD00A3A00

## Discovery

This binding does not support discovery.

## Thing Configuration

The following configuration parameter applys to `ald1Unidirectional` and `ald1Bidirectional`.

| Name          | Description                              | Type    | Required |
|---------------|------------------------------------------|---------|----------|
| pollInterval  | Time between polling the data in ms      | Integer | yes      |

The Thing needs a Modbus serial slave Bridge to operate.

One of the following serial settings need to be configured in the Bridge:

- 9600 baud, 2 stop bit, no parity
- 9600 baud, 1 stop bit, even parity
- 9600 baud, 1 stop bit, odd parity

## Channels

The following Channels apply to `ald1Unidirectional` and `ald1Bidirectional` if not stated otherwise.

| Name                | Type                     | Description                                                   |
|---------------------|--------------------------|---------------------------------------------------------------|
| total_energy        | Number:Energy            | Energy Total                                                  |
| partial_energy      | Number:Energy            | Energy Counter Resettable (only unidirectional meter)         |
| feeding_back_energy | Number:Energy            | Energy Feeding Back (only bidirectional meter)                |
| voltage             | Number:ElectricPotential | Effective Voltage                                             |
| current             | Number:ElectricCurrent   | Effective Current                                             |
| active_power        | Number:Power             | Effective Active Power (negative numbers mean feeding back)   |
| reactive_power      | Number:Power             | Effective Reactive Power (negative numbers mean feeding back) |
| power_factor        | Number:Dimensionless     | Power Factor                                                  |

## Full Example

### .items

```java
Number:Energy ALD1_Total_Energy "[%.2f %unit%]"        {channel="modbus:ald1Bidirectional:8b6e85623b:total_energy"}
Number:Energy ALD1_Feeding_Back_Energy "[%.2f %unit%]" {channel="modbus:ald1Bidirectional:8b6e85623b:feeding_back_energy"}
Number:ElectricPotential ALD1_Voltage "[%d %unit%]"    {channel="modbus:ald1Bidirectional:8b6e85623b:voltage"}
Number:ElectricCurrent ALD1_Current "[%.1f %unit%]"    {channel="modbus:ald1Bidirectional:8b6e85623b:current"}
Number:Power ALD1_Active_Power "[%.2f %unit%]"         {channel="modbus:ald1Bidirectional:8b6e85623b:active_power"}
Number:Power ALD1_Reactive_Power "[%.2f %unit%]"       {channel="modbus:ald1Bidirectional:8b6e85623b:reactive_power"}
Number:Dimensionless ALD1_Power_Factor "[%.2f]"        {channel="modbus:ald1Bidirectional:8b6e85623b:power_factor"}
```

### .sitemap

```perl
sitemap ald1 label="ALD1 Energy Meter"
{
    Default item=ALD1_Total_Energy label="Total Energy"
    Default item=ALD1_Feeding_Back_Energy label="Feeding Back Energy"
    Default item=ALD1_Voltage label="Voltage"
    Default item=ALD1_Current label="Current"
    Default item=ALD1_Active_Power label="Active Power"
    Default item=ALD1_Reactive_Power label="Reactive Power"
    Default item=ALD1_Power_Factor label="Power Factor"
}
```
