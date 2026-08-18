# Delta for ATW Cloud Schedule Management

## ADDED Requirements

### Requirement: List a unit's cloud schedule entries via ThingAction

The binding MUST expose a `listSchedules()` `ThingAction` on `atw-unit` (per ADR-011) that
returns the unit's current cloud schedule entries, so a rule can inspect what's currently
scheduled before creating, updating, or deleting an entry. This is not a Channel/Item.

#### Scenario: A rule lists the current schedule entries

- GIVEN an ATW unit thing is `ONLINE`
- WHEN a rule invokes the unit's `listSchedules()` action
- THEN it receives each entry's `id`, `days`, `time`, and configured fields

### Requirement: Create a new schedule entry via ThingAction

The binding MUST expose a `createSchedule(...)` `ThingAction` (per ADR-011) that creates a
new cloud schedule entry for an ATW unit via a client-generated identifier, mirroring
`POST /monitor/atwcloudschedule/{unitId}`.

#### Scenario: A new heating-only schedule entry is created

- GIVEN an ATW unit thing is `ONLINE`
- AND a rule invokes `createSchedule(...)` with `days`, `time`, `power=true`,
  `operationModeZone1=HeatRoomTemperature`, `setTemperatureZone1=21`
- WHEN the binding submits the create request
- THEN the request carries a fresh, client-generated UUID as `id`
- AND `operationModeZone1` is translated to its confirmed integer code (`0`) before being
  sent
- AND the action returns the generated `id`
- AND the entry appears in a subsequent `listSchedules()` call

#### Scenario: A cooling-mode schedule entry is rejected pending confirmed integer codes

- GIVEN an ATW unit thing reports `hasCoolingMode=true`
- AND a rule invokes `createSchedule(...)` with `operationModeZone1=CoolRoomTemperature` or
  `CoolFlowTemperature`
- WHEN the binding would otherwise submit the create request
- THEN the binding MUST reject the call instead of guessing an unconfirmed integer code
- AND it logs a warning identifying this as a known gap (see this change's proposal, Open
  Questions)
- AND the action returns a failure result rather than throwing

### Requirement: Update an existing schedule entry via ThingAction

The binding MUST expose an `updateSchedule(id, ...)` `ThingAction` (per ADR-011) that
updates a schedule entry previously created or discovered via `listSchedules()`.

#### Scenario: An existing entry's temperature is changed

- GIVEN a schedule entry with a known `id` exists on the unit
- WHEN a rule invokes `updateSchedule(id, setTemperatureZone1=19)`
- THEN the binding submits an update request carrying that `id`
- AND the entry's other fields are left unchanged unless also specified in the call

### Requirement: Delete a schedule entry via ThingAction

The binding MUST expose a `deleteSchedule(id)` `ThingAction` (per ADR-011), mirroring
`DELETE /monitor/atwcloudschedule/{unitId}/{scheduleId}`.

#### Scenario: An entry is removed

- GIVEN a schedule entry with a known `id` exists on the unit
- WHEN a rule invokes `deleteSchedule(id)`
- THEN the binding submits the delete request
- AND the entry no longer appears in a subsequent `listSchedules()` call

### Requirement: Toggle the per-unit schedule master switch via ThingAction

The binding MUST expose a `setSchedulesEnabled(enabled)` `ThingAction` (per ADR-011) that
enables or disables all of a unit's cloud schedules at once, independent of any individual
entry's own state, mirroring `PUT /monitor/atwcloudschedule/{unitId}/enabled`.

#### Scenario: All schedules are suspended without deleting them

- GIVEN an ATW unit has one or more schedule entries
- WHEN a rule invokes `setSchedulesEnabled(false)`
- THEN none of the unit's schedule entries fire at their configured times
- AND the entries remain present and unmodified in `listSchedules()`, ready to resume when
  `setSchedulesEnabled(true)` is called

### Requirement: Schedule writes never bypass the request pacer

Schedule create/update/delete/enable calls MUST be routed through the same shared
`MelCloudHomeRequestPacer` as every other control command (per ADR-008), not issued
directly.

#### Scenario: A burst of schedule edits is paced like any other control command

- GIVEN several schedule create/update/delete actions are triggered in quick succession
- WHEN the binding processes them
- THEN each request is scheduled through the bridge's shared request pacer
- AND no schedule request bypasses the pacer's deduplication/rate-limiting

---
