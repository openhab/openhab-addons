# ADR-003: MELCloud Home ATA/ATW Unit Things

## Status

Accepted

## Context

ADR-002 delivered a working account bridge (`melcloudhomeaccount`) that logs
in and keeps a Bearer access token fresh. The bridge has no channels and no
child Things yet — this ADR designs the first real device Things: Air-to-Air
(`melcloudhomeataunit`) and Air-to-Water (`melcloudhomeatwunit`) units, per
`reversed.md`'s recommended MVP scope, now scoped to include energy and
outdoor-temperature telemetry.

A public reference implementation
([`andrew-blake/melcloudhome`](https://github.com/andrew-blake/melcloudhome))
was read for ground truth on the mobile BFF's read/write contract, correcting
and extending what `reversed.md` had inferred from static analysis alone:

- **Read**: a single `GET /context` (on `mobile.bff.melcloudhome.com`)
  returns every building the user owns or has guest access to, each with its
  `airToAirUnits`/`airToWaterUnits`. Each unit's dynamic state comes back as a
  `settings` **array of `{name, value}` pairs** (PascalCase names, string
  values), not a flat JSON object — this needs its own parsing step, separate
  from the unit's own top-level camelCase fields (`id`, `givenDisplayName`,
  `rssi`, `capabilities`, ...).
- **Write**: `PUT /monitor/ataunit/{id}` / `PUT /monitor/atwunit/{id}`. The
  server expects **every** control field in the body on every call — fields
  not being changed must be sent as JSON `null`, not omitted. Gson's default
  `new Gson()` omits `null` fields when serializing, so the control DTOs
  need `new GsonBuilder().serializeNulls().create()`.
- **Telemetry**: `GET /telemetry/telemetry/energy/{id}` (energy, both unit
  types) and `GET /report/v1/trendsummary` (outdoor temperature, parsed out
  of a `datasets[].label` containing `OUTDOOR_TEMPERATURE`) — both are
  separate, per-unit calls, not part of `/context`.
- ATW's API-reported capability temperature ranges are flagged by the
  reference implementation as unreliable; it substitutes hardcoded safe
  defaults (10–30 °C zone, 40–60 °C DHW) instead of trusting
  `minSetTemperature`/`maxSetTemperature`/etc. from the server.

## Decision

### Package layout

New files under the existing `internal.home` package tree
(`api`/`api.dto`/`config`/`handler`/`discovery`), following the project's
package-by-feature rule.

### Shared reads, centralized in the bridge

`MelCloudHomeAccountHandler` polls `GET /context` on a fixed schedule
(`CONTEXT_POLL_INTERVAL_SECONDS = 60`, matching the reference
implementation's climate/sensor cadence) and fans the result out to
registered unit handlers, rather than each unit handler polling `/context`
independently — one call serves every unit under the account, which matters
given the platform's known rate-limit sensitivity (flagged as a risk in
`reversed.md`). Unit handlers register themselves with the bridge (by
`unitId`) in `initialize()`/`bridgeStatusChanged()`, mirroring the existing
`melcloudaccount`/`acdevice` bridge-child pattern already used elsewhere in
this binding, and unregister in `dispose()`.

### Energy/outdoor-temperature telemetry: per-unit, longer interval

Unlike `/context`, the telemetry endpoints are inherently per-unit, so each
unit handler schedules its **own** poll directly against the bridge's access
token, at a much longer interval (`TELEMETRY_POLL_INTERVAL_SECONDS = 1800`,
i.e. 30 minutes, matching the reference implementation) — these values
change slowly and do not need 60 s freshness.

### Control payloads: full-body PUT with explicit nulls

Each unit handler builds one control-request DTO per command, populated with
`null` for every field except the one being changed, and serializes it with
a `Gson` instance built via `new GsonBuilder().serializeNulls().create()`
(a dedicated instance, since the shared BFF client's other DTOs should keep
Gson's default null-omitting behavior). No optimistic local state update is
applied after a successful write — the next scheduled `/context` poll (at
most 60 s later) is the single source of truth, keeping the handler simpler
and avoiding drift if a write silently fails to apply server-side.

### Capability-driven, but defensive, channel ranges

`AtaCapabilities`/`AtwCapabilities` (already documented in `reversed.md`)
drive dynamic `min`/`max`/`step` via `updateConfigDescriptionURIs`-free
runtime `StateDescription` overrides is out of scope for this iteration
(state description fragments are a further iteration); for now, channel
`min`/`max`/`step` are declared statically in the thing-type XML using safe,
conservative values (ATA: 10–31 °C; ATW zone: 10–30 °C, ATW DHW: 40–60 °C),
matching the reference implementation's decision to not trust
server-reported ATW ranges. Revisit once real capability payloads across
several unit models have been observed.

### Discovery

A new `home.discovery.MelCloudHomeUnitDiscoveryService`
(`AbstractThingHandlerDiscoveryService<MelCloudHomeAccountHandler>`, the same
base class the existing `MelCloudDiscoveryService` uses) triggers its own
one-off `/context` fetch on scan and creates a `DiscoveryResult` per
ATA/ATW unit found, keyed by `unitId` as the representation property.
`MelCloudHomeAccountHandler#getServices()` registers it, following the
existing `MelCloudAccountHandler` pattern in this binding.

### New Thing types and channels

- `melcloudhomeataunit`: `power`, `operationModeString` (String: `Heat`,
  `Cool`, `Automatic`, `Dry`, `Fan` — used as-is, no numeric mapping needed
  since the Home API is string-native), `setTemperature`, `roomTemperature`,
  `fanSpeed`, `vaneHorizontal`, `vaneVertical`, `outdoorTemperature`,
  `energyConsumed`, `rssi`, `isInError`, `errorCode`, `inStandbyMode`.
- `melcloudhomeatwunit`: `power`, `setTemperatureZone1`/`roomTemperatureZone1`,
  `setTemperatureZone2`/`roomTemperatureZone2` (only shown if `hasZone2`),
  `zone1OperationMode`/`zone2OperationMode` (String:
  `HeatRoomTemperature`/`HeatFlowTemperature`/`HeatCurve`/
  `CoolRoomTemperature`/`CoolFlowTemperature`), `operationStatus` (read-only,
  what the 3-way valve is doing right now), `tankWaterTemperature`/
  `tankTargetWaterTemperature`, `forcedHotWaterMode`, `outdoorTemperature`,
  `energyConsumed`/`energyProduced`/`cop`, `rssi`, `isInError`, `errorCode`,
  `inStandbyMode`, `holidayMode`, `frostProtection`.

Channels already defined for the legacy `acdevice`/`heatpumpdevice` Things
(`power`, `setTemperature`, `roomTemperature`, `fanSpeed`, `vaneHorizontal`,
`vaneVertical`, `setTemperatureZone1`/`2`, `roomTemperatureZone1`/`2`,
`forcedHotWaterMode`, `tankWaterTemperature`, `tankTargetWaterTemperature`)
are reused as-is — channel IDs are scoped per thing-type, so reusing the
same String constant across the legacy and Home Thing types is safe and
matches the existing convention (several of these constants are already
shared between `acdevice` and `heatpumpdevice` today).

## Error Escalation Strategy

- `/context` poll failure (any `MelCloudCommException`) → logged at `debug`
  (routine/expected transient failures are common on any polling loop),
  bridge stays in its current status (auth-related status changes remain
  `MelCloudHomeAccountHandler`'s concern from ADR-002); registered unit
  handlers are simply not updated that cycle and keep their last known
  state.
- A unit handler that never receives its first `/context` update (e.g. the
  configured `unitId` doesn't exist) stays `UNKNOWN`/logs at `debug` — it
  does not go `OFFLINE` on a single miss, since a transient bridge poll
  failure must not flap every unit's status.
- Telemetry (energy/outdoor-temperature) poll failures are logged at
  `debug` and leave the corresponding channels simply unupdated — these are
  explicitly nice-to-have per the reference implementation, never a reason
  to change Thing status.
- Control command failures (`MelCloudCommException` from the `PUT` call) are
  logged at `warn` with the channel/command, and nothing else — the command
  is not retried automatically.
- Discovery failures are logged at `debug`; a failed scan simply yields no
  results.

## Consequences

### Positive

- One `/context` call serves all units, respecting the platform's
  rate-limit sensitivity regardless of how many units/Things are configured.
- Reused channel IDs keep a consistent user experience between the legacy
  and Home Thing families.
- Ground-truth-confirmed endpoints/payloads remove most of the guesswork
  ADR-001 flagged as needing a live traffic capture.

### Negative

- No dynamic `StateDescription` from `capabilities` yet — static,
  conservative ranges may reject valid setpoints for units with a wider
  range, or accept values a specific unit doesn't actually support. Revisit
  once capability payloads from multiple real units are available.
- No WebSocket push in this iteration — all state is polling-driven, so
  changes made from the official app can take up to `CONTEXT_POLL_INTERVAL_SECONDS`
  to appear.
- No scenes, schedules, or sharing — unchanged from `reversed.md`'s
  recommended phasing.
