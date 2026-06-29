# Shelly Binding — Power Meter Channels

Power meter data is exposed in two channel groups per device.

## Channel Groups

| Group        | Name pattern                  | Present on                                                |
| ------------ | ----------------------------- | --------------------------------------------------------- |
| Per-meter    | `meter`, `meter1`–`meter3`    | All metered devices                                       |
| Device total | `device`                      | Multi-meter devices (Pro 3EM, Pro EM-50, Plus 2PM family) |

Single-meter devices (Plus 1PM, Shelly 1PM, etc.) use the group name `meter`.
Multi-meter devices (Pro 3EM, Pro EM-50) use `meter1`, `meter2`, `meter3`.

## Per-Meter Channels

| Channel ID      | Type                     | Unit | Description                                                |
| --------------- | ------------------------ | ---- | ---------------------------------------------------------- |
| `currentPower`  | Number:Power             | W    | Active power (replaces `currentWatts`)                     |
| `currentWatts`  | Number:Power             | W    | **Deprecated** — alias for `currentPower`; old items keep working |
| `totalEnergy`   | Number:Energy            | kWh  | Total accumulated consumed energy (replaces `totalKWH`)    |
| `totalKWH`      | Number:Energy            | kWh  | **Deprecated** — alias for `totalEnergy`; old items keep working  |
| `returnedEnergy`| Number:Energy            | kWh  | Total accumulated returned (feed-in) energy (replaces `returnedKWH`) |
| `returnedKWH`   | Number:Energy            | kWh  | **Deprecated** — alias for `returnedEnergy`; old items keep working |
| `reactivePower` | Number:Power             | VAR  | Reactive power (replaces `reactiveWatts`; unit changed W→VAR) |
| `reactiveWatts` | Number:Power             | VAR  | **Deprecated** — alias for `reactivePower`; old items keep working |
| `apparentPower` | Number:Power             | VA   | Apparent power — data-driven; absent if not reported       |
| `voltage`       | Number:ElectricPotential | V    | Phase voltage                                              |
| `current`       | Number:ElectricCurrent   | A    | Phase current                                              |
| `frequency`     | Number:Frequency         | Hz   | Mains frequency — data-driven; absent if firmware does not report it (Gen4 PM and EM devices only) |
| `powerFactor`   | Number                   | —    | Power factor −1.0 to +1.0 (dimensionless `Number`)         |
| `neutralCurrent`| Number:ElectricCurrent   | A    | Neutral conductor current (Pro 3EM, Pro EM-50 only)        |
| `lastEnergy1`   | Number:Energy            | Wh   | Energy consumed in the previous complete minute (replaces `lastPower1`) |
| `lastPower1`    | Number:Power             | W    | **Deprecated** — alias for `lastEnergy1`; old items keep working |
| `lastUpdate`    | DateTime                 | —    | Timestamp of the last meter update                         |

### `lastPower1` / `lastEnergy1` — Backward Compatibility

`lastPower1` is kept active alongside `lastEnergy1`.
Every update writes `lastPower1` (W, legacy value) **and** `lastEnergy1` (Wh, correct energy value)
so that items linked to either channel continue to receive updates without re-discovery.
The unit type mismatch (W vs. Wh) prevents the wrong value from crossing over:
writing W to a `Number:Energy` channel is a silent no-op, so each channel always holds
the value in its own unit.

This applies to all metered devices — Gen1 relay-PM (from `counters[0]` watt-minutes),
Gen2+ relay-PM (from `aenergy.by_minute[0]` Wh), and Gen1/Gen2 3EM devices.

## Device-Total Channels (group `device`)

| Channel ID                | Type          | Unit | Description                                               |
| ------------------------- | ------------- | ---- | --------------------------------------------------------- |
| `totalEnergy`             | Number:Energy | kWh  | Device-level total consumed energy (all phases/clamps)    |
| `accumulatedPower`        | Number:Power  | W    | Device-level instantaneous active power (sum of meters)   |
| `accumulatedApparent`     | Number:Power  | VA   | Device-level apparent power sum (Pro 3EM, Pro EM-50 only) |
| `accumulatedReturnedEnergy` | Number:Energy | kWh | Device-level total returned energy (Pro 3EM, Pro EM-50)   |
| `accumulatedWatts`        | Number:Power  | W    | **Deprecated** — alias for `accumulatedPower`             |
| `accumulatedReturned`     | Number:Energy | kWh  | **Deprecated** — alias for `accumulatedReturnedEnergy`    |
| `accumulatedWTotal`       | Number:Energy | kWh  | **Deprecated** — alias for `totalEnergy`                  |

`totalEnergy` uses the hardware-reported device total from `emdata:0` when available, and
falls back to the sum of per-meter values otherwise.
It never shows 0 when real data is available.

## Breaking Changes in openHAB 5.x (PR #20805)

The changes below require **existing Things to be deleted and re-discovered** to get the new
channel IDs. Old channel IDs that are listed as "active" continue to receive updates so existing
item links and rules keep working without immediate changes.

### Channel Renames (auto-migrated — no re-discovery needed)

| Old channel ID             | New channel ID              | Old unit | New unit | Old channel stays active |
| -------------------------- | --------------------------- | -------- | -------- | ------------------------ |
| `meterN#lastPower1`        | `meterN#lastEnergy1`        | W        | Wh       | Yes — receives same value in W |
| `meterN#reactiveWatts`     | `meterN#reactivePower`      | W        | VAR      | Yes                      |
| `meterN#currentWatts`      | `meterN#currentPower`       | W        | W        | Yes                      |
| `meterN#totalKWH`          | `meterN#totalEnergy`        | kWh      | kWh      | Yes                      |
| `meterN#returnedKWH`       | `meterN#returnedEnergy`     | kWh      | kWh      | Yes                      |
| `device#accumulatedWatts`  | `device#accumulatedPower`   | W        | W        | Yes                      |
| `device#accumulatedReturned` | `device#accumulatedReturnedEnergy` | kWh | kWh | Yes                  |
| `device#accumulatedWTotal` | `device#totalEnergy`        | kWh      | kWh      | Yes                      |
| `device#accumulatedKWHTotal` | `device#totalEnergy`      | kWh      | kWh      | Yes                      |
| `nmeter#nmTreshhold`       | `nmeter#nmThreshold`        | A        | A        | Yes                      |

### Channels Requiring Re-discovery

| Old channel ID               | New channel ID     | Reason                                                               |
| ---------------------------- | ------------------ | -------------------------------------------------------------------- |
| `device#totalKWH` (device group) | `device#totalEnergy` | Name collision with `meter#totalKWH` prevents auto-rename; re-discover the Thing |

### Channel Type Changes

| Channel ID             | Old type            | New type | Impact                                                    |
| ---------------------- | ------------------- | -------- | --------------------------------------------------------- |
| `meterN#powerFactor`   | `Number:Dimensionless` | `Number` | Range is now −1.0 to +1.0; persistence entries and UoM transformations may break |

### New Channels (no migration needed — re-discover to get them)

| Group   | Channel ID            | Type          | Unit | Devices                                       |
| ------- | --------------------- | ------------- | ---- | --------------------------------------------- |
| `meterN` | `apparentPower`      | Number:Power  | VA   | Pro 3EM, Pro EM-50, Plus EM, Plus EM Mini G4  |
| `device` | `accumulatedApparent`| Number:Power  | VA   | Pro 3EM, Pro EM-50 only                       |
| `meterN` | `lastEnergy1`        | Number:Energy | Wh   | All relay-PM devices (Gen1 and Gen2+)         |
| `meterN` | `frequency`          | Number:Frequency | Hz | Gen4 PM devices and EM devices; absent on Gen2/Gen3 Plus 1PM |

## Device Support Matrix

| Device family                      | Meter groups         | `totalEnergy` | `returnedEnergy` | `reactivePower` | `apparentPower` | `lastEnergy1` | `frequency` |
| ---------------------------------- | -------------------- | ------------- | ---------------- | --------------- | --------------- | ------------- | ----------- |
| Gen1 relay-PM (Shelly 1PM, 2.5)    | meter                | Yes           | —                | —               | —               | Yes           | —           |
| Gen1 3EM                           | meter1–meter3        | Yes           | Yes              | Yes             | —               | Yes           | —           |
| Plus 1PM / Plus 2PM (Gen2/Gen3)    | meter / meter1–meter2 | Yes          | —                | —               | —               | Yes           | —           |
| Plus 1PM Gen4                      | meter                | Yes           | —                | —               | —               | Yes           | Yes         |
| Plus Plug S                        | meter                | Yes           | —                | —               | —               | Yes           | —           |
| Pro 3EM / 3EM-63 / 3EM-400        | meter1–meter3        | Yes           | Yes              | —               | Yes             | Yes           | Yes         |
| Pro EM-50                          | meter1–meter2        | Yes           | Yes              | —               | Yes             | Yes           | Yes         |
| Plus EM Mini Gen4                  | meter                | Yes           | Yes              | —               | Yes             | Yes           | Yes         |

A `—` entry means the channel is not reported by the firmware and will not appear.

## Migration Notes

The binding applies channel migration automatically at startup (schema version check).
No user action is needed for channels listed as "auto-migrated": both the old and new channel IDs
are active and receive the same updates.
Update item links and rules to the new channel IDs at your convenience before the
deprecated channels are removed in a future release.

For `device#totalKWH` (device group only), **delete and re-discover the Thing** to get
`device#totalEnergy`. The old `device#totalKWH` channel stops receiving updates after upgrade.

## See Also

- [README.md](../README.md) — full channel table and breaking-changes list
- [AdvancedUsers.md](AdvancedUsers.md) — firmware upgrade and troubleshooting
