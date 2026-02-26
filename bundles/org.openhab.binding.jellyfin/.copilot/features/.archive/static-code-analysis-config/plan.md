# Implementation Plan: Static Code Analysis Configuration

**Feature**: `static-code-analysis-config`
**Created**: 2026-01-03
**Status**: Analysis Phase

## Objective

Analyze and configure static code analysis (checkstyle, spotbugs, PMD via SAT plugin) to exclude generated code in the Jellyfin binding, resolving build warnings/errors from generated API client code.

## Scope

1. Identify all static analysis tools used by openHAB addons project
2. Locate generated code directories/files in Jellyfin binding
3. Determine exclusion mechanisms for each tool
4. Implement exclusions at binding level (POM configuration)
5. Document any limitations requiring parent-level changes

## Tasks

- [x] Build binding with full analysis to identify errors
- [x] Identify SAT plugin and its configuration
- [x] Locate generated code paths
- [x] Analyze exclusion options
- [ ] Create suppression/exclusion configuration
- [ ] Test configuration with full build
- [ ] Document results and recommendations

## Generated Code Location

- **Path**: `src/main/java/org/openhab/binding/jellyfin/internal/api/generated/`
- **Marker**: `@jakarta.annotation.Generated` annotation
- **Generator**: OpenAPI Generator (openapi-tools/openapi-generator-cli)

## Analysis Tools Used

- **SAT Plugin** (org.openhab.tools.sat:sat-plugin:0.17.0)
  - Checkstyle
  - PMD
  - Spotbugs

## Configuration Files (Repository Root)

- Checkstyle: `tools/static-code-analysis/checkstyle/suppressions.xml`
- Spotbugs: `tools/static-code-analysis/spotbugs/suppressions.xml`
- PMD: (TBD - need to check if separate suppression file exists)

## Next Steps

1. Check if binding-level suppression files are possible
2. Test adding exclusions to binding POM
3. Document findings in analysis report
