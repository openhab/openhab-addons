# ADR 005: Review checklist fixes

## Status

Accepted

## Context

A review against the openHAB review checklist and the repository guidance in `AGENTS.md` found gaps that would be raised in a pull request.
Four of them needed a decision because the checklist leaves room or the earlier code chose the opposite on purpose.

## Decision

- **Connection failure logging:** A failed connection attempt of the bridge is logged at `debug`.
  The thing status already carries the message, and the framework logs status changes.
  The same applies to the expected device failures "command rejected", "command not sent" and "history period not fetched", and to the two routine `info` messages.
  `warn` stays for input the user has to correct, such as invalid idle periods.
- **Interruption:** `RscpClient` sets the interrupt flag again when an `InterruptedIOException` that is not a `SocketTimeoutException` ends a connect, send or receive.
  A read timeout stays an expected communication failure.
  The bridge handler neither updates the status nor schedules a reconnect on an interrupted thread.
- **Semantic tags:** Read-only channel types with a clear quantity (power, energy, voltage, current, temperature, frequency) get the tags `Measurement` and the matching property.
  Writable power, energy and current limits get `Setpoint` and the matching property.
  Switches, strings and dimensionless values stay untagged until a mapping is agreed.
- **Handshake diagnosis:** The failure is told apart by the exception type only.
  The text of the exception message is no longer used for control flow.
- **Tests:** The two fixed pauses in `RscpClientTest` stay because a request that must not arrive can only be shown by waiting.
  They are documented and the class-wide timeout bounds them.
- **Labels:** Channel labels are at most 25 characters.
  The history labels `Self Consumption This ...` became `Self Consumed This ...`, and `Battery Discharge This ...` became `Discharge This ...`, so the `This Week`, `This Month` and `This Year` suffixes of the other history channels stay.

## Consequences

### Positive

- A lost connection no longer fills the default log level on every retry.
- Disposing the handler during blocking socket I/O cannot trigger a reconnect.
- Tagged channels are picked up by the semantic model without manual tagging.

### Negative

- A user who does not watch the thing status sees a failed connection only at `debug` level.
- Items linked to the renamed channel types keep working, but the UI shows the new labels.
- Untagged writable channels are still missing from the semantic model.
