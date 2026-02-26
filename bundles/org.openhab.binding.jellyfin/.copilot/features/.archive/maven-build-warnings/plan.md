# Maven Build Warnings Fix - Implementation Plan

**Feature**: maven-build-warnings
**Created**: 2026-01-03
**Status**: Active

## Problem Statement

The Jellyfin binding shows Maven build warnings:

1. **Missing plugin version**: `maven-bundle-plugin` has no explicit version in POM (line 48)
2. **Plugin descriptor failure**: `maven-release-plugin:3.21.0` cannot be resolved

## Analysis

### Warning 1: Missing maven-bundle-plugin Version

- **Current state**: Plugin declared without `<version>` element
- **Effective POM**: Shows version 6.0.0 inherited from parent
- **Maven recommendation**: Explicit version in child POM preferred for stability

**Root cause**: Child POM relies on parent pluginManagement inheritance

### Warning 2: maven-release-plugin Failure

- **Symptom**: Plugin `3.21.0` not found in openhab-snapshot repository
- **Context**: No explicit reference to this plugin in binding POM
- **Likely cause**: Parent POM or Maven central repository access issue
- **Impact**: Does not affect build (only affects release process)

## Decision Questions

Before implementing fixes, need clarification:

1. Should child binding POMs explicitly declare maven-bundle-plugin version?
2. Is maven-release-plugin warning expected (parent POM responsibility)?
3. Should we suppress warnings that come from parent POM issues?

## Proposed Solutions

### Option A: Add Explicit Plugin Version (Recommended)

Add `<version>` to maven-bundle-plugin declaration in binding POM:

```xml
<plugin>
  <groupId>org.apache.felix</groupId>
  <artifactId>maven-bundle-plugin</artifactId>
  <version>6.0.0</version>
  <configuration>
    ...
  </configuration>
</plugin>
```

**Pros**: Eliminates warning, explicit dependency
**Cons**: Duplicates parent POM version (maintenance risk)

### Option B: Keep Inherited Version

Leave as-is, document that warning is benign.

**Pros**: Follows openHAB pattern, single source of truth
**Cons**: Warning persists

### Option C: Check Parent POM Convention

Research other bindings to see standard practice.

## Implementation Steps

- [ ] Check 3-5 other openHAB bindings for maven-bundle-plugin usage
- [ ] Identify openHAB project convention
- [ ] Clarify decision questions with developer
- [ ] Apply chosen solution
- [ ] Verify warnings eliminated
- [ ] Document decision rationale

## Out of Scope

- Fixing maven-release-plugin issue (parent POM responsibility)
- Changing parent POM pluginManagement

## Success Criteria

- maven-bundle-plugin warning eliminated or documented as expected
- Build succeeds without binding-specific warnings
- Solution follows openHAB project conventions
