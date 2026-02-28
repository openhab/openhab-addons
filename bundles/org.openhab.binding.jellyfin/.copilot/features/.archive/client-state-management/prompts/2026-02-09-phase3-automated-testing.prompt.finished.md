# Feature Prompt: Client State Management - Phase 3

**Date:** 2026-02-09
**Feature:** client-state-management
**Phase:** 3 - Automated Testing
**Session:** Unit and Integration Tests for State Management

---

## Objective

Validate the client state management implementation through comprehensive **automated testing**:

- Unit tests for session timeout logic
- Unit tests for WebSocket message handling
- Integration tests for state calculation
- Integration tests for task coordination
- Test coverage analysis (>80% target)

**Note:** Manual testing with real hardware (Fire TV) is **deferred to Phase 4**.

---

## Context

**Phase 1 & 2 Completion:**

- ✅ Session timeout logic implemented (60s timeout, 30s check interval)
- ✅ Client status based on bridge + session + timeout
- ✅ WebSocket always initialized (no configuration needed)
- ✅ Automatic fallback to ServerSyncTask when WebSocket fails
- ✅ `useWebSocket` configuration completely removed

**Current State:**

- WebSocket implementation complete (PR #18628)
- Polling fallback mechanism in place
- TaskManager logic optimized
- Documentation updated
- **No automated tests yet for new logic**

**Goal:**

- Create comprehensive unit tests for new logic
- Create integration tests for state coordination
- Achieve >80% code coverage for modified classes
- Verify all critical paths work via automated tests
- Defer manual hardware testing to Phase 4

---

## Tasks - Phase 3

### Task 3.1: Unit Tests - Session Timeout Logic

**File:** `src/test/java/org/openhab/binding/jellyfin/internal/handler/ClientHandlerTest.java`

**Test Cases to Implement:**

1. **Test Session Timeout Detection:**

   ```java
   @Test
   public void testSessionTimeoutDetection() {
       // Setup: Mock session update received at T0
       // Mock time advancement: T0 + 61 seconds
       // Assert: Client status changes to OFFLINE
       // Assert: lastSessionUpdateTimestamp not updated
   }
   ```

2. **Test Active Session Keeps Client Online:**

   ```java
   @Test
   public void testActiveSessionKeepsClientOnline() {
       // Setup: Session update at T0
       // Mock: Advance 30s, send update, advance 30s
       // Assert: Client stays ONLINE throughout
       // Assert: lastSessionUpdateTimestamp updates correctly
   }
   ```

3. **Test Timeout Check Interval:**

   ```java
   @Test
   public void testTimeoutCheckScheduling() {
       // Verify: sessionTimeoutMonitor scheduled at 30s intervals
       // Verify: Check executes when scheduled
       // Verify: Check respects timeout threshold
   }
   ```

4. **Test State Calculation Paths:**

   ```java
   @Test
   public void testUpdateClientStateLogic() {
       // Case 1: Bridge ONLINE + session active (within 60s) → ONLINE
       // Case 2: Bridge ONLINE + session timeout (>60s) → OFFLINE
       // Case 3: Bridge OFFLINE + session active → OFFLINE
       // Case 4: Bridge OFFLINE + session timeout → OFFLINE
   }
   ```

5. **Test Bridge Status Change Handling:**

   ```java
   @Test
   public void testBridgeStatusChangeTrigger() {
       // Setup: Client ONLINE with active session
       // Act: Bridge changes to OFFLINE
       // Assert: updateClientState() called immediately
       // Assert: Client marked OFFLINE (overrides session)
   }
   ```

**Mocking Requirements:**

- Mock `ScheduledExecutorService` for time control
- Mock bridge status via `ThingStatusInfo`
- Mock `SessionEventBus` for session updates
- Mock `@Nullable` annotations correctly

**Coverage Target:** >85% for ClientHandler session logic

---

### Task 3.2: Unit Tests - WebSocket Integration

**Files:**

- `src/test/java/org/openhab/binding/jellyfin/internal/server/WebSocketTaskTest.java`
- `src/test/java/org/openhab/binding/jellyfin/internal/server/SessionsMessageHandlerTest.java`

**Test Cases to Implement:**

1. **Test WebSocket Connection Success:**

   ```java
   @Test
   public void testWebSocketConnectionSuccess() {
       // Setup: Mock ApiClient, valid token
       // Act: WebSocketTask.run()
       // Assert: WebSocket connected
       // Assert: Connection status tracked correctly
   }
   ```

2. **Test SessionsMessage Parsing:**

   ```java
   @Test
   public void testSessionsMessageParsing() {
       // Setup: Mock SessionsMessage JSON payload
       // Act: Handler receives message
       // Assert: SessionManager updated correctly
       // Assert: SessionEventBus publishes session events
   }
   ```

3. **Test Automatic Fallback After Retries:**

   ```java
   @Test
   public void testWebSocketFallbackAfter10Failures() {
       // Setup: Mock connection failures
       // Act: Simulate 10 consecutive failures
       // Assert: Fallback callback invoked
       // Assert: Retry count resets
   }
   ```

4. **Test Exponential Backoff Delays:**

   ```java
   @Test
   public void testReconnectionExponentialBackoff() {
       // Verify delays: 1s → 2s → 4s → 8s → 16s → 32s → 60s (capped)
       // Verify: Delay never exceeds 60s
       // Verify: Backoff resets after successful connection
   }
   ```

5. **Test Resource Cleanup on Dispose:**

   ```java
   @Test
   public void testWebSocketResourceCleanup() {
       // Setup: Active WebSocket connection
       // Act: dispose() called
       // Assert: Connection closed
       // Assert: No resource leaks
   }
   ```

**Mocking Requirements:**

- Mock WebSocket connection (okhttp3 WebSocket)
- Mock `SessionsMessage` JSON payloads
- Mock fallback callback (`Runnable`)
- Mock `SessionManager` interactions

**Coverage Target:** >80% for WebSocketTask and SessionsMessageHandler

---

### Task 3.3: Integration Tests - State Management

**File:** `src/test/java/org/openhab/binding/jellyfin/internal/integration/ClientStateIntegrationTest.java`

**Test Cases to Implement:**

1. **Test Full Client State Lifecycle:**

   ```java
   @Test
   public void testClientStateLifecycle() {
       // Setup: Server bridge ONLINE, client initialized
       // Act 1: Session update received → Assert client ONLINE
       // Act 2: Advance time 61s → Assert client OFFLINE (timeout)
       // Act 3: New session received → Assert client ONLINE again
   }
   ```

2. **Test Bridge Status Overrides Session:**

   ```java
   @Test
   public void testBridgeOfflineOverridesActiveSession() {
       // Setup: Client ONLINE with active session
       // Act: Bridge status changes to OFFLINE
       // Assert: Client immediately OFFLINE
       // Assert: Session timeout monitoring continues
   }
   ```

3. **Test Multiple Clients with Independent Sessions:**

   ```java
   @Test
   public void testMultipleClientsIndependentState() {
       // Setup: 2 ClientHandlers, 2 sessions
       // Act: Session 1 timeout, Session 2 active
       // Assert: Client 1 OFFLINE, Client 2 ONLINE
       // Assert: No interference between clients
   }
   ```

4. **Test Session Update Routing via Event Bus:**

   ```java
   @Test
   public void testSessionEventBusRouting() {
       // Setup: ClientHandler subscribed to SessionEventBus
       // Act: SessionEventBus.publish(sessionUpdate)
       // Assert: Correct ClientHandler receives update
       // Assert: Other ClientHandlers not affected
   }
   ```

5. **Test Session Timeout with Bridge Reconnect:**

   ```java
   @Test
   public void testSessionTimeoutDuringBridgeReconnect() {
       // Setup: Client ONLINE, bridge goes OFFLINE
       // Act: Session times out while bridge offline
       // Act: Bridge reconnects
       // Assert: Client remains OFFLINE (no session)
   }
   ```

**Mocking Requirements:**

- Mock `ServerHandler` as bridge
- Mock `SessionEventBus` with publish/subscribe
- Create multiple `ClientHandler` instances
- Mock `SessionInfoDto` objects

**Coverage Target:** >75% for integration scenarios

---

### Task 3.4: Integration Tests - Task Coordination

**File:** `src/test/java/org/openhab/binding/jellyfin/internal/integration/TaskCoordinationTest.java`

**Test Cases to Implement:**

1. **Test WebSocket Task Preferred Over Polling:**

   ```java
   @Test
   public void testWebSocketPreferredWhenAvailable() {
       // Setup: TaskManager with both WebSocketTask and ServerSyncTask
       // Act: Server state → CONNECTED
       // Assert: WebSocketTask started
       // Assert: ServerSyncTask NOT started
   }
   ```

2. **Test Fallback to Polling on WebSocket Failure:**

   ```java
   @Test
   public void testAutomaticFallbackToPolling() {
       // Setup: WebSocketTask fails after 10 retries
       // Act: handleWebSocketFallback() invoked
       // Assert: WebSocketTask stopped
       // Assert: ServerSyncTask started automatically
   }
   ```

3. **Test Mutual Exclusivity of Tasks:**

   ```java
   @Test
   public void testNoSimultaneousWebSocketAndPolling() {
       // Setup: TaskManager managing task lifecycle
       // Act: Various state transitions
       // Assert: WebSocketTask and ServerSyncTask never both active
       // Assert: Transitions are clean (stop before start)
   }
   ```

4. **Test Task State Transition Matrix:**

   ```java
   @Test
   public void testTaskManagerStateTransitions() {
       // Test: CONFIGURED state → ConnectionTask only
       // Test: CONNECTED state → WebSocketTask + DiscoveryTask
       // Test: ERROR state → All tasks stopped
       // Test: DISPOSED state → All tasks stopped
   }
   ```

5. **Test Task Scheduling and Intervals:**

   ```java
   @Test
   public void testTaskSchedulingIntervals() {
       // Verify: ServerSyncTask scheduled at refreshSeconds interval
       // Verify: DiscoveryTask scheduled correctly
       // Verify: WebSocketTask runs immediately (0 delay)
   }
   ```

**Mocking Requirements:**

- Mock `TaskManager` and `TaskFactory`
- Mock `AbstractTask` implementations
- Mock `ScheduledExecutorService` with scheduling verification
- Mock task execution callbacks

**Coverage Target:** >80% for TaskManager state logic

---

### Task 3.5: Test Coverage Analysis

**Goal:** Achieve >80% code coverage for all modified classes

**Tools:**

- JaCoCo for coverage measurement
- Maven plugin: `mvn clean test jacoco:report`

**Classes to Analyze:**

1. **ClientHandler.java**
   - Target: >85% coverage
   - Focus: Session timeout logic, state calculation

2. **WebSocketTask.java**
   - Target: >80% coverage
   - Focus: Connection management, fallback logic

3. **SessionsMessageHandler.java**
   - Target: >80% coverage
   - Focus: Message parsing, event publishing

4. **TaskManager.java**
   - Target: >80% coverage
   - Focus: State transitions, task scheduling

**Report Generation:**

```bash
# Generate coverage report
mvn clean test jacoco:report

# View report
open target/site/jacoco/index.html
```

**Deliverables:**

- Coverage report saved to `.copilot/features/client-state-management/test-results/coverage-report.html`
- Document coverage percentages in session report
- Identify any untestable code and justify

---

## Test Execution Strategy

### Phase 3A: Unit Tests (Week 1)

1. Implement ClientHandler unit tests
2. Implement WebSocketTask unit tests
3. Run tests: `mvn test -Dtest=*Test`
4. Verify all pass, fix failures

### Phase 3B: Integration Tests (Week 1-2)

1. Implement state management integration tests
2. Implement task coordination integration tests
3. Run tests: `mvn verify`
4. Verify all pass, fix failures

### Phase 3C: Coverage Analysis (Week 2)

1. Generate JaCoCo coverage report
2. Analyze coverage gaps
3. Add tests for uncovered critical paths
4. Achieve >80% target

---

## Acceptance Criteria

- [ ] All unit tests implemented and passing
- [ ] All integration tests implemented and passing
- [ ] Code coverage >80% for modified classes
- [ ] ClientHandler session logic fully tested
- [ ] WebSocket fallback logic verified via tests
- [ ] State calculation correctness verified
- [ ] Task coordination logic verified
- [ ] No regressions in existing tests
- [ ] Test report documented in session

---

## Mocking Strategy

### OpenHAB Framework Mocking

Use **Mockito** for mocking openHAB interfaces:

```java
@ExtendWith(MockitoExtension.class)
public class ClientHandlerTest {
    @Mock
    private ThingStatusInfoChangedEvent bridgeStatusEvent;

    @Mock
    private ScheduledExecutorService scheduler;

    @Mock
    private SessionEventBus sessionEventBus;
}
```

### Time Control for Timeout Testing

Use `ScheduledExecutorService` mock to control time:

```java
// Capture scheduled task
ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
verify(scheduler).scheduleWithFixedDelay(taskCaptor.capture(), eq(30L), eq(30L), eq(TimeUnit.SECONDS));

// Manually execute timeout check
Runnable timeoutCheck = taskCaptor.getValue();
timeoutCheck.run(); // Simulates 30s elapsed
```

### WebSocket Connection Mocking

Mock `okhttp3.WebSocket` for connection testing:

```java
@Mock
private WebSocket webSocket;

@Mock
private WebSocketListener listener;

// Simulate connection success
doAnswer(invocation -> {
    listener.onOpen(webSocket, mock(Response.class));
    return null;
}).when(webSocketClient).connect();
```

---

## Dependencies and Prerequisites

**Build Requirements:**

- JUnit 5 (already in project)
- Mockito (already in project)
- JaCoCo Maven plugin (verify in pom.xml)

**Phase Prerequisites:**

- ✅ Phase 1 complete (timeout logic implemented)
- ✅ Phase 2 complete (WebSocket optimization done)
- ✅ Code compiles without errors
- ✅ Existing tests pass

**Blockers:**

- None - ready for test development

---

## Related Files

- [Implementation Plan](../plan.md)
- [Phase 1 Session Report](../sessions/2026-02-09-phase1-session-timeout-implementation.md)
- [Phase 2 Session Report](../sessions/2026-02-09-phase2-websocket-realtime.md)
- [ClientHandler.java](../../../../src/main/java/org/openhab/binding/jellyfin/internal/handler/ClientHandler.java)
- [WebSocketTask.java](../../../../src/main/java/org/openhab/binding/jellyfin/internal/server/WebSocketTask.java)
- [TaskManager.java](../../../../src/main/java/org/openhab/binding/jellyfin/internal/handler/TaskManager.java)

---

## Expected Outcomes

### Success Metrics

1. **Test Count:** 20-30 new tests created
2. **Coverage:** >80% for all modified classes
3. **Pass Rate:** 100% of tests pass
4. **Execution Time:** <5 minutes for full test suite
5. **CI Integration:** Tests run successfully in Maven build

### Documentation

Session report must include:

- List of all tests created
- Coverage percentage per class
- Any coverage gaps and justification
- Test execution time metrics
- Lessons learned about testability

---

**Status:** Ready for Implementation
**Priority:** High (automated testing required before manual validation)
**Estimated Time:** 8-12 hours (test development + debugging)
**Complexity:** Medium-High (requires good mocking strategy)
