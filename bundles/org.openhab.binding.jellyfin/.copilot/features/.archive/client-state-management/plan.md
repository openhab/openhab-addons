# Implementation Plan: Client State Management Enhancement

**Feature:** Client State Management Enhancement
**Issue:** #17674 - SDK Version 1.4.x no longer supports recent Jellyfin Server Versions (>10.8)
**PR:** #18628 - [jellyfin] Add support for server versions > 10.8
**Created:** 2026-02-09
**Archived:** 2026-02-28
**Status:** ✅ Archived — all phases complete; remaining items tracked in PR #18628
**Priority:** High - Critical for correct client online/offline detection

---

## Problem Statement

### Current Issue

The current ClientHandler implementation incorrectly ties client online status directly to bridge (server) status, causing all clients to show as ONLINE whenever the server is online, regardless of actual device connectivity.

**Current Broken Behavior:**

- Server ONLINE → ALL clients marked ONLINE (incorrect)
- TV powered OFF → Client shows ONLINE (should be OFFLINE)
- Phone disconnected → Client shows ONLINE (should be OFFLINE)

### Root Cause

```java
// ClientHandler.bridgeStatusChanged() - WRONG
if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
    updateStatus(ThingStatus.ONLINE);  // ❌ Assumes all clients online
}
```

### Correct Behavior Needed

Client status must reflect **TWO independent conditions**:

1. **Bridge availability** (prerequisite - can't function if bridge offline)
2. **Session presence** (actual device connectivity - device must have active/recent session)

---

## Design Decisions

### Detection Strategy

**Polling for Client Discovery:**

- Use ServerSyncTask (60s interval) to detect NEW clients joining
- Poll `/Sessions` endpoint to discover connected devices
- Trigger ClientDiscoveryService when new sessions appear

**WebSocket for Active Clients:**

- Once client discovered, use WebSocket exclusively for real-time updates
- WebSocket provides `SessionsMessage` events (<1s latency)
- No configuration option to disable WebSocket (always use when available)

**Fallback Behavior:**

- If WebSocket connection fails → Fall back to ServerSyncTask polling
- ServerSyncTask continues to provide session updates via event bus

### Architecture Approach

**Inline Implementation (Recommended):**

- No separate ClientStateManager class (keep simple)
- No separate ClientTaskManager class (only 1 task)
- Add session timeout monitoring directly in ClientHandler
- Justification: Client logic is simple (3 states), doesn't justify separate managers

**State Tracking:**

- Track `lastSessionUpdateTimestamp` in ClientHandler
- Add `sessionTimeoutMonitor` scheduled task (checks every 30s)
- Session timeout: 60 seconds without update → mark OFFLINE

---

## Implementation Phases

### Phase 1: Core Session Timeout Logic

**Files Modified:** `ClientHandler.java`

**Tasks:**

1. Add session tracking fields
   - `lastSessionUpdateTimestamp` (long)
   - `sessionTimeoutMonitor` (ScheduledFuture)
   - `SESSION_TIMEOUT_MS` constant (60,000ms)

2. Update `initialize()` method
   - Start session timeout monitor task
   - Schedule at fixed delay (every 30s)

3. Update `onSessionUpdate()` method
   - Record timestamp when session update received
   - Call new `updateClientState()` method

4. Update `bridgeStatusChanged()` method
   - Remove auto-ONLINE assumption
   - Call `updateClientState()` instead

5. Add new `updateClientState()` method
   - Check bridge status first
   - Check session presence second
   - Check session timeout third
   - Update thing status accordingly

6. Add `checkSessionTimeout()` method
   - Compare current time vs lastSessionUpdateTimestamp
   - If >60s, clear session and mark OFFLINE

7. Update `dispose()` method
   - Cancel session timeout monitor
   - Clean up resources

**Acceptance Criteria:**

- ✅ Client OFFLINE when bridge OFFLINE
- ✅ Client OFFLINE when no session exists
- ✅ Client OFFLINE when session timeout (>60s)
- ✅ Client ONLINE only when bridge ONLINE + active session

---

### Phase 2: WebSocket Real-Time Updates

**Files Modified:** `ServerHandler.java`, `TaskManager.java`

**Context:** WebSocketTask already implemented (Phase 3, Task 2 completed in PR #18628)

**Tasks:**

1. Verify WebSocket message routing
   - Ensure `SessionsMessage` events reach SessionEventBus
   - Verify SessionEventBus → ClientHandler event flow

2. Remove `useWebSocket` configuration option
   - Always use WebSocket when available
   - Simplify logic: no user configuration needed

3. Update TaskManager logic
   - Remove configuration checks for WebSocket
   - Always prefer WebSocket over ServerSyncTask when CONNECTED
   - Keep ServerSyncTask as fallback only

4. Verify WebSocket fallback behavior
   - Test handleWebSocketFallback() mechanism
   - Ensure ServerSyncTask starts when WebSocket fails

**Acceptance Criteria:**

- ✅ WebSocket used exclusively when server CONNECTED
- ✅ Session updates delivered <1s via WebSocket
- ✅ Fallback to polling if WebSocket fails
- ✅ No configuration option (simplified)

---

### Phase 3: Automated Testing

**Files Modified:** Test files

**Focus:** Automated unit and integration tests (defer manual testing to end)

**Tasks:**

1. Unit Tests - Session Timeout Logic
   - Test session timeout detection (60s threshold)
   - Test updateClientState() logic paths (ONLINE/OFFLINE transitions)
   - Test bridge status change handling
   - Mock time progression for timeout testing

2. Unit Tests - WebSocket Integration
   - Test WebSocket message handling (SessionsMessage parsing)
   - Test automatic fallback to polling
   - Test handleWebSocketFallback() mechanism
   - Mock WebSocket connection failures

3. Integration Tests - State Management
   - Test client state calculation (bridge + session + timeout)
   - Test multiple clients with independent sessions
   - Test session updates via event bus
   - Mock server bridge and session data

4. Integration Tests - Task Coordination
   - Test TaskManager WebSocket preference logic
   - Test ServerSyncTask fallback activation
   - Test no duplicate updates (WebSocket + polling)
   - Mock task lifecycle transitions

5. Test Coverage Analysis
   - Verify >80% code coverage for modified classes
   - Ensure all critical paths tested
   - Document any untestable code

**Acceptance Criteria:**

- ✅ All automated tests passing
- ✅ >80% code coverage for ClientHandler
- ✅ WebSocket fallback logic verified via tests
- ✅ Session timeout logic verified via tests
- ✅ No regressions in existing tests

---

### Phase 4: Documentation and Manual Testing

**Files Modified:** PR description, documentation

**Tasks:**

1. Update PR #18628 description
   - Mark Phase 3 tasks as complete
   - Document automated test coverage
   - Update completion status

2. Create final documentation
   - Connection state sequence diagram
   - Client state logic flowchart
   - WebSocket vs polling comparison
   - Update architecture documentation

3. Code cleanup
   - Clean up comments and TODOs
   - Run code formatter
   - Verify no deprecated code remains

4. Manual Testing (Real Hardware)
   - Test with Fire TV: power ON → ONLINE, power OFF → OFFLINE after 60s
   - Test WebSocket latency (<1s for state updates)
   - Verify automatic fallback to polling
   - 24-hour stability test (optional)

5. Final validation
   - Run full build
   - Check for warnings
   - Verify all tests pass
   - Validate manual test results

**Acceptance Criteria:**

- ✅ PR description reflects current state
- ✅ All documentation updated
- ✅ Zero build warnings
- ✅ All automated tests passing
- ✅ Manual testing validates real-world behavior

---

## File Changes Summary

### Core Implementation Files

| File | Type | Description |
|------|------|-------------|
| `ClientHandler.java` | Modified | Add session timeout logic, updateClientState() |
| `ServerHandler.java` | Modified | Remove useWebSocket config option |
| `TaskManager.java` | Modified | Simplify WebSocket vs polling logic |
| `Configuration.java` | Modified | Remove useWebSocket field |

### Documentation Files

| File | Type | Description |
|------|------|-------------|
| `docs/architecture/client-state.md` | New | Client state management documentation |
| `docs/architecture/connection-state-sequence.md` | Modified | Update with client state logic |
| `.copilot/features/client-state-management/pr-description.md` | New | Local PR description copy |

---

## Risk Assessment

### Low Risk

- ✅ Inline implementation keeps changes localized
- ✅ Builds on existing WebSocket infrastructure
- ✅ Backward compatible (only improves detection)

### Medium Risk

- ⚠️ Session timeout value (60s) may need tuning
- ⚠️ WebSocket fallback logic needs thorough testing

### Mitigation

- Extensive integration testing with real devices
- Monitor performance in production environment
- Document timeout configuration for future adjustment

---

## Dependencies

### External Dependencies

- None (uses existing infrastructure)

### Internal Dependencies

- ✅ WebSocketTask (already implemented)
- ✅ SessionEventBus (already implemented)
- ✅ ServerSyncTask (already implemented as fallback)

### Build Dependencies

- Java 17
- Maven 3.x
- openHAB core libraries

---

## Success Criteria

### Functional Requirements

1. ✅ Client status accurately reflects device connectivity
2. ✅ WebSocket provides <1s update latency
3. ✅ Session timeout detects device offline within 60s
4. ✅ Bridge offline → all clients offline
5. ✅ Automatic fallback to polling if WebSocket fails

### Non-Functional Requirements

1. ✅ No performance degradation
2. ✅ Memory usage remains stable
3. ✅ Code maintainability preserved
4. ✅ Documentation complete and accurate

---

## Next Steps

1. **Review and Approval** - Review this plan with developer
2. **Phase 1 Implementation** - Begin ClientHandler modifications
3. **Testing** - Validate with real devices
4. **Documentation** - Update PR description and docs
5. **Merge** - Submit for code review and merge

---

## Related Documentation

- [Connection State Visualization](.copilot/temp/connection-state-visualization-20260209.md)
- [State-Task Relationship Analysis](.copilot/temp/state-task-relationship-analysis-20260209.md)
- [PR #18628](https://github.com/openhab/openhab-addons/pull/18628)
- [Issue #17674](https://github.com/openhab/openhab-addons/issues/17674)

---

**Plan Version:** 1.0
**Last Updated:** 2026-02-09
**Author:** GitHub Copilot (Claude Sonnet 4.5)
