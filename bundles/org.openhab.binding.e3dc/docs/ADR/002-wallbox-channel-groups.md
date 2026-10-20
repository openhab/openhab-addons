# ADR 002: Wallbox channel groups

## Status

Accepted. The group layout is superseded by [ADR 004](004-wallbox-groups-status-control-and-session.md).

## Context

The wallbox thing had one channel group `wallbox` with 23 channels: measurements, states, settings,
actions and counters in one list. The user interface shows them in alphabetical order, so related values
were far apart. The meaning of the `energy-l1` to `energy-l3` channels was also unclear. They report the
energy of the last charging session, but were easy to read as a counter or as a daily value.

## Decision

The thing has six channel groups: `status`, `power`, `control`, `settings`, `counters` and `last-charge`.

- `status`: plugged, locked, charging, key state and vehicle state of charge.
- `power`: power per phase.
- `control`: sun mode, suspended and toggle, the switches used during a charging session.
- `settings`: charge currents, number of phases and automatic phase switch, the values that are set rarely.
- `counters`: total and solar energy of the wallbox.
- `last-charge`: the phases used and the energy per phase of the last charging session.

The channel ids lose the part that the group already names, for example `power#l1` instead of
`wallbox#power-l1`. The channel type ids follow the existing rule of group id plus channel id.
The request of the wallbox data stays one container, keyed by `GROUP_WALLBOX`, so the grouping does not
change what is asked from the device.

## Consequences

Pros:

- Related channels are next to each other in the user interface and in the item model.
- The `last-charge` group says what the values are. The phase bits sit there because the wallbox
  reports them for the last session, not live: they stay on while it is unplugged.
- No additional request to the device.

Cons:

- All channel UIDs of the wallbox change. Items linked to `wallbox#...` have to be linked again.
- The binding is not released yet, so no migration is provided.
