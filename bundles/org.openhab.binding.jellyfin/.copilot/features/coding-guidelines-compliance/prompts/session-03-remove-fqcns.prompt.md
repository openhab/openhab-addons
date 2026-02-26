````prompt
# Session Prompt: Session 3 — Remove Fully Qualified Class Names

**Feature**: `coding-guidelines-compliance`
**Phase**: 3 of 5
**Date context**: 2026-02-26

---

## Objective

Fix violation **V3**: Six source files contain fully qualified class names (FQCNs) used
inline in method bodies, field declarations, and interface signatures instead of
`import` statements at the top. The openHAB coding guidelines (§B Java Coding Style)
require short names with `import` declarations — FQCNs in code are forbidden except
inside `import` statements and Javadoc.

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

Feature assignment: **`coding-guidelines-compliance`** (phase 2 is `complete`).

---

## Baseline

Build: **SUCCESS** | Tests: **204/204** | Spotless: **clean**
Note: One flaky test exists in the baseline — `ClientDiscoveryServiceTest.testSanitizeDeviceIdReplacesSpecialCharacters`
fails intermittently (~50% of full-suite runs) due to test-ordering/mock-isolation issues
unrelated to these changes. If it fails after your changes, re-run `mvn test` once to confirm
it is the same pre-existing flakiness.

---

## Scope (This Session Only)

Six files in `src/main/java/org/openhab/binding/jellyfin/`:

| File | FQCNs to resolve |
|------|------------------|
| `internal/BindingConfiguration.java` | 1 |
| `internal/handler/ClientHandler.java` | 9 types, ~25 occurrences |
| `internal/handler/ServerHandler.java` | 7 types, ~15 occurrences |
| `internal/handler/TaskManager.java` | 1 |
| `internal/handler/TaskManagerInterface.java` | 1 |
| `internal/exceptions/ContextualExceptionHandler.java` | 1 |

**Out of scope**: `src/main/java/org/openhab/binding/jellyfin/internal/thirdparty/`
(third-party generated code), test files, any other guideline beyond FQCN removal.

---

## Required Changes Per File

### 1. `internal/BindingConfiguration.java`

**Add import** (alphabetically after existing `java.util.*` block):
```java
import org.openhab.core.config.core.Configuration;
```

**Replace**:
```java
org.openhab.core.config.core.Configuration config = new org.openhab.core.config.core.Configuration();
```
→
```java
Configuration config = new Configuration();
```

---

### 2. `internal/handler/ClientHandler.java`

**Add imports** (none of these exist; insert in alphabetical order within their groups):

```java
import java.util.HashMap;
import java.util.UUID;
```

```java
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
```

**Replace all occurrences** of the following FQCNs with their short names:

| FQCN | Short name | Approx. lines |
|------|-----------|--------------|
| `org.openhab.core.library.types.PlayPauseType` | `PlayPauseType` | 239–243 |
| `org.openhab.core.library.types.NextPreviousType` | `NextPreviousType` | 248–252 |
| `org.openhab.core.library.types.RewindFastforwardType` | `RewindFastforwardType` | 257–261 |
| `org.openhab.core.library.types.PercentType` | `PercentType` | 271–272 |
| `org.openhab.core.library.types.DecimalType` | `DecimalType` | 280–281, 393–394, 402–403 |
| `org.openhab.core.library.types.StringType` | `StringType` | 291–292, 307–308, 333–334, 342–343, 375–376, 384–385 |
| `org.openhab.core.library.types.OnOffType` | `OnOffType` | 354–356, 365–368 |
| `java.util.HashMap` | `HashMap` | 440 |
| `java.util.UUID` | `UUID` | 309, 344, 538, 801 |

---

### 3. `internal/handler/ServerHandler.java`

**Add imports** (missing from current import block):

```java
import java.net.URISyntaxException;
import java.util.UUID;
```

```java
import org.openhab.binding.jellyfin.internal.handler.tasks.ServerSyncTask;
import org.openhab.binding.jellyfin.internal.server.SessionsMessageHandler;
import org.openhab.binding.jellyfin.internal.server.WebSocketTask;
import org.openhab.core.config.core.Configuration;
```

Note: `java.util.ArrayList`, `java.util.List`, and
`org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask` are **already imported** —
do not add duplicates.

**Replace all occurrences** of the following FQCNs:

| FQCN | Short name | Approx. lines |
|------|-----------|--------------|
| `org.openhab.binding.jellyfin.internal.server.WebSocketTask` | `WebSocketTask` | 151, 161, 164, 166, 185, 188, 192–194, 628–632 |
| `org.openhab.binding.jellyfin.internal.server.SessionsMessageHandler` | `SessionsMessageHandler` | 161 |
| `org.openhab.binding.jellyfin.internal.handler.tasks.ServerSyncTask` | `ServerSyncTask` | 199, 203 |
| `java.util.List` | `List` | 291 |
| `java.util.UUID` | `UUID` | 291, 293, 309, 312, 322, 382, 385, 395 |
| `java.util.ArrayList` | `ArrayList` | 291 |
| `org.openhab.core.config.core.Configuration` | `Configuration` | 806, 836 |
| `java.net.URISyntaxException` | `URISyntaxException` | 890 |

---

### 4. `internal/handler/TaskManager.java`

**Add import** (alphabetically after the existing `org.openhab.binding.jellyfin.internal.handler.tasks.*` block):

```java
import org.openhab.binding.jellyfin.internal.server.WebSocketTask;
```

**Replace all occurrences**:

| FQCN | Short name | Approx. lines |
|------|-----------|--------------|
| `org.openhab.binding.jellyfin.internal.server.WebSocketTask` | `WebSocketTask` | 151, 152, 223, 225 |

---

### 5. `internal/handler/TaskManagerInterface.java`

**Add import** (alphabetically after the existing `org.openhab.binding.jellyfin.*` block):

```java
import org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask;
```

**Replace all occurrences**:

| FQCN | Short name | Approx. lines |
|------|-----------|--------------|
| `org.openhab.binding.jellyfin.internal.handler.tasks.AbstractTask` | `AbstractTask` | 57, 71, 94 |

---

### 6. `internal/exceptions/ContextualExceptionHandler.java`

**Add import** (alphabetically after the existing `org.openhab.binding.jellyfin.*` block):

```java
import org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType;
```

**Replace all occurrences**:

| FQCN | Short name | Approx. lines |
|------|-----------|--------------|
| `org.openhab.binding.jellyfin.internal.types.ExceptionHandlerType` | `ExceptionHandlerType` | 27 |

---

## Implementation Notes

- **Read each file's current state before editing** — line numbers are approximate and
  may have shifted.
- **Do not create duplicate imports** — verify before adding any import that it is not
  already present.
- **Import grouping**: openHAB uses blank-line-separated groups: `java.*`, `javax.*`,
  `org.*` (non-openhab), `org.openhab.*`, followed by a group for `com.*`, etc. Spotless
  will enforce the final order — just ensure no duplicates.
- **Use `multi_replace_string_in_file`** to apply multiple independent edits in parallel
  for efficiency.

---

## Validation (Mandatory)

Run in this exact order:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

After build, verify no FQCNs remain in the affected files:

```bash
grep -n "\bjava\.util\.\|org\.openhab\.core\.library\.types\.\|org\.openhab\.binding\.jellyfin\.internal\." \
  src/main/java/org/openhab/binding/jellyfin/internal/BindingConfiguration.java \
  src/main/java/org/openhab/binding/jellyfin/internal/handler/ClientHandler.java \
  src/main/java/org/openhab/binding/jellyfin/internal/handler/ServerHandler.java \
  src/main/java/org/openhab/binding/jellyfin/internal/handler/TaskManager.java \
  src/main/java/org/openhab/binding/jellyfin/internal/handler/TaskManagerInterface.java \
  src/main/java/org/openhab/binding/jellyfin/internal/exceptions/ContextualExceptionHandler.java \
  | grep -v "^.*:import \|^.*:/[*/]"
```

Expected: zero matches (or only lines that are legitimately annotated/commented).

---

## Session Report

Create the session report at:

```
.copilot/features/coding-guidelines-compliance/sessions/2026-02-26-session-03-remove-fqcns.md
```

Use the template at `.github/templates/agent-session-report.template.md`.

Update `active-features.json` to set phase 3 `status` → `"complete"` with
`"completedDate": "<today's date>"`.

---

## Commit

```bash
git add src/main/java/org/openhab/binding/jellyfin/internal/BindingConfiguration.java
git add src/main/java/org/openhab/binding/jellyfin/internal/handler/ClientHandler.java
git add src/main/java/org/openhab/binding/jellyfin/internal/handler/ServerHandler.java
git add src/main/java/org/openhab/binding/jellyfin/internal/handler/TaskManager.java
git add src/main/java/org/openhab/binding/jellyfin/internal/handler/TaskManagerInterface.java
git add src/main/java/org/openhab/binding/jellyfin/internal/exceptions/ContextualExceptionHandler.java
git add .copilot/
git commit -s -m "[jellyfin] Fix FQCN violations: replace inline qualified names with imports"
```

---

## Next Session

After this session completes, proceed with:
`.copilot/features/coding-guidelines-compliance/prompts/session-04-config-lifecycle.prompt.md`
````
