# ADR-005: MELCloud Home Thing-Type ID/Filename Cleanup, `thingTypeVersion` Removal

## Status

Accepted

## Context

Real-world testing against a live A.C. unit (see ADR-003/ADR-004) confirmed the
MELCloud Home ATA/ATW Things work correctly, prompting a cleanup pass before
they're considered stable:

- The Thing-type IDs introduced in ADR-001–ADR-003
  (`melcloudhomeaccount`/`melcloudhomeataunit`/`melcloudhomeatwunit`) are
  unnecessarily long — the `bindingId="melcloud"` on the `thing-descriptions`
  root already scopes every ID to this binding, so repeating `melcloudhome`
  inside each ID is redundant.
- `melCloudHomeAtaUnit.xml`/`melCloudHomeAtwUnit.xml` declared a
  `thingTypeVersion` property that nothing in the codebase ever reads —
  no migration logic depends on it, so it's dead configuration.
- The three MELCloud Home XML filenames used camelCase
  (`melCloudHomeAccount.xml`, ...), inconsistent with the hyphenated
  filename convention preferred for this binding going forward.

## Decision

- **Thing-type IDs shortened**: `melcloudhomeaccount` → `home-account`,
  `melcloudhomeataunit` → `ata-unit`, `melcloudhomeatwunit` → `atw-unit`.
  Updated in the XML (`id`/`bridge-type-ref` attributes),
  `MelCloudBindingConstants` (the `ThingTypeUID` string values — the Java
  constant names themselves, e.g. `THING_TYPE_MELCLOUD_HOME_ACCOUNT`, are
  unchanged), and Javadoc references in
  `MelCloudHomeUnitDiscoveryService`/`MelCloudHomeAccountConfig`/`MelCloudHomeUnitConfig`.
  The legacy `melcloudaccount`/`acdevice`/`heatpumpdevice` Thing types are
  untouched.
- **`thingTypeVersion` removed** from `ata-unit`/`atw-unit` (it was never
  present on the `home-account` bridge to begin with). The legacy
  `heatpumpdevice` Thing type keeps its own `thingTypeVersion` — out of scope
  here.
- **Filenames hyphenated**, only for the three MELCloud Home files:
  `melCloudHomeAccount.xml` → `mel-cloud-home-account.xml`,
  `melCloudHomeAtaUnit.xml` → `mel-cloud-home-ata-unit.xml`,
  `melCloudHomeAtwUnit.xml` → `mel-cloud-home-atw-unit.xml`. The legacy
  `acDevice.xml`/`heatpumpDevice.xml`/`melCloudAccount.xml` and the shared
  `channels.xml` keep their existing camelCase names.

Historical ADRs (001–004) still reference the old, longer IDs in prose; they
are left as-is as a historical record of the decisions at the time, per this
project's convention of not rewriting past ADRs.

## Consequences

### Positive

- Shorter, less redundant Thing-type IDs (`melcloud:ata-unit:...` instead of
  `melcloud:melcloudhomeataunit:...`).
- No dead `thingTypeVersion` configuration on the two unit Thing types.
- Consistent hyphenated filenames for the MELCloud Home XML descriptors.

### Negative

- **Breaking change for any existing MELCloud Home Things**: openHAB
  identifies a Thing instance by its full `ThingTypeUID`
  (`melcloud:<old-id>:...`), so this ID rename orphans any `home-account`/
  `ata-unit`/`atw-unit` Things a tester has already created — they must be
  deleted and re-added (or manually edited if managed via `.things` files).
  Acceptable here since these Things are still marked experimental/pre-release
  in practice (no released version has shipped with the old IDs), but this
  would not be an acceptable change post-release without a documented
  migration path.
