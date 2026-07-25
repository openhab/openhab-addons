# ADR-004: Use the `system.signal-strength` System Channel for `rssi`

## Status

Accepted

## Context

ADR-003 introduced the `rssi` channel on `melcloudhomeataunit`/`melcloudhomeatwunit`,
backed by a custom `rssi-channel` channel-type (`Number`, read-only, raw dBm
value, tagged `Measurement`/`RSSI`).

openHAB core already ships a standard `system.signal-strength` channel type
for exactly this purpose. It is a `Number` channel with a well-known
`StateDescription` of five discrete quality levels (`0`..`4`, no
signal..excellent) that UIs render as a signal-bars icon. Reusing it instead
of a custom channel-type is standard practice across bindings and avoids
maintaining a bespoke label/description/tag set.

## Decision

- Replace the custom `rssi-channel` channel-type with a direct reference to
  `system.signal-strength` in both `melCloudHomeAtaUnit.xml` and
  `melCloudHomeAtwUnit.xml` (`<channel id="rssi" typeId="system.signal-strength"/>`).
  The custom `rssi-channel` definition is removed from `channels.xml`.
- `system.signal-strength` expects a `0`..`4` quality value, not a raw dBm
  reading, so `MelCloudHomeAtaUnitHandler`/`MelCloudHomeAtwUnitHandler` each
  gained a private `mapRssiToSignalStrength(int rssi)` helper that buckets
  the MELCloud Home API's raw dBm value:
  - `>= -60 dBm` → `4` (excellent)
  - `>= -70 dBm` → `3` (good)
  - `>= -80 dBm` → `2` (fair)
  - `>= -90 dBm` → `1` (poor)
  - below that → `0` (no signal)

  These thresholds are the common Wi-Fi RSSI convention used by other
  openHAB bindings; no unit-specific calibration data was available to
  refine them.
- The channel ID (`rssi`) and the `CHANNEL_RSSI` constant are unchanged —
  only the channel-type reference and the value written to it changed.

## Consequences

### Positive

- Standard signal-bars UI widget "for free", consistent with other bindings.
- No custom label/description/i18n upkeep for this channel going forward.

### Negative

- The raw dBm value is no longer exposed on this channel; a user who wants
  the precise dBm reading (rather than a 0-4 quality bucket) has no channel
  for that anymore.
- The dBm-to-quality thresholds are generic Wi-Fi conventions, not verified
  against real MELCloud Home hardware behavior; revisit if they prove too
  coarse or miscalibrated once more field data is available.
