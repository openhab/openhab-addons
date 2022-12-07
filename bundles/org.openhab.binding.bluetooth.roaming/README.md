# Bluetooth Roaming Adapter

This extension adds support for accessing Bluetooth devices from any other configured adapter via a virtual adapter.

## Supported Things

It defines the following bridge type:

| Bridge Type ID | Description                                                                                                |
|----------------|------------------------------------------------------------------------------------------------------------|
| roaming        | A virtual Bluetooth adapter that interacts with Bluetooth devices through their nearest Bluetooth adapter. |

## Channels

Devices which use a `roaming` adapter as their bridge also gain the following channels:

| Channel ID       | Item Type | Description                                                                          |
|------------------|-----------|--------------------------------------------------------------------------------------|
| adapter-uid      | String    | The thingUID of the adapter that is nearest to this device                           |
| adapter-location | String    | The nearest adapter's `Location` value as specified in the adapter's thing properties |

## Discovery

Roaming adapters cannot be discovered, they can only be created manually.

## Bridge Configuration

The Roaming bridge has an optional parameter `groupUIDs` that configures which Bluetooth adapters this roaming bridge will be monitored for the purpose of roaming devices.
`groupUIDs` must be formatted as a comma separated list of Bluetooth adapter thing UID values.
If the `groupUIDs` parameter is not specified or left empty then the Roaming adapter will track devices across all other Bluetooth adapters.

Additionally, the Roaming bridge has the parameter `backgroundDiscovery` that can be set to `true` or `false`.
When set to `true`, a device discovered on any other adapter will have a corresponding `roaming` discovery.
The `backgroundDiscovery` parameter is true by default.

## Example

This is how a Roaming adapter can be configured textually in a *.things file:

```java
Bridge bluetooth:roaming:ctrl "BLE Roaming Adapter" [ backgroundDiscovery=true]
Bridge bluetooth:roaming:other "BLE Roaming Adapter" [ backgroundDiscovery=true, groupUIDs="bluetooth:bluez:hci0,bluetooth:bluez:hci1"]
```
