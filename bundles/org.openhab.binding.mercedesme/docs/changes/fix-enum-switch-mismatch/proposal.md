# Proposal: Fix Enum-Typed Switch Attributes Always Reading UNDEF

## Intent

Several Switch-type channels never show a real state - they stay `UNDEF` indefinitely, matching
community reports of "Wash Water" and "Park Brake" not updating
([community.openhab.org/t/mercedes-me/136866/199](https://community.openhab.org/t/mercedes-me/136866/199)).

Root cause: `Mapper.fromVehicleStatusUpdate()` converts `parkbrakestatus`, `precondNow`,
`precondSeatFrontRight`/`FrontLeft`/`RearRight`/`RearLeft`, and `warningwashwater` via `putEnum(...)`,
which stores the value in the `int_value` oneof case (see `vehicle-events.proto`:
`ParkbrakestatusEnumAttribute`, `PrecondNowEnumAttribute`, `PrecondSeatEnumAttribute`,
`WarningwashwaterEnumAttribute` all wrap a proto enum, not a bool). But
`Mapper.getChannelStateMap()`'s "Switches" case reads these via `value.hasBoolValue()` /
`value.getBoolValue()`. Since the value lives in `int_value`, `hasBoolValue()` is always `false`, so
the `else` branch always falls back to `UnDefType.UNDEF` - regardless of the actual reported state.

Confirmed against real captured data, not just the `.proto` schema: `src/test/resources/vehiclestatusupdates/vsu-eqa-1.raw`
(line 682) and `vsu-eqa-2.raw` (line 768) both show
`parkbrakestatus { value: PARKBRAKESTATUS_ENGAGED metadata {...} }` - a populated enum, never a bare
bool. Both files' `warningwashwater { metadata {...} }` entries have no `value:` line, i.e. the
proto3-default `WARNINGWASHWATER_INACTIVE = 0` (also confirms the enum message shape, not a bool).

All four enums involved are binary 0/1:

| Key | Enum | 0 | 1 |
|---|---|---|---|
| `MB_KEY_PARKBRAKESTATUS` | `Parkbrakestatus` | `NOT_ENGAGED` | `ENGAGED` |
| `MB_KEY_PRECOND_NOW` | `PrecondNow` | `INACTIVE` | `ACTIVE` |
| `MB_KEY_PRECOND_SEAT_FRONT_RIGHT`/`FRONT_LEFT`/`REAR_RIGHT`/`REAR_LEFT` | `PrecondSeat` | `OFF` | `ON` |
| `MB_KEY_WARNINGWASHWATER` | `Warningwashwater` | `INACTIVE` | `TRIGGERED` |

## Scope

In scope:

- `Mapper.getChannelStateMap()`'s "Switches" case MUST also accept a value delivered via `int_value`
  (in addition to the existing `bool_value` handling), treating a non-zero value as `ON` and `0` as
  `OFF`, since all four affected enums use `0` for their "off"/inactive/not-engaged member.
- The five genuinely bool-valued keys already in the same case
  (`MB_KEY_WARNINGBRAKEFLUID`, `MB_KEY_WARNINGBRAKELININGWEAR`, `MB_KEY_WARNINGCOOLANTLEVELLOW`,
  `MB_KEY_WARNINGENGINELIGHT`, `MB_KEY_CHARGINGACTIVE`, all via `putBool()`) MUST keep working exactly
  as before - `hasBoolValue()` is checked first and unchanged.

Out of scope:

- Any enum with more than two members read through this case - none of the seven affected keys have
  one (verified against `vehicle-events.proto`), so this isn't handled generically here.
- The old `VEPUpdate` (non-typed) push path - it already delivered `bool_value` directly for these
  keys (per the existing, working `hasBoolValue()` branch), so it is unaffected either way.

## Open Questions

- None. Enum definitions and real captured data both confirm the `0`/`1` binary mapping.

---
