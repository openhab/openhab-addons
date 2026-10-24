# ADR-002: Keep the All-in-One Add-on Under the ID `modbus`

## Status

`Proposed`

## Context

The change `split-modbus-device-addons` requires that users who relied on the add-on that installs all devices keep their installation working ("Deprecated all-in-one add-on remains available", "Old and new add-ons coexist").

The add-on `modbus` is the feature `openhab-binding-modbus`, referenced by users in `addons.cfg` (`binding = modbus`) or through the UI.
Only one add-on can have the ID `modbus`, and a bundle can carry only one `addon.xml`.

A second version of this decision gave `modbus` to the lean core and moved the aggregate to `modbus-legacy`.
A test on a real instance (upgrade from 5.2 to 5.3 with E3DC things) showed that this is a breaking change: `binding = modbus` then installs only the core, and the E3DC thing has the status `ERROR: HANDLER` until the user installs the device add-on.

## Decision

We will keep the ID `modbus` for the aggregate and give the lean core the ID `modbus-core`:

- The core `addon.xml` has the ID `modbus-core` ("Modbus Core Binding"). Its feature is `openhab-binding-modbus-core` and lists the transport and the core bundle.
- The new source-less bundle `org.openhab.binding.modbus.legacy` carries the aggregate: `addon.xml` (ID `modbus`, name "Modbus Binding (All Devices)"), `feature.xml` (feature `openhab-binding-modbus`) and a `README.md`.
- The aggregate feature lists the core bundle and the 11 device bundles that existed at the time of the change. It is self-contained, because the build verifies every bundle's `feature.xml` on its own. The list is frozen: later devices are not added.
- It replaces the hand-written aggregate in `footer.xml`, which is removed.
- Installations that have `modbus` configured behave as before. There is no breaking change.

Alternatives considered:

| Alternative | Why not |
|---|---|
| Core gets `modbus`, aggregate gets `modbus-legacy` (second version) | Breaking change on update: `binding = modbus` installs only the core. |
| Core keeps `modbus`, a migration installs missing device add-ons at startup | Extra code in the core bundle with a hard-coded mapping from thing type to add-on; dependence on the add-on service being ready at startup; not verified. |
| Add a `bindingId` field to the add-on DTO in openhab-core | Clean for the UI, but needs changes in three repositories and a core release. |

## Consequences

### Positive

- Existing installations keep working after the update without any action.
- The lean installation is available through the device add-ons.
- The aggregate is frozen and can be removed in a later major release.

### Negative

- The Main UI offers "Add Thing" only for an add-on whose ID equals the binding ID `modbus`. With only the core or device add-ons installed, the UI lists no thing types for them. Workaround: open `/settings/things/add/modbus`, or use a `.things` file. A small UI change (treat IDs with the prefix `modbus-` as the binding `modbus`) is a follow-up in openhab-webui.
- One additional bundle without code, with a `pom.xml`, an entry in `bundles/pom.xml` and the BOM, and a code owner.
- The all-in-one entry `modbus` stays visible in the add-on list and can be picked by mistake.
