# Bluetooth Binding

This binding provides support for generic Bluetooth devices.

It has the following extensions:

<!--list-subs-->

## Bridges

In order to function, this binding requires a Bluetooth adapter to be present, which handles the wireless communication.
As there is no standard in Bluetooth for such dongles resp. chips, different adapters require a different implementation.
This is why the Bluetooth binding itself does not come with any bridge handlers for such adapters itself, but instead is extensible by additional bundles which can implement support for a specific adapter. 

For Linux, there exists a special bundle which provides a Bluetooth bridge that talks to BlueZ.
This should be the best choice for any Linux-based single board computers like e.g. the Raspberry Pi.

## Supported Things

Two thing types are supported by this binding:

| Thing Type ID | Description                                                                                             |
|---------------|---------------------------------------------------------------------------------------------------------|
| beacon        | A Bluetooth device that is not connected, but only broadcasts annoucements.                             |
| connected     | A Bluetooth device that allows a direct connection and which provides specific services when connected. |


## Discovery

Discovery is performed through the Bluetooth bridge.
Normally, any broadcasting Bluetooth device can be uniquely identified and thus a bridge can create an inbox result for it.
As this might lead to a huge list of devices, bridges usually also offer a way to deactivate this behavior.

## Thing Configuration

Both thing types only require a single configuration parameter `address`, which corresponds to the Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX").

## Channels

Every Bluetooth thing has the following channel:

| Channel ID | Item Type | Description                                                                                         |
|------------|-----------|-----------------------------------------------------------------------------------------------------|
| rssi       | Number    | The "Received Signal Strength Indicator", the [RSSI](https://blog.bluetooth.com/proximity-and-rssi) |

`connected` Things are dynamically queried for their services and if they support certain standard GATT characteristics, the appropriate channels are automatically added as well:

| Channel ID    | Item Type | Description                                                     |
|---------------|-----------|-----------------------------------------------------------------|
| battery_level | Number    | The device's battery level in percent                           |


## Full Example

demo.things (assuming you have a Bluetooth bridge with the ID `bluetooth:bluez:hci0`):

```
bluetooth:beacon:hci0:b1  "BLE Beacon" (bluetooth:bluez:hci0) [ address="68:64:4C:14:FC:C4" ]
```

demo.items:

```
Number Beacon_RSSI "My Beacon [%.0f]" { channel="bluetooth:beacon:hci0:b1:rssi" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame {
        Text item=Beacon_RSSI
    }
}
```

See also the following extensions for further examples:

<!--list-subs-->
