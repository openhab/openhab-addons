# Implementation Plan: Coding Guidelines Compliance

**Feature**: `coding-guidelines-compliance`
**Created**: 2026-02-26
**Author**: Patrik Gfeller
**Status**: Ready

---

## Objective

Fix all confirmed violations of the official [openHAB coding guidelines](https://www.openhab.org/docs/developer/guidelines.html)
identified during the analysis session on 2026-02-26.

Only guideline violations are in scope. General best-practice improvements (thread safety,
`instanceof` pattern matching, `var` consistency, etc.) are **out of scope** for this plan.

---

## Background

### Archive Research

- **`static-code-analysis-config`** (archived 2026-01-13): Confirmed that SAT plugin
  (Checkstyle, PMD, SpotBugs) reads exclusion files from repo root only — binding-level
  suppression is not architecturally possible. Sessions 1–3 below do not require suppression;
  they eliminate the violations directly.
- **`maven-build-warnings`** (archived): No overlapping scope.

### Violations Addressed

| ID  | Guideline Section                       | Violation                                                                                          |
| --- | --------------------------------------- | -------------------------------------------------------------------------------------------------- |
| V1  | §F Logging / §D.3 SLF4J                 | `System.err.println()` and `printStackTrace()` in `ErrorEventBus` and `ContextualExceptionHandler` |
| V2  | §B Java Coding Style                    | `UuidDeserializer` missing `@NonNullByDefault`; missing `@Nullable` on return type                 |
| V3  | §B Code Format (SAT/Checkstyle)         | Fully qualified class names inside method bodies in `ServerHandler` instead of import statements   |
| V4  | §E Runtime Behavior / openHAB lifecycle | `getConfigAs()` called in constructor instead of `initialize()`                                    |
| V5  | §D Default Libraries                    | `WebSocketClient` instantiated directly instead of using `WebSocketClientFactory` service          |

---

## Sessions

### Session 1 — SLF4J Compliance (V1)

**Files**: `ErrorEventBus.java`, `ContextualExceptionHandler.java`
**Risk**: Minimal (2 tiny classes, no logic change)
**Dependencies**: None

**Changes**:

1. `ErrorEventBus` — add `private final Logger logger` field; replace the
   `System.err.println(...)` + `e.printStackTrace()` block in `publishEvent()` with
   `logger.warn("Error in event listener: {}", e.getMessage(), e)`.

2. `ContextualExceptionHandler` — add `private final Logger logger` field; replace
   `exception.printStackTrace()` in `handle()` with
   `logger.warn("[{}] Exception handled: {}", context, exception.getMessage(), exception)`.

**Validation**:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

Zero `System.err` / `printStackTrace` references must remain in non-generated code.

---

### Session 2 — `@NonNullByDefault` and Null Annotations (V2)

**Files**: `UuidDeserializer.java`
**Risk**: Minimal
**Dependencies**: Session 1 complete (build must be green)

**Changes**:

1. Add `@NonNullByDefault` class annotation.
2. Change return type of `deserialize()` to `@Nullable UUID` (Jackson handles null field
   mapping correctly; the annotation is required by the null-analysis policy).
3. Remove the superfluous `if (value == null || value.isEmpty())` early-return path that
   returns `null` without annotation — either keep it and annotate, or throw
   `InvalidFormatException` for truly invalid input and annotate the method `@Nullable`.

**Validation**:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

---

### Session 3 — Remove FQCNs, Add Imports (V3)

**Files**: `ServerHandler.java`
**Risk**: Low (mechanical, no logic change)
**Dependencies**: Session 2 complete (build must be green)

**Affected methods** (FQCN occurrences to eliminate):

| Method                      | FQCNs to replace with imports             |
| --------------------------- | ----------------------------------------- |
| `initializeWebSocketTask()` | `WebSocketTask`, `SessionsMessageHandler` |
| `handleWebSocketFallback()` | `WebSocketTask`, `ServerSyncTask`         |
| `sendGeneralCommand()`      | `SessionApi`, `GeneralCommand`            |
| `playItem()`                | `List`, `ArrayList`, `UUID`               |

**Changes**:

1. Add all missing `import` statements at the top of `ServerHandler.java`.
2. Replace every remaining FQCN occurrence in method bodies with the simple name.

**Note**: `ServerSyncTask` and `WebSocketTask` are already imported via
`tasks.*` / `server.*` — verify before adding duplicate imports.

**Validation**:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

No FQCN in method body must remain (Checkstyle will confirm via SAT during full build).

---

### Session 4 — Configuration Lifecycle Fix (V4)

**Files**: `ServerHandler.java`, `Configuration.java` (read-only)
**Risk**: Medium — changes handler constructor and `initialize()` lifecycle
**Dependencies**: Session 3 complete

**Design decision**: The `configuration` field is currently `final`, which prevents it from
being set in `initialize()`. Options:

| Option | Description                                                                                                 | Recommendation                               |
| ------ | ----------------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| A      | Make `configuration` non-`final`, initialize with safe defaults in constructor, overwrite in `initialize()` | ✅ Preferred                                 |
| B      | Remove `configuration` field entirely; call `getConfigAs()` locally each time it is needed                  | Only if field is only used in `initialize()` |

**Changes** (Option A):

1. Remove `final` from `private final Configuration configuration`.
2. Initialize field to `new Configuration()` (default/empty) in constructor as placeholder.
3. Move the real `this.configuration = this.getConfigAs(Configuration.class)` call to
   `initialize()`, before any code that uses configuration values.
4. Verify that `initializeWebSocketTask()` and all other methods that access
   `this.configuration` are only invoked after full initialization.

**Validation**:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

Manually verify: stop and restart the binding; confirm that changing a config value and
saving takes effect without removing/re-adding the Thing.

---

### Session 5 — WebSocketClientFactory Service Injection (V5)

**Files**: `WebSocketTask.java`, `HandlerFactory.java`, `TaskFactory.java`,
`TaskFactoryInterface.java`
**Risk**: Medium-High — OSGi service injection plumbing across multiple classes
**Dependencies**: Session 4 complete

**Context**: openHAB guidelines require:
> WebSocketClient instances should be obtained by the handler factory through the
> WebSocketClientFactory service and unless there are specific configuration
> requirements, the shared instance should be used.

**Changes**:

1. `HandlerFactory` — inject `org.openhab.core.io.net.http.WebSocketClientFactory` as
   `@Reference` via constructor parameter (`@Activate`).
2. Pass the `WebSocketClientFactory` down through `TaskFactory`
   (constructor parameter + `TaskFactoryInterface`).
3. `TaskFactory.createWebSocketTask()` — accept factory; call
   `webSocketClientFactory.getCommonWebSocketClient()` to obtain the shared client
   and pass it to `WebSocketTask`.
4. `WebSocketTask` — replace `new WebSocketClient()` with the injected shared instance.
   **Do NOT** call `webSocketClient.stop()` on a shared instance; only lifecycle-manage
   it if a dedicated (non-shared) client is explicitly required.

**Validation**:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

Verify OSGi wiring is correct by starting the bundle in a running openHAB instance and
confirming the WebSocket connects without errors.

---

## Sequencing Rationale

Sessions are ordered by risk (smallest first) and dependency chain. Each session must end
with a passing `mvn clean install` build before the next begins. This ensures the feature
can be shipped incrementally if needed.

```
V1 (Logging) → V2 (Annotations) → V3 (FQCNs) → V4 (Lifecycle) → V5 (OSGi Service)
```

Sessions 1–3 are pure correctness fixes with no behaviour change. Sessions 4–5 touch
runtime behaviour and require extra validation.

---

## Post-Session Refactoring Integration (2026-03-02)

The `refactor-clienthandler` feature (session 2026-03-01) extracted four responsibilities
from `ClientHandler.java` into standalone utility classes and rewrote the handler itself
(1023 → 392 lines). The new/modified files were reviewed against this compliance plan.

### New production files — compliance status

| File | V1 | V2 | V3 |
|---|---|---|---|
| `util/tick/TickConverter.java` | n/a | ✅ `@NonNullByDefault` | ✅ no FQCNs |
| `util/extrapolation/PlaybackExtrapolator.java` | ✅ SLF4J | ✅ `@NonNullByDefault` | ✅ no FQCNs |
| `util/timeout/SessionTimeoutMonitor.java` | ✅ SLF4J | ✅ `@NonNullByDefault` | ✅ no FQCNs |
| `util/command/ClientCommandRouter.java` | ✅ SLF4J | ✅ `@NonNullByDefault` | ✅ no FQCNs |
| `util/discovery/DeviceIdSanitizer.java` | n/a | ✅ `@NonNullByDefault` | ✅ no FQCNs |
| `handler/ClientHandler.java` (rewritten) | ✅ SLF4J | ✅ `@NonNullByDefault` | ✅ no FQCNs |

### V3 regressions found and fixed (2026-03-02)

During integration review, two files introduced by the refactoring contained FQCNs
in method bodies:

| File | Issue | Fix |
|---|---|---|
| `discovery/ClientDiscoveryService.java` | `new java.util.LinkedHashMap<>()`, `new java.util.ArrayList<>()` in `discoverClients()`; unused `prev` variable | Added `import java.util.ArrayList; import java.util.LinkedHashMap;`; replaced FQCNs; removed unused variable |
| `handler/ClientDiscoveryServiceTest.java` | `java.util.Map`, `java.util.LinkedHashMap`, `org.openhab.core.config.discovery.DiscoveryResult`, `org.openhab.core.thing.Thing` used as FQCNs in test method body | Added `import java.util.LinkedHashMap;`; replaced all FQCNs with simple names (all types already imported) |

Build after fix: **Tests run: 253, Failures: 0, Errors: 0 — BUILD SUCCESS**

---

## Definition of Done

- [ ] All 5 sessions complete with passing `mvn clean install`
- [x] Zero `System.err` / `printStackTrace` in non-generated production code *(Session 1)*
- [x] All non-DTO classes have `@NonNullByDefault` *(Session 2 + refactoring)*
- [x] No FQCNs inside method bodies in non-generated production code *(Session 3 + 2026-03-02 fix)*
- [ ] `getConfigAs()` called only in `initialize()`, not in constructors *(Session 4 pending)*
- [ ] `WebSocketClient` obtained via `WebSocketClientFactory` service *(Session 5 pending)*
- [ ] `mvn spotless:check` passes (exit code 0)
- [ ] Feature marked complete in `active-features.json`
