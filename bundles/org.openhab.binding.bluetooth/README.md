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

| Thing Type ID | Description                                                                                                |
|---------------|------------------------------------------------------------------------------------------------------------|
| beacon        | A Bluetooth device that is not connected, but only broadcasts annoucements.                                |
| connected     | A Bluetooth device that allows a direct connection and which provides specific services when connected.    |
| roaming       | A virtual Bluetooth adapter that interacts with Bluetooth devices through their nearest Bluetooth adapter. |


## Discovery

Discovery is performed through the Bluetooth bridge.
Normally, any broadcasting Bluetooth device can be uniquely identified and thus a bridge can create an inbox result for it.
As this might lead to a huge list of devices, bridges usually also offer a way to deactivate this behavior.

## Thing Configuration

Both thing types only require a single configuration parameter `address`, which corresponds to the Bluetooth address of the device (in format "XX:XX:XX:XX:XX:XX").

## Channels

Every Bluetooth thing has the following channels:

| Channel ID    | Item Type | Description                                                                                         |
|---------------|-----------|-----------------------------------------------------------------------------------------------------|
| rssi          | Number    | The "Received Signal Strength Indicator", the [RSSI](https://blog.bluetooth.com/proximity-and-rssi) |
| last-activity | DateTime  | The last time that any radio activity was received from this device                                 |

`connected` Things are dynamically queried for their services and if they support certain standard GATT characteristics, the appropriate channels are automatically added as well:

| Channel ID    | Item Type | Description                                                     |
|---------------|-----------|-----------------------------------------------------------------|
| battery_level | Number    | The device's battery level in percent                           |


Devices which use a `roaming` adapter as their bridge also gain the following channels:

| Channel ID       | Item Type | Description                                                     |
|------------------|-----------|-----------------------------------------------------------------|
| adapter-uid      | String    | The thingUID of the adapter that is nearest to this device      |
| adapter-location | String    | The value of the `Location` specified for the nearest adapter    |


## Roaming Adapter
A roaming adapter thing will be put into the Inbox automatically. 
It is advised to create your roaming adapter thing from the Inbox but you can also create it manually as per the example below.

## Full Example

demo.things:

```
Bridge bluetooth:roaming:ctrl "BLE Roaming Adapter" [ address="FF:FF:FF:FF:FF:FF", discovery=true ]
Thing bluetooth:beacon:ctrl:b1  "BLE Beacon" (bluetooth:roaming:ctrl) [ address="68:64:4C:14:FC:C4" ]
```

demo.items:

```
Number Beacon_RSSI "My Beacon [%.0f]" { channel="bluetooth:beacon:ctrl:b1:rssi" }
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
