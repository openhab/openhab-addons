# ADR 004: Command queue, child thing status and channel conventions

## Status

Accepted

## Context

`handleCommand` is called on a framework thread.
A command writes to the device through a synchronized, blocking `RscpClient`, so a slow or stuck device blocked that thread and the commands of all other things.
Child things also stayed `UNKNOWN` without a reason when the bridge was missing or offline.
A review of the channel types found labels longer than the openHAB checklist allows, and static data modelled as channels.

## Decision

- Each thing handler owns an `E3DCCommandQueue`. It runs submitted tasks one by one on the shared scheduler, in the order of submission, and drops waiting tasks on dispose.
- A task that throws an unexpected exception is logged and does not stop the following tasks.
- A child thing goes `OFFLINE` with `BRIDGE_UNINITIALIZED` or `BRIDGE_OFFLINE` as long as its bridge is not connected, `UNKNOWN` once the bridge is online, and `ONLINE` after its first data.
- The power plant handler guards the held mode with its own lock object instead of `this`.
- Channel labels have at most three words and fewer than 25 characters; the week, month and year labels drop the word "This".
- `design-capacity` and `design-energy` of module and pack and `inverter-string-count` stay channels, although they are rarely changing. The energy computation uses `design-capacity`, `design-energy` varies with the voltage and the string count is read live.
- Status, warning, alarm and error codes get no `options`, because neither the RSCP documentation nor rscp2mqtt names the meaning of the values. Invented values would mislead.
- `updateStatus(ThingStatus, ThingStatusDetail, String)` stays, because the replacement exists only since openHAB 5.3. It is migrated once 5.2 is no longer supported.

## Alternatives

| Option | Pros | Cons |
| --- | --- | --- |
| Queue per handler (chosen) | Ordered, no blocking of the framework, no extra threads | A command waits behind a slow one; waiting commands are lost on dispose |
| Execute directly in `handleCommand` | Simplest | A slow device blocks the framework thread |
| One thread per command | No waiting | Unordered writes, many threads |
| Static data as properties | Matches the guideline | A value used for calculations or changing at runtime cannot be a property |
| Invented `options` for codes | Nicer UI | Wrong texts for values nobody documented |

## Consequences

- Commands no longer block the framework, but a command can take as long as the device needs.
- Pending commands are lost when a thing is disposed. This is accepted because the user sees the unchanged state.
- Users have to delete and rediscover things after the label changes; run `mvn i18n:generate-default-translations` to refresh the translation file.
- The accepted deviations are documented here, so a review does not report them again.
