# ClientHandler Refactoring — Class Diagram

**Feature:** `refactor-clienthandler`
**Date:** 2026-03-02
**Status:** Archived

---

## Overview

`ClientHandler` was refactored from a 1023-line God Class into a lean 392-line coordinator
that delegates to four single-responsibility utility classes. `ClientDiscoveryService` gained
a dedicated `DeviceIdSanitizer` helper.

---

## Class Diagram

```mermaid
classDiagram
    direction TB

    %% ─────────────────────────────────────────────
    %% Core handler layer
    %% ─────────────────────────────────────────────
    class ClientHandler {
        -SessionTimeoutMonitor timeoutMonitor
        -PlaybackExtrapolator extrapolator
        -ClientCommandRouter commandRouter
        -SessionInfoDto currentSession
        -String deviceId
        +initialize()
        +dispose()
        +handleCommand(ChannelUID, Command)
        +onSessionUpdate(SessionInfoDto)
        +bridgeStatusChanged(ThingStatusInfo)
        +updateStateFromSession(SessionInfoDto)
        -updateClientState()
        -onSessionTimeout()
    }

    class ServerHandler {
        <<bridge>>
    }

    %% ─────────────────────────────────────────────
    %% Session event infrastructure
    %% ─────────────────────────────────────────────
    class SessionEventListener {
        <<interface>>
        +onSessionUpdate(SessionInfoDto)
    }

    class SessionEventBus {
        +subscribe(deviceId, listener)
        +unsubscribe(deviceId, listener)
        +publishSessionUpdate(deviceId, session)
        +clear()
    }

    %% ─────────────────────────────────────────────
    %% Extracted utility classes
    %% ─────────────────────────────────────────────
    class ClientCommandRouter {
        -ServerHandler serverHandler
        -Supplier~SessionInfoDto~ sessionSupplier
        -ScheduledFuture~?~ delayedCommand
        +route(ChannelUID, Command)
        +cancelDelayedCommand()
        +dispose()
    }

    class PlaybackExtrapolator {
        -long extrapolatedPositionTicks
        -ScheduledFuture~?~ task
        +start(SessionInfoDto)
        +stop()
        +dispose()
        +getExtrapolatedPositionTicks() long
    }

    class SessionTimeoutMonitor {
        -long timeoutMs
        -long lastActivityTimestamp
        +start(scheduler, deviceId, hasSession, onTimeout)
        +stop()
        +recordActivity()
        +resetActivity()
        +isTimedOut() boolean
    }

    class TickConverter {
        <<utility>>
        +TICKS_PER_SECOND$ long
        +secondsToTicks(long)$ long
        +ticksToSeconds(long)$ long
        +percentToTicks(runTimeTicks, percent)$ long
        +ticksToPercent(ticks, runTimeTicks)$ int
    }

    %% ─────────────────────────────────────────────
    %% Discovery layer
    %% ─────────────────────────────────────────────
    class ClientDiscoveryService {
        +initialize()
        +discoverClients()
    }

    class DeviceIdSanitizer {
        <<utility>>
        +sanitize(deviceId)$ String
    }

    %% ─────────────────────────────────────────────
    %% Relationships
    %% ─────────────────────────────────────────────

    %% ClientHandler implements SessionEventListener (receives updates from SessionEventBus)
    ClientHandler ..|> SessionEventListener

    %% SessionEventBus routes events to all registered listeners
    SessionEventBus --> SessionEventListener : notifies

    %% ServerHandler publishes to the event bus
    ServerHandler --> SessionEventBus : publishes to

    %% ClientHandler delegates to extracted utilities
    ClientHandler *-- SessionTimeoutMonitor : owns
    ClientHandler *-- PlaybackExtrapolator : owns
    ClientHandler *-- ClientCommandRouter : owns

    %% ClientHandler obtains ServerHandler via bridge
    ClientHandler --> ServerHandler : getBridge()

    %% ClientCommandRouter uses ServerHandler to send API commands
    ClientCommandRouter --> ServerHandler : sends commands via

    %% Both time-math users depend on TickConverter
    PlaybackExtrapolator ..> TickConverter : uses
    ClientCommandRouter ..> TickConverter : uses

    %% ClientDiscoveryService delegates device ID normalisation
    ClientDiscoveryService ..> DeviceIdSanitizer : uses
```

---

## Key Design Changes

| Before                                           | After                                                    |
| ------------------------------------------------ | -------------------------------------------------------- |
| `ClientHandler` — 1023 lines, all logic inline   | `ClientHandler` — 392 lines, coordination only           |
| Tick math (`10_000_000L`) scattered in handler   | `TickConverter` — static utility, single source of truth |
| Position extrapolation loop in handler           | `PlaybackExtrapolator` — self-contained, testable        |
| Session timeout logic in handler                 | `SessionTimeoutMonitor` — configurable, reusable         |
| 14-channel `if/else` dispatch in handler         | `ClientCommandRouter` — dedicated router                 |
| `sanitizeDeviceId()` in `ClientDiscoveryService` | `DeviceIdSanitizer` — static utility, directly tested    |

---

## Package Structure

```
internal/
├── handler/
│   └── ClientHandler.java            (coordinator, 392 lines)
├── discovery/
│   └── ClientDiscoveryService.java   (uses DeviceIdSanitizer)
└── util/
    ├── command/
    │   └── ClientCommandRouter.java  (14-channel dispatch)
    ├── discovery/
    │   └── DeviceIdSanitizer.java    (static sanitise)
    ├── extrapolation/
    │   └── PlaybackExtrapolator.java (per-second tick counter)
    ├── tick/
    │   └── TickConverter.java        (static tick math)
    └── timeout/
        └── SessionTimeoutMonitor.java (activity + timeout)
```
