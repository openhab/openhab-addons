# ADR 001: One time zone for the history periods

## Status

Accepted

## Context

The device counts its history periods (day, week, month, year) in local time.
The reference client `rscp2mqtt` derives the start of a period from the local date and `mktime`.
The binding mixed two zones: the date was taken in UTC, while the start of the period used `ZoneId.systemDefault()`.
Around midnight, and for installations whose zone differs from UTC, the request then asked for the wrong day.
The "history is due" check and the yearly time series entries had the same inconsistency.

## Decision

All day boundaries of the history use the zone of the openHAB installation, provided by the `TimeZoneProvider` service.
The factory injects the provider into the bridge handler, the thing handlers read the zone from their bridge.
The zone is read each time it is needed, so a change of the regional settings takes effect without a restart.

## Alternatives

| Option | Pros | Cons |
| --- | --- | --- |
| `TimeZoneProvider` (chosen) | Idiomatic in openHAB, follows the regional settings, testable with a fixed zone | Wrong if the installation and the plant are in different zones |
| `ZoneId.systemDefault()` everywhere | No new dependency | Depends on the JVM, ignores the openHAB regional settings, hard to test |
| UTC everywhere | Simple | Does not match the local periods of the device and its portal |
| Time zone reported by the device | Matches the device exactly | Only known after the first poll, needs a bootstrap rule and a fallback |

## Consequences

- `E3DCBridgeHandler` takes a `TimeZoneProvider`, `E3DCHandlerFactory` has an activation constructor with a `@Reference`.
- `periodHistoryRequest` and `yearHistoryRequest` take the zone as a parameter.
- Tests pin the start of a period for `Europe/Berlin` and `Australia/Sydney`.
- A plant outside the zone of the openHAB installation needs a configurable zone later.
