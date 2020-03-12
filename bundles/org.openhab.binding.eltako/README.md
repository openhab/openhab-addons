# Eltako Binding
The Eltako Binding integrates the Eltako Series 14 device family into OpenHAB.
This binding is focused on the connecting by serial interface only. For 
controlling Series 14 devices wirelessly please refer to EnOcean binding.

## Supported Things

### Bridges
The following bridge devices are supported by the binding
* FAM14 (please see FAQ section before usage)
* FGW14

### Things
The following things are supported by the binding
* FUD14
* FSB14

### Overview
|Thing type      | Description                                                     | Pairing   | Tested |
|----------------|-----------------------------------------------------------------|-----------|--------|
| FAM14          | RS485 Busmaster with wireless capabilities and 12V power supply | Discovery | Yes    |
| FGW14          | RS485 <-> USB Gateway                                           | Manually  | Yes    |
| FUD14          | 1 channel dimmer                                                | -         | Yes    |
| FSB14          | 2 channel rollershutter controller                              | -         | Yes    |

## Discovery

At the moment only the FAM14 gateway supports the discovery of new devices.
As soon as at least one FAM14 has been configured the discovery scan will be
started as soon as new devices should be added.

## Thing Configuration
|Thing type      | Parameters                              | Channels                    |
|----------------|-----------------------------------------|-----------------------------|
| FAM14          | SerialComPort                           | -                           |
| FGW14          | SerialComPort, SerialBaudRate, DeviceId | -                           |
| FUD14          | DeviceId                                | brightness, speed, blocking |
| FSB14          | DeviceId                                | control, runtime            |

### Prerequirements
Before any thing can be added to OpenHAB all used devices need to be configured
by using the PCT14 software provided by Eltako. For this a FAM14 Gateway is mandatory!

Following settings need to be set:
* All connected devices need to have a unique DeviceID (range 1 - 254). Some devices require multiple DeviceIDs!
* For each device a specific telegram ID needs to be configured on which it listenes to. Otherwise it will not respond to the telegrams send by the eltako binding.
    * For example if device ID is 0x00 0x00 0x00 0x04 the listening ID should be 0x03 0x00 0x00 0x01
    * This listening ID can be configured in the last section of "ID-Zuordnungsbereich"
    * As function the "...GVFS..." needs to be configured. Function name can variate between device types.
* All devices need to be added to the "Rückmeldeliste" of the FAM14 gateway. Otherwise the FGW14 will not forward any status telegrams to OpenHAB.
* Set FAM14 to mode 5 by using the upper selection wheel.

### PaperUI
First add a bridge type device (FAM14 or FGW14). This bridge will act as a gateway handling telegram transfer with the Eltako bus devices.

Devices can be added by simply selecting the device and setting the needed parameters. The DeviceID is a mandatory parameter which needs to be identical to the ID set by PCT14 tool.

### Thing Files
As for all OpenHAB bindings things can be added by using the "textmode".
For example a FGW14 serving as a bridge for a FUD14 and a FSB14 can be created with the following config:

```xtend
Bridge eltako:FGW14:gateway "Gateway" [SerialComPort="COM11", deviceId="4"]{
    Thing eltako:FUD14:dimmerkitchen "DimmerKitchen" @ "Kitchen" [deviceId="1"]
    Thing eltako:FSB14:rollerkitchen "RollerKitchen" @ "Kitchen" [deviceId="8"]
}
```

## Channels
Here you will find a description of all the different channel types used by the Eltako devices.

| Channel     | Type          | Description                                                           | Command Types    |
|-------------|---------------|-----------------------------------------------------------------------|------------------|
| brightness  | Dimmer        | Percentage value for dimmers                                          | Percent          |
| speed       | Number        | Values in number format                                               | Decimal          |
| blocking    | Switch        | Switch Item, used for anything that needs to be switched ON and OFF   | OnOff            |
| control     | Rollershutter | Roller shutter Item, typically used for blinds                        | UpDown, StopMove |
| runtime     | Number        | Values in number format                                               | Decimal          |

## Full Example
Here you can find a complete example to get you started with the Eltako binding:

default.things file:
```xtend
Bridge eltako:FGW14:gateway "Gateway" [SerialComPort="COM11", deviceId="4"]{
    Thing eltako:FUD14:dimmerkitchen "DimmerKitchen" @ "Kitchen" [deviceId="1"]
    Thing eltako:FSB14:rollerkitchen "RollerKitchen" @ "Kitchen" [deviceId="8"]
}
```

default.items file:
```xtend
Dimmer          Dimmer_FUD14        "Dimmer FUD14"      {channel="eltako:FUD14:dimmerkitchen:brightness"}
Number          Speed_FUD14         "Speed FUD14"       {channel="eltako:FUD14:dimmerkitchen:speed"}
Rollershutter   Control_FSB14       "Control FSB14"     {channel="eltako:FSB14:rollerkitchen:control"}
Number          Runtime_FSB14       "Runtime FSB14"     {channel="eltako:FSB14:rollerkitchen:runtime"}
```

default.sidemap file:
```xtend
sitemap default label="My home automation" {
    Frame label="FUD14" {
        Default item=Dimmer_FUD14       label="Brightness"
        Setpoint item=Speed_FUD14       label="Speed"
    }

    Frame label="FSB14" {
        Default item=Control_FSB14      label="Control"
        Setpoint item=Runtime_FSB14     label="Runtime"
    }
}
```

## FAQ

### Devices are not always respond on telegrams send by FAM14 gateway
The FAM14 gateway is intended to be used as a config interface for PCT14 only.
Although it can be used to transmit telegrams to Eltako devices there is a high risk of
bus collisions in case Eltako devices are transmitting data at the same time.
In this case the transmitted telegram gets corrupted and the command will not be executed.

