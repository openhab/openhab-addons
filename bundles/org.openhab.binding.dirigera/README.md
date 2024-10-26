# Dirigera Binding

Binding supporting the DIRIGERA Gateway from Ikea. 

It provides devices and scenes towards openHAB. 
With this it's possible to connect them with other devices outside of the Ikea world.

The goal is not to provide similar functionality of the IKEA Home Smart App like create / remove scenes, rename devices or handling rooms.

## Supported Things

The DIRIGERA `bridge` is providing the connection to all devices and scenes.  

Refer to below sections which devices are supported and are covered by `things` connected to the DIRIGERA bridge.

- [Air Purifier](#air-purifier)
- [Blinds](#blinds)
- [Lights](#lights)
- [Power Plugs]()
- [Remote Controls]()
- [Repeater]()
- [Shortcut buttons]()
- [Sensors]()
- [Speakers]()

## Discovery

The discovery will try to identify your DIRIGERA Gateway. 
This may take some time because your whole network is scanned in order to find it.

**Before adding the bridge** read [Pairing section](#gateway-pairing).   

Devices connected to this bridge will be detected automatically unless you don't switch it off in [Bridge Configuration](#bridge-configuration)

## Bridge Configuration

| Name            | Type    | Description                                                | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------|---------|----------|----------|
| ipAddress       | text    | DIRIGERA IP Address                                        | N/A     | yes      | no       |
| id              | text    | Unique id of this gateway                                  | N/A     | no       | no       |
| discovery       | boolean | Configure if paired devices shall be detected by discovery | true    | no       | no       |

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

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| sunrise | DateTime | R         | This is the control channel |
| sunset  | DateTime | R         | This is the control channel |
| pairing | Switch | RW         | This is the control channel |
| statistics | Switch | R         | This is the control channel |

## Things

The binding is in development phase alpha.
Goal is to extend testing in the community to cover as many as possible old and new devices.
Your help is needed to extend and fix the current implementation.

### Unknown Devices

Filter your traces regarding 'DIRIGERA MODEL Unsuppoerted Device'. 
The trace cotains a json object at the end which is needed to implememnt a corresponding hanlder.

### Problem with Device

Each device has 'json' channel which is reflecting the structural data of a device. 
This channel is only for development purposes

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| json    | String | R          | Device json                 |

If you see wrong, missing or too much channels this data is needed to adapt implementation.

### Thing Configuration

| Name            | Type    | Description                                                | Default | Required |
|-----------------|---------|------------------------------------------------------------|---------|----------|
| id              | text    | Unique id of this gateway                                  | N/A     | yes      |

Each thing is identified by a unique id which is mandatory to configure.
Discovery will automatically identify the id.

### Air Purifier

Implementation, Test and known issues

| Name            | Implemented    | Tested     | Remarks |
|-----------------|----------------|------------|---------|
| STARKVIND       | yes            | no         |         |
| others?         |                | no         |         |

#### Air Purifier Channels

| Channel               | Type              | Read/Write | Description                                                      |
|-----------------------|-------------------|------------|------------------------------------------------------------------|
| fan-mode              | Number            | RW         | Fan on, off, speed or automatic behavior                         |
| motor-time            | Number:Time       | R          | Motor runtime in minutes                                         |
| filter-elapsed        | Number:Time       | R          | Filter elapsed time in minutes                                   |
| filter-reamin         | Number:Time       | R          | Time to filter replacement in minutes                            |
| filter-lifetime       | Number:Time       | R          | Filter lifetime in minutes                                       |
| filter-alarm          | Switch            | R          | Filter alarm signal                                              |
| particulate-matter    | Number:Density    | R          | Category 2.5 particulate matter                                  |
| disable-light         | Switch            | RW         | Disable status light on plug                                     |
| child-lock            | Switch            | RW         | Child lock for button on plug                                    |
| ota-status            | Number            | R          | Over-the-air overall status                                      |
| ota-state             | Number            | R          | Over-the-air current state                                       |
| ota-progress          | Number            | R          | Over-the-air current progress if state is download in progress   |
| json                  | String            | R          | JSON structure and updates of this device                        |

#### Air Purifier Channel Mappings

'fan-mode'

- 0 : Auto
- 1 : Low
- 2 : Medium
- 3 : High
- 4 : On
- 5 : Off

'ota-status'

- 0 : Up to date
- 1 : Update available

'ota-state'

- 0 : Ready to check
- 1 : Check in progress
- 2 : Ready to download
- 3 : Download in progress

### Blinds

| Name            | Implemented    | Tested     | Remarks |
|-----------------|----------------|------------|---------|
| PRAKTLYSING     | yes            | no         |         |
| others?         |                | no         |         |

#### Blind Channels

| Channel               | Type                  | Read/Write | Description                                                      |
|-----------------------|-----------------------|------------|------------------------------------------------------------------|
| blind-state           | Number                | RW         | State if blind is moving up, down or stopped                     |
| target-level          | Dimmer                | RW         | Target blind level                                               |
| current-level         | Dimmer                | R          | Current blind level                                              |
| battery-level         | Number:Dimensionless  | R          | State of the battery powering blind                              |
| ota-status            | Number                | R          | Over-the-air overall status                                      |
| ota-state             | Number                | R          | Over-the-air current state                                       |
| ota-progress          | Number                | R          | Over-the-air current progress if state is download in progress   |
| json                  | String                | R          | JSON structure and updates of this device                        |

 
#### Blind Channel Mappings

'blind-state'

- 0 : Stopped
- 1 : Up
- 2 : Down

See [further mappings here](#air-purifier-channel-mappings) for OTA  

### Lights

Light devices in several variants.
Can be light bulbs, LED stripes, remote driver, ...
Below there are 3 different variants to cover **all** light devices.
Please check!

| Name                              | Implemented   | Tested        | Remarks |
|-----------------------------------|---------------|---------------|---------|
| TRADFRI Color bulbs               | yes           | personally    |         |
| TRADFRI Color Temperature bulbs   | yes           | personally    |         |
| TRADFRI Dimmable bulbs            | yes           | no            |         |
| ORMANAS LED Strip                 | yes           | personally    |         |
| TRADFRI Driver                    | yes           | no            |         |
| others?                           |               | no            |         |


#### Dimmable Lights

| Channel               | Type                  | Read/Write | Description                                                      |
|-----------------------|-----------------------|------------|------------------------------------------------------------------|
| power-state           | Switch                | RW         | Power state of light                                             |
| brightness            | Dimmer                | RW         | Control brightness of light                                      |
| startup               | Number                | RW         | Startup behavior after power cutoff                              |
| ota-status            | Number                | R          | Over-the-air overall status                                      |
| ota-state             | Number                | R          | Over-the-air current state                                       |
| ota-progress          | Number                | R          | Over-the-air current progress if state is download in progress   |
| json                  | String                | R          | JSON structure and updates of this device                        |

##### Startup Channel Mappings

The startup defines how the device shall behave after a power cutoff.
If there's a dedicated hardwired light switch which cuts power towards the bulb it makes sense to sitch them on every time the switch is pressed.
But it's also possible to recover the last state.

'startup'

- 0 : Previous
- 1 : On
- 2 : Off

See [mappings for OTA](#air-purifier-channel-mappings)

#### Temperature Lights

| Channel               | Type                  | Read/Write | Description                                                      |
|-----------------------|-----------------------|------------|------------------------------------------------------------------|
| power-state           | Switch                | RW         | Power state of light                                             |
| brightness            | Dimmer                | RW         | Control brightness of light                                      |
| temperature           | Dimmer                | RW         | Control temperature of light from cold to warm                   |
| startup               | Number                | RW         | Startup behavior after power cutoff                              |
| ota-status            | Number                | R          | Over-the-air overall status                                      |
| ota-state             | Number                | R          | Over-the-air current state                                       |
| ota-progress          | Number                | R          | Over-the-air current progress if state is download in progress   |
| json                  | String                | R          | JSON structure and updates of this device                        |

See [mappings for sartup](#startup-channel-mappings)
See [mappings for OTA](#air-purifier-channel-mappings)

#### Color Lights

| Channel               | Type                  | Read/Write | Description                                                      |
|-----------------------|-----------------------|------------|------------------------------------------------------------------|
| power-state           | Switch                | RW         | Power state of light                                             |
| color                 | Color                 | RW         | Control light with color, saturation and brightness              |
| startup               | Number                | RW         | Startup behavior after power cutoff                              |
| ota-status            | Number                | R          | Over-the-air overall status                                      |
| ota-state             | Number                | R          | Over-the-air current state                                       |
| ota-progress          | Number                | R          | Over-the-air current progress if state is download in progress   |
| json                  | String                | R          | JSON structure and updates of this device                        |

See [mappings for sartup](#startup-channel-mappings)
See [mappings for OTA](#air-purifier-channel-mappings)

### Power Plugs

| Name                              | Implemented   | Tested        | Remarks |
|-----------------------------------|---------------|---------------|---------|
| TRADFRI                           | yes           | no            |         |
| TRETAKT                           | yes           | personally    |         |
| INSPELNING                        | yes           | personally    |         |
| others?                           |               | no            |         |

#### Simple Plug

Simple plug with controler of power state and startup behavior

| Channel               | Type                  | Read/Write | Description                                                      |
|-----------------------|-----------------------|------------|------------------------------------------------------------------|
| power-state           | Switch                | RW         | Power state of plug                                              |
| startup               | Number                | RW         | Startup behavior after power cutoff                              |
| ota-status            | Number                | R          | Over-the-air overall status                                      |
| ota-state             | Number                | R          | Over-the-air current state                                       |
| ota-progress          | Number                | R          | Over-the-air current progress if state is download in progress   |
| json                  | String                | R          | JSON structure and updates of this device                        |

See [mappings for sartup](#startup-channel-mappings)
See [mappings for OTA](#air-purifier-channel-mappings)

#### Power Plug

Power plug with controler of power state, startup behavior, hardware on/off button and status light

| Channel               | Type                  | Read/Write | Description                                                      |
|-----------------------|-----------------------|------------|------------------------------------------------------------------|
| power-state           | Switch                | RW         | Power state of plug                                              |
| child-lock            | Switch                | RW         | Child lock for button on plug                                    |
| disable-light         | Switch                | RW         | Disable status light on plug                                     |
| startup               | Number                | RW         | Startup behavior after power cutoff                              |
| ota-status            | Number                | R          | Over-the-air overall status                                      |
| ota-state             | Number                | R          | Over-the-air current state                                       |
| ota-progress          | Number                | R          | Over-the-air current progress if state is download in progress   |
| json                  | String                | R          | JSON structure and updates of this device                        |

See [mappings for sartup](#startup-channel-mappings)
See [mappings for OTA](#air-purifier-channel-mappings)

#### Smart Power Plug

Smart plug like [Power Plug](#power-plug) plus measuring capability

| Channel               | Type                  | Read/Write | Description                                                      |
|-----------------------|-----------------------|------------|------------------------------------------------------------------|
| power-state           | Switch                | RW         | Power state of plug                                              |
| child-lock            | Switch                | RW         | Child lock for button on plug                                    |
| disable-light         | Switch                | RW         | Disable status light on plug                                     |
| power                 | Number                | R          | Electric power delivered by plug                                 |
| energy-total          | Number                | R          | Total energy consumption                                         |
| energy-reset          | Number                | R          | Energy consumption since last rese                               |
| ampere                | Number                | R          | Electric current measured by plug                                | 
| voltage               | Number                | R          | Electric potential of plug                                       |
| startup               | Number                | RW         | Startup behavior after power cutoff                              |
| ota-status            | Number                | R          | Over-the-air overall status                                      |
| ota-state             | Number                | R          | Over-the-air current state                                       |
| ota-progress          | Number                | R          | Over-the-air current progress if state is download in progress   |
| json                  | String                | R          | JSON structure and updates of this device                        |

See [mappings for sartup](#startup-channel-mappings)
See [mappings for OTA](#air-purifier-channel-mappings)

### Remote Controls

### Sensors

#### Motion Sensor

#### Motion Light Sensor

#### Water Sensor

#### Air Quality Sensor

### Shortcut Buttons

### Speaker

### Repeater


## Full Example

_Provide a full usage example based on textual configuration files._
_*.things, *.items examples are mandatory as textual configuration is well used by many users._
_*.sitemap examples are optional._

### Thing Configuration

```java
Example thing configuration goes here.
```

### Item Configuration

```java
Example item configuration goes here.
```

### Sitemap Configuration

```perl
Optional Sitemap configuration goes here.
Remove this section, if not needed.
```

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
