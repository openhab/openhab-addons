# Shelly Binding — Power Meter Migration (openHAB 5.2.1)

This document describes the power meter channel changes introduced with openHAB 5.2.1, the automatic migration, and the unit conversions applied by the binding.
The channel reference (types, units, descriptions) per device is part of the [README](../README.md) channel tables.

## Channel Groups

| Group        | Name pattern               | Present on                                                |
| ------------ | -------------------------- | --------------------------------------------------------- |
| Per-meter    | `meter`, `meter1`–`meter3` | All metered devices                                       |
| Device total | `device`                   | Multi-meter devices (Pro 3EM, Pro EM-50, Plus 2PM family) |

Single-meter devices (Plus 1PM, Shelly 1PM, etc.) use the group name `meter`.
Multi-meter devices (Pro 3EM, Pro EM-50) use `meter1`, `meter2`, `meter3`.

## Channel Renames (auto-migrated — no re-discovery needed)

Old channel IDs stay active as deprecated aliases and continue to receive updates, so existing item links and rules keep working.
Deprecated channels are marked `[deprecated] Use <new channel> instead.` in their channel description and are flagged as advanced.

| Old channel ID               | New channel ID                     | Old unit | New unit | Old channel stays active       |
| ---------------------------- | ---------------------------------- | -------- | -------- | ------------------------------ |
| `meterN#currentWatts`        | `meterN#currentPower`              | W        | W        | Yes                            |
| `meterN#totalKWH`            | `meterN#totalEnergy`               | kWh      | kWh      | Yes                            |
| `meterN#returnedKWH`         | `meterN#returnedEnergy`            | kWh      | kWh      | Yes                            |
| `meterN#reactiveWatts`       | `meterN#reactivePower`             | W        | VAR      | Yes — old channel keeps W unit |
| `meterN#lastPower1`          | `meterN#energyAvg1Min`             | W        | Wh       | Yes — average power in W       |
| `device#accumulatedWatts`    | `device#accumulatedPower`          | W        | W        | Yes                            |
| `device#accumulatedReturned` | `device#accumulatedReturnedEnergy` | kWh      | kWh      | Yes                            |
| `device#accumulatedWTotal`   | `device#totalEnergy`               | kWh      | kWh      | Yes                            |
| `device#totalKWH`            | `device#totalEnergy`               | kWh      | kWh      | Yes                            |
| `nmeter#nmTreshhold`         | `nmeter#nmThreshold`               | A        | A        | Yes                            |

The `device#totalKWH` rename uses a group-qualified migration rule, so it does not conflict with the per-meter `meterN#totalKWH` channels.
The old `device#accumulatedWTotal` channel was always reporting wrong values (Amperes/1000 instead of kWh); `device#totalEnergy` reports the correct total.

## Channel Type Change

`meterN#powerFactor` changed type from `Number:Dimensionless` to `Number` (range −1.0 to +1.0).
This is an in-place type change on the same channel id, not a rename, so there is no dual-write and no migration path for it.
Items statically linked as `Number:Dimensionless` should be relinked; persistence configs and unit-based rules referencing this channel may need adjustment.

## New Channels (re-discover the Thing to get them)

| Group    | Channel ID            | Type             | Unit | Devices                                                      |
| -------- | --------------------- | ---------------- | ---- | ------------------------------------------------------------ |
| `meterN` | `apparentPower`       | Number:Power     | VA   | Pro 3EM, Pro EM-50, Plus EM, Plus EM Mini G4                 |
| `device` | `accumulatedApparent` | Number:Power     | VA   | Pro 3EM, Pro EM-50 only                                      |
| `meterN` | `energyAvg1Min`       | Number:Energy    | Wh   | All relay-PM devices (Gen1 and Gen2+)                        |
| `meterN` | `frequency`           | Number:Frequency | Hz   | Gen4 PM devices and EM devices; absent on Gen2/Gen3 Plus 1PM |

## Minute Energy Channels — Units and Conversion

The firmware reports the last-minute energy in different raw units per generation.
The binding converts both to Wh so `energyAvg1Min` is directly comparable across devices:

| Generation | Source field           | Raw unit              | Conversion        |
| ---------- | ---------------------- | --------------------- | ----------------- |
| Gen1       | `meters[].counters[0]` | Watt-minutes (W-min)  | value ÷ 60 → Wh   |
| Gen2+      | `aenergy.by_minute[0]` | Milliwatt-hours (mWh) | value ÷ 1000 → Wh |

### `lastPower1` / `energyAvg1Min` — Backward Compatibility

`lastPower1` is kept active alongside `energyAvg1Min`.
Every update writes `lastPower1` (W, average power of the previous minute) and `energyAvg1Min` (Wh, energy of that minute), so items linked to either channel continue to receive updates without re-discovery.
Each channel only receives states in its own unit.

This applies to all metered devices — Gen1 relay-PM, Gen2+ relay-PM, and Gen1/Gen2 3EM devices.

## Device Support Matrix

| Device family                   | Meter groups          | `totalEnergy` | `returnedEnergy` | `reactivePower` | `apparentPower` | `energyAvg1Min` | `frequency` |
| ------------------------------- | --------------------- | ------------- | ---------------- | --------------- | --------------- | --------------- | ----------- |
| Gen1 relay-PM (Shelly 1PM, 2.5) | meter                 | Yes           | —                | —               | —               | Yes             | —           |
| Gen1 3EM                        | meter1–meter3         | Yes           | Yes              | Yes             | —               | Yes             | —           |
| Plus 1PM / Plus 2PM (Gen2/Gen3) | meter / meter1–meter2 | Yes           | —                | —               | —               | Yes             | —           |
| Plus 1PM Gen4                   | meter                 | Yes           | —                | —               | —               | Yes             | Yes         |
| Plus Plug S                     | meter                 | Yes           | —                | —               | —               | Yes             | —           |
| Pro 3EM / 3EM-63 / 3EM-400      | meter1–meter3         | Yes           | Yes              | —               | Yes             | Yes             | Yes         |
| Pro EM-50                       | meter1–meter2         | Yes           | Yes              | —               | Yes             | Yes             | Yes         |
| Plus EM Mini Gen4               | meter                 | Yes           | Yes              | —               | Yes             | Yes             | Yes         |

A `—` entry means the channel is not reported by the firmware and will not appear.
The Pro 3EM monophase profile exposes the three clamps as `meter1`–`meter3` as well (one independent meter per clamp).

## Migration Notes

The binding applies channel migration automatically at startup (schema version check).
No user action is needed for the renames listed above: both the old and new channel IDs are active and receive the same updates.
Update item links and rules to the new channel IDs at your convenience before the deprecated channels are removed in a future release.
Note for testers of interim PR #20805 builds: the temporary `lastEnergy1` channel was replaced by `energyAvg1Min`; delete and re-discover affected Things once.

## See Also

- [README.md](../README.md) — full channel tables per device
- [AdvancedUsers.md](AdvancedUsers.md) — firmware upgrade and troubleshooting
