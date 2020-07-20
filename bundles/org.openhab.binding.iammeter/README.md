# Iammeter Binding 

[Iammeter](https://www.iammeter.com) provides real-time readings of single-phase (WEM3080, WEM3162) and three-phase (WEM3080T) meters from IAMMETER over Wi-Fi.

## Use of the binding

The Iammeter is exposed as one thing with a number of channels that can be used to read the values for different aspects of your Iammeter devices. 

## Setup of the binding

You can configure the Thing via the openHAB UI:
1.install "Iammeter binding" in "configuration" section
2.add "Iammeter binding" in "Things" section
3.config Iammeter device's IP


## Available channels

The following table is taken from the official manual and contains all available channels.

Single-phase energy meter (WEM3080/WEM3162)
name	Unit	Description
voltage_a	V	Voltage.
current_a	A	current.
power_a	W	active power.
importenergy_a	kWh	Energy consumption from gird
exportgrid_a	kWh	Energy export to grid

Three-phase energy meter (WEM3080T)
name	Unit	Description
voltage_a	V	A phase voltage
current_a	A	A phase current
power_a	W	A phase active power
importenergy_a	kWh	A phase import energy
exportgrid_a	kWh	A phase export energy
frequency_a	kWh	A phase frequency
pf_a	kWh	A phase power factor
voltage_b	V	B phase voltage
current_b	A	B phase current
power_b	W	B phase active power
importenergy_b	kWh	B phase import energy
exportgrid_b	kWh	B phase export energy
frequency_b	kWh	B phase frequency
pf_b	kWh	B phase power factor
voltage_c	V	C phase voltage
current_c	A	C phase current
power_c	W	C phase active power
importenergy_c	kWh	C phase import energy
exportgrid_c	kWh	C phase export energy
frequency_c	kWh	C phase frequency
pf_c	kWh	C phase power factor

## More information

More information about the Iammeter devices can be found in the [Iammeter website](https://www.iammeter.com). 
