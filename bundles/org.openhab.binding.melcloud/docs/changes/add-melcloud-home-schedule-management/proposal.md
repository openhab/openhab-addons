# Proposal: MELCloud Home ATW Cloud Schedule Management

## Intent

A forum user ([community.openhab.org/t/169892/14](https://community.openhab.org/t/mitsubishi-melcloudhome-binding/169892/14))
asked for a switch channel to disable zone 1 heating and hot water production based on
solar availability/electricity price, driven by the `zone1Active`/`hotWaterActive` fields
they observed inside their MELCloud Home app's schedule payload.

Investigation established that those fields are properties of individual scheduled time
slots, not a live device-level toggle — the real-time control endpoint
(`PUT /monitor/atwunit/{unitId}`) has no zone-scoped or DHW-scoped on/off command, only the
whole-unit `power` switch and the DHW-priority `forcedHotWaterMode` boost. There is no
"turn zone 1 off right now" call to expose as a simple channel.

What _does_ exist is a separate cloud-schedule API
(`/monitor/atwcloudschedule/{unitId}`) that lets a client create, update, delete, and
mass-enable/disable a unit's own scheduled entries — the same mechanism the MELCloud Home
app itself uses to build the schedule table this binding cannot currently see or touch.
This proposal scopes a feature to manage that schedule table from openHAB, as a building
block other automations (including, but not limited to, the original solar/price use case)
can compose with.

**This is explicitly not a drop-in fix for the original ask.** Schedule entries only fire
at their configured `time`; nothing here delivers instant, condition-triggered zone/DHW
control. An openHAB rule wanting genuinely reactive control should keep driving the
existing `zone1-operation-mode`/`set-temperature-zone1`/`forced-hot-water-mode` channels
directly (e.g. dropping the zone 1 setpoint as a proxy for "off"). Schedule management is
useful in its own right — e.g. temporarily suspending the vendor schedule while openHAB
takes over, or keeping the app's schedule table in sync with an openHAB-side plan — and is
scoped as such.

## Scope

In scope:

- Reading a unit's existing cloud schedule entries.
- Creating a new schedule entry.
- Updating an existing schedule entry (by `id`).
- Deleting a schedule entry (by `id`).
- Toggling the per-unit schedule master switch (`enabled`), independent of any single
  entry's own state.
- All five operations above are exposed as `ThingActions` on `atw-unit`, not as Channels
  or Items — see ADR-011 for the rationale (a variable-length list of multi-field records
  doesn't fit the single-scalar Channel/Item model this binding otherwise uses).
- ATW units only, matching the forum request. ATA schedule management
  (`/monitor/cloudschedule/{unitId}`) is a separate, not-yet-scoped feature; the two APIs
  have different (and differently-evidenced) payload shapes and should not be bundled into
  one change.

Out of scope:

- Any device-level, condition-triggered on/off control for zone 1 or hot water — no such
  API exists to build it on (see Intent above).
- ATA schedule management.
- MELCloud Home "Scenes" (`/api/scene/...`), a related but distinct feature.

## Open Questions

These need to be resolved — most by a fresh, driven traffic capture against a real ATW
unit — before `$Architect`/`$Dev` can start:

- **Payload discrepancy on `zone1Active`/`zone2Active`/`hotWaterActive`.** The forum
  user's own captured schedule payload includes these three booleans directly on each
  schedule entry. The community `andrew-blake/melcloudhome` project's ATW API reference
  does not list them in its documented Create/Update Schedule body — it does show a
  `zone1Active`/`zone2Active` pair, but on an unrelated field (`frostProtection`'s status
  shape), not on schedule entries. Either the reference documentation is incomplete for
  this endpoint, or these fields are a newer addition to the app's schedule payload that
  hasn't been captured yet. This must be re-verified directly (see the forum user's own
  payload as a starting data point) before committing to a spec'd field list — the answer
  changes what "off" actually means for a schedule entry.
- **Endpoint host/path uncertainty.** The documented paths
  (`POST/PUT /monitor/atwcloudschedule/{unitId}`, `PUT .../enabled`,
  `DELETE .../{unitId}/{scheduleId}`) come from a reference document whose own "Known
  Limitations" section flags the schedule section as weakly sourced (no HAR/VCR citation,
  unlike neighboring sections). This binding talks to the mobile BFF host
  (`mobile.bff.melcloudhome.com`) exclusively (see ADR-002); it is not confirmed these
  paths exist on that host in this exact shape, only inferred by analogy with the ATA
  twin endpoint, which _was_ confirmed on the web host but only assumed-by-analogy on
  mobile.
- **`operationModeZone1`/`operationModeZone2` integer mapping is incomplete.** Only the
  three heating modes have a claimed mapping (`0`=`HeatRoomTemperature`,
  `1`=`HeatFlowTemperature`, `2`=`HeatCurve`), and even that is marked unconfirmed by the
  source document's own "Known Limitations" note. The two cooling modes
  (`CoolRoomTemperature`, `CoolFlowTemperature`, relevant when `hasCoolingMode=true`) have
  no claimed integer value at all. A unit with cooling capability cannot be spec'd for
  schedule writes until this gap is closed.
- **Whether `POST` truly serves both create and update.** The ATW reference documents a
  single `POST` for "Create/Update," unlike the ATA twin, which was confirmed (via live
  capture) to split into `POST` for create and a differently-shaped, nested-body `PUT` for
  update. Given the ATA finding directly contradicted an earlier assumption of endpoint
  symmetry, the ATW claim of one shared `POST` should not be trusted without its own
  direct capture.
- **What actually sets a schedule entry's `enabled` field.** The ATA schedule capture
  found the per-entry `enabled` field sent as `false` in every observed create/update,
  even when the UI's visible per-entry toggle was on — that toggle apparently maps to
  `power`, not `enabled`. Whether the ATW schedule entry has the same quirk, and what (if
  anything) actually flips `enabled` to `true`, is unknown.

Given the number and nature of these gaps, `$Spec`'s recommendation is that the next step
is a driven capture session (mitmproxy or a Claude-in-Chrome-injected `fetch`/`XHR` hook
against the MELCloud Home web or mobile app, creating/editing/deleting a real ATW schedule
entry with cooling modes and the `zone1Active`-family fields specifically exercised) rather
than proceeding to `$Architect` on the current evidence.

---
