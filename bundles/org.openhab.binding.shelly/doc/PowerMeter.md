# Shelly Binding — Power Meter Channels

This document describes the power meter channel structure supported by the Shelly binding,
including changes introduced in openHAB 5.x and migration notes for users upgrading from 4.x.

## Channel Groups

Power meter data is exposed in two groups:

| Group        | Name pattern                  | Present on                                               |
| ------------ | ----------------------------- | -------------------------------------------------------- |
| Per-meter    | meter, meter1, meter2, meter3 | All metered devices                                      |
| Device total | device                        | Multi-meter devices (Pro 3EM, Pro EM-50, Plus 2PM family) |

Single-meter devices (Plus 1PM, Shelly 1PM, etc.) use the group name `meter`.
Multi-meter devices (Pro 3EM, Pro EM-50) use `meter1`, `meter2`, `meter3`.

## Per-Meter Channels

| Channel ID     | Type                     | Unit | Description                                              |
| -------------- | ------------------------ | ---- | -------------------------------------------------------- |
| currentWatts   | Number:Power             | W    | Active power (deprecated alias → `currentPower`)         |
| currentPower   | Number:Power             | W    | Active power                                             |
| totalKWH       | Number:Energy            | kWh  | Total accumulated consumed energy (deprecated alias → `totalEnergy`) |
| totalEnergy    | Number:Energy            | kWh  | Total accumulated consumed energy                        |
| returnedKWH    | Number:Energy            | kWh  | Total accumulated returned (feed-in) energy (deprecated alias → `returnedEnergy`) |
| returnedEnergy | Number:Energy            | kWh  | Total accumulated returned (feed-in) energy              |
| reactivePower  | Number:Power             | VAR  | Reactive power (was `reactiveWatts` — W → VAR rename)    |
| apparentPower  | Number:Power             | VA   | Apparent power (data-driven; absent if not reported)     |
| voltage        | Number:ElectricPotential | V    | Phase voltage                                            |
| current        | Number:ElectricCurrent   | A    | Phase current                                            |
| frequency      | Number:Frequency         | Hz   | Mains frequency — data-driven; absent if not reported by firmware (Gen4 PM devices and EM devices only; **not** available on Gen2/Gen3 Plus 1PM) |
| powerFactor    | Number                   | —    | Power factor −1.0 to +1.0 (dimensionless)                |
| neutralCurrent | Number:ElectricCurrent   | A    | Neutral conductor current (Pro 3EM / Pro EM-50 only)     |
| lastPower1     | Number:Power             | W    | Deprecated alias for `lastEnergy1` — kept for backward compatibility; receives the same updates |
| lastEnergy1    | Number:Energy            | Wh   | Energy consumed during the previous complete minute. On Gen1 relay-PM devices this is derived from the `counters[0]` watt-minute value; on Gen2 relay-PM it comes from `aenergy.by_minute[0]`. |
| lastUpdate     | DateTime                 | —    | Timestamp of the last meter update                       |

### Dual-write for `lastPower1` / `lastEnergy1`

Items linked to the old `lastPower1` channel do not need to be changed immediately.
Every time `lastEnergy1` (Wh) is written, the binding also writes the same numeric value to
`lastPower1` — the unit type prevents a full round-trip conversion, so both channels receive
the current Wh value. This allows a smooth migration without rule changes.

## Device-Total Channels (group `device`)

| Channel ID               | Type          | Unit | Description                                              |
| ------------------------ | ------------- | ---- | -------------------------------------------------------- |
| totalEnergy              | Number:Energy | kWh  | Device-level total consumed energy (all phases/clamps)   |
| accumulatedPower         | Number:Power  | W    | Device-level instantaneous active power (sum of meters)  |
| accumulatedApparent      | Number:Power  | VA   | Device-level apparent power sum (Pro 3EM / Pro EM-50)    |
| accumulatedReturnedEnergy| Number:Energy | kWh  | Device-level total returned energy (Pro 3EM / Pro EM-50) |

`totalEnergy` uses the hardware-reported device total from `emdata:0` when available; otherwise
it sums the per-meter values. Never shows 0 when real data is available.

## Device Support Matrix

| Device family                      | Meter groups           | totalEnergy | returnedEnergy | reactivePower | apparentPower | lastEnergy1 | frequency      |
| ---------------------------------- | ---------------------- | ----------- | -------------- | ------------- | ------------- | ----------- | -------------- |
| Gen1 relay-PM (Shelly 1PM, 2.5)    | meter                  | Yes         | —              | —             | —             | Yes         | —              |
| Gen1 3EM                           | meter1–meter3          | Yes         | Yes            | Yes           | —             | Yes         | —              |
| Plus 1PM Gen2/Gen3                 | meter                  | Yes         | —              | —             | —             | Yes         | —              |
| Plus 1PM Gen4                      | meter                  | Yes         | —              | —             | —             | Yes         | Yes            |
| Plus 2PM / Plus 2PM Gen3           | meter1–meter2          | Yes         | —              | —             | —             | Yes         | —              |
| Plus Plug S                        | meter                  | Yes         | —              | —             | —             | Yes         | —              |
| Pro 3EM / Pro 3EM-63 / Pro 3EM-400 | meter1–meter3          | Yes         | Yes            | —             | Yes           | Yes         | Yes            |
| Pro EM-50                          | meter1–meter2          | Yes         | Yes            | —             | Yes           | Yes         | Yes            |
| Plus EM Mini Gen4                  | meter                  | Yes         | Yes            | —             | Yes           | Yes         | Yes            |

Channels marked `—` are not reported by the device firmware and will not appear in the UI.

## Breaking Changes in openHAB 5.x

The following channel IDs changed. **Existing Things must be deleted and re-discovered** to get
the new channels. Item links and rules must be updated accordingly.

| Old channel ID               | New channel ID       | Auto-migrated | Notes                                                         |
| ---------------------------- | -------------------- | ------------- | ------------------------------------------------------------- |
| meterN#reactiveWatts         | meterN#reactivePower | Yes           | Unit changed: W → VAR. Old channel stays active.             |
| meterN#lastPower1            | meterN#lastEnergy1   | Yes           | Unit changed: W (power-like) → Wh (energy). Old channel stays active; both receive updates. |
| device#accumulatedWTotal     | device#totalEnergy   | Yes           | Was reporting wrong values (Amperes/1000). Old channel stays active. |
| device#accumulatedKWHTotal   | device#totalEnergy   | Yes           | Relay-PM (Plus 2PM etc.). Old channel stays active.           |
| device#totalKWH (dev. group) | device#totalEnergy   | **No**        | Name collision with `meter#totalKWH`; re-discovery required.  |
| meterN#powerFactor type      | Number (−1.0..+1.0)  | N/A           | Was Number:Dimensionless; persistence / UoM links may break.  |

Channels that kept the same ID (`currentWatts`, `totalKWH` per-meter, `returnedKWH`,
`accumulatedWatts`) require no migration and receive updates as before.

## Migration Notes

The binding applies channel migration automatically at startup (no user action needed) for
channels with a 1:1 rename (`lastPower1 → lastEnergy1`, `reactiveWatts → reactivePower`).
The old channel remains visible alongside the new channel and continues to receive updates
until you re-discover the Thing and switch your item links to the new channel IDs.

For channels that require re-discovery, the old channel-type is retained as a deprecated stub
so existing Things do not log `channel-type not found` errors after upgrade.

## See Also

- [README.md](../README.md) — full channel table and breaking-changes list
- [AdvancedUsers.md](AdvancedUsers.md) — firmware upgrade and troubleshooting
