# SMA Energy Meter Binding

This Binding is used to display the measured values of a SMA Energy Meter device.
It shows active, reactive, and apparent power and energy, plus power factor, current, voltage, frequency, and version values published by the meter.

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
| `serialNumber`   | Serial number   | Decimal serial number of a meter; hexadecimal remains supported for compatibility. | yes      |                 |
| `mcastGroup`     | Multicast Group | Multicast group used by meter.                             | yes      | 239.12.255.254  |
| `port`           | Port            | Port number used by meter.                                 | no       | 9522            |
| `pollingPeriod`  | Polling Period  | Polling period used to publish meter reading (in seconds). | no       | 30              |

The polling period parameter is used to trigger readout of meter. In case if two consecutive readout attempts fail Thing will report offline status.

## Channels

| Channel | Description |
|---|---|
| activePowerIn / activePowerOut | Purchased and grid feed-in active power |
| activePowerInL1 / activePowerOutL1 | Purchased and grid feed-in active power L1 |
| activePowerInL2 / activePowerOutL2 | Purchased and grid feed-in active power L2 |
| activePowerInL3 / activePowerOutL3 | Purchased and grid feed-in active power L3 |
| activeEnergyIn / activeEnergyOut | Purchased and grid feed-in active energy |
| activeEnergyInL1 / activeEnergyOutL1 | Purchased and grid feed-in active energy L1 |
| activeEnergyInL2 / activeEnergyOutL2 | Purchased and grid feed-in active energy L2 |
| activeEnergyInL3 / activeEnergyOutL3 | Purchased and grid feed-in active energy L3 |
| reactivePowerIn / reactivePowerOut | Purchased and grid feed-in reactive power |
| reactivePowerInL1 / reactivePowerOutL1 | Purchased and grid feed-in reactive power L1 |
| reactivePowerInL2 / reactivePowerOutL2 | Purchased and grid feed-in reactive power L2 |
| reactivePowerInL3 / reactivePowerOutL3 | Purchased and grid feed-in reactive power L3 |
| reactiveEnergyIn / reactiveEnergyOut | Purchased and grid feed-in reactive energy |
| reactiveEnergyInL1 / reactiveEnergyOutL1 | Purchased and grid feed-in reactive energy L1 |
| reactiveEnergyInL2 / reactiveEnergyOutL2 | Purchased and grid feed-in reactive energy L2 |
| reactiveEnergyInL3 / reactiveEnergyOutL3 | Purchased and grid feed-in reactive energy L3 |
| apparentPowerIn / apparentPowerOut | Purchased and grid feed-in apparent power |
| apparentPowerInL1 / apparentPowerOutL1 | Purchased and grid feed-in apparent power L1 |
| apparentPowerInL2 / apparentPowerOutL2 | Purchased and grid feed-in apparent power L2 |
| apparentPowerInL3 / apparentPowerOutL3 | Purchased and grid feed-in apparent power L3 |
| apparentEnergyIn / apparentEnergyOut | Purchased and grid feed-in apparent energy |
| apparentEnergyInL1 / apparentEnergyOutL1 | Purchased and grid feed-in apparent energy L1 |
| apparentEnergyInL2 / apparentEnergyOutL2 | Purchased and grid feed-in apparent energy L2 |
| apparentEnergyInL3 / apparentEnergyOutL3 | Purchased and grid feed-in apparent energy L3 |
| powerFactor / powerFactorL1 / powerFactorL2 / powerFactorL3 | Power factor total and per phase |
| currentL1 / currentL2 / currentL3 | Current per phase |
| voltageL1 / voltageL2 / voltageL3 | Voltage per phase |
| frequency | Grid frequency |
| version | Device version |

Older channel links using the former `powerIn` / `energyIn` names are migrated automatically through the binding update description.

## Full example

N/A
