# Continue SAT Cleanup - Null-Safety Fixes

**Feature**: sat-cleanup
**Session Date**: 2026-01-09
**Status**: ⏳ IN PROGRESS
**Commit**: 557341db82 (package-info.java implementation)

## Current State

**Package-info.java approach**: ✅ IMPLEMENTED
**Generator fix**: ✅ COMPLETED
**Error reduction**: 74 → 20 errors (73% improvement)
**Remaining work**: Fix 20 compilation errors across 5 files (binding code only)

### What Was Done

1. ✅ Modified `tools/generate-sources/scripts/generate.sh` to add sed-based post-processing
   - Replaces `@NonNull` with `@Nullable` on all field declarations
   - Pattern: `s/^\([[:space:]]*\)@org\.eclipse\.jdt\.annotation\.NonNull\([[:space:]]*\)$/\1@org.eclipse.jdt.annotation.Nullable\2/g`
   - **NEW**: Removes `@NonNullByDefault` annotation and import from all generated classes
   - Runs after Docker OpenAPI generation, before Spotless formatting

2. ✅ Created `package-info.java` for thirdparty package
   - Location: `src/main/java/org/openhab/binding/jellyfin/internal/thirdparty/package-info.java`
   - **Does NOT include** `@NonNullByDefault` annotation
   - Documents rationale: generated external API code should not enforce null-safety

3. ✅ Regenerated all 439 API model files
   - All field declarations now use `@Nullable` annotations
   - All classes now **without** `@NonNullByDefault` (package-level control)
   - Eliminated ALL compilation errors in generated code (was ~50% of total)

3. ✅ Fixed ServerDiscoveryService (2 errors resolved)
   - Added null checks for `systemInformation.getId()` and `systemInformation.getVersion()`
   - Extract to local variables before `map.put()`

4. ⚠️ **BROKE** UuidDeserializer (3 NEW errors)
   - Attempted fix: Marked return type as `@Nullable UUID`
   - **Problem**: Parent interface `JsonDeserializer<UUID>` expects `@NonNull UUID` return
   - **Error**: "The return type is incompatible" + "Illegal redefinition of parameter"
   - **Solution needed**: Remove all annotations from override (rely on `@NonNullByDefault` at class level)

5. ✅ Fixed ServerDiscoveryResult
   - Added `@Nullable` import
   - Marked `endpointAddress` field and getter as `@Nullable`

## Remaining Errors (20 total)

### 1. UuidDeserializer.java (1 error) - **PRIORITY 1**

**Line**: 38

**Current broken code**:
```java
@Override
public @Nullable UUID deserialize(JsonParser parser, DeserializationContext context) throws IOException {
```

**Fix required**:
```java
// Remove @Nullable from return type - parent interface doesn't use JDT annotations
@Override
public UUID deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    // Keep the null return logic - it's allowed by Jackson
    String value = parser.getValueAsString();
    if (value == null || value.isEmpty()) {
        return null;  // This is OK - Jackson allows null from deserializers
    }
    // ... rest of implementation
}
```

**Rationale**: Jackson's `JsonDeserializer<T>` parent interface doesn't constrain return with JDT annotations. Adding `@Nullable` creates "illegal redefinition" errors. Simply remove annotation, rely on permissive parent interface.

### 2. ClientHandler.java (7 errors)

| Line | Issue | Code Example | Fix Pattern |
|------|-------|--------------|-------------|
| 135 | Nullable String → @NonNull | `session.getDeviceId()` → method param | Extract to local, add `if (deviceId != null)` guard |
| 152 | Nullable String → @NonNull | Similar pattern | Same as above |
| 465 | Nullable PlayCommand → @NonNull | `session.getPlayCommand()` | `PlayCommand cmd = session.getPlayCommand(); if (cmd != null) { ... }` |
| 488 | Passing null → @NonNull UUID | Direct null parameter | Check if intent is UUID.randomUUID() or skip call if null |
| 494 | Passing null → @NonNull UUID | Same as 488 | Same fix |
| 700 | Nullable BaseItemKind → @NonNull | `item.getType()` | Extract, guard with null check |
| 709 | Nullable BaseItemKind → @NonNull | Same as 700 | Same fix |

### 3. ServerHandler.java (7 errors)

| Line | Issue | Pattern |
|------|-------|---------|
| 201, 215, 238, 262, 387, 656 | Nullable String → @NonNull | Extract API response strings, add null guards |
| 672 | Passing null → @NonNull Object | Check if null is intentional or needs default value |

**All follow same pattern**: Extract nullable value to local variable, add `if (value != null)` before passing to @NonNull parameter.

### 4. ClientScanTask.java (1 error)

**Line**: 70

**Issue**: `response.getDevices()` returns `@Nullable List<DeviceInfoDto>`, passed to method expecting `@NonNull List`

**Fix**:
```java
List<DeviceInfoDto> devices = response.getDevices();
if (devices != null) {
    processDevices(devices);
} else {
    // Handle no devices case
}
```

### 5. ServerConfiguration.java (1 error)

**Line**: 61

**Issue**: Nullable value passed to `@NonNull CharSequence` parameter

**Fix**: Use `String.isEmpty()` with null check or provide default empty string

### 6. ServerStateManager.java (4 errors)

**Lines**: 55, 61, 74, 78

**Issue**: Passing `null` to `StateAnalysis` factory methods expecting `@NonNull URI`

**Current**: Has `@SuppressWarnings("null")` annotation
**Decision**: Keep suppression (was discussed in previous session as acceptable for this specific case)
**Action**: Verify suppression is properly applied at method/class level

## Next Steps

1. **IMMEDIATELY fix UuidDeserializer** (removes 3 errors)
   - Remove ALL annotations from `deserialize()` method signature
   - Test that null return still works with Jackson

2. **Fix remaining 20 errors** using standard null-safety patterns:
   - Extract nullable API responses to local variables
   - Add `if (value != null)` guards before passing to @NonNull parameters
   - For null literals: Check if intentional or needs replacement with default value

3. **Verify compilation** succeeds (zero errors)

4. **Run SAT verification**: `mvn verify` to ensure spotbugs suppresses warnings in thirdparty package

5. **Commit all changes** with detailed conventional commit message

6. **Create session report** documenting generator fix, decisions, and outcomes

## Key Decisions Made

1. **Generator Template Approach**: Sed post-processing instead of Mustache templates
   - **Rationale**: OpenAPI Java generator doesn't support field-level annotation templates
   - **Trade-off**: Post-processing is less elegant but more reliable

2. **@Nullable on Generated Fields**: Mark all generated model fields as @Nullable
   - **Rationale**: Builder pattern classes don't initialize fields in default constructor
   - **Impact**: Eliminates "field may not have been initialized" errors (46 errors fixed)

3. **UuidDeserializer Return Type**: Cannot override with @Nullable
   - **Rationale**: Parent interface doesn't constrain return/parameters - adding annotations creates conflicts
   - **Solution**: Remove all annotations from override, rely on class-level `@NonNullByDefault`

4. **ServerStateManager Null URIs**: Keep `@SuppressWarnings("null")`
   - **Rationale**: Intentional design - null URI represents specific state (discussed in previous session)
   - **Action**: No changes needed

## Files Modified

- ✅ `tools/generate-sources/scripts/generate.sh` (generator fix + @NonNullByDefault removal)
- ✅ `src/main/java/.../thirdparty/package-info.java` (NEW - disables null-safety for generated code)
- ✅ All 439 files in `src/main/java/.../thirdparty/api/current/model/` (regenerated without @NonNullByDefault)
- ✅ `ServerDiscoveryService.java` (null checks added)
- ⚠️ `UuidDeserializer.java` (needs fix - currently has @Nullable annotation that breaks)
- ✅ `ServerDiscoveryResult.java` (@Nullable field/getter)
- ⚠️ `ClientHandler.java` (needs 7 fixes)
- ⚠️ `ServerHandler.java` (needs 7 fixes)
- ⚠️ `ClientScanTask.java` (needs 1 fix)
- ⚠️ `ServerConfiguration.java` (needs 1 fix)
- ⚠️ `ServerStateManager.java` (verify suppression - 4 errors)

**Commit**: 557341db82 - "refactor(jellyfin): disable null-safety for generated thirdparty API code"

## Commands for Reference

```bash
# Compile and check errors
mvn compile -q 2>&1 | grep "\.java:\[" | grep -v "^\[ERROR\].*\.java:\[" | head -30

# Count unique errors (Maven reports each error twice)
mvn compile 2>&1 | grep "/src/main" | grep ERROR | wc -l
# Then divide by 2 for actual count

# Run SAT verification after zero errors
mvn verify

# Apply code formatting
mvn spotless:apply
```

## Expected Outcome

After completing fixes:
- ✅ Zero compilation errors
- ✅ SAT plugin suppresses warnings in `thirdparty` package
- ✅ All generated code uses `@Nullable` fields (preventing future builder pattern errors)
- ✅ Generated code has NO class-level null-safety (package-info.java controls this)
- ✅ Binding code maintains explicit null-checking for external API data
- ✅ Generator script documents both post-processing steps
- ✅ Conventional commit with detailed explanation

---

**Status**: ⏳ IN PROGRESS (2026-01-09)
**Commit**: 557341db82 (package-info.java approach implemented)
**Next Action**: Fix UuidDeserializer.java first (removes 1 error), then remaining 19 binding code errors
