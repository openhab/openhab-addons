# SMA Energy Meter Binding

This Binding is used to display the measured values of a SMA Energy Meter device.
It shows purchased and grid feed-in power and energy.

## Supported Things

This Binding supports SMA Energy Meter devices.

## Discovery

The Energy Meter is discovered by receiving data on the default multicast IP address.

## Binding Configuration

No binding configuration required.

## Thing Configuration

Usually no manual configuration is required, as the multicast IP address and the port remain on their factory set values.
Optionally, a refresh interval (in seconds) can be defined.

## Channels

The channel names is a combination of three attributes

- L1-L3 if there is a phase relevant for the value
- total or current value
- the name of the measured value (current, voltage, consumption, ...)

this leads to channel names in the following format: L1_TOTAL_CONSUME

Here are some examples:

| Channel Name       | Channel name old | Description         |
|--------------------|------------------|---------------------|
| CURRENT_CONSUME    | powerIn          | Purchased power     |
| CURRENT_SUPPLY     | powerOut         | Grid feed-in power  |
| TOTAL_CONSUME      | energyIn         | Purchased energy    |
| L1_CURRENT_CONSUME | powerInL1        | Purchased power L3  |
| L2_CURRENT_CONSUME | powerInL2        | Purchased power L2  |
| L3_CURRENT_CONSUME | powerInL3        | Purchased power L3  |

## Full example

N/A
