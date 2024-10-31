# DIRIGERA Binding

Binding supporting the DIRIGERA Gateway from IKEA. 

It provides devices and scenes towards openHAB. 
With this it's possible to connect them with other devices outside of the IKEA world.

The goal is not to provide similar functionality of the IKEA Home Smart App like create / remove scenes, rename devices or handling rooms.

## Supported Things

The DIRIGERA `bridge` is providing the connection to all devices and scenes.  

Refer to below sections which devices are supported and are covered by `things` connected to the DIRIGERA bridge.

| ThingTypeUUID         | Description                                                | Tested       | Section                                   | Products                                  |
|-----------------------|------------------------------------------------------------|--------------|-------------------------------------------|-------------------------------------------|
| `gateway`             | IKEA Gateway for smart products                            | personally   | [Gateway](#gateway-channels)              | DIRIGERA                                  |
| `air-purifier`        | Air cleaning device with particle filter                   | no           | [Air Purifier](#air-purifier)             | STARKVIND                                 |
| `air-quality`         | Air measure for temperature, humidity and particles        | no           | [Sensors](#air-quality-sensor)            | VINDSTYRKA                                |
| `blind`               | Window or door blind                                       | no           | [Blinds](#blinds)                         | PRAKTLYSING ,KADRILJ ,FRYKTUR, TREDANSEN  |
| `blind-controller`    | Controller to open and close blinds                        | no           | [Controller](#blind-controller)           | TRÅDFRI                                   |
| `dimmable-light`      | Light with brightness support                              | no           | [Lights](#dimmable-lights)                | TRÅDFRI                                   |
| `temperature-light`   | Light with color temperature support                       | personally   | [Lights](#temperature-lights)             | TRÅDFRI, FLOALT                           |
| `color-light`         | Light with color support                                   | personally   | [Lights](#color-lights)                   | TRÅDFRI, ORMANÅS                          |
| `light-controller`    | Controller to handle light attributes                      | personally   | [Controller](#light-controller)           | TRÅDFRI, RODRET,STYRBAAR                  |
| `motion-sensor`       | Sensor detecting motion events                             | no           | [Sensors](#motion-sensor)                 | TRÅDFRI                                   |
| `motion-light-sensor` | Sensor detecting motion events and measures light level    | personally   | [Sensors](#motion-light-sensor)           | VALLHORN                                  |
| `single-shortcut`     | Shortcut controller with one button                        | no           | [Controller](#single-shortcut-controller) | TRÅDFRI                                   |
| `double-shortcut`     | Shortcut controller with two buttons                       | personally   | [Controller](#double-shortcut-controller) | SOMRIG                                    |
| `simple-plug`         | Switchable power plug                                      | no           | [Plugs](#simple-plug)                     | TRÅDFRI, ÅSKVÄDER                         |
| `power-plug`          | Switchable power plug with status light and child lock     | personally   | [Plugs](#power-plug)                      | TRETAKT                                   |
| `smart-plug`          | Switchable plug with electricity measurements              | personally   | [Plugs](#smart-power-plug)                | INSPELNING                                |
| `speaker`             | Speaker with player activities                             | personally   | [Speaker](#speaker)                       | SYMFONISK                                 |
| `sound-controller`    | Controller for speakers                                    | no           | [Controller](#sound-controller)           | SYMFONISK, TRÅDFRI                        |
| `contact-sensor`      | Sensor tracking if windows or doors are open               | personally   | [Sensors](#contact-sensor)                | PARASOLL                                  |
| `water-sensor`        | Sensor to detect water leaks                               | no           | [Sensors](#water-sensor)                  | BADRING                                   |
| `repeater`            | Repeater to strengthen signal                              | personally   | [Repeater](#repeater)                     | TRÅDFRI                                   |
| `scene`               | Scene from IKEA home smart App which can be triggered      | personally   | [Scenes](#scenes)                         | -                                         |

## Discovery

The discovery will try to identify your DIRIGERA Gateway. 
This may take some time because your whole network is scanned in order to find it.

**Before adding the bridge** read [Pairing section](#gateway-pairing).   

Devices connected to this bridge will be detected automatically unless you don't switch it off in [Bridge Configuration](#bridge-configuration)

## Gateway Bridge

### Bridge Configuration

| Name            | Type    | Description                                                | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------|---------|----------|----------|
| `ipAddress`     | text    | DIRIGERA IP Address                                        | N/A     | yes      | no       |
| `id`            | text    | Unique id of this gateway                                  | N/A     | no       | no       |
| `discovery`     | boolean | Configure if paired devices shall be detected by discovery | true    | no       | no       |

- ipAddress - use discovery to obtain this value automatically or enter it manually if known
- id - will be detected automatically after successful pairing
- discovery - will run continuously in the background and detect new, deleted or changed devices. Switch it off to deactivate discovery

### Gateway Pairing

First setup requires pairing the DIRIGERA gateway with openHAB.
You need physical access to the gateway to finish pairing so ensure you can reach it quickly.

Let's start pairing

1. Add the bridge found in discovery 
2. Pairing started automatically after creation!
3. Press the button on the DIRIGERA rear site
4. Your brdige shall switch to ONLINE 

### Gateway Channels

| Channel         | Type      | Read/Write | Description                                  | Advanced |
|-----------------|-----------|------------|----------------------------------------------|----------|
| `pairing`       | Switch    | RW         | Sets DIRIGERA hub into pairing mode          |          |
| `sunrise`       | DateTime  | R          | Date and time of next sunrise                |          |
| `sunset`        | DateTime  | R          | Date and time of next sunset                 |          |
| `statistics`    | String    | R          | Several statistics about gateway activities  |          |
| `ota-status`    | Number    | R          | Over-the-air overall status                  |    X     |
| `ota-state`     | Number    | R          | Over-the-air current state                   |    X     |
| `ota-progress`  | Number    | R          | Over-the-air current progress                |    X     |
| `json`          | String    | R          | JSON structure and updates of this device    |    X     |

### OTA Mappings

Mappings for `ota-status`

- 0 : Up to date
- 1 : Update available

Mappings for `ota-state`

- 0 : Ready to check
- 1 : Check in progress
- 2 : Ready to download
- 3 : Download in progress
- 4 : Update in progress
- 5 : Update failed
- 6 : Ready to update
- 7 : Check failed
- 8 : Download failed
- 9 : Update complete
- 10 : Battery check failed

## Things

The binding is in development phase alpha.
Goal is to extend testing in the community to cover as many as possible old and new devices.
Your help is needed to extend and fix the current implementation.

### Unknown Devices

Filter your traces regarding 'DIRIGERA MODEL Unsuppoerted Device'. 
The trace cotains a json object at the end which is needed to implememnt a corresponding hanlder.

### Problem with Device

Each device has 'json' channel which is reflecting the structural data of a device. 
If you see wrong, missing or too much channels this data is needed to adapt implementation.
This channel is only for development purposes.

| Channel   | Type   | Read/Write | Description                                  | Advanced |
|-----------|--------|------------|----------------------------------------------|----------|
| `json`    | String | R          | JSON structure and updates of this device    |    X     |


### Thing Configuration

Each thing is identified by a unique id which is mandatory to configure.
Discovery will automatically identify the id.

| Name              | Type    | Description                         | Default | Required |
|-------------------|---------|-------------------------------------|---------|----------|
| `id`              | text    | Unique id of this device / scene    | N/A     | yes      |


## Air Purifier

Air cleaning device with particle filter.

| Channel               | Type              | Read/Write | Description                                  | Advanced |
|-----------------------|-------------------|------------|----------------------------------------------|----------|
| `fan-mode`            | Number            | RW         | Fan on, off, speed or automatic behavior     |          |
| `motor-time`          | Number:Time       | R          | Motor runtime in minutes                     |          |
| `filter-elapsed`      | Number:Time       | R          | Filter elapsed time in minutes               |          |
| `filter-reamin`       | Number:Time       | R          | Time to filter replacement in minutes        |          |
| `filter-lifetime`     | Number:Time       | R          | Filter lifetime in minutes                   |          |
| `filter-alarm`        | Switch            | R          | Filter alarm signal                          |          |
| `particulate-matter`  | Number:Density    | R          | Category 2.5 particulate matter              |          |
| `disable-light`       | Switch            | RW         | Disable status light on plug                 |          |
| `child-lock`          | Switch            | RW         | Child lock for button on plug                |          |
| `ota-status`          | Number            | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number            | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number            | R          | Over-the-air current progress                |    X     |
| `json`                | String            | R          | JSON structure and updates of this device    |    X     |

### Air Purifier Channel Mappings

Mappings for `fan-mode`

- 0 : Auto
- 1 : Low
- 2 : Medium
- 3 : High
- 4 : On
- 5 : Off

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Blinds

Window or door blind.

| Channel               | Type                  | Read/Write | Description                                      | Advanced |
|-----------------------|-----------------------|------------|--------------------------------------------------|----------|
| `blind-state`         | Number                | RW         | State if blind is moving up, down or stopped     |          |
| `target-level`        | Dimmer                | RW         | Target blind level                               |          |
| `current-level`       | Dimmer                | R          | Current blind level                              |          |
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering blind              |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                      |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                       |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                    |    X     |
| `json`                | String                | R          | JSON structure and updates of this device        |    X     |

 
#### Blind Channel Mappings

Mappings for `blind-state`

- 0 : Stopped
- 1 : Up
- 2 : Down

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Lights

Light devices in several variants.
Can be light bulbs, LED stripes, remote driver and more.


## Dimmable Lights

Light with brightness support.

| Channel               | Type                  | Read/Write | Description                                      | Advanced |
|-----------------------|-----------------------|------------|--------------------------------------------------|----------|
| `power-state`         | Switch                | RW         | Power state of light                             |          |
| `brightness`          | Dimmer                | RW         | Control brightness of light                      |          |
| `startup`             | Number                | RW         | Startup behavior after power cutoff              |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                      |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                       |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                    |    X     |
| `json`                | String                | R          | JSON structure and updates of this device        |    X     |

### Startup Channel Mappings

The startup defines how the device shall behave after a power cutoff.
If there's a dedicated hardwired light switch which cuts power towards the bulb it makes sense to sitch them on every time the switch is pressed.
But it's also possible to recover the last state.

'startup'

- 0 : Previous
- 1 : On
- 2 : Off
- 2 : Switch

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Temperature Lights

Light with color temperature support.

| Channel               | Type                  | Read/Write | Description                                      | Advanced |
|-----------------------|-----------------------|------------|--------------------------------------------------|----------|
| `power-state`         | Switch                | RW         | Power state of light                             |          |
| `brightness`          | Dimmer                | RW         | Control brightness of light                      |          |
| `temperature`         | Dimmer                | RW         | Control temperature of light from cold to warm   |          |
| `startup`             | Number                | RW         | Startup behavior after power cutoff              |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                      |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                       |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                    |    X     |
| `json`                | String                | R          | JSON structure and updates of this device        |    X     |

See [sartup mappings](#startup-channel-mappings) for device startup behavior.
See [OTA channel mappings](#ota-mappings) for over the air updates.

## Color Lights

Light with color support.

| Channel               | Type                  | Read/Write | Description                                          | Advanced |
|-----------------------|-----------------------|------------|------------------------------------------------------|----------|
| `power-state`         | Switch                | RW         | Power state of light                                 |          |
| `color`               | Color                 | RW         | Control light with color, saturation and brightness  |          |
| `startup`             | Number                | RW         | Startup behavior after power cutoff                  |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                          |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                           |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                        |    X     |
| `json`                | String                | R          | JSON structure and updates of this device            |    X     |

See [sartup mappings](#startup-channel-mappings) for device startup behavior.
See [OTA channel mappings](#ota-mappings) for over the air updates.

## Power Plugs

Power plugs in different variants.

## Simple Plug

Simple plug with controler of power state and startup behavior.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `power-state`         | Switch                | RW         | Power state of plug                          |          |
| `startup`             | Number                | RW         | Startup behavior after power cutoff          |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [sartup mappings](#startup-channel-mappings) for device startup behavior.
See [OTA channel mappings](#ota-mappings) for over the air updates.

## Power Plug

Power plug with control of power state, startup behavior, hardware on/off button and status light.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `power-state`         | Switch                | RW         | Power state of plug                          |          |
| `child-lock`          | Switch                | RW         | Child lock for button on plug                |          |
| `disable-light`       | Switch                | RW         | Disable status light on plug                 |          |
| `startup`             | Number                | RW         | Startup behavior after power cutoff          |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [sartup mappings](#startup-channel-mappings) for device startup behavior.
See [OTA channel mappings](#ota-mappings) for over the air updates.

## Smart Power Plug

Smart plug like [Power Plug](#power-plug) plus measuring capability.

| Channel               | Type                      | Read/Write | Description                                  | Advanced |
|-----------------------|---------------------------|------------|----------------------------------------------|----------|
| `power-state`         | Switch                    | RW         | Power state of plug                          |          |
| `child-lock`          | Switch                    | RW         | Child lock for button on plug                |          |
| `disable-light`       | Switch                    | RW         | Disable status light on plug                 |          |
| `power`               | Number:Power              | R          | Electric power delivered by plug             |          |
| `energy-total`        | Number:Energy             | R          | Total energy consumption                     |          |
| `energy-reset`        | Number:Energy             | R          | Energy consumption since last rese           |          |
| `ampere`              | Number:ElectricCurrent    | R          | Electric current measured by plug            |          | 
| `voltage`             | Number:ElectricPotential  | R          | Electric potential of plug                   |          |
| `startup`             | Number                    | RW         | Startup behavior after power cutoff          |          |
| `ota-status`          | Number                    | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                    | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                    | R          | Over-the-air current progress                |    X     |
| `json`                | String                    | R          | JSON structure and updates of this device    |    X     |

See [sartup mappings](#startup-channel-mappings) for device startup behavior.
See [OTA channel mappings](#ota-mappings) for over the air updates.


## Sensors

Various sensors for detecting events and measuring.

## Motion Sensor

Sensor detecting motion events.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `detection`           | Switch                | R          | Flag if detection happened                   |          |
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Motion Light Sensor

Sensor detecting motion events and measures light level.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `detection`           | Switch                | R          | Flag if detection happened                   |          |
| `illuminance`         | Number:Illuminance    | R          | Illuminance in Lux                           |          |
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

## Water Sensor

Sensor to detect water leaks.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `detection`           | Switch                | R          | Flag if detection happened                   |          |
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Contact Sensor

Sensor tracking if windows or doors are open

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `contact`             | Contact               | R          | State if door or window is open or closed    |          |
| `illuminance`         | Number:Illuminance    | R          | Illuminance in Lux                           |          |
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

## Air Quality Sensor

Air measure for temperature, humidity and particles.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `temperature`         | Number:Temperature    | R          | Air Temperature                              |          |
| `humidity`            | Number:Dimensionless  | R          | Air Humidity                                 |          |
| `particulate-matter`  | Number:Density        | R          | Category 2.5 particulate matter              |          |
| `voc-index`           | Number:Density        | R          | Volatile organic compounds measure           |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Controller

Controller for lights, plugs, blinds, shortcuts and speakers.

## Single Shortcut Controller

Shortcut controller with one button.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `button1`             | trigger               | R          | Trigger of first button                      |          |
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

### Button Triggers

Triggers for `button1`

- PRESSED
- DOUBLE_PRESSED
- LONG_PRESSED

## Double Shortcut Controller

Shortcut controller with two buttons.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `button1`             | trigger               | R          | Trigger of first button                      |          |
| `button2`             | trigger               | R          | Trigger of second button                     |          |
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

Triggers for `button1` and `button2`

- PRESSED
- DOUBLE_PRESSED
- LONG_PRESSED

## Light Controller

Controller to handle light attributes.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Blind Controller

Controller to open and close blinds.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Sound Controller

Controller for speakers.

| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `battery-level`       | Number:Dimensionless  | R          | State of the battery powering the sensor     |          |
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

See [OTA channel mappings](#ota-mappings) for over the air updates.

## Speaker

Speaker with player activities.
 
| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `player`              | Player                | RW         | Player Control                               |          |
| `volume`              | Dimmer                | RW         | Handle volume in percent                     |          |
| `mute`                | Switch                | RW         | Mute current audio without stop playing      |          |
| `shuffle`             | Switch                | RW         | Control shuffle mode                         |          |
| `crossfade`           | Switch                | RW         | Cross fading between tracks                  |          |
| `repeat`              | Number                | RW         | Over-the-air overall status                  |    X     |
| `track`               | String                | R          | Current playing track                        |    X     |
| `image`               | RawType               | R          | Current playing track image                  |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

## Repeater

Repeater to strengthen signal.
 
| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `ota-status`          | Number                | R          | Over-the-air overall status                  |    X     |
| `ota-state`           | Number                | R          | Over-the-air current state                   |    X     |
| `ota-progress`        | Number                | R          | Over-the-air current progress                |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

## Scenes

Scene from IKEA home smart App which can be triggered.
 
| Channel               | Type                  | Read/Write | Description                                  | Advanced |
|-----------------------|-----------------------|------------|----------------------------------------------|----------|
| `trigger`             | Number                | RW         | Perform / undo scene execution               |          |
| `last-trigger`        | DateTime              | R          | Date and time when last trigger occurred     |    X     |
| `json`                | String                | R          | JSON structure and updates of this device    |    X     |

Scenes are defined in IKEA home samrt App and can be perfomred via `trigger` channel.
Two commands are defined:

- 0 : Perform
- 1 : Undo

If command 0 (Perform) is sent scene will be executed.
There's a 30 seconds timeslot to send command 1 (Undo). 
The countdown is updating `trigger` channel state which can be evaluated if an undo operation is still possible.
State will switch to `Undef` after countdown.

## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Bridge dirigera:gateway:myhome                      "My wonderful Home"         [ ipAddress="1.2.3.4", discovery=true ] {
    Thing temperature-light     living-room-bulb    "Living Room Table Lamp"    [ id="aaaaaaaa-bbbb-xxxx-yyyy-zzzzzzzzzzzz"]
    Thing smart-plug            dishwasher          "Dishwasher"                [ id="zzzzzzzz-yyyy-xxxx-aaaa-bbbbbbbbbbbb"]
}
```

### Item Configuration

```java
Switch                      Table_Lamp_Power_State      { channel="dirigera:temperature-light:myhome:living-room-bulb:power-state" }
Dimmer                      Table_Lamp_Brightness       { channel="dirigera:temperature-light:myhome:living-room-bulb:brightness" }
Dimmer                      Table_Lamp_Temperature      { channel="dirigera:temperature-light:myhome:living-room-bulb:temperature" }
Number                      Table_Lamp_Startup          { channel="dirigera:temperature-light:myhome:living-room-bulb:startup" }
Number                      Table_Lamp_OTA_Status       { channel="dirigera:temperature-light:myhome:living-room-bulb:ota-status" }
Number                      Table_Lamp_OTA_State        { channel="dirigera:temperature-light:myhome:living-room-bulb:ota-state" }
Number                      Table_Lamp_OTA_Progress     { channel="dirigera:temperature-light:myhome:living-room-bulb:ota-progress" }
String                      Table_Lamp_JSON             { channel="dirigera:temperature-light:myhome:living-room-bulb:json" }

Switch                      Dishwasher_Power_State      { channel="dirigera:smart-plug:myhome:dishwasher:power-state" }
Switch                      Dishwasher_Child_lock       { channel="dirigera:smart-plug:myhome:dishwasher:child-lock" }
Switch                      Dishwasher_Disable_Light    { channel="dirigera:smart-plug:myhome:dishwasher:disable-light" }
Number:Power                Dishwasher_Power            { channel="dirigera:smart-plug:myhome:dishwasher:power" }
Number:Energy               Dishwasher_Energy_Total     { channel="dirigera:smart-plug:myhome:dishwasher:energy-total" }
Number:Energy               Dishwasher_Energy_Reset     { channel="dirigera:smart-plug:myhome:dishwasher:energy-reset" }
Number:ElectricCurrent      Dishwasher_Ampere           { channel="dirigera:smart-plug:myhome:dishwasher:ampere" }
Number:ElectricPotential    Dishwasher_Voltage          { channel="dirigera:smart-plug:myhome:dishwasher:voltage" }
Number                      Dishwasher_Startup          { channel="dirigera:smart-plug:myhome:dishwasher:startup" }
Number                      Dishwasher_OTA_Status       { channel="dirigera:smart-plug:myhome:dishwasher:ota-status" }
Number                      Dishwasher_OTA_State        { channel="dirigera:smart-plug:myhome:dishwasher:ota-state" }
Number                      Dishwasher_OTA_Progress     { channel="dirigera:smart-plug:myhome:dishwasher:ota-progress" }
String                      Dishwasher_JSON             { channel="dirigera:smart-plug:myhome:dishwasher:json" }
```
## Roadmap

- Configure connections between sensors and light / plug devices
- Timeout after sensor detection
- Controller circadianPresets

## Credits

This work is based on [Leggin](https://github.com/Leggin/dirigera) and [dvdgeisler](https://github.com/dvdgeisler/DirigeraClient).
Without these contributions this binding wouldn't be possible!
