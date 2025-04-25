# Luxom Binding

This binding integrates with a <https://luxom.io/> based system through a Luxom IP interface module.
The binding has been tested with the DS65L IP interface, but it's not an official binding by Luxom.

The API implementation is based on the following documentation:

- <https://old.luxom.io/uploads/ppfiles/27/LUXOM_ASCII.pdf>

- <https://old.luxom.io/uploads/ppfiles/28/LUXOM_ASCII_extended.pdf>

## Supported Things

This binding currently supports the following thing types:

- **ipbridge** - The Lutron main repeater/processor/hub
- **dimmer** - Light dimmer
- **switch** - Switch or relay module

## Thing Configuration

### Bridge

The Bridge thing has two parameters:

- ipAddress: This is the IP address of the IP interface module
- port: The listening port (optional, defaults to 2300)

```java
Bridge luxom:bridge:myhouse [ ipAddress="192.168.0.50", port="2300"] {
    ...
}
```

### Devices

Each device has an address on the Luxom bus, this address must be specified in the 'address' parameter.
You will have to look it up in your documentation or in the 'Luxom Plusconfig' software.

Sometimes a device does not send back a confirmation over the bus having set the correct state.
Some dimmers do the dimming, but do not send back the set brightness level.
To be able to use these devices, you can add the `doesNotReply=true` parameter so that the binding immediately sets the item's state and does not wait for confirmation.

#### Dimmers

Dimmers support the optional advanced parameters `onLevel`, `onToLast` and `stepPercentage`:

- The `onLevel` parameter specifies the level to which the dimmer will go when sent an ON command. It defaults to 100.
- The `onToLast` parameter is a boolean that defaults to false. If set to "true", the dimmer will go to its last non-zero level when sent an ON command. If the last non-zero level cannot be determined, the value of `onLevel` will be used instead.
- The `stepPercentage` specifies the in-/decrease in percentage of brightness. Default is 5.

A **dimmer** thing has a single channel _Lighting.Brightness_ with type Dimmer and category DimmableLight.

Thing configuration file example:

```java
Thing dimmer dimmerLightLiving1 [address="A,02", onLevel="50", onToLast="false", stepPercentage="5"]
```

#### Switches

Switches take no additional parameters.
A _switch_ thing has a single channel **switch** with type Switch and category Switch.

Thing configuration file example:

```java
Thing switch switchLiving1 [address="A,02"]
```

### Channels

The following is a summary of channels for all Luxom things:

| Thing               | Channel        | Item Type     | Description                       |
|---------------------|----------------|---------------|-----------------------------------|
| dimmer              | brightness     | Dimmer        | Increase/decrease the light level |
| switch              | switch         | Switch        | Switch the device on/off          |

### Full Example

demo.things:

```java
Bridge luxom:bridge:myhouse [ ipAddress="192.168.0.50", port="2300"] {
    Thing switch switchBedroom1 "Switch 1" @ "Bedroom" [address="1,01"]
    Thing dimmer dimmerBedroom1 "dimmer 1" @ "Bedroom" [address="A,02"]
    Thing dimmer dimmerKitchen1 "dimmer 1" @ "Kitchen" [address="A,04", doesNotReply=true]
}
```

demo.items:

```java
Dimmer          FF_Bedroom_Lights             "Bedroom dimmer light"   <light>            (FF_Living, gLight)      ["Lighting"] {channel="luxom:dimmer:myhouse:dimmerBedroom1:brightness", ga="Light", homekit="Lighting, Lighting.Brightness"}
Switch          FF_Bedroom_PowerOutlet1       "Bedroom Power Outlet 1"   <poweroutlet>    (FF_Living, gPower)      ["Switchable"] {channel="luxom:switch:myhouse:switchBedroom1:switch", ga="Outlet"}
Dimmer          FF_Kitchen_Lights             "Kitchen dimmer light"   <light>            (FF_Kitchen, gLight)     ["Lighting"] {channel="luxom:dimmer:myhouse:dimmerKitchen1:brightness", ga="Light", homekit="Lighting, Lighting.Brightness"}
```
