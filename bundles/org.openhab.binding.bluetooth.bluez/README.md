# Bluetooth BlueZ Adapter

This extension supports Bluetooth access via BlueZ and DBus on Linux.
This is architecture agnostic and uses Unix Sockets.

# Setup

Please note that at least BlueZ 5.43 is required.

Some settings are required to ensure that openHAB has access to Bluez.
To allow openHAB to access Bluez via dbus you need to add the following entry within your dbus configuration in `/etc/dbus-1/system.d/bluetooth.conf`

```xml
<busconfig>
  <policy user="root">
    ...
  </policy>
  <policy group="bluetooth">
    <allow send_destination="org.bluez"/>
  </policy>
  ...
</busconfig>
```

and add openHAB to the "bluetooth" group.

```shell
sudo adduser openhab bluetooth
```

Also, in case you don't want to manually enable your bluetooth adapters with `bluetoothctl`, ensure that it's automatically enabled by setting the option `AutoEnable` in your `/etc/bluetooth/main.conf` to `true`.
Restart running services for changes to take effect.

```shell
systemctl restart dbus
systemctl restart bluetooth
systemctl restart openhab
```

## Supported Things

It defines the following bridge type:

| Bridge Type ID | Description                                                               |
|----------------|---------------------------------------------------------------------------|
| bluez          | A Bluetooth adapter that is supported by BlueZ                            |

## Discovery

If BlueZ is enabled and can be accessed, all available adapters are automatically discovered.

## Bridge Configuration

The bluez bridge requires the configuration parameter `address`, which corresponds to the Bluetooth address of the adapter (in format "XX:XX:XX:XX:XX:XX").

Additionally, the parameter `backgroundDiscovery` can be set to true/false.When set to true, any Bluetooth device of which broadcasts are received is added to the Inbox.

## Example

This is how a BlueZ adapter can be configured textually in a *.things file:

```java
Bridge bluetooth:bluez:hci0 [ address="12:34:56:78:90:AB", backgroundDiscovery=false ]
```
