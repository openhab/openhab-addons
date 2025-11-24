# HomeKit Binding

This binding allows pairing with HomeKit accessories and **imports** their services as channel groups and their respective service- characteristics as channels.
Do not confuse this with the other HomeKit **integration** (https://www.openhab.org/addons/integrations/homekit/) which **exports** openHAB Items to a HomeKit controller.

## Supported Things

There are three types of Things supported:

- `lan-accessory`: This integrates a single HomeKit accessory, which has its own LAN connection.
  Its services appear as channel groups, and their respective characteristics appear as channels.
- `child-accessory`: This integrates a single HomeKit accessory, which does NOT have its own LAN connection.
  It has the same functionality as a `lan-accessory`, except that its communication is done via a `bridge-accessory` (see below).
- `bridge-accessory`: This integrates a HomeKit bridge accessory, which has its own LAN connection.
  It does not have any own channels. But instead it contains multiple `child-accessory` Things (see above).

Things of type `bridge-accessory` and `lan-accessory` both communicate directly with their HomeKit accessory device via the LAN.
Whereas child `child-accessory` Things communicate via their respective `bridge-accessory` Thing.

## Discovery

Both `lan-accessory` and `bridge-accessory` Things will be auto- discovered via mDNS.
And once a `bridge-accessory` Thing has been instantiated and paired, its `child-accessory` Things will also be auto- discovered.

## Configuration for `bridge-accessory` and `lan-accessory` Things

The following table shows the thing configuration parameters for `bridge-accessory` and `lan-accessory` Things.

| Name              | Type    | Description                                          | Default   | Required | Advanced |
|-------------------|---------|------------------------------------------------------|-----------|----------|----------|
| `ipAddress`       | text    | IP v4 address of the HomeKit accessory.              | see below | yes      | yes      |
| `hostName`        | text    | The fully qualified host name as discovered by mDNS. | see below | yes      | yes      |
| `macAddress`      | text    | Unique accessory identifier.                         | see below | yes      | yes      |
| `refreshInterval` | integer | Interval at which the accessory is polled in sec.    | 60        | no       | yes      |

NOTE: as a general rule, if you create the things via the Inbox, then all of the above configuration parameters will have their proper values already preset.

As a general rule `ipAddress` is set by the mDNS auto- discovery process.
However you can configure it manually if you wish.
It must match the format `123.123.123.123:4567` representing its IP v4 address and port.

As a general rule, `hostName` is set by the mDNS auto- discovery process.
However you can configure it manually if you wish.
It must be the fully qualified host name (e.g. `foobar._hap._tcp.local.` or, if the port is not 0 or 80, `foobar._hap._tcp.local.:1234` ) as found manually via (say) an mDNS discovery app.

As a general rule, `macAddress` is set by the mDNS auto- discovery process.
However you can configure it manually if you wish.
It must be the unique accessory identifier as found manually via (say) an mDNS discovery app.

### Configuration for `child-accessory` Things

The following table shows the thing configuration parameters for `child-accessory` Things.

| Name              | Type    | Description                                          | Default   | Required | Advanced |
|-------------------|---------|------------------------------------------------------|-----------|----------|----------|
| `accessoryID`     | integer | ID of the accessory.                                 | see below | yes      | yes      |

As a general rule, `accessoryID` is set by the child auto- discovery process.
However you can configure it manually if you wish.
It must be the ID of the accessory within the `bridge-accessory`.

## Thing Pairing

The `bridge-accessory` and `lan-accessory` Things need to be paired with their respective HomeKit accessories.
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

For `lan-accessory` and `child-accessory` Things, the Channels are auto-created depending on the services and characteristics published by the HomeKit accessory.
Things of type `bridge-accessory` do not have own channels.

As a general rule openHAB has one channel for each HomeKit charactersitic.
Some HomeKit accessories have separate charactersitics for 'target' and 'current' states.
The two charactersitics may have different values (e.g. for a thermostat).
In all such cases the thing has a channel for each characteristic so that both values can be accessed.

Some HomeKit characteristics represent fixed information e.g. model number, firmware version, etc.
Such values appear in openHAB as properties of the respectinve thing.

### Special Extra HSBType Channel

In openHAB the norm is that lighting objects shall be represented by a single `HSBType` channel which manages hue, saturation, brightness, and on-off states.
By contrast a HomeKit accessory has four separate characteristics for hue, saturation, brightness, and on-off.
So the thing creates one additional `HSBType` channel that amalgamates hue, saturation, brightness, and on-off characteristics, according to the openHAB norm.

## File Based Configuration

### Thing Configuration

Things are automatically configured when they are discovered.
So for this reason it is difficult to create Things via a '.things' file, and therefore not recommended.

```java
Bridge homekit:bridge-accessory:velux "VELUX Gateway" [ host="192.168.0.235:5001", macAddress="XX:XX:XX:XX:XX:XX", hostName="foobar._hap._tcp.local.", refreshInterval=60 ] {
    Thing accessory2 "VELUX Sensor" @ "Hallway" [ accessoryID=2 ]
    Thing accessory3 "VELUX Window" @ "Hallway" [ accessoryID=3 ]
    Thing accessory4 "VELUX Window" @ "Small bathroom" [ accessoryID=4 ]
}
```

### Item Configuration

```java
Number:Temperature Color_Temperature "Color Temperature [%.1f mired]" <light> [ColorTemperature, Setpoint] { channel="homekit:lan-accessory:297b703df234:lightbulb#color-temperature", unit="mired" }
```

### Sitemap Configuration

```perl
Slider item=Color_Temperature
```
