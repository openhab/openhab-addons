# ADR-006: Convert MELCloud Home ATA Operation Mode/Fan Speed/Vane Channels to `Number`

## Status

Accepted

## Context

The MELCloud Home ATA unit (`ata-unit`) exposed `operationModeString`,
`fanSpeed`, `vaneHorizontal`, and `vaneVertical` as `String` channels:

- `operationModeString` used word values (`Heat`/`Cool`/`Automatic`/`Dry`/`Fan`),
  backed by a dedicated `ataOperationModeString-channel` channel-type.
- `fanSpeed`/`vaneHorizontal`/`vaneVertical` reused the legacy A.C. Device's
  shared, numeric-string channel-types (`fanSpeed-channel`/
  `vaneHorizontal-channel`/`vaneVertical-channel`), even though the MELCloud
  Home API actually returns/accepts word values (`Auto`/`One`/`Two`/`Three`/
  `Four`/`Five` for fan speed and vane vertical, plus `Swing` for vertical;
  `Left`/`LeftCentre`/`Centre`/`RightCentre`/`Right`/`Swing` for horizontal,
  per `MelCloudHomeAtaUnit`'s own normalization) — a pre-existing mismatch
  between the channel-type's declared numeric options and the words actually
  sent to the channel.

`Number` channels with a `state` option list give a cleaner UI (dropdown of
named values, consistent typing with the rest of the binding) and let the
four channels round-trip through simple integer codes instead of free-form
strings. Note that the legacy MELCloud binding's `operationMode`/`fanSpeed`/
`vaneHorizontal`/`vaneVertical` channels (`acdevice`/`heatpumpdevice`) are
themselves still `String`-typed, just with numeric-_looking_ option keys
(`1` = Heat, `2` = Dry, `3` = Cool, `7` = Fan, `8` = Auto, etc.) — this ADR
does not touch those, and does not reuse their channel-types, since a
genuine `Number` item-type is desired for the ATA unit specifically.

This change is scoped to the MELCloud Home ATA unit only. The legacy binding's
`acdevice`/`heatpumpdevice` channels and their shared channel-types are left
untouched, so existing legacy installations are unaffected.

## Decision

- `operationModeString` is replaced by an `operationMode` channel on
  `ata-unit`, bound to a new, ATA-specific `ataOperationMode-channel`
  channel-type (`Number`, options `1`=Heat, `2`=Dry, `3`=Cool, `7`=Fan,
  `8`=Auto — the same codes the legacy A.C. Device's `operationMode-channel`
  uses as option keys, for consistency, even though that legacy channel-type
  itself remains `String`). `MelCloudHomeAtaUnitHandler` maps the API's word
  value onto these codes (`Automatic` → `8`, etc.) in both directions. The
  dedicated `ataOperationModeString-channel` channel-type is removed from
  `channels.xml`.
- `operationMode`/`fanSpeed`/`vaneHorizontal`/`vaneVertical` keep their
  channel IDs but are rebound to four new, ATA-specific `Number`
  channel-types (`ataOperationMode-channel`, `ataFanSpeed-channel`,
  `ataVaneHorizontal-channel`, `ataVaneVertical-channel`), instead of the
  shared legacy channel-types:
  - Fan speed and vertical vane codes mirror the numeric encoding
    `MelCloudHomeAtaUnit` already normalizes from the raw API (`0`=Auto,
    `1`=One, `2`=Two, `3`=Three, `4`=Four, `5`=Five, plus `7`=Swing for
    vertical only) — this mapping was already established in code, just not
    previously exposed as the channel's own type.
  - Horizontal codes have no equivalent numeric encoding in the underlying
    API (it is always word-based); the codes `0`=Auto, `1`=Left,
    `2`=Left Centre, `3`=Centre, `4`=Right Centre, `5`=Right, `12`=Swing are
    invented to mirror the legacy binding's `vaneHorizontal-channel`
    numbering for consistency.
- `MelCloudHomeAtaUnitHandler` gained bidirectional word/code lookup tables
  for all four channels, plus a `toInt(Command)` helper (parallel to the
  existing `toCelsius(Command)` helper) used in `handleCommand`. Unknown
  codes (command) or unknown words (state update) are logged at debug level
  and skipped rather than sent to the API or crashing the update.
- The now-unused `CHANNEL_OPERATION_MODE_STRING` constant is removed from
  `MelCloudBindingConstants`; the existing `CHANNEL_OPERATION_MODE`,
  `CHANNEL_FAN_SPEED`, `CHANNEL_VANE_HORIZONTAL`, and `CHANNEL_VANE_VERTICAL`
  constants are reused as-is (channel IDs are scoped per Thing type, so
  sharing the ID string with the legacy binding is not a conflict).
- `src/main/resources/OH-INF/i18n/melcloud.properties` is regenerated via
  `mvn i18n:generate-default-translations` as part of the next `$Release`
  pass; it is not hand-edited for this change.

## Consequences

### Positive

- Consistent `Number` typing for mode/fan-speed/vane channels across both
  Thing families, with dropdown-friendly option lists in the UI.
- Fan speed and vertical vane codes are no longer invented — they reuse the
  mapping the code already had to derive from the raw API, removing one
  layer of indirection.
- The legacy binding (`acdevice`/`heatpumpdevice`) is completely unaffected;
  no migration is needed for existing MELCloud (non-Home) installations.

### Negative

- Breaking change for existing MELCloud Home `ata-unit` installations: any
  `.items` file binding a `String` item to `operationModeString`, `fanSpeed`,
  `vaneHorizontal`, or `vaneVertical` must be updated to a `Number` item, and
  `operationModeString` must be renamed to `operationMode`.
- The horizontal vane codes are an invented convention (no numeric encoding
  exists in the underlying API), so they cannot be cross-checked against a
  wire-format numeric value the way fan speed, vertical vane, and operation
  mode can.
