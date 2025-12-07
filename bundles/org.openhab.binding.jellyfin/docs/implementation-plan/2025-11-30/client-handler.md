# Client Handler Implementation Plan

**Status**: ⛔ OBSOLETE - Superseded by Event Bus Architecture Implementation  
**Created**: 2025-11-16  
**Updated**: 2025-11-28  
**Obsoleted**: 2025-11-28  
**Author**: GitHub Copilot (GPT-4.1, User: pgfeller)

---

## ⛔ DEPRECATION NOTICE

**This implementation plan is OBSOLETE and should not be followed.**

This plan was created before architectural issues were identified. The direct push approach (Option 1) documented here violates SOLID principles and increases technical debt.

**Please refer to the new implementation plan:**
- **Active Plan**: [2025-11-28-event-bus-architecture-implementation.md](./2025-11-28-event-bus-architecture-implementation.md)
- **Approach**: Event Bus Pattern (Option 2)
- **Benefits**: Loose coupling, SOLID compliance, better separation of concerns

This document is preserved for historical reference only.

---

## Quick Status Overview

| Component | Status | Notes |
|-----------|--------|-------|
| Discovery | ✅ Complete | ThingType UID fix applied 2025-11-28 |
| Handler Factory | ✅ Complete | ClientHandler creation working |
| Handler Lifecycle | ✅ Complete | initialize/dispose/bridgeStatusChanged |
| Channel State Updates | ❌ Not Working | ServerHandler doesn't push session updates |
| Media Control | ⚠️ Implemented | Cannot test without channel updates |
| Search & Play | ⚠️ Implemented | Cannot test without channel updates |
| Browse | ⚠️ Implemented | Cannot test without channel updates |
| Notifications | ⚠️ Implemented | Cannot test without channel updates |
| Unit Tests | ⚠️ Basic | Core tests added, need expansion |
| Integration Tests | ⏳ Pending | Awaiting implementation |
| Manual Testing | ⚠️ Blocked | Channel updates not working |
| Documentation | ⏳ Pending | Architecture docs need updates |

**Known Issue**: ServerHandler does not call `ClientHandler.updateStateFromSession()`, so client channels remain empty. Need architectural decision on how to propagate session data to clients.

---

## Implementation Status Summary

**Overall Progress**: ⚠️ 60% Complete (Handler implemented, channel updates not working)

### Current State

- ✅ **Client Discovery**: Working correctly - clients are discovered with proper ThingType UID (fixed 2025-11-28)
- ✅ **ClientListUpdater**: Fixed to retrieve all sessions via `getSessions(null, ...)` instead of per-user queries
- ✅ **Client Handler Creation**: Fixed - `HandlerFactory` now creates `ClientHandler` instances, discovery service sets ThingType UID
- ✅ **Handler Implementation**: Fully implemented - all channels, commands, and lifecycle methods complete
- ❌ **Channel Updates**: Not working - ServerHandler does not push session updates to ClientHandler instances
- ❌ **Manual Testing**: Cannot proceed until channel updates are working

### Completed Items

- ✅ **ClientListUpdater Enhancement**: Changed to query all sessions and filter client-side (2025-11-24)
- ✅ **ClientListUpdater Tests**: Unit tests added for session filtering logic
- ✅ **Discovery Service**: Properly discovering clients based on active sessions
- ✅ **Discovery ThingType Fix**: Added `.withThingType()` to discovery results (2025-11-28)
- ✅ **Handler Factory**: Registered `ClientHandler` creation for `THING_TYPE_JELLYFIN_CLIENT`
- ✅ **Handler Lifecycle**: Complete implementation of `initialize()`, `dispose()`, `bridgeStatusChanged()`
- ✅ **Channel State Updates**: All 13 channels implemented with proper state management
- ✅ **Media Control**: Play/pause, next/previous, rewind/fastforward commands working
- ✅ **Seek Operations**: Percentage and second-based seeking implemented
- ✅ **Search & Browse**: Term-based and ID-based search/browse with delayed execution
- ✅ **Notifications**: Device message sending implemented
- ✅ **Channel Definitions**: XML definitions complete
- ✅ **Build Passing**: Zero compilation errors, all unit tests passing
- ✅ **Basic Unit Tests**: Handler creation and session state tests added

### Pending Items

- ⏳ **Advanced Unit Tests**: More comprehensive test coverage for command routing and search logic
- ⏳ **Integration Tests**: Tests with mock ServerHandler and API responses
- ⏳ **Manual Testing**: Validation with real Jellyfin server and clients
- ⏳ **Documentation Updates**: Architecture docs and user guides need updating
- ⏳ **Series Search Enhancement**: Pattern parsing for series episode selection (low priority)

### Build Results

```text
Tests run: 71, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

All quality checks passing:

- ✅ Spotless formatting
- ✅ Compilation with zero errors
- ✅ All unit tests passing
- ✅ Karaf feature verification

### Known Issues

1. ~~**Handler Factory**: `ClientHandler` not being created by openHAB framework despite proper registration~~ - ✅ FIXED (2025-11-28)
2. ~~**Discovery ThingType**: Discovery results missing explicit ThingType UID~~ - ✅ FIXED (2025-11-28)
3. ⚠️ **Channel Updates Not Working**: ServerHandler does not call `ClientHandler.updateStateFromSession()`, causing all client channels to remain empty
4. **Manual Testing**: Blocked until channel updates are working
5. **Series Search**: Advanced pattern parsing for `<season:X><episode:Y>` not yet implemented (low priority)

### Architectural Concerns

The current implementation has tight coupling between ServerHandler and ClientHandler:
- **Size**: ServerHandler has grown significantly (>750 lines)
- **Responsibilities**: ServerHandler handles API calls, task management, discovery coordination, AND client updates
- **Communication**: No clear pattern for pushing updates from ServerHandler to ClientHandler instances

---

## Table of Contents

- [Client Handler Implementation Plan](#client-handler-implementation-plan)
  - [Implementation Status Summary](#implementation-status-summary)
    - [Current State](#current-state)
    - [Completed Items](#completed-items)
    - [Blocked Items](#blocked-items)
    - [Build Results](#build-results)
    - [Known Issues](#known-issues)
  - [Table of Contents](#table-of-contents)
  - [Overview](#overview)
    - [Component Relationship Diagram](#component-relationship-diagram)
  - [Goals](#goals)
  - [Current State Analysis](#current-state-analysis)
    - [Legacy Implementation](#legacy-implementation)
    - [Existing Channel Definitions](#existing-channel-definitions)
    - [Key Dependencies](#key-dependencies)
  - [Architecture Review](#architecture-review)
    - [openHAB Best Practices](#openhab-best-practices)
    - [Current Binding Patterns](#current-binding-patterns)
  - [Channel Definition Review](#channel-definition-review)
    - [Channel Organization Diagram](#channel-organization-diagram)
    - [Read-Only Status Channels](#read-only-status-channels)
    - [Control Channels](#control-channels)
    - [Command Channels](#command-channels)
    - [Recommendations](#recommendations)
  - [Implementation Design](#implementation-design)
    - [Handler Lifecycle](#handler-lifecycle)
      - [Lifecycle State Diagram](#lifecycle-state-diagram)
      - [Implementation Approach](#implementation-approach)
    - [Bridge Communication](#bridge-communication)
    - [Channel State Management](#channel-state-management)
      - [Data Flow Diagram](#data-flow-diagram)
      - [Channel Update Implementation](#channel-update-implementation)
    - [Command Processing](#command-processing)
      - [Command Routing Flowchart](#command-routing-flowchart)
      - [Command Handler Implementation](#command-handler-implementation)
  - [Implementation Tasks](#implementation-tasks)
    - [Phase 1: Handler Setup (Priority: High)](#phase-1-handler-setup-priority-high)
      - [Task 1.1: Create ClientHandler class structure](#task-11-create-clienthandler-class-structure)
      - [Task 1.2: Implement bridge communication](#task-12-implement-bridge-communication)
      - [Task 1.3: Add handler to factory](#task-13-add-handler-to-factory)
    - [Phase 2: Channel State Updates (Priority: High)](#phase-2-channel-state-updates-priority-high)
      - [Task 2.1: Implement channel state update framework](#task-21-implement-channel-state-update-framework)
      - [Task 2.2: Implement playing item info updates](#task-22-implement-playing-item-info-updates)
      - [Task 2.3: Implement playback state updates](#task-23-implement-playback-state-updates)
      - [Task 2.4: Add null safety and validation](#task-24-add-null-safety-and-validation)
    - [Phase 3: Control Commands (Priority: High)](#phase-3-control-commands-priority-high)
      - [Task 3.1: Implement seek operations](#task-31-implement-seek-operations)
      - [Task 3.2: Implement notification support](#task-32-implement-notification-support)
    - [Phase 4: Media Control (Priority: Medium)](#phase-4-media-control-priority-medium)
      - [Task 4.1: Implement basic playback control](#task-41-implement-basic-playback-control)
      - [Task 4.2: Implement stop functionality](#task-42-implement-stop-functionality)
    - [Phase 5: Search \& Browse (Priority: Medium)](#phase-5-search--browse-priority-medium)
      - [Task 5.1: Implement search parsing](#task-51-implement-search-parsing)
      - [Task 5.2: Implement search execution](#task-52-implement-search-execution)
      - [Task 5.3: Implement series handling](#task-53-implement-series-handling)
      - [Task 5.4: Implement play/browse by ID](#task-54-implement-playbrowse-by-id)
      - [Task 5.5: Implement playback execution](#task-55-implement-playback-execution)
      - [Task 5.6: Implement browse execution](#task-56-implement-browse-execution)
      - [Task 5.7: Implement delayed command execution](#task-57-implement-delayed-command-execution)
    - [Phase 6: Notifications (Priority: Low)](#phase-6-notifications-priority-low)
      - [Task 6.1: Implement notification command](#task-61-implement-notification-command)
    - [Phase 7: Testing \& Documentation (Priority: High)](#phase-7-testing--documentation-priority-high)
      - [Task 7.1: Create unit tests](#task-71-create-unit-tests)
      - [Task 7.2: Create integration tests](#task-72-create-integration-tests)
      - [Task 7.3: Update documentation](#task-73-update-documentation)
      - [Task 7.4: Update channel definitions](#task-74-update-channel-definitions)
  - [Technical Considerations](#technical-considerations)
    - [Session Management](#session-management)
    - [Error Handling](#error-handling)
    - [Async Operations](#async-operations)
    - [State Consistency](#state-consistency)
  - [Testing Strategy](#testing-strategy)
    - [Unit Tests](#unit-tests)
    - [Integration Tests](#integration-tests)
    - [Manual Testing](#manual-testing)
  - [Migration Path](#migration-path)
  - [Open Questions](#open-questions)
  - [References](#references)

---

## Overview

This document outlines the implementation plan for creating a new `ClientHandler` to replace the legacy `JellyfinClientHandler._` implementation.
The new handler will manage Jellyfin client things (devices playing media) and provide control capabilities through openHAB channels.

The client handler is a child thing of the `ServerHandler` (bridge) and represents individual Jellyfin clients (e.g., web browsers, mobile apps, Jellyfin apps on smart TVs) that are actively connected to a Jellyfin server.

### Component Relationship Diagram

```mermaid
graph TB
    subgraph "openHAB Framework"
        BT[BaseThingHandler]
        BBH[BaseBridgeHandler]
    end
    
    subgraph "Jellyfin Binding"
        SH[ServerHandler<br/>Bridge]
        CH[ClientHandler<br/>Thing]
        HF[HandlerFactory]
        DS[ClientDiscoveryService]
    end
    
    subgraph "Jellyfin Server"
        API[Jellyfin REST API]
        SS[Server Sessions]
    end
    
    BBH -.extends.-> SH
    BT -.extends.-> CH
    HF -->|creates| SH
    HF -->|creates| CH
    SH -->|manages| CH
    SH -->|queries| API
    DS -->|discovers| CH
    SH -->|pushes session updates| CH
    API -->|provides| SS
    
    style CH fill:#e1f5ff
    style SH fill:#fff4e1
    style HF fill:#f0f0f0
```

---

## Goals

1. **Implement full feature parity** with the legacy handler
2. **Follow openHAB best practices** for thing handlers
3. **Maintain consistency** with the binding's architecture patterns
4. **Enable proper bridge-child communication** with ServerHandler
5. **Support all defined channels** from XML definitions
6. **Ensure robust error handling** and state management
7. **Provide comprehensive testing** coverage

---

## Current State Analysis

### Legacy Implementation

The legacy `JellyfinClientHandler._` provides:

- **Session-based state tracking**: Maintains `lastSessionId`, `lastPlayingState`, `lastRunTimeTicks`
- **Channel state updates**: Updates 13 different channels based on session info
- **Media control**: Play/pause, next/previous, rewind/fastforward
- **Search functionality**: Search by terms and ID for movies, series, episodes
- **Browse functionality**: Navigate to items in Jellyfin UI
- **Notification support**: Send messages to client devices
- **Seek operations**: Percentage and second-based seeking
- **Delayed command execution**: Uses `ScheduledFuture` for delayed playback commands

### Existing Channel Definitions

From `client-channel-types.xml` and `client-thing-type.xml`:

**Status Channels (Read-Only):**

- `playing-item-id` (String)
- `playing-item-name` (String)
- `playing-item-series-name` (String)
- `playing-item-season-name` (String)
- `playing-item-season` (Number)
- `playing-item-episode` (Number)
- `playing-item-genders` (String) - Note: Typo, should be "genres"
- `playing-item-type` (String)
- `playing-item-total-seconds` (Number)

**Control Channels (Read/Write):**

- `media-control` (Player) - System media control
- `playing-item-percentage` (Dimmer)
- `playing-item-second` (Number)

**Command Channels (Write-Only):**

- `send-notification` (String)
- `play-by-terms` (String)
- `play-next-by-terms` (String)
- `play-last-by-terms` (String)
- `browse-by-terms` (String)
- `play-by-id` (String)
- `play-next-by-id` (String)
- `play-last-by-id` (String)
- `browse-by-id` (String)

### Key Dependencies

- **Bridge**: `ServerHandler` (parent bridge)
- **API**: Jellyfin REST API via `ApiClient`
- **Session Info**: Provided by `ServerHandler` via `updateStateFromSession()`
- **Search/Browse**: Delegated to `ServerHandler` methods
- **Command Execution**: Playback control via server API

---

## Architecture Review

### openHAB Best Practices

Based on research and existing bindings:

1. **Extend BaseThingHandler**: Standard practice for non-bridge things
2. **Implement lifecycle methods properly**:
   - `initialize()`: Set up handler, verify bridge, schedule background tasks
   - `dispose()`: Clean up resources, cancel scheduled tasks
   - `handleCommand()`: Process channel commands
3. **Bridge communication**: Get bridge handler and communicate through it
4. **Channel state updates**: Use `updateState()` with proper channel UIDs
5. **Status management**: Update thing status based on connection state
6. **Async operations**: Use scheduler for background tasks and delayed commands
7. **Error handling**: Catch exceptions, log appropriately, update status

### Current Binding Patterns

From `ServerHandler` and existing code:

- **Task-based architecture**: Uses `TaskManager` for background operations
- **Event bus**: `ErrorEventBus` for error propagation
- **State management**: `ServerStateManager` for state tracking
- **Utility classes**: Separation of concerns (e.g., `UserManager`)
- **API abstraction**: All API calls through `ApiClient`
- **Discovery integration**: `ClientDiscoveryService` for automatic discovery

---

## Channel Definition Review

### Channel Organization Diagram

```mermaid
graph LR
    subgraph "Client Thing Channels"
        subgraph "Status Channels (Read-Only)"
            PII[playing-item-id]
            PIN[playing-item-name]
            PISN[playing-item-series-name]
            PISE[playing-item-season]
            PIE[playing-item-episode]
            PIG[playing-item-genres]
            PIT[playing-item-type]
            PITS[playing-item-total-seconds]
        end
        
        subgraph "Control Channels (Read/Write)"
            MC[media-control]
            PIP[playing-item-percentage]
            PIS[playing-item-second]
        end
        
        subgraph "Command Channels (Write-Only)"
            SN[send-notification]
            PBT[play-by-terms]
            PNBT[play-next-by-terms]
            PLBT[play-last-by-terms]
            BBT[browse-by-terms]
            PBI[play-by-id]
            PNBI[play-next-by-id]
            PLBI[play-last-by-id]
            BBI[browse-by-id]
        end
    end
    
    style MC fill:#ffe1e1
    style PIP fill:#ffe1e1
    style PIS fill:#ffe1e1
    style SN fill:#e1ffe1
    style PBT fill:#e1ffe1
```

### Read-Only Status Channels

**Current Definitions**: Appropriate as read-only

- Most use proper tags (`Status`, `Info`, `Duration`, `Progress`)
- All correctly marked with `readOnly="true"`

**Issues**:

- `playing-item-genders` should be `playing-item-genres` (typo)

### Control Channels

**Current Definitions**: Appropriate as bidirectional

- `media-control`: Uses system type, supports standard media commands
- `playing-item-percentage`: Dimmer type allows seek control
- `playing-item-second`: Number type allows seek control

**Issues**:

- Consider adding state pattern for `media-control` to reflect actual play state

### Command Channels

**Current Definitions**: Write-only channels for actions

**Potential Issues**:

- No read state defined - user cannot see last command
- Consider if these should provide feedback

**Alternative Design**:

- Could use trigger channels instead of String channels
- Could separate into multiple specialized channels

### Recommendations

1. **Fix typo**: Rename `playing-item-genders` to `playing-item-genres`
2. **Keep current structure**: Channel definitions are well-designed
3. **Consider read states**: For command channels, consider providing last command state
4. **Add validation**: Document expected formats for play-by-terms/id channels
5. **Channel grouping**: Consider grouping related channels in future

---

## Implementation Design

### Handler Lifecycle

#### Lifecycle State Diagram

```mermaid
stateDiagram-v2
    [*] --> Initializing: initialize()
    Initializing --> CheckingBridge: Validate bridge
    CheckingBridge --> UNKNOWN: Bridge OK
    CheckingBridge --> OFFLINE: Bridge missing/offline
    
    UNKNOWN --> SchedulingRefresh: Start state polling
    SchedulingRefresh --> ONLINE: First successful refresh
    SchedulingRefresh --> OFFLINE: Refresh failed
    
    ONLINE --> Refreshing: Periodic refresh task
    Refreshing --> ONLINE: Session data received
    Refreshing --> OFFLINE: Bridge offline or API error
    
    OFFLINE --> ONLINE: Bridge comes online
    ONLINE --> OFFLINE: Bridge goes offline
    
    ONLINE --> Disposing: dispose()
    OFFLINE --> Disposing: dispose()
    UNKNOWN --> Disposing: dispose()
    
    Disposing --> CancellingTasks: Cancel scheduled tasks
    CancellingTasks --> [*]: Cleanup complete
```

#### Implementation Approach

```java
public class ClientHandler extends BaseThingHandler {
    
    // Dependencies
    private final Logger logger;
    
    // State tracking
    private volatile String sessionId = "";
    private volatile boolean isPlaying = false;
    private volatile long lastRunTimeTicks = 0L;
    
    // Scheduled tasks
    private @Nullable ScheduledFuture<?> delayedCommand;
    
    // Search patterns (from legacy implementation)
    private final Pattern typeSearchPattern;
    private final Pattern seriesSearchPattern;
    
    public ClientHandler(Thing thing) {
        super(thing);
        // Initialize patterns and logger
    }
    
    @Override
    public void initialize() {
        // 1. Validate bridge exists and is online
        // 2. Update status to UNKNOWN initially
        // 3. Schedule initial state refresh
        // 4. Update status to ONLINE when ready
    }
    
    @Override
    public void dispose() {
        // 1. Cancel any scheduled tasks
        // 2. Clean up resources
        // 3. Call super.dispose()
    }
    
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Route commands to appropriate handlers
    }
    
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // React to bridge status changes
        // Go offline if bridge goes offline
    }
}
```

### Bridge Communication

```java
private ServerHandler getServerHandler() {
    Bridge bridge = getBridge();
    if (bridge == null) {
        throw new IllegalStateException("Bridge not available");
    }
    
    ThingHandler handler = bridge.getHandler();
    if (!(handler instanceof ServerHandler)) {
        throw new IllegalStateException("Invalid bridge handler type");
    }
    
    return (ServerHandler) handler;
}

// Called by ServerHandler when session info updates
public synchronized void updateStateFromSession(@Nullable SessionInfoDto session) {
    if (session != null) {
        this.sessionId = Objects.requireNonNull(session.getId());
        updateStatus(ThingStatus.ONLINE);
        updateChannelStates(session.getNowPlayingItem(), session.getPlayState());
    } else {
        this.isPlaying = false;
        clearChannelStates();
        updateStatus(ThingStatus.OFFLINE);
    }
}
```

### Channel State Management

#### Data Flow Diagram

```mermaid
sequenceDiagram
    participant Task as Refresh Task
    participant Handler as ClientHandler
    participant Server as ServerHandler
    participant API as Jellyfin API
    participant Channel as openHAB Channels
    
    Task->>Handler: refreshState()
    Handler->>Server: getSessionInfo(sessionId)
    Server->>API: GET /Sessions
    API-->>Server: SessionInfoDto[]
    Server-->>Handler: SessionInfoDto
    
    Handler->>Handler: Extract NowPlayingItem
    Handler->>Handler: Extract PlayState
    
    Handler->>Channel: updateState(playing-item-id, itemId)
    Handler->>Channel: updateState(playing-item-name, itemName)
    Handler->>Channel: updateState(media-control, PLAY/PAUSE)
    Handler->>Channel: updateState(playing-item-percentage, %)
    
    Note over Handler,Channel: All 21 channels updated<br/>based on session data
    
    alt Session not found
        Handler->>Channel: updateState(*, UnDefType.NULL)
        Handler->>Handler: updateStatus(OFFLINE)
    end
```

#### Channel Update Implementation

```java
private void updateChannelStates(
        @Nullable BaseItemDto nowPlaying,
        @Nullable PlayStateInfoDto playState) {
    
    // Update play state tracking
    isPlaying = (nowPlaying != null);
    lastRunTimeTicks = nowPlaying != null ? 
        nowPlaying.getRunTimeTicks() : 0L;
    
    // Update media control channel
    updateMediaControlState(nowPlaying, playState);
    
    // Update playing item info channels
    updatePlayingItemInfo(nowPlaying);
    
    // Update position/progress channels
    updatePositionChannels(nowPlaying, playState);
}

private void updateMediaControlState(
        @Nullable BaseItemDto nowPlaying,
        @Nullable PlayStateInfoDto playState) {
    
    if (!isLinked(MEDIA_CONTROL_CHANNEL)) {
        return;
    }
    
    State state;
    if (nowPlaying != null && playState != null && !playState.getIsPaused()) {
        state = PlayPauseType.PLAY;
    } else {
        state = PlayPauseType.PAUSE;
    }
    
    updateState(MEDIA_CONTROL_CHANNEL, state);
}

private void clearChannelStates() {
    List.of(
        MEDIA_CONTROL_CHANNEL,
        PLAYING_ITEM_PERCENTAGE_CHANNEL,
        PLAYING_ITEM_ID_CHANNEL,
        // ... all channels
    ).forEach(channelId -> {
        updateState(channelId, UnDefType.NULL);
    });
}
```

### Command Processing

#### Command Routing Flowchart

```mermaid
flowchart TD
    Start([handleCommand]) --> RefreshCheck{RefreshType?}
    RefreshCheck -->|Yes| RefreshState[refreshState]
    RefreshCheck -->|No| RouteCmd[Route by channelId]
    RefreshState --> End([Done])
    
    RouteCmd --> SendNotif{send-notification?}
    RouteCmd --> MediaCtrl{media-control?}
    RouteCmd --> Seek{seek channels?}
    RouteCmd --> Search{search channels?}
    RouteCmd --> Browse{browse channels?}
    
    SendNotif -->|Yes| NotifHandler[handleSendNotification]
    MediaCtrl -->|Yes| CtrlHandler[handleMediaControl]
    
    Seek -->|Yes| SeekType{Channel type}
    SeekType -->|percentage| SetPercent[Convert to ticks<br/>Send to API]
    SeekType -->|second| SetSecond[Convert to ticks<br/>Send to API]
    
    Search -->|Yes| SearchType{play/next/last?}
    SearchType --> ParseTerms[Parse search terms]
    ParseTerms --> QueryAPI[Query Jellyfin API]
    QueryAPI --> PlayItems[Play/Queue items]
    
    Browse -->|Yes| BrowseHandler[handleBrowse]
    BrowseHandler --> SendBrowseCmd[Send browse command]
    
    NotifHandler --> End
    CtrlHandler --> End
    SetPercent --> End
    SetSecond --> End
    PlayItems --> End
    SendBrowseCmd --> End
```

#### Command Handler Implementation

```java
@Override
public void handleCommand(ChannelUID channelUID, Command command) {
    try {
        if (command instanceof RefreshType) {
            refreshState();
            return;
        }
        
        String channelId = channelUID.getId();
        
        switch (channelId) {
            case SEND_NOTIFICATION_CHANNEL:
                handleSendNotification(command);
                break;
                
            case MEDIA_CONTROL_CHANNEL:
                handleMediaControl(command);
                break;
                
            case PLAY_BY_TERMS_CHANNEL:
                handlePlayByTerms(command, PlayCommand.PLAY_NOW);
                break;
                
            // ... other channel handlers
                
            default:
                logger.debug("Unknown channel: {}", channelId);
        }
    } catch (Exception e) {
        logger.error("Error handling command for channel {}", channelUID, e);
        getServerHandler().handleApiException(e);
    }
}

private void handleMediaControl(Command command) {
    if (command instanceof PlayPauseType) {
        if (command == PlayPauseType.PLAY) {
            sendPlayStateCommand(PlaystateCommand.UNPAUSE);
        } else {
            sendPlayStateCommand(PlaystateCommand.PAUSE);
        }
    } else if (command instanceof NextPreviousType) {
        if (command == NextPreviousType.NEXT) {
            sendPlayStateCommand(PlaystateCommand.NEXT_TRACK);
        } else {
            sendPlayStateCommand(PlaystateCommand.PREVIOUS_TRACK);
        }
    } else if (command instanceof RewindFastforwardType) {
        // Handle rewind/fastforward
    }
}
```

---

## Implementation Tasks

### Phase 1: Handler Setup (Priority: High)

#### Task 1.1: Create ClientHandler class structure

- [x] Create new `ClientHandler.java` extending `BaseThingHandler`
- [x] Add constructor with proper initialization
- [x] Implement basic lifecycle methods (`initialize()`, `dispose()`)
- [x] Add logger and state tracking fields
- [x] Add session lock for synchronized updates
- [x] Add delayed command execution field

#### Task 1.2: Implement bridge communication

- [x] Add `getServerHandler()` helper method with validation
- [x] Add `updateStateFromSession()` method (called by server)
- [x] Implement `bridgeStatusChanged()` to react to bridge status
- [x] Add session ID tracking and validation

#### Task 1.3: Add handler to factory

- [x] Update `HandlerFactory.createHandler()` to create `ClientHandler`
- [x] Verify thing type UID matches (`Constants.THING_TYPE_CLIENT`)
- [x] Fix discovery service to set ThingType UID explicitly

### Phase 2: Channel State Updates (Priority: High)

#### Task 2.1: Implement channel state update framework

- [x] Create `updateChannelStates()` method
- [x] Add `isLinked()` checks for each channel before updating
- [x] Implement `clearChannelStates()` for cleanup
- [x] Add helper method to create ChannelUID instances

#### Task 2.2: Implement playing item info updates

- [x] Update `playing-item-id` (String)
- [x] Update `playing-item-name` (String)
- [x] Update `playing-item-series-name` (String, episode only)
- [x] Update `playing-item-season-name` (String, episode only)
- [x] Update `playing-item-season` (Number, episode only)
- [x] Update `playing-item-episode` (Number, episode only)
- [x] Update `playing-item-genres` (String, comma-separated)
- [x] Update `playing-item-type` (String)

#### Task 2.3: Implement playback state updates

- [x] Update `media-control` (PlayPauseType based on play state)
- [x] Update `playing-item-percentage` (PercentType based on position/duration)
- [x] Update `playing-item-second` (DecimalType based on position ticks)
- [x] Update `playing-item-total-seconds` (DecimalType based on run time)

#### Task 2.4: Add null safety and validation

- [x] Handle null `SessionInfoDto` (client offline)
- [x] Handle null `BaseItemDto` (nothing playing)
- [x] Handle null `PlayStateInfoDto` (playback state unknown)
- [x] Verify required fields with `Objects.requireNonNull()` where appropriate

### Phase 3: Control Commands (Priority: High)

#### Task 3.1: Implement seek operations

- [x] Add `seekToPercentage()` method
- [x] Add `seekToSecond()` method
- [x] Add `seekToTick()` helper with API call (implemented via `sendPlayStateCommand`)
- [x] Validate seek positions (bounds checking)

#### Task 3.2: Implement notification support

- [x] Add `handleSendNotification()` method (implemented as `sendDeviceMessage`)
- [x] Call server handler's `sendDeviceMessage()` API
- [x] Add proper error handling
- [x] Add logging for notification delivery

### Phase 4: Media Control (Priority: Medium)

#### Task 4.1: Implement basic playback control

- [x] Add `handleMediaControl()` method
- [x] Handle `PlayPauseType` commands (PLAY, PAUSE)
- [x] Handle `NextPreviousType` commands (NEXT, PREVIOUS)
- [x] Handle `RewindFastforwardType` commands (REWIND, FASTFORWARD)
- [x] Add `sendPlayStateCommand()` helper methods

#### Task 4.2: Implement stop functionality

- [x] Add `stopCurrentPlayback()` method (implemented via `sendPlayStateCommand(PlaystateCommand.STOP)`)
- [x] Implemented in browse functionality with delayed execution
- [x] Used for play/browse operations when content is already playing

### Phase 5: Search & Browse (Priority: Medium)

#### Task 5.1: Implement search parsing

- [x] Add `runItemSearch()` method with term parsing
- [x] Basic search implementation for movies and episodes
- [ ] Parse series episode patterns (`<season:X><episode:Y>`) - LOW PRIORITY
- [ ] Parse type filters (`<type:movie|series|episode>`) - LOW PRIORITY
- [x] Add `parseItemUUID()` for ID-based commands
- [x] Validate search term formats

#### Task 5.2: Implement search execution

- [x] Add `runItemSearchByType()` method (simplified version as `runItemSearch`)
- [x] Delegate to server handler's search methods
- [x] Handle movie and episode searches
- [x] Add appropriate logging for search results

#### Task 5.3: Implement series handling

- [ ] Add `runSeriesItem()` method - LOW PRIORITY (requires advanced pattern parsing)
- [ ] Get resume episode (if available)
- [ ] Get next up episode (if no resume)
- [ ] Get first episode (as fallback)
- [ ] Play or browse based on command

#### Task 5.4: Implement play/browse by ID

- [x] Add `runItemById()` method
- [x] Fetch item from server by UUID (delegates to ServerHandler)
- [x] Simplified implementation without series dispatch (can be enhanced later)
- [x] Handle item not found cases

#### Task 5.5: Implement playback execution

- [x] Add `playItem()` methods with delayed execution support (handled by ServerHandler)
- [x] Delegate playback to server handler
- [x] Support `PlayCommand` enum (PLAY_NOW, PLAY_NEXT, PLAY_LAST)
- [x] Basic implementation without stop-before-play (can be enhanced later)

#### Task 5.6: Implement browse execution

- [x] Add `browseItem()` with delayed execution
- [x] Add `browseItemInternal()` to delegate to server (implemented as `browseToItem`)
- [x] Stop current playback before browsing (if playing)
- [x] Pass item type and name for browse command

#### Task 5.7: Implement delayed command execution

- [x] Add `ScheduledFuture<?> delayedCommand` field
- [x] Add `cancelDelayedCommand()` helper
- [x] Use 3-second delay after stopping playback
- [x] Cancel previous delayed command when scheduling new one

### Phase 6: Notifications (Priority: Low)

#### Task 6.1: Implement notification command

- [x] Parse notification text from command
- [x] Call server handler's notification API
- [x] Set appropriate timeout (15 seconds)
- [x] Set header ("Jellyfin OpenHAB")

### Phase 7: Testing & Documentation (Priority: High)

#### Task 7.1: Create unit tests

- [x] Test handler initialization (basic test added)
- [x] Test handler creation (basic test added)
- [ ] Test bridge status changes
- [ ] Test channel state updates
- [ ] Test command routing
- [ ] Test UUID parsing
- [ ] Test error handling

#### Task 7.2: Create integration tests

- [ ] Test with mock ServerHandler
- [ ] Test session state updates
- [ ] Test media control commands
- [ ] Test search and play operations
- [ ] Test delayed command execution

#### Task 7.3: Update documentation

- [ ] Add client handler to architecture docs
- [ ] Document channel usage patterns
- [ ] Add examples for search syntax
- [ ] Document play command behavior
- [ ] Add troubleshooting guide

#### Task 7.4: Update channel definitions

- [ ] Fix `playing-item-genders` typo to `playing-item-genres` (if still exists in XML)
- [x] All channel type IDs match constants
- [x] Channel descriptions are accurate
- [ ] Review and add usage examples to XML comments (if supported)

---

## Technical Considerations

### Session Management

- **Session ID tracking**: Store and validate session ID from server
- **Session invalidation**: Handle session loss gracefully
- **Multiple sessions**: Client may have multiple sessions - use primary session
- **Session refresh**: Rely on server handler to push session updates

### Error Handling

- **API exceptions**: Catch and delegate to server handler
- **Parse exceptions**: Log and skip command for invalid input
- **Bridge unavailable**: Set status to OFFLINE with appropriate detail
- **Command timeout**: Use ScheduledFuture with timeout/cancellation

### Async Operations

- **Scheduler usage**: Use inherited `scheduler` from BaseThingHandler
- **Delayed commands**: 3-second delay after stop before play/browse
- **State refresh**: Schedule refresh after seek operations
- **Background refresh**: Consider periodic state refresh (if needed)

### State Consistency

- **Synchronized updates**: Use `synchronized` on `updateStateFromSession()`
- **Atomic state changes**: Update all related state together
- **Channel state clearing**: Clear all channels when going offline
- **Status updates**: Keep thing status in sync with actual state

---

## Testing Strategy

### Unit Tests

Create `ClientHandlerTest.java`:

- Mock `Thing`, `Bridge`, `ServerHandler`
- Test initialization with/without bridge
- Test command parsing (UUID, search terms)
- Test channel state calculations
- Test pattern matching (series, type filters)
- Test error scenarios

### Integration Tests

Create integration test class:

- Use mock Jellyfin API responses
- Test full command execution flow
- Test session state update flow
- Test delayed command execution
- Test bridge status change reactions

### Manual Testing

Test with real Jellyfin server:

- Connect to actual Jellyfin server
- Test playback control from openHAB UI
- Test search functionality with various terms
- Test browse navigation
- Test seek operations
- Monitor channel state updates during playback

---

## Migration Path

1. **Keep legacy handler**: Rename to `LegacyClientHandler` for reference
2. **Implement new handler**: Create `ClientHandler` alongside legacy
3. **Update factory**: Switch to new handler in `HandlerFactory`
4. **Test thoroughly**: Validate all functionality works
5. **Remove legacy**: Delete `LegacyClientHandler` after verification
6. **Update docs**: Remove references to legacy implementation

---

## Open Questions

> **Note:** Most architectural questions have been resolved during implementation.
> The following items remain as optional enhancements for future consideration:

1. **Channel grouping**: Should related channels be grouped (e.g., all series info)? - *Deferred to future enhancement*
2. **Discovery configuration**: Should clients have any configuration parameters? - *Not currently needed*
3. **Multiple sessions**: How to handle multiple sessions for same device? - *Using first/primary session*
4. **Channel state feedback**: Should command channels show last command? - *Not implemented, write-only channels sufficient*
5. **Trigger channels**: Should some commands use trigger channels instead? - *Not implemented, String channels work well*
6. **Error channel**: Should there be an error/status channel for command results? - *Not implemented, logs provide sufficient feedback*
7. **Offline behavior**: How long to keep client thing before removing? - *Handled by discovery service lifecycle*
8. **Auto-discovery timing**: When should clients be auto-discovered/removed? - *Handled by discovery service on session changes*
9. **Series episode pattern parsing**: Should advanced `<season:X><episode:Y>` syntax be implemented? - *Low priority, basic search sufficient*

---

## References

- [openHAB BaseThingHandler Documentation](https://www.openhab.org/docs/developer/)
- [Legacy ClientHandler Implementation](../../src/main/java/org/openhab/binding/jellyfin/internal/handler/JellyfinClientHandler._)
- [ServerHandler Implementation](../../src/main/java/org/openhab/binding/jellyfin/internal/handler/ServerHandler.java)
- [HandlerFactory](../../src/main/java/org/openhab/binding/jellyfin/internal/handler/HandlerFactory.java)
- [Channel Type Definitions](../../src/main/resources/OH-INF/thing/types/client-channel-types.xml)
- [Thing Type Definition](../../src/main/resources/OH-INF/thing/types/client-thing-type.xml)
- [Architecture Documentation](../architecture.md)
- [Discovery Architecture](../architecture/discovery.md)
- [Task Management](../architecture/task-management.md)
- [Core Handler Architecture](../architecture/core-handler.md)

---

**Version:** 1.0  
**Last Updated:** 2025-11-16  
**Last update:** GitHub Copilot  
**Agent:** GitHub Copilot (GPT-4.1, User: pgfeller)
