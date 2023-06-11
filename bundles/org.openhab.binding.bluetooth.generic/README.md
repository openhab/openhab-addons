# Generic Bluetooth Device

This binding adds support for devices that expose [Bluetooth Generic Attributes (GATT)](https://www.bluetooth.com/specifications/gatt/)

## Supported Things

Only a single thing type is added by this binding:

| Thing Type ID | Description                                     |
|---------------|-------------------------------------------------|
| generic       | A generic connectable bluetooth device          |

## Discovery

As any other Bluetooth device, generic bluetooth devices are discovered automatically by the corresponding bridge.
Generic bluetooth devices will be discovered for any connectable bluetooth device that doesn't match another bluetooth binding.

## Thing Configuration

| Parameter       | Required | Default | Description                                                         |
|-----------------|----------|---------|---------------------------------------------------------------------|
| address         | yes      |         | The address of the bluetooth device (in format "XX:XX:XX:XX:XX:XX") |
| pollingInterval | no       | 30      | The frequency at which readable characteristics will refresh        |

## Channels

Channels will be dynamically created based on types of characteristics the device supports.
This binding contains a mostly complete database of standardized GATT services and characteristics
that is used to map characteristics to one or multiple channels.

Characteristics not in the database will be mapped to a single `String` channel labeled `Unknown`.
The data visible from unknown channels will be the raw binary data formated as hexadecimal.
Data written (if the unknown characteristic has write support) to unknown channels must likewise be in hexadecimal.
