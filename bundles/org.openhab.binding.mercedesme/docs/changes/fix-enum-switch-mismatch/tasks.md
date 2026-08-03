# Tasks: Fix Enum-Typed Switch Attributes Always Reading UNDEF

## 1. Mapper

- [x] 1.1 In the "Switches" case of `Mapper.getChannelStateMap()`, after the existing
      `Utils.isNil(value)` check, try `value.hasBoolValue()` first (unchanged), then fall back to
      `value.hasIntValue()` treating a non-zero value as `ON` and `0` as `OFF`, else `UNDEF`.

## 2. Tests

- [x] 2.1 `MapperTest`: `MB_KEY_PARKBRAKESTATUS` with `int_value = 1` -> `OnOffType.ON`.
- [x] 2.2 `MapperTest`: `MB_KEY_WARNINGWASHWATER` with `int_value = 0` -> `OnOffType.OFF`, not `UNDEF`.
- [x] 2.3 `MapperTest`: `MB_KEY_CHARGINGACTIVE` with `bool_value = true` -> `OnOffType.ON` (regression
      guard - genuine bool_value keys must keep working).

---
