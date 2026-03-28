# TimescaleDB Persistence

This service persists and queries openHAB item states using [TimescaleDB](https://www.timescale.com/), a time-series database built on PostgreSQL.

Unlike the generic JDBC persistence (which can also connect to TimescaleDB via the PostgreSQL driver), this service is purpose-built for TimescaleDB and leverages its native time-series features:

- **Hypertables** for automatic time-based partitioning and fast range queries
- **In-place downsampling** — raw data is aggregated and replaced in the hypertable directly, so openHAB reads aggregated data transparently without any schema changes
- **Per-item downsampling config** via item metadata (namespace `timescaledb`)
- **Compression Policies** to automatically compress older data and reduce storage
- **Retention Policies** to automatically drop data older than a configured threshold

## Prerequisites

- TimescaleDB 2.x installed and running (as a PostgreSQL extension)
- A database and user created for openHAB

```sql
CREATE DATABASE openhab;
CREATE USER openhab WITH PASSWORD 'openhab';
GRANT ALL PRIVILEGES ON DATABASE openhab TO openhab;

-- Connect to openhab database, then:
CREATE EXTENSION IF NOT EXISTS timescaledb;
```

## Database Schema

The service **creates all tables automatically on startup** — no manual DDL required.
Item states are stored in a single hypertable `items` (columns: `time`, `item_id`, `value`, `string`, `unit`, `downsampled`) and a name-lookup table `item_meta` (columns: `id`, `name`, `label`, `value`, `metadata`).

## State Type Mapping

| openHAB Type      | `value` column                 | `string` column | `unit` column          |
|-------------------|--------------------------------|-----------------|------------------------|
| `DecimalType`     | numeric value                  | —               | —                      |
| `QuantityType`    | numeric value (stripped)       | —               | unit string, e.g. `°C` |
| `OnOffType`       | `1.0` (ON) / `0.0` (OFF)       | —               | —                      |
| `OpenClosedType`  | `1.0` (OPEN) / `0.0` (CLOSED)  | —               | —                      |
| `PercentType`     | `0.0`–`100.0`                  | —               | —                      |
| `UpDownType`      | `0.0` (UP) / `1.0` (DOWN)      | —               | —                      |
| `HSBType`         | —                              | `H,S,B`         | —                      |
| `DateTimeType`    | —                              | ISO-8601        | —                      |
| `StringType`      | —                              | raw string      | —                      |

## Configuration

Configure via `$OPENHAB_CONF/services/timescaledb.cfg` or in the UI under `Settings → Add-ons → TimescaleDB → Configure`.

| Property               | Default   | Required | Description                                               |
|------------------------|-----------|:--------:|-----------------------------------------------------------|
| `url`                  |           | Yes      | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/openhab` |
| `user`                 | `openhab` | No       | Database user                                             |
| `password`             |           | Yes      | Database password                                         |
| `chunkInterval`        | `7 days`  | No       | TimescaleDB chunk interval for the hypertable             |
| `retentionDays`        | `0`       | No       | Drop data older than N days. `0` = disabled               |
| `compressionAfterDays` | `0`       | No       | Compress chunks older than N days. `0` = disabled         |
| `maxConnections`       | `5`       | No       | Maximum DB connections in the pool                        |
| `connectTimeout`       | `5000`    | No       | Connection timeout in milliseconds                        |

## Persistence Configuration

All item- and event-related configuration is defined in `persistence/timescaledb.persist`:

```java
Strategies {
    everyMinute : "0 * * * * ?"
    everyHour   : "0 0 * * * ?"
    everyDay    : "0 0 0 * * ?"
    default = everyChange
}

Items {
    *             : strategy = everyChange, restoreOnStartup
    Temperature_* : strategy = everyMinute
    Energy_*      : strategy = everyHour
}
```

## Per-Item Downsampling and Metadata Tags

Per-item behaviour is configured via item metadata in the `timescaledb` namespace.

### Metadata format

```text
timescaledb="<label>" [ aggregation="<fn>", downsampleInterval="<interval>",
    retainRawDays="<n>", retentionDays="<n>", <custom-tag>="<value>", ... ]
```

| Metadata key         | Values / default             | Description                                                                                                   |
|----------------------|------------------------------|---------------------------------------------------------------------------------------------------------------|
| value (main)         | any string                   | User-defined label stored in `item_meta.value`. Leave blank (single space `" "`) if only retention is needed. |
| `aggregation`        | `AVG`, `MAX`, `MIN`, `SUM`   | Downsampling aggregation function. Omit if no downsampling is needed.                                         |
| `downsampleInterval` | e.g. `1h`, `15m`, `1d`       | Time bucket size for aggregation. Required when `aggregation` is set.                                         |
| `retainRawDays`      | integer, default `5`         | Keep raw data for N days before replacing with aggregated rows.                                               |
| `retentionDays`      | integer, default `0`         | Drop all data older than N days. `0` = disabled.                                                              |
| custom tags          | any key=value pairs          | Stored unfiltered as JSONB in `item_meta.metadata`. Queryable via Grafana/SQL JSONB operators.                |

The **entire config map** (all keys including `aggregation`, `downsampleInterval`, etc.) is stored as JSONB in `item_meta.metadata`, enabling flexible SQL/Grafana filtering.

### Configuration in `.items` files

```java
// Downsampling + custom tags for Grafana filtering
Number:Temperature Sensor_Temperature_Living "Living Room [%.1f °C]" {
    timescaledb="sensor.temperature" [ aggregation="AVG", downsampleInterval="1h",
        retainRawDays="5", location="living_room", kind="temperature" ]
}

Number:Power Meter_Power_House "House Power [%.1f W]" {
    timescaledb="meter.power" [ aggregation="AVG", downsampleInterval="15m",
        retainRawDays="3", retentionDays="365" ]
}

Number:Energy Meter_Energy_House "House Energy [%.3f kWh]" {
    timescaledb="meter.energy" [ aggregation="SUM", downsampleInterval="1h", retainRawDays="7" ]
}

// Retention-only: no downsampling, just drop data older than 30 days.
// The value must be a single space " " — openHAB rejects a truly empty string.
Number:Temperature Sensor_Temp_Outdoor {
    timescaledb=" " [ retentionDays="30" ]
}
```

### Configuration in mainUI

**Downsampling + Retention + Tags:**

`Item → Metadata → Add Metadata → Enter namespace "timescaledb"`:

- Value: `sensor.temperature` (or any descriptive label)
- Additional config: `aggregation=AVG`, `downsampleInterval=1h`, `retainRawDays=5`, `retentionDays=365`, `location=living_room`

**Retention-only (no downsampling):**

`Item → Metadata → Add Metadata → Enter namespace "timescaledb"`:

- Value: ` ` (a single space — openHAB rejects an empty value)
- Additional config: `retentionDays=30`

### How in-place downsampling works

The downsampling runs as a scheduled job (daily, at midnight):

```text
For each item with timescaledb metadata:
  1. Parse operation + downsampleInterval from metadata
  2. Compute cutoff = NOW() - retainRawDays
  3. SELECT time_bucket(interval, time), agg_fn(value), MAX(unit)
       FROM items
       WHERE item_id = ? AND downsampled = FALSE AND time < cutoff
       GROUP BY bucket
  4. INSERT aggregated rows with downsampled = TRUE
  5. DELETE original rows (downsampled = FALSE, time < cutoff)
```

This keeps the hypertable as the single source of truth. openHAB reads aggregated and raw data from the same table — no query changes needed.

### Supported intervals

| Metadata value | SQL interval  |
|----------------|---------------|
| `1m`           | `1 minute`    |
| `5m`           | `5 minutes`   |
| `15m`          | `15 minutes`  |
| `30m`          | `30 minutes`  |
| `1h`           | `1 hour`      |
| `6h`           | `6 hours`     |
| `1d`           | `1 day`       |

## Querying from openHAB

The service implements the full `QueryablePersistenceService` interface:

| openHAB Query                              | TimescaleDB Implementation            |
|--------------------------------------------|---------------------------------------|
| `historicState(item, timestamp)`           | `SELECT … ORDER BY time DESC LIMIT 1` |
| `averageSince(item, timestamp)`            | `AVG(value) WHERE time >= ?`          |
| `sumSince(item, timestamp)`                | `SUM(value) WHERE time >= ?`          |
| `minSince(item, timestamp)`                | `MIN(value) WHERE time >= ?`          |
| `maxSince(item, timestamp)`                | `MAX(value) WHERE time >= ?`          |
| `countSince(item, timestamp)`              | `COUNT(*) WHERE time >= ?`            |
| `getAllStatesBetween(item, begin, end)`    | Range scan (raw + downsampled)        |
| `removeAllStatesBetween(item, begin, end)` | `DELETE WHERE time BETWEEN ? AND ?`   |

## Compression

When `compressionAfterDays > 0`, the service configures automatic chunk compression:

```sql
ALTER TABLE items SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'item_id',
    timescaledb.compress_orderby   = 'time DESC'
);
SELECT add_compression_policy('items', INTERVAL '30 days');
```

## Retention

When `retentionDays > 0` (global config), a TimescaleDB retention policy is added:

```sql
SELECT add_retention_policy('items', INTERVAL '365 days');
```

Per-item retention (via metadata `retentionDays`) is applied by the daily downsampling job using a targeted DELETE.
This works independently of downsampling: an item can have `retentionDays` set without any aggregation function
(use a single space `" "` as the metadata value in that case).

## Grafana Integration

TimescaleDB works natively with the Grafana PostgreSQL data source.

### Query by item name

```sql
-- Raw + downsampled data for a sensor (last 24 h)
SELECT
  time_bucket('5 minutes', time) AS time,
  AVG(value)                     AS temperature,
  MAX(unit)                      AS unit
FROM items
JOIN item_meta ON items.item_id = item_meta.id
WHERE item_meta.name = 'Sensor_Temperature_Living'
  AND time > NOW() - INTERVAL '24 hours'
GROUP BY 1
ORDER BY 1;
```

### Filter by label (`item_meta.value`)

```sql
-- All items labelled "sensor.temperature" (last 24 h)
SELECT
  time_bucket('5 minutes', time) AS time,
  item_meta.name                 AS sensor,
  AVG(value)                     AS temperature
FROM items
JOIN item_meta ON items.item_id = item_meta.id
WHERE item_meta.value = 'sensor.temperature'
  AND time > NOW() - INTERVAL '24 hours'
GROUP BY 1, 2
ORDER BY 1;
```

### Filter by custom tag (`item_meta.metadata` JSONB)

```sql
-- All temperature sensors in the living room
SELECT
  time_bucket('5 minutes', time) AS time,
  item_meta.name                 AS sensor,
  AVG(value)                     AS temperature
FROM items
JOIN item_meta ON items.item_id = item_meta.id
WHERE item_meta.metadata->>'location' = 'living_room'
  AND item_meta.metadata->>'kind'     = 'temperature'
  AND time > NOW() - INTERVAL '24 hours'
GROUP BY 1, 2
ORDER BY 1;
```

## Differences from JDBC Persistence

| Feature                        | JDBC Persistence     | TimescaleDB Persistence |
|--------------------------------|----------------------|-------------------------|
| TimescaleDB hypertables        | No (plain tables)    | Yes                     |
| In-place downsampling          | No                   | Yes                     |
| Per-item aggregation config    | No                   | Yes (item metadata)     |
| Automatic compression          | No                   | Yes                     |
| Retention policies             | No                   | Yes (global + per-item) |
| Unit stored per measurement    | No                   | Yes                     |
| Multiple DB backends           | Yes                  | No (TimescaleDB only)   |
| Schema (one table per item)    | Yes                  | No (single hypertable)  |
