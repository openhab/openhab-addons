# Iammeter Binding

[Iammeter](https://www.iammeter.com) provides real-time readings of single-phase (WEM3080, WEM3162) and three-phase (WEM3080T) meters from IAMMETER over Wi-Fi.

## Use of the binding

The Iammeter is exposed as one thing with a number of channels that can be used to read the values for different aspects of your Iammeter devices.

## Setup of the binding

You can add the Iammeter device via the openHAB UI manually.

## Available channels

The following table is taken from the official manual and contains all available channels.

Single-phase energy meter (WEM3080/WEM3162)
| Name           | Unit | Description                  | Type                     |
|----------------|------|------------------------------|--------------------------|
| voltage_a      | V    | Voltage                      | Number:ElectricPotential |
| current_a      | A    | Current                      | Number:ElectricCurrent   |
| power_a        | W    | Active power                 | Number:Power             |
| importenergy_a | kWh  | Energy consumption from gird | Number:Energy            |
| exportgrid_a   | kWh  | Energy export to grid        | Number:Energy            |

Three-phase energy meter (WEM3080T)
| Name           | Unit | Description           | Type                     |
|----------------|------|-----------------------|--------------------------|
| voltage_a      | V    | A phase voltage       | Number:ElectricPotential |
| current_a      | A    | A phase current       | Number:ElectricCurrent   |
| power_a        | W    | A phase active power  | Number:Power             |
| importenergy_a | kWh  | A phase import energy | Number:Energy            |
| exportgrid_a   | kWh  | A phase export energy | Number:Energy            |
| frequency_a    | kWh  | A phase frequency     | Number:Frequency         |
| pf_a           | kWh  | A phase power factor  | Number                   |
| voltage_b      | V    | B phase voltage       | Number:ElectricPotential |
| current_b      | A    | B phase current       | Number:ElectricCurrent   |
| power_b        | W    | B phase active power  | Number:Power             |
| importenergy_b | kWh  | B phase import energy | Number:Energy            |
| exportgrid_b   | kWh  | B phase export energy | Number:Energy            |
| frequency_b    | kWh  | B phase frequency     | Number:Frequency         |
| pf_b           | kWh  | B phase power factor  | Number                   |
| voltage_c      | V    | C phase voltage       | Number:ElectricPotential |
| current_c      | A    | C phase current       | Number:ElectricCurrent   |
| power_c        | W    | C phase active power  | Number:Power             |
| importenergy_c | kWh  | C phase import energy | Number:Energy            |
| exportgrid_c   | kWh  | C phase export energy | Number:Energy            |
| frequency_c    | kWh  | C phase frequency     | Number:Frequency         |
| pf_c           | kWh  | C phase power factor  | Number                   |

## More information

More information about the Iammeter devices can be found in the [Iammeter website](https://www.iammeter.com).
