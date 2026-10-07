# Legacy Poller Migration

`poller2` owns channels on its poller bridge. Migrating the legacy `poller` + `data` topology is opt-in and review-first. The legacy configuration remains supported until you explicitly remove it.

> The commands below are openHAB console commands, not Main UI controls. This documentation does not claim a Main UI migration wizard or automated screenshot coverage.

## Before you start

1. Back up or export the current Thing and Item/link configuration using your normal openHAB backup process. Retain a copy outside the runtime.
1. Record the current legacy Thing UIDs, linked Items, values, and any known device commands so you can compare them after migration.
1. Read the [legacy-only reference](legacy-poller.md) and identify custom `readTransform`, all write configuration, and JSON-producing write transforms. The migration deliberately leaves these for manual work.

## Preview and review

At the openHAB console, run:

```text
openhab> modbus migrate preview
```

The command prints a preview identity, proposed YAML, and `MANUAL:` notices. It only reads the legacy configuration; it does not create, delete, or relink anything. Save the complete console output with the backup. Review every target poller, channel, and replacement link, and resolve every `MANUAL:` entry yourself. Do not change the legacy source configuration between preview and apply.

## Apply

Apply exactly the identity returned by the reviewed preview:

```text
openhab> modbus migrate apply <preview-identity>
```

Apply creates the reviewed managed target Things/channels and replacement links. It does **not** delete legacy Things or legacy links. It can refuse an unknown or stale identity; when that happens, generate a new preview and repeat review rather than substituting an identity.

## Verify live operation

With both configurations still available:

1. Confirm each new `poller2` Thing is online and has the expected channels.
1. Compare representative live values with the retained legacy Item values, including address boundaries and byte/word order where applicable.
1. For commandable migrated channels, perform a controlled command and verify the physical device and the next poll result. Do not assume action acceptance means that a device write succeeded.
1. Check logs and Thing status for configuration or communication errors before considering the migration accepted.

## Roll back or clean up

If verification fails, remove only the generated migration targets and replacement links:

```text
openhab> modbus migrate rollback <preview-identity>
```

Rollback leaves every legacy Thing and link untouched.

There is no `cleanupLegacy` console command. After successful live verification, clean up legacy Things and links separately through your normal managed-configuration process, using the backup to restore them if needed. Treat cleanup as a separate, irreversible change: export again immediately before it, remove only the legacy resources you reviewed, then verify the surviving `poller2` configuration once more.

## Scope and limitations

The migration plans safe legacy read topology only. It does not automatically convert custom read transforms, write configuration, or JSON-producing write transforms; those appear as manual work and are excluded from generated YAML and apply. It never removes legacy resources during preview or apply.
