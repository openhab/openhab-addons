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

| Parameter        | Name            | Description                                                | Required | Default         |
|------------------|-----------------|------------------------------------------------------------|----------|-----------------|
| `serialNumber`   | Serial number   | Serial number of a meter.                                  | yes      |                 |
| `mcastGroup`     | Multicast Group | Multicast group used by meter.                             | yes      | 239.12.255.254  |
| `port`           | Port            | Port number used by meter.                                 | no       | 9522            |
| `pollingPeriod`  | Polling Period  | Polling period used to publish meter reading (in seconds). | no       | 30              |

The polling period parameter is used to trigger readout of meter. In case if two consecutive readout attempts fail thing will report offline status.

## Channels

| Channel     | Description            |
|-------------|------------------------|
| powerIn     | Purchased power        |
| powerInL1   | Purchased power L1     |
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
