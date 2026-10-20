# ADR 004: Wallbox groups status-control and session

## Status

Accepted. Supersedes the group layout of [ADR 002](002-wallbox-channel-groups.md).

## Context

ADR 002 split the wallbox channels into six groups. In use, the split was finer than needed:
`status`, `control` and `settings` are all about the state of the wallbox and what can be changed on it,
and `power` and `last-charge` both describe the charging session.

## Decision

The thing has three channel groups: `status-control`, `session` and `counters`.

- `status-control` (label "Status & Control"): the former `status`, `control` and `settings` groups.
  The channel ids stay the same.
- `session` (label "Session"): the former `last-charge` group plus the per phase power. The power
  channels are renamed from `l1`, `l2`, `l3` to `power-l1`, `power-l2`, `power-l3`, because the group
  no longer names what the value is.
- `counters`: unchanged.

The channel type ids stay as they are. The request of the wallbox data stays one container, so the
device is asked for the same data as before.

## Consequences

Pros:

- Fewer groups, and the switches and the values they change are in one place.
- The charging session has one group for its live power and its last values.

Cons:

- The channel UIDs of the wallbox change again, for example `control#sun-mode` becomes
  `status-control#sun-mode`. Items have to be linked again.
- The `session` group mixes a live value (power) with values of the last session. The channel
  descriptions say which is which.
- The binding is not released yet, so no migration is provided.
