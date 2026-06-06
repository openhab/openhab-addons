# Hunter Douglas (Luxaflex) PowerView Binding for Bluetooth

This is an openHAB binding for Bluetooth for [Hunter Douglas PowerView](https://www.hunterdouglas.com/operating-systems/motorized/powerview-motorization/overview) motorized shades via Bluetooth Low Energy (BLE).
In some countries the PowerView system is sold under the brand name [Luxaflex](https://www.luxaflex.com/).

This binding supports Generation 3 shades connected directly via their in built Bluetooth Low Energy interface.
For shades that are connected via a hub or gateway, there is a different [Hunter Douglas (Luxaflex) PowerView binding](https://www.openhab.org/addons/bindings/hdpowerview/).

PowerView shades have motorization control for their vertical position, and some also have vane controls to change the angle of their slats.

## Supported Things

| Thing | Description                                                                        |
|-------|------------------------------------------------------------------------------------|
| shade | A Powerview Generation 3 motorized shade connected via Bluetooth Low Energy (BLE). |

## Bluetooth Bridge

Before you can create `shade` Things, you must first create a Bluetooth Bridge to contain them.
The instructions for creating a Bluetooth Bridge are in the [Bluetooth binding documentation](https://www.openhab.org/addons/bindings/bluetooth/).

## Discovery

Make sure your shades are visible via BLE in the PowerView app before attempting discovery.

The discovery process can be started by pressing the `+` button at the lower right of the Main UI Things page, selecting the Bluetooth binding, and pressing `Scan`.
Any discovered shades will be displayed with the name prefix 'Powerview Shade'.

## Configuration

| Configuration Parameter | Type               | Description                                                                                                         |
|-------------------------|--------------------|---------------------------------------------------------------------------------------------------------------------|
| address                 | Required           | The Bluetooth MAC address of the shade.                                                                             |
| bleTimeout              | Optional, Advanced | The maximum number of seconds to wait before transactions over Bluetooth will time out (default = 6 seconds).       |
| heartbeatDelay          | Optional, Advanced | The scanning interval in seconds at which the binding checks if the Shade is on- or off- line (default 15 seconds). |
| pollingDelay            | Optional, Advanced | The scanning interval in seconds at which the binding checks the battery status (default 300 seconds).              |
| encryptionKey           | Optional           | The key to be used when encrypting commands to the shade. See [next chapter](#encryption-key).                      |

## Encryption Key

If you want to send position commands to a shade, then an encryption key may be required.
If the shade is NOT included in the Powerview App, then an encryption key is not required.
But if it IS in the Powerview App, then openHAB has to use the same encryption key as used by the App.
Currently you can only discover the encryption key by snooping the network traffic between the App and the shade.
Please post on the openHAB community forum for advice about how to do this.

## Channels

A shade always implements a roller shutter channel `position` which controls the vertical position of the shade's (primary) rail.
If the shade has slats or rotatable vanes, there is also a dimmer channel `tilt` which controls the slat / vane position.
If it is a dual action (top-down plus bottom-up) shade, there is also a roller shutter channel `secondary` which controls the vertical position of the secondary rail.

| Channel       | Item Type            | Description                                           |
|---------------|----------------------|-------------------------------------------------------|
| position      | Rollershutter        | The vertical position of the shade's rail.            |
| secondary     | Rollershutter        | The vertical position of the secondary rail (if any). |
| tilt          | Dimmer               | The degree of opening of the slats or vanes (if any). |
| battery-level | Number:Dimensionless | Battery level (10% = low, 50% = medium, 100% = high). |
| rssi          | Number:Power         | Received Signal Strength Indication.                  |

Note: the channels `secondary` and `tilt` only exist if the shade physically supports such channels.

## Examples

```java
// things
Bridge bluetooth:bluegiga:abc123 "Bluetooth Stick" @ "Comms Cabinet" [port="COM3"] {
    // shade NOT integrated in Powerview App
    Thing bluetooth:shade:112233445566 "North Window Shade" @ "Office" [address="11:22:33:44:55:66"]

    // or, shade integrated in Powerview App
    Thing bluetooth:shade:112233445566 "North Window Shade" @ "Office" [address="11:22:33:44:55:66", encryptionKey="59409c980e627e2fc702c2efcbd4064d"]
}

// items
Rollershutter Shade_Position "Shade Position" {channel="bluetooth:shade:abc123:112233445566:position"}
Dimmer Shade_Position2 "Shade Position" {channel="bluetooth:shade:abc123:112233445566:position"}
Dimmer Shade_Tilt "Shade Tilt" {channel="bluetooth:shade:abc123:112233445566:tilt"}
Number:Dimensionless Shade_Battery_Level "Shade Battery Level" {channel="bluetooth:shade:abc123:112233445566:battery-level"}
Number:Power Shade_RSSI "Shade Signal Strength" {channel="bluetooth:shade:abc123:112233445566:rssi"}
```
