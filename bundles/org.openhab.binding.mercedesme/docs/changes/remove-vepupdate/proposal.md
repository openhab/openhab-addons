# Proposal: Remove VEPUpdate as Internal Update Carrier

## Intent

The binding currently has two independent ways a vehicle status update reaches the code: the legacy
`VEPUpdate` WebSocket push (generic `Map<string, VehicleAttributeStatus>`) and the typed
`VehicleStatusUpdate` push (added in app version 165-1, one strongly-typed field per attribute). Both are
already converted into the same `Map<String, VehicleAttributeStatus>` shape today, but the typed path does
so by building a synthetic `VEPUpdate` protobuf object just to reuse the existing pipeline. `VEPUpdate` is
also used as the return type of a REST fallback endpoint (`RestApi.restGetVehicleAttributes`) that has no
caller in production code, and as the input type of a manual debug utility (`TestUpdate`) that is not part
of the automated test suite.

This creates avoidable indirection (typed data is downgraded to a generic map just to be re-wrapped in a
legacy protobuf type) and dead code (the REST fallback and its debug utility). The goal of this change is a
single, wire-format-agnostic internal representation for a vehicle status update, with the two protobuf
push formats treated purely as ingress encodings.

## Scope

In scope:

- Remove `RestApi.restGetVehicleAttributes()` and its backing endpoint field — confirmed unused outside of
  a manual debug utility (`TestUpdate`), never called from `AccountHandler`, `VehicleHandler`, or
  `Websocket`.
- Remove `TestUpdate` (manual debug utility, not part of the automated test suite) since it has no
  remaining purpose once the endpoint it exercises is gone; the existing TRACE-level dump of raw
  `VehicleStatusUpdate` in `AccountHandler` already documents itself as the intended way to capture live
  fixtures.
- Introduce an internal, binding-owned carrier type for a single vehicle's status update (attribute map +
  full/partial flag) that replaces `VEPUpdate` everywhere between `AccountHandler` and `VehicleHandler`,
  including the event queues on both sides.
- Both existing WebSocket push branches (`PushMessage.hasVepUpdates()` and
  `PushMessage.hasVehicleStatusUpdates()`) keep being accepted at the `AccountHandler` ingress boundary and
  are normalized into the new internal type there. Neither branch is removed at the wire level.
- Adjust `Utils.proto2Json` to take the attribute map directly instead of a `VEPUpdate`, decoupling the
  diagnostic `proto-update` channel from the legacy protobuf type.
- Correct the stale Javadoc on `Mapper.fromVehicleStatusUpdate()`, which currently claims temperature
  points, charge programs, and auxiliary warnings are excluded from the typed conversion — they are not;
  the implementation (`putTemperaturePoints`/`putChargePrograms`/`putAuxheatwarnings`) already covers all
  three. Verified against two real `VehicleStatusUpdate` captures (`vsu-eqa-1.raw`, `vsu-eqa-2.raw`).
- Update all affected unit tests (`VehicleHandlerTest`, `ProtoTest`, `ProtoConverter`) to build/consume the
  new internal type instead of `VEPUpdate`.

Out of scope (explicitly deferred):

- Removing the `PushMessage.hasVepUpdates()` WebSocket branch itself. There is no evidence available in
  this change that the Mercedes backend has stopped sending the legacy push for any account/vehicle/app
  version combination. Dropping the branch without that evidence risks silently missing updates for
  vehicles that still receive it. This can be revisited once confirmed by observing production traffic
  over time with the new unified internal type in place (logging already distinguishes which branch fired).
- Adding new channels for the 68 attributes observed in `VehicleStatusUpdate` captures that have no
  existing `MB_KEY_*` / channel mapping today (e.g. `battery_health`, `min_soc`, `weekly_profile`,
  `vehicle_health_status`). None of them regress an existing channel; exposing them is a separate,
  additive change.
- Verifying field parity of the typed `VehicleStatusUpdate` push for combustion and hybrid vehicles. The
  two available captures (`vsu-eqa-1.raw`, `vsu-eqa-2.raw`) are both BEV. This is deferred until
  combustion/hybrid captures are available — the requesting user has explicitly accepted this residual risk
  for this change.
- Any `pom.xml` change. No new dependency is introduced by this change.

## Open Questions

- Does the Mercedes backend still send legacy `VEPUpdate` pushes at all for accounts on a current app
  version, or is the typed `VehicleStatusUpdate` push now exclusive? Unknown; kept accepting defensively
  (see "Out of scope"). **Resolved by the requesting user, see Addendum below: accept the risk and remove
  it anyway.**
- Should the 68 currently-unmapped `VehicleStatusUpdate` attributes become new channels in a follow-up
  change? Product decision, not addressed here.

## Addendum: Full removal (Phase 2)

The original scope above deliberately kept the `PushMessage.hasVepUpdates()` WebSocket ingress branch,
because there was no evidence available about whether the Mercedes backend still sends it. That deferral was
a scoping decision made during this change, not something the requesting user asked for — the original
instruction was an explicit total cleanup ("Lösche alle VEPUpdates aus dem Produktiven und Testcode"). Once
pointed out, the user confirmed: remove it anyway, accepting the residual risk.

Additional scope now covered:

- Removed the `PushMessage.hasVepUpdates()` branch from `AccountHandler.handleMessage()` entirely, along
  with the `AcknowledgeVEPUpdatesByVIN` import/usage. `hasVehicleStatusUpdates()` is now the sole handled
  vehicle-update push type.
- Removed `ProtoTest`'s wire-format-level tests (`vinAndPositionAnaon`, `testProtoDecoding`,
  `testProtoBlob2Json`, `testEndChargeTime`) — all four depended on decoding real `.blob` captures of the
  now-unsupported legacy wire format. `testEndChargeTime` in particular was verifying arithmetic consistency
  of the raw fixture data itself, not calling any binding code (`Utils.getEndOfChargeTime()` was never
  invoked) — so no actual logic coverage was lost by removing it.
- Simplified `VehicleHandlerTest.testChargeProgramUpdate()`: it used to build a full `PushMessage` via
  `JsonFormat`, assert `hasVepUpdates()`, and manually reproduce `AccountHandler`'s legacy normalization step
  since that step no longer exists. `PartialUpdate-MaxSoc.json` was converted from the raw
  `PushMessage`/`VEPUpdate` JsonFormat wire shape to the same flat internal-map shape already used by every
  other `proto-json/*.json` fixture, and the test now drives it through `ProtoConverter.json2Proto()`
  directly.
- `Mapper.getChannelStateMap()`'s `bool_value` fallback branches for door/lock status (added for PR #21343
  review item #1) were deliberately **kept**, not removed: they don't reference the `VEPUpdate` type at all,
  only the generic `VehicleAttributeStatus` value-kind oneof, and `Mapper.fromVehicleStatusUpdate()` never
  emits `bool_value` for these keys — so this is now genuinely unreachable defensive code, not "VEPUpdate
  residue". Comments in `Mapper.java`/`MapperTest.java` were reworded to stop implying an active legacy
  source still exists. This is a judgment call, not something explicitly requested — flagged here in case it
  should be removed too in a future change.

Residual risk accepted by the user: if the Mercedes backend still sends a legacy `VEPUpdate` push for any
account/vehicle/app-version combination, that push is now silently ignored by `AccountHandler` (falls
through to the final `else` branch, logged at `TRACE` as "not handled") — no acknowledgment is sent, so the
server will keep retrying it. This cannot be verified from this environment; a human should monitor
`AccountHandler`'s DEBUG-level `handling message type {}` log after deploying this change.

Orphaned resources from Phase 1/2 have since been deleted by the requesting user directly on the mounted
project folder (`TestUpdate.java`, `src/test/resources/proto-blob/`, the stray `__probe2.tmp` probe file) —
the sandbox file-delete restriction only affected this environment, not the human's own filesystem access.
`mvn clean install` (including tests) has since been run successfully by the requesting user.

## Addendum: TRACE dump anonymization (Phase 3)

Following removal of `VEPUpdate`, the plan for closing the "field parity for combustion/hybrid vehicles"
open item (see above) is to capture real `VehicleStatusUpdate` traffic via `AccountHandler`'s existing
TRACE-level dump and turn it into new `proto-json/*.json` test fixtures. That dump logged the raw
`VehicleStatusUpdate` via `toString()` (protobuf TextFormat), which includes `fin_or_vin` and the GPS
position (`position_lat`/`position_long`) in cleartext — both personal data, and both would otherwise need
manual redaction before a capture could be pasted into a PR fixture.

Added `AccountHandler.anonymizeForTrace()`: builds a copy of the `VehicleStatusUpdate` via `toBuilder()`
with `fin_or_vin` replaced by a fixed placeholder and, if present, `position_lat`/`position_long` replaced
by the same dummy coordinates (`1.23`/`4.56`) already used by `Utils.proto2Json()` for the diagnostic
`proto-update` channel — kept consistent with that existing convention rather than inventing a new one. Only
these two fields are touched; every other attribute is operational vehicle status, not personal data, so it
is left untouched to keep captures useful for fixture-building.

Not changed: `Utils.proto2Json()` itself (the diagnostic channel was already anonymizing position, and never
included the VIN in its output since it only receives the per-VIN attribute map, not the full
`VehicleStatusUpdate`).

Not run: `mvn clean install` for this specific addendum — same environment limitation as before (no `mvn`,
no `javac`, JRE-only sandbox). Verified by manual review and cross-checking the generated protobuf API in
`src/3rdparty/java/com/daimler/mbcarkit/proto/VehicleEvents.java` (`toBuilder()`, `hasPositionLat()`/
`hasPositionLong()`, both `setPositionLat`/`setPositionLong` overloads, `DoubleAttribute.newBuilder(prototype)`)
instead of compiling. A human must run `mvn clean install` before merging.

---
