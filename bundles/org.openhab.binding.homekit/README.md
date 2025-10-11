# HomeKit Binding

This binding allows pairing with HomeKit accessory devices and importing their services as channel groups and their respective service- characteristics as channels.

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

Things are mostly automatically configured when they are discovered.
However the following are the .

| Name              | Type    | Description                                       | Default | Required  | Advanced  |
|-------------------|---------|---------------------------------------------------|---------|-----------|-----------|
| `host`            | text    | IP v4 address of the HomeKit accessory.           | N/A     | see below | see below |
| `pairingCode`     | text    | Code used for pairing with the HomeKit accessory. | N/A     | see below | see below |
| `refreshInterval` | integer | Interval at which the accessory is polled in sec. | 60      | no        | yes       |

Things of type `bridge` and stand-alone `accessory` Things require both a `host` and a `pairingCode`.
The `host` is set by the mDNS auto- discovery process.
And the `pairingCode` must be entered manually.

Child `accessory` Things do not require neither a `host` nor a `pairingCode`.
Therefore these parameters are preset to `n/a`.

## Channels

Channels will be auto-created depending on the services and characteristics published by the HomeKit accessory.

### Thing Configuration

Things are mostly automatically configured when they are discovered.
So for this reason it is extremely difficult to create Things via a '.things' file.

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
