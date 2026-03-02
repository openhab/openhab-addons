````prompt
# Session Prompt: Session 4 — Configuration Lifecycle Fix

**Feature**: `coding-guidelines-compliance`
**Phase**: 4 of 5
**Date context**: 2026-03-02

---

## Objective

Fix violation **V4**: `getConfigAs()` is called inside the `ServerHandler` constructor instead
of inside `initialize()`. The openHAB lifecycle contract (§E Runtime Behavior) requires
that `getConfigAs()` be called only in `initialize()`, after the framework has fully wired
the Thing and its configuration.

---

## Session Mode

**Implementation** — files will be modified.

Load and follow ALL `.github/` instructions before making any changes:

1. `.github/copilot-instructions.md` (discovery entry point)
2. `.github/instruction-manifest.json` (filter by `appliesTo: openhab-binding`)
3. `project-type.txt` → `openhab-binding`
4. Core instructions (mandatory): `00-agent-workflow-core.md`, `03-code-quality-core.md`
5. Technology: `.github/technologies/openhab/` (null-safety standards)
6. Project: `.github/projects/openhab-binding/openhab-binding-coding-guidelines.md`
7. **Claude-specific**: `.github/00-agent-workflow/00.9-claude-specific-guidelines.md`

Feature assignment: **`coding-guidelines-compliance`** (phase 3 is `complete`).

---

## Baseline

Build: **SUCCESS** | Tests: **253/253** | Spotless: **clean**
Note: One flaky test exists — `ClientDiscoveryServiceTest.testSanitizeDeviceIdReplacesSpecialCharacters`
fails intermittently (~50% of full-suite runs). Re-run once to confirm pre-existing flakiness.

---

## Scope (This Session Only)

Two files:

| File | Change |
|------|--------|
| `internal/handler/ServerHandler.java` | Move `getConfigAs()` from constructor to `initialize()` |
| `src/test/.../handler/ServerHandlerTest.java` | Update `TestServerHandler` to use reflection for config |

**Out of scope**: Any other change, third-party code, test files beyond `ServerHandlerTest.java`.

---

## Required Changes

### 1. `internal/handler/ServerHandler.java`

**Step 1 — Remove `final`**:

```java
// Before
private final Configuration configuration;

// After
private Configuration configuration;
```

**Step 2 — Constructor placeholder**:

```java
// Before (in constructor)
this.configuration = this.getConfigAs(Configuration.class);

// After
this.configuration = new Configuration();
```

**Step 3 — Move to `initialize()`**:

Add as the first statement inside the `try` block in `initialize()`:

```java
@Override
public void initialize() {
    try {
        this.configuration = this.getConfigAs(Configuration.class);  // ← ADD THIS LINE
        setState(ServerState.INITIALIZING);
        scheduler.execute(initializeHandler());
    } catch (Exception e) {
        ...
    }
}
```

### 2. `src/test/.../handler/ServerHandlerTest.java`

The `TestServerHandler` inner class currently uses a static `configForCtor` workaround to
intercept `getConfigAs()` during the super constructor call. After Step 2 above, the
constructor no longer calls `getConfigAs()`, so `this.configuration` will be `new Configuration()`
(default) instead of the test config after construction.

**Fix**: Set the `configuration` field via reflection in the `TestServerHandler` constructor,
immediately after setting `thingField`. The field is now non-`final`.

```java
// In TestServerHandler constructor, inside the try/catch that sets thingField:
java.lang.reflect.Field configurationField = ServerHandler.class.getDeclaredField("configuration");
configurationField.setAccessible(true);
configurationField.set(this, config);
```

**Important**: Keep `configForCtor` / `setConfigForCtor` in place for now (used in many test
call sites). It has no effect on the constructor anymore but is harmless.

---

## Verification Sequence

All methods that read `this.configuration` must only be invoked after `initialize()` completes.
Confirm:

- `initializeWebSocketTask()` → called from `handleConnection()` callback, which is triggered
  by `setState(CONNECTED)` inside `initializeHandler()` — after `initialize()` is called ✅
- `handleConfigurationUpdate()` → called by framework only after initialization ✅
- `resolveServerUri()` → called from `initializeHandler()` (scheduled by `initialize()`) ✅
- Constructor setup code that does `taskManager.initializeTasks(...)` passes lambdas that
  capture `this` — lambdas only fire at runtime, after `initialize()` has set `configuration` ✅

---

## Validation (Mandatory)

Run in this exact order:

```bash
mvn spotless:apply -f pom.xml
mvn test -f pom.xml
```

Expected: BUILD SUCCESS, 253 tests pass (or re-run once if the known flaky test fails).

---

## Session Report

Create the session report at:

```
.copilot/features/coding-guidelines-compliance/sessions/2026-03-02-session-04-config-lifecycle.md
```

Update `active-features.json` to set phase 4 `status` → `"complete"` with
`"completedDate": "2026-03-02"`.

---

## Commit

```bash
git add src/main/java/org/openhab/binding/jellyfin/internal/handler/ServerHandler.java
git add src/test/java/org/openhab/binding/jellyfin/internal/handler/ServerHandlerTest.java
git add .copilot/
git add .github/
git commit -s -m "[jellyfin] Fix config lifecycle: move getConfigAs() from constructor to initialize()"
```

---

## Next Session

After this session completes, proceed with:
`.copilot/features/coding-guidelines-compliance/prompts/session-05-websocket-factory.prompt.md`
````
