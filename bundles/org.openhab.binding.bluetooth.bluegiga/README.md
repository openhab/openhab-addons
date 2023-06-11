# Bluetooth BlueGiga Adapter

This extension supports Bluetooth access via a BlueGiga (BLED112) USB dongle.

## Supported Things

It defines the following bridge type:

| Bridge Type ID | Description                                                               |
|----------------|---------------------------------------------------------------------------|
| bluegiga       | A BlueGiga USB dongle using a BLED112 chip                                |

## Discovery

The adapter cannot be discovered; its serial port must be manually configured.

## Bridge Configuration

The bluegiga bridge requires the configuration parameter `port`, which corresponds to the serial port the dongle is connected to.
Additionally, the parameter `backgroundDiscovery` can be set to true/false. When set to true, any Bluetooth device of which broadcasts are received is added to the Inbox.

## Example

This is how a BlueGiga adapter can be configured textually in a *.things file:

```java
Bridge bluetooth:bluegiga:1 [ port="/dev/ttyS0", backgroundDiscovery=false ]
```
