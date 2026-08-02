# Delta for channels

## ADDED Requirements

### Requirement: Attribute Validity Gate

The binding SHALL treat a `VehicleAttributeStatus` as having no usable value when either its
`nil_value` oneof is set to `true`, or its `status` field is not `VALUE_VALID`.

#### Scenario: nil_value flag set

- GIVEN a `VehicleAttributeStatus` with `nil_value = true`
- WHEN the binding evaluates whether the attribute has a usable value
- THEN the binding treats the attribute as having no usable value

#### Scenario: status reports the value was not received

- GIVEN a `VehicleAttributeStatus` with `status = VALUE_NOT_RECEIVED` and `int_value = 0`
- WHEN the binding evaluates whether the attribute has a usable value
- THEN the binding treats the attribute as having no usable value
- AND the default `int_value = 0` is not forwarded as if it were a real reading

#### Scenario: status reports the value is invalid

- GIVEN a `VehicleAttributeStatus` with `status = VALUE_INVALID`
- WHEN the binding evaluates whether the attribute has a usable value
- THEN the binding treats the attribute as having no usable value

#### Scenario: status reports the value is not available

- GIVEN a `VehicleAttributeStatus` with `status = VALUE_NOT_AVAILABLE`
- WHEN the binding evaluates whether the attribute has a usable value
- THEN the binding treats the attribute as having no usable value

#### Scenario: status is valid

- GIVEN a `VehicleAttributeStatus` with `status = VALUE_VALID` and a populated numeric value
- WHEN the binding evaluates whether the attribute has a usable value
- THEN the binding treats the attribute as having a usable value

### Requirement: Percentage Channels Report UNDEF When Unavailable

The binding MUST update a percentage channel (State of Charge, tank level, AdBlue level,
eco-score sub-scores) to `UNDEF` instead of a numeric percent value when the source attribute has
no usable value per the Attribute Validity Gate requirement.

#### Scenario: State of Charge update arrives without a usable value during charging

- GIVEN a vehicle is actively charging and periodically sending State of Charge updates
- WHEN one such update has `status = VALUE_NOT_RECEIVED` and `int_value = 0`
- THEN the State of Charge channel is set to `UNDEF`
- AND the State of Charge channel is not set to `0 %`

#### Scenario: State of Charge update arrives with a valid value

- GIVEN a vehicle sends a State of Charge update with `status = VALUE_VALID` and `int_value = 74`
- WHEN the binding processes the update
- THEN the State of Charge channel is set to `74 %`

### Requirement: Numeric Channels Report UNDEF When Unavailable

The binding MUST update a distance (odo, range, trip distance, eco-score bonus range), power
(charging power), speed (average speed), consumption (electric/liquid, start and reset), or tire
pressure channel to `UNDEF` instead of a numeric value when the source attribute has no usable value
per the Attribute Validity Gate requirement, instead of forwarding the internal nil sentinel
(literally, or clamped to `0` by a `Math.max(0, ...)` floor).

#### Scenario: Charging power update arrives without a usable value

- GIVEN a vehicle is actively charging
- WHEN a Charging Power update arrives with `status = VALUE_NOT_RECEIVED`
- THEN the Charging Power channel is set to `UNDEF`
- AND the Charging Power channel is not set to `0 kW`

#### Scenario: Odometer update arrives without a usable value

- WHEN an Odometer update arrives with `status = VALUE_INVALID`
- THEN the Odometer channel is set to `UNDEF`
- AND the Odometer channel is not set to `-1 km`

#### Scenario: Charging power update arrives with a valid value

- GIVEN a vehicle sends a Charging Power update with `status = VALUE_VALID` and `double_value = 11.0`
- WHEN the binding processes the update
- THEN the Charging Power channel is set to `11 kW`

---

_Delta for the `channels` domain. On archive, both requirements are appended to
`org.openhab.binding.mercedesme/docs/specs/channels/spec.md` (new domain file, since none exists
yet)._
