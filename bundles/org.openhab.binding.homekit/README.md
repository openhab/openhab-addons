# HomeKit Binding

This binding allows pairing with HomeKit accessories and **imports** their services as channel groups and their respective service- characteristics as channels.
Do not confuse this with the other HomeKit **integration** (https://www.openhab.org/addons/integrations/homekit/) which **exports** openHAB Items to a HomeKit controller.

## Supported Things

There are two types of Things supported:

- `accessory`: This integrates a single HomeKit accessory, whereby its services appear as channel groups, and the respective characteristics appear as channels.
- `bridge`: This integrates a HomeKit bridge accessory containing multiple child `accessory` Things.
  So Things of type `accessory` either represent a stand-alone accessories or a child of a `bridge` Thing.

Things of type `bridge` and stand-alone `accessory` Things both communicate directly with their HomeKit device over the LAN.
Whereas child `accessory` Things communicate via their respective `bridge` Thing.

## Discovery

Both `bridge` and stand-alone `accessory` Things will be auto discovered via mDNS.
Once a `bridge` Thing has been instantiated and paired, its child `accessory` Things will also be auto- discovered.

## Thing Configuration

The following table shows the thing configuration parameters.

| Name              | Type    | Description                                          | Default | Required  | Advanced  |
|-------------------|---------|------------------------------------------------------|---------|-----------|-----------|
| `ipAddress`       | text    | IP v4 address of the HomeKit accessory.              | N/A     | see below | no        |
| `hostName`        | text    | The fully qualified host name as discovered by mDNS. | N/A     | see below | yes       |
| `macAddress`      | text    | Unique accessory identifier.                         | N/A     | see below | yes       |
| `accessoryID`     | integer | ID of the accessory.                                 | N/A     | see below | yes       |
| `refreshInterval` | integer | Interval at which the accessory is polled in sec.    | 60      | no        | yes       |

NOTE: as a general rule, if you create the things via the Inbox, then all of the above configuration parameters will have their proper values already preset.

As a general rule `ipAddress` is set by the mDNS auto- discovery process.
However you can configure it manually if you wish.
It must match the format `123.123.123.123:4567` representing its IP v4 address and port.
Child `accessory` Things do not require a `ipAddress`.
Therefore child things have this parameter preset to `n/a`.

As a general rule, `hostName` is set by the mDNS auto- discovery process.
However you can configure it manually if you wish.
It must be the fully qualified host name (e.g. `foobar.local` or, if the port is not 0 or 80, `foobar.local:1234` ) as found manually via (say) an mDNS discovery app.
Child `accessory` Things do not require a `hostName`.
Therefore child things have this parameter preset to `n/a`.

As a general rule, `macAddress` is set by the mDNS auto- discovery process.
However you can configure it manually if you wish.
It must be the unique accessory identifier as found manually via (say) an mDNS discovery app.
Child `accessory` Things do not require a `macAddress`.
Therefore child things have this parameter preset to `n/a`.

As a general rule, `accessoryID` is set by the mDNS auto- discovery process, or child discovery process.
However you can configure it manually if you wish.
It must be the ID of the accessory within the bridge, or `1` if it is a root accessory.

## Thing Pairing

The `bridge` and stand-alone `accessory` Things need to be paired with their respective HomeKit accessories.
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

Channels are auto-created depending on the services and characteristics published by the HomeKit accessory.

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
So for this reason it is extremely difficult to create Things via a '.things' file, and is therefore not recommended.

```java
Bridge homekit:bridge:velux "VELUX Gateway" [ host="192.168.0.235:5001", macAddress="XX:XX:XX:XX:XX:XX", accessoryID=1 ] {
    Thing accessory 2 "VELUX Sensor" @ "Hallway" [ host="n/a", accessoryID=2 ]
    Thing accessory 3 "VELUX Window" @ "Hallway" [ host="n/a", accessoryID=3 ]
    Thing accessory 4 "VELUX Window" @ "Small bathroom" [ host="n/a", accessoryID=4 ]
}
```

### Item Configuration

```java
Number:Temperature Color_Temperature "Color Temperature [%.1f mired]" <light> [ColorTemperature, Setpoint] { channel="homekit:accessory:297b703df234:lightbulb#color-temperature", unit="mired" }
```

### Sitemap Configuration

```perl
Slider item=Color_Temperature
```
