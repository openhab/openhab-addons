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

The `bridge` and stand-alone `accessory` Things need to be paired with their respective HomeKit accessories.
This requires entering the HomeKit pairing code as a configuration parameter in the binding.
Note that HomeKit accessories can only be paired with one controller, so if it is already paired with something else, you will need to remove that pairing first.

The following table shows the thing configuration parameters.

| Name              | Type    | Description                                       | Default | Required  | Advanced  |
|-------------------|---------|---------------------------------------------------|---------|-----------|-----------|
| `host`            | text    | IP v4 address of the HomeKit accessory.           | N/A     | see below | see below |
| `pairingCode`     | text    | Code used for pairing with the HomeKit accessory. | N/A     | see below | see below |
| `refreshInterval` | integer | Interval at which the accessory is polled in sec. | 60      | no        | yes       |

Things of type `bridge` and `accessory` require both a `host` and a `pairingCode`.

The `host` is set by the mDNS auto- discovery process.
It must match the format `123.123.123.123:4567` representing its IP v4 address and port.

The `pairingCode` must be entered manually.
It must match the format `XXX-XX-XXX` or `XXXX-XXXX` or `XXXXXXXX` where `X` is a single digit.

Child `accessory` Things do not require neither a `host` nor a `pairingCode`.
Therefore child things have these parameters preset to `n/a`.

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
So for this reason it is extremely difficult to create Things via a '.things' file, and is therefore not recommeneded.

### Item Configuration

```java
Number:Temperature Color_Temperature "Color Temperature [%.1f mired]" <light> [ColorTemperature, Setpoint] { channel="homekit:accessory:297b703df234:lightbulb#color-temperature", unit="mired" }
```

### Sitemap Configuration

```perl
Slider item=Color_Temperature
```
