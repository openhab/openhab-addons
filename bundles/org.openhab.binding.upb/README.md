# UPB Binding

Universal Powerline Bus (UPB) is a protocol for communication over household electrical wiring.

## Supported Things

The binding has not yet been tested with a variety of devices, so specific device support is limited.
Instead the binding provides some generic devices, and allows adding channels that match the type of device.

- `serial-pim` - Serial PIM
- `generic` - Generic UPB device
- `virtual` - "Virtual" device that allows scene selection

Specific devices that are supported:

- `leviton-38a00-1` - 6-button scene switch

## Binding Configuration

The following binding configuration parameters are supported:

| Parameter                | Description                                    | Config   | Default |
| ------------------------ | ---------------------------------------------- |--------- | ------- |
| networkId                | Default UPB network ID (0-255)                 | Optional | -       |

## Thing Configuration

### Serial PIM

You need a Powerline Interface Module (PIM) for the binding to work.
This is a piece of equipment that connects a computer to the powerline.
There are a few different PIM interfaces but this binding only supports serial PIMs.

The `serial-pim` takes the following configuration parameters:

| Parameter                | Description                                    | Config   | Default |
| ------------------------ | ---------------------------------------------- |--------- | ------- |
| port                     | Serial port where the PIM is connected         | Required | -       |

### Generic device

The `generic` thing type supports most UPB devices such as dimmers, light switches,
and appliance modules. It has the following configuration parameters:

| Parameter                | Description                                    | Config   | Default        |
| ------------------------ | ---------------------------------------------- |--------- | -------------- |
| networkId                | ID of the UPB network (0-255)                  | Optional | binding config |
| unitId                   | Unit ID (unique address) of the device (1-250) | Required | -              |

### Virtual device

The `virtual` pseudo-device does not correspond to any real device on the UPB network.
It is nevertheless useful for reading and setting the current scene.
The device has two channels, `linkActivated` and `linkDeactivated`.
If a device on the UPB network activates or de-activates a scene by broadcasting a link
activation command, the link ID (or scene number) can be read from
the corresponding channel.
Similarly, updating the channel with a link ID will send out the
corresponding link command on the UPB network.

## Channels

These channels are available for generic devices:

| Channel Type    | Item type | Description                                     |
| --------------- | --------- | ----------------------------------------------- |
| dimmer          | Dimmer    | Level/brightness, or on/off for switches        |
| scene-selection | -         | Trigger channel for scene selection             |

The virtual device supports the `link` channel type:

| Channel Type | Item type | Description                            |
| ------------ | --------- | -------------------------------------- |
| link         | Number    | A scene to activate or deactivate      |

## Full Example

Here is a sample `.things configuration file:

```java
Bridge upb:serial-pim:pim "UPB PIM" @ "Basement" [port="/dev/ttyUSB0"] {
  Thing generic light-switch "Living Room Light" [networkId=1, unitId=1]

  Thing virtual upb-scene "UPB Scene Control" [networkId=1, unitId=250]

  Thing leviton-38a00-1 scene-switch "Scene Switch" @ "Bedroom" [networkId=1, unitId=2] {
    Channels:
      Type scene-selection : btnOn [linkId=1]
      Type scene-selection : btnOff [linkId=2]
      Type scene-selection : btnA [linkId=3]
      Type scene-selection : btnB [linkId=4]
      Type scene-selection : btnC [linkId=5]
      Type scene-selection : btnD [linkId=6]
  }
}

```

And the items:

```java
// Configure as either Switch or Dimmer
Dimmer LivingRoomLight "UPB Light Switch" {channel="upb:generic:pim:light-switch:dimmer"}

// A scene selector (does not correspond to a physical device)
Number UPB_Active_Scene "Active UPB Scene" {channel="upb:virtual:pim:upb-scene:linkActivated"}
```
