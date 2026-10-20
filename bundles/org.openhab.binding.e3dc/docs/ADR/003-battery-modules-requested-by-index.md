# ADR 003: Battery modules are requested by index

## Status

Accepted

## Context

All battery module things showed the values of module 0, whatever their `moduleIndex` was. The request sent
`BAT_REQ_DCB_INFO` without a value, and the device answers that with a single module. The handler also fell
back to the first `BAT_DCB_INFO` container when none carried the configured index, which hid the problem.
rscp2mqtt sends `BAT_REQ_DCB_INFO` once per module with the module index as `uchar8` value.

## Decision

- The request of the first battery carries one `BAT_REQ_DCB_INFO` with the index value per configured
  module thing. The bare `BAT_REQ_DCB_INFO` is no longer sent.
- A module thing whose index is missing in the answer stays empty instead of reading another module.
- Discovery reads `BAT_DCB_COUNT` from the battery answer and then requests the modules `0` to count - 1.
  Without a count it requests module 0 only.
- The battery group has a channel `module-count` that shows `BAT_DCB_COUNT`.

## Consequences

Pros:

- Every module thing shows the values of its own module, as rscp2mqtt does.
- The module count is visible in openHAB and helps to check the discovery.

Cons:

- Discovery needs a second request per battery.
- Modules of further batteries are still addressed through battery 0, as before.
