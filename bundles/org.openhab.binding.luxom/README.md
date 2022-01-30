# Luxom Binding

This binding integrates with a https://luxom.io/ based system through a Luxom IP interface module.
The binding has been tested with the DS65L IP interface, but it's not an official binding by Luxom.

The communication api is based on the following documentation: 

- https://old.luxom.io/uploads/ppfiles/27/LUXOM_ASCII.pdf
- https://old.luxom.io/uploads/ppfiles/28/LUXOM_ASCII_extended.pdf

## Supported Things

For the moment only buttons & dimmers are supported.

## Discovery

There's no autodiscovery support.

## Binding Configuration

No extra binding configuration needed.

## Thing Configuration

### Bridge

the Bridge thing has 2 parameters:

- ipAddress: this is the IP of the IP interface module 
- port: the listening port (normally 2300)

```
Bridge luxom:bridge:myhouse [ ipAddress="192.168.0.50", port="2300"] {
    ...
}
```

### Devices

- Each device has an address on the Luxom bus, his adres must be specified in the 'address' parameter. You will have to look it up in your documentation or in the 'Luxom Plusconfig' software.
- Sometimes a devices does not send back a confirmation over the bus having set the correct state. I've encountered it with one dimmer, the dimmer does do the dimming, but does not send back the set brightness level. To be able to use these devices, you can add the `doesNotReply=true` parameter so that the binding immediately set's the items state and does not wait for confirmation.
  
#### Dimmers

Dimmers support the optional advanced parameters `onLevel`, `onToLast` and `stepPercentage` :

- The `onLevel` parameter specifies the level to which the dimmer will go when sent an ON command. It defaults to 100.
- The `onToLast` parameter is a boolean that defaults to false. If set to "true", the dimmer will go to its last non-zero level when sent an ON command. If the last non-zero level cannot be determined, the value of `onLevel` will be used instead.
- The `stepPercentage` specifies the in-/decrease in percentage of brightness. Default is 5.

A **dimmer** thing has a single channel *Lighting.Brightness* with type Dimmer and category DimmableLight.

Thing configuration file example:

```
Thing dimmer dimmerLightLiving1 [address="A,02", onLevel="50", onToLast="false", stepPercentage="5"]
```

#### Switches

Switches take no additional parameters.
A **switch** thing has a single channel *switch* with type Switch and category Switch.

Thing configuration file example:

```
Thing switch switchLiving1 [address="A,02"]
```

## Channels

### Bridge channels

| channel           | type   | description            |
|-------------------|--------|------------------------|
| button            | Switch | Push button is pressed |
| system.brightness | Number | % of light intensity   |

## Full Example

demo.Things:
```
Bridge luxom:bridge:myhouse [ ipAddress="192.168.0.50", port="2300"] {
    Thing switch switchLiving1 "Switch 1" @ "living room" [address="1,01"]
    Thing dimmer dimmerLightLiving1 "dimmer 1" @ "living room" [address="A,02"]
    Thing dimmer dimmerLightKitchen1 "dimmer 1" @ "kitchen" [address="A,04", doesNotReply=true]
}
```

demo.Items:
```
Dimmer          FF_Living_Lights             "Living light"   <light>            (FF_Living, gLight)      ["Lighting"] {channel="luxom:dimmer:myhouse:dimmerLightKitchen1:brightness", ga="Light", homekit="Lighting, Lighting.Brightness"}
Switch          FF_Living_PowerOutlet1       "Living Power Outlet 1"   <poweroutlet>            (FF_Living, gPower)            ["Switchable"] {channel="luxom:switch:elsenmario:switchLiving1:switch", ga="Outlet"}
Dimmer          FF_Kitchen_Lights            "Keuken licht"   <light>            (FF_Kitchen, gLight)             ["Lighting"] {channel="luxom:dimmer:elsenmario:keukenLicht:brightness", ga="Light", homekit="Lighting, Lighting.Brightness"}
```
