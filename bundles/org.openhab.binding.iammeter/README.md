# Iammeter Binding 

[Iammeter](https://www.iammeter.com) provides real-time readings of single-phase (WEM3080, WEM3162) and three-phase (WEM3080T) meters from IAMMETER over Wi-Fi.

## Use of the binding

The Iammeter is exposed as one thing with a number of channels that can be used to read the values for different aspects of your Iammeter devices. 

## Setup of the binding

You can add the Iammeter device via the openHAB UI manually.


## Available channels

The following table is taken from the official manual and contains all available channels.

Single-phase energy meter (WEM3080/WEM3162)
|Name|Unit|Description|Type|
|----|----|----|----|
|voltage_a|V|Voltage|Number|
|current_a|A|Current|Number|
|power_a|W|Active power|Number|
|importenergy_a|kWh|Energy consumption from gird|Number|
|exportgrid_a|kWh|Energy export to grid|Number|

Three-phase energy meter (WEM3080T)
|Name|Unit|Description|Type|
|----|----|----|----|
|voltage_a|V|A phase voltage|Number|
|current_a|A|A phase current|Number|
|power_a|W|A phase active power|Number|
|importenergy_a|kWh|A phase import energy|Number|
|exportgrid_a|kWh|A phase export energy|Number|
|frequency_a|kWh|A phase frequency|Number|
|pf_a|kWh|A phase power factor|Number|
|voltage_b|V|B phase voltage|Number|
|current_b|A|B phase current|Number|
|power_b|W|B phase active power|Number|
|importenergy_b|kWh|B phase import energy|Number|
|exportgrid_b|kWh|B phase export energy|Number|
|frequency_b|kWh|B phase frequency|Number|
|pf_b|kWh|B phase power factor|Number|
|voltage_c|V|C phase voltage|Number|
|current_c|A|C phase current|Number|
|power_c|W|C phase active power|Number|
|importenergy_c|kWh|C phase import energy|Number|
|exportgrid_c|kWh|C phase export energy|Number|
|frequency_c|kWh|C phase frequency|Number|
|pf_c|kWh|C phase power factor|Number|

## More information

More information about the Iammeter devices can be found in the [Iammeter website](https://www.iammeter.com). 
