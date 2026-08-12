# Delta for MELCloud Home State Sync

## ADDED Requirements

### Requirement: Push-accelerated context refresh

The binding SHOULD refresh a unit's state sooner than the next scheduled poll when the
MELCloud Home push channel signals that the unit's state may have changed.

#### Scenario: Delta for a registered unit triggers an immediate refresh

- GIVEN the bridge is `ONLINE` with the push channel connected
- AND an ATA or ATW unit has a registered listener
- WHEN a `unitStateChanged` delta arrives for that unit's ID
- THEN the binding performs a `/context` poll outside the fixed 60-second schedule
- AND the resulting state is fanned out to the unit's listener as usual

#### Scenario: Push channel unavailable falls back to fixed polling

- GIVEN the push channel is disconnected or has never connected successfully
- WHEN the fixed poll interval elapses
- THEN the binding still performs the regular `/context` poll and fan-out
- AND bridge status remains unaffected by the push channel's connection state

### Requirement: Push channel is never a state source

The binding MUST source unit state values only from `GET /context` responses, never from
the content of a push delta payload.

#### Scenario: Delta payload content is not applied directly

- GIVEN a `unitStateChanged` delta arrives with field values in its payload
- WHEN the binding processes the delta
- THEN it discards the payload content after using it as a trigger
- AND it applies only the state returned by the subsequent `/context` call

### Requirement: Real-time updates are configurable

The binding MUST allow the push channel to be disabled per bridge instance.

#### Scenario: Real-time updates disabled

- GIVEN a `home-account` bridge is configured with real-time updates disabled
- WHEN the bridge initializes
- THEN no WebSocket connection is attempted
- AND the fixed-interval poll is the only source of state refreshes

---
