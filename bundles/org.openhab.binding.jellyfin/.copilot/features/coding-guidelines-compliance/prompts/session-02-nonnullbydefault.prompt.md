````prompt
# Session Prompt: Session 2 — @NonNullByDefault and Null Annotations

**Feature**: `coding-guidelines-compliance`
**Phase**: 2 of 5
**Date context**: 2026-02-26

---

## Objective

Fix violation **V2**: `UuidDeserializer` is missing the mandatory `@NonNullByDefault`
class annotation and its `deserialize()` method returns `null` without the required
`@Nullable` return-type annotation.

The official openHAB coding guidelines (§B Java Coding Style) require:
- All non-DTO classes must carry `@NonNullByDefault` at class level.
- Any method that may return `null` must explicitly annotate its return type with
  `@Nullable`.

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

Feature assignment: **`coding-guidelines-compliance`** (registered in
`.copilot/features/active-features.json`; phase 1 is `complete`).

---

## Baseline

The build currently fails with **386 pre-existing SAT errors** (was 388 before session 1).
This is a known baseline. The goal of this session is to eliminate the V2 violation and
reduce the SAT error count — not to reach zero (that happens after sessions 3–5).

---

## Scope (This Session Only)

One file: `src/main/java/org/openhab/binding/jellyfin/internal/api/util/UuidDeserializer.java`

**Out of scope**: any other file, any other guideline, any refactoring beyond the minimum
needed to fix V2.

---

## Current State

```java
public class UuidDeserializer extends JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;           // ← returns null without @Nullable annotation
        }
        // ... UUID parsing logic
    }
}
```

Problems:
1. Class is missing `@NonNullByDefault`.
2. Return type is `UUID` but the method can return `null` — must be `@Nullable UUID`.
3. No import for `org.eclipse.jdt.annotation.NonNullByDefault` or
   `org.eclipse.jdt.annotation.Nullable`.

---

## Required Changes

### `UuidDeserializer.java`

1. Add imports (in alphabetical order with the existing imports):

   ```java
   import org.eclipse.jdt.annotation.NonNullByDefault;
   import org.eclipse.jdt.annotation.Nullable;
   ```

2. Add `@NonNullByDefault` class annotation (immediately before the class declaration):

   ```java
   @NonNullByDefault
   public class UuidDeserializer extends JsonDeserializer<UUID> {
   ```

3. Change the return type of `deserialize()` to `@Nullable UUID`:

   ```java
   @Override
   public @Nullable UUID deserialize(JsonParser parser, DeserializationContext context) throws IOException {
   ```

   The `null` return for empty/null values is intentional Jackson behaviour — keep it and
   annotate rather than throwing.

No other changes are needed. The UUID parsing logic remains untouched.

---

## Validation (Mandatory)

Run in this exact order after making the changes:

```bash
mvn spotless:apply -f pom.xml
mvn clean install -f pom.xml
```

After the build, verify with grep that the annotation is present:

```bash
grep -n "@NonNullByDefault\|@Nullable" \
  src/main/java/org/openhab/binding/jellyfin/internal/api/util/UuidDeserializer.java
```

Expected output: at least two matches — one `@NonNullByDefault` on the class, one
`@Nullable` on the return type.

---

## Session Report

Create the session report at:

```
.copilot/features/coding-guidelines-compliance/sessions/2026-02-26-session-02-nonnullbydefault.md
```

Use the template at `.github/templates/agent-session-report.template.md`.

Update `active-features.json` to set phase 2 `status` → `"complete"` with
`"completedDate": "<today's date>"`.

---

## Commit

After successful build and session report, commit with:

```bash
git add src/main/java/org/openhab/binding/jellyfin/internal/api/util/UuidDeserializer.java
git add .copilot/
git commit -s -m "[jellyfin] Fix null-safety compliance: add @NonNullByDefault and @Nullable to UuidDeserializer"
```

The `-s` flag adds the mandatory DCO sign-off.

---

## Next Session

After this session completes, proceed with:
`.copilot/features/coding-guidelines-compliance/prompts/session-03-remove-fqcns.prompt.md`
(to be created at end of this session or by the planning agent).
````
