# Client State Management Enhancement Feature

**Feature ID:** `client-state-management`
**Status:** Active - Phase 1 In Progress
**Created:** 2026-02-09
**Issue:** [#17674](https://github.com/openhab/openhab-addons/issues/17674)
**PR:** [#18628](https://github.com/openhab/openhab-addons/pull/18628)

---

## Overview

This feature fixes the critical issue where ClientHandler incorrectly shows all clients as ONLINE when the server bridge is online, regardless of actual device connectivity status.

### Problem
- TV powered OFF → still shows ONLINE ❌
- Phone disconnected → still shows ONLINE ❌
- Only bridge status considered, not device session ❌

### Solution
- Track session presence and timeout (60s)
- Use WebSocket exclusively for real-time updates
- Polling only for new client discovery
- Accurate online/offline status ✅

---

## Feature Structure

```
.copilot/features/client-state-management/
├── README.md                           # This file
├── plan.md                             # Multi-phase implementation plan
├── pr-description.md                   # Local copy of PR #18628 description
├── prompts/                            # Feature prompts for each session
│   └── 2026-02-09-phase1-session-timeout.prompt.md
└── sessions/                           # Session reports (created during work)
    └── (sessions created here as work progresses)
```

---

## Implementation Phases

### Phase 1: Core Session Timeout Logic ⏳ (Current)
**Status:** Not Started
**Files:** `ClientHandler.java`

- Add session tracking (timestamp, timeout monitor)
- Implement `updateClientState()` logic
- Add `checkSessionTimeout()` monitor
- Update initialize/dispose lifecycle

**Prompt:** [phase1-session-timeout.prompt.md](prompts/2026-02-09-phase1-session-timeout.prompt.md)

### Phase 2: WebSocket Exclusive Usage
**Status:** Not Started
**Files:** `ServerHandler.java`, `TaskManager.java`, `Configuration.java`

- Remove `useWebSocket` configuration option
- Always use WebSocket when CONNECTED
- Verify SessionsMessage → SessionEventBus flow
- Test WebSocket fallback to polling

### Phase 3: Testing and Validation
**Status:** Not Started
**Files:** Test files, documentation

- Unit tests for timeout detection
- Integration tests with real devices
- Performance validation
- Documentation updates

### Phase 4: PR Update and Cleanup
**Status:** Not Started
**Files:** PR description, cleanup

- Update PR #18628 description
- Remove unused configuration code
- Final validation

---

## Key Design Decisions

### 1. Polling vs WebSocket Strategy
**Decision:** Use polling to **discover** clients, then WebSocket **exclusively** for updates

**Rationale:**
- ServerSyncTask (60s polling) detects NEW clients joining
- WebSocket provides real-time updates (<1s latency) for ACTIVE clients
- No dual-mode complexity (simpler code)

### 2. No Configuration Option
**Decision:** Always use WebSocket (no `useWebSocket` config)

**Rationale:**
- Simplifies architecture
- Users always get best performance
- Removes decision burden from users
- Fallback automatic if WebSocket fails

### 3. Inline Implementation
**Decision:** No separate ClientStateManager or ClientTaskManager

**Rationale:**
- Client logic is simple (3 states, 1 task)
- Server needs managers (7 states, 5 tasks) - complexity justified
- Client doesn't justify separate classes - inline is clearer
- Can extract later if complexity grows

### 4. Session Timeout: 60 seconds
**Decision:** Mark client OFFLINE after 60s without session update

**Rationale:**
- Balance between responsiveness and false positives
- Matches typical network timeout expectations
- Configurable in future if needed

---

## File Changes

### Modified Files
| File | Changes | Phase |
|------|---------|-------|
| `ClientHandler.java` | Add session timeout logic | 1 |
| `ServerHandler.java` | Remove useWebSocket config | 2 |
| `TaskManager.java` | Simplify WebSocket logic | 2 |
| `Configuration.java` | Remove useWebSocket field | 2 |

### New Documentation
| File | Purpose |
|------|---------|
| `docs/architecture/client-state.md` | Document client state logic |
| `.copilot/features/client-state-management/*` | Feature tracking and plans |

---

## Success Criteria

### Functional
- ✅ Client status accurately reflects device connectivity
- ✅ WebSocket updates <1s latency
- ✅ Offline detection within 60s
- ✅ Automatic WebSocket fallback to polling

### Non-Functional
- ✅ No performance degradation
- ✅ Memory usage stable
- ✅ Code maintainability preserved
- ✅ Zero build warnings

---

## Testing Strategy

### Unit Tests
- Session timeout detection
- updateClientState() logic paths
- Bridge status change handling

### Integration Tests
- TV power cycle (ON → OFF → ON)
- Phone disconnect/reconnect
- Server restart with multiple clients

### Performance
- WebSocket latency measurement
- Timeout monitor overhead
- Memory leak detection

---

## Related Documentation

- [Implementation Plan](plan.md)
- [PR Description](pr-description.md)
- [Connection State Visualization](../../temp/connection-state-visualization-20260209.md)
- [State-Task Relationship](../../temp/state-task-relationship-analysis-20260209.md)

---

## Session History

Sessions will be documented here as work progresses:

- *(No sessions yet)*

---

**Last Updated:** 2026-02-09
**Next Action:** Begin Phase 1 implementation
