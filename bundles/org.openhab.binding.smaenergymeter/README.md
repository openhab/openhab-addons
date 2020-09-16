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

| Channel     | Description            |
|-------------|------------------------|
| powerIn     | Purchased power        |
| powerInL1   | Purchased power L3     |
| powerInL2   | Purchased power L2     |
| powerInL3   | Purchased power L3     |
| powerOut    | Grid feed-in power     |
| powerOutL1  | Grid feed-in power L1  |
| powerOutL2  | Grid feed-in power L2  |
| powerOutL3  | Grid feed-in power L3  |
| energyIn    | Purchased energy       |
| energyInL1  | Purchased energy L1    |
| energyInL2  | Purchased energy L2    |
| energyInL3  | Purchased energy L3    |
| energyOut   | Grid feed-in energy    |
| energyOutL1 | Grid feed-in energy L1 |
| energyOutL2 | Grid feed-in energy L2 |
| energyOutL3 | Grid feed-in energy L3 |

## Full example

N/A
