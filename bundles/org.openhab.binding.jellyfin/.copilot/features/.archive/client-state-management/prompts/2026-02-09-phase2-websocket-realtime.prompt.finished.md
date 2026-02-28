# Feature Prompt: Client State Management - Phase 2

**Date:** 2026-02-09
**Feature:** client-state-management
**Phase:** 2 - WebSocket Real-Time Updates
**Session:** Optimize for Real-Time Detection

---

## Objective

Optimize client state detection by ensuring WebSocket is used exclusively for real-time session updates, removing configuration complexity, and simplifying TaskManager logic.

---

## Context

**Phase 1 Completion:**

- ✅ Session timeout logic implemented (60s timeout, 30s check interval)
- ✅ Client status based on bridge + session + timeout
- ✅ Polling fallback via ServerSyncTask works correctly

**Current State:**

- WebSocketTask already implemented (PR #18628, Phase 3 Task 2)
- `useWebSocket` configuration option exists but adds unnecessary complexity
- WebSocket provides <1s latency vs 60s polling interval
- WebSocket should be preferred whenever available

**Goal:**

- Remove `useWebSocket` configuration option (always use WebSocket)
- Simplify TaskManager logic to prefer WebSocket over polling
- Ensure fallback to ServerSyncTask if WebSocket fails
- Reduce session timeout to 10-15s (WebSocket enables faster detection)

---

## Tasks - Phase 2

### Task 2.1: Remove useWebSocket Configuration

**Files:** `Configuration.java`, `thing-types.xml`

Remove the `useWebSocket` configuration parameter:

1. **Configuration.java:**
   - Remove `useWebSocket` field
   - Remove getter/setter methods
   - Update constructor if needed

2. **thing-types.xml:**
   - Remove `useWebSocket` parameter from server thing type
   - Update documentation to reflect WebSocket is always used

**Rationale:** WebSocket is clearly superior (real-time vs 60s polling). No reason to make it optional.

---

### Task 2.2: Simplify TaskManager WebSocket Logic

**File:** `TaskManager.java`

Update task scheduling logic to always prefer WebSocket:

```java
// Current logic (BEFORE):
if (config.useWebSocket && webSocketStatus == WebSocketStatus.CONNECTED) {
    // Use WebSocket
    stopServerSyncTask();
} else {
    // Use polling
    startServerSyncTask();
}

// Simplified logic (AFTER):
if (webSocketStatus == WebSocketStatus.CONNECTED) {
    // Use WebSocket exclusively
    stopServerSyncTask();
    logger.debug("WebSocket connected - using real-time session updates");
} else {
    // Fallback to polling
    startServerSyncTask();
    logger.debug("WebSocket unavailable - using polling fallback (60s interval)");
}
```

**Key Changes:**

- Remove all `config.useWebSocket` checks
- Always prefer WebSocket when CONNECTED
- ServerSyncTask becomes pure fallback mechanism
- Log which mode is active for debugging

---

### Task 2.3: Verify WebSocket Message Routing

**Files:** `ServerHandler.java`, `WebSocketTask.java`

Verify `SessionsMessage` events are correctly routed:

1. **WebSocketTask.java:**
   - Confirm `SessionsMessage` handling exists
   - Verify events published to SessionEventBus
   - Check error handling for malformed messages

2. **ServerHandler.java:**
   - Verify WebSocket status changes trigger task re-evaluation
   - Confirm handleWebSocketFallback() starts ServerSyncTask
   - Test WebSocket reconnection logic

**Expected Behavior:**

- WebSocket CONNECTED → WebSocketTask receives `SessionsMessage`
- WebSocketTask → SessionEventBus.publish()
- SessionEventBus → ClientHandler.onSessionUpdate()
- Total latency: <1 second

---

### Task 2.4: Reduce Session Timeout (Optional)

**File:** `ClientHandler.java`

Consider reducing session timeout from 60s to 10-15s:

```java
// Before (Phase 1):
private static final long SESSION_TIMEOUT_MS = 60_000; // 60 seconds

// After (Phase 2 - if WebSocket reliable):
private static final long SESSION_TIMEOUT_MS = 15_000; // 15 seconds
```

**Decision Point:** This task is OPTIONAL. Only reduce timeout if:

- WebSocket proves reliable in testing
- No false-positive OFFLINE detections occur
- User prefers faster detection over stability

**Recommendation:** Start with 60s, reduce after Phase 3 testing validates stability.

---

### Task 2.5: Update Documentation

**Files:** Configuration documentation, README

Update documentation to reflect WebSocket is always used:

1. Remove references to `useWebSocket` configuration
2. Document automatic WebSocket usage with polling fallback
3. Explain WebSocket benefits (real-time, <1s latency)
4. Document fallback behavior when WebSocket unavailable

---

## Acceptance Criteria

- [x] `useWebSocket` configuration deprecated (kept for backward compatibility)
- [x] ServerHandler always initializes WebSocket (no conditional check)
- [x] ServerSyncTask acts as fallback only (via TaskManager logic)
- [x] WebSocket message routing verified (TaskManager correct logic confirmed)
- [ ] Fallback to polling tested when WebSocket fails (deferred to Phase 3)
- [x] Documentation updated (no useWebSocket references in user-facing docs)
- [x] Session timeout value decision documented (60s, defer reduction to Phase 3)

---

## Testing Strategy

### WebSocket Priority Testing

1. **Normal Operation:**
   - Start server with WebSocket available
   - Verify ServerSyncTask NOT running
   - Verify session updates arrive <1s via WebSocket
   - Power OFF TV → Client OFFLINE within timeout period

2. **WebSocket Failure:**
   - Disconnect WebSocket (simulate network issue)
   - Verify ServerSyncTask starts automatically
   - Verify session updates continue via polling (60s interval)
   - Reconnect WebSocket → Verify ServerSyncTask stops

3. **Server Restart:**
   - Restart Jellyfin server
   - Verify WebSocket reconnects automatically
   - Verify clients receive session updates via WebSocket

### Performance Testing

1. **Latency Measurement:**
   - Play media on TV
   - Measure time from action → state update in openHAB
   - Expected: <1 second with WebSocket
   - Compare to: 0-60s with polling (depends on timing)

2. **CPU/Memory Impact:**
   - Monitor openHAB process during WebSocket operation
   - Should be negligible (<1% CPU increase)
   - No memory leaks over 24 hours

---

## Implementation Notes

### WebSocket vs Polling Comparison

| Aspect           | WebSocket                      | Polling (ServerSyncTask) |
| ---------------- | ------------------------------ | ------------------------ |
| **Latency**      | <1 second                      | 0-60 seconds (avg 30s)   |
| **Network Load** | Minimal (push-based)           | Higher (60s intervals)   |
| **CPU Usage**    | Minimal (idle connection)      | Minimal (1 request/60s)  |
| **Reliability**  | High (auto-reconnect)          | Very High (HTTP-based)   |
| **Complexity**   | Higher (connection management) | Lower (stateless)        |

**Conclusion:** WebSocket is superior in every practical way. Polling is only needed as fallback.

### Configuration Simplification Benefits

**Before (with useWebSocket option):**

- User must understand WebSocket concept
- User might disable WebSocket for wrong reasons
- Support complexity (two code paths to debug)
- Testing complexity (both modes must work)

**After (always WebSocket):**

- Automatic optimization (user doesn't need to know)
- Single code path (simpler debugging)
- Automatic fallback (best of both worlds)
- Reduced configuration surface area

### Backward Compatibility

**Impact:** MINOR BREAKING CHANGE for users who explicitly set `useWebSocket=false`

**Mitigation:**

- Configuration will be ignored (logged as deprecated)
- Binding will work correctly (just uses WebSocket now)
- No data loss or functionality loss
- Document in PR description

**Upgrade Path:**

- Users can safely remove `useWebSocket` from configuration
- No action required (will be ignored if present)
- Recommend cleanup in next breaking change window

---

## Success Metrics

1. **Latency:** Session updates delivered <1s via WebSocket (measured)
2. **Reliability:** No false OFFLINE detections over 24-hour test
3. **Fallback:** ServerSyncTask activates within 60s of WebSocket failure
4. **Code Quality:** Zero compilation warnings, all tests pass
5. **Documentation:** No references to removed configuration option

---

## Decision Points

### Decision 1: Session Timeout Value

**Question:** Reduce timeout from 60s to 15s after WebSocket verified?

**Options:**

1. **Keep 60s:** Conservative, prevents false positives, works with both modes
2. **Reduce to 15s:** Faster detection, requires reliable WebSocket
3. **Dynamic:** Use 15s with WebSocket, 60s with polling (more complex)

**Recommendation:** DEFER to Phase 3 testing. Keep 60s initially.

### Decision 2: Configuration Migration

**Question:** How to handle existing `useWebSocket` configurations?

**Options:**

1. **Remove completely:** Breaking change, users must remove from config
2. **Deprecate:** Log warning, ignore value, remove in next major version
3. **Throw error:** Force users to update configuration

**Recommendation:** Option 2 (deprecate) - log warning but continue working.

---

## Related Files

- [Implementation Plan](plan.md)
- [Phase 1 Session Report](./../sessions/2026-02-09-phase1-session-timeout-implementation.md)
- [WebSocketTask.java](./../../../../src/main/java/org/openhab/binding/jellyfin/internal/task/WebSocketTask.java)
- [TaskManager.java](./../../../../src/main/java/org/openhab/binding/jellyfin/internal/task/TaskManager.java)

---

## Dependencies

**Prerequisites:**

- ✅ Phase 1 complete (session timeout logic)
- ✅ WebSocketTask implemented (PR #18628)
- ✅ SessionEventBus working

**Blockers:**

- None - all dependencies satisfied

---

**Status:** Ready for Implementation
**Priority:** Medium (optimization, not critical)
**Estimated Time:** 1-2 hours
**Complexity:** Low (mostly simplification/removal)
