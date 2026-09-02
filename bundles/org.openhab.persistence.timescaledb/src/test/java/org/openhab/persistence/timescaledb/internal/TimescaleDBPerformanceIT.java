/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.persistence.timescaledb.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Performance and scaling tests for the TimescaleDB persistence layer.
 *
 * <p>
 * Runs against the external DB configured via environment variables HOST, PORT, DBNAME, USER, PASSWORD.
 * All test items use the prefix {@code perf_} to avoid conflicts with production data.
 *
 * <p>
 * Scale constants at the top of the class can be increased for full-scale runs.
 * Current values are sized for a smoke/verification run (seconds, not minutes).
 *
 * <p>
 * Run with:
 *
 * <pre>
 *   mvn test -Dtest=TimescaleDBPerformanceIT -pl bundles/org.openhab.persistence.timescaledb
 * </pre>
 *
 * @author René Ulbricht - Initial contribution
 * @see PERFORMANCE_TESTS.md for full scenario descriptions and SLOs
 */
@Tag("performance")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
@SuppressWarnings("null")
class TimescaleDBPerformanceIT {

    // ------------------------------------------------------------------
    // Scale constants — increase for full-scale runs
    // ------------------------------------------------------------------

    /** T-01: number of sequential inserts for baseline latency measurement. */
    static final int BASELINE_INSERTS = 100;

    /** T-02/T-06: concurrent writer threads (full-scale: 600). */
    static final int WRITE_THREADS = 20;

    /** T-02/T-06: sustained-load duration in seconds (full-scale: 600). */
    static final int WRITE_DURATION_SECONDS = 30;

    /** T-03: number of items in the cold-start burst (full-scale: 3,000). */
    static final int BURST_ITEMS = 100;

    /** T-04/T-05: rows pre-seeded for query tests (full-scale: 2,592,000). */
    static final int QUERY_HISTORY_ROWS = 2_000;

    /** T-05: page size for pagination test. */
    static final int PAGE_SIZE = 50;

    /** T-06: concurrent reader threads during mixed-load test. */
    static final int READ_THREADS = 5;

    /** T-07/T-08: items configured for downsampling (full-scale: 3,000). */
    static final int DOWNSAMPLE_ITEMS = 15;

    /** T-07/T-08: raw rows per downsample item (5-min intervals for 1 day = 288). */
    static final int DOWNSAMPLE_ROWS_PER_ITEM = 288;

    /** T-09: items seeded with 6-day-old data for retention test (full-scale: 600). */
    static final int RETENTION_ITEMS = 15;

    // ------------------------------------------------------------------
    // SLO thresholds (milliseconds) — calibrated for smoke scale + remote DB
    // ------------------------------------------------------------------

    static final long SLO_WRITE_P99_MS = 200;
    static final long SLO_WRITE_P95_MS = 100;
    static final long SLO_QUERY_1H_P99_MS = 100;
    static final long SLO_QUERY_7D_P99_MS = 500;
    static final long SLO_QUERY_30D_P99_MS = 1_000;
    static final long SLO_PAGE_P99_MS = 100;
    static final long SLO_BURST_TOTAL_MS = 30_000;
    static final long SLO_DOWNSAMPLE_JOB_MS = 120_000;
    static final long SLO_RETENTION_MS = 30_000;
    static final long SLO_SCHEMA_INIT_MS = 2_000;
    static final long SLO_SCHEMA_REINIT_MS = 500;
    static final long SLO_QUERY_30D_P99_18M_MS = 1_000;
    static final long SLO_DOWNSAMPLE_JOB_18M_MS = 1_800_000;
    /** T-01: p50 write latency smoke gate (spec: < 5 ms). */
    static final long SLO_WRITE_P50_MS = 50;
    /** T-04: last-24h query p99 smoke gate (spec: < 100 ms). */
    static final long SLO_QUERY_24H_P99_MS = 200;
    /** T-06: read p99 smoke gate (spec: < 150 ms). */
    static final long SLO_READ_P99_MS = 300;
    /** T-02/T-09: connection acquisition p99 (spec: < 100 ms). */
    static final long SLO_CONN_ACQ_P99_MS = 100;
    /**
     * T-10: max acquisition time for the fastest poolSize threads under saturation (spec: < 100 ms). Relaxed to 500 ms
     * for smoke/CI: with minimumIdle=1 the pool must create 2 new TCP connections on-demand against a remote DB.
     */
    static final long SLO_T10_FAST_ACQ_MAX_MS = 500;

    // ------------------------------------------------------------------
    // Shared state
    // ------------------------------------------------------------------

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBPerformanceIT.class);

    private static @Nullable HikariDataSource dataSource;

    /** Returns the data source, guaranteed non-null during test execution. */
    private static HikariDataSource ds() {
        HikariDataSource d = dataSource;
        if (d == null) {
            throw new IllegalStateException("dataSource not initialized — @BeforeAll did not run");
        }
        return d;
    }

    @BeforeAll
    static void connect() throws SQLException {
        // Prefer namespaced variables to avoid collisions with generic HOST/USER shell variables.
        String host = firstNonBlankEnv("TIMESCALEDB_HOST", "HOST");
        String port = firstNonBlankEnv("TIMESCALEDB_PORT", "PORT");
        String db = firstNonBlankEnv("TIMESCALEDB_DBNAME", "DBNAME");
        String user = firstNonBlankEnv("TIMESCALEDB_USER", "USER");
        String pass = firstNonBlankEnv("TIMESCALEDB_PASSWORD", "PASSWORD");

        // Only run when explicit DB coordinates are provided.
        assumeTrue(isNonBlank(host) && isNonBlank(db),
                "TimescaleDB perf tests require TIMESCALEDB_HOST/TIMESCALEDB_DBNAME (or HOST/DBNAME) — skipping");

        String nonNullHost = Objects.requireNonNull(host);
        String nonNullDb = Objects.requireNonNull(db);
        String nonNullPort = Objects.requireNonNullElse(port, "5432");
        String nonNullUser = Objects.requireNonNullElse(user, "");
        String nonNullPass = Objects.requireNonNullElse(pass, "");

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:postgresql://" + nonNullHost + ":" + nonNullPort + "/" + nonNullDb);
        cfg.setUsername(nonNullUser);
        cfg.setPassword(nonNullPass);
        cfg.setMaximumPoolSize(Math.max(WRITE_THREADS, 10));
        // Avoid connection storms against pgbouncer/managed DBs during pool startup.
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(10_000);
        applyBoundedStartupTimeouts(cfg);
        cfg.setPoolName("perf-test");
        dataSource = new HikariDataSource(cfg);

        // Ensure schema exists
        try (Connection conn = ds().getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }
    }

    @AfterAll
    static void cleanup() {
        HikariDataSource d = dataSource;
        if (d == null) {
            return;
        }
        // Remove all test items and their data
        try (Connection conn = d.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM items WHERE item_id IN (SELECT id FROM item_meta WHERE name LIKE 'perf_%')")) {
            ps.setQueryTimeout(30);
            int deleted = ps.executeUpdate();
            LOGGER.info("[cleanup] deleted {} perf rows", deleted);
        } catch (SQLException e) {
            LOGGER.warn("[cleanup] timed out/failed while deleting perf rows: {}", e.getMessage());
        }
        try (Connection conn = d.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM item_meta WHERE name LIKE 'perf_%'")) {
            ps.setQueryTimeout(30);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warn("[cleanup] timed out/failed while deleting perf item metadata: {}", e.getMessage());
        }
        try {
            d.close();
        } finally {
            dataSource = null;
        }
    }

    // ------------------------------------------------------------------
    // T-01 · Single Write — Baseline Latency
    // ------------------------------------------------------------------

    @Test
    @Order(1)
    void t01SinglewriteBaselinelatency() throws Exception {
        LOGGER.info("=== T-01: Single Write Baseline Latency ===");
        int itemId = getOrCreate("perf_t01_item", null);

        List<Long> latencies = new ArrayList<>(BASELINE_INSERTS);
        ZonedDateTime base = ZonedDateTime.now().minusMinutes(BASELINE_INSERTS);

        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < BASELINE_INSERTS; i++) {
                long t0 = System.nanoTime();
                TimescaleDBQuery.insert(conn, itemId, base.plusSeconds(i),
                        new TimescaleDBMapper.Row((double) i, null, null));
                latencies.add(nanosToMillis(System.nanoTime() - t0));
            }
        }

        Latencies l = Latencies.of(latencies);
        l.print("T-01");

        assertTrue(l.p99 <= SLO_WRITE_P99_MS,
                String.format("T-01 p99 %d ms exceeds SLO %d ms", l.p99, SLO_WRITE_P99_MS));
        assertTrue(l.p95 <= SLO_WRITE_P95_MS,
                String.format("T-01 p95 %d ms exceeds SLO %d ms", l.p95, SLO_WRITE_P95_MS));
        assertTrue(l.p50 <= SLO_WRITE_P50_MS,
                String.format("T-01 p50 %d ms exceeds SLO %d ms", l.p50, SLO_WRITE_P50_MS));
        assertTrue(l.max <= SLO_WRITE_P99_MS,
                String.format("T-01 max %d ms exceeds SLO %d ms", l.max, SLO_WRITE_P99_MS));
    }

    // ------------------------------------------------------------------
    // T-02 · Sustained Write Throughput
    // ------------------------------------------------------------------

    @Test
    @Order(2)
    void t02Sustainedwritethroughput() throws Exception {
        LOGGER.info("=== T-02: Sustained Write Throughput ({} threads, {}s) ===", WRITE_THREADS,
                WRITE_DURATION_SECONDS);

        // Pre-register items
        int[] itemIds = new int[WRITE_THREADS];
        for (int i = 0; i < WRITE_THREADS; i++) {
            itemIds[i] = getOrCreate("perf_t02_item_" + i, null);
        }

        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        AtomicInteger errors = new AtomicInteger(0);
        AtomicInteger totalWrites = new AtomicInteger(0);
        ConcurrentLinkedQueue<Long> acquireLats = new ConcurrentLinkedQueue<>();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(WRITE_THREADS);
        long deadline = System.currentTimeMillis() + WRITE_DURATION_SECONDS * 1_000L;

        ExecutorService pool = Executors.newFixedThreadPool(WRITE_THREADS);
        for (int t = 0; t < WRITE_THREADS; t++) {
            final int itemId = itemIds[t];
            pool.submit(() -> {
                try {
                    startLatch.await();
                    int seq = 0;
                    while (System.currentTimeMillis() < deadline) {
                        ZonedDateTime ts = ZonedDateTime.now();
                        long t0 = System.nanoTime();
                        try (Connection conn = ds().getConnection()) {
                            acquireLats.add(nanosToMillis(System.nanoTime() - t0));
                            TimescaleDBQuery.insert(conn, itemId, ts,
                                    new TimescaleDBMapper.Row((double) seq++, null, null));
                        } catch (SQLException e) {
                            acquireLats.add(nanosToMillis(System.nanoTime() - t0));
                            errors.incrementAndGet();
                        }
                        latencies.add(nanosToMillis(System.nanoTime() - t0));
                        totalWrites.incrementAndGet();
                        sleepMs(1_000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        long wallStart = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await(WRITE_DURATION_SECONDS + 10, TimeUnit.SECONDS);
        pool.shutdown();
        long wallMs = System.currentTimeMillis() - wallStart;

        Latencies l = Latencies.of(new ArrayList<>(latencies));
        double throughput = totalWrites.get() * 1000.0 / wallMs;
        LOGGER.info("T-02: {} writes in {} ms → {} writes/s, errors={}", totalWrites.get(), wallMs, throughput,
                errors.get());
        l.print("T-02");
        Latencies al = Latencies.of(new ArrayList<>(acquireLats));
        al.print("T-02 acquisition");

        assertEquals(0, errors.get(), "T-02: no write errors expected");
        assertTrue(l.p95 <= SLO_WRITE_P95_MS,
                String.format("T-02 p95 %d ms exceeds SLO %d ms", l.p95, SLO_WRITE_P95_MS));
        assertTrue(l.p99 <= SLO_WRITE_P99_MS,
                String.format("T-02 p99 %d ms exceeds SLO %d ms", l.p99, SLO_WRITE_P99_MS));
        assertTrue(al.p99 <= SLO_CONN_ACQ_P99_MS,
                String.format("T-02 connection acquisition p99 %d ms exceeds SLO %d ms", al.p99, SLO_CONN_ACQ_P99_MS));
        double expectedThroughput = WRITE_THREADS * 0.8;
        assertTrue(throughput >= expectedThroughput, String
                .format("T-02: throughput %.1f writes/s below expected %.1f writes/s", throughput, expectedThroughput));
    }

    // ------------------------------------------------------------------
    // T-03 · Write Burst — Cold-Start Spike
    // ------------------------------------------------------------------

    @Test
    @Order(3)
    void t03WriteburstColdstart() throws Exception {
        LOGGER.info("=== T-03: Write Burst ({} items) ===", BURST_ITEMS);

        int[] itemIds = new int[BURST_ITEMS];
        for (int i = 0; i < BURST_ITEMS; i++) {
            itemIds[i] = getOrCreate("perf_t03_item_" + i, null);
        }

        AtomicInteger errors = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(BURST_ITEMS);
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(BURST_ITEMS, 50));
        ZonedDateTime now = ZonedDateTime.now();

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < BURST_ITEMS; i++) {
            final int itemId = itemIds[i];
            pool.submit(() -> {
                try (Connection conn = ds().getConnection()) {
                    TimescaleDBQuery.insert(conn, itemId, now, new TimescaleDBMapper.Row(1.0, null, null));
                } catch (SQLException e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        pool.shutdown();
        long elapsed = System.currentTimeMillis() - t0;
        LOGGER.info("T-03: {} burst inserts in {} ms, errors={}", BURST_ITEMS, elapsed, errors.get());

        assertEquals(0, errors.get(), "T-03: no burst write errors expected");
        assertTrue(elapsed <= SLO_BURST_TOTAL_MS,
                String.format("T-03: burst took %d ms, exceeds SLO %d ms", elapsed, SLO_BURST_TOTAL_MS));
    }

    // ------------------------------------------------------------------
    // T-04 · Query Latency — Single Time-Series
    // ------------------------------------------------------------------

    @Test
    @Order(4)
    void t04QuerylatencySingletimeseries() throws Exception {
        LOGGER.info("=== T-04: Query Latency ({} rows pre-seeded) ===", QUERY_HISTORY_ROWS);

        int itemId = getOrCreate("perf_t04_sensor", null);
        NumberItem item = new NumberItem("perf_t04_sensor");

        // Seed rows spread over 30 days
        ZonedDateTime base = ZonedDateTime.now().minusDays(30);
        long intervalSeconds = (30L * 24 * 3600) / QUERY_HISTORY_ROWS;
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < QUERY_HISTORY_ROWS; i++) {
                TimescaleDBQuery.insert(conn, itemId, base.plusSeconds(i * intervalSeconds),
                        new TimescaleDBMapper.Row((double) i, null, null));
            }
        }
        LOGGER.info("T-04: seeded {} rows (interval ~{}s)", QUERY_HISTORY_ROWS, intervalSeconds);

        // Measure query latency for different time windows
        ZonedDateTime now = ZonedDateTime.now();
        assertQueryWindow("T-04 last-1h", item, itemId, now.minusHours(1), now, SLO_QUERY_1H_P99_MS);
        assertQueryWindow("T-04 last-24h", item, itemId, now.minusDays(1), now, SLO_QUERY_24H_P99_MS);
        assertQueryWindow("T-04 last-7d", item, itemId, now.minusDays(7), now, SLO_QUERY_7D_P99_MS);
        assertQueryWindow("T-04 last-30d", item, itemId, now.minusDays(30), now, SLO_QUERY_30D_P99_MS);
    }

    // ------------------------------------------------------------------
    // T-05 · Query Latency — Pagination
    // ------------------------------------------------------------------

    @Test
    @Order(5)
    void t05QuerylatencyPagination() throws Exception {
        LOGGER.info("=== T-05: Pagination (pageSize={}) ===", PAGE_SIZE);

        int itemId = getOrCreate("perf_t05_sensor", null);
        NumberItem item = new NumberItem("perf_t05_sensor");

        int totalRows = PAGE_SIZE * 10;
        ZonedDateTime base = ZonedDateTime.now().minusHours(totalRows);
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < totalRows; i++) {
                TimescaleDBQuery.insert(conn, itemId, base.plusHours(i),
                        new TimescaleDBMapper.Row((double) i, null, null));
            }
        }

        List<Long> latencies = new ArrayList<>();
        List<ZonedDateTime> allTimestamps = new ArrayList<>();

        try (Connection conn = ds().getConnection()) {
            for (int page = 0; page < 10; page++) {
                FilterCriteria f = new FilterCriteria();
                f.setItemName("perf_t05_sensor");
                f.setOrdering(Ordering.ASCENDING);
                f.setPageSize(PAGE_SIZE);
                f.setPageNumber(page);

                long t0 = System.nanoTime();
                var results = TimescaleDBQuery.query(conn, item, itemId, f);
                latencies.add(nanosToMillis(System.nanoTime() - t0));

                assertEquals(PAGE_SIZE, results.size(),
                        String.format("T-05: page %d should have %d rows", page, PAGE_SIZE));

                results.forEach(r -> allTimestamps.add(r.getTimestamp()));
            }
        }

        // Verify no overlapping timestamps across pages
        long distinctCount = allTimestamps.stream().distinct().count();
        assertEquals(allTimestamps.size(), distinctCount, "T-05: pages must not overlap");

        Latencies l = Latencies.of(latencies);
        l.print("T-05");
        assertTrue(l.p99 <= SLO_PAGE_P99_MS, String.format("T-05 p99 %d ms exceeds SLO %d ms", l.p99, SLO_PAGE_P99_MS));
    }

    // ------------------------------------------------------------------
    // T-06 · Concurrent Read/Write — Mixed Load
    // ------------------------------------------------------------------

    @Test
    @Order(6)
    void t06ConcurrentreadwriteMixedload() throws Exception {
        LOGGER.info("=== T-06: Mixed Load ({} writers, {} readers, {}s) ===", WRITE_THREADS, READ_THREADS,
                WRITE_DURATION_SECONDS);

        // Seed one query item
        int queryItemId = getOrCreate("perf_t06_query", null);
        NumberItem queryItem = new NumberItem("perf_t06_query");
        ZonedDateTime base = ZonedDateTime.now().minusHours(1);
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < 100; i++) {
                TimescaleDBQuery.insert(conn, queryItemId, base.plusMinutes(i),
                        new TimescaleDBMapper.Row((double) i, null, null));
            }
        }

        int[] writerItemIds = new int[WRITE_THREADS];
        for (int i = 0; i < WRITE_THREADS; i++) {
            writerItemIds[i] = getOrCreate("perf_t06_writer_" + i, null);
        }

        ConcurrentLinkedQueue<Long> writeLats = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Long> readLats = new ConcurrentLinkedQueue<>();
        AtomicInteger writeErrors = new AtomicInteger(0);
        AtomicInteger readErrors = new AtomicInteger(0);

        long deadline = System.currentTimeMillis() + WRITE_DURATION_SECONDS * 1_000L;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(WRITE_THREADS + READ_THREADS);

        ExecutorService pool = Executors.newFixedThreadPool(WRITE_THREADS + READ_THREADS);

        // Writers
        for (int t = 0; t < WRITE_THREADS; t++) {
            final int itemId = writerItemIds[t];
            pool.submit(() -> {
                try {
                    start.await();
                    int seq = 0;
                    while (System.currentTimeMillis() < deadline) {
                        long t0 = System.nanoTime();
                        try (Connection conn = ds().getConnection()) {
                            TimescaleDBQuery.insert(conn, itemId, ZonedDateTime.now(),
                                    new TimescaleDBMapper.Row((double) seq++, null, null));
                        } catch (SQLException e) {
                            writeErrors.incrementAndGet();
                        }
                        writeLats.add(nanosToMillis(System.nanoTime() - t0));
                        sleepMs(1_000);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        // Readers
        for (int t = 0; t < READ_THREADS; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    while (System.currentTimeMillis() < deadline) {
                        FilterCriteria f = new FilterCriteria();
                        f.setItemName("perf_t06_query");
                        f.setBeginDate(ZonedDateTime.now().minusHours(1));
                        long t0 = System.nanoTime();
                        try (Connection conn = ds().getConnection()) {
                            TimescaleDBQuery.query(conn, queryItem, queryItemId, f);
                        } catch (SQLException e) {
                            readErrors.incrementAndGet();
                        }
                        readLats.add(nanosToMillis(System.nanoTime() - t0));
                        sleepMs(500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(WRITE_DURATION_SECONDS + 15, TimeUnit.SECONDS);
        pool.shutdown();

        Latencies wl = Latencies.of(new ArrayList<>(writeLats));
        Latencies rl = Latencies.of(new ArrayList<>(readLats));
        wl.print("T-06 writes");
        rl.print("T-06 reads");
        LOGGER.info("T-06: write errors={}, read errors={}", writeErrors.get(), readErrors.get());

        assertEquals(0, writeErrors.get(), "T-06: no write errors during mixed load");
        assertEquals(0, readErrors.get(), "T-06: no read errors during mixed load");
        assertTrue(wl.p99 <= SLO_WRITE_P99_MS,
                String.format("T-06 write p99 %d ms exceeds SLO %d ms", wl.p99, SLO_WRITE_P99_MS));
        assertTrue(rl.p99 <= SLO_READ_P99_MS,
                String.format("T-06 read p99 %d ms exceeds SLO %d ms", rl.p99, SLO_READ_P99_MS));
    }

    // ------------------------------------------------------------------
    // T-07 · Downsampling Job — Runtime
    // ------------------------------------------------------------------

    @Test
    @Order(7)
    void t07DownsamplejobRuntime() throws Exception {
        LOGGER.info("=== T-07: Downsampling Job ({} items × {} rows) ===", DOWNSAMPLE_ITEMS, DOWNSAMPLE_ROWS_PER_ITEM);

        // Seed raw data: DOWNSAMPLE_ROWS_PER_ITEM rows per item at 5-min intervals, all > 1 day old
        ZonedDateTime base = ZonedDateTime.now().minusDays(2);
        int[] ids = new int[DOWNSAMPLE_ITEMS];
        for (int i = 0; i < DOWNSAMPLE_ITEMS; i++) {
            ids[i] = getOrCreate("perf_t07_item_" + i, null);
        }
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < DOWNSAMPLE_ITEMS; i++) {
                for (int r = 0; r < DOWNSAMPLE_ROWS_PER_ITEM; r++) {
                    TimescaleDBQuery.insert(conn, ids[i], base.plusMinutes(r * 5L),
                            new TimescaleDBMapper.Row((double) r, null, null));
                }
            }
        }
        LOGGER.info("T-07: seeded {} raw rows", DOWNSAMPLE_ITEMS * DOWNSAMPLE_ROWS_PER_ITEM);

        // Build metadata: all items → AVG, 1h buckets, retainRawDays=0
        MetadataRegistry mr = mock(MetadataRegistry.class);
        List<Metadata> metaList = new ArrayList<>();
        for (int i = 0; i < DOWNSAMPLE_ITEMS; i++) {
            String name = "perf_t07_item_" + i;
            Metadata m = new Metadata(new MetadataKey("timescaledb", name), "AVG",
                    Map.of("downsampleInterval", "1h", "retainRawDays", "0"));
            metaList.add(m);
            when(mr.get(new MetadataKey("timescaledb", name))).thenReturn(m);
        }
        when(mr.getAll()).thenAnswer(inv -> metaList);

        TimescaleDBMetadataService ms = new TimescaleDBMetadataService(mr);
        Map<String, Integer> idMap = new java.util.HashMap<>();
        for (int i = 0; i < DOWNSAMPLE_ITEMS; i++) {
            idMap.put("perf_t07_item_" + i, ids[i]);
        }

        TimescaleDBDownsampleJob job = new TimescaleDBDownsampleJob(ds(), ms);

        long t0 = System.currentTimeMillis();
        job.run();
        long elapsed = System.currentTimeMillis() - t0;
        LOGGER.info("T-07: job completed in {} ms", elapsed);

        // Verify: no raw rows remain for any of the test items
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < DOWNSAMPLE_ITEMS; i++) {
                int id = ids[i];
                try (PreparedStatement ps = conn
                        .prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ? AND downsampled = FALSE")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next());
                        assertEquals(0, rs.getInt(1), String.format("T-07: item %d still has raw rows after job", i));
                    }
                }
            }

            // Verify aggregated rows exist
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ? AND downsampled = TRUE")) {
                ps.setInt(1, ids[0]);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertTrue(rs.getInt(1) > 0, "T-07: no aggregated rows found for item_0");
                }
            }
        }

        assertTrue(elapsed <= SLO_DOWNSAMPLE_JOB_MS,
                String.format("T-07: job took %d ms, exceeds SLO %d ms", elapsed, SLO_DOWNSAMPLE_JOB_MS));
    }

    // ------------------------------------------------------------------
    // T-08 · Downsampling Job — Correctness Under Concurrent Writes
    // ------------------------------------------------------------------

    @Test
    @Order(8)
    void t08DownsamplejobCorrectnessunderload() throws Exception {
        LOGGER.info("=== T-08: Downsampling Correctness Under Concurrent Writes ===");

        int itemId = getOrCreate("perf_t08_item", null);

        // Ensure a clean slate so repeated runs on the same DB don't carry over stale data
        try (Connection conn = ds().getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM items WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        }

        // Seed 2-day-old raw data to be aggregated
        ZonedDateTime old = ZonedDateTime.now().minusDays(2);
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < 50; i++) {
                TimescaleDBQuery.insert(conn, itemId, old.plusMinutes(i * 30L),
                        new TimescaleDBMapper.Row((double) i, null, null));
            }
        }

        // Count raw rows before
        long rawBefore = countRows(itemId, false);
        LOGGER.info("T-08: raw rows before job: {}", rawBefore);

        // Start concurrent writers writing *current* timestamps (not eligible for this job run)
        AtomicInteger writeErrors = new AtomicInteger(0);
        CountDownLatch writersDone = new CountDownLatch(5);
        long deadline = System.currentTimeMillis() + 10_000;

        for (int t = 0; t < 5; t++) {
            Thread.ofVirtual().start(() -> {
                int seq = 0;
                while (System.currentTimeMillis() < deadline) {
                    try (Connection conn = ds().getConnection()) {
                        TimescaleDBQuery.insert(conn, itemId, ZonedDateTime.now(),
                                new TimescaleDBMapper.Row((double) seq, null, null));
                    } catch (SQLException e) {
                        writeErrors.incrementAndGet();
                    }
                    sleepMs(200);
                }
                writersDone.countDown();
            });
        }

        // Run job while writers are active
        MetadataRegistry mr = mock(MetadataRegistry.class);
        Metadata m = new Metadata(new MetadataKey("timescaledb", "perf_t08_item"), "AVG",
                Map.of("downsampleInterval", "1h", "retainRawDays", "0"));
        when(mr.get(new MetadataKey("timescaledb", "perf_t08_item"))).thenReturn(m);
        when(mr.getAll()).thenAnswer(inv -> List.of(m));

        new TimescaleDBDownsampleJob(ds(), new TimescaleDBMetadataService(mr)).run();

        writersDone.await(15, TimeUnit.SECONDS);

        // Old raw rows must be gone
        long rawOldAfter = countRowsBefore(itemId, false, ZonedDateTime.now().minusDays(1));
        assertEquals(0, rawOldAfter, "T-08: old raw rows must be gone after job");

        // Aggregated rows must exist
        long agg = countRows(itemId, true);
        assertTrue(agg > 0, "T-08: aggregated rows must exist after job");

        // Recent raw rows must still exist: concurrent current writes continue while old data is downsampled.
        long rawRecentAfter = countRowsAfter(itemId, false, ZonedDateTime.now().minusMinutes(1));
        assertTrue(rawRecentAfter > 0,
                "T-08: recent raw rows must remain after downsampling while concurrent writes are active");

        // No write errors from concurrent writers
        assertEquals(0, writeErrors.get(), "T-08: concurrent writes must not error");

        // Verify no duplicate aggregated rows (one row per time bucket per item)
        try (Connection conn = ds().getConnection();
                PreparedStatement dupPs = conn.prepareStatement(
                        "SELECT COUNT(*) FROM (SELECT time FROM items WHERE item_id = ? AND downsampled = TRUE GROUP BY time HAVING COUNT(*) > 1) AS dups")) {
            dupPs.setInt(1, itemId);
            try (ResultSet dupRs = dupPs.executeQuery()) {
                assertTrue(dupRs.next());
                assertEquals(0L, dupRs.getLong(1), "T-08: duplicate aggregated rows must not exist");
            }
        }

        LOGGER.info("T-08: old raw rows after={}, recent raw rows={}, aggregated={}, write errors={}", rawOldAfter,
                rawRecentAfter, agg, writeErrors.get());
    }

    // ------------------------------------------------------------------
    // T-09 · Retention Cleanup
    // ------------------------------------------------------------------

    @Test
    @Order(9)
    void t09Retentioncleanup() throws Exception {
        LOGGER.info("=== T-09: Retention Cleanup ({} items) ===", RETENTION_ITEMS);

        int[] ids = new int[RETENTION_ITEMS];
        for (int i = 0; i < RETENTION_ITEMS; i++) {
            ids[i] = getOrCreate("perf_t09_item_" + i, null);
        }

        // Seed 6-day-old rows (must be deleted) and 2-day-old rows (must survive with 5d retention)
        ZonedDateTime old = ZonedDateTime.now().minusDays(6);
        ZonedDateTime recent = ZonedDateTime.now().minusDays(2);
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < RETENTION_ITEMS; i++) {
                for (int r = 0; r < 10; r++) {
                    TimescaleDBQuery.insert(conn, ids[i], old.plusHours(r),
                            new TimescaleDBMapper.Row((double) r, null, null));
                    TimescaleDBQuery.insert(conn, ids[i], recent.plusHours(r),
                            new TimescaleDBMapper.Row((double) r, null, null));
                }
            }
        }

        // Build metadata: retentionDays=5, retainRawDays=0
        MetadataRegistry mr = mock(MetadataRegistry.class);
        List<Metadata> metaList = new ArrayList<>();
        for (int i = 0; i < RETENTION_ITEMS; i++) {
            String name = "perf_t09_item_" + i;
            Metadata meta = new Metadata(new MetadataKey("timescaledb", name), "AVG",
                    Map.of("downsampleInterval", "1h", "retainRawDays", "0", "retentionDays", "5"));
            metaList.add(meta);
            when(mr.get(new MetadataKey("timescaledb", name))).thenReturn(meta);
        }
        when(mr.getAll()).thenAnswer(inv -> metaList);

        TimescaleDBMetadataService ms = new TimescaleDBMetadataService(mr);
        Map<String, Integer> idMap = new java.util.HashMap<>();
        for (int i = 0; i < RETENTION_ITEMS; i++) {
            idMap.put("perf_t09_item_" + i, ids[i]);
        }

        // Start concurrent writers to validate write latency is not blocked during cleanup
        ConcurrentLinkedQueue<Long> writeLats = new ConcurrentLinkedQueue<>();
        AtomicInteger writerErrors = new AtomicInteger(0);
        int writerCount = 3;
        CountDownLatch writersDone = new CountDownLatch(writerCount);
        int writerItemId = getOrCreate("perf_t09_writer", null);
        AtomicInteger writerSeq = new AtomicInteger(0);
        long writerDeadline = System.currentTimeMillis() + SLO_RETENTION_MS + 5_000;
        for (int w = 0; w < writerCount; w++) {
            Thread.ofVirtual().start(() -> {
                while (System.currentTimeMillis() < writerDeadline) {
                    long wt0 = System.nanoTime();
                    try (Connection conn = ds().getConnection()) {
                        TimescaleDBQuery.insert(conn, writerItemId,
                                ZonedDateTime.now().plusSeconds(writerSeq.getAndIncrement()),
                                new TimescaleDBMapper.Row(1.0, null, null));
                    } catch (SQLException e) {
                        writerErrors.incrementAndGet();
                    }
                    writeLats.add(nanosToMillis(System.nanoTime() - wt0));
                    sleepMs(100);
                }
                writersDone.countDown();
            });
        }

        long t0 = System.currentTimeMillis();
        new TimescaleDBDownsampleJob(ds(), ms).run();
        long elapsed = System.currentTimeMillis() - t0;

        writersDone.await(SLO_RETENTION_MS / 1000 + 10, TimeUnit.SECONDS);
        Latencies wl = Latencies.of(new ArrayList<>(writeLats));
        wl.print("T-09 write during cleanup");
        LOGGER.info("T-09: cleanup completed in {} ms, write errors={}", elapsed, writerErrors.get());

        // All 6-day-old rows must be gone for every item
        try (Connection conn = ds().getConnection()) {
            for (int id : ids) {
                long old6d = countRowsBefore(id, null, ZonedDateTime.now().minusDays(5));
                assertEquals(0, old6d, String.format("T-09: item_id=%d still has rows older than 5 days", id));
            }
        }

        assertTrue(elapsed <= SLO_RETENTION_MS,
                String.format("T-09: cleanup took %d ms, exceeds SLO %d ms", elapsed, SLO_RETENTION_MS));
        assertEquals(0, writerErrors.get(), "T-09: no write errors during concurrent cleanup");
        assertTrue(wl.p99 <= SLO_WRITE_P99_MS,
                String.format("T-09: write p99 %d ms during cleanup exceeds SLO %d ms", wl.p99, SLO_WRITE_P99_MS));
    }

    // ------------------------------------------------------------------
    // T-10 · Connection Pool Saturation
    // ------------------------------------------------------------------

    @Test
    @Order(10)
    void t10ConnectionpoolSaturation() throws Exception {
        LOGGER.info("=== T-10: Connection Pool Saturation (pool=3, 15 concurrent writers) ===");

        // Create a separate pool with only 3 connections
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(ds().getJdbcUrl());
        cfg.setUsername(ds().getUsername());
        cfg.setPassword(ds().getPassword());
        cfg.setMaximumPoolSize(3);
        cfg.setMinimumIdle(1);
        cfg.setConnectionTimeout(5_000);
        applyBoundedStartupTimeouts(cfg);
        cfg.setPoolName("perf-saturation");

        int threadCount = 15;
        int itemId = getOrCreate("perf_t10_item", null);
        long rowsBefore = countRows(itemId, false);

        HikariDataSource smallPool;
        try {
            smallPool = new HikariDataSource(cfg);
        } catch (Exception initEx) {
            assumeTrue(false, "T-10 skipped: second pool could not be initialised (infrastructure constraint): "
                    + initEx.getMessage());
            return; // unreachable — satisfies flow analysis
        }

        try (HikariDataSource saturatedPool = smallPool) {
            ConcurrentLinkedQueue<Long> acquisitionTimes = new ConcurrentLinkedQueue<>();
            AtomicInteger timeouts = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(threadCount);
            CountDownLatch start = new CountDownLatch(1);

            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            for (int t = 0; t < threadCount; t++) {
                final int ti = t;
                pool.submit(() -> {
                    try {
                        start.await();
                        long t0 = System.nanoTime();
                        try (Connection conn = saturatedPool.getConnection()) {
                            acquisitionTimes.add(nanosToMillis(System.nanoTime() - t0));
                            TimescaleDBQuery.insert(conn, itemId, ZonedDateTime.now().plusSeconds(ti * 10L),
                                    new TimescaleDBMapper.Row(1.0, null, null));
                            sleepMs(100); // hold connection briefly
                        } catch (SQLException e) {
                            acquisitionTimes.add(nanosToMillis(System.nanoTime() - t0));
                            String msg = e.getMessage();
                            if (msg != null && msg.contains("timeout")) {
                                timeouts.incrementAndGet();
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            start.countDown();
            latch.await(30, TimeUnit.SECONDS);
            pool.shutdown();

            Latencies l = Latencies.of(new ArrayList<>(acquisitionTimes));
            l.print("T-10 acquisition");
            LOGGER.info("T-10: timeouts={} out of {} threads", timeouts.get(), threadCount);

            // The fastest poolSize threads get a connection immediately; check their max acquisition time.
            int poolSize = cfg.getMaximumPoolSize();
            List<Long> sortedTimes = new ArrayList<>(acquisitionTimes);
            Collections.sort(sortedTimes);
            long fastMax = sortedTimes.isEmpty() ? 0L : sortedTimes.get(Math.min(poolSize, sortedTimes.size()) - 1);
            assertTrue(fastMax <= SLO_T10_FAST_ACQ_MAX_MS,
                    String.format("T-10: fastest %d threads max acquisition %d ms too slow (expected <= %d ms)",
                            poolSize, fastMax, SLO_T10_FAST_ACQ_MAX_MS));

            // Verify no silent data loss: successful connections must have written their row.
            long rowsAfter = countRows(itemId, false);
            long expectedRows = rowsBefore + (threadCount - timeouts.get());
            assertTrue(rowsAfter >= expectedRows, String
                    .format("T-10: silent drop detected — expected >= %d rows but found %d", expectedRows, rowsAfter));
        }
    }

    // ------------------------------------------------------------------
    // T-11 · Schema Initialisation Speed & Idempotency
    // ------------------------------------------------------------------

    @Test
    @Order(11)
    void t11SchemainitSpeedandidempotency() throws Exception {
        LOGGER.info("=== T-11: Schema Init Speed & Idempotency ===");

        // Note: first-init against an empty DB (spec: < 2 s) is not separately timed here because
        // TimescaleDBSchema.initialize() was already called in @BeforeAll. Dropping and re-creating
        // the schema would be too destructive in a shared integration-test environment.
        // The re-init timing below covers the idempotency branch (tables already exist).

        // Repeated init on existing schema
        long t0 = System.currentTimeMillis();
        try (Connection conn = ds().getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }
        long elapsed = System.currentTimeMillis() - t0;
        LOGGER.info("T-11: re-init on existing schema: {} ms", elapsed);

        assertTrue(elapsed <= SLO_SCHEMA_REINIT_MS,
                String.format("T-11: re-init took %d ms, exceeds SLO %d ms", elapsed, SLO_SCHEMA_REINIT_MS));

        // Must be idempotent
        assertDoesNotThrow(() -> {
            try (Connection conn = ds().getConnection()) {
                TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
            }
        }, "T-11: repeated init must not throw");
    }

    // ------------------------------------------------------------------
    // T-12 · Long-Horizon Dataset (18 months) using bulk direct write
    // ------------------------------------------------------------------

    @Test
    @Order(12)
    @Timeout(value = 45, unit = TimeUnit.MINUTES)
    void t12LonghorizonBulkseed18months() throws Exception {
        assumeTrue(envFlag("TIMESCALEDB_ENABLE_HEAVY_18M", false),
                "T-12 disabled: set TIMESCALEDB_ENABLE_HEAVY_18M=true to run heavy 18-month bulk test");

        long days = envLong("TIMESCALEDB_T12_DAYS", 548L);
        int itemCount = (int) envLong("TIMESCALEDB_T12_ITEM_COUNT", 3_000L);
        long totalRows = envLong("TIMESCALEDB_T12_TARGET_ROWS", theoreticalDownsampledRowsFor18MonthProfile(days));
        int segmentRows = (int) envLong("TIMESCALEDB_T12_SEGMENT_ROWS", 1_000_000L);
        int seedThreads = (int) envLong("TIMESCALEDB_T12_SEED_THREADS",
                Math.max(2, Math.min(12, Runtime.getRuntime().availableProcessors())));
        int itemBatchSize = (int) envLong("TIMESCALEDB_T12_ITEM_BATCH_SIZE", 100L);
        int retainRawDays = (int) envLong("TIMESCALEDB_T12_RETAIN_RAW_DAYS", 1L);
        int rawWindowDays = (int) envLong("TIMESCALEDB_T12_RAW_WINDOW_DAYS", Math.max(2, retainRawDays + 1));
        long rowsPerItem = Math.max(1L, totalRows / itemCount);
        long rawRowsPerItem = Math.max(1L, rawWindowDays * 24L * 12L);

        LOGGER.info(
                "=== T-12: Heavy 18M server-side dataset (days={}, items={}, model=downsampled, targetRows={}, rows/item={}, rawWindowDays={}, rawRows/item={}, retainRawDays={}, segmentRows={}, seedThreads={}, itemBatchSize={}) ===",
                days, itemCount, totalRows, rowsPerItem, rawWindowDays, rawRowsPerItem, retainRawDays, segmentRows,
                seedThreads, itemBatchSize);

        int[] ids = new int[itemCount];
        for (int i = 0; i < itemCount; i++) {
            ids[i] = getOrCreate("perf_t12_item_" + i, null);
        }

        logInfo("T-12 cleanup start: deleting existing rows for {} items", itemCount);
        long deletedRows = deleteRowsByIds(ids);
        logInfo("T-12 cleanup done: deletedRows={}", deletedRows);

        Instant historicalStart = ZonedDateTime.now().minusDays(days).toInstant();
        long historicalStepSeconds = Math.max(1L, (days * 86_400L) / rowsPerItem);

        List<int[]> itemBatches = partitionIds(ids, Math.max(1, itemBatchSize));
        long historicalStepsPerBatch = (rowsPerItem + segmentRows - 1L) / segmentRows;
        long historicalExpectedSqlBatches = itemBatches.size() * historicalStepsPerBatch;
        long historicalTotalRows = rowsPerItem * itemCount;
        AtomicLong insertedRows = new AtomicLong(0);
        AtomicLong startedExecutions = new AtomicLong(0);
        AtomicLong completedExecutions = new AtomicLong(0);
        long seedStartNs = System.nanoTime();

        logInfo("T-12 seed historical start: targetRows={} expectedSqlBatches={} rowsPerItem={} stepSeconds={} segmentRows={} seedThreads={} itemBatchSize={}",
                historicalTotalRows, historicalExpectedSqlBatches, rowsPerItem, historicalStepSeconds, segmentRows,
                seedThreads, itemBatchSize);

        ExecutorService seedPool = Executors.newFixedThreadPool(Math.max(1, Math.min(seedThreads, itemBatches.size())));
        try {
            List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
            for (int[] batch : itemBatches) {
                futures.add(seedPool.submit(() -> {
                    try (Connection conn = ds().getConnection()) {
                        seedRowsServerSideBatch(conn, "historical", true, batch, historicalStart, rowsPerItem,
                                historicalStepSeconds, segmentRows, insertedRows, historicalTotalRows,
                                historicalExpectedSqlBatches, seedStartNs, startedExecutions, completedExecutions);
                    } catch (SQLException e) {
                        throw new IllegalStateException("Failed to seed historical rows", e);
                    }
                }));
            }
            for (var future : futures) {
                future.get();
            }
        } finally {
            seedPool.shutdown();
            seedPool.awaitTermination(10, TimeUnit.MINUTES);
        }

        long elapsedMs = Math.max(1L, nanosToMillis(System.nanoTime() - seedStartNs));
        long createdTotal = insertedRows.get();
        long remaining = Math.max(0L, historicalTotalRows - createdTotal);
        long rate = (createdTotal * 1000L) / elapsedMs;
        logInfo("T-12 seed historical done: createdTotal={} remaining={} elapsedMs={} avgRate={} rows/s", createdTotal,
                remaining, elapsedMs, rate);

        Instant rawWindowStart = ZonedDateTime.now().minusDays(rawWindowDays).toInstant();
        long rawStepSeconds = 300L;
        long rawStepsPerBatch = (rawRowsPerItem + segmentRows - 1L) / segmentRows;
        long rawExpectedSqlBatches = itemBatches.size() * rawStepsPerBatch;
        long rawTotalRows = rawRowsPerItem * itemCount;

        insertedRows.set(0);
        startedExecutions.set(0);
        completedExecutions.set(0);
        long rawSeedStartNs = System.nanoTime();

        logInfo("T-12 seed raw-window start: targetRows={} expectedSqlBatches={} rowsPerItem={} stepSeconds={} windowDays={} retainRawDays={}",
                rawTotalRows, rawExpectedSqlBatches, rawRowsPerItem, rawStepSeconds, rawWindowDays, retainRawDays);

        ExecutorService rawSeedPool = Executors
                .newFixedThreadPool(Math.max(1, Math.min(seedThreads, itemBatches.size())));
        try {
            List<java.util.concurrent.Future<?>> rawFutures = new ArrayList<>();
            for (int[] batch : itemBatches) {
                rawFutures.add(rawSeedPool.submit(() -> {
                    try (Connection conn = ds().getConnection()) {
                        seedRowsServerSideBatch(conn, "raw-window", false, batch, rawWindowStart, rawRowsPerItem,
                                rawStepSeconds, segmentRows, insertedRows, rawTotalRows, rawExpectedSqlBatches,
                                rawSeedStartNs, startedExecutions, completedExecutions);
                    } catch (SQLException e) {
                        throw new IllegalStateException("Failed to seed raw-window rows", e);
                    }
                }));
            }
            for (var future : rawFutures) {
                future.get();
            }
        } finally {
            rawSeedPool.shutdown();
            rawSeedPool.awaitTermination(10, TimeUnit.MINUTES);
        }

        long rawElapsedMs = Math.max(1L, nanosToMillis(System.nanoTime() - rawSeedStartNs));
        long rawCreatedTotal = insertedRows.get();
        long rawRemaining = Math.max(0L, rawTotalRows - rawCreatedTotal);
        long rawRate = (rawCreatedTotal * 1000L) / rawElapsedMs;
        logInfo("T-12 seed raw-window done: createdTotal={} remaining={} elapsedMs={} avgRate={} rows/s",
                rawCreatedTotal, rawRemaining, rawElapsedMs, rawRate);

        long seededDownsampledRows = countRowsByIds(ids, true);
        long downsampledRemaining = Math.max(0L, historicalTotalRows - seededDownsampledRows);
        logInfo("T-12 seed verify historical: createdDownsampledRows={} targetRows={} remainingRows={}",
                seededDownsampledRows, historicalTotalRows, downsampledRemaining);
        assertEquals(historicalTotalRows, seededDownsampledRows,
                String.format("T-12 historical seed mismatch: expected %d rows, found %d", historicalTotalRows,
                        seededDownsampledRows));

        ZonedDateTime cutoffBeforeJob = ZonedDateTime.now().minusDays(retainRawDays);
        long rawOldBefore = countRowsByIdsBefore(ids, false, cutoffBeforeJob);
        long rawRecentBefore = countRowsByIdsAfter(ids, false, cutoffBeforeJob);
        logInfo("T-12 seed verify raw-window: rawOldBefore={} rawRecentBefore={} cutoffDays={}", rawOldBefore,
                rawRecentBefore, retainRawDays);
        assertTrue(rawOldBefore > 0L,
                "T-12 raw-window seed mismatch: expected pending raw rows older than retainRawDays");
        assertTrue(rawRecentBefore > 0L,
                "T-12 raw-window seed mismatch: expected recent raw rows within retainRawDays");

        int writeSamples = (int) envLong("TIMESCALEDB_T12_WRITE_SAMPLES", 500L);
        measureHeavyDatasetWriteLatency(ids, writeSamples);

        long downsampleElapsed = runHeavyDatasetDownsample(ids, retainRawDays);
        logInfo("T-12 downsample SLO check: elapsed={} ms <= {} ms", downsampleElapsed, SLO_DOWNSAMPLE_JOB_18M_MS);
        assertTrue(downsampleElapsed <= SLO_DOWNSAMPLE_JOB_18M_MS, String
                .format("T-12 downsample took %d ms, exceeds SLO %d ms", downsampleElapsed, SLO_DOWNSAMPLE_JOB_18M_MS));

        NumberItem queryItem = new NumberItem("perf_t12_item_0");
        ZonedDateTime now = ZonedDateTime.now();
        assertQueryWindow("T-12 last-1h", queryItem, ids[0], now.minusHours(1), now, SLO_QUERY_1H_P99_MS);
        assertQueryWindow("T-12 last-7d", queryItem, ids[0], now.minusDays(7), now, SLO_QUERY_7D_P99_MS);
        assertQueryWindow("T-12 last-30d", queryItem, ids[0], now.minusDays(30), now, SLO_QUERY_30D_P99_18M_MS);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static int getOrCreate(String name, @Nullable String label) throws SQLException {
        try (Connection conn = ds().getConnection()) {
            return TimescaleDBQuery.getOrCreateItemId(conn, name, label);
        }
    }

    private static long countRows(int itemId, boolean downsampled) throws SQLException {
        try (Connection conn = ds().getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ? AND downsampled = ?")) {
            ps.setInt(1, itemId);
            ps.setBoolean(2, downsampled);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static long countRowsBefore(int itemId, @Nullable Boolean downsampled, ZonedDateTime before)
            throws SQLException {
        String sql = downsampled == null ? "SELECT COUNT(*) FROM items WHERE item_id = ? AND time < ?"
                : "SELECT COUNT(*) FROM items WHERE item_id = ? AND time < ? AND downsampled = " + downsampled;
        try (Connection conn = ds().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setTimestamp(2, java.sql.Timestamp.from(before.toInstant()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static long countRowsAfter(int itemId, @Nullable Boolean downsampled, ZonedDateTime after)
            throws SQLException {
        String sql = downsampled == null ? "SELECT COUNT(*) FROM items WHERE item_id = ? AND time >= ?"
                : "SELECT COUNT(*) FROM items WHERE item_id = ? AND time >= ? AND downsampled = " + downsampled;
        try (Connection conn = ds().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setTimestamp(2, java.sql.Timestamp.from(after.toInstant()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private void assertQueryWindow(String label, NumberItem item, int itemId, ZonedDateTime from, ZonedDateTime to,
            long sloP99Ms) throws SQLException {
        int runs = 10;
        List<Long> latencies = new ArrayList<>(runs);
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < runs; i++) {
                FilterCriteria f = new FilterCriteria();
                f.setItemName(item.getName());
                f.setBeginDate(from);
                f.setEndDate(to);
                f.setOrdering(Ordering.DESCENDING);
                long t0 = System.nanoTime();
                TimescaleDBQuery.query(conn, item, itemId, f);
                latencies.add(nanosToMillis(System.nanoTime() - t0));
            }
        }
        Latencies l = Latencies.of(latencies);
        l.print(label);
        logInfo("{} SLO check: p99={} ms <= {} ms", label, l.p99, sloP99Ms);
        assertTrue(l.p99 <= sloP99Ms, String.format("%s p99 %d ms exceeds SLO %d ms", label, l.p99, sloP99Ms));
    }

    private void measureHeavyDatasetWriteLatency(int[] ids, int writeSamples) throws SQLException {
        int samples = Math.max(1, writeSamples);
        logInfo("T-12 write phase start: persistence-layer inserts samples={}", samples);

        List<Long> latencies = new ArrayList<>(samples);
        ZonedDateTime base = ZonedDateTime.now();
        try (Connection conn = ds().getConnection()) {
            for (int i = 0; i < samples; i++) {
                int itemId = ids[i % ids.length];
                long t0 = System.nanoTime();
                TimescaleDBQuery.insert(conn, itemId, base.plusSeconds(i),
                        new TimescaleDBMapper.Row((double) i, null, null));
                latencies.add(nanosToMillis(System.nanoTime() - t0));
            }
        }

        Latencies l = Latencies.of(latencies);
        l.print("T-12 writes");
        logInfo("T-12 writes SLO check: p95={} ms <= {} ms, p99={} ms <= {} ms", l.p95, SLO_WRITE_P95_MS, l.p99,
                SLO_WRITE_P99_MS);
        assertTrue(l.p95 <= SLO_WRITE_P95_MS,
                String.format("T-12 writes p95 %d ms exceeds SLO %d ms", l.p95, SLO_WRITE_P95_MS));
        assertTrue(l.p99 <= SLO_WRITE_P99_MS,
                String.format("T-12 writes p99 %d ms exceeds SLO %d ms", l.p99, SLO_WRITE_P99_MS));
    }

    private long runHeavyDatasetDownsample(int[] ids, int retainRawDays) throws SQLException {
        logInfo("T-12 downsample phase start: items={} mode=AVG interval=1h retainRawDays={}", ids.length,
                retainRawDays);

        MetadataRegistry mr = mock(MetadataRegistry.class);
        List<Metadata> metaList = new ArrayList<>(ids.length);
        Map<String, Integer> idMap = new HashMap<>(ids.length);

        for (int i = 0; i < ids.length; i++) {
            String name = "perf_t12_item_" + i;
            Metadata m = new Metadata(new MetadataKey("timescaledb", name), "AVG",
                    Map.of("downsampleInterval", "1h", "retainRawDays", String.valueOf(retainRawDays)));
            metaList.add(m);
            idMap.put(name, ids[i]);
            when(mr.get(new MetadataKey("timescaledb", name))).thenReturn(m);
        }
        when(mr.getAll()).thenAnswer(inv -> metaList);

        TimescaleDBDownsampleJob job = new TimescaleDBDownsampleJob(ds(), new TimescaleDBMetadataService(mr));

        long t0 = System.currentTimeMillis();
        job.run();
        long elapsed = System.currentTimeMillis() - t0;

        ZonedDateTime cutoff = ZonedDateTime.now().minusDays(retainRawDays);
        long rawOldAfter = countRowsByIdsBefore(ids, false, cutoff);
        long rawRecentAfter = countRowsByIdsAfter(ids, false, cutoff);
        long downsampledAfter = countRowsByIds(ids, true);
        logInfo("T-12 downsample phase done: elapsed={} ms rawOldAfter={} rawRecentAfter={} downsampledAfter={}",
                elapsed, rawOldAfter, rawRecentAfter, downsampledAfter);

        assertEquals(0L, rawOldAfter, "T-12 downsample: old raw rows must be removed for configured items");
        assertTrue(downsampledAfter > 0L, "T-12 downsample: aggregated rows must exist after job");
        return elapsed;
    }

    private static long nanosToMillis(long nanos) {
        return nanos / 1_000_000;
    }

    private static void seedRowsServerSideBatch(Connection conn, String phase, boolean downsampled, int[] itemIds,
            Instant start, long rowsPerItem, long stepSeconds, int segmentRows, AtomicLong insertedRows,
            long totalRowsAllItems, long expectedSqlBatches, long startNs, AtomicLong startedExecutions,
            AtomicLong completedExecutions) throws SQLException {
        final String sql = """
                INSERT INTO items (time, item_id, value, unit, downsampled)
                SELECT (?::timestamptz + (gs * ? * interval '1 second')) AS time,
                       i.item_id                                     AS item_id,
                       ((gs + i.item_id) % 10000)::double precision AS value,
                       NULL                                         AS unit,
                   ?                                            AS downsampled
                FROM unnest(?::integer[]) AS i(item_id)
                CROSS JOIN generate_series(?, ?) AS gs
                """;

        long insertedPerItem = 0;
        Integer[] boxedIds = Arrays.stream(itemIds).boxed().toArray(Integer[]::new);
        while (insertedPerItem < rowsPerItem) {
            long from = insertedPerItem;
            long to = Math.min(rowsPerItem - 1, insertedPerItem + segmentRows - 1L);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, java.sql.Timestamp.from(start));
                ps.setLong(2, stepSeconds);
                ps.setBoolean(3, downsampled);
                java.sql.Array itemArray = conn.createArrayOf("integer", boxedIds);
                try {
                    ps.setArray(4, itemArray);
                    ps.setLong(5, from);
                    ps.setLong(6, to);
                    long sqlBatchId = startedExecutions.incrementAndGet();
                    long batchRowsPlanned = (to - from + 1L) * itemIds.length;
                    logInfo("T-12 {} batch start: sqlBatch={}/{} batchItems={} itemRowRange={}-{} batchRowsPlanned={} downsampled={}",
                            phase, sqlBatchId, expectedSqlBatches, itemIds.length, from, to, batchRowsPlanned,
                            downsampled);
                    int insertedNow = ps.executeUpdate();
                    long totalInserted = insertedRows.addAndGet(insertedNow);
                    long completed = completedExecutions.incrementAndGet();
                    long elapsedMs = Math.max(1L, nanosToMillis(System.nanoTime() - startNs));
                    long rate = (totalInserted * 1000L) / elapsedMs;
                    double percent = (totalInserted * 100.0) / totalRowsAllItems;
                    long remaining = Math.max(0L, totalRowsAllItems - totalInserted);
                    logInfo("T-12 {} batch done: sqlBatch={}/{} insertedBatchRows={} createdTotal={}/{} remaining={} progress={}% rate={} rows/s completedSqlBatches={} batchItems={} itemRowRange={}-{} elapsed={} ms downsampled={}",
                            phase, sqlBatchId, expectedSqlBatches, insertedNow, totalInserted, totalRowsAllItems,
                            remaining, String.format("%.2f", percent), rate, completed, itemIds.length, from, to,
                            elapsedMs, downsampled);
                } finally {
                    itemArray.free();
                }
            }
            insertedPerItem = to + 1;
        }
    }

    private static long deleteRowsByIds(int[] ids) throws SQLException {
        final String sql = "DELETE FROM items WHERE item_id = ANY(?::integer[])";
        Integer[] boxedIds = Arrays.stream(ids).boxed().toArray(Integer[]::new);
        try (Connection conn = ds().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.Array itemArray = conn.createArrayOf("integer", boxedIds);
            try {
                ps.setArray(1, itemArray);
                return ps.executeUpdate();
            } finally {
                itemArray.free();
            }
        }
    }

    private static long countRowsByIds(int[] ids, boolean downsampled) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM items WHERE item_id = ANY(?::integer[]) AND downsampled = ?";
        Integer[] boxedIds = Arrays.stream(ids).boxed().toArray(Integer[]::new);
        try (Connection conn = ds().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.Array itemArray = conn.createArrayOf("integer", boxedIds);
            try {
                ps.setArray(1, itemArray);
                ps.setBoolean(2, downsampled);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            } finally {
                itemArray.free();
            }
        }
    }

    private static long countRowsByIdsBefore(int[] ids, boolean downsampled, ZonedDateTime cutoff) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM items WHERE item_id = ANY(?::integer[]) AND downsampled = ? AND time < ?";
        Integer[] boxedIds = Arrays.stream(ids).boxed().toArray(Integer[]::new);
        try (Connection conn = ds().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.Array itemArray = conn.createArrayOf("integer", boxedIds);
            try {
                ps.setArray(1, itemArray);
                ps.setBoolean(2, downsampled);
                ps.setTimestamp(3, java.sql.Timestamp.from(cutoff.toInstant()));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            } finally {
                itemArray.free();
            }
        }
    }

    private static long countRowsByIdsAfter(int[] ids, boolean downsampled, ZonedDateTime cutoff) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM items WHERE item_id = ANY(?::integer[]) AND downsampled = ? AND time >= ?";
        Integer[] boxedIds = Arrays.stream(ids).boxed().toArray(Integer[]::new);
        try (Connection conn = ds().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.Array itemArray = conn.createArrayOf("integer", boxedIds);
            try {
                ps.setArray(1, itemArray);
                ps.setBoolean(2, downsampled);
                ps.setTimestamp(3, java.sql.Timestamp.from(cutoff.toInstant()));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            } finally {
                itemArray.free();
            }
        }
    }

    private static void logInfo(String messagePattern, Object... args) {
        String msg = Objects.requireNonNullElse(MessageFormatter.arrayFormat(messagePattern, args).getMessage(),
                messagePattern);
        LOGGER.info(msg);
    }

    private static List<int[]> partitionIds(int[] ids, int batchSize) {
        List<int[]> batches = new ArrayList<>();
        for (int i = 0; i < ids.length; i += batchSize) {
            int end = Math.min(ids.length, i + batchSize);
            batches.add(Arrays.copyOfRange(ids, i, end));
        }
        return batches;
    }

    private static long theoreticalDownsampledRowsFor18MonthProfile(long days) {
        // Approximation based on documented downsample intervals in PERFORMANCE_TESTS.md.
        long retainedRowsPerDay = (300L * 96L) + (1_200L * 24L) + (1_500L);
        return days * retainedRowsPerDay;
    }

    private static long envLong(String key, long defaultValue) {
        String raw = System.getenv(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean envFlag(String key, boolean defaultValue) {
        String raw = System.getenv(key);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(raw.trim());
    }

    private static void applyBoundedStartupTimeouts(HikariConfig cfg) {
        // Keep startup bounded in case host/port are unreachable.
        cfg.setInitializationFailTimeout(10_000);
        cfg.addDataSourceProperty("connectTimeout", "10");
    }

    private static boolean isNonBlank(@Nullable String value) {
        return value != null && !value.isBlank();
    }

    private static @Nullable String firstNonBlankEnv(String preferred, String fallback) {
        String preferredValue = System.getenv(preferred);
        if (isNonBlank(preferredValue)) {
            return preferredValue;
        }
        String fallbackValue = System.getenv(fallback);
        return isNonBlank(fallbackValue) ? fallbackValue : null;
    }

    private static void sleepMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------------
    // Latency statistics helper
    // ------------------------------------------------------------------

    record Latencies(long p50, long p95, long p99, long max, long min, double avg, int count) {

        static Latencies of(List<Long> values) {
            if (values.isEmpty()) {
                return new Latencies(0, 0, 0, 0, 0, 0, 0);
            }
            Collections.sort(values);
            int n = values.size();
            return new Latencies(values.get((int) (n * 0.50)), values.get((int) (n * 0.95)),
                    values.get((int) (n * 0.99)), values.get(n - 1), values.get(0),
                    values.stream().mapToLong(Long::longValue).average().orElse(0), n);
        }

        void print(String label) {
            logInfo("{} n={}  min={}ms  avg={}ms  p50={}ms  p95={}ms  p99={}ms  max={}ms", label, count, min, avg, p50,
                    p95, p99, max);
        }
    }
}
