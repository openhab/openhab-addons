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
There can be only a single roaming adapter on a system.

## Bridge Configuration

The roaming bridge has a single parameter `backgroundDiscovery` that can be set to `true` or `false`. 
When set to `true`, a device discovered on any other adapter will have a corresponding `roaming` discovery.
The `backgroundDiscovery` parameter is true by default.

## Example

This is how an Roaming adapter can be configured textually in a *.things file:

```
Bridge bluetooth:roaming:ctrl "BLE Roaming Adapter" [ backgroundDiscovery=true ]
```
