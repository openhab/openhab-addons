# Tasks: Split Modbus Into a Core Add-on and One Add-on per Device

## 1. Device bundles

- [x] 1.1 Add `src/main/feature/feature.xml` to each of the 11 device bundles (ankersolix, e3dc, foxinverter, helioseasycontrols, kermi, lambda, sbc, stiebeleltron, studer, sungrow, sunspec).
- [x] 1.2 Add `src/main/resources/OH-INF/addon/addon.xml` to each of the 11 device bundles with its own ID, name, description and keywords.

## 2. Core bundle

- [x] 2.1 Add `src/main/feature/feature.xml` with the feature `openhab-binding-modbus` (lean core).
- [x] 2.2 Change `addon.xml` to ID `modbus`, name `Modbus Binding`, and remove the device keywords. (First ID `modbus-core`, changed in 7.1.)

## 3. Deprecated aggregate

- [x] 3.1 Add (without `pom.xml`, see 4.1) the bundle `org.openhab.binding.modbus.legacy` with `addon.xml` (ID `modbus-legacy`, marked deprecated) and `feature.xml` (self-contained feature `openhab-binding-modbus-legacy` listing the core and the 11 device bundles). (First ID `modbus`, changed in 7.1.)
- [x] 3.2 Add a README for the legacy bundle pointing to the individual add-ons.

## 4. Build wiring (needs approval, see `pom-proposal.md`)

- [x] 4.1 Add the `pom.xml` of the legacy bundle.
- [x] 4.2 Register the legacy bundle in `bundles/pom.xml` and `bom/openhab-addons/pom.xml`.
- [x] 4.3 Remove the `modbus*` exclusion in `features/openhab-addons/pom.xml`.
- [x] 4.4 Remove the hand-written `openhab-binding-modbus` block in `features/openhab-addons/src/main/resources/footer.xml`.

## 5. Documentation

- [x] 5.1 Update the core README with an installation section.
- [x] 5.2 Update device READMEs that describe the installation prerequisite.

## 6. Verification

- [x] 6.1 Every `feature.xml` and `addon.xml` is well-formed. Schema validation happens in the build.
- [ ] 6.2 Run the runtime scenarios of `specs/addon-packaging/spec.md` on a test distribution (install core only, single device, two devices, upgrade with `modbus`, coexistence, uninstall of `modbus`). Done for installation, see `test-report.md`; not covered: uninstall of `modbus`, add-on store entries.
- [x] 6.3 Build with Spotless, i18n generation and `clean install` for the 13 modules and the features module. The generated i18n files are not committed, see ADR-001.

## 7. Add-on IDs (found in the tests on a real instance)

- [x] 7.1 First change: core gets `modbus`, aggregate `modbus-legacy`. Replaced by 7.2 because it breaks existing installations.
- [x] 7.2 Final IDs (ADR-002): the aggregate keeps `modbus` ("Modbus Binding (All Devices)", feature `openhab-binding-modbus`), the lean core gets `modbus-core` ("Modbus Core Binding", feature `openhab-binding-modbus-core`). ADR-001, ADR-002, spec, proposal and READMEs updated; features rebuilt and verified (13).
- [ ] 7.3 Test on a real instance: upgrade from openHAB 5.2 with `modbus` (E3DC online without action), blank instance with only the E3DC add-on.
- [ ] 7.4 Follow-up outside this repository: make the Main UI map the IDs `modbus-*` to the binding `modbus` for "Add Thing".
