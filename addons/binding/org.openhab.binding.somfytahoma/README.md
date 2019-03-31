# Somfy Tahoma Binding

This binding integrates the [Somfy Tahoma](https://www.somfy.fr/produits/domotique/maison-connectee-tahoma) and [Somfy Connexoon](https://www.somfy.fr/produits/domotique/equipements-connectes-connexoon) home automation systems.

## Supported Things

 Currently these things are supported:

- bridge (Somfy Tahoma bridge, which can discover gateways, roller shutters, awnings, switches and action groups)
- gateways (Somfy Tahoma gateway - gateway status)
- gates (control gate, get state)
- roller shutters (UP, DOWN, STOP control of a roller shutter). IO Homecontrol devices are allowed to set exact position of a shutter (0-100%)
- blinds (UP, DOWN, STOP control of a blind). IO Homecontrol devices are allowed to set exact position of a blinds (0-100%) as well as orientation of slats (0-100%)
- screens (UP, DOWN, STOP control of a screen). IO Homecontrol devices are allowed to set exact position of a screen (0-100%)
- garage doors (UP, DOWN, STOP control of a garage door). IO Homecontrol devices are allowed to set exact position of a garage door (0-100%)
- awnings (UP, DOWN, STOP control of an awning). IO Homecontrol devices are allowed to set exact position of an awning (0-100%)
- windows (UP, DOWN, STOP control of a window). IO Homecontrol devices are allowed to set exact position of a window (0-100%)
- pergolas (UP, DOWN, STOP control of a pergola). IO Homecontrol devices are allowed to set exact position of a pergola (0-100%)
- on/off switches (connected by RTS, IO protocol or supported by USB stick - z-wave, enocean, ..)
- light switches (similar to on/off)
- light sensors (luminance value)
- occupancy sensors (OPEN/CLOSE contact)
- smoke sensors (OPEN/CLOSE contact)
- contact sensors (OPEN/CLOSE contact)
- temperature sensor (get temperature)
- door locks (LOCK/UNLOCK, OPEN/CLOSE commands)
- heating systems (control temperature, set heating level)
- alarms (both interior/external)
- pods
- action groups (scenarios which can execute predefined Tahoma group of steps, e.g. send to all roller shutters DOWN command, one by one)

Both Somfy Tahoma and Somfy Connexoon gateways have been confirmed working.

## Discovery

To start a discovery, just
 
- install this binding
- open Paper UI
- add a new thing in menu Configuration/Things
- choose SomfyTahoma Binding and select Somfy Tahoma Bridge
- enter your email (login) and password to the TahomaLink cloud portal
 
If the supplied TahomaLink credentials are correct, the automatic discovery starts immediately and detected roller shutters, awnings, switches and action groups appear in Paper UI inbox. 

## Thing Configuration

To manually configure the thing you have to specify bridge and things in *.things file in conf/addons directory of your openHAB 2.x installation.
To manually link the thing channels to items just use the *.items file in conf/items directory of your openHAB 2.x installation. 
To retrieve thing configuration and url parameter, just add the automatically discovered device from your inbox and copy its values from thing edit page. (the url parameter is visible on edit page only)
Please see the example below.

## Channels

| Thing         | Channel      | Note  |
| ------------- |:-------------:| -----:|
| bridge        | N.A |  bridge does not expose any channel |
| gateway       | status  | status of your Tahoma gateway |
| gate          | gate_command | used for controlling your gate (open, close, stop, pedestrian) |
| gate          | gate_state | get state of your gate |
| roller shutter, screen, venetian blind, garage door, awning, window, pergola | control |  device controller which reacts to commands UP/DOWN/STOP + closure 0-100 |
| venetian blind | orientation | percentual orientation of the blind's slats, it can have value 0-100). For IO Homecontrol devices only (non RTS)|
| action group | execute_action | switch which reacts to ON command and triggers the predefined Tahoma action |
| onoff, light | switch | reacts to standard ON/OFF commands |
| smoke sensor, occupancy sensor & contact sensor | contact | normal value is CLOSE, changes to OPEN when detection triggered |
| light sensor | luminance | light luminance value in luxes |
| pod | cyclic_button_state | pod cyclic button state |
| pod | battery_status_state | pod battery status state |
| pod | lighting_led_pod_mod_state | lighting led pod mod state |
| interior alarm | alarm_command | used for sending commands to Somfy alarm device |
| interior alarm | intrusion_control | used for alarm external intrusion controlling |
| interior alarm | alarm_state | state of the Somfy alarm |
| interior alarm | target_alarm_state | target state of the Somfy alarm |
| interior alarm | intrusion_state | intrusion state of the Somfy alarm |
| external alarm | active_zones_state | state of external alarm active zones |
| door lock | lock | switch representing unlocked/locked state |
| door lock | open | switch representing open/close state |
| on/off heating system | target_heating_level | target heating level (off, eco, comfort, frostprotection) |
| heating system | current_temperature | current temperature of the heating system |
| heating system | current_state | current state of the heating system|
| heating system | target_temperature | target temperature of the heating system |
| heating system | battery_level | battery level of the heating system |
| temperature sensor | temperature | temperature reported by the sensor |

When roller shutter-like thing receives STOP command two possible behaviours are possible

- when the roller shutter is idle then MY command is interpreted (the roller shutter/exterior screen/awning goes to your favourite position)
- when the roller shutter is moving then STOP command is interpreted (the roller shutter/exterior screen/awning stops)


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
    Thing light 1b8e7d29-bf1e-4ae1-9432-3dfef52ef14d "Light switch" [ url="enocean://0204-4519-8041/4294453515/2" ]
    Thing lightsensor 2c90808c3a0c193f013a743f2f660f12 "Light sensor" [ url="io://0204-4519-8041/13527450" ]
    Thing occupancysensor 995e16ca-07c4-4111-9cda-504cb5120f82 "Occupancy sensor" [ url="io://0204-4519-8041/4855758" ]
    Thing smokesensor 9438e6ff-c17e-40d7-a4b4-3e797eca5bf7 "Smoke sensor" [ url="io://0204-4510-8041/13402124" ]
}
```

Awnings, garage doors, screens, blinds, and windows things have the same notation as roller shutters. Just use "awning", "garagedoor", "screen", "blind" or "window" instead of "rolleshutter" in thing definition. 

.items file

```
String TahomaVersion "Tahoma version [%s]" { channel="somfytahoma:gateway:237dbae7:1214-4519-8041:version" }
Rollershutter RollerShutterBedroom "Roller shutter [%d %%]"  {channel="somfytahoma:rollershutter:237dbae7:31da8dac-8e09-455a-bc7a-6ed70f740001:control"}
Dimmer RollerShutterBedroomD "Roller shutter dimmer [%.1f]"  {channel="somfytahoma:rollershutter:237dbae7:31da8dac-8e09-455a-bc7a-6ed70f740001:control"}
Rollershutter RollerShutterLiving "Roller shutter [%d %%]"  {channel="somfytahoma:rollershutter:237dbae7:87bf0403-a45d-4037-b874-28f4ece30004:control" }
Dimmer RollerShutterLivingD "Roller shutter dimmer [%.1f]"  {channel="somfytahoma:rollershutter:237dbae7:87bf0403-a45d-4037-b874-28f4ece30004:control"}
Rollershutter RollerShutterHall "Roller shutter [%d %%]"  {channel="somfytahoma:rollershutter:237dbae7:68bee082-63ab-421d-9830-3ea561601234:control"}
Dimmer RollerShutterHallD "Roller shutter dimmer [%.1f]"  {channel="somfytahoma:rollershutter:237dbae7:68bee082-63ab-421d-9830-3ea561601234:control"}

Rollershutter AwningTerrace "Terrace awning [%d %%]"  {channel="somfytahoma:awning:237dbae7:24cee082-63ab-421d-9830-3ea561601234:control"}
Dimmer AwningTerraceD "Terrace awning dimmer [%.1f]"  {channel="somfytahoma:awning:237dbae7:24cee082-63ab-421d-9830-3ea561601234:control"}

Switch Rollers1UP "Rollers 1st floor UP" {channel="somfytahoma:actiongroup:237dbae7:2104c46f-478d-6543-956a-10bd93b5dc54:execute_action", autoupdate="false"}
Switch Rollers1DOWN "Rollers 1st floor DOWN" {channel="somfytahoma:actiongroup:237dbae7:0b5f195a-5223-5432-b1af-f5fa1d59074f:execute_action", autoupdate="false"}
Switch Rollers2UP "Rollers 2nd floor UP" {channel="somfytahoma:actiongroup:237dbae7:712c0019-b422-1234-b4da-208e249c571b:execute_action", autoupdate="false"}
Switch Rollers2DOWN "Rollers 2nd floor DOWN" {channel="somfytahoma:actiongroup:237dbae7:e201637b-de3b-1234-b7af-5693811a953b:execute_action", autoupdate="false"}

Switch TahomaZwaveSwitch "Switch" { channel="somfytahoma:onoff:237dbae7:095d6c49-9712-4220-a4c3-d3bb7a6cc5f0:switch" }
Switch TahomaLightSwitch "Light Switch" { channel="somfytahoma:light:237dbae7:1b8e7d29-bf1e-4ae1-9432-3dfef52ef14d:switch" }

Number LightSensor "Light Sensor [%.1f lux]" { channel="somfytahoma:lightsensor:237dbae7:2c90808c3a0c193f013a743f2f660f12:luminance" }

Contact OccupancySensor "Occupancy Sensor is [%s]" { channel="somfytahoma:occupancysensor:237dbae7:995e16ca-07c4-4111-9cda-504cb5120f82:contact" }
Contact SmokeSensor "Smoke Sensor is [%s]" { channel="somfytahoma:smokesensor:237dbae7:9438e6ff-c17e-40d7-a4b4-3e797eca5bf7:contact" }
Contact ContactSensor "Contact Sensor is [%s]" { channel="somfytahoma:contactsensor:237dbae7:6612f2e3-d23d-21dd-b3a6-13ef7abcd134:contact" }

Number TemperatureSensor "Temperature is [%2.1f °C]" { channel="somfytahoma:temperaturesensor:237dbae7:6612f2e3-d23d-21dd-b4a7-13ef7abcd134:temperature" }
String HeatingSystemLevel "Heating level [%s]" { channel="somfytahoma:onoffheatingsystem:237dbae7:6612f2e3-abcd-21dd-b3a6-13ef7abcd134:target_heating_level"}

Switch DoorLock "Lock" { channel="somfytahoma:doorlock:237dbae7:6612f2e3-bcde-21dd-b3a6-13ef7abcd134:lock" }
Switch DoorLockOpenClose "Open/Close" { channel="somfytahoma:doorlock:237dbae7:6612f2e3-bcde-21dd-b3a6-13ef7abcd134:open" }

```

.sitemap file

```
Text item=TahomaVersion
Switch item=Rollers1UP label="Roller shutters 1st floor" mappings=[ON="UP"]
Switch item=Rollers1DOWN  label="Roller shutters 1st floor" mappings=[ON="DOWN"]
Switch item=Rollers2UP label="Roller shutters 2nd floor" mappings=[ON="UP"]
Switch item=Rollers2DOWN  label="Roller shutters 2nd floor" mappings=[ON="DOWN"]
Switch item=RollerShutterBedroom
Slider item=RollerShutterBedroomD
Switch item=RollerShutterLiving
Slider item=RollerShutterLivingD
Switch item=TahomaZwaveSwitch
Switch item=TahomaLightSwitch
Text item=LightSensor
Text item=OccupancySensor
Text item=SmokeSensor
Text item=ContactSensor
Text item=TemperatureSensor
Switch item=HeatingSystemOnOff
Selection item=HeatingSystemLevel mappings=["frostprotection"="FROST PROTECTION", "comfort"="COMFORT", "eco"="ECO", "off"="OFF"]
Switch item=DoorLock
Switch item=DoorLockOpenClose
```

## Alexa compatibility

This binding is compatible with the official Alexa Smart Home Skill. 
Since Rolleshutter items are unsupported, only Dimmer with control channel can be used.
Syntax in .item file is as follows:

```
Dimmer RollerShutterLivingD "Roller shutter living [%.1f]"  [ "Lighting" ] {channel="somfytahoma:rollershutter:237dbae7:87bf0403-a45d-4037-b874-28f4ece30004:control"}
```

Alexa can set the roller shutter (awning, blind, ...) to a specific position as well as send ON (interpretted as UP) and OFF commands (interpretted as DOWN).
