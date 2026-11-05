# Proposal: Split Modbus Into a Core Add-on and One Add-on per Device

## Intent

Installing the Modbus binding in the openHAB UI currently installs the core and every device extension (E3DC, SunSpec, Sungrow, ...).
Most users run one or two devices and receive the thing types, documentation and bundles of all others.
Users shall be able to install the Modbus core alone, or the core plus only the devices they own.
Existing installations must keep working without any configuration change.

## Scope

In scope:

- A lean `Modbus Core` add-on with bridges, poller and the generic data thing.
- One add-on entry per existing device bundle (11 bundles), each pulling in the core automatically.
- The former all-in-one add-on keeps the ID `modbus`, frozen in content, so existing installations do not break. The lean core gets the ID `modbus-core`.
- Wiring in the distribution feature build so the new features are part of the add-on feature repository.
- README updates describing the new installation options.

Out of scope:

- Any Java, thing type, channel or configuration change.
- Renaming bundles, artifact IDs or the shared `modbus` binding ID used by all thing types.
- The `modbus.hager` bundle: it currently contains no sources, no README and no resources, and is not part of the build.
- A catalogue-driven generic device binding (see ADR-001, alternatives).
- Removing the all-in-one add-on `modbus` (separate change).

## Open Questions

- How should the i18n generator be handled? Add-on IDs differ from the binding ID `modbus`, so the generated thing type keys do not match the runtime keys (see ADR-001).
- Does the build and the add-on store accept a source-less bundle that only carries an `addon.xml` and a `feature.xml` (needed to keep the deprecated `modbus` entry)? To be verified by the first build.
- Who is the code owner of the new `org.openhab.binding.modbus.legacy` bundle in `CODEOWNERS`?
- Does the documentation generator behind `<!--list-subs-->` in the core README list the new bundle? Needs a check in the openhab-docs pipeline.
- For an upstream pull request: do the openhab-addons maintainers accept Modbus deviating from the aggregation used by Bluetooth and MQTT?
- Which release removes the all-in-one add-on `modbus`?
