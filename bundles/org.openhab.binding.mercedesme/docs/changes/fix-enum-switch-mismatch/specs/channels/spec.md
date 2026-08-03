# Delta for channels

## ADDED Requirements

### Requirement: Enum-Typed Switch Attributes Report Their Actual State

The binding SHALL treat a binary (`0`/`1`) enum-typed attribute delivered via `int_value` the same way
as a boolean switch attribute: `0` maps to `OFF`, any non-zero value maps to `ON`.

#### Scenario: Park brake reported as engaged via int_value

- GIVEN a `parkbrakestatus` update with `int_value = 1` (`PARKBRAKESTATUS_ENGAGED`)
- WHEN the binding processes the update
- THEN the Park Brake channel is set to `ON`

#### Scenario: Wash water warning reported as inactive via int_value

- GIVEN a `warningwashwater` update with `int_value = 0` (`WARNINGWASHWATER_INACTIVE`)
- WHEN the binding processes the update
- THEN the Wash Water channel is set to `OFF`
- AND the Wash Water channel is not set to `UNDEF`

#### Scenario: Genuine boolean switch attribute is unaffected

- GIVEN a `chargingactive` update with `bool_value = true`
- WHEN the binding processes the update
- THEN the Charging Active channel is set to `ON`, using the existing `bool_value` handling

---

_Delta for the `channels` domain. On archive, this requirement is appended to
`org.openhab.binding.mercedesme/docs/specs/channels/spec.md`._
