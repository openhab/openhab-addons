# Test Report: Add-on Packaging

Environment: openHAB 5.3.0 snapshot distribution (build 143) in a throwaway instance, Modbus bundles built from this change, features loaded from the generated feature repository and started through `featuresBoot`.
Evidence: thing types served by the REST API, installed bundle locations, and the log.

## Build

| Step | Result |
|---|---|
| Spotless apply and check, i18n generation, `clean install` for the 13 Modbus modules | Passed |
| Karaf feature verification per bundle | Passed after the legacy feature was made self-contained |
| Features module with the aggregated feature repository, verification of all 13 features | Passed |

## Scenarios

| Scenario | Result | Evidence |
|---|---|---|
| Install the core only | Passed | Thing types: `data`, `poller`, `serial`, `tcp`. Bundles: core. |
| Install a single device (E3DC) | Passed | E3DC thing types plus the core types, no SunSpec types. Bundles: core, e3dc. |
| Install two devices (E3DC, SunSpec) | Passed | Thing types of both. Core installed once. |
| Existing installation with the add-on `modbus` and a configured E3DC thing | Passed | All 13 bundles installed, 37 thing types. The configured bridge and E3DC thing start polling (connection attempts to the unreachable test address in the log). |
| Deprecated add-on and E3DC add-on installed together | Passed | Each bundle installed once, same thing types as the legacy add-on alone. |
| Uninstall of the deprecated add-on | Not tested | Needs the Karaf console, which could not be used in the test environment. |
| Add-on store entries, names and the deprecation text | Not tested | The add-on REST API needs an administrator login. |

## Upgrade from openHAB 5.2.3 to 5.3

Simulated upgrade of an installation that uses the old all-in-one `modbus` add-on.

| Step | Result | Evidence |
| --- | --- | --- |
| Start openHAB 5.2.3 (release) with `binding = modbus` in `conf/services/addons.cfg` and a `.things` file with a Modbus TCP bridge and an E3DC thing | Passed | 31 Modbus thing types. Bundles `modbus` and 10 device bundles at 5.2.3. Connection warnings from the E3DC polling show that the handler runs. |
| Stop 5.2.3, install a fresh 5.3.0 snapshot, carry over `conf` and `userdata` (without cache, tmp and logs), take the 5.3 `userdata/etc` files | Passed | Same `addons.cfg` and `.things` file, no manual change. |
| Start 5.3 | Passed | 37 Modbus thing types (the 31 of 5.2.3 plus the types of the bundles added since). 13 bundles at 5.3.0-SNAPSHOT, including `modbus.legacy`. The E3DC thing polls as before (connection refused to the dummy host). No feature or resolution errors for Modbus. |

Limitations:

- The `runtime/bin/update` script was not used. It downloads a release version, which does not exist for a snapshot. Replacing `userdata/etc` by hand does the same.
- The Thing came from a `.things` file, not from state created in the UI (JSONDB).
- The feature repository was a locally built file, not the published 5.3 repository.
- Errors about unrelated features (for example `binding-knx`) in the log come from the shortened local feature file and are not related to Modbus.

## Upgrade with a real device (Windows, 2026-10-21)

| Step | Result | Evidence |
| --- | --- | --- |
| openHAB 5.2.3 (release) on Windows with the add-on "Modbus Binding" installed in the UI, a Modbus TCP bridge and an E3DC thing created in the UI, thing ONLINE with real values | Passed | Channel values shown, for example household consumption 532 W. |
| Upgrade to 5.3.0-SNAPSHOT (build 5651) with the split Modbus add-ons: `conf` and `userdata` taken over, `userdata/etc` from 5.3, cache cleared | Passed | The stored add-on `modbus` is found and installed (`Modbus manager activated`). The E3DC thing and the bridge go ONLINE again, channels show values. Result confirmed manually by the code owner. |

Notes:

- The first start failed because the test package contained a damaged feature file (test setup error, fixed). Karaf remembers a failed first start in `userdata/cache` and `userdata/tmp`. Clearing both is part of a normal upgrade.
- The upgrade used locally built bundles and a local feature file instead of the published repository.

## Single device on a blank instance (Windows, 2026-10-21)

| Step | Result | Evidence |
| --- | --- | --- |
| Blank openHAB 5.3.0-SNAPSHOT (build 5651), install only "Modbus E3DC Binding" in the add-on store | Passed | The store lists "Modbus E3DC Binding" as its own entry. `bundle:list` shows exactly three Modbus bundles: Modbus Binding (core, start level 80), E3DC Modbus Binding (81) and Modbus Transport. No SunSpec, Sungrow or legacy bundle. |

Open point: for the add-on `modbus` the store showed the name "Modbus Binding" with the old logo, not "Modbus Binding (Deprecated)". Probably the online add-on catalog of the released version is used. To be checked after the first published build.

## Rebuild with changed add-on IDs (2026-10-21, cloud environment)

The Main UI needs the add-on ID to equal the binding ID `modbus` for "Add Thing" (ADR-001, ADR-002). Core is now `modbus` ("Modbus Binding"), the aggregate is `modbus-legacy` ("Modbus All Devices (Deprecated)"). The results above were obtained with the earlier IDs `modbus-core` and `modbus`.

| Scenario | Result | Evidence |
| --- | --- | --- |
| Build of the 13 modules and the features module | Passed | `Features verified: 13, failures: 0`. |
| Core only (`openhab-binding-modbus`) | Passed | Thing types data, poller, serial, tcp. One bundle. |
| E3DC only | Passed | Core types plus e3dc, e3dc-wallbox. Bundles: core, e3dc. |
| E3DC and SunSpec | Passed | Types of both. Core once. |
| Legacy (`openhab-binding-modbus-legacy`) with an E3DC things file | Passed | 13 bundles, E3DC handler active (connection refused to the dummy host). |
| Lean core with an E3DC things file (state after an update from the former add-on `modbus`) | Passed | Only the core bundle, no E3DC thing types. The thing waits for the device add-on. This is the documented breaking change. |
| "Add Thing" through the entry "Modbus Binding" in the Main UI | Not tested yet | To be done on the Windows instance with the package v2. |

## Real instance with the final IDs (option A, ADR-002)

Environment: Windows, openHAB 5.3.0-SNAPSHOT build 146 with the local Modbus bundles, real E3DC.

| Test | Result |
|---|---|
| Upgrade from 5.2 (add-on `modbus`, Modbus TCP Slave and E3DC things online) to 5.3, no user action | Passed. The E3DC thing delivers values; no `ERROR: HANDLER`. |
| Blank 5.3, only "Modbus E3DC Binding" installed | Passed. `bundle:list` shows 3 Modbus bundles: core (80), E3DC (81), transport (80). |
| Cloud scenarios: core only, E3DC, E3DC + SunSpec, `modbus` with things file, `modbus-core` with things file | Passed. 1, 2, 3, 13 and 1 Modbus bundles. |

Not tested: "Add Thing" through the device entries (needs the Main UI follow-up, task 7.4), uninstall of `modbus` through the UI.
