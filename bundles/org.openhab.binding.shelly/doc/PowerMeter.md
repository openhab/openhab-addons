# Shelly Binding — Power Meter Channels

Power meter data is exposed in two channel groups per device.

## Channel Groups

| Group        | Name pattern               | Present on                                                |
| ------------ | -------------------------- | --------------------------------------------------------- |
| Per-meter    | `meter`, `meter1`–`meter3` | All metered devices                                       |
| Device total | `device`                   | Multi-meter devices (Pro 3EM, Pro EM-50, Plus 2PM family) |

Single-meter devices (Plus 1PM, Shelly 1PM, etc.) use the group name `meter`.
Multi-meter devices (Pro 3EM, Pro EM-50) use `meter1`, `meter2`, `meter3`.

## Per-Meter Channels

| Channel ID       | Type                     | Unit | Description                                                              |
| ---------------- | ------------------------ | ---- | ------------------------------------------------------------------------ |
| `currentPower`   | Number:Power             | W    | Active power (replaces `currentWatts`)                                   |
| `currentWatts`   | Number:Power             | W    | **Deprecated** — alias for `currentPower`; old items keep working        |
| `totalEnergy`    | Number:Energy            | kWh  | Total accumulated consumed energy (replaces `totalKWH`)                  |
| `totalKWH`       | Number:Energy            | kWh  | **Deprecated** — alias for `totalEnergy`; old items keep working         |
| `returnedEnergy` | Number:Energy            | kWh  | Total accumulated returned (feed-in) energy (replaces `returnedKWH`)     |
| `returnedKWH`    | Number:Energy            | kWh  | **Deprecated** — alias for `returnedEnergy`; old items keep working      |
| `reactivePower`  | Number:Power             | VAR  | Reactive power (replaces `reactiveWatts`; unit changed W→VAR)            |
| `reactiveWatts`  | Number:Power             | W    | **Deprecated** — use `reactivePower`; keeps the pre-5.2 W unit (numeric value identical) |
| `apparentPower`  | Number:Power             | VA   | Apparent power — data-driven; absent if not reported                     |
| `voltage`        | Number:ElectricPotential | V    | Phase voltage                                                            |
| `current`        | Number:ElectricCurrent   | A    | Phase current                                                            |
| `frequency`      | Number:Frequency         | Hz   | Mains frequency — data-driven; absent if the firmware does not report it |
| `powerFactor`    | Number                   | —    | Power factor −1.0 to +1.0 (dimensionless `Number`)                       |
| `neutralCurrent` | Number:ElectricCurrent   | A    | Neutral conductor current (Pro 3EM, Pro EM-50 only)                      |
| `energyAvg1Min`  | Number:Energy            | Wh   | Energy consumed in the previous complete minute (replaces `lastPower1`)  |
| `energyAvg2Min`  | Number:Energy            | Wh   | Energy consumed in the complete minute two minutes ago (advanced)        |
| `energyAvg3Min`  | Number:Energy            | Wh   | Energy consumed in the complete minute three minutes ago (advanced)      |
| `lastPower1`     | Number:Power             | W    | **Deprecated** — average power of the previous minute; use `energyAvg1Min` |
| `lastUpdate`     | DateTime                 | —    | Timestamp of the last meter update                                       |

### Minute Energy Channels — Units and Conversion

The firmware reports per-minute energy in different raw units per generation.
The binding converts both to Wh so all `energyAvgNMin` channels are directly comparable:

| Generation | Source field                | Raw unit             | Conversion         |
| ---------- | --------------------------- | -------------------- | ------------------ |
| Gen1       | `meters[].counters[0..2]`   | Watt-minutes (W-min) | value ÷ 60 → Wh    |
| Gen2+      | `aenergy.by_minute[0..2]`   | Milliwatt-hours (mWh)| value ÷ 1000 → Wh  |

Slot 0 is the last complete minute, slot 1 the minute before, slot 2 the one before that.

### `lastPower1` / `energyAvg1Min` — Backward Compatibility

`lastPower1` is kept active alongside `energyAvg1Min`.
Every update writes `lastPower1` (W, average power of the previous minute) **and** `energyAvg1Min` (Wh, energy of that minute) so items linked to either channel continue to receive updates without re-discovery.
The unit type mismatch (W vs. Wh) prevents the wrong value from crossing over: writing W to a `Number:Energy` channel is a silent no-op, so each channel always holds the value in its own unit.

This applies to all metered devices — Gen1 relay-PM, Gen2+ relay-PM, and Gen1/Gen2 3EM devices.

## Device-Total Channels (group `device`)

| Channel ID                  | Type          | Unit | Description                                               |
| --------------------------- | ------------- | ---- | --------------------------------------------------------- |
| `totalEnergy`               | Number:Energy | kWh  | Device-level total consumed energy (all phases/clamps)    |
| `accumulatedPower`          | Number:Power  | W    | Device-level instantaneous active power (sum of meters)   |
| `accumulatedApparent`       | Number:Power  | VA   | Device-level apparent power sum (Pro 3EM, Pro EM-50 only) |
| `accumulatedReturnedEnergy` | Number:Energy | kWh  | Device-level total returned energy (Pro 3EM, Pro EM-50)   |
| `accumulatedWatts`          | Number:Power  | W    | **Deprecated** — alias for `accumulatedPower`             |
| `accumulatedReturned`       | Number:Energy | kWh  | **Deprecated** — alias for `accumulatedReturnedEnergy`    |
| `accumulatedWTotal`         | Number:Energy | kWh  | **Deprecated** — alias for `totalEnergy`                  |
| `totalKWH`                  | Number:Energy | kWh  | **Deprecated** — alias for `totalEnergy`                  |

`totalEnergy` uses the hardware-reported device total from `emdata:0` when available, and falls back to the sum of per-meter values otherwise.
It never shows 0 when real data is available.

## Breaking Changes in openHAB 5.x (PR #20805)

Old channel IDs that are listed as "active" continue to receive updates so existing item links and rules keep working without immediate changes.

### Channel Renames (auto-migrated — no re-discovery needed)

| Old channel ID               | New channel ID                     | Old unit | New unit | Old channel stays active |
| ---------------------------- | ---------------------------------- | -------- | -------- | ------------------------ |
| `meterN#lastPower1`          | `meterN#energyAvg1Min`             | W        | Wh       | Yes — average power in W |
| `meterN#reactiveWatts`       | `meterN#reactivePower`             | W        | VAR      | Yes — old channel keeps W unit |
| `meterN#currentWatts`        | `meterN#currentPower`              | W        | W        | Yes                      |
| `meterN#totalKWH`            | `meterN#totalEnergy`               | kWh      | kWh      | Yes                      |
| `meterN#returnedKWH`         | `meterN#returnedEnergy`            | kWh      | kWh      | Yes                      |
| `device#accumulatedWatts`    | `device#accumulatedPower`          | W        | W        | Yes                      |
| `device#accumulatedReturned` | `device#accumulatedReturnedEnergy` | kWh      | kWh      | Yes                      |
| `device#accumulatedWTotal`   | `device#totalEnergy`               | kWh      | kWh      | Yes                      |
| `device#accumulatedKWHTotal` | `device#totalEnergy`               | kWh      | kWh      | Yes                      |
| `device#totalKWH`            | `device#totalEnergy`               | kWh      | kWh      | Yes                      |
| `nmeter#nmTreshhold`         | `nmeter#nmThreshold`               | A        | A        | Yes                      |

The `device#totalKWH` rename uses a group-qualified migration rule, so it does not conflict with the per-meter `meterN#totalKWH` channels.

### Channel Type Changes

| Channel ID           | Old type               | New type | Impact                                                                            |
| -------------------- | ---------------------- | -------- | --------------------------------------------------------------------------------- |
| `meterN#powerFactor` | `Number:Dimensionless` | `Number` | Range is now −1.0 to +1.0; persistence entries and UoM transformations may break  |

### New Channels (no migration needed — re-discover to get them)

| Group    | Channel ID            | Type             | Unit | Devices                                                      |
| -------- | --------------------- | ---------------- | ---- | ------------------------------------------------------------ |
| `meterN` | `apparentPower`       | Number:Power     | VA   | Pro 3EM, Pro EM-50, Plus EM, Plus EM Mini G4                 |
| `device` | `accumulatedApparent` | Number:Power     | VA   | Pro 3EM, Pro EM-50 only                                      |
| `meterN` | `energyAvg1Min`       | Number:Energy    | Wh   | All relay-PM devices (Gen1 and Gen2+)                        |
| `meterN` | `energyAvg2Min`       | Number:Energy    | Wh   | All relay-PM devices (advanced channel)                      |
| `meterN` | `energyAvg3Min`       | Number:Energy    | Wh   | All relay-PM devices (advanced channel)                      |
| `meterN` | `frequency`           | Number:Frequency | Hz   | Gen4 PM devices and EM devices; absent on Gen2/Gen3 Plus 1PM |

## Device Support Matrix

| Device family                   | Meter groups           | `totalEnergy` | `returnedEnergy` | `reactivePower` | `apparentPower` | `energyAvg1Min` | `frequency` |
| ------------------------------- | ---------------------- | ------------- | ---------------- | --------------- | --------------- | --------------- | ----------- |
| Gen1 relay-PM (Shelly 1PM, 2.5) | meter                  | Yes           | —                | —               | —               | Yes             | —           |
| Gen1 3EM                        | meter1–meter3          | Yes           | Yes              | Yes             | —               | Yes             | —           |
| Plus 1PM / Plus 2PM (Gen2/Gen3) | meter / meter1–meter2  | Yes           | —                | —               | —               | Yes             | —           |
| Plus 1PM Gen4                   | meter                  | Yes           | —                | —               | —               | Yes             | Yes         |
| Plus Plug S                     | meter                  | Yes           | —                | —               | —               | Yes             | —           |
| Pro 3EM / 3EM-63 / 3EM-400      | meter1–meter3          | Yes           | Yes              | —               | Yes             | Yes             | Yes         |
| Pro EM-50                       | meter1–meter2          | Yes           | Yes              | —               | Yes             | Yes             | Yes         |
| Plus EM Mini Gen4               | meter                  | Yes           | Yes              | —               | Yes             | Yes             | Yes         |

A `—` entry means the channel is not reported by the firmware and will not appear.

## Migration Notes

The binding applies channel migration automatically at startup (schema version check).
No user action is needed for channels listed as "auto-migrated": both the old and new channel IDs are active and receive the same updates.
Update item links and rules to the new channel IDs at your convenience before the deprecated channels are removed in a future release.
Deprecated channels are marked `[deprecated] Use <new channel> instead.` in their channel description and are flagged as advanced.
Interim test builds of PR #20805 created a channel named `lastEnergy1`; it is superseded by `energyAvg1Min` (auto-migrated) and its channel type is kept as a stub so those Things load without errors.

## See Also

- [README.md](../README.md) — full channel table and breaking-changes list
- [AdvancedUsers.md](AdvancedUsers.md) — firmware upgrade and troubleshooting
