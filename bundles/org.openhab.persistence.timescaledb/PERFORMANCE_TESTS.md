# TimescaleDB Persistence Layer — Performance & Scaling Tests

## Environment Profile

### Items & Types

| Category                                         | Count | Share |
|--------------------------------------------------|-------|-------|
| Numeric (energy, temperature, fill level, etc.)  | 2,100 | 70 %  |
| Switch / Contact                                 |   600 | 20 %  |
| Other (image, player, location, color)           |   300 | 10 %  |
| **Total**                                        | **3,000** | |

### Write Frequency

| Group          | Items | Rate    | Writes/s |
|----------------|-------|---------|----------|
| High-frequency | 600   | 1 s     | 600      |
| Low-frequency  | 2,400 | 5 min   | 8        |
| **Peak total** |       |         | **~608** |

**Daily volume (before downsampling):**

- High-frequency: 600 × 86,400 = **51.8 M rows/day**
- Low-frequency : 2,400 × 288  = **691,200 rows/day**
- **Total: ~52.5 M rows/day**

### Downsampling Configuration

| Interval | Share of items | Count |
|----------|----------------|-------|
| 15 min   | 10 %           | 300   |
| 1 h      | 40 %           | 1,200 |
| 1 d      | 50 %           | 1,500 |

Aggregation functions: MAX 30 % / MIN 20 % / SUM 25 % / AVG 25 %

Raw rows are **deleted immediately after aggregation**.

### Retention Rules

| Rule                                              | Share  | Count |
|---------------------------------------------------|--------|-------|
| No downsampling config → delete after 5 days      | ~20 %  | ~600  |
| Daily delete (high-frequency, explicitly configured) | 20 % of HF | 120 |

---

## Test Scenarios & SLOs

### T-01 · Single Write — Baseline Latency

**Goal:** Measure the latency of a single `TimescaleDBQuery.insert()` against the real DB under idle conditions.

**Method:** 100 sequential inserts for one item; measure per-insert latency.

| Metric | Target    |
|--------|-----------|
| p50    | < 5 ms    |
| p95    | < 20 ms   |
| p99    | < 50 ms   |
| max    | < 200 ms  |

---

### T-02 · Sustained Write Throughput — Peak Load

**Goal:** Verify the layer sustains 600 writes/s over 10 minutes without latency degradation
or connection-pool exhaustion.

**Method (smoke scale):** 20 threads × 1 write/s for 30 s; pool size 10.

| Metric                        | Target          |
|-------------------------------|-----------------|
| Throughput                    | ≥ target writes/s |
| p95 write latency             | < 50 ms         |
| p99 write latency             | < 100 ms        |
| Error rate (SQLExceptions)    | 0 %             |
| Connection acquisition p99    | < 100 ms        |

---

### T-03 · Write Burst — Cold-Start Spike

**Goal:** A sudden burst of 3,000 inserts (all items at once, e.g. after openHAB restart)
must be absorbed in bounded time.

**Method (smoke scale):** 300 items, parallel insert via thread pool.

| Metric                        | Target  |
|-------------------------------|---------|
| Total time for all inserts    | < 10 s  |
| Error rate                    | 0 %     |

---

### T-04 · Query Latency — Single Time-Series

**Goal:** Measure read latency for typical dashboard queries against a populated table.

**Precondition:** 30 days of data for one high-frequency item (~2.6 M rows at 1 s interval).

| Time window (1 item) | p50     | p95      | p99      |
|----------------------|---------|----------|----------|
| Last 1 h             | < 5 ms  | < 20 ms  | < 50 ms  |
| Last 24 h            | < 10 ms | < 40 ms  | < 100 ms |
| Last 7 d             | < 30 ms | < 100 ms | < 250 ms |
| Last 30 d            | < 100 ms| < 300 ms | < 600 ms |

---

### T-05 · Query Latency — Pagination

**Goal:** `FilterCriteria` with `pageSize=100` across multiple pages must not degrade.

| Metric                             | Target   |
|------------------------------------|----------|
| p99 per page (pages 0–9)           | < 50 ms  |
| No result overlap between pages    | ✓        |

---

### T-06 · Concurrent Read/Write — Mixed Load

**Goal:** Simultaneous write (openHAB stores) + read (Grafana/HABPanel) must not
interfere.

**Method (smoke scale):** 20 writer threads + 10 reader threads for 30 s.

| Metric                  | Target       |
|-------------------------|--------------|
| Write throughput        | ≥ target writes/s |
| Read p99                | < 150 ms     |
| Total error rate        | 0 %          |
| No deadlocks            | ✓            |

---

### T-07 · Downsampling Job — Runtime

**Goal:** Daily `TimescaleDBDownsampleJob.run()` for all configured items with one day of
raw data must complete in acceptable time.

**Method (smoke scale):** 30 items × 288 rows (5-min data for 1 day); run job.

| Metric                                    | Target   |
|-------------------------------------------|----------|
| Total job duration                        | < 5 min  |
| Raw rows remaining after job              | 0        |
| Aggregated rows correct (1 per bucket)    | ✓        |
| Errors / rollbacks                        | 0        |

---

### T-08 · Downsampling Job — Correctness Under Concurrent Writes

**Goal:** Job runs while new raw rows are being written concurrently; no gap, no
double-aggregation.

| Metric                                              | Target |
|-----------------------------------------------------|--------|
| No raw row in eligible time window after job        | ✓      |
| No duplicate aggregated rows                        | ✓      |
| Transactional integrity (no partial state)          | ✓      |

---

### T-09 · Retention Cleanup — Delete Throughput

**Goal:** Daily cleanup removes old data within acceptable time and does not block writes.

**Precondition:** Data from 6 days ago present.

| Metric                                | Target   |
|---------------------------------------|----------|
| Cleanup 30 items (daily rule)         | < 30 s   |
| No rows outside retention window after cleanup | ✓ |
| Write latency p99 during cleanup      | < 100 ms |

---

### T-10 · Connection Pool — Saturation Behaviour

**Goal:** Pool saturation must not cause silent data loss; SQLExceptions must be
logged, not swallowed.

**Method (smoke scale):** Pool size 3, 15 concurrent writers.

| Metric                                           | Target  |
|--------------------------------------------------|---------|
| Connection acquisition p99 (≤ pool-size threads) | < 100 ms|
| No connection timeout for ≤ pool-size threads    | ✓       |
| On timeout: exception surfaced, no silent drop   | ✓       |

---

### T-11 · Schema Initialisation — Speed & Idempotency

**Goal:** `TimescaleDBSchema.initialize()` is fast and safe to call repeatedly.

| Metric                                     | Target   |
|--------------------------------------------|----------|
| First init (empty DB)                      | < 2 s    |
| Repeated init (tables exist)               | < 500 ms |
| No exception on existing schema            | ✓        |

---

### T-12 · Long-Horizon DB Fill (18 Months) — Server-Side Direct Generation

**Goal:** Validate regular persistence flows and query SLOs on a significantly filled
database using a long-horizon dataset inserted via direct bulk write.

**Method:** Use server-side SQL generation (`generate_series`) to seed rows quickly,
then run and measure multiple regular-case operations on that data volume:

- cleanup (clear previous T-12 rows)
- persistence-layer inserts (`TimescaleDBQuery.insert`)
- downsampling job runtime (`TimescaleDBDownsampleJob.run`)
- repeated query windows (1h, 7d, 30d)

| Metric                                    | Target       |
|-------------------------------------------|--------------|
| Persistence-layer write p95               | < 100 ms     |
| Persistence-layer write p99               | < 200 ms     |
| Downsample job runtime (heavy T-12)       | < 1800000 ms |
| Last 1h query p99 (seeded item)           | < 100 ms     |
| Last 7d query p99 (seeded item)           | < 500 ms     |
| Last 30d query p99 (seeded item)          | < 1000 ms    |

This scenario is intentionally **opt-in** and disabled by default:

- `TIMESCALEDB_ENABLE_HEAVY_18M=true`
- Optional tuning:
    - `TIMESCALEDB_T12_DAYS` (default `548`)
    - `TIMESCALEDB_T12_ITEM_COUNT` (default `3000`)
    - `TIMESCALEDB_T12_TARGET_ROWS` (default: calculated from downsampled/retained profile; explicit override)
    - `TIMESCALEDB_T12_SEGMENT_ROWS` (default `1000000`)
    - `TIMESCALEDB_T12_SEED_THREADS` (default: auto, bounded by CPU count)
    - `TIMESCALEDB_T12_ITEM_BATCH_SIZE` (default `100`)
    - `TIMESCALEDB_T12_WRITE_SAMPLES` (default `500`)

Default model details:

- Uses the documented aggregation intervals (15m/1h/1d) as retained-row approximation.
- Seeding runs as parallel batched bulk inserts with explicit phase logging:
    `cleanup start/done`, `seed start/done`, and per-batch `start/done` with
    `insertedBatchRows`, `createdTotal`, `remaining`, and throughput.
- Query windows are measured multiple times per window and logged with latency
    stats and explicit SLO checks.

---

## Implementation Notes

### Assertion Scope (Current Test Code)

The table above describes target SLOs for production-scale scenarios.
Current automated assertions in `TimescaleDBPerformanceIT` are calibrated smoke gates
for CI/dev environments and are therefore less strict for selected metrics
(for example write/query p99 thresholds).

Use the heavy `T-12` scenario for large-volume validation, and tune constants/environment
to approach production-scale conditions.

All tests live in `TimescaleDBPerformanceIT` tagged `@Tag("performance")` and require
the external DB via environment variables `HOST`, `PORT`, `DBNAME`, `USER`, `PASSWORD`.

Run:
```bash
mvn test -Dtest=TimescaleDBPerformanceIT \
         -pl bundles/org.openhab.persistence.timescaledb
```

Run heavy 18-month scenario:
```bash
TIMESCALEDB_ENABLE_HEAVY_18M=true \
mvn test -Dtest=TimescaleDBPerformanceIT#t12_longHorizon_bulkSeed_18months \
         -pl bundles/org.openhab.persistence.timescaledb
```

Each test uses item-name prefix `perf_tXX_` to isolate test data from production data.
A `@AfterAll` step deletes all `perf_` items.

### Scale Constants

The test class exposes top-level constants that can be increased for full-scale runs:

| Constant                  | Smoke value | Full-scale value |
|---------------------------|-------------|------------------|
| `WRITE_THREADS`           | 20          | 600              |
| `WRITE_DURATION_SECONDS`  | 30          | 600              |
| `BURST_ITEMS`             | 100         | 3,000            |
| `QUERY_HISTORY_ROWS`      | 2,000       | 2,592,000        |
| `READ_THREADS`            | 5           | 10               |
| `DOWNSAMPLE_ITEMS`        | 15          | 3,000            |
| `RETENTION_ITEMS`         | 15          | 600              |

---

## Last Known Test Status

### Unit + Container Tests (`mvn test`)

| Test class                          | Tests | Result         | Date       |
|-------------------------------------|-------|----------------|------------|
| `BundleManifestTest`                | 2     | ✅ PASS        | 2026-03-13 |
| `TimescaleDBMapperTest`             | 47    | ✅ PASS        | 2026-03-13 |
| `TimescaleDBMetadataServiceTest`    | 26    | ✅ PASS        | 2026-03-13 |
| `TimescaleDBQueryTest`              | 14    | ✅ PASS        | 2026-03-13 |
| `TimescaleDBDownsampleJobTest`      | 8     | ✅ PASS        | 2026-03-13 |
| `TimescaleDBDownsampleSemanticsTest`| 29    | ✅ PASS        | 2026-03-13 |
| `TimescaleDBSchemaTest`             | 7     | ✅ PASS        | 2026-03-13 |
| `TimescaleDBPersistenceServiceTest` | 20    | ✅ PASS        | 2026-03-13 |
| `TimescaleDBContainerTest`          | 30    | ✅ PASS        | 2026-03-13 |
| **Total**                           | **183** | **✅ 0 failures** | 2026-03-13 |

### Performance Tests (`TimescaleDBPerformanceIT`)

Not run in this environment — require external TimescaleDB (env vars `HOST`, `PORT`, `DBNAME`, `USER`, `PASSWORD`).
