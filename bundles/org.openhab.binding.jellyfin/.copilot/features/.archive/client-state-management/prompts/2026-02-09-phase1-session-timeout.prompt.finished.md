# Feature Prompt: Client State Management - Phase 1

**Date:** 2026-02-09
**Feature:** client-state-management
**Phase:** 1 - Core Session Timeout Logic
**Session:** Initial Implementation

---

## Objective

Implement session timeout logic in ClientHandler to accurately detect client online/offline status based on session presence and timeout monitoring.

---

## Context

**Current Problem:**

- ClientHandler incorrectly marks all clients as ONLINE when bridge is ONLINE
- TV powered OFF still shows as ONLINE (incorrect)
- Phone disconnected still shows as ONLINE (incorrect)

**Root Cause:**

```java
// ClientHandler.bridgeStatusChanged() - WRONG
if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
    updateStatus(ThingStatus.ONLINE);  // ❌ Assumes all clients online
}
```

**Correct Behavior:**
Client status = Bridge ONLINE + Active Session + No Timeout

---

## Tasks - Phase 1

### Task 1.1: Add Session Tracking Fields

**File:** `ClientHandler.java`

Add fields to track session state:

```java
private long lastSessionUpdateTimestamp = 0;
private static final long SESSION_TIMEOUT_MS = 60_000; // 60 seconds
private @Nullable ScheduledFuture<?> sessionTimeoutMonitor;
```

### Task 1.2: Update initialize() Method

**File:** `ClientHandler.java`

Start session timeout monitor:

```java
@Override
public void initialize() {
    // ... existing initialization code ...

    // Start session timeout monitor (checks every 30s)
    sessionTimeoutMonitor = scheduler.scheduleWithFixedDelay(
        this::checkSessionTimeout,
        SESSION_TIMEOUT_MS,
        SESSION_TIMEOUT_MS / 2,  // Check every 30s
        TimeUnit.MILLISECONDS
    );
}
```

### Task 1.3: Update onSessionUpdate() Method

**File:** `ClientHandler.java`

Record timestamp when session update received:

```java
@Override
public void onSessionUpdate(@Nullable SessionInfoDto session) {
    synchronized (sessionLock) {
        currentSession = session;
        lastSessionUpdateTimestamp = System.currentTimeMillis();
    }

    updateClientState();
    updateStateFromSession(session);
}
```

### Task 1.4: Update bridgeStatusChanged() Method

**File:** `ClientHandler.java`

Remove auto-ONLINE assumption:

```java
@Override
public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
    logger.debug("Bridge status changed to {} for client {}", bridgeStatusInfo.getStatus(), thing.getUID());

    if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
        // Bridge online - check session before marking client online
        updateClientState();
    } else {
        // Bridge offline - all clients offline
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Server bridge is offline");
        clearChannelStates();
    }
}
```

### Task 1.5: Add updateClientState() Method

**File:** `ClientHandler.java`

New method to determine client status:

```java
/**
 * Updates client status based on bridge status, session presence, and timeout.
 *
 * Priority checks:
 * 1. Bridge must be ONLINE
 * 2. Session must exist
 * 3. Session must not be timed out (>60s)
 */
private void updateClientState() {
    // Priority 1: Check bridge status
    Bridge bridge = getBridge();
    if (bridge == null || bridge.getStatus() != ThingStatus.ONLINE) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Server bridge is not available or offline");
        return;
    }

    // Priority 2 & 3: Check session presence and timeout
    synchronized (sessionLock) {
        if (currentSession == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Device not connected to server");
        } else {
            long timeSinceLastUpdate = System.currentTimeMillis() - lastSessionUpdateTimestamp;
            if (timeSinceLastUpdate > SESSION_TIMEOUT_MS) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "No session update received (timeout)");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }
}
```

### Task 1.6: Add checkSessionTimeout() Method

**File:** `ClientHandler.java`

Periodic timeout check:

```java
/**
 * Checks for session timeout and marks client offline if no update received.
 * Called every 30 seconds by session timeout monitor.
 */
private void checkSessionTimeout() {
    synchronized (sessionLock) {
        if (currentSession != null) {
            long timeSinceLastUpdate = System.currentTimeMillis() - lastSessionUpdateTimestamp;
            if (timeSinceLastUpdate > SESSION_TIMEOUT_MS) {
                logger.info("Session timeout detected for device {} ({}s without update)",
                           deviceId, timeSinceLastUpdate / 1000);
                currentSession = null;
                updateClientState();
                clearChannelStates();
            }
        }
    }
}
```

### Task 1.7: Update dispose() Method

**File:** `ClientHandler.java`

Cancel session timeout monitor:

```java
@Override
public void dispose() {
    logger.debug("Disposing ClientHandler for thing {}", thing.getUID());

    // Cancel session timeout monitor
    ScheduledFuture<?> monitor = sessionTimeoutMonitor;
    if (monitor != null && !monitor.isDone()) {
        monitor.cancel(true);
    }
    sessionTimeoutMonitor = null;

    // ... existing dispose code ...
}
```

---

## Acceptance Criteria

- [ ] Client shows OFFLINE when bridge is OFFLINE
- [ ] Client shows OFFLINE when no session exists (device not connected)
- [ ] Client shows OFFLINE when session timeout (>60s without update)
- [ ] Client shows ONLINE only when bridge ONLINE + active session + recent update
- [ ] Session timeout check runs every 30 seconds
- [ ] All resources cleaned up properly in dispose()

---

## Testing Strategy

### Unit Tests

1. Test updateClientState() with various combinations:
   - Bridge OFFLINE → Client OFFLINE
   - Bridge ONLINE + no session → Client OFFLINE
   - Bridge ONLINE + session timeout → Client OFFLINE
   - Bridge ONLINE + active session → Client ONLINE

2. Test checkSessionTimeout():
   - Session update within 60s → no timeout
   - Session update >60s ago → timeout triggers

### Integration Tests

1. **TV Power Cycle Test**
   - Start: TV ON, playing content → Client ONLINE
   - Turn OFF TV → Within 60s, Client OFFLINE
   - Turn ON TV → Client ONLINE again

2. **Phone Disconnect Test**
   - Start: Phone connected, playing → Client ONLINE
   - Disconnect WiFi → Within 60s, Client OFFLINE
   - Reconnect → Client ONLINE again

3. **Server Restart Test**
   - Start: Multiple clients ONLINE
   - Restart server → All clients OFFLINE (bridge offline)
   - Server back online → Clients reconnect properly

---

## Implementation Notes

### Thread Safety

- Use `synchronized (sessionLock)` for all session access
- lastSessionUpdateTimestamp is volatile-like (accessed in synchronized blocks)
- ScheduledFuture cancellation in dispose() is thread-safe

### Performance

- Timeout check every 30s: minimal overhead
- No polling for session updates (uses event bus)
- Session timeout 60s: reasonable balance (adjustable if needed)

### Backward Compatibility

- No API changes
- No configuration changes
- Only improves status accuracy

---

## Success Metrics

1. **Accuracy:** Client status matches actual device connectivity 100%
2. **Responsiveness:** Offline detection within 60 seconds
3. **Performance:** No measurable CPU/memory increase
4. **Stability:** No resource leaks or thread issues

---

## Related Files

- [Implementation Plan](plan.md)
- [PR Description](pr-description.md)
- [State-Task Analysis](.copilot/temp/state-task-relationship-analysis-20260209.md)

---

**Status:** ✅ COMPLETED - 2026-02-09
**Priority:** High
**Estimated Time:** 2-3 hours
**Actual Time:** ~30 minutes
