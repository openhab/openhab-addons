# Dirigera Binding

Binding supporting the DIRIGERA Gateway from Ikea. 

It provides devices and scenes towards openHAB. 
With this it's possible to connect them with other devices outside of the Ikea world.

The goal is not to provide similar functionality of the IKEA Home Smart App like create / remove scenes, rename devices or handling rooms.

## Supported Things

The DIRIGERA `bridge` is providing the connection to all devices and scenes.  

Refer to below sections which devices are supported and are covered by `things` connected to the DIRIGERA bridge.

- Lights
- Remote Controls
- Speakers
- Air Purifiers
- Blinds
- Sensors
- Power Plugs
- Shortcut buttons
- Repeater

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

The binding is in development phase stage alpha.
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

### Lights

Lamps, LED Panels, LED Stripes

#### Temperature Light

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| json    | String | R          | Device json                 |


#### Color Light

### Remote Controls

### Air Purifiers

### Blinds

### Sensors

#### Motion Sensor

#### Motion Light Sensor

#### Water Sensor

#### Air Quality Sensor

### Power Plugs

#### Power Plug

#### Smart Power Plug

### Shortcut Buttons

### Speaker

### Repeater

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| Channel | Type   | Read/Write | Description                 |
|---------|--------|------------|-----------------------------|
| control | Switch | RW         | This is the control channel |

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
