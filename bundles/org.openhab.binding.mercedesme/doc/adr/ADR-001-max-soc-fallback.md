# ADR-001: Support `charge#max-soc` for vehicles without `chargePrograms` data

**Status:** Accepted
**Date:** 2026-07-06
**Last updated:** 2026-07-08 (see Amendment)
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
| `BatteryMaxSocConfigure` | 28 | `int32 max_soc = 1` only | No charge program needed — sets max-soc directly. Closest shape match for vehicles with a flat `maxSoc` attribute, but rejected in a real-vehicle test against a CLA-class car — see Amendment. |
| `ChargingConfigure` | 72 | `charge_program` enum (DEFAULT/HOME/WORK) + `max_soc` (Int32Value) | Newest field number in the oneof. Originally scoped out as a still-evolving unified replacement, but adopted for the flat-attribute write path once `BatteryMaxSocConfigure` failed real-vehicle testing — see Amendment. |

No `command-capabilities` / `feature-capabilities` data is present in either test fixture, so the write path cannot be gated on a confirmed capability key yet — see Action Items.

## Decision

Adopt a **shape-detection fallback**: keep the existing `chargePrograms`-based logic for vehicles that report it, and add a parallel flat-attribute path for vehicles that don't.

1. Track whether a vehicle's update ever included `chargePrograms` (`VehicleHandler.hasChargePrograms`).
1. **Read side**: if `chargePrograms` is present, keep the existing per-program logic. If absent, populate `charge#max-soc` directly from the flat `maxSoc` attribute (`Constants.MB_KEY_MAX_SOC`), and derive dynamic command options from `maxSocLowerLimit`/`maxSocUpperLimit` (`Constants.MB_KEY_MAX_SOC_LOWER_LIMIT`/`MB_KEY_MAX_SOC_UPPER_LIMIT`) via `MercedesMeCommandOptionProvider`, in 10% steps within the reported bounds — instead of the static XML list.
1. **Write side**: branch on the same flag. Vehicles with program data keep sending `ChargeProgramConfigure`. Vehicles without it send `ChargingConfigure` instead (originally `BatteryMaxSocConfigure` — see Amendment), bypassing the `commandChargeProgramConfigure` capability gate (which doesn't apply to this command).

## Options Considered

### Option A: Read-only fallback, keep `ChargeProgramConfigure` for all writes

Fixes display only; leaves "changing" max-soc unverified/likely broken for CLA-class vehicles.

### Option B: Shape-detection fallback (read + write) — chosen

Fixes both display and control; reuses the existing dynamic-option pattern (already used for HVAC zones and charge programs); no regression for existing `chargePrograms` vehicles. Originally sent `BatteryMaxSocConfigure` for the write side as the closest proto match by shape; a real-vehicle test against a CLA-class car showed this command fails, so the write side was switched to `ChargingConfigure` — see Amendment.

### Option C: Send both commands redundantly

Avoids detection but risks conflicting/unpredictable backend behavior and poor failure signal.

### Option D: Migrate everything to `ChargingConfigure`

Highest risk — field layout not fully understood, would break `auto_unlock`/`location_based_charging`/`clock_timer`/`eco_charging` semantics tied to `ChargeProgramConfigure` until re-mapped. Not chosen: the Amendment below only swaps the command used on the flat-attribute (no `chargePrograms`) write path, it does not touch the `ChargeProgramConfigure` path, so this remains a partial, not full, migration.

## Consequences

- Easier: display of current max-soc works for both vehicle shapes; command options reflect real per-vehicle bounds instead of a static guess.
- Harder: two write code paths to maintain instead of one, until/unless the backend generations converge.
- Revisit: once the real command-capability keys for `VehicleAPI`-backend vehicles are known, re-evaluate whether `ChargingConfigure` should also replace `ChargeProgramConfigure` for vehicles that do report `chargePrograms`.

## Implementation

- `Constants.java`: added `MB_KEY_MAX_SOC`, `MB_KEY_MAX_SOC_LOWER_LIMIT`, `MB_KEY_MAX_SOC_UPPER_LIMIT`.
- `VehicleHandler.java`: added `hasChargePrograms` field; fallback read path + `updateMaxSocCommandOptions(...)`; write-path branch in `handleCommand` sending `ChargingConfigure` when `!hasChargePrograms` (originally `BatteryMaxSocConfigure` — see Amendment).
- Test-only `ProtoConverter.clientMessage2Json(...)`: added `hasChargingConfigure()` branch, unwrapping the `Int32Value max_soc` field the same way as `ChargeProgramConfigure`, so tests can inspect the sent command.
- `VehicleHandlerTest.java`: added `testMaxSocFallbackWithoutChargePrograms()` using `MB-BEV-CLA.json`, asserting the channel resolves to `100 %` and that commanding it sends `ChargingConfigure` with the requested value.

## Action Items

1. [x] Add `MB_KEY_MAX_SOC`, `MB_KEY_MAX_SOC_LOWER_LIMIT`, `MB_KEY_MAX_SOC_UPPER_LIMIT` constants.
1. [x] Track `hasChargePrograms`; add the flat-attribute fallback read path.
1. [x] Build dynamic `max-soc` command options from `maxSocLowerLimit`/`maxSocUpperLimit`.
1. [x] Branch the `OH_CHANNEL_MAX_SOC` command handler between `ChargingConfigure` and `ChargeProgramConfigure`.
1. [x] Confirm against a real CLA-class vehicle capture that `BatteryMaxSocConfigure` is actually accepted before this ships to users. Result: rejected — see Amendment.
1. [x] Wire `MB-BEV-CLA.json` into `VehicleHandlerTest.java`.
1. [x] Add a command test asserting `ChargingConfigure` is sent for a thing whose last update had no `chargePrograms`.
1. [x] Confirm `ChargingConfigure` against a real CLA-class vehicle capture before this ships to users. Result: confirmed — see [openhab-addons#21032](https://github.com/openhab/openhab-addons/issues/21032#issuecomment-4922536211).

## Amendment (2026-07-08): switch write path to `ChargingConfigure`

Real-vehicle testing against a CLA-class car showed the `BatteryMaxSocConfigure` command from the original Decision was rejected/had no effect. The write path for vehicles without `chargePrograms` (`!hasChargePrograms`) now sends `ChargingConfigure` (field 72, setting only `max_soc`, leaving `charge_program` at its `DEFAULT_CHARGE_PROGRAM` default) instead.

This does not change the overall shape-detection approach (Option B) or touch the `ChargeProgramConfigure` path used for vehicles that do report `chargePrograms` — it only swaps which command is sent on the fallback write branch. `BatteryMaxSocConfigure` is no longer used anywhere in the binding.

Files touched: `VehicleHandler.java` (write-path branch and ADR-001 comments), `ProtoConverter.java` (test helper: `hasChargingConfigure()` branch replacing `hasBatteryMaxSoc()`, with `Int32Value` unwrapping), `VehicleHandlerTest.java` (`testMaxSocFallbackWithoutChargePrograms()` comment/assertion).

Confirmed: `ChargingConfigure` is accepted by a real CLA-class vehicle — see [openhab-addons#21032](https://github.com/openhab/openhab-addons/issues/21032#issuecomment-4922536211). No open checkpoints remain before this ships to users.
