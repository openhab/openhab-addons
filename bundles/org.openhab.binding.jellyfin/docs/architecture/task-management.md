# Task Management Architecture

This page documents the task management system in the Jellyfin binding.

## Table of Contents

- [Task Management Architecture](#task-management-architecture)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
  - [Architecture Diagram](#architecture-diagram)
  - [Tasks by Server State](#tasks-by-server-state)
    - [State-Task Mapping](#state-task-mapping)
    - [Task Lifecycle](#task-lifecycle)
    - [ServerSyncTask & Discovery Task Processing Flow](#serversynctask-and-discovery-task-processing-flow)
  - [Client Discovery Integration](#client-discovery-integration)
    - [Async Service Injection Pattern](#async-service-injection-pattern)
    - [Deferred Task Creation Flow](#deferred-task-creation-flow)
    - [Key Design Decisions](#key-design-decisions)
  - [Summary](#summary)

## Overview

The task management system is built around the `TaskManager` class, which acts as the central coordinator for all task-related operations in the binding. It manages the complete lifecycle of tasks and provides a clean, testable architecture through dependency injection.

### TaskManager Responsibilities

The `TaskManager` class provides the following core capabilities:

1. **Task Initialization**: Creates initial tasks (ConnectionTask, UpdateTask, ServerSyncTask) via `initializeTasks()`
2. **State-Driven Management**: Automatically starts/stops tasks based on server state via `processStateChange()`
3. **Discovery Task Creation**: Handles deferred creation of DiscoveryTask via `createDiscoveryTask()` when ClientDiscoveryService becomes available
4. **Task Cleanup**: Stops and cleans up all tasks via `stopAllTasks()` during handler disposal

### Architecture Principles

The TaskManager follows SOLID principles:

- **Single Responsibility**: Manages only task lifecycle, delegates task creation to TaskFactory
- **Dependency Injection**: Accepts TaskFactoryInterface via constructor for testability
- **Instance-Based**: Each ServerHandler has its own TaskManager instance with injected dependencies
- **State-Driven**: Tasks are automatically managed based on ServerState enum values

### Test Coverage

The TaskManager has comprehensive unit test coverage (19 test methods) in `TaskManagerTest`:

- **Constructor validation** - Verifies proper dependency injection
- **Task initialization** - Tests creation of 3 core tasks, handles null discovery service
- **State transitions** - Tests all 8 ServerState values (CONFIGURED, CONNECTED, INITIALIZING, ERROR, DISPOSED, DISCOVERED, NEEDS_AUTHENTICATION)
- **Task lifecycle** - Verifies proper starting/stopping of tasks during state changes
- **WebSocket vs Polling** - Tests mutual exclusivity of WebSocketTask and ServerSyncTask
- **Discovery task** - Tests deferred creation pattern
- **Cleanup operations** - Tests stopAllTasks() with various scenarios (null futures, cancelled tasks, done tasks)
- **Edge cases** - Zero-interval tasks, empty task maps, missing tasks

All tests use Mockito for mocking dependencies and verify both behavior and state.

## Architecture Diagram

```mermaid
%%{init: { 'themeVariables': { 'fontSize': '14px' }, 'class': { 'useMaxWidth': false } } }%%
classDiagram
    %% Task management relationships
    TaskManager --> TaskFactoryInterface : uses (injected)
    TaskManager --> AbstractTask : manages
    TaskFactoryInterface <|.. TaskFactory
    TaskFactory ..> AbstractTask : creates

    class TaskManager {
        -TaskFactoryInterface taskFactory
        +TaskManager(TaskFactoryInterface)
        +initializeTasks(...) Map~String,AbstractTask~
        +createDiscoveryTask(...) AbstractTask
        +processStateChange(...) void
        +stopAllTasks(Map) void
        -getTaskIdsForState(ServerState, Map) List~String~
        -startTaskInternal(String, Map, Map, Scheduler) void
        -stopTaskInternal(String, Map, Map) void
        -scheduleTask(AbstractTask, int, int, Scheduler) ScheduledFuture
        -stopScheduledTask(ScheduledFuture) void
    }

    class TaskFactoryInterface {
        <<interface>>
        +createConnectionTask(...) ConnectionTask
        +createUpdateTask(...) UpdateTask
        +createServerSyncTask(...) ServerSyncTask
        +createDiscoveryTask(...) DiscoveryTask
    }

    class TaskFactory {
        +createConnectionTask(...) ConnectionTask
        +createUpdateTask(...) UpdateTask
        +createServerSyncTask(...) ServerSyncTask
        +createDiscoveryTask(...) DiscoveryTask
    }

    class AbstractTask {
        <<abstract>>
        +getId() String
        +getStartupDelay() int
        +getInterval() int
        +run()
    }

    %% Color scheme for external libraries
    style AbstractTask fill:#99ccff,stroke:#6699cc,color:#000
```

## Tasks by Server State

The `TaskManager.processStateChange()` method is called whenever the server handler
transitions to a new state.
This method automatically starts and stops tasks based
on the server's current state.

### State-Task Mapping

The following table shows which tasks are active for each server state:

| Server State             | Active Tasks                                         | Purpose                                                                           |
| ------------------------ | ---------------------------------------------------- | --------------------------------------------------------------------------------- |
| **INITIALIZING**         | None                                                 | Initial state before configuration is analyzed                                    |
| **DISCOVERED**           | None                                                 | Server was found via discovery but not yet configured                             |
| **NEEDS_AUTHENTICATION** | None                                                 | Configuration exists but no access token is available                             |
| **CONFIGURED**           | `ConnectionTask`                                     | Establishes connection and authenticates with the server                          |
| **CONNECTED**            | `WebSocketTask` OR `ServerSyncTask`, `DiscoveryTask` | Real-time updates via WebSocket (or polling fallback) and discovers clients       |
| **ERROR**                | None                                                 | Error state - tasks stopped until error is resolved                               |
| **DISPOSED**             | None                                                 | Handler is disposed - all tasks permanently stopped                               |

**Note**: `UpdateTask` is not automatically started by state transitions. It is created during initialization but only used when explicitly triggered by configuration updates or manual operations.

### Task Lifecycle

1. **Initialization**: When the handler initializes, `initializeTasks()` creates
   `ConnectionTask`, `UpdateTask`, and `ServerSyncTask` but does not start them.
   The `DiscoveryTask` is created later when the `ClientDiscoveryService` becomes
   available (see [Client Discovery Integration](#client-discovery-integration)).

2. **State Transition**: When `setState()` is called, it triggers
   `TaskManager.processStateChange()` which:
   - Determines which tasks should be active for the new state
   - Stops any running tasks that are not needed
   - Starts any required tasks that are not yet running

3. **CONFIGURED → CONNECTED Transition**:
   - In `CONFIGURED` state, `ConnectionTask` starts running
   - `ConnectionTask` attempts to connect and authenticate
   - On successful authentication, state transitions to `CONNECTED`
   - `ConnectionTask` stops automatically (not needed in `CONNECTED` state)
   - `ServerSyncTask` starts to synchronize server sessions (polling fallback for session updates)
   - `DiscoveryTask` starts to automatically discover Jellyfin clients and now also performs user synchronization immediately before discovery (see note below) (if available)

   **See also:** Diagram: `docs/architecture/connection-state-sequence.md` — sequence diagram showing connection, `CONNECTED` state, WebSocket vs polling, the `ServerSync` loop, and WebSocket fallback.

4. **Disposal**: When the handler is disposed, `stopAllTasks()` is called to
   cancel all scheduled tasks and clear the task registry.

<!-- markdownlint-disable MD033 -->
<a id="serversynctask-and-discovery-task-processing-flow"></a>
<!-- markdownlint-enable MD033 -->

### ServerSyncTask & Discovery Task Processing Flow

The user synchronization responsibility has been moved: the `DiscoveryTask` now
fetches the list of users immediately prior to discovery, and `ClientListUpdater`
continues to retrieve sessions (using `SessionApi.getSessions(null, ...)`) and
filter them by the set of active user IDs. `ServerSyncTask` remains responsible
for periodic session refresh (polling fallback) but no longer fetches the user
list.

```mermaid
%%{init: { 'themeVariables': { 'fontSize': '14px' }, 'sequence': { 'actorMargin': 20 } } }%%
sequenceDiagram
    participant DTask as DiscoveryTask
    participant API as ApiClient
    participant Handler as ServerHandler
    participant UserMgr as UserManager
    participant Updater as ClientListUpdater
    participant SessionAPI as SessionApi
    participant CDS as ClientDiscoveryService

    DTask->>API: GET /Users
    API-->>DTask: List<UserDto>
    DTask->>Handler: usersHandler.accept(users)

    Handler->>UserMgr: processUsersList(users, activeUserIds)
    UserMgr-->>Handler: UserChangeResult
    Handler->>Handler: Update activeUserIds

    Handler->>Updater: updateClients(apiClient, activeUserIds, clients)
    Updater->>SessionAPI: getSessions(null, ...)
    SessionAPI-->>Updater: List<SessionInfoDto>
    Updater->>Updater: Filter by activeUserIds & populate clientMap
    Updater-->>Handler: Client map updated

    Handler->>CDS: discoverClients()
    CDS->>CDS: find and publish client discovery results
```

**Key Steps:**

1. **User Retrieval (DiscoveryTask)**: `DiscoveryTask` fetches the users list via `GET /Users` and passes it to the handler
2. **User Processing**: `UserManager` filters enabled/visible users and detects changes
3. **Session Sync**: `ClientListUpdater` retrieves all active sessions once and filters them by `activeUserIds` (uses `getSessions(null, ...)`), then updates the handler's client map
4. **Discovery Trigger**: `DiscoveryTask` calls `ClientDiscoveryService.discoverClients()` to create/update client things

**Notes:**

- `ServerSyncTask` no longer fetches users; its role is reduced to periodic session refresh when polling is used (WebSocket fallback)

- Using a single `getSessions(null, ...)` call and filtering client-side ensures reliable discovery of client devices across various Jellyfin configurations

## Client Discovery Integration

The `DiscoveryTask` is created differently from other tasks due to the asynchronous
nature of openHAB's `ThingHandlerService` injection lifecycle.

### Async Service Injection Pattern

openHAB injects `ThingHandlerService` instances (like `ClientDiscoveryService`)
asynchronously after the handler's `initialize()` method completes.
This means:

1. **Handler Initialize**: `ServerHandler.initialize()` is called first
2. **Task Initialization**: `TaskManager.initializeTasks()` creates `ConnectionTask`,
   `UpdateTask`, and `ServerSyncTask` (but NOT `DiscoveryTask` yet)
3. **Service Injection**: openHAB framework injects `ClientDiscoveryService`
4. **Service Initialize**: `ClientDiscoveryService.initialize()` is called
5. **Callback**: Discovery service notifies handler via
   `ServerHandler.onDiscoveryServiceInitialized()`
6. **Discovery Task Creation**: Handler calls `TaskManager.createDiscoveryTask()`
   to create the `DiscoveryTask`
7. **Task Registration**: `DiscoveryTask` is added to the task registry
8. **State Check**: If server is already `CONNECTED`, `DiscoveryTask` starts immediately

### Deferred Task Creation Flow

```mermaid
%%{init: { 'themeVariables': { 'fontSize': '14px' }, 'sequence': { 'actorMargin': 20 } } }%%
sequenceDiagram
    participant FW as openHAB Framework
    participant Handler as ServerHandler
    participant DiscSvc as ClientDiscoveryService
    participant TaskMgr as TaskManager
    participant Task as DiscoveryTask

    FW->>Handler: initialize()
    Handler->>TaskMgr: initializeTasks()
    TaskMgr-->>Handler: 3 tasks created<br/>(no DiscoveryTask yet)

    Note over FW,Handler: Service injection happens async

    FW->>DiscSvc: <<inject service>>
    FW->>DiscSvc: initialize()
    DiscSvc->>Handler: onDiscoveryServiceInitialized(this)
    Handler->>TaskMgr: createDiscoveryTask(...)
    TaskMgr->>Task: <<create>>
    TaskMgr-->>Handler: DiscoveryTask
    Handler->>Handler: Add to task registry

    alt Server already CONNECTED
        Handler->>TaskMgr: processStateChange(CONNECTED)
        TaskMgr->>Task: schedule()
        Note over Task: DiscoveryTask starts<br/>running every 60s
    end
```

### Key Design Decisions

1. **Callback Pattern**: Discovery service notifies handler when ready, rather than
   handler polling for service availability
2. **Lazy Creation**: `DiscoveryTask` is created only when `ClientDiscoveryService`
   becomes available
3. **State-Driven**: Once created, `DiscoveryTask` follows the same state-driven
   lifecycle as other tasks (active in `CONNECTED` state only)
4. **Automatic Discovery**: Runs every 60 seconds in `CONNECTED` state to detect
   new Jellyfin clients

## Summary

Task management is handled by a dedicated manager and factory, supporting
extensibility and testability.
The state-driven task lifecycle ensures that only necessary tasks are running,
optimizing resource usage and maintaining proper server communication patterns.

The binding manages five task types:

- **ConnectionTask**: Establishes initial connection and authentication (`CONFIGURED` state)
- **UpdateTask**: Updates handler state (as needed by configuration changes)
- **WebSocketTask**: Maintains WebSocket connection for real-time updates (`CONNECTED` state, preferred)
- **ServerSyncTask**: Synchronizes sessions via polling (fallback in `CONNECTED` state); **user synchronization moved to `DiscoveryTask`**
- **DiscoveryTask**: Automatically discovers Jellyfin clients (`CONNECTED` state)

### WebSocket vs Polling Task Selection

In the `CONNECTED` state, the binding prefers WebSocket for real-time updates:

- **WebSocket enabled** (`useWebSocket=true`, default): `WebSocketTask` runs, `ServerSyncTask` does not
- **WebSocket disabled** (`useWebSocket=false`): `ServerSyncTask` runs for polling
- **WebSocket fallback**: After 10 failed reconnection attempts, `WebSocketTask` triggers callback to start `ServerSyncTask`

**Mutual Exclusivity**: `WebSocketTask` and `ServerSyncTask` never run concurrently to avoid duplicate session updates.

The `DiscoveryTask` uses a callback pattern to handle asynchronous service injection,
ensuring proper integration with openHAB's `ThingHandlerService` lifecycle.

See the [architecture overview](../architecture.md) for context and
[server state transitions](server-state.md) for details on state management.
