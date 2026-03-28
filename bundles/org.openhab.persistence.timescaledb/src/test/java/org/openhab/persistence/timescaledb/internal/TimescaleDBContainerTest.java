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
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Integration tests that run against a real TimescaleDB instance via Testcontainers.
 *
 * <p>
 * These tests verify end-to-end behavior: schema creation, store/query round-trips for
 * all state types, downsampling job execution, and the {@code remove()} operation.
 *
 * <p>
 * Tag: {@code integration} — can be excluded from fast unit-test runs with
 * {@code mvn test -Dgroups='!integration'}.
 *
 * @author René Ulbricht - Initial contribution
 */
@Tag("integration")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })
@SuppressWarnings("null")
class TimescaleDBContainerTest {

    @Container
    static final PostgreSQLContainer DB;

    static {
        var container = new PostgreSQLContainer(
                DockerImageName.parse("timescale/timescaledb:latest-pg16").asCompatibleSubstituteFor("postgres"));
        container.withDatabaseName("openhab_test");
        container.withUsername("openhab");
        container.withPassword("openhab");
        DB = container;
    }

    private static HikariDataSource dataSource;

    @BeforeAll
    static void setUpDataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(DB.getJdbcUrl());
        cfg.setUsername(DB.getUsername());
        cfg.setPassword(DB.getPassword());
        cfg.setMaximumPoolSize(3);
        dataSource = new HikariDataSource(cfg);
    }

    @BeforeEach
    void initSchema() throws SQLException {
        // Drop and recreate for test isolation
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
        }
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }
    }

    // ------------------------------------------------------------------
    // Schema
    // ------------------------------------------------------------------

    @Test
    @Order(1)
    void schemaHypertableexists() throws SQLException {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT hypertable_name FROM timescaledb_information.hypertables "
                                + "WHERE hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Hypertable 'items' should exist");
        }
    }

    @Test
    @Order(2)
    void schemaItemmetatableexists() throws SQLException {
        try (Connection conn = dataSource.getConnection();
                var stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM item_meta")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1), "item_meta should be empty after init");
        }
    }

    // ------------------------------------------------------------------
    // Store / query round-trips — all state types
    // ------------------------------------------------------------------

    @Test
    @Order(10)
    void roundTripDecimaltype() throws SQLException {
        storeAndVerify("DecimalSensor", new NumberItem("DecimalSensor"), new DecimalType(42.5),
                s -> assertInstanceOf(DecimalType.class, s),
                s -> assertEquals(42.5, ((DecimalType) s).doubleValue(), 1e-6));
    }

    @Test
    @Order(11)
    void roundTripQuantitytypeTemperature() throws SQLException {
        storeAndVerify("TempSensor", new NumberItem("TempSensor"), new QuantityType<>("23.4 °C"),
                s -> assertInstanceOf(QuantityType.class, s), s -> assertEquals("23.4 °C", s.toString()));
    }

    @Test
    @Order(12)
    void roundTripOnofftype() throws SQLException {
        storeAndVerify("Switch1", new SwitchItem("Switch1"), OnOffType.ON, s -> assertEquals(OnOffType.ON, s));
        storeAndVerify("Switch2", new SwitchItem("Switch2"), OnOffType.OFF, s -> assertEquals(OnOffType.OFF, s));
    }

    @Test
    @Order(13)
    void roundTripOpenclosedtype() throws SQLException {
        storeAndVerify("Contact1", new ContactItem("Contact1"), OpenClosedType.OPEN,
                s -> assertEquals(OpenClosedType.OPEN, s));
    }

    @Test
    @Order(14)
    void roundTripPercenttype() throws SQLException {
        storeAndVerify("Dimmer1", new DimmerItem("Dimmer1"), new PercentType(75),
                s -> assertInstanceOf(PercentType.class, s), s -> assertEquals(75, ((PercentType) s).intValue()));
    }

    @Test
    @Order(15)
    void roundTripHsbtype() throws SQLException {
        storeAndVerify("Color1", new ColorItem("Color1"), new HSBType("120,50,80"),
                s -> assertInstanceOf(HSBType.class, s), s -> assertEquals("120,50,80", s.toString()));
    }

    @Test
    @Order(16)
    void roundTripDatetimetype() throws SQLException {
        ZonedDateTime dt = ZonedDateTime.parse("2024-06-01T10:00:00+02:00");
        storeAndVerify("DateTime1", new DateTimeItem("DateTime1"), new DateTimeType(dt),
                s -> assertInstanceOf(DateTimeType.class, s));
    }

    @Test
    @Order(17)
    void roundTripStringtype() throws SQLException {
        storeAndVerify("String1", new StringItem("String1"), new StringType("hello world"),
                s -> assertEquals("hello world", s.toString()));
    }

    @Test
    @Order(18)
    void roundTripPointtypeLocationitem() throws SQLException {
        storeAndVerify("Location1", new LocationItem("Location1"), new PointType("52.5200,13.4050,34.0000"),
                s -> assertInstanceOf(PointType.class, s), s -> {
                    PointType pt = (PointType) s;
                    assertEquals(52.52, pt.getLatitude().doubleValue(), 1e-3);
                    assertEquals(13.405, pt.getLongitude().doubleValue(), 1e-3);
                });
    }

    @Test
    @Order(19)
    void roundTripPlaypausetypePlayeritem() throws SQLException {
        storeAndVerify("Player1", new PlayerItem("Player1"), PlayPauseType.PLAY,
                s -> assertEquals(PlayPauseType.PLAY, s));
        storeAndVerify("Player2", new PlayerItem("Player2"), PlayPauseType.PAUSE,
                s -> assertEquals(PlayPauseType.PAUSE, s));
    }

    @Test
    @Order(20)
    void roundTripStringlisttypeCallitem() throws SQLException {
        storeAndVerify("Call1", new CallItem("Call1"), new StringListType("Alice", "Bob"),
                s -> assertInstanceOf(StringListType.class, s),
                s -> assertEquals(new StringListType("Alice", "Bob").toString(), s.toString()));
    }

    @Test
    @Order(21)
    void roundTripRawtypeImageitem() throws SQLException {
        byte[] bytes = { (byte) 0x89, 0x50, 0x4E, 0x47 }; // PNG magic bytes
        storeAndVerify("Image1", new ImageItem("Image1"), new RawType(bytes, "image/png"),
                s -> assertInstanceOf(RawType.class, s), s -> {
                    RawType raw = (RawType) s;
                    assertEquals("image/png", raw.getMimeType());
                    assertArrayEquals(bytes, raw.getBytes());
                });
    }

    @Test
    @Order(22)
    void roundTripUpdowntypeRollershutteritem() throws SQLException {
        storeAndVerify("Roller2", new RollershutterItem("Roller2"), new PercentType(30),
                s -> assertInstanceOf(PercentType.class, s), s -> assertEquals(30, ((PercentType) s).intValue()));
    }

    @Test
    @Order(23)
    void roundTripGroupitemWithnumberbase() throws SQLException {
        var baseItem = new NumberItem("GBase");
        var groupItem = new GroupItem("Group1", baseItem);
        storeAndVerify("Group1", groupItem, new DecimalType(55.5), s -> assertInstanceOf(DecimalType.class, s),
                s -> assertEquals(55.5, ((DecimalType) s).doubleValue(), 1e-6));
    }

    @Test
    @Order(24)
    void roundTripGroupitemWithswitchbase() throws SQLException {
        var baseItem = new SwitchItem("GBase");
        var groupItem = new GroupItem("GroupSwitch1", baseItem);
        storeAndVerify("GroupSwitch1", groupItem, OnOffType.ON, s -> assertEquals(OnOffType.ON, s));
    }

    @Test
    @Order(25)
    void roundTripGroupitemWithcolorbase() throws SQLException {
        var baseItem = new ColorItem("GBase");
        var groupItem = new GroupItem("GroupColor1", baseItem);
        storeAndVerify("GroupColor1", groupItem, new HSBType("240,100,50"), s -> assertInstanceOf(HSBType.class, s),
                s -> assertEquals("240,100,50", s.toString()));
    }

    // ------------------------------------------------------------------
    // Query — filtering and ordering
    // ------------------------------------------------------------------

    @Test
    @Order(31)
    void queryDaterangeReturnsonlyrowsinrange() throws SQLException {
        ZonedDateTime t1 = ZonedDateTime.now().minusDays(10);
        ZonedDateTime t2 = ZonedDateTime.now().minusDays(5);
        ZonedDateTime t3 = ZonedDateTime.now().minusDays(1);
        NumberItem item = new NumberItem("RangeSensor");

        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "RangeSensor", null);
            TimescaleDBQuery.insert(conn, id, t1, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, t2, new TimescaleDBMapper.Row(2.0, null, null));
            TimescaleDBQuery.insert(conn, id, t3, new TimescaleDBMapper.Row(3.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("RangeSensor");
            filter.setBeginDate(ZonedDateTime.now().minusDays(7));
            filter.setEndDate(ZonedDateTime.now().minusDays(2));

            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(1, results.size(), "Only t2 should be in range");
            assertEquals(2.0, ((DecimalType) results.get(0).getState()).doubleValue(), 1e-6);
        }
    }

    @Test
    @Order(32)
    void queryOrderingAscending() throws SQLException {
        NumberItem item = new NumberItem("OrderSensor");
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "OrderSensor", null);
            ZonedDateTime base = ZonedDateTime.now().minusHours(3);
            TimescaleDBQuery.insert(conn, id, base.plusHours(2), new TimescaleDBMapper.Row(3.0, null, null));
            TimescaleDBQuery.insert(conn, id, base, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, base.plusHours(1), new TimescaleDBMapper.Row(2.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("OrderSensor");
            filter.setOrdering(Ordering.ASCENDING);

            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(3, results.size());
            assertEquals(1.0, ((DecimalType) results.get(0).getState()).doubleValue(), 1e-6);
            assertEquals(3.0, ((DecimalType) results.get(2).getState()).doubleValue(), 1e-6);
        }
    }

    @Test
    @Order(33)
    void queryPaginationLimitsresults() throws SQLException {
        NumberItem item = new NumberItem("PageSensor");
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "PageSensor", null);
            ZonedDateTime base = ZonedDateTime.now().minusHours(10);
            for (int i = 0; i < 10; i++) {
                TimescaleDBQuery.insert(conn, id, base.plusHours(i), new TimescaleDBMapper.Row((double) i, null, null));
            }

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("PageSensor");
            filter.setOrdering(Ordering.ASCENDING);
            filter.setPageSize(3);
            filter.setPageNumber(0);

            List<HistoricItem> page0 = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(3, page0.size());

            filter.setPageNumber(1);
            List<HistoricItem> page1 = TimescaleDBQuery.query(conn, item, id, filter);
            assertEquals(3, page1.size());

            // Pages should not overlap
            assertNotEquals(page0.get(0).getTimestamp(), page1.get(0).getTimestamp());
        }
    }

    // ------------------------------------------------------------------
    // Duplicate timestamp handling
    // ------------------------------------------------------------------

    @Test
    @Order(29)
    void insertDuplicatetimestampIssilentlyIgnored() throws SQLException {
        NumberItem item = new NumberItem("DupSensor");
        ZonedDateTime ts = ZonedDateTime.now().minusHours(1);

        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "DupSensor", null);

            TimescaleDBQuery.insert(conn, id, ts, new TimescaleDBMapper.Row(1.0, null, null));
            // Second write for the same (time, item_id) — must be silently ignored
            TimescaleDBQuery.insert(conn, id, ts, new TimescaleDBMapper.Row(99.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("DupSensor");
            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);

            assertEquals(1, results.size(), "Duplicate timestamp must be silently ignored, only one row stored");
            assertEquals(1.0, ((DecimalType) results.get(0).getState()).doubleValue(), 1e-6,
                    "First value must be kept, the duplicate discarded");
        }
    }

    // ------------------------------------------------------------------
    // remove
    // ------------------------------------------------------------------

    @Test
    @Order(30)
    void removeDeletesbydaterange() throws SQLException {
        NumberItem item = new NumberItem("DeleteSensor");
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "DeleteSensor", null);
            ZonedDateTime base = ZonedDateTime.now().minusDays(5);
            TimescaleDBQuery.insert(conn, id, base, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, base.plusDays(1), new TimescaleDBMapper.Row(2.0, null, null));
            TimescaleDBQuery.insert(conn, id, base.plusDays(4), new TimescaleDBMapper.Row(3.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("DeleteSensor");
            filter.setBeginDate(base.minusMinutes(1));
            filter.setEndDate(base.plusDays(2));

            int deleted = TimescaleDBQuery.remove(conn, id, filter);
            assertEquals(2, deleted, "Should delete the first two rows");

            FilterCriteria allFilter = new FilterCriteria();
            allFilter.setItemName("DeleteSensor");
            List<HistoricItem> remaining = TimescaleDBQuery.query(conn, item, id, allFilter);
            assertEquals(1, remaining.size(), "One row should remain");
            assertEquals(3.0, ((DecimalType) remaining.get(0).getState()).doubleValue(), 1e-6);
        }
    }

    // ------------------------------------------------------------------
    // item_id caching
    // ------------------------------------------------------------------

    @Test
    @Order(40)
    void itemIdCacheSamenamereturnssameid() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            int id1 = TimescaleDBQuery.getOrCreateItemId(conn, "CacheSensor", "label");
            int id2 = TimescaleDBQuery.getOrCreateItemId(conn, "CacheSensor", "label");
            assertEquals(id1, id2, "Same name should always return the same item_id");
        }
    }

    // ------------------------------------------------------------------
    // Duplicate / constraint behaviour
    // ------------------------------------------------------------------

    @Test
    @Order(45)
    void insertDuplicateRawrowIsSilentlydroppedNoerror() throws SQLException {
        // Insert the same (time, item_id) twice as raw rows.
        // ON CONFLICT DO NOTHING must swallow the conflict silently — no exception.
        ZonedDateTime ts = ZonedDateTime.now().minusHours(1);
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "DupSensor", null);
            TimescaleDBQuery.insert(conn, id, ts, new TimescaleDBMapper.Row(1.0, null, null));
            assertDoesNotThrow(() -> TimescaleDBQuery.insert(conn, id, ts, new TimescaleDBMapper.Row(2.0, null, null)),
                    "Second insert with same (time, item_id) must not throw — ON CONFLICT DO NOTHING");
        }

        // Only one row must exist (the first write wins)
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*), value FROM items WHERE item_id = (SELECT id FROM item_meta WHERE name = 'DupSensor') GROUP BY value");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "At least one row must exist");
            assertEquals(1, rs.getInt(1), "Exactly one raw row must be stored despite two inserts");
            assertEquals(1.0, rs.getDouble(2), 1e-6, "First write must win");
            assertFalse(rs.next(), "No second row must exist");
        }
    }

    @Test
    @Order(46)
    void downsampleRowAtBucketBoundaryCoexistsWithRawrow() throws SQLException {
        // Regression test for: downsampled row at exact bucket-boundary timestamp used to
        // conflict with the raw row at the same timestamp because the UNIQUE constraint was
        // only on (time, item_id). With UNIQUE(time, item_id, downsampled) both rows can
        // coexist (one with downsampled=FALSE, one with downsampled=TRUE).
        //
        // Setup: one raw row exactly at the 2h bucket boundary (e.g. 02:00:00), plus one
        // raw row inside the same bucket (02:30:00). The downsampling job must produce one
        // downsampled row at 02:00:00 WITHOUT silently dropping it due to the raw row there.

        ZonedDateTime bucketBoundary = ZonedDateTime.now().minusDays(2).withHour(2).withMinute(0).withSecond(0)
                .withNano(0);
        ZonedDateTime insideBucket = bucketBoundary.plusMinutes(30);

        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "BoundarySensor", null);
            // raw row exactly on the bucket boundary
            TimescaleDBQuery.insert(conn, id, bucketBoundary, new TimescaleDBMapper.Row(10.0, null, null));
            // raw row inside the bucket
            TimescaleDBQuery.insert(conn, id, insideBucket, new TimescaleDBMapper.Row(20.0, null, null));
        }

        MetadataRegistry mr = mock(MetadataRegistry.class);
        Metadata meta = new Metadata(new MetadataKey("timescaledb", "BoundarySensor"), "sensor.boundary",
                Map.of("aggregation", "AVG", "downsampleInterval", "2h", "retainRawDays", "0"));
        when(mr.get(new MetadataKey("timescaledb", "BoundarySensor"))).thenReturn(meta);
        when(mr.getAll()).thenAnswer(inv -> List.of(meta));

        int[] itemId = new int[1];
        try (Connection conn = dataSource.getConnection()) {
            itemId[0] = TimescaleDBQuery.getOrCreateItemId(conn, "BoundarySensor", null);
        }

        new TimescaleDBDownsampleJob(dataSource, new TimescaleDBMetadataService(mr)).run();

        try (Connection conn = dataSource.getConnection()) {
            // The downsampled row at the bucket boundary must exist (avg of 10.0 and 20.0 = 15.0)
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT value FROM items WHERE item_id = ? AND downsampled = TRUE")) {
                ps.setInt(1, itemId[0]);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Downsampled row must exist even when a raw row sits on the bucket boundary");
                    assertEquals(15.0, rs.getDouble(1), 1e-6,
                            "Downsampled value must be the average of both raw rows (10 + 20) / 2 = 15");
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Downsampling job (integration)
    // ------------------------------------------------------------------

    @Test
    @Order(50)
    void downsampleJobAggregatesrawrowsanddeletesthem() throws SQLException {
        // Seed 6 raw rows: 3 buckets of 2h each, all older than retainRawDays=0 (use 0 for test)
        // We use retainRawDays=0 so the job processes all rows immediately (NOW() - 0 days)
        // Base is set to :30 past midnight so no raw row lands on a 2h bucket boundary — if a raw row
        // shared the exact bucket-boundary timestamp the ON CONFLICT DO NOTHING on the aggregated INSERT
        // would silently discard the downsampled row (correct behaviour in production, but not what we
        // want to assert here).
        ZonedDateTime base = ZonedDateTime.now().minusDays(1).withHour(0).withMinute(30).withSecond(0).withNano(0);
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "DownsampleSensor", null);

            // 3 pairs, each pair falls in a different 2h bucket (00:30/01:30, 02:30/03:30, 04:30/05:30)
            for (int bucket = 0; bucket < 3; bucket++) {
                for (int offset = 0; offset < 2; offset++) {
                    TimescaleDBQuery.insert(conn, id, base.plusHours(bucket * 2L + offset),
                            new TimescaleDBMapper.Row((double) (bucket * 10 + offset), null, null));
                }
            }
        }

        // Configure metadata for downsampling with retainRawDays=0
        MetadataRegistry metadataRegistry = mock(MetadataRegistry.class);
        Metadata meta = new Metadata(new MetadataKey("timescaledb", "DownsampleSensor"), "sensor.downsample",
                Map.of("aggregation", "AVG", "downsampleInterval", "2h", "retainRawDays", "0"));
        when(metadataRegistry.get(new MetadataKey("timescaledb", "DownsampleSensor"))).thenReturn(meta);
        when(metadataRegistry.getAll()).thenAnswer(inv -> List.of(meta));

        TimescaleDBMetadataService metaService = new TimescaleDBMetadataService(metadataRegistry);

        // Resolve item_id for the job
        int[] storedId = new int[1];
        try (Connection conn = dataSource.getConnection()) {
            storedId[0] = TimescaleDBQuery.getOrCreateItemId(conn, "DownsampleSensor", null);
        }

        TimescaleDBDownsampleJob job = new TimescaleDBDownsampleJob(dataSource, metaService);
        job.run();

        // Verify: raw rows gone, aggregated rows present
        try (Connection conn = dataSource.getConnection()) {
            int id = storedId[0];

            // No raw rows should remain (retainRawDays=0 means all raw rows are eligible)
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ? AND downsampled = FALSE")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(0, rs.getInt(1), "No raw rows should remain after downsampling");
                }
            }

            // 3 aggregated rows should exist (one per 2h bucket)
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ? AND downsampled = TRUE")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(3, rs.getInt(1), "3 aggregated rows should exist");
                }
            }
        }
    }

    @Test
    @Order(51)
    void downsampleJobRetentiondaysDropsolddata() throws SQLException {
        ZonedDateTime old = ZonedDateTime.now().minusDays(60);
        ZonedDateTime recent = ZonedDateTime.now().minusHours(1);

        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "RetentionSensor", null);
            TimescaleDBQuery.insert(conn, id, old, new TimescaleDBMapper.Row(1.0, null, null));
            TimescaleDBQuery.insert(conn, id, recent, new TimescaleDBMapper.Row(2.0, null, null));
        }

        MetadataRegistry mr = mock(MetadataRegistry.class);
        // retentionDays=30 → the 60-day-old row should be deleted
        Metadata meta = new Metadata(new MetadataKey("timescaledb", "RetentionSensor"), "sensor.retention",
                Map.of("aggregation", "AVG", "downsampleInterval", "1h", "retainRawDays", "0", "retentionDays", "30"));
        when(mr.get(new MetadataKey("timescaledb", "RetentionSensor"))).thenReturn(meta);
        when(mr.getAll()).thenAnswer(inv -> List.of(meta));

        TimescaleDBMetadataService ms = new TimescaleDBMetadataService(mr);

        int[] retentionId = new int[1];
        try (Connection conn = dataSource.getConnection()) {
            retentionId[0] = TimescaleDBQuery.getOrCreateItemId(conn, "RetentionSensor", null);
        }

        new TimescaleDBDownsampleJob(dataSource, ms).run();

        try (Connection conn = dataSource.getConnection()) {
            FilterCriteria all = new FilterCriteria();
            all.setItemName("RetentionSensor");
            List<HistoricItem> remaining = TimescaleDBQuery.query(conn, new NumberItem("RetentionSensor"),
                    retentionId[0], all);
            // Only the recent row should survive (as a downsampled row)
            assertEquals(1, remaining.size(), "Old data should have been dropped by retention policy");
        }
    }

    // ------------------------------------------------------------------
    // Compression / Retention policies (schema level)
    // ------------------------------------------------------------------

    @Test
    @Order(60)
    void schemaCompressionpolicyIsregistered() throws SQLException {
        // Re-init schema with compression enabled
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
        }
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 30, 0);
        }

        // Verify compression is enabled on the hypertable
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT compression_enabled FROM timescaledb_information.hypertables "
                                + "WHERE hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "Hypertable 'items' should exist");
            assertTrue(rs.getBoolean(1), "Compression should be enabled on the hypertable");
        }
        // Verify a background policy job was registered (table was freshly recreated, so any job is the compression
        // job)
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM timescaledb_information.jobs WHERE hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0, "A compression policy job should be registered");
        }
    }

    @Test
    @Order(61)
    void schemaRetentionpolicyIsregistered() throws SQLException {
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
        }
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 365);
        }

        // Verify a background policy job was registered (table was freshly recreated, so any job is the retention job)
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM timescaledb_information.jobs WHERE hypertable_name = 'items'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) > 0, "A retention policy job should be registered");
        }
    }

    // ------------------------------------------------------------------
    // TimescaleDBPersistenceService — full lifecycle via real DB
    // ------------------------------------------------------------------

    @Test
    @Order(70)
    void serviceActivateInitializesschemaandschedulesjob() throws Exception {
        MetadataRegistry mr = mock(MetadataRegistry.class);
        when(mr.getAll()).thenReturn(Collections.emptyList());
        TimescaleDBPersistenceService service = new TimescaleDBPersistenceService(mock(ItemRegistry.class), mr,
                new TimescaleDBMetadataService(mr));

        service.activate(Map.of("url", DB.getJdbcUrl(), "user", DB.getUsername(), "password", DB.getPassword()));

        var dsField = TimescaleDBPersistenceService.class.getDeclaredField("dataSource");
        dsField.setAccessible(true);
        assertNotNull(dsField.get(service), "DataSource must be initialized after activate()");

        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJob");
        jobField.setAccessible(true);
        assertNotNull(jobField.get(service), "Downsampling job must be scheduled after activate()");

        service.deactivate();
        assertNull(dsField.get(service), "DataSource must be null after deactivate()");
        assertNull(jobField.get(service), "Downsampling job must be null after deactivate()");
    }

    @Test
    @Order(71)
    void serviceStoreandqueryViaserviceinterface() throws Exception {
        MetadataRegistry mr = mock(MetadataRegistry.class);
        when(mr.getAll()).thenReturn(Collections.emptyList());
        ItemRegistry ir = mock(ItemRegistry.class);
        NumberItem item = new NumberItem("ServiceSensor");
        when(ir.getItem("ServiceSensor")).thenReturn(item);

        TimescaleDBPersistenceService service = new TimescaleDBPersistenceService(ir, mr,
                new TimescaleDBMetadataService(mr));
        service.activate(Map.of("url", DB.getJdbcUrl(), "user", DB.getUsername(), "password", DB.getPassword()));

        try {
            service.store(item, ZonedDateTime.now(), new DecimalType(77.7), null);

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("ServiceSensor");
            List<org.openhab.core.persistence.HistoricItem> results = StreamSupport
                    .stream(service.query(filter).spliterator(), false).toList();

            assertFalse(results.isEmpty(), "Service.query() must return the stored value");
            assertEquals(77.7, ((DecimalType) results.get(0).getState()).doubleValue(), 1e-6);

            assertTrue(service.remove(filter), "Service.remove() must return true for known item");
        } finally {
            service.deactivate();
        }
    }

    @Test
    @Order(72)
    @SuppressWarnings("unchecked")
    void serviceDeactivateCancelsscheduledfuture() throws Exception {
        MetadataRegistry mr = mock(MetadataRegistry.class);
        when(mr.getAll()).thenReturn(Collections.emptyList());
        TimescaleDBPersistenceService service = new TimescaleDBPersistenceService(mock(ItemRegistry.class), mr,
                new TimescaleDBMetadataService(mr));
        service.activate(Map.of("url", DB.getJdbcUrl(), "user", DB.getUsername(), "password", DB.getPassword()));

        var jobField = TimescaleDBPersistenceService.class.getDeclaredField("downsampleJob");
        jobField.setAccessible(true);
        ScheduledFuture<Object> job = (ScheduledFuture<Object>) jobField.get(service);
        assertNotNull(job);
        assertFalse(job.isCancelled(), "Job must not be cancelled before deactivate()");

        service.deactivate();
        assertTrue(job.isCancelled(), "Job must be cancelled after deactivate()");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // item_meta.value + metadata JSONB (integration)
    // ------------------------------------------------------------------

    @Test
    @Order(80)
    void valueStringIsStoredInItemMetaValueColumn() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBQuery.getOrCreateItemId(conn, "ValueSensor", "label", "sensor.temperature", null);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT value FROM item_meta WHERE name = 'ValueSensor'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals("sensor.temperature", rs.getString(1));
        }
    }

    @Test
    @Order(81)
    void valueStringIsUpdatedOnUpsert() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBQuery.getOrCreateItemId(conn, "UpdatableSensor", "label", "old.value", null);
            TimescaleDBQuery.getOrCreateItemId(conn, "UpdatableSensor", "label", "new.value", null);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT value FROM item_meta WHERE name = 'UpdatableSensor'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals("new.value", rs.getString(1));
        }
    }

    @Test
    @Order(82)
    void nullValueAndMetadataStoreNulls() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBQuery.getOrCreateItemId(conn, "NoMetaSensor", null);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT value, metadata FROM item_meta WHERE name = 'NoMetaSensor'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertNull(rs.getString(1), "value must be NULL when not provided");
            assertNull(rs.getString(2), "metadata must be NULL when not provided");
        }
    }

    @Test
    @Order(83)
    void metadataJsonbIsStoredAndQueryableViaJsonbOperators() throws SQLException {
        String json = "{\"aggregation\":\"AVG\",\"location\":\"kitchen\"}";
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBQuery.getOrCreateItemId(conn, "JsonSensor", null, "sensor.temp", json);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT name FROM item_meta WHERE metadata->>'location' = 'kitchen'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "JSONB operator ->> must work for filtering");
            assertEquals("JsonSensor", rs.getString(1));
        }
    }

    @Test
    @Order(84)
    void schemaMigrationAddsValueTextAndMetadataJsonbToProductionSchema() throws SQLException {
        // Simulate the real production schema: item_meta without value/metadata columns
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
            stmt.execute("CREATE TABLE item_meta (id SERIAL PRIMARY KEY, name TEXT NOT NULL UNIQUE, "
                    + "label TEXT, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW())");
        }

        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT column_name, data_type " + "FROM information_schema.columns "
                                + "WHERE table_name = 'item_meta' AND column_name IN ('value', 'metadata') "
                                + "ORDER BY column_name");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "metadata column must have been added");
            assertEquals("metadata", rs.getString("column_name"));
            assertEquals("jsonb", rs.getString("data_type"), "metadata must be JSONB");
            assertTrue(rs.next(), "value column must have been added");
            assertEquals("value", rs.getString("column_name"));
            assertEquals("text", rs.getString("data_type"), "value must be TEXT");
        }
    }

    @Test
    @Order(85)
    void schemaMigrationDoesNotBlockWhenAnotherTransactionLocksItemMeta() throws Exception {
        // Simulate an existing installation with old metadata TEXT column
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS items CASCADE");
            stmt.execute("DROP TABLE IF EXISTS item_meta CASCADE");
            stmt.execute("CREATE TABLE item_meta (id SERIAL PRIMARY KEY, name TEXT NOT NULL UNIQUE, "
                    + "label TEXT, metadata TEXT, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW())");
        }

        // Open a transaction that holds ACCESS SHARE lock on item_meta — simulates a long-running query
        Connection blockingConn = dataSource.getConnection();
        blockingConn.setAutoCommit(false);
        try (var stmt = blockingConn.createStatement()) {
            stmt.execute("SELECT * FROM item_meta FOR SHARE");
        }

        // Migration must complete within a few seconds despite the blocking transaction.
        // The lock_timeout in the DO-blocks means it gives up quickly and logs a warning
        // instead of blocking the service start indefinitely.
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        } finally {
            blockingConn.rollback();
            blockingConn.close();
        }
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed < 15_000,
                "Schema initialization must not block indefinitely when item_meta is locked — took " + elapsed + "ms");
    }

    // ------------------------------------------------------------------
    // Migration end-to-end: existing data must survive schema changes
    // ------------------------------------------------------------------

    /**
     * Simulates the production upgrade path from the earliest schema
     * (item_meta with only id/name/label/created_at, no value/metadata columns)
     * to the current schema. Existing rows in item_meta and their linked
     * items entries must survive the migration intact.
     */
    @Test
    @Order(86)
    void schemaMigrationPreservesExistingRowsWhenUpgradingFromOriginalSchema() throws SQLException {
        // @BeforeEach already created the full current schema — insert real data first,
        // then strip the new columns to simulate a pre-migration production state.
        int existingId;
        try (Connection conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            // Insert an existing item via the current API
            existingId = TimescaleDBQuery.getOrCreateItemId(conn, "LegacySensor", "Living Room Temp");

            // Insert a measurement for that item
            TimescaleDBQuery.insert(conn, existingId, ZonedDateTime.now().minusHours(1),
                    new TimescaleDBMapper.Row(21.5, null, null));

            // Now simulate the old schema: drop the columns that did not exist originally
            stmt.execute("ALTER TABLE item_meta DROP COLUMN IF EXISTS value");
            stmt.execute("ALTER TABLE item_meta DROP COLUMN IF EXISTS metadata");
        }

        // Run migration — must succeed without error
        try (Connection conn = dataSource.getConnection()) {
            TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
        }

        // Verify: item_meta row survived with correct name and label
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT id, name, label FROM item_meta WHERE name = 'LegacySensor'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next(), "item_meta row for LegacySensor must survive migration");
            assertEquals(existingId, rs.getInt("id"), "item_meta.id must not change during migration");
            assertEquals("Living Room Temp", rs.getString("label"));
        }

        // Verify: items measurement still referenced by its original item_meta.id
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ?")) {
            ps.setInt(1, existingId);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "items row must survive migration — FK reference must remain valid");
        }

        // Verify: new columns were added with NULL for old rows
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT value, metadata FROM item_meta WHERE name = 'LegacySensor'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertNull(rs.getString("value"), "value must be NULL for rows that predate the migration");
            assertNull(rs.getString("metadata"), "metadata must be NULL for rows that predate the migration");
        }

        // Verify: service is fully operational after migration — new store + query round-trip works
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, "LegacySensor", "Living Room Temp");
            assertEquals(existingId, id, "getOrCreateItemId must return the existing row, not create a duplicate");

            TimescaleDBQuery.insert(conn, id, ZonedDateTime.now(), new TimescaleDBMapper.Row(22.0, null, null));

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName("LegacySensor");
            NumberItem item = new NumberItem("LegacySensor");
            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);
            assertFalse(results.isEmpty(), "Query must return results after migration");
        }
    }

    /**
     * initialize() must be idempotent: running it a second time on an already-migrated schema
     * must not throw, not duplicate rows, and not corrupt existing data.
     */
    @Test
    @Order(87)
    void schemaInitializeIsIdempotent() throws SQLException {
        // @BeforeEach already ran initialize() once — insert data
        int existingId;
        try (Connection conn = dataSource.getConnection()) {
            existingId = TimescaleDBQuery.getOrCreateItemId(conn, "IdempotentSensor", "Idempotency Test", "sensor.test",
                    null);
            TimescaleDBQuery.insert(conn, existingId, ZonedDateTime.now().minusMinutes(5),
                    new TimescaleDBMapper.Row(42.0, null, null));
        }

        // Run initialize() a second time — must not throw
        assertDoesNotThrow(() -> {
            try (Connection conn = dataSource.getConnection()) {
                TimescaleDBSchema.initialize(conn, "7 days", 0, 0);
            }
        }, "initialize() must be idempotent — running it twice must not throw");

        // item_meta row must not be duplicated
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT COUNT(*) FROM item_meta WHERE name = 'IdempotentSensor'");
                ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "Second initialize() must not duplicate item_meta rows");
        }

        // items data must still be intact
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM items WHERE item_id = ?")) {
            ps.setInt(1, existingId);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "items data must survive second initialize() call");
        }
    }

    @SafeVarargs
    private void storeAndVerify(String itemName, org.openhab.core.items.Item item, State state,
            java.util.function.Consumer<State>... assertions) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            int id = TimescaleDBQuery.getOrCreateItemId(conn, itemName, null);
            TimescaleDBMapper.Row row = TimescaleDBMapper.toRow(state);
            assertNotNull(row, "Mapper must produce a row for state type: " + state.getClass().getSimpleName());
            TimescaleDBQuery.insert(conn, id, ZonedDateTime.now(), row);

            FilterCriteria filter = new FilterCriteria();
            filter.setItemName(itemName);
            List<HistoricItem> results = TimescaleDBQuery.query(conn, item, id, filter);

            assertFalse(results.isEmpty(), "Query should return at least one result for item " + itemName);
            State loadedState = results.get(0).getState();
            for (var assertion : assertions) {
                assertion.accept(loadedState);
            }
        }
    }
}
