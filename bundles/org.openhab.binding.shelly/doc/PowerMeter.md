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
| currentWatts   | Number:Power             | W    | Active power                                             |
| totalKWH       | Number:Energy            | kWh  | Total accumulated consumed energy                        |
| returnedKWH    | Number:Energy            | kWh  | Total accumulated returned (feed-in) energy              |
| reactivePower  | Number:Power             | VAR  | Reactive power                                           |
| apparentPower  | Number:Power             | VA   | Apparent power (data-driven; absent if not reported)     |
| voltage        | Number:ElectricPotential | V    | Phase voltage                                            |
| current        | Number:ElectricCurrent   | A    | Phase current                                            |
| frequency      | Number:Frequency         | Hz   | Line frequency (data-driven; absent if not reported)     |
| powerFactor    | Number                   | —    | Power factor −1.0 to +1.0 (dimensionless)                |
| neutralCurrent | Number:ElectricCurrent   | A    | Neutral conductor current (Pro 3EM / Pro EM-50 only)     |
| lastEnergy1    | Number:Energy            | kWh  | Energy in the last complete minute                       |
| lastUpdate     | DateTime                 | —    | Timestamp of the last meter update                       |

## Device-Total Channels (group `device`)

| Channel ID          | Type          | Unit | Description                                              |
| ------------------- | ------------- | ---- | -------------------------------------------------------- |
| totalEnergy         | Number:Energy | kWh  | Device-level total consumed energy (all phases/clamps)   |
| accumulatedWatts    | Number:Power  | W    | Device-level instantaneous active power (sum of meters)  |
| accumulatedApparent | Number:Power  | VA   | Device-level apparent power sum (Pro 3EM / Pro EM-50)    |
| accumulatedReturned | Number:Energy | kWh  | Device-level total returned energy (Pro 3EM / Pro EM-50) |

`totalEnergy` uses the hardware-reported device total from `emdata:0` when available; otherwise
it sums the per-meter values. Never shows 0 when real data is available.

## Breaking Changes in openHAB 5.x

The following channel IDs changed. **Existing Things must be deleted and re-discovered**, and
item links / rules must be updated accordingly.

| Old channel ID               | New channel ID       | Auto-migrated | Notes                                                         |
| ---------------------------- | -------------------- | ------------- | ------------------------------------------------------------- |
| meterN#reactiveWatts         | meterN#reactivePower | Yes           | Unit changed: W → VAR                                         |
| meterN#lastPower1            | meterN#lastEnergy1   | Yes           | No re-discovery needed                                        |
| device#accumulatedWTotal     | device#totalEnergy   | Yes           | Pro 3EM                                                       |
| device#accumulatedKWHTotal   | device#totalEnergy   | Yes           | Relay-PM (Plus 2PM, Plus 1PM, Plus Strip 4 Gen4)              |
| device#totalKWH (dev. group) | device#totalEnergy   | No            | Name collision with meter#totalKWH; requires re-discovery     |
| meterN#powerFactor (type)    | Number (−1.0..+1.0)  | N/A           | Was Number:Dimensionless; persistence / UoM links break       |

Channels whose names did not change (`currentWatts`, `totalKWH`, `returnedKWH`,
`accumulatedWatts`) require no migration.

## Device Support Matrix

| Device family                      | Meter groups           | totalEnergy | returnedKWH | reactivePower | apparentPower |
| ---------------------------------- | ---------------------- | ----------- | ----------- | ------------- | ------------- |
| Gen1 relay-PM (Shelly 1PM, 2.5)    | meter                  | Yes         | —           | —             | —             |
| Gen1 3EM                           | meter1–meter3          | Yes         | Yes         | Yes           | —             |
| Plus 1PM / Plus 2PM                | meter / meter1–meter2  | Yes         | —           | —             | —             |
| Pro 3EM / Pro 3EM-63 / Pro 3EM-400 | meter1–meter3          | Yes         | Yes         | —             | Yes           |
| Pro EM-50                          | meter1–meter2          | Yes         | Yes         | —             | Yes           |
| Plus EM Mini Gen4                  | meter                  | Yes         | Yes         | —             | Yes           |

## Migration Notes

The binding applies channel migration automatically at startup (no user action needed) for
channels with a 1:1 rename (`lastPower1 → lastEnergy1`). The old channel remains visible in
the UI alongside the new channel until re-discovery.

For channels that require re-discovery, the old channel-type is retained as a deprecated stub
so existing Things do not log `channel-type not found` errors after upgrade.

## See Also

- [README.md](../README.md) — full channel table and breaking-changes list
- [AdvancedUsers.md](AdvancedUsers.md) — firmware upgrade and troubleshooting
