# Complete Channel Lists and Migration Examples

Both alternatives use solarman:logger; inverterType selects the channel set.
Use one active Thing per physical logger and choose only the files for its selected profile.

| Profile              | Entries | Read-only | Read/write | Complete list                                | Generic Item definitions                     |
| -------------------- | ------- | --------- | ---------- | -------------------------------------------- | -------------------------------------------- |
| deye_hybrid          | 81      | 63        | 18         | [Legacy channels](deye_hybrid.md)            | [Legacy Items](deye_hybrid.items)            |
| sunsynk_single_phase | 150     | 77        | 73         | [Advanced channels](sunsynk_single_phase.md) | [Advanced Items](sunsynk_single_phase.items) |

Counts include aliases and do not guarantee availability on every firmware.
The advanced list comprises 147 pinned Kellerza entries and three separately attributed, read-only binding metadata channels.
Versions and inverter identity use String Items without units.
The current upstream legacy definition removes inverter-gen-connected-status and inverter-gen-power; historical 83-entry lists do not describe this main-based source.

The [combined CSV](channels.csv) records types, units, access, registers and exact String options.
The [migration map](migration-map.md), [mapping CSV](migration-map.csv), [functional proposals](single-phase-register-review.md) and [definition CSV](single-phase-register-review.csv) account for all 81 current legacy entries.
The [register index](register-index.csv) is profile-qualified, preserves complete tuples and distinguishes computed inputs from direct values.
Automatic numeric candidates require matching ordered registers, signedness, scale, offset, unit and Item type.
Changed interpretation, partial fault tuples, split masks and different Item types require deliberate review; no tool deploys a link automatically.

## Using the Files

1. Save the existing Thing configuration and Item links, and suspend rules that send commands while changing profiles.
1. The two .things files are alternatives for a new installation and declare the same generic UID; install only one after replacing its hostname/serial.
1. For an existing Thing, retain its UID where appropriate and select the intended profile; do not add a second active logger connection.
1. Use only the matching .items file, replacing solarman:logger:example with the actual Thing UID. Generic Legacy/Advanced names are examples, not required renames.
1. When migrating existing Items, preserve their names where type and meaning remain compatible. Review rules, sitemap, persistence and semantic references before changing a type or name.
1. Advanced profiles reject legacy additionalRequests and custom channels. These remain supported for legacy selections.
1. Start with readings and compare them with the inverter. Review current/power signedness, phase-versus-total meaning, temperature interpretation and energy-counter meaning separately.
1. Test an intended writable control only after checking its limits and recording its original setting; confirm inverter/readback agreement and restore temporary values before resuming rules.

Creating/linking Items sends no command.
Writable examples use autoupdate="false" to show observed state; writable channel types also veto optimistic default updates.
Six SOC commands are six serialized transactions; failure of one does not roll back the others.
Changing the profile back restores that version's legacy channels and saved links, not device settings already written or channels removed upstream.

## Common Links

| Function        | deye_hybrid channel             | sunsynk_single_phase channel | Item type                |
| --------------- | ------------------------------- | ---------------------------- | ------------------------ |
| Battery SOC     | battery-battery-soc             | battery-soc                  | Number:Dimensionless     |
| Battery voltage | battery-battery-voltage         | battery-voltage              | Number:ElectricPotential |
| Battery power   | battery-battery-power           | battery-power                | Number:Power             |
| Battery current | battery-battery-current         | battery-current              | Number:ElectricCurrent   |
| Program 6 SOC   | time-of-use-time-of-use-soc-6   | prog6-capacity               | Number:Dimensionless     |
| Program 6 power | time-of-use-time-of-use-power-6 | prog6-power                  | Number:Power             |
| Program 6 time  | time-of-use-time-of-use-time-6  | prog6-time                   | String                   |

For example, these are alternative declarations for the same Item name.
Use the first only with deye_hybrid and the second only with sunsynk_single_phase:

```java
Number:Dimensionless ExampleProgramSOC6 "Program 6 SOC [%s]" {
    channel="solarman:logger:example:time-of-use-time-of-use-soc-6",
    unit="%",
    autoupdate="false"
}
```

```java
Number:Dimensionless ExampleProgramSOC6 "Program 6 SOC [%s]" {
    channel="solarman:logger:example:prog6-capacity",
    unit="%",
    autoupdate="false"
}
```

The binding String additions use communication-board-version at 14, control-board-version at 13 and inverter-id at 3..7.
They reuse existing reads and are specific to the single-phase profile.
If an older personal declaration used Number for a version code, review its type and external references before converting it to String.

## Timing and Device Validation

Legacy periodic refresh accepts at least 1 second; advanced periodic refresh requires at least 5 seconds.
These are not command delays.
Advanced confirmation is scheduled at least 50 milliseconds after an accepted write, with the logger queue/connection reserved until the transaction completes.
Time writes use HH:mm and validate neighboring periods; their physical qualification is separate from confirmed time readings.
See the README's selected write warning, [hardware coverage](../../../README.md#hardware-test-coverage) and [hardware procedure](hardware-validation.md).

Regenerate these files with the commands in [source provenance](../../source-provenance.md).
This metadata review does not establish live import/UI behavior or equivalence for all operating conditions.
