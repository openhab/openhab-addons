# Architectural Proposal: Client Session Update Mechanism

**Date**: 2025-11-28  
**Author**: GitHub Copilot (Claude Sonnet 4.5)  
**Status**: Proposal  
**Priority**: High - Blocks client channel functionality

---

## Problem Statement

Currently, `ClientHandler` instances cannot update their channel states because `ServerHandler` does not propagate session information from the `clients` map to child handlers. The `ClientHandler.updateStateFromSession()` method exists but is never called, leaving all client channels empty.

### Current Issues

1. **Missing Communication**: No mechanism to push session updates from ServerHandler to ClientHandler
2. **Tight Coupling**: ServerHandler has grown to >750 lines with multiple responsibilities
3. **Unclear Ownership**: API calls for client control are delegated to ServerHandler, but session data isn't pushed back
4. **Size Concerns**: ServerHandler handles too many responsibilities (API, tasks, discovery, configuration, client updates)

---

## Architectural Options

### Option 1: Direct Child Update (Simple Push Model)

**Implementation**: ServerHandler iterates child things and calls `updateStateFromSession()` after each client list update.

```java
private void updateClientHandlers() {
    getThing().getThings().forEach(childThing -> {
        var handler = childThing.getHandler();
        if (handler instanceof ClientHandler clientHandler) {
            String deviceId = childThing.getUID().getId();
            SessionInfoDto session = clients.values().stream()
                .filter(s -> deviceId.equals(s.getDeviceId()))
                .findFirst().orElse(null);
            clientHandler.updateStateFromSession(session);
        }
    });
}
```

**Pros**:
- ✅ Simple implementation (already implemented and reverted)
- ✅ Direct control flow - easy to understand
- ✅ No new infrastructure needed
- ✅ Minimal code changes

**Cons**:
- ❌ Increases ServerHandler size and responsibilities
- ❌ Tight coupling between ServerHandler and ClientHandler
- ❌ ServerHandler must know about ClientHandler internals
- ❌ Doesn't scale well with additional child types
- ❌ No flexibility for different update strategies

**Recommendation**: ⚠️ Quick fix but adds technical debt

---

### Option 2: Event Bus Pattern (Decoupled Notification)

**Implementation**: Create a `SessionEventBus` that publishes session updates. ClientHandlers subscribe to events for their device ID.

```java
// New class
public class SessionEventBus {
    private final Map<String, List<SessionEventListener>> listeners = new ConcurrentHashMap<>();
    
    public void subscribe(String deviceId, SessionEventListener listener) {
        listeners.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(listener);
    }
    
    public void publishSessionUpdate(String deviceId, SessionInfoDto session) {
        List<SessionEventListener> deviceListeners = listeners.get(deviceId);
        if (deviceListeners != null) {
            deviceListeners.forEach(l -> l.onSessionUpdate(session));
        }
    }
}

// ClientHandler subscribes during initialize()
public void initialize() {
    String deviceId = getThing().getUID().getId();
    getServerHandler().getSessionEventBus().subscribe(deviceId, this::updateStateFromSession);
}

// ServerHandler publishes after updating clients
private void updateClientList() {
    ClientListUpdater.updateClients(apiClient, Set.copyOf(activeUserIds), clients);
    clients.forEach((id, session) -> sessionEventBus.publishSessionUpdate(session.getDeviceId(), session));
}
```

**Pros**:
- ✅ Loose coupling - ServerHandler doesn't know about ClientHandler
- ✅ Extensible - easy to add more listeners
- ✅ Follows existing pattern (ErrorEventBus already in use)
- ✅ ClientHandler controls when to subscribe/unsubscribe
- ✅ Separates concerns cleanly

**Cons**:
- ⚠️ Adds new infrastructure (SessionEventBus class)
- ⚠️ More complex than direct push
- ⚠️ Need to manage listener lifecycle (subscribe/unsubscribe)
- ⚠️ Potential for memory leaks if unsubscribe not called

**Recommendation**: ✅ Best balance of decoupling and complexity

---

### Option 3: Polling Model (Client-Initiated)

**Implementation**: Each ClientHandler starts its own update task that periodically fetches session data via ServerHandler API.

```java
// ClientHandler manages its own refresh task
public class ClientHandler extends BaseThingHandler {
    private @Nullable ScheduledFuture<?> refreshTask;
    
    @Override
    public void initialize() {
        refreshTask = scheduler.scheduleWithFixedDelay(() -> {
            String deviceId = getThing().getUID().getId();
            SessionInfoDto session = getServerHandler().getSessionForDevice(deviceId);
            updateStateFromSession(session);
        }, 0, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void dispose() {
        if (refreshTask != null) {
            refreshTask.cancel(true);
        }
    }
}

// ServerHandler provides query API
public SessionInfoDto getSessionForDevice(String deviceId) {
    return clients.values().stream()
        .filter(s -> deviceId.equals(s.getDeviceId()))
        .findFirst().orElse(null);
}
```

**Pros**:
- ✅ Complete separation - no push mechanism needed
- ✅ ClientHandler fully controls update frequency
- ✅ ServerHandler API is simple (just a getter)
- ✅ Each client can have different refresh rates

**Cons**:
- ❌ More API calls / iterations (N clients × refresh rate)
- ❌ Potential resource waste if many clients
- ❌ Delayed updates (depends on poll interval)
- ❌ Each client needs its own scheduled task
- ❌ Not real-time - misses immediate state changes

**Recommendation**: ⚠️ Less efficient, only if event bus overhead is too high

---

### Option 4: Hybrid Approach (Event + Lazy Fetch)

**Implementation**: Lightweight events notify clients of changes, clients fetch on demand.

```java
// Minimal event notification
public interface SessionChangeListener {
    void onSessionsChanged(); // No data passed
}

// ClientHandler refreshes on notification
public void onSessionsChanged() {
    scheduler.execute(() -> {
        String deviceId = getThing().getUID().getId();
        SessionInfoDto session = getServerHandler().getSessionForDevice(deviceId);
        updateStateFromSession(session);
    });
}

// ServerHandler notifies all clients after update
private void updateClientList() {
    ClientListUpdater.updateClients(apiClient, Set.copyOf(activeUserIds), clients);
    sessionChangeNotifier.notifyAll(); // Broadcast to all listeners
}
```

**Pros**:
- ✅ Combines event benefits with on-demand fetching
- ✅ Lower memory overhead (no session data in events)
- ✅ Clients control concurrency of their updates
- ✅ Flexible - clients can batch/debounce updates

**Cons**:
- ⚠️ More complex than pure push or poll
- ⚠️ Potential thundering herd if many clients update at once
- ⚠️ Two-step process (notify + fetch)
- ⚠️ Clients might miss rapid changes

**Recommendation**: ⚠️ Over-engineered for current needs

---

### Option 5: Direct API Access (Client Owns SessionApi)

**Implementation**: Each ClientHandler gets its own `SessionApi` instance and fetches its session directly from Jellyfin.

```java
public class ClientHandler extends BaseThingHandler {
    private @Nullable SessionApi sessionApi;
    private @Nullable ScheduledFuture<?> refreshTask;
    
    @Override
    public void initialize() {
        ApiClient apiClient = getServerHandler().getApiClient();
        sessionApi = new SessionApi(apiClient);
        
        refreshTask = scheduler.scheduleWithFixedDelay(() -> {
            try {
                String deviceId = getThing().getUID().getId();
                
                // Fetch ALL sessions and find ours
                List<SessionInfoDto> sessions = sessionApi.getSessions(null, null, null, null, null);
                SessionInfoDto ourSession = sessions.stream()
                    .filter(s -> deviceId.equals(s.getDeviceId()))
                    .findFirst().orElse(null);
                
                updateStateFromSession(ourSession);
            } catch (Exception e) {
                logger.warn("Failed to fetch session: {}", e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
```

**Pros**:
- ✅ Complete independence from ServerHandler
- ✅ No coupling between ServerHandler and ClientHandler
- ✅ Real data from API (not cached)
- ✅ Simple design - each client is self-contained

**Cons**:
- ❌ **Communication overhead**: N clients × refresh rate × full session list
- ❌ **API load**: Each client makes its own API call (doesn't reuse ServerHandler's existing calls)
- ❌ **Inefficient**: All clients fetch ALL sessions, then filter
- ❌ **Duplicate work**: ServerHandler already fetches sessions for discovery
- ❌ **Scaling issues**: 10 clients = 10× API calls every refresh interval

**Recommendation**: ❌ Too inefficient, wastes API bandwidth

---

## Detailed Comparison

| Aspect | Option 1: Push | Option 2: Event Bus | Option 3: Polling | Option 4: Hybrid | Option 5: Direct API |
|--------|----------------|---------------------|-------------------|------------------|----------------------|
| **Coupling** | High | Low | Low | Medium | None |
| **Complexity** | Low | Medium | Medium | High | Low |
| **Efficiency** | High | High | Medium | Medium | Low |
| **Scalability** | Low | High | Medium | High | Poor |
| **Real-time Updates** | Yes | Yes | No | Yes | No |
| **API Overhead** | None | None | None | None | Very High |
| **Code Changes** | Minimal | Moderate | Moderate | High | Minimal |
| **Maintenance** | Easy | Medium | Medium | Complex | Easy |
| **Testability** | Harder | Easier | Easier | Medium | Easier |

---

## Recommendation: Option 2 (Event Bus Pattern)

### Rationale

1. **Follows Existing Pattern**: Already using `ErrorEventBus` successfully
2. **Loose Coupling**: ServerHandler doesn't need to know about ClientHandler
3. **Extensible**: Easy to add future listener types (UI updates, logging, etc.)
4. **Efficient**: No duplicate API calls, leverages existing session fetching
5. **Clean Separation**: Each component has clear responsibilities

### Implementation Plan

#### Phase 1: Create SessionEventBus

```java
package org.openhab.binding.jellyfin.internal.events;

public interface SessionEventListener {
    void onSessionUpdate(SessionInfoDto session);
}

public class SessionEventBus {
    private final Map<String, List<SessionEventListener>> listeners = new ConcurrentHashMap<>();
    
    public void subscribe(String deviceId, SessionEventListener listener) {
        listeners.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(listener);
    }
    
    public void unsubscribe(String deviceId, SessionEventListener listener) {
        List<SessionEventListener> deviceListeners = listeners.get(deviceId);
        if (deviceListeners != null) {
            deviceListeners.remove(listener);
        }
    }
    
    public void publishSessionUpdate(String deviceId, @Nullable SessionInfoDto session) {
        List<SessionEventListener> deviceListeners = listeners.get(deviceId);
        if (deviceListeners != null) {
            deviceListeners.forEach(l -> l.onSessionUpdate(session));
        }
    }
    
    public void clear() {
        listeners.clear();
    }
}
```

#### Phase 2: Update ServerHandler

```java
public class ServerHandler extends BaseBridgeHandler {
    private final SessionEventBus sessionEventBus;
    
    public ServerHandler(...) {
        this.sessionEventBus = new SessionEventBus();
    }
    
    public SessionEventBus getSessionEventBus() {
        return sessionEventBus;
    }
    
    private void updateClientList() {
        ClientListUpdater.updateClients(apiClient, Set.copyOf(activeUserIds), clients);
        
        // Publish session updates to event bus
        Set<String> activeDeviceIds = new HashSet<>();
        clients.forEach((id, session) -> {
            String deviceId = session.getDeviceId();
            activeDeviceIds.add(deviceId);
            sessionEventBus.publishSessionUpdate(deviceId, session);
        });
        
        // Notify offline clients (send null session)
        // TODO: Track previous device IDs to detect offline clients
        
        if (discoveryService != null) {
            discoveryService.discoverClients();
        }
    }
    
    @Override
    public void dispose() {
        sessionEventBus.clear();
        super.dispose();
    }
}
```

#### Phase 3: Update ClientHandler

```java
public class ClientHandler extends BaseThingHandler implements SessionEventListener {
    
    @Override
    public void initialize() {
        // Subscribe to session updates for this device
        String deviceId = getThing().getUID().getId();
        getServerHandler().getSessionEventBus().subscribe(deviceId, this);
        
        updateStatus(ThingStatus.UNKNOWN);
    }
    
    @Override
    public void dispose() {
        // Unsubscribe from session updates
        String deviceId = getThing().getUID().getId();
        ServerHandler handler = getServerHandler();
        if (handler != null) {
            handler.getSessionEventBus().unsubscribe(deviceId, this);
        }
        
        super.dispose();
    }
    
    @Override
    public void onSessionUpdate(@Nullable SessionInfoDto session) {
        // This method is called by SessionEventBus when session data changes
        updateStateFromSession(session);
    }
    
    // updateStateFromSession() already exists - no changes needed
}
```

### Benefits Over Current Implementation

1. **Reduced ServerHandler Size**: No need for `updateClientHandlers()` method
2. **Clear Responsibilities**: 
   - ServerHandler: Fetch sessions, publish events
   - ClientHandler: Subscribe to events, update channels
3. **Testability**: Easy to mock SessionEventBus in tests
4. **Flexibility**: Other components can subscribe to session events
5. **Lifecycle Management**: Subscription/unsubscription handled by clients

### Migration Path

1. ✅ Create `SessionEventBus` and `SessionEventListener` (new files)
2. ✅ Add event bus to ServerHandler constructor
3. ✅ Update `ServerHandler.updateClientList()` to publish events
4. ✅ Update `ClientHandler.initialize()` to subscribe
5. ✅ Update `ClientHandler.dispose()` to unsubscribe
6. ✅ Add unit tests for event bus
7. ✅ Test with real Jellyfin server

---

## Alternative Recommendations

### If Event Bus Overhead is Unacceptable

**Use Option 1 (Direct Push)** as a temporary solution:
- ✅ Gets functionality working immediately
- ✅ Can refactor to event bus later
- ⚠️ Document as technical debt
- ⚠️ Plan refactoring in next sprint

### If Client Independence is Critical

**Use Option 3 (Polling)** with optimization:
- ✅ Clients poll ServerHandler's cached sessions (not Jellyfin API)
- ✅ ServerHandler provides `getSessionForDevice(deviceId)` method
- ✅ Configure reasonable refresh interval (5-10 seconds)
- ⚠️ Accept slight delay in updates

---

## Open Questions

1. **Offline Detection**: How should we notify clients when their session goes offline?
   - Option A: Publish `null` session to event bus
   - Option B: Separate `onSessionOffline()` event
   - **Recommendation**: Option A (simpler)

2. **Event Frequency**: Should we throttle/debounce events if sessions update very frequently?
   - Current Jellyfin binding behavior: Sessions update every 5 seconds (configurable)
   - **Recommendation**: No throttling needed at this frequency

3. **Error Handling**: What happens if a ClientHandler's `onSessionUpdate()` throws an exception?
   - **Recommendation**: Catch and log in event bus, don't break other listeners

4. **Thread Safety**: Should event delivery be synchronous or async?
   - **Recommendation**: Synchronous for now (simpler), can optimize later if needed

---

## Related Documentation

- [Current Implementation Plan](../implementation-plan/client-handler.md)
- [Error Event Bus Implementation](../../src/main/java/org/openhab/binding/jellyfin/internal/events/ErrorEventBus.java)
- [ServerHandler Architecture](./core-handler.md)
- [Task Management](./task-management.md)

---

**Decision Required By**: 2025-11-30  
**Impact**: High - Blocks client channel functionality  
**Effort**: Medium (1-2 days implementation + testing)
