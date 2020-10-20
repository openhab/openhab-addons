# Bluetooth BlueZ Adapter

This extension supports Bluetooth access via BlueZ and DBus on Linux. This is architecture agnostic and uses Unix Sockets.


## Supported Things

It defines the following bridge type:

| Bridge Type ID | Description                                                               |
|----------------|---------------------------------------------------------------------------|
| bluez          | A Bluetooth adapter that is supported by BlueZ                            |


## Bridge Configuration

The bluez bridge requires the configuration parameter `address`, which corresponds to the Bluetooth address of the adapter (in format "XX:XX:XX:XX:XX:XX").

Additionally, the parameter `backgroundDiscovery` can be set to true/false.When set to true, any Bluetooth device of which broadcasts are received is added to the Inbox.

## Example

This is how an BlueZ adapter can be configured textually in a *.things file:

```
<<<<<<< HEAD
Bridge bluetooth:bluez:hci0 [ address="12:34:56:78:90:AB" ]
=======
Bridge bluetooth:bluez:hci0 [ address="12:34:56:78:90:AB", backgroundDiscovery=false ]
>>>>>>> bluetooth-channels
```

##Troubleshooting
If you are unable to install this binding due to the `bluez-dbus` bundle missing, try running this in the openhab-cli to fix the issue:
```
bundle:install -s https://repo1.maven.org/maven2/com/github/hypfvieh/bluez-dbus-osgi/0.1.3/bluez-dbus-osgi-0.1.3.jar
```
