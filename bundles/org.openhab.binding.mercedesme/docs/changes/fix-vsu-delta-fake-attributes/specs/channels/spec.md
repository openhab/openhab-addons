# Delta for channels

## ADDED Requirements

### Requirement: Partial VehicleStatusUpdate Only Converts Fields Actually Present

`Mapper.fromVehicleStatusUpdate()` SHALL only include an attribute in its output map when the
corresponding field is actually present (`hasXxx()` is `true`) on the incoming `VehicleStatusUpdate`. It
SHALL NOT fabricate a default/zero-valued entry for a field the incoming message did not set.

#### Scenario: Partial update only touching preconditioning leaves other channels untouched

- GIVEN a `VehicleStatusUpdate` with `full_update = false` that only has `precond_now`, `precond_state`,
  and `vtime` set
- WHEN the binding converts it via `Mapper.fromVehicleStatusUpdate()`
- THEN the resulting map contains only the keys with a direct equivalent for those three fields
- AND the map does not contain `MB_KEY_SOC`, `MB_KEY_OVERALL_RANGE`, `MB_KEY_CHARGING_POWER`, or any
  other key whose field was absent from the incoming update

#### Scenario: Absent field does not overwrite a previously known channel value

- GIVEN the SoC channel currently shows `76 %` from an earlier update
- WHEN a partial `VehicleStatusUpdate` arrives without the `soc` field set
- THEN the SoC channel remains at `76 %`
- AND it is not reset to `0 %`

#### Scenario: Full update still populates every field the server actually sent

- GIVEN a `VehicleStatusUpdate` with `full_update = true` that has all of its typed fields set
- WHEN the binding converts it via `Mapper.fromVehicleStatusUpdate()`
- THEN the resulting map contains an entry for every one of those set fields, unchanged from today

---

_Delta for the `channels` domain. On archive, this requirement is appended to
`org.openhab.binding.mercedesme/docs/specs/channels/spec.md`._
