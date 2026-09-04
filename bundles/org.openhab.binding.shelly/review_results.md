# PR Review — shelly_presence (Presence Gen4)

Scope: `origin/main...HEAD` plus working tree, 21 files / ~1000 lines.
Profile: `binding-large.md` + `self-review.md` + `mechanical-checks.md`.

## Mechanical battery

| Check | Command | Result |
| ----- | ------- | ------ |
| 1 i18n key integrity | `comm -23 keys.used keys.def` | empty |
| 2 channel-type labels | `comm -23 ct.exp ct.def` | 0 missing |
| 3 no `@text/` in thing XML | `grep -rn '@text/' OH-INF/thing/` | empty |
| 4 indexed label mismatch | `awk -f idxchk.awk` | empty |
| 5 flag census | `grep -rno 'profile.is[A-Z]...'` | `isPresence` 7 sites vs `isFlood` 8 — parity |
| 6 new suppressions | `git diff ... \| grep @SuppressWarnings` | none |
| 7 compiler warnings | `grep -nE 'WARNING\|potential null...' build.log` | 3, all pre-existing, none in branch files |
| 8 spotless + markdownlint | `mvn spotless:apply` / full `install` | clean |
| 9 scope hygiene | `git diff ... -- README.md OH-INF/i18n/` | every hunk belongs to this PR |
| 11 branch reachability | mutation run (see below) | 3 tests turned red |

Mutation check (`mechanical-checks.md` §11): gating the presence channel on `sdata.presence != null`
instead of `profile.isPresence`, and dropping the main-zone id match in `updatePresenceStatus`,
turned exactly the intended tests red
(`presenceChannelsAreCreatedEvenWhenNoZoneReportedYet`, `statusZoneFromAnotherZoneLeavesSensorDataUnchanged`,
`statusPicksTheConfiguredMainZoneOutOfSeveralZones`). Both mutations reverted.

## Findings

| # | Sev | Area | Finding | Status |
| - | --- | ---- | ------- | ------ |
| 1 | MINOR | `Shelly2PresenceZoneAdapters`, `Shelly2ApiJsonDTO`, `ShellyPresenceJsonDTO` | The config-side zone list (`Shelly2GetConfigResult.presenceZones`, `Shelly2SettingsPresence`, `ConfigZoneFactory`) is parsed but never read by any production path — only by tests. Dead surface a reviewer will ask about. | fixed |
| 2 | MINOR | `shellyGen2_sensor.xml`, `shelly.properties` | New `presenceControl` channel-group-type duplicates the generic `control` group that every other Gen2 sensor thing-type reuses (`openhab-base.md` §15). | fixed |
| 3 | MINOR | `ShellyShellyTcpDiscoveryParticipant` | `getServiceType()` returns a bare literal while the base class defines a documented `SERVICE_TYPE` constant. | fixed |
| 4 | NIT | `shellyGen2_sensor.xml`, `shelly.properties` | Thing-type description used an em dash; the four sibling thing-type descriptions in the same file use a plain hyphen. | fixed |
| 5 | NIT | `ShellyShellyTcpDiscoveryParticipant` | The doubled `ShellyShelly` prefix reads as a typo. Left as-is — the class moves to its own PR. | open |

Fix for #1 collapsed the two generic `TypeAdapterFactory` subclasses into one concrete
`Shelly2PresenceZoneAdapter` for the status result, removing the template-method base class.

## Verified invariants (`self-review.md` §3)

- _State ownership_ — `sensorData` is one final instance per api client, never rebuilt; both the
  event path (`onNotifyEvent`) and `setPresenceSensor` write it, and the poll path publishes from it
  (`openhab-base.md` §22). No revert-on-next-poll.
- _Both directions_ — presence/objectCount arrive by NotifyEvent and by poll; both write the cache
  and both are covered by tests.
- _Instance identity_ (§23) — events and status entries are matched on the full `presencezone:<id>`
  key resolved from the device's own `main_zone` config, re-read on every profile refresh, with the
  default zone as fallback. Negative tests cover a non-main zone on both paths.
- _Failure/absence_ — null/empty zone list, zone without values, malformed `main_zone` key, and a
  config with no `main_zone`/`enable` are each covered.
- _Shared-flag sweep_ (§20) — `isPresence` only widens `isSensor`; `hasBattery`/`alwaysOn` are
  unaffected, so no shipped thing type changes behaviour.

## Known, not a finding of this PR

`ShellyStatusSensor` fields are written on the WebSocket callback thread and read on the scheduler
thread without `volatile` (`openhab-base.md` §4). That is the pre-existing pattern for every sensor
family in this DTO (flood, smoke, temperature); changing it is a binding-wide change, not a
Presence one.

## Unreviewed areas

- `ShellyThingCreatorTest` / device-table rows beyond the Presence entry.
- The Gen1 code paths, which only gained `setPresenceSensor()` throwing "Request not supported".
