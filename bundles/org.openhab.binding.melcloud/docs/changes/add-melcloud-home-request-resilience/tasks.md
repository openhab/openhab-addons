# Tasks: MELCloud Home Request Pacing and Command Deduplication

## 1. Request pacer

- [x] 1.1 Add a shared, per-bridge minimum-interval pacer wrapping every
  `MelCloudHomeApiClient` call issued through `MelCloudHomeAccountHandler` and the unit
  handlers (poll and control alike) — `MelCloudHomeRequestPacer`, non-blocking: runs
  synchronously when no wait is needed, otherwise hands off to the bridge's `scheduler`
- [x] 1.2 Make the interval a named constant so it can be tuned later without an API
  change — `DEFAULT_MIN_REQUEST_INTERVAL_MILLIS = 500`

## 2. Command deduplication

- [x] 2.1 Before sending an ATA or ATW control `PUT`, compare each field of the request
  against the last known unit state from the most recent `/context` fan-out — collapses to
  a single-field comparison per case, since `handleCommand` sets exactly one field per call
- [x] 2.2 Skip the `PUT` and log at `debug` if every field already matches the last known
  state
- [x] 2.3 Always send the `PUT` if no prior known state exists yet for the unit

## 3. Tests

- [x] 3.1 Two commands issued within the pacing interval are spaced apart, not dropped —
  `MelCloudHomeRequestPacerTest`
- [x] 3.2 A command matching the last known state is skipped (dedup) — ATA/ATW handler
  tests
- [x] 3.3 A command differing from the last known state is always sent — ATA/ATW handler
  tests
- [x] 3.4 A command is sent when no prior known state exists for the unit — ATA/ATW handler
  tests

## 4. Build verification

- [ ] 4.1 Run `mvn spotless:apply` and `mvn clean install` locally (this session had no
  Maven/Java 21/dependency cache available to verify the build itself — see
  `$Review`/`$Release` before merging)

---
