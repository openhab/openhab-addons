# Legacy Poller Migration

`poller2` owns channels on the poller bridge. Migrating the legacy `poller` + `data` topology is opt-in and is intentionally a review-first workflow.

## Safety contract

1. Run a **preview**. It only reads legacy configuration and returns a stable preview identity, reviewable YAML, and a JSON manifest of every source Thing/channel/link and its proposed target.
2. Review the YAML and manifest. Do not alter legacy Things between preview and apply.
3. Call **apply** with exactly the preview identity. Apply creates target poller/channel/link configuration only. It does **not** remove legacy Things or links.
4. Verify operation with the retained legacy configuration still available for comparison.
5. Either call **rollback** to remove only target configuration created by that preview, or separately call **cleanupLegacy** after accepting the migration. Cleanup is intentionally not part of apply.

The binding never converts a custom `readTransform`, any legacy write configuration, or a JSON-producing JSON write transform. Such sources are emitted in `manualWork` with a reason and are excluded from generated YAML and apply.

## Java service API

The public `org.openhab.binding.modbus.migration.ModbusMigrationWorkflow` is the binding-facing workflow. A caller supplies a `MigrationApplier` backed by its managed Thing and link provider:

```java
MigrationPreview preview = workflow.preview(legacyPollers, legacyData);
String yamlForReview = preview.yaml();
String manifestForAudit = preview.manifestJson();

workflow.apply(preview.identity(), legacyPollers, legacyData);
// legacy topology remains unchanged here
workflow.rollback(preview.identity());       // remove only generated targets
// or, after verification:
workflow.cleanupLegacy(preview.identity());  // explicit, irreversible legacy cleanup
```

`apply` recomputes the preview from the supplied source snapshot and rejects an identity that is unknown or no longer current. A rollback removes targets only; it never reconstructs or modifies legacy configuration. Cleanup is rejected unless the matching preview is currently applied.

The migration API is intentionally independent of poller2 write support. It plans only safe legacy read topology and makes writes/manual transformations visible for manual migration.
