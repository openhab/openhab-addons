# ADR-001: Support `charge#max-soc` for vehicles without `chargePrograms` data

**Status:** Accepted
**Date:** 2026-07-06
**Deciders:** Bernd (binding maintainer)

## Context

`charge#max-soc` is declared as a supported channel for BEV/Hybrid things (`charge-channel-types.xml`), but for the `MB-BEV-CLA` fixture it never gets a value.

`VehicleHandler.java` only derived the channel from the nested `chargePrograms` attribute: it builds a `chargeGroupValueStorage` map from `chargeProgramParameters`, then looks up `maxSoc` for the `selectedChargeProgram` index. The state update only fired if `chargeGroupValueStorage` was non-empty.

`MB-BEV-CLA.json` has **no `chargePrograms` key** — it only reports flat, standalone attributes:

```json
"maxSoc": { "int_value": 100, "display_value": "100", ... },
"maxSocLowerLimit": { "int_value": 30, "display_value": "30", ... },
"maxSocUpperLimit": { "int_value": 100, "display_value": "100", ... }
```

By contrast, `MB-BEV-EQA.json` and `MB-BEV-ChargeProgram0.json` report both the flat `maxSoc` attribute _and_ the nested `chargePrograms` list. Since the handler only read the nested form, `selectedChargeProgram` got set (from `selectedChargeProgram` = 4) but `chargeGroupValueStorage` stayed empty, so the update block was skipped and the channel was never populated for the CLA-style payload.

Separately, `maxSocLowerLimit` / `maxSocUpperLimit` were read nowhere in the codebase. The channel's command options were a static list in XML (`50/60/70/80/90/100 %`), independent of what a given vehicle actually supports (the CLA's own lower limit is 30%, which wasn't even offered).

### Proto findings (what the backend actually allows for setting max-soc)

`vehicle-commands.proto` defines three different commands that touch max-soc, at different points in the API's evolution:

| Command | Field # | Shape | Notes |
|---|---|---|---|
| `ChargeProgramConfigure` | 34 | `charge_program` enum (DEFAULT/HOME/WORK) + `max_soc` (Int32Value) + `auto_unlock`/`location_based_charging`/`clock_timer`/`eco_charging` | Previously used unconditionally by the binding. Requires selecting a program. |
| `BatteryMaxSocConfigure` | 28 | `int32 max_soc = 1` only | No charge program needed — sets max-soc directly. This shape matches vehicles that report a flat `maxSoc` attribute with no program list. |
| `ChargingConfigure` | 72 | `charge_program` enum (DEFAULT/HOME/WORK) + `max_soc` (Int32Value) | Newest field number in the oneof — looks like a still-evolving unified replacement; out of scope for this fix. |

No `command-capabilities` / `feature-capabilities` data is present in either test fixture, so the write path cannot be gated on a confirmed capability key yet — see Action Items.

## Decision

Adopt a **shape-detection fallback**: keep the existing `chargePrograms`-based logic for vehicles that report it, and add a parallel flat-attribute path for vehicles that don't.

1. Track whether a vehicle's update ever included `chargePrograms` (`VehicleHandler.hasChargePrograms`).
1. **Read side**: if `chargePrograms` is present, keep the existing per-program logic. If absent, populate `charge#max-soc` directly from the flat `maxSoc` attribute (`Constants.MB_KEY_MAX_SOC`), and derive dynamic command options from `maxSocLowerLimit`/`maxSocUpperLimit` (`Constants.MB_KEY_MAX_SOC_LOWER_LIMIT`/`MB_KEY_MAX_SOC_UPPER_LIMIT`) via `MercedesMeCommandOptionProvider`, in 10% steps within the reported bounds — instead of the static XML list.
1. **Write side**: branch on the same flag. Vehicles with program data keep sending `ChargeProgramConfigure`. Vehicles without it send `BatteryMaxSocConfigure` instead, bypassing the `commandChargeProgramConfigure` capability gate (which doesn't apply to this command).
1. `ChargingConfigure` is left for a follow-up ADR once its full field layout and real-world acceptance are confirmed.

## Options Considered

### Option A: Read-only fallback, keep `ChargeProgramConfigure` for all writes

Fixes display only; leaves "changing" max-soc unverified/likely broken for CLA-class vehicles.

### Option B: Shape-detection fallback (read + write) — chosen

Fixes both display and control; reuses the existing dynamic-option pattern (already used for HVAC zones and charge programs); no regression for existing `chargePrograms` vehicles. Main open risk: `BatteryMaxSocConfigure` is the closest proto match by shape but not yet confirmed against a real CLA capture.

### Option C: Send both commands redundantly

Avoids detection but risks conflicting/unpredictable backend behavior and poor failure signal.

### Option D: Migrate everything to `ChargingConfigure`

Highest risk — field layout not fully understood, would break `auto_unlock`/`location_based_charging`/`clock_timer`/`eco_charging` semantics tied to `ChargeProgramConfigure` until re-mapped.

## Consequences

- Easier: display of current max-soc works for both vehicle shapes; command options reflect real per-vehicle bounds instead of a static guess.
- Harder: two write code paths to maintain instead of one, until/unless the backend generations converge.
- Revisit: once `ChargingConfigure`'s full field layout and the real command-capability keys for `VehicleAPI`-backend vehicles are known, re-evaluate whether it should replace `BatteryMaxSocConfigure` and/or `ChargeProgramConfigure`.

## Implementation

- `Constants.java`: added `MB_KEY_MAX_SOC`, `MB_KEY_MAX_SOC_LOWER_LIMIT`, `MB_KEY_MAX_SOC_UPPER_LIMIT`.
- `VehicleHandler.java`: added `hasChargePrograms` field; fallback read path + `updateMaxSocCommandOptions(...)`; write-path branch in `handleCommand` sending `BatteryMaxSocConfigure` when `!hasChargePrograms`.
- Test-only `ProtoConverter.clientMessage2Json(...)`: added `hasBatteryMaxSoc()` branch so tests can inspect the sent command.
- `VehicleHandlerTest.java`: added `testMaxSocFallbackWithoutChargePrograms()` using `MB-BEV-CLA.json`, asserting the channel resolves to `100 %` and that commanding it sends `BatteryMaxSocConfigure` with the requested value.

## Action Items

1. [x] Add `MB_KEY_MAX_SOC`, `MB_KEY_MAX_SOC_LOWER_LIMIT`, `MB_KEY_MAX_SOC_UPPER_LIMIT` constants.
1. [x] Track `hasChargePrograms`; add the flat-attribute fallback read path.
1. [x] Build dynamic `max-soc` command options from `maxSocLowerLimit`/`maxSocUpperLimit`.
1. [x] Branch the `OH_CHANNEL_MAX_SOC` command handler between `BatteryMaxSocConfigure` and `ChargeProgramConfigure`.
1. [ ] Confirm against a real CLA-class vehicle capture (or ask the contributor who supplied the fixture) that `BatteryMaxSocConfigure` is actually accepted before this ships to users.
1. [x] Wire `MB-BEV-CLA.json` into `VehicleHandlerTest.java`.
1. [x] Add a command test asserting `BatteryMaxSocConfigure` is sent for a thing whose last update had no `chargePrograms`.
