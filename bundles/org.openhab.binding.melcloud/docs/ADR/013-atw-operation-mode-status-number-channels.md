# ADR-013: Convert MELCloud Home ATW Operation Status/Zone Operation Mode Channels to `Number`

## Status

Accepted

## Context

The ATW unit (`atw-unit`) exposed `operation-status`, `zone1-operation-mode` and
`zone2-operation-mode` as `String` channels carrying the API's words
(`HeatRoomTemperature`, `HotWater`, ...). `operation-status` had no option list
at all. As with the ATA channels (ADR-006), `Number` channels with a `state`
option list give a dropdown in the UI and typed, stable item values.

The MELCloud Home app defines these values as enums:

- `AtwOperationModesZone`: HeatRoomTemperature=0, HeatFlowTemperature=1,
  HeatCurve=2, CoolRoomTemperature=3, CoolFlowTemperature=4, DryFloor=5.
- `AtwOperationMode` (status): Unknown=0, Stop=1, HotWater=2, Heating=3,
  Cooling=4, FreezeStat=5, LegionellaPrevention=6.

## Decision

- `zoneOperationMode-channel` and `operationStatus-channel` become `Number`
  channel-types whose option codes are the app's own enum values. `DryFloor`
  (5) is added to the zone modes. `Unknown` (0) is not offered.
- `MelCloudHomeAtwUnitHandler` holds the word/code lookup tables. Commands are
  translated from code to word before the API call; state updates translate
  the word to its code.
- An unknown code (command) is logged at debug level and not sent. An unknown
  word (state update) is logged at debug level and published as `UNDEF`
  (ADR-009). A word the API starts sending needs a new table entry and channel
  option before it is shown.
- Schedule actions (ADR-011/012) are unchanged.

## Consequences

### Positive

- No free-form strings in the item model; consistent with the ATA channels.
- Codes are taken from the app, not invented.

### Negative

- Breaking change for users of the previous `String` items; existing items and
  rules must be switched to `Number` and the codes.
- Unmapped API words show as `UNDEF` until the mapping is extended.
- `DryFloor` as a command word is taken from the app enum and is not confirmed
  against a real API call.
