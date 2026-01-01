# Core Handler Architecture

This page documents the main handler structure and dependency injection in the
Jellyfin binding.

```mermaid
classDiagram
    %% Core handler relationships
    HandlerFactory --> ServerHandler : creates
    HandlerFactory --> ClientHandler : creates
    HandlerFactory --> TaskManager : creates
    HandlerFactory --> ApiClientFactory : uses

    ServerHandler --> TaskManagerInterface : uses (injected)
    ServerHandler --> ApiClient : uses
    ServerHandler --> ErrorEventBus : owns
    ServerHandler --> UserManager : uses
    ServerHandler --> ConfigurationManager : uses
    ServerHandler --> ServerStateManager : uses
    ServerHandler --> WebSocketTask : manages lifecycle
    ServerHandler --> ServerSyncTask : fallback polling
    ServerHandler --> ClientHandler : notifies via\nupdateStateFromSession()

    ClientHandler --> ServerHandler : delegates commands to
    ClientHandler --> ClientStateUpdater : delegates\nstate calculation to
    ClientHandler ..> SessionInfoDto : receives updates

    %% Key interfaces
    TaskManagerInterface <|.. TaskManager
    ErrorEventListener <|.. ServerHandler
    BaseBridgeHandler <|-- ServerHandler
    BaseThingHandler <|-- ClientHandler

    class HandlerFactory {
        +createHandler(Thing) ThingHandler
    }

    class ServerHandler {
        -TaskManagerInterface taskManager
        -Map~String, SessionInfoDto~ clients
        +sendPlayStateCommand(String, PlaystateCommand, Long)
        +playItem(String, PlayCommand, String, Long)
        +searchItem(String, String, BaseItemKind)
        +getClients() Map
    }

    class ClientHandler {
        -SessionInfoDto currentSession
        +updateStateFromSession(SessionInfoDto)
    }

    class ClientStateUpdater {
        +calculateChannelStates(SessionInfoDto$) Map~String, State~
    }

    class TaskManagerInterface {
        <<interface>>
        +initializeTasks(...) Map
        +processStateChange(...)
        +stopAllTasks(Map)
    }

    class SessionInfoDto {
        <<record>>
        +getId() String
        +getDeviceId() String
        +getUserId() UUID
        +getNowPlayingItem() BaseItemDto
        +getPlayState() PlayerStateInfo
    }

    %% Color scheme for external libraries
    classDef openhabCore fill:#ffb366,stroke:#cc8533,color:#000
    classDef openhabApi fill:#99dd99,stroke:#66bb66,color:#000
    class BaseThingHandler openhabCore
    class BaseBridgeHandler openhabCore
    class SessionInfoDto openhabApi
```

## Summary

The core handler architecture separates the creation and orchestration of
handlers, task management, and API client instantiation.

### Handler Relationships

- **ServerHandler** (Bridge): Manages server connection, authenticates, and maintains
  a list of active client sessions.
  Acts as a bridge handler for client devices.
  Manages WebSocket connection lifecycle and automatic fallback to polling.
- **ClientHandler** (Thing): Represents individual Jellyfin client devices.
  Receives session state updates from the parent ServerHandler and delegates commands
  back to the server.

### Communication Flow

#### Real-Time Updates (WebSocket)

1. ServerHandler starts WebSocketTask when `useWebSocket=true` (default)
2. WebSocket receives real-time SessionsMessage events from Jellyfin server
3. SessionsMessageHandler parses messages and publishes to SessionEventBus
4. ClientHandler subscribes to SessionEventBus and receives updates
5. ClientHandler calls `updateStateFromSession()` to update channels

#### Polling Updates (Fallback)

1. ServerHandler starts ServerSyncTask when WebSocket is disabled or fails
2. ServerSyncTask polls the Jellyfin server for active sessions (every `refreshSeconds`)
3. For each session, ServerHandler calls `updateStateFromSession()` on the corresponding ClientHandler
4. ClientHandler updates its channels with the session information

#### Command Flow

When a command is sent to a ClientHandler channel, it delegates to ServerHandler
methods like `sendPlayStateCommand()` or `playItem()`.

### WebSocket Lifecycle Management

**Initialization:**

- ServerHandler creates WebSocketTask with fallback callback: `() -> handleWebSocketFallback()`
- TaskManager starts WebSocketTask when state transitions to `CONNECTED`

**Reconnection:**

- Exponential backoff on connection failures: 1→2→4→8→16→32→60s (max 10 attempts)
- Automatic state reset on successful connection

**Fallback:**

- After max retries, WebSocketTask invokes fallback callback
- `handleWebSocketFallback()` stops WebSocket and starts ServerSyncTask for polling
- Polling continues until binding restart

See the [architecture overview](../architecture.md) for context.
