# Somfy Tahoma Binding

This binding integrates the [Somfy Tahoma](https://www.somfy.fr/produits/domotique/maison-connectee-tahoma) home automation system.

## Supported Things

Currently supports these things
- bridge (Somfy Tahoma bridge, which can discover gateways, roller shutters, awnings, switches and action groups)
- gateways (Somfy Tahoma gateway - getting firmware version)
- roller shutters (UP, DOWN, STOP control of a roller shutter). IO Homecontrol devices are allowed to set exact position of a shutter (0-100%)
- awnings (UP, DOWN, STOP control of an awning). IO Homecontrol devices are allowed to set exact position of an awning (0-100%)
- on/off switches (connected by RTS, IO protocol or supported by USB stick - z-wave, enocean, ..)
- action groups (can execute predefined Tahoma action - groups of steps, e.g. send to all roller shutters DOWN command, one by one)

Currently only Somfy Tahoma device has been tested.

## Discovery

To start a discovery, just 
- install this binding
- open Paper UI
- add a new thing in menu Configuration/Things
- choose SomfyTahoma Binding and select Somfy Tahoma Bridge
- enter your email (login) and password to the TahomaLink cloud portal
 
If the supplied TahomaLink credentials are correct, the automatic discovery starts immediately and detected roller shutters, awnings, switches and action groups appear in Paper UI inbox. 

## Thing Configuration

To manually configure the thing you have to specify bridge and things in *.things file in conf/addons directory of your OpenHAB 2.x installation.
To manually link the thing channels to items just use the *.items file in conf/items directory of your OpenHAB 2.x installation. 
To retrieve thing configuration and url parameter, just add the automatically discovered device from you inbox and copy its values from thing edit page. (the url parameter is visible on edit page only)
Please see the example below.

## Channels

A bridge does not expose any channel.

Gateways expose this read only channel:
- version (this is a firmware version of your Tahoma gateway)

Roller shutters and awnings expose these channels:
- position (a percentual position of the roller shutter, it can have value 0-100). For IO Homecontrol devices only (non RTS)!
- control (a rollershutter controller which reacts to commands UP/DOWN/STOP)

When STOP command received two possible behaviours are possible
- when the roller shutter is idle then MY command is interpreted (the roller shutter/awning goes to your favourite position)
- when the roller shutter is moving then STOP command is interpreted (the roller shutter/awning stops)

An action group thing has this channel:
- trigger (a switch which reacts to ON command and triggers the predefined Tahoma action)

A on/off thing has this channel:
- switch (reacts to standard ON/OFF commands)

## Full Example

.things file
```
Bridge somfytahoma:bridge:237dbae7 "Somfy Tahoma Bridge" [ email="my@email.com", password="MyPassword", refresh=30 ] {
    Thing gateway 1214-4519-8041 "Tahoma gateway" [ id="1214-4519-8041" ]
    Thing rollershutter 31da8dac-8e09-455a-bc7a-6ed70f740001 "Bedroom" [ url="io://0204-1234-8041/6825356" ]
    Thing rollershutter 87bf0403-a45d-4037-b874-28f4ece30004 "Living room" [ url="io://0204-1234-8041/3832644" ]
    Thing rollershutter 68bee082-63ab-421d-9830-3ea561601234 "Hall" [ url="io://0204-1234-8041/4873641" ]
    Thing actiongroup 2104c46f-478d-6543-956a-10bd93b5dc54 "1st floor up" [ url="2104c46f-478d-6543-956a-10bd93b5dc54" ]
    Thing actiongroup 0b5f195a-5223-5432-b1af-f5fa1d59074f "1st floor down" [ url="0b5f195a-5223-5432-b1af-f5fa1d59074f" ]
    Thing actiongroup 712c0019-b422-1234-b4da-208e249c571b "2nd floor up" [ url="712c0019-b422-1234-b4da-208e249c571b" ]
    Thing actiongroup e201637b-de3b-1234-b7af-5693811a953b "2nd floor down" [ url="e201637b-de3b-1234-b7af-5693811a953b" ]
    Thing onoff 095d6c49-9712-4220-a4c3-d3bb7a6cc5f0 "Zwave switch" [ url="zwave://0204-4519-8041/5" ]
}
```
.items file
```
String TahomaVersion "Tahoma version [%s]" { channel="somfytahoma:gateway:237dbae7:1214-4519-8041:version" }
Rollershutter RollerShutterBedroom "Roller shutter [%d %%]"  {channel="somfytahoma:rollershutter:237dbae7:31da8dac-8e09-455a-bc7a-6ed70f740001:control"}
Dimmer RollerShutterBedroomD "Roller shutter dimmer [%.1f]"  {channel="somfytahoma:rollershutter:237dbae7:31da8dac-8e09-455a-bc7a-6ed70f740001:position"}
Rollershutter RollerShutterLiving "Roller shutter [%d %%]"  {channel="somfytahoma:rollershutter:237dbae7:87bf0403-a45d-4037-b874-28f4ece30004:control" }
Dimmer RollerShutterLivingD "Roller shutter dimmer [%.1f]"  {channel="somfytahoma:rollershutter:237dbae7:87bf0403-a45d-4037-b874-28f4ece30004:position"}
Rollershutter RollerShutterHall "Roller shutter [%d %%]"  {channel="somfytahoma:rollershutter:237dbae7:68bee082-63ab-421d-9830-3ea561601234:control"}
Dimmer RollerShutterHallD "Roller shutter dimmer [%.1f]"  {channel="somfytahoma:rollershutter:237dbae7:68bee082-63ab-421d-9830-3ea561601234:position"}

Rollershutter AwningTerrace "Terrace awning [%d %%]"  {channel="somfytahoma:awning:237dbae7:24cee082-63ab-421d-9830-3ea561601234:control"}
Dimmer AwningTerraceD "Terrace awning dimmer [%.1f]"  {channel="somfytahoma:awning:237dbae7:24cee082-63ab-421d-9830-3ea561601234:position"}

Switch Rollers1UP "Rollers 1st floor UP" {channel="somfytahoma:actiongroup:237dbae7:2104c46f-478d-6543-956a-10bd93b5dc54:trigger", autoupdate="false"}
Switch Rollers1DOWN "Rollers 1st floor DOWN" {channel="somfytahoma:actiongroup:237dbae7:0b5f195a-5223-5432-b1af-f5fa1d59074f:trigger", autoupdate="false"}
Switch Rollers2UP "Rollers 2nd floor UP" {channel="somfytahoma:actiongroup:237dbae7:712c0019-b422-1234-b4da-208e249c571b:trigger", autoupdate="false"}
Switch Rollers2DOWN "Rollers 2nd floor DOWN" {channel="somfytahoma:actiongroup:237dbae7:e201637b-de3b-1234-b7af-5693811a953b:trigger", autoupdate="false"}

Switch TahomaZwaveSwitch "Switch" { channel="somfytahoma:onoff:237dbae7:095d6c49-9712-4220-a4c3-d3bb7a6cc5f0:switch" }
```