# Clarify openHAB Binding File Headers Requirements

**Status**: ⏳ NOT STARTED (2025-12-05)

## Objective
Audit and clarify mandatory file header and class documentation requirements for openHAB bindings, then update the copilot instructions to ensure consistency and enforce compliance in future sessions.

## Problem Statement
- Current state: Some Java files in the binding have proper headers and class documentation; others (especially test files) have inconsistent formatting.
- openHAB guidelines state: "Every Java file must have a license header."
- Actual implementation: Varies across main/test/generated code.
- Requirement: Establish clear, enforceable rules for this binding.

## Context & Findings

### Current State
- **Main source files**: ✅ Have file headers (EPL-2.0 copyright 2010-2025), package declaration, imports, and class JavaDoc with `@author` tag.
- **Test files**: ⚠️ INCONSISTENT
  - Some have proper headers (e.g., `ClientHandlerTest.java`).
  - Some lack headers entirely (e.g., `ClientListUpdaterTest.java` before fix).
- **Generated API files**: All have headers (auto-generated).
- **Test classes**: Documentation varies—some have JavaDoc, some don't.

### openHAB Guideline References
From https://www.openhab.org/docs/developer/guidelines.html:
- Section A. Directory and File Layout: "Every Java file must have a license header."
- Section C. Documentation: "JavaDoc is required to describe...every class."
- Provided tool: `mvn license:format` to automatically add missing headers.

### What We Fixed
- Added missing license header to `ClientStateUpdaterTest.java`.
- Updated incomplete header in `ClientListUpdaterTest.java`.
- Added/improved class JavaDoc with `@author` tags.

## Tasks for Clarification Session
- [ ] **Confirm**: Are generated code files (API models) exempt from header requirements, or must they be regenerated with proper headers?
- [ ] **Confirm**: Are all test classes required to have class-level JavaDoc with `@author` tags, like main classes?
- [ ] **Clarify**: Should `mvn license:format` be part of routine CI/pre-commit validation for openHAB bindings?
- [ ] **Decide**: Are DTOs (Data Transfer Objects) exempt from JavaDoc as stated in guidelines, or do they need minimal class docs?
- [ ] **Audit scope**: Should a complete binding audit be done (all ~500+ Java files) or just new/modified files?
- [ ] **Instruction update**: Add a specific openHAB binding file header/documentation checklist to `.github/projects/` instructions.

## Proposed Closed Questions (answer with numbers/yes-no)
1. Should **all** Java files in this binding have the EPL-2.0 license header, or are generated files allowed to be auto-generated without manual verification?
2. Should **test files** be treated the same as main source files (JavaDoc + @author required), or can they have minimal/no class documentation?
3. Should DTOs (model classes in api.generated.current.model) have class-level JavaDoc, or are they exempt as "data transfer objects"?
4. Is running `mvn license:format` expected as a mandatory pre-commit step for openHAB bindings, or only when adding new files?
5. Should a full binding audit be conducted now, or only enforce headers going forward on new/modified files?

## Outputs Expected
- Clear, documented requirement for file headers/documentation specific to openHAB bindings.
- Updated copilot instruction file for this project (e.g., `.github/projects/openhab-binding-XX.md` or similar).
- Guidance on handling generated/DTO code.
- Optional: List of all files needing header updates, prioritized by category.
