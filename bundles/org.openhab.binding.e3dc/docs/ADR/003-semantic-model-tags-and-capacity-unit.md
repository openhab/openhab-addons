# ADR 003: Semantic model tags and the unit of battery capacities

## Status

Accepted

## Context

Most channel types carried no semantic tags, the five equipment thing types declared no equipment tag, and the
battery capacities were plain numbers with the unit only in the state pattern. This leaves the channels out of the
semantic model and makes unit conversion impossible. Some descriptions also explained functionality, which the
content rules for thing type XML forbid.

## Decision

1. Every equipment thing type declares an equipment tag: `Inverter` (power plant), `Battery` (battery and battery
   module), `EVSE` (wallbox) and `ElectricMeter` (power meter).
1. Every channel type declares a point tag, and a property tag where the semantic model has a fitting one:
   - Read-only values are `Measurement`, state flags and codes are `Status`.
   - Settable switches are `Switch`, settable numbers and texts are `Setpoint` or `Control`.
   - `Switch` is a sub-tag of `Control`, so the more specific tag is used alone.
   - Charge levels use `StateOfCharge`, modes use `Mode`, times use `Duration`, timestamps use `Timestamp`.
   - Feature toggles carry `Enabled` next to `Switch` or `Status`.
   - The charging and discharging blocks carry no lock tag: `LockState` describes the lock state of a door or
     window, not a software blocked charge, so these channels stay at `Status`.
1. The capacities in ampere hours use the item type `Number:ElectricCharge` with the state pattern
   `%.1f %unit%`, and the handler publishes them in `Units.AMPERE_HOUR`.
1. Descriptions of channel types are one short sentence without functionality or references.

## Consequences

- Positive: channels take part in the semantic model, capacities convert between units.
- Negative: existing things must be deleted and discovered again, because the channel types changed.
- Negative: the model has no tag for ratios, counts or capacities, so these channels carry `Measurement` only.

## Alternatives considered

- Keep plain numbers for capacities: no change for users, but no unit conversion.
- Add channel categories: not required by the review checklist and not done here.
