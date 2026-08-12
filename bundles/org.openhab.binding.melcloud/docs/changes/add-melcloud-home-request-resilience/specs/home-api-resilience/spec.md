# Delta for MELCloud Home API Resilience

## ADDED Requirements

### Requirement: Minimum request spacing

The binding MUST enforce a minimum interval between outbound MELCloud Home API calls
issued for the same bridge, whether from the fixed `/context` poll, a push-triggered
refresh, or a unit control command.

#### Scenario: Rapid successive commands are spaced, not dropped

- GIVEN a unit received a control command less than the minimum interval ago
- WHEN another control command is issued for the same bridge
- THEN the binding delays the new API call until the minimum interval has elapsed
- AND the command is still sent, unchanged

### Requirement: Control command deduplication

The binding MUST skip an outbound control call when every field of the requested change
already matches the last known state of the unit.

#### Scenario: Redundant command is skipped

- GIVEN the last known state of a unit has power `ON`
- WHEN a command requesting power `ON` is issued for that unit
- THEN the binding does not send a control `PUT` for that command
- AND the binding logs the skip at `debug` level

#### Scenario: Differing command is always sent

- GIVEN the last known state of a unit has power `ON`
- WHEN a command requesting power `OFF` is issued for that unit
- THEN the binding sends a control `PUT` reflecting the requested change

#### Scenario: First command with no known state is always sent

- GIVEN no `/context` poll has completed yet for a unit since binding startup
- WHEN a command is issued for that unit
- THEN the binding sends a control `PUT` for that command regardless of its value

---
