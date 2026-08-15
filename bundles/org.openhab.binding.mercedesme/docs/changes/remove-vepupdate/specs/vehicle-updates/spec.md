# Delta for Vehicle Updates

## ADDED Requirements

### Requirement: Unified update normalization

The binding SHALL normalize every vehicle status push it receives, regardless of which of the two
supported wire formats delivered it, into the same internal representation before any channel state is
derived from it.

#### Scenario: Legacy push updates channels

- GIVEN an active account connection
- WHEN the backend delivers a vehicle status push in the legacy (generic attribute map) wire format
- THEN the affected vehicle's channels are updated exactly as they were before this change

#### Scenario: Typed push updates channels

- GIVEN an active account connection
- WHEN the backend delivers a vehicle status push in the typed wire format
- THEN the affected vehicle's channels are updated exactly as they were before this change

#### Scenario: Partial update behaves identically regardless of wire format

- GIVEN a vehicle thing that has already received a full update
- WHEN a partial (delta) update arrives, in either wire format, containing only a subset of attributes
- THEN only the channels corresponding to the attributes present in that update are refreshed
- AND all other channels retain their last known state

### Requirement: No REST-based vehicle-attributes fallback

The binding SHALL NOT retrieve vehicle status data via the REST `vehicleattributes` endpoint. Vehicle
status data is only obtained through the WebSocket push channel described above.

#### Scenario: Capabilities discovery is unaffected

- GIVEN a newly discovered vehicle
- WHEN the binding queries vehicle capabilities during discovery
- THEN capability discovery continues to work via the existing REST capabilities/commands endpoints,
  unaffected by the removal of the vehicle-attributes REST fallback

### Requirement: Diagnostic proto-update channel remains available

The binding MUST continue to expose the raw update payload on the diagnostic `proto-update` channel when
that channel is linked to an item, with content equivalent to what was exposed before this change.

#### Scenario: Linked diagnostic channel receives combined payload

- GIVEN the `proto-update` channel of a vehicle thing is linked to an item
- WHEN a vehicle status update (full or partial, either wire format) is processed
- THEN the item receives a JSON payload representing the update, combined with previously received
  attributes as before

---
