# ADR 002: Serialized poll cycles and treating a defective frame as a lost connection

## Status

Accepted

## Context

A scheduled poll, a refresh triggered by a channel and a reconnect could run at the same time on different scheduler threads.
They shared the state of the bridge: dropped tags, logged answers, the history bookkeeping and the loop ratio window.
Those collections are not thread safe.
The first poll after a connect was not guarded, so an exception from decoding a defective frame ended the thread while the bridge stayed online.
The decoder did not check the magic, the length or the CRC, and accessed buffers without bounds checks.
After a defective frame the cipher chaining of the connection cannot be trusted anyway.

## Decision

- A `ReentrantLock` serializes `connectAndPoll` and `pollCycle`; all poll state is only touched while it is held.
- A refresh uses `tryLock` and is skipped when a poll is running, because that poll delivers fresh data.
- The poll job is replaced and cancelled under its own short lock, so the dispose never waits for a blocking poll.
- Every poll, including the first, runs through `pollGuarded`. An unexpected exception closes the connection and schedules a reconnect.
- `RscpCodec.decodeFrame` verifies the magic, the announced length and the CRC and reports problems as `RscpException`.
- Fixed size items that announce too few bytes, unknown types and truncated containers are rejected the same way.
- `RscpClient` checks the magic of the first decrypted block before it reads the rest of the frame.

## Alternatives

| Option | Pros | Cons |
| --- | --- | --- |
| One lock around the cycles (chosen) | Small change, the state needs no individual protection | A long poll delays a reconnect or refresh until it ends, which a refresh avoids by skipping |
| Thread safe collections only | No blocking | The history bookkeeping and the loop ratio are compound state, the races remain |
| Single threaded executor for the bridge | No locks | Own executor to manage, blocking I/O needs a dedicated thread |
| Keep the connection after a defective frame | Fewer reconnects | The cipher state is out of sync, every following answer would fail |

## Consequences

- A defective frame costs one reconnect instead of a silent loss of data.
- A device that sends a wrong CRC would now be reported as offline, which makes the problem visible instead of hiding it.
- Tests cover the rejection of a wrong magic, a wrong CRC, a truncated frame, an unknown type, a short item and a runaway container.
