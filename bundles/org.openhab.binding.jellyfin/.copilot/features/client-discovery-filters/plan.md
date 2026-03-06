# Plan: Client Discovery Filters

**Feature**: `client-discovery-filters`
**Date**: 2026-03-06
**Status**: in-progress

## Objective

Add 7 boolean configuration parameters to the Jellyfin server bridge thing that enable/disable
discovery of Jellyfin clients by category. Also remove the dead `clientActiveWithInSeconds` field
that was declared but never evaluated.

## Background

Jellyfin client types are free-form strings (e.g., `"Jellyfin Web"`, `"Jellyfin for Android TV"`).
There is no server-side enum. Filtering is implemented by matching `session.getClient()` substrings
case-insensitively. Web UI defaults to `false`; all other categories default to `true`.

## Scope

### Phase 0 — Dead Code Removal

Remove `clientActiveWithInSeconds` (declared, exposed in UI, documented, but never evaluated):

- `Configuration.java` — Remove field + JavaDoc
- `server-bridge-type.xml` — Remove `<parameter name="clientActiveWithInSeconds">`
- `ServerHandler.java` — Remove two assignments (L647 + L658)
- `SystemInfoConfigurationExtractor.java` — Remove copy line
- `UriConfigurationExtractor.java` — Remove copy line
- `jellyfin.properties` — Remove label + description lines
- `jellyfin_it.properties` — Remove label + description lines
- `ConfigurationUpdateTest.java` — Remove setUp() usage + test assertions
- `ConfigurationManagerTest.java` — Remove copy line from custom extractor test
- `README.md` — Remove parameter row + example usage

### Phase 1 — Configuration

- `Configuration.java` — Add 7 boolean fields with JavaDoc
- `server-bridge-type.xml` — Add `<parameter-group>` + 7 `<parameter>` elements

### Phase 2 — Constants

- `Constants.java` — Add 9 lowercase client name substring constants

### Phase 3 — Filter Logic

- `ClientDiscoveryService.java` — Add `isClientCategoryEnabled()` helper and filter in second pass

### Phase 4 — Tests

- `ClientDiscoveryServiceTest.java` — Fix `setUp()` to mock `getConfiguration()`; add 13 new filter tests

### Phase 5 — Documentation

- `README.md` — Add "Advanced: Client Discovery Filters" section

### Phase 6 — Build Validation

- `mvn spotless:apply` then `mvn clean install`

## Known Client Name Patterns

| Category     | Reported Client Names                                          | Default  |
|-------------|----------------------------------------------------------------|----------|
| Web          | `Jellyfin Web`                                                 | `false`  |
| Android      | `Jellyfin for Android`                                         | `true`   |
| Android TV   | `Jellyfin for Android TV`                                      | `true`   |
| iOS          | `Jellyfin iOS`, `Swiftfin`, `Infuse`                           | `true`   |
| Kodi         | `JellyCon`, `Jellyfin for Kodi`                                | `true`   |
| Roku         | `Jellyfin for Roku`                                            | `true`   |
| Other        | anything not matched by above                                  | `true`   |

## Key Decisions

- Filter location: `ClientDiscoveryService.discoverClients()` (discovery concern, not session management)
- Config access: `thingHandler.getThing().getConfiguration().as(Configuration.class)`
- Filter applied in second pass (after deduplication, before `thingDiscovered()`)
- Android TV must be checked before Android (substring containment ordering)
- Null/blank client name falls back to "Other" category
