# HassLink Entity & Device Filtering Specification

This document defines the configuration parameters, evaluation model, and execution rules for entity and device filtering within the `hasslink` openHAB binding.

## 1. Architectural Model & Filter Logic

Filtering in `hasslink` operates as a **Sequential Pipeline** using **Composite AND** logic across criteria levels, with a top-priority override for explicitly linked entities.

### 1.1 The Composite AND Logic

When multiple filtering criteria (such as `includedEntities`, `includedDomains`, and `includedLabels`) are configured on a Thing, **all active inclusion criteria must evaluate to `true`** for a candidate entity to be accepted.

Specifically:

- **Exclusions (`excluded*`) act as immediate short-circuit rejections.** If an entity matches _any_ active exclusion rule (`excludedEntities`, `excludedDomains`, or `excludedLabels`), it is immediately rejected (`false`).
- **Inclusions (`included*`) act as a composite allowlist.** If an inclusion filter category is active (non-empty), candidate entities must match that filter.
  If multiple inclusion filters are active (e.g., both `includedEntities` AND `includedDomains`), an entity must satisfy **ALL** of them to pass.

## 2. Evaluation Flow (`isEntityAllowedForDevice`)

Candidate entities belonging to a Home Assistant Device are evaluated step-by-step in the following exact sequence:

1. **Explicit Thing Entity Match (`config.entityIds`)**: Checks if the entity is explicitly defined in `entityIds`.
   If yes, it is **allowed immediately** (returning `true`), bypassing all other rules.
1. **Explicit Entity Exclusion (`config.excludedEntities`)**: Checks if the entity matches any glob or string in `excludedEntities`.
   If yes, it is **rejected immediately** (returning `false`).
1. **Explicit Entity Inclusion (`config.includedEntities`)**: Checks if the `includedEntities` filter is active (non-blank).
   If active and the entity does not match, it is **rejected** (returning `false`); otherwise, it passes to the next step.
1. **Domain Filtering (`config.excludedDomains` / `config.includedDomains`)**: Checks if the entity domain matches `excludedDomains` (rejection) or if `includedDomains` is active and the domain does not match (rejection); otherwise, it passes to the next step.
1. **Label Filtering (`config.excludedLabels` / `config.includedLabels`)**: Checks if any entity or device label matches `excludedLabels` (rejection), or if `includedLabels` is active and no labels match (rejection).
   If all checks pass, the entity is **allowed** (returning `true`).

## 3. Top Priority Override: `config.entityIds`

The `entityIds` configuration array on a `device` Thing represents an explicit user request to link specific Home Assistant entities to openHAB channels.

- Entities listed in `entityIds` **bypass all device exclusions and filter rules** (`excludedEntities`, `excludedDomains`, `includedDomains`, `excludedLabels`, `includedLabels`).
- Execution returns `true` immediately at **Step 1**, ensuring explicitly defined items are never filtered out.

## 4. Wildcard Glob Matching & Whitespace Handling

### 4.1 Wildcard Glob Patterns

All entity, domain, area, and label filter options support standard wildcard glob syntax:

- `*` matches zero or more characters (e.g., `sensor.*_temperature`, `*_version`, `*_mac`).
- `?` matches exactly one character (e.g., `switch.plug_?`).
- String comparison is case-insensitive.

### 4.2 Blank and Whitespace Filter Normalization

UI configurations and text files can sometimes supply empty strings or lists containing whitespace elements (e.g. `[""]` or `["   "]`).

- Filter lists containing only empty or whitespace-only elements are recognized as **inactive filters** (`hasActiveFilter = false`).
- Inactive filters are skipped during evaluation and will not cause blanket rejections of all incoming entities.

## 5. Summary Matrix & Examples

| Scenario                            | Config Setup                                                                   | Candidate Entity          | Result       | Reason                                                                          |
|:------------------------------------|:-------------------------------------------------------------------------------|:--------------------------|:-------------|:--------------------------------------------------------------------------------|
| **Explicit Override**               | `entityIds = ["sensor.attic_temp"]`<br>`excludedDomains = ["sensor"]`          | `sensor.attic_temp`       | **ALLOWED**  | Step 1 match; bypasses domain exclusion.                                        |
| **Excluded Entity Glob**            | `excludedEntities = ["*_version"]`                                             | `sensor.mower_fw_version` | **REJECTED** | Step 2 match against wildcard pattern.                                          |
| **Composite Inclusions (Match)**    | `includedEntities = ["sensor.livingroom_*"]`<br>`includedDomains = ["sensor"]` | `sensor.livingroom_temp`  | **ALLOWED**  | Matches both `includedEntities` AND `includedDomains`.                          |
| **Composite Inclusions (Mismatch)** | `includedEntities = ["sensor.livingroom_*"]`<br>`includedDomains = ["switch"]` | `sensor.livingroom_temp`  | **REJECTED** | Matches `includedEntities` but fails `includedDomains` (Composite AND failure). |
| **Domain Exclusion**                | `excludedDomains = ["update", "button"]`                                       | `update.firmware`         | **REJECTED** | Step 4 match against `excludedDomains`.                                         |
