# Tasks: MELCloud Home ATW Cloud Schedule Management

## 1. Protocol discovery (blocking — see proposal.md Open Questions)

- [ ] 1.1 Driven capture (mitmproxy or Claude-in-Chrome-injected `fetch`/`XHR` hook)
  against a real ATW unit: create, edit, and delete a schedule entry, specifically
  exercising a cooling-mode entry (`CoolRoomTemperature`/`CoolFlowTemperature`) and an
  entry with the `zone1Active`/`zone2Active`/`hotWaterActive` fields the forum user
  observed, to resolve whether they belong on schedule entries at all
- [ ] 1.2 Confirm whether the capture in 1.1 was reachable on the mobile BFF host
  (`mobile.bff.melcloudhome.com`, the only host this binding talks to per ADR-002) or only
  on the legacy web host — if only the latter, escalate to `$Architect` on whether this
  change is viable at all without a second host client
- [ ] 1.3 Confirm whether `POST /monitor/atwcloudschedule/{unitId}` truly serves both
  create and update, or whether ATW splits them the way the ATA twin does (flat-body
  `POST` for create, nested `{id, schedule: {...}}` `PUT` for update)
- [ ] 1.4 Confirm what sets a schedule entry's `enabled` field, and whether it interacts
  with the per-unit master switch (`PUT .../enabled`)
- [ ] 1.5 Document all findings from 1.1-1.4, updating this change's `proposal.md` and the
  `spec.md` scenarios to match confirmed reality before `$Architect` starts design

## 2. Architecture ($Architect, after 1.5)

- [x] 2.1 Decide how schedule entries are surfaced to openHAB — **resolved: `ThingActions`
  on `atw-unit`, not Channels/Items** (`listSchedules`/`createSchedule`/`updateSchedule`/
  `deleteSchedule`/`setSchedulesEnabled`), see ADR-011. Still open: finalize exact action
  parameter/return types once 1.1-1.4 confirm the real payload shape.
- [x] 2.2 Decide the DTO shape for `MelCloudHomeAtwScheduleEntry` and control request(s) —
  **resolved provisionally: see ADR-012.** Done without task 1.1's live capture (human
  owner's explicit choice); ADR-012's Status section flags every unconfirmed field.
  `$Dev` must re-verify against real traffic before/during implementation, not treat this
  as settled.
- [x] 2.3 Confirm no new dependency is needed — confirmed in ADR-012: reuses the existing
  Gson/HTTP client stack, per ADR-001/ADR-002

## 3. Implementation ($Dev, after 2.x)

- [x] 3.1 Add DTOs for schedule entries and control requests — `MelCloudHomeAtwScheduleEntry`
  (read) and `MelCloudHomeAtwScheduleWriteRequest` (write), per ADR-012
- [x] 3.2 Add API client methods for create/update/delete/enable, routed through
  `MelCloudHomeRequestPacer` — `createOrUpdateAtwSchedule`/`deleteAtwSchedule`/
  `setAtwScheduleEnabled` in `MelCloudHomeApiClient`; the pacer gained a new
  `scheduleBlocking(PacedCall<T>)` overload since `ThingActions` need a synchronous result,
  unlike `handleCommand`'s existing fire-and-forget `schedule(Runnable)`
- [x] 3.3 Add a `MelCloudHomeAtwScheduleActions` `ThingActions` class implementing
  `listSchedules`/`createSchedule`/`updateSchedule`/`deleteSchedule`/
  `setSchedulesEnabled`, registered on `MelCloudHomeAtwUnitHandler` per ADR-011 (no
  Channel/Item wiring)
- [x] 3.4 Reject cooling-mode schedule writes with a clear log warning until 1.1 confirms
  their integer codes (spec scenario "cooling-mode schedule entry is rejected")

Done without task 1's live capture (human owner's explicit choice, consistent with 2.2).
**Not compiled or run against a real Maven build** — this bundle checkout has no reactor
parent/openHAB core artifacts available in the environment this was written in
(`mvn -v` found no `mvn` at all). `$Release` could not run `spotless:apply` or
`clean install` for the same reason; see §5.

## 4. Tests ($QA/$Dev)

- [x] 4.1 Create/update/delete each submit the expected request body and route through the
  request pacer — `MelCloudHomeApiClientTest` (wire-level: URL/body) and
  `MelCloudHomeAtwUnitHandlerTest` (field mapping) cover this; `MelCloudHomeRequestPacerTest`
  covers `scheduleBlocking` itself (success, exception propagation, blocking-until-paced)
- [x] 4.2 A cooling-mode create is rejected without a network call —
  `whenCreateScheduleIsCalledWithCoolingModeThenItIsRejectedWithoutCallingApiClient`
- [x] 4.3 Master enable/disable submits the expected request and does not touch individual
  entries — `whenSetSchedulesEnabledIsCalledThenApiClientIsCalledWithValue`
- [x] 4.4 (added) An unrecognized day name is rejected without a network call
- [x] 4.5 (added) With no bridge connected, actions return their failure sentinel without
  calling the API client

**Not run** — no Maven/JUnit runner available in this environment; written and reviewed
against the existing test suite's conventions, not executed. `$Release`/CI must run
`mvn test` before this is trusted.

---
