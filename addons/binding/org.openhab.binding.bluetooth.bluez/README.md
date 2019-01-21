# Bluetooth BlueZ Adapter

This extension supports Bluetooth access via BlueZ on Linux (ARMv6hf).

Please note that at least BlueZ 5.43 is required, while 5.48 or above are [not (yet) supported](https://github.com/intel-iot-devkit/tinyb/issues/131) either.

Also note that the OS user needs to be a member of the "bluetooth" group of Linux in order to have the rights to access the BlueZ stack.

## Supported Things

It defines the following bridge type:

| Bridge Type ID | Description                                                               |
|----------------|---------------------------------------------------------------------------|
| bluez          | A Bluetooth adapter that is supported by BlueZ                            |


## Discovery

If BlueZ is enabled and can be accessed, all available adapters are automatically discovered.

## Bridge Configuration

The bluez bridge requires the configuration parameter `address`, which corresponds to the Bluetooth address of the adapter (in format "XX:XX:XX:XX:XX:XX").
Additionally, the parameter `discovery` can be set to true/false.When set to true, any Bluetooth device of which broadcasts are received is added to the Inbox.

## Example

This is how an BlueZ adapter can be configured textually in a *.things file:

```
Bridge bluetooth:bluez:hci0 [ address="12:34:56:78:90:AB", discovery=false ]
```
