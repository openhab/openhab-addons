# Somfy Tahoma Binding

This binding integrates the
[Somfy Tahoma](https://www.somfy.fr/produits/1811478/)
and
[Somfy Connexoon](https://www.somfy.fr/produits/1811429/)
home automation systems.
Any home automation system based on the OverKiz API is potentially supported.

## Supported Things

 Currently these things are supported:

- bridge (cloud bridge, which can discover gateways, roller shutters, awnings, switches and action groups)
- gateways (gateway status)
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
- dimmer lights (light switches with intensity setting)
- light sensors (luminance value)
- occupancy sensors (OPEN/CLOSE contact)
- smoke sensors (OPEN/CLOSE contact, alarm check)
- contact sensors (OPEN/CLOSE contact)
- temperature sensors (get temperature)
- electricity sensors (get energy consumption)
- door locks (LOCK/UNLOCK, OPEN/CLOSE commands)
- heating systems (control temperature, set heating level)
- valve heating systems (control temperature, derogation mode and temperature)
- exterior heating systems (set heating level)
- alarms (both interior/external)
- pods
- docks (battery info, siren control)
- sirens (battery status full/low/normal/verylow, siren control ON/OFF, setting memorized volume)
- action groups (scenarios which can execute predefined Tahoma group of steps, e.g. send to all roller shutters DOWN command, one by one)
- thermostats (read status and battery level)
- water heater system (monitor and control)

Both Somfy Tahoma and Somfy Connexoon gateways have been confirmed working.

## Discovery

To start a discovery, just

- Add a new bridge thing.
- Configure the bridge selecting your cloud portal (www.tahomalink.com by default) and setting your email (login) and password to the cloud portal.

If the supplied credentials are correct, the automatic discovery can be used to scan and detect roller shutters, awnings, switches and action groups that will appear in your Inbox.

## Thing Configuration

To retrieve thing configuration and url parameter, just add the automatically discovered device from your inbox and copy its values from thing edit page. (the url parameter is visible on edit page only)
Please see the example below.

## Channels

| Thing                                                                         | Channel                      | Note                                                                                                                        |
|-------------------------------------------------------------------------------|------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| bridge                                                                        | N.A                          | bridge does not expose any channel                                                                                          |
| gateway                                                                       | status                       | status of your gateway                                                                                                      |
| gateway                                                                       | scenarios                    | used to run the scenarios defined in the cloud portal                                                                       |
| gate                                                                          | gate_command                 | used for controlling your gate (open, close, stop, pedestrian)                                                              |
| gate                                                                          | gate_state                   | get state of your gate (open, closed, pedestrian)                                                                           |
| gate                                                                          | gate_position                | get position (0-100%) of your gate (where supported)                                                                        |
| roller shutter, screen, venetian blind, garage door, awning, pergola, curtain | control                      | device controller which reacts to commands UP/DOWN/ON/OFF/OPEN/CLOSE/MY/STOP + closure 0-100                                |
| window                                                                        | control                      | device controller which reacts to commands UP/DOWN/ON/OFF/OPEN/CLOSE/STOP + closure 0-100                                   |
| silent roller shutter                                                         | silent_control               | similar to control channel but in silent mode                                                                               |
| venetian blind, adjustable slats roller shutter, bioclimatic pergola          | orientation                  | percentual orientation of the blind's slats, it can have value 0-100. For IO Homecontrol devices only (non RTS)             |
| venetian blind, adjustable slats roller shutter                               | closure_orientation          | percentual closure and orientation of the blind's slats, it can have value 0-100. For IO Homecontrol devices only (non RTS) |
| adjustable slats roller shutter                                               | rocker                       | used for setting the rocker position of the roller shutter, the only position allowing the slats control                    |
| bioclimatic pergola                                                           | slats                        | slats state (open/closed)                                                                                                   |
| bioclimatic pergola                                                           | pergola_command              | used for controlling biclimatic pergola (closeSlats, openSlats, stop)                                                       |
| action group                                                                  | execute_action               | switch which reacts to ON command and triggers the predefined Tahoma action                                                 |
| onoff, light                                                                  | switch                       | reacts to standard ON/OFF commands                                                                                          |
| dimmer light                                                                  | light_intensity              | sets/gets intensity of the dimmer light or ON/OFF                                                                           |
| smoke sensor, occupancy sensor, contact sensor & water sensor                 | contact                      | normal value is CLOSE, changes to OPEN when detection triggered                                                             |
| smoke sensor, occupancy sensor, contact sensor & water sensor                 | sensor_defect                | indicates the health of the sensor (dead, lowBatter, maintenanceRequired, noDefect)                                         |
| smoke sensor                                                                  | radio_battery                | maintenance radio part battery state (low, normal)                                                                          |
| smoke sensor                                                                  | sensor_battery               | maintenance sensor part battery state (absence, low, normal)                                                                |
| smoke sensor                                                                  | short_check                  | triggering the smoke sensor's short check                                                                                   |
| smoke sensor                                                                  | long_check                   | triggering the smoke sensor's long check                                                                                    |
| light sensor                                                                  | luminance                    | light luminance value in luxes                                                                                              |
| electricity sensor                                                            | energy_consumption           | energy consumption value in watts                                                                                           |
| humidity sensor                                                               | humidity                     | current relative humidity                                                                                                   |
| dock                                                                          | battery_status               | indicates running on battery (yes/no)                                                                                       |
| dock                                                                          | battery_level                | remaining battery percentage                                                                                                |
| dock                                                                          | siren_status                 | used for controlling and getting siren state (on, off, cyclic)                                                              |
| dock                                                                          | short_beep                   | testing of dock's siren - short beep                                                                                        |
| dock                                                                          | long_beep                    | testing of dock's siren - long beep                                                                                         |
| siren                                                                         | battery                      | battery level full/low/normal/verylow                                                                                       |
| siren                                                                         | onoff                        | controlling siren status ON/OFF                                                                                             |
| siren                                                                         | memorized_volume             | setting memorized volume (normal/highest)                                                                                   |
| pod                                                                           | cyclic_button                | pod cyclic button state                                                                                                     |
| pod                                                                           | battery_status               | pod battery status state                                                                                                    |
| pod                                                                           | lighting_led_pod_mode        | lighting LED pod mod state                                                                                                  |
| interior alarm                                                                | alarm_command                | used for sending commands to Somfy alarm device                                                                             |
| interior alarm                                                                | intrusion_control            | used for alarm external intrusion controlling                                                                               |
| interior alarm, myfox alarm                                                   | alarm_state                  | state of the Somfy alarm                                                                                                    |
| interior alarm                                                                | target_alarm_state           | target state of the Somfy alarm                                                                                             |
| interior alarm, myfox alarm                                                   | intrusion_state              | intrusion state of the Somfy alarm                                                                                          |
| external alarm                                                                | active_zones_state           | state of external alarm active zones                                                                                        |
| door lock                                                                     | lock                         | switch representing unlocked/locked state                                                                                   |
| door lock                                                                     | open                         | switch representing open/close state                                                                                        |
| on/off heating system                                                         | target_heating_level         | target heating level (off, eco, comfort, frostprotection)                                                                   |
| heating system                                                                | current_temperature          | current temperature of the heating system                                                                                   |
| heating system                                                                | current_state                | current state of the heating system                                                                                         |
| heating system, valve heating system, thermostat                              | target_temperature           | target temperature of the heating system                                                                                    |
| heating system, valve heating system, thermostat                              | battery_level                | battery level of the heating system                                                                                         |
| valve heating system, thermostat                                              | derogation_heating_mode      | derogation heating mode of the thermostat (away, freeze, manual, ...)                                                       |
| valve heating system, thermostat                                              | derogated_target_temperature | target temperature of the heating system                                                                                    |
| valve heating system                                                          | current_heating_mode         | current heating mode of the thermostatic valve                                                                              |
| valve heating system                                                          | open_closed_valve            | current open/closed state of the thermostatic valve                                                                         |
| valve heating system                                                          | operating mode               | operating mode of the thermostatic valve                                                                                    |
| thermostat                                                                    | heating_mode                 | standard heating mode of the thermostat (away, freeze, manual, ...)                                                         |
| thermostat                                                                    | derogation_activation        | derogation activation state (inactive, active)                                                                              |
| exterior heating system                                                       | heating_level                | heating level of the exterior heating system or ON/OFF                                                                      |
| temperature sensor                                                            | temperature                  | temperature reported by the sensor                                                                                          |
| myfox camera, myfox alarm                                                     | cloud_status                 | cloud connection status                                                                                                     |
| myfox camera                                                                  | shutter                      | controlling of the camera shutter                                                                                           |
| myfox alarm                                                                   | myfox_alarm_command          | used for sending commands to Somfy Myfox alarm device                                                                       |
| waterheatersystem                                                             | middlewater_temperature      | Number:Temperature indicating the temperature of the water at the middle of the heater |
| waterheatersystem                                                             | boost_mode                   | Switch allowing to enable or disable the booster. When switching to ON, by default, the Boost duration will be set for 1 day.|
| waterheatersystem                                                             | away_mode                    | Defines if away mode is On or Off (no water heating) |
| waterheatersystem                                                             | away_mode_duration           | Defines if away mode the duration in days. |
| waterheatersystem                                                             | boost_mode_duration          | The duration of the Boost mode in days. Valid from 1 to 7. |
| waterheatersystem                                                             | power_heatpump               | Current consumption/power of the heatpump in Watts. |
| waterheatersystem                                                             | power_heatelec               | Current consumption/power of the electric resistance in Watts. |
| waterheatersystem                                                             | showers                      | Virtual channel, representing the number of desired showers - between 3 to 5. It actually switches the desired temperature to 50.0, 54.5 or 62.0 Celcius degrees. Please note that in ECO mode, only 3 and 4 showers are allowed. |
| waterheatersystem                                                             | heat_pump_operating_time     | Number of hours the heatpump has been operating |
| waterheatersystem                                                             | electric_booster_operating_time | number of hours the electric booster has been operating. |
| waterheatersystem                                                             | mode                         | The current mode of the boiler. Can be: autoMode / manualEcoInactive / manualEcoActive |
| waterheatersystem                                                             | target_temperature           | Water target temperature in degrees. Read only. Temperature desired is managed through mode and showers channels. |

To run a scenario inside a rule for example, the ID of the scenario will be required.
You can list all the scenarios IDs with the following console command: `somfytahoma <bridgeUID> scenarios`.

### Remarks

All things which have a RSSI (received signal strength indication) state, expose a channel "rssi".

When a roller shutter-like thing receives STOP command, there are two possible behaviours

- when the roller shutter is idle then MY command is interpreted (the roller shutter/exterior screen/awning goes to your favourite position)
- when the roller shutter is moving then STOP command is interpreted (the roller shutter/exterior screen/awning stops)

If you want to set the MY position of a roller shutter and you don't care the possible movement, try sending the MOVE command (OH2 does not know MY, so it stands for "move to MY position")

```
CONTROL_CHANNEL.sendCommand(MOVE)
```

Blinds and adjustable slats roller shutters can control their closure and orientation by sending a comma separated string consisting of closure (0-100) and orientation (0-100) to the "closure_orientaion" channel.

```
CLOSURE_ORIENTATION_CHANNEL.sendCommand("50,50")
```

## Full Example

.things file

```
Bridge somfytahoma:bridge:237dbae7 "Somfy Tahoma Bridge" [ email="my@email.com", password="MyPassword", refresh=10 , statusTimeout=30] {
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
    Thing electricitysensor 9998e6ff-c17e-40d7-a4b4-3e797eca5bf7 "Electricity sensor" [ url="io://0204-4510-8041/288702124" ]
    Thing dock 1212f2e3-bcde-21dd-b3a6-13ef7abcd134 "Dock" [ url="io://0204-4510-8041/244402124" ]
    Thing siren 1212f2e3-aeae-21dd-b3a6-13ef7abcd134 "Siren" [ url="io://0204-4510-8041/244405678" ]
    Thing extheatingsystem 1212f2e3-aeae-21dd-b3a6-13ef7abcd155 "Ext heating system" [ url="io://0204-4510-8041/144405678" ]
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

Switch DimmerLightSwitch "Dimmer Light Switch" { channel="somfytahoma:dimmerlight:237dbae7:1b8e7d29-bf1e-4ae1-9432-3dfef52ef14e:light_intensity" }
Dimmer DimmerLightIntensity "Dimmer Light intensity [%.1f]"  {channel="somfytahoma:dimmerlight:237dbae7:1b8e7d29-bf1e-4ae1-9432-3dfef52ef14e:light_intensity"}

Number LightSensor "Light Sensor [%.1f lux]" { channel="somfytahoma:lightsensor:237dbae7:2c90808c3a0c193f013a743f2f660f12:luminance" }
Number:Energy EnergyConsumptionSensor "Energy Consumption [%.1f W]" { channel="somfytahoma:electricitysensor:237dbae7:9998e6ff-c17e-40d7-a4b4-3e797eca5bf7:energy_consumption" }

Contact OccupancySensor "Occupancy Sensor is [%s]" { channel="somfytahoma:occupancysensor:237dbae7:995e16ca-07c4-4111-9cda-504cb5120f82:contact" }
Contact SmokeSensor "Smoke Sensor is [%s]" { channel="somfytahoma:smokesensor:237dbae7:9438e6ff-c17e-40d7-a4b4-3e797eca5bf7:contact" }
Contact ContactSensor "Contact Sensor is [%s]" { channel="somfytahoma:contactsensor:237dbae7:6612f2e3-d23d-21dd-b3a6-13ef7abcd134:contact" }

Number TemperatureSensor "Temperature is [%2.1f °C]" { channel="somfytahoma:temperaturesensor:237dbae7:6612f2e3-d23d-21dd-b4a7-13ef7abcd134:temperature" }
String HeatingSystemLevel "Heating level [%s]" { channel="somfytahoma:onoffheatingsystem:237dbae7:6612f2e3-abcd-21dd-b3a6-13ef7abcd134:target_heating_level"}

Switch DoorLock "Lock" { channel="somfytahoma:doorlock:237dbae7:6612f2e3-bcde-21dd-b3a6-13ef7abcd134:lock" }
Switch DoorLockOpenClose "Open/Close" { channel="somfytahoma:doorlock:237dbae7:6612f2e3-bcde-21dd-b3a6-13ef7abcd134:open" }

String DockBatteryStatus "Dock battery status [%s]" { channel="somfytahoma:dock:237dbae7:1212f2e3-bcde-21dd-b3a6-13ef7abcd134:battery_status" }
String DockBatteryLevel "Dock battery level [%s]" { channel="somfytahoma:dock:237dbae7:1212f2e3-bcde-21dd-b3a6-13ef7abcd134:battery_level" }
String DockSiren "Dock siren [%s]" { channel="somfytahoma:dock:237dbae7:1212f2e3-bcde-21dd-b3a6-13ef7abcd134:siren" }
Switch DockShortBeep "Dock short beep" { channel="somfytahoma:dock:237dbae7:1212f2e3-bcde-21dd-b3a6-13ef7abcd134:short_beep" }
Switch DockLongBeep "Dock long beep" { channel="somfytahoma:dock:237dbae7:1212f2e3-bcde-21dd-b3a6-13ef7abcd134:long_beep" }

String SirenBattery "Siren battery [%s]" { channel="somfytahoma:siren:237dbae7:1212f2e3-aeae-21dd-b3a6-13ef7abcd134:battery" }
Switch SirenSwitch "Siren switch" { channel="somfytahoma:siren:237dbae7:1212f2e3-aeae-21dd-b3a6-13ef7abcd134:onoff" }
String SirenVolume "Siren volume [%s]" { channel="somfytahoma:siren:237dbae7:1212f2e3-aeae-21dd-b3a6-13ef7abcd134:memorized_volume" }

Dimmer HeatingLevel "Ext heating level [%.1f]"  { channel="somfytahoma:exteriorheatingsystem:237dbae7:1212f2e3-aeae-21dd-b3a6-13ef7abcd155:heating_level" }
Switch HeatingSwitch "Ext heating switch"  { channel="somfytahoma:exteriorheatingsystem:237dbae7:1212f2e3-aeae-21dd-b3a6-13ef7abcd155:heating_level" }
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
Switch item=DimmerLightSwitch
Slider item=DimmerLightIntensity
Text item=LightSensor
Text item=OccupancySensor
Text item=SmokeSensor
Text item=ContactSensor
Text item=TemperatureSensor
Text item=ElectricitySensor
Switch item=HeatingSystemOnOff
Selection item=HeatingSystemLevel mappings=["frostprotection"="FROST PROTECTION", "comfort"="COMFORT", "eco"="ECO", "off"="OFF"]
Switch item=DoorLock
Switch item=DoorLockOpenClose
Text item=DockBatteryStatus
Text item=DockBatteryLevel
Selection item=DockSiren mappings=["off"="OFF", "on"="ON", "cyclic"="CYCLIC"]
Switch item=DockShortBeep
Switch item=DockLongBeep
String item=SirenBattery
Switch item=SirenSwitch
Selection item=SirenVolume mappings=["normal"="NORMAL", "highest"="HIGHEST"]
Slider item=HeatingLevel
```

## Alexa compatibility

This binding is compatible with the official Alexa Smart Home Skill.
Since Rolleshutter items are unsupported, only Dimmer with control channel can be used.
Syntax in .item file is as follows:

```
Dimmer RollerShutterLivingD "Roller shutter living [%.1f]"  [ "Lighting" ] {channel="somfytahoma:rollershutter:237dbae7:87bf0403-a45d-4037-b874-28f4ece30004:control"}
```

Alexa can set the roller shutter (awning, blind, ...) to a specific position as well as send ON (interpretted as UP) and OFF commands (interpretted as DOWN).
