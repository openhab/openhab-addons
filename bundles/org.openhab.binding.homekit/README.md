# HomeKit Binding

This binding allows pairing with HomeKit accessories and **imports** their services as channel groups and their respective service- characteristics as channels.
Do not confuse this with the [HomeKit system integration](https://www.openhab.org/addons/integrations/homekit/) which **exports** openHAB Items to a HomeKit controller.

## Supported Things

There are three types of Things supported:

- `accessory`: This integrates a single HomeKit accessory, which has its own LAN connection.
  Its services appear as Channel Groups, and their respective characteristics appear as Channels.
- `bridged-accessory`: This integrates a single HomeKit accessory, which does NOT have its own LAN connection.
  It has the same functionality as an `accessory`, except that its communication is done via a `bridge` (see below).
- `bridge`: This integrates a HomeKit bridge accessory, which has its own LAN connection.
  It does not have any own Channels.
  Instead it contains multiple `bridged-accessory` Things (see above).

Things of type `bridge` and `accessory` both communicate directly with their HomeKit accessory device via the LAN.
Whereas `bridged-accessory` Things communicate via their respective `bridge` Thing.

## Discovery

Both `accessory` and `bridge` Things will be auto-discovered via mDNS.
And once a `bridge` Thing has been instantiated and paired, its `bridged-accessory` Things will also be auto-discovered.

## Configuration for `bridge` and `accessory` Things

The following table shows the Thing configuration parameters for `bridge` and `accessory` Things.

| Name              | Type    | Description                                          | Default   | Required | Advanced |
|-------------------|---------|------------------------------------------------------|-----------|----------|----------|
| `ipAddress`       | text    | IP v4 address of the HomeKit accessory.              | see below | yes      | yes      |
| `httpHostHeader`  | text    | The fully qualified host name as discovered by mDNS. | see below | yes      | yes      |
| `macAddress`      | text    | Unique accessory identifier.                         | see below | yes      | yes      |
| `refreshInterval` | integer | Interval at which the accessory is polled in sec.    | 60        | no       | yes      |

NOTE: as a general rule, if you create the Things via the Inbox, then all of the above configuration parameters will have their proper values already preset.

As a general rule `ipAddress` is set by the mDNS auto-discovery process.
However you can configure it manually if you wish.
It must match the format `123.123.123.123:4567` representing its IP v4 address and port.

As a general rule `httpHostHeader` is set by the mDNS auto-discovery process.
However you can configure it manually if you wish.
The `httpHostHeader` is required for the 'Host:' header of HTTP requests sent to the `accessory` or `bridge`.
It must be the fully qualified host name (e.g. `foobar._hap._tcp.local.` or, if the port is not 0 or 80, `foobar._hap._tcp.local.:1234`) as found manually via (say) an mDNS discovery app.

As a general rule `macAddress` is set by the mDNS auto-discovery process.
However you can configure it manually if you wish.
It must be the unique accessory identifier as found manually via (say) an mDNS discovery app.

### Configuration for `bridged-accessory` Things

The following table shows the Thing configuration parameters for `bridged-accessory` Things.

| Name              | Type    | Description                                          | Default   | Required | Advanced |
|-------------------|---------|------------------------------------------------------|-----------|----------|----------|
| `accessoryID`     | integer | ID of the accessory.                                 | see below | yes      | yes      |

As a general rule `accessoryID` is set by the auto-discovery process.
However you can configure it manually if you wish.
It must be the ID of the `bridged-accessory` within the `bridge`.

## Thing Pairing

The `bridge` and `accessory` Things need to be paired with their respective HomeKit accessories.
This requires entering the HomeKit pairing code by means of a Thing Action.

Note that HomeKit accessories can only be paired with one controller, so if it is already paired with something else, you will need to remove that pairing first.
There are two forms of pairing:

1. Simple pairing.
  This works directly between two devices – a HomeKit client (this binding) and a HomeKit accessory.
  In this case you need only to enter the pairing code into the Thing Action.
1. Pairing with external authorization.
  In addition to the HomeKit client (this binding) and the HomeKit accessory, it requires an additional third party to put the accessory into pairing mode.
  Typically the additional third party can be either a) using the accessory's app to put it into pairing mode, or b) pressing a pairing button on the device.

In either case above, the Pairing Code must be entered manually into the Thing Action dialog.
The Pairing Code must match the format `XXX-XX-XXX` or `XXXX-XXXX` or `XXXXXXXX` where `X` is a single digit.

For case 1. above, the `With External Authentication` switch must be `OFF`.
Whereas for case 2. above, must be `ON`.

## Channels

For `accessory` and `bridged-accessory` Things, the Channels are auto-created depending on the services and characteristics published by the HomeKit accessory.
Things of type `bridge` do not have own Channels.

As a general rule openHAB has one Channel for each HomeKit characteristic.
Some HomeKit accessories have separate characteristics for 'target' and 'current' states.
The two characteristics may have different values (e.g. for a thermostat).
In all such cases the Thing has a Channel for each characteristic so that both values can be accessed.

Some HomeKit characteristics represent fixed information e.g. model number, firmware version, etc.
Such values appear in openHAB as properties of the respective Thing.

### Special Extra HSBType Channel

In openHAB the norm is that lighting objects shall be represented by a single `HSBType` Channel which manages hue, saturation, brightness, and on-off states.
By contrast a HomeKit accessory has four separate characteristics for hue, saturation, brightness, and on-off.
So the Thing creates one additional `HSBType` Channel that amalgamates hue, saturation, brightness, and on-off characteristics, according to the openHAB norm.

## Integration with Apple Home App / Ecosystem

Many HomeKit accessories are able only to be paired with one client.
This means that if you want to pair such an accessory with this binding, you must first unpair it from the Apple Home app.

If you want to integrate such an accessory with both this binding and with the Apple Home ecosystem, then you can use this binding to import the Channels as OpenHAB Items, and then use the OpenHAB system integration addon to re-export those Items to the Apple HomeKit eco system.

## File Based Configuration

### Thing Configuration

Things are automatically configured when they are discovered.
So for this reason it is difficult to create Things via a '.things' file, and therefore not recommended.

```java
Bridge homekit:bridge:velux "VELUX Gateway" [ host="192.168.0.235:5001", macAddress="XX:XX:XX:XX:XX:XX", hostName="foobar._hap._tcp.local.", refreshInterval=60 ] {
    Thing bridged-accessory sensor "VELUX Sensor" @ "Hallway" [ accessoryID=2 ]
    Thing bridged-accessory skylight_hallway "VELUX Window" @ "Hallway" [ accessoryID=3 ]
    Thing bridged-accessory skylight_bathroom "VELUX Window" @ "Bathroom" [ accessoryID=4 ]
}
```

### Item Configuration

```java
Group VeluxSensorSwitch "Velux indoor climate sensor" (Hallway) ["Sensor"]

Number:Dimensionless Velux_Hallway_CO2 "CO2 [%d ppm]" <carbondioxide> (VeluxSensorSwitch) ["Measurement", "CO2"] { channel="homekit:bridged-accessory:velux:sensor:sensor-carbon-dioxide#carbon-dioxide-level-17", unit="ppm" }
Number:Dimensionless Velux_Hallway_Humidity "Humidity [%.0f %%]" <humidity> (VeluxSensorSwitch) ["Measurement", "Humidity"] { channel="homekit:bridged-accessory:velux:sensor:sensor-humidity#relative-humidity-current-13", unit="%" }
Number:Temperature Velux_Hallway_Temperature "Temperature" <temperature> (VeluxSensorSwitch) ["Measurement", "Temperature"] { channel="homekit:bridged-accessory:velux:sensor:sensor-temperature#temperature-current-10", unit="°C" }

Group SkylightHallway "Skylight window" (Hallway) ["Window"]

Rollershutter SkylightHallway_Position "Position" (SkylightHallway) ["OpenState"] { channel="homekit:bridged-accessory:velux:skylight_hallway:window#position-target-11" }

Group SkylightBathroom "Skylight window" (SmallBathroom) ["Window"]

Rollershutter SkylightBathroom_Position "Position" (SkylightBathroom) ["OpenState"] { channel="homekit:bridged-accessory:velux:skylight_bathroom:window#position-target-11" }
```

### Sitemap Configuration

```perl
Slider item=SkylightHallway_Position
Slider item=SkylightBathroom_Position
```
