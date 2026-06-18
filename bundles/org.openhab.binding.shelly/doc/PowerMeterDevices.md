# Shelly Power Meter Devices

Reference for all Shelly devices with power measurement capability.

## Channel Abbreviations

**Per-meter channels** (group `meter` for single-meter devices, `meter1`, `meter2`, … for multi-meter devices):

| ID   | Channel         | Unit                            |
| ---- | --------------- | ------------------------------- |
| W    | `currentWatts`  | W — active power                |
| kWh  | `totalKWH`      | kWh — consumed energy           |
| kWh← | `returnedKWH`   | kWh — returned / feed-in energy |
| VAR  | `reactiveWatts` | VAR — reactive power            |
| PF   | `powerFactor`   | − (−1.0 … +1.0)                 |
| V    | `voltage`       | V                               |
| A    | `current`       | A                               |
| Hz   | `frequency`     | Hz — line frequency             |

**Device-level channels** (group `device`):

| ID      | Channel                | Notes                                                                     |
| ------- | ---------------------- | ------------------------------------------------------------------------- |
| ΣW      | `accumulatedWatts`     | Total active power; device-reported when available                        |
| ΣkWh    | `accumulatedWTotal`    | Sum of per-meter kWh; only when device has no hardware total              |
| ΣkWh-hw | `totalKWH` (device)    | Hardware total from `emdata:0` / `em1data:x`; Gen2 EM only                |
| ΣkWh←   | `accumulatedReturned`  | Device-level returned energy                                              |

## Gen1 — Simple Relay PM

Energy counters stored in **RAM** → **lost on device restart**. Use openHAB persistence for long-term tracking.

| Device                | Thing type                           | # Meters | Per-meter channels | Device-level channels | Counter on restart | Notes                                   |
| --------------------- | ------------------------------------ | -------- | ------------------ | --------------------- | ------------------ | --------------------------------------- |
| Shelly 1PM            | `shelly1pm`                          | 1        | W, kWh             | —                     | **Lost**           | No V/A/PF/Hz                            |
| Shelly Plug S         | `shellyplugs`                        | 1        | W, kWh             | —                     | **Lost**           | No V/A/PF/Hz                            |
| Shelly Plug US (Gen1) | `shellyplugu1`                       | 1        | W, kWh             | —                     | **Lost**           | No V/A/PF/Hz                            |
| Shelly 2.5            | `shelly25-relay` / `shelly25-roller` | 2        | W, kWh             | ΣW, ΣkWh              | **Lost**           | 2 relay+PM; roller mode shares channels |

## Gen1 — Energy Meters (EM)

Energy counters stored in device **NVM** → **preserved on restart**.

| Device     | Thing type  | # Meters | Per-meter channels          | Device-level channels      | Counter on restart | Notes                                                                  |
| ---------- | ----------- | -------- | --------------------------- | -------------------------- | ------------------ | ---------------------------------------------------------------------- |
| Shelly EM  | `shellyem`  | 2        | W, kWh, kWh←, VAR, PF, V, A | ΣW, ΣkWh, ΣkWh←            | Preserved          | 2 CT clamps + 1 relay output; relay not separately metered             |
| Shelly 3EM | `shellyem3` | 3        | W, kWh, kWh←, VAR, PF, V, A | ΣW, ΣkWh, ΣkWh←, neutral-I | Preserved          | 3-phase; neutral-current channel (group `nmeter`); no controlled relay |

## Gen2 / Gen3 / Gen4 — Relay with PM (switch:x)

Energy counters in device **NVM** → **preserved on restart**.
`frequency` channel created data-driven (`emeter.frequency != null`): only Gen4+ firmware exposes `freq` in `switch:x` / `pm1:x` status.
No reactive power and no apparent power on switch:x interface.

| Device                 | Thing type             | Gen   | # Meters | Per-meter channels   | Device-level channels | Counter on restart | Notes                           |
| ---------------------- | ---------------------- | ----- | -------- | -------------------- | --------------------- | ------------------ | ------------------------------- |
| Plus 1PM / G3 / G4     | `shellyplus1pm`        | 2/3/4 | 1        | W, kWh, V, A, PF     | —                     | Preserved          | Hz on Gen4+ only                |
| Plus 2PM (relay)       | `shellyplus2pm-relay`  | 2/3/4 | 2        | W, kWh, V, A, PF     | ΣW, ΣkWh              | Preserved          | Hz on Gen4+ only                |
| Plus 2PM (roller)      | `shellyplus2pm-roller` | 2/3/4 | 2        | W, kWh, V, A, PF     | ΣW, ΣkWh              | Preserved          | Roller: single virtual channel  |
| Plus Plug C PM (UK/CH) | `shellyplusplugcpm`    | 2     | 1        | W, kWh, V, A, PF     | —                     | Preserved          |                                 |
| Plus Plug US G4        | `shellyplugusg4`       | 4     | 1        | W, kWh, V, A, PF, Hz | —                     | Preserved          | Gen4: Hz always present         |
| PM Mini / G3           | `shellypmmini`         | 2/3   | 1        | W, kWh, V, A, PF     | —                     | Preserved          | No relay                        |
| 1PM Mini / G3 / G4     | `shelly1pmmini`        | 2/3/4 | 1        | W, kWh, V, A, PF     | —                     | Preserved          | Hz on Gen4+ only                |
| Pro 1PM                | `shellypro1pm`         | 2     | 1        | W, kWh, V, A, PF     | —                     | Preserved          |                                 |
| Pro 2PM (relay)        | `shellypro2pm-relay`   | 2     | 2        | W, kWh, V, A, PF     | ΣW, ΣkWh              | Preserved          |                                 |
| Pro 2PM (roller)       | `shellypro2pm-roller`  | 2     | 2        | W, kWh, V, A, PF     | ΣW, ΣkWh              | Preserved          | Roller: single virtual channel  |
| Pro 4PM                | `shellypro4pm`         | 2     | 4        | W, kWh, V, A, PF     | ΣW, ΣkWh              | Preserved          | 4 independent relay+PM channels |

## Gen2 / Gen3 / Gen4 — Single-Clamp EM (em1:x)

Energy counters in device **NVM** → **preserved on restart**.
`reactiveWatts` channel **not available** (em1:x API does not expose reactive power).

| Device          | Thing type     | Gen | # Meters | Per-meter channels              | Device-level channels   | Counter on restart | Notes                                                                         |
| --------------- | -------------- | --- | -------- | ------------------------------- | ----------------------- | ------------------ | ----------------------------------------------------------------------------- |
| EM Mini / G4    | `shellyemmini` | 2/4 | 1        | W, kWh, kWh←¹, VA, PF, V, A, Hz | —                       | Preserved          | ¹`returnedKWH` data-driven; single CT clamp                                   |
| Plus EM / EM G3 | `shellyplusem` | 3   | 2        | W, kWh, kWh←, PF, V, A, Hz     | ΣW, ΣkWh-hw, ΣkWh←        | Preserved          | 2 CT clamps; 1 relay (group `relay`); no reactive power                        |

## Gen2 / Gen3 — 3-Phase EM (em:x)

Energy counters in device **NVM** → **preserved on restart**.
`reactiveWatts` channel **not available** — em:x API exposes only active, apparent, and PF per phase.
Neutral-current channel (group `nmeter`) where hardware supports it.

| Device               | Thing type        | Gen | # Meters | Per-meter channels             | Device-level channels              | Counter on restart | Notes                                                                                               |
| -------------------- | ----------------- | --- | -------- | ------------------------------ | ---------------------------------- | ------------------ | --------------------------------------------------------------------------------------------------- |
| Plus 3EM-63 / 3EM G3 | `shellyplus3em63` | 3   | 3        | W, kWh, kWh←, VA, PF, V, A, Hz | ΣW, ΣkWh-hw, ΣkWh←, neutral-I | Preserved          | 3-phase 63A; no relay                                                                               |
| Pro EM-50            | `shellyproem50`   | 2   | 2        | W, kWh, kWh←, PF, V, A, Hz     | ΣW, ΣkWh-hw, ΣkWh←            | Preserved          | 2×50A CT clamps; 1 relay (group `relay`); `resetTotals` channel per meter; no reactive power        |
| Pro 3EM              | `shellypro3em`    | 2   | 3        | W, kWh, kWh←, VA, PF, V, A, Hz | ΣW, ΣkWh-hw, ΣkWh←, neutral-I | Preserved          | 3-phase                                                                                             |
| Pro 3EM-63           | `shellypro3em63`  | 2   | 3        | W, kWh, kWh←, VA, PF, V, A, Hz | ΣW, ΣkWh-hw, ΣkWh←, neutral-I | Preserved          | 3-phase 63A CT clamps                                                                               |
| Pro 3EM-400          | `shellypro3em400` | 2   | 3        | W, kWh, kWh←, VA, PF, V, A, Hz | ΣW, ΣkWh-hw, ΣkWh←, neutral-I | Preserved          | 3-phase 400A CT clamps                                                                              |

## Known Restrictions and Issues

| Issue                                     | Devices                                                                | Status                                                                              |
| ----------------------------------------- | ---------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| Energy counter reset on restart           | Gen1 1PM, Plug S, Plug US, 2.5                                         | By design — use OH persistence                                                      |
| No reactive power                         | All Gen2+ EM (em:x, em1:x)                                             | API limitation — not in em:x protocol                                               |
| `frequency` Gen1 EM/3EM                   | `shellyem`, `shellyem3`                                                | Data-driven; channel absent if device doesn't report it                             |
| `returnedKWH` (emdata) always null        | `shellypro3em`, `shellypro3em63`, `shellypro3em400`, `shellyplus3em63` | Wrong `@SerializedName` (`_act_ret_` keys) — fixed in PR [#20805][pr-fixmeters]     |
| Per-phase frequency always null           | Same 3-phase EM devices                                                | `a_freq`/`b_freq`/`c_freq` mapping missing — fixed in PR [#20805][pr-fixmeters]    |
| Pro EM-50 slot routing (clamp assignment) | `shellyproem50`                                                        | Deferred to separate PR                                                             |

[pr-fixmeters]: https://github.com/openhab/openhab-addons/pull/20805
