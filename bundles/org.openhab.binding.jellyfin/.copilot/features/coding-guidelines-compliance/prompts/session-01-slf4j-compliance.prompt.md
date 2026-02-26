# Session Prompt: Session 1 — SLF4J Compliance

**Feature**: `coding-guidelines-compliance`
**Phase**: 1 of 5
**Date context**: 2026-02-26

---

## Objective

Fix violation **V1**: `System.err.println()` and `printStackTrace()` are used in two
production classes. The official openHAB coding guidelines (§F Logging, §D.3) mandate
SLF4J as the **only** permitted logging mechanism in OSGi bundles.

---

## Session Mode

**Implementation** — files will be modified.

Load and follow ALL `.github/` instructions before making any changes:

1. `.github/copilot-instructions.md` (discovery entry point)
2. `.github/instruction-manifest.json` (filter by `appliesTo: openhab-binding`)
3. `project-type.txt` → `openhab-binding`
4. Core instructions (mandatory): `00-agent-workflow-core.md`, `03-code-quality-core.md`
5. Technology: `.github/technologies/openhab/` (logging standards)
6. Project: `.github/projects/openhab-binding/openhab-binding-coding-guidelines.md`

Feature assignment: **`coding-guidelines-compliance`** (already registered in
`.copilot/features/active-features.json`).

---

## Scope (This Session Only)

Two files in `src/main/java/org/openhab/binding/jellyfin/internal/`:

| File | Violation |
|------|-----------|
| `events/ErrorEventBus.java` | `System.err.println()` + `e.printStackTrace()` in `publishEvent()` catch block |
| `exceptions/ContextualExceptionHandler.java` | `exception.printStackTrace()` in `handle()` |

**Out of scope**: any other file, any other guideline, any refactoring beyond the
minimum needed to fix V1.

---

## Required Changes

### `ErrorEventBus.java`

1. Add SLF4J logger field (already used elsewhere in the project — follow the existing
   pattern exactly):

   ```java
   private final Logger logger = LoggerFactory.getLogger(ErrorEventBus.class);
   ```

2. In `publishEvent()`, replace:

   ```java
   System.err.println("Error in event listener: " + e.getMessage());
   e.printStackTrace();
   ```

   with:

   ```java
   logger.warn("Error in event listener: {}", e.getMessage(), e);
   ```

### `ContextualExceptionHandler.java`

1. Add SLF4J logger field:

   ```java
   private final Logger logger = LoggerFactory.getLogger(ContextualExceptionHandler.class);
   ```

2. In `handle()`, replace:

   ```java
   exception.printStackTrace();
   ```

   with:

   ```java
   logger.warn("[{}] Exception handled: {}", context, exception.getMessage(), exception);
   ```

3. Add the required `import` statements for `Logger` and `LoggerFactory` if not already
   present.

---

## Validation (Mandatory)

Run in this exact order after making the changes:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

Both commands must succeed with exit code 0 and **zero build errors**.

After build succeeds, verify with grep that no violations remain in non-generated code:

```bash
grep -r "System\.err\|printStackTrace" \
  src/main/java/org/openhab/binding/jellyfin/internal/ \
  --include="*.java"
```

Expected output: **no matches**.

---

## Session Report

Create the session report at:

```
.copilot/features/coding-guidelines-compliance/sessions/2026-02-26-session-01-slf4j-compliance.md
```

Use the template at `.github/templates/agent-session-report.template.md`.

Update `active-features.json` to set phase 1 `status` → `"complete"` with
`"completedDate": "<today's date>"`.

---

## Commit

After successful build and session report, commit with:

```bash
git add src/main/java/org/openhab/binding/jellyfin/internal/events/ErrorEventBus.java
git add src/main/java/org/openhab/binding/jellyfin/internal/exceptions/ContextualExceptionHandler.java
git add .copilot/
git commit -s -m "[jellyfin] Fix SLF4J compliance: replace System.err and printStackTrace with logger"
```

The `-s` flag adds the mandatory DCO sign-off.

---

## Next Session

After this session completes, proceed with:
`.copilot/features/coding-guidelines-compliance/prompts/session-02-nonnullbydefault.md`
(to be created at end of this session or by the planning agent).
