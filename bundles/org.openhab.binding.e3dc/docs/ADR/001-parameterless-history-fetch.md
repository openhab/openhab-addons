# ADR 001: Parameterless history fetch

## Status

Accepted

## Context

The thing action `fetchHistory(fromYear, toYear)` and its wrapper `fetchAllHistory()` both loaded the yearly
history of earlier years. The wrapper needed the bridge parameter `historyStartYear`, which the user had to
maintain by hand. The time series are written with the policy `REPLACE`, so years that are fetched again do
not overlap or duplicate.

## Decision

There is one thing action `fetchHistory()` without parameters. It starts at the last completed year and goes
back year by year. It stops after two years in a row without data, or at the year 2000. A year in which all
values are zero counts as without data, because the device may answer a year before its installation that
way. The bridge parameter `historyStartYear` and the action `fetchAllHistory` are removed.

## Consequences

Pros:

- One action, no configuration parameter, no need to know the installation year.
- Repeated runs are harmless because of `REPLACE`.

Cons:

- A range can no longer be fetched on purpose; every run asks for all years.
- A gap of more than one year inside the history ends the fetch early.
- The stop rule depends on how the device answers years before its installation; it has to be confirmed
  against a real device.
- This is a change of the thing action API.
