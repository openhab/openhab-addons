# AGENTS.md - TimescaleDB Persistence Development Guide

## Context

You are working on a **native TimescaleDB persistence service** for openHAB.
TimescaleDB is a time-series extension for PostgreSQL — all standard PostgreSQL JDBC drivers work, but the schema and queries use TimescaleDB-specific features.

**openHAB has no built-in downsampling/aggregation framework.** `FilterCriteria`, `PersistenceStrategy` and `PersistenceItemConfiguration` contain no aggregation concepts. Everything must be implemented inside this service.

---

## Architecture

### Key Classes

| Class | Role |
|---|---|
| `TimescaleDBPersistenceService` | Main OSGi service, implements `ModifiablePersistenceService` |
| `TimescaleDBMapper` | `State` ↔ SQL value conversion (all openHAB item types) |
| `TimescaleDBSchema` | Schema creation and migration on startup |
| `TimescaleDBQuery` | SQL query builder for all persistence operations |
| `TimescaleDBMetadataService` | Reads per-item downsampling config from `MetadataRegistry` |
| `TimescaleDBDownsampleJob` | Scheduled daily job: aggregates + deletes raw rows in-place |

### OSGi Service Registration

- Service ID: `timescaledb`
- Implements: `ModifiablePersistenceService` (= `QueryablePersistenceService` + `remove()`)
- Config PID: `org.openhab.persistence.timescaledb`
- `@ConfigurableService` present → visible in mainUI under Settings → Other Services
- Config description: `OH-INF/config/timescaledb.xml`
- `ConfigurationPolicy.REQUIRE` — service does not start without configuration
- Scheduler: `ThreadPoolManager.getScheduledPool("timescaledb")` (shared pool — never call `shutdownNow()`)
- Deactivate: `ScheduledFuture.cancel(false)`, then `HikariDataSource.close()`
- State indicator: `dataSource != null` — no `initialized` boolean

### Dependencies

- JDBC Driver: `org.postgresql:postgresql`
- Connection pooling: HikariCP (already used in other openHAB bundles)
- openHAB Core: `org.openhab.core.persistence`, `org.openhab.core.items` (for `MetadataRegistry`)

---

## Database Schema

```sql
CREATE TABLE item_meta (
    id         SERIAL PRIMARY KEY,
    name       TEXT NOT NULL UNIQUE,
    label      TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE items (
    time        TIMESTAMPTZ      NOT NULL,
    item_id     INTEGER          NOT NULL REFERENCES item_meta(id),
    value       DOUBLE PRECISION,
    string      TEXT,
    unit        TEXT,                          -- stored per row, NOT in item_meta
    downsampled BOOLEAN          NOT NULL DEFAULT FALSE
);

SELECT create_hypertable('items', 'time');
CREATE INDEX ON items (item_id, time DESC);
```

### Why `unit` is per row, not in `item_meta`

A `QuantityType` unit can change over time (sensor reconfiguration, firmware update, etc.). Storing it in `item_meta` would corrupt historical reads. The unit is stored with each measurement and read back from the row when reconstructing `QuantityType` states.

### Why downsampling is in-place (same hypertable)

openHAB reads persisted data directly from the hypertable via `QueryablePersistenceService`. If aggregated data lived in separate views or tables, openHAB would not see it without query-layer changes. In-place replacement (delete raw rows → insert aggregated rows with `downsampled=TRUE`) keeps a single source of truth that openHAB reads transparently.

---

## State Type Mapping

All openHAB item types are fully supported. `TimescaleDBMapper` handles the conversion in both directions.

### Store direction (`toRow`)

| State type | `value` column | `string` column | `unit` column |
|---|---|---|---|
| `QuantityType` | numeric | null | unit string (e.g. `"°C"`) |
| `DecimalType` | numeric | null | null |
| `OnOffType` | `ON=1.0 / OFF=0.0` | null | null |
| `OpenClosedType` | `OPEN=1.0 / CLOSED=0.0` | null | null |
| `PercentType` | 0.0–100.0 | null | null |
| `UpDownType` | `UP=0.0 / DOWN=1.0` | null | null |
| `HSBType` | null | `"H,S,B"` | null |
| `DateTimeType` | null | ISO-8601 string | null |
| `PointType` | null | `"lat,lon[,alt]"` | null |
| `PlayPauseType` | null | enum name (`"PLAY"`, `"PAUSE"`, …) | null |
| `StringListType` | null | comma-separated values | null |
| `RawType` | null | Base64-encoded bytes | MIME type |
| `StringType` | null | raw string | null |

### Load direction (`toState`)

`GroupItem` is unwrapped to its base item before dispatch. Item type determines how the row is interpreted:

- `ColorItem` → `HSBType` (parsed from `string`)
- `DateTimeItem` → `DateTimeType` (parsed from `string`)
- `LocationItem` → `PointType` (parsed from `string`)
- `PlayerItem` → `PlayPauseType` (parsed from `string`)
- `CallItem` → `StringListType` (parsed from `string`)
- `ImageItem` → `RawType` (Base64-decoded from `string`, MIME type from `unit`)
- `DimmerItem` / `RollershutterItem` → `PercentType` (**must be checked before `SwitchItem`**)
- `SwitchItem` → `OnOffType`
- `ContactItem` → `OpenClosedType`
- `NumberItem` with `unit != null` → `QuantityType`
- `NumberItem` without unit → `DecimalType`
- anything else with `string` → `StringType`

**Critical instanceof ordering in `toRow()`:** `HSBType` before `PercentType` before `DecimalType`
(because `HSBType extends PercentType extends DecimalType`).

---

## Per-Item Downsampling via Item Metadata

### How to read metadata (same pattern as InfluxDB persistence)

```java
@Reference
private MetadataRegistry metadataRegistry;

private Optional<Metadata> getItemMetadata(String itemName) {
    MetadataKey key = new MetadataKey("timescaledb", itemName);
    return Optional.ofNullable(metadataRegistry.get(key));
}
```

`Metadata` has:
- `getValue()` → main value string, e.g. `"AVG"`, `"MAX"`, `"MIN"`, `"SUM"`, or `""` (no aggregation)
- `getConfiguration()` → `Map<String, Object>` with keys like `"downsampleInterval"`, `"retainRawDays"`, `"retentionDays"`

### Metadata format (configured by users in .items files)

```java
Number:Temperature MySensor {
    timescaledb="AVG" [ downsampleInterval="1h", retainRawDays="5", retentionDays="365" ]
}
```

### Parsing the metadata

```java
public record DownsampleConfig(
    AggregationFunction function,   // AVG / MAX / MIN / SUM
    Duration interval,              // e.g. Duration.ofHours(1)
    int retainRawDays,              // default 5
    int retentionDays               // default 0 = disabled
) {}

public enum AggregationFunction { AVG, MAX, MIN, SUM }
```

Interval parsing — **validate against an allowlist** (used in SQL string formatting):

| Metadata value | SQL interval literal |
|---|---|
| `1m` | `1 minute` |
| `5m` | `5 minutes` |
| `15m` | `15 minutes` |
| `30m` | `30 minutes` |
| `1h` | `1 hour` |
| `6h` | `6 hours` |
| `1d` | `1 day` |

Throw `IllegalArgumentException` for any value not in this list to prevent SQL injection.

---

## Downsampling Job (`TimescaleDBDownsampleJob`)

Runs daily (e.g. via `@Scheduled` or openHAB's `CronScheduler`). For each item that has `timescaledb` metadata with a non-empty aggregation function:

```sql
-- Step 1: aggregate raw rows older than retainRawDays into buckets
INSERT INTO items (time, item_id, value, unit, downsampled)
SELECT
    time_bucket('<interval>', time)  AS time,
    item_id,
    <AGG_FN>(value)                 AS value,
    last(unit, time)                AS unit,   -- keep most recent unit in bucket
    TRUE                            AS downsampled
FROM items
WHERE item_id = ?
  AND downsampled = FALSE
  AND time < NOW() - INTERVAL '<retainRawDays> days'
GROUP BY time_bucket('<interval>', time), item_id
ON CONFLICT DO NOTHING;

-- Step 2: delete replaced raw rows
DELETE FROM items
WHERE item_id = ?
  AND downsampled = FALSE
  AND time < NOW() - INTERVAL '<retainRawDays> days';

-- Step 3 (if retentionDays > 0): drop everything older than retention window
DELETE FROM items
WHERE item_id = ?
  AND time < NOW() - INTERVAL '<retentionDays> days';
```

**Important:**
- `<interval>`, `<AGG_FN>`, `<retainRawDays>`, `<retentionDays>` are formatted into the SQL string — **never from user input directly**. Validate interval against allowlist, validate function against enum. Use `?` for `item_id`.
- `last(unit, time)` is a TimescaleDB hyperfuction — verify it is available, otherwise use `MAX(unit)` as fallback.
- Run steps 1+2 in a transaction per item to avoid partial state.
- Log errors per item and continue (don't abort the entire job on a single-item failure).

---

## Query Implementation

- All item name / date / state lookups use JDBC `PreparedStatement` — no string concatenation for user-controlled values.
- `time_bucket()` interval is formatted as a string but validated against the allowlist above.
- `historicState`: `WHERE item_id=? AND time <= ? ORDER BY time DESC LIMIT 1`
- `getAllStatesBetween`: `WHERE item_id=? AND time BETWEEN ? AND ? ORDER BY time ASC` — returns both raw and downsampled rows.
- Aggregate queries (`averageSince`, `minSince`, etc.): `WHERE item_id=? AND time >= ?` — operate on all rows including downsampled ones, which is correct.

---

## item_id Caching

Cache `name → item_id` in a `ConcurrentHashMap` to avoid a SELECT on every `store()` call. Invalidate on service restart. Auto-insert into `item_meta` on first `store()` if the item is unknown.

```java
private final Map<String, Integer> itemIdCache = new ConcurrentHashMap<>();

private int getOrCreateItemId(String name, @Nullable String label) {
    return itemIdCache.computeIfAbsent(name, n -> fetchOrInsertItemMeta(n, label));
}
```

---

## Testing

### Unit Tests (no DB required)

Location: `src/test/java/org/openhab/persistence/timescaledb/internal/`

- `TimescaleDBMapperTest` — State ↔ SQL value round-trips for all state types
- `TimescaleDBMetadataServiceTest` — parsing of metadata values and config keys
- `TimescaleDBDownsampleJobTest` — SQL generation for aggregation/delete, interval allowlist validation

### Integration Tests (requires Docker + TimescaleDB)

Use Testcontainers:

```java
@Container
static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("timescale/timescaledb:latest-pg16")
    .withDatabaseName("openhab_test")
    .withUsername("openhab")
    .withPassword("openhab");
```

Test: schema creation, store/query round-trips, downsampling job result, compression policy creation.

---

## Common Pitfalls

1. **TimescaleDB extension not installed**: check on startup with `SELECT extname FROM pg_extension WHERE extname='timescaledb'`, fail with a clear error if missing.
2. **`last()` availability**: `last(unit, time)` requires the TimescaleDB Toolkit — check availability, fall back to `MAX(unit)` otherwise.
3. **Compression + INSERT conflict**: compressed chunks are read-only. The downsampling INSERT must target the uncompressed region (data newer than `compressionAfterDays`). Ensure `retainRawDays < compressionAfterDays`.
4. **Interval allowlist is mandatory**: `time_bucket('1h', time)` is dynamically formatted — any non-allowlisted value must throw before it reaches SQL.
5. **`ON CONFLICT DO NOTHING`** on the aggregation INSERT: the job may run twice if interrupted; duplicate bucket rows must be prevented.
6. **`QuantityType` unit changes**: never update `item_meta` with a unit — the unit lives on each row. On read, take the `unit` value from the row.

---

## Relevant openHAB Core APIs

- `org.openhab.core.persistence.QueryablePersistenceService` — implement this
- `org.openhab.core.persistence.FilterCriteria` — query parameters passed to `query()`
- `org.openhab.core.items.MetadataRegistry` — OSGi service, inject via `@Reference`
- `org.openhab.core.items.Metadata` — `getValue()` + `getConfiguration()` for per-item config
- `org.openhab.core.items.MetadataKey` — constructed as `new MetadataKey("timescaledb", itemName)`
- `org.openhab.core.library.types.*` — `QuantityType`, `DecimalType`, `OnOffType`, etc.

## References

- [TimescaleDB docs](https://docs.timescale.com/)
- [time_bucket()](https://docs.timescale.com/api/latest/hyperfunctions/time_bucket/)
- [last()](https://docs.timescale.com/api/latest/hyperfunctions/last/)
- [Compression](https://docs.timescale.com/use-timescale/latest/compression/)
- InfluxDB persistence (metadata pattern): `bundles/org.openhab.persistence.influxdb/src/main/java/.../InfluxDBMetadataService.java`
- Existing downsampling logic (Python/MongoDB): `DOWNSAMPLE_IMPLEMENTATION_GUIDE.md` in this bundle
