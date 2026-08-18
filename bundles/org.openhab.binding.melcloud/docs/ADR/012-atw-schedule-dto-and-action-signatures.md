# ADR-012: ATW Schedule DTO Shapes and ThingAction Signatures

## Status

Proposed — **provisional pending live capture**

This ADR was written without the live-capture pass `docs/changes/add-melcloud-home-schedule-management/proposal.md`
recommended as a prerequisite (see that document's Open Questions and `tasks.md` §1). The
human owner chose to proceed on currently available evidence rather than block on
capturing traffic first. Every field/type decision below is marked with its evidence
level; anything marked **unconfirmed** should be re-checked against a real capture before
or during `$Dev` implementation, not assumed correct because it's written down here.

## Context

ADR-011 decided schedule management is exposed via `ThingActions`
(`listSchedules`/`createSchedule`/`updateSchedule`/`deleteSchedule`/
`setSchedulesEnabled`) on `atw-unit`, not Channels. This ADR fills in the DTO field types
and action parameter/return shapes `$Dev` needs to start implementation (tasks 2.2/2.3).

Two independent sources exist, and they disagree on representation in ways that matter for
typing:

1. **The forum user's own captured payload** (a `schedule` array, evidently read from the
   MELCloud Home app/API) — `days` as lowercase day-name strings
   (`["monday", "tuesday", ...]`), `operationModeZone1` as a lowercase string
   (`"heatRoomTemperature"`) or `null`, plus `zone1Active`/`zone2Active`/`hotWaterActive`
   booleans not documented anywhere else.
1. **The community `andrew-blake/melcloudhome` project's write-endpoint documentation**
   (`POST /monitor/atwcloudschedule/{unitId}`) — `days` as day-number integers
   (`0`=Sunday..`6`=Saturday), `operationModeZone1` as an integer (`0`/`1`/`2` for the
   three heating modes; cooling unconfirmed), and no `zone1Active`/`zone2Active`/
   `hotWaterActive` fields at all.

Both are plausible without contradicting each other: the **read** shape (what the app
displays, source 1) and the **write** shape (what the API accepts on `POST`, source 2)
may simply differ, matching the pattern this binding already handles elsewhere (the
real-time control API sends/receives `operationModeZone1` as a PascalCase string, while
the schedule write API is documented to want an integer for the same logical field). This
ADR treats them as two different DTOs rather than trying to force one shape to serve both
directions.

## Decision

### Two DTOs, not one

- `MelCloudHomeAtwScheduleEntry` — the **read** shape, modeled directly on the forum
  user's captured payload. Used by `listSchedules()`'s return value.
- `MelCloudHomeAtwScheduleWriteRequest` — the **write** shape, modeled on the
  `melcloudhome` project's documented `POST` body. Used by `createSchedule`/
  `updateSchedule`'s request.

```java
public class MelCloudHomeAtwScheduleEntry {
    public String id = "";
    public List<String> days = List.of();           // lowercase day names; confirmed (source 1)
    public String time = "";                          // "HH:mm:ss"; confirmed (source 1)
    public boolean power;
    public @Nullable Double setTankWaterTemperature;
    public boolean forcedHotWaterMode;
    public boolean zone1Active;                        // unconfirmed against any API doc — only
    public boolean zone2Active;                         // seen in the forum user's own capture
    public boolean hotWaterActive;                      // (see proposal.md Open Questions)
    public @Nullable Double setTemperatureZone1;
    public @Nullable Double setTemperatureZone2;
    public @Nullable String operationModeZone1;         // lowercase string, e.g. "heatRoomTemperature";
    public @Nullable String operationModeZone2;          // casing vs. the live control API's PascalCase
                                                          // ("HeatRoomTemperature") is UNCONFIRMED — this
                                                          // class assumes lowercase as captured; a case-
                                                          // insensitive compare should be used until verified
}

public class MelCloudHomeAtwScheduleWriteRequest {
    public @Nullable String id;                          // client-generated UUID on create; existing id on update
    public List<Integer> days = List.of();               // 0=Sunday..6=Saturday — UNCONFIRMED (only in
                                                            // melcloudhome docs, no HAR citation for ATW)
    public String time = "";
    public @Nullable Boolean power;
    public @Nullable Double setTemperatureZone1;
    public @Nullable Double setTemperatureZone2;
    public @Nullable Double setTankWaterTemperature;
    public @Nullable Boolean forcedHotWaterMode;
    public @Nullable Integer operationModeZone1;          // 0/1/2 for heating modes — UNCONFIRMED, no
    public @Nullable Integer operationModeZone2;           // cooling code exists at all yet (see ADR/spec
                                                            // "cooling-mode schedule entry is rejected")
    // No zone1Active/zone2Active/hotWaterActive field: not present in the documented write body.
    // If task 1.1's capture shows they belong here too, this class needs revision.
}
```

`MelCloudHomeAtwUnit` gains a `List<MelCloudHomeAtwScheduleEntry> schedule` field,
**assumed** (unconfirmed — see below) to arrive embedded in the same `/context` response
this binding already polls, matching how the forum user's capture showed it nested inside
what appears to be unit state rather than fetched from a dedicated list endpoint. No
`melcloudhome` documentation confirms a `GET` schedule-list endpoint exists at all; the
working assumption is that `/context` already carries it and no new poll is needed for
`listSchedules()`. **If this assumption is wrong** (schedule requires a separate `GET`
call this binding doesn't currently make), `listSchedules()` needs its own network call
and possibly its own poll/cache strategy — a materially different implementation. This is
the single highest-impact unconfirmed assumption in this ADR.

### ThingAction signatures

openHAB `ThingActions` favor simple, boxed/primitive parameter types over nested objects.
`days` is passed as a comma-separated string of lowercase day names (matching the read
DTO's vocabulary, translated to the write DTO's day-number ints internally) rather than
exposing the integer encoding to rule authors:

```java
@RuleAction(label = "List Schedules", description = "List this unit's cloud schedule entries")
List<Map<String, Object>> listSchedules();

@RuleAction(label = "Create Schedule", description = "Create a new cloud schedule entry")
String createSchedule(
    @ActionInput(name = "days") String days,               // e.g. "monday,wednesday,friday"
    @ActionInput(name = "time") String time,                // "HH:mm:ss"
    @ActionInput(name = "power") Boolean power,
    @ActionInput(name = "operationModeZone1") @Nullable String operationModeZone1,
    @ActionInput(name = "setTemperatureZone1") @Nullable Double setTemperatureZone1,
    @ActionInput(name = "setTemperatureZone2") @Nullable Double setTemperatureZone2,
    @ActionInput(name = "setTankWaterTemperature") @Nullable Double setTankWaterTemperature,
    @ActionInput(name = "forcedHotWaterMode") @Nullable Boolean forcedHotWaterMode
);                                                            // returns the generated id, or "" on failure/reject

@RuleAction(label = "Update Schedule", description = "Update an existing cloud schedule entry")
boolean updateSchedule(@ActionInput(name = "id") String id, /* same optional fields as createSchedule */ ...);

@RuleAction(label = "Delete Schedule", description = "Delete a cloud schedule entry")
boolean deleteSchedule(@ActionInput(name = "id") String id);

@RuleAction(label = "Set Schedules Enabled", description = "Enable or disable all of this unit's schedules")
boolean setSchedulesEnabled(@ActionInput(name = "enabled") boolean enabled);
```

`listSchedules()` returns `List<Map<String, Object>>` (one map per
`MelCloudHomeAtwScheduleEntry`, field-named keys) rather than the DTO type directly, since
`ThingActions` return types must be usable from both Java and scripted rule languages
(DSL/JS/Python), which cannot deserialize an arbitrary binding-internal class.

### Cooling-mode rejection (per spec.md)

`createSchedule`/`updateSchedule` reject any call whose `operationModeZone1`/
`operationModeZone2` string does not map to one of the three confirmed heating words
(`"heatRoomTemperature"`, `"heatFlowTemperature"`, `"heatCurve"`, case-insensitive per the
casing caveat above), returning `""`/`false` and logging a warning, exactly as
`spec.md`'s "cooling-mode schedule entry is rejected" scenario requires.

### No new dependency (task 2.3)

Confirmed: both DTOs use the same Gson-based (de)serialization every other DTO in
`internal.home.api.dto` already uses; the write endpoints are called through
`MelCloudHomeApiClient`'s existing HTTP stack. No `pom.xml` change is needed.

## Consequences

### Positive

- DTO split matches the (admittedly unconfirmed) evidence that read and write shapes
  genuinely differ, instead of forcing one Gson class to silently misparse one direction.
- `String`-based day-of-week and mode parameters keep the `ThingAction` surface
  rule-author-friendly; the integer encodings stay an internal `$Dev` concern.

### Negative

- **This entire ADR is built on unconfirmed evidence** (see Status). Concretely at risk of
  being wrong: whether `schedule` is embedded in `/context` at all, whether
  `zone1Active`/`zone2Active`/`hotWaterActive` belong on the write DTO too, the exact
  casing of `operationModeZone1` string values, and the day-number vs. day-name mapping
  direction. `$Dev` should budget for a revision pass once task 1.1's capture (if it ever
  happens) lands, rather than treating this as final.
- `listSchedules()` returning `List<Map<String, Object>>` loses compile-time type safety
  for rule authors compared to a typed return; this is a known `ThingActions` limitation,
  not specific to this design.
