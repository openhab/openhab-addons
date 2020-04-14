# Bluetooth DBus-BlueZ Adapter

This extension supports Bluetooth access via BlueZ and DBus on Linux. This is architecture agnostic and uses Unix Sockets.


## Supported Things

It defines the following bridge type:

| Bridge Type ID | Description                                                               |
|----------------|---------------------------------------------------------------------------|
| bluezdbus      | A Bluetooth adapter that is supported by DBus-BlueZ                       |


## Bridge Configuration

The dbus-bluez bridge requires the configuration parameter `address`, which corresponds to the Bluetooth address of the adapter (in format "XX:XX:XX:XX:XX:XX").

## Example

This is how an BlueZ adapter can be configured textually in a *.things file:

```
Bridge bluetooth:dbusbluez:hci0 [ address="12:34:56:78:90:AB" ]
```
