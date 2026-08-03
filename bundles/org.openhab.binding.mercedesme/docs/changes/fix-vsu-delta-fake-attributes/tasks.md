# Tasks: Fix Partial VehicleStatusUpdate Fabricating Zero Values

## 1. Mapper

- [x] 1.1 In `Mapper.fromVehicleStatusUpdate()`, gate every one of the ~95 `putXxx(...)` calls (bool,
      plain int64/double, enum, distance, pressure, speed, ratio, clock-hour, consumption, and the three
      complex array-typed fields) behind the corresponding `vsu.hasXxx()` check, so a field absent from
      the incoming message is never added to the output map.

## 2. Tests

- [x] 2.1 `MapperTest`: a `VehicleStatusUpdate` with only `precond_now` set converts to a map containing
      only `MB_KEY_PRECOND_NOW` - not all 95 (`whenPartialUpdateOnlyTouchesPrecondThenOtherAttributesAreAbsent`).
- [x] 2.2 `MapperTest`: a field absent from the incoming update is not present in the output map at all
      (as opposed to present with a default/zero value) (`whenFieldAbsentFromUpdateThenNotIncludedInMap`).
- [x] 2.3 `MapperTest` regression guard: the existing fixture-based tests (`loadFixture()`, exercising all
      22 previously-passing field-conversion tests) still pass unchanged, confirming every field the
      fixture actually sets still converts exactly as before.

---
